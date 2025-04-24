package permintaan;

import bridging.BPJSSPRI;
import com.sun.javafx.iio.ImageStorage.ImageType;
import fungsi.BackgroundMusic;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import static java.awt.image.ImageObserver.HEIGHT;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import keuangan.DlgKamar;
import laporan.DlgCariPenyakit;
import simrskhanza.DlgKamarInap;
import rekammedis.RMRiwayatPerawatan;
import surat.SuratPersetujuanRawatInap;
import laporan.DlgBerkasRawat;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


/**
 *
 * @author dosen
 */
public class DlgPermintaanRanap extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0,nilai_detik,bookingbaru=0;
    private String alarm="",nol_detik,detik,sql="",finger="",FileName = "", kodeberkas = "";
    private DlgKamar kamar=new DlgKamar(null,false);
    public  DlgCariDokter dokter=new DlgCariDokter(null,true);
    private boolean aktif=false;
    private BackgroundMusic music;
    private DlgCariPenyakit penyakit=new DlgCariPenyakit(null,false);
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
    

    /** Creates new form DlgPemberianInfus
     * @param parent
     * @param modal */
    public DlgPermintaanRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat",//0 no.rwt
            "No.RM",//1 no.rm
            "Nama Pasien",//2 nm pasien
            "J.K.",//3 jk
            "Umur",//4 umur
            "No.Telp",//5 no. telp
            "Cara Bayar",//6 cara bayar
            "Asal Poli/Unit",//7 asal poili
            "Dokter Yang Memeriksa",//8 dr. pemeriksa
            "Tgl. Inap",//9 tanggal
            "Diagnosa Awal",//10 diagnosa
            "Catatan",//11 catatan
            "KodeDokter",//12 kd dpjp
            "Nama Dokter",//13 kd dpjp                 
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 14; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(25);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(90);
            }else if(i==6){
                column.setPreferredWidth(120);
            }else if(i==7){
                column.setPreferredWidth(130);
            }else if(i==8){
                column.setPreferredWidth(160);
            }else if(i==9){
                column.setPreferredWidth(65);
            }else if(i==10){
                column.setPreferredWidth(160);
            }else if(i==11){
                column.setPreferredWidth(160);
            }else if(i==12){
                column.setPreferredWidth(150);            
            }else if(i==13){
                column.setPreferredWidth(150);                                             
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());


        NoRw.setDocument(new batasInput((byte)17).getKata(NoRw));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        Catatan.setDocument(new batasInput((byte)50).getKata(Catatan));
        Diagnosa.setDocument(new batasInput((byte)50).getKata(Diagnosa));
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
        
        kamar.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(kamar.getTable().getSelectedRow()!= -1){   
                    KdKamar.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),1).toString());
                    KdBangsal.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),2).toString());
                    NmBangsal.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),3).toString()+" ( "+kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),4).toString()+")");
                    HargaKamar.setText(Valid.SetAngka(Valid.SetAngka(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),5).toString())));
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
        
        kamar.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    kamar.dispose();
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
                if( penyakit.getTable().getSelectedRow()!= -1){ 
                    if((penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),0).toString()+" - "+penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),1).toString()).length()<50){
                        Diagnosa.setText(penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),0).toString()+" - "+penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),1).toString());
                    }else{
                        Diagnosa.setText((penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),0).toString()+" - "+penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),1).toString()).substring(0,50));
                    }   
                }  
                Diagnosa.requestFocus();
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
                 if(akses.getform().equals("DlgPermintaanRanap")){
                     if(dokter.getTable().getSelectedRow()!= -1){                   
                         kddok.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                         nmdok.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                     }     
                     kddok.requestFocus();
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
            alarm=koneksiDB.ALARMPERMINTAANRANAP();
        } catch (Exception e) {
            alarm="no";
        }
        
        ChkInput.setSelected(true);
        isForm();
        
        if(alarm.equals("yes")){
            jam();
        }
        
        ChkAccor.setSelected(true);
        isMenu();
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
        jPopupMenu1 = new javax.swing.JPopupMenu();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnUpload = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass10 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        panelCari = new widget.panelisi();
        R1 = new widget.RadioButton();
        jLabel15 = new widget.Label();
        R2 = new widget.RadioButton();
        DTPCari1 = new widget.Tanggal();
        jLabel25 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.PanelBiasa();
        NoRw = new widget.TextBox();
        NmPasien = new widget.TextBox();
        DTPTgl = new widget.Tanggal();
        jLabel10 = new widget.Label();
        NoRM = new widget.TextBox();
        jLabel5 = new widget.Label();
        NoTelp = new widget.TextBox();
        jLabel8 = new widget.Label();
        Poli = new widget.TextBox();
        jLabel9 = new widget.Label();
        jLabel11 = new widget.Label();
        nm_dokterrj = new widget.TextBox();
        jLabel12 = new widget.Label();
        Diagnosa = new widget.TextBox();
        CaraBayar = new widget.TextBox();
        jLabel13 = new widget.Label();
        btnKamar = new widget.Button();
        NmBangsal = new widget.TextBox();
        KdBangsal = new widget.TextBox();
        KdKamar = new widget.TextBox();
        jLabel20 = new widget.Label();
        KdDokter = new widget.TextBox();
        HargaKamar = new widget.TextBox();
        Catatan = new widget.TextBox();
        jLabel14 = new widget.Label();
        btnDiagnosa = new widget.Button();
        jLabel42 = new widget.Label();
        kddok = new widget.TextBox();
        nmdok = new widget.TextBox();
        BtnSeekDokter = new widget.Button();
        TglSO = new widget.Tanggal();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        ScrollMenu = new widget.ScrollPane();
        FormMenu = new widget.PanelBiasa();
        BtnKamarInap = new widget.Button();
        BtnRiwayatPasien = new widget.Button();
        BtnSuratPermintaan = new widget.Button();
        BtnSuratPRI = new widget.Button();
        BtnPersetujuanRanap = new widget.Button();
        BtnBerkasDigital = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Permintaan Rawat Inap ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N

        tbObat.setAutoCreateRowSorter(true);
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

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 144));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(55, 55));
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
        panelGlass8.add(BtnPrint);

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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        panelGlass10.setName("panelGlass10"); // NOI18N
        panelGlass10.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(68, 23));
        panelGlass10.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(370, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass10.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
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
        panelGlass10.add(BtnCari);

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
        panelGlass10.add(BtnAll);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass10.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass10.add(LCount);

        jPanel3.add(panelGlass10, java.awt.BorderLayout.CENTER);

        panelCari.setName("panelCari"); // NOI18N
        panelCari.setPreferredSize(new java.awt.Dimension(44, 43));
        panelCari.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 9));

        buttonGroup1.add(R1);
        R1.setSelected(true);
        R1.setText("Menunggu Masuk Rawat Inap");
        R1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        R1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R1.setName("R1"); // NOI18N
        R1.setPreferredSize(new java.awt.Dimension(195, 23));
        panelCari.add(R1);

        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(40, 23));
        panelCari.add(jLabel15);

        buttonGroup1.add(R2);
        R2.setText("Sudah Masuk Rawat Inap :");
        R2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R2.setName("R2"); // NOI18N
        R2.setPreferredSize(new java.awt.Dimension(165, 23));
        panelCari.add(R2);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10-02-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        DTPCari1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPCari1ItemStateChanged(evt);
            }
        });
        DTPCari1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari1KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari1);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("s.d");
        jLabel25.setName("jLabel25"); // NOI18N
        jLabel25.setPreferredSize(new java.awt.Dimension(30, 23));
        panelCari.add(jLabel25);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10-02-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        DTPCari2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPCari2ItemStateChanged(evt);
            }
        });
        DTPCari2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari2KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari2);

        jPanel3.add(panelCari, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 186));
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
        FormInput.setPreferredSize(new java.awt.Dimension(190, 77));
        FormInput.setLayout(null);

        NoRw.setEditable(false);
        NoRw.setHighlighter(null);
        NoRw.setName("NoRw"); // NOI18N
        NoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoRwKeyPressed(evt);
            }
        });
        FormInput.add(NoRw);
        NoRw.setBounds(73, 10, 125, 23);

        NmPasien.setEditable(false);
        NmPasien.setHighlighter(null);
        NmPasien.setName("NmPasien"); // NOI18N
        FormInput.add(NmPasien);
        NmPasien.setBounds(288, 10, 330, 23);

        DTPTgl.setForeground(new java.awt.Color(50, 70, 50));
        DTPTgl.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10-02-2025" }));
        DTPTgl.setDisplayFormat("dd-MM-yyyy");
        DTPTgl.setName("DTPTgl"); // NOI18N
        DTPTgl.setOpaque(false);
        DTPTgl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPTglKeyPressed(evt);
            }
        });
        FormInput.add(DTPTgl);
        DTPTgl.setBounds(528, 70, 90, 23);

        jLabel10.setText("Tanggal :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(454, 70, 70, 23);

        NoRM.setEditable(false);
        NoRM.setHighlighter(null);
        NoRM.setName("NoRM"); // NOI18N
        FormInput.add(NoRM);
        NoRM.setBounds(200, 10, 86, 23);

        jLabel5.setText("No.Rawat :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(0, 10, 69, 23);

        NoTelp.setEditable(false);
        NoTelp.setHighlighter(null);
        NoTelp.setName("NoTelp"); // NOI18N
        FormInput.add(NoTelp);
        NoTelp.setBounds(73, 40, 120, 23);

        jLabel8.setText("No.Telp :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(0, 40, 69, 23);

        Poli.setEditable(false);
        Poli.setHighlighter(null);
        Poli.setName("Poli"); // NOI18N
        FormInput.add(Poli);
        Poli.setBounds(459, 40, 159, 23);

        jLabel9.setText("Unit/Poli :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(395, 40, 60, 23);

        jLabel11.setText("Dokter :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(0, 70, 69, 23);

        nm_dokterrj.setEditable(false);
        nm_dokterrj.setHighlighter(null);
        nm_dokterrj.setName("nm_dokterrj"); // NOI18N
        FormInput.add(nm_dokterrj);
        nm_dokterrj.setBounds(73, 70, 318, 23);

        jLabel12.setText("Diagnosa :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(0, 130, 69, 23);

        Diagnosa.setHighlighter(null);
        Diagnosa.setName("Diagnosa"); // NOI18N
        Diagnosa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosaKeyPressed(evt);
            }
        });
        FormInput.add(Diagnosa);
        Diagnosa.setBounds(73, 130, 545, 23);

        CaraBayar.setEditable(false);
        CaraBayar.setHighlighter(null);
        CaraBayar.setName("CaraBayar"); // NOI18N
        FormInput.add(CaraBayar);
        CaraBayar.setBounds(271, 40, 120, 23);

        jLabel13.setText("Cara Bayar :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(192, 40, 75, 23);

        btnKamar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnKamar.setMnemonic('2');
        btnKamar.setToolTipText("Alt+2");
        btnKamar.setName("btnKamar"); // NOI18N
        btnKamar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKamarActionPerformed(evt);
            }
        });
        FormInput.add(btnKamar);
        btnKamar.setBounds(590, 220, 28, 23);

        NmBangsal.setEditable(false);
        NmBangsal.setHighlighter(null);
        NmBangsal.setName("NmBangsal"); // NOI18N
        FormInput.add(NmBangsal);
        NmBangsal.setBounds(230, 220, 260, 23);

        KdBangsal.setEditable(false);
        KdBangsal.setName("KdBangsal"); // NOI18N
        FormInput.add(KdBangsal);
        KdBangsal.setBounds(160, 220, 65, 23);

        KdKamar.setEditable(false);
        KdKamar.setHighlighter(null);
        KdKamar.setName("KdKamar"); // NOI18N
        FormInput.add(KdKamar);
        KdKamar.setBounds(80, 220, 80, 23);

        jLabel20.setText("Kamar :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(0, 220, 69, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        FormInput.add(KdDokter);
        KdDokter.setBounds(400, 70, 70, 26);

        HargaKamar.setEditable(false);
        HargaKamar.setHighlighter(null);
        HargaKamar.setName("HargaKamar"); // NOI18N
        FormInput.add(HargaKamar);
        HargaKamar.setBounds(490, 220, 103, 23);

        Catatan.setHighlighter(null);
        Catatan.setName("Catatan"); // NOI18N
        Catatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CatatanKeyPressed(evt);
            }
        });
        FormInput.add(Catatan);
        Catatan.setBounds(73, 160, 545, 23);

        jLabel14.setText("Catatan :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(10, 160, 60, 23);

        btnDiagnosa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnDiagnosa.setMnemonic('3');
        btnDiagnosa.setToolTipText("Alt+3");
        btnDiagnosa.setName("btnDiagnosa"); // NOI18N
        btnDiagnosa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDiagnosaActionPerformed(evt);
            }
        });
        btnDiagnosa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDiagnosaKeyPressed(evt);
            }
        });
        FormInput.add(btnDiagnosa);
        btnDiagnosa.setBounds(625, 130, 30, 23);

        jLabel42.setText("DPJP :");
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(0, 100, 70, 23);

        kddok.setEditable(false);
        kddok.setHighlighter(null);
        kddok.setName("kddok"); // NOI18N
        FormInput.add(kddok);
        kddok.setBounds(73, 100, 100, 23);

        nmdok.setEditable(false);
        nmdok.setHighlighter(null);
        nmdok.setName("nmdok"); // NOI18N
        FormInput.add(nmdok);
        nmdok.setBounds(175, 100, 443, 23);

        BtnSeekDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeekDokter.setMnemonic('1');
        BtnSeekDokter.setToolTipText("Alt+1");
        BtnSeekDokter.setName("BtnSeekDokter"); // NOI18N
        BtnSeekDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeekDokterActionPerformed(evt);
            }
        });
        BtnSeekDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeekDokterKeyPressed(evt);
            }
        });
        FormInput.add(BtnSeekDokter);
        BtnSeekDokter.setBounds(625, 100, 30, 24);

        TglSO.setForeground(new java.awt.Color(50, 70, 50));
        TglSO.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10-02-2025" }));
        TglSO.setDisplayFormat("dd-MM-yyyy");
        TglSO.setName("TglSO"); // NOI18N
        TglSO.setOpaque(false);
        TglSO.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglSOKeyPressed(evt);
            }
        });
        FormInput.add(TglSO);
        TglSO.setBounds(630, 10, 90, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(200, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout());

        ChkAccor.setBackground(new java.awt.Color(255, 250, 250));
        ChkAccor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(250, 255, 248)));
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.setFocusable(false);
        ChkAccor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkAccor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkAccor.setName("ChkAccor"); // NOI18N
        ChkAccor.setPreferredSize(new java.awt.Dimension(15, 20));
        ChkAccor.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkAccorActionPerformed(evt);
            }
        });
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.EAST);

        ScrollMenu.setBorder(null);
        ScrollMenu.setName("ScrollMenu"); // NOI18N
        ScrollMenu.setPreferredSize(new java.awt.Dimension(130, 43));

        FormMenu.setBackground(new java.awt.Color(255, 255, 255));
        FormMenu.setBorder(null);
        FormMenu.setName("FormMenu"); // NOI18N
        FormMenu.setPreferredSize(new java.awt.Dimension(115, 43));
        FormMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 1));

        BtnKamarInap.setForeground(new java.awt.Color(0, 0, 0));
        BtnKamarInap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/resumeri.png"))); // NOI18N
        BtnKamarInap.setText("Kamar Inap");
        BtnKamarInap.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnKamarInap.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnKamarInap.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnKamarInap.setName("BtnKamarInap"); // NOI18N
        BtnKamarInap.setPreferredSize(new java.awt.Dimension(160, 30));
        BtnKamarInap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKamarInapActionPerformed(evt);
            }
        });
        FormMenu.add(BtnKamarInap);

        BtnRiwayatPasien.setForeground(new java.awt.Color(0, 0, 0));
        BtnRiwayatPasien.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/resumeri.png"))); // NOI18N
        BtnRiwayatPasien.setText("Riwayat Perawatan");
        BtnRiwayatPasien.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnRiwayatPasien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnRiwayatPasien.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnRiwayatPasien.setName("BtnRiwayatPasien"); // NOI18N
        BtnRiwayatPasien.setPreferredSize(new java.awt.Dimension(160, 30));
        BtnRiwayatPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRiwayatPasienActionPerformed(evt);
            }
        });
        FormMenu.add(BtnRiwayatPasien);

        BtnSuratPermintaan.setForeground(new java.awt.Color(0, 0, 0));
        BtnSuratPermintaan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/resumeri.png"))); // NOI18N
        BtnSuratPermintaan.setText("Surat Permintaan Ranap");
        BtnSuratPermintaan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnSuratPermintaan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnSuratPermintaan.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnSuratPermintaan.setName("BtnSuratPermintaan"); // NOI18N
        BtnSuratPermintaan.setPreferredSize(new java.awt.Dimension(160, 30));
        BtnSuratPermintaan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSuratPermintaanActionPerformed(evt);
            }
        });
        FormMenu.add(BtnSuratPermintaan);

        BtnSuratPRI.setForeground(new java.awt.Color(0, 0, 0));
        BtnSuratPRI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/resumeri.png"))); // NOI18N
        BtnSuratPRI.setText("Perintah Rawat Inap BPJS");
        BtnSuratPRI.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnSuratPRI.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnSuratPRI.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnSuratPRI.setName("BtnSuratPRI"); // NOI18N
        BtnSuratPRI.setPreferredSize(new java.awt.Dimension(160, 30));
        BtnSuratPRI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSuratPRIActionPerformed(evt);
            }
        });
        FormMenu.add(BtnSuratPRI);

        BtnPersetujuanRanap.setForeground(new java.awt.Color(0, 0, 0));
        BtnPersetujuanRanap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/resumeri.png"))); // NOI18N
        BtnPersetujuanRanap.setText("Persetujuan Rawat Inap");
        BtnPersetujuanRanap.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnPersetujuanRanap.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnPersetujuanRanap.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnPersetujuanRanap.setName("BtnPersetujuanRanap"); // NOI18N
        BtnPersetujuanRanap.setPreferredSize(new java.awt.Dimension(160, 30));
        BtnPersetujuanRanap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPersetujuanRanapActionPerformed(evt);
            }
        });
        FormMenu.add(BtnPersetujuanRanap);

        BtnBerkasDigital.setForeground(new java.awt.Color(0, 0, 0));
        BtnBerkasDigital.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/resumeri.png"))); // NOI18N
        BtnBerkasDigital.setText("Berkas Digital Perawatan");
        BtnBerkasDigital.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnBerkasDigital.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnBerkasDigital.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnBerkasDigital.setName("BtnBerkasDigital"); // NOI18N
        BtnBerkasDigital.setPreferredSize(new java.awt.Dimension(160, 30));
        BtnBerkasDigital.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBerkasDigitalActionPerformed(evt);
            }
        });
        FormMenu.add(BtnBerkasDigital);

        ScrollMenu.setViewportView(FormMenu);

        PanelAccor.add(ScrollMenu, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelAccor, java.awt.BorderLayout.WEST);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void NoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoRwKeyPressed
        //Valid.pindah(evt,Status,KdDokter);
        
}//GEN-LAST:event_NoRwKeyPressed

    private void DTPTglKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPTglKeyPressed
        Valid.pindah(evt,TCari,Diagnosa);
}//GEN-LAST:event_DTPTglKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(NoRw.getText().trim().equals("")||NoRM.getText().trim().equals("")||NmPasien.getText().trim().equals("")){
            Valid.textKosong(TCari,"Pasien");        
        }else if(Diagnosa.getText().trim().equals("")){
            Valid.textKosong(Diagnosa,"Diagnosa");
        }else{
            if(Sequel.menyimpantf("permintaan_ranap_","?,?,?,?,?,?","Pasien",6,new String[]{
                NoRw.getText(),Valid.SetTgl(DTPTgl.getSelectedItem()+""),"",Diagnosa.getText(),Catatan.getText(),KdDokter.getText()
            })==true){
                tampil();
                Sequel.menyimpan2("dpjp_ranap","?,?",2,new String[]{NoRw.getText(),kddok.getText()});
                emptTeks();
            }
        }
        
//        //auto upload berkas digital
//        Random rand = new Random();
//        int randomNumber = rand.nextInt(100) + 1;
//        FileName = randomNumber + "_" + tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString()+"_SO";
//        CreatePDF(FileName);
//        String filePath = "tmpPDF/" + FileName;        
//        UploadPDF(FileName, "berkasrawat/pages/upload/");
//        HapusPDF();
//        ppBerkasDigitalBtnPrintActionPerformed(evt);   

}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
           Valid.pindah(evt,Catatan,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        ChkInput.setSelected(true);
        isForm(); 
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(Valid.hapusTabletf(tabMode,NoRw,"permintaan_ranap_","no_rawat")==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            emptTeks();
            LCount.setText(""+tabMode.getRowCount());
        }
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnPrint);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Map<String, Object> param = new HashMap<>();    
            param.put("kd_dokter",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",KdDokter.getText()));            
            param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+KdDokter.getText()+".png");
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            sql="";
            if(R1.isSelected()==true){
                sql="select permintaan_ranap_.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,reg_periksa.umurdaftar,reg_periksa.sttsumur,"+
                    "pasien.no_tlp,penjab.png_jawab,poliklinik.nm_poli,dokter.nm_dokter,permintaan_ranap_.tanggal,permintaan_ranap_.kd_kamar,kamar.kd_bangsal,"+
                    "bangsal.nm_bangsal,kamar.trf_kamar,permintaan_ranap_.diagnosa,permintaan_ranap_.catatan from permintaan_ranap_ "+
                    "inner join reg_periksa on permintaan_ranap_.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join kamar on permintaan_ranap_.kd_kamar=kamar.kd_kamar "+
                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                    "where permintaan_ranap_.no_rawat not in (select DISTINCT no_rawat from kamar_inap) "+
                    (TCari.getText().equals("")?"":"and (permintaan_ranap_.no_rawat like '%"+TCari.getText().trim()+"%' or reg_periksa.no_rkm_medis like '%"+TCari.getText().trim()+"%' or pasien.nm_pasien like '%"+TCari.getText().trim()+"%' "+
                    "or penjab.png_jawab like '%"+TCari.getText().trim()+"%' or poliklinik.nm_poli like '%"+TCari.getText().trim()+"%' or dokter.nm_dokter like '%"+TCari.getText().trim()+"%' or bangsal.nm_bangsal like '%"+TCari.getText().trim()+"%' "+
                    "or permintaan_ranap_.diagnosa like '%"+TCari.getText().trim()+"%')")+" order by permintaan_ranap_.tanggal";
            }else if(R2.isSelected()==true){
                sql="select permintaan_ranap_.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,reg_periksa.umurdaftar,reg_periksa.sttsumur,"+
                    "pasien.no_tlp,penjab.png_jawab,poliklinik.nm_poli,dokter.nm_dokter,permintaan_ranap_.tanggal,permintaan_ranap_.kd_kamar,kamar.kd_bangsal,"+
                    "bangsal.nm_bangsal,kamar.trf_kamar,permintaan_ranap_.diagnosa,permintaan_ranap_.catatan from permintaan_ranap_ "+
                    "inner join reg_periksa on permintaan_ranap_.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join kamar on permintaan_ranap_.kd_kamar=kamar.kd_kamar "+
                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                    "where permintaan_ranap_.no_rawat in (select DISTINCT no_rawat from kamar_inap) and permintaan_ranap_.tanggal between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' "+
                    (TCari.getText().equals("")?"":"and (permintaan_ranap_.no_rawat like '%"+TCari.getText().trim()+"%' or reg_periksa.no_rkm_medis like '%"+TCari.getText().trim()+"%' or pasien.nm_pasien like '%"+TCari.getText().trim()+"%' "+
                    "or penjab.png_jawab like '%"+TCari.getText().trim()+"%' or poliklinik.nm_poli like '%"+TCari.getText().trim()+"%' or dokter.nm_dokter like '%"+TCari.getText().trim()+"%' or bangsal.nm_bangsal like '%"+TCari.getText().trim()+"%' "+
                    "or permintaan_ranap_.diagnosa like '%"+TCari.getText().trim()+"%')")+" order by permintaan_ranap_.tanggal";
            }
            
            Valid.MyReportqry("rptPermintaanRawatInap.jasper","report","::[ Data Pemesanan Rawat Inap ]::",sql,param);
            this.setCursor(Cursor.getDefaultCursor());
        }
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnKeluar);
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
            Valid.pindah(evt, BtnCari, NmPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
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
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
  isForm();                
}//GEN-LAST:event_ChkInputActionPerformed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(NoRw.getText().trim().equals("")||NoRM.getText().trim().equals("")||NmPasien.getText().trim().equals("")){
            Valid.textKosong(TCari,"Pasien");
//        }else if(KdBangsal.getText().trim().equals("")||KdKamar.getText().trim().equals("")||NmBangsal.getText().trim().equals("")){
//            Valid.textKosong(btnKamar,"Kamar/Bangsal");
        }else if(Diagnosa.getText().trim().equals("")){
            Valid.textKosong(Diagnosa,"Diagnosa");
        }else{
            if(tbObat.getSelectedRow()> -1){
                if(Sequel.mengedittf("permintaan_ranap_","no_rawat=?","no_rawat=?,tanggal=?,kd_kamar=?,diagnosa=?,catatan=?",6,new String[]{
                    NoRw.getText(),Valid.SetTgl(DTPTgl.getSelectedItem()+""),Diagnosa.getText(),Catatan.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
                })==true){
                    tampil();
                    Sequel.mengedit("kamar","kd_kamar=?","status='DIBOOKING'",1,new String[]{KdKamar.getText()});
                    emptTeks();
                }
            }
        }
        
//        //auto upload berkas digital
//        Random rand = new Random();
//        int randomNumber = rand.nextInt(100) + 1;
//        FileName = randomNumber + "_" + tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString()+"_SO";
//        CreatePDF(FileName);
//        String filePath = "tmpPDF/" + FileName;        
//        UploadPDF(FileName, "berkasrawat/pages/upload/");
//        HapusPDF();
//        ppBerkasDigitalBtnPrintActionPerformed(evt);   

    }//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnKeluar);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        aktif=true;
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void btnKamarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKamarActionPerformed
        kamar.load();
        kamar.isCek();
        kamar.emptTeks();
        kamar.tampil();
        kamar.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kamar.setLocationRelativeTo(internalFrame1);
        kamar.setVisible(true);        
    }//GEN-LAST:event_btnKamarActionPerformed

    private void DiagnosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosaKeyPressed
        Valid.pindah(evt,DTPTgl,Catatan);
    }//GEN-LAST:event_DiagnosaKeyPressed

    private void CatatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CatatanKeyPressed
        Valid.pindah(evt,Diagnosa,BtnSimpan);
    }//GEN-LAST:event_CatatanKeyPressed

    private void DTPCari2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari2KeyPressed

    private void DTPCari2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPCari2ItemStateChanged
        R2.setSelected(true);
    }//GEN-LAST:event_DTPCari2ItemStateChanged

    private void DTPCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari1KeyPressed

    private void DTPCari1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPCari1ItemStateChanged
        R2.setSelected(true);
    }//GEN-LAST:event_DTPCari1ItemStateChanged

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        aktif=false;
        kamar.dispose();
    }//GEN-LAST:event_formWindowClosed

    private void btnDiagnosaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiagnosaActionPerformed
        penyakit.isCek();
        penyakit.emptTeks();
        penyakit.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penyakit.setLocationRelativeTo(internalFrame1);
        penyakit.setVisible(true);
    }//GEN-LAST:event_btnDiagnosaActionPerformed

    private void btnDiagnosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDiagnosaKeyPressed
        Valid.pindah(evt,Diagnosa,Catatan);
    }//GEN-LAST:event_btnDiagnosaKeyPressed

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        isMenu();
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void BtnKamarInapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKamarInapActionPerformed
    // MODIF MUSTAFA
    if (tabMode.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "Maaf, tabel masih kosong!");
        TCari.requestFocus();
        return;
    }

    if (tbObat.getSelectedRow() == -1) {
        JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data!");
        return;
    }

    try {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String tanggalDipilih = Valid.SetTgl(DTPTgl.getSelectedItem() + "");
        String tanggalHariIni = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String tanggalregistrasi = Valid.SetTgl(TglSO.getSelectedItem() + "");

        // Langsung buka DlgKamarInap jika memenuhi kondisi cepat
        if (tanggalHariIni.equals(tanggalregistrasi) || "Unit IGD".equals(Poli.getText())) {
            bukaDlgKamarInap();
            return;
        }

        // Dapatkan nomor rawat terakhir dan buat nomor baru
        String queryNomorRawat = "SELECT IFNULL(MAX(CONVERT(RIGHT(no_rawat, 6), SIGNED)), 0) " +
                                 "FROM reg_periksa WHERE tgl_registrasi = ?";
        int nomorTerakhir = 0;

        try (PreparedStatement ps = koneksi.prepareStatement(queryNomorRawat)) {
            ps.setString(1, tanggalDipilih);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nomorTerakhir = rs.getInt(1);
                }
            }
        }

        // Hitung no_rawat baru
        nomorTerakhir++;
        String noRawatBaruLanjutan = String.format("%06d", nomorTerakhir);
        String noRawatBaru = dateformat.format(DTPTgl.getDate()) + "/" + noRawatBaruLanjutan;

        // Ambil data pasien untuk dimasukkan ke rawat inap
        String querySelect = "SELECT no_reg, kd_dokter, no_rkm_medis, kd_poli, p_jawab, almt_pj, hubunganpj, " +
                             "biaya_reg, stts, stts_daftar, kd_pj, umurdaftar, sttsumur, status_bayar, status_poli " +
                             "FROM reg_periksa WHERE no_rawat = ?";
        try (PreparedStatement psSelect = koneksi.prepareStatement(querySelect)) {
            psSelect.setString(1, NoRw.getText());
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    // Lakukan insert rawat inap
                    String queryInsert = "INSERT INTO reg_periksa (no_reg, no_rawat, tgl_registrasi, jam_reg, kd_dokter, " +
                                         "no_rkm_medis, kd_poli, p_jawab, almt_pj, hubunganpj, biaya_reg, stts, stts_daftar, " +
                                         "status_lanjut, kd_pj, umurdaftar, sttsumur, status_bayar, status_poli) " +
                                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement psInsert = koneksi.prepareStatement(queryInsert)) {
                        psInsert.setString(1, rs.getString("no_reg"));
                        psInsert.setString(2, noRawatBaru);
                        psInsert.setDate(3, java.sql.Date.valueOf(tanggalDipilih));
                        psInsert.setTime(4, new java.sql.Time(System.currentTimeMillis()));
                        psInsert.setString(5, rs.getString("kd_dokter"));
                        psInsert.setString(6, rs.getString("no_rkm_medis"));
                        psInsert.setString(7, rs.getString("kd_poli"));
                        psInsert.setString(8, rs.getString("p_jawab"));
                        psInsert.setString(9, rs.getString("almt_pj"));
                        psInsert.setString(10, rs.getString("hubunganpj"));
                        psInsert.setDouble(11, rs.getDouble("biaya_reg"));
                        psInsert.setString(12, rs.getString("stts"));
                        psInsert.setString(13, rs.getString("stts_daftar"));
                        psInsert.setString(14, "Ranap");
                        psInsert.setString(15, rs.getString("kd_pj"));
                        psInsert.setInt(16, rs.getInt("umurdaftar"));
                        psInsert.setString(17, rs.getString("sttsumur"));
                        psInsert.setString(18, "Belum Bayar");
                        psInsert.setString(19, rs.getString("status_poli"));
                        psInsert.executeUpdate();                           
                    }

                    // Insert ke dpjp_ranap
                    Sequel.queryu("INSERT INTO dpjp_ranap (no_rawat, kd_dokter) VALUES ('" + noRawatBaru + "', '" + kddok.getText() + "')");

                } else {
                    JOptionPane.showMessageDialog(null, "Data pasien tidak ditemukan!");
                    return;
                }
            }
        }

        // Tampilkan dialog rawat inap
        akses.setstatus(true);
        DlgKamarInap kamarinap = new DlgKamarInap(null, false);
        kamarinap.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        kamarinap.setLocationRelativeTo(internalFrame1);
        kamarinap.emptTeks();
        kamarinap.isCek();
        kamarinap.setNoRm(noRawatBaru, NoRM.getText(), NmPasien.getText());
        kamarinap.setDiagnosaAwal(Diagnosa.getText());
        kamarinap.setVisible(true);

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
        e.printStackTrace();
    } finally {
        this.setCursor(Cursor.getDefaultCursor());
    }

    }//GEN-LAST:event_BtnKamarInapActionPerformed

private void bukaDlgKamarInap() {
    akses.setstatus(true);
    DlgKamarInap kamarinap = new DlgKamarInap(null, false);
    kamarinap.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
    kamarinap.setLocationRelativeTo(internalFrame1);
    kamarinap.emptTeks();
    kamarinap.isCek();
    kamarinap.setNoRm(NoRw.getText(), NoRM.getText(), NmPasien.getText());
    kamarinap.setDiagnosaAwal(Diagnosa.getText());
    kamarinap.setVisible(true);
}    
    
    private void BtnSuratPermintaanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSuratPermintaanActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();
                param.put("Kode_Dokter",Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",KdDokter.getText()));            
                param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+KdDokter.getText()+".png");
                param.put("dpjp",Sequel.cariIsi("select dpjp_ranap.kd_dokter from dpjp_ranap where dpjp_ranap.kd_dokter=?",KdDokter.getText()));
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());  
//                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",KdDokter.getText());
//                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+Dokter.getText()+"\nID "+(finger.equals("")?KdDokter.getText():finger)+"\n"+DTPTgl.getSelectedItem()); 
                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                Valid.MyReportqry("rptSuratPermintaanRawatInap.jasper","report","::[ Surat Permintaan Rawat Inap ]::",
                        "select permintaan_ranap_.no_rawat,"
                                + "reg_periksa.no_rkm_medis,"
                                + "pasien.nm_pasien,"
                                + "pasien.jk,"
                                + "reg_periksa.umurdaftar,"
                                + "reg_periksa.sttsumur,"
                                + "pasien.no_tlp,"
                                + "penjab.png_jawab,"
                                + "poliklinik.nm_poli,"
                                + "dokter.nm_dokter,"
                                + "permintaan_ranap_.tanggal,"
                                + "permintaan_ranap_.diagnosa,"
                                + "permintaan_ranap_.catatan,"
                                + "Group_concat(dr.nm_dokter) as nm_dokter_dpjp,"
                                + "reg_periksa.kd_dokter "
                                + "from "
                                + "permintaan_ranap_ "+
                        "inner join reg_periksa on permintaan_ranap_.no_rawat=reg_periksa.no_rawat "+
                        "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                        "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                        "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                        "Join dpjp_ranap on permintaan_ranap_.no_rawat = dpjp_ranap.no_rawat " +
                        "Join dokter as dr on dpjp_ranap.kd_dokter = dr.kd_dokter "+        
                        "where reg_periksa.no_rawat='"+NoRw.getText()+"' Group by permintaan_ranap_.no_rawat",param);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnSuratPermintaanActionPerformed

    private void BtnRiwayatPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRiwayatPasienActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMRiwayatPerawatan resume=new RMRiwayatPerawatan(null,true);
                resume.setNoRm(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
                resume.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                resume.setLocationRelativeTo(internalFrame1);
                resume.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnRiwayatPasienActionPerformed

    private void BtnSuratPRIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSuratPRIActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    ps=koneksi.prepareStatement("select pasien.no_peserta,pasien.tgl_lahir,pasien.jk from pasien where pasien.no_rkm_medis=?");
                    try {
                        ps.setString(1,NoRM.getText());
                        rs=ps.executeQuery();
                        if(rs.next()){
                            if(rs.getString("no_peserta").length()<13){
                                JOptionPane.showMessageDialog(null,"Kartu BPJS Pasien tidak valid, silahkan hubungi bagian terkait..!!");
                            }else{
                                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                BPJSSPRI form=new BPJSSPRI(null,false);
                                form.setNoRm(NoRw.getText(),rs.getString("no_peserta"),NoRM.getText(),NmPasien.getText(),rs.getString("tgl_lahir"),rs.getString("jk"),"-");
                                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                                form.setLocationRelativeTo(internalFrame1);
                                form.setVisible(true);
                                this.setCursor(Cursor.getDefaultCursor());
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
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
                }
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnSuratPRIActionPerformed

    private void BtnPersetujuanRanapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPersetujuanRanapActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                SuratPersetujuanRawatInap resume=new SuratPersetujuanRawatInap(null,false);
                resume.isCek();
                resume.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                resume.setLocationRelativeTo(internalFrame1);
                resume.setVisible(true);
                resume.emptTeks();
                resume.setNoRm(NoRw.getText(),DTPCari2.getDate());
                resume.tampil();
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }      
        }
    }//GEN-LAST:event_BtnPersetujuanRanapActionPerformed

    private void BtnSeekDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeekDokterActionPerformed
        akses.setform("DlgPermintaanRanap");
        i=1;
        //        dokter.load();
        dokter.isCek();
        dokter.emptTeks();
        //        dokter.tampil();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnSeekDokterActionPerformed

    private void BtnSeekDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeekDokterKeyPressed
        Valid.pindah(evt,kddok,nmdok);
    }//GEN-LAST:event_BtnSeekDokterKeyPressed

    private void BtnBerkasDigitalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBerkasDigitalActionPerformed
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
        this.setCursor(Cursor.getDefaultCursor());        // TODO add your handling code here:
    }//GEN-LAST:event_BtnBerkasDigitalActionPerformed

    private void TglSOKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglSOKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TglSOKeyPressed

    private void BtnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUploadActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
    if (tbObat.getSelectedRow() == -1) {
        JOptionPane.showMessageDialog(null, "Silahkan pilih dulu data pasiennya..!!");
        this.setCursor(Cursor.getDefaultCursor());
        return;
    }

    String norawat = tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString();
    String no_rawat_upload = norawat;

    try {
        // Cari no_rkm_medis pasien tsb
        String no_rkm_medis = Sequel.cariIsi("SELECT no_rkm_medis FROM reg_periksa WHERE no_rawat=?", norawat);

        // Cari no_rawat rawat inap terbaru (jika ada)
        String norawatRanapTerbaru = Sequel.cariIsi(
            "SELECT no_rawat FROM reg_periksa WHERE no_rkm_medis=? AND status_lanjut='Ranap' ORDER BY tgl_registrasi DESC, jam_reg DESC LIMIT 1",
            no_rkm_medis
        );

        if(!norawatRanapTerbaru.equals("")){
            no_rawat_upload = norawatRanapTerbaru;
        }
    } catch (Exception e) {
        System.out.println("Notif Cek no_rawat_upload : " + e);
    }

    // Format nama file: yyyyMMddHHmmss_noRawat_SODigital
    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
    String timestamp = sdf.format(new Date());
    FileName = timestamp + "_" + no_rawat_upload.replace("/", "") + "_SODigital";

    CreatePDF(FileName);
    ConvertPDFtoJPG(FileName);
    UploadJPG(FileName, "berkasrawat/pages/upload/");
    HapusJPG();

    ppBerkasDigitalBtnPrintActionPerformed(evt);

    this.setCursor(Cursor.getDefaultCursor());
}

private void closeResultSet(ResultSet rs) {
    if (rs != null) {
        try {
            rs.close();
        } catch (Exception e) {
            System.out.println("Notif Close RS : " + e);
        }
    }
}

private void closePreparedStatement(PreparedStatement ps) {
    if (ps != null) {
        try {
            ps.close();
        } catch (Exception e) {
            System.out.println("Notif Close PS : " + e);
        }
    }
    }//GEN-LAST:event_BtnUploadActionPerformed

    private void BtnUploadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUploadKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUploadKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgPermintaanRanap dialog = new DlgPermintaanRanap(new javax.swing.JFrame(), true);
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
    private widget.Button BtnBerkasDigital;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKamarInap;
    private widget.Button BtnKeluar;
    private widget.Button BtnPersetujuanRanap;
    private widget.Button BtnPrint;
    private widget.Button BtnRiwayatPasien;
    private widget.Button BtnSeekDokter;
    private widget.Button BtnSimpan;
    private widget.Button BtnSuratPRI;
    private widget.Button BtnSuratPermintaan;
    private widget.Button BtnUpload;
    private widget.TextBox CaraBayar;
    private widget.TextBox Catatan;
    private widget.CekBox ChkAccor;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Tanggal DTPTgl;
    private widget.TextBox Diagnosa;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormMenu;
    private widget.TextBox HargaKamar;
    private widget.TextBox KdBangsal;
    private widget.TextBox KdDokter;
    private widget.TextBox KdKamar;
    private widget.Label LCount;
    private widget.TextBox NmBangsal;
    private widget.TextBox NmPasien;
    private widget.TextBox NoRM;
    private widget.TextBox NoRw;
    private widget.TextBox NoTelp;
    private widget.PanelBiasa PanelAccor;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox Poli;
    private widget.RadioButton R1;
    private widget.RadioButton R2;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane ScrollMenu;
    private widget.TextBox TCari;
    private widget.Tanggal TglSO;
    private widget.Button btnDiagnosa;
    private widget.Button btnKamar;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel20;
    private widget.Label jLabel25;
    private widget.Label jLabel42;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox kddok;
    private widget.TextBox nm_dokterrj;
    private widget.TextBox nmdok;
    private widget.panelisi panelCari;
    private widget.panelisi panelGlass10;
    private widget.panelisi panelGlass8;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    public void tampil() {     
        Valid.tabelKosong(tabMode);
        try{ 
            if(R1.isSelected()==true){
                ps=koneksi.prepareStatement("select "
                        + "permintaan_ranap_.no_rawat,"
                        + "reg_periksa.no_rkm_medis,"
                        + "pasien.nm_pasien,"
                        + "pasien.jk,"
                        + "reg_periksa.umurdaftar,"
                        + "reg_periksa.sttsumur,"
                        + "pasien.no_tlp,"
                        + "penjab.png_jawab,"
                        + "poliklinik.nm_poli,"
                        + "dokter.nm_dokter,"
                        + "permintaan_ranap_.tanggal,"                                                
                        + "permintaan_ranap_.diagnosa,"                        
                        + "permintaan_ranap_.catatan,"
                        + "permintaan_ranap_.kd_dokter "
                        + "from permintaan_ranap_ "+
                    "inner join reg_periksa on permintaan_ranap_.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+                    
                    "where permintaan_ranap_.no_rawat not in (select DISTINCT no_rawat from kamar_inap) "+
                    (TCari.getText().equals("")?"":"and "
                            + "(permintaan_ranap_.no_rawat like ? or "
                            + "reg_periksa.no_rkm_medis like ? "
                            + "or pasien.nm_pasien like ?"
                            + "or penjab.png_jawab like ? "
                            + "or poliklinik.nm_poli like ? "
                            + "or dokter.nm_dokter like ? "                            
                            + "or permintaan_ranap_.diagnosa like ?)")+" "
                            + "order by permintaan_ranap_.tanggal");
                try {
                    if(!TCari.getText().equals("")){
                        ps.setString(1,"%"+TCari.getText().trim()+"%");
                        ps.setString(2,"%"+TCari.getText().trim()+"%");
                        ps.setString(3,"%"+TCari.getText().trim()+"%");
                        ps.setString(4,"%"+TCari.getText().trim()+"%");
                        ps.setString(5,"%"+TCari.getText().trim()+"%");
                        ps.setString(6,"%"+TCari.getText().trim()+"%");
                        ps.setString(7,"%"+TCari.getText().trim()+"%");                        
                    }
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabMode.addRow(new String[]{
                            rs.getString("no_rawat"),//0 no.rawat
                            rs.getString("no_rkm_medis"),//1 no.rm
                            rs.getString("nm_pasien"),//2 nama
                            rs.getString("jk"),//3 jk
                            rs.getString("umurdaftar")+" "+rs.getString("sttsumur"),//4 umur
                            rs.getString("no_tlp"),//5 no.telp
                            rs.getString("png_jawab"),//6 cara bayar
                            rs.getString("nm_poli"),//7 nama poli
                            rs.getString("nm_dokter"),//8 dr. pemeriksa
                            rs.getString("tanggal"),//9 tanggal
                            rs.getString("diagnosa"),//10 diagnosa
                            rs.getString("catatan"),//11 catatan
                            rs.getString("kd_dokter"),//12 kd dpjp
                            rs.getString("nm_dokter")//12 kd dpjp    
                        });
                    }
                } catch (Exception e) {
                    System.out.println("Notif Kamar : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }
            }else if(R2.isSelected()==true){
                ps=koneksi.prepareStatement("select "
                        + "permintaan_ranap_.no_rawat,"
                        + "reg_periksa.no_rkm_medis,"
                        + "pasien.nm_pasien,"
                        + "pasien.jk,"
                        + "reg_periksa.umurdaftar,"
                        + "reg_periksa.sttsumur,"
                        + "pasien.no_tlp,"
                        + "penjab.png_jawab,"
                        + "poliklinik.nm_poli,"
                        + "dokter.nm_dokter,"
                        + "permintaan_ranap_.tanggal,"                                                
                        + "permintaan_ranap_.diagnosa,"                        
                        + "permintaan_ranap_.catatan,"
                        + "permintaan_ranap_.kd_dokter "
                        + "from permintaan_ranap_ "+
                    "inner join reg_periksa on permintaan_ranap_.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+                    
                    "where permintaan_ranap_.no_rawat in (select DISTINCT no_rawat from kamar_inap) "
                        + "and permintaan_ranap_.tanggal between ? and ? "+
                    (TCari.getText().equals("")?"":"and "
                            + "(permintaan_ranap_.no_rawat like ? "
                            + "or reg_periksa.no_rkm_medis like ? "
                            + "or pasien.nm_pasien like ? "
                            + "or penjab.png_jawab like ? "
                            + "or poliklinik.nm_poli like ? "
                            + "or dokter.nm_dokter like ? "                            
                            + "or permintaan_ranap_.diagnosa like ?)")+" "
                            + "order by permintaan_ranap_.tanggal");
                try {
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    if(!TCari.getText().equals("")){
                        ps.setString(3,"%"+TCari.getText().trim()+"%");
                        ps.setString(4,"%"+TCari.getText().trim()+"%");
                        ps.setString(5,"%"+TCari.getText().trim()+"%");
                        ps.setString(6,"%"+TCari.getText().trim()+"%");
                        ps.setString(7,"%"+TCari.getText().trim()+"%");
                        ps.setString(8,"%"+TCari.getText().trim()+"%");
                        ps.setString(9,"%"+TCari.getText().trim()+"%");                        
                    }
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabMode.addRow(new String[]{
                            rs.getString("no_rawat"),//0 no.rawat
                            rs.getString("no_rkm_medis"),//1 no.rm
                            rs.getString("nm_pasien"),//2 nama
                            rs.getString("jk"),//3 jk
                            rs.getString("umurdaftar")+" "+rs.getString("sttsumur"),//4 umur
                            rs.getString("no_tlp"),//5 no.telp
                            rs.getString("png_jawab"),//6 cara bayar
                            rs.getString("nm_poli"),//7 nama poli
                            rs.getString("nm_dokter"),//8 dr. pemeriksa
                            rs.getString("tanggal"),//9 tanggal
                            rs.getString("diagnosa"),//10 diagnosa
                            rs.getString("catatan"),//11 catatan
                            rs.getString("kd_dokter"),//12 kd dpjp
                            rs.getString("nm_dokter")//13 kd dpjp
                        });
                    }
                } catch (Exception e) {
                    System.out.println("Notif Kamar : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }


    public void emptTeks() {
        NoRw.setText("");
        NoRM.setText("");
        NmPasien.setText("");
        NoTelp.setText("");
        CaraBayar.setText("");
        Poli.setText("");
        nm_dokterrj.setText("");        
        Diagnosa.setText("");
        Catatan.setText("");
        DTPTgl.setDate(new Date());
        kddok.setText("");
        nmdok.setText("");        
        Diagnosa.requestFocus();
    }

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            NoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()); 
            NoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()); 
            NmPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            NoTelp.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            CaraBayar.setText(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            Poli.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
            nm_dokterrj.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());            
            Diagnosa.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            Catatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            kddok.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            nmdok.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            KdDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            Valid.SetTgl(DTPTgl,tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            
            String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString(); // Asumsi kolom 0 adalah no_rawat
            String query = "SELECT tgl_registrasi FROM reg_periksa WHERE no_rawat = ?";
            try (PreparedStatement ps = koneksi.prepareStatement(query)) {
                ps.setString(1, noRawat);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Valid.SetTgl(TglSO, rs.getString("tgl_registrasi"));
                    } else {
                        JOptionPane.showMessageDialog(null, "Data tidak ditemukan untuk no_rawat: " + noRawat);
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat mengambil data tgl_registrasi: " + e.getMessage());
                e.printStackTrace();
            }
            
        }
    }
    
    public void setNoRm(String norwt,String norm,String nama,String namadokter,String carabayar,String poli,String notelp,String kodedokter) {
        NoRw.setText(norwt);
        NoRM.setText(norm);
        NmPasien.setText(nama);
        nm_dokterrj.setText(namadokter);
        CaraBayar.setText(carabayar);
        Poli.setText(poli);
        NoTelp.setText(notelp);
        KdDokter.setText(kodedokter);
        TCari.setText(norwt);
        ChkInput.setSelected(true);
        aktif=false;                           
        isForm();
        
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,220));
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
        BtnSimpan.setEnabled(akses.getpermintaan_ranap());
        BtnHapus.setEnabled(akses.getpermintaan_ranap());
        BtnPrint.setEnabled(akses.getpermintaan_ranap());
        BtnKamarInap.setEnabled(akses.getkamar_inap());
        BtnRiwayatPasien.setEnabled(akses.getresume_pasien());
        BtnSuratPRI.setEnabled(akses.getbpjs_surat_pri());
        BtnEdit.setEnabled(akses.getpermintaan_ranap());   
        BtnPersetujuanRanap.setEnabled(akses.getsurat_persetujuan_rawat_inap());
    }

    private void jam(){
        ActionListener taskPerformer = (ActionEvent e) -> {
            if(aktif==true){
                nol_detik = "";
                Date now = Calendar.getInstance().getTime();
                nilai_detik = now.getSeconds();
                if (nilai_detik <= 9) {
                    nol_detik = "0";
                }

                detik = nol_detik + Integer.toString(nilai_detik);
                if(detik.equals("05")){
                    bookingbaru=Sequel.cariInteger("select count(*) from permintaan_ranap_ where no_rawat not in (select DISTINCT no_rawat from kamar_inap) ");
                    if(bookingbaru>0){
                        try {
                            music = new BackgroundMusic("./suara/alarm.mp3");
                            music.start();
                        } catch (Exception ex) {
                            System.out.println(ex);
                        }
                        
                        i=JOptionPane.showConfirmDialog(null, "Ada permintaan rawat inap baru, apa mau ditampilkan????","Konfirmasi",JOptionPane.YES_NO_OPTION);
                        if(i==JOptionPane.YES_OPTION){
                            R1.setSelected(true);
                            TCari.setText("");
                            tampil();
                        }
                    }
                }
            }                
        };
        new Timer(1000, taskPerformer).start();
    }
    
    private void isMenu(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(175,HEIGHT));
            FormMenu.setVisible(true); 
            ChkAccor.setVisible(true);
        }else if(ChkAccor.isSelected()==false){  
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
            FormMenu.setVisible(false);    
            ChkAccor.setVisible(true);
        }
    }

    private void CreatePDF(String FileName) {
    if (tabMode.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "Maaf, table masih kosong...!!!!");
        TCari.requestFocus();
        return;
    }

    if (tbObat.getSelectedRow() == -1) {
        JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data...!!!!");
        return;
    }

    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    Map<String, Object> param = new HashMap<>();
    param.put("Kode_Dokter", Sequel.cariIsi("SELECT dokter.nm_dokter FROM dokter WHERE dokter.kd_dokter=?", KdDokter.getText()));
    param.put("ttd", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/tandatangandokter/pages/upload/" + KdDokter.getText() + ".png");
    param.put("dpjp", Sequel.cariIsi("SELECT dpjp_ranap.kd_dokter FROM dpjp_ranap WHERE dpjp_ranap.kd_dokter=?", KdDokter.getText()));
    param.put("namars", akses.getnamars());
    param.put("alamatrs", akses.getalamatrs());
    param.put("kotars", akses.getkabupatenrs());
    param.put("propinsirs", akses.getpropinsirs());
    param.put("kontakrs", akses.getkontakrs());
    param.put("emailrs", akses.getemailrs());
    param.put("logo", Sequel.cariGambar("SELECT setting.logo FROM setting"));

    String norawat = NoRw.getText();
    String no_rawat_upload = norawat;

    try {
        // ambil no_rawat_upload kalau ada
        String cekUpload = Sequel.cariIsi("SELECT ifnull(no_rawat_upload,'') FROM permintaan_ranap_ WHERE no_rawat=?", norawat);
        if (!cekUpload.isEmpty()) {
            no_rawat_upload = cekUpload;
        }
    } catch (Exception e) {
        System.out.println("Notif Cek no_rawat_upload : " + e);
    }

    Valid.MyReportPDFqryUpload(
        "rptSuratPermintaanRawatInap.jasper",
        "report",
        "::[ Surat Permintaan Rawat Inap ]::",
        "SELECT permintaan_ranap_.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, reg_periksa.umurdaftar, reg_periksa.sttsumur, " +
        "pasien.no_tlp, penjab.png_jawab, poliklinik.nm_poli, dokter.nm_dokter, permintaan_ranap_.tanggal, permintaan_ranap_.diagnosa, " +
        "permintaan_ranap_.catatan, group_concat(dr.nm_dokter) AS nm_dokter_dpjp, reg_periksa.kd_dokter " +
        "FROM permintaan_ranap_ " +
        "INNER JOIN reg_periksa ON permintaan_ranap_.no_rawat=reg_periksa.no_rawat " +
        "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
        "INNER JOIN penjab ON reg_periksa.kd_pj=penjab.kd_pj " +
        "INNER JOIN dokter ON reg_periksa.kd_dokter=dokter.kd_dokter " +
        "INNER JOIN poliklinik ON reg_periksa.kd_poli=poliklinik.kd_poli " +
        "JOIN dpjp_ranap ON permintaan_ranap_.no_rawat=dpjp_ranap.no_rawat " +
        "JOIN dokter AS dr ON dpjp_ranap.kd_dokter=dr.kd_dokter " +
        "WHERE reg_periksa.no_rawat='" + no_rawat_upload + "'",
        FileName, param
    );

    this.setCursor(Cursor.getDefaultCursor());
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

    private void ppBerkasDigitalBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    if (tabMode.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "Maaf, data sudah habis...!!!!");
        TCari.requestFocus();
    } else if (tbObat.getSelectedRow() > -1) {
        String norawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim();
        String no_rawat_upload = norawat; // default pakai no_rawat biasa

        try {
            // Cari no_rkm_medis pasien tsb
            String no_rkm_medis = Sequel.cariIsi("SELECT no_rkm_medis FROM reg_periksa WHERE no_rawat=?", norawat);

            // Cari no_rawat rawat inap terbaru (jika ada)
            String norawatRanapTerbaru = Sequel.cariIsi(
                "SELECT no_rawat FROM reg_periksa WHERE no_rkm_medis=? AND status_lanjut='Ranap' ORDER BY tgl_registrasi DESC, jam_reg DESC LIMIT 1",
                no_rkm_medis
            );

            if (!norawatRanapTerbaru.equals("")) {
                no_rawat_upload = norawatRanapTerbaru;
            }
        } catch (Exception e) {
            System.out.println("Notif Cek no_rawat_upload : " + e);
        }

        DlgBerkasRawat berkas = new DlgBerkasRawat(null, true);
        berkas.setJudul("::[ Berkas Digital Perawatan ]::", "berkasrawat/pages");

        try {
            String url = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" +
                    koneksiDB.HYBRIDWEB() + "/berkasrawat/";

            if (akses.gethapus_berkas_digital_perawatan()) {
                url += "login2.php?act=login&usere=" + koneksiDB.USERHYBRIDWEB() +
                       "&passwordte=" + koneksiDB.PASHYBRIDWEB() + "&no_rawat=" + no_rawat_upload;
            } else {
                url += "login2nonhapus.php?act=login&usere=" + koneksiDB.USERHYBRIDWEB() +
                       "&passwordte=" + koneksiDB.PASHYBRIDWEB() + "&no_rawat=" + no_rawat_upload;
            }

            berkas.loadURL(url);
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
        }

        berkas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        berkas.setLocationRelativeTo(internalFrame1);
        berkas.setVisible(true);
    }

    this.setCursor(Cursor.getDefaultCursor());
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

        // Pastikan folder tmpJPG ada
        File jpgDir = new File("tmpJPG");
        if (!jpgDir.exists() && !jpgDir.mkdir()) {
            System.err.println("Gagal membuat folder tmpJPG.");
            document.close();
            return;
        }

        // Render halaman pertama PDF ke gambar
        BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);

        // Simpan ke JPG
        File jpgFile = new File(jpgDir, FileName + ".jpg");
        ImageIO.write(image, "jpg", jpgFile);

        // Tutup dokumen PDF
        document.close();

        System.out.println("Konversi berhasil: " + jpgFile.getAbsolutePath());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void UploadJPG(String FileName, String docpath) {
    try {
        File file = new File("tmpJPG/" + FileName + ".jpg");
        byte[] data = FileUtils.readFileToByteArray(file);

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(
                "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/upload.php?doc=" + docpath
        );

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("file", new ByteArrayBody(data, FileName + ".jpg"));
        postRequest.setEntity(reqEntity);
        httpClient.execute(postRequest);

        // --- Ambil no_rawat dari tbObat
        String no_rawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim();
        String no_rkm_medis = Sequel.cariIsi("SELECT no_rkm_medis FROM reg_periksa WHERE no_rawat=?", no_rawat);

        // --- Cari no_rawat rawat inap terbaru (jika ada)
        String no_rawat_upload = Sequel.cariIsi(
                "SELECT no_rawat FROM reg_periksa WHERE no_rkm_medis=? AND status_lanjut='Ranap' ORDER BY tgl_registrasi DESC, jam_reg DESC LIMIT 1",
                no_rkm_medis
        );

        // --- Kalau ketemu, ganti no_rawat pake yang upload
        if(!no_rawat_upload.equals("")){
            no_rawat = no_rawat_upload;
        }

        String kodeberkas = Sequel.cariIsi(
                "SELECT kode FROM master_berkas_digital WHERE nama LIKE '%SO Digital%'"
        );

        String lokasi_file = "pages/upload/" + FileName + ".jpg";

        boolean uploadSuccess;
        if (Sequel.cariInteger("SELECT COUNT(no_rawat) FROM berkas_digital_perawatan WHERE lokasi_file=?", lokasi_file) > 0) {
            // Update
            uploadSuccess = Sequel.mengedittf(
                    "berkas_digital_perawatan",
                    "lokasi_file=?",
                    "no_rawat=?,kode=?,lokasi_file=?",
                    4,
                    new String[]{no_rawat, kodeberkas, lokasi_file, lokasi_file}
            );
        } else {
            // Insert
            uploadSuccess = Sequel.menyimpantf(
                    "berkas_digital_perawatan",
                    "?,?,?",
                    "No.Rawat",
                    3,
                    new String[]{no_rawat, kodeberkas, lokasi_file}
            );
        }

        if (uploadSuccess) {
            JOptionPane.showMessageDialog(null, "Upload berhasil!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Upload gagal disimpan ke database.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }

    } catch (Exception e) {
        System.out.println("Upload error : " + e);
        JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat upload: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }
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
