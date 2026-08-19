package surat;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariDokter2;
import laporan.DlgCariPenyakit;
import laporan.DlgBerkasRawat;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
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
import simrskhanza.DlgCariPoli;
import simrskhanza.DlgCariPoli2;
import wa.WhatsappKirimFonnte;

/**
 *
 * @author dosen
 */
public class SuratSKDPAntarPoli extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private int i=0,kuota=0;
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private DlgCariDokter2 dokter2=new DlgCariDokter2(null,false);
    private DlgCariPoli poli=new DlgCariPoli(null,false);
    private DlgCariPoli2 poli2=new DlgCariPoli2(null,false);
    private String URUTNOREG="",status="",kdpoli="",nmpoli="",noantri="",aktifjadwal="";
    private DlgCariPenyakit penyakit=new DlgCariPenyakit(null,false);
    private String finger="";
    private final int COL_NO_RAWAT = 0;
    private final int COL_TAHUN = 1;
    private final int COL_NO_RM = 2;
    private final int COL_NAMA_PASIEN = 3;
    private final int COL_DIAGNOSA = 4;
    private final int COL_TERAPI = 5;
    private final int COL_ALASAN1 = 6;
    private final int COL_ALASAN2 = 7;
    private final int COL_RTL1 = 8;
    private final int COL_RTL2 = 9;
    private final int COL_TANGGAL_DATANG = 10;
    private final int COL_TANGGAL_RUJUKAN = 11;
    private final int COL_NO_SURAT = 12;
    private final int COL_NO_REG = 13;
    private final int COL_KD_DOKTER = 14;
    private final int COL_NM_DOKTER = 15;
    private final int COL_KD_POLI = 16;
    private final int COL_NM_POLI = 17;
    private final int COL_STATUS = 18;
    
    private String getNilaiTabel(int row, int col) {
        try {
            if(row < 0 || col < 0 || col >= tabMode.getColumnCount()){
                return "";
            }
            int modelRow = row;
            if(tbObat != null && tbObat.getRowSorter() != null && row < tbObat.getRowCount()){
                modelRow = tbObat.convertRowIndexToModel(row);
            }
            if(modelRow < 0 || modelRow >= tabMode.getRowCount()){
                return "";
            }
            Object nilai = tabMode.getValueAt(modelRow, col);
            return nilai == null ? "" : nilai.toString();
        } catch (Exception e) {
            return "";
        }
    }
    

    /** Creates new form DlgPemberianInfus
     * @param parent
     * @param modal */
    public SuratSKDPAntarPoli(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        tabMode=new DefaultTableModel(null,new Object[]{
                "No.Rawat","Tahun","No.RM","Nama Pasien","Diagnosa","Terapi","Alasan Kontrol","Alasan 2",
                "Rencana Tindak Lanjut 1","Rencana Tindak Lanjut 2","Periksa Kembali",
                "Tanggal Surat","No.Surat","No.Reg","Kode Dokter","Nama Dokter",
                "Kode Poli","Nama Poli","Status"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 19; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==COL_NO_RAWAT){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_TAHUN){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_NO_RM){
                column.setPreferredWidth(80);
            }else if(i==COL_NAMA_PASIEN){
                column.setPreferredWidth(150);
            }else if(i==COL_DIAGNOSA){
                column.setPreferredWidth(150);
            }else if(i==COL_TERAPI){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_ALASAN1){
                column.setPreferredWidth(150);
            }else if(i==COL_ALASAN2){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_RTL1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_RTL2){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_TANGGAL_DATANG){
                column.setPreferredWidth(115);
            }else if(i==COL_TANGGAL_RUJUKAN){
                column.setPreferredWidth(115);
            }else if(i==COL_NO_SURAT){
                column.setPreferredWidth(52);
            }else if(i==COL_NO_REG){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_KD_DOKTER){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_NM_DOKTER){
                column.setPreferredWidth(150);
            }else if(i==COL_KD_POLI){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==COL_NM_POLI){
                column.setPreferredWidth(150);
            }else if(i==COL_STATUS){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());


        TNoRM.setDocument(new batasInput((byte)15).getKata(TNoRM));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        Diagnosa.setDocument(new batasInput((int)50).getKata(Diagnosa));
        Terapi.setDocument(new batasInput((int)50).getKata(Terapi));
        Alasan1.setDocument(new batasInput((int)50).getKata(Alasan1));
        Alasan2.setDocument(new batasInput((int)50).getKata(Alasan2));
        Rtl1.setDocument(new batasInput((int)50).getKata(Rtl1));
        Rtl2.setDocument(new batasInput((int)50).getKata(Rtl2));
        NoReg.setDocument(new batasInput((byte)6).getKata(NoReg));
        KdDokter.setDocument(new batasInput((byte)20).getKata(KdDokter));
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
            public void windowOpened(WindowEvent e) {;}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){                    
                    KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    isNomer();
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
        
        dokter2.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {;}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter2.getTable().getSelectedRow()!= -1){                    
                    KdDokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),0).toString());
                    NmDokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),1).toString());
                    if(aktifjadwal.equals("aktif")){
                        kuota=Integer.parseInt(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),13).toString());
                    }
                    isNomer();                        
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
        
        poli.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli.getTable().getSelectedRow()!= -1){                    
                    KdPoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),0).toString());
                    NmPoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),1).toString());
                    isNomer();
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
        
        poli2.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli2.getTable().getSelectedRow()!= -1){                    
                    KdPoli.setText(poli2.getTable().getValueAt(poli2.getTable().getSelectedRow(),0).toString());
                    NmPoli.setText(poli2.getTable().getValueAt(poli2.getTable().getSelectedRow(),1).toString());
                    isNomer();
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
            aktifjadwal=koneksiDB.JADWALDOKTERDIREGISTRASI();
            URUTNOREG=koneksiDB.URUTNOREG();
        } catch (Exception ex) {
            aktifjadwal="";
            URUTNOREG="";
        }
        
        
        TanggalPeriksa.setDate(new Date());
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
        MnSurat = new javax.swing.JMenuItem();
        ppBerkasDigital = new javax.swing.JMenuItem();
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
        BtnAll = new widget.Button();
        BtnUpload = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass10 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        panelCari = new widget.panelisi();
        R1 = new widget.RadioButton();
        R2 = new widget.RadioButton();
        DTPCari1 = new widget.Tanggal();
        jLabel22 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        R3 = new widget.RadioButton();
        DTPCari3 = new widget.Tanggal();
        jLabel25 = new widget.Label();
        DTPCari4 = new widget.Tanggal();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRM = new widget.TextBox();
        jLabel9 = new widget.Label();
        NmDokter = new widget.TextBox();
        TPasien = new widget.TextBox();
        TanggalSurat = new widget.Tanggal();
        Status = new widget.ComboBox();
        jLabel10 = new widget.Label();
        KdDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel37 = new widget.Label();
        jLabel11 = new widget.Label();
        KdPoli = new widget.TextBox();
        NmPoli = new widget.TextBox();
        BtnPoli = new widget.Button();
        jLabel5 = new widget.Label();
        Alasan1 = new widget.TextBox();
        Alasan2 = new widget.TextBox();
        jLabel8 = new widget.Label();
        jLabel12 = new widget.Label();
        Rtl1 = new widget.TextBox();
        jLabel13 = new widget.Label();
        Rtl2 = new widget.TextBox();
        jLabel14 = new widget.Label();
        TanggalPeriksa = new widget.Tanggal();
        jLabel15 = new widget.Label();
        NoSurat = new widget.TextBox();
        Diagnosa = new widget.TextBox();
        jLabel16 = new widget.Label();
        jLabel17 = new widget.Label();
        Terapi = new widget.TextBox();
        NoReg = new widget.TextBox();
        jLabel18 = new widget.Label();
        btnDiagnosa = new widget.Button();
        BtnCari1 = new widget.Button();
        NoRawat = new widget.TextBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnSurat.setBackground(new java.awt.Color(255, 255, 254));
        MnSurat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnSurat.setForeground(new java.awt.Color(50, 50, 50));
        MnSurat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnSurat.setText("Surat Kontrol");
        MnSurat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnSurat.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnSurat.setName("MnSurat"); // NOI18N
        MnSurat.setPreferredSize(new java.awt.Dimension(180, 26));
        MnSurat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnSuratActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnSurat);

        ppBerkasDigital.setBackground(new java.awt.Color(255, 255, 254));
        ppBerkasDigital.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBerkasDigital.setForeground(new java.awt.Color(50, 50, 50));
        ppBerkasDigital.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBerkasDigital.setText("Berkas Digital Perawatan");
        ppBerkasDigital.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBerkasDigital.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBerkasDigital.setName("ppBerkasDigital"); // NOI18N
        ppBerkasDigital.setPreferredSize(new java.awt.Dimension(220, 26));
        ppBerkasDigital.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBerkasDigitalBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBerkasDigital);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Surat Kontrol ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbObat.setAutoCreateRowSorter(true);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbObatKeyReleased(evt);
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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        panelGlass10.setName("panelGlass10"); // NOI18N
        panelGlass10.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass10.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(450, 23));
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
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

        R1.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R1);
        R1.setSelected(true);
        R1.setText("Menunggu");
        R1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R1.setName("R1"); // NOI18N
        R1.setPreferredSize(new java.awt.Dimension(85, 23));
        panelCari.add(R1);

        R2.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R2);
        R2.setText("Tanggal :");
        R2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R2.setName("R2"); // NOI18N
        R2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelCari.add(R2);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "21-11-2024" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(100, 23));
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

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("s.d");
        jLabel22.setName("jLabel22"); // NOI18N
        jLabel22.setPreferredSize(new java.awt.Dimension(25, 23));
        panelCari.add(jLabel22);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "21-11-2024" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPCari2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari2KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari2);

        R3.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R3);
        R3.setText("Selesai :");
        R3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R3.setName("R3"); // NOI18N
        R3.setPreferredSize(new java.awt.Dimension(85, 23));
        panelCari.add(R3);

        DTPCari3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "21-11-2024" }));
        DTPCari3.setDisplayFormat("dd-MM-yyyy");
        DTPCari3.setName("DTPCari3"); // NOI18N
        DTPCari3.setOpaque(false);
        DTPCari3.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPCari3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPCari3ItemStateChanged(evt);
            }
        });
        DTPCari3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari3KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari3);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("s.d");
        jLabel25.setName("jLabel25"); // NOI18N
        jLabel25.setPreferredSize(new java.awt.Dimension(25, 23));
        panelCari.add(jLabel25);

        DTPCari4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "21-11-2024" }));
        DTPCari4.setDisplayFormat("dd-MM-yyyy");
        DTPCari4.setName("DTPCari4"); // NOI18N
        DTPCari4.setOpaque(false);
        DTPCari4.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPCari4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPCari4ItemStateChanged(evt);
            }
        });
        DTPCari4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari4KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari4);

        jPanel3.add(panelCari, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 216));
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
        FormInput.setPreferredSize(new java.awt.Dimension(190, 107));
        FormInput.setLayout(null);

        jLabel4.setText("Pasien :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 92, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(96, 10, 87, 23);

        jLabel9.setText("Spesialis/Sub :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(0, 70, 92, 23);

        NmDokter.setEditable(false);
        NmDokter.setHighlighter(null);
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(190, 70, 160, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(185, 10, 190, 23);

        TanggalSurat.setForeground(new java.awt.Color(50, 70, 50));
        TanggalSurat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "21-11-2024 11:50:18" }));
        TanggalSurat.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TanggalSurat.setName("TanggalSurat"); // NOI18N
        TanggalSurat.setOpaque(false);
        TanggalSurat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalSuratKeyPressed(evt);
            }
        });
        FormInput.add(TanggalSurat);
        TanggalSurat.setBounds(290, 40, 132, 23);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Menunggu", "Sudah Periksa", "Batal Periksa" }));
        Status.setName("Status"); // NOI18N
        Status.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusKeyPressed(evt);
            }
        });
        FormInput.add(Status);
        Status.setBounds(260, 320, 130, 23);

        jLabel10.setText("Tanggal Surat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(190, 40, 95, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        FormInput.add(KdDokter);
        KdDokter.setBounds(100, 70, 87, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('X');
        BtnDokter.setToolTipText("Alt+X");
        BtnDokter.setName("BtnDokter"); // NOI18N
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
        BtnDokter.setBounds(350, 70, 28, 23);

        jLabel37.setText("Status :");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(200, 320, 50, 23);

        jLabel11.setText("Unit/Poli :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(380, 70, 80, 23);

        KdPoli.setEditable(false);
        KdPoli.setHighlighter(null);
        KdPoli.setName("KdPoli"); // NOI18N
        FormInput.add(KdPoli);
        KdPoli.setBounds(470, 70, 70, 23);

        NmPoli.setEditable(false);
        NmPoli.setHighlighter(null);
        NmPoli.setName("NmPoli"); // NOI18N
        FormInput.add(NmPoli);
        NmPoli.setBounds(550, 70, 165, 23);

        BtnPoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli.setMnemonic('X');
        BtnPoli.setToolTipText("Alt+X");
        BtnPoli.setName("BtnPoli"); // NOI18N
        BtnPoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPoliActionPerformed(evt);
            }
        });
        BtnPoli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPoliKeyPressed(evt);
            }
        });
        FormInput.add(BtnPoli);
        BtnPoli.setBounds(720, 70, 28, 23);

        jLabel5.setText("Alasan Kontrol :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(0, 100, 92, 23);

        Alasan1.setHighlighter(null);
        Alasan1.setName("Alasan1"); // NOI18N
        Alasan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Alasan1KeyPressed(evt);
            }
        });
        FormInput.add(Alasan1);
        Alasan1.setBounds(100, 100, 620, 23);

        Alasan2.setHighlighter(null);
        Alasan2.setName("Alasan2"); // NOI18N
        Alasan2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Alasan2KeyPressed(evt);
            }
        });
        FormInput.add(Alasan2);
        Alasan2.setBounds(490, 350, 266, 23);

        jLabel8.setText("Alasan 2 :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(400, 350, 95, 23);

        jLabel12.setText("Tindak Lanjut 1 :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(20, 380, 92, 23);

        Rtl1.setHighlighter(null);
        Rtl1.setName("Rtl1"); // NOI18N
        Rtl1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Rtl1KeyPressed(evt);
            }
        });
        FormInput.add(Rtl1);
        Rtl1.setBounds(120, 380, 279, 23);

        jLabel13.setText("Tindak Lanjut 2 :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(400, 380, 95, 23);

        Rtl2.setHighlighter(null);
        Rtl2.setName("Rtl2"); // NOI18N
        Rtl2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Rtl2KeyPressed(evt);
            }
        });
        FormInput.add(Rtl2);
        Rtl2.setBounds(490, 380, 266, 23);

        jLabel14.setText("Periksa Kontrol :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(450, 40, 92, 23);

        TanggalPeriksa.setForeground(new java.awt.Color(50, 70, 50));
        TanggalPeriksa.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "21-11-2024 11:50:18" }));
        TanggalPeriksa.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TanggalPeriksa.setName("TanggalPeriksa"); // NOI18N
        TanggalPeriksa.setOpaque(false);
        TanggalPeriksa.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TanggalPeriksaItemStateChanged(evt);
            }
        });
        TanggalPeriksa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalPeriksaKeyPressed(evt);
            }
        });
        FormInput.add(TanggalPeriksa);
        TanggalPeriksa.setBounds(550, 40, 132, 23);

        jLabel15.setText("No.Surat :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(40, 40, 60, 23);

        NoSurat.setHighlighter(null);
        NoSurat.setName("NoSurat"); // NOI18N
        NoSurat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoSuratKeyPressed(evt);
            }
        });
        FormInput.add(NoSurat);
        NoSurat.setBounds(100, 40, 85, 23);

        Diagnosa.setHighlighter(null);
        Diagnosa.setName("Diagnosa"); // NOI18N
        Diagnosa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosaKeyPressed(evt);
            }
        });
        FormInput.add(Diagnosa);
        Diagnosa.setBounds(100, 130, 620, 23);

        jLabel16.setText("Diagnosa :");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 130, 92, 23);

        jLabel17.setText("Terapi :");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(400, 320, 95, 23);

        Terapi.setHighlighter(null);
        Terapi.setName("Terapi"); // NOI18N
        Terapi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TerapiKeyPressed(evt);
            }
        });
        FormInput.add(Terapi);
        Terapi.setBounds(490, 320, 266, 23);

        NoReg.setHighlighter(null);
        NoReg.setName("NoReg"); // NOI18N
        NoReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoRegKeyPressed(evt);
            }
        });
        FormInput.add(NoReg);
        NoReg.setBounds(130, 310, 70, 23);

        jLabel18.setText("No.Reg :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(30, 310, 95, 23);

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
        btnDiagnosa.setBounds(720, 130, 28, 23);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/whatsapp (2).png"))); // NOI18N
        BtnCari1.setMnemonic('7');
        BtnCari1.setText("Whatsapp");
        BtnCari1.setToolTipText("Alt+7");
        BtnCari1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnCari1.setName("BtnCari1"); // NOI18N
        BtnCari1.setPreferredSize(new java.awt.Dimension(100, 23));
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
        BtnCari1.setBounds(750, 60, 100, 40);

        NoRawat.setEditable(false);
        NoRawat.setHighlighter(null);
        NoRawat.setName("NoRawat"); // NOI18N
        FormInput.add(NoRawat);
        NoRawat.setBounds(380, 10, 130, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        Valid.pindah(evt,Status,KdDokter);
        
}//GEN-LAST:event_TNoRMKeyPressed

    private void TanggalSuratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalSuratKeyPressed
        Valid.pindah(evt,TCari,Status);
}//GEN-LAST:event_TanggalSuratKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRM,"pasien");
        }else if(NmDokter.getText().trim().equals("")||KdDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Dokter");
        }else if(NmPoli.getText().trim().equals("")||NmPoli.getText().trim().equals("")){
            Valid.textKosong(KdPoli,"Poli");
        }else if(NoSurat.getText().trim().equals("")){
            Valid.textKosong(NoSurat,"No.Surat");
        }else if(NoReg.getText().trim().equals("")){
            Valid.textKosong(NoReg,"No.Antri");
        }else if(Alasan1.getText().trim().equals("")){
            Valid.textKosong(Terapi,"Alasan Kontrol");
        }else if(Diagnosa.getText().trim().equals("")){
            Valid.textKosong(Diagnosa,"Diagnosa");
        }else{
             if(akses.getkode().equals("Admin Utama")){
                isBooking();
            }else{
                if(aktifjadwal.equals("aktif")){
                    if(Sequel.cariInteger("select count(booking_registrasi.no_rkm_medis) from booking_registrasi where booking_registrasi.kd_dokter='"+KdDokter.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"' ")>=kuota){
                        JOptionPane.showMessageDialog(null,"Eiiits, Kuota registrasi penuh..!!!");
                        TCari.requestFocus();
                    }else{
                        isBooking();
                    }                    
                }else{
                    isBooking();
                } 
            }            
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,NoReg,BtnBatal);
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
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TanggalSurat.requestFocus();
        }else if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal menghapus. Pilih dulu data yang mau dihapus.\nKlik data pada table untuk memilih...!!!!");
        }else if(!(TPasien.getText().trim().equals(""))){
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.queryu2tf("delete from skdp_bpjs where tahun=? and no_antrian=?",2,new String[]{
                    getNilaiTabel(tbObat.getSelectedRow(),COL_TAHUN),getNilaiTabel(tbObat.getSelectedRow(),COL_NO_SURAT)
                })==true){
                    Sequel.queryu2("delete from booking_registrasi where no_rkm_medis=? and tanggal_periksa=?",2,new String[]{
                        getNilaiTabel(tbObat.getSelectedRow(),COL_NO_RM),getNilaiTabel(tbObat.getSelectedRow(),COL_TANGGAL_DATANG)
                    });
                    tabMode.removeRow(tbObat.getSelectedRow());
                    LCount.setText(""+tabMode.getRowCount());
                    emptTeks();
                }                        
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau anda hapus...\n Klik data pada table untuk memilih data...!!!!");
            }  
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
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            Map<String, Object> param = new HashMap<>();  
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            
            Sequel.queryu("delete from temporary_booking_registrasi");
            for(i=0;i<tabMode.getRowCount();i++){ 
                Sequel.menyimpan("temporary_booking_registrasi","'0','"+
                    getNilaiTabel(i,COL_TAHUN)+"','"+
                    getNilaiTabel(i,COL_NO_RM)+"','"+
                    getNilaiTabel(i,COL_NAMA_PASIEN)+"','"+
                    getNilaiTabel(i,COL_DIAGNOSA)+"','"+
                    getNilaiTabel(i,COL_TERAPI)+"','"+
                    getNilaiTabel(i,COL_ALASAN1)+"','"+
                    getNilaiTabel(i,COL_ALASAN2)+"','"+
                    getNilaiTabel(i,COL_RTL1)+"','"+
                    getNilaiTabel(i,COL_RTL2)+"','"+
                    getNilaiTabel(i,COL_TANGGAL_DATANG)+"','"+
                    getNilaiTabel(i,COL_TANGGAL_RUJUKAN)+"','"+
                    getNilaiTabel(i,COL_NO_SURAT)+"','"+
                    getNilaiTabel(i,COL_NO_REG)+"','"+
                    getNilaiTabel(i,COL_KD_DOKTER)+"','"+
                    getNilaiTabel(i,COL_NM_DOKTER)+"','"+
                    getNilaiTabel(i,COL_KD_POLI)+"','"+
                    getNilaiTabel(i,COL_NM_POLI)+"','"+
                    getNilaiTabel(i,COL_STATUS)+"','','','','','','','','','','','','','','','','','','',''","Rekap Nota Pembayaran");
            }
             
            Valid.MyReport("rptSuratSKDPBPJS2.jasper","report","::[ Laporan Daftar Surat Kontrol ]::",param);
        }
        this.setCursor(Cursor.getDefaultCursor());
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
            Valid.pindah(evt, BtnCari, TPasien);
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

private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
    if(aktifjadwal.equals("aktif")){
        if(akses.getkode().equals("Admin Utama")){
            dokter.isCek();        
            dokter.TCari.requestFocus();
            dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dokter.setLocationRelativeTo(internalFrame1);
            dokter.setVisible(true);
        }else{
            dokter2.setPoli(NmPoli.getText());
            dokter2.isCek();  
            dokter2.SetHari(TanggalPeriksa.getDate());   
            dokter2.tampil();
            dokter2.TCari.requestFocus();
            dokter2.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dokter2.setLocationRelativeTo(internalFrame1);
            dokter2.setVisible(true);
        }                
    }else{
        dokter.isCek();        
        dokter.TCari.requestFocus();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }
}//GEN-LAST:event_BtnDokterActionPerformed

private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
    if(evt.getKeyCode()==KeyEvent.VK_SPACE){
        BtnDokterActionPerformed(null);
    }else{
        Valid.pindah(evt,Rtl2,BtnPoli);
    }        
}//GEN-LAST:event_BtnDokterKeyPressed

private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
  isForm();                
}//GEN-LAST:event_ChkInputActionPerformed

    private void DTPCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari1KeyPressed

    }//GEN-LAST:event_DTPCari1KeyPressed

    private void DTPCari2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari2KeyPressed
        R2.setSelected(true);
    }//GEN-LAST:event_DTPCari2KeyPressed

    private void DTPCari3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari3KeyPressed

    private void DTPCari4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari4KeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRM,"pasien");
        }else if(NmDokter.getText().trim().equals("")||KdDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Dokter");
        }else if(NmPoli.getText().trim().equals("")||NmPoli.getText().trim().equals("")){
            Valid.textKosong(KdPoli,"Poli");
        }else if(NoSurat.getText().trim().equals("")){
            Valid.textKosong(NoSurat,"No.Surat");
        }else if(NoReg.getText().trim().equals("")){
            Valid.textKosong(NoReg,"No.Antri");
        }else if(Terapi.getText().trim().equals("")){
            Valid.textKosong(Terapi,"Terapi");
        }else if(Diagnosa.getText().trim().equals("")){
            Valid.textKosong(Diagnosa,"Diagnosa");
        }else{
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.mengedittf("skdp_bpjs","tahun=? and no_antrian=?","tahun=?,no_rkm_medis=?,diagnosa=?,terapi=?,alasan1=?,alasan2=?,rtl1=?,rtl2=?,tanggal_datang=?,tanggal_rujukan=?,no_antrian=?,kd_dokter=?,status=?",15,new String[]{
                        TanggalPeriksa.getSelectedItem().toString().substring(6,10),TNoRM.getText(),Diagnosa.getText(),Terapi.getText(),
                        Alasan1.getText(),Alasan2.getText(),Rtl1.getText(),Rtl2.getText(),Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),
                        Valid.SetTgl(TanggalSurat.getSelectedItem()+""),NoSurat.getText(),KdDokter.getText(),Status.getSelectedItem().toString(),
                        getNilaiTabel(tbObat.getSelectedRow(),COL_TAHUN),getNilaiTabel(tbObat.getSelectedRow(),COL_NO_SURAT)
                  })==true){
                    Sequel.mengedit3("booking_registrasi","no_rkm_medis=? and tanggal_periksa=?","tanggal_booking=?,no_rkm_medis=?,tanggal_periksa=?,kd_dokter=?,kd_poli=?,no_reg=?",8,new String[]{
                         Valid.SetTgl(TanggalSurat.getSelectedItem()+""),TNoRM.getText(),
                         Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),KdDokter.getText(),
                         KdPoli.getText(),NoReg.getText(),getNilaiTabel(tbObat.getSelectedRow(),COL_NO_RM),
                         getNilaiTabel(tbObat.getSelectedRow(),COL_TANGGAL_DATANG)
                    });
                    tbObat.setValueAt(cariNoRawatKontrol(TNoRM.getText(), Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""), KdPoli.getText()),tbObat.getSelectedRow(),COL_NO_RAWAT);
                    tbObat.setValueAt(TanggalPeriksa.getSelectedItem().toString().substring(6,10),tbObat.getSelectedRow(),COL_TAHUN);
                    tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),COL_NO_RM);
                    tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),COL_NAMA_PASIEN);
                    tbObat.setValueAt(Diagnosa.getText(),tbObat.getSelectedRow(),COL_DIAGNOSA);
                    tbObat.setValueAt(Terapi.getText(),tbObat.getSelectedRow(),COL_TERAPI);
                    tbObat.setValueAt(Alasan1.getText(),tbObat.getSelectedRow(),COL_ALASAN1);
                    tbObat.setValueAt(Alasan2.getText(),tbObat.getSelectedRow(),COL_ALASAN2);
                    tbObat.setValueAt(Rtl1.getText(),tbObat.getSelectedRow(),COL_RTL1);
                    tbObat.setValueAt(Rtl2.getText(),tbObat.getSelectedRow(),COL_RTL2);
                    tbObat.setValueAt(Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+" "+TanggalPeriksa.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),COL_TANGGAL_DATANG);
                    tbObat.setValueAt(Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),COL_TANGGAL_RUJUKAN);
                    tbObat.setValueAt(NoSurat.getText(),tbObat.getSelectedRow(),COL_NO_SURAT);
                    tbObat.setValueAt(NoReg.getText(),tbObat.getSelectedRow(),COL_NO_REG);
                    tbObat.setValueAt(KdDokter.getText(),tbObat.getSelectedRow(),COL_KD_DOKTER);
                    tbObat.setValueAt(NmDokter.getText(),tbObat.getSelectedRow(),COL_NM_DOKTER);
                    tbObat.setValueAt(KdPoli.getText(),tbObat.getSelectedRow(),COL_KD_POLI);
                    tbObat.setValueAt(NmPoli.getText(),tbObat.getSelectedRow(),COL_NM_POLI);
                    tbObat.setValueAt(Status.getSelectedItem().toString(),tbObat.getSelectedRow(),COL_STATUS);
                    emptTeks();
                }
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau anda ganti...\n Klik data pada table untuk memilih data...!!!!");
            }                
        }
    }//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnKeluar);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void StatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusKeyPressed
        Valid.pindah(evt,TanggalSurat,Diagnosa);
    }//GEN-LAST:event_StatusKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void DTPCari1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPCari1ItemStateChanged
        R2.setSelected(true);
    }//GEN-LAST:event_DTPCari1ItemStateChanged

    private void DTPCari3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPCari3ItemStateChanged
        R3.setSelected(true);
    }//GEN-LAST:event_DTPCari3ItemStateChanged

    private void DTPCari4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPCari4ItemStateChanged
        R3.setSelected(true);
    }//GEN-LAST:event_DTPCari4ItemStateChanged

    private void Alasan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Alasan1KeyPressed
        Valid.pindah(evt,Terapi,Alasan2);
    }//GEN-LAST:event_Alasan1KeyPressed

    private void Alasan2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Alasan2KeyPressed
        Valid.pindah(evt,Alasan1,Rtl1);
    }//GEN-LAST:event_Alasan2KeyPressed

    private void Rtl1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Rtl1KeyPressed
        Valid.pindah(evt,Alasan2,Rtl2);
    }//GEN-LAST:event_Rtl1KeyPressed

    private void Rtl2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Rtl2KeyPressed
        Valid.pindah(evt,Rtl1,BtnDokter);
    }//GEN-LAST:event_Rtl2KeyPressed

    private void TanggalPeriksaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalPeriksaKeyPressed
        Valid.pindah(evt,BtnPoli,NoSurat);
    }//GEN-LAST:event_TanggalPeriksaKeyPressed

    private void NoSuratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoSuratKeyPressed
        Valid.pindah(evt,TanggalPeriksa,NoReg);
    }//GEN-LAST:event_NoSuratKeyPressed

    private void TanggalPeriksaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TanggalPeriksaItemStateChanged
        try {
            isNomer();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_TanggalPeriksaItemStateChanged

    private void DiagnosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosaKeyPressed
        Valid.pindah(evt,Status,Terapi);
    }//GEN-LAST:event_DiagnosaKeyPressed

    private void TerapiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TerapiKeyPressed
        Valid.pindah(evt,Diagnosa,Alasan1);
    }//GEN-LAST:event_TerapiKeyPressed

    private void BtnPoliKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPoliKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPoliActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnDokter,TanggalPeriksa);
        }
    }//GEN-LAST:event_BtnPoliKeyPressed

    private void BtnPoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoliActionPerformed
        if(aktifjadwal.equals("aktif")){
            if(akses.getkode().equals("Admin Utama")){
                poli.isCek();        
                poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                poli.setLocationRelativeTo(internalFrame1);
                poli.setVisible(true);
            }else{
                poli2.isCek();                     
                poli2.SetHari(TanggalPeriksa.getDate());
                poli2.tampil(); 
                poli2.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                poli2.setLocationRelativeTo(internalFrame1);
                poli2.setVisible(true);
            }                
        }else{
            poli.isCek();        
            poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            poli.setLocationRelativeTo(internalFrame1);
            poli.setVisible(true);
        }
    }//GEN-LAST:event_BtnPoliActionPerformed

    private void NoRegKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoRegKeyPressed
        Valid.pindah(evt,NoSurat,BtnSimpan);
    }//GEN-LAST:event_NoRegKeyPressed

    private void MnSuratActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnSuratActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TanggalSurat.requestFocus();
        }else if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal mencetak. Pilih dulu data yang mau dicetak.\nKlik data pada table untuk memilih...!!!!");
        }else if(!(TPasien.getText().trim().equals(""))){
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();  
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());   
                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",getNilaiTabel(tbObat.getSelectedRow(),COL_KD_DOKTER));
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+getNilaiTabel(tbObat.getSelectedRow(),COL_NM_DOKTER)+"\nID "+(finger.equals("")?getNilaiTabel(tbObat.getSelectedRow(),COL_KD_DOKTER):finger)+"\n"+Valid.SetTgl3(getNilaiTabel(tbObat.getSelectedRow(),COL_TANGGAL_RUJUKAN)));
                Sequel.queryu("delete from temporary_booking_registrasi");                
                Sequel.menyimpan("temporary_booking_registrasi","'0','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_TAHUN)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_NO_RM)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_NAMA_PASIEN)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_DIAGNOSA)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_TERAPI)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_ALASAN1)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_ALASAN2)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_RTL1)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_RTL2)+"','"+
                    TanggalPeriksa.getSelectedItem()+"','"+
                    TanggalSurat.getSelectedItem()+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_NO_SURAT)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_NO_REG)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_KD_DOKTER)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_NM_DOKTER)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_KD_POLI)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_NM_POLI)+"','"+
                    getNilaiTabel(tbObat.getSelectedRow(),COL_STATUS)+"','','','','','','','','','','','','','','','','','','',''","Surat Kontrol");

                Valid.MyReport("rptSuratSKDPBPJS2.jasper","report","::[ Surat Kontrol ]::",param); 
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau dicetak...\n Klik data pada table untuk memilih data...!!!!");
            }  
        }
    }//GEN-LAST:event_MnSuratActionPerformed

    private void tbObatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbObatKeyReleased

    private void btnDiagnosaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiagnosaActionPerformed
        penyakit.isCek();
        penyakit.emptTeks();
        penyakit.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penyakit.setLocationRelativeTo(internalFrame1);
        penyakit.setVisible(true);
    }//GEN-LAST:event_btnDiagnosaActionPerformed

    private void btnDiagnosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDiagnosaKeyPressed
        Valid.pindah(evt,Status,Terapi);
    }//GEN-LAST:event_btnDiagnosaKeyPressed

    private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        if(TPasien.getText().trim().equals("")&&TNoRM.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu data pasien...!!!");
            TCari.requestFocus();
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            WhatsappKirimFonnte kirim=new WhatsappKirimFonnte(null,false);
            kirim.setNoRm(TNoRM.getText(),NoRawat.getText(),NmPoli.getText(),NmDokter.getText(),TanggalPeriksa.getDate());
            kirim.setSize(720,330);
            kirim.setLocationRelativeTo(internalFrame1);
            kirim.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnCari1ActionPerformed

    private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCari1KeyPressed

    private void ppBerkasDigitalBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBerkasDigitalBtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (tabMode.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "Maaf, data sudah habis...!!!!");
                TCari.requestFocus();
            } else if (tbObat.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(null, "Silahkan pilih dulu data SKDP Antar Poli yang mau dibuka berkas digitalnya...!!!!");
            } else {
                String noRawatUpload = cariNoRawatKontrolUntukUpload();
                if (noRawatUpload.equals("")) {
                    JOptionPane.showMessageDialog(null,
                            "No. rawat tujuan berkas digital tidak ditemukan.\n"
                            + "Pastikan pasien sudah registrasi pada tanggal kontrol/periksa kembali.",
                            "Berkas Digital Perawatan", JOptionPane.WARNING_MESSAGE);
                } else {
                    bukaBerkasDigitalPerawatan(noRawatUpload);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif buka berkas digital SKDP Antar Poli : " + e);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_ppBerkasDigitalBtnPrintActionPerformed



    private void BtnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUploadActionPerformed
        if (tbObat.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih dulu data SKDP Antar Poli yang mau di-upload...!!!!");
            return;
        }

        String noRawatUpload = cariNoRawatKontrolUntukUpload();
        if (noRawatUpload.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "No. rawat tujuan upload tidak ditemukan.\n"
                    + "Pastikan pasien sudah registrasi pada tanggal kontrol/periksa kembali.",
                    "Upload SKDP Antar Poli", JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
            String timestamp = sdf.format(new Date());
            String fileName = timestamp + "_" + noRawatUpload.replace("/", "") + "_SKDP_ANTAR_POLI";

            if (!CreatePDF(fileName)) {
                return;
            }
            ConvertPDFtoJPG(fileName);
            UploadJPG(fileName, "berkasrawat/pages/upload/", noRawatUpload);
            HapusJPG();
            bukaBerkasDigitalPerawatan(noRawatUpload);
        } catch (Exception e) {
            System.out.println("Notif Upload SKDP Antar Poli : " + e);
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat upload : " + e.getMessage(), "Upload SKDP Antar Poli", JOptionPane.ERROR_MESSAGE);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnUploadActionPerformed

    private void BtnUploadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUploadKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnUploadActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnAll,BtnKeluar);
        }
    }//GEN-LAST:event_BtnUploadKeyPressed

    private String cariNoRawatKontrolUntukUpload() {
        String noRawatKontrol = "";
        if (tbObat.getSelectedRow() == -1) {
            return noRawatKontrol;
        }

        noRawatKontrol = getNilaiTabel(tbObat.getSelectedRow(), COL_NO_RAWAT).trim();
        if (!noRawatKontrol.equals("")) {
            return noRawatKontrol;
        }

        String noRm = getNilaiTabel(tbObat.getSelectedRow(), COL_NO_RM).trim();
        String tglKontrol = getNilaiTabel(tbObat.getSelectedRow(), COL_TANGGAL_DATANG).trim();
        String kdPoliTujuan = getNilaiTabel(tbObat.getSelectedRow(), COL_KD_POLI).trim();
        return cariNoRawatKontrol(noRm, tglKontrol, kdPoliTujuan);
    }

    private String cariNoRawatKontrol(String noRm, String tglKontrol, String kdPoliTujuan) {
        String noRawatKontrol = "";
        if (noRm == null || noRm.trim().equals("") || tglKontrol == null || tglKontrol.trim().equals("")) {
            return noRawatKontrol;
        }

        tglKontrol = tglKontrol.trim();
        if (tglKontrol.length() >= 10) {
            tglKontrol = tglKontrol.substring(0, 10);
        }

        PreparedStatement psUpload = null;
        ResultSet rsUpload = null;
        try {
            psUpload = koneksi.prepareStatement(
                    "select reg_periksa.no_rawat from reg_periksa "
                    + "where reg_periksa.no_rkm_medis=? "
                    + "and reg_periksa.tgl_registrasi=? "
                    + "order by if(reg_periksa.kd_poli=?,0,1), reg_periksa.jam_reg asc limit 1");
            psUpload.setString(1, noRm.trim());
            psUpload.setString(2, tglKontrol);
            psUpload.setString(3, kdPoliTujuan == null ? "" : kdPoliTujuan.trim());
            rsUpload = psUpload.executeQuery();
            if (rsUpload.next()) {
                noRawatKontrol = rsUpload.getString("no_rawat");
            }
        } catch (Exception e) {
            System.out.println("Notif cari no_rawat upload SKDP Antar Poli : " + e);
        } finally {
            try {
                if (rsUpload != null) {
                    rsUpload.close();
                }
                if (psUpload != null) {
                    psUpload.close();
                }
            } catch (Exception e) {
                System.out.println("Notif close cari no_rawat upload SKDP Antar Poli : " + e);
            }
        }
        return noRawatKontrol;
    }

    private void bukaBerkasDigitalPerawatan(String noRawatUpload) {
        if (noRawatUpload == null || noRawatUpload.trim().equals("")) {
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgBerkasRawat berkas = new DlgBerkasRawat(null, true);
        berkas.setJudul("::[ Berkas Digital Perawatan ]::", "berkasrawat/pages");
        try {
            if (akses.gethapus_berkas_digital_perawatan() == true) {
                berkas.loadURL("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/" + "berkasrawat/login2.php?act=login&usere=" + koneksiDB.USERHYBRIDWEB() + "&passwordte=" + koneksiDB.PASHYBRIDWEB() + "&no_rawat=" + noRawatUpload);
            } else {
                berkas.loadURL("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/" + "berkasrawat/login2nonhapus.php?act=login&usere=" + koneksiDB.USERHYBRIDWEB() + "&passwordte=" + koneksiDB.PASHYBRIDWEB() + "&no_rawat=" + noRawatUpload);
            }
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
        }

        berkas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        berkas.setLocationRelativeTo(internalFrame1);
        berkas.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }

    private boolean CreatePDF(String fileName) {
        if (tbObat.getSelectedRow() != -1) {
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("namars", akses.getnamars());
                param.put("alamatrs", akses.getalamatrs());
                param.put("kotars", akses.getkabupatenrs());
                param.put("propinsirs", akses.getpropinsirs());
                param.put("kontakrs", akses.getkontakrs());
                param.put("emailrs", akses.getemailrs());
                param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
                finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", getNilaiTabel(tbObat.getSelectedRow(), COL_KD_DOKTER));
                param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + getNilaiTabel(tbObat.getSelectedRow(), COL_NM_DOKTER) + "\nID " + (finger.equals("") ? getNilaiTabel(tbObat.getSelectedRow(), COL_KD_DOKTER) : finger) + "\n" + Valid.SetTgl3(getNilaiTabel(tbObat.getSelectedRow(), COL_TANGGAL_RUJUKAN)));

                Sequel.queryu("delete from temporary_booking_registrasi");
                Sequel.menyimpan("temporary_booking_registrasi", "'0','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_TAHUN) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_NO_RM) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_NAMA_PASIEN) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_DIAGNOSA) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_TERAPI) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_ALASAN1) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_ALASAN2) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_RTL1) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_RTL2) + "','"
                        + TanggalPeriksa.getSelectedItem() + "','"
                        + TanggalSurat.getSelectedItem() + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_NO_SURAT) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_NO_REG) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_KD_DOKTER) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_NM_DOKTER) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_KD_POLI) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_NM_POLI) + "','"
                        + getNilaiTabel(tbObat.getSelectedRow(), COL_STATUS) + "','','','','','','','','','','','','','','','','','',''", "Surat Kontrol");

                File pdfDir = new File("tmpPDF");
                if (!pdfDir.exists()) {
                    pdfDir.mkdirs();
                }
                JasperPrint jasperPrint = JasperFillManager.fillReport("report/rptSuratSKDPBPJS2.jasper", param, koneksi);
                JasperExportManager.exportReportToPdfFile(jasperPrint, "tmpPDF/" + fileName + ".pdf");
                return true;
            } catch (Exception e) {
                System.out.println("Notif Create PDF SKDP Antar Poli : " + e);
                JOptionPane.showMessageDialog(null, "Gagal membuat file PDF SKDP : " + e.getMessage(), "Upload SKDP Antar Poli", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private void ConvertPDFtoJPG(String fileName) {
        try {
            File pdfFile = new File("tmpPDF/" + fileName + ".pdf");
            if (!pdfFile.exists()) {
                System.err.println("File PDF tidak ditemukan: " + pdfFile.getAbsolutePath());
                return;
            }

            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            File jpgDir = new File("tmpJPG");
            if (!jpgDir.exists() && !jpgDir.mkdir()) {
                System.err.println("Gagal membuat folder tmpJPG.");
                document.close();
                return;
            }

            for (int page = 0; page < totalPages; page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300);
                String jpgName = totalPages == 1 ? fileName + ".jpg" : fileName + "_page_" + (page + 1) + ".jpg";
                File jpgFile = new File(jpgDir, jpgName);
                ImageIO.write(image, "jpg", jpgFile);
            }
            document.close();
        } catch (IOException e) {
            System.out.println("Notif Convert PDF SKDP Antar Poli : " + e);
        }
    }

    private void UploadJPG(String fileName, String docpath, String noRawatUpload) {
        try {
            File jpgDir = new File("tmpJPG");
            if (!jpgDir.exists()) {
                System.err.println("Folder tmpJPG tidak ditemukan.");
                return;
            }

            File[] jpgFiles = jpgDir.listFiles((dir, name) ->
                    (name.equals(fileName + ".jpg") || (name.startsWith(fileName + "_page_") && name.endsWith(".jpg")))
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
                    String kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%SKDP%'");
                    if (kodeberkas.equals("")) {
                        kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%Surat Kontrol%'");
                    }
                    String filePath = "pages/upload/" + jpgFile.getName();
                    boolean uploadSuccess;
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
                        
                        MetadataBerkas.simpan(
                        koneksi,
                        noRawatUpload,
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

    private void HapusJPG() {
        File file = new File("tmpJPG");
        String[] myFiles;
        if (file.isDirectory()) {
            myFiles = file.list();
            if (myFiles != null) {
                for (int i = 0; i < myFiles.length; i++) {
                    File myFile = new File(file, myFiles[i]);
                    myFile.delete();
                }
            }
        }
    }


    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SuratSKDPAntarPoli dialog = new SuratSKDPAntarPoli(new javax.swing.JFrame(), true);
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
    private widget.TextBox Alasan1;
    private widget.TextBox Alasan2;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCari1;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPoli;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.Button BtnUpload;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Tanggal DTPCari3;
    private widget.Tanggal DTPCari4;
    private widget.TextBox Diagnosa;
    private widget.PanelBiasa FormInput;
    private widget.TextBox KdDokter;
    private widget.TextBox KdPoli;
    private widget.Label LCount;
    private javax.swing.JMenuItem MnSurat;
    private widget.TextBox NmDokter;
    private widget.TextBox NmPoli;
    private widget.TextBox NoRawat;
    private widget.TextBox NoReg;
    private widget.TextBox NoSurat;
    private javax.swing.JPanel PanelInput;
    private widget.RadioButton R1;
    private widget.RadioButton R2;
    private widget.RadioButton R3;
    private widget.TextBox Rtl1;
    private widget.TextBox Rtl2;
    private widget.ScrollPane Scroll;
    private widget.ComboBox Status;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TPasien;
    private widget.Tanggal TanggalPeriksa;
    private widget.Tanggal TanggalSurat;
    private widget.TextBox Terapi;
    private widget.Button btnDiagnosa;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel22;
    private widget.Label jLabel25;
    private widget.Label jLabel37;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelCari;
    private widget.panelisi panelGlass10;
    private widget.panelisi panelGlass8;
    private javax.swing.JMenuItem ppBerkasDigital;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    private void tampil() {     
        if(R1.isSelected()==true){
            status=" skdp_bpjs.status='Menunggu' ";
        }else if(R2.isSelected()==true){
            status=" skdp_bpjs.tanggal_rujukan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' ";
        }else if(R3.isSelected()==true){
            status=" skdp_bpjs.status<>'Menunggu' and skdp_bpjs.tanggal_rujukan between '"+Valid.SetTgl(DTPCari3.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari4.getSelectedItem()+"")+"' ";           
        }
        Valid.tabelKosong(tabMode);
        try {
            ps=koneksi.prepareStatement(
                    "select skdp_bpjs.tahun,skdp_bpjs.no_rkm_medis,pasien.nm_pasien,"+
                    "skdp_bpjs.diagnosa,skdp_bpjs.terapi,skdp_bpjs.alasan1,skdp_bpjs.alasan2,"+
                    "skdp_bpjs.rtl1,skdp_bpjs.rtl2,skdp_bpjs.tanggal_datang,skdp_bpjs.tanggal_rujukan,"+
                    "skdp_bpjs.no_antrian,skdp_bpjs.kd_dokter,dokter.nm_dokter,skdp_bpjs.status "+
                    "from skdp_bpjs inner join pasien inner join dokter on "+
                    "skdp_bpjs.no_rkm_medis=pasien.no_rkm_medis and skdp_bpjs.kd_dokter=dokter.kd_dokter "+
                    "where "+status+" and skdp_bpjs.no_rkm_medis like ? or "+
                    status+" and pasien.nm_pasien like ? or "+
                    status+" and skdp_bpjs.diagnosa like ? or "+
                    status+" and skdp_bpjs.terapi like ? or "+
                    status+" and skdp_bpjs.no_antrian like ? or "+
                    status+" and dokter.nm_dokter like ? order by skdp_bpjs.tanggal_rujukan,skdp_bpjs.no_antrian");
            try {
                ps.setString(1,"%"+TCari.getText().trim()+"%");
                ps.setString(2,"%"+TCari.getText().trim()+"%");
                ps.setString(3,"%"+TCari.getText().trim()+"%");
                ps.setString(4,"%"+TCari.getText().trim()+"%");
                ps.setString(5,"%"+TCari.getText().trim()+"%");
                ps.setString(6,"%"+TCari.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    kdpoli="";nmpoli="";noantri="";
                    ps2=koneksi.prepareStatement(
                            "select booking_registrasi.kd_poli,poliklinik.nm_poli,booking_registrasi.no_reg "+
                            "from booking_registrasi inner join poliklinik on booking_registrasi.kd_poli=poliklinik.kd_poli "+
                            "where booking_registrasi.kd_dokter=? and booking_registrasi.tanggal_periksa=? and booking_registrasi.no_rkm_medis=?");
                    try {
                        ps2.setString(1,rs.getString("kd_dokter"));
                        ps2.setString(2,rs.getString("tanggal_datang").substring(0,10));
                        ps2.setString(3,rs.getString("no_rkm_medis"));
                        rs2=ps2.executeQuery();
                        if(rs2.next()){
                            kdpoli=rs2.getString("kd_poli");
                            nmpoli=rs2.getString("nm_poli");
                            noantri=rs2.getString("no_reg");
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
                    tabMode.addRow(new Object[]{
                        cariNoRawatKontrol(rs.getString("no_rkm_medis"), rs.getString("tanggal_datang"), kdpoli),
                        rs.getInt("tahun"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("diagnosa"),rs.getString("terapi"),rs.getString("alasan1"),
                        rs.getString("alasan2"),rs.getString("rtl1"),rs.getString("rtl2"),
                        rs.getString("tanggal_datang"),rs.getString("tanggal_rujukan"),rs.getString("no_antrian"),
                        noantri,rs.getString("kd_dokter"),rs.getString("nm_dokter"),kdpoli,
                        nmpoli,rs.getString("status")
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
            System.out.println("Notif : "+e);
        } 
        LCount.setText(""+tabMode.getRowCount());
    }


    public void emptTeks() {
        KdDokter.setText("");
        NmDokter.setText("");
        KdPoli.setText("");
        NmPoli.setText("");
        Alasan1.setText("");
        Alasan2.setText("");
        Rtl1.setText("");
        Rtl2.setText("");
        Terapi.setText("");
        Diagnosa.setText("");
        TanggalSurat.setDate(new Date());
        TanggalSurat.requestFocus();
        isNomer();
    }
    
    private void isNomer(){
        switch (URUTNOREG) {
            case "poli":
                Valid.autoNomer3("select ifnull(MAX(CONVERT(booking_registrasi.no_reg,signed)),0) from booking_registrasi where booking_registrasi.kd_poli='"+KdPoli.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"'","",3,NoReg);
                break;
            case "dokter":
                Valid.autoNomer3("select ifnull(MAX(CONVERT(booking_registrasi.no_reg,signed)),0) from booking_registrasi where booking_registrasi.kd_dokter='"+KdDokter.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"'","",3,NoReg);
                break;
            case "dokter + poli":             
                Valid.autoNomer3("select ifnull(MAX(CONVERT(booking_registrasi.no_reg,signed)),0) from booking_registrasi where booking_registrasi.kd_dokter='"+KdDokter.getText()+"' and booking_registrasi.kd_poli='"+KdPoli.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"'","",3,NoReg);
                break;
            default:
                Valid.autoNomer3("select ifnull(MAX(CONVERT(booking_registrasi.no_reg,signed)),0) from booking_registrasi where booking_registrasi.kd_dokter='"+KdDokter.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"'","",3,NoReg);
                break;
        }
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(skdp_bpjs.no_antrian,6),signed)),0) from skdp_bpjs where skdp_bpjs.tahun='"+TanggalPeriksa.getSelectedItem().toString().substring(6,10)+"' ","",6,NoSurat);  
    }

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            NoRawat.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_NO_RAWAT));
            TNoRM.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_NO_RM)); 
            TPasien.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_NAMA_PASIEN));
            Diagnosa.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_DIAGNOSA));
            Terapi.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_TERAPI));
            Alasan1.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_ALASAN1));
            Alasan2.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_ALASAN2));
            Rtl1.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_RTL1));
            Rtl2.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_RTL2));
            Valid.SetTgl2(TanggalPeriksa,getNilaiTabel(tbObat.getSelectedRow(),COL_TANGGAL_DATANG));
            Valid.SetTgl2(TanggalSurat,getNilaiTabel(tbObat.getSelectedRow(),COL_TANGGAL_RUJUKAN));
            NoSurat.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_NO_SURAT));
            NoReg.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_NO_REG));
            KdDokter.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_KD_DOKTER));
            NmDokter.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_NM_DOKTER));
            KdPoli.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_KD_POLI));
            NmPoli.setText(getNilaiTabel(tbObat.getSelectedRow(),COL_NM_POLI));
            Status.setSelectedItem(getNilaiTabel(tbObat.getSelectedRow(),COL_STATUS));
        }
    }
    
    public void setNoRm(String norm,String nama) {
        TNoRM.setText(norm);
        TPasien.setText(nama);
        TCari.setText(norm);
        ChkInput.setSelected(true);
        isForm();
        tampil();
    }
    
    public void setNoRm(String norm,String nama,String kodepoli,String namapoli,String kodedokter,String namadokter) {
        TNoRM.setText(norm);
        TPasien.setText(nama);
        KdPoli.setText(kodepoli);
        NmPoli.setText(namapoli);
        KdDokter.setText(kodedokter);
        NmDokter.setText(namadokter);
        TCari.setText(norm);
        ChkInput.setSelected(true);
        isForm();
        tampil();
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,216));
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
        BtnSimpan.setEnabled(akses.getskdp_bpjs());
        BtnHapus.setEnabled(akses.getskdp_bpjs());
        BtnPrint.setEnabled(akses.getskdp_bpjs());
        BtnEdit.setEnabled(akses.getskdp_bpjs());
    }

    public JTable getTable(){
        return tbObat;
    }

    private void isBooking(){
        if(Sequel.menyimpantf("skdp_bpjs","?,?,?,?,?,?,?,?,?,?,?,?,?","Tahun dan nomor surat",13,new String[]{
             TanggalPeriksa.getSelectedItem().toString().substring(6,10),TNoRM.getText(),Diagnosa.getText(),Terapi.getText(),
             Alasan1.getText(),Alasan2.getText(),Rtl1.getText(),Rtl2.getText(),Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+" "+TanggalPeriksa.getSelectedItem().toString().substring(11,19),
             Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),NoSurat.getText(),KdDokter.getText(),Status.getSelectedItem().toString()
         })==true){
             Sequel.menyimpan2("booking_registrasi","?,?,?,?,?,?,?,?,?,?,?","Pasien dan Tanggal",11,new String[]{
                Valid.SetTgl(TanggalSurat.getSelectedItem()+""),TanggalSurat.getSelectedItem().toString().substring(11,19),TNoRM.getText(),
                Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),KdDokter.getText(),
                KdPoli.getText(),NoReg.getText(),Sequel.cariIsi("select pasien.kd_pj from pasien where pasien.no_rkm_medis=?",TNoRM.getText()),"0",
                Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+" "+TanggalPeriksa.getSelectedItem().toString().substring(11,19),
                "belum"
             });
             tabMode.addRow(new String[]{
                cariNoRawatKontrol(TNoRM.getText(), Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""), KdPoli.getText()),
                TanggalPeriksa.getSelectedItem().toString().substring(6,10),TNoRM.getText(),TPasien.getText(),Diagnosa.getText(),Terapi.getText(),Alasan1.getText(),Alasan2.getText(),
                Rtl1.getText(),Rtl2.getText(),Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+" "+TanggalPeriksa.getSelectedItem().toString().substring(11,19),
                Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),NoSurat.getText(),NoReg.getText(),
                KdDokter.getText(),NmDokter.getText(),KdPoli.getText(),NmPoli.getText(),Status.getSelectedItem().toString()
             });
             emptTeks();
             LCount.setText(""+tabMode.getRowCount());
         } 
    }
}
