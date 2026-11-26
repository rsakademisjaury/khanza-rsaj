/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgJnsPerawatan.java
 *
 * Created on May 22, 2010, 11:58:21 PM
 */

package bridging;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import laporan.DlgCariPenyakit;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author dosen
 */
public final class DlgDataTB extends javax.swing.JDialog {
    private final DefaultTableModel tabMode, tabMode2;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private HttpHeaders headers ;
    private HttpEntity requestEntity;
    private RestTemplate rest;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode nameNode;
    private JsonNode response;
    private ApiKemenkesSITT api=new ApiKemenkesSITT();
    private YaskiReferensiPropinsi propinsi=new YaskiReferensiPropinsi(null,false);
    private YaskiReferensiKabupaten kabupaten=new YaskiReferensiKabupaten(null,false);
    private YaskiReferensiKecamatan kecamatan=new YaskiReferensiKecamatan(null,false);
    private YaskiReferensiKelurahan kelurahan=new YaskiReferensiKelurahan(null,false);
    private DlgCariPenyakit penyakit=new DlgCariPenyakit(null,false);
    private String id_tb_03="",kdwasor="",idrs="",URL="",requestJson="",PaduanOATHasil="",tidakada="";
    private FileReader myObj;
    
    /** Creates new form DlgJnsPerawatan
     * @param parent
     * @param modal */
    public DlgDataTB(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        aturTampilanInputTB();
        Date hariIni = new Date();
        DTPCari1.setDate(hariIni);
        DTPCari2.setDate(hariIni);

        this.setLocation(8,1);
        Rectangle ruang = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1320, ruang.width), Math.min(850, ruang.height));

        tabMode=new DefaultTableModel(null,new Object[]{
                "No.Rawat","No.RM","Nama Pasien","J.K.","Tgl.Lahir","Umur","No.JKN","NIK","Alamat","Kd.Kel","Kelurahan","Kd.Kec","Kecamatan","Kd.Kab","Kabupaten",
                "Kd.Prop","Propinsi","Periode Laporan","Tanggal Laporan","Jenis Rujukan","Keterangan Rujukan","Riwayat Pengobatan","Lokasi TB","Tipe Diagnosis",
                "Status HIV","Skoring Anak","Skoring 5","Skoring 6","Mulai Berobat","Paduan OAT","Sumber Obat","Keterangan Sumber Obat","Mikroskopis Sebelum Pengobatan",
                "Tes Cepat Sebelum Pengobatan","Biakan Sebelum Pengobatan","Mikroskopis Bl.2","No.Reg Lab Bl.2","Mikroskopis Bl.3","No.Reg Lab Bl.3","Mikroskopis Bl.5",
                "No.Reg Lab Bl.5","Mikroskopis Akhir","No.Reg Lab Akhir","Akhir Berobat","Hasil Akhir Pengobatan","Dianjurkan Tes HIV","Tanggal Tes HIV","Hasil Tes HIV",
                "PPK","ART","TB DM","Terapi DM","Pindah RO","Status Pengobatan","Foto Toraks","Toraks Tdk Dilakukan","Keterangan","ICD X","Nama Penyakit","ID TB"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbJnsPerawatan.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbJnsPerawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbJnsPerawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 60; i++) {
            TableColumn column = tbJnsPerawatan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(25);
            }else if(i==4){
                column.setPreferredWidth(65);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(90);
            }else if(i==7){
                column.setPreferredWidth(105);
            }else if(i==8){
                column.setPreferredWidth(165);
            }else if(i==9){
                column.setPreferredWidth(45);
            }else if(i==10){
                column.setPreferredWidth(110);
            }else if(i==11){
                column.setPreferredWidth(45);
            }else if(i==12){
                column.setPreferredWidth(110);
            }else if(i==13){
                column.setPreferredWidth(45);
            }else if(i==14){
                column.setPreferredWidth(110);
            }else if(i==15){
                column.setPreferredWidth(45);
            }else if(i==16){
                column.setPreferredWidth(110);
            }else if(i==17){
                column.setPreferredWidth(110);
            }else if(i==18){
                column.setPreferredWidth(120);
            }else if(i==19){
                column.setPreferredWidth(140);
            }else if(i==20){
                column.setPreferredWidth(120);
            }else if(i==21){
                column.setPreferredWidth(170);
            }else if(i==22){
                column.setPreferredWidth(90);
            }else if(i==23){
                column.setPreferredWidth(140);
            }else if(i==24){
                column.setPreferredWidth(100);
            }else if(i==25){
                column.setPreferredWidth(70);
            }else if(i==26){
                column.setPreferredWidth(140);
            }else if(i==27){
                column.setPreferredWidth(140);
            }else if(i==28){
                column.setPreferredWidth(75);
            }else if(i==29){
                column.setPreferredWidth(150);
            }else if(i==30){
                column.setPreferredWidth(90);
            }else if(i==31){
                column.setPreferredWidth(140);
            }else if(i==32){
                column.setPreferredWidth(170);
            }else if(i==33){
                column.setPreferredWidth(165);
            }else if(i==34){
                column.setPreferredWidth(150);
            }else if(i==35){
                column.setPreferredWidth(90);
            }else if(i==36){
                column.setPreferredWidth(85);
            }else if(i==37){
                column.setPreferredWidth(90);
            }else if(i==38){
                column.setPreferredWidth(85);
            }else if(i==39){
                column.setPreferredWidth(90);
            }else if(i==40){
                column.setPreferredWidth(85);
            }else if(i==41){
                column.setPreferredWidth(95);
            }else if(i==42){
                column.setPreferredWidth(90);
            }else if(i==43){
                column.setPreferredWidth(75);
            }else if(i==44){
                column.setPreferredWidth(120);
            }else if(i==45){
                column.setPreferredWidth(100);
            }else if(i==46){
                column.setPreferredWidth(90);
            }else if(i==47){
                column.setPreferredWidth(80);
            }else if(i==48){
                column.setPreferredWidth(50);
            }else if(i==49){
                column.setPreferredWidth(50);
            }else if(i==50){
                column.setPreferredWidth(50);
            }else if(i==51){
                column.setPreferredWidth(70);
            }else if(i==52){
                column.setPreferredWidth(60);
            }else if(i==53){
                column.setPreferredWidth(110);
            }else if(i==54){
                column.setPreferredWidth(90);
            }else if(i==55){
                column.setPreferredWidth(170);
            }else if(i==56){
                column.setPreferredWidth(140);
            }else if(i==57){
                column.setPreferredWidth(50);
            }else if(i==58){
                column.setPreferredWidth(170);
            }else if(i==59){
                column.setPreferredWidth(100);
            }
        }
        tbJnsPerawatan.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabMode2=new DefaultTableModel(null,new Object[]{
                "Nama Faskes",
                "Asal Poli",
                "Tgl. Register",
                "No. Kartu",
                "No. KTP",
                "Nama Pasien",
                "Hasil Lab [TCM-BTA] ",
                "Hasil Radiologi ",
                "Diagnosa Dokter [TBC/Bukan TBC]",
                "SITB [Sudah ada di SITB/Belum ada di SITB]",
                "Keterangan"                
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbJnsPerawatan1.setModel(tabMode2);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbJnsPerawatan1.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbJnsPerawatan1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 10; i++) {
            TableColumn column = tbJnsPerawatan1.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(190);
            }else if(i==1){
                column.setPreferredWidth(150);
            }else if(i==2){
                column.setPreferredWidth(75);
            }else if(i==3){
                column.setPreferredWidth(110);
            }else if(i==4){
                column.setPreferredWidth(100);
            }else if(i==5){
                column.setPreferredWidth(100);
            }else if(i==6){
                column.setPreferredWidth(100);
            }else if(i==7){
                column.setPreferredWidth(140);
            }else if(i==8){
                column.setPreferredWidth(150);
            }else if(i==9){
                column.setPreferredWidth(200);            
            }
        }
        tbJnsPerawatan1.setDefaultRenderer(Object.class, new WarnaTable());
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        KdProp.setDocument(new batasInput((byte)10).getOnlyAngka(KdProp));
        KdKec.setDocument(new batasInput((byte)10).getOnlyAngka(KdKec));
        KdKab.setDocument(new batasInput((byte)10).getOnlyAngka(KdKab));
        KdKel.setDocument(new batasInput((byte)10).getOnlyAngka(KdKel));
        KeteranganRujukan.setDocument(new batasInput((byte)100).getKata(KeteranganRujukan));
       // PaduanOAT.setDocument(new batasInput((int)500).getKata(PaduanOAT));
        KeteranganSO.setDocument(new batasInput((int)500).getKata(KeteranganSO));
        Keterangan.setDocument(new batasInput((int)100).getKata(Keterangan));
        PemeriksaanLaboratAkhirNoReg.setDocument(new batasInput((byte)15).getKata(PemeriksaanLaboratAkhirNoReg));
        PemeriksaanLaboratBulan2NoReg.setDocument(new batasInput((byte)15).getKata(PemeriksaanLaboratBulan2NoReg));
        PemeriksaanLaboratBulan3NoReg.setDocument(new batasInput((byte)15).getKata(PemeriksaanLaboratBulan3NoReg));
        PemeriksaanLaboratBulan5NoReg.setDocument(new batasInput((byte)15).getKata(PemeriksaanLaboratBulan5NoReg));
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        muatDaftarAktif();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        muatDaftarAktif();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        muatDaftarAktif();
                    }
                }
            });
        }  
        
        propinsi.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(propinsi.getTable().getSelectedRow()!= -1){                   
                    KdProp.setText(propinsi.getTable().getValueAt(propinsi.getTable().getSelectedRow(),1).toString());
                    Propinsi.setText(propinsi.getTable().getValueAt(propinsi.getTable().getSelectedRow(),2).toString().toUpperCase());
                    KdProp.requestFocus();
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
        
        propinsi.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    propinsi.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        }); 
        
        kabupaten.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(kabupaten.getTable().getSelectedRow()!= -1){                   
                    KdKab.setText(kabupaten.getTable().getValueAt(kabupaten.getTable().getSelectedRow(),1).toString());
                    Kabupaten.setText(kabupaten.getTable().getValueAt(kabupaten.getTable().getSelectedRow(),2).toString().toUpperCase());
                    KdKab.requestFocus();
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
        
        kabupaten.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    kabupaten.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        }); 
        
        kecamatan.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(kecamatan.getTable().getSelectedRow()!= -1){                   
                    KdKec.setText(kecamatan.getTable().getValueAt(kecamatan.getTable().getSelectedRow(),1).toString());
                    Kecamatan.setText(kecamatan.getTable().getValueAt(kecamatan.getTable().getSelectedRow(),2).toString().toUpperCase());
                    KdKec.requestFocus();
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
        
        kecamatan.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    kecamatan.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        kelurahan.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(kelurahan.getTable().getSelectedRow()!= -1){                   
                    KdKel.setText(kelurahan.getTable().getValueAt(kelurahan.getTable().getSelectedRow(),1).toString());
                    Kelurahan.setText(kelurahan.getTable().getValueAt(kelurahan.getTable().getSelectedRow(),2).toString().toUpperCase());
                    KdKel.requestFocus();
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
        
        kelurahan.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    kelurahan.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        penyakit.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(penyakit.getTable().getSelectedRow()!= -1){                   
                    kdpenyakit.setText(penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),0).toString());
                    nmpenyakit.setText(penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),1).toString());
                }     
                kdpenyakit.requestFocus();
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
            kdwasor = koneksiDB.KABUPATENSITT();
            idrs=koneksiDB.IDSITT();
            URL = koneksiDB.URLAPISITT();
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

        TNoRwCari = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel58 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel59 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        LStatusRawatCari = new widget.Label();
        CmbStatusRawatCari = new widget.ComboBox();
        panelKataKunci = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        Scroll1 = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        jLabel3 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TNmPasien = new widget.TextBox();
        JK = new widget.TextBox();
        jLabel4 = new widget.Label();
        Tanggal = new widget.TextBox();
        jLabel5 = new widget.Label();
        Umur = new widget.TextBox();
        jLabel8 = new widget.Label();
        jLabel9 = new widget.Label();
        NoKartu = new widget.TextBox();
        jLabel10 = new widget.Label();
        NIK = new widget.TextBox();
        jLabel11 = new widget.Label();
        PeriodeLaporan = new widget.ComboBox();
        jLabel12 = new widget.Label();
        TanggalLaporan = new widget.Tanggal();
        jLabel13 = new widget.Label();
        Alamat = new widget.TextBox();
        Kelurahan = new widget.TextBox();
        BtnKelurahan = new widget.Button();
        Kecamatan = new widget.TextBox();
        BtnKecamatan = new widget.Button();
        BtnPropinsi = new widget.Button();
        Propinsi = new widget.TextBox();
        BtnKabupaten = new widget.Button();
        Kabupaten = new widget.TextBox();
        BtnCari1 = new widget.Button();
        KdProp = new widget.TextBox();
        BtnCari2 = new widget.Button();
        KdKel = new widget.TextBox();
        KdKec = new widget.TextBox();
        KdKab = new widget.TextBox();
        BtnCari3 = new widget.Button();
        BtnCari4 = new widget.Button();
        Rujukan = new widget.ComboBox();
        jLabel14 = new widget.Label();
        jLabel15 = new widget.Label();
        KeteranganRujukan = new widget.TextBox();
        jLabel16 = new widget.Label();
        TipeDiagnosis = new widget.ComboBox();
        jLabel17 = new widget.Label();
        Lokasi = new widget.ComboBox();
        jLabel18 = new widget.Label();
        Riwayat = new widget.ComboBox();
        jLabel19 = new widget.Label();
        StatusHIV = new widget.ComboBox();
        jLabel20 = new widget.Label();
        SkoringAnak = new widget.ComboBox();
        jLabel21 = new widget.Label();
        Skoring5 = new widget.ComboBox();
        jLabel22 = new widget.Label();
        Skoring6 = new widget.ComboBox();
        jLabel23 = new widget.Label();
        MulaiBerobat = new widget.Tanggal();
        jLabel24 = new widget.Label();
        jLabel25 = new widget.Label();
        SumberObat = new widget.ComboBox();
        KeteranganSO = new widget.TextBox();
        jLabel26 = new widget.Label();
        jLabel27 = new widget.Label();
        SebelumPengobatanMikroskopis = new widget.ComboBox();
        jLabel28 = new widget.Label();
        SebelumPengobatanTesCepat = new widget.ComboBox();
        jLabel29 = new widget.Label();
        jLabel30 = new widget.Label();
        SebelumPengobatanBiakan = new widget.ComboBox();
        jLabel34 = new widget.Label();
        jLabel35 = new widget.Label();
        PemeriksaanLaboratBulan2Mikroskopis = new widget.ComboBox();
        jLabel36 = new widget.Label();
        PemeriksaanLaboratBulan2NoReg = new widget.TextBox();
        PemeriksaanLaboratBulan3NoReg = new widget.TextBox();
        jLabel37 = new widget.Label();
        PemeriksaanLaboratBulan3Mikroskopis = new widget.ComboBox();
        jLabel38 = new widget.Label();
        jLabel39 = new widget.Label();
        jLabel40 = new widget.Label();
        jLabel41 = new widget.Label();
        PemeriksaanLaboratBulan5Mikroskopis = new widget.ComboBox();
        jLabel42 = new widget.Label();
        PemeriksaanLaboratBulan5NoReg = new widget.TextBox();
        PemeriksaanLaboratAkhirNoReg = new widget.TextBox();
        jLabel43 = new widget.Label();
        PemeriksaanLaboratAkhirPengobatanMikroskopis = new widget.ComboBox();
        jLabel44 = new widget.Label();
        jLabel45 = new widget.Label();
        jLabel31 = new widget.Label();
        AkhirBerobat = new widget.Tanggal();
        jLabel46 = new widget.Label();
        HasilAkhirPengobatan = new widget.ComboBox();
        jLabel32 = new widget.Label();
        DianjurkanTesHIV = new widget.Tanggal();
        TanggalTesHIV = new widget.Tanggal();
        jLabel33 = new widget.Label();
        jLabel47 = new widget.Label();
        HasilTesHIV = new widget.ComboBox();
        PPK = new widget.ComboBox();
        jLabel48 = new widget.Label();
        jLabel49 = new widget.Label();
        ART = new widget.ComboBox();
        jLabel50 = new widget.Label();
        TBDM = new widget.ComboBox();
        jLabel51 = new widget.Label();
        TerapiDM = new widget.ComboBox();
        PindahRO = new widget.ComboBox();
        jLabel52 = new widget.Label();
        Status = new widget.ComboBox();
        jLabel53 = new widget.Label();
        jLabel54 = new widget.Label();
        FotoToraks = new widget.ComboBox();
        jLabel55 = new widget.Label();
        ToraksTidakDilakukan = new widget.ComboBox();
        jLabel56 = new widget.Label();
        Keterangan = new widget.TextBox();
        jLabel57 = new widget.Label();
        kdpenyakit = new widget.TextBox();
        nmpenyakit = new widget.TextBox();
        btnBangsal = new widget.Button();
        PaduanOAT = new widget.ComboBox();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbJnsPerawatan = new widget.Table();
        internalFrame4 = new widget.InternalFrame();
        Scroll2 = new widget.ScrollPane();
        tbJnsPerawatan1 = new widget.Table();

        TNoRwCari.setHighlighter(null);
        TNoRwCari.setName("TNoRwCari"); // NOI18N
        TNoRwCari.setPreferredSize(new java.awt.Dimension(120, 23));
        TNoRwCari.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TNoRwCariMouseClicked(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Pasien Teridentifikasi TB ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 134));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(80, 30));
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
        BtnBatal.setPreferredSize(new java.awt.Dimension(80, 30));
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
        BtnHapus.setPreferredSize(new java.awt.Dimension(80, 30));
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
        BtnEdit.setPreferredSize(new java.awt.Dimension(80, 30));
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
        BtnPrint.setToolTipText("Cetak / ekspor Excel (Alt+T)");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(80, 30));
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
        BtnAll.setPreferredSize(new java.awt.Dimension(80, 30));
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
        BtnKeluar.setPreferredSize(new java.awt.Dimension(80, 30));
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

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel58.setText("Periode :");
        jLabel58.setToolTipText("Tanggal laporan di Daftar TB, tanggal register/masuk di Format Dinas");
        jLabel58.setName("jLabel58"); // NOI18N
        jLabel58.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass9.add(jLabel58);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "17-09-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel59.setText("s.d");
        jLabel59.setName("jLabel59"); // NOI18N
        jLabel59.setPreferredSize(new java.awt.Dimension(24, 23));
        panelGlass9.add(jLabel59);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "17-09-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        LStatusRawatCari.setText("Status Rawat :");
        LStatusRawatCari.setName("LStatusRawatCari"); // NOI18N
        LStatusRawatCari.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass9.add(LStatusRawatCari);

        CmbStatusRawatCari.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Rawat Inap", "Rawat Jalan" }));
        CmbStatusRawatCari.setName("CmbStatusRawatCari"); // NOI18N
        CmbStatusRawatCari.setPreferredSize(new java.awt.Dimension(120, 23));
        panelGlass9.add(CmbStatusRawatCari);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        panelKataKunci.setName("panelKataKunci"); // NOI18N
        panelKataKunci.setPreferredSize(new java.awt.Dimension(44, 44));
        panelKataKunci.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelKataKunci.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(210, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelKataKunci.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Terapkan filter (Alt+2)");
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
        panelKataKunci.add(BtnCari);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(75, 23));
        panelKataKunci.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(80, 23));
        panelKataKunci.add(LCount);

        jPanel3.add(panelKataKunci, java.awt.BorderLayout.CENTER);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 613));
        FormInput.setLayout(null);

        jLabel3.setText("Pasien :");
        jLabel3.setName("jLabel3"); // NOI18N
        FormInput.add(jLabel3);
        jLabel3.setBounds(0, 10, 61, 23);

        TNoRw.setEditable(false);
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(64, 10, 140, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(206, 10, 100, 23);

        TNmPasien.setEditable(false);
        TNmPasien.setHighlighter(null);
        TNmPasien.setName("TNmPasien"); // NOI18N
        FormInput.add(TNmPasien);
        TNmPasien.setBounds(308, 10, 300, 23);

        JK.setEditable(false);
        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N
        FormInput.add(JK);
        JK.setBounds(610, 10, 95, 23);

        jLabel4.setText("Tgl.Lahir :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 40, 61, 23);

        Tanggal.setEditable(false);
        Tanggal.setHighlighter(null);
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(64, 40, 100, 23);

        jLabel5.setText("Umur :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(179, 40, 50, 23);

        Umur.setEditable(false);
        Umur.setHighlighter(null);
        Umur.setName("Umur"); // NOI18N
        Umur.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                UmurKeyPressed(evt);
            }
        });
        FormInput.add(Umur);
        Umur.setBounds(232, 40, 65, 23);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("Th");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(299, 40, 30, 23);

        jLabel9.setText("No.JKN :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(330, 40, 55, 23);

        NoKartu.setEditable(false);
        NoKartu.setHighlighter(null);
        NoKartu.setName("NoKartu"); // NOI18N
        NoKartu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoKartuKeyPressed(evt);
            }
        });
        FormInput.add(NoKartu);
        NoKartu.setBounds(388, 40, 120, 23);

        jLabel10.setText("NIK :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(517, 40, 40, 23);

        NIK.setEditable(false);
        NIK.setHighlighter(null);
        NIK.setName("NIK"); // NOI18N
        NIK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NIKKeyPressed(evt);
            }
        });
        FormInput.add(NIK);
        NIK.setBounds(560, 40, 145, 23);

        jLabel11.setText("Periode Laporan :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(0, 160, 100, 23);

        PeriodeLaporan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1=Januari - Maret", "2=April - Juni", "3=Juli - September", "4=Oktober - Desember" }));
        PeriodeLaporan.setName("PeriodeLaporan"); // NOI18N
        PeriodeLaporan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PeriodeLaporanKeyPressed(evt);
            }
        });
        FormInput.add(PeriodeLaporan);
        PeriodeLaporan.setBounds(103, 160, 163, 23);

        jLabel12.setText("Tanggal :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(270, 160, 54, 23);

        TanggalLaporan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12-09-2025 12:41:15" }));
        TanggalLaporan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TanggalLaporan.setName("TanggalLaporan"); // NOI18N
        TanggalLaporan.setOpaque(false);
        TanggalLaporan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalLaporanKeyPressed(evt);
            }
        });
        FormInput.add(TanggalLaporan);
        TanggalLaporan.setBounds(327, 160, 130, 23);

        jLabel13.setText("Alamat :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(0, 70, 61, 23);

        Alamat.setEditable(false);
        Alamat.setText("ALAMAT");
        Alamat.setHighlighter(null);
        Alamat.setName("Alamat"); // NOI18N
        Alamat.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                AlamatMouseMoved(evt);
            }
        });
        Alamat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                AlamatMouseExited(evt);
            }
        });
        Alamat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlamatKeyPressed(evt);
            }
        });
        FormInput.add(Alamat);
        Alamat.setBounds(64, 70, 641, 23);

        Kelurahan.setEditable(false);
        Kelurahan.setText("KELURAHAN");
        Kelurahan.setHighlighter(null);
        Kelurahan.setName("Kelurahan"); // NOI18N
        Kelurahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KelurahanKeyPressed(evt);
            }
        });
        FormInput.add(Kelurahan);
        Kelurahan.setBounds(126, 100, 180, 23);

        BtnKelurahan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnKelurahan.setMnemonic('2');
        BtnKelurahan.setToolTipText("ALt+2");
        BtnKelurahan.setName("BtnKelurahan"); // NOI18N
        BtnKelurahan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKelurahanActionPerformed(evt);
            }
        });
        FormInput.add(BtnKelurahan);
        BtnKelurahan.setBounds(340, 100, 28, 23);

        Kecamatan.setEditable(false);
        Kecamatan.setText("KECAMATAN");
        Kecamatan.setHighlighter(null);
        Kecamatan.setName("Kecamatan"); // NOI18N
        Kecamatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KecamatanKeyPressed(evt);
            }
        });
        FormInput.add(Kecamatan);
        Kecamatan.setBounds(463, 100, 180, 23);

        BtnKecamatan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnKecamatan.setMnemonic('3');
        BtnKecamatan.setToolTipText("ALt+3");
        BtnKecamatan.setName("BtnKecamatan"); // NOI18N
        BtnKecamatan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKecamatanActionPerformed(evt);
            }
        });
        FormInput.add(BtnKecamatan);
        BtnKecamatan.setBounds(677, 100, 28, 23);

        BtnPropinsi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPropinsi.setMnemonic('4');
        BtnPropinsi.setToolTipText("ALt+4");
        BtnPropinsi.setName("BtnPropinsi"); // NOI18N
        BtnPropinsi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPropinsiActionPerformed(evt);
            }
        });
        FormInput.add(BtnPropinsi);
        BtnPropinsi.setBounds(677, 130, 28, 23);

        Propinsi.setEditable(false);
        Propinsi.setText("PROPINSI");
        Propinsi.setHighlighter(null);
        Propinsi.setName("Propinsi"); // NOI18N
        Propinsi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PropinsiKeyPressed(evt);
            }
        });
        FormInput.add(Propinsi);
        Propinsi.setBounds(463, 130, 180, 23);

        BtnKabupaten.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnKabupaten.setMnemonic('4');
        BtnKabupaten.setToolTipText("ALt+4");
        BtnKabupaten.setName("BtnKabupaten"); // NOI18N
        BtnKabupaten.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKabupatenActionPerformed(evt);
            }
        });
        FormInput.add(BtnKabupaten);
        BtnKabupaten.setBounds(340, 130, 28, 23);

        Kabupaten.setEditable(false);
        Kabupaten.setText("KABUPATEN");
        Kabupaten.setHighlighter(null);
        Kabupaten.setName("Kabupaten"); // NOI18N
        Kabupaten.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KabupatenKeyPressed(evt);
            }
        });
        FormInput.add(Kabupaten);
        Kabupaten.setBounds(126, 130, 180, 23);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari1.setMnemonic('2');
        BtnCari1.setToolTipText("Alt+2");
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
        FormInput.add(BtnCari1);
        BtnCari1.setBounds(646, 130, 28, 23);

        KdProp.setEditable(false);
        KdProp.setHighlighter(null);
        KdProp.setName("KdProp"); // NOI18N
        KdProp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPropKeyPressed(evt);
            }
        });
        FormInput.add(KdProp);
        KdProp.setBounds(401, 130, 60, 23);

        BtnCari2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari2.setMnemonic('2');
        BtnCari2.setToolTipText("Alt+2");
        BtnCari2.setName("BtnCari2"); // NOI18N
        BtnCari2.setPreferredSize(new java.awt.Dimension(28, 23));
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
        FormInput.add(BtnCari2);
        BtnCari2.setBounds(309, 100, 28, 23);

        KdKel.setEditable(false);
        KdKel.setHighlighter(null);
        KdKel.setName("KdKel"); // NOI18N
        KdKel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdKelKeyPressed(evt);
            }
        });
        FormInput.add(KdKel);
        KdKel.setBounds(64, 100, 60, 23);

        KdKec.setEditable(false);
        KdKec.setHighlighter(null);
        KdKec.setName("KdKec"); // NOI18N
        KdKec.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdKecKeyPressed(evt);
            }
        });
        FormInput.add(KdKec);
        KdKec.setBounds(401, 100, 60, 23);

        KdKab.setEditable(false);
        KdKab.setHighlighter(null);
        KdKab.setName("KdKab"); // NOI18N
        KdKab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdKabKeyPressed(evt);
            }
        });
        FormInput.add(KdKab);
        KdKab.setBounds(64, 130, 60, 23);

        BtnCari3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari3.setMnemonic('2');
        BtnCari3.setToolTipText("Alt+2");
        BtnCari3.setName("BtnCari3"); // NOI18N
        BtnCari3.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCari3ActionPerformed(evt);
            }
        });
        BtnCari3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCari3KeyPressed(evt);
            }
        });
        FormInput.add(BtnCari3);
        BtnCari3.setBounds(646, 100, 28, 23);

        BtnCari4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari4.setMnemonic('2');
        BtnCari4.setToolTipText("Alt+2");
        BtnCari4.setName("BtnCari4"); // NOI18N
        BtnCari4.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCari4ActionPerformed(evt);
            }
        });
        BtnCari4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCari4KeyPressed(evt);
            }
        });
        FormInput.add(BtnCari4);
        BtnCari4.setBounds(309, 130, 28, 23);

        Rujukan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Inisiatif pasien/Keluarga", "Anggota Masyarakat/Kader", "Faskes", "Dokter Praktek Mandiri", "Poli lain", "Lain-lain" }));
        Rujukan.setName("Rujukan"); // NOI18N
        Rujukan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RujukanKeyPressed(evt);
            }
        });
        FormInput.add(Rujukan);
        Rujukan.setBounds(520, 160, 185, 23);

        jLabel14.setText("Rujukan :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(464, 160, 53, 23);

        jLabel15.setText("Ket.Rujukan :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(0, 190, 100, 23);

        KeteranganRujukan.setHighlighter(null);
        KeteranganRujukan.setName("KeteranganRujukan"); // NOI18N
        KeteranganRujukan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganRujukanKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganRujukan);
        KeteranganRujukan.setBounds(103, 190, 140, 23);

        jLabel16.setText("Tipe Diagnosis :");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 220, 100, 23);

        TipeDiagnosis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Terkonfirmasi bakteriologis", "Terdiagnosis klinis" }));
        TipeDiagnosis.setName("TipeDiagnosis"); // NOI18N
        TipeDiagnosis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TipeDiagnosisKeyPressed(evt);
            }
        });
        FormInput.add(TipeDiagnosis);
        TipeDiagnosis.setBounds(103, 220, 187, 23);

        jLabel17.setText("Lokasi :");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(557, 190, 40, 23);

        Lokasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Paru", "Ekstraparu" }));
        Lokasi.setName("Lokasi"); // NOI18N
        Lokasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LokasiKeyPressed(evt);
            }
        });
        FormInput.add(Lokasi);
        Lokasi.setBounds(600, 190, 105, 23);

        jLabel18.setText("Riwayat :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(248, 190, 50, 23);

        Riwayat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Baru", "Kambuh", "Diobati setelah gagal", "Diobati Setelah Putus Berobat", "Lain-lain", "Riwayat Pengobatan Sebelumnya Tidak Diketahui", "Pindahan" }));
        Riwayat.setName("Riwayat"); // NOI18N
        Riwayat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RiwayatKeyPressed(evt);
            }
        });
        FormInput.add(Riwayat);
        Riwayat.setBounds(301, 190, 250, 23);

        jLabel19.setText("Status HIV :");
        jLabel19.setName("jLabel19"); // NOI18N
        FormInput.add(jLabel19);
        jLabel19.setBounds(293, 220, 68, 23);

        StatusHIV.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Positif", "Negatif", "Tidak diketahui" }));
        StatusHIV.setName("StatusHIV"); // NOI18N
        StatusHIV.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusHIVKeyPressed(evt);
            }
        });
        FormInput.add(StatusHIV);
        StatusHIV.setBounds(363, 220, 127, 23);

        jLabel20.setText("Skoring Anak :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(488, 220, 84, 23);

        SkoringAnak.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "Tidak dilakukan" }));
        SkoringAnak.setName("SkoringAnak"); // NOI18N
        SkoringAnak.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkoringAnakKeyPressed(evt);
            }
        });
        FormInput.add(SkoringAnak);
        SkoringAnak.setBounds(575, 220, 130, 23);

        jLabel21.setText("Skoring 5 :");
        jLabel21.setName("jLabel21"); // NOI18N
        FormInput.add(jLabel21);
        jLabel21.setBounds(0, 250, 100, 23);

        Skoring5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Uji Tuberkulin Positif", "Ada Kontak TB Paru", "Uji Tuberkulin Negatif", "Tidak Ada Kontak TB Paru" }));
        Skoring5.setName("Skoring5"); // NOI18N
        Skoring5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Skoring5KeyPressed(evt);
            }
        });
        FormInput.add(Skoring5);
        Skoring5.setBounds(103, 250, 175, 23);

        jLabel22.setText("Skoring 6 :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(282, 250, 60, 23);

        Skoring6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ada Kontak TB Paru", "Tidak Ada", "Tidak Jelas Kontak TB Paru" }));
        Skoring6.setName("Skoring6"); // NOI18N
        Skoring6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Skoring6KeyPressed(evt);
            }
        });
        FormInput.add(Skoring6);
        Skoring6.setBounds(345, 250, 180, 23);

        jLabel23.setText("Mulai Berobat :");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(532, 250, 80, 23);

        MulaiBerobat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12-09-2025" }));
        MulaiBerobat.setDisplayFormat("dd-MM-yyyy");
        MulaiBerobat.setName("MulaiBerobat"); // NOI18N
        MulaiBerobat.setOpaque(false);
        MulaiBerobat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MulaiBerobatKeyPressed(evt);
            }
        });
        FormInput.add(MulaiBerobat);
        MulaiBerobat.setBounds(615, 250, 90, 23);

        jLabel24.setText("Paduan OAT :");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(0, 280, 100, 23);

        jLabel25.setText("Sumber Obat :");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(282, 280, 80, 23);

        SumberObat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Program TB", "Bayar Sendiri", "Asuransi", "Lain-lain" }));
        SumberObat.setName("SumberObat"); // NOI18N
        SumberObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SumberObatKeyPressed(evt);
            }
        });
        FormInput.add(SumberObat);
        SumberObat.setBounds(365, 280, 115, 23);

        KeteranganSO.setHighlighter(null);
        KeteranganSO.setName("KeteranganSO"); // NOI18N
        KeteranganSO.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganSOKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganSO);
        KeteranganSO.setBounds(585, 280, 120, 23);

        jLabel26.setText("Keterangan S.O. :");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(482, 280, 100, 23);

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("Sebelum Pengobatan :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(11, 310, 120, 23);

        SebelumPengobatanMikroskopis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Negatif", "1-19", "1+", "2+", "3+", "Tidak dilakukan" }));
        SebelumPengobatanMikroskopis.setName("SebelumPengobatanMikroskopis"); // NOI18N
        SebelumPengobatanMikroskopis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SebelumPengobatanMikroskopisKeyPressed(evt);
            }
        });
        FormInput.add(SebelumPengobatanMikroskopis);
        SebelumPengobatanMikroskopis.setBounds(103, 330, 150, 23);

        jLabel28.setText("Tes Cepat :");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(274, 330, 70, 23);

        SebelumPengobatanTesCepat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rif sensitif", "Rif resisten", "Negatif", "Rif Indeterminated", "Invalid", "Error", "No Result", "Tidak dilakukan" }));
        SebelumPengobatanTesCepat.setName("SebelumPengobatanTesCepat"); // NOI18N
        SebelumPengobatanTesCepat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SebelumPengobatanTesCepatKeyPressed(evt);
            }
        });
        FormInput.add(SebelumPengobatanTesCepat);
        SebelumPengobatanTesCepat.setBounds(347, 330, 160, 23);

        jLabel29.setText("Mikroskopis :");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(0, 330, 100, 23);

        jLabel30.setText("Biakan :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(527, 330, 50, 23);

        SebelumPengobatanBiakan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Negatif", "1-19 BTA", "1+", "2+", "3+", "4+", "NTM", "Kontaminasi", "Tidak dilakukan" }));
        SebelumPengobatanBiakan.setName("SebelumPengobatanBiakan"); // NOI18N
        SebelumPengobatanBiakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SebelumPengobatanBiakanKeyPressed(evt);
            }
        });
        FormInput.add(SebelumPengobatanBiakan);
        SebelumPengobatanBiakan.setBounds(580, 330, 125, 23);

        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel34.setText("Pemeriksaan Laborat Bulan ke 2 :");
        jLabel34.setName("jLabel34"); // NOI18N
        FormInput.add(jLabel34);
        jLabel34.setBounds(11, 360, 174, 23);

        jLabel35.setText("Mikroskopis :");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(0, 380, 100, 23);

        PemeriksaanLaboratBulan2Mikroskopis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Negatif", "1-19", "1+", "2+", "3+", "Tidak dilakukan" }));
        PemeriksaanLaboratBulan2Mikroskopis.setName("PemeriksaanLaboratBulan2Mikroskopis"); // NOI18N
        PemeriksaanLaboratBulan2Mikroskopis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanLaboratBulan2MikroskopisKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanLaboratBulan2Mikroskopis);
        PemeriksaanLaboratBulan2Mikroskopis.setBounds(103, 380, 125, 23);

        jLabel36.setText("No.Reg :");
        jLabel36.setName("jLabel36"); // NOI18N
        FormInput.add(jLabel36);
        jLabel36.setBounds(230, 380, 50, 23);

        PemeriksaanLaboratBulan2NoReg.setHighlighter(null);
        PemeriksaanLaboratBulan2NoReg.setName("PemeriksaanLaboratBulan2NoReg"); // NOI18N
        PemeriksaanLaboratBulan2NoReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanLaboratBulan2NoRegKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanLaboratBulan2NoReg);
        PemeriksaanLaboratBulan2NoReg.setBounds(283, 380, 65, 23);

        PemeriksaanLaboratBulan3NoReg.setHighlighter(null);
        PemeriksaanLaboratBulan3NoReg.setName("PemeriksaanLaboratBulan3NoReg"); // NOI18N
        PemeriksaanLaboratBulan3NoReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanLaboratBulan3NoRegKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanLaboratBulan3NoReg);
        PemeriksaanLaboratBulan3NoReg.setBounds(640, 380, 65, 23);

        jLabel37.setText("No.Reg :");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(587, 380, 50, 23);

        PemeriksaanLaboratBulan3Mikroskopis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Negatif", "1-19", "1+", "2+", "3+", "Tidak dilakukan" }));
        PemeriksaanLaboratBulan3Mikroskopis.setName("PemeriksaanLaboratBulan3Mikroskopis"); // NOI18N
        PemeriksaanLaboratBulan3Mikroskopis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanLaboratBulan3MikroskopisKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanLaboratBulan3Mikroskopis);
        PemeriksaanLaboratBulan3Mikroskopis.setBounds(460, 380, 125, 23);

        jLabel38.setText("Mikroskopis :");
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(357, 380, 100, 23);

        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel39.setText("Pemeriksaan Laborat Bulan ke 3 :");
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(370, 360, 174, 23);

        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel40.setText("Pemeriksaan Laborat Bulan ke 5 :");
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(11, 410, 174, 23);

        jLabel41.setText("Mikroskopis :");
        jLabel41.setName("jLabel41"); // NOI18N
        FormInput.add(jLabel41);
        jLabel41.setBounds(0, 430, 100, 23);

        PemeriksaanLaboratBulan5Mikroskopis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Negatif", "1-19", "1+", "2+", "3+", "Tidak dilakukan" }));
        PemeriksaanLaboratBulan5Mikroskopis.setName("PemeriksaanLaboratBulan5Mikroskopis"); // NOI18N
        PemeriksaanLaboratBulan5Mikroskopis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanLaboratBulan5MikroskopisKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanLaboratBulan5Mikroskopis);
        PemeriksaanLaboratBulan5Mikroskopis.setBounds(103, 430, 125, 23);

        jLabel42.setText("No.Reg :");
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(230, 430, 50, 23);

        PemeriksaanLaboratBulan5NoReg.setHighlighter(null);
        PemeriksaanLaboratBulan5NoReg.setName("PemeriksaanLaboratBulan5NoReg"); // NOI18N
        PemeriksaanLaboratBulan5NoReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanLaboratBulan5NoRegKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanLaboratBulan5NoReg);
        PemeriksaanLaboratBulan5NoReg.setBounds(283, 430, 65, 23);

        PemeriksaanLaboratAkhirNoReg.setHighlighter(null);
        PemeriksaanLaboratAkhirNoReg.setName("PemeriksaanLaboratAkhirNoReg"); // NOI18N
        PemeriksaanLaboratAkhirNoReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanLaboratAkhirNoRegKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanLaboratAkhirNoReg);
        PemeriksaanLaboratAkhirNoReg.setBounds(640, 430, 65, 23);

        jLabel43.setText("No.Reg :");
        jLabel43.setName("jLabel43"); // NOI18N
        FormInput.add(jLabel43);
        jLabel43.setBounds(587, 430, 50, 23);

        PemeriksaanLaboratAkhirPengobatanMikroskopis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Negatif", "1-19", "1+", "2+", "3+", "Tidak dilakukan" }));
        PemeriksaanLaboratAkhirPengobatanMikroskopis.setName("PemeriksaanLaboratAkhirPengobatanMikroskopis"); // NOI18N
        PemeriksaanLaboratAkhirPengobatanMikroskopis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanLaboratAkhirPengobatanMikroskopisKeyPressed(evt);
            }
        });
        FormInput.add(PemeriksaanLaboratAkhirPengobatanMikroskopis);
        PemeriksaanLaboratAkhirPengobatanMikroskopis.setBounds(460, 430, 125, 23);

        jLabel44.setText("Mikroskopis :");
        jLabel44.setName("jLabel44"); // NOI18N
        FormInput.add(jLabel44);
        jLabel44.setBounds(357, 430, 100, 23);

        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel45.setText("Pemeriksaan Laborat Akhir Pengobatan :");
        jLabel45.setName("jLabel45"); // NOI18N
        FormInput.add(jLabel45);
        jLabel45.setBounds(370, 410, 245, 23);

        jLabel31.setText("Akhir Berobat :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(0, 460, 100, 23);

        AkhirBerobat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12-09-2025" }));
        AkhirBerobat.setDisplayFormat("dd-MM-yyyy");
        AkhirBerobat.setName("AkhirBerobat"); // NOI18N
        AkhirBerobat.setOpaque(false);
        AkhirBerobat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AkhirBerobatKeyPressed(evt);
            }
        });
        FormInput.add(AkhirBerobat);
        AkhirBerobat.setBounds(103, 460, 90, 23);

        jLabel46.setText("Hasil Akhir Pengobatan :");
        jLabel46.setName("jLabel46"); // NOI18N
        FormInput.add(jLabel46);
        jLabel46.setBounds(190, 460, 140, 23);

        HasilAkhirPengobatan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Belum", "Sembuh", "Pengobatan Lengkap", "Lost To Follow Up", "Meninggal", "Gagal", "Pindah" }));
        HasilAkhirPengobatan.setName("HasilAkhirPengobatan"); // NOI18N
        HasilAkhirPengobatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HasilAkhirPengobatanKeyPressed(evt);
            }
        });
        FormInput.add(HasilAkhirPengobatan);
        HasilAkhirPengobatan.setBounds(333, 460, 160, 23);

        jLabel32.setText("Dianjurkan Tes HIV :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(512, 460, 100, 23);

        DianjurkanTesHIV.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12-09-2025" }));
        DianjurkanTesHIV.setDisplayFormat("dd-MM-yyyy");
        DianjurkanTesHIV.setName("DianjurkanTesHIV"); // NOI18N
        DianjurkanTesHIV.setOpaque(false);
        DianjurkanTesHIV.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DianjurkanTesHIVKeyPressed(evt);
            }
        });
        FormInput.add(DianjurkanTesHIV);
        DianjurkanTesHIV.setBounds(615, 460, 90, 23);

        TanggalTesHIV.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12-09-2025" }));
        TanggalTesHIV.setDisplayFormat("dd-MM-yyyy");
        TanggalTesHIV.setName("TanggalTesHIV"); // NOI18N
        TanggalTesHIV.setOpaque(false);
        TanggalTesHIV.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalTesHIVKeyPressed(evt);
            }
        });
        FormInput.add(TanggalTesHIV);
        TanggalTesHIV.setBounds(103, 490, 90, 23);

        jLabel33.setText("Tanggal Tes HIV :");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(0, 490, 100, 23);

        jLabel47.setText("Hasil Tes HIV :");
        jLabel47.setName("jLabel47"); // NOI18N
        FormInput.add(jLabel47);
        jLabel47.setBounds(192, 490, 90, 23);

        HasilTesHIV.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Reaktif", "Non Reaktif", "Indeterminated" }));
        HasilTesHIV.setName("HasilTesHIV"); // NOI18N
        HasilTesHIV.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HasilTesHIVKeyPressed(evt);
            }
        });
        FormInput.add(HasilTesHIV);
        HasilTesHIV.setBounds(285, 490, 140, 23);

        PPK.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ya", "Tidak" }));
        PPK.setName("PPK"); // NOI18N
        PPK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PPKKeyPressed(evt);
            }
        });
        FormInput.add(PPK);
        PPK.setBounds(470, 490, 95, 23);

        jLabel48.setText("PPK :");
        jLabel48.setName("jLabel48"); // NOI18N
        FormInput.add(jLabel48);
        jLabel48.setBounds(427, 490, 40, 23);

        jLabel49.setText("ART :");
        jLabel49.setName("jLabel49"); // NOI18N
        FormInput.add(jLabel49);
        jLabel49.setBounds(561, 490, 45, 23);

        ART.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ya", "Tidak" }));
        ART.setName("ART"); // NOI18N
        ART.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ARTKeyPressed(evt);
            }
        });
        FormInput.add(ART);
        ART.setBounds(609, 490, 95, 23);

        jLabel50.setText("TB DM :");
        jLabel50.setName("jLabel50"); // NOI18N
        FormInput.add(jLabel50);
        jLabel50.setBounds(0, 520, 100, 23);

        TBDM.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ya", "Tidak" }));
        TBDM.setName("TBDM"); // NOI18N
        TBDM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TBDMKeyPressed(evt);
            }
        });
        FormInput.add(TBDM);
        TBDM.setBounds(103, 520, 77, 23);

        jLabel51.setText("Terapi DM :");
        jLabel51.setName("jLabel51"); // NOI18N
        FormInput.add(jLabel51);
        jLabel51.setBounds(183, 520, 63, 23);

        TerapiDM.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OHO", "Inj. Insulin" }));
        TerapiDM.setName("TerapiDM"); // NOI18N
        TerapiDM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TerapiDMKeyPressed(evt);
            }
        });
        FormInput.add(TerapiDM);
        TerapiDM.setBounds(249, 520, 102, 23);

        PindahRO.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ya", "Tidak" }));
        PindahRO.setName("PindahRO"); // NOI18N
        PindahRO.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PindahROKeyPressed(evt);
            }
        });
        FormInput.add(PindahRO);
        PindahRO.setBounds(421, 520, 77, 23);

        jLabel52.setText("Pindah RO :");
        jLabel52.setName("jLabel52"); // NOI18N
        FormInput.add(jLabel52);
        jLabel52.setBounds(353, 520, 65, 23);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sesuai Standar", "Tidak Sesuai Standar" }));
        Status.setName("Status"); // NOI18N
        Status.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusKeyPressed(evt);
            }
        });
        FormInput.add(Status);
        Status.setBounds(549, 520, 155, 23);

        jLabel53.setText("Status :");
        jLabel53.setName("jLabel53"); // NOI18N
        FormInput.add(jLabel53);
        jLabel53.setBounds(499, 520, 47, 23);

        jLabel54.setText("Foto Toraks :");
        jLabel54.setName("jLabel54"); // NOI18N
        FormInput.add(jLabel54);
        jLabel54.setBounds(0, 550, 100, 23);

        FotoToraks.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Positif", "Negatif", "Tidak Dilakukan" }));
        FotoToraks.setName("FotoToraks"); // NOI18N
        FotoToraks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FotoToraksKeyPressed(evt);
            }
        });
        FormInput.add(FotoToraks);
        FotoToraks.setBounds(103, 550, 127, 23);

        jLabel55.setText("Toraks Tdk Dilakukan :");
        jLabel55.setName("jLabel55"); // NOI18N
        FormInput.add(jLabel55);
        jLabel55.setBounds(228, 550, 120, 23);

        ToraksTidakDilakukan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak dilakukan", "Setelah terapi antibioka non OAT: tidak ada perbaikan Klinis, ada faktor resiko TB, dan atas pertimbangan dokter", "Setelah terapi antibioka non OAT: ada Perbaikan Klinis" }));
        ToraksTidakDilakukan.setName("ToraksTidakDilakukan"); // NOI18N
        ToraksTidakDilakukan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ToraksTidakDilakukanKeyPressed(evt);
            }
        });
        FormInput.add(ToraksTidakDilakukan);
        ToraksTidakDilakukan.setBounds(351, 550, 353, 23);

        jLabel56.setText("Keterangan :");
        jLabel56.setName("jLabel56"); // NOI18N
        FormInput.add(jLabel56);
        jLabel56.setBounds(0, 580, 100, 23);

        Keterangan.setHighlighter(null);
        Keterangan.setName("Keterangan"); // NOI18N
        Keterangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKeyPressed(evt);
            }
        });
        FormInput.add(Keterangan);
        Keterangan.setBounds(103, 580, 200, 23);

        jLabel57.setText("ICD X :");
        jLabel57.setName("jLabel57"); // NOI18N
        FormInput.add(jLabel57);
        jLabel57.setBounds(300, 580, 48, 23);

        kdpenyakit.setEditable(false);
        kdpenyakit.setHighlighter(null);
        kdpenyakit.setName("kdpenyakit"); // NOI18N
        kdpenyakit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpenyakitKeyPressed(evt);
            }
        });
        FormInput.add(kdpenyakit);
        kdpenyakit.setBounds(351, 580, 67, 23);

        nmpenyakit.setEditable(false);
        nmpenyakit.setName("nmpenyakit"); // NOI18N
        nmpenyakit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nmpenyakitKeyPressed(evt);
            }
        });
        FormInput.add(nmpenyakit);
        nmpenyakit.setBounds(420, 580, 253, 23);

        btnBangsal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnBangsal.setMnemonic('1');
        btnBangsal.setToolTipText("Alt+1");
        btnBangsal.setName("btnBangsal"); // NOI18N
        btnBangsal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBangsalActionPerformed(evt);
            }
        });
        btnBangsal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnBangsalKeyPressed(evt);
            }
        });
        FormInput.add(btnBangsal);
        btnBangsal.setBounds(676, 580, 28, 23);

        PaduanOAT.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Belum", "Kategori 1", "Kategori Anak", "Kategori Lepasan" }));
        PaduanOAT.setName("PaduanOAT"); // NOI18N
        PaduanOAT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PaduanOATKeyPressed(evt);
            }
        });
        FormInput.add(PaduanOAT);
        PaduanOAT.setBounds(103, 280, 150, 23);

        Scroll1.setViewportView(FormInput);

        internalFrame2.add(Scroll1, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Data Pasien Teridentifikasi TB", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
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
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbJnsPerawatanKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbJnsPerawatan);

        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Daftar Pasien Teridentifikasi TB", internalFrame3);

        internalFrame4.setBorder(null);
        internalFrame4.setName("internalFrame4"); // NOI18N
        internalFrame4.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbJnsPerawatan1.setAutoCreateRowSorter(true);
        tbJnsPerawatan1.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbJnsPerawatan1.setName("tbJnsPerawatan1"); // NOI18N
        tbJnsPerawatan1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbJnsPerawatan1MouseClicked(evt);
            }
        });
        tbJnsPerawatan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbJnsPerawatan1KeyPressed(evt);
            }
        });
        Scroll2.setViewportView(tbJnsPerawatan1);

        internalFrame4.add(Scroll2, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("   Daftar Pasien Teridentifikasi TB Format Dinas   ", internalFrame4);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(PaduanOAT.getSelectedItem().equals("Belum")){
            PaduanOATHasil = "";
        }else{
            PaduanOATHasil = PaduanOAT.getSelectedItem().toString();
        }
        
        if(TNoRw.getText().trim().equals("")||TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KdKel.getText().trim().equals("")||Kelurahan.getText().trim().equals("")){
            Valid.textKosong(BtnKelurahan,"Kelurahan");
        }else if(KdKec.getText().trim().equals("")||Kecamatan.getText().trim().equals("")){
            Valid.textKosong(BtnKecamatan,"Kecamatan");
        }else if(KdKab.getText().trim().equals("")||Kabupaten.getText().trim().equals("")){
            Valid.textKosong(BtnKabupaten,"Kabupaten");
        }else if(KdProp.getText().trim().equals("")||Propinsi.getText().trim().equals("")){
            Valid.textKosong(BtnPropinsi,"Propinsi");
        }else if(KeteranganRujukan.getText().trim().equals("")){
            Valid.textKosong(KeteranganRujukan,"Keterangan Rujukan");
        }else if(KeteranganSO.getText().trim().equals("")){
            Valid.textKosong(KeteranganSO,"Keterangan Sumber Obat");
        }else if(PemeriksaanLaboratBulan2NoReg.getText().trim().equals("")){
            Valid.textKosong(PemeriksaanLaboratBulan2NoReg,"No.Reg Pemeriksaan Laborat Bulan 2");
        }else if(PemeriksaanLaboratBulan3NoReg.getText().trim().equals("")){
            Valid.textKosong(PemeriksaanLaboratBulan3NoReg,"No.Reg Pemeriksaan Laborat Bulan 3");
        }else if(PemeriksaanLaboratBulan5NoReg.getText().trim().equals("")){
            Valid.textKosong(PemeriksaanLaboratBulan5NoReg,"No.Reg Pemeriksaan Laborat Bulan 5");
        }else if(PemeriksaanLaboratAkhirNoReg.getText().trim().equals("")){
            Valid.textKosong(PemeriksaanLaboratAkhirNoReg,"No.Reg Pemeriksaan Laborat Akhir Pengobatan");
        }else if(Keterangan.getText().trim().equals("")){
            Valid.textKosong(Keterangan,"Keterangan");
        }else if(kdpenyakit.getText().trim().equals("")||nmpenyakit.getText().trim().equals("")){
            Valid.textKosong(btnBangsal,"Penyakit");
        }else{
            id_tb_03="";
            try {
                if(HasilAkhirPengobatan.getSelectedItem().equals("Belum")){
                    headers = new HttpHeaders();
                headers.add("X-rs-id",idrs);
                headers.add("X-Timestamp",String.valueOf(api.GetUTCdatetimeAsString())); 
                headers.add("X-pass",koneksiDB.PASSSITT()); 
                headers.add("Content-Type","application/json");
                requestJson ="{" +
                    "\"id_tb_03\":\"\"," +
                    "\"kd_pasien\":\""+TNmPasien.getText().toUpperCase()+"\"," +
                    "\"nik\":\""+NIK.getText()+"\"," +
                    "\"jenis_kelamin\":\""+JK.getText().substring(0,1)+"\"," +
                    "\"alamat_lengkap\":\""+Alamat.getText()+"\"," +
                    "\"id_propinsi_faskes\":\""+KdProp.getText()+"\"," +
                    "\"kd_kabupaten_faskes\":\""+KdKab.getText()+"\"," +
                    "\"id_propinsi_pasien\":\""+KdProp.getText()+"\"," +
                    "\"kd_kabupaten_pasien\":\""+KdKab.getText()+"\"," +
                    "\"kd_fasyankes\":\""+idrs+"\"," +
                    "\"kode_icd_x\":\""+kdpenyakit.getText()+"\"," +
                    "\"tipe_diagnosis\":\""+TipeDiagnosis.getSelectedItem().toString().replaceAll("Terkonfirmasi bakteriologis","1").toString().replaceAll("Terdiagnosis klinis","2")+"\"," +
                    "\"klasifikasi_lokasi_anatomi\":\""+Lokasi.getSelectedItem().toString().replaceAll("Paru","1").toString().replaceAll("Ekstraparu","2")+"\"," +
                    "\"klasifikasi_riwayat_pengobatan\":\""+Riwayat.getSelectedItem().toString().replaceAll("Baru","1").toString().replaceAll("Kambuh","2").toString().replaceAll("Diobati setelah gagal","3").toString().replaceAll("Diobati Setelah Putus Berobat","4").toString().replaceAll("Lain-lain","5").toString().replaceAll("Riwayat Pengobatan Sebelumnya Tidak Diketahui","6").toString().replaceAll("Pindahan","7")+"\"," +
                    "\"tanggal_mulai_pengobatan\":\""+Valid.SetTgl4(MulaiBerobat.getSelectedItem()+"")+"\"," +
                    "\"paduan_oat\":\""+PaduanOATHasil+"\"," +
                    "\"sebelum_pengobatan_hasil_mikroskopis\":\""+SebelumPengobatanMikroskopis.getSelectedItem()+"\"," +
                    "\"sebelum_pengobatan_hasil_tes_cepat\":\""+SebelumPengobatanTesCepat.getSelectedItem()+"\"," +
                    "\"sebelum_pengobatan_hasil_biakan\":\""+SebelumPengobatanBiakan.getSelectedItem()+"\"," +
                    "\"hasil_mikroskopis_bulan_2\":\""+PemeriksaanLaboratBulan2Mikroskopis.getSelectedItem()+"\"," +
                    "\"hasil_mikroskopis_bulan_3\":\""+PemeriksaanLaboratBulan3Mikroskopis.getSelectedItem()+"\"," +
                    "\"hasil_mikroskopis_bulan_5\":\""+PemeriksaanLaboratBulan5Mikroskopis.getSelectedItem()+"\"," +
                    "\"akhir_pengobatan_hasil_mikroskopis\":\""+PemeriksaanLaboratAkhirPengobatanMikroskopis.getSelectedItem()+"\"," +
                    "\"tanggal_hasil_akhir_pengobatan\":\""+Valid.SetTgl4(AkhirBerobat.getSelectedItem()+"")+"\"," +
                    "\"hasil_akhir_pengobatan\":\""+tidakada+"\"," +
                    "\"tgl_lahir\":\""+Tanggal.getText().replaceAll("-","")+"\"," +
                    "\"foto_toraks\":\""+FotoToraks.getSelectedItem()+"\"" +
                "}";
                System.out.println(requestJson);
                requestEntity = new HttpEntity(requestJson,headers);
                requestJson=api.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody();
                System.out.println(requestJson);
                root = mapper.readTree(requestJson);
                id_tb_03=root.path("id_tb_03").asText();
               }else{
                headers = new HttpHeaders();
                headers.add("X-rs-id",idrs);
                headers.add("X-Timestamp",String.valueOf(api.GetUTCdatetimeAsString())); 
                headers.add("X-pass",koneksiDB.PASSSITT()); 
                headers.add("Content-Type","application/json");
                requestJson ="{" +
                    "\"id_tb_03\":\"\"," +
                    "\"kd_pasien\":\""+TNmPasien.getText().toUpperCase()+"\"," +
                    "\"nik\":\""+NIK.getText()+"\"," +
                    "\"jenis_kelamin\":\""+JK.getText().substring(0,1)+"\"," +
                    "\"alamat_lengkap\":\""+Alamat.getText()+"\"," +
                    "\"id_propinsi_faskes\":\""+KdProp.getText()+"\"," +
                    "\"kd_kabupaten_faskes\":\""+KdKab.getText()+"\"," +
                    "\"id_propinsi_pasien\":\""+KdProp.getText()+"\"," +
                    "\"kd_kabupaten_pasien\":\""+KdKab.getText()+"\"," +
                    "\"kd_fasyankes\":\""+idrs+"\"," +
                    "\"kode_icd_x\":\""+kdpenyakit.getText()+"\"," +
                    "\"tipe_diagnosis\":\""+TipeDiagnosis.getSelectedItem().toString().replaceAll("Terkonfirmasi bakteriologis","1").toString().replaceAll("Terdiagnosis klinis","2")+"\"," +
                    "\"klasifikasi_lokasi_anatomi\":\""+Lokasi.getSelectedItem().toString().replaceAll("Paru","1").toString().replaceAll("Ekstraparu","2")+"\"," +
                    "\"klasifikasi_riwayat_pengobatan\":\""+Riwayat.getSelectedItem().toString().replaceAll("Baru","1").toString().replaceAll("Kambuh","2").toString().replaceAll("Diobati setelah gagal","3").toString().replaceAll("Diobati Setelah Putus Berobat","4").toString().replaceAll("Lain-lain","5").toString().replaceAll("Riwayat Pengobatan Sebelumnya Tidak Diketahui","6").toString().replaceAll("Pindahan","7")+"\"," +
                    "\"tanggal_mulai_pengobatan\":\""+Valid.SetTgl4(MulaiBerobat.getSelectedItem()+"")+"\"," +
                    "\"paduan_oat\":\""+PaduanOATHasil+"\"," +
                    "\"sebelum_pengobatan_hasil_mikroskopis\":\""+SebelumPengobatanMikroskopis.getSelectedItem()+"\"," +
                    "\"sebelum_pengobatan_hasil_tes_cepat\":\""+SebelumPengobatanTesCepat.getSelectedItem()+"\"," +
                    "\"sebelum_pengobatan_hasil_biakan\":\""+SebelumPengobatanBiakan.getSelectedItem()+"\"," +
                    "\"hasil_mikroskopis_bulan_2\":\""+PemeriksaanLaboratBulan2Mikroskopis.getSelectedItem()+"\"," +
                    "\"hasil_mikroskopis_bulan_3\":\""+PemeriksaanLaboratBulan3Mikroskopis.getSelectedItem()+"\"," +
                    "\"hasil_mikroskopis_bulan_5\":\""+PemeriksaanLaboratBulan5Mikroskopis.getSelectedItem()+"\"," +
                    "\"akhir_pengobatan_hasil_mikroskopis\":\""+PemeriksaanLaboratAkhirPengobatanMikroskopis.getSelectedItem()+"\"," +
                    "\"tanggal_hasil_akhir_pengobatan\":\""+Valid.SetTgl4(AkhirBerobat.getSelectedItem()+"")+"\"," +
                    "\"hasil_akhir_pengobatan\":\""+HasilAkhirPengobatan.getSelectedItem()+"\"," +
                    "\"tgl_lahir\":\""+Tanggal.getText().replaceAll("-","")+"\"," +
                    "\"foto_toraks\":\""+FotoToraks.getSelectedItem()+"\"" +
                "}";
                System.out.println(requestJson);
                requestEntity = new HttpEntity(requestJson,headers);
                requestJson=api.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody();
                System.out.println(requestJson);
                root = mapper.readTree(requestJson);
                id_tb_03=root.path("id_tb_03").asText();
                    }
            } catch (Exception ex) {
                System.out.println("Notifikasi Bridging : "+ex);
                if(ex.toString().contains("UnknownHostException")){
                    JOptionPane.showMessageDialog(null,"Koneksi ke server SITB terputus...!");
                }else if(ex.toString().contains("502")){
                    JOptionPane.showMessageDialog(null,"Connection timed out. Hayati lelah bang...!");
                }
            }
            if(Sequel.menyimpantf("data_tb","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","Data",50,new String[]{
                    TNoRw.getText(),id_tb_03,PeriodeLaporan.getSelectedItem().toString(),Valid.SetTgl(TanggalLaporan.getSelectedItem()+"")+" "+TanggalLaporan.getSelectedItem().toString().substring(11,19), 
                    Valid.SetTgl(TanggalLaporan.getSelectedItem()+"").substring(0,4),kdwasor,"0",KdProp.getText(),KdKab.getText(),KdKec.getText(),KdKel.getText(),Rujukan.getSelectedItem().toString(),
                    KeteranganRujukan.getText(),TipeDiagnosis.getSelectedItem().toString().replaceAll("1. Terkonfirmasi bakteriologis","Terkonfirmasi bakteriologis").toString().replaceAll("2. Terdiagnosis klinis","Terdiagnosis klinis"),Lokasi.getSelectedItem().toString().replaceAll("1. Paru","Paru").toString().replaceAll("2. Ekstraparu","Ekstraparu"),Riwayat.getSelectedItem().toString().replaceAll("1. Baru","Baru").toString().replaceAll("2. Kambuh","Kambuh").toString().replaceAll("3. Diobati setelah gagal","Diobati setelah gagal").toString().replaceAll("4. Diobati Setelah Putus Berobat","Diobati Setelah Putus Berobat").toString().replaceAll("5. Lain-lain","Lain-lain").toString().replaceAll("6. Riwayat Pengobatan Sebelumnya Tidak Diketahui","Riwayat Pengobatan Sebelumnya Tidak Diketahui").toString().replaceAll("7. Pindahan","Pindahan"),StatusHIV.getSelectedItem().toString(),
                    SkoringAnak.getSelectedItem().toString(),Skoring5.getSelectedItem().toString(),Skoring6.getSelectedItem().toString(),Valid.SetTgl(MulaiBerobat.getSelectedItem()+""), 
                    PaduanOATHasil,SumberObat.getSelectedItem().toString(),KeteranganSO.getText(),SebelumPengobatanMikroskopis.getSelectedItem().toString(),SebelumPengobatanTesCepat.getSelectedItem().toString(),
                    SebelumPengobatanBiakan.getSelectedItem().toString(),PemeriksaanLaboratBulan2NoReg.getText(),PemeriksaanLaboratBulan2Mikroskopis.getSelectedItem().toString(), 
                    PemeriksaanLaboratBulan3NoReg.getText(),PemeriksaanLaboratBulan3Mikroskopis.getSelectedItem().toString(),PemeriksaanLaboratBulan5NoReg.getText(),
                    PemeriksaanLaboratBulan5Mikroskopis.getSelectedItem().toString(),PemeriksaanLaboratAkhirNoReg.getText(),PemeriksaanLaboratAkhirPengobatanMikroskopis.getSelectedItem().toString(),
                    Valid.SetTgl(AkhirBerobat.getSelectedItem()+""),HasilAkhirPengobatan.getSelectedItem().toString(),Valid.SetTgl(DianjurkanTesHIV.getSelectedItem().toString()+""), 
                    Valid.SetTgl(TanggalTesHIV.getSelectedItem()+""),HasilTesHIV.getSelectedItem().toString(),PPK.getSelectedItem().toString(),ART.getSelectedItem().toString(), 
                    TBDM.getSelectedItem().toString(),TerapiDM.getSelectedItem().toString(),PindahRO.getSelectedItem().toString(),Status.getSelectedItem().toString(),FotoToraks.getSelectedItem().toString(),
                    ToraksTidakDilakukan.getSelectedItem().toString(),Keterangan.getText(),kdpenyakit.getText()
                })==true){
                    emptTeks();
                    tampil();
            }
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,btnBangsal,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();       
        TabRawat.setSelectedIndex(0); 
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tbJnsPerawatan.getSelectedRow()>-1){
            Sequel.meghapus("data_tb","no_rawat",tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),0).toString());
            emptTeks();
            tampil();
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
        if(PaduanOAT.getSelectedItem().equals("Belum")){
        PaduanOATHasil = "";
        }else{
        PaduanOATHasil = PaduanOAT.getSelectedItem().toString();
        }
        if(TNoRw.getText().trim().equals("")||TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KdKel.getText().trim().equals("")||Kelurahan.getText().trim().equals("")){
            Valid.textKosong(BtnKelurahan,"Kelurahan");
        }else if(KdKec.getText().trim().equals("")||Kecamatan.getText().trim().equals("")){
            Valid.textKosong(BtnKecamatan,"Kecamatan");
        }else if(KdKab.getText().trim().equals("")||Kabupaten.getText().trim().equals("")){
            Valid.textKosong(BtnKabupaten,"Kabupaten");
        }else if(KdProp.getText().trim().equals("")||Propinsi.getText().trim().equals("")){
            Valid.textKosong(BtnPropinsi,"Propinsi");
        }else if(KeteranganRujukan.getText().trim().equals("")){
            Valid.textKosong(KeteranganRujukan,"Keterangan Rujukan");
        }else if(KeteranganSO.getText().trim().equals("")){
            Valid.textKosong(KeteranganSO,"Keterangan Sumber Obat");
        }else if(PemeriksaanLaboratBulan2NoReg.getText().trim().equals("")){
            Valid.textKosong(PemeriksaanLaboratBulan2NoReg,"No.Reg Pemeriksaan Laborat Bulan 2");
        }else if(PemeriksaanLaboratBulan3NoReg.getText().trim().equals("")){
            Valid.textKosong(PemeriksaanLaboratBulan3NoReg,"No.Reg Pemeriksaan Laborat Bulan 3");
        }else if(PemeriksaanLaboratBulan5NoReg.getText().trim().equals("")){
            Valid.textKosong(PemeriksaanLaboratBulan5NoReg,"No.Reg Pemeriksaan Laborat Bulan 5");
        }else if(PemeriksaanLaboratAkhirNoReg.getText().trim().equals("")){
            Valid.textKosong(PemeriksaanLaboratAkhirNoReg,"No.Reg Pemeriksaan Laborat Akhir Pengobatan");
        }else if(Keterangan.getText().trim().equals("")){
            Valid.textKosong(Keterangan,"Keterangan");
        }else if(kdpenyakit.getText().trim().equals("")||nmpenyakit.getText().trim().equals("")){
            Valid.textKosong(btnBangsal,"Penyakit");
        }else{
            if(tbJnsPerawatan.getSelectedRow()>-1){
                id_tb_03="";
                try {
                    headers = new HttpHeaders();
                    headers.add("X-rs-id",idrs);
                    headers.add("X-Timestamp",String.valueOf(api.GetUTCdatetimeAsString())); 
                    headers.add("X-pass",koneksiDB.PASSSITT()); 
                    headers.add("Content-Type","application/json");
                    if(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),59).toString().equals("")){
                        requestJson ="{" +
                            "\"id_tb_03\":\"\"," +
                            "\"kd_pasien\":\""+TNmPasien.getText().toUpperCase()+"\"," +
                            "\"nik\":\""+NIK.getText()+"\"," +
                            "\"jenis_kelamin\":\""+JK.getText().substring(0,1)+"\"," +
                            "\"alamat_lengkap\":\""+Alamat.getText()+"\"," +
                            "\"id_propinsi_faskes\":\""+KdProp.getText()+"\"," +
                            "\"kd_kabupaten_faskes\":\""+KdKab.getText()+"\"," +
                            "\"id_propinsi_pasien\":\""+KdProp.getText()+"\"," +
                            "\"kd_kabupaten_pasien\":\""+KdKab.getText()+"\"," +
                            "\"kd_fasyankes\":\""+idrs+"\"," +
                            "\"kode_icd_x\":\""+kdpenyakit.getText()+"\"," +
                            "\"tipe_diagnosis\":\""+TipeDiagnosis.getSelectedItem().toString().replaceAll("Terkonfirmasi bakteriologis","1").toString().replaceAll("Terdiagnosis klinis","2")+"\"," +
                            "\"klasifikasi_lokasi_anatomi\":\""+Lokasi.getSelectedItem().toString().replaceAll("Paru","1").toString().replaceAll("Ekstraparu","2")+"\"," +
                            "\"klasifikasi_riwayat_pengobatan\":\""+Riwayat.getSelectedItem().toString().replaceAll("Baru","1").toString().replaceAll("Kambuh","2").toString().replaceAll("Diobati setelah gagal","3").toString().replaceAll("Diobati Setelah Putus Berobat","4").toString().replaceAll("Lain-lain","5").toString().replaceAll("Riwayat Pengobatan Sebelumnya Tidak Diketahui","6").toString().replaceAll("Pindahan","7")+"\"," +
                            "\"tanggal_mulai_pengobatan\":\""+Valid.SetTgl4(MulaiBerobat.getSelectedItem()+"")+"\"," +
                            "\"paduan_oat\":\""+PaduanOATHasil+"\"," +
                            "\"sebelum_pengobatan_hasil_mikroskopis\":\""+SebelumPengobatanMikroskopis.getSelectedItem()+"\"," +
                            "\"sebelum_pengobatan_hasil_tes_cepat\":\""+SebelumPengobatanTesCepat.getSelectedItem()+"\"," +
                            "\"sebelum_pengobatan_hasil_biakan\":\""+SebelumPengobatanBiakan.getSelectedItem()+"\"," +
                            "\"hasil_mikroskopis_bulan_2\":\""+PemeriksaanLaboratBulan2Mikroskopis.getSelectedItem()+"\"," +
                            "\"hasil_mikroskopis_bulan_3\":\""+PemeriksaanLaboratBulan3Mikroskopis.getSelectedItem()+"\"," +
                            "\"hasil_mikroskopis_bulan_5\":\""+PemeriksaanLaboratBulan5Mikroskopis.getSelectedItem()+"\"," +
                            "\"akhir_pengobatan_hasil_mikroskopis\":\""+PemeriksaanLaboratAkhirPengobatanMikroskopis.getSelectedItem()+"\"," +
                            "\"tanggal_hasil_akhir_pengobatan\":\""+Valid.SetTgl4(AkhirBerobat.getSelectedItem()+"")+"\"," +
                            "\"hasil_akhir_pengobatan\":\""+HasilAkhirPengobatan.getSelectedItem()+"\"," +
                            "\"tgl_lahir\":\""+Tanggal.getText().replaceAll("-","")+"\"," +
                            "\"foto_toraks\":\""+FotoToraks.getSelectedItem()+"\"" +
                        "}";
                        System.out.println(requestJson);
                        requestEntity = new HttpEntity(requestJson,headers);
                        requestJson=api.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody();
                        System.out.println(requestJson);
                        root = mapper.readTree(requestJson);
                        id_tb_03=root.path("id_tb_03").asText();
                    }else{
                        requestJson ="{" +
                            "\"id_tb_03\":\""+tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),59).toString()+"\"," +
                            "\"kd_pasien\":\""+TNmPasien.getText().toUpperCase()+"\"," +
                            "\"nik\":\""+NIK.getText()+"\"," +
                            "\"jenis_kelamin\":\""+JK.getText().substring(0,1)+"\"," +
                            "\"alamat_lengkap\":\""+Alamat.getText()+"\"," +
                            "\"id_propinsi_faskes\":\""+KdProp.getText()+"\"," +
                            "\"kd_kabupaten_faskes\":\""+KdKab.getText()+"\"," +
                            "\"id_propinsi_pasien\":\""+KdProp.getText()+"\"," +
                            "\"kd_kabupaten_pasien\":\""+KdKab.getText()+"\"," +
                            "\"kd_fasyankes\":\""+idrs+"\"," +
                            "\"kode_icd_x\":\""+kdpenyakit.getText()+"\"," +
                            "\"tipe_diagnosis\":\""+TipeDiagnosis.getSelectedItem().toString().replaceAll("Terkonfirmasi bakteriologis","1").toString().replaceAll("Terdiagnosis klinis","2")+"\"," +
                            "\"klasifikasi_lokasi_anatomi\":\""+Lokasi.getSelectedItem().toString().replaceAll("Paru","1").toString().replaceAll("Ekstraparu","2")+"\"," +
                            "\"klasifikasi_riwayat_pengobatan\":\""+Riwayat.getSelectedItem().toString().replaceAll("Baru","1").toString().replaceAll("Kambuh","2").toString().replaceAll("Diobati setelah gagal","3").toString().replaceAll("Diobati Setelah Putus Berobat","4").toString().replaceAll("Lain-lain","5").toString().replaceAll("Riwayat Pengobatan Sebelumnya Tidak Diketahui","6").toString().replaceAll("Pindahan","7")+"\"," +
                            "\"tanggal_mulai_pengobatan\":\""+Valid.SetTgl4(MulaiBerobat.getSelectedItem()+"")+"\"," +
                            "\"paduan_oat\":\""+PaduanOATHasil+"\"," +
                            "\"sebelum_pengobatan_hasil_mikroskopis\":\""+SebelumPengobatanMikroskopis.getSelectedItem()+"\"," +
                            "\"sebelum_pengobatan_hasil_tes_cepat\":\""+SebelumPengobatanTesCepat.getSelectedItem()+"\"," +
                            "\"sebelum_pengobatan_hasil_biakan\":\""+SebelumPengobatanBiakan.getSelectedItem()+"\"," +
                            "\"hasil_mikroskopis_bulan_2\":\""+PemeriksaanLaboratBulan2Mikroskopis.getSelectedItem()+"\"," +
                            "\"hasil_mikroskopis_bulan_3\":\""+PemeriksaanLaboratBulan3Mikroskopis.getSelectedItem()+"\"," +
                            "\"hasil_mikroskopis_bulan_5\":\""+PemeriksaanLaboratBulan5Mikroskopis.getSelectedItem()+"\"," +
                            "\"akhir_pengobatan_hasil_mikroskopis\":\""+PemeriksaanLaboratAkhirPengobatanMikroskopis.getSelectedItem()+"\"," +
                            "\"tanggal_hasil_akhir_pengobatan\":\""+Valid.SetTgl4(AkhirBerobat.getSelectedItem()+"")+"\"," +
                            "\"hasil_akhir_pengobatan\":\""+HasilAkhirPengobatan.getSelectedItem()+"\"," +
                            "\"tgl_lahir\":\""+Tanggal.getText().replaceAll("-","")+"\"," +
                            "\"foto_toraks\":\""+FotoToraks.getSelectedItem()+"\"" +
                        "}";
                        System.out.println(requestJson);
                        requestEntity = new HttpEntity(requestJson,headers);
                        requestJson=api.getRest().exchange(URL, HttpMethod.POST, requestEntity, String.class).getBody();
                        System.out.println(requestJson);
                        root = mapper.readTree(requestJson);
                        id_tb_03=tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),59).toString();
                    }
                } catch (Exception ex) {
                    System.out.println("Notifikasi Bridging : "+ex);
                    if(ex.toString().contains("UnknownHostException")){
                        JOptionPane.showMessageDialog(null,"Koneksi ke server SITT terputus...!");
                    }else if(ex.toString().contains("502")){
                        JOptionPane.showMessageDialog(null,"Connection timed out. Hayati lelah bang...!");
                    }
                }
                if(Sequel.mengedittf("data_tb","no_rawat=?","no_rawat=?,id_tb_03=?,id_periode_laporan=?,tanggal_buat_laporan=?,tahun_buat_laporan=?,kd_wasor=?,noregkab=?,id_propinsi=?,kd_kabupaten=?,id_kecamatan=?,id_kelurahan=?,nama_rujukan=?,sebutkan1=?,tipe_diagnosis=?,klasifikasi_lokasi_anatomi=?,klasifikasi_riwayat_pengobatan=?,klasifikasi_status_hiv=?,total_skoring_anak=?,konfirmasiSkoring5=?,konfirmasiSkoring6=?,tanggal_mulai_pengobatan=?,paduan_oat=?,sumber_obat=?,sebutkan=?,sebelum_pengobatan_hasil_mikroskopis=?,sebelum_pengobatan_hasil_tes_cepat=?,sebelum_pengobatan_hasil_biakan=?,noreglab_bulan_2=?,hasil_mikroskopis_bulan_2=?,noreglab_bulan_3=?,hasil_mikroskopis_bulan_3=?,noreglab_bulan_5=?,hasil_mikroskopis_bulan_5=?,akhir_pengobatan_noreglab=?,akhir_pengobatan_hasil_mikroskopis=?,tanggal_hasil_akhir_pengobatan=?,hasil_akhir_pengobatan=?,tanggal_dianjurkan_tes=?,tanggal_tes_hiv=?,hasil_tes_hiv=?,ppk=?,art=?,tb_dm=?,terapi_dm=?,pindah_ro=?,status_pengobatan=?,foto_toraks=?,toraks_tdk_dilakukan=?,keterangan=?,kode_icd_x=?",51,new String[]{
                        TNoRw.getText(),id_tb_03,PeriodeLaporan.getSelectedItem().toString(),Valid.SetTgl(TanggalLaporan.getSelectedItem()+"")+" "+TanggalLaporan.getSelectedItem().toString().substring(11,19), 
                        Valid.SetTgl(TanggalLaporan.getSelectedItem()+"").substring(0,4),kdwasor,"0",KdProp.getText(),KdKab.getText(),KdKec.getText(),KdKel.getText(),Rujukan.getSelectedItem().toString(),
                        KeteranganRujukan.getText(),TipeDiagnosis.getSelectedItem().toString().replaceAll("1. Terkonfirmasi bakteriologis","Terkonfirmasi bakteriologis").toString().replaceAll("2. Terdiagnosis klinis","Terdiagnosis klinis"),Lokasi.getSelectedItem().toString().replaceAll("1. Paru","Paru").toString().replaceAll("2. Ekstraparu","Ekstraparu"),Riwayat.getSelectedItem().toString().replaceAll("1. Baru","Baru").toString().replaceAll("2. Kambuh","Kambuh").toString().replaceAll("3. Diobati setelah gagal","Diobati setelah gagal").toString().replaceAll("4. Diobati Setelah Putus Berobat","Diobati Setelah Putus Berobat").toString().replaceAll("5. Lain-lain","Lain-lain").toString().replaceAll("6. Riwayat Pengobatan Sebelumnya Tidak Diketahui","Riwayat Pengobatan Sebelumnya Tidak Diketahui").toString().replaceAll("7. Pindahan","Pindahan"),StatusHIV.getSelectedItem().toString(),
                        SkoringAnak.getSelectedItem().toString(),Skoring5.getSelectedItem().toString(),Skoring6.getSelectedItem().toString(),Valid.SetTgl(MulaiBerobat.getSelectedItem()+""), 
                        PaduanOATHasil,SumberObat.getSelectedItem().toString(),KeteranganSO.getText(),SebelumPengobatanMikroskopis.getSelectedItem().toString(),SebelumPengobatanTesCepat.getSelectedItem().toString(),
                        SebelumPengobatanBiakan.getSelectedItem().toString(),PemeriksaanLaboratBulan2NoReg.getText(),PemeriksaanLaboratBulan2Mikroskopis.getSelectedItem().toString(), 
                        PemeriksaanLaboratBulan3NoReg.getText(),PemeriksaanLaboratBulan3Mikroskopis.getSelectedItem().toString(),PemeriksaanLaboratBulan5NoReg.getText(),
                        PemeriksaanLaboratBulan5Mikroskopis.getSelectedItem().toString(),PemeriksaanLaboratAkhirNoReg.getText(),PemeriksaanLaboratAkhirPengobatanMikroskopis.getSelectedItem().toString(),
                        Valid.SetTgl(AkhirBerobat.getSelectedItem()+""),HasilAkhirPengobatan.getSelectedItem().toString(),Valid.SetTgl(DianjurkanTesHIV.getSelectedItem().toString()+""), 
                        Valid.SetTgl(TanggalTesHIV.getSelectedItem()+""),HasilTesHIV.getSelectedItem().toString(),PPK.getSelectedItem().toString(),ART.getSelectedItem().toString(), 
                        TBDM.getSelectedItem().toString(),TerapiDM.getSelectedItem().toString(),PindahRO.getSelectedItem().toString(),Status.getSelectedItem().toString(),FotoToraks.getSelectedItem().toString(),
                        ToraksTidakDilakukan.getSelectedItem().toString(),Keterangan.getText(),kdpenyakit.getText(),tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),0).toString()
                    })==true){
                        emptTeks();
                        tampil();
                        TabRawat.setSelectedIndex(1);
                }
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
            dispose();
        }else{Valid.pindah(evt,BtnAll,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        boolean formatDinas = TabRawat.getSelectedIndex() == 2;
        if (!formatDinas && CmbStatusRawatCari.getSelectedIndex() == 0) {
            Object[] opsi = {"Laporan Standar", "Excel (.xlsx)"};
            int pilihan = JOptionPane.showOptionDialog(this, "Pilih output cetak.", "Cetak Data TB",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opsi, opsi[0]);
            if (pilihan == 0) {
                cetakLaporanStandar();
                return;
            }
            if (pilihan != 1) {
                return;
            }
        }

        if (muatDaftar(formatDinas)) {
            DefaultTableModel model = formatDinas ? tabMode2 : tabMode;
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada data dalam hasil filter untuk diekspor.");
                return;
            }
            simpanExcel(model, formatDinas ? "Format Dinas TB" : "Data TB");
        }
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if (evt.getKeyCode() != KeyEvent.VK_SPACE) {
            Valid.pindah(evt, BtnEdit, BtnAll);
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
        if (TabRawat.getSelectedIndex() == 0) {
            TabRawat.setSelectedIndex(1);
        }
        muatDaftarAktif();
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() != KeyEvent.VK_SPACE) {
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        CmbStatusRawatCari.setSelectedIndex(0);
        BtnCariActionPerformed(null);
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() != KeyEvent.VK_SPACE) {
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbJnsPerawatanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbJnsPerawatanMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
                if(evt.getClickCount()==2){
                    TabRawat.setSelectedIndex(0);
                   
                }
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbJnsPerawatanMouseClicked

    private void tbJnsPerawatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbJnsPerawatanKeyPressed
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
            }else if(evt.getKeyCode()==KeyEvent.VK_A){                
                for(i=0;i<tbJnsPerawatan.getRowCount();i++){ 
                    tbJnsPerawatan.setValueAt(true,i,0);
                }
            } 
        }
}//GEN-LAST:event_tbJnsPerawatanKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        muatDaftarAktif();
    }//GEN-LAST:event_TabRawatMouseClicked

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select concat(pasien.no_rkm_medis,', ',pasien.nm_pasien) from reg_periksa inner join pasien "+
                " on pasien.no_rkm_medis=reg_periksa.no_rkm_medis where reg_periksa.no_rawat=? ",TNoRM,TNoRw.getText());
        }else{
            //Valid.pindah(evt,TCari,kdoperator1);
        }
    }//GEN-LAST:event_TNoRwKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalKeyPressed

    private void UmurKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_UmurKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_UmurKeyPressed

    private void NoKartuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoKartuKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NoKartuKeyPressed

    private void NIKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NIKKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NIKKeyPressed

    private void PeriodeLaporanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PeriodeLaporanKeyPressed
        Valid.pindah(evt,KdProp,TanggalLaporan);
    }//GEN-LAST:event_PeriodeLaporanKeyPressed

    private void TanggalLaporanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalLaporanKeyPressed
        Valid.pindah(evt,PeriodeLaporan,Rujukan);
    }//GEN-LAST:event_TanggalLaporanKeyPressed

    private void AlamatMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AlamatMouseMoved
        if(Alamat.getText().equals("ALAMAT")){
            Alamat.setText("");
        }
    }//GEN-LAST:event_AlamatMouseMoved

    private void AlamatMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AlamatMouseExited
        if(Alamat.getText().equals("")){
            Alamat.setText("ALAMAT");
        }
    }//GEN-LAST:event_AlamatMouseExited

    private void AlamatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlamatKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(Alamat.getText().equals("")){
                Alamat.setText("ALAMAT");
            }
            if(Kelurahan.getText().equals("KELURAHAN")){
                Kelurahan.setText("");
            }
            Kelurahan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            if(Alamat.getText().equals("")){
                Alamat.setText("ALAMAT");
            }
            //TKtp.requestFocus();
        }
    }//GEN-LAST:event_AlamatKeyPressed

    private void KelurahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KelurahanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(Kelurahan.getText().equals("")){
                Kelurahan.setText("KELURAHAN");
            }
            if(Kecamatan.getText().equals("KECAMATAN")){
                Kecamatan.setText("");
            }
            Kecamatan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            if(Kelurahan.getText().equals("")){
                Kelurahan.setText("KELURAHAN");
            }
            if(Alamat.getText().equals("ALAMAT")){
                Alamat.setText("");
            }
            Alamat.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnKelurahanActionPerformed(null);
        }
    }//GEN-LAST:event_KelurahanKeyPressed

    private void BtnKelurahanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKelurahanActionPerformed
        if(KdKec.getText().trim().equals("")||Kecamatan.getText().trim().equals("")||Kecamatan.getText().trim().equals("KECAMATAN")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih kecamatan dulu..!!");
            BtnKelurahan.requestFocus();
        }else{
            kelurahan.setPropinsi(KdKec.getText(),Kecamatan.getText());
            kelurahan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            kelurahan.setLocationRelativeTo(internalFrame1);
            kelurahan.setVisible(true);
        }
    }//GEN-LAST:event_BtnKelurahanActionPerformed

    private void KecamatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KecamatanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(Kecamatan.getText().equals("")){
                Kecamatan.setText("KECAMATAN");
            }
            if(Kabupaten.getText().equals("KABUPATEN")){
                Kabupaten.setText("");
            }
            Kabupaten.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            if(Kecamatan.getText().equals("")){
                Kecamatan.setText("KECAMATAN");
            }
            if(Kelurahan.getText().equals("KELURAHAN")){
                Kelurahan.setText("");
            }
            Kelurahan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnKecamatanActionPerformed(null);
        }
    }//GEN-LAST:event_KecamatanKeyPressed

    private void BtnKecamatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKecamatanActionPerformed
        if(KdKab.getText().trim().equals("")||Kabupaten.getText().trim().equals("")||Kabupaten.getText().trim().equals("KABUPATEN")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih kabupaten dulu..!!");
            BtnKabupaten.requestFocus();
        }else{
            kecamatan.setPropinsi(KdKab.getText(),Kabupaten.getText());
            kecamatan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            kecamatan.setLocationRelativeTo(internalFrame1);
            kecamatan.setVisible(true);
        }
    }//GEN-LAST:event_BtnKecamatanActionPerformed

    private void BtnPropinsiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPropinsiActionPerformed
        propinsi.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        propinsi.setLocationRelativeTo(internalFrame1);
        propinsi.setVisible(true);
    }//GEN-LAST:event_BtnPropinsiActionPerformed

    private void PropinsiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PropinsiKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(Propinsi.getText().equals("")){
                Propinsi.setText("PROPINSI");
            }
            /*if(AlamatPj.getText().equals("ALAMAT")){
                AlamatPj.setText("");
            }
            AlamatPj.requestFocus();*/
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            if(Propinsi.getText().equals("")){
                Propinsi.setText("PROPINSI");
            }
            if(Kabupaten.getText().equals("KABUPATEN")){
                Kabupaten.setText("");
            }
            Kabupaten.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnPropinsiActionPerformed(null);
        }
    }//GEN-LAST:event_PropinsiKeyPressed

    private void BtnKabupatenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKabupatenActionPerformed
        if(KdProp.getText().trim().equals("")||Propinsi.getText().trim().equals("")||Propinsi.getText().trim().equals("PROPINSI")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih propinsi dulu..!!");
            BtnPropinsi.requestFocus();
        }else{
            kabupaten.setPropinsi(KdProp.getText(),Propinsi.getText());
            kabupaten.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            kabupaten.setLocationRelativeTo(internalFrame1);
            kabupaten.setVisible(true);
        }   
    }//GEN-LAST:event_BtnKabupatenActionPerformed

    private void KabupatenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KabupatenKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(Kabupaten.getText().equals("")){
                Kabupaten.setText("KABUPATEN");
            }
            if(Propinsi.getText().equals("PROPINSI")){
                Propinsi.setText("");
            }
            Propinsi.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            if(Kabupaten.getText().equals("")){
                Kabupaten.setText("KABUPATEN");
            }
            if(Kecamatan.getText().equals("KECAMATAN")){
                Kecamatan.setText("");
            }
            Kecamatan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnKabupatenActionPerformed(null);
        }
    }//GEN-LAST:event_KabupatenKeyPressed

    private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        if(Propinsi.getText().equals("")||Propinsi.getText().equals("-")||Propinsi.getText().equals("PROPINSI")){
            JOptionPane.showMessageDialog(null,"Nama Propinsi masih kosong, silahkan pilih propinsi yang ada...!!!");
            BtnPropinsi.requestFocus();
        }else{
            try {
                myObj = new FileReader("./cache/propinsi.iyem");
                root = mapper.readTree(myObj);
                Valid.tabelKosong(tabMode);
                response = root.path("propinsi");
                if(response.isArray()){
                    for(JsonNode list:response){
                        if(list.path("id").asText().toLowerCase().contains(KdProp.getText().toLowerCase())){
                            Propinsi.setText(list.path("nama").asText());
                        }
                    }
                }
                myObj.close();
            } catch (Exception ex) {
                System.out.println("Notifikasi : "+ex);
            }
        }
    }//GEN-LAST:event_BtnCari1ActionPerformed

    private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCari1KeyPressed

    private void KdPropKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPropKeyPressed
        Valid.pindah(evt, KdKab,PeriodeLaporan);
    }//GEN-LAST:event_KdPropKeyPressed

    private void BtnCari2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari2ActionPerformed
        if(KdKec.getText().equals("")||KdKec.getText().equals("0")){
            JOptionPane.showMessageDialog(null,"Kode Kecamatan masih kosong, silahkan pilih kecamatan yang ada...!!!");
            BtnKecamatan.requestFocus();
        }else{
            if(Kelurahan.getText().equals("")||Kelurahan.getText().equals("-")||Kelurahan.getText().equals("KELURAHAN")){
                JOptionPane.showMessageDialog(null,"Nama Kelurahan masih kosong, silahkan pilih kelurahan yang ada...!!!");
                BtnKelurahan.requestFocus();
            }else{
                try {
                    myObj = new FileReader("./cache/kelurahan.iyem");
                    root = mapper.readTree(myObj);
                    Valid.tabelKosong(tabMode);
                    response = root.path("kelurahan");
                    if(response.isArray()){
                        for(JsonNode list:response){
                            if(list.path("id").asText().toLowerCase().contains(KdKel.getText().toLowerCase())){
                                Kelurahan.setText(list.path("nama").asText());
                            }
                        }
                    }
                    myObj.close();
                } catch (Exception ex) {
                    System.out.println("Notifikasi : "+ex);
                }
            }
        }
    }//GEN-LAST:event_BtnCari2ActionPerformed

    private void BtnCari2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCari2KeyPressed

    private void KdKelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdKelKeyPressed
        Valid.pindah(evt, TCari,KdKec);
    }//GEN-LAST:event_KdKelKeyPressed

    private void KdKecKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdKecKeyPressed
        Valid.pindah(evt, KdKel,KdKab);
    }//GEN-LAST:event_KdKecKeyPressed

    private void KdKabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdKabKeyPressed
        Valid.pindah(evt, KdKec,KdProp);
    }//GEN-LAST:event_KdKabKeyPressed

    private void BtnCari3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari3ActionPerformed
        if(KdKab.getText().equals("")||KdKab.getText().equals("0")){
            JOptionPane.showMessageDialog(null,"Kode Kabupaten masih kosong, silahkan pilih kabupaten yang ada...!!!");
            BtnKabupaten.requestFocus();
        }else{
            if(Kecamatan.getText().equals("")||Kecamatan.getText().equals("-")||Kecamatan.getText().equals("KECAMATAN")){
                JOptionPane.showMessageDialog(null,"Nama Kecamatan masih kosong, silahkan pilih kecamatan yang ada...!!!");
                BtnKecamatan.requestFocus();
            }else{
                try {
                    myObj = new FileReader("./cache/kecamatan.iyem");
                    root = mapper.readTree(myObj);
                    Valid.tabelKosong(tabMode);
                    response = root.path("kecamatan");
                    if(response.isArray()){
                        for(JsonNode list:response){
                            if(list.path("id").asText().toLowerCase().contains(KdKec.getText().toLowerCase())){
                                Kecamatan.setText(list.path("nama").asText());
                            }
                        }
                    }
                    myObj.close();
                } catch (Exception ex) {
                    System.out.println("Notifikasi : "+ex);
                }
            }
        }
    }//GEN-LAST:event_BtnCari3ActionPerformed

    private void BtnCari3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCari3KeyPressed

    private void BtnCari4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari4ActionPerformed
        if(KdProp.getText().equals("")||KdProp.getText().equals("0")){
            JOptionPane.showMessageDialog(null,"Kode Propinsi masih kosong, silahkan pilih propinsi yang ada...!!!");
            BtnPropinsi.requestFocus();
        }else{
            if(Kabupaten.getText().equals("")||Kabupaten.getText().equals("-")||Kabupaten.getText().equals("KABUPATEN")){
                JOptionPane.showMessageDialog(null,"Nama Kabupaten masih kosong, silahkan pilih kabupaten yang ada...!!!");
                BtnPropinsi.requestFocus();
            }else{
                try {
                    myObj = new FileReader("./cache/kabupaten.iyem");
                    root = mapper.readTree(myObj);
                    Valid.tabelKosong(tabMode);
                    response = root.path("kabupaten");
                    if(response.isArray()){
                        for(JsonNode list:response){
                            if(list.path("id").asText().toLowerCase().contains(KdKab.getText().toLowerCase())){
                                Kabupaten.setText(list.path("nama").asText());
                            }
                        }
                    }
                    myObj.close();
                } catch (Exception ex) {
                    System.out.println("Notifikasi : "+ex);
                }
            }
        }
    }//GEN-LAST:event_BtnCari4ActionPerformed

    private void BtnCari4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCari4KeyPressed

    private void RujukanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RujukanKeyPressed
        Valid.pindah(evt,TanggalLaporan,KeteranganRujukan);
    }//GEN-LAST:event_RujukanKeyPressed

    private void KeteranganRujukanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganRujukanKeyPressed
        Valid.pindah(evt,Rujukan,Riwayat);
    }//GEN-LAST:event_KeteranganRujukanKeyPressed

    private void TipeDiagnosisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TipeDiagnosisKeyPressed
        Valid.pindah(evt,Lokasi,StatusHIV);
    }//GEN-LAST:event_TipeDiagnosisKeyPressed

    private void LokasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LokasiKeyPressed
        Valid.pindah(evt,Riwayat,TipeDiagnosis);
    }//GEN-LAST:event_LokasiKeyPressed

    private void RiwayatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RiwayatKeyPressed
        Valid.pindah(evt,KeteranganRujukan,Lokasi);
    }//GEN-LAST:event_RiwayatKeyPressed

    private void StatusHIVKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusHIVKeyPressed
        Valid.pindah(evt,TipeDiagnosis,SkoringAnak);
    }//GEN-LAST:event_StatusHIVKeyPressed

    private void SkoringAnakKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkoringAnakKeyPressed
        Valid.pindah(evt,StatusHIV,Skoring5);
    }//GEN-LAST:event_SkoringAnakKeyPressed

    private void Skoring5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Skoring5KeyPressed
        Valid.pindah(evt,SkoringAnak,Skoring6);
    }//GEN-LAST:event_Skoring5KeyPressed

    private void Skoring6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Skoring6KeyPressed
        Valid.pindah(evt,Skoring5,MulaiBerobat);
    }//GEN-LAST:event_Skoring6KeyPressed

    private void MulaiBerobatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MulaiBerobatKeyPressed
        Valid.pindah(evt,Skoring6,PaduanOAT);
    }//GEN-LAST:event_MulaiBerobatKeyPressed

    private void SumberObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SumberObatKeyPressed
        Valid.pindah(evt,PaduanOAT,KeteranganSO);
    }//GEN-LAST:event_SumberObatKeyPressed

    private void KeteranganSOKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganSOKeyPressed
        Valid.pindah(evt,SumberObat,SebelumPengobatanMikroskopis);
    }//GEN-LAST:event_KeteranganSOKeyPressed

    private void SebelumPengobatanMikroskopisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SebelumPengobatanMikroskopisKeyPressed
        Valid.pindah(evt,KeteranganSO,SebelumPengobatanTesCepat);
    }//GEN-LAST:event_SebelumPengobatanMikroskopisKeyPressed

    private void SebelumPengobatanTesCepatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SebelumPengobatanTesCepatKeyPressed
        Valid.pindah(evt,SebelumPengobatanMikroskopis,SebelumPengobatanBiakan);
    }//GEN-LAST:event_SebelumPengobatanTesCepatKeyPressed

    private void SebelumPengobatanBiakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SebelumPengobatanBiakanKeyPressed
        Valid.pindah(evt,SebelumPengobatanTesCepat,PemeriksaanLaboratBulan2Mikroskopis);
    }//GEN-LAST:event_SebelumPengobatanBiakanKeyPressed

    private void PemeriksaanLaboratBulan2MikroskopisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanLaboratBulan2MikroskopisKeyPressed
        Valid.pindah(evt,SebelumPengobatanBiakan,PemeriksaanLaboratBulan2NoReg);
    }//GEN-LAST:event_PemeriksaanLaboratBulan2MikroskopisKeyPressed

    private void PemeriksaanLaboratBulan2NoRegKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanLaboratBulan2NoRegKeyPressed
        Valid.pindah(evt,PemeriksaanLaboratBulan2Mikroskopis,PemeriksaanLaboratBulan3Mikroskopis);
    }//GEN-LAST:event_PemeriksaanLaboratBulan2NoRegKeyPressed

    private void PemeriksaanLaboratBulan3NoRegKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanLaboratBulan3NoRegKeyPressed
        Valid.pindah(evt,PemeriksaanLaboratBulan3Mikroskopis,PemeriksaanLaboratBulan5Mikroskopis);
    }//GEN-LAST:event_PemeriksaanLaboratBulan3NoRegKeyPressed

    private void PemeriksaanLaboratBulan3MikroskopisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanLaboratBulan3MikroskopisKeyPressed
        Valid.pindah(evt,PemeriksaanLaboratBulan2NoReg,PemeriksaanLaboratBulan3NoReg);
    }//GEN-LAST:event_PemeriksaanLaboratBulan3MikroskopisKeyPressed

    private void PemeriksaanLaboratBulan5MikroskopisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanLaboratBulan5MikroskopisKeyPressed
        Valid.pindah(evt,PemeriksaanLaboratBulan3NoReg,PemeriksaanLaboratBulan5NoReg);
    }//GEN-LAST:event_PemeriksaanLaboratBulan5MikroskopisKeyPressed

    private void PemeriksaanLaboratBulan5NoRegKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanLaboratBulan5NoRegKeyPressed
        Valid.pindah(evt,PemeriksaanLaboratBulan5Mikroskopis,PemeriksaanLaboratAkhirPengobatanMikroskopis);
    }//GEN-LAST:event_PemeriksaanLaboratBulan5NoRegKeyPressed

    private void PemeriksaanLaboratAkhirNoRegKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanLaboratAkhirNoRegKeyPressed
        Valid.pindah(evt,PemeriksaanLaboratAkhirPengobatanMikroskopis,AkhirBerobat);
    }//GEN-LAST:event_PemeriksaanLaboratAkhirNoRegKeyPressed

    private void PemeriksaanLaboratAkhirPengobatanMikroskopisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanLaboratAkhirPengobatanMikroskopisKeyPressed
        Valid.pindah(evt,PemeriksaanLaboratAkhirPengobatanMikroskopis,PemeriksaanLaboratAkhirNoReg);
    }//GEN-LAST:event_PemeriksaanLaboratAkhirPengobatanMikroskopisKeyPressed

    private void AkhirBerobatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AkhirBerobatKeyPressed
        Valid.pindah(evt,PemeriksaanLaboratAkhirNoReg,HasilAkhirPengobatan);
    }//GEN-LAST:event_AkhirBerobatKeyPressed

    private void HasilAkhirPengobatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HasilAkhirPengobatanKeyPressed
        Valid.pindah(evt,AkhirBerobat,DianjurkanTesHIV);
    }//GEN-LAST:event_HasilAkhirPengobatanKeyPressed

    private void DianjurkanTesHIVKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DianjurkanTesHIVKeyPressed
        Valid.pindah(evt,HasilAkhirPengobatan,TanggalTesHIV);
    }//GEN-LAST:event_DianjurkanTesHIVKeyPressed

    private void TanggalTesHIVKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalTesHIVKeyPressed
        Valid.pindah(evt,DianjurkanTesHIV,HasilTesHIV);
    }//GEN-LAST:event_TanggalTesHIVKeyPressed

    private void HasilTesHIVKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HasilTesHIVKeyPressed
        Valid.pindah(evt,TanggalTesHIV,PPK);
    }//GEN-LAST:event_HasilTesHIVKeyPressed

    private void PPKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PPKKeyPressed
        Valid.pindah(evt,HasilTesHIV,ART);
    }//GEN-LAST:event_PPKKeyPressed

    private void ARTKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ARTKeyPressed
        Valid.pindah(evt,PPK,TBDM);
    }//GEN-LAST:event_ARTKeyPressed

    private void TBDMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TBDMKeyPressed
        Valid.pindah(evt,ART,TerapiDM);
    }//GEN-LAST:event_TBDMKeyPressed

    private void TerapiDMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TerapiDMKeyPressed
        Valid.pindah(evt,TBDM,PindahRO);
    }//GEN-LAST:event_TerapiDMKeyPressed

    private void PindahROKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PindahROKeyPressed
        Valid.pindah(evt,TerapiDM,Status);
    }//GEN-LAST:event_PindahROKeyPressed

    private void StatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusKeyPressed
        Valid.pindah(evt,PindahRO,FotoToraks);
    }//GEN-LAST:event_StatusKeyPressed

    private void FotoToraksKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FotoToraksKeyPressed
        Valid.pindah(evt,Status,ToraksTidakDilakukan);
    }//GEN-LAST:event_FotoToraksKeyPressed

    private void ToraksTidakDilakukanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ToraksTidakDilakukanKeyPressed
        Valid.pindah(evt,FotoToraks,Keterangan);
    }//GEN-LAST:event_ToraksTidakDilakukanKeyPressed

    private void KeteranganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKeyPressed
        Valid.pindah(evt,ToraksTidakDilakukan,btnBangsal);
    }//GEN-LAST:event_KeteranganKeyPressed

    private void kdpenyakitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpenyakitKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select penyakit.nm_penyakit from penyakit where penyakit.kd_penyakit=?", nmpenyakit,kdpenyakit.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnBangsalActionPerformed(null);
        }else{
            Valid.pindah(evt, kdpenyakit,BtnSimpan);
        }
    }//GEN-LAST:event_kdpenyakitKeyPressed

    private void nmpenyakitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nmpenyakitKeyPressed
        Valid.pindah(evt,kdpenyakit,BtnSimpan);
    }//GEN-LAST:event_nmpenyakitKeyPressed

    private void btnBangsalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBangsalActionPerformed
        penyakit.isCek();
        penyakit.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penyakit.setLocationRelativeTo(internalFrame1);
        penyakit.setVisible(true);
    }//GEN-LAST:event_btnBangsalActionPerformed

    private void btnBangsalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnBangsalKeyPressed
        Valid.pindah(evt,Keterangan,BtnSimpan);
    }//GEN-LAST:event_btnBangsalKeyPressed

    private void PaduanOATKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PaduanOATKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PaduanOATKeyPressed

    private void tbJnsPerawatan1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbJnsPerawatan1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbJnsPerawatan1MouseClicked

    private void tbJnsPerawatan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbJnsPerawatan1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbJnsPerawatan1KeyPressed

    private void TNoRwCariMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TNoRwCariMouseClicked

    }//GEN-LAST:event_TNoRwCariMouseClicked

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgDataTB dialog = new DlgDataTB(new javax.swing.JFrame(), true);
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
    private widget.ComboBox ART;
    private widget.Tanggal AkhirBerobat;
    private widget.TextBox Alamat;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCari1;
    private widget.Button BtnCari2;
    private widget.Button BtnCari3;
    private widget.Button BtnCari4;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKabupaten;
    private widget.Button BtnKecamatan;
    private widget.Button BtnKeluar;
    private widget.Button BtnKelurahan;
    private widget.Button BtnPrint;
    private widget.Button BtnPropinsi;
    private widget.Button BtnSimpan;
    private widget.ComboBox CmbStatusRawatCari;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Tanggal DianjurkanTesHIV;
    private widget.PanelBiasa FormInput;
    private widget.ComboBox FotoToraks;
    private widget.ComboBox HasilAkhirPengobatan;
    private widget.ComboBox HasilTesHIV;
    private widget.TextBox JK;
    private widget.TextBox Kabupaten;
    private widget.TextBox KdKab;
    private widget.TextBox KdKec;
    private widget.TextBox KdKel;
    private widget.TextBox KdProp;
    private widget.TextBox Kecamatan;
    private widget.TextBox Kelurahan;
    private widget.TextBox Keterangan;
    private widget.TextBox KeteranganRujukan;
    private widget.TextBox KeteranganSO;
    private widget.Label LCount;
    private widget.Label LStatusRawatCari;
    private widget.ComboBox Lokasi;
    private widget.Tanggal MulaiBerobat;
    private widget.TextBox NIK;
    private widget.TextBox NoKartu;
    private widget.ComboBox PPK;
    private widget.ComboBox PaduanOAT;
    private widget.TextBox PemeriksaanLaboratAkhirNoReg;
    private widget.ComboBox PemeriksaanLaboratAkhirPengobatanMikroskopis;
    private widget.ComboBox PemeriksaanLaboratBulan2Mikroskopis;
    private widget.TextBox PemeriksaanLaboratBulan2NoReg;
    private widget.ComboBox PemeriksaanLaboratBulan3Mikroskopis;
    private widget.TextBox PemeriksaanLaboratBulan3NoReg;
    private widget.ComboBox PemeriksaanLaboratBulan5Mikroskopis;
    private widget.TextBox PemeriksaanLaboratBulan5NoReg;
    private widget.ComboBox PeriodeLaporan;
    private widget.ComboBox PindahRO;
    private widget.TextBox Propinsi;
    private widget.ComboBox Riwayat;
    private widget.ComboBox Rujukan;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll2;
    private widget.ComboBox SebelumPengobatanBiakan;
    private widget.ComboBox SebelumPengobatanMikroskopis;
    private widget.ComboBox SebelumPengobatanTesCepat;
    private widget.ComboBox Skoring5;
    private widget.ComboBox Skoring6;
    private widget.ComboBox SkoringAnak;
    private widget.ComboBox Status;
    private widget.ComboBox StatusHIV;
    private widget.ComboBox SumberObat;
    private widget.ComboBox TBDM;
    private widget.TextBox TCari;
    private widget.TextBox TNmPasien;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TNoRwCari;
    private javax.swing.JTabbedPane TabRawat;
    private widget.TextBox Tanggal;
    private widget.Tanggal TanggalLaporan;
    private widget.Tanggal TanggalTesHIV;
    private widget.ComboBox TerapiDM;
    private widget.ComboBox TipeDiagnosis;
    private widget.ComboBox ToraksTidakDilakukan;
    private widget.TextBox Umur;
    private widget.Button btnBangsal;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.InternalFrame internalFrame4;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel3;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel36;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel4;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel44;
    private widget.Label jLabel45;
    private widget.Label jLabel46;
    private widget.Label jLabel47;
    private widget.Label jLabel48;
    private widget.Label jLabel49;
    private widget.Label jLabel5;
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
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private widget.TextBox kdpenyakit;
    private widget.TextBox nmpenyakit;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.panelisi panelKataKunci;
    private widget.Table tbJnsPerawatan;
    private widget.Table tbJnsPerawatan1;
    // End of variables declaration//GEN-END:variables

    private static final Color TB_BG = new Color(244, 247, 252);
    private static final Color TB_TEXT = new Color(27, 45, 70);
    private static final Color TB_LABEL = new Color(65, 87, 113);
    private static final Color TB_BORDER = new Color(217, 227, 239);
    private static final Color TB_OUTLINE = new Color(174, 200, 229);
    private static final Color TB_BLUE = new Color(37, 99, 165);
    private static final Color TB_ACTIVE = new Color(233, 243, 255);
    private static final Font TB_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TB_SEMIBOLD = "Segoe UI Semibold".equals(
            new Font("Segoe UI Semibold", Font.PLAIN, 13).getFamily())
            ? new Font("Segoe UI Semibold", Font.PLAIN, 13) : TB_FONT.deriveFont(Font.BOLD);

    private void aturTampilanInputTB() {
        // Gunakan kembali komponen dan event dari form NetBeans untuk tata letak tab input.
        FormInput.removeAll();
        FormInput.setOpaque(false);
        FormInput.setPreferredSize(null);
        FormInput.setLayout(new BoxLayout(FormInput, BoxLayout.Y_AXIS));
        FormInput.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        JPanel area = new JPanel(new BorderLayout(12, 12));
        area.setBackground(TB_BG);
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel pasien = kartuTB("Identitas Pasien");
        tambahBarisTB(pasien, new int[]{16, 10, 38, 17, 9},
                kolomTB("No. Rawat", TNoRw), kolomTB("No. RM", TNoRM),
                kolomTB("Nama pasien", TNmPasien), kolomTB("Jenis kelamin", JK), kolomTB("Umur", Umur));
        Umur.setToolTipText("Umur dalam tahun");
        area.add(pasien, BorderLayout.NORTH);

        JPanel identitas = kartuTB("Identitas & Pelaporan");
        tambahBarisTB(identitas, new int[]{1, 1, 1}, kolomTB("Tanggal lahir", Tanggal),
                kolomTB("No. JKN", NoKartu), kolomTB("NIK", NIK));
        tambahBarisTB(identitas, new int[]{1}, kolomTB("Alamat", Alamat));
        tambahBarisTB(identitas, new int[]{1, 1},
                wilayahTB("Kelurahan", KdKel, Kelurahan, BtnCari2, BtnKelurahan),
                wilayahTB("Kecamatan", KdKec, Kecamatan, BtnCari3, BtnKecamatan));
        tambahBarisTB(identitas, new int[]{1, 1},
                wilayahTB("Kabupaten / Kota", KdKab, Kabupaten, BtnCari4, BtnKabupaten),
                wilayahTB("Provinsi", KdProp, Propinsi, BtnCari1, BtnPropinsi));
        tambahBarisTB(identitas, new int[]{1, 1}, kolomTB("Periode laporan", PeriodeLaporan),
                kolomTB("Tanggal laporan", TanggalLaporan));
        tambahBarisTB(identitas, new int[]{1, 1}, kolomTB("Rujukan", Rujukan),
                kolomTB("Keterangan rujukan", KeteranganRujukan));

        JPanel diagnosis = kartuTB("Diagnosis & Skoring");
        tambahBarisTB(diagnosis, new int[]{2, 1}, kolomTB("Tipe diagnosis", TipeDiagnosis), kolomTB("Lokasi TB", Lokasi));
        tambahBarisTB(diagnosis, new int[]{2, 1}, kolomTB("Riwayat pengobatan", Riwayat), kolomTB("Skoring anak", SkoringAnak));
        tambahBarisTB(diagnosis, new int[]{1, 1}, kolomTB("Skoring 5", Skoring5), kolomTB("Skoring 6", Skoring6));
        btnBangsal.setToolTipText("Pilih penyakit (ICD X)");
        tambahBarisTB(diagnosis, new int[]{1, 4}, kolomTB("ICD X", kdpenyakit),
                kolomTB("Nama penyakit", nmpenyakit, btnBangsal));

        JPanel pengobatan = kartuTB("Pengobatan");
        tambahBarisTB(pengobatan, new int[]{1, 1, 1}, kolomTB("Mulai berobat", MulaiBerobat),
                kolomTB("Paduan OAT", PaduanOAT), kolomTB("Sumber obat", SumberObat));
        tambahBarisTB(pengobatan, new int[]{2, 1}, kolomTB("Keterangan sumber obat", KeteranganSO),
                kolomTB("Status pengobatan", Status));

        JPanel lab = kartuTB("Pemeriksaan Lab");
        tambahBarisTB(lab, new int[]{1}, labelTB("Sebelum pengobatan", 13, true));
        tambahBarisTB(lab, new int[]{1, 1, 1}, kolomTB("Mikroskopis", SebelumPengobatanMikroskopis),
                kolomTB("Tes cepat", SebelumPengobatanTesCepat), kolomTB("Biakan", SebelumPengobatanBiakan));
        tambahBarisTB(lab, new int[]{1}, labelTB("Pemantauan pengobatan", 13, true));
        JPanel kepalaLab = barisTB(new int[]{3, 4, 4}, labelTB("Waktu periksa", 12, true),
                labelTB("Mikroskopis", 12, true), labelTB("No. registrasi lab", 12, true));
        kepalaLab.setOpaque(true);
        kepalaLab.setBackground(TB_ACTIVE);
        kepalaLab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        lab.add(kepalaLab);
        tambahBarisLabTB(lab, "Bulan ke-2", PemeriksaanLaboratBulan2Mikroskopis, PemeriksaanLaboratBulan2NoReg, false);
        tambahBarisLabTB(lab, "Bulan ke-3", PemeriksaanLaboratBulan3Mikroskopis, PemeriksaanLaboratBulan3NoReg, true);
        tambahBarisLabTB(lab, "Bulan ke-5", PemeriksaanLaboratBulan5Mikroskopis, PemeriksaanLaboratBulan5NoReg, false);
        tambahBarisLabTB(lab, "Akhir pengobatan", PemeriksaanLaboratAkhirPengobatanMikroskopis,
                PemeriksaanLaboratAkhirNoReg, true);

        JPanel komorbid = kartuTB("HIV & Komorbid");
        tambahBarisTB(komorbid, new int[]{1, 1, 1}, kolomTB("Status HIV", StatusHIV),
                kolomTB("Dianjurkan tes HIV", DianjurkanTesHIV), kolomTB("Tanggal tes HIV", TanggalTesHIV));
        tambahBarisTB(komorbid, new int[]{2, 1, 1}, kolomTB("Hasil tes HIV", HasilTesHIV),
                kolomTB("PPK", PPK), kolomTB("ART", ART));
        tambahBarisTB(komorbid, new int[]{1, 1}, kolomTB("TB DM", TBDM), kolomTB("Terapi DM", TerapiDM));

        JPanel hasil = kartuTB("Hasil & Keterangan");
        tambahBarisTB(hasil, new int[]{1, 2, 1}, kolomTB("Akhir berobat", AkhirBerobat),
                kolomTB("Hasil akhir pengobatan", HasilAkhirPengobatan), kolomTB("Pindah RO", PindahRO));
        tambahBarisTB(hasil, new int[]{1, 2}, kolomTB("Foto toraks", FotoToraks),
                kolomTB("Alasan toraks tidak dilakukan", ToraksTidakDilakukan));
        tambahBarisTB(hasil, new int[]{1}, kolomTB("Keterangan", Keterangan));

        JPanel[] bagian = {identitas, diagnosis, pengobatan, lab, komorbid, hasil};
        JPanel bagianAwal = new JPanel(new GridBagLayout());
        bagianAwal.setOpaque(false);
        bagianAwal.setAlignmentX(Component.LEFT_ALIGNMENT);
        bagianAwal.add(identitas);
        bagianAwal.add(diagnosis);
        aturDuaKartuTB(bagianAwal, true);
        FormInput.add(bagianAwal);
        for (int idx = 2; idx < bagian.length; idx++) {
            FormInput.add(Box.createVerticalStrut(12));
            FormInput.add(bagian[idx]);
        }
        // Susun dua kartu pertama secara vertikal ketika lebar layar terbatas.
        Scroll1.setBorder(BorderFactory.createEmptyBorder());
        Scroll1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Scroll1.getViewport().setBackground(TB_BG);
        Scroll1.getVerticalScrollBar().setUnitIncrement(24);
        Scroll1.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            private boolean sebaris = true;

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                boolean lebarCukup = Scroll1.getViewport().getWidth() >= 905;
                if (sebaris != lebarCukup) {
                    sebaris = lebarCukup;
                    aturDuaKartuTB(bagianAwal, sebaris);
                    FormInput.invalidate();
                    FormInput.revalidate();
                    FormInput.repaint();
                }
            }
        });

        JPanel navigasi = kartuTB("Bagian Isian");
        navigasi.setPreferredSize(new Dimension(192, navigasi.getPreferredSize().height));
        ButtonGroup pilihan = new ButtonGroup();
        JToggleButton pilihanAwal = null;
        for (JPanel panel : bagian) {
            JToggleButton tombol = new JToggleButton(panel.getName());
            tombol.setFont(TB_SEMIBOLD.deriveFont(12f));
            tombol.setHorizontalAlignment(SwingConstants.LEFT);
            tombol.setOpaque(false);
            tombol.setContentAreaFilled(false);
            tombol.setForeground(TB_LABEL);
            tombol.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
            tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            tombol.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    if (tombol.isSelected() || tombol.getModel().isRollover()) {
                        Graphics2D sketsa = (Graphics2D) g.create();
                        sketsa.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        sketsa.setColor(TB_ACTIVE);
                        sketsa.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                        sketsa.setColor(TB_OUTLINE);
                        sketsa.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
                        sketsa.dispose();
                    }
                    super.paint(g, c);
                }
            });
            tombol.setRolloverEnabled(true);
            tombol.addItemListener(evt -> tombol.setForeground(tombol.isSelected() ? TB_BLUE : TB_LABEL));
            tombol.addActionListener(evt -> {
                int top = SwingUtilities.convertPoint(panel, 0, 0, FormInput).y;
                int batas = Math.max(0, FormInput.getHeight() - Scroll1.getViewport().getHeight());
                Scroll1.getViewport().setViewPosition(new Point(0, Math.min(top, batas)));
            });
            if (pilihanAwal == null) {
                pilihanAwal = tombol;
            }
            pilihan.add(tombol);
            navigasi.add(tombol);
            navigasi.add(Box.createVerticalStrut(5));
        }
        JToggleButton awal = pilihanAwal;
        awal.setSelected(true);
        area.add(navigasi, BorderLayout.WEST);
        area.add(Scroll1, BorderLayout.CENTER);

        internalFrame2.setOpaque(false);
        internalFrame2.remove(Scroll1);
        internalFrame2.add(area, BorderLayout.CENTER);
        aturFooterInputTB(awal);
    }

    private static void aturDuaKartuTB(JPanel wadah, boolean sebaris) {
        GridBagLayout tataLetak = (GridBagLayout) wadah.getLayout();
        GridBagConstraints posisi = new GridBagConstraints();
        posisi.gridx = 0;
        posisi.gridy = 0;
        posisi.weightx = 1;
        posisi.anchor = GridBagConstraints.NORTH;
        posisi.fill = GridBagConstraints.HORIZONTAL;
        tataLetak.setConstraints(wadah.getComponent(0), posisi);
        posisi.gridx = sebaris ? 1 : 0;
        posisi.gridy = sebaris ? 0 : 1;
        posisi.insets = sebaris ? new Insets(0, 12, 0, 0) : new Insets(10, 0, 0, 0);
        tataLetak.setConstraints(wadah.getComponent(1), posisi);
        wadah.setMaximumSize(new Dimension(Integer.MAX_VALUE, wadah.getPreferredSize().height));
        wadah.invalidate();
    }

    private void aturFooterInputTB(JToggleButton pilihanAwal) {
        LayoutManager tataLetakDaftar = panelGlass8.getLayout();
        Dimension ukuranDaftar = jPanel3.getPreferredSize();
        GridBagLayout tataLetakInput = new GridBagLayout();
        Component[] tombol = {BtnBatal, BtnEdit, BtnHapus, BtnPrint, BtnAll, BtnKeluar, BtnSimpan};
        for (int idx = 0; idx < tombol.length; idx++) {
            GridBagConstraints posisi = new GridBagConstraints();
            posisi.gridx = idx;
            posisi.gridy = 0;
            posisi.insets = new Insets(0, idx == 0 ? 12 : 3, 0, 3);
            posisi.anchor = GridBagConstraints.WEST;
            posisi.weightx = tombol[idx] == BtnKeluar ? 1 : 0;
            tataLetakInput.setConstraints(tombol[idx], posisi);
        }
        Dimension ukuranTombolSimpan = BtnSimpan.getPreferredSize();
        Dimension ukuranTombolSemua = BtnAll.getPreferredSize();
        Color warnaSimpan = BtnSimpan.getForeground();
        javax.swing.border.Border borderSimpan = BtnSimpan.getBorder();
        boolean gambarBorderSimpan = BtnSimpan.isBorderPainted();
        Runnable aturFooter = () -> {
            boolean input = TabRawat.getSelectedIndex() == 0;
            panelGlass9.setVisible(!input);
            panelKataKunci.setVisible(!input);
            jPanel3.setPreferredSize(input ? new Dimension(44, 50) : ukuranDaftar);
            panelGlass8.setLayout(input ? tataLetakInput : tataLetakDaftar);
            BtnSimpan.setPreferredSize(input ? new Dimension(112, 32) : ukuranTombolSimpan);
            BtnAll.setPreferredSize(input ? new Dimension(94, 30) : ukuranTombolSemua);
            BtnSimpan.setForeground(input ? TB_BLUE : warnaSimpan);
            BtnSimpan.setBorder(input ? BorderFactory.createLineBorder(TB_OUTLINE, 1, true) : borderSimpan);
            BtnSimpan.setBorderPainted(input || gambarBorderSimpan);
            jPanel3.revalidate();
            jPanel3.repaint();
        };
        Runnable awalForm = () -> {
            pilihanAwal.setSelected(true);
            Scroll1.getViewport().setViewPosition(new Point());
        };
        TabRawat.addChangeListener(evt -> {
            aturFooter.run();
            if (TabRawat.getSelectedIndex() == 0) {
                awalForm.run();
            }
        });
        BtnBatal.addActionListener(evt -> {
            if (TabRawat.getSelectedIndex() == 0) {
                awalForm.run();
            }
        });
        aturFooter.run();
    }

    private static JLabel labelTB(String teks, int ukuran, boolean tebal) {
        JLabel label = new JLabel(teks);
        label.setFont((tebal ? TB_SEMIBOLD : TB_FONT).deriveFont((float) ukuran));
        label.setForeground(tebal ? TB_LABEL : TB_TEXT);
        return label;
    }

    private static JPanel kartuTB(String judul) {
        JPanel kartu = new JPanel();
        kartu.setName(judul);
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBackground(Color.WHITE);
        kartu.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TB_BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel kepala = labelTB(judul, 15, true);
        kepala.setForeground(TB_TEXT);
        kepala.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(kepala);
        kartu.add(Box.createVerticalStrut(10));
        return kartu;
    }

    private static JPanel barisTB(int[] lebar, JComponent... kolom) {
        JPanel baris = new JPanel(new GridBagLayout());
        baris.setOpaque(false);
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int idx = 0; idx < kolom.length; idx++) {
            GridBagConstraints posisi = new GridBagConstraints();
            posisi.gridx = idx;
            posisi.gridy = 0;
            posisi.weightx = lebar[idx];
            posisi.fill = GridBagConstraints.HORIZONTAL;
            posisi.insets = new Insets(0, idx == 0 ? 0 : 8, 0, 0);
            kolom[idx].setPreferredSize(new Dimension(0, kolom[idx].getPreferredSize().height));
            kolom[idx].setMinimumSize(kolom[idx].getPreferredSize());
            baris.add(kolom[idx], posisi);
        }
        return baris;
    }

    private static void tambahBarisTB(JPanel kartu, int[] lebar, JComponent... kolom) {
        JPanel baris = barisTB(lebar, kolom);
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, baris.getPreferredSize().height));
        kartu.add(baris);
        kartu.add(Box.createVerticalStrut(8));
    }

    private JPanel kolomTB(String judul, JComponent kontrol, JButton... aksi) {
        return kolomTB(judul, kontrol, null, aksi);
    }

    private JPanel wilayahTB(String judul, JTextField kode, JTextField nama, JButton isiKode, JButton pilih) {
        isiKode.setToolTipText("Cocokkan kode " + judul.toLowerCase(Locale.ROOT));
        pilih.setToolTipText("Pilih " + judul.toLowerCase(Locale.ROOT));
        kode.getAccessibleContext().setAccessibleName("Kode " + judul);
        return kolomTB(judul, nama, kode, isiKode, pilih);
    }

    private JPanel kolomTB(String judul, JComponent kontrol, JTextField kode, JButton... aksi) {
        JLabel label = labelTB(judul, 12, true);
        label.setLabelFor(kontrol);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        JPanel kolom = new JPanel(new BorderLayout());
        kolom.setOpaque(false);
        kolom.add(label, BorderLayout.NORTH);
        JPanel isian = new JPanel(new BorderLayout(4, 0));
        isian.setOpaque(false);
        isian.add(bidangTB(kontrol), BorderLayout.CENTER);
        if (kode != null) {
            JComponent bidangKode = bidangTB(kode);
            bidangKode.setPreferredSize(new Dimension(45, 33));
            isian.add(bidangKode, BorderLayout.WEST);
        }
        if (aksi.length > 0) {
            JPanel tombol = new JPanel(new GridLayout(1, aksi.length, 3, 0));
            tombol.setOpaque(false);
            for (JButton item : aksi) {
                item.setPreferredSize(new Dimension(25, 33));
                item.getAccessibleContext().setAccessibleName(item.getToolTipText());
                tombol.add(item);
            }
            isian.add(tombol, BorderLayout.EAST);
        }
        kolom.add(isian, BorderLayout.CENTER);
        return kolom;
    }

    private void tambahBarisLabTB(JPanel lab, String waktu, JComponent hasil, JComponent nomor, boolean belang) {
        hasil.getAccessibleContext().setAccessibleName("Mikroskopis " + waktu);
        nomor.getAccessibleContext().setAccessibleName("No. registrasi lab " + waktu);
        JPanel baris = barisTB(new int[]{3, 4, 4}, labelTB(waktu, 13, true), bidangTB(hasil), bidangTB(nomor));
        baris.setOpaque(true);
        baris.setBackground(belang ? TB_BG : Color.WHITE);
        baris.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, baris.getPreferredSize().height));
        lab.add(baris);
    }

    private JComponent bidangTB(JComponent kontrol) {
        Color latar = kontrol instanceof JTextField && !((JTextField) kontrol).isEditable()
                ? TB_BG : Color.WHITE;
        kontrol.setFont(TB_FONT);
        kontrol.setForeground(TB_TEXT);
        kontrol.setBackground(latar);
        kontrol.setOpaque(false);

        if (kontrol instanceof widget.ComboBox) {
            // Pertahankan UI kalender bawaan widget.Tanggal.
            ((JComboBox<?>) kontrol).setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton panah = new JButton() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D sketsa = (Graphics2D) g.create();
                            sketsa.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            sketsa.setColor(TB_BLUE);
                            int x = getWidth() / 2;
                            int y = getHeight() / 2;
                            sketsa.drawPolyline(new int[]{x - 4, x, x + 4}, new int[]{y - 2, y + 2, y - 2}, 3);
                            sketsa.dispose();
                        }
                    };
                    panah.setBorder(BorderFactory.createEmptyBorder());
                    panah.setPreferredSize(new Dimension(22, 20));
                    panah.setFocusable(false);
                    panah.setOpaque(false);
                    return panah;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // Latar dan outline satu piksel digambar oleh panel isian.
                }
            });
            kontrol.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
            kontrol.setBackground(latar);
            kontrol.setOpaque(false);
        } else if (kontrol instanceof widget.Tanggal) {
            kontrol.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 4));
        }

        JPanel bidang = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D sketsa = (Graphics2D) g.create();
                sketsa.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                sketsa.setColor(latar);
                sketsa.fillRoundRect(0, 0, getWidth(), getHeight(), 11, 11);
                sketsa.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                super.paintChildren(g);
                Graphics2D sketsa = (Graphics2D) g.create();
                sketsa.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                sketsa.setColor(kontrol.isFocusOwner() ? TB_BLUE : TB_OUTLINE);
                sketsa.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 11, 11);
                sketsa.dispose();
            }
        };
        bidang.setOpaque(false);
        bidang.setPreferredSize(new Dimension(0, 33));
        bidang.add(kontrol, BorderLayout.CENTER);
        kontrol.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                bidang.repaint();
                if (SwingUtilities.isDescendingFrom(bidang, FormInput)) {
                    bidang.scrollRectToVisible(new Rectangle(0, -20, bidang.getWidth(), bidang.getHeight() + 26));
                }
            }

            @Override
            public void focusLost(FocusEvent evt) {
                bidang.repaint();
            }
        });
        return bidang;
    }

    private void cetakLaporanStandar() {
        if (!muatDaftar(false)) {
            return;
        }
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data dalam hasil filter untuk dicetak.");
            return;
        }

        LocalDate awal = tanggalFilter(DTPCari1.getDate());
        LocalDate akhir = tanggalFilter(DTPCari2.getDate());
        DateTimeFormatter formatTampil = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("periode", awal.format(formatTampil) + " s.d. " + akhir.format(formatTampil));
            param.put("logo", Sequel.cariGambar("select logo from setting"));
            param.put("tanggal1", awal + " 00:00:00");
            param.put("tanggal2", akhir + " 23:59:59");
            param.put("parameter", "%" + TCari.getText().trim() + "%");
            Valid.MyReport("rptDataTB.jasper", "report", "::[ Data Pasien Teridentifikasi TB ]::", param);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void simpanExcel(DefaultTableModel model, String namaSheet) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Excel Data TB");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setSelectedFile(new File(namaSheet.replace(' ', '_') + "_"
                + tanggalFilter(DTPCari1.getDate()) + "_sd_" + tanggalFilter(DTPCari2.getDate()) + ".xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile().getAbsoluteFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }
        boolean gantiFile = file.exists();
        if (gantiFile && JOptionPane.showConfirmDialog(this, "Ganti file " + file.getName() + "?",
                "Simpan Excel", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Path tujuan = file.toPath();
            Path sementara = Files.createTempFile(tujuan.getParent(), "data_tb_", ".tmp");
            try {
                tulisExcel(sementara, model, namaSheet);
                if (gantiFile) {
                    Files.move(sementara, tujuan, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.move(sementara, tujuan);
                }
            } finally {
                Files.deleteIfExists(sementara);
            }
            JOptionPane.showMessageDialog(this, model.getRowCount() + " data berhasil disimpan ke Excel.");
        } catch (IOException | XMLStreamException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan Excel.\n" + e.getMessage(),
                    "Simpan Excel", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private static void tulisExcel(Path file, DefaultTableModel model, String namaSheet)
            throws IOException, XMLStreamException {
        if (model.getRowCount() >= 1048576) {
            throw new IOException("Jumlah baris melebihi kapasitas satu sheet Excel.");
        }

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        String spreadsheet = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
        String relPackage = "http://schemas.openxmlformats.org/package/2006/relationships";
        String relOffice = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        try (ZipOutputStream xlsx = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            tulisBagianExcel(xlsx, "[Content_Types].xml", xml +
                    "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                    "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                    "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                    "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                    "</Types>");
            tulisBagianExcel(xlsx, "_rels/.rels", xml +
                    "<Relationships xmlns=\"" + relPackage + "\"><Relationship Id=\"rId1\" Type=\"" + relOffice +
                    "/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            tulisBagianExcel(xlsx, "xl/workbook.xml", xml + "<workbook xmlns=\"" + spreadsheet +
                    "\" xmlns:r=\"" + relOffice + "\"><bookViews><workbookView/></bookViews><sheets><sheet name=\"" + namaSheet +
                    "\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            tulisBagianExcel(xlsx, "xl/_rels/workbook.xml.rels", xml +
                    "<Relationships xmlns=\"" + relPackage + "\"><Relationship Id=\"rId1\" Type=\"" + relOffice +
                    "/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");

            int jumlahKolom = model.getColumnCount();
            String[] kolomExcel = new String[jumlahKolom];
            xlsx.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            XMLStreamWriter sheet = XMLOutputFactory.newInstance().createXMLStreamWriter(xlsx, "UTF-8");
            sheet.writeStartDocument("UTF-8", "1.0");
            sheet.writeStartElement("worksheet");
            sheet.writeDefaultNamespace(spreadsheet);
            sheet.writeStartElement("sheetViews");
            sheet.writeStartElement("sheetView");
            sheet.writeAttribute("workbookViewId", "0");
            sheet.writeEmptyElement("pane");
            sheet.writeAttribute("ySplit", "1");
            sheet.writeAttribute("topLeftCell", "A2");
            sheet.writeAttribute("activePane", "bottomLeft");
            sheet.writeAttribute("state", "frozen");
            sheet.writeEndElement();
            sheet.writeEndElement();
            sheet.writeStartElement("cols");
            for (int kolom = 0; kolom < jumlahKolom; kolom++) {
                StringBuilder huruf = new StringBuilder();
                for (int nomor = kolom + 1; nomor > 0; nomor = (nomor - 1) / 26) {
                    huruf.insert(0, (char) ('A' + (nomor - 1) % 26));
                }
                kolomExcel[kolom] = huruf.toString();
                String indeks = Integer.toString(kolom + 1);
                sheet.writeEmptyElement("col");
                sheet.writeAttribute("min", indeks);
                sheet.writeAttribute("max", indeks);
                sheet.writeAttribute("width", Integer.toString(Math.max(20, model.getColumnName(kolom).length() + 3)));
                sheet.writeAttribute("customWidth", "1");
            }
            sheet.writeEndElement();
            sheet.writeStartElement("sheetData");
            Pattern kodeExcel = Pattern.compile("(?i)_(?=x[0-9a-f]{4}_)");
            Pattern karakterXmlTidakValid = Pattern.compile("[^\\x09\\x0A\\x0D\\x20-\\x{D7FF}\\x{E000}-\\x{FFFD}\\x{10000}-\\x{10FFFF}]");
            // Gunakan sel teks agar nomor identitas tetap utuh dan isi sel tidak menjadi rumus.
            for (int baris = -1; baris < model.getRowCount(); baris++) {
                String nomorBaris = Integer.toString(baris + 2);
                sheet.writeStartElement("row");
                sheet.writeAttribute("r", nomorBaris);
                for (int kolom = 0; kolom < jumlahKolom; kolom++) {
                    Object nilai = baris < 0 ? model.getColumnName(kolom) : model.getValueAt(baris, kolom);
                    if (nilai != null) {
                        String teks = nilai.toString();
                        if (teks.length() > 32767) {
                            throw new IOException("Isi sel " + kolomExcel[kolom] + nomorBaris + " terlalu panjang untuk Excel.");
                        }
                        sheet.writeStartElement("c");
                        sheet.writeAttribute("r", kolomExcel[kolom] + nomorBaris);
                        sheet.writeAttribute("t", "inlineStr");
                        sheet.writeStartElement("is");
                        sheet.writeStartElement("t");
                        sheet.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "space", "preserve");
                        String teksXml = karakterXmlTidakValid.matcher(teks).replaceAll("");
                        sheet.writeCharacters(kodeExcel.matcher(teksXml).replaceAll("_x005F_"));
                        sheet.writeEndElement();
                        sheet.writeEndElement();
                        sheet.writeEndElement();
                    }
                }
                sheet.writeEndElement();
            }
            sheet.writeEndElement();
            sheet.writeEmptyElement("autoFilter");
            sheet.writeAttribute("ref", "A1:" + kolomExcel[jumlahKolom - 1] + (model.getRowCount() + 1));
            sheet.writeEndElement();
            sheet.writeEndDocument();
            sheet.flush();
            sheet.close();
            xlsx.closeEntry();
        }
    }

    private static void tulisBagianExcel(ZipOutputStream xlsx, String nama, String isi) throws IOException {
        xlsx.putNextEntry(new ZipEntry(nama));
        xlsx.write(isi.getBytes(StandardCharsets.UTF_8));
        xlsx.closeEntry();
    }

    private void tampil() {
        muatDaftar(false);
    }

    private void muatDaftarAktif() {
        int tab = TabRawat.getSelectedIndex();
        if (tab == 1 || tab == 2) {
            muatDaftar(tab == 2);
        }
    }

    private static LocalDate tanggalFilter(Date tanggal) {
        return Instant.ofEpochMilli(tanggal.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String statusRawatFilter() {
        switch (CmbStatusRawatCari.getSelectedIndex()) {
            case 1: return "Ranap";
            case 2: return "Ralan";
            default: return "";
        }
    }

    private boolean muatDaftar(boolean formatDinas) {
        DefaultTableModel model = formatDinas ? tabMode2 : tabMode;
        Valid.tabelKosong(model);
        LCount.setText("0");

        Date pilihanAwal = DTPCari1.getDate();
        Date pilihanAkhir = DTPCari2.getDate();
        if (pilihanAwal == null || pilihanAkhir == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal awal dan tanggal akhir.");
            return false;
        }

        LocalDate awal = tanggalFilter(pilihanAwal);
        LocalDate akhir = tanggalFilter(pilihanAkhir);
        if (awal.isAfter(akhir)) {
            JOptionPane.showMessageDialog(this, "Tanggal awal tidak boleh melebihi tanggal akhir.");
            return false;
        }

        String statusRawat = statusRawatFilter();
        String kataKunci = TCari.getText().trim();
        try {
            if (koneksi == null) {
                throw new SQLException("Koneksi database belum tersedia.");
            }
            if (formatDinas) {
                tampil2(awal, akhir.plusDays(1), statusRawat, kataKunci);
            } else {
                tampil(awal, akhir.plusDays(1), statusRawat, kataKunci);
            }
            LCount.setText(Integer.toString(model.getRowCount()));
            return true;
        } catch (SQLException e) {
            Valid.tabelKosong(model);
            System.out.println("Notifikasi filter Data TB : " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Gagal memuat data TB. Periksa koneksi atau log aplikasi.",
                    "Data TB", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static void isiParameterFilter(PreparedStatement query, LocalDate awal, LocalDate batasAkhir,
            String statusRawat, String kataKunci, int jumlahKolomPencarian) throws SQLException {
        query.setDate(1, java.sql.Date.valueOf(awal));
        query.setDate(2, java.sql.Date.valueOf(batasAkhir));
        query.setString(3, statusRawat);
        query.setString(4, statusRawat);
        query.setString(5, kataKunci);
        String pola = "%" + kataKunci + "%";
        for (int parameter = 6; parameter < 6 + jumlahKolomPencarian; parameter++) {
            query.setString(parameter, pola);
        }
    }

    private void tampil(LocalDate awal, LocalDate batasAkhir, String statusRawat, String kataKunci) throws SQLException {
        String sql =
                "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,"+
                "case when reg_periksa.sttsumur='Th' then reg_periksa.umurdaftar else 0 end as umur,"+
                "pasien.no_peserta,pasien.no_ktp,pasien.alamat,data_tb.id_kelurahan,coalesce(kelurahan.nm_kel,'') as nm_kel,data_tb.id_kecamatan,coalesce(kecamatan.nm_kec,'') as nm_kec,data_tb.kd_kabupaten,coalesce(kabupaten.nm_kab,'') as nm_kab,"+
                "data_tb.id_propinsi,coalesce(propinsi.nm_prop,'') as nm_prop,data_tb.id_periode_laporan,data_tb.tanggal_buat_laporan,data_tb.nama_rujukan,data_tb.sebutkan1,data_tb.klasifikasi_riwayat_pengobatan,"+
                "data_tb.klasifikasi_lokasi_anatomi,data_tb.tipe_diagnosis,data_tb.klasifikasi_status_hiv,data_tb.total_skoring_anak,data_tb.konfirmasiSkoring5,data_tb.konfirmasiSkoring6,"+
                "data_tb.tanggal_mulai_pengobatan,data_tb.paduan_oat,data_tb.sumber_obat,data_tb.sebutkan,data_tb.sebelum_pengobatan_hasil_mikroskopis,data_tb.sebelum_pengobatan_hasil_tes_cepat,"+
                "data_tb.sebelum_pengobatan_hasil_biakan,data_tb.hasil_mikroskopis_bulan_2,data_tb.noreglab_bulan_2,data_tb.hasil_mikroskopis_bulan_3,data_tb.noreglab_bulan_3,"+
                "data_tb.hasil_mikroskopis_bulan_5,data_tb.noreglab_bulan_5,data_tb.akhir_pengobatan_hasil_mikroskopis,data_tb.akhir_pengobatan_noreglab,data_tb.tanggal_hasil_akhir_pengobatan,"+
                "data_tb.hasil_akhir_pengobatan,data_tb.tanggal_dianjurkan_tes,data_tb.tanggal_tes_hiv,data_tb.hasil_tes_hiv,data_tb.ppk,data_tb.art,data_tb.tb_dm,data_tb.terapi_dm,"+
                "data_tb.pindah_ro,data_tb.status_pengobatan,data_tb.foto_toraks,data_tb.toraks_tdk_dilakukan,data_tb.keterangan,"+
                "coalesce(data_tb.kode_icd_x,'') as kode_icd_x,coalesce(penyakit.nm_penyakit,'') as nm_penyakit,data_tb.id_tb_03 "+
                "from data_tb join reg_periksa on reg_periksa.no_rawat=data_tb.no_rawat "+
                "join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                "left join kelurahan on pasien.kd_kel=kelurahan.kd_kel "+
                "left join kecamatan on pasien.kd_kec=kecamatan.kd_kec "+
                "left join kabupaten on pasien.kd_kab=kabupaten.kd_kab "+
                "left join propinsi on pasien.kd_prop=propinsi.kd_prop "+
                "left join penyakit on data_tb.kode_icd_x=penyakit.kd_penyakit "+
                "where data_tb.tanggal_buat_laporan >= ? and data_tb.tanggal_buat_laporan < ? "+
                "and (? = '' or reg_periksa.status_lanjut = ?) "+
                "and (? = '' or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? "+
                "or pasien.nm_pasien like ? or pasien.alamat like ? or pasien.no_ktp like ? "+
                "or kelurahan.nm_kel like ? or kecamatan.nm_kec like ? or kabupaten.nm_kab like ? "+
                "or propinsi.nm_prop like ? or data_tb.nama_rujukan like ? "+
                "or data_tb.klasifikasi_riwayat_pengobatan like ? or data_tb.klasifikasi_lokasi_anatomi like ? "+
                "or data_tb.tipe_diagnosis like ?) order by data_tb.tanggal_buat_laporan";
        try (PreparedStatement query = koneksi.prepareStatement(sql)) {
            isiParameterFilter(query, awal, batasAkhir, statusRawat, kataKunci, 13);
            try (ResultSet rs = query.executeQuery()) {
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("jk"),rs.getString("tgl_lahir"),
                        rs.getString("umur")+"Th",rs.getString("no_peserta"),rs.getString("no_ktp"),rs.getString("alamat"),rs.getString("id_kelurahan"),
                        rs.getString("nm_kel"),rs.getString("id_kecamatan"),rs.getString("nm_kec"),rs.getString("kd_kabupaten"),rs.getString("nm_kab"),
                        rs.getString("id_propinsi"),rs.getString("nm_prop"),rs.getString("id_periode_laporan"),rs.getString("tanggal_buat_laporan"),
                        rs.getString("nama_rujukan"),rs.getString("sebutkan1"),rs.getString("klasifikasi_riwayat_pengobatan"),rs.getString("klasifikasi_lokasi_anatomi"),
                        rs.getString("tipe_diagnosis"),rs.getString("klasifikasi_status_hiv"),rs.getString("total_skoring_anak"),rs.getString("konfirmasiSkoring5"),
                        rs.getString("konfirmasiSkoring6"),rs.getString("tanggal_mulai_pengobatan"),rs.getString("paduan_oat"),rs.getString("sumber_obat"),
                        rs.getString("sebutkan"),rs.getString("sebelum_pengobatan_hasil_mikroskopis"),rs.getString("sebelum_pengobatan_hasil_tes_cepat"),
                        rs.getString("sebelum_pengobatan_hasil_biakan"),rs.getString("hasil_mikroskopis_bulan_2"),rs.getString("noreglab_bulan_2"),
                        rs.getString("hasil_mikroskopis_bulan_3"),rs.getString("noreglab_bulan_3"),rs.getString("hasil_mikroskopis_bulan_5"),
                        rs.getString("noreglab_bulan_5"),rs.getString("akhir_pengobatan_hasil_mikroskopis"),rs.getString("akhir_pengobatan_noreglab"),
                        rs.getString("tanggal_hasil_akhir_pengobatan"),rs.getString("hasil_akhir_pengobatan"),rs.getString("tanggal_dianjurkan_tes"),
                        rs.getString("tanggal_tes_hiv"),rs.getString("hasil_tes_hiv"),rs.getString("ppk"),rs.getString("art"),rs.getString("tb_dm"),
                        rs.getString("terapi_dm"),rs.getString("pindah_ro"),rs.getString("status_pengobatan"),rs.getString("foto_toraks"),
                        rs.getString("toraks_tdk_dilakukan"),rs.getString("keterangan"),rs.getString("kode_icd_x"),rs.getString("nm_penyakit"),rs.getString("id_tb_03")
                    });
                }
            }
        }
    }

    private void tampil2(LocalDate awal, LocalDate batasAkhir, String statusRawat, String kataKunci) throws SQLException {
        String tanggalRegister = "CASE WHEN rp.status_lanjut='Ranap' THEN " +
                "COALESCE((SELECT ki.tgl_masuk FROM kamar_inap ki WHERE ki.no_rawat = rp.no_rawat " +
                "ORDER BY ki.tgl_masuk ASC, ki.jam_masuk ASC LIMIT 1), rp.tgl_registrasi) " +
                "ELSE rp.tgl_registrasi END";
        String sql =
            "SELECT " +
            "  'RS. Akademis Jaury Jusuf Putera' AS nama_faskes, " +

            // Asal Poli: poli utk Ralan, bangsal utk Ranap (fallback aman)
            "  CASE WHEN rp.status_lanjut='Ranap' THEN " +
            "       COALESCE( (SELECT b.nm_bangsal " +
            "                  FROM kamar_inap ki " +
            "                  JOIN kamar k ON k.kd_kamar = ki.kd_kamar " +
            "                  JOIN bangsal b ON b.kd_bangsal = k.kd_bangsal " +
            "                  WHERE ki.no_rawat = rp.no_rawat " +
            "                  ORDER BY ki.tgl_masuk ASC, ki.jam_masuk ASC LIMIT 1), " +
            "                COALESCE(pol.nm_poli,'')) " +
            "       ELSE COALESCE(pol.nm_poli,'') " +
            "  END AS asal_poli, " +

            "  " + tanggalRegister + " AS tgl_registrasi, " +

            "  COALESCE(p.no_peserta,'') AS no_kartu, " +
            "  COALESCE(p.no_ktp,'')     AS no_ktp, " +
            "  COALESCE(p.nm_pasien,'')  AS nm_pasien, " +

            // ===== Hasil Lab TCM/BTA (diringkas; sesuaikan keyword jika perlu) =====
            "  COALESCE(( " +
            "    SELECT GROUP_CONCAT(CONCAT(TRIM(COALESCE(tl.Pemeriksaan,'')), ': ', TRIM(COALESCE(dpl.nilai,''))) " +
            "           ORDER BY pl.tgl_periksa DESC, pl.jam DESC SEPARATOR '; ' ) " +
            "    FROM periksa_lab pl " +
            "    JOIN detail_periksa_lab dpl ON dpl.no_rawat=pl.no_rawat AND dpl.kd_jenis_prw=pl.kd_jenis_prw " +
            "    LEFT JOIN template_laboratorium tl ON tl.id_template=dpl.id_template " +
            "    WHERE pl.no_rawat = rp.no_rawat " +
            "      AND ( UPPER(COALESCE(tl.Pemeriksaan,'')) LIKE '%TCM%' " +
            "         OR UPPER(COALESCE(tl.Pemeriksaan,'')) LIKE '%GENEXPERT%' " +
            "         OR UPPER(COALESCE(tl.Pemeriksaan,'')) LIKE '%BTA%' " +
            "         OR UPPER(COALESCE(tl.Pemeriksaan,'')) LIKE '%MTB%' " +
            "         OR UPPER(COALESCE(tl.Pemeriksaan,'')) LIKE '%RIF%' ) " +
            "  ), '') AS hasil_tcm_bta, " +

            // ===== Hasil Radiologi terbaru =====
            "  COALESCE(( " +
            "    SELECT hr.hasil FROM hasil_radiologi hr " +
            "    WHERE hr.no_rawat = rp.no_rawat " +
            "    ORDER BY hr.tgl_periksa DESC, hr.jam DESC " +
            "    LIMIT 1 " +
            "  ), '') AS hasil_radiologi, " +

            // ===== Diagnosa Dokter → TBC / Bukan TBC =====
            "  CASE WHEN EXISTS ( " +
            "    SELECT 1 FROM diagnosa_pasien dp " +
            "    JOIN penyakit py ON py.kd_penyakit = dp.kd_penyakit " +
            "    WHERE dp.no_rawat = rp.no_rawat " +
            "      AND ( py.kd_penyakit LIKE 'A15%' OR py.kd_penyakit LIKE 'A16%' " +
            "         OR py.kd_penyakit LIKE 'A17%' OR py.kd_penyakit LIKE 'A18%' " +
            "         OR py.kd_penyakit LIKE 'A19%' OR UPPER(py.nm_penyakit) LIKE '%TUBER%' ) " +
            "  ) THEN 'TBC' ELSE 'Bukan TBC' END AS diag_tb, " +

            // ===== SITB label (concat No.SITB jika 'Sudah Ada') =====
            "  CASE WHEN tb.sitb='Sudah Ada' " +
            "       THEN CONCAT('Sudah Ada - No. SITB ', COALESCE(tb.no_sitb,'')) " +
            "       ELSE COALESCE(tb.sitb,'') END AS sitb, " +

            // ===== Keterangan (tindak lanjut) =====
            "  COALESCE(tb.tindak_lanjut,'') AS tindak_lanjut " +

            "FROM terindikasi_tb tb " +
            "JOIN reg_periksa   rp   ON rp.no_rawat = tb.no_rawat " +
            "LEFT JOIN poliklinik pol ON pol.kd_poli = rp.kd_poli " +
            "LEFT JOIN pasien     p   ON p.no_rkm_medis = rp.no_rkm_medis " +
            "WHERE tb.keterangan = 'Terindikasi TB' " +
            "AND (" + tanggalRegister + ") >= ? AND (" + tanggalRegister + ") < ? " +
            "AND (? = '' OR rp.status_lanjut = ?) " +
            "AND (? = '' OR rp.no_rawat LIKE ? OR p.no_rkm_medis LIKE ? OR p.nm_pasien LIKE ? " +
            "OR p.no_ktp LIKE ? OR p.no_peserta LIKE ? OR tb.no_sitb LIKE ?) " +
            "ORDER BY tgl_registrasi DESC, rp.no_rawat DESC";

        try (PreparedStatement query = koneksi.prepareStatement(sql)) {
            isiParameterFilter(query, awal, batasAkhir, statusRawat, kataKunci, 6);
            try (ResultSet rs = query.executeQuery()) {
                while (rs.next()) {
                    tabMode2.addRow(new Object[]{
                        rs.getString("nama_faskes"),     // Nama Faskes
                        rs.getString("asal_poli"),       // Asal Poli / Bangsal (Ranap)
                        rs.getString("tgl_registrasi"),  // Tgl. Register / tgl_masuk (Ranap)
                        rs.getString("no_kartu"),        // No. Kartu
                        rs.getString("no_ktp"),          // No. KTP
                        rs.getString("nm_pasien"),       // Nama Pasien
                        rs.getString("hasil_tcm_bta"),   // Hasil Lab [TCM-BTA]
                        rs.getString("hasil_radiologi"), // Hasil Radiologi
                        rs.getString("diag_tb"),         // Diagnosa Dokter [TBC/Bukan TBC]
                        rs.getString("sitb"),            // SITB (dengan No.SITB jika Sudah Ada)
                        rs.getString("tindak_lanjut")    // Keterangan (tindak_lanjut)
                    });
                }
            }
        }
    }

    public void emptTeks() {
        TNoRw.setText("");
        TNoRM.setText("");
        TNmPasien.setText("");
        JK.setText("");
        Tanggal.setText("");
        Umur.setText("");
        NoKartu.setText("");
        NIK.setText("");
        Alamat.setText("");
        KdKel.setText("");
        Kelurahan.setText("KELURAHAN");
        KdKec.setText("");
        Kecamatan.setText("KECAMATAN");
        KdKab.setText("");
        Kabupaten.setText("KABUPATEN");
        KdProp.setText("");
        Propinsi.setText("PROPINSI");
        KeteranganRujukan.setText("");
        KeteranganSO.setText("");
        PemeriksaanLaboratAkhirNoReg.setText("");
        PemeriksaanLaboratBulan2NoReg.setText("");
        PemeriksaanLaboratBulan3NoReg.setText("");
        PemeriksaanLaboratBulan5NoReg.setText("");
        Keterangan.setText("");
        kdpenyakit.setText("");
        nmpenyakit.setText("");
        PeriodeLaporan.requestFocus();
    }

    private void getData() {
        if(tbJnsPerawatan.getSelectedRow()!= -1){
            TNoRw.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),0).toString());
            TNoRM.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),1).toString());
            TNmPasien.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),2).toString());
            JK.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),3).toString().replaceAll("L","Laki-Laki").replaceAll("P","Perempuan"));
            Tanggal.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),4).toString());
            Umur.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),5).toString().replaceAll("Th",""));
            NoKartu.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),6).toString());
            NIK.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),7).toString());
            Alamat.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),8).toString());
            KdKel.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),9).toString());
            Kelurahan.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),10).toString());
            KdKec.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),11).toString());
            Kecamatan.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),12).toString());
            KdKab.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),13).toString());
            Kabupaten.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),14).toString());
            KdProp.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),15).toString());
            Propinsi.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),16).toString());
            PeriodeLaporan.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),17).toString());
            Valid.SetTgl2(TanggalLaporan,tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),18).toString());
            Rujukan.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),19).toString());
            KeteranganRujukan.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),20).toString());
            Riwayat.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),21).toString());
            Lokasi.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),22).toString());
            TipeDiagnosis.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),23).toString());
            StatusHIV.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),24).toString());
            SkoringAnak.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),25).toString());
            Skoring5.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),26).toString());
            Skoring6.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),27).toString());
            Valid.SetTgl(MulaiBerobat,tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),28).toString());
            PaduanOAT.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),29).toString());
            SumberObat.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),30).toString());
            KeteranganSO.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),31).toString());
            SebelumPengobatanMikroskopis.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),32).toString());
            SebelumPengobatanTesCepat.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),33).toString());
            SebelumPengobatanBiakan.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),34).toString());
            PemeriksaanLaboratBulan2Mikroskopis.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),35).toString());
            PemeriksaanLaboratBulan2NoReg.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),36).toString());
            PemeriksaanLaboratBulan3Mikroskopis.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),37).toString());
            PemeriksaanLaboratBulan3NoReg.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),38).toString());
            PemeriksaanLaboratBulan5Mikroskopis.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),39).toString());
            PemeriksaanLaboratBulan5NoReg.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),40).toString());
            PemeriksaanLaboratAkhirPengobatanMikroskopis.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),41).toString());
            PemeriksaanLaboratAkhirNoReg.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),42).toString());
            Valid.SetTgl(AkhirBerobat,tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),43).toString());
            HasilAkhirPengobatan.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),44).toString());
            Valid.SetTgl(DianjurkanTesHIV,tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),45).toString());
            Valid.SetTgl(TanggalTesHIV,tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),46).toString());
            HasilTesHIV.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),47).toString());
            PPK.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),48).toString());
            ART.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),49).toString());
            TBDM.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),50).toString());
            TerapiDM.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),51).toString());
            PindahRO.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),52).toString());
            Status.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),53).toString());
            FotoToraks.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),54).toString());
            ToraksTidakDilakukan.setSelectedItem(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),55).toString());
            Keterangan.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),56).toString());
            kdpenyakit.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),57).toString());
            nmpenyakit.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),58).toString());
        }
    }
    
    public void isCek(){
        TabRawat.setSelectedIndex(1);
        tampil();
        BtnSimpan.setEnabled(akses.getkemenkes_sitt());
        BtnHapus.setEnabled(akses.getkemenkes_sitt());
        BtnEdit.setEnabled(akses.getkemenkes_sitt());
        BtnPrint.setEnabled(akses.getkemenkes_sitt());
    }
    
    public void setNoRM(String norawat){
        TabRawat.setSelectedIndex(0);
        TNoRw.setText(norawat);
        try {
            ps=koneksi.prepareStatement(
                "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,reg_periksa.umurdaftar, "+
                "reg_periksa.sttsumur,pasien.no_peserta,pasien.no_ktp,pasien.alamat,pasien.kd_kel,kelurahan.nm_kel,"+
                "pasien.kd_kec,kecamatan.nm_kec,pasien.kd_kab,kabupaten.nm_kab,pasien.kd_prop,propinsi.nm_prop "+
                "from pasien inner join reg_periksa inner join kelurahan inner join kecamatan inner join kabupaten "+
                "inner join propinsi on pasien.no_rkm_medis=reg_periksa.no_rkm_medis and pasien.kd_kel=kelurahan.kd_kel "+
                "and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_prop=propinsi.kd_prop "+
                "where reg_periksa.no_rawat=?"
            );
            try {
                ps.setString(1, norawat);
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TNmPasien.setText(rs.getString("nm_pasien"));
                    JK.setText(rs.getString("jk").replaceAll("P","Perempuan").replaceAll("L","Laki-Laki"));
                    Tanggal.setText(rs.getString("tgl_lahir"));
                    if(rs.getString("sttsumur").equals("Th")){
                        Umur.setText(rs.getString("umurdaftar"));
                    }else{
                        Umur.setText("0");
                    }
                    NoKartu.setText(rs.getString("no_peserta"));
                    NIK.setText(rs.getString("no_ktp"));
                    Alamat.setText(rs.getString("alamat"));    
                    KdKel.setText(rs.getString("kd_kel"));  
                    Kelurahan.setText(rs.getString("nm_kel"));  
                    KdKec.setText(rs.getString("kd_kec"));    
                    Kecamatan.setText(rs.getString("nm_kec"));  
                    KdKab.setText(rs.getString("kd_kab"));    
                    Kabupaten.setText(rs.getString("nm_kab"));  
                    KdProp.setText(rs.getString("kd_prop"));    
                    Propinsi.setText(rs.getString("nm_prop"));    
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
