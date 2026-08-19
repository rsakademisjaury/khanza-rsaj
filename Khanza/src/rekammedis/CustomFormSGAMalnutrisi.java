/*
 * By Mas Elkhanza
 */


package rekammedis;

//import freehand.DlgTTDCatatanProgramTindakanRehabilitasi;
import fungsi.MetadataBerkas;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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


/**
 *
 * @author perpustakaan
 */
public final class CustomFormSGAMalnutrisi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private StringBuilder htmlContent;
    private String finger="",FileName = "", kodeberkas = "";


    
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public CustomFormSGAMalnutrisi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        pasangListenerOtomatis();
        
        tabMode=new DefaultTableModel(null,new Object[]{
            
            "No.Rawat",
            "No.RM",
            "Nama Pasien",
            "Tgl.Lahir",
            "J.K.",
            "Kode Dokter",
            "Nama Dokter",
            "Tanggal",
            "Kamar",
            "Diagnosa Medis",
            "Riwayat Asupan 1",
            "Skor Riwayat Asupan 1",
            "Lama Asupan Menurun",
            "Riwayat Asupan 2",
            "Skor Riwayat Asupan 2",
            "Berat Badan 3",
            "Skor Berat Badan 3",
            "BB Saat ini",
            "Besarnya Kehilangan",
            "Berat Badan 4",
            "Skor Berat Badan 4",
            "Perubahan BB",
            "Berat Badan 5",
            "Skor Berat Badan 5",
            "Gejala 6",
            "Skor Gejala 6",
            "Gejala 7",
            "Skor Gejala 7",
            "Gejala 8",
            "Skor Gejala 8",
            "Gejala 9",
            "Skor Gejala 9",
            "Gejala 10",
            "Skor Gejala 10",
            "Gejala 11",
            "Skor Gejala 11",
            "Gejala 12",
            "Skor Gejala 12",
            "Gejala 13",
            "Skor Gejala 13",
            "Gejala 14",
            "Skor Gejala 14",
            "Gejala 15",
            "Skor Gejala 15",
            "Kapasitas Fungsional 16",
            "Skor Kapasitas Fungsional 16",
            "Kapasitas Fungsional 17",
            "Skor Kapasitas Fungsional 17",
            "Penyakit 18",
            "Skor Penyakit 18",
            "Pemeriksaan Fisik 19",
            "Skor Pemeriksaan Fisik 19",
            "Pemeriksaan Fisik 20",
            "Skor Pemeriksaan Fisik 20",
            "Pemeriksaan Fisik 21",
            "Skor Pemeriksaan Fisik 21",
            "Pemeriksaan Fisik 22",
            "Skor Pemeriksaan Fisik 22",
            "Pemeriksaan Fisik 23",
            "Skor Pemeriksaan Fisik 23",
            "Pemeriksaan Fisik 24",
            "Skor Pemeriksaan Fisik 24",
            "Pemeriksaan Fisik 25",
            "Skor Pemeriksaan Fisik 25",
            "Pemeriksaan Fisik 26",
            "Skor Pemeriksaan Fisik 26",
            "Pemeriksaan Fisik 27",
            "Skor Pemeriksaan Fisik 27",
            "Pemeriksaan Fisik 28",
            "Skor Pemeriksaan Fisik 28",
            "Pemeriksaan Fisik 29",
            "Skor Pemeriksaan Fisik 29",
            "Pemeriksaan Fisik 30",
            "Skor Pemeriksaan Fisik 30",
            "Pemeriksaan Fisik 31",
            "Skor Pemeriksaan Fisik 31",
            "Laboratorium 32",
            "Skor Laboratorium 32",
            "Laboratorium 33",
            "Skor Laboratorium 33",
            "Faktor Kontribusi 34",
            "Skor Faktor Kontribusi 34",
            "Faktor Kontribusi 34",
            "Skor Faktor Kontribusi 34",
            "Skor SGA",
            "Catatan"


            
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 86; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){column.setPreferredWidth(105);
            }else if(i==1){column.setPreferredWidth(70);
            }else if(i==2){column.setPreferredWidth(150);
            }else if(i==3){column.setPreferredWidth(65);
            }else if(i==4){column.setPreferredWidth(65);
            }else if(i==5){column.setPreferredWidth(80);
            }else if(i==6){column.setPreferredWidth(150);
            }else if(i==7){column.setPreferredWidth(115);
            }else if(i==8){column.setPreferredWidth(80);
            }else if(i==9){column.setPreferredWidth(100);
            }else if(i==10){column.setPreferredWidth(127);
            }else if(i==11){column.setPreferredWidth(127);
            }else if(i==12){column.setPreferredWidth(250);
            }else if(i==13){column.setPreferredWidth(110);
            }else if(i==14){column.setPreferredWidth(110);
            }else if(i==15){column.setPreferredWidth(110);
            }else if(i==16){column.setPreferredWidth(100);
            }else if(i==17){column.setPreferredWidth(127);
            }else if(i==18){column.setPreferredWidth(127);
            }else if(i==19){column.setPreferredWidth(250);
            }else if(i==20){column.setPreferredWidth(110);
            }else if(i==21){column.setPreferredWidth(110);
            }else if(i==22){column.setPreferredWidth(110);
            }else if(i==23){column.setPreferredWidth(100);
            }else if(i==24){column.setPreferredWidth(127);
            }else if(i==25){column.setPreferredWidth(127);
            }else if(i==26){column.setPreferredWidth(250);
            }else if(i==27){column.setPreferredWidth(110);
            }else if(i==28){column.setPreferredWidth(110);
            }else if(i==29){column.setPreferredWidth(110);
            }else if(i==30){column.setPreferredWidth(100);
            }else if(i==31){column.setPreferredWidth(127);
            }else if(i==32){column.setPreferredWidth(127);
            }else if(i==33){column.setPreferredWidth(250);
            }else if(i==34){column.setPreferredWidth(110);
            }else if(i==35){column.setPreferredWidth(110);
            }else if(i==36){column.setPreferredWidth(110);
            }else if(i==37){column.setPreferredWidth(100);
            }else if(i==38){column.setPreferredWidth(127);
            }else if(i==39){column.setPreferredWidth(127);
            }else if(i==40){column.setPreferredWidth(250);
            }else if(i==41){column.setPreferredWidth(110);
            }else if(i==42){column.setPreferredWidth(110);
            }else if(i==43){column.setPreferredWidth(110);
            }else if(i==44){column.setPreferredWidth(100);
            }else if(i==45){column.setPreferredWidth(127);
            }else if(i==46){column.setPreferredWidth(127);
            }else if(i==47){column.setPreferredWidth(250);
            }else if(i==48){column.setPreferredWidth(110);
            }else if(i==49){column.setPreferredWidth(110);
            }else if(i==50){column.setPreferredWidth(110);
            }else if(i==51){column.setPreferredWidth(100);
            }else if(i==52){column.setPreferredWidth(110);
            }else if(i==53){column.setPreferredWidth(110);
            }else if(i==54){column.setPreferredWidth(110);
            }else if(i==55){column.setPreferredWidth(100);
            }else if(i==56){column.setPreferredWidth(127);
            }else if(i==57){column.setPreferredWidth(127);
            }else if(i==58){column.setPreferredWidth(250);
            }else if(i==59){column.setPreferredWidth(110);
            }else if(i==60){column.setPreferredWidth(110);
            }else if(i==61){column.setPreferredWidth(110);
            }else if(i==62){column.setPreferredWidth(100);
            }else if(i==63){column.setPreferredWidth(110);
            }else if(i==64){column.setPreferredWidth(110);
            }else if(i==65){column.setPreferredWidth(110);
            }else if(i==66){column.setPreferredWidth(100);
            }else if(i==67){column.setPreferredWidth(127);
            }else if(i==68){column.setPreferredWidth(127);
            }else if(i==69){column.setPreferredWidth(250);
            }else if(i==70){column.setPreferredWidth(110);
            }else if(i==71){column.setPreferredWidth(110);
            }else if(i==72){column.setPreferredWidth(110);
            }else if(i==73){column.setPreferredWidth(100);
            }else if(i==74){column.setPreferredWidth(110);
            }else if(i==75){column.setPreferredWidth(110);
            }else if(i==76){column.setPreferredWidth(110);
            }else if(i==77){column.setPreferredWidth(100);
            }else if(i==78){column.setPreferredWidth(127);
            }else if(i==79){column.setPreferredWidth(127);
            }else if(i==80){column.setPreferredWidth(250);
            }else if(i==81){column.setPreferredWidth(110);
            }else if(i==82){column.setPreferredWidth(110);
            }else if(i==83){column.setPreferredWidth(110);
            }else if(i==84){column.setPreferredWidth(60);
            }else if(i==85){column.setPreferredWidth(60);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        DiagnosisMedis.setDocument(new batasInput((int)3000).getKata(DiagnosisMedis));
        LamaAsupanMenurun.setDocument(new batasInput((int)1000).getKata(LamaAsupanMenurun));
        BBBiasanya.setDocument(new batasInput((int)50).getKata(BBBiasanya));
        BBSaatIni.setDocument(new batasInput((int)50).getKata(BBSaatIni));
        BesarnyaKehilangan.setDocument(new batasInput((int)50).getKata(BesarnyaKehilangan));
        PerubahanBB.setDocument(new batasInput((int)50).getKata(PerubahanBB));
        SkorSGA.setDocument(new batasInput((int)2).getKata(SkorSGA));
        Catatan.setDocument(new batasInput((int)3000).getKata(Catatan));               
                
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
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){
                    KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    KdDokter.requestFocus();
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

        LoadHTML = new widget.editorpane();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        buttonGroup9 = new javax.swing.ButtonGroup();
        buttonGroup10 = new javax.swing.ButtonGroup();
        buttonGroup11 = new javax.swing.ButtonGroup();
        buttonGroup12 = new javax.swing.ButtonGroup();
        buttonGroup13 = new javax.swing.ButtonGroup();
        buttonGroup14 = new javax.swing.ButtonGroup();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnCetak = new widget.Button();
        BtnAll = new widget.Button();
        BtnUpload = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        label14 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        Jk = new widget.TextBox();
        jLabel10 = new widget.Label();
        jLabel11 = new widget.Label();
        label11 = new widget.Label();
        TglAsuhan = new widget.Tanggal();
        NmPoli = new widget.TextBox();
        label15 = new widget.Label();
        KdPoli = new widget.TextBox();
        label16 = new widget.Label();
        BeratBadan4 = new widget.ComboBox();
        SkorBeratBadan4 = new widget.TextBox();
        label18 = new widget.Label();
        RiwayatAsupan1 = new widget.ComboBox();
        SkorRiwayatAsupan1 = new widget.TextBox();
        label19 = new widget.Label();
        label20 = new widget.Label();
        RiwayatAsupan2 = new widget.ComboBox();
        label21 = new widget.Label();
        label22 = new widget.Label();
        BeratBadan3 = new widget.ComboBox();
        SkorRiwayatAsupan2 = new widget.TextBox();
        SkorBeratBadan3 = new widget.TextBox();
        label23 = new widget.Label();
        label24 = new widget.Label();
        BeratBadan5 = new widget.ComboBox();
        SkorBeratBadan5 = new widget.TextBox();
        label25 = new widget.Label();
        label26 = new widget.Label();
        Gejala6 = new widget.ComboBox();
        SkorGejala6 = new widget.TextBox();
        label27 = new widget.Label();
        Gejala7 = new widget.ComboBox();
        SkorGejala7 = new widget.TextBox();
        label28 = new widget.Label();
        Gejala8 = new widget.ComboBox();
        SkorGejala8 = new widget.TextBox();
        label29 = new widget.Label();
        Gejala9 = new widget.ComboBox();
        SkorGejala9 = new widget.TextBox();
        label30 = new widget.Label();
        Gejala10 = new widget.ComboBox();
        SkorGejala10 = new widget.TextBox();
        label31 = new widget.Label();
        Gejala11 = new widget.ComboBox();
        SkorGejala11 = new widget.TextBox();
        label32 = new widget.Label();
        Gejala12 = new widget.ComboBox();
        SkorGejala12 = new widget.TextBox();
        label33 = new widget.Label();
        Gejala13 = new widget.ComboBox();
        SkorGejala13 = new widget.TextBox();
        label34 = new widget.Label();
        Gejala14 = new widget.ComboBox();
        SkorGejala14 = new widget.TextBox();
        label35 = new widget.Label();
        Gejala15 = new widget.ComboBox();
        SkorGejala15 = new widget.TextBox();
        label36 = new widget.Label();
        label37 = new widget.Label();
        KapasitasFungsional16 = new widget.ComboBox();
        SkorKapasitasFungsional16 = new widget.TextBox();
        label38 = new widget.Label();
        KapasitasFungsional17 = new widget.ComboBox();
        SkorKapasitasFungsional17 = new widget.TextBox();
        label39 = new widget.Label();
        label40 = new widget.Label();
        Penyakit18 = new widget.ComboBox();
        SkorPenyakit18 = new widget.TextBox();
        label41 = new widget.Label();
        label42 = new widget.Label();
        PemFis19 = new widget.ComboBox();
        SkorPemFis19 = new widget.TextBox();
        label43 = new widget.Label();
        PemFis20 = new widget.ComboBox();
        SkorPemFis20 = new widget.TextBox();
        label44 = new widget.Label();
        PemFis21 = new widget.ComboBox();
        SkorPemFis21 = new widget.TextBox();
        label45 = new widget.Label();
        label46 = new widget.Label();
        PemFis22 = new widget.ComboBox();
        SkorPemFis22 = new widget.TextBox();
        label47 = new widget.Label();
        PemFis23 = new widget.ComboBox();
        SkorPemFis23 = new widget.TextBox();
        label48 = new widget.Label();
        PemFis24 = new widget.ComboBox();
        SkorPemFis24 = new widget.TextBox();
        label49 = new widget.Label();
        PemFis25 = new widget.ComboBox();
        SkorPemFis25 = new widget.TextBox();
        label50 = new widget.Label();
        PemFis26 = new widget.ComboBox();
        SkorPemFis26 = new widget.TextBox();
        label51 = new widget.Label();
        PemFis27 = new widget.ComboBox();
        SkorPemFis27 = new widget.TextBox();
        label52 = new widget.Label();
        PemFis28 = new widget.ComboBox();
        SkorPemFis28 = new widget.TextBox();
        label53 = new widget.Label();
        PemFis29 = new widget.ComboBox();
        SkorPemFis29 = new widget.TextBox();
        label54 = new widget.Label();
        label55 = new widget.Label();
        PemFis30 = new widget.ComboBox();
        SkorPemFis30 = new widget.TextBox();
        label56 = new widget.Label();
        PemFis31 = new widget.ComboBox();
        SkorPemFis31 = new widget.TextBox();
        label57 = new widget.Label();
        Lab32 = new widget.ComboBox();
        SkorLab32 = new widget.TextBox();
        label58 = new widget.Label();
        Lab33 = new widget.ComboBox();
        SkorLab33 = new widget.TextBox();
        label59 = new widget.Label();
        FaktorKontribusi34 = new widget.ComboBox();
        SkorFaktorKontribusi34 = new widget.TextBox();
        label60 = new widget.Label();
        label61 = new widget.Label();
        label62 = new widget.Label();
        FaktorKontribusi35 = new widget.ComboBox();
        SkorFaktorKontribusi35 = new widget.TextBox();
        label63 = new widget.Label();
        PerubahanBB = new widget.TextBox2();
        label64 = new widget.Label();
        label66 = new widget.Label();
        label68 = new widget.Label();
        label69 = new widget.Label();
        label70 = new widget.Label();
        label71 = new widget.Label();
        jLabel9 = new javax.swing.JLabel();
        DiagnosisMedis = new widget.TextBox();
        SkorSGA = new widget.TextBox2();
        label12 = new widget.Label();
        label65 = new widget.Label();
        label67 = new widget.Label();
        scrollPane1 = new widget.ScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        Catatan = new widget.TextArea();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        jSeparator10 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator12 = new javax.swing.JSeparator();
        jSeparator13 = new javax.swing.JSeparator();
        label72 = new widget.Label();
        LamaAsupanMenurun = new widget.TextBox();
        BBBiasanya = new widget.TextBox();
        BBSaatIni = new widget.TextBox();
        BesarnyaKehilangan = new widget.TextBox();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Formulir Subjektif Global Asessmen  ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(467, 500));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
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

        BtnCetak.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnCetak.setMnemonic('G');
        BtnCetak.setText("Cetak");
        BtnCetak.setToolTipText("Alt+G");
        BtnCetak.setName("BtnCetak"); // NOI18N
        BtnCetak.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnCetak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCetakActionPerformed(evt);
            }
        });
        BtnCetak.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCetakKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnCetak);

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
        panelGlass8.add(BtnUpload);

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

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.setPreferredSize(new java.awt.Dimension(457, 480));
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setPreferredSize(new java.awt.Dimension(1300, 1920));
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 200));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(1300, 1920));
        FormInput.setLayout(null);

        TNoRw.setText("2025/05/05/000000");
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(75, 10, 130, 26);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(275, 10, 300, 26);

        TNoRM.setEditable(false);
        TNoRM.setText("123456");
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(210, 10, 60, 26);

        label14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label14.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(450, 590, 450, 26);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdDokter);
        KdDokter.setBounds(75, 40, 130, 26);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        NmDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmDokter);
        NmDokter.setBounds(210, 40, 340, 26);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('2');
        BtnDokter.setToolTipText("Alt+2");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        BtnDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnDokterKeyPressed(evt);
            }
        });
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(550, 40, 28, 26);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(580, 10, 60, 26);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(650, 10, 140, 26);

        Jk.setEditable(false);
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        FormInput.add(Jk);
        Jk.setBounds(650, 40, 140, 26);

        jLabel10.setText("No.Rawat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 10, 70, 26);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(610, 40, 30, 26);

        label11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label11.setText("Skor SGA :");
        label11.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(880, 1680, 220, 30);

        TglAsuhan.setForeground(new java.awt.Color(50, 70, 50));
        TglAsuhan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12-06-2025 06:22:04" }));
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setName("TglAsuhan"); // NOI18N
        TglAsuhan.setOpaque(false);
        TglAsuhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglAsuhanKeyPressed(evt);
            }
        });
        FormInput.add(TglAsuhan);
        TglAsuhan.setBounds(650, 70, 140, 26);

        NmPoli.setEditable(false);
        NmPoli.setHighlighter(null);
        NmPoli.setName("NmPoli"); // NOI18N
        FormInput.add(NmPoli);
        NmPoli.setBounds(210, 70, 340, 26);

        label15.setText("Dokter :");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label15);
        label15.setBounds(0, 40, 70, 26);

        KdPoli.setEditable(false);
        KdPoli.setHighlighter(null);
        KdPoli.setName("KdPoli"); // NOI18N
        FormInput.add(KdPoli);
        KdPoli.setBounds(75, 70, 130, 26);

        label16.setText("Kamar :");
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label16);
        label16.setBounds(0, 70, 70, 26);

        BeratBadan4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ringan", "Sedang", "Berat" }));
        BeratBadan4.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        BeratBadan4.setName("BeratBadan4"); // NOI18N
        BeratBadan4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                BeratBadan4ItemStateChanged(evt);
            }
        });
        BeratBadan4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BeratBadan4KeyPressed(evt);
            }
        });
        FormInput.add(BeratBadan4);
        BeratBadan4.setBounds(30, 540, 210, 30);

        SkorBeratBadan4.setEditable(false);
        SkorBeratBadan4.setBackground(new java.awt.Color(242, 242, 242));
        SkorBeratBadan4.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorBeratBadan4.setName("SkorBeratBadan4"); // NOI18N
        FormInput.add(SkorBeratBadan4);
        SkorBeratBadan4.setBounds(250, 540, 30, 30);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label18.setText("18.  Secara umum, adanya hiperkatabolik. Bila ada, kategorinya (kondisi stres metabolik akut/hiperkatabolik)");
        label18.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label18);
        label18.setBounds(10, 930, 650, 26);

        RiwayatAsupan1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Asupan cukup dan tidak ada perubahan", "Diet padat suboptimal", "Diet cair penuh (full liquid)/hanya oral nutrition supplements (ONS)", "Asupan minimal, clear fluid, atau starvasi (menurun tahap berat)" }));
        RiwayatAsupan1.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        RiwayatAsupan1.setName("RiwayatAsupan1"); // NOI18N
        RiwayatAsupan1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                RiwayatAsupan1ItemStateChanged(evt);
            }
        });
        RiwayatAsupan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RiwayatAsupan1KeyPressed(evt);
            }
        });
        FormInput.add(RiwayatAsupan1);
        RiwayatAsupan1.setBounds(30, 240, 520, 30);

        SkorRiwayatAsupan1.setEditable(false);
        SkorRiwayatAsupan1.setBackground(new java.awt.Color(242, 242, 242));
        SkorRiwayatAsupan1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorRiwayatAsupan1.setName("SkorRiwayatAsupan1"); // NOI18N
        FormInput.add(SkorRiwayatAsupan1);
        SkorRiwayatAsupan1.setBounds(560, 240, 30, 30);

        label19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label19.setText("RIWAYAT ASUPAN ");
        label19.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label19);
        label19.setBounds(10, 180, 170, 30);

        label20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label20.setText("2.  Asupan dalam 2 hari terakhir");
        label20.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label20);
        label20.setBounds(660, 210, 450, 26);

        RiwayatAsupan2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cukup", "Membaik tapi tidak adekuat", "Tidak ada perbaikan atau tidak adekuat" }));
        RiwayatAsupan2.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        RiwayatAsupan2.setName("RiwayatAsupan2"); // NOI18N
        RiwayatAsupan2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                RiwayatAsupan2ItemStateChanged(evt);
            }
        });
        RiwayatAsupan2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RiwayatAsupan2KeyPressed(evt);
            }
        });
        FormInput.add(RiwayatAsupan2);
        RiwayatAsupan2.setBounds(680, 240, 260, 30);

        label21.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label21.setText("3.  Perubahan BB tanpa cairan selama 6 bulan terakhir");
        label21.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label21.setName("label21"); // NOI18N
        label21.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label21);
        label21.setBounds(10, 390, 450, 26);

        label22.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label22.setText("kg ( jika diketahui )");
        label22.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label22.setName("label22"); // NOI18N
        label22.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label22);
        label22.setBounds(1070, 510, 160, 26);

        BeratBadan3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak ada (berat badan stabil) atau penurunan <5%", "Penurunan 5-10% tanpa stabilitas atau peningkatan", "Penurunan >10% dan sedang proses berjalan terus" }));
        BeratBadan3.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        BeratBadan3.setName("BeratBadan3"); // NOI18N
        BeratBadan3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                BeratBadan3ItemStateChanged(evt);
            }
        });
        BeratBadan3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BeratBadan3KeyPressed(evt);
            }
        });
        FormInput.add(BeratBadan3);
        BeratBadan3.setBounds(340, 390, 340, 30);

        SkorRiwayatAsupan2.setEditable(false);
        SkorRiwayatAsupan2.setBackground(new java.awt.Color(242, 242, 242));
        SkorRiwayatAsupan2.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorRiwayatAsupan2.setName("SkorRiwayatAsupan2"); // NOI18N
        FormInput.add(SkorRiwayatAsupan2);
        SkorRiwayatAsupan2.setBounds(950, 240, 30, 30);

        SkorBeratBadan3.setEditable(false);
        SkorBeratBadan3.setBackground(new java.awt.Color(242, 242, 242));
        SkorBeratBadan3.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorBeratBadan3.setName("SkorBeratBadan3"); // NOI18N
        FormInput.add(SkorBeratBadan3);
        SkorBeratBadan3.setBounds(690, 390, 30, 30);

        label23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label23.setText("7.  Anorexia");
        label23.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label23.setName("label23"); // NOI18N
        label23.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label23);
        label23.setBounds(10, 656, 150, 26);

        label24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label24.setText("5.  Perubahan BB 2 minggu terakhir besarnya ");
        label24.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label24.setName("label24"); // NOI18N
        label24.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label24);
        label24.setBounds(730, 510, 280, 26);

        BeratBadan5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Meningkat", "Tidak Ada", "Menurun" }));
        BeratBadan5.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        BeratBadan5.setName("BeratBadan5"); // NOI18N
        BeratBadan5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                BeratBadan5ItemStateChanged(evt);
            }
        });
        BeratBadan5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BeratBadan5KeyPressed(evt);
            }
        });
        FormInput.add(BeratBadan5);
        BeratBadan5.setBounds(750, 540, 240, 30);

        SkorBeratBadan5.setEditable(false);
        SkorBeratBadan5.setBackground(new java.awt.Color(242, 242, 242));
        SkorBeratBadan5.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorBeratBadan5.setName("SkorBeratBadan5"); // NOI18N
        FormInput.add(SkorBeratBadan5);
        SkorBeratBadan5.setBounds(1000, 540, 30, 30);

        label25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label25.setText("BERAT BADAN ");
        label25.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label25.setName("label25"); // NOI18N
        label25.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label25);
        label25.setBounds(10, 360, 100, 30);

        label26.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label26.setText("4.  Jika perubahan BB tidak diketahui, apakah ada kehilangan berat badan secara subjektif selama 6 bulan terakhir");
        label26.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label26.setName("label26"); // NOI18N
        label26.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label26);
        label26.setBounds(10, 510, 760, 26);

        Gejala6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala6.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala6.setName("Gejala6"); // NOI18N
        Gejala6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala6ItemStateChanged(evt);
            }
        });
        Gejala6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala6KeyPressed(evt);
            }
        });
        FormInput.add(Gejala6);
        Gejala6.setBounds(160, 620, 230, 30);

        SkorGejala6.setEditable(false);
        SkorGejala6.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala6.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala6.setName("SkorGejala6"); // NOI18N
        FormInput.add(SkorGejala6);
        SkorGejala6.setBounds(400, 620, 30, 30);

        label27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label27.setText("6.  Nyeri Telan");
        label27.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label27.setName("label27"); // NOI18N
        label27.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label27);
        label27.setBounds(10, 620, 150, 26);

        Gejala7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala7.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala7.setName("Gejala7"); // NOI18N
        Gejala7.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala7ItemStateChanged(evt);
            }
        });
        Gejala7.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala7KeyPressed(evt);
            }
        });
        FormInput.add(Gejala7);
        Gejala7.setBounds(160, 655, 230, 30);

        SkorGejala7.setEditable(false);
        SkorGejala7.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala7.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala7.setName("SkorGejala7"); // NOI18N
        FormInput.add(SkorGejala7);
        SkorGejala7.setBounds(400, 655, 30, 30);

        label28.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label28.setText("8.  Muntah");
        label28.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label28.setName("label28"); // NOI18N
        label28.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label28);
        label28.setBounds(10, 690, 150, 26);

        Gejala8.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala8.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala8.setName("Gejala8"); // NOI18N
        Gejala8.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala8ItemStateChanged(evt);
            }
        });
        Gejala8.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala8KeyPressed(evt);
            }
        });
        FormInput.add(Gejala8);
        Gejala8.setBounds(160, 690, 230, 30);

        SkorGejala8.setEditable(false);
        SkorGejala8.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala8.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala8.setName("SkorGejala8"); // NOI18N
        FormInput.add(SkorGejala8);
        SkorGejala8.setBounds(400, 690, 30, 30);

        label29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label29.setText("9.  Mual");
        label29.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label29.setName("label29"); // NOI18N
        label29.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label29);
        label29.setBounds(10, 726, 150, 30);

        Gejala9.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala9.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala9.setName("Gejala9"); // NOI18N
        Gejala9.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala9ItemStateChanged(evt);
            }
        });
        Gejala9.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala9KeyPressed(evt);
            }
        });
        FormInput.add(Gejala9);
        Gejala9.setBounds(160, 725, 230, 30);

        SkorGejala9.setEditable(false);
        SkorGejala9.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala9.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala9.setName("SkorGejala9"); // NOI18N
        FormInput.add(SkorGejala9);
        SkorGejala9.setBounds(400, 725, 30, 30);

        label30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label30.setText("10.  Disfagia");
        label30.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label30.setName("label30"); // NOI18N
        label30.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label30);
        label30.setBounds(10, 770, 150, 26);

        Gejala10.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala10.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala10.setName("Gejala10"); // NOI18N
        Gejala10.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala10ItemStateChanged(evt);
            }
        });
        Gejala10.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala10KeyPressed(evt);
            }
        });
        FormInput.add(Gejala10);
        Gejala10.setBounds(160, 760, 230, 30);

        SkorGejala10.setEditable(false);
        SkorGejala10.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala10.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala10.setName("SkorGejala10"); // NOI18N
        FormInput.add(SkorGejala10);
        SkorGejala10.setBounds(400, 760, 30, 30);

        label31.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label31.setText("11.  Diare");
        label31.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label31.setName("label31"); // NOI18N
        label31.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label31);
        label31.setBounds(460, 620, 230, 30);

        Gejala11.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala11.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala11.setName("Gejala11"); // NOI18N
        Gejala11.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala11ItemStateChanged(evt);
            }
        });
        Gejala11.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala11KeyPressed(evt);
            }
        });
        FormInput.add(Gejala11);
        Gejala11.setBounds(690, 620, 230, 30);

        SkorGejala11.setEditable(false);
        SkorGejala11.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala11.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala11.setName("SkorGejala11"); // NOI18N
        FormInput.add(SkorGejala11);
        SkorGejala11.setBounds(930, 620, 30, 30);

        label32.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label32.setText("12.  Masalah Gigi Geligi");
        label32.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label32.setName("label32"); // NOI18N
        label32.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label32);
        label32.setBounds(460, 655, 150, 30);

        Gejala12.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala12.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala12.setName("Gejala12"); // NOI18N
        Gejala12.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala12ItemStateChanged(evt);
            }
        });
        Gejala12.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala12KeyPressed(evt);
            }
        });
        FormInput.add(Gejala12);
        Gejala12.setBounds(690, 655, 230, 30);

        SkorGejala12.setEditable(false);
        SkorGejala12.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala12.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala12.setName("SkorGejala12"); // NOI18N
        FormInput.add(SkorGejala12);
        SkorGejala12.setBounds(930, 655, 30, 30);

        label33.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label33.setText("13.  Cepat Penuh");
        label33.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label33.setName("label33"); // NOI18N
        label33.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label33);
        label33.setBounds(460, 690, 150, 30);

        Gejala13.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala13.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala13.setName("Gejala13"); // NOI18N
        Gejala13.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala13ItemStateChanged(evt);
            }
        });
        Gejala13.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala13KeyPressed(evt);
            }
        });
        FormInput.add(Gejala13);
        Gejala13.setBounds(690, 690, 230, 30);

        SkorGejala13.setEditable(false);
        SkorGejala13.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala13.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala13.setName("SkorGejala13"); // NOI18N
        FormInput.add(SkorGejala13);
        SkorGejala13.setBounds(930, 690, 30, 30);

        label34.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label34.setText("14.  Konstipasi");
        label34.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label34.setName("label34"); // NOI18N
        label34.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label34);
        label34.setBounds(460, 725, 150, 30);

        Gejala14.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Intermiten/Ringan/Beberapa Kali", "Konstan/Berat/Berkali-Kali" }));
        Gejala14.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala14.setName("Gejala14"); // NOI18N
        Gejala14.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala14ItemStateChanged(evt);
            }
        });
        Gejala14.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala14KeyPressed(evt);
            }
        });
        FormInput.add(Gejala14);
        Gejala14.setBounds(690, 725, 230, 30);

        SkorGejala14.setEditable(false);
        SkorGejala14.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala14.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala14.setName("SkorGejala14"); // NOI18N
        FormInput.add(SkorGejala14);
        SkorGejala14.setBounds(930, 725, 30, 30);

        label35.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label35.setText("17.  Kapasitas fungsional dalam 2 minggu terkahir");
        label35.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label35.setName("label35"); // NOI18N
        label35.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label35);
        label35.setBounds(590, 850, 310, 30);

        Gejala15.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sembuh", "Perbaikan", "Tidak Ada Perubahan/Memburuk" }));
        Gejala15.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Gejala15.setName("Gejala15"); // NOI18N
        Gejala15.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Gejala15ItemStateChanged(evt);
            }
        });
        Gejala15.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Gejala15KeyPressed(evt);
            }
        });
        FormInput.add(Gejala15);
        Gejala15.setBounds(690, 760, 230, 30);

        SkorGejala15.setEditable(false);
        SkorGejala15.setBackground(new java.awt.Color(242, 242, 242));
        SkorGejala15.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorGejala15.setName("SkorGejala15"); // NOI18N
        FormInput.add(SkorGejala15);
        SkorGejala15.setBounds(930, 760, 30, 30);

        label36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label36.setText("GEJALA-GEJALA ( YANG MEMPENGARUHI ASUPAN MAKAN ) ");
        label36.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label36.setName("label36"); // NOI18N
        label36.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label36);
        label36.setBounds(10, 590, 570, 30);

        label37.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label37.setText("15.  Gejala Selama 2 Minggu Terakhir");
        label37.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label37.setName("label37"); // NOI18N
        label37.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label37);
        label37.setBounds(460, 760, 240, 30);

        KapasitasFungsional16.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak ada disfungsi", "Kesulitan dalam melakukan aktivitas normal/ambulasi", "Hanya dapat duduk/ditempat tidur" }));
        KapasitasFungsional16.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        KapasitasFungsional16.setName("KapasitasFungsional16"); // NOI18N
        KapasitasFungsional16.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                KapasitasFungsional16ItemStateChanged(evt);
            }
        });
        KapasitasFungsional16.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KapasitasFungsional16KeyPressed(evt);
            }
        });
        FormInput.add(KapasitasFungsional16);
        KapasitasFungsional16.setBounds(160, 850, 360, 30);

        SkorKapasitasFungsional16.setEditable(false);
        SkorKapasitasFungsional16.setBackground(new java.awt.Color(242, 242, 242));
        SkorKapasitasFungsional16.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorKapasitasFungsional16.setName("SkorKapasitasFungsional16"); // NOI18N
        FormInput.add(SkorKapasitasFungsional16);
        SkorKapasitasFungsional16.setBounds(530, 850, 30, 30);

        label38.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label38.setText("16.  Deskripsi");
        label38.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label38.setName("label38"); // NOI18N
        label38.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label38);
        label38.setBounds(10, 850, 150, 30);

        KapasitasFungsional17.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Membaik", "Tidak ada perubahan", "Memburuk" }));
        KapasitasFungsional17.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        KapasitasFungsional17.setName("KapasitasFungsional17"); // NOI18N
        KapasitasFungsional17.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                KapasitasFungsional17ItemStateChanged(evt);
            }
        });
        KapasitasFungsional17.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KapasitasFungsional17KeyPressed(evt);
            }
        });
        FormInput.add(KapasitasFungsional17);
        KapasitasFungsional17.setBounds(890, 850, 150, 30);

        SkorKapasitasFungsional17.setEditable(false);
        SkorKapasitasFungsional17.setBackground(new java.awt.Color(242, 242, 242));
        SkorKapasitasFungsional17.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorKapasitasFungsional17.setName("SkorKapasitasFungsional17"); // NOI18N
        FormInput.add(SkorKapasitasFungsional17);
        SkorKapasitasFungsional17.setBounds(1050, 850, 30, 30);

        label39.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label39.setText("KAPASITAS FUNGSIONAL ( KELELAHAN DAN PROGRESIFITAS KEHILANGAN FUNGSI ) ");
        label39.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label39.setName("label39"); // NOI18N
        label39.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label39);
        label39.setBounds(10, 815, 890, 30);

        label40.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label40.setText("Asupan tidak cukup, lamanya asupan menurun");
        label40.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label40.setName("label40"); // NOI18N
        label40.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label40);
        label40.setBounds(30, 280, 320, 26);

        Penyakit18.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak ada", "Rendah (contoh: hernia inguinal, penyakit kejiawaan, tukak lambung)", "Sedang (contoh: infeksi, penyakit ginjal kronis,perdarahan saluran cerna)", "Tinggi(contoh: sepsis, kanker, luka bakar, TB, stroke, sakit kritis, trauma multiple)" }));
        Penyakit18.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        Penyakit18.setName("Penyakit18"); // NOI18N
        Penyakit18.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Penyakit18ItemStateChanged(evt);
            }
        });
        Penyakit18.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Penyakit18KeyPressed(evt);
            }
        });
        FormInput.add(Penyakit18);
        Penyakit18.setBounds(35, 960, 535, 30);

        SkorPenyakit18.setEditable(false);
        SkorPenyakit18.setBackground(new java.awt.Color(242, 242, 242));
        SkorPenyakit18.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPenyakit18.setName("SkorPenyakit18"); // NOI18N
        FormInput.add(SkorPenyakit18);
        SkorPenyakit18.setBounds(580, 960, 30, 30);

        label41.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label41.setText("PENYAKIT DAN HUBUNGANNYA DENGAN KEBUTUHAN GIZI ");
        label41.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label41.setName("label41"); // NOI18N
        label41.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label41);
        label41.setBounds(10, 900, 460, 30);

        label42.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label42.setText("19.  Orbita");
        label42.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label42.setName("label42"); // NOI18N
        label42.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label42);
        label42.setBounds(10, 1045, 310, 30);

        PemFis19.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Terlihat bantalan lemak disekitar mata", "Lingkar mata agak gelap, agak cekung", "Mata cekung, lingkar mata gelap, kulit kusam" }));
        PemFis19.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        PemFis19.setName("PemFis19"); // NOI18N
        PemFis19.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis19ItemStateChanged(evt);
            }
        });
        PemFis19.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis19KeyPressed(evt);
            }
        });
        FormInput.add(PemFis19);
        PemFis19.setBounds(250, 1045, 320, 30);

        SkorPemFis19.setEditable(false);
        SkorPemFis19.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis19.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis19.setName("SkorPemFis19"); // NOI18N
        FormInput.add(SkorPemFis19);
        SkorPemFis19.setBounds(580, 1045, 30, 30);

        label43.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label43.setText("20.  Lingkar lengan atas (bisep/trisep)");
        label43.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label43.setName("label43"); // NOI18N
        label43.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label43);
        label43.setBounds(10, 1080, 310, 30);

        PemFis20.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Massa lemak teraba cukup", "Massa lemak masih terasa, tapi tidak cukup penuh", "Massa lemak terasa sedikit sekali/tidak teraba" }));
        PemFis20.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        PemFis20.setName("PemFis20"); // NOI18N
        PemFis20.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis20ItemStateChanged(evt);
            }
        });
        PemFis20.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis20KeyPressed(evt);
            }
        });
        FormInput.add(PemFis20);
        PemFis20.setBounds(250, 1080, 320, 30);

        SkorPemFis20.setEditable(false);
        SkorPemFis20.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis20.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis20.setName("SkorPemFis20"); // NOI18N
        FormInput.add(SkorPemFis20);
        SkorPemFis20.setBounds(580, 1080, 30, 30);

        label44.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label44.setText("21.  Daerah dada dan punggung (iga, punggung bawah, dan linea mid aksila)");
        label44.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label44.setName("label44"); // NOI18N
        label44.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label44);
        label44.setBounds(10, 1115, 470, 30);

        PemFis21.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Dada tampak penuh, tulang iga tidak terlihat, krista iliaka tidak menonjol", "Sela iga tampak jauh", "Iga gambang, krista iliaka sangat menonjol" }));
        PemFis21.setName("PemFis21"); // NOI18N
        PemFis21.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis21ItemStateChanged(evt);
            }
        });
        PemFis21.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis21KeyPressed(evt);
            }
        });
        FormInput.add(PemFis21);
        PemFis21.setBounds(35, 1150, 535, 30);

        SkorPemFis21.setEditable(false);
        SkorPemFis21.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis21.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis21.setName("SkorPemFis21"); // NOI18N
        FormInput.add(SkorPemFis21);
        SkorPemFis21.setBounds(580, 1150, 30, 30);

        label45.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label45.setText("PEMERIKSAAN FISIK ( Lemak Subkutan ) ");
        label45.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label45.setName("label45"); // NOI18N
        label45.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label45);
        label45.setBounds(10, 1010, 460, 30);

        label46.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label46.setText("22.  Pelipis - muskulus temporalis");
        label46.setName("label46"); // NOI18N
        label46.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label46);
        label46.setBounds(10, 1230, 410, 30);

        PemFis22.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak cekung, otot temporal dapat terlihat", "Sedikit cekung", "Cekung" }));
        PemFis22.setName("PemFis22"); // NOI18N
        PemFis22.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis22ItemStateChanged(evt);
            }
        });
        PemFis22.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis22KeyPressed(evt);
            }
        });
        FormInput.add(PemFis22);
        PemFis22.setBounds(380, 1230, 440, 30);

        SkorPemFis22.setEditable(false);
        SkorPemFis22.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis22.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis22.setName("SkorPemFis22"); // NOI18N
        FormInput.add(SkorPemFis22);
        SkorPemFis22.setBounds(830, 1230, 30, 30);

        label47.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label47.setText("24.  Akromion dan klavikula");
        label47.setName("label47"); // NOI18N
        label47.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label47);
        label47.setBounds(10, 1310, 410, 30);

        PemFis23.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak terlihat pada laki-laki. terlihat, tapi tidak menonjol pada perempuan", "Terlihat pada laki-laki dan agak menonjol pada perempuan", "Tulang klavikula tampak sangat menonjol" }));
        PemFis23.setName("PemFis23"); // NOI18N
        PemFis23.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis23ItemStateChanged(evt);
            }
        });
        PemFis23.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis23KeyPressed(evt);
            }
        });
        FormInput.add(PemFis23);
        PemFis23.setBounds(380, 1270, 440, 30);

        SkorPemFis23.setEditable(false);
        SkorPemFis23.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis23.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis23.setName("SkorPemFis23"); // NOI18N
        FormInput.add(SkorPemFis23);
        SkorPemFis23.setBounds(830, 1270, 30, 30);

        label48.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label48.setText("23.  Klavikula - muskulus pektoralis mayor, deltiod dan trapezius");
        label48.setName("label48"); // NOI18N
        label48.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label48);
        label48.setBounds(10, 1270, 410, 30);

        PemFis24.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Area bahu, dada dan leher tampak utuh dan bulat", "Prosessus akromin agak menonjol", "Sendi bahu tampak menonjol, prosessus akromin sangat menonjol" }));
        PemFis24.setName("PemFis24"); // NOI18N
        PemFis24.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis24ItemStateChanged(evt);
            }
        });
        PemFis24.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis24KeyPressed(evt);
            }
        });
        FormInput.add(PemFis24);
        PemFis24.setBounds(380, 1310, 440, 30);

        SkorPemFis24.setEditable(false);
        SkorPemFis24.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis24.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis24.setName("SkorPemFis24"); // NOI18N
        FormInput.add(SkorPemFis24);
        SkorPemFis24.setBounds(830, 1310, 30, 30);

        label49.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label49.setText("25.  Tulang Scapular - muskulus trapezius. supraspinus, infraspinus");
        label49.setName("label49"); // NOI18N
        label49.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label49);
        label49.setBounds(10, 1350, 410, 30);

        PemFis25.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tulang tidak menonjol, tidak ada cekungan tulang", "Cekungan ringan atau tulang tampak sedikit menonjol", "Tulang tampak menonjol, tampak cekungan antara tulang iga-skapula/bahu tulang belakang" }));
        PemFis25.setName("PemFis25"); // NOI18N
        PemFis25.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis25ItemStateChanged(evt);
            }
        });
        PemFis25.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis25KeyPressed(evt);
            }
        });
        FormInput.add(PemFis25);
        PemFis25.setBounds(380, 1350, 440, 30);

        SkorPemFis25.setEditable(false);
        SkorPemFis25.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis25.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis25.setName("SkorPemFis25"); // NOI18N
        FormInput.add(SkorPemFis25);
        SkorPemFis25.setBounds(830, 1350, 30, 30);

        label50.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label50.setText("26.  Punggung(dorsal) tangan - muskulus interosseus");
        label50.setName("label50"); // NOI18N
        label50.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label50);
        label50.setBounds(10, 1390, 410, 30);

        PemFis26.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tangan daerah jempol tampak cekung dan antara jempol dan telunjuk", "Sedikit cekung", "Tampak penonjolan oto(tampak datar pada beberapa orang gizi baik)" }));
        PemFis26.setName("PemFis26"); // NOI18N
        PemFis26.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis26ItemStateChanged(evt);
            }
        });
        PemFis26.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis26KeyPressed(evt);
            }
        });
        FormInput.add(PemFis26);
        PemFis26.setBounds(380, 1390, 440, 30);

        SkorPemFis26.setEditable(false);
        SkorPemFis26.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis26.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis26.setName("SkorPemFis26"); // NOI18N
        FormInput.add(SkorPemFis26);
        SkorPemFis26.setBounds(830, 1390, 30, 30);

        label51.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label51.setText("27.  Lutut");
        label51.setName("label51"); // NOI18N
        label51.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label51);
        label51.setBounds(10, 1430, 410, 30);

        PemFis27.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tempurung lutut tidak terlalu menonjol, otot-otot masik tampak", "Tempurung lutut agak menonjol", "Tempurung lutut menonjol" }));
        PemFis27.setName("PemFis27"); // NOI18N
        PemFis27.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis27ItemStateChanged(evt);
            }
        });
        PemFis27.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis27KeyPressed(evt);
            }
        });
        FormInput.add(PemFis27);
        PemFis27.setBounds(380, 1430, 440, 30);

        SkorPemFis27.setEditable(false);
        SkorPemFis27.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis27.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis27.setName("SkorPemFis27"); // NOI18N
        FormInput.add(SkorPemFis27);
        SkorPemFis27.setBounds(830, 1430, 30, 30);

        label52.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label52.setText("28.  Paha atas");
        label52.setName("label52"); // NOI18N
        label52.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label52);
        label52.setBounds(10, 1470, 410, 30);

        PemFis28.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Paha tampak utuh dan paha dalam tidak cekung", "Paha bagian dalam sedikit cekung", "Paha bagian dalam tampak cekung, kecil dan kurus" }));
        PemFis28.setName("PemFis28"); // NOI18N
        PemFis28.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis28ItemStateChanged(evt);
            }
        });
        PemFis28.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis28KeyPressed(evt);
            }
        });
        FormInput.add(PemFis28);
        PemFis28.setBounds(380, 1470, 440, 30);

        SkorPemFis28.setEditable(false);
        SkorPemFis28.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis28.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis28.setName("SkorPemFis28"); // NOI18N
        FormInput.add(SkorPemFis28);
        SkorPemFis28.setBounds(830, 1470, 30, 30);

        label53.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label53.setText("29.  Betis");
        label53.setName("label53"); // NOI18N
        label53.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label53);
        label53.setBounds(10, 1510, 410, 30);

        PemFis29.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Otot tampak jelas", "Otot tidak terlalu tampak atau terbentuk", "Kurus, otot tampak kecil dan bahkan tidak terlihat" }));
        PemFis29.setName("PemFis29"); // NOI18N
        PemFis29.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis29ItemStateChanged(evt);
            }
        });
        PemFis29.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis29KeyPressed(evt);
            }
        });
        FormInput.add(PemFis29);
        PemFis29.setBounds(380, 1510, 440, 30);

        SkorPemFis29.setEditable(false);
        SkorPemFis29.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis29.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis29.setName("SkorPemFis29"); // NOI18N
        FormInput.add(SkorPemFis29);
        SkorPemFis29.setBounds(830, 1510, 30, 30);

        label54.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label54.setText("PEMERIKSAAN FISIK ( WASTING OTOT )");
        label54.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label54.setName("label54"); // NOI18N
        label54.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label54);
        label54.setBounds(10, 1200, 460, 30);

        label55.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label55.setText("30.  Kaki, wajah, ekstremitas superior, ekstremitas inferior");
        label55.setName("label55"); // NOI18N
        label55.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label55);
        label55.setBounds(10, 1595, 360, 26);

        PemFis30.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Edema paling ringan ekstremitas inferior sedikit bengkak, identasi atau cekungan edema segera hilang", "Edema paling ringan-sedang, ekstremitas superior dan inferior sedikit bengkak, identasi atau cekungan edema segera hilang(berlangsung 0-30 detik)", "Edema piting yang dalam hingga sangat dalam, identasi atau cekungan edema berlangsung 30-60 detik, pada wajah dan ekstremitas bilateral tampak bengkak (+3 sampai +4)" }));
        PemFis30.setName("PemFis30"); // NOI18N
        PemFis30.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis30ItemStateChanged(evt);
            }
        });
        PemFis30.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis30KeyPressed(evt);
            }
        });
        FormInput.add(PemFis30);
        PemFis30.setBounds(380, 1595, 940, 30);

        SkorPemFis30.setEditable(false);
        SkorPemFis30.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis30.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        SkorPemFis30.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis30.setName("SkorPemFis30"); // NOI18N
        FormInput.add(SkorPemFis30);
        SkorPemFis30.setBounds(1330, 1595, 30, 30);

        label56.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label56.setText("31.  Asciters");
        label56.setName("label56"); // NOI18N
        label56.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label56);
        label56.setBounds(10, 1630, 360, 26);

        PemFis31.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak ada", "Tampak hanya pada pencitraan", "Ada" }));
        PemFis31.setName("PemFis31"); // NOI18N
        PemFis31.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                PemFis31ItemStateChanged(evt);
            }
        });
        PemFis31.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemFis31KeyPressed(evt);
            }
        });
        FormInput.add(PemFis31);
        PemFis31.setBounds(380, 1630, 490, 30);

        SkorPemFis31.setEditable(false);
        SkorPemFis31.setBackground(new java.awt.Color(242, 242, 242));
        SkorPemFis31.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorPemFis31.setName("SkorPemFis31"); // NOI18N
        FormInput.add(SkorPemFis31);
        SkorPemFis31.setBounds(880, 1630, 30, 30);

        label57.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label57.setText("32.  Albumin");
        label57.setName("label57"); // NOI18N
        label57.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label57);
        label57.setBounds(10, 1715, 400, 26);

        Lab32.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "3.0 - 3.5 g/dl", "2.5 - 2.9 g/dl", "<2.5 g/dl" }));
        Lab32.setName("Lab32"); // NOI18N
        Lab32.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Lab32ItemStateChanged(evt);
            }
        });
        Lab32.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Lab32KeyPressed(evt);
            }
        });
        FormInput.add(Lab32);
        Lab32.setBounds(220, 1715, 160, 30);

        SkorLab32.setEditable(false);
        SkorLab32.setBackground(new java.awt.Color(242, 242, 242));
        SkorLab32.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorLab32.setName("SkorLab32"); // NOI18N
        FormInput.add(SkorLab32);
        SkorLab32.setBounds(390, 1715, 30, 30);

        label58.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label58.setText("33.  Total lymphocyte count (TLC)");
        label58.setName("label58"); // NOI18N
        label58.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label58);
        label58.setBounds(10, 1750, 360, 26);

        Lab33.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1200 - 1500 /mm3", "800 -1199 /mm3", "< 800 /mm3" }));
        Lab33.setName("Lab33"); // NOI18N
        Lab33.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Lab33ItemStateChanged(evt);
            }
        });
        Lab33.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Lab33KeyPressed(evt);
            }
        });
        FormInput.add(Lab33);
        Lab33.setBounds(220, 1750, 160, 30);

        SkorLab33.setEditable(false);
        SkorLab33.setBackground(new java.awt.Color(242, 242, 242));
        SkorLab33.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorLab33.setName("SkorLab33"); // NOI18N
        FormInput.add(SkorLab33);
        SkorLab33.setBounds(390, 1750, 30, 30);

        label59.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label59.setText("34.  Peradangan");
        label59.setName("label59"); // NOI18N
        label59.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label59);
        label59.setBounds(460, 1715, 120, 30);

        FaktorKontribusi34.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak ada/ringan", "Sedang", "Berat" }));
        FaktorKontribusi34.setName("FaktorKontribusi34"); // NOI18N
        FaktorKontribusi34.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                FaktorKontribusi34ItemStateChanged(evt);
            }
        });
        FaktorKontribusi34.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FaktorKontribusi34KeyPressed(evt);
            }
        });
        FormInput.add(FaktorKontribusi34);
        FaktorKontribusi34.setBounds(580, 1715, 140, 30);

        SkorFaktorKontribusi34.setEditable(false);
        SkorFaktorKontribusi34.setBackground(new java.awt.Color(242, 242, 242));
        SkorFaktorKontribusi34.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        SkorFaktorKontribusi34.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorFaktorKontribusi34.setName("SkorFaktorKontribusi34"); // NOI18N
        FormInput.add(SkorFaktorKontribusi34);
        SkorFaktorKontribusi34.setBounds(730, 1715, 140, 30);

        label60.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label60.setText("LABORATORIUM ( Bila Ada ) ");
        label60.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label60.setName("label60"); // NOI18N
        label60.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label60);
        label60.setBounds(10, 1680, 460, 30);

        label61.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label61.setText("PEMERIKSAAN FISIK ( Retensi Cairan )");
        label61.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label61.setName("label61"); // NOI18N
        label61.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label61);
        label61.setBounds(10, 1560, 460, 30);

        label62.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label62.setText("35.  Sarkopenia");
        label62.setName("label62"); // NOI18N
        label62.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label62);
        label62.setBounds(460, 1750, 120, 30);

        FaktorKontribusi35.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak ada/ringan", "Sedang", "Berat" }));
        FaktorKontribusi35.setName("FaktorKontribusi35"); // NOI18N
        FaktorKontribusi35.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                FaktorKontribusi35ItemStateChanged(evt);
            }
        });
        FaktorKontribusi35.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FaktorKontribusi35KeyPressed(evt);
            }
        });
        FormInput.add(FaktorKontribusi35);
        FaktorKontribusi35.setBounds(580, 1750, 140, 30);

        SkorFaktorKontribusi35.setEditable(false);
        SkorFaktorKontribusi35.setBackground(new java.awt.Color(242, 242, 242));
        SkorFaktorKontribusi35.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        SkorFaktorKontribusi35.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SkorFaktorKontribusi35.setName("SkorFaktorKontribusi35"); // NOI18N
        FormInput.add(SkorFaktorKontribusi35);
        SkorFaktorKontribusi35.setBounds(730, 1750, 140, 30);

        label63.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label63.setText("1.  Asupan makanan, perubahan jumlah asupan akhir-akhir ini dibandingkan dengan biasanya");
        label63.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label63.setName("label63"); // NOI18N
        label63.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label63);
        label63.setBounds(10, 210, 590, 26);

        PerubahanBB.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        PerubahanBB.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        PerubahanBB.setName("PerubahanBB"); // NOI18N
        FormInput.add(PerubahanBB);
        PerubahanBB.setBounds(1000, 510, 60, 26);

        label64.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label64.setText("( Pakai . (titik) untuk pecahan )");
        label64.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label64.setName("label64"); // NOI18N
        label64.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label64);
        label64.setBounds(290, 450, 200, 26);

        label66.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label66.setText("BB Saat ini");
        label66.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label66.setName("label66"); // NOI18N
        label66.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label66);
        label66.setBounds(30, 450, 80, 26);

        label68.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label68.setText("Besarnya kehilangan");
        label68.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label68.setName("label68"); // NOI18N
        label68.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label68);
        label68.setBounds(30, 480, 150, 26);

        label69.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label69.setText("kg");
        label69.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label69.setName("label69"); // NOI18N
        label69.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label69);
        label69.setBounds(250, 420, 40, 26);

        label70.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label70.setText("kg");
        label70.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label70.setName("label70"); // NOI18N
        label70.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label70);
        label70.setBounds(250, 480, 40, 26);

        label71.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label71.setText("kg");
        label71.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label71.setName("label71"); // NOI18N
        label71.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label71);
        label71.setBounds(250, 450, 40, 26);

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("Diagnosa Medis");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(10, 130, 140, 30);

        DiagnosisMedis.setName("DiagnosisMedis"); // NOI18N
        FormInput.add(DiagnosisMedis);
        DiagnosisMedis.setBounds(140, 130, 840, 30);

        SkorSGA.setEditable(false);
        SkorSGA.setBackground(new java.awt.Color(242, 242, 242));
        SkorSGA.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        SkorSGA.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        SkorSGA.setFont(new java.awt.Font("Segoe UI Semibold", 1, 36)); // NOI18N
        SkorSGA.setName("SkorSGA"); // NOI18N
        SkorSGA.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                SkorSGAInputMethodTextChanged(evt);
            }
        });
        SkorSGA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                SkorSGAKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SkorSGAKeyTyped(evt);
            }
        });
        FormInput.add(SkorSGA);
        SkorSGA.setBounds(880, 1715, 80, 65);

        label12.setText("Tanggal :");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label12);
        label12.setBounds(590, 70, 52, 26);

        label65.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label65.setText("BB Biasanya");
        label65.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        label65.setName("label65"); // NOI18N
        label65.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label65);
        label65.setBounds(30, 420, 120, 26);

        label67.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label67.setText("FAKTOR KONTRIBUSI");
        label67.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label67.setName("label67"); // NOI18N
        label67.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label67);
        label67.setBounds(460, 1680, 500, 30);

        scrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane1.setName("scrollPane1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        Catatan.setColumns(20);
        Catatan.setRows(5);
        Catatan.setName("Catatan"); // NOI18N
        jScrollPane1.setViewportView(Catatan);

        scrollPane1.setViewportView(jScrollPane1);

        FormInput.add(scrollPane1);
        scrollPane1.setBounds(90, 1805, 870, 90);

        jSeparator3.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator3.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(0, 120, 1370, 1);

        jSeparator4.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator4.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator4.setName("jSeparator4"); // NOI18N
        FormInput.add(jSeparator4);
        jSeparator4.setBounds(0, 170, 1370, 1);

        jSeparator5.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator5.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator5.setName("jSeparator5"); // NOI18N
        FormInput.add(jSeparator5);
        jSeparator5.setBounds(0, 355, 1370, 1);

        jSeparator6.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator6.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator6.setName("jSeparator6"); // NOI18N
        FormInput.add(jSeparator6);
        jSeparator6.setBounds(0, 585, 1370, 1);

        jSeparator7.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator7.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator7.setName("jSeparator7"); // NOI18N
        FormInput.add(jSeparator7);
        jSeparator7.setBounds(0, 810, 1370, 1);

        jSeparator8.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator8.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator8.setName("jSeparator8"); // NOI18N
        FormInput.add(jSeparator8);
        jSeparator8.setBounds(0, 895, 1370, 1);

        jSeparator9.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator9.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator9.setName("jSeparator9"); // NOI18N
        FormInput.add(jSeparator9);
        jSeparator9.setBounds(0, 1005, 1370, 1);

        jSeparator10.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator10.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator10.setName("jSeparator10"); // NOI18N
        FormInput.add(jSeparator10);
        jSeparator10.setBounds(0, 1195, 1370, 1);

        jSeparator11.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator11.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator11.setName("jSeparator11"); // NOI18N
        FormInput.add(jSeparator11);
        jSeparator11.setBounds(0, 1555, 1370, 1);

        jSeparator12.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator12.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator12.setName("jSeparator12"); // NOI18N
        FormInput.add(jSeparator12);
        jSeparator12.setBounds(0, 1675, 1370, 1);

        jSeparator13.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator13.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator13.setName("jSeparator13"); // NOI18N
        FormInput.add(jSeparator13);
        jSeparator13.setBounds(0, 1795, 1370, 1);

        label72.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label72.setText("CATATAN : ");
        label72.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        label72.setName("label72"); // NOI18N
        label72.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label72);
        label72.setBounds(10, 1800, 480, 30);

        LamaAsupanMenurun.setName("LamaAsupanMenurun"); // NOI18N
        FormInput.add(LamaAsupanMenurun);
        LamaAsupanMenurun.setBounds(30, 310, 960, 30);

        BBBiasanya.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        BBBiasanya.setName("BBBiasanya"); // NOI18N
        BBBiasanya.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                BBBiasanyaKeyReleased(evt);
            }
        });
        FormInput.add(BBBiasanya);
        BBBiasanya.setBounds(160, 420, 80, 26);

        BBSaatIni.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        BBSaatIni.setName("BBSaatIni"); // NOI18N
        BBSaatIni.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                BBSaatIniKeyReleased(evt);
            }
        });
        FormInput.add(BBSaatIni);
        BBSaatIni.setBounds(160, 450, 80, 26);

        BesarnyaKehilangan.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        BesarnyaKehilangan.setName("BesarnyaKehilangan"); // NOI18N
        FormInput.add(BesarnyaKehilangan);
        BesarnyaKehilangan.setBounds(160, 480, 80, 26);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Penilaian", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Asuhan :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12-06-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12-06-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(195, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
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
        jLabel7.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(LCount);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        TabRawat.addTab("Data Penilaian", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{            
            Valid.pindah(evt,TCari,BtnDokter);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else{
            if(Sequel.menyimpantf("sga_malnutrisi","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","No.Rawat",81,new String[]{
                TNoRw.getText(),
                Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
                KdDokter.getText(),
                KdPoli.getText(),
                DiagnosisMedis.getText(),
                RiwayatAsupan1.getSelectedItem().toString(),
                SkorRiwayatAsupan1.getText(),
                LamaAsupanMenurun.getText(),
                RiwayatAsupan2.getSelectedItem().toString(),
                SkorRiwayatAsupan2.getText(),
                BeratBadan3.getSelectedItem().toString(),
                SkorBeratBadan3.getText(),
                BBBiasanya.getText(),
                BBSaatIni.getText(),
                BesarnyaKehilangan.getText(),
                BeratBadan4.getSelectedItem().toString(),
                SkorBeratBadan4.getText(),
                BeratBadan5.getSelectedItem().toString(),
                SkorBeratBadan5.getText(),
                Gejala6.getSelectedItem().toString(),
                SkorGejala6.getText(),
                Gejala7.getSelectedItem().toString(),
                SkorGejala7.getText(),
                Gejala8.getSelectedItem().toString(),
                SkorGejala8.getText(),
                Gejala9.getSelectedItem().toString(),
                SkorGejala9.getText(),
                Gejala10.getSelectedItem().toString(),
                SkorGejala10.getText(),
                Gejala11.getSelectedItem().toString(),
                SkorGejala11.getText(),
                Gejala12.getSelectedItem().toString(),
                SkorGejala12.getText(),
                Gejala13.getSelectedItem().toString(),
                SkorGejala13.getText(),
                Gejala14.getSelectedItem().toString(),
                SkorGejala14.getText(),
                Gejala15.getSelectedItem().toString(),
                SkorGejala15.getText(),
                KapasitasFungsional16.getSelectedItem().toString(),
                SkorKapasitasFungsional16.getText(),
                KapasitasFungsional17.getSelectedItem().toString(),
                SkorKapasitasFungsional17.getText(),
                Penyakit18.getSelectedItem().toString(),
                SkorPenyakit18.getText(),
                PemFis19.getSelectedItem().toString(),
                SkorPemFis19.getText(),
                PemFis20.getSelectedItem().toString(),
                SkorPemFis20.getText(),
                PemFis21.getSelectedItem().toString(),
                SkorPemFis21.getText(),
                PemFis22.getSelectedItem().toString(),
                SkorPemFis22.getText(),
                PemFis23.getSelectedItem().toString(),
                SkorPemFis23.getText(),
                PemFis24.getSelectedItem().toString(),
                SkorPemFis24.getText(),
                PemFis25.getSelectedItem().toString(),
                SkorPemFis25.getText(),
                PemFis26.getSelectedItem().toString(),
                SkorPemFis26.getText(),
                PemFis27.getSelectedItem().toString(),
                SkorPemFis27.getText(),
                PemFis28.getSelectedItem().toString(),
                SkorPemFis28.getText(),
                PemFis29.getSelectedItem().toString(),
                SkorPemFis29.getText(),
                PemFis30.getSelectedItem().toString(),
                SkorPemFis30.getText(),
                PemFis31.getSelectedItem().toString(),
                SkorPemFis31.getText(),
                Lab32.getSelectedItem().toString(),
                SkorLab32.getText(),
                Lab33.getSelectedItem().toString(),
                SkorLab33.getText(),
                FaktorKontribusi34.getSelectedItem().toString(),
                SkorFaktorKontribusi34.getText(),
                FaktorKontribusi35.getSelectedItem().toString(),
                SkorFaktorKontribusi35.getText(),
                SkorSGA.getText(),
                Catatan.getText()
                })==true){
                    emptTeks();
                    tampil();
                    TabRawat.setSelectedIndex(1);
            }
        }
    
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }
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
        if(tbObat.getSelectedRow()>-1){
            if(akses.getkode().equals("Admin Utama")){
                hapus();
            }else{
                if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                    hapus();
                }else{
                    JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
                }
            }
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
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
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                        ganti();
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh dokter yang bersangkutan..!!");
                    }
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit,TCari);}
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
            TCari.setText("");
            tampil();
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if((evt.getClickCount()==2)&&(tbObat.getSelectedColumn()==0)){
                TabRawat.setSelectedIndex(0);
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
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
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
//        Valid.pindah(evt,DiagnosaTambahan,TglAsuhan);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==1){
            tampil();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void TglAsuhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglAsuhanKeyPressed
//        Valid.pindah2(evt,BtnDokter,Anamnesis);
    }//GEN-LAST:event_TglAsuhanKeyPressed

    private void BeratBadan4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BeratBadan4KeyPressed
//        Valid.pindah(evt,Hasil,Catatan);
    }//GEN-LAST:event_BeratBadan4KeyPressed

    private void RiwayatAsupan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RiwayatAsupan1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_RiwayatAsupan1KeyPressed

    private void RiwayatAsupan2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RiwayatAsupan2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_RiwayatAsupan2KeyPressed

    private void BeratBadan3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BeratBadan3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BeratBadan3KeyPressed

    private void BeratBadan5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BeratBadan5KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BeratBadan5KeyPressed

    private void Gejala6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala6KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala6KeyPressed

    private void Gejala7KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala7KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala7KeyPressed

    private void Gejala8KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala8KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala8KeyPressed

    private void Gejala9KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala9KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala9KeyPressed

    private void Gejala10KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala10KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala10KeyPressed

    private void Gejala11KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala11KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala11KeyPressed

    private void Gejala12KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala12KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala12KeyPressed

    private void Gejala13KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala13KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala13KeyPressed

    private void Gejala14KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala14KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala14KeyPressed

    private void Gejala15KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Gejala15KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala15KeyPressed

    private void KapasitasFungsional16KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KapasitasFungsional16KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KapasitasFungsional16KeyPressed

    private void KapasitasFungsional17KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KapasitasFungsional17KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KapasitasFungsional17KeyPressed

    private void Penyakit18KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Penyakit18KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Penyakit18KeyPressed

    private void PemFis19KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis19KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis19KeyPressed

    private void PemFis20KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis20KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis20KeyPressed

    private void PemFis21KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis21KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis21KeyPressed

    private void PemFis22KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis22KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis22KeyPressed

    private void PemFis23KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis23KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis23KeyPressed

    private void PemFis24KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis24KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis24KeyPressed

    private void PemFis25KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis25KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis25KeyPressed

    private void PemFis26KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis26KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis26KeyPressed

    private void PemFis27KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis27KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis27KeyPressed

    private void PemFis28KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis28KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis28KeyPressed

    private void PemFis29KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis29KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis29KeyPressed

    private void PemFis30KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis30KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis30KeyPressed

    private void PemFis31KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemFis31KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PemFis31KeyPressed

    private void Lab32KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Lab32KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Lab32KeyPressed

    private void Lab33KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Lab33KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Lab33KeyPressed

    private void FaktorKontribusi34KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FaktorKontribusi34KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_FaktorKontribusi34KeyPressed

    private void FaktorKontribusi35KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FaktorKontribusi35KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_FaktorKontribusi35KeyPressed

    private void RiwayatAsupan1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_RiwayatAsupan1ItemStateChanged
    isCombo1();        // TODO add your handling code here:
    }//GEN-LAST:event_RiwayatAsupan1ItemStateChanged

    private void KapasitasFungsional16ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_KapasitasFungsional16ItemStateChanged
        isCombo16();        // TODO add your handling code here:
    }//GEN-LAST:event_KapasitasFungsional16ItemStateChanged

    private void FaktorKontribusi35ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_FaktorKontribusi35ItemStateChanged
isCombo35();        // TODO add your handling code here:
    }//GEN-LAST:event_FaktorKontribusi35ItemStateChanged

    private void FaktorKontribusi34ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_FaktorKontribusi34ItemStateChanged
isCombo34();         // TODO add your handling code here:
    }//GEN-LAST:event_FaktorKontribusi34ItemStateChanged

    private void Lab33ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Lab33ItemStateChanged
isCombo33();        // TODO add your handling code here:
    }//GEN-LAST:event_Lab33ItemStateChanged

    private void Lab32ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Lab32ItemStateChanged
isCombo32();         // TODO add your handling code here:
    }//GEN-LAST:event_Lab32ItemStateChanged

    private void PemFis31ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis31ItemStateChanged
isCombo31();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis31ItemStateChanged

    private void PemFis30ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis30ItemStateChanged
isCombo30();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis30ItemStateChanged

    private void PemFis29ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis29ItemStateChanged
isCombo29();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis29ItemStateChanged

    private void PemFis28ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis28ItemStateChanged
isCombo28();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis28ItemStateChanged

    private void PemFis27ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis27ItemStateChanged
isCombo27();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis27ItemStateChanged

    private void PemFis26ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis26ItemStateChanged
isCombo26();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis26ItemStateChanged

    private void PemFis25ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis25ItemStateChanged
isCombo25();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis25ItemStateChanged

    private void PemFis24ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis24ItemStateChanged
isCombo24();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis24ItemStateChanged

    private void PemFis23ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis23ItemStateChanged
isCombo23();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis23ItemStateChanged

    private void PemFis22ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis22ItemStateChanged
isCombo22();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis22ItemStateChanged

    private void PemFis21ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis21ItemStateChanged
isCombo21();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis21ItemStateChanged

    private void PemFis20ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis20ItemStateChanged
isCombo20();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis20ItemStateChanged

    private void PemFis19ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PemFis19ItemStateChanged
isCombo19();         // TODO add your handling code here:
    }//GEN-LAST:event_PemFis19ItemStateChanged

    private void Penyakit18ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Penyakit18ItemStateChanged
isCombo18();        // TODO add your handling code here:
    }//GEN-LAST:event_Penyakit18ItemStateChanged

    private void KapasitasFungsional17ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_KapasitasFungsional17ItemStateChanged
isCombo17();        // TODO add your handling code here:
    }//GEN-LAST:event_KapasitasFungsional17ItemStateChanged

    private void Gejala15ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala15ItemStateChanged
isCombo15();        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala15ItemStateChanged

    private void Gejala14ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala14ItemStateChanged
isCombo14();        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala14ItemStateChanged

    private void Gejala13ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala13ItemStateChanged
isCombo13();        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala13ItemStateChanged

    private void Gejala12ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala12ItemStateChanged
isCombo12();        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala12ItemStateChanged

    private void Gejala11ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala11ItemStateChanged
isCombo11();        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala11ItemStateChanged

    private void Gejala10ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala10ItemStateChanged
isCombo10();        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala10ItemStateChanged

    private void Gejala9ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala9ItemStateChanged
isCombo9();        // TODO add your handling code here:
    }//GEN-LAST:event_Gejala9ItemStateChanged

    private void Gejala8ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala8ItemStateChanged
isCombo8();          // TODO add your handling code here:
    }//GEN-LAST:event_Gejala8ItemStateChanged

    private void Gejala7ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala7ItemStateChanged
isCombo7();          // TODO add your handling code here:
    }//GEN-LAST:event_Gejala7ItemStateChanged

    private void Gejala6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Gejala6ItemStateChanged
isCombo6();          // TODO add your handling code here:
    }//GEN-LAST:event_Gejala6ItemStateChanged

    private void BeratBadan5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_BeratBadan5ItemStateChanged
isCombo5();          // TODO add your handling code here:
    }//GEN-LAST:event_BeratBadan5ItemStateChanged

    private void BeratBadan4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_BeratBadan4ItemStateChanged
isCombo4();          // TODO add your handling code here:
    }//GEN-LAST:event_BeratBadan4ItemStateChanged

    private void BeratBadan3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_BeratBadan3ItemStateChanged
isCombo3();          // TODO add your handling code here:
    }//GEN-LAST:event_BeratBadan3ItemStateChanged

    private void RiwayatAsupan2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_RiwayatAsupan2ItemStateChanged
isCombo2();          // TODO add your handling code here:
    }//GEN-LAST:event_RiwayatAsupan2ItemStateChanged

    private void SkorSGAKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkorSGAKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_SkorSGAKeyReleased

    private void SkorSGAInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_SkorSGAInputMethodTextChanged
     
    }//GEN-LAST:event_SkorSGAInputMethodTextChanged

    private void SkorSGAKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkorSGAKeyTyped
        evt.setKeyChar(Character.toUpperCase(evt.getKeyChar()));        // TODO add your handling code here:
    }//GEN-LAST:event_SkorSGAKeyTyped

    private void BtnCetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCetakActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select logo from setting"));            
            param.put("tanggal1", Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
            param.put("tanggal2", Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
            param.put("Kode_Dokter",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",KdDokter.getText()));           
            param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+KdDokter.getText()+".png");
            String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
            String logoPath = projectDir + "/setting/logopdgki.png"; // Jalur relatif dari folder proyek
            param.put("logopdgki", logoPath);
            

            String query = "SELECT reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, " +
                           "reg_periksa.umurdaftar, reg_periksa.sttsumur, pasien.alamat, pasien.agama, pasien.pnd AS pendidikan, pasien.pekerjaan, " +
                           "(SELECT kamar_inap.tgl_masuk FROM kamar_inap WHERE kamar_inap.no_rawat = reg_periksa.no_rawat ORDER BY kamar_inap.tgl_masuk ASC LIMIT 1) AS tgl_masuk, " +
                           "(SELECT pemeriksaan_ranap.tinggi FROM pemeriksaan_ranap WHERE pemeriksaan_ranap.no_rawat = reg_periksa.no_rawat ORDER BY pemeriksaan_ranap.tgl_perawatan ASC LIMIT 1) AS tinggi_badan, " +
                           "(SELECT bangsal.nm_bangsal FROM kamar_inap INNER JOIN kamar ON kamar_inap.kd_kamar = kamar.kd_kamar INNER JOIN bangsal ON kamar.kd_bangsal = bangsal.kd_bangsal " +
                           " WHERE kamar_inap.no_rawat = reg_periksa.no_rawat ORDER BY kamar_inap.tgl_masuk ASC LIMIT 1) AS kamar, " +
                           "sga.tanggal, sga.kd_dokter, dokter.nm_dokter, sga.kd_kamar, sga.DiagnosisMedis, " +
                           "sga.RiwayatAsupan1, sga.SkorRiwayatAsupan1, sga.LamaAsupanMenurun, sga.RiwayatAsupan2, sga.SkorRiwayatAsupan2, " +
                           "sga.BeratBadan3, sga.SkorBeratBadan3, sga.BBBiasanya, sga.BBSaatIni, sga.BesarnyaKehilangan, " +
                           "sga.BeratBadan4, sga.SkorBeratBadan4, sga.BeratBadan5, sga.SkorBeratBadan5, " +
                           "sga.Gejala6, sga.SkorGejala6, sga.Gejala7, sga.SkorGejala7, sga.Gejala8, sga.SkorGejala8, " +
                           "sga.Gejala9, sga.SkorGejala9, sga.Gejala10, sga.SkorGejala10, sga.Gejala11, sga.SkorGejala11, " +
                           "sga.Gejala12, sga.SkorGejala12, sga.Gejala13, sga.SkorGejala13, sga.Gejala14, sga.SkorGejala14, " +
                           "sga.Gejala15, sga.SkorGejala15, " +
                           "sga.KapasitasFungsional16, sga.SkorKapasitasFungsional16, sga.KapasitasFungsional17, sga.SkorKapasitasFungsional17, " +
                           "sga.Penyakit18, sga.SkorPenyakit18, " +
                           "sga.PemFis19, sga.SkorPemFis19, sga.PemFis20, sga.SkorPemFis20, sga.PemFis21, sga.SkorPemFis21, " +
                           "sga.PemFis22, sga.SkorPemFis22, sga.PemFis23, sga.SkorPemFis23, sga.PemFis24, sga.SkorPemFis24, " +
                           "sga.PemFis25, sga.SkorPemFis25, sga.PemFis26, sga.SkorPemFis26, sga.PemFis27, sga.SkorPemFis27, " +
                           "sga.PemFis28, sga.SkorPemFis28, sga.PemFis29, sga.SkorPemFis29, sga.PemFis30, sga.SkorPemFis30, " +
                           "sga.PemFis31, sga.SkorPemFis31, " +
                           "sga.Lab32, sga.SkorLab32, sga.Lab33, sga.SkorLab33, " +
                           "sga.FaktorKontribusi34, sga.SkorFaktorKontribusi34, sga.FaktorKontribusi35, sga.SkorFaktorKontribusi35, " +
                           "sga.SkorSGA, sga.Catatan " +
                           "FROM sga_malnutrisi sga " +
                           "INNER JOIN reg_periksa ON sga.no_rawat = reg_periksa.no_rawat " +
                           "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
                           "INNER JOIN dokter ON sga.kd_dokter = dokter.kd_dokter " +
                           "WHERE sga.no_rawat ='" + tbObat.getValueAt(tbObat.getSelectedRow(),0).toString() + "'";


            Valid.MyReportqry("rptFormulirSGA.jasper", "report", "::[ Formulir SGA Malnutrisi ]::", query, param);
        } catch (Exception e) {
            System.out.println("Error saat mencetak SGA: " + e);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnCetakActionPerformed

    private void BtnCetakKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCetakKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCetakKeyPressed

    private void BtnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUploadActionPerformed

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String timestamp = sdf.format(new Date());

        FileName = timestamp + "_" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().replace("/", "") + "_FormulirSGA";

        CreatePDF(FileName);
        ConvertPDFtoJPG(FileName);
        UploadJPG(FileName, "berkasrawat/pages/upload/");
        HapusJPG();

        ppBerkasDigitalBtnPrintActionPerformed(evt);

    }//GEN-LAST:event_BtnUploadActionPerformed

    private void BtnUploadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUploadKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUploadKeyPressed

    private void BBBiasanyaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BBBiasanyaKeyReleased

        float darah1 = Float.parseFloat(BBBiasanya.getText());
        float hsl = darah1;
        BesarnyaKehilangan.setText(String.valueOf(hsl));
    }//GEN-LAST:event_BBBiasanyaKeyReleased

    private void BBSaatIniKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BBSaatIniKeyReleased

        float darah1 = Float.parseFloat(BBBiasanya.getText());
        float darah2 = Float.parseFloat(BBSaatIni.getText());
        
        
        float hsl = darah1+darah2;
        BesarnyaKehilangan.setText(String.valueOf(hsl));
    }//GEN-LAST:event_BBSaatIniKeyReleased

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            CustomFormSGAMalnutrisi dialog = new CustomFormSGAMalnutrisi(new javax.swing.JFrame(), true);
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
    private widget.TextBox BBBiasanya;
    private widget.TextBox BBSaatIni;
    private widget.ComboBox BeratBadan3;
    private widget.ComboBox BeratBadan4;
    private widget.ComboBox BeratBadan5;
    private widget.TextBox BesarnyaKehilangan;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCetak;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnSimpan;
    private widget.Button BtnUpload;
    private widget.TextArea Catatan;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextBox DiagnosisMedis;
    private widget.ComboBox FaktorKontribusi34;
    private widget.ComboBox FaktorKontribusi35;
    private widget.PanelBiasa FormInput;
    private widget.ComboBox Gejala10;
    private widget.ComboBox Gejala11;
    private widget.ComboBox Gejala12;
    private widget.ComboBox Gejala13;
    private widget.ComboBox Gejala14;
    private widget.ComboBox Gejala15;
    private widget.ComboBox Gejala6;
    private widget.ComboBox Gejala7;
    private widget.ComboBox Gejala8;
    private widget.ComboBox Gejala9;
    private widget.TextBox Jk;
    private widget.ComboBox KapasitasFungsional16;
    private widget.ComboBox KapasitasFungsional17;
    private widget.TextBox KdDokter;
    private widget.TextBox KdPoli;
    private widget.Label LCount;
    private widget.ComboBox Lab32;
    private widget.ComboBox Lab33;
    private widget.TextBox LamaAsupanMenurun;
    private widget.editorpane LoadHTML;
    private widget.TextBox NmDokter;
    private widget.TextBox NmPoli;
    private widget.ComboBox PemFis19;
    private widget.ComboBox PemFis20;
    private widget.ComboBox PemFis21;
    private widget.ComboBox PemFis22;
    private widget.ComboBox PemFis23;
    private widget.ComboBox PemFis24;
    private widget.ComboBox PemFis25;
    private widget.ComboBox PemFis26;
    private widget.ComboBox PemFis27;
    private widget.ComboBox PemFis28;
    private widget.ComboBox PemFis29;
    private widget.ComboBox PemFis30;
    private widget.ComboBox PemFis31;
    private widget.ComboBox Penyakit18;
    private widget.TextBox2 PerubahanBB;
    private widget.ComboBox RiwayatAsupan1;
    private widget.ComboBox RiwayatAsupan2;
    private widget.ScrollPane Scroll;
    private widget.TextBox SkorBeratBadan3;
    private widget.TextBox SkorBeratBadan4;
    private widget.TextBox SkorBeratBadan5;
    private widget.TextBox SkorFaktorKontribusi34;
    private widget.TextBox SkorFaktorKontribusi35;
    private widget.TextBox SkorGejala10;
    private widget.TextBox SkorGejala11;
    private widget.TextBox SkorGejala12;
    private widget.TextBox SkorGejala13;
    private widget.TextBox SkorGejala14;
    private widget.TextBox SkorGejala15;
    private widget.TextBox SkorGejala6;
    private widget.TextBox SkorGejala7;
    private widget.TextBox SkorGejala8;
    private widget.TextBox SkorGejala9;
    private widget.TextBox SkorKapasitasFungsional16;
    private widget.TextBox SkorKapasitasFungsional17;
    private widget.TextBox SkorLab32;
    private widget.TextBox SkorLab33;
    private widget.TextBox SkorPemFis19;
    private widget.TextBox SkorPemFis20;
    private widget.TextBox SkorPemFis21;
    private widget.TextBox SkorPemFis22;
    private widget.TextBox SkorPemFis23;
    private widget.TextBox SkorPemFis24;
    private widget.TextBox SkorPemFis25;
    private widget.TextBox SkorPemFis26;
    private widget.TextBox SkorPemFis27;
    private widget.TextBox SkorPemFis28;
    private widget.TextBox SkorPemFis29;
    private widget.TextBox SkorPemFis30;
    private widget.TextBox SkorPemFis31;
    private widget.TextBox SkorPenyakit18;
    private widget.TextBox SkorRiwayatAsupan1;
    private widget.TextBox SkorRiwayatAsupan2;
    private widget.TextBox2 SkorSGA;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal TglAsuhan;
    private widget.TextBox TglLahir;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup10;
    private javax.swing.ButtonGroup buttonGroup11;
    private javax.swing.ButtonGroup buttonGroup12;
    private javax.swing.ButtonGroup buttonGroup13;
    private javax.swing.ButtonGroup buttonGroup14;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.ButtonGroup buttonGroup9;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private widget.Label label11;
    private widget.Label label12;
    private widget.Label label14;
    private widget.Label label15;
    private widget.Label label16;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.Label label21;
    private widget.Label label22;
    private widget.Label label23;
    private widget.Label label24;
    private widget.Label label25;
    private widget.Label label26;
    private widget.Label label27;
    private widget.Label label28;
    private widget.Label label29;
    private widget.Label label30;
    private widget.Label label31;
    private widget.Label label32;
    private widget.Label label33;
    private widget.Label label34;
    private widget.Label label35;
    private widget.Label label36;
    private widget.Label label37;
    private widget.Label label38;
    private widget.Label label39;
    private widget.Label label40;
    private widget.Label label41;
    private widget.Label label42;
    private widget.Label label43;
    private widget.Label label44;
    private widget.Label label45;
    private widget.Label label46;
    private widget.Label label47;
    private widget.Label label48;
    private widget.Label label49;
    private widget.Label label50;
    private widget.Label label51;
    private widget.Label label52;
    private widget.Label label53;
    private widget.Label label54;
    private widget.Label label55;
    private widget.Label label56;
    private widget.Label label57;
    private widget.Label label58;
    private widget.Label label59;
    private widget.Label label60;
    private widget.Label label61;
    private widget.Label label62;
    private widget.Label label63;
    private widget.Label label64;
    private widget.Label label65;
    private widget.Label label66;
    private widget.Label label67;
    private widget.Label label68;
    private widget.Label label69;
    private widget.Label label70;
    private widget.Label label71;
    private widget.Label label72;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            if(TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                        "select bangsal.nm_bangsal,kamar.kd_kamar,reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,sga_malnutrisi.tanggal,sga_malnutrisi.kd_dokter,sga_malnutrisi.kd_kamar,"+
                        
                        "sga_malnutrisi.DiagnosisMedis,sga_malnutrisi.RiwayatAsupan1,sga_malnutrisi.SkorRiwayatAsupan1,sga_malnutrisi.LamaAsupanMenurun,sga_malnutrisi.RiwayatAsupan2,sga_malnutrisi.SkorRiwayatAsupan2,sga_malnutrisi.BeratBadan3,sga_malnutrisi.SkorBeratBadan3,"+
                        "sga_malnutrisi.BBBiasanya,sga_malnutrisi.BBSaatIni,sga_malnutrisi.BesarnyaKehilangan,sga_malnutrisi.BeratBadan4,sga_malnutrisi.SkorBeratBadan4,sga_malnutrisi.BeratBadan5,sga_malnutrisi.SkorBeratBadan5,sga_malnutrisi.Gejala6,sga_malnutrisi.SkorGejala6,"+
                        "sga_malnutrisi.Gejala7,sga_malnutrisi.SkorGejala7,sga_malnutrisi.Gejala8,sga_malnutrisi.SkorGejala8,sga_malnutrisi.Gejala9,sga_malnutrisi.SkorGejala9,sga_malnutrisi.Gejala10,sga_malnutrisi.SkorGejala10,sga_malnutrisi.Gejala11,sga_malnutrisi.SkorGejala11,"+
                        "sga_malnutrisi.Gejala12,sga_malnutrisi.SkorGejala12,sga_malnutrisi.Gejala13,sga_malnutrisi.SkorGejala13,sga_malnutrisi.Gejala14,sga_malnutrisi.SkorGejala14,sga_malnutrisi.Gejala15,sga_malnutrisi.SkorGejala15,sga_malnutrisi.KapasitasFungsional16,"+
                        "sga_malnutrisi.SkorKapasitasFungsional16,sga_malnutrisi.KapasitasFungsional17,sga_malnutrisi.SkorKapasitasFungsional17,sga_malnutrisi.Penyakit18,"+
                        "sga_malnutrisi.SkorPenyakit18,sga_malnutrisi.PemFis19,sga_malnutrisi.SkorPemFis19,sga_malnutrisi.PemFis20,sga_malnutrisi.SkorPemFis20,sga_malnutrisi.PemFis21,sga_malnutrisi.SkorPemFis21,sga_malnutrisi.PemFis22,"+
                        "sga_malnutrisi.SkorPemFis22,sga_malnutrisi.PemFis23,sga_malnutrisi.SkorPemFis23,sga_malnutrisi.PemFis24,sga_malnutrisi.SkorPemFis24,sga_malnutrisi.PemFis25,sga_malnutrisi.SkorPemFis25,sga_malnutrisi.PemFis26,"+
                        "sga_malnutrisi.SkorPemFis26,sga_malnutrisi.PemFis27,sga_malnutrisi.SkorPemFis27,sga_malnutrisi.PemFis28,sga_malnutrisi.SkorPemFis28,sga_malnutrisi.PemFis29,sga_malnutrisi.SkorPemFis29,sga_malnutrisi.PemFis30,"+
                        "sga_malnutrisi.SkorPemFis30,sga_malnutrisi.PemFis31,sga_malnutrisi.SkorPemFis31,sga_malnutrisi.Lab32,sga_malnutrisi.SkorLab32,sga_malnutrisi.Lab33,sga_malnutrisi.SkorLab33,sga_malnutrisi.FaktorKontribusi34,"+
                        "sga_malnutrisi.SkorFaktorKontribusi34,sga_malnutrisi.FaktorKontribusi35,sga_malnutrisi.SkorFaktorKontribusi35,sga_malnutrisi.SkorSGA,sga_malnutrisi.Catatan,"+

                        "dokter.nm_dokter from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join sga_malnutrisi on reg_periksa.no_rawat=sga_malnutrisi.no_rawat "+
                        "inner join kamar on sga_malnutrisi.kd_kamar=kamar.kd_kamar "+
                        "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                        "inner join dokter on sga_malnutrisi.kd_dokter=dokter.kd_dokter where "+
                        "sga_malnutrisi.tanggal between ? and ? order by sga_malnutrisi.tanggal");
            }else{
                ps=koneksi.prepareStatement(
                        "select bangsal.nm_bangsal,kamar.kd_kamar,reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,sga_malnutrisi.tanggal,sga_malnutrisi.kd_dokter,sga_malnutrisi.kd_kamar,"+
                        
                        "sga_malnutrisi.DiagnosisMedis,sga_malnutrisi.RiwayatAsupan1,sga_malnutrisi.SkorRiwayatAsupan1,sga_malnutrisi.LamaAsupanMenurun,sga_malnutrisi.RiwayatAsupan2,sga_malnutrisi.SkorRiwayatAsupan2,sga_malnutrisi.BeratBadan3,sga_malnutrisi.SkorBeratBadan3,"+
                        "sga_malnutrisi.BBBiasanya,sga_malnutrisi.BBSaatIni,sga_malnutrisi.BesarnyaKehilangan,sga_malnutrisi.BeratBadan4,sga_malnutrisi.SkorBeratBadan4,sga_malnutrisi.BeratBadan5,sga_malnutrisi.SkorBeratBadan5,sga_malnutrisi.Gejala6,sga_malnutrisi.SkorGejala6,"+
                        "sga_malnutrisi.Gejala7,sga_malnutrisi.SkorGejala7,sga_malnutrisi.Gejala8,sga_malnutrisi.SkorGejala8,sga_malnutrisi.Gejala9,sga_malnutrisi.SkorGejala9,sga_malnutrisi.Gejala10,sga_malnutrisi.SkorGejala10,sga_malnutrisi.Gejala11,sga_malnutrisi.SkorGejala11,"+
                        "sga_malnutrisi.Gejala12,sga_malnutrisi.SkorGejala12,sga_malnutrisi.Gejala13,sga_malnutrisi.SkorGejala13,sga_malnutrisi.Gejala14,sga_malnutrisi.SkorGejala14,sga_malnutrisi.Gejala15,sga_malnutrisi.SkorGejala15,sga_malnutrisi.KapasitasFungsional16,"+
                        "sga_malnutrisi.SkorKapasitasFungsional16,sga_malnutrisi.KapasitasFungsional17,sga_malnutrisi.SkorKapasitasFungsional17,sga_malnutrisi.Penyakit18,"+
                        "sga_malnutrisi.SkorPenyakit18,sga_malnutrisi.PemFis19,sga_malnutrisi.SkorPemFis19,sga_malnutrisi.PemFis20,sga_malnutrisi.SkorPemFis20,sga_malnutrisi.PemFis21,sga_malnutrisi.SkorPemFis21,sga_malnutrisi.PemFis22,"+
                        "sga_malnutrisi.SkorPemFis22,sga_malnutrisi.PemFis23,sga_malnutrisi.SkorPemFis23,sga_malnutrisi.PemFis24,sga_malnutrisi.SkorPemFis24,sga_malnutrisi.PemFis25,sga_malnutrisi.SkorPemFis25,sga_malnutrisi.PemFis26,"+
                        "sga_malnutrisi.SkorPemFis26,sga_malnutrisi.PemFis27,sga_malnutrisi.SkorPemFis27,sga_malnutrisi.PemFis28,sga_malnutrisi.SkorPemFis28,sga_malnutrisi.PemFis29,sga_malnutrisi.SkorPemFis29,sga_malnutrisi.PemFis30,"+
                        "sga_malnutrisi.SkorPemFis30,sga_malnutrisi.PemFis31,sga_malnutrisi.SkorPemFis31,sga_malnutrisi.Lab32,sga_malnutrisi.SkorLab32,sga_malnutrisi.Lab33,sga_malnutrisi.SkorLab33,sga_malnutrisi.FaktorKontribusi34,"+
                        "sga_malnutrisi.SkorFaktorKontribusi34,sga_malnutrisi.FaktorKontribusi35,sga_malnutrisi.SkorFaktorKontribusi35,sga_malnutrisi.SkorSGA,sga_malnutrisi.Catatan,"+
                                
                        "dokter.nm_dokter from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join sga_malnutrisi on reg_periksa.no_rawat=sga_malnutrisi.no_rawat "+
                        "inner join kamar on sga_malnutrisi.kd_kamar=kamar.kd_kamar "+
                        "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                        
                        "inner join dokter on sga_malnutrisi.kd_dokter=dokter.kd_dokter where "+
                        "sga_malnutrisi.tanggal between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                        "sga_malnutrisi.kd_dokter like ? or dokter.nm_dokter like ?) order by sga_malnutrisi.tanggal");
            }
                
            try {
                if(TCari.getText().trim().equals("")){
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                }else{
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                }   
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_lahir"),rs.getString("jk"),rs.getString("kd_dokter"),rs.getString("nm_dokter"),rs.getString("tanggal"),rs.getString("nm_bangsal"),
                        
                        rs.getString("DiagnosisMedis"),rs.getString("RiwayatAsupan1"),rs.getString("SkorRiwayatAsupan1"),rs.getString("LamaAsupanMenurun"),rs.getString("RiwayatAsupan2"),rs.getString("SkorRiwayatAsupan2"),rs.getString("BeratBadan3"),
                        rs.getString("SkorBeratBadan3"),rs.getString("BBBiasanya"),rs.getString("BBSaatIni"),rs.getString("BesarnyaKehilangan"),rs.getString("BeratBadan4"),rs.getString("SkorBeratBadan4"),
                        rs.getString("BeratBadan5"),rs.getString("SkorBeratBadan5"),rs.getString("Gejala6"),rs.getString("SkorGejala6"),rs.getString("Gejala7"),rs.getString("SkorGejala7"),rs.getString("Gejala8"),rs.getString("SkorGejala8"),
                        rs.getString("Gejala9"),rs.getString("SkorGejala9"),rs.getString("Gejala10"),rs.getString("SkorGejala10"),rs.getString("Gejala11"),rs.getString("SkorGejala11"),rs.getString("Gejala12"),rs.getString("SkorGejala12"),
                        rs.getString("Gejala13"),rs.getString("SkorGejala13"),rs.getString("Gejala14"),rs.getString("SkorGejala14"),rs.getString("Gejala15"),rs.getString("SkorGejala15"),rs.getString("KapasitasFungsional16"),rs.getString("SkorKapasitasFungsional16"),
                        rs.getString("KapasitasFungsional17"),rs.getString("SkorKapasitasFungsional17"),rs.getString("Penyakit18"),rs.getString("SkorPenyakit18"),rs.getString("PemFis19"),rs.getString("SkorPemFis19"),rs.getString("PemFis20"),rs.getString("SkorPemFis20"),
                        rs.getString("PemFis21"),rs.getString("SkorPemFis21"),rs.getString("PemFis22"),rs.getString("SkorPemFis22"),rs.getString("PemFis23"),rs.getString("SkorPemFis23"),rs.getString("PemFis24"),rs.getString("SkorPemFis24"),rs.getString("PemFis25"),
                        rs.getString("SkorPemFis25"),rs.getString("PemFis26"),rs.getString("SkorPemFis26"),rs.getString("PemFis27"),rs.getString("SkorPemFis27"),rs.getString("PemFis28"),rs.getString("SkorPemFis28"),rs.getString("PemFis29"),rs.getString("SkorPemFis29"),
                        rs.getString("PemFis30"),rs.getString("SkorPemFis30"),rs.getString("PemFis31"),rs.getString("SkorPemFis31"),rs.getString("Lab32"),rs.getString("SkorLab32"),rs.getString("Lab33"),rs.getString("SkorLab33"),rs.getString("FaktorKontribusi34"),
                        rs.getString("SkorFaktorKontribusi34"),rs.getString("FaktorKontribusi35"),rs.getString("SkorFaktorKontribusi35"),rs.getString("SkorSGA"),rs.getString("Catatan")
                        
                        
                       
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
    }

    public void emptTeks() {
        
        TglAsuhan.setDate(new Date());
        DiagnosisMedis.setText("");
        DiagnosisMedis.setText("");
                
        SkorRiwayatAsupan1.setText("A");
        LamaAsupanMenurun.setText("");

        SkorRiwayatAsupan2.setText("A");

        SkorBeratBadan3.setText("A");
        BBBiasanya.setText("");
        BBSaatIni.setText("");
        BesarnyaKehilangan.setText("");

        SkorBeratBadan4.setText("A");

        SkorBeratBadan5.setText("A");

        SkorGejala6.setText("A");

        SkorGejala7.setText("A");

        SkorGejala8.setText("A");

        SkorGejala9.setText("A");

        SkorGejala10.setText("A");

        SkorGejala11.setText("A");

        SkorGejala12.setText("A");

        SkorGejala13.setText("A");

        SkorGejala14.setText("A");

        SkorGejala15.setText("A");

        SkorKapasitasFungsional16.setText("A");

        SkorKapasitasFungsional17.setText("A");

        SkorPenyakit18.setText("A");

        SkorPemFis19.setText("A");

        SkorPemFis20.setText("A");

        SkorPemFis21.setText("A");

        SkorPemFis22.setText("A");

        SkorPemFis23.setText("A");

        SkorPemFis24.setText("A");

        SkorPemFis25.setText("A");

        SkorPemFis26.setText("A");

        SkorPemFis27.setText("A");

        SkorPemFis28.setText("A");

        SkorPemFis29.setText("A");

        SkorPemFis30.setText("A");

        SkorPemFis31.setText("A");

        SkorLab32.setText("A");

        SkorLab33.setText("A");

        SkorFaktorKontribusi34.setText("Tidak Ada");

        SkorFaktorKontribusi35.setText("Tidak Ada");
        SkorSGA.setText("A");
        Catatan.setText("");

        
    } 

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()); 
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            Jk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()); 
            Valid.SetTgl2(TglAsuhan,tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
            NmPoli.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            DiagnosisMedis.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            RiwayatAsupan1.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            SkorRiwayatAsupan1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            LamaAsupanMenurun.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            RiwayatAsupan2.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            SkorRiwayatAsupan2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            BeratBadan3.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            SkorBeratBadan3.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            BBBiasanya.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            BBSaatIni.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
            BesarnyaKehilangan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            BeratBadan4.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
            SkorBeratBadan4.setText(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
            BeratBadan5.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
            SkorBeratBadan5.setText(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString());
            Gejala6.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),24).toString());
            SkorGejala6.setText(tbObat.getValueAt(tbObat.getSelectedRow(),25).toString());
            Gejala7.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString());
            SkorGejala7.setText(tbObat.getValueAt(tbObat.getSelectedRow(),27).toString());
            Gejala8.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),28).toString());
            SkorGejala8.setText(tbObat.getValueAt(tbObat.getSelectedRow(),29).toString());
            Gejala9.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),30).toString());
            SkorGejala9.setText(tbObat.getValueAt(tbObat.getSelectedRow(),31).toString());
            Gejala10.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),32).toString());
            SkorGejala10.setText(tbObat.getValueAt(tbObat.getSelectedRow(),33).toString());
            Gejala11.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),34).toString());
            SkorGejala11.setText(tbObat.getValueAt(tbObat.getSelectedRow(),35).toString());
            Gejala12.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),36).toString());
            SkorGejala12.setText(tbObat.getValueAt(tbObat.getSelectedRow(),37).toString());
            Gejala13.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),38).toString());
            SkorGejala13.setText(tbObat.getValueAt(tbObat.getSelectedRow(),39).toString());
            Gejala14.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),40).toString());
            SkorGejala14.setText(tbObat.getValueAt(tbObat.getSelectedRow(),41).toString());
            Gejala15.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),42).toString());
            SkorGejala15.setText(tbObat.getValueAt(tbObat.getSelectedRow(),43).toString());
            KapasitasFungsional16.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),44).toString());
            SkorKapasitasFungsional16.setText(tbObat.getValueAt(tbObat.getSelectedRow(),45).toString());
            KapasitasFungsional17.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),46).toString());
            SkorKapasitasFungsional17.setText(tbObat.getValueAt(tbObat.getSelectedRow(),47).toString());
            Penyakit18.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),48).toString());
            SkorPenyakit18.setText(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString());
            PemFis19.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),50).toString());
            SkorPemFis19.setText(tbObat.getValueAt(tbObat.getSelectedRow(),51).toString());
            PemFis20.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),52).toString());
            SkorPemFis20.setText(tbObat.getValueAt(tbObat.getSelectedRow(),53).toString());
            PemFis21.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),54).toString());
            SkorPemFis21.setText(tbObat.getValueAt(tbObat.getSelectedRow(),55).toString());
            PemFis22.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),56).toString());
            SkorPemFis22.setText(tbObat.getValueAt(tbObat.getSelectedRow(),57).toString());
            PemFis23.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),58).toString());
            SkorPemFis23.setText(tbObat.getValueAt(tbObat.getSelectedRow(),59).toString());
            PemFis24.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),60).toString());
            SkorPemFis24.setText(tbObat.getValueAt(tbObat.getSelectedRow(),61).toString());
            PemFis25.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),62).toString());
            SkorPemFis25.setText(tbObat.getValueAt(tbObat.getSelectedRow(),63).toString());
            PemFis26.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),64).toString());
            SkorPemFis26.setText(tbObat.getValueAt(tbObat.getSelectedRow(),65).toString());
            PemFis27.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),66).toString());
            SkorPemFis27.setText(tbObat.getValueAt(tbObat.getSelectedRow(),67).toString());
            PemFis28.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),68).toString());
            SkorPemFis28.setText(tbObat.getValueAt(tbObat.getSelectedRow(),69).toString());
            PemFis29.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),70).toString());
            SkorPemFis29.setText(tbObat.getValueAt(tbObat.getSelectedRow(),71).toString());
            PemFis30.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),72).toString());
            SkorPemFis30.setText(tbObat.getValueAt(tbObat.getSelectedRow(),73).toString());
            PemFis31.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),74).toString());
            SkorPemFis31.setText(tbObat.getValueAt(tbObat.getSelectedRow(),75).toString());
            Lab32.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),76).toString());
            SkorLab32.setText(tbObat.getValueAt(tbObat.getSelectedRow(),77).toString());
            Lab33.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),78).toString());
            SkorLab33.setText(tbObat.getValueAt(tbObat.getSelectedRow(),79).toString());
            FaktorKontribusi34.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),80).toString());
            SkorFaktorKontribusi34.setText(tbObat.getValueAt(tbObat.getSelectedRow(),81).toString());
            FaktorKontribusi35.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),82).toString());
            SkorFaktorKontribusi35.setText(tbObat.getValueAt(tbObat.getSelectedRow(),83).toString());
            SkorSGA.setText(tbObat.getValueAt(tbObat.getSelectedRow(),84).toString());
            Catatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),85).toString());
        }
    }

    private void isRawat() {
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
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
public void isPasien() {
        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis=? ",TPasien,TNoRM.getText());
        Sequel.cariIsi("select if(jk='L','Laki-Laki','Perempuan') from pasien where no_rkm_medis=? ",Jk,TNoRM.getText());
        Sequel.cariIsi("select tgl_lahir from pasien where no_rkm_medis=? ",TglLahir,TNoRM.getText());
        Sequel.cariIsi("select k.kd_kamar FROM kamar_inap ki JOIN (SELECT kip.no_rawat, MAX(CONCAT(kip.tgl_masuk, ' ',kip.jam_masuk)) AS lr FROM kamar_inap kip WHERE kip.no_rawat ='"+TNoRw.getText()+"' GROUP BY kip.no_rawat) AS mk ON ki.no_rawat = mk.no_rawat AND ki.tgl_masuk = DATE(mk.lr) AND ki.jam_masuk = TIME(mk.lr) JOIN kamar k ON ki.kd_kamar = k.kd_kamar JOIN bangsal b ON k.kd_bangsal = b.kd_bangsal WHERE ki.no_rawat = '"+TNoRw.getText()+"' ",KdPoli);
        Sequel.cariIsi("select b.nm_bangsal FROM kamar_inap ki JOIN (SELECT kip.no_rawat, MAX(CONCAT(kip.tgl_masuk, ' ',kip.jam_masuk)) AS lr FROM kamar_inap kip WHERE kip.no_rawat ='"+TNoRw.getText()+"' GROUP BY kip.no_rawat) AS mk ON ki.no_rawat = mk.no_rawat AND ki.tgl_masuk = DATE(mk.lr) AND ki.jam_masuk = TIME(mk.lr) JOIN kamar k ON ki.kd_kamar = k.kd_kamar JOIN bangsal b ON k.kd_bangsal = b.kd_bangsal WHERE ki.no_rawat = '"+TNoRw.getText()+"'",NmPoli);
    }    
public void setNoRm(String norwt,Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);    
        
       
        
        try (PreparedStatement ps = koneksi.prepareStatement(
        "SELECT COUNT(*) FROM sga_malnutrisi WHERE no_rawat = ?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    TabRawat.setSelectedIndex(1); 
                    isRawat();                   
                    tampil();
                    isPasien();
                } else {
                    TabRawat.setSelectedIndex(0); 
                    isRawat();                                   }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }    
        
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getdata_resume_pasien());
        BtnHapus.setEnabled(akses.getdata_resume_pasien());
        BtnEdit.setEnabled(akses.getdata_resume_pasien());
        BtnEdit.setEnabled(akses.getdata_resume_pasien());
        if(akses.getjml2()>=1){
            KdDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokter.setText(akses.getkode());
            Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", NmDokter,KdDokter.getText());
            if(NmDokter.getText().equals("")){
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
            }
        }            
    }
    
    public void setTampil(){
       TabRawat.setSelectedIndex(1);
    }

    private void hapus() {
        if(Sequel.queryu2tf("delete from sga_malnutrisi where no_rawat=?",1,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            TabRawat.setSelectedIndex(1);
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    private void ganti() {
        
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else{
                if(Sequel.mengedittf("sga_malnutrisi","no_rawat=?","no_rawat=?,tanggal=?,kd_dokter=?,kd_kamar=?,DiagnosisMedis=?,RiwayatAsupan1=?,SkorRiwayatAsupan1=?,LamaAsupanMenurun=?," +
"                RiwayatAsupan2=?,SkorRiwayatAsupan2=?,BeratBadan3=?,SkorBeratBadan3=?,BBBiasanya=?,BBSaatIni=?,BesarnyaKehilangan=?,BeratBadan4=?," +
"                SkorBeratBadan4=?,BeratBadan5=?,SkorBeratBadan5=?,Gejala6=?,SkorGejala6=?,Gejala7=?,SkorGejala7=?,Gejala8=?,SkorGejala8=?," +
"                Gejala9=?,SkorGejala9=?,Gejala10=?,SkorGejala10=?,Gejala11=?,SkorGejala11=?,Gejala12=?,SkorGejala12=?,Gejala13=?," +
"                SkorGejala13=?,Gejala14=?,SkorGejala14=?,Gejala15=?,SkorGejala15=?,KapasitasFungsional16=?,SkorKapasitasFungsional16=?," +
"                KapasitasFungsional17=?,SkorKapasitasFungsional17=?,Penyakit18=?,SkorPenyakit18=?,PemFis19=?,SkorPemFis19=?,PemFis20=?," +
"                SkorPemFis20=?,PemFis21=?,SkorPemFis21=?,PemFis22=?,SkorPemFis22=?,PemFis23=?,SkorPemFis23=?,PemFis24=?,SkorPemFis24=?,"+
"                PemFis25=?,SkorPemFis25=?,PemFis26=?,SkorPemFis26=?,PemFis27=?,SkorPemFis27=?,PemFis28=?,SkorPemFis28=?,"+
"                PemFis29=?,SkorPemFis29=?,PemFis30=?,SkorPemFis30=?,PemFis31=?,SkorPemFis31=?,Lab32=?,SkorLab32=?,"+
"                Lab33=?,SkorLab33=?,FaktorKontribusi34=?,SkorFaktorKontribusi34=?,FaktorKontribusi35=?,SkorFaktorKontribusi35=?,SkorSGA=?,Catatan=?",82,new String[]{
                
                TNoRw.getText(),Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),KdDokter.getText(),KdPoli.getText(),
    
                DiagnosisMedis.getText(),
                RiwayatAsupan1.getSelectedItem().toString(),
                SkorRiwayatAsupan1.getText(),
                LamaAsupanMenurun.getText(),
                RiwayatAsupan2.getSelectedItem().toString(),
                SkorRiwayatAsupan2.getText(),
                BeratBadan3.getSelectedItem().toString(),
                SkorBeratBadan3.getText(),
                BBBiasanya.getText(),
                BBSaatIni.getText(),
                BesarnyaKehilangan.getText(),
                BeratBadan4.getSelectedItem().toString(),
                SkorBeratBadan4.getText(),
                BeratBadan5.getSelectedItem().toString(),
                SkorBeratBadan5.getText(),
                Gejala6.getSelectedItem().toString(),
                SkorGejala6.getText(),
                Gejala7.getSelectedItem().toString(),
                SkorGejala7.getText(),
                Gejala8.getSelectedItem().toString(),
                SkorGejala8.getText(),
                Gejala9.getSelectedItem().toString(),
                SkorGejala9.getText(),
                Gejala10.getSelectedItem().toString(),
                SkorGejala10.getText(),
                Gejala11.getSelectedItem().toString(),
                SkorGejala11.getText(),
                Gejala12.getSelectedItem().toString(),
                SkorGejala12.getText(),
                Gejala13.getSelectedItem().toString(),
                SkorGejala13.getText(),
                Gejala14.getSelectedItem().toString(),
                SkorGejala14.getText(),
                Gejala15.getSelectedItem().toString(),
                SkorGejala15.getText(),
                KapasitasFungsional16.getSelectedItem().toString(),
                SkorKapasitasFungsional16.getText(),
                KapasitasFungsional17.getSelectedItem().toString(),
                SkorKapasitasFungsional17.getText(),
                Penyakit18.getSelectedItem().toString(),
                SkorPenyakit18.getText(),
                PemFis19.getSelectedItem().toString(),
                SkorPemFis19.getText(),
                PemFis20.getSelectedItem().toString(),
                SkorPemFis20.getText(),
                PemFis21.getSelectedItem().toString(),
                SkorPemFis21.getText(),
                PemFis22.getSelectedItem().toString(),
                SkorPemFis22.getText(),
                PemFis23.getSelectedItem().toString(),
                SkorPemFis23.getText(),
                PemFis24.getSelectedItem().toString(),
                SkorPemFis24.getText(),
                PemFis25.getSelectedItem().toString(),
                SkorPemFis25.getText(),
                PemFis26.getSelectedItem().toString(),
                SkorPemFis26.getText(),
                PemFis27.getSelectedItem().toString(),
                SkorPemFis27.getText(),
                PemFis28.getSelectedItem().toString(),
                SkorPemFis28.getText(),
                PemFis29.getSelectedItem().toString(),
                SkorPemFis29.getText(),
                PemFis30.getSelectedItem().toString(),
                SkorPemFis30.getText(),
                PemFis31.getSelectedItem().toString(),
                SkorPemFis31.getText(),
                Lab32.getSelectedItem().toString(),
                SkorLab32.getText(),
                Lab33.getSelectedItem().toString(),
                SkorLab33.getText(),
                FaktorKontribusi34.getSelectedItem().toString(),
                SkorFaktorKontribusi34.getText(),
                FaktorKontribusi35.getSelectedItem().toString(),
                SkorFaktorKontribusi35.getText(),
                SkorSGA.getText(),
                Catatan.getText(),
                
                tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
                })==true){
                    emptTeks();
                    tampil();
                    TabRawat.setSelectedIndex(1);
            }
        }
    }
    
    private void isCombo1(){
        if(RiwayatAsupan1.getSelectedIndex()==0){
            SkorRiwayatAsupan1.setText("A");
        }else if(RiwayatAsupan1.getSelectedIndex()==1){
            SkorRiwayatAsupan1.setText("B");
        }else if(RiwayatAsupan1.getSelectedIndex()==2){
            SkorRiwayatAsupan1.setText("C");
        }else if(RiwayatAsupan1.getSelectedIndex()==3){
            SkorRiwayatAsupan1.setText("C");
        }
    }
    
    private void isCombo2(){
        if(RiwayatAsupan2.getSelectedIndex()==0){
            SkorRiwayatAsupan2.setText("A");
        }else if(RiwayatAsupan2.getSelectedIndex()==1){
            SkorRiwayatAsupan2.setText("B");
        }else if(RiwayatAsupan2.getSelectedIndex()==2){
            SkorRiwayatAsupan2.setText("C");
        }
    }
    
    private void isCombo3(){
        if(BeratBadan3.getSelectedIndex()==0){
            SkorBeratBadan3.setText("A");
        }else if(BeratBadan3.getSelectedIndex()==1){
            SkorBeratBadan3.setText("B");
        }else if(BeratBadan3.getSelectedIndex()==2){
            SkorBeratBadan3.setText("C");
        }
    }
    
    private void isCombo4(){
        if(BeratBadan4.getSelectedIndex()==0){
            SkorBeratBadan4.setText("A");
        }else if(BeratBadan4.getSelectedIndex()==1){
            SkorBeratBadan4.setText("B");
        }else if(BeratBadan4.getSelectedIndex()==2){
            SkorBeratBadan4.setText("C");
        }
    }
    
    private void isCombo5(){
        if(BeratBadan5.getSelectedIndex()==0){
            SkorBeratBadan5.setText("A");
        }else if(BeratBadan5.getSelectedIndex()==1){
            SkorBeratBadan5.setText("B");
        }else if(BeratBadan5.getSelectedIndex()==2){
            SkorBeratBadan5.setText("C");
        }
    }
    
    private void isCombo6(){
        if(Gejala6.getSelectedIndex()==0){
            SkorGejala6.setText("A");
        }else if(Gejala6.getSelectedIndex()==1){
            SkorGejala6.setText("B");
        }else if(Gejala6.getSelectedIndex()==2){
            SkorGejala6.setText("C");
        }
    }
    
    private void isCombo7(){
        if(Gejala7.getSelectedIndex()==0){
            SkorGejala7.setText("A");
        }else if(Gejala7.getSelectedIndex()==1){
            SkorGejala7.setText("B");
        }else if(Gejala7.getSelectedIndex()==2){
            SkorGejala7.setText("C");
        }
    }
    
    private void isCombo8(){
        if(Gejala8.getSelectedIndex()==0){
            SkorGejala8.setText("A");
        }else if(Gejala8.getSelectedIndex()==1){
            SkorGejala8.setText("B");
        }else if(Gejala8.getSelectedIndex()==2){
            SkorGejala8.setText("C");
        }
    }
    
    private void isCombo9(){
        if(Gejala9.getSelectedIndex()==0){
            SkorGejala9.setText("A");
        }else if(Gejala9.getSelectedIndex()==1){
            SkorGejala9.setText("B");
        }else if(Gejala9.getSelectedIndex()==2){
            SkorGejala9.setText("C");
        }
    }
    
    private void isCombo10(){
        if(Gejala10.getSelectedIndex()==0){
            SkorGejala10.setText("A");
        }else if(Gejala10.getSelectedIndex()==1){
            SkorGejala10.setText("B");
        }else if(Gejala10.getSelectedIndex()==2){
            SkorGejala10.setText("C");
        }
    }
    
    private void isCombo11(){
        if(Gejala11.getSelectedIndex()==0){
            SkorGejala11.setText("A");
        }else if(Gejala11.getSelectedIndex()==1){
            SkorGejala11.setText("B");
        }else if(Gejala11.getSelectedIndex()==2){
            SkorGejala11.setText("C");
        }
    }
    
    private void isCombo12(){
        if(Gejala12.getSelectedIndex()==0){
            SkorGejala12.setText("A");
        }else if(Gejala12.getSelectedIndex()==1){
            SkorGejala12.setText("B");
        }else if(Gejala12.getSelectedIndex()==2){
            SkorGejala12.setText("C");
        }
    }
    
    private void isCombo13(){
        if(Gejala13.getSelectedIndex()==0){
            SkorGejala13.setText("A");
        }else if(Gejala13.getSelectedIndex()==1){
            SkorGejala13.setText("B");
        }else if(Gejala13.getSelectedIndex()==2){
            SkorGejala13.setText("C");
        }
    }
    
    private void isCombo14(){
        if(Gejala14.getSelectedIndex()==0){
            SkorGejala14.setText("A");
        }else if(Gejala14.getSelectedIndex()==1){
            SkorGejala14.setText("B");
        }else if(Gejala14.getSelectedIndex()==2){
            SkorGejala14.setText("C");
        }
    }
    
    private void isCombo15(){
        if(Gejala15.getSelectedIndex()==0){
            SkorGejala15.setText("A");
        }else if(Gejala15.getSelectedIndex()==1){
            SkorGejala15.setText("B");
        }else if(Gejala15.getSelectedIndex()==2){
            SkorGejala15.setText("C");
        }
    }
    
    private void isCombo16(){
        if(KapasitasFungsional16.getSelectedIndex()==0){
            SkorKapasitasFungsional16.setText("A");
        }else if(KapasitasFungsional16.getSelectedIndex()==1){
            SkorKapasitasFungsional16.setText("B");
        }else if(KapasitasFungsional16.getSelectedIndex()==2){
            SkorKapasitasFungsional16.setText("C");
        }
    }
    
    private void isCombo17(){
        if(KapasitasFungsional17.getSelectedIndex()==0){
            SkorKapasitasFungsional17.setText("A");
        }else if(KapasitasFungsional17.getSelectedIndex()==1){
            SkorKapasitasFungsional17.setText("B");
        }else if(KapasitasFungsional17.getSelectedIndex()==2){
            SkorKapasitasFungsional17.setText("C");
        }
    }
    
    private void isCombo18(){
        if(Penyakit18.getSelectedIndex()==0){
            SkorPenyakit18.setText("A");
        }else if(Penyakit18.getSelectedIndex()==1){
            SkorPenyakit18.setText("B");
        }else if(Penyakit18.getSelectedIndex()==2){
            SkorPenyakit18.setText("B");
        }else if(Penyakit18.getSelectedIndex()==3){
            SkorPenyakit18.setText("C");
        }
    }
    
    private void isCombo19(){
        if(PemFis19.getSelectedIndex()==0){
            SkorPemFis19.setText("A");
        }else if(PemFis19.getSelectedIndex()==1){
            SkorPemFis19.setText("B");
        }else if(PemFis19.getSelectedIndex()==2){
            SkorPemFis19.setText("C");
        }
    }
    
    private void isCombo20(){
        if(PemFis20.getSelectedIndex()==0){
            SkorPemFis20.setText("A");
        }else if(PemFis20.getSelectedIndex()==1){
            SkorPemFis20.setText("B");
        }else if(PemFis20.getSelectedIndex()==2){
            SkorPemFis20.setText("C");
        }
    }
    
    private void isCombo21(){
        if(PemFis21.getSelectedIndex()==0){
            SkorPemFis21.setText("A");
        }else if(PemFis21.getSelectedIndex()==1){
            SkorPemFis21.setText("B");
        }else if(PemFis21.getSelectedIndex()==2){
            SkorPemFis21.setText("C");
        }
    }
    
    private void isCombo22(){
        if(PemFis22.getSelectedIndex()==0){
            SkorPemFis22.setText("A");
        }else if(PemFis22.getSelectedIndex()==1){
            SkorPemFis22.setText("B");
        }else if(PemFis22.getSelectedIndex()==2){
            SkorPemFis22.setText("C");
        }
    }
    
    private void isCombo23(){
        if(PemFis23.getSelectedIndex()==0){
            SkorPemFis23.setText("A");
        }else if(PemFis23.getSelectedIndex()==1){
            SkorPemFis23.setText("B");
        }else if(PemFis23.getSelectedIndex()==2){
            SkorPemFis23.setText("C");
        }
    }
    
    private void isCombo24(){
        if(PemFis24.getSelectedIndex()==0){
            SkorPemFis24.setText("A");
        }else if(PemFis24.getSelectedIndex()==1){
            SkorPemFis24.setText("B");
        }else if(PemFis24.getSelectedIndex()==2){
            SkorPemFis24.setText("C");
        }
    }
    
    private void isCombo25(){
        if(PemFis25.getSelectedIndex()==0){
            SkorPemFis25.setText("A");
        }else if(PemFis25.getSelectedIndex()==1){
            SkorPemFis25.setText("B");
        }else if(PemFis25.getSelectedIndex()==2){
            SkorPemFis25.setText("C");
        }
    }
    
    private void isCombo26(){
        if(PemFis26.getSelectedIndex()==0){
            SkorPemFis26.setText("A");
        }else if(PemFis26.getSelectedIndex()==1){
            SkorPemFis26.setText("B");
        }else if(PemFis26.getSelectedIndex()==2){
            SkorPemFis26.setText("C");
        }
    }
    
    private void isCombo27(){
        if(PemFis27.getSelectedIndex()==0){
            SkorPemFis27.setText("A");
        }else if(PemFis27.getSelectedIndex()==1){
            SkorPemFis27.setText("B");
        }else if(PemFis27.getSelectedIndex()==2){
            SkorPemFis27.setText("C");
        }
    }
    
    private void isCombo28(){
        if(PemFis28.getSelectedIndex()==0){
            SkorPemFis28.setText("A");
        }else if(PemFis28.getSelectedIndex()==1){
            SkorPemFis28.setText("B");
        }else if(PemFis28.getSelectedIndex()==2){
            SkorPemFis28.setText("C");
        }
    }
    
    private void isCombo29(){
        if(PemFis29.getSelectedIndex()==0){
            SkorPemFis29.setText("A");
        }else if(PemFis29.getSelectedIndex()==1){
            SkorPemFis29.setText("B");
        }else if(PemFis29.getSelectedIndex()==2){
            SkorPemFis29.setText("C");
        }
    }
    
    private void isCombo30(){
        if(PemFis30.getSelectedIndex()==0){
            SkorPemFis30.setText("A");
        }else if(PemFis30.getSelectedIndex()==1){
            SkorPemFis30.setText("B");
        }else if(PemFis30.getSelectedIndex()==2){
            SkorPemFis30.setText("C");
        }
    }
    
    private void isCombo31(){
        if(PemFis31.getSelectedIndex()==0){
            SkorPemFis31.setText("A");
        }else if(PemFis31.getSelectedIndex()==1){
            SkorPemFis31.setText("B");
        }else if(PemFis31.getSelectedIndex()==2){
            SkorPemFis31.setText("C");
        }
    }
    
    private void isCombo32(){
        if(Lab32.getSelectedIndex()==0){
            SkorLab32.setText("A");
        }else if(Lab32.getSelectedIndex()==1){
            SkorLab32.setText("B");
        }else if(Lab32.getSelectedIndex()==2){
            SkorLab32.setText("C");
        }
    }
    
    private void isCombo33(){
        if(Lab33.getSelectedIndex()==0){
            SkorLab33.setText("A");
        }else if(Lab33.getSelectedIndex()==1){
            SkorLab33.setText("B");
        }else if(Lab33.getSelectedIndex()==2){
            SkorLab33.setText("C");
        }
    }
    
    private void isCombo34(){
        if(FaktorKontribusi34.getSelectedIndex()==0){
            SkorFaktorKontribusi34.setText("Tidak Ada");
        }else if(FaktorKontribusi34.getSelectedIndex()==1){
            SkorFaktorKontribusi34.setText("Ada");
        }else if(FaktorKontribusi34.getSelectedIndex()==2){
            SkorFaktorKontribusi34.setText("Ada");
        }
    }
    
    private void isCombo35(){
        if(FaktorKontribusi35.getSelectedIndex()==0){
            SkorFaktorKontribusi35.setText("Tidak Ada");
        }else if(FaktorKontribusi35.getSelectedIndex()==1){
            SkorFaktorKontribusi35.setText("Ada");
        }else if(FaktorKontribusi35.getSelectedIndex()==2){
            SkorFaktorKontribusi35.setText("Ada");
        }
    }

public void hitungSkorSGA() {
    int countA = 0, countB = 0, countC = 0;
    int total = 0;

    // Ambil semua nilai skor dan hitung jumlah A, B, C
    String[] nilaiSkor = {
        SkorRiwayatAsupan1.getText().trim().toUpperCase(),
        SkorRiwayatAsupan2.getText().trim().toUpperCase(),
        SkorBeratBadan3.getText().trim().toUpperCase(),
        SkorBeratBadan4.getText().trim().toUpperCase(),
        SkorBeratBadan5.getText().trim().toUpperCase(),
        SkorGejala6.getText().trim().toUpperCase(),
        SkorGejala7.getText().trim().toUpperCase(),
        SkorGejala8.getText().trim().toUpperCase(),
        SkorGejala9.getText().trim().toUpperCase(),
        SkorGejala10.getText().trim().toUpperCase(),
        SkorGejala11.getText().trim().toUpperCase(),
        SkorGejala12.getText().trim().toUpperCase(),
        SkorGejala13.getText().trim().toUpperCase(),
        SkorGejala14.getText().trim().toUpperCase(),
        SkorGejala15.getText().trim().toUpperCase(),
        SkorKapasitasFungsional16.getText().trim().toUpperCase(),
        SkorKapasitasFungsional17.getText().trim().toUpperCase(),
        SkorPenyakit18.getText().trim().toUpperCase(),
        SkorPemFis19.getText().trim().toUpperCase(),
        SkorPemFis20.getText().trim().toUpperCase(),
        SkorPemFis21.getText().trim().toUpperCase(),
        SkorPemFis22.getText().trim().toUpperCase(),
        SkorPemFis23.getText().trim().toUpperCase(),
        SkorPemFis24.getText().trim().toUpperCase(),
        SkorPemFis25.getText().trim().toUpperCase(),
        SkorPemFis26.getText().trim().toUpperCase(),
        SkorPemFis27.getText().trim().toUpperCase(),
        SkorPemFis28.getText().trim().toUpperCase(),
        SkorPemFis29.getText().trim().toUpperCase(),
        SkorPemFis30.getText().trim().toUpperCase(),
        SkorPemFis31.getText().trim().toUpperCase(),
        SkorLab32.getText().trim().toUpperCase(),
        SkorLab33.getText().trim().toUpperCase(),
        SkorFaktorKontribusi34.getText().trim().toUpperCase(),
        SkorFaktorKontribusi35.getText().trim().toUpperCase()
    };

    // Hitung jumlah A, B, dan C
    for (String skor : nilaiSkor) {
        if (skor.equals("A")) countA++;
        else if (skor.equals("B")) countB++;
        else if (skor.equals("C")) countC++;
    }

    total = countA + countB + countC;

    if (total == 0) {
        SkorSGA.setText(""); // tidak ada input valid
        return;
    }

    if ((double)countA / total >= 0.5) {
        SkorSGA.setText("A");
    } else if ((double)countB / total >= 0.5) {
        SkorSGA.setText("B");
    } else if ((double)countC / total >= 0.5) {
        SkorSGA.setText("C");
    } else {
        SkorSGA.setText(""); // tidak ada mayoritas jelas
    }
}

private void pasangListenerOtomatis() {
    DocumentListener listener = new DocumentListener() {
        public void insertUpdate(DocumentEvent e) { hitungSkorSGA(); }
        public void removeUpdate(DocumentEvent e) { hitungSkorSGA(); }
        public void changedUpdate(DocumentEvent e) { hitungSkorSGA(); }
    };

    SkorRiwayatAsupan1.getDocument().addDocumentListener(listener);
    SkorRiwayatAsupan2.getDocument().addDocumentListener(listener);
    SkorBeratBadan3.getDocument().addDocumentListener(listener);
    SkorBeratBadan4.getDocument().addDocumentListener(listener);
    SkorBeratBadan5.getDocument().addDocumentListener(listener);
    SkorGejala6.getDocument().addDocumentListener(listener);
    SkorGejala7.getDocument().addDocumentListener(listener);
    SkorGejala8.getDocument().addDocumentListener(listener);
    SkorGejala9.getDocument().addDocumentListener(listener);
    SkorGejala10.getDocument().addDocumentListener(listener);
    SkorGejala11.getDocument().addDocumentListener(listener);
    SkorGejala12.getDocument().addDocumentListener(listener);
    SkorGejala13.getDocument().addDocumentListener(listener);
    SkorGejala14.getDocument().addDocumentListener(listener);
    SkorGejala15.getDocument().addDocumentListener(listener);
    SkorKapasitasFungsional16.getDocument().addDocumentListener(listener);
    SkorKapasitasFungsional17.getDocument().addDocumentListener(listener);
    SkorPenyakit18.getDocument().addDocumentListener(listener);
    SkorPemFis19.getDocument().addDocumentListener(listener);
    SkorPemFis20.getDocument().addDocumentListener(listener);
    SkorPemFis21.getDocument().addDocumentListener(listener);
    SkorPemFis22.getDocument().addDocumentListener(listener);
    SkorPemFis23.getDocument().addDocumentListener(listener);
    SkorPemFis24.getDocument().addDocumentListener(listener);
    SkorPemFis25.getDocument().addDocumentListener(listener);
    SkorPemFis26.getDocument().addDocumentListener(listener);
    SkorPemFis27.getDocument().addDocumentListener(listener);
    SkorPemFis28.getDocument().addDocumentListener(listener);
    SkorPemFis29.getDocument().addDocumentListener(listener);
    SkorPemFis30.getDocument().addDocumentListener(listener);
    SkorPemFis31.getDocument().addDocumentListener(listener);
    SkorLab32.getDocument().addDocumentListener(listener);
    SkorLab33.getDocument().addDocumentListener(listener);
    SkorFaktorKontribusi34.getDocument().addDocumentListener(listener);
    SkorFaktorKontribusi35.getDocument().addDocumentListener(listener);
}

private void CreatePDF(String FileName) {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select logo from setting"));
            param.put("tanggal1", Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
            param.put("tanggal2", Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
            param.put("Kode_Dokter",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",KdDokter.getText()));            
            param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+KdDokter.getText()+".png");         


            Valid.MyReportPDFqryUpload("rptFormulirSGA.jasper", "report", "::[ Formulir SGA Malnutrisi ]::", 
                            "SELECT reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, " +
                           "reg_periksa.umurdaftar, reg_periksa.sttsumur, pasien.alamat, pasien.agama, pasien.pnd AS pendidikan, pasien.pekerjaan, " +
                           "(SELECT kamar_inap.tgl_masuk FROM kamar_inap WHERE kamar_inap.no_rawat = reg_periksa.no_rawat ORDER BY kamar_inap.tgl_masuk ASC LIMIT 1) AS tgl_masuk, " +
                           "(SELECT pemeriksaan_ranap.tinggi FROM pemeriksaan_ranap WHERE pemeriksaan_ranap.no_rawat = reg_periksa.no_rawat ORDER BY pemeriksaan_ranap.tgl_perawatan ASC LIMIT 1) AS tinggi_badan, " +
                           "(SELECT bangsal.nm_bangsal FROM kamar_inap INNER JOIN kamar ON kamar_inap.kd_kamar = kamar.kd_kamar INNER JOIN bangsal ON kamar.kd_bangsal = bangsal.kd_bangsal " +
                           " WHERE kamar_inap.no_rawat = reg_periksa.no_rawat ORDER BY kamar_inap.tgl_masuk ASC LIMIT 1) AS kamar, " +
                           "sga.tanggal, sga.kd_dokter, dokter.nm_dokter, sga.kd_kamar, sga.DiagnosisMedis, " +
                           "sga.RiwayatAsupan1, sga.SkorRiwayatAsupan1, sga.LamaAsupanMenurun, sga.RiwayatAsupan2, sga.SkorRiwayatAsupan2, " +
                           "sga.BeratBadan3, sga.SkorBeratBadan3, sga.BBBiasanya, sga.BBSaatIni, sga.BesarnyaKehilangan, " +
                           "sga.BeratBadan4, sga.SkorBeratBadan4, sga.BeratBadan5, sga.SkorBeratBadan5, " +
                           "sga.Gejala6, sga.SkorGejala6, sga.Gejala7, sga.SkorGejala7, sga.Gejala8, sga.SkorGejala8, " +
                           "sga.Gejala9, sga.SkorGejala9, sga.Gejala10, sga.SkorGejala10, sga.Gejala11, sga.SkorGejala11, " +
                           "sga.Gejala12, sga.SkorGejala12, sga.Gejala13, sga.SkorGejala13, sga.Gejala14, sga.SkorGejala14, " +
                           "sga.Gejala15, sga.SkorGejala15, " +
                           "sga.KapasitasFungsional16, sga.SkorKapasitasFungsional16, sga.KapasitasFungsional17, sga.SkorKapasitasFungsional17, " +
                           "sga.Penyakit18, sga.SkorPenyakit18, " +
                           "sga.PemFis19, sga.SkorPemFis19, sga.PemFis20, sga.SkorPemFis20, sga.PemFis21, sga.SkorPemFis21, " +
                           "sga.PemFis22, sga.SkorPemFis22, sga.PemFis23, sga.SkorPemFis23, sga.PemFis24, sga.SkorPemFis24, " +
                           "sga.PemFis25, sga.SkorPemFis25, sga.PemFis26, sga.SkorPemFis26, sga.PemFis27, sga.SkorPemFis27, " +
                           "sga.PemFis28, sga.SkorPemFis28, sga.PemFis29, sga.SkorPemFis29, sga.PemFis30, sga.SkorPemFis30, " +
                           "sga.PemFis31, sga.SkorPemFis31, " +
                           "sga.Lab32, sga.SkorLab32, sga.Lab33, sga.SkorLab33, " +
                           "sga.FaktorKontribusi34, sga.SkorFaktorKontribusi34, sga.FaktorKontribusi35, sga.SkorFaktorKontribusi35, " +
                           "sga.SkorSGA, sga.Catatan " +
                           "FROM sga_malnutrisi sga " +
                           "INNER JOIN reg_periksa ON sga.no_rawat = reg_periksa.no_rawat " +
                           "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
                           "INNER JOIN dokter ON sga.kd_dokter = dokter.kd_dokter " +   
                           "WHERE sga.no_rawat ='" + TNoRw.getText() + "' ",FileName, param);  
                    
        } catch (Exception e) {
            System.out.println("Error saat mencetak SGA: " + e);
        } finally {
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
        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%BERKAS RESUME%'");
        if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='pages/upload/" + FileName + ".pdf'") > 0) {
            uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".pdf", "pages/upload/" + FileName + ".pdf"
            });
        } else {
            uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".pdf"
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


//    try {
//        // Pastikan file PDF ada
//        File pdfFile = new File("tmpPDF/" + FileName + ".pdf");
//        if (!pdfFile.exists()) {
//            System.err.println("File PDF tidak ditemukan: " + pdfFile.getAbsolutePath());
//            return;
//        }
//
//        // Load PDF
//        PDDocument document = PDDocument.load(pdfFile);
//        PDFRenderer pdfRenderer = new PDFRenderer(document);
//
//        // Pastikan folder tmpJPG ada
//        File jpgDir = new File("tmpJPG");
//        if (!jpgDir.exists() && !jpgDir.mkdir()) {
//            System.err.println("Gagal membuat folder tmpJPG.");
//            document.close();
//            return;
//        }
//
//        // Render halaman pertama PDF ke gambar
//        BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);
//
//        // Simpan ke JPG
//        File jpgFile = new File(jpgDir, FileName + ".jpg");
//        ImageIO.write(image, "jpg", jpgFile);
//
//        // Tutup dokumen PDF
//        document.close();
//
//        System.out.println("Konversi berhasil: " + jpgFile.getAbsolutePath());
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
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
                kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%Formulir SGA%'");
                String filePath = "pages/upload/" + jpgFile.getName();

                if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='" + filePath + "'") > 0) {
                    uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(), kodeberkas, filePath, filePath
                    });
                } else {
                    uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(), kodeberkas, filePath
                    });
                }

                if (uploadSuccess) {
                    
                    MetadataBerkas.simpan(
                        koneksi,
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim(),
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
//        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%Berkas Resume%'");
//        if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='pages/upload/" + FileName + ".jpg'") > 0) {
//            uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
//                tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".jpg", "pages/upload/" + FileName + ".jpg"
//            });
//        } else {
//            uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
//                tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString().trim(), kodeberkas, "pages/upload/" + FileName + ".jpg"
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

private void ppBerkasDigitalBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {                                                        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(!tbObat.getValueAt(tbObat.getSelectedRow(),0).toString().equals("")){
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    DlgBerkasRawat berkas=new DlgBerkasRawat(null,true);
                    berkas.setJudul("::[ Berkas Digital Perawatan ]::","berkasrawat/pages");
                    try {
                        if(akses.gethapus_berkas_digital_perawatan()==true){
                            berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                        }else{
                            berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2nonhapus.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                        }   
                    } catch (Exception ex) {
                        System.out.println("Notifikasi : "+ex);
                    }

                    berkas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                    berkas.setLocationRelativeTo(internalFrame1);
                    berkas.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                }
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }                  

}