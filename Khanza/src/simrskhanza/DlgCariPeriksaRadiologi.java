package simrskhanza;
import bridging.ApiOrthanc;
import bridging.OrthancDICOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.MetadataBerkas;
import kepegawaian.DlgCariPetugas;
import keuangan.Jurnal;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
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
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import rekammedis.MasterCariTemplateHasilRadiologi;
import rekammedis.RMRiwayatPerawatan;
import wa.HasilPenunjangWhatsapp;

public class DlgCariPeriksaRadiologi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModeDicom;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Jurnal jur=new Jurnal();
    private Connection koneksi=koneksiDB.condb();
    private DlgCariPasien member=new DlgCariPasien(null,false);
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private DlgCariPetugas petugas=new DlgCariPetugas(null,false);
    private MasterCariTemplateHasilRadiologi templatehasil=new MasterCariTemplateHasilRadiologi(null,false);
    private boolean sukses=false;
    private JsonNode root;
    private final ObjectMapper orthancMapper=new ObjectMapper();
    private boolean tabelAccessionOrthancSiap=false;
    private String kunciPerbaikanAccession="";
    private String accessionPerluPerbaikan="";
    private int i;
    private StringBuilder htmlContent;
    private widget.Button BtnKirimHasilRad;
    private PreparedStatement ps,ps2,ps3,ps4,ps5,psrekening,pscetakradiologi1;
    private ResultSet rs,rs2,rs3,rs5,rsrekening,rscetakradiologi1;
    private String kamar,namakamar,pemeriksaan="",pilihan="",status="",finger="";
    private double ttl=0,item=0;
    private double ttljmdokter=0,ttljmpetugas=0,ttlkso=0,ttlpendapatan=0,ttlbhp=0,ttljasasarana=0,ttljmperujuk=0,ttlmenejemen=0;;
    private String kdpetugas="",kdpenjab="",Suspen_Piutang_Radiologi_Ranap="",Radiologi_Ranap="",Beban_Jasa_Medik_Dokter_Radiologi_Ranap="",Utang_Jasa_Medik_Dokter_Radiologi_Ranap="",
            Beban_Jasa_Medik_Petugas_Radiologi_Ranap="",Utang_Jasa_Medik_Petugas_Radiologi_Ranap="",Beban_Kso_Radiologi_Ranap="",Utang_Kso_Radiologi_Ranap="",
            HPP_Persediaan_Radiologi_Rawat_Inap="",Persediaan_BHP_Radiologi_Rawat_Inap="",Beban_Jasa_Sarana_Radiologi_Ranap="",Utang_Jasa_Sarana_Radiologi_Ranap="",
            Beban_Jasa_Perujuk_Radiologi_Ranap="",Utang_Jasa_Perujuk_Radiologi_Ranap="",Beban_Jasa_Menejemen_Radiologi_Ranap="",Utang_Jasa_Menejemen_Radiologi_Ranap="",
            Suspen_Piutang_Radiologi_Ralan="",Radiologi_Ralan="",Beban_Jasa_Medik_Dokter_Radiologi_Ralan="",Utang_Jasa_Medik_Dokter_Radiologi_Ralan="",
            Beban_Jasa_Medik_Petugas_Radiologi_Ralan="",Utang_Jasa_Medik_Petugas_Radiologi_Ralan="",Beban_Kso_Radiologi_Ralan="",Utang_Kso_Radiologi_Ralan="",
            HPP_Persediaan_Radiologi_Rawat_Jalan="",Persediaan_BHP_Radiologi_Rawat_Jalan="",Beban_Jasa_Sarana_Radiologi_Ralan="",Utang_Jasa_Sarana_Radiologi_Ralan="",
            Beban_Jasa_Perujuk_Radiologi_Ralan="",Utang_Jasa_Perujuk_Radiologi_Ralan="",Beban_Jasa_Menejemen_Radiologi_Ralan="",Utang_Jasa_Menejemen_Radiologi_Ralan="",kodeberkas="",FileName="";

    /** Creates new form DlgProgramStudi
     * @param parent
     * @param modal */
    public DlgCariPeriksaRadiologi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        pastikanTabelAccessionOrthanc();

        Object[] row={"No.Rawat","Pasien","Petugas","Tgl.Periksa","Jam Periksa","Dokter Perujuk","Penanggung Jawab","Tanggal Lahir","Diagnosa Klinis","No. SEP"};
        tabMode=new DefaultTableModel(null,row){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokter.setModel(tabMode);

        tbDokter.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 10; i++) {
            TableColumn column = tbDokter.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(110);
            }else if(i==1){
                column.setPreferredWidth(350);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(80);
            }else if(i==5){
                column.setPreferredWidth(170);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(80);
            }else if(i==8){
                column.setPreferredWidth(150);            
            }else if(i==9){
                column.setPreferredWidth(130);            
            }
        }
        tbDokter.setDefaultRenderer(Object.class, new WarnaTable());
        
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
                column.setPreferredWidth(310);
            }else if(i==2){
                column.setPreferredWidth(310);
            }
        }
        tbListDicom.setDefaultRenderer(Object.class, new WarnaTable());

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
                if(akses.getform().equals("DlgCariPeriksaRadiologi")){
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
                if(akses.getform().equals("DlgCariPeriksaRadiologi")){
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
                if(akses.getform().equals("DlgCariPeriksaRadiologi")){
                    if(petugas.getTable().getSelectedRow()!= -1){     
                        if(pilihan.equals("ubahpetugas")){
                            KdPtgUbah.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                            NmPtgUbah.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                            KdPtgUbah.requestFocus();
                        }else if(pilihan.equals("caripetugas")){
                            kdptg.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                            nmptg.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                            kdptg.requestFocus();
                        }                            
                    }  
                        
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
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(akses.getform().equals("DlgCariPeriksaRadiologi")){
                    if(dokter.getTable().getSelectedRow()!= -1){
                        if(pilihan.equals("perujuk")){
                            KodePerujuk.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                            NmPerujuk.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                            KodePerujuk.requestFocus();
                        }else if(pilihan.equals("penjab")){
                            KodePj.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                            NmDokterPj.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                            KodePj.requestFocus();
                        }  
                    }   
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
        
        templatehasil.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(akses.getform().equals("DlgCariPeriksaRadiologi")){
                    if(templatehasil.getTable().getSelectedRow()!= -1){                   
                        HasilPeriksa.setText(templatehasil.getTable().getValueAt(templatehasil.getTable().getSelectedRow(),2).toString());
                    } 
                    HasilPeriksa.requestFocus();
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
            psrekening=koneksi.prepareStatement(
                    "select set_akun_ranap.Suspen_Piutang_Radiologi_Ranap,set_akun_ranap.Radiologi_Ranap,set_akun_ranap.Beban_Jasa_Medik_Dokter_Radiologi_Ranap,"+
                    "set_akun_ranap.Utang_Jasa_Medik_Dokter_Radiologi_Ranap,set_akun_ranap.Beban_Jasa_Medik_Petugas_Radiologi_Ranap,set_akun_ranap.Utang_Jasa_Medik_Petugas_Radiologi_Ranap,"+
                    "set_akun_ranap.Beban_Kso_Radiologi_Ranap,set_akun_ranap.Utang_Kso_Radiologi_Ranap,set_akun_ranap.HPP_Persediaan_Radiologi_Rawat_Inap,"+
                    "set_akun_ranap.Persediaan_BHP_Radiologi_Rawat_Inap,set_akun_ranap.Beban_Jasa_Sarana_Radiologi_Ranap,set_akun_ranap.Utang_Jasa_Sarana_Radiologi_Ranap,"+
                    "set_akun_ranap.Beban_Jasa_Perujuk_Radiologi_Ranap,set_akun_ranap.Utang_Jasa_Perujuk_Radiologi_Ranap,set_akun_ranap.Beban_Jasa_Menejemen_Radiologi_Ranap,"+
                    "set_akun_ranap.Utang_Jasa_Menejemen_Radiologi_Ranap from set_akun_ranap");
            try {
                rsrekening=psrekening.executeQuery();
                while(rsrekening.next()){
                    Suspen_Piutang_Radiologi_Ranap=rsrekening.getString("Suspen_Piutang_Radiologi_Ranap");
                    Radiologi_Ranap=rsrekening.getString("Radiologi_Ranap");
                    Beban_Jasa_Medik_Dokter_Radiologi_Ranap=rsrekening.getString("Beban_Jasa_Medik_Dokter_Radiologi_Ranap");
                    Utang_Jasa_Medik_Dokter_Radiologi_Ranap=rsrekening.getString("Utang_Jasa_Medik_Dokter_Radiologi_Ranap");
                    Beban_Jasa_Medik_Petugas_Radiologi_Ranap=rsrekening.getString("Beban_Jasa_Medik_Petugas_Radiologi_Ranap");
                    Utang_Jasa_Medik_Petugas_Radiologi_Ranap=rsrekening.getString("Utang_Jasa_Medik_Petugas_Radiologi_Ranap");
                    Beban_Kso_Radiologi_Ranap=rsrekening.getString("Beban_Kso_Radiologi_Ranap");
                    Utang_Kso_Radiologi_Ranap=rsrekening.getString("Utang_Kso_Radiologi_Ranap");
                    HPP_Persediaan_Radiologi_Rawat_Inap=rsrekening.getString("HPP_Persediaan_Radiologi_Rawat_Inap");
                    Persediaan_BHP_Radiologi_Rawat_Inap=rsrekening.getString("Persediaan_BHP_Radiologi_Rawat_Inap");
                    Beban_Jasa_Sarana_Radiologi_Ranap=rsrekening.getString("Beban_Jasa_Sarana_Radiologi_Ranap");
                    Utang_Jasa_Sarana_Radiologi_Ranap=rsrekening.getString("Utang_Jasa_Sarana_Radiologi_Ranap");
                    Beban_Jasa_Perujuk_Radiologi_Ranap=rsrekening.getString("Beban_Jasa_Perujuk_Radiologi_Ranap");
                    Utang_Jasa_Perujuk_Radiologi_Ranap=rsrekening.getString("Utang_Jasa_Perujuk_Radiologi_Ranap");
                    Beban_Jasa_Menejemen_Radiologi_Ranap=rsrekening.getString("Beban_Jasa_Menejemen_Radiologi_Ranap");
                    Utang_Jasa_Menejemen_Radiologi_Ranap=rsrekening.getString("Utang_Jasa_Menejemen_Radiologi_Ranap");
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
            
            psrekening=koneksi.prepareStatement(
                    "select set_akun_ralan.Suspen_Piutang_Radiologi_Ralan,set_akun_ralan.Radiologi_Ralan,set_akun_ralan.Beban_Jasa_Medik_Dokter_Radiologi_Ralan,"+
                    "set_akun_ralan.Utang_Jasa_Medik_Dokter_Radiologi_Ralan,set_akun_ralan.Beban_Jasa_Medik_Petugas_Radiologi_Ralan,set_akun_ralan.Utang_Jasa_Medik_Petugas_Radiologi_Ralan,"+
                    "set_akun_ralan.Beban_Kso_Radiologi_Ralan,set_akun_ralan.Utang_Kso_Radiologi_Ralan,set_akun_ralan.HPP_Persediaan_Radiologi_Rawat_Jalan,"+
                    "set_akun_ralan.Persediaan_BHP_Radiologi_Rawat_Jalan,set_akun_ralan.Beban_Jasa_Sarana_Radiologi_Ralan,set_akun_ralan.Utang_Jasa_Sarana_Radiologi_Ralan,"+
                    "set_akun_ralan.Beban_Jasa_Perujuk_Radiologi_Ralan,set_akun_ralan.Utang_Jasa_Perujuk_Radiologi_Ralan,set_akun_ralan.Beban_Jasa_Menejemen_Radiologi_Ralan,"+
                    "set_akun_ralan.Utang_Jasa_Menejemen_Radiologi_Ralan from set_akun_ralan");
            try {
                rsrekening=psrekening.executeQuery();
                while(rsrekening.next()){
                    Suspen_Piutang_Radiologi_Ralan=rsrekening.getString("Suspen_Piutang_Radiologi_Ralan");
                    Radiologi_Ralan=rsrekening.getString("Radiologi_Ralan");
                    Beban_Jasa_Medik_Dokter_Radiologi_Ralan=rsrekening.getString("Beban_Jasa_Medik_Dokter_Radiologi_Ralan");
                    Utang_Jasa_Medik_Dokter_Radiologi_Ralan=rsrekening.getString("Utang_Jasa_Medik_Dokter_Radiologi_Ralan");
                    Beban_Jasa_Medik_Petugas_Radiologi_Ralan=rsrekening.getString("Beban_Jasa_Medik_Petugas_Radiologi_Ralan");
                    Utang_Jasa_Medik_Petugas_Radiologi_Ralan=rsrekening.getString("Utang_Jasa_Medik_Petugas_Radiologi_Ralan");
                    Beban_Kso_Radiologi_Ralan=rsrekening.getString("Beban_Kso_Radiologi_Ralan");
                    Utang_Kso_Radiologi_Ralan=rsrekening.getString("Utang_Kso_Radiologi_Ralan");
                    HPP_Persediaan_Radiologi_Rawat_Jalan=rsrekening.getString("HPP_Persediaan_Radiologi_Rawat_Jalan");
                    Persediaan_BHP_Radiologi_Rawat_Jalan=rsrekening.getString("Persediaan_BHP_Radiologi_Rawat_Jalan");
                    Beban_Jasa_Sarana_Radiologi_Ralan=rsrekening.getString("Beban_Jasa_Sarana_Radiologi_Ralan");
                    Utang_Jasa_Sarana_Radiologi_Ralan=rsrekening.getString("Utang_Jasa_Sarana_Radiologi_Ralan");
                    Beban_Jasa_Perujuk_Radiologi_Ralan=rsrekening.getString("Beban_Jasa_Perujuk_Radiologi_Ralan");
                    Utang_Jasa_Perujuk_Radiologi_Ralan=rsrekening.getString("Utang_Jasa_Perujuk_Radiologi_Ralan");
                    Beban_Jasa_Menejemen_Radiologi_Ralan=rsrekening.getString("Beban_Jasa_Menejemen_Radiologi_Ralan");
                    Utang_Jasa_Menejemen_Radiologi_Ralan=rsrekening.getString("Utang_Jasa_Menejemen_Radiologi_Ralan");
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
        LoadHTML.setEditable(false);
        LoadHTML.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
              Desktop desktop = Desktop.getDesktop();
              try {
                desktop.browse(e.getURL().toURI());
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
        });
     
        ChkAccor.setSelected(false);
        isPhoto();
        installKirimHasilRadiologiWhatsapp();
    }


    /**
     * Penyesuaian tampilan dibuat di luar initComponents() supaya source
     * generated NetBeans dan seluruh fungsi lama tetap utuh.
     */
    private void installKirimHasilRadiologiWhatsapp() {
        try {
            sembunyikanTabRadiologi("Photo Radiologi");
            sembunyikanTabRadiologi("Integrasi Orthanc");
            if (TabData.indexOfComponent(FormHasilRadiologi) >= 0) {
                TabData.setSelectedComponent(FormHasilRadiologi);
            }

            if (BtnKirimHasilRad == null) {
                BtnKirimHasilRad = new widget.Button();
                BtnKirimHasilRad.setIcon(new javax.swing.ImageIcon(
                        getClass().getResource("/picture/whatsapp (2).png")));
                BtnKirimHasilRad.setText("Kirim Hasil Rad");
                BtnKirimHasilRad.setToolTipText(
                        "Kirim hasil radiologi dalam bentuk PDF ke WhatsApp pasien");
                BtnKirimHasilRad.setName("BtnKirimHasilRad");
                BtnKirimHasilRad.setPreferredSize(new java.awt.Dimension(140, 30));
                BtnKirimHasilRad.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        kirimHasilRadiologiKeWhatsappPasien();
                    }
                });
            }

            // Sisipkan tepat setelah tombol Upload tanpa memindahkan fungsi tombol lain.
            if (panelisi1 != null && BtnKirimHasilRad.getParent() != panelisi1) {
                java.awt.Component[] komponen = panelisi1.getComponents();
                int posisi = komponen.length;
                for (int i = 0; i < komponen.length; i++) {
                    if (komponen[i] == BtnUpload) {
                        posisi = i + 1;
                        break;
                    }
                }
                panelisi1.add(BtnKirimHasilRad, posisi);
                panelisi1.revalidate();
                panelisi1.repaint();
            }
        } catch (Exception ex) {
            System.out.println("Notif pemasangan tombol Kirim Hasil Rad : " + ex);
        }
    }

    private void sembunyikanTabRadiologi(String judulTab) {
        if (TabData == null || judulTab == null) return;
        for (int i = TabData.getTabCount() - 1; i >= 0; i--) {
            String judul = TabData.getTitleAt(i);
            if (judul != null && judul.trim().equalsIgnoreCase(judulTab.trim())) {
                TabData.removeTabAt(i);
            }
        }
    }

    /**
     * Mengikuti alur Kirim Hasil Lab: berkas final yang sudah di-upload
     * ditampilkan pada popup lokal, nomor WhatsApp dapat diedit, lalu seluruh
     * halaman digabung menjadi satu PDF sebelum dikirim.
     */
    private void kirimHasilRadiologiKeWhatsappPasien() {
        int baris = getBarisPemeriksaanTerpilih();
        if (baris < 0) {
            JOptionPane.showMessageDialog(null,
                    "Silakan pilih dahulu data pemeriksaan radiologi yang akan dikirim.");
            return;
        }

        String noRawat = nilaiTabel(tbDokter, baris, 0);
        String tglPeriksa = nilaiTabel(tbDokter, baris, 3);
        String jamPeriksa = nilaiTabel(tbDokter, baris, 4);
        if (noRawat.equals("") || tglPeriksa.equals("") || jamPeriksa.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "No. rawat atau tanggal/jam pemeriksaan belum terbaca.\n"
                    + "Silakan pilih kembali baris pemeriksaan pasien.");
            return;
        }

        String hasilTersimpan = ambilHasilRadiologiTersimpanWA(
                noRawat, tglPeriksa, jamPeriksa);
        if (hasilTersimpan == null || hasilTersimpan.trim().equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Hasil bacaan radiologi belum tersimpan.\n"
                    + "Simpan/Update hasil bacaan terlebih dahulu sebelum dikirim.");
            return;
        }

        java.util.ArrayList<HasilPenunjangWhatsapp.Berkas> daftarBerkas =
                ambilBerkasRadiologiFinalUntukKirimWA(noRawat, tglPeriksa, jamPeriksa);
        if (daftarBerkas.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Belum ditemukan berkas hasil radiologi untuk pemeriksaan ini.\n\n"
                    + "No. Rawat : " + noRawat + "\n"
                    + "Tgl/Jam   : " + tglPeriksa + " / " + jamPeriksa + "\n\n"
                    + "Klik tombol Upload terlebih dahulu, kemudian klik Kirim Hasil Rad.");
            return;
        }

        try {
            String namaPasien = Sequel.cariIsi(
                    "select pasien.nm_pasien from reg_periksa "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where reg_periksa.no_rawat=? limit 1", noRawat);
            if (namaPasien == null || namaPasien.trim().equals("")) {
                namaPasien = nilaiTabel(tbDokter, baris, 1);
            }
            String noHp = Sequel.cariIsi(
                    "select pasien.no_tlp from reg_periksa "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where reg_periksa.no_rawat=? limit 1", noRawat);

            String sesi = HasilPenunjangWhatsapp.buatSesi(
                    noRawat, namaPasien, noHp, "Radiologi", daftarBerkas);
            java.util.ArrayList<String> ids = new java.util.ArrayList<String>();
            for (HasilPenunjangWhatsapp.Berkas berkas : daftarBerkas) {
                ids.add(berkas.id);
            }
            String urlPopup = HasilPenunjangWhatsapp.getUrlPopup(sesi, ids);
            if (urlPopup == null || urlPopup.trim().equals("")) {
                JOptionPane.showMessageDialog(null,
                        "Popup pengiriman WhatsApp belum dapat dibuka.");
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new java.net.URI(urlPopup));
            } else {
                Runtime.getRuntime().exec(
                        "rundll32 url.dll,FileProtocolHandler " + urlPopup);
            }
        } catch (Exception ex) {
            System.out.println("Notif kirim hasil radiologi ke WhatsApp pasien : " + ex);
            JOptionPane.showMessageDialog(null,
                    "Gagal membuka popup Kirim Hasil Rad.\n\nDetail : "
                    + (ex.getMessage() == null ? ex.toString() : ex.getMessage()));
        }
    }

    private String ambilHasilRadiologiTersimpanWA(String noRawat,
            String tglPeriksa, String jamPeriksa) {
        PreparedStatement psHasilWA = null;
        ResultSet rsHasilWA = null;
        try {
            psHasilWA = koneksi.prepareStatement(
                    "select ifnull(hasil,'') as hasil from hasil_radiologi "
                    + "where no_rawat=? and tgl_periksa=? and jam=? limit 1");
            psHasilWA.setString(1, noRawat);
            psHasilWA.setString(2, tglPeriksa);
            psHasilWA.setString(3, jamPeriksa);
            rsHasilWA = psHasilWA.executeQuery();
            if (rsHasilWA.next()) return rsHasilWA.getString("hasil");
        } catch (Exception ex) {
            System.out.println("Notif cek hasil radiologi untuk WA : " + ex);
        } finally {
            try { if (rsHasilWA != null) rsHasilWA.close(); } catch (Exception ex) {}
            try { if (psHasilWA != null) psHasilWA.close(); } catch (Exception ex) {}
        }
        return "";
    }

    /**
     * Mengambil satu kelompok upload radiologi terbaru untuk no. rawat yang
     * dipilih. File multi-halaman (_page_1, _page_2, dst.) tetap dikirim
     * sebagai satu kelompok dan digabung menjadi satu PDF oleh modul WA.
     */
    private java.util.ArrayList<HasilPenunjangWhatsapp.Berkas>
            ambilBerkasRadiologiFinalUntukKirimWA(String noRawat,
                    String tglPeriksa, String jamPeriksa) {
        java.util.ArrayList<HasilPenunjangWhatsapp.Berkas> hasil =
                new java.util.ArrayList<HasilPenunjangWhatsapp.Berkas>();
        PreparedStatement psBerkasWA = null;
        ResultSet rsBerkasWA = null;
        java.util.LinkedHashMap<String, java.util.ArrayList<String[]>> kelompok =
                new java.util.LinkedHashMap<String, java.util.ArrayList<String[]>>();
        String kunciNoRawat = noRawat == null ? ""
                : noRawat.replaceAll("[^A-Za-z0-9]", "").toLowerCase(java.util.Locale.ROOT);

        try {
            psBerkasWA = koneksi.prepareStatement(
                    "select berkas_digital_perawatan.kode,berkas_digital_perawatan.lokasi_file "
                    + "from berkas_digital_perawatan "
                    + "where berkas_digital_perawatan.no_rawat=? "
                    + "and trim(ifnull(berkas_digital_perawatan.lokasi_file,''))<>'' "
                    + "and (berkas_digital_perawatan.kode in ("
                    + "select master_berkas_digital.kode from master_berkas_digital "
                    + "where upper(master_berkas_digital.nama) like '%RADIOLOG%') "
                    + "or lower(berkas_digital_perawatan.lokasi_file) like '%hasilradiologi%' "
                    + "or lower(berkas_digital_perawatan.lokasi_file) like '%hasil_radiologi%' "
                    + "or lower(berkas_digital_perawatan.lokasi_file) like '%radiologi%') "
                    + "order by berkas_digital_perawatan.lokasi_file");
            psBerkasWA.setString(1, noRawat);
            rsBerkasWA = psBerkasWA.executeQuery();
            while (rsBerkasWA.next()) {
                String lokasi = rsBerkasWA.getString("lokasi_file");
                String kode = rsBerkasWA.getString("kode");
                if (!berkasGambarHasilRadiologiWA(lokasi)) continue;
                String namaFile = ambilNamaFileHasilRadiologiWA(lokasi);
                String namaKecil = namaFile.toLowerCase(java.util.Locale.ROOT);
                if (!kunciNoRawat.equals("") && !namaKecil.contains(kunciNoRawat)) {
                    continue;
                }
                String kunciKelompok = buatKunciKelompokUploadRadiologiWA(namaFile);
                java.util.ArrayList<String[]> daftar = kelompok.get(kunciKelompok);
                if (daftar == null) {
                    daftar = new java.util.ArrayList<String[]>();
                    kelompok.put(kunciKelompok, daftar);
                }
                daftar.add(new String[]{lokasi, kode == null ? "" : kode, namaFile});
            }
        } catch (Exception ex) {
            System.out.println("Notif ambil berkas radiologi final untuk WA : " + ex);
        } finally {
            try { if (rsBerkasWA != null) rsBerkasWA.close(); } catch (Exception ex) {}
            try { if (psBerkasWA != null) psBerkasWA.close(); } catch (Exception ex) {}
        }

        if (kelompok.isEmpty()) return hasil;

        java.util.ArrayList<String> kunciTindakan =
                ambilKunciTindakanUploadRadiologiWA(noRawat, tglPeriksa, jamPeriksa);
        boolean adaTindakanSama = false;
        for (String kunci : kelompok.keySet()) {
            if (kelompokSesuaiTindakanRadiologiWA(kunci, kunciTindakan)) {
                adaTindakanSama = true;
                break;
            }
        }

        String kunciTanggal = buatKunciTanggalUploadRadiologiWA(tglPeriksa);
        boolean adaTanggalSama = false;
        if (!adaTindakanSama) {
            for (String kunci : kelompok.keySet()) {
                if (!kunciTanggal.equals("") && kunci.startsWith(kunciTanggal + "_")) {
                    adaTanggalSama = true;
                    break;
                }
            }
        }

        String kelompokTerpilih = "";
        long waktuTerpilih = Long.MIN_VALUE;
        for (String kunci : kelompok.keySet()) {
            if (adaTindakanSama
                    && !kelompokSesuaiTindakanRadiologiWA(kunci, kunciTindakan)) {
                continue;
            }
            if (!adaTindakanSama && adaTanggalSama
                    && !kunci.startsWith(kunciTanggal + "_")) {
                continue;
            }
            long waktu = waktuKelompokUploadRadiologiWA(kunci);
            if (kelompokTerpilih.equals("") || waktu > waktuTerpilih
                    || (waktu == waktuTerpilih
                    && kunci.compareToIgnoreCase(kelompokTerpilih) > 0)) {
                kelompokTerpilih = kunci;
                waktuTerpilih = waktu;
            }
        }
        if (kelompokTerpilih.equals("")) return hasil;

        java.util.ArrayList<String[]> daftarTerpilih = kelompok.get(kelompokTerpilih);
        java.util.Collections.sort(daftarTerpilih,
                new java.util.Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] b) {
                int halamanA = urutanHalamanUploadRadiologiWA(a[2]);
                int halamanB = urutanHalamanUploadRadiologiWA(b[2]);
                if (halamanA != halamanB) return halamanA - halamanB;
                return a[2].compareToIgnoreCase(b[2]);
            }
        });

        int nomor = 1;
        for (String[] berkas : daftarTerpilih) {
            hasil.add(new HasilPenunjangWhatsapp.Berkas(
                    "rad_final_" + nomor++,
                    buatUrlBerkasRadiologiWA(berkas[0]),
                    berkas[2], berkas[0], noRawat, berkas[1]));
        }
        return hasil;
    }

    private java.util.ArrayList<String> ambilKunciTindakanUploadRadiologiWA(
            String noRawat, String tglPeriksa, String jamPeriksa) {
        java.util.ArrayList<String> hasil = new java.util.ArrayList<String>();
        PreparedStatement psTindakanWA = null;
        ResultSet rsTindakanWA = null;
        try {
            psTindakanWA = koneksi.prepareStatement(
                    "select distinct jns_perawatan_radiologi.nm_perawatan "
                    + "from periksa_radiologi inner join jns_perawatan_radiologi "
                    + "on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                    + "where periksa_radiologi.no_rawat=? "
                    + "and periksa_radiologi.tgl_periksa=? and periksa_radiologi.jam=?");
            psTindakanWA.setString(1, noRawat);
            psTindakanWA.setString(2, tglPeriksa);
            psTindakanWA.setString(3, jamPeriksa);
            rsTindakanWA = psTindakanWA.executeQuery();
            while (rsTindakanWA.next()) {
                String nama = rsTindakanWA.getString("nm_perawatan");
                String kunci = nama == null ? ""
                        : nama.replaceAll("[^A-Za-z0-9]", "_")
                                .toLowerCase(java.util.Locale.ROOT);
                if (!kunci.equals("") && !hasil.contains(kunci)) hasil.add(kunci);
            }
        } catch (Exception ex) {
            System.out.println("Notif kunci tindakan radiologi untuk WA : " + ex);
        } finally {
            try { if (rsTindakanWA != null) rsTindakanWA.close(); } catch (Exception ex) {}
            try { if (psTindakanWA != null) psTindakanWA.close(); } catch (Exception ex) {}
        }
        return hasil;
    }

    private boolean kelompokSesuaiTindakanRadiologiWA(String kelompok,
            java.util.List<String> kunciTindakan) {
        if (kelompok == null || kunciTindakan == null || kunciTindakan.isEmpty()) {
            return false;
        }
        String nilai = kelompok.toLowerCase(java.util.Locale.ROOT);
        for (String kunci : kunciTindakan) {
            if (kunci != null && !kunci.equals("") && nilai.contains(kunci)) {
                return true;
            }
        }
        return false;
    }

    private boolean berkasGambarHasilRadiologiWA(String lokasiFile) {
        String lokasi = lokasiFile == null ? ""
                : lokasiFile.toLowerCase(java.util.Locale.ROOT).trim();
        return lokasi.endsWith(".jpg") || lokasi.endsWith(".jpeg")
                || lokasi.endsWith(".png") || lokasi.endsWith(".bmp")
                || lokasi.endsWith(".gif") || lokasi.endsWith(".webp");
    }

    private String ambilNamaFileHasilRadiologiWA(String lokasiFile) {
        String lokasi = lokasiFile == null ? ""
                : lokasiFile.replace('\\', '/').trim();
        int posisi = lokasi.lastIndexOf('/');
        return posisi >= 0 ? lokasi.substring(posisi + 1) : lokasi;
    }

    private String buatKunciKelompokUploadRadiologiWA(String namaFile) {
        String nama = namaFile == null ? "" : namaFile.trim();
        nama = nama.replaceFirst("(?i)\\.(jpg|jpeg|png|bmp|gif|webp)$", "");
        nama = nama.replaceFirst("(?i)_page_[0-9]+$", "");
        return nama;
    }

    private int urutanHalamanUploadRadiologiWA(String namaFile) {
        java.util.regex.Matcher pencocok = java.util.regex.Pattern
                .compile("(?i)_page_([0-9]+)\\.(jpg|jpeg|png|bmp|gif|webp)$")
                .matcher(namaFile == null ? "" : namaFile);
        if (pencocok.find()) {
            try { return Integer.parseInt(pencocok.group(1)); } catch (Exception ex) {}
        }
        return 1;
    }

    private String buatKunciTanggalUploadRadiologiWA(String tanggal) {
        String nilai = tanggal == null ? "" : tanggal.trim();
        if (nilai.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            String[] bagian = nilai.split("-");
            return bagian[2] + bagian[1] + bagian[0];
        }
        if (nilai.matches("[0-9]{2}[-/][0-9]{2}[-/][0-9]{4}")) {
            String[] bagian = nilai.split("[-/]");
            return bagian[0] + bagian[1] + bagian[2];
        }
        return "";
    }

    private long waktuKelompokUploadRadiologiWA(String namaKelompok) {
        java.util.regex.Matcher pencocok = java.util.regex.Pattern
                .compile("^([0-9]{8}_[0-9]{6})")
                .matcher(namaKelompok == null ? "" : namaKelompok);
        if (!pencocok.find()) return 0L;
        try {
            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy_HHmmss");
            format.setLenient(false);
            return format.parse(pencocok.group(1)).getTime();
        } catch (Exception ex) {
            return 0L;
        }
    }

    private String buatUrlBerkasRadiologiWA(String lokasiFile) {
        String lokasi = lokasiFile == null ? ""
                : lokasiFile.replace('\\', '/').trim();
        while (lokasi.startsWith("/")) lokasi = lokasi.substring(1);
        if (lokasi.startsWith("http://") || lokasi.startsWith("https://")) {
            if (lokasi.contains("/berkasrawat/pages/upload/")) return lokasi;
            if (lokasi.contains("/berkasrawat/")) {
                lokasi = lokasi.substring(lokasi.indexOf("/berkasrawat/")
                        + "/berkasrawat/".length());
                while (lokasi.startsWith("/")) lokasi = lokasi.substring(1);
            } else {
                return lokasi;
            }
        }
        if (lokasi.startsWith(koneksiDB.HYBRIDWEB() + "/")) {
            lokasi = lokasi.substring((koneksiDB.HYBRIDWEB() + "/").length());
        }
        if (lokasi.startsWith("berkasrawat/")) {
            lokasi = lokasi.substring("berkasrawat/".length());
        }
        if (lokasi.startsWith("pages/upload/")) {
            lokasi = lokasi.substring("pages/upload/".length());
        } else if (lokasi.startsWith("upload/")) {
            lokasi = lokasi.substring("upload/".length());
        } else if (lokasi.startsWith("pages/")) {
            lokasi = lokasi.substring("pages/".length());
            if (lokasi.startsWith("upload/")) {
                lokasi = lokasi.substring("upload/".length());
            }
        }
        return "http://" + koneksiDB.HOSTHYBRIDWEB() + ":"
                + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB()
                + "/berkasrawat/pages/upload/" + lokasi;
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
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakNota = new javax.swing.JMenuItem();
        MnUbahDokterPetugas = new javax.swing.JMenuItem();
        ppBerkasDigital = new javax.swing.JMenuItem();
        ppRiwayat = new javax.swing.JMenuItem();
        Penjab = new widget.TextBox();
        Jk = new widget.TextBox();
        Umur = new widget.TextBox();
        Alamat = new widget.TextBox();
        NoRM = new widget.TextBox();
        WindowGantiDokterParamedis = new javax.swing.JDialog();
        internalFrame5 = new widget.InternalFrame();
        FormInput = new widget.panelisi();
        BtnSimpan4 = new widget.Button();
        BtnCloseIn4 = new widget.Button();
        jLabel7 = new widget.Label();
        KodePj = new widget.TextBox();
        NmDokterPj = new widget.TextBox();
        btnDokterPj = new widget.Button();
        btnDokter = new widget.Button();
        NmPerujuk = new widget.TextBox();
        KodePerujuk = new widget.TextBox();
        jLabel9 = new widget.Label();
        jLabel12 = new widget.Label();
        KdPtgUbah = new widget.TextBox();
        NmPtgUbah = new widget.TextBox();
        btnPetugas1 = new widget.Button();
        Petugas = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        scrollPane1 = new widget.ScrollPane();
        tbDokter = new widget.Table();
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
        TNoSEP = new widget.TextBox();
        NoSEP = new widget.Label();
        LDiagnosaKlinis = new widget.Label();
        TDiagnosaKlinis = new widget.TextBox();
        panelisi1 = new widget.panelisi();
        label10 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        label9 = new widget.Label();
        BtnHapus = new widget.Button();
        BtnAll = new widget.Button();
        BtnPrint = new widget.Button();
        BtnUpload = new widget.Button();
        BtnGenerateTokenPACS = new widget.Button();
        BtnFotoPACS = new widget.Button();
        BtnKeluar = new widget.Button();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        TabData = new javax.swing.JTabbedPane();
        FormPhoto = new widget.PanelBiasa();
        FormPass2 = new widget.PanelBiasa();
        btnAmbilPhoto = new widget.Button();
        BtnRefreshPhoto = new widget.Button();
        BtnWAPasien = new widget.Button();
        BtnWAPerujuk = new widget.Button();
        BtnWAdrRad = new widget.Button();
        Scroll4 = new widget.ScrollPane();
        LoadHTML = new widget.editorpane();
        FormHasilRadiologi = new widget.PanelBiasa();
        Scroll3 = new widget.ScrollPane();
        HasilPeriksa = new widget.TextArea();
        panelGlass6 = new widget.panelisi();
        btnAmbilPhoto1 = new widget.Button();
        BtnSimpan = new widget.Button();
        BtnPrint1 = new widget.Button();
        FormOrthan = new widget.PanelBiasa();
        Scroll5 = new widget.ScrollPane();
        tbListDicom = new widget.Table();
        panelGlass7 = new widget.panelisi();
        btnDicom = new widget.Button();

        Kd2.setName("Kd2"); // NOI18N
        Kd2.setPreferredSize(new java.awt.Dimension(207, 23));

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCetakNota.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakNota.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakNota.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakNota.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakNota.setText("Cetak Nota Radiologi");
        MnCetakNota.setName("MnCetakNota"); // NOI18N
        MnCetakNota.setPreferredSize(new java.awt.Dimension(220, 28));
        MnCetakNota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakNotaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakNota);

        MnUbahDokterPetugas.setBackground(new java.awt.Color(255, 255, 254));
        MnUbahDokterPetugas.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnUbahDokterPetugas.setForeground(new java.awt.Color(50, 50, 50));
        MnUbahDokterPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnUbahDokterPetugas.setText("Ubah P.J.Lab, Perujuk & Petugas");
        MnUbahDokterPetugas.setName("MnUbahDokterPetugas"); // NOI18N
        MnUbahDokterPetugas.setPreferredSize(new java.awt.Dimension(220, 28));
        MnUbahDokterPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnUbahDokterPetugasActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnUbahDokterPetugas);

        ppBerkasDigital.setBackground(new java.awt.Color(255, 255, 254));
        ppBerkasDigital.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBerkasDigital.setForeground(new java.awt.Color(50, 50, 50));
        ppBerkasDigital.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBerkasDigital.setText("Berkas Digital Perawatan");
        ppBerkasDigital.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBerkasDigital.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBerkasDigital.setName("ppBerkasDigital"); // NOI18N
        ppBerkasDigital.setPreferredSize(new java.awt.Dimension(220, 28));
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
        ppRiwayat.setPreferredSize(new java.awt.Dimension(220, 28));
        ppRiwayat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppRiwayatBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppRiwayat);

        Penjab.setEditable(false);
        Penjab.setFocusTraversalPolicyProvider(true);
        Penjab.setName("Penjab"); // NOI18N
        Penjab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PenjabKeyPressed(evt);
            }
        });

        Jk.setEditable(false);
        Jk.setFocusTraversalPolicyProvider(true);
        Jk.setName("Jk"); // NOI18N

        Umur.setEditable(false);
        Umur.setHighlighter(null);
        Umur.setName("Umur"); // NOI18N

        Alamat.setEditable(false);
        Alamat.setHighlighter(null);
        Alamat.setName("Alamat"); // NOI18N

        NoRM.setName("NoRM"); // NOI18N
        NoRM.setPreferredSize(new java.awt.Dimension(207, 23));

        WindowGantiDokterParamedis.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindowGantiDokterParamedis.setName("WindowGantiDokterParamedis"); // NOI18N
        WindowGantiDokterParamedis.setUndecorated(true);
        WindowGantiDokterParamedis.setResizable(false);

        internalFrame5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)), "::[ Ubah P.J.Rad, Perujuk & Petugas ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame5.setName("internalFrame5"); // NOI18N
        internalFrame5.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(89, 434));
        FormInput.setLayout(null);

        BtnSimpan4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan4.setMnemonic('S');
        BtnSimpan4.setText("Simpan");
        BtnSimpan4.setToolTipText("Alt+S");
        BtnSimpan4.setName("BtnSimpan4"); // NOI18N
        BtnSimpan4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpan4ActionPerformed(evt);
            }
        });
        FormInput.add(BtnSimpan4);
        BtnSimpan4.setBounds(470, 15, 100, 30);

        BtnCloseIn4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnCloseIn4.setMnemonic('U');
        BtnCloseIn4.setText("Tutup");
        BtnCloseIn4.setToolTipText("Alt+U");
        BtnCloseIn4.setName("BtnCloseIn4"); // NOI18N
        BtnCloseIn4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseIn4ActionPerformed(evt);
            }
        });
        FormInput.add(BtnCloseIn4);
        BtnCloseIn4.setBounds(470, 60, 100, 30);

        jLabel7.setText("Dokter P.J. :");
        jLabel7.setName("jLabel7"); // NOI18N
        FormInput.add(jLabel7);
        jLabel7.setBounds(0, 12, 92, 23);

        KodePj.setEditable(false);
        KodePj.setName("KodePj"); // NOI18N
        FormInput.add(KodePj);
        KodePj.setBounds(95, 12, 113, 23);

        NmDokterPj.setEditable(false);
        NmDokterPj.setHighlighter(null);
        NmDokterPj.setName("NmDokterPj"); // NOI18N
        FormInput.add(NmDokterPj);
        NmDokterPj.setBounds(210, 12, 208, 23);

        btnDokterPj.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnDokterPj.setMnemonic('4');
        btnDokterPj.setToolTipText("ALt+4");
        btnDokterPj.setName("btnDokterPj"); // NOI18N
        btnDokterPj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDokterPjActionPerformed(evt);
            }
        });
        FormInput.add(btnDokterPj);
        btnDokterPj.setBounds(420, 12, 28, 23);

        btnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnDokter.setMnemonic('4');
        btnDokter.setToolTipText("ALt+4");
        btnDokter.setName("btnDokter"); // NOI18N
        btnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDokterActionPerformed(evt);
            }
        });
        FormInput.add(btnDokter);
        btnDokter.setBounds(420, 42, 28, 23);

        NmPerujuk.setEditable(false);
        NmPerujuk.setHighlighter(null);
        NmPerujuk.setName("NmPerujuk"); // NOI18N
        FormInput.add(NmPerujuk);
        NmPerujuk.setBounds(210, 42, 208, 23);

        KodePerujuk.setName("KodePerujuk"); // NOI18N
        KodePerujuk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KodePerujukKeyPressed(evt);
            }
        });
        FormInput.add(KodePerujuk);
        KodePerujuk.setBounds(95, 42, 113, 23);

        jLabel9.setText("Dokter Perujuk :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(0, 42, 92, 23);

        jLabel12.setText("Petugas :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(0, 72, 92, 23);

        KdPtgUbah.setName("KdPtgUbah"); // NOI18N
        KdPtgUbah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPtgUbahKeyPressed(evt);
            }
        });
        FormInput.add(KdPtgUbah);
        KdPtgUbah.setBounds(95, 72, 113, 23);

        NmPtgUbah.setEditable(false);
        NmPtgUbah.setName("NmPtgUbah"); // NOI18N
        FormInput.add(NmPtgUbah);
        NmPtgUbah.setBounds(210, 72, 208, 23);

        btnPetugas1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas1.setMnemonic('2');
        btnPetugas1.setToolTipText("Alt+2");
        btnPetugas1.setName("btnPetugas1"); // NOI18N
        btnPetugas1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugas1ActionPerformed(evt);
            }
        });
        FormInput.add(btnPetugas1);
        btnPetugas1.setBounds(420, 72, 28, 23);

        internalFrame5.add(FormInput, java.awt.BorderLayout.CENTER);

        WindowGantiDokterParamedis.getContentPane().add(internalFrame5, java.awt.BorderLayout.CENTER);

        Petugas.setEditable(false);
        Petugas.setHighlighter(null);
        Petugas.setName("Petugas"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Pemeriksaan Radiologi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

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

        internalFrame1.add(scrollPane1, java.awt.BorderLayout.CENTER);

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
        label16.setBounds(330, 10, 60, 23);

        label13.setText("Petugas :");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label13);
        label13.setBounds(330, 40, 60, 23);

        kdmem.setName("kdmem"); // NOI18N
        kdmem.setPreferredSize(new java.awt.Dimension(80, 23));
        kdmem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdmemKeyPressed(evt);
            }
        });
        panelisi3.add(kdmem);
        kdmem.setBounds(400, 10, 75, 23);

        kdptg.setName("kdptg"); // NOI18N
        kdptg.setPreferredSize(new java.awt.Dimension(80, 23));
        kdptg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdptgKeyPressed(evt);
            }
        });
        panelisi3.add(kdptg);
        kdptg.setBounds(400, 40, 75, 23);

        nmmem.setEditable(false);
        nmmem.setName("nmmem"); // NOI18N
        nmmem.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi3.add(nmmem);
        nmmem.setBounds(480, 10, 240, 23);

        nmptg.setEditable(false);
        nmptg.setName("nmptg"); // NOI18N
        nmptg.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi3.add(nmptg);
        nmptg.setBounds(480, 40, 240, 23);

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
        btnPasien.setBounds(720, 10, 28, 23);

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
        btnPetugas.setBounds(720, 40, 28, 23);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label18);
        label18.setBounds(178, 40, 30, 23);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl2KeyPressed(evt);
            }
        });
        panelisi3.add(Tgl2);
        Tgl2.setBounds(205, 40, 100, 23);

        TNoSEP.setEditable(false);
        TNoSEP.setHighlighter(null);
        TNoSEP.setName("TNoSEP"); // NOI18N
        TNoSEP.setPreferredSize(new java.awt.Dimension(140, 23));
        panelisi3.add(TNoSEP);
        TNoSEP.setBounds(850, 10, 235, 23);

        NoSEP.setText("No. SEP :");
        NoSEP.setName("NoSEP"); // NOI18N
        NoSEP.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(NoSEP);
        NoSEP.setBounds(760, 10, 80, 23);

        LDiagnosaKlinis.setText("Diagnosa Klinis :");
        LDiagnosaKlinis.setName("LDiagnosaKlinis"); // NOI18N
        LDiagnosaKlinis.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(LDiagnosaKlinis);
        LDiagnosaKlinis.setBounds(740, 40, 100, 23);

        TDiagnosaKlinis.setEditable(false);
        TDiagnosaKlinis.setHighlighter(null);
        TDiagnosaKlinis.setName("TDiagnosaKlinis"); // NOI18N
        TDiagnosaKlinis.setPreferredSize(new java.awt.Dimension(140, 23));
        panelisi3.add(TDiagnosaKlinis);
        TDiagnosaKlinis.setBounds(850, 40, 310, 23);

        internalFrame1.add(panelisi3, java.awt.BorderLayout.PAGE_START);

        panelisi1.setName("panelisi1"); // NOI18N
        panelisi1.setPreferredSize(new java.awt.Dimension(100, 56));
        panelisi1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label10.setText("Key Word :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi1.add(label10);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(170, 23));
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

        BtnFotoPACS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/x-ray.png"))); // NOI18N
        BtnFotoPACS.setMnemonic('T');
        BtnFotoPACS.setText("Lihat Foto");
        BtnFotoPACS.setToolTipText("Lihat Foto di browser GIP Launcher berdasarkan Accession Number");
        BtnFotoPACS.setName("BtnFotoPACS"); // NOI18N
        BtnFotoPACS.setPreferredSize(new java.awt.Dimension(110, 30));
        BtnFotoPACS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnFotoPACSActionPerformed(evt);
            }
        });
        BtnFotoPACS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnFotoPACSKeyPressed(evt);
            }
        });
        panelisi1.add(BtnFotoPACS);

        BtnGenerateTokenPACS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/lock (2).png"))); // NOI18N
        BtnGenerateTokenPACS.setMnemonic('P');
        BtnGenerateTokenPACS.setText("Perbaiki Accession");
        BtnGenerateTokenPACS.setToolTipText("Periksa dan perbaiki Accession Number di Orthanc");
        BtnGenerateTokenPACS.setName("BtnGenerateTokenPACS"); // NOI18N
        BtnGenerateTokenPACS.setPreferredSize(new java.awt.Dimension(155, 30));
        BtnGenerateTokenPACS.setVisible(false);
        BtnGenerateTokenPACS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGenerateTokenPACSActionPerformed(evt);
            }
        });
        BtnGenerateTokenPACS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnGenerateTokenPACSKeyPressed(evt);
            }
        });
        panelisi1.add(BtnGenerateTokenPACS);

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

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(445, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout(1, 1));

        ChkAccor.setBackground(new java.awt.Color(255, 250, 250));
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
        FormPhoto.setPreferredSize(new java.awt.Dimension(400, 700));
        FormPhoto.setLayout(new java.awt.BorderLayout());

        FormPass2.setBackground(new java.awt.Color(255, 255, 255));
        FormPass2.setBorder(null);
        FormPass2.setName("FormPass2"); // NOI18N
        FormPass2.setPreferredSize(new java.awt.Dimension(115, 40));

        btnAmbilPhoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        btnAmbilPhoto.setMnemonic('U');
        btnAmbilPhoto.setText("Ambil");
        btnAmbilPhoto.setToolTipText("Alt+U");
        btnAmbilPhoto.setName("btnAmbilPhoto"); // NOI18N
        btnAmbilPhoto.setPreferredSize(new java.awt.Dimension(100, 30));
        btnAmbilPhoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAmbilPhotoActionPerformed(evt);
            }
        });
        FormPass2.add(btnAmbilPhoto);

        BtnRefreshPhoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh.png"))); // NOI18N
        BtnRefreshPhoto.setMnemonic('U');
        BtnRefreshPhoto.setText("Refresh");
        BtnRefreshPhoto.setToolTipText("Alt+U");
        BtnRefreshPhoto.setName("BtnRefreshPhoto"); // NOI18N
        BtnRefreshPhoto.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnRefreshPhoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRefreshPhotoActionPerformed(evt);
            }
        });
        FormPass2.add(BtnRefreshPhoto);

        BtnWAPasien.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/whatsapp (2).png"))); // NOI18N
        BtnWAPasien.setMnemonic('U');
        BtnWAPasien.setText("Pasien");
        BtnWAPasien.setToolTipText("Alt+U");
        BtnWAPasien.setName("BtnWAPasien"); // NOI18N
        BtnWAPasien.setPreferredSize(new java.awt.Dimension(80, 30));
        BtnWAPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnWAPasienActionPerformed(evt);
            }
        });
        FormPass2.add(BtnWAPasien);

        BtnWAPerujuk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/whatsapp (2).png"))); // NOI18N
        BtnWAPerujuk.setMnemonic('U');
        BtnWAPerujuk.setText("Perujuk");
        BtnWAPerujuk.setToolTipText("Alt+U");
        BtnWAPerujuk.setName("BtnWAPerujuk"); // NOI18N
        BtnWAPerujuk.setPreferredSize(new java.awt.Dimension(90, 30));
        BtnWAPerujuk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnWAPerujukActionPerformed(evt);
            }
        });
        FormPass2.add(BtnWAPerujuk);

        BtnWAdrRad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/whatsapp (2).png"))); // NOI18N
        BtnWAdrRad.setMnemonic('U');
        BtnWAdrRad.setText("dr. Radiologi");
        BtnWAdrRad.setToolTipText("Alt+U");
        BtnWAdrRad.setName("BtnWAdrRad"); // NOI18N
        BtnWAdrRad.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnWAdrRad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnWAdrRadActionPerformed(evt);
            }
        });
        FormPass2.add(BtnWAdrRad);

        FormPhoto.add(FormPass2, java.awt.BorderLayout.PAGE_END);

        Scroll4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll4.setName("Scroll4"); // NOI18N
        Scroll4.setOpaque(true);
        Scroll4.setPreferredSize(new java.awt.Dimension(400, 400));

        LoadHTML.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        LoadHTML.setName("LoadHTML"); // NOI18N
        Scroll4.setViewportView(LoadHTML);

        FormPhoto.add(Scroll4, java.awt.BorderLayout.CENTER);

        TabData.addTab("Photo Radiologi", FormPhoto);

        FormHasilRadiologi.setBackground(new java.awt.Color(255, 255, 255));
        FormHasilRadiologi.setBorder(null);
        FormHasilRadiologi.setName("FormHasilRadiologi"); // NOI18N
        FormHasilRadiologi.setPreferredSize(new java.awt.Dimension(115, 73));
        FormHasilRadiologi.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll3.setName("Scroll3"); // NOI18N
        Scroll3.setOpaque(true);

        HasilPeriksa.setBorder(null);
        HasilPeriksa.setColumns(25);
        HasilPeriksa.setRows(40);
        HasilPeriksa.setName("HasilPeriksa"); // NOI18N
        Scroll3.setViewportView(HasilPeriksa);

        FormHasilRadiologi.add(Scroll3, java.awt.BorderLayout.CENTER);

        panelGlass6.setBorder(null);
        panelGlass6.setName("panelGlass6"); // NOI18N
        panelGlass6.setPreferredSize(new java.awt.Dimension(115, 40));

        btnAmbilPhoto1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnAmbilPhoto1.setMnemonic('U');
        btnAmbilPhoto1.setText("Template");
        btnAmbilPhoto1.setToolTipText("Alt+U");
        btnAmbilPhoto1.setName("btnAmbilPhoto1"); // NOI18N
        btnAmbilPhoto1.setPreferredSize(new java.awt.Dimension(105, 30));
        btnAmbilPhoto1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAmbilPhoto1ActionPerformed(evt);
            }
        });
        panelGlass6.add(btnAmbilPhoto1);

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

        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint1.setMnemonic('T');
        BtnPrint1.setText("Cetak");
        BtnPrint1.setToolTipText("Alt+T");
        BtnPrint1.setName("BtnPrint1"); // NOI18N
        BtnPrint1.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrint1ActionPerformed(evt);
            }
        });
        BtnPrint1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrint1KeyPressed(evt);
            }
        });
        panelGlass6.add(BtnPrint1);

        FormHasilRadiologi.add(panelGlass6, java.awt.BorderLayout.PAGE_END);

        TabData.addTab("Hasil Bacaan Radiologi", FormHasilRadiologi);

        FormOrthan.setBackground(new java.awt.Color(255, 255, 255));
        FormOrthan.setBorder(null);
        FormOrthan.setName("FormOrthan"); // NOI18N
        FormOrthan.setPreferredSize(new java.awt.Dimension(115, 73));
        FormOrthan.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll5.setName("Scroll5"); // NOI18N
        Scroll5.setOpaque(true);

        tbListDicom.setName("tbListDicom"); // NOI18N
        tbListDicom.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbListDicomMouseClicked(evt);
            }
        });
        tbListDicom.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbListDicomKeyPressed(evt);
            }
        });
        Scroll5.setViewportView(tbListDicom);

        FormOrthan.add(Scroll5, java.awt.BorderLayout.CENTER);

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

        internalFrame1.add(PanelAccor, java.awt.BorderLayout.EAST);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
/*
private void KdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TKdKeyPressed
    Valid.pindah(evt,BtnCari,Nm);
}//GEN-LAST:event_TKdKeyPressed
*/

    private void btnPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasienActionPerformed
        akses.setform("DlgCariPeriksaRadiologi");
        member.emptTeks();
        member.isCek();
        member.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        member.setLocationRelativeTo(internalFrame1);
        member.setAlwaysOnTop(false);
        member.setVisible(true);
    }//GEN-LAST:event_btnPasienActionPerformed

    private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        akses.setform("DlgCariPeriksaRadiologi");
        pilihan="caripetugas";
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
        NoRawat.setText("");
        kdmem.setText("");
        nmmem.setText("");
        kdptg.setText("");
        nmptg.setText("");
        tampil();
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
        BtnCariActionPerformed(evt);
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            TCari.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            
            Sequel.queryu("delete from temporary_radiologi");
            int row=tabMode.getRowCount();
            for(i=0;i<row;i++){  
                Sequel.menyimpan("temporary_radiologi","'0','"+
                                tabMode.getValueAt(i,0).toString()+"','"+
                                tabMode.getValueAt(i,1).toString()+"','"+
                                tabMode.getValueAt(i,2).toString()+"','"+
                                tabMode.getValueAt(i,3).toString()+"','"+
                                tabMode.getValueAt(i,4).toString()+"','"+
                                tabMode.getValueAt(i,5).toString()+"','"+
                                tabMode.getValueAt(i,6).toString()+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Periksa Radiologi"); 
            }
            
            Map<String, Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            Valid.MyReport("rptDataRadiologi.jasper","report","::[ Data Pemeriksaan Radiologi ]::",param);
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
    if(tabMode.getRowCount()==0){
        JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
        TCari.requestFocus();
    }else if(tbDokter.getSelectedRow()<= -1){
        JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
    }else {
        if(Kd2.getText().equals("")||Petugas.getText().equals("")){
           JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
        }else{
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
                        ttljmdokter=Sequel.cariIsiAngka("select sum(periksa_radiologi.tarif_tindakan_dokter) from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljmpetugas=Sequel.cariIsiAngka("select sum(periksa_radiologi.tarif_tindakan_petugas) from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlkso=Sequel.cariIsiAngka("select sum(periksa_radiologi.kso) from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlbhp=Sequel.cariIsiAngka("select sum(periksa_radiologi.bhp) from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlpendapatan=Sequel.cariIsiAngka("select sum(periksa_radiologi.biaya) from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljasasarana=Sequel.cariIsiAngka("select sum(periksa_radiologi.bagian_rs) from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttljmperujuk=Sequel.cariIsiAngka("select sum(periksa_radiologi.tarif_perujuk) from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                        ttlmenejemen=Sequel.cariIsiAngka("select sum(periksa_radiologi.menejemen) from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                  "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                  "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");

                        status=Sequel.cariIsi("select periksa_radiologi.status from periksa_radiologi where periksa_radiologi.no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                      "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                      "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");

                        if(Sequel.queryu2tf("delete from periksa_radiologi where periksa_radiologi.no_rawat=? and tgl_periksa=? and jam=?",3,new String[]{
                            tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString()
                        })==true){
                            Sequel.queryu2("delete from hasil_radiologi where no_rawat=? and tgl_periksa=? and jam=?",3,new String[]{
                                tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString()
                            });
                            Sequel.queryu2("delete from gambar_radiologi where no_rawat=? and tgl_periksa=? and jam=?",3,new String[]{
                                tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString()
                            });

                            ttlbhp=ttlbhp+Sequel.cariIsiAngka("select sum(beri_bhp_radiologi.total) from beri_bhp_radiologi where no_rawat='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),0) +
                                      "' and tgl_periksa='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),3) +
                                      "' and jam='"+tbDokter.getValueAt(tbDokter.getSelectedRow(),4) +"'");
                            if(Sequel.queryu2tf("delete from beri_bhp_radiologi where no_rawat=? and tgl_periksa=? and jam=?",3,new String[]{
                                tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString()
                            })==false){
                                sukses=false;
                            }
                        }else{
                            sukses=false;
                        } 

                        if(sukses==true){
                            if(status.equals("Ranap")){
                                Sequel.queryu("delete from tampjurnal");    
                                if(ttlpendapatan>0){
                                    Sequel.menyimpan("tampjurnal","'"+Suspen_Piutang_Radiologi_Ranap+"','Suspen Piutang Radiologi Ranap','0','"+ttlpendapatan+"'","kredit=kredit+'"+(ttlpendapatan)+"'","kd_rek='"+Suspen_Piutang_Radiologi_Ranap+"'");     
                                    Sequel.menyimpan("tampjurnal","'"+Radiologi_Ranap+"','Pendapatan Radiologi Rawat Inap','"+ttlpendapatan+"','0'","debet=debet+'"+(ttlpendapatan)+"'","kd_rek='"+Radiologi_Ranap+"'");                              
                                }
                                if(ttljmdokter>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Medik_Dokter_Radiologi_Ranap+"','Beban Jasa Medik Dokter Radiologi Ranap','0','"+ttljmdokter+"'","kredit=kredit+'"+(ttljmdokter)+"'","kd_rek='"+Beban_Jasa_Medik_Dokter_Radiologi_Ranap+"'");    
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Medik_Dokter_Radiologi_Ranap+"','Utang Jasa Medik Dokter Radiologi Ranap','"+ttljmdokter+"','0'","debet=debet+'"+(ttljmdokter)+"'","kd_rek='"+Utang_Jasa_Medik_Dokter_Radiologi_Ranap+"'");                           
                                }
                                if(ttljmpetugas>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Medik_Petugas_Radiologi_Ranap+"','Beban Jasa Medik Petugas Radiologi Ranap','0','"+ttljmpetugas+"'","kredit=kredit+'"+(ttljmpetugas)+"'","kd_rek='"+Beban_Jasa_Medik_Petugas_Radiologi_Ranap+"'");    
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Medik_Petugas_Radiologi_Ranap+"','Utang Jasa Medik Petugas Radiologi Ranap','"+ttljmpetugas+"','0'","debet=debet+'"+(ttljmpetugas)+"'","kd_rek='"+Utang_Jasa_Medik_Petugas_Radiologi_Ranap+"'");                             
                                }
                                if(ttlbhp>0){
                                    Sequel.menyimpan("tampjurnal","'"+HPP_Persediaan_Radiologi_Rawat_Inap+"','HPP Persediaan Radiologi Rawat Inap','0','"+ttlbhp+"'","kredit=kredit+'"+(ttlbhp)+"'","kd_rek='"+HPP_Persediaan_Radiologi_Rawat_Inap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Persediaan_BHP_Radiologi_Rawat_Inap+"','Persediaan BHP Radiologi Rawat Inap','"+ttlbhp+"','0'","debet=debet+'"+(ttlbhp)+"'","kd_rek='"+Persediaan_BHP_Radiologi_Rawat_Inap+"'");                                
                                }
                                if(ttlkso>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Kso_Radiologi_Ranap+"','HPP Persediaan Radiologi Rawat Inap','0','"+ttlkso+"'","kredit=kredit+'"+(ttlkso)+"'","kd_rek='"+Beban_Kso_Radiologi_Ranap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Kso_Radiologi_Ranap+"','Persediaan BHP Radiologi Rawat Inap','"+ttlkso+"','0'","debet=debet+'"+(ttlkso)+"'","kd_rek='"+Utang_Kso_Radiologi_Ranap+"'");                                
                                }
                                if(ttljasasarana>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Sarana_Radiologi_Ranap+"','Beban Jasa Sarana Radiologi Ranap','0','"+ttljasasarana+"'","kredit=kredit+'"+(ttljasasarana)+"'","kd_rek='"+Beban_Jasa_Sarana_Radiologi_Ranap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Sarana_Radiologi_Ranap+"','Utang Jasa Sarana Radiologi Ranap','"+ttljasasarana+"','0'","debet=debet+'"+(ttljasasarana)+"'","kd_rek='"+Utang_Jasa_Sarana_Radiologi_Ranap+"'");                              
                                }
                                if(ttljmperujuk>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Perujuk_Radiologi_Ranap+"','Beban Jasa Perujuk Radiologi Ranap','0','"+ttljmperujuk+"'","kredit=kredit+'"+(ttljmperujuk)+"'","kd_rek='"+Beban_Jasa_Perujuk_Radiologi_Ranap+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Perujuk_Radiologi_Ranap+"','Utang Jasa Perujuk Radiologi Ranap','"+ttljmperujuk+"','0'","debet=debet+'"+(ttljmperujuk)+"'","kd_rek='"+Utang_Jasa_Perujuk_Radiologi_Ranap+"'");                               
                                }
                                if(ttlmenejemen>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Menejemen_Radiologi_Ranap+"','Beban Jasa Menejemen Radiologi Ranap','0','"+ttlmenejemen+"'","kredit=kredit+'"+(ttlmenejemen)+"'","kd_rek='"+Beban_Jasa_Menejemen_Radiologi_Ranap+"'");     
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Menejemen_Radiologi_Ranap+"','Utang Jasa Menejemen Radiologi Ranap','"+ttlmenejemen+"','0'","debet=debet+'"+(ttlmenejemen)+"'","kd_rek='"+Utang_Jasa_Menejemen_Radiologi_Ranap+"'");                               
                                }
                                sukses=jur.simpanJurnal(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),"U","PEMBATALAN PEMERIKSAAN RADIOLOGI RAWAT INAP PASIEN "+tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString()+" OLEH "+akses.getkode());  
                            }else if(status.equals("Ralan")){
                                Sequel.queryu("delete from tampjurnal");    
                                if(ttlpendapatan>0){
                                    Sequel.menyimpan("tampjurnal","'"+Suspen_Piutang_Radiologi_Ralan+"','Suspen Piutang Radiologi Ralan','0','"+ttlpendapatan+"'","kredit=kredit+'"+(ttlpendapatan)+"'","kd_rek='"+Suspen_Piutang_Radiologi_Ralan+"'");     
                                    Sequel.menyimpan("tampjurnal","'"+Radiologi_Ralan+"','Pendapatan Radiologi Rawat Inap','"+ttlpendapatan+"','0'","debet=debet+'"+(ttlpendapatan)+"'","kd_rek='"+Radiologi_Ralan+"'");                              
                                }
                                if(ttljmdokter>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Medik_Dokter_Radiologi_Ralan+"','Beban Jasa Medik Dokter Radiologi Ralan','0','"+ttljmdokter+"'","kredit=kredit+'"+(ttljmdokter)+"'","kd_rek='"+Beban_Jasa_Medik_Dokter_Radiologi_Ralan+"'");    
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Medik_Dokter_Radiologi_Ralan+"','Utang Jasa Medik Dokter Radiologi Ralan','"+ttljmdokter+"','0'","debet=debet+'"+(ttljmdokter)+"'","kd_rek='"+Utang_Jasa_Medik_Dokter_Radiologi_Ralan+"'");                           
                                }
                                if(ttljmpetugas>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Medik_Petugas_Radiologi_Ralan+"','Beban Jasa Medik Petugas Radiologi Ralan','0','"+ttljmpetugas+"'","kredit=kredit+'"+(ttljmpetugas)+"'","kd_rek='"+Beban_Jasa_Medik_Petugas_Radiologi_Ralan+"'");    
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Medik_Petugas_Radiologi_Ralan+"','Utang Jasa Medik Petugas Radiologi Ralan','"+ttljmpetugas+"','0'","debet=debet+'"+(ttljmpetugas)+"'","kd_rek='"+Utang_Jasa_Medik_Petugas_Radiologi_Ralan+"'");                             
                                }
                                if(ttlbhp>0){
                                    Sequel.menyimpan("tampjurnal","'"+HPP_Persediaan_Radiologi_Rawat_Jalan+"','HPP Persediaan Radiologi Rawat Jalan','0','"+ttlbhp+"'","kredit=kredit+'"+(ttlbhp)+"'","kd_rek='"+HPP_Persediaan_Radiologi_Rawat_Jalan+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Persediaan_BHP_Radiologi_Rawat_Jalan+"','Persediaan BHP Radiologi Rawat Jalan','"+ttlbhp+"','0'","debet=debet+'"+(ttlbhp)+"'","kd_rek='"+Persediaan_BHP_Radiologi_Rawat_Jalan+"'");                                
                                }
                                if(ttlkso>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Kso_Radiologi_Ralan+"','HPP Persediaan Radiologi Rawat Inap','0','"+ttlkso+"'","kredit=kredit+'"+(ttlkso)+"'","kd_rek='"+Beban_Kso_Radiologi_Ralan+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Kso_Radiologi_Ralan+"','Persediaan BHP Radiologi Rawat Inap','"+ttlkso+"','0'","debet=debet+'"+(ttlkso)+"'","kd_rek='"+Utang_Kso_Radiologi_Ralan+"'");                                
                                }
                                if(ttljasasarana>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Sarana_Radiologi_Ralan+"','Beban Jasa Sarana Radiologi Ralan','0','"+ttljasasarana+"'","kredit=kredit+'"+(ttljasasarana)+"'","kd_rek='"+Beban_Jasa_Sarana_Radiologi_Ralan+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Sarana_Radiologi_Ralan+"','Utang Jasa Sarana Radiologi Ralan','"+ttljasasarana+"','0'","debet=debet+'"+(ttljasasarana)+"'","kd_rek='"+Utang_Jasa_Sarana_Radiologi_Ralan+"'");                              
                                }
                                if(ttljmperujuk>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Perujuk_Radiologi_Ralan+"','Beban Jasa Perujuk Radiologi Ralan','0','"+ttljmperujuk+"'","kredit=kredit+'"+(ttljmperujuk)+"'","kd_rek='"+Beban_Jasa_Perujuk_Radiologi_Ralan+"'");   
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Perujuk_Radiologi_Ralan+"','Utang Jasa Perujuk Radiologi Ralan','"+ttljmperujuk+"','0'","debet=debet+'"+(ttljmperujuk)+"'","kd_rek='"+Utang_Jasa_Perujuk_Radiologi_Ralan+"'");                               
                                }
                                if(ttlmenejemen>0){
                                    Sequel.menyimpan("tampjurnal","'"+Beban_Jasa_Menejemen_Radiologi_Ralan+"','Beban Jasa Menejemen Radiologi Ralan','0','"+ttlmenejemen+"'","kredit=kredit+'"+(ttlmenejemen)+"'","kd_rek='"+Beban_Jasa_Menejemen_Radiologi_Ralan+"'");     
                                    Sequel.menyimpan("tampjurnal","'"+Utang_Jasa_Menejemen_Radiologi_Ralan+"','Utang Jasa Menejemen Radiologi Ralan','"+ttlmenejemen+"','0'","debet=debet+'"+(ttlmenejemen)+"'","kd_rek='"+Utang_Jasa_Menejemen_Radiologi_Ralan+"'");                               
                                }
                                sukses=jur.simpanJurnal(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),"U","PEMBATALAN PEMERIKSAAN RADIOLOGI RAWAT JALAN PASIEN "+tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString()+" OLEH "+akses.getkode());  
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
            isPhoto();
            panggilPhoto();
            tampilOrthanc();
            perbaruiTombolAccessionOrthanc();
        } catch (java.lang.NullPointerException e) {
        }
    }
}//GEN-LAST:event_tbDokterMouseClicked

private void tbDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDokterKeyPressed
   if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                    javax.swing.SwingUtilities.invokeLater(() -> perbaruiTombolAccessionOrthanc());
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbDokterKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void MnCetakNotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakNotaActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")||Petugas.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
            }else{
                try {
                    ps4=koneksi.prepareStatement(
                        "select periksa_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,"+
                        "periksa_radiologi.dokter_perujuk,periksa_radiologi.kd_dokter,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,pasien.alamat,dokter.nm_dokter from periksa_radiologi inner join reg_periksa inner join pasien inner join petugas  inner join dokter "+
                        "on periksa_radiologi.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and periksa_radiologi.nip=petugas.nip and periksa_radiologi.kd_dokter=dokter.kd_dokter where "+
                        "periksa_radiologi.tgl_periksa=? and periksa_radiologi.jam=? and periksa_radiologi.no_rawat=? group by concat(periksa_radiologi.no_rawat,periksa_radiologi.tgl_periksa,periksa_radiologi.jam)");
                    try {
                        ps4.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                        ps4.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                        ps4.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                        rs=ps4.executeQuery();
                        while(rs.next()){
                            Sequel.queryu("delete from temporary_radiologi");
                            koneksi.setAutoCommit(false);
                            ps2=koneksi.prepareStatement(
                                "select jns_perawatan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.biaya from periksa_radiologi inner join jns_perawatan_radiologi "+
                                "on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw where periksa_radiologi.no_rawat=? and periksa_radiologi.tgl_periksa=? "+
                                "and periksa_radiologi.jam=?");   
                            try {
                                ps2.setString(1,rs.getString("no_rawat"));
                                ps2.setString(2,rs.getString("tgl_periksa"));
                                ps2.setString(3,rs.getString("jam"));
                                rs2=ps2.executeQuery();
                                ttl=0;
                                while(rs2.next()){
                                    item=rs2.getDouble("biaya");
                                    ttl=ttl+item;                    
                                    Sequel.menyimpan("temporary_radiologi","'0','"+rs2.getString("nm_perawatan")+"','"+item+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Transaksi Biaya Lab");
                                } 
                            } catch (Exception e) {
                                System.out.println("simrskhanza.DlgCariPeriksaRadiologi.MnCetakNotaActionPerformed() ps2 : "+e);
                            } finally{
                                if(rs2!=null){
                                    rs2.close();
                                }
                                if(ps2!=null){
                                    ps2.close();
                                }
                            }

                            Sequel.menyimpan("temporary_radiologi","'0','Total Biaya Pemeriksaan Radiologi','"+ttl+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','',''","Transaksi Biaya Lab");
                            Valid.panggilUrl("billing/LaporanBiayaRadiologi.php?norm="+rs.getString("no_rkm_medis")+"&pasien="+rs.getString("nm_pasien").replaceAll(" ","_")
                                    +"&tanggal="+rs.getString("tgl_periksa")+"&jam="+rs.getString("jam")+"&pjlab="+rs.getString("nm_dokter").replaceAll(" ","_")
                                    +"&petugas="+rs.getString("nama").replaceAll(" ","_")+"&kasir="+Sequel.cariIsi("select pegawai.nama from pegawai where pegawai.nik=?",akses.getkode())
                                    +"&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB());
                            koneksi.setAutoCommit(true);  
                        }
                    } catch (Exception e) {
                        System.out.println("simrskhanza.DlgCariPeriksaRadiologi.MnCetakNotaActionPerformed() ps4 : "+e);
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
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_MnCetakNotaActionPerformed

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrint1ActionPerformed
        if(Kd2.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            pemeriksaan="";
            try {
                ps2=koneksi.prepareStatement(
                            "select jns_perawatan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.biaya,"+
                            "periksa_radiologi.kd_dokter,periksa_radiologi.nip,periksa_radiologi.proyeksi,periksa_radiologi.kV,periksa_radiologi.mAS,periksa_radiologi.FFD,"+
                            "periksa_radiologi.BSF,periksa_radiologi.inak,periksa_radiologi.jml_penyinaran,periksa_radiologi.dosis from periksa_radiologi inner join jns_perawatan_radiologi "+
                            "on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw where periksa_radiologi.no_rawat=? and periksa_radiologi.tgl_periksa=? "+
                            "and periksa_radiologi.jam=?"); 
                try {
                    ps2.setString(1,Kd2.getText());
                    ps2.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps2.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    rs2=ps2.executeQuery();
                    while(rs2.next()){
                        pemeriksaan=rs2.getString("nm_perawatan")+", "+pemeriksaan;
                        kdpenjab=rs2.getString("kd_dokter");
                        kdpetugas=rs2.getString("nip");
                    }
                } catch (Exception e) {
                    System.out.println("simrskhanza.DlgCariPeriksaRadiologi.BtnPrint1ActionPerformed() ps2 : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                    if(ps2!=null){
                        ps2.close();
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Notifikasi Pemeriksaan : "+e);
            }          
            Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=? ",NoRM,Kd2.getText());
            Sequel.cariIsi("select pasien.jk from pasien where pasien.no_rkm_medis=? ",Jk,NoRM.getText());
            Sequel.cariIsi("select umur from pasien where no_rkm_medis=?",Umur,NoRM.getText());
            Sequel.cariIsi("select concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat from pasien inner join kelurahan inner join kecamatan inner join kabupaten on pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where no_rkm_medis=? ",Alamat,NoRM.getText());
            
            kamar=Sequel.cariIsi("select ifnull(kd_kamar,'') from kamar_inap where no_rawat='"+Kd2.getText()+"' order by tgl_masuk desc limit 1");
            if(!kamar.equals("")){
                namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                            " where kamar.kd_kamar='"+kamar+"' ");            
                kamar="Kamar";
            }else if(kamar.equals("")){
                kamar="Poli";
                namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                            "where reg_periksa.no_rawat='"+Kd2.getText()+"'");
            }
            Map<String, Object> param = new HashMap<>();
            param.put("noperiksa",Kd2.getText());
            param.put("norm",NoRM.getText());
            param.put("namapasien",Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=? ",NoRM.getText()));
            param.put("jkel",Jk.getText());
            param.put("umur",Umur.getText());
            param.put("nik",Sequel.cariIsi("select no_ktp from pasien where pasien.no_rkm_medis=? ",NoRM.getText()));
            param.put("lahir",Sequel.cariIsi("select DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') from pasien where pasien.no_rkm_medis=? ",NoRM.getText()));
            param.put("pengirim",tbDokter.getValueAt(tbDokter.getSelectedRow(),5).toString());
            param.put("tanggal",Valid.SetTgl3(tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString()));
            param.put("penjab",tbDokter.getValueAt(tbDokter.getSelectedRow(),6).toString());
            param.put("petugas",tbDokter.getValueAt(tbDokter.getSelectedRow(),2).toString());
            param.put("alamat",Alamat.getText());
            param.put("kamar",kamar);
            param.put("namakamar",namakamar);
            param.put("pemeriksaan",pemeriksaan);
            param.put("jam",tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());
            param.put("hasil",HasilPeriksa.getText());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",kdpenjab);
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbDokter.getValueAt(tbDokter.getSelectedRow(),6).toString()+"\nID "+(finger.equals("")?kdpenjab:finger)+"\n"+Valid.SetTgl3(tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString()));  
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",kdpetugas);
            param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbDokter.getValueAt(tbDokter.getSelectedRow(),2).toString()+"\nID "+(finger.equals("")?kdpetugas:finger)+"\n"+Valid.SetTgl3(tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString()));  

            pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih hasil pemeriksaan..!","Hasil Pemeriksaan",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Model 1","Model 2", "Model 3","PDF Model 1","PDF Model 2","PDF Model 3"},"Model 1");
            switch (pilihan) {
                case "Model 1":
                      Valid.MyReport("rptPeriksaRadiologi.jasper","report","::[ Pemeriksaan Radiologi ]::",param);
                      break;
                case "Model 2":
                      Valid.MyReport("rptPeriksaRadiologi2.jasper","report","::[ Pemeriksaan Radiologi ]::",param);
                      break;
                case "Model 3":
                      Valid.MyReport("rptPeriksaRadiologi3.jasper","report","::[ Pemeriksaan Radiologi ]::",param);
                      break;
                case "PDF Model 1":
                      Valid.MyReportPDF("rptPeriksaRadiologi.jasper","report","::[ Pemeriksaan Radiologi ]::",param);
                      break;
                case "PDF Model 2":
                      Valid.MyReportPDF("rptPeriksaRadiologi2.jasper","report","::[ Pemeriksaan Radiologi ]::",param);
                      break;
                case "PDF Model 3":
                      Valid.MyReportPDF("rptPeriksaRadiologi3.jasper","report","::[ Pemeriksaan Radiologi ]::",param);
                      break;
            }                        
            
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnPrint1ActionPerformed

    private void BtnPrint1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrint1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
          //  Valid.pindah(evt, BtnBatal,BtnCari);
        }
    }//GEN-LAST:event_BtnPrint1KeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(Kd2.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
        }else{
            if(HasilPeriksa.getText().equals("")){
                Sequel.queryu2("delete from hasil_radiologi where no_rawat=? and tgl_periksa=? and jam=?",3,new String[]{
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString()
                });
            }else{
                if(Sequel.menyimpantf2("hasil_radiologi","?,?,?,?","Hasil Pemeriksaan", 4,new String[]{
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString(),HasilPeriksa.getText()
                })==false){
                    Sequel.queryu2("update hasil_radiologi set hasil=? where no_rawat=? and tgl_periksa=? and jam=?",4,new String[]{
                        HasilPeriksa.getText(),tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),
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

    private void PenjabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PenjabKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PenjabKeyPressed

    private void BtnSimpan4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpan4ActionPerformed
        if(tbDokter.getSelectedRow()>-1){
            if(!tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString().equals("")){
                Sequel.queryu2("update periksa_radiologi set nip=?,dokter_perujuk=?,kd_dokter=? where no_rawat=? and tgl_periksa=? and jam=?",6,new String[]{
                    KdPtgUbah.getText(),KodePerujuk.getText(),KodePj.getText(),tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString(),
                    tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString()
                }); 
                tampil();
                dokter.dispose();
                petugas.dispose();
                WindowGantiDokterParamedis.dispose();
            }
        }                
    }//GEN-LAST:event_BtnSimpan4ActionPerformed

    private void BtnCloseIn4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseIn4ActionPerformed
        dokter.dispose();
        petugas.dispose();
        WindowGantiDokterParamedis.dispose();
    }//GEN-LAST:event_BtnCloseIn4ActionPerformed

    private void btnDokterPjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDokterPjActionPerformed
        pilihan="penjab";
        akses.setform("DlgCariPeriksaRadiologi");
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_btnDokterPjActionPerformed

    private void btnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDokterActionPerformed
        pilihan="perujuk";
        akses.setform("DlgCariPeriksaRadiologi");
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_btnDokterActionPerformed

    private void KodePerujukKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KodePerujukKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            NmPerujuk.setText(dokter.tampil3(KodePerujuk.getText()));
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnDokterActionPerformed(null);
        }else{
            Valid.pindah(evt,KdPtgUbah,BtnSimpan4);
        }
    }//GEN-LAST:event_KodePerujukKeyPressed

    private void KdPtgUbahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPtgUbahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            NmPtgUbah.setText(petugas.tampil3(KdPtgUbah.getText()));
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnPetugasActionPerformed(null);
        }else{
            Valid.pindah(evt,KodePj,KodePerujuk);
        }
    }//GEN-LAST:event_KdPtgUbahKeyPressed

    private void btnPetugas1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugas1ActionPerformed
        akses.setform("DlgCariPeriksaRadiologi");
        pilihan="ubahpetugas";
        petugas.emptTeks();
        petugas.isCek();
        petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setAlwaysOnTop(false);
        petugas.setVisible(true);
    }//GEN-LAST:event_btnPetugas1ActionPerformed

    private void MnUbahDokterPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnUbahDokterPetugasActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")||Petugas.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
            }else{
                if(Sequel.cariRegistrasi(tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString())>0){
                    JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi, data tidak boleh diubah.\nSilahkan hubungi bagian kasir/keuangan ..!!");
                    TCari.requestFocus();
                }else{
                    panggilMedis();
                }
            } 
        }
    }//GEN-LAST:event_MnUbahDokterPetugasActionPerformed

    private void ppBerkasDigitalBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBerkasDigitalBtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")||Petugas.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
            }else{
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
            } 
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_ppBerkasDigitalBtnPrintActionPerformed

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        if(tbDokter.getSelectedRow()!= -1){
            isPhoto();
            panggilPhoto();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
        }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void btnAmbilPhotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAmbilPhotoActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")||Petugas.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
            }else{
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Valid.panggilUrl("radiologi/login.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString()+"&tanggal="+tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString()+"&jam="+tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                this.setCursor(Cursor.getDefaultCursor()); 
            }         
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnAmbilPhotoActionPerformed

    private void BtnRefreshPhotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRefreshPhotoActionPerformed
        panggilPhoto();
    }//GEN-LAST:event_BtnRefreshPhotoActionPerformed

    private void btnAmbilPhoto1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAmbilPhoto1ActionPerformed
        akses.setform("DlgCariPeriksaRadiologi");
        templatehasil.emptTeks();
        templatehasil.isCek();
        templatehasil.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        templatehasil.setLocationRelativeTo(internalFrame1);
        templatehasil.setVisible(true);
    }//GEN-LAST:event_btnAmbilPhoto1ActionPerformed

    private void btnDicomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDicomActionPerformed
        if(tabModeDicom.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else {
            if(tbListDicom.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                OrthancDICOM orthan=new OrthancDICOM(null,false);
//                orthan.setJudul("::[ DICOM Orthanc Pasien "+tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString()+", Series "+tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString()+" ]::",tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString().replaceAll("/",""),tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString(),tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                try {
                    System.out.println("URL : "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/web-viewer/app/viewer.html?series="+tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString());
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

    private void tbListDicomMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbListDicomMouseClicked
        if(tabModeDicom.getRowCount()!=0){
            if(evt.getClickCount()==2){
                btnDicomActionPerformed(null);
            }
        }
    }//GEN-LAST:event_tbListDicomMouseClicked

    private void tbListDicomKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbListDicomKeyPressed
        if(tabModeDicom.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                btnDicomActionPerformed(null);
            }
        }
    }//GEN-LAST:event_tbListDicomKeyPressed

    private void ppRiwayatBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppRiwayatBtnPrintActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")||Petugas.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
            }else{
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMRiwayatPerawatan resume=new RMRiwayatPerawatan(null,true);
                pilihan=Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=?",Kd2.getText());
                resume.setNoRm(pilihan,tbDokter.getValueAt(tbDokter.getSelectedRow(),1).toString().replaceAll(pilihan+" ",""));
                resume.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                resume.setLocationRelativeTo(internalFrame1);
                resume.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            } 
        }
    }//GEN-LAST:event_ppRiwayatBtnPrintActionPerformed

    private void BtnWAPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnWAPasienActionPerformed
        // TODO add your handling code here:

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")||Petugas.getText().equals("")){
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!");
            }else{

                try {
                    ps = koneksi.prepareStatement("SELECT pasien.no_rkm_medis, pasien.nm_pasien, pasien.no_tlp, gambar_radiologi.lokasi_gambar, reg_periksa.no_rawat, gambar_radiologi.tgl_periksa, gambar_radiologi.jam, hasil_radiologi.hasil \n" +
                        "FROM pasien INNER JOIN reg_periksa ON  pasien.no_rkm_medis = reg_periksa.no_rkm_medis \n" +
                        "INNER JOIN gambar_radiologi ON reg_periksa.no_rawat = gambar_radiologi.no_rawat \n" +
                        "INNER JOIN hasil_radiologi ON reg_periksa.no_rawat = hasil_radiologi.no_rawat \n" +
                        "WHERE reg_periksa.no_rawat=? AND gambar_radiologi.tgl_periksa =? and gambar_radiologi.jam =? AND pasien.no_rkm_medis = reg_periksa.no_rkm_medis " +
                        "AND gambar_radiologi.no_rawat = reg_periksa.no_rawat AND hasil_radiologi.tgl_periksa=gambar_radiologi.tgl_periksa and hasil_radiologi.jam = gambar_radiologi.jam " +
                        "ORDER BY gambar_radiologi.tgl_periksa, gambar_radiologi.jam");

                } catch (SQLException ex) {
                    Logger.getLogger(DlgCariPeriksaRadiologi.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    ps.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                    ps.setString(2, tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString());
                    ps.setString(3, tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
                    rs = ps.executeQuery();
                    htmlContent = new StringBuilder();
                    // start while
                    while (rs.next()) {

                        try {

                            String url = "https://api.fonnte.com/send";
                            String tokenWA = koneksiDBSalim.TOKENWA();
                            String noWa = rs.getString("no_tlp");
                            String countryCode = "62";
                            String template = Sequel.cariIsi("select wa_template from wa_template where no_template=?", "RAD");
                            String pesan = template.replace("{nama}", rs.getString("nm_pasien"))
                            .replace("{rm}", rs.getString("no_rkm_medis"))
                            .replace("{antrian}", "")
                            .replace("{poli}", "")
                            .replace("{perujuk}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 5).toString())
                            .replace("{dokter}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 6).toString())
                            .replace("{hasil}", rs.getString("hasil"))
                            .replace("{tanggal}", rs.getString("tgl_periksa"));
                            String simpanPesan = pesan.replace("*", "").replace("_", "").replace("{", "").replace("}", "");
                            String urlHasil = koneksiDBSalim.URLWEBAPPS() + "radiologi/" + rs.getString("lokasi_gambar");
                            String postData = "target=" + noWa + "&message=" + pesan + "&url=" + urlHasil + "&typing=true&countryCode=" + countryCode;
                            URL obj = new URL(url);
                            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Authorization", tokenWA);
                            con.setDoOutput(true);

                            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                                wr.write(postData.getBytes(StandardCharsets.UTF_8));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                                String inputLine;
                                StringBuilder response = new StringBuilder();

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }

                                System.out.println(response.toString());

                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode jsonNode = objectMapper.readTree(response.toString());

                                if ("false".equals(jsonNode.get("status").asText())) {
                                    JOptionPane.showMessageDialog(null, "Gagal mengirim, mohon cek koneksi Whatsapp Gateway !!", "Error", JOptionPane.ERROR_MESSAGE);
                                } else if ("true".equals(jsonNode.get("status").asText())) {
                                    LocalDateTime now = LocalDateTime.now();
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    String dateNow = now.format(formatter);
                                    Sequel.queryu("INSERT INTO wa_report VALUES ('" + jsonNode.get("id").get(0).asText() + "', '" + rs.getString("no_rkm_medis") + "', '" + noWa + "', '" + akses.getkode() + "', 'Kirim Hasil Radilogi Pasien', '" + jsonNode.get("target").get(0).asText() + "', '" + simpanPesan + "', '', '" + jsonNode.get("status").asText() + "', '', '" + dateNow + "', '" + dateNow + "')");
                                    JOptionPane.showMessageDialog(null, "Berhasil Mengirim Pesan !!", "Success", JOptionPane.INFORMATION_MESSAGE);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    // end while

                } catch (Exception e) {
                    System.out.println("Notif : " + e);
                }

            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnWAPasienActionPerformed

    private void BtnWAPerujukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnWAPerujukActionPerformed
        // TODO add your handling code here:

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")||Petugas.getText().equals("")){
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!");
            }else{

                try {
                    ps = koneksi.prepareStatement("SELECT pasien.no_rkm_medis, pasien.nm_pasien, pasien.no_tlp, gambar_radiologi.lokasi_gambar, reg_periksa.no_rawat, gambar_radiologi.tgl_periksa, gambar_radiologi.jam, hasil_radiologi.hasil \n" +
                        "FROM pasien INNER JOIN reg_periksa ON  pasien.no_rkm_medis = reg_periksa.no_rkm_medis \n" +
                        "INNER JOIN gambar_radiologi ON reg_periksa.no_rawat = gambar_radiologi.no_rawat \n" +
                        "INNER JOIN hasil_radiologi ON reg_periksa.no_rawat = hasil_radiologi.no_rawat \n" +
                        "WHERE reg_periksa.no_rawat=? AND gambar_radiologi.tgl_periksa =? and gambar_radiologi.jam =? AND pasien.no_rkm_medis = reg_periksa.no_rkm_medis " +
                        "AND gambar_radiologi.no_rawat = reg_periksa.no_rawat AND hasil_radiologi.tgl_periksa=gambar_radiologi.tgl_periksa and hasil_radiologi.jam = gambar_radiologi.jam " +
                        "ORDER BY gambar_radiologi.tgl_periksa, gambar_radiologi.jam");

                } catch (SQLException ex) {
                    Logger.getLogger(DlgCariPeriksaRadiologi.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    ps.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                    ps.setString(2, tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString());
                    ps.setString(3, tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
                    rs = ps.executeQuery();
                    htmlContent = new StringBuilder();
                    // start while
                    while (rs.next()) {

                        try {

                            String url = "https://api.fonnte.com/send";
                            String tokenWA = koneksiDBSalim.TOKENWA();
                            String countryCode = "62";
                            String template = Sequel.cariIsi("select wa_template from wa_template where no_template=?", "RADP");
                            String noWa = Sequel.cariIsi("select no_telp from dokter where nm_dokter=?", tbDokter.getValueAt(tbDokter.getSelectedRow(), 5).toString());
                            String pesan = template.replace("{nama}", rs.getString("nm_pasien"))
                            .replace("{rm}", rs.getString("no_rkm_medis"))
                            .replace("{antrian}", "")
                            .replace("{poli}", "")
                            .replace("{perujuk}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 5).toString())
                            .replace("{dokter}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 6).toString())
                            .replace("{hasil}", rs.getString("hasil"))
                            .replace("{tanggal}", rs.getString("tgl_periksa"));
                            String simpanPesan = pesan.replace("*", "").replace("_", "").replace("{", "").replace("}", "");
                            String urlHasil = koneksiDBSalim.URLWEBAPPS() + "radiologi/" + rs.getString("lokasi_gambar");
                            String postData = "target=" + noWa + "&message=" + pesan + "&url=" + urlHasil + "&typing=true&countryCode=" + countryCode;
                            URL obj = new URL(url);
                            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Authorization", tokenWA);
                            con.setDoOutput(true);

                            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                                wr.write(postData.getBytes(StandardCharsets.UTF_8));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                                String inputLine;
                                StringBuilder response = new StringBuilder();

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }

                                System.out.println(response.toString());

                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode jsonNode = objectMapper.readTree(response.toString());

                                if ("false".equals(jsonNode.get("status").asText())) {
                                    JOptionPane.showMessageDialog(null, "Gagal mengirim, mohon cek koneksi Whatsapp Gateway !!", "Error", JOptionPane.ERROR_MESSAGE);
                                } else if ("true".equals(jsonNode.get("status").asText())) {
                                    LocalDateTime now = LocalDateTime.now();
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    String dateNow = now.format(formatter);
                                    Sequel.queryu("INSERT INTO wa_report VALUES ('" + jsonNode.get("id").get(0).asText() + "', '" + rs.getString("no_rkm_medis") + "', '" + noWa + "', '" + akses.getkode() + "', 'Kirim Hasil Radilogi Pasien', '" + jsonNode.get("target").get(0).asText() + "', '" + simpanPesan + "', '', '" + jsonNode.get("status").asText() + "', '', '" + dateNow + "', '" + dateNow + "')");
                                    JOptionPane.showMessageDialog(null, "Berhasil Mengirim Pesan !!", "Success", JOptionPane.INFORMATION_MESSAGE);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    // end while

                } catch (Exception e) {
                    System.out.println("Notif : " + e);
                }

            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnWAPerujukActionPerformed

    private void BtnWAdrRadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnWAdrRadActionPerformed
        // TODO add your handling code here:

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else if(tbDokter.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
        }else {
            if(Kd2.getText().equals("")||Petugas.getText().equals("")){
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!");
            }else{

                try {
                    ps = koneksi.prepareStatement("SELECT pasien.no_rkm_medis, pasien.nm_pasien, pasien.no_tlp, gambar_radiologi.lokasi_gambar, reg_periksa.no_rawat, gambar_radiologi.tgl_periksa, gambar_radiologi.jam, hasil_radiologi.hasil \n" +
                        "FROM pasien INNER JOIN reg_periksa ON  pasien.no_rkm_medis = reg_periksa.no_rkm_medis \n" +
                        "INNER JOIN gambar_radiologi ON reg_periksa.no_rawat = gambar_radiologi.no_rawat \n" +
                        "INNER JOIN hasil_radiologi ON reg_periksa.no_rawat = hasil_radiologi.no_rawat \n" +
                        "WHERE reg_periksa.no_rawat=? AND gambar_radiologi.tgl_periksa =? and gambar_radiologi.jam =? AND pasien.no_rkm_medis = reg_periksa.no_rkm_medis " +
                        "AND gambar_radiologi.no_rawat = reg_periksa.no_rawat AND hasil_radiologi.tgl_periksa=gambar_radiologi.tgl_periksa and hasil_radiologi.jam = gambar_radiologi.jam " +
                        "ORDER BY gambar_radiologi.tgl_periksa, gambar_radiologi.jam");

                } catch (SQLException ex) {
                    Logger.getLogger(DlgCariPeriksaRadiologi.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    ps.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                    ps.setString(2, tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString());
                    ps.setString(3, tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
                    rs = ps.executeQuery();
                    htmlContent = new StringBuilder();
                    // start while
                    while (rs.next()) {

                        try {

                            String url = "https://api.fonnte.com/send";
                            String tokenWA = koneksiDBSalim.TOKENWA();
                            String countryCode = "62";
                            String template = Sequel.cariIsi("select wa_template from wa_template where no_template=?", "RADR");
                            String noWa = Sequel.cariIsi("select no_telp from dokter where nm_dokter=?", tbDokter.getValueAt(tbDokter.getSelectedRow(), 6).toString());
                            String pesan = template.replace("{nama}", rs.getString("nm_pasien"))
                            .replace("{rm}", rs.getString("no_rkm_medis"))
                            .replace("{antrian}", "")
                            .replace("{poli}", "")
                            .replace("{perujuk}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 5).toString())
                            .replace("{dokter}", tbDokter.getValueAt(tbDokter.getSelectedRow(), 6).toString())
                            .replace("{hasil}", rs.getString("hasil"))
                            .replace("{tanggal}", rs.getString("tgl_periksa"));
                            String simpanPesan = pesan.replace("*", "").replace("_", "").replace("{", "").replace("}", "");
                            String urlHasil = koneksiDBSalim.URLWEBAPPS() + "radiologi/" + rs.getString("lokasi_gambar");
                            String postData = "target=" + noWa + "&message=" + pesan + "&url=" + urlHasil + "&typing=true&countryCode=" + countryCode;
                            URL obj = new URL(url);
                            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Authorization", tokenWA);
                            con.setDoOutput(true);

                            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                                wr.write(postData.getBytes(StandardCharsets.UTF_8));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                                String inputLine;
                                StringBuilder response = new StringBuilder();

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }

                                System.out.println(response.toString());

                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode jsonNode = objectMapper.readTree(response.toString());

                                if ("false".equals(jsonNode.get("status").asText())) {
                                    JOptionPane.showMessageDialog(null, "Gagal mengirim, mohon cek koneksi Whatsapp Gateway !!", "Error", JOptionPane.ERROR_MESSAGE);
                                } else if ("true".equals(jsonNode.get("status").asText())) {
                                    LocalDateTime now = LocalDateTime.now();
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    String dateNow = now.format(formatter);
                                    Sequel.queryu("INSERT INTO wa_report VALUES ('" + jsonNode.get("id").get(0).asText() + "', '" + rs.getString("no_rkm_medis") + "', '" + noWa + "', '" + akses.getkode() + "', 'Kirim Hasil Radilogi Pasien', '" + jsonNode.get("target").get(0).asText() + "', '" + simpanPesan + "', '', '" + jsonNode.get("status").asText() + "', '', '" + dateNow + "', '" + dateNow + "')");
                                    JOptionPane.showMessageDialog(null, "Berhasil Mengirim Pesan !!", "Success", JOptionPane.INFORMATION_MESSAGE);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    // end while

                } catch (Exception e) {
                    System.out.println("Notif : " + e);
                }

            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnWAdrRadActionPerformed

    private void BtnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUploadActionPerformed

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
//        String timestamp = sdf.format(new Date());
//
//        FileName = timestamp + "_" + tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().replace("/", "") + "_HasilRadiologi";
//
//        CreatePDF(FileName);
//        ConvertPDFtoJPG(FileName);
//        UploadJPG(FileName, "berkasrawat/pages/upload/");
//        HapusJPG();
//
//        ppBerkasDigitalBtnPrintActionPerformed(evt);

    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
    String timestamp = sdf.format(new Date());

    String noRawat = tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().replace("/", "");
    String kdJenisPrw = tbDokter.getValueAt(tbDokter.getSelectedRow()+2, 2).toString();
    String nmTindakan = "";

    try {
        ps = koneksi.prepareStatement(
            "SELECT jp.nm_perawatan FROM periksa_radiologi pr " +
            "INNER JOIN jns_perawatan_radiologi jp ON pr.kd_jenis_prw = jp.kd_jenis_prw " +
            "WHERE pr.no_rawat = ? AND pr.kd_jenis_prw = ?"
        );
        ps.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
        ps.setString(2, kdJenisPrw);
        rs = ps.executeQuery();
        if(rs.next()) {
            nmTindakan = rs.getString("nm_perawatan");
            nmTindakan = nmTindakan.replaceAll("[^a-zA-Z0-9]", "_"); // Biar aman buat nama file
        }
    } catch (Exception e) {
        System.out.println("Notif : "+e);
    } finally {
        if(rs != null){try {
            rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(DlgCariPeriksaRadiologi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(ps != null){try {
            ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(DlgCariPeriksaRadiologi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    if(nmTindakan.isEmpty()){
        nmTindakan = "HasilRadiologi"; // fallback kalau nggak ketemu tindakan
    }

    FileName = timestamp + "_" + noRawat + "_" + nmTindakan;

    CreatePDF(FileName);
    ConvertPDFtoJPG(FileName);
    UploadJPG(FileName, "berkasrawat/pages/upload/");
    HapusJPG();

    ppBerkasDigitalBtnPrintActionPerformed(evt);

    }//GEN-LAST:event_BtnUploadActionPerformed

    private void BtnUploadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUploadKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUploadKeyPressed

    private void BtnGenerateTokenPACSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGenerateTokenPACSActionPerformed
        int baris = getBarisPemeriksaanTerpilih();
        if (baris == -1) {
            JOptionPane.showMessageDialog(null,
                    "Maaf, silahkan pilih pemeriksaan radiologi terlebih dahulu..!!");
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (!pastikanTabelAccessionOrthanc()) {
                JOptionPane.showMessageDialog(null,
                        "Struktur tabel radiologi_pacs_token belum dapat "
                        + "disiapkan..!!\nPeriksa hak ALTER database dan log aplikasi.");
                return;
            }

            String noRawat = nilaiTabel(tbDokter, baris, 0);
            String tglPeriksa = nilaiTabel(tbDokter, baris, 3);
            String jamPeriksa = nilaiTabel(tbDokter, baris, 4);
            String kdJenisPrw = pilihKodeJenisPerawatan(baris, true);
            if (kdJenisPrw.equals("")) {
                return;
            }

            String[] dataPermintaan = pilihDataPermintaanRadiologi(
                    noRawat, tglPeriksa, jamPeriksa, kdJenisPrw, true);
            String noOrder = dataPermintaan[0];
            String kdJenisPrwPermintaan = dataPermintaan[1];
            if (noOrder.equals("")) {
                return;
            }

            String accessionNumber = bentukAccessionNumber(
                    noOrder, kdJenisPrwPermintaan);
            String patientId = teksAman(Sequel.cariIsi(
                    "select no_rkm_medis from reg_periksa where no_rawat=?",
                    noRawat
            ));
            if (patientId.equals("")) {
                JOptionPane.showMessageDialog(null,
                        "Patient ID / No. RM pasien tidak ditemukan..!!");
                return;
            }

            String studyDate = tglPeriksa.replace("-", "");
            JsonNode studies = cariStudyOrthanc(
                    accessionNumber, patientId, studyDate);
            if (jumlahDataOrthanc(studies) == 0) {
                // Accession Number adalah acuan utama. Pencarian kedua tidak
                // membatasi tanggal agar study lama yang sudah ditandai tetap ditemukan.
                studies = cariStudyOrthanc(accessionNumber, "", "");
            }

            JsonNode study = null;
            boolean accessionDiperbaiki = false;
            if (jumlahDataOrthanc(studies) > 0) {
                study = pilihStudyOrthanc(
                        studies,
                        "Pilih Study dengan Accession Number " + accessionNumber
                );
                if (study == null) {
                    return;
                }
            } else {
                JsonNode kandidat = cariStudyOrthanc("", patientId, studyDate);
                String infoTanggal = "";
                if (jumlahDataOrthanc(kandidat) == 0) {
                    kandidat = cariStudyOrthanc("", patientId, "");
                    infoTanggal = "\nStudy pada tanggal pemeriksaan tidak ditemukan."
                            + "\nSilahkan pastikan pilihan study di bawah ini.";
                }

                if (jumlahDataOrthanc(kandidat) == 0) {
                    JOptionPane.showMessageDialog(null,
                            "Study Orthanc untuk Patient ID " + patientId
                            + " tidak ditemukan..!!");
                    return;
                }

                study = pilihStudyOrthanc(
                        kandidat,
                        "Pilih Study Orthanc yang akan diperbaiki Accession Number"
                );
                if (study == null) {
                    return;
                }

                if (!studyOrthancBolehDipakai(
                        study.path("ID").asText(), accessionNumber)) {
                    return;
                }

                String accessionLama = tagStudyOrthanc(
                        study, "AccessionNumber");
                String peringatan = "";
                if (accessionLama.equals("")) {
                    peringatan = "\n\nAccession Number study saat ini masih "
                            + "kosong dan akan diisi.";
                } else if (!accessionLama.equals(accessionNumber)) {
                    peringatan = "\n\nPERHATIAN: Accession Number study saat ini adalah "
                            + accessionLama + " dan akan diganti.";
                }

                int jawaban = JOptionPane.showConfirmDialog(
                        null,
                        "Perbaiki Accession Number pada study Orthanc berikut?"
                        + infoTanggal
                        + "\n\nAccession Number : " + accessionNumber
                        + "\nNo. Order        : " + noOrder
                        + "\nKode Pemeriksaan : " + kdJenisPrw
                        + infoKodePermintaan(
                                kdJenisPrw, kdJenisPrwPermintaan)
                        + "\nPatient ID       : " + tagPatientOrthanc(
                                study, "PatientID")
                        + "\nStudy Date       : " + tagStudyOrthanc(
                                study, "StudyDate")
                        + "\nOrthanc Study ID : " + study.path("ID").asText()
                        + peringatan,
                        "Konfirmasi Perbaiki Accession Number",
                        JOptionPane.YES_NO_OPTION,
                        accessionLama.equals("")
                                ? JOptionPane.QUESTION_MESSAGE
                                : JOptionPane.WARNING_MESSAGE
                );
                if (jawaban != JOptionPane.YES_OPTION) {
                    return;
                }

                if (!ubahAccessionOrthanc(
                        study.path("ID").asText(), accessionNumber)) {
                    JOptionPane.showMessageDialog(null,
                            "Orthanc menolak perbaikan Accession Number..!!");
                    return;
                }
                accessionDiperbaiki = true;

                // /modify membuat resource study baru. Cari ulang agar ID study
                // yang disimpan selalu menunjuk resource hasil modifikasi.
                studies = cariStudyOrthanc(
                        accessionNumber, patientId, studyDate);
                if (jumlahDataOrthanc(studies) == 0) {
                    studies = cariStudyOrthanc(accessionNumber, "", "");
                }
                study = pilihStudyOrthanc(
                        studies,
                        "Verifikasi Study hasil perbaikan Accession Number"
                );
                if (study == null) {
                    JOptionPane.showMessageDialog(null,
                            "Accession Number sudah diperbaiki, tetapi study hasil "
                            + "modifikasi belum dapat diverifikasi.\n"
                            + "Data belum disimpan. Silahkan coba kembali..!!");
                    return;
                }
            }

            if (!studyOrthancBolehDipakai(
                    study.path("ID").asText(), accessionNumber)) {
                return;
            }
            if (!konfirmasiPatientStudy(
                    study, patientId, accessionNumber,
                    "Study akan disimpan sebagai mapping pemeriksaan.")) {
                return;
            }
            simpanAccessionOrthanc(
                    noRawat, tglPeriksa, jamPeriksa, noOrder,
                    kdJenisPrw, accessionNumber,
                    study.path("ID").asText()
            );
            tandaiAccessionSesuai(accessionNumber);
            JOptionPane.showMessageDialog(null,
                    (accessionDiperbaiki
                            ? "Accession Number berhasil diperbaiki dan "
                            + "mapping disimpan.\n\n"
                            : "Accession Number pada Orthanc sudah sesuai.\n"
                            + "Mapping pemeriksaan berhasil disimpan.\n\n")
                    + "Accession Number : " + accessionNumber + "\n"
                    + "No. Order        : " + noOrder + "\n"
                    + "Kode Pemeriksaan : " + kdJenisPrw
                    + infoKodePermintaan(
                            kdJenisPrw, kdJenisPrwPermintaan) + "\n"
                    + "Orthanc Study ID : " + study.path("ID").asText());
        } catch (Exception e) {
            System.out.println("Notif Perbaiki Accession Orthanc : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal memeriksa atau memperbaiki Accession Number "
                    + "di Orthanc..!!\n"
                    + pesanKesalahan(e));
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnGenerateTokenPACSActionPerformed

    private void BtnGenerateTokenPACSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnGenerateTokenPACSKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnGenerateTokenPACSActionPerformed(null);
        }
    }//GEN-LAST:event_BtnGenerateTokenPACSKeyPressed


    private void BtnFotoPACSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnFotoPACSActionPerformed
        int baris = getBarisPemeriksaanTerpilih();

        if (baris == -1) {
            JOptionPane.showMessageDialog(null,
                    "Maaf, silahkan pilih pemeriksaan radiologi terlebih dahulu..!!");
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (!pastikanTabelAccessionOrthanc()) {
                JOptionPane.showMessageDialog(null,
                        "Struktur tabel radiologi_pacs_token belum dapat "
                        + "disiapkan..!!\nPeriksa hak ALTER database dan log aplikasi.");
                return;
            }

            String noRawat = nilaiTabel(tbDokter, baris, 0);
            String tglPeriksa = nilaiTabel(tbDokter, baris, 3);
            String jamPeriksa = nilaiTabel(tbDokter, baris, 4);
            String patientId = teksAman(Sequel.cariIsi(
                    "select no_rkm_medis from reg_periksa where no_rawat=?",
                    noRawat
            ));
            if (patientId.equals("")) {
                JOptionPane.showMessageDialog(null,
                        "Patient ID / No. RM pasien tidak ditemukan..!!");
                return;
            }

            // Bila seluruh tindakan pada sesi hanya mempunyai satu Accession
            // yang tersedia di Orthanc, lewati dialog pilihan tindakan.
            String[] dataSesi = cariAccessionTunggalSemuaTindakan(
                    noRawat, tglPeriksa, jamPeriksa, patientId);
            boolean accessionTunggalSesi =
                    !dataSesi[0].equals("");
            String kdJenisPrw;
            String[] dataAccession;
            String kdJenisPrwPermintaan;
            if (accessionTunggalSesi) {
                kdJenisPrw = dataSesi[4];
                dataAccession = new String[]{
                    dataSesi[0], dataSesi[1], dataSesi[2]
                };
                kdJenisPrwPermintaan = dataSesi[3];
            } else {
                kdJenisPrw = pilihKodeJenisPerawatan(baris, true);
                if (kdJenisPrw.equals("")) {
                    return;
                }
                dataAccession = ambilDataAccessionOrthanc(
                        noRawat, tglPeriksa, jamPeriksa, kdJenisPrw);
                kdJenisPrwPermintaan =
                        kodePermintaanDariAccession(
                                dataAccession[0], kdJenisPrw);
            }

            String accessionNumber = dataAccession[0];
            String noOrder = dataAccession[1];
            String orthancStudyId = dataAccession[2];

            // Fallback wajib: bila mapping belum ada, gunakan noorder sebagai
            // Accession utama. Format lama noorder.kd_jenis_prw disiapkan
            // sebagai fallback agar foto lama tetap dapat dibuka.
            if (accessionNumber.equals("")) {
                String[] dataPermintaan =
                        pilihDataPermintaanRadiologi(
                                noRawat, tglPeriksa, jamPeriksa,
                                kdJenisPrw, true);
                noOrder = dataPermintaan[0];
                kdJenisPrwPermintaan = dataPermintaan[1];
                if (noOrder.equals("")) {
                    return;
                }
                accessionNumber = bentukAccessionNumber(
                        noOrder, kdJenisPrwPermintaan);
            }
            String accessionNumberLama = bentukAccessionNumberLama(
                    noOrder, kdJenisPrwPermintaan);

            JsonNode study = null;

            // ID mapping tersimpan adalah jalur paling tepat dan cepat, tetapi
            // tetap diverifikasi terhadap Accession Number sebelum digunakan.
            if (!orthancStudyId.equals("")) {
                try {
                    JsonNode studyTersimpan = ambilStudyOrthanc(orthancStudyId);
                    if (accessionNumber.equals(
                            tagStudyOrthanc(studyTersimpan, "AccessionNumber"))
                            && patientId.equals(tagPatientOrthanc(
                                    studyTersimpan, "PatientID"))) {
                        study = studyTersimpan;
                    }
                } catch (Exception e) {
                    System.out.println(
                            "Notif Study Orthanc tersimpan tidak tersedia : " + e);
                }
            }

            if (study == null) {
                JsonNode studies = cariStudyOrthanc(
                        accessionNumber, patientId,
                        tglPeriksa.replace("-", ""));
                if (jumlahDataOrthanc(studies) == 0) {
                    studies = cariStudyOrthanc(accessionNumber, "", "");
                }
                if (jumlahDataOrthanc(studies) == 0
                        && !accessionNumberLama.equals("")
                        && !accessionNumberLama.equals(accessionNumber)) {
                    JsonNode studiesLama = cariStudyOrthanc(
                            accessionNumberLama, patientId,
                            tglPeriksa.replace("-", ""));
                    if (jumlahDataOrthanc(studiesLama) == 0) {
                        studiesLama = cariStudyOrthanc(
                                accessionNumberLama, "", "");
                    }
                    if (jumlahDataOrthanc(studiesLama) > 0) {
                        studies = studiesLama;
                        accessionNumber = accessionNumberLama;
                    }
                }
                if (jumlahDataOrthanc(studies) == 0) {
                    tandaiAccessionPerluPerbaikan(
                            noRawat, tglPeriksa, jamPeriksa,
                            accessionNumber);
                    JOptionPane.showMessageDialog(null,
                            "Citra Orthanc dengan Accession Number "
                            + accessionNumber + " tidak ditemukan..!!\n\n"
                            + "No. Order        : " + noOrder + "\n"
                            + "Kode Pemeriksaan : " + kdJenisPrw
                            + (accessionNumberLama.equals("")
                                    || accessionNumberLama.equals(accessionNumber)
                                    ? ""
                                    : "\nFormat lama dicoba: "
                                    + accessionNumberLama)
                            + infoKodePermintaan(
                                    kdJenisPrw,
                                    kdJenisPrwPermintaan));
                    return;
                }
                study = pilihStudyOrthanc(
                        studies,
                        "Pilih Study Orthanc " + accessionNumber
                );
                if (study == null) {
                    return;
                }
                if (!konfirmasiPatientStudy(
                        study, patientId, accessionNumber,
                        "Study ditemukan berdasarkan Accession Number.")) {
                    return;
                }
            }

            // Jika satu Accession memuat seluruh tindakan pada sesi, simpan
            // mapping yang masih kosong agar klik berikutnya tetap langsung.
            if (accessionTunggalSesi) {
                simpanAccessionTindakanBelumTerpetakan(
                        noRawat, tglPeriksa, jamPeriksa, noOrder,
                        accessionNumber, study.path("ID").asText()
                );
            } else if (dataAccession[0].equals("")) {
                // Saat tombol Lihat Foto memakai fallback, hasil yang sudah
                // terverifikasi langsung dicatat untuk pemanggilan berikutnya.
                simpanAccessionOrthanc(
                        noRawat, tglPeriksa, jamPeriksa, noOrder,
                        kdJenisPrw, accessionNumber,
                        study.path("ID").asText()
                );
            }

            tandaiAccessionSesuai(accessionNumber);
            bukaFotoGipBrowser(accessionNumber);
        } catch (Exception e) {
            System.out.println("Notif Lihat Foto GIP Launcher : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal membuka foto di browser GIP Launcher..!!\n"
                    + pesanKesalahan(e));
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnFotoPACSActionPerformed

    private void BtnFotoPACSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnFotoPACSKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnFotoPACSActionPerformed(null);
        }
    }//GEN-LAST:event_BtnFotoPACSKeyPressed

    /** Membuka GIP Launcher di browser default berdasarkan Accession Number. */
    private void bukaFotoGipBrowser(String accessionNumber)
            throws Exception {
        String accession = teksAman(accessionNumber);
        if (accession.equals("")) {
            throw new IOException("Accession Number masih kosong");
        }
        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(
                        Desktop.Action.BROWSE)) {
            throw new IOException(
                    "Browser default tidak tersedia pada komputer ini");
        }

        String url = "http://192.168.10.4:8080/"
                + "gipLauncher#/monitorConfig"
                + "?uname=klinisi&pass=klinisi&accno="
                + java.net.URLEncoder.encode(accession, "UTF-8");
        System.out.println("GIP Launcher dibuka via Accession Number : "
                + accession);
        System.out.println("URL GIP Launcher : " + url);
        Desktop.getDesktop().browse(new java.net.URI(url));
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgCariPeriksaRadiologi dialog = new DlgCariPeriksaRadiologi(new javax.swing.JFrame(), true);
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
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnCloseIn4;
    private widget.Button BtnFotoPACS;
    private widget.Button BtnGenerateTokenPACS;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnPrint1;
    private widget.Button BtnRefreshPhoto;
    private widget.Button BtnSimpan;
    private widget.Button BtnSimpan4;
    private widget.Button BtnUpload;
    private widget.Button BtnWAPasien;
    private widget.Button BtnWAPerujuk;
    private widget.Button BtnWAdrRad;
    private widget.CekBox ChkAccor;
    private widget.PanelBiasa FormHasilRadiologi;
    private widget.panelisi FormInput;
    private widget.PanelBiasa FormOrthan;
    private widget.PanelBiasa FormPass2;
    private widget.PanelBiasa FormPhoto;
    private widget.TextArea HasilPeriksa;
    private widget.TextBox Jk;
    private widget.TextBox Kd2;
    private widget.TextBox KdPtgUbah;
    private widget.TextBox KodePerujuk;
    private widget.TextBox KodePj;
    private widget.Label LDiagnosaKlinis;
    private widget.editorpane LoadHTML;
    private javax.swing.JMenuItem MnCetakNota;
    private javax.swing.JMenuItem MnUbahDokterPetugas;
    private widget.TextBox NmDokterPj;
    private widget.TextBox NmPerujuk;
    private widget.TextBox NmPtgUbah;
    private widget.TextBox NoRM;
    private widget.TextBox NoRawat;
    private widget.Label NoSEP;
    private widget.PanelBiasa PanelAccor;
    private widget.TextBox Penjab;
    private widget.TextBox Petugas;
    private widget.ScrollPane Scroll3;
    private widget.ScrollPane Scroll4;
    private widget.ScrollPane Scroll5;
    private widget.TextBox TCari;
    private widget.TextBox TDiagnosaKlinis;
    private widget.TextBox TNoSEP;
    private javax.swing.JTabbedPane TabData;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.TextBox Umur;
    private javax.swing.JDialog WindowGantiDokterParamedis;
    private widget.Button btnAmbilPhoto;
    private widget.Button btnAmbilPhoto1;
    private widget.Button btnDicom;
    private widget.Button btnDokter;
    private widget.Button btnDokterPj;
    private widget.Button btnPasien;
    private widget.Button btnPetugas;
    private widget.Button btnPetugas1;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame5;
    private widget.Label jLabel12;
    private widget.Label jLabel7;
    private widget.Label jLabel9;
    private javax.swing.JPopupMenu jPopupMenu1;
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
    private widget.panelisi panelisi1;
    private widget.panelisi panelisi3;
    private javax.swing.JMenuItem ppBerkasDigital;
    private javax.swing.JMenuItem ppRiwayat;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbDokter;
    private widget.Table tbListDicom;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        try {
            Valid.tabelKosong(tabMode);   
            if(NoRawat.getText().equals("")&&kdmem.getText().equals("")&&kdptg.getText().equals("")&&TCari.getText().equals("")){
                ps=koneksi.prepareStatement(
                        "select periksa_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,petugas.nama,periksa_radiologi.tgl_periksa,pasien.tgl_lahir,"+
                        "periksa_radiologi.jam,periksa_radiologi.dokter_perujuk,periksa_radiologi.kd_dokter,penjab.png_jawab,dokter.nm_dokter,bridging_sep.no_sep,permintaan_radiologi.diagnosa_klinis "+
                        "from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "+
                        "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join petugas on periksa_radiologi.nip=petugas.nip "+
                        "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                        "left join bridging_sep on periksa_radiologi.no_rawat=bridging_sep.no_rawat "+
                        "left join permintaan_radiologi on periksa_radiologi.no_rawat=permintaan_radiologi.no_rawat "+
                        "inner join dokter on periksa_radiologi.kd_dokter=dokter.kd_dokter where "+
                        "periksa_radiologi.tgl_periksa between ? and ? group by concat(periksa_radiologi.no_rawat,periksa_radiologi.tgl_periksa,periksa_radiologi.jam) "+
                        "order by periksa_radiologi.tgl_periksa desc,periksa_radiologi.jam desc");
            }else{
                ps=koneksi.prepareStatement(
                        "select periksa_radiologi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,petugas.nama,periksa_radiologi.tgl_periksa,pasien.tgl_lahir,"+
                        "periksa_radiologi.jam,periksa_radiologi.dokter_perujuk,periksa_radiologi.kd_dokter,penjab.png_jawab,dokter.nm_dokter,bridging_sep.no_sep,permintaan_radiologi.diagnosa_klinis "+
                        "from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "+
                        "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join petugas on periksa_radiologi.nip=petugas.nip "+
                        "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                        "left join bridging_sep on periksa_radiologi.no_rawat=bridging_sep.no_rawat "+
                        "left join permintaan_radiologi on periksa_radiologi.no_rawat=permintaan_radiologi.no_rawat "+
                        "inner join dokter on periksa_radiologi.kd_dokter=dokter.kd_dokter where "+
                        "periksa_radiologi.tgl_periksa between ? and ? and periksa_radiologi.no_rawat like ? and reg_periksa.no_rkm_medis like ? "+
                        "and petugas.nip like ? and (pasien.nm_pasien like ? or petugas.nama like ? or reg_periksa.no_rkm_medis like ? or penjab.png_jawab like ? ) "+
                        "group by concat(periksa_radiologi.no_rawat,periksa_radiologi.tgl_periksa,periksa_radiologi.jam) "+
                        "order by periksa_radiologi.tgl_periksa desc,periksa_radiologi.jam desc");
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
                }
                    
                rs=ps.executeQuery();
                ttl=0;
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
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis")+" "+rs.getString("nm_pasien")+" ("+kamar+" : "+namakamar+")",rs.getString("nama"),
                        rs.getString("tgl_periksa"),rs.getString("jam"),dokter.tampil3(rs.getString("dokter_perujuk")),rs.getString("nm_dokter"),rs.getString("tgl_lahir"),rs.getString("diagnosa_klinis"),rs.getString("no_sep")
                    });
                    tabMode.addRow(new Object[]{"","Proyeksi & Dosis Radiasi","Kode Periksa","Nama Pemeriksaan","Biaya","Cara Bayar : "+rs.getString("png_jawab"),""});
                    ps2=koneksi.prepareStatement(
                            "select jns_perawatan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.biaya,"+"concat("+
                            "if(periksa_radiologi.proyeksi<>'',concat('Proyeksi : ',periksa_radiologi.proyeksi,', '),''),"+
                            "if(periksa_radiologi.kV<>'',concat('kV : ',periksa_radiologi.kV,', '),''),"+
                            "if(periksa_radiologi.mAS<>'',concat('mAS : ',periksa_radiologi.mAS,', '),''),"+
                            "if(periksa_radiologi.FFD<>'',concat('FFD : ',periksa_radiologi.FFD,', '),''),"+
                            "if(periksa_radiologi.BSF<>'',concat('BSF : ',periksa_radiologi.BSF,', '),''),"+
                            "if(periksa_radiologi.inak<>'',concat('Inak : ',periksa_radiologi.inak,', '),''),"+
                            "if(periksa_radiologi.jml_penyinaran<>'',concat('Jml Penyinaran : ',periksa_radiologi.jml_penyinaran,', '),''),"+
                            "if(periksa_radiologi.dosis<>'',concat('Dosis Radiasi : ',periksa_radiologi.dosis),'')) as proyeksi from periksa_radiologi "+
                            "inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw where periksa_radiologi.no_rawat=? and periksa_radiologi.tgl_periksa=? "+
                            "and periksa_radiologi.jam=?"); 
                    try {
                        ps2.setString(1,rs.getString("no_rawat"));
                        ps2.setString(2,rs.getString("tgl_periksa"));
                        ps2.setString(3,rs.getString("jam"));
                        rs2=ps2.executeQuery();
                        while(rs2.next()){  
                            ttl=ttl+rs2.getDouble("biaya");
                            tabMode.addRow(new Object[]{"",rs2.getString("proyeksi"),rs2.getString("kd_jenis_prw"),rs2.getString("nm_perawatan"),Valid.SetAngka(rs2.getDouble("biaya")),"",""});
                        }
                    } catch (Exception e) {
                        System.out.println("simrskhanza.DlgCariPeriksaRadiologi.tampil() ps2 : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }
                        
                    ps3=koneksi.prepareStatement(
                            "select beri_bhp_radiologi.kode_brng,ipsrsbarang.nama_brng,beri_bhp_radiologi.kode_sat,beri_bhp_radiologi.jumlah, "+
                            "beri_bhp_radiologi.total from beri_bhp_radiologi inner join ipsrsbarang on ipsrsbarang.kode_brng=beri_bhp_radiologi.kode_brng "+
                            "where beri_bhp_radiologi.no_rawat=? and beri_bhp_radiologi.tgl_periksa=? and beri_bhp_radiologi.jam=?");  
                    try {
                        ps3.setString(1,rs.getString("no_rawat"));
                        ps3.setString(2,rs.getString("tgl_periksa"));
                        ps3.setString(3,rs.getString("jam"));
                        rs3=ps3.executeQuery();
                        rs3.last();
                        if(rs3.getRow()>0){
                            tabMode.addRow(new Object[]{"","","Kode BHP","Nama BHP","Satuan","Jumlah",""});
                            rs3.beforeFirst();
                            while(rs3.next()){  
                                tabMode.addRow(new Object[]{"","",rs3.getString("kode_brng"),rs3.getString("nama_brng"),rs3.getString("kode_sat"),rs3.getString("jumlah"),""});
                            }
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
                System.out.println("simrskhanza.DlgCariPeriksaRadiologi.tampil() PS : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }                
            if(ttl>0){
                tabMode.addRow(new Object[]{">>","Total : "+Valid.SetAngka(ttl),"","","","",""});
            }
            
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
    }
    
    public void SetNoRw(String norw){
        NoRawat.setText(norw);
        tampil();
        Sequel.cariIsi("select reg_periksa.tgl_registrasi from reg_periksa where reg_periksa.no_rawat='"+norw+"'", Tgl1);
    }
    
    /**
     * Menampilkan status mapping Accession Number untuk tindakan yang dipilih.
     * Nama komponen dipertahankan agar file .form NetBeans tetap kompatibel.
     */
    private void perbaruiTombolAccessionOrthanc() {
        int baris = getBarisPemeriksaanTerpilih();
        if (baris == -1) {
            sembunyikanTombolPerbaikiAccession();
            return;
        }

        try {
            String noRawat = nilaiTabel(tbDokter, baris, 0);
            String tglPeriksa = nilaiTabel(tbDokter, baris, 3);
            String jamPeriksa = nilaiTabel(tbDokter, baris, 4);
            String kunciPemeriksaan = kunciPemeriksaanAccession(
                    noRawat, tglPeriksa, jamPeriksa);
            if (!accessionPerluPerbaikan.equals("")
                    && kunciPerbaikanAccession.equals(
                            kunciPemeriksaan)) {
                tampilkanTombolPerbaikiAccession(
                        noRawat, tglPeriksa, jamPeriksa,
                        accessionPerluPerbaikan);
                return;
            }
            sembunyikanTombolPerbaikiAccession();

            String kdJenisPrw = pilihKodeJenisPerawatan(baris, false);
            if (kdJenisPrw.equals("")) {
                String accessionSesi =
                        ambilAccessionSamaSemuaTindakan(
                                noRawat, tglPeriksa, jamPeriksa);
                if (!accessionSesi.equals("")) {
                    tandaiAccessionSesuai(accessionSesi);
                    return;
                }
                BtnGenerateTokenPACS.setToolTipText(
                        "Pilih baris tindakan radiologi yang akan "
                        + "diperiksa atau diperbaiki");
                return;
            }

            String[] data = ambilDataAccessionOrthanc(
                    noRawat, tglPeriksa, jamPeriksa, kdJenisPrw
            );
            if (!data[0].equals("")) {
                tandaiAccessionSesuai(data[0]);
            }
        } catch (Exception e) {
            System.out.println(
                    "Notif Update Tombol Accession Orthanc : " + e);
        }
    }

    private void tandaiAccessionSesuai(String accessionNumber) {
        kunciPerbaikanAccession = "";
        accessionPerluPerbaikan = "";
        BtnGenerateTokenPACS.setText("Accession Sesuai");
        BtnGenerateTokenPACS.setToolTipText(
                "Mapping terverifikasi ke Accession Number Orthanc: "
                + teksAman(accessionNumber));
        BtnGenerateTokenPACS.setEnabled(false);
        BtnGenerateTokenPACS.setVisible(false);
        panelisi1.revalidate();
        panelisi1.repaint();
    }

    private void tandaiAccessionPerluPerbaikan(
            String noRawat, String tglPeriksa, String jamPeriksa,
            String accessionNumber) {
        tampilkanTombolPerbaikiAccession(
                noRawat, tglPeriksa, jamPeriksa, accessionNumber);
    }

    private void tampilkanTombolPerbaikiAccession(
            String noRawat, String tglPeriksa, String jamPeriksa,
            String accessionNumber) {
        kunciPerbaikanAccession = kunciPemeriksaanAccession(
                noRawat, tglPeriksa, jamPeriksa);
        accessionPerluPerbaikan = teksAman(accessionNumber);
        BtnGenerateTokenPACS.setText("Perbaiki Accession");
        BtnGenerateTokenPACS.setToolTipText(
                "Accession Number " + accessionPerluPerbaikan
                        + " belum ditemukan di Orthanc; klik untuk memperbaiki");
        BtnGenerateTokenPACS.setEnabled(true);
        BtnGenerateTokenPACS.setVisible(true);
        panelisi1.revalidate();
        panelisi1.repaint();
    }

    private void sembunyikanTombolPerbaikiAccession() {
        kunciPerbaikanAccession = "";
        accessionPerluPerbaikan = "";
        BtnGenerateTokenPACS.setText("Perbaiki Accession");
        BtnGenerateTokenPACS.setToolTipText(
                "Periksa dan perbaiki Accession Number di Orthanc");
        BtnGenerateTokenPACS.setEnabled(false);
        BtnGenerateTokenPACS.setVisible(false);
        panelisi1.revalidate();
        panelisi1.repaint();
    }

    private String kunciPemeriksaanAccession(
            String noRawat, String tglPeriksa, String jamPeriksa) {
        return teksAman(noRawat) + "|"
                + teksAman(tglPeriksa) + "|"
                + teksAman(jamPeriksa);
    }

    /**
     * Mengembalikan Accession bila seluruh tindakan pada sesi sudah mempunyai
     * mapping yang lengkap dan semuanya menunjuk Accession yang sama.
     */
    private String ambilAccessionSamaSemuaTindakan(
            String noRawat, String tglPeriksa, String jamPeriksa)
            throws SQLException {
        LinkedHashMap<String, String> tindakan =
                new LinkedHashMap<String, String>();
        String sql = "select distinct kd_jenis_prw "
                + "from periksa_radiologi "
                + "where no_rawat=? and tgl_periksa=? and jam=? "
                + "order by kd_jenis_prw";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, noRawat);
            pst.setString(2, tglPeriksa);
            pst.setString(3, jamPeriksa);
            try (ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    String kode =
                            teksAman(rst.getString("kd_jenis_prw"));
                    if (!kode.equals("")) {
                        tindakan.put(kode, kode);
                    }
                }
            }
        }
        if (tindakan.isEmpty()) {
            return "";
        }

        String accessionSama = "";
        for (String kodeTindakan : tindakan.keySet()) {
            String[] mapping = ambilDataAccessionOrthanc(
                    noRawat, tglPeriksa, jamPeriksa, kodeTindakan);
            if (mapping[0].equals("")) {
                return "";
            }
            if (accessionSama.equals("")) {
                accessionSama = mapping[0];
            } else if (!accessionSama.equals(mapping[0])) {
                return "";
            }
        }
        return accessionSama;
    }

    /**
     * Menentukan baris header pemeriksaan yang dipilih. Baris tindakan berada
     * di bawah header dan mempunyai kolom No.Rawat kosong.
     */
    private int getBarisPemeriksaanTerpilih() {
        int baris = tbDokter.getSelectedRow();
        if (baris >= 0) {
            Object nilaiAwal = tbDokter.getValueAt(baris, 0);
            if (nilaiAwal != null
                    && nilaiAwal.toString().trim().equals(">>")) {
                return -1;
            }
        }
        while (baris >= 0) {
            Object nilai = tbDokter.getValueAt(baris, 0);
            if (nilai != null) {
                String noRawat = nilai.toString().trim();
                if (!noRawat.equals("") && !noRawat.equals(">>")) {
                    return baris;
                }
            }
            baris--;
        }
        return -1;
    }

    private String nilaiTabel(JTable tabel, int baris, int kolom) {
        if (baris < 0 || baris >= tabel.getRowCount()
                || kolom < 0 || kolom >= tabel.getColumnCount()) {
            return "";
        }
        Object nilai = tabel.getValueAt(baris, kolom);
        return nilai == null ? "" : nilai.toString().trim();
    }

    /**
     * Mengambil kd_jenis_prw yang benar. Bila satu sesi mempunyai beberapa
     * tindakan, baris detail yang dipilih diprioritaskan; selain itu pengguna
     * wajib memilih dan aplikasi tidak menebak.
     */
    private String pilihKodeJenisPerawatan(
            int barisHeader, boolean tampilkanDialog) {
        LinkedHashMap<String, String> tindakan =
                new LinkedHashMap<String, String>();
        String noRawat = nilaiTabel(tbDokter, barisHeader, 0);
        String tglPeriksa = nilaiTabel(tbDokter, barisHeader, 3);
        String jamPeriksa = nilaiTabel(tbDokter, barisHeader, 4);

        String sql = "select distinct pr.kd_jenis_prw,jp.nm_perawatan "
                + "from periksa_radiologi pr "
                + "inner join jns_perawatan_radiologi jp "
                + "on jp.kd_jenis_prw=pr.kd_jenis_prw "
                + "where pr.no_rawat=? and pr.tgl_periksa=? and pr.jam=? "
                + "order by pr.kd_jenis_prw";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, noRawat);
            pst.setString(2, tglPeriksa);
            pst.setString(3, jamPeriksa);
            try (ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    tindakan.put(
                            rst.getString("kd_jenis_prw").trim(),
                            rst.getString("nm_perawatan").trim()
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Ambil Tindakan Radiologi : " + e);
            if (tampilkanDialog) {
                JOptionPane.showMessageDialog(null,
                        "Daftar tindakan radiologi tidak dapat dibaca..!!\n"
                        + pesanKesalahan(e));
            }
            return "";
        }

        int barisPilihan = tbDokter.getSelectedRow();
        String kodeTerpilih = nilaiTabel(tbDokter, barisPilihan, 2);
        String namaTerpilih = nilaiTabel(tbDokter, barisPilihan, 3);
        if (tindakan.containsKey(kodeTerpilih)
                && tindakan.get(kodeTerpilih).equals(namaTerpilih)) {
            return kodeTerpilih;
        }

        if (tindakan.size() == 1) {
            return tindakan.keySet().iterator().next();
        }

        if (tindakan.isEmpty()) {
            if (tampilkanDialog) {
                JOptionPane.showMessageDialog(null,
                        "Tindakan radiologi pada sesi pemeriksaan ini "
                        + "tidak ditemukan..!!");
            }
            return "";
        }

        if (!tampilkanDialog) {
            return "";
        }

        LinkedHashMap<String, String> pilihanTindakan =
                new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : tindakan.entrySet()) {
            String label = entry.getKey() + " - " + entry.getValue();
            pilihanTindakan.put(label, entry.getKey());
        }
        Object pilihanTindakanUser = JOptionPane.showInputDialog(
                null,
                "Sesi ini mempunyai beberapa tindakan.\n"
                + "Pilih tindakan yang akan diproses:",
                "Pilih Tindakan Radiologi",
                JOptionPane.QUESTION_MESSAGE,
                null,
                pilihanTindakan.keySet().toArray(),
                pilihanTindakan.keySet().iterator().next()
        );
        if (pilihanTindakanUser == null) {
            return "";
        }
        return pilihanTindakan.get(pilihanTindakanUser.toString());
    }

    /**
     * Mendeteksi satu Accession yang dipakai bersama oleh seluruh tindakan
     * dalam satu sesi. Pendeteksian hanya otomatis bila mapping seluruh
     * tindakan sudah sama, atau hanya satu kandidat Accession pada waktu hasil
     * yang benar-benar tersedia di Orthanc.
     *
     * @return accession_number, noorder, orthanc_study_id,
     *         kd_jenis_prw permintaan, kd_jenis_prw tindakan.
     */
    private String[] cariAccessionTunggalSemuaTindakan(
            String noRawat, String tglPeriksa, String jamPeriksa,
            String patientId) throws Exception {
        String[] kosong = new String[]{"", "", "", "", ""};
        LinkedHashMap<String, String> tindakan =
                new LinkedHashMap<String, String>();
        String sqlTindakan =
                "select distinct pr.kd_jenis_prw,jp.nm_perawatan "
                + "from periksa_radiologi pr "
                + "inner join jns_perawatan_radiologi jp "
                + "on jp.kd_jenis_prw=pr.kd_jenis_prw "
                + "where pr.no_rawat=? and pr.tgl_periksa=? and pr.jam=? "
                + "order by pr.kd_jenis_prw";
        try (PreparedStatement pst =
                koneksi.prepareStatement(sqlTindakan)) {
            pst.setString(1, noRawat);
            pst.setString(2, tglPeriksa);
            pst.setString(3, jamPeriksa);
            try (ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    tindakan.put(
                            teksAman(rst.getString("kd_jenis_prw")),
                            teksAman(rst.getString("nm_perawatan"))
                    );
                }
            }
        }
        if (tindakan.size() <= 1) {
            return kosong;
        }

        LinkedHashMap<String, String[]> mappingTersimpan =
                new LinkedHashMap<String, String[]>();
        int jumlahTindakanTerpetakan = 0;
        String sqlMapping =
                "select kd_jenis_prw,accession_number,noorder,"
                + "orthanc_study_id from radiologi_pacs_token "
                + "where no_rawat=? and tgl_periksa=? and jam=? "
                + "and accession_number is not null "
                + "and accession_number<>'' order by kd_jenis_prw";
        try (PreparedStatement pst =
                koneksi.prepareStatement(sqlMapping)) {
            pst.setString(1, noRawat);
            pst.setString(2, tglPeriksa);
            pst.setString(3, jamPeriksa);
            try (ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    String kodeTindakan =
                            teksAman(rst.getString("kd_jenis_prw"));
                    if (!tindakan.containsKey(kodeTindakan)) {
                        continue;
                    }
                    String accession =
                            teksAman(rst.getString("accession_number"));
                    if (accession.equals("")) {
                        continue;
                    }
                    String noOrder = teksAman(rst.getString("noorder"));
                    if (noOrder.equals("")) {
                        noOrder = noOrderDariAccession(accession);
                    }
                    jumlahTindakanTerpetakan++;
                    mappingTersimpan.put(
                            accession,
                            new String[]{
                                accession,
                                noOrder,
                                teksAman(rst.getString(
                                        "orthanc_study_id")),
                                kodePermintaanDariAccession(
                                        accession, kodeTindakan),
                                kodeTindakan
                            }
                    );
                }
            }
        }

        // Mapping berbeda berarti tindakan memang mengarah ke beberapa study.
        if (mappingTersimpan.size() > 1) {
            return kosong;
        }
        if (mappingTersimpan.size() == 1
                && jumlahTindakanTerpetakan == tindakan.size()) {
            return mappingTersimpan.values().iterator().next();
        }

        // Tanpa mapping lengkap, hanya kandidat pada waktu hasil yang sama
        // yang boleh dipakai untuk keputusan otomatis. Format baru noorder
        // diprioritaskan, sedangkan noorder.kd_jenis_prw tetap didaftarkan
        // sebagai kandidat agar study lama masih dapat ditemukan.
        LinkedHashMap<String, String[]> kandidat =
                new LinkedHashMap<String, String[]>();
        String kodeTindakanAwal =
                tindakan.keySet().iterator().next();
        String sqlPermintaan =
                "select distinct ppr.noorder,ppr.kd_jenis_prw "
                + "from permintaan_pemeriksaan_radiologi ppr "
                + "inner join permintaan_radiologi pr "
                + "on pr.noorder=ppr.noorder "
                + "where pr.no_rawat=? and pr.tgl_hasil=? "
                + "and pr.jam_hasil=? "
                + "order by ppr.noorder,ppr.kd_jenis_prw";
        try (PreparedStatement pst =
                koneksi.prepareStatement(sqlPermintaan)) {
            pst.setString(1, noRawat);
            pst.setString(2, tglPeriksa);
            pst.setString(3, jamPeriksa);
            try (ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    String noOrder =
                            teksAman(rst.getString("noorder"));
                    String kodePermintaan =
                            teksAman(rst.getString("kd_jenis_prw"));
                    if (noOrder.equals("")
                            || kodePermintaan.equals("")) {
                        continue;
                    }
                    String accession = bentukAccessionNumber(
                            noOrder, kodePermintaan);
                    kandidat.put(
                            accession,
                            new String[]{
                                accession, noOrder, "",
                                kodePermintaan, kodeTindakanAwal
                            }
                    );
                    String accessionLama = bentukAccessionNumberLama(
                            noOrder, kodePermintaan);
                    if (!accessionLama.equals("")
                            && !accessionLama.equals(accession)) {
                        kandidat.put(
                                accessionLama,
                                new String[]{
                                    accessionLama, noOrder, "",
                                    kodePermintaan, kodeTindakanAwal
                                }
                        );
                    }
                }
            }
        }
        for (Map.Entry<String, String[]> entry
                : mappingTersimpan.entrySet()) {
            kandidat.put(entry.getKey(), entry.getValue());
        }
        if (kandidat.isEmpty()) {
            return kosong;
        }

        LinkedHashMap<String, String[]> tersediaDiOrthanc =
                new LinkedHashMap<String, String[]>();
        for (Map.Entry<String, String[]> entry : kandidat.entrySet()) {
            JsonNode studies = cariStudyOrthanc(
                    entry.getKey(), patientId,
                    tglPeriksa.replace("-", ""));
            if (jumlahDataOrthanc(studies) == 0) {
                studies = cariStudyOrthanc(
                        entry.getKey(), patientId, "");
            }
            if (jumlahDataOrthanc(studies) > 0) {
                tersediaDiOrthanc.put(
                        entry.getKey(), entry.getValue());
            }
        }
        if (tersediaDiOrthanc.size() != 1) {
            return kosong;
        }
        Map.Entry<String, String[]> accessionTunggal =
                tersediaDiOrthanc.entrySet().iterator().next();
        if (!mappingTersimpan.isEmpty()
                && !mappingTersimpan.containsKey(
                        accessionTunggal.getKey())) {
            // Jangan mengubah mapping lama secara otomatis walaupun study lama
            // sedang tidak tersedia; biarkan pengguna memilih secara eksplisit.
            return kosong;
        }
        return accessionTunggal.getValue();
    }

    /**
     * Mengisi mapping yang masih kosong ketika satu Accession telah
     * terverifikasi memuat seluruh tindakan dalam sesi yang sama.
     */
    private void simpanAccessionTindakanBelumTerpetakan(
            String noRawat, String tglPeriksa, String jamPeriksa,
            String noOrder, String accessionNumber,
            String orthancStudyId) throws SQLException {
        LinkedHashMap<String, String> tindakan =
                new LinkedHashMap<String, String>();
        String sql = "select distinct kd_jenis_prw "
                + "from periksa_radiologi "
                + "where no_rawat=? and tgl_periksa=? and jam=? "
                + "order by kd_jenis_prw";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, noRawat);
            pst.setString(2, tglPeriksa);
            pst.setString(3, jamPeriksa);
            try (ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    String kode =
                            teksAman(rst.getString("kd_jenis_prw"));
                    if (!kode.equals("")) {
                        tindakan.put(kode, kode);
                    }
                }
            }
        }

        for (String kodeTindakan : tindakan.keySet()) {
            String[] mapping = ambilDataAccessionOrthanc(
                    noRawat, tglPeriksa, jamPeriksa, kodeTindakan);
            if (mapping[0].equals("")) {
                simpanAccessionOrthanc(
                        noRawat, tglPeriksa, jamPeriksa, noOrder,
                        kodeTindakan, accessionNumber, orthancStudyId
                );
            }
        }
    }

    /**
     * Mencari noorder sumber Accession Number beserta kd_jenis_prw
     * permintaannya. Accession Number hanya memakai noorder, sedangkan kode
     * tindakan tetap dipakai untuk memilih dan menyimpan mapping pemeriksaan.
     * Kode tindakan pada periksa_radiologi dapat berbeda dari kode pada
     * permintaan_pemeriksaan_radiologi. Urutan prioritas:
     * kode+waktu sama, waktu sama, kode sama, lalu seluruh permintaan.
     *
     * @return noorder dan kd_jenis_prw dari permintaan radiologi.
     */
    private String[] pilihDataPermintaanRadiologi(
            String noRawat, String tglPeriksa, String jamPeriksa,
            String kdJenisPrw, boolean tampilkanDialog) {
        String[] kosong = new String[]{"", ""};
        LinkedHashMap<String, String[]> semua =
                new LinkedHashMap<String, String[]>();
        LinkedHashMap<String, String[]> waktuTepat =
                new LinkedHashMap<String, String[]>();
        LinkedHashMap<String, String[]> kodeTepat =
                new LinkedHashMap<String, String[]>();
        LinkedHashMap<String, String[]> kodeWaktuTepat =
                new LinkedHashMap<String, String[]>();

        // Accession Number wajib memakai ppr.noorder saja. kd_jenis_prw tetap
        // dibaca untuk memilih dan menyimpan mapping tindakan yang tepat.
        // no_rawat ditelusuri melalui relasi permintaan_radiologi.noorder.
        String sql = "select distinct ppr.noorder,ppr.kd_jenis_prw,"
                + "ifnull(jp.nm_perawatan,'') as nm_perawatan,"
                + "pr.tgl_permintaan,pr.jam_permintaan,"
                + "case when pr.tgl_hasil=? and pr.jam_hasil=? "
                + "then 0 else 1 end as prioritas "
                + "from permintaan_pemeriksaan_radiologi ppr "
                + "inner join permintaan_radiologi pr "
                + "on pr.noorder=ppr.noorder "
                + "left join jns_perawatan_radiologi jp "
                + "on jp.kd_jenis_prw=ppr.kd_jenis_prw "
                + "where pr.no_rawat=? "
                + "order by prioritas,pr.tgl_permintaan desc,"
                + "pr.jam_permintaan desc,ppr.noorder desc,"
                + "ppr.kd_jenis_prw";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, tglPeriksa);
            pst.setString(2, jamPeriksa);
            pst.setString(3, noRawat);
            try (ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    String noOrder = teksAman(rst.getString("noorder"));
                    String kodePermintaan =
                            teksAman(rst.getString("kd_jenis_prw"));
                    if (noOrder.equals("")
                            || kodePermintaan.equals("")) {
                        continue;
                    }
                    String accession = bentukAccessionNumber(
                            noOrder, kodePermintaan);
                    String namaPemeriksaan =
                            teksAman(rst.getString("nm_perawatan"));
                    String info = rst.getString("tgl_permintaan")
                            + " " + rst.getString("jam_permintaan");
                    String[] data = new String[]{
                        noOrder, kodePermintaan, namaPemeriksaan, info
                    };
                    semua.put(accession, data);
                    boolean waktuSama = rst.getInt("prioritas") == 0;
                    boolean kodeSama =
                            kodePermintaan.equals(kdJenisPrw);
                    if (waktuSama) {
                        waktuTepat.put(accession, data);
                    }
                    if (kodeSama) {
                        kodeTepat.put(accession, data);
                    }
                    if (waktuSama && kodeSama) {
                        kodeWaktuTepat.put(accession, data);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Notif Ambil Data Permintaan Radiologi : " + e);
            if (tampilkanDialog) {
                JOptionPane.showMessageDialog(null,
                        "Data permintaan radiologi tidak dapat dibaca..!!\n"
                        + pesanKesalahan(e));
            }
            return kosong;
        }

        LinkedHashMap<String, String[]> kandidat;
        if (!kodeWaktuTepat.isEmpty()) {
            kandidat = kodeWaktuTepat;
        } else if (!waktuTepat.isEmpty()) {
            kandidat = waktuTepat;
        } else if (!kodeTepat.isEmpty()) {
            kandidat = kodeTepat;
        } else {
            kandidat = semua;
        }

        if (kandidat.size() == 1) {
            String[] data =
                    kandidat.values().iterator().next();
            return new String[]{data[0], data[1]};
        }
        if (kandidat.isEmpty()) {
            if (tampilkanDialog) {
                JOptionPane.showMessageDialog(null,
                        "Permintaan radiologi untuk data berikut "
                        + "tidak ditemukan..!!"
                        + "\nNo. Rawat        : " + noRawat
                        + "\nKode Pemeriksaan : " + kdJenisPrw);
            }
            return kosong;
        }
        if (!tampilkanDialog) {
            return kosong;
        }

        LinkedHashMap<String, String[]> pilihan =
                new LinkedHashMap<String, String[]>();
        for (Map.Entry<String, String[]> entry
                : kandidat.entrySet()) {
            String[] data = entry.getValue();
            String nama = data[2].equals("")
                    ? "(nama pemeriksaan tidak tersedia)" : data[2];
            String label = entry.getKey() + " | " + nama
                    + " | Permintaan " + data[3];
            pilihan.put(label, data);
        }
        Object pilihanUser = JOptionPane.showInputDialog(
                null,
                "Ditemukan beberapa permintaan radiologi untuk "
                + "kode tindakan " + kdJenisPrw + ".\n"
                + "Pilih Accession Number yang tepat:",
                "Pilih Accession Number Radiologi",
                JOptionPane.QUESTION_MESSAGE,
                null,
                pilihan.keySet().toArray(),
                pilihan.keySet().iterator().next()
        );
        if (pilihanUser == null) {
            return kosong;
        }
        String[] data = pilihan.get(pilihanUser.toString());
        return new String[]{data[0], data[1]};
    }

    private String bentukAccessionNumber(
            String noOrder, String kdJenisPrw) {
        return noOrder.trim();
    }

    private String bentukAccessionNumberLama(
            String noOrder, String kdJenisPrw) {
        String order = teksAman(noOrder);
        String kode = teksAman(kdJenisPrw);
        if (order.equals("") || kode.equals("")) {
            return "";
        }
        return order + "." + kode;
    }

    private String noOrderDariAccession(String accessionNumber) {
        String accession = teksAman(accessionNumber);
        int pemisah = accession.lastIndexOf(".");
        if (pemisah > 0) {
            return accession.substring(0, pemisah).trim();
        }
        return accession;
    }

    private String kodePermintaanDariAccession(
            String accessionNumber, String kodeFallback) {
        String accession = teksAman(accessionNumber);
        int pemisah = accession.lastIndexOf(".");
        if (pemisah >= 0 && pemisah < accession.length() - 1) {
            return accession.substring(pemisah + 1).trim();
        }
        return teksAman(kodeFallback);
    }

    private String infoKodePermintaan(
            String kodePemeriksaan, String kodePermintaan) {
        String kode = teksAman(kodePermintaan);
        if (kode.equals("")
                || kode.equals(teksAman(kodePemeriksaan))) {
            return "";
        }
        return "\nKode Permintaan  : " + kode;
    }

    /**
     * Membuat atau memigrasikan radiologi_pacs_token tanpa menghapus data token
     * lama. token_pacs dipertahankan sebagai kolom legacy, sedangkan mapping
     * baru unik per no_rawat + tanggal + jam + kd_jenis_prw.
     */
    private synchronized boolean pastikanTabelAccessionOrthanc() {
        if (tabelAccessionOrthancSiap) {
            return true;
        }
        try (java.sql.Statement st = koneksi.createStatement()) {
            st.executeUpdate(
                    "create table if not exists radiologi_pacs_token ("
                    + "no_rawat varchar(17) not null,"
                    + "tgl_periksa date not null,"
                    + "jam time not null,"
                    + "token_pacs varchar(64) null default null,"
                    + "noorder varchar(40) not null default '',"
                    + "kd_jenis_prw varchar(20) not null default '',"
                    + "accession_number varchar(64) null default null,"
                    + "orthanc_study_id varchar(64) null default null,"
                    + "tgl_buat datetime not null,"
                    + "pembuat varchar(50) not null,"
                    + "tgl_kirim datetime null default null,"
                    + "pengirim varchar(50) null default null,"
                    + "primary key "
                    + "(no_rawat,tgl_periksa,jam,kd_jenis_prw),"
                    + "key idx_radiologi_pacs_accession "
                    + "(accession_number)"
                    + ") engine=InnoDB"
            );

            if (kolomTabelAda("token_pacs")
                    && kolomTokenLegacyPerluMigrasi()) {
                st.executeUpdate(
                        "alter table radiologi_pacs_token "
                        + "modify token_pacs varchar(64) null default null");
            } else if (!kolomTabelAda("token_pacs")) {
                st.executeUpdate(
                        "alter table radiologi_pacs_token "
                        + "add column token_pacs varchar(64) "
                        + "null default null after jam");
            }
            tambahKolomAccessionJikaBelumAda(
                    st, "noorder",
                    "varchar(40) not null default '' after token_pacs");
            tambahKolomAccessionJikaBelumAda(
                    st, "kd_jenis_prw",
                    "varchar(20) not null default '' after noorder");
            tambahKolomAccessionJikaBelumAda(
                    st, "accession_number",
                    "varchar(64) null default null after kd_jenis_prw");
            tambahKolomAccessionJikaBelumAda(
                    st, "orthanc_study_id",
                    "varchar(64) null default null after accession_number");
            tambahKolomAccessionJikaBelumAda(
                    st, "tgl_kirim",
                    "datetime null default null after pembuat");
            tambahKolomAccessionJikaBelumAda(
                    st, "pengirim",
                    "varchar(50) null default null after tgl_kirim");

            if (!primaryKeyMemuatKolom("kd_jenis_prw")) {
                if (indexTabelAda("PRIMARY")) {
                    st.executeUpdate(
                            "alter table radiologi_pacs_token "
                            + "drop primary key,"
                            + "add primary key "
                            + "(no_rawat,tgl_periksa,jam,kd_jenis_prw)");
                } else {
                    st.executeUpdate(
                            "alter table radiologi_pacs_token "
                            + "add primary key "
                            + "(no_rawat,tgl_periksa,jam,kd_jenis_prw)");
                }
            }
            if (!indexTabelAda("idx_radiologi_pacs_accession")) {
                st.executeUpdate(
                        "alter table radiologi_pacs_token "
                        + "add index idx_radiologi_pacs_accession "
                        + "(accession_number)");
            }
            tabelAccessionOrthancSiap = true;
            return true;
        } catch (Exception e) {
            System.out.println(
                    "Notif Migrasi Tabel Accession Orthanc : " + e);
            return false;
        }
    }

    private void tambahKolomAccessionJikaBelumAda(
            java.sql.Statement st, String namaKolom, String definisi)
            throws SQLException {
        if (!kolomTabelAda(namaKolom)) {
            st.executeUpdate(
                    "alter table radiologi_pacs_token add column "
                    + namaKolom + " " + definisi);
        }
    }

    private boolean kolomTabelAda(String namaKolom) throws SQLException {
        String sql = "select count(*) from information_schema.columns "
                + "where table_schema=database() "
                + "and table_name='radiologi_pacs_token' "
                + "and column_name=?";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, namaKolom);
            try (ResultSet rst = pst.executeQuery()) {
                return rst.next() && rst.getInt(1) > 0;
            }
        }
    }

    private boolean kolomTokenLegacyPerluMigrasi() throws SQLException {
        String sql = "select is_nullable,character_maximum_length "
                + "from information_schema.columns "
                + "where table_schema=database() "
                + "and table_name='radiologi_pacs_token' "
                + "and column_name='token_pacs' limit 1";
        try (PreparedStatement pst = koneksi.prepareStatement(sql);
                ResultSet rst = pst.executeQuery()) {
            return rst.next()
                    && (!"YES".equalsIgnoreCase(
                            rst.getString("is_nullable"))
                    || rst.getInt("character_maximum_length") < 64);
        }
    }

    private boolean indexTabelAda(String namaIndex) throws SQLException {
        String sql = "select count(*) from information_schema.statistics "
                + "where table_schema=database() "
                + "and table_name='radiologi_pacs_token' "
                + "and index_name=?";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, namaIndex);
            try (ResultSet rst = pst.executeQuery()) {
                return rst.next() && rst.getInt(1) > 0;
            }
        }
    }

    private boolean primaryKeyMemuatKolom(String namaKolom)
            throws SQLException {
        String sql = "select count(*) from information_schema.statistics "
                + "where table_schema=database() "
                + "and table_name='radiologi_pacs_token' "
                + "and index_name='PRIMARY' and column_name=?";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, namaKolom);
            try (ResultSet rst = pst.executeQuery()) {
                return rst.next() && rst.getInt(1) > 0;
            }
        }
    }

    private void simpanAccessionOrthanc(
            String noRawat, String tglPeriksa, String jamPeriksa,
            String noOrder, String kdJenisPrw, String accessionNumber,
            String orthancStudyId) throws SQLException {
        String sql = "insert into radiologi_pacs_token "
                + "(no_rawat,tgl_periksa,jam,token_pacs,noorder,"
                + "kd_jenis_prw,accession_number,orthanc_study_id,"
                + "tgl_buat,pembuat,tgl_kirim,pengirim) "
                + "values (?,?,?,null,?,?,?,?,now(),?,now(),?) "
                + "on duplicate key update "
                + "noorder=values(noorder),"
                + "accession_number=values(accession_number),"
                + "orthanc_study_id=values(orthanc_study_id),"
                + "tgl_kirim=now(),pengirim=values(pengirim)";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, noRawat);
            pst.setString(2, tglPeriksa);
            pst.setString(3, jamPeriksa);
            pst.setString(4, noOrder);
            pst.setString(5, kdJenisPrw);
            pst.setString(6, accessionNumber);
            pst.setString(7, orthancStudyId);
            pst.setString(8, akses.getkode());
            pst.setString(9, akses.getkode());
            pst.executeUpdate();
        }
    }

    /**
     * @return accession_number, noorder, orthanc_study_id.
     */
    private String[] ambilDataAccessionOrthanc(
            String noRawat, String tglPeriksa, String jamPeriksa,
            String kdJenisPrw) {
        String[] hasil = new String[]{"", "", ""};
        String sql = "select accession_number,noorder,orthanc_study_id "
                + "from radiologi_pacs_token "
                + "where no_rawat=? and tgl_periksa=? and jam=? "
                + "and kd_jenis_prw=? limit 1";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, noRawat);
            pst.setString(2, tglPeriksa);
            pst.setString(3, jamPeriksa);
            pst.setString(4, kdJenisPrw);
            try (ResultSet rst = pst.executeQuery()) {
                if (rst.next()) {
                    hasil[0] = teksAman(rst.getString("accession_number"));
                    hasil[1] = teksAman(rst.getString("noorder"));
                    hasil[2] = teksAman(rst.getString("orthanc_study_id"));
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Notif Ambil Accession Number Orthanc : " + e);
        }
        return hasil;
    }

    /**
     * Mencegah satu study Orthanc ditimpa oleh accession pemeriksaan lain.
     *
     * @return accession_number, no_rawat, kd_jenis_prw.
     */
    private String[] ambilMappingStudyOrthancLain(
            String orthancStudyId, String accessionNumber)
            throws SQLException {
        String[] hasil = new String[]{"", "", ""};
        String sql = "select accession_number,no_rawat,kd_jenis_prw "
                + "from radiologi_pacs_token "
                + "where orthanc_study_id=? and accession_number<>? "
                + "and accession_number is not null "
                + "and accession_number<>'' limit 1";
        try (PreparedStatement pst = koneksi.prepareStatement(sql)) {
            pst.setString(1, orthancStudyId);
            pst.setString(2, accessionNumber);
            try (ResultSet rst = pst.executeQuery()) {
                if (rst.next()) {
                    hasil[0] = teksAman(
                            rst.getString("accession_number"));
                    hasil[1] = teksAman(rst.getString("no_rawat"));
                    hasil[2] = teksAman(
                            rst.getString("kd_jenis_prw"));
                }
            }
        }
        return hasil;
    }

    private boolean studyOrthancBolehDipakai(
            String orthancStudyId, String accessionNumber)
            throws SQLException {
        String[] mapping = ambilMappingStudyOrthancLain(
                orthancStudyId, accessionNumber);
        if (mapping[0].equals("")) {
            return true;
        }
        JOptionPane.showMessageDialog(null,
                "Study Orthanc ini sudah dipakai oleh mapping "
                + "pemeriksaan lain dan tidak boleh ditimpa..!!\n\n"
                + "Accession tersimpan : " + mapping[0] + "\n"
                + "No. Rawat           : " + mapping[1] + "\n"
                + "Kode Pemeriksaan    : " + mapping[2] + "\n"
                + "Orthanc Study ID    : " + orthancStudyId + "\n\n"
                + "Silahkan pilih study Orthanc yang berbeda.");
        return false;
    }

    private String teksAman(String nilai) {
        return nilai == null ? "" : nilai.trim();
    }

    /**
     * Mencari study memakai Orthanc REST API. Field kosong sengaja tidak
     * dimasukkan ke Query sehingga method ini juga dapat dipakai untuk fallback.
     */
    private JsonNode cariStudyOrthanc(
            String accessionNumber, String patientId, String studyDate)
            throws Exception {
        com.fasterxml.jackson.databind.node.ObjectNode query =
                orthancMapper.createObjectNode();
        if (!teksAman(accessionNumber).equals("")) {
            query.put("AccessionNumber", accessionNumber.trim());
        }
        if (!teksAman(patientId).equals("")) {
            query.put("PatientID", patientId.trim());
        }
        if (!teksAman(studyDate).equals("")) {
            query.put("StudyDate", studyDate.trim());
        }

        com.fasterxml.jackson.databind.node.ObjectNode body =
                orthancMapper.createObjectNode();
        body.put("Level", "Study");
        body.set("Query", query);
        body.put("Expand", true);
        return requestJsonOrthanc("/tools/find", "POST", body);
    }

    private JsonNode ambilStudyOrthanc(String orthancStudyId)
            throws Exception {
        validasiIdOrthanc(orthancStudyId);
        return requestJsonOrthanc(
                "/studies/" + orthancStudyId, "GET", null);
    }

    private JsonNode ambilSeriesOrthanc(String orthancSeriesId)
            throws Exception {
        validasiIdOrthanc(orthancSeriesId);
        return requestJsonOrthanc(
                "/series/" + orthancSeriesId, "GET", null);
    }

    private boolean ubahAccessionOrthanc(
            String orthancStudyId, String accessionNumber) {
        try {
            validasiIdOrthanc(orthancStudyId);
            com.fasterxml.jackson.databind.node.ObjectNode replace =
                    orthancMapper.createObjectNode();
            replace.put("AccessionNumber", accessionNumber);

            com.fasterxml.jackson.databind.node.ObjectNode body =
                    orthancMapper.createObjectNode();
            body.set("Replace", replace);
            body.put("KeepSource", false);
            requestJsonOrthanc(
                    "/studies/" + orthancStudyId + "/modify",
                    "POST", body);
            return true;
        } catch (Exception e) {
            System.out.println(
                    "Notif Ubah Accession Orthanc : " + e);
            return false;
        }
    }

    private void validasiIdOrthanc(String id) throws IOException {
        if (id == null || !id.matches("[A-Fa-f0-9-]+")) {
            throw new IOException("ID resource Orthanc tidak valid");
        }
    }

    private JsonNode requestJsonOrthanc(
            String path, String method, JsonNode body) throws Exception {
        ApiOrthanc orthanc = new ApiOrthanc();
        org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();
        headers.add("Authorization", "Basic " + orthanc.Auth());
        headers.add("Accept", "application/json");

        String requestBody = body == null
                ? null : orthancMapper.writeValueAsString(body);
        if (body != null) {
            headers.setContentType(
                    org.springframework.http.MediaType.APPLICATION_JSON);
        }
        org.springframework.http.HttpEntity<String> request =
                body == null
                        ? new org.springframework.http.HttpEntity<String>(
                                headers)
                        : new org.springframework.http.HttpEntity<String>(
                                requestBody, headers);
        org.springframework.http.ResponseEntity<String> response =
                orthanc.getRest().exchange(
                        urlDasarOrthanc() + path,
                        org.springframework.http.HttpMethod.valueOf(method),
                        request,
                        String.class
                );
        String responseBody = response.getBody();
        if (responseBody == null || responseBody.trim().equals("")) {
            return orthancMapper.createObjectNode();
        }
        return orthancMapper.readTree(responseBody);
    }

    private String urlDasarOrthanc() {
        String url = koneksiDB.URLORTHANC().trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url + ":" + String.valueOf(koneksiDB.PORTORTHANC()).trim();
    }

    private int jumlahDataOrthanc(JsonNode data) {
        if (data == null || data.isNull() || data.isMissingNode()) {
            return 0;
        }
        if (data.isArray()) {
            return data.size();
        }
        return data.path("ID").asText().equals("") ? 0 : 1;
    }

    private List<JsonNode> daftarStudyOrthanc(JsonNode studies) {
        List<JsonNode> daftar = new ArrayList<JsonNode>();
        if (studies == null) {
            return daftar;
        }
        if (studies.isArray()) {
            for (JsonNode study : studies) {
                if (!study.path("ID").asText().equals("")) {
                    daftar.add(study);
                }
            }
        } else if (!studies.path("ID").asText().equals("")) {
            daftar.add(studies);
        }
        return daftar;
    }

    private JsonNode pilihStudyOrthanc(
            JsonNode studies, String judul) {
        List<JsonNode> daftar = daftarStudyOrthanc(studies);
        if (daftar.isEmpty()) {
            return null;
        }
        if (daftar.size() == 1) {
            return daftar.get(0);
        }

        LinkedHashMap<String, JsonNode> pilihanStudy =
                new LinkedHashMap<String, JsonNode>();
        for (JsonNode study : daftar) {
            pilihanStudy.put(labelStudyOrthanc(study), study);
        }
        Object pilihanStudyUser = JOptionPane.showInputDialog(
                null,
                "Ditemukan beberapa study. Pilih study yang tepat:",
                judul,
                JOptionPane.QUESTION_MESSAGE,
                null,
                pilihanStudy.keySet().toArray(),
                pilihanStudy.keySet().iterator().next()
        );
        if (pilihanStudyUser == null) {
            return null;
        }
        return pilihanStudy.get(pilihanStudyUser.toString());
    }

    private String labelStudyOrthanc(JsonNode study) {
        String deskripsi = tagStudyOrthanc(
                study, "StudyDescription");
        if (deskripsi.equals("")) {
            deskripsi = "(tanpa deskripsi)";
        }
        String accession = tagStudyOrthanc(
                study, "AccessionNumber");
        if (accession.equals("")) {
            accession = "(kosong)";
        }
        return tagPatientOrthanc(study, "PatientID")
                + " | " + tagStudyOrthanc(study, "StudyDate")
                + " | " + deskripsi
                + " | Accession: " + accession
                + " | ID: " + study.path("ID").asText();
    }

    private String tagStudyOrthanc(JsonNode study, String namaTag) {
        if (study == null) {
            return "";
        }
        String nilai = study.path("MainDicomTags")
                .path(namaTag).asText();
        if (nilai.equals("")) {
            nilai = study.path("RequestedTags")
                    .path(namaTag).asText();
        }
        return nilai.trim();
    }

    private String tagPatientOrthanc(JsonNode study, String namaTag) {
        if (study == null) {
            return "";
        }
        String nilai = study.path("PatientMainDicomTags")
                .path(namaTag).asText();
        if (nilai.equals("")) {
            nilai = tagStudyOrthanc(study, namaTag);
        }
        return nilai.trim();
    }

    private boolean konfirmasiPatientStudy(
            JsonNode study, String patientId, String accessionNumber,
            String konteks) {
        String patientStudy = tagPatientOrthanc(study, "PatientID");
        String accessionStudy =
                tagStudyOrthanc(study, "AccessionNumber");
        boolean patientBerbeda = !patientId.equals("")
                && !patientId.equals(patientStudy);
        boolean accessionBerbeda = !accessionNumber.equals("")
                && !accessionNumber.equals(accessionStudy);
        if (!patientBerbeda && !accessionBerbeda) {
            return true;
        }

        String peringatan = konteks
                + "\n\nData study tidak sepenuhnya sama dengan pemeriksaan:"
                + "\nPatient ID pemeriksaan : " + patientId
                + "\nPatient ID study       : " + patientStudy
                + "\nAccession dicari       : " + accessionNumber
                + "\nAccession study        : " + accessionStudy
                + "\nOrthanc Study ID       : "
                + study.path("ID").asText()
                + "\n\nTetap gunakan study ini?";
        return JOptionPane.showConfirmDialog(
                null,
                peringatan,
                "Verifikasi Study Orthanc",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    private String pilihSeriesOrthanc(JsonNode study) throws Exception {
        JsonNode detailStudy = study;
        JsonNode daftarSeries = detailStudy.path("Series");
        if (!daftarSeries.isArray() || daftarSeries.size() == 0) {
            detailStudy = ambilStudyOrthanc(
                    study.path("ID").asText());
            daftarSeries = detailStudy.path("Series");
        }

        List<String> seriesIds = new ArrayList<String>();
        if (daftarSeries.isArray()) {
            for (JsonNode series : daftarSeries) {
                String id = series.asText().trim();
                if (!id.equals("") && !seriesIds.contains(id)) {
                    seriesIds.add(id);
                }
            }
        }
        if (seriesIds.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Study Orthanc ini belum mempunyai series/citra..!!");
            return "";
        }
        if (seriesIds.size() == 1) {
            return seriesIds.get(0);
        }

        String seriesTabel = "";
        int barisSeries = tbListDicom.getSelectedRow();
        if (barisSeries >= 0) {
            seriesTabel = nilaiTabel(tbListDicom, barisSeries, 2);
        }

        LinkedHashMap<String, String> pilihanSeries =
                new LinkedHashMap<String, String>();
        String pilihanAwal = "";
        for (String seriesId : seriesIds) {
            String label;
            try {
                label = labelSeriesOrthanc(
                        ambilSeriesOrthanc(seriesId), seriesId);
            } catch (Exception e) {
                label = "Series ID: " + seriesId;
            }
            pilihanSeries.put(label, seriesId);
            if (seriesId.equals(seriesTabel)) {
                pilihanAwal = label;
            }
        }
        if (pilihanAwal.equals("")) {
            pilihanAwal = pilihanSeries.keySet().iterator().next();
        }

        Object pilihanSeriesUser = JOptionPane.showInputDialog(
                null,
                "Study mempunyai beberapa series.\n"
                + "Pilih series/citra yang akan dibuka:",
                "Pilih Series Orthanc",
                JOptionPane.QUESTION_MESSAGE,
                null,
                pilihanSeries.keySet().toArray(),
                pilihanAwal
        );
        if (pilihanSeriesUser == null) {
            return "";
        }
        return pilihanSeries.get(pilihanSeriesUser.toString());
    }

    private String labelSeriesOrthanc(
            JsonNode series, String seriesId) {
        JsonNode tags = series.path("MainDicomTags");
        String modality = tags.path("Modality").asText();
        String nomor = tags.path("SeriesNumber").asText();
        String deskripsi = tags.path("SeriesDescription").asText();
        if (modality.equals("")) {
            modality = "(tanpa modality)";
        }
        if (nomor.equals("")) {
            nomor = "-";
        }
        if (deskripsi.equals("")) {
            deskripsi = "(tanpa deskripsi)";
        }
        return modality + " | Series " + nomor + " | " + deskripsi
                + " | ID: " + seriesId;
    }

    private String pesanKesalahan(Exception e) {
        Throwable sumber = e;
        while (sumber.getCause() != null
                && sumber.getCause() != sumber) {
            sumber = sumber.getCause();
        }
        String pesan = sumber.getMessage();
        return pesan == null || pesan.trim().equals("")
                ? sumber.toString() : pesan;
    }
    private void getData() {
        Kd2.setText("");
        TDiagnosaKlinis.setText("");
        TNoSEP.setText("");        
        if(tbDokter.getSelectedRow()!= -1){
            Kd2.setText(tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());     
            Petugas.setText(tbDokter.getValueAt(tbDokter.getSelectedRow(),6).toString());
            TDiagnosaKlinis.setText(tbDokter.getValueAt(tbDokter.getSelectedRow(),8).toString());
            try{
                TNoSEP.setText(tbDokter.getValueAt(tbDokter.getSelectedRow(),9).toString());
                }
            catch (Exception e){
            }            
        }
    }
    
    public void isCek(){
        MnCetakNota.setEnabled(akses.getperiksa_radiologi());
        BtnHapus.setEnabled(akses.getperiksa_radiologi());
        MnUbahDokterPetugas.setEnabled(akses.getperiksa_radiologi());
        btnAmbilPhoto.setEnabled(akses.getperiksa_radiologi());
        BtnSimpan.setEnabled(akses.getperiksa_radiologi());
        BtnPrint.setEnabled(akses.getperiksa_radiologi());
        ppRiwayat.setEnabled(akses.getresume_pasien());
        ppBerkasDigital.setEnabled(akses.getberkas_digital_perawatan());     
    }
 
    public void setPasien(String pasien){
        NoRawat.setText(pasien);
    }

    private void panggilMedis() {
        try {
            ps5=koneksi.prepareStatement(
                "select periksa_radiologi.nip,petugas.nama,periksa_radiologi.dokter_perujuk,"+
                "periksa_radiologi.kd_dokter,dokter.nm_dokter from periksa_radiologi "+
                "inner join petugas on periksa_radiologi.nip=petugas.nip "+
                "inner join dokter on periksa_radiologi.kd_dokter=dokter.kd_dokter "+
                "where periksa_radiologi.no_rawat=? "+
                "and periksa_radiologi.tgl_periksa=? and periksa_radiologi.jam=?");  
            try {
                ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                rs5=ps5.executeQuery();
                if(rs5.next()){      
                    KodePerujuk.setText(rs5.getString("dokter_perujuk"));
                    NmPerujuk.setText(dokter.tampil3(rs5.getString("dokter_perujuk")));
                    KodePj.setText(rs5.getString("kd_dokter"));
                    NmDokterPj.setText(rs5.getString("nm_dokter"));
                    KdPtgUbah.setText(rs5.getString("nip"));
                    NmPtgUbah.setText(rs5.getString("nama"));
                    WindowGantiDokterParamedis.setSize(600,130);
                    WindowGantiDokterParamedis.setLocationRelativeTo(internalFrame1);
                    WindowGantiDokterParamedis.setVisible(true);
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
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
    }
 
    private void isPhoto(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(internalFrame1.getWidth()-300,HEIGHT));
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
        if(TabData.isVisible()==true){
            if(tbDokter.getSelectedRow()!= -1){
                if((!Kd2.getText().equals(""))&&(!Petugas.getText().equals(""))){
                     try {
                        ps=koneksi.prepareStatement("select gambar_radiologi.lokasi_gambar from gambar_radiologi where gambar_radiologi.no_rawat=? and gambar_radiologi.tgl_periksa=? and gambar_radiologi.jam=? ");
                        try {
                            ps.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs=ps.executeQuery();
                            htmlContent = new StringBuilder();
                            while(rs.next()){
                                htmlContent.append("<tr><td border='0' align='center'><a href='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/radiologi/"+rs.getString("lokasi_gambar")+"'><img src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/radiologi/"+rs.getString("lokasi_gambar")+"' alt='photo' width='"+(internalFrame1.getWidth()-370)+"' height='"+(internalFrame1.getWidth()-370)+"'/></a></td></tr>");
                            }
                            LoadHTML.setText(
                                "<html>"+
                                  "<table width='100%' border='0' align='center' cellpadding='1px' cellspacing='1px' class='tbl_form'>"+
                                    htmlContent.toString()+
                                  "</table>"+
                                "</html>"
                            );
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
                        
                        ps5=koneksi.prepareStatement("select hasil_radiologi.hasil from hasil_radiologi where hasil_radiologi.no_rawat=? and hasil_radiologi.tgl_periksa=? and hasil_radiologi.jam=?");  
                        try {
                            ps5.setString(1,tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString());
                            ps5.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                            ps5.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                            rs5=ps5.executeQuery();
                            if(rs5.next()){  
                                HasilPeriksa.setText(rs5.getString("hasil"));
                            }else{
                                HasilPeriksa.setText("");
                            }
                        } catch (Exception e) {
                            System.out.println("Notif ps5 : "+e);
                        } finally{
                            if(rs5!=null){
                                rs5.close();
                            }
                            if(ps5!=null){
                                ps5.close();
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } 
                }else{
                    LoadHTML.setText("<html><body><center><br><br><font face='tahoma' size='2' color='#434343'></font></center></body></html>");
                    HasilPeriksa.setText("");
                }
            }
        }
    }
    
//     public void RadPdfKlaimBPJS(String NomorRawat, String NoRekamMedis) {
//         pemeriksaan = "";
//         try {
//             ps2 = koneksi.prepareStatement(
//                     "SELECT\n"
//                     + "	permintaan_radiologi.noorder, \n"
//                     + "	periksa_radiologi.no_rawat, \n"
//                     + "	permintaan_radiologi.tgl_permintaan, \n"
//                     + "	permintaan_radiologi.jam_permintaan, \n"
//                     + "	permintaan_radiologi.tgl_sampel, \n"
//                     + "	permintaan_radiologi.jam_sampel, \n"
//                     + "	permintaan_radiologi.tgl_hasil, \n"
//                     + "	permintaan_radiologi.jam_hasil, \n"
//                     + "	periksa_radiologi.kd_dokter, \n"
//                     + "	permintaan_radiologi.diagnosa_klinis, \n"
//                     + "	periksa_radiologi.kd_jenis_prw, \n"
//                     + "	jns_perawatan_radiologi.nm_perawatan, \n"
//                     + "	hasil_radiologi.hasil, \n"
//                     + "	reg_periksa.no_rkm_medis\n"
//                     + "FROM\n"
//                     + "	permintaan_radiologi\n"
//                     + "	INNER JOIN\n"
//                     + "	periksa_radiologi\n"
//                     + "	ON \n"
//                     + "		permintaan_radiologi.no_rawat = periksa_radiologi.no_rawat\n"
//                     + "	INNER JOIN\n"
//                     + "	jns_perawatan_radiologi\n"
//                     + "	ON \n"
//                     + "		periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw\n"
//                     + "	INNER JOIN\n"
//                     + "	hasil_radiologi\n"
//                     + "	ON \n"
//                     + "		periksa_radiologi.no_rawat = hasil_radiologi.no_rawat\n"
//                     + "	INNER JOIN\n"
//                     + "	reg_periksa\n"
//                     + "	ON \n"
//                     + "		hasil_radiologi.no_rawat = reg_periksa.no_rawat AND\n"
//                     + "		periksa_radiologi.no_rawat = reg_periksa.no_rawat AND\n"
//                     + "		permintaan_radiologi.no_rawat = reg_periksa.no_rawat where periksa_radiologi.no_rawat='" + NomorRawat + "' \n"
//                     + "		group by permintaan_radiologi.noorder,periksa_radiologi.no_rawat");
//             try {
// //                ps2.setString(1, NomorRawat);
//                 rs2 = ps2.executeQuery();
//                 while (rs2.next()) {
// //                    pemeriksaan = rs2.getString("nm_perawatan") + "\n" + pemeriksaan;
// //                    kdpenjab = rs2.getString("kd_dokter");
// //                    kdpetugas = rs2.getString("nip");
// //                    System.out.println("ps mencari hasil radiologi" + rs2.getString("no_rawat"));
//                     pscetakradiologi1 = koneksi.prepareStatement("SELECT\n"
//                             + "	permintaan_radiologi.noorder, \n"
//                             + "	jns_perawatan_radiologi.nm_perawatan, \n"
//                             + "	permintaan_radiologi.tgl_sampel, \n"
//                             + "	permintaan_radiologi.jam_sampel, \n"
//                             + "	hasil_radiologi.tgl_periksa, \n"
//                             + "	hasil_radiologi.jam, \n"
//                             + "	hasil_radiologi.hasil, \n"
//                             + "	hasil_radiologi.hasil\n"
//                             + "FROM\n"
//                             + "	periksa_radiologi\n"
//                             + "	INNER JOIN\n"
//                             + "	jns_perawatan_radiologi\n"
//                             + "	ON \n"
//                             + "		periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw\n"
//                             + "	INNER JOIN\n"
//                             + "	permintaan_radiologi\n"
//                             + "	ON \n"
//                             + "		permintaan_radiologi.no_rawat = periksa_radiologi.no_rawat AND\n"
//                             + "		periksa_radiologi.tgl_periksa = permintaan_radiologi.tgl_hasil AND\n"
//                             + "		periksa_radiologi.jam = permintaan_radiologi.jam_hasil\n"
//                             + "	INNER JOIN\n"
//                             + "	dokter\n"
//                             + "	ON \n"
//                             + "		periksa_radiologi.dokter_perujuk = dokter.kd_dokter\n"
//                             + "	INNER JOIN\n"
//                             + "	hasil_radiologi\n"
//                             + "	ON \n"
//                             + "		periksa_radiologi.no_rawat = hasil_radiologi.no_rawat AND\n"
//                             + "		periksa_radiologi.tgl_periksa = hasil_radiologi.tgl_periksa AND\n"
//                             + "		periksa_radiologi.jam = hasil_radiologi.jam \n"
//                             + "		\n"
//                             + "		where periksa_radiologi.no_rawat='" + rs2.getString("no_rawat") + "'");

//                     try {
//                         rscetakradiologi1 = pscetakradiologi1.executeQuery();
//                         int nomorurut = 0;

//                         while (rscetakradiologi1.next()) {
//                             nomorurut++;
// //                            System.out.println("nomor urut " + nomorurut);
//                             Map<String, Object> param = new HashMap<>();
//                             param.put("noperiksa", NomorRawat);
//                             param.put("norm", rs2.getString("no_rkm_medis"));
//                             param.put("namapasien", Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis='" + rs2.getString("no_rkm_medis") + "'"));
//                             param.put("jkel", Sequel.cariIsi("select jk from pasien where no_rkm_medis='" + rs2.getString("no_rkm_medis") + "'"));
//                             param.put("tgl_lahir", Sequel.cariIsi("select DATE_FORMAT(tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis='" + rs2.getString("no_rkm_medis") + "'"));
//                             param.put("lahir", Sequel.cariIsi("select DATE_FORMAT(tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis='" + rs2.getString("no_rkm_medis") + "'"));
//                             param.put("pengirim", Sequel.cariIsi("SELECT dokter.nm_dokter FROM periksa_radiologi INNER JOIN dokter ON periksa_radiologi.dokter_perujuk = dokter.kd_dokter WHERE periksa_radiologi.no_rawat = ?", rs2.getString("no_rawat")));
//                             param.put("tanggal", rscetakradiologi1.getString("tgl_sampel"));
//                             param.put("penjab", Sequel.cariIsi("SELECT\n"
//                                     + "	dokter.nm_dokter\n"
//                                     + "FROM\n"
//                                     + "	dokter\n"
//                                     + "	INNER JOIN\n"
//                                     + "	set_pjlab\n"
//                                     + "	ON \n"
//                                     + "		dokter.kd_dokter = set_pjlab.kd_dokterrad"));
//                             param.put("petugas", "");
//                             param.put("alamat", "");
//                             param.put("kamar", "");
//                             param.put("namakamar", "");
//                             param.put("pemeriksaan", rscetakradiologi1.getString("nm_perawatan"));
//                             //param.put("jam", tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
//                             param.put("jam", rscetakradiologi1.getString("jam_sampel"));
//                             param.put("namars", akses.getnamars());
//                             param.put("alamatrs", akses.getalamatrs());
//                             param.put("kotars", akses.getkabupatenrs());
//                             param.put("propinsirs", akses.getpropinsirs());
//                             param.put("kontakrs", akses.getkontakrs());
//                             param.put("emailrs", akses.getemailrs());
//                             param.put("hasil", rscetakradiologi1.getString("hasil"));
//                             param.put("logo", Sequel.cariGambar("select logo from setting"));
//                             param.put("jam_hasil", rscetakradiologi1.getString("jam"));
//                             param.put("diagnosa_klinis", rs2.getString("diagnosa_klinis"));
//                             finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", kdpenjab);
//                             param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + Sequel.cariIsi("SELECT\n"
//                                     + "	dokter.nm_dokter\n"
//                                     + "FROM\n"
//                                     + "	dokter\n"
//                                     + "	INNER JOIN\n"
//                                     + "	set_pjlab\n"
//                                     + "	ON \n"
//                                     + "		dokter.kd_dokter = set_pjlab.kd_dokterrad") + "\nID " + (finger.equals("") ? kdpenjab : finger) + "\n" + rscetakradiologi1.getString("tgl_periksa"));
//                             finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", kdpetugas);
//                             param.put("finger2", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + petugas + "\nID " + (finger.equals("") ? kdpetugas : finger) + "\n" + rscetakradiologi1.getString("tgl_periksa"));
//                             String hasil = Sequel.cariIsi("select hasil from hasil_radiologi where hasil_radiologi.no_rawat='" + NomorRawat + "' and hasil_radiologi.tgl_periksa='" + rscetakradiologi1.getString("tgl_periksa") + "' and hasil_radiologi.jam='" + rscetakradiologi1.getString("jam") + "'").toString();

//                             if (hasil.contains("<p>") || hasil.contains("<strong>") || hasil.contains("<ul>")) {
//                                 Valid.MyReport("rptPeriksaRadiologiHtml.jasper", "report", "RAD", param);
//                             } else {
//                                 Valid.MyReport("rptPeriksaRadiologi.jasper", "report", "RAD", param);
//                             }

//                         }
//                     } catch (Exception e) {

//                     } finally {
//                         if (rscetakradiologi1 != null) {
//                             rscetakradiologi1.close();
//                         }
//                         if (pscetakradiologi1 != null) {
//                             pscetakradiologi1.close();
//                         }
//                     }

//                 }
//             } catch (Exception e) {
//                 System.out.println("simrskhanza.DlgCariPeriksaRadiologi.BtnPrint1ActionPerformed() ps2 : " + e);
//             } finally {
//                 if (rs2 != null) {
//                     rs2.close();
//                 }
//                 if (ps2 != null) {
//                     ps2.close();
//                 }
//             }

//         } catch (Exception e) {
//             System.out.println("Notifikasi Pemeriksaan : " + e);
//         }

//         this.setCursor(Cursor.getDefaultCursor());

//     }

//     public void RadPdfKlaimBPJSKompilasi(String NomorRawat, String NoRekamMedis) {
//         pemeriksaan = "";
//         try {
//             ps2 = koneksi.prepareStatement(
//                     "SELECT\n"
//                     + "	permintaan_radiologi.noorder, \n"
//                     + "	periksa_radiologi.no_rawat, \n"
//                     + "	permintaan_radiologi.tgl_permintaan, \n"
//                     + "	permintaan_radiologi.jam_permintaan, \n"
//                     + "	permintaan_radiologi.tgl_sampel, \n"
//                     + "	permintaan_radiologi.jam_sampel, \n"
//                     + "	permintaan_radiologi.tgl_hasil, \n"
//                     + "	permintaan_radiologi.jam_hasil, \n"
//                     + "	periksa_radiologi.kd_dokter, \n"
//                     + "	permintaan_radiologi.diagnosa_klinis, \n"
//                     + "	periksa_radiologi.kd_jenis_prw, \n"
//                     + "	jns_perawatan_radiologi.nm_perawatan, \n"
//                     + "	hasil_radiologi.hasil, \n"
//                     + "	reg_periksa.no_rkm_medis\n"
//                     + "FROM\n"
//                     + "	permintaan_radiologi\n"
//                     + "	INNER JOIN\n"
//                     + "	periksa_radiologi\n"
//                     + "	ON \n"
//                     + "		permintaan_radiologi.no_rawat = periksa_radiologi.no_rawat\n"
//                     + "	INNER JOIN\n"
//                     + "	jns_perawatan_radiologi\n"
//                     + "	ON \n"
//                     + "		periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw\n"
//                     + "	INNER JOIN\n"
//                     + "	hasil_radiologi\n"
//                     + "	ON \n"
//                     + "		periksa_radiologi.no_rawat = hasil_radiologi.no_rawat\n"
//                     + "	INNER JOIN\n"
//                     + "	reg_periksa\n"
//                     + "	ON \n"
//                     + "		hasil_radiologi.no_rawat = reg_periksa.no_rawat AND\n"
//                     + "		periksa_radiologi.no_rawat = reg_periksa.no_rawat AND\n"
//                     + "		permintaan_radiologi.no_rawat = reg_periksa.no_rawat where periksa_radiologi.no_rawat='" + NomorRawat + "' \n"
//                     + "		group by permintaan_radiologi.noorder,periksa_radiologi.no_rawat");
//             try {
// //                ps2.setString(1, NomorRawat);
//                 rs2 = ps2.executeQuery();
//                 while (rs2.next()) {
// //                    pemeriksaan = rs2.getString("nm_perawatan") + "\n" + pemeriksaan;
// //                    kdpenjab = rs2.getString("kd_dokter");
// //                    kdpetugas = rs2.getString("nip");
// //                    System.out.println("ps mencari hasil radiologi" + rs2.getString("no_rawat"));
//                     pscetakradiologi1 = koneksi.prepareStatement("SELECT\n"
//                             + "	permintaan_radiologi.noorder, \n"
//                             + "	jns_perawatan_radiologi.nm_perawatan, \n"
//                             + "	permintaan_radiologi.tgl_sampel, \n"
//                             + "	permintaan_radiologi.jam_sampel, \n"
//                             + "	hasil_radiologi.tgl_periksa, \n"
//                             + "	hasil_radiologi.jam, \n"
//                             + "	hasil_radiologi.hasil, \n"
//                             + "	hasil_radiologi.hasil\n"
//                             + "FROM\n"
//                             + "	periksa_radiologi\n"
//                             + "	INNER JOIN\n"
//                             + "	jns_perawatan_radiologi\n"
//                             + "	ON \n"
//                             + "		periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw\n"
//                             + "	INNER JOIN\n"
//                             + "	permintaan_radiologi\n"
//                             + "	ON \n"
//                             + "		permintaan_radiologi.no_rawat = periksa_radiologi.no_rawat AND\n"
//                             + "		periksa_radiologi.tgl_periksa = permintaan_radiologi.tgl_hasil AND\n"
//                             + "		periksa_radiologi.jam = permintaan_radiologi.jam_hasil\n"
//                             + "	INNER JOIN\n"
//                             + "	dokter\n"
//                             + "	ON \n"
//                             + "		periksa_radiologi.dokter_perujuk = dokter.kd_dokter\n"
//                             + "	INNER JOIN\n"
//                             + "	hasil_radiologi\n"
//                             + "	ON \n"
//                             + "		periksa_radiologi.no_rawat = hasil_radiologi.no_rawat AND\n"
//                             + "		periksa_radiologi.tgl_periksa = hasil_radiologi.tgl_periksa AND\n"
//                             + "		periksa_radiologi.jam = hasil_radiologi.jam \n"
//                             + "		\n"
//                             + "		where periksa_radiologi.no_rawat='" + rs2.getString("no_rawat") + "'");

//                     try {
//                         rscetakradiologi1 = pscetakradiologi1.executeQuery();
//                         int nomorurut = 0;

//                         while (rscetakradiologi1.next()) {
//                             nomorurut++;
// //                            System.out.println("nomor urut " + nomorurut);
//                             Map<String, Object> param = new HashMap<>();
//                             param.put("noperiksa", NomorRawat);
//                             param.put("norm", rs2.getString("no_rkm_medis"));
//                             param.put("namapasien", Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis='" + rs2.getString("no_rkm_medis") + "'"));
//                             param.put("jkel", Sequel.cariIsi("select jk from pasien where no_rkm_medis='" + rs2.getString("no_rkm_medis") + "'"));
//                             param.put("tgl_lahir", Sequel.cariIsi("select DATE_FORMAT(tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis='" + rs2.getString("no_rkm_medis") + "'"));
//                             param.put("lahir", Sequel.cariIsi("select DATE_FORMAT(tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis='" + rs2.getString("no_rkm_medis") + "'"));
//                             param.put("pengirim", Sequel.cariIsi("SELECT dokter.nm_dokter FROM periksa_radiologi INNER JOIN dokter ON periksa_radiologi.dokter_perujuk = dokter.kd_dokter WHERE periksa_radiologi.no_rawat = ?", rs2.getString("no_rawat")));
//                             param.put("tanggal", rscetakradiologi1.getString("tgl_sampel"));
//                             param.put("penjab", Sequel.cariIsi("SELECT\n"
//                                     + "	dokter.nm_dokter\n"
//                                     + "FROM\n"
//                                     + "	dokter\n"
//                                     + "	INNER JOIN\n"
//                                     + "	set_pjlab\n"
//                                     + "	ON \n"
//                                     + "		dokter.kd_dokter = set_pjlab.kd_dokterrad"));
//                             param.put("petugas", "");
//                             param.put("alamat", "");
//                             param.put("kamar", "");
//                             param.put("namakamar", "");
//                             param.put("pemeriksaan", rscetakradiologi1.getString("nm_perawatan"));
//                             //param.put("jam", tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
//                             param.put("jam", rscetakradiologi1.getString("jam_sampel"));
//                             param.put("namars", akses.getnamars());
//                             param.put("alamatrs", akses.getalamatrs());
//                             param.put("kotars", akses.getkabupatenrs());
//                             param.put("propinsirs", akses.getpropinsirs());
//                             param.put("kontakrs", akses.getkontakrs());
//                             param.put("emailrs", akses.getemailrs());
//                             param.put("hasil", rscetakradiologi1.getString("hasil"));
//                             param.put("logo", Sequel.cariGambar("select logo from setting"));
//                             param.put("jam_hasil", rscetakradiologi1.getString("jam"));
//                             param.put("diagnosa_klinis", rs2.getString("diagnosa_klinis"));
//                             finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", kdpenjab);
//                             param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + Sequel.cariIsi("SELECT\n"
//                                     + "	dokter.nm_dokter\n"
//                                     + "FROM\n"
//                                     + "	dokter\n"
//                                     + "	INNER JOIN\n"
//                                     + "	set_pjlab\n"
//                                     + "	ON \n"
//                                     + "		dokter.kd_dokter = set_pjlab.kd_dokterrad") + "\nID " + (finger.equals("") ? kdpenjab : finger) + "\n" + rscetakradiologi1.getString("tgl_periksa"));
//                             finger = Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", kdpetugas);
//                             param.put("finger2", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + petugas + "\nID " + (finger.equals("") ? kdpetugas : finger) + "\n" + rscetakradiologi1.getString("tgl_periksa"));
//                             String hasil = Sequel.cariIsi("select hasil from hasil_radiologi where hasil_radiologi.no_rawat='" + NomorRawat + "' and hasil_radiologi.tgl_periksa='" + rscetakradiologi1.getString("tgl_periksa") + "' and hasil_radiologi.jam='" + rscetakradiologi1.getString("jam") + "'").toString();

//                             if (hasil.contains("<p>") || hasil.contains("<strong>") || hasil.contains("<ul>")) {
//                                 Valid.MyReportPDFKlaim("rptPeriksaRadiologiHtml.jasper", "report", "4RAD", param, "hasilkompilasiklaim", NomorRawat.replaceAll("/", "") + "-" + nomorurut);

//                             } else {
//                                 Valid.MyReportPDFKlaim("rptPeriksaRadiologi.jasper", "report", "4RAD", param, "hasilkompilasiklaim", NomorRawat.replaceAll("/", "") + "-" + nomorurut);

//                             }
// //                            Valid.MyReportPDFKlaim("rptPeriksaRadiologi.jasper", "report", "4RAD", param, "hasilkompilasiklaim", NomorRawat.replaceAll("/", "") + "-" + nomorurut);

//                         }
//                     } catch (Exception e) {

//                     } finally {
//                         if (rscetakradiologi1 != null) {
//                             rscetakradiologi1.close();
//                         }
//                         if (pscetakradiologi1 != null) {
//                             pscetakradiologi1.close();
//                         }
//                     }

//                 }
//             } catch (Exception e) {
//                 System.out.println("simrskhanza.DlgCariPeriksaRadiologi.BtnPrint1ActionPerformed() ps2 : " + e);
//             } finally {
//                 if (rs2 != null) {
//                     rs2.close();
//                 }
//                 if (ps2 != null) {
//                     ps2.close();
//                 }
//             }

//         } catch (Exception e) {
//             System.out.println("Notifikasi Pemeriksaan : " + e);
//         }

//         this.setCursor(Cursor.getDefaultCursor());

//     }

    private void tampilOrthanc() {
        if(TabData.isVisible()==true){
            if(tbDokter.getSelectedRow()!= -1){
                if((!Kd2.getText().equals(""))&&(!Petugas.getText().equals(""))){
                     if(TabData.getSelectedIndex()==2){
                         try {
                             Valid.tabelKosong(tabModeDicom);
                             ApiOrthanc orthanc=new ApiOrthanc();
                             root=orthanc.AmbilSeries(Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=?",tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString()),Valid.SetTgl(Tgl1.getSelectedItem()+"").replaceAll("-",""),Valid.SetTgl(Tgl2.getSelectedItem()+"").replaceAll("-",""));
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
    }
    
private void CreatePDF(String FileName) {
        if(Kd2.getText().equals("")){
               JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu...!!!!"); 
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            pemeriksaan="";
            try {
                ps2=koneksi.prepareStatement(
                            "select jns_perawatan_radiologi.kd_jenis_prw,jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.biaya,"+
                            "periksa_radiologi.kd_dokter,periksa_radiologi.nip,periksa_radiologi.proyeksi,periksa_radiologi.kV,periksa_radiologi.mAS,periksa_radiologi.FFD,"+
                            "periksa_radiologi.BSF,periksa_radiologi.inak,periksa_radiologi.jml_penyinaran,periksa_radiologi.dosis from periksa_radiologi inner join jns_perawatan_radiologi "+
                            "on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw where periksa_radiologi.no_rawat=? and periksa_radiologi.tgl_periksa=? "+
                            "and periksa_radiologi.jam=?"); 
                try {
                    ps2.setString(1,Kd2.getText());
                    ps2.setString(2,tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString());
                    ps2.setString(3,tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
                    rs2=ps2.executeQuery();
                    while(rs2.next()){
                        pemeriksaan=rs2.getString("nm_perawatan")+", "+pemeriksaan;
                        kdpenjab=rs2.getString("kd_dokter");
                        kdpetugas=rs2.getString("nip");
                    }
                } catch (Exception e) {
                    System.out.println("simrskhanza.DlgCariPeriksaRadiologi.BtnPrint1ActionPerformed() ps2 : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                    if(ps2!=null){
                        ps2.close();
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Notifikasi Pemeriksaan : "+e);
            }          
            Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=? ",NoRM,Kd2.getText());
            Sequel.cariIsi("select pasien.jk from pasien where pasien.no_rkm_medis=? ",Jk,NoRM.getText());
            Sequel.cariIsi("select umur from pasien where no_rkm_medis=?",Umur,NoRM.getText());
            Sequel.cariIsi("select concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat from pasien inner join kelurahan inner join kecamatan inner join kabupaten on pasien.kd_kel=kelurahan.kd_kel and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab where no_rkm_medis=? ",Alamat,NoRM.getText());
            
            kamar=Sequel.cariIsi("select ifnull(kd_kamar,'') from kamar_inap where no_rawat='"+Kd2.getText()+"' order by tgl_masuk desc limit 1");
            if(!kamar.equals("")){
                namakamar=kamar+", "+Sequel.cariIsi("select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "+
                            " where kamar.kd_kamar='"+kamar+"' ");            
                kamar="Kamar";
            }else if(kamar.equals("")){
                kamar="Poli";
                namakamar=Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "+
                            "where reg_periksa.no_rawat='"+Kd2.getText()+"'");
            }
            Map<String, Object> param = new HashMap<>();
            param.put("noperiksa",Kd2.getText());
            param.put("norm",NoRM.getText());
            param.put("namapasien",Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=? ",NoRM.getText()));
            param.put("jkel",Jk.getText());
            param.put("umur",Umur.getText());
            param.put("nik",Sequel.cariIsi("select no_ktp from pasien where pasien.no_rkm_medis=? ",NoRM.getText()));
            param.put("lahir",Sequel.cariIsi("select DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') from pasien where pasien.no_rkm_medis=? ",NoRM.getText()));
            param.put("pengirim",tbDokter.getValueAt(tbDokter.getSelectedRow(),5).toString());
            param.put("tanggal",Valid.SetTgl3(tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString()));
            param.put("penjab",tbDokter.getValueAt(tbDokter.getSelectedRow(),6).toString());
            param.put("petugas",tbDokter.getValueAt(tbDokter.getSelectedRow(),2).toString());
            param.put("alamat",Alamat.getText());
            param.put("kamar",kamar);
            param.put("namakamar",namakamar);
            param.put("pemeriksaan",pemeriksaan);
            param.put("jam",tbDokter.getValueAt(tbDokter.getSelectedRow(),4).toString());
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());
            param.put("hasil",HasilPeriksa.getText());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",kdpenjab);
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbDokter.getValueAt(tbDokter.getSelectedRow(),6).toString()+"\nID "+(finger.equals("")?kdpenjab:finger)+"\n"+Valid.SetTgl3(tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString()));  
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",kdpetugas);
            param.put("finger2","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbDokter.getValueAt(tbDokter.getSelectedRow(),2).toString()+"\nID "+(finger.equals("")?kdpetugas:finger)+"\n"+Valid.SetTgl3(tbDokter.getValueAt(tbDokter.getSelectedRow(),3).toString()));  

            pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih hasil pemeriksaan..!","Hasil Pemeriksaan",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Model 1","Model 2", "Model 3","PDF Model 1","PDF Model 2","PDF Model 3"},"Model 1");
            switch (pilihan) {
                case "Model 1":
                      Valid.MyReportPDFqryUpload("rptPeriksaRadiologi.jasper","report","::[ Pemeriksaan Radiologi ]::",
                              "select current_date as tanggal",FileName, param);
                      break;
                case "Model 2":
                      Valid.MyReportPDFqryUpload("rptPeriksaRadiologi2.jasper","report","::[ Pemeriksaan Radiologi ]::",
                            "select current_date as tanggal",FileName, param);
                      break;
                case "Model 3":
                      Valid.MyReportPDFqryUpload("rptPeriksaRadiologi3.jasper","report","::[ Pemeriksaan Radiologi ]::",
                            "select current_date as tanggal",FileName, param);
                case "PDF Model 1":
                      Valid.MyReportPDFqryUpload("rptPeriksaRadiologi.jasper","report","::[ Pemeriksaan Radiologi ]::",
                            "select current_date as tanggal",FileName, param);
                      break;
                case "PDF Model 2":
                      Valid.MyReportPDFqryUpload("rptPeriksaRadiologi2.jasper","report","::[ Pemeriksaan Radiologi ]::",
                            "select current_date as tanggal",FileName, param);
                      break;
                case "PDF Model 3":
                      Valid.MyReportPDFqryUpload("rptPeriksaRadiologi3.jasper","report","::[ Pemeriksaan Radiologi ]::",
                            "select current_date as tanggal",FileName, param);
                      break;
            }                        
            
            this.setCursor(Cursor.getDefaultCursor());
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
                kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%BERKAS RADIOLOGI%'");
                String filePath = "pages/upload/" + jpgFile.getName();

                if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='" + filePath + "'") > 0) {
                    uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                        tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim(), kodeberkas, filePath, filePath
                    });
                } else {
                    uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                        tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim(), kodeberkas, filePath
                    });
                }

                if (uploadSuccess) {
                    
                    MetadataBerkas.simpan(
                        koneksi,
                        tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim(),
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
//        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%BERKAS RADIOLOGI%'");
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
    
}
