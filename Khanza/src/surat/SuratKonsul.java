package surat;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariDokter2;
import kepegawaian.DlgCariDokter3;
import kepegawaian.DlgCariPegawai;
import keuangan.DlgKamar;
import laporan.DlgCariPenyakit;
import simrskhanza.DlgCariPoli;
import simrskhanza.DlgCariPoli2;
import simrskhanza.DlgCariPoli3;

/**
 *
 * @author dosen
 */
public class SuratKonsul extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();    
    private PreparedStatement ps,ps2,ps3;
    private ResultSet rs,rs2;
    private int i=0;
    private DlgCariDokter dokter=new DlgCariDokter(null,true);
    private DlgCariDokter3 dokter3=new DlgCariDokter3(null,true);
    private DlgCariPoli poli=new DlgCariPoli(null,true);
    private DlgCariPoli3 poli3=new DlgCariPoli3(null,true);
    private String status="",kamarasal="",namakamar="";
    public  DlgKamar kamar=new DlgKamar(null,true);
    public  DlgCariPegawai pegawai=new DlgCariPegawai(null,false);
    private String konsultasiparamput="";
    

    /** Creates new form DlgPemberianInfus
     * @param parent
     * @param modal */
    public SuratKonsul(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();                                
        
        tabMode=new DefaultTableModel(null,new Object[]{
        "No Rawat","No RM","Nama Pasien","No Surat","Jk","Tanggal Surat","Kode Dokter Perujuk","Dokter Perujuk",
        "Kode Poli Perujuk","Poli Perujuk","Tanggal Lahir","Kode Dokter Tujuan","Nama Dokter Tujuan",
        "Kode Poli Tujuan","Nama Poli Tujuan","Umur","Kategori Konsul","Status","Ikhtiar Klinik dan Lab Singkat","Tanggal Jawaban","Hal-hal Temuan","Konsultasi yang diminta","Anjuran","Diagnosis","Lain-lain"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 21; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(100);
            }else if(i==2){
                column.setPreferredWidth(100);
            }else if(i==3){
                column.setPreferredWidth(100);
            }else if(i==4){
                column.setPreferredWidth(100);
            }else if(i==5){
                column.setPreferredWidth(100);
            }else if(i==6){
                column.setPreferredWidth(100);
            }else if(i==7){
                column.setPreferredWidth(200);
            }else if(i==8){
                column.setPreferredWidth(200);
            }else if(i==9){
                column.setPreferredWidth(200);
            }else if(i==10){
                column.setPreferredWidth(200);
            }else if(i==11){
                column.setPreferredWidth(200);
            }else if(i==12){
                column.setPreferredWidth(200);
            }else if(i==13){
                column.setPreferredWidth(200);
            }else if(i==14){
                column.setPreferredWidth(200);
            }else if(i==15){
                column.setPreferredWidth(200);
            }else if(i==16){
                column.setPreferredWidth(200);
            }else if(i==17){
                column.setPreferredWidth(200);
            }else if(i==18){
                column.setPreferredWidth(200);
            }else if(i==19){
                column.setPreferredWidth(200);
            }else if(i==20){
                column.setPreferredWidth(200);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
                
        TNoRM.setDocument(new batasInput((byte)15).getKata(TNoRM));
        NoSurat.setDocument(new batasInput((byte)20).getKata(NoSurat));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        KdDokterTujuan.setDocument(new batasInput((byte)20).getKata(KdDokterTujuan));
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
        
                
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {;}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){                    
                    KdDokterPerujuk.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NmDokterPerujuk.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());

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
        
        dokter3.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {;}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter3.getTable().getSelectedRow()!= -1){                    
                    KdDokterTujuan.setText(dokter3.getTable().getValueAt(dokter3.getTable().getSelectedRow(),0).toString());
                    NmDokterTujuan.setText(dokter3.getTable().getValueAt(dokter3.getTable().getSelectedRow(),1).toString());
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
                    KdPoliAsal.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),0).toString());
                    NmPoliAsal.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),1).toString());
                    
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
        
        kamar.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
   //             if(akses.getform().equals("SuratKonsul")){
                    if(kamar.getTable().getSelectedRow()!= -1){   
                        KdPoliAsal.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),1).toString());
                        NmPoliAsal.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),3).toString());
                }
  //          }
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
                if(akses.getform().equals("DlgKamarInap")){
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        kamar.dispose();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });        
        
        poli3.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli3.getTable().getSelectedRow()!= -1){                    
                    KdPoliTujuan.setText(poli3.getTable().getValueAt(poli3.getTable().getSelectedRow(),0).toString());
                    NmPoliTujuan.setText(poli3.getTable().getValueAt(poli3.getTable().getSelectedRow(),1).toString());
                    
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
        MnJawabanKonsul = new javax.swing.JMenuItem();
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
        BtnKeluar = new widget.Button();
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
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRM = new widget.TextBox();
        jLabel9 = new widget.Label();
        NmDokterTujuan = new widget.TextBox();
        TPasien = new widget.TextBox();
        TanggalSurat = new widget.Tanggal();
        Status = new widget.ComboBox();
        jLabel10 = new widget.Label();
        KdDokterTujuan = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel37 = new widget.Label();
        jLabel11 = new widget.Label();
        KdPoliTujuan = new widget.TextBox();
        NmPoliTujuan = new widget.TextBox();
        BtnPoli = new widget.Button();
        jLabel12 = new widget.Label();
        jLabel15 = new widget.Label();
        NoSurat = new widget.TextBox();
        TglLahir = new widget.TextBox();
        jLabel19 = new widget.Label();
        jLabel20 = new widget.Label();
        Umur = new widget.TextBox();
        jLabel21 = new widget.Label();
        JK = new widget.TextBox();
        jLabel23 = new widget.Label();
        KdDokterPerujuk = new widget.TextBox();
        NmDokterPerujuk = new widget.TextBox();
        BtnDokter1 = new widget.Button();
        scrollPane8 = new widget.ScrollPane();
        permintaankonsul = new widget.TextArea();
        scrollPane9 = new widget.ScrollPane();
        klinikdanlab = new widget.TextArea();
        jLabel14 = new widget.Label();
        kategori_konsul = new widget.ComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel27 = new widget.Label();
        KdPoliAsal = new widget.TextBox();
        NmPoliAsal = new widget.TextBox();
        BtnPoli1 = new widget.Button();
        TanggalJawaban = new widget.Tanggal();
        jLabel28 = new widget.Label();
        jLabel29 = new widget.Label();
        scrollPane10 = new widget.ScrollPane();
        halhaltemuan = new widget.TextArea();
        TNoRw = new widget.TextBox();
        diagnosis = new widget.TextBox();
        jLabel17 = new widget.Label();
        jLabel18 = new widget.Label();
        scrollPane11 = new widget.ScrollPane();
        anjuran = new widget.TextArea();
        scrollPane12 = new widget.ScrollPane();
        lainlain = new widget.TextArea();
        jSeparator12 = new javax.swing.JSeparator();
        jLabel32 = new widget.Label();
        jLabel30 = new widget.Label();
        jLabel31 = new widget.Label();
        jLabel33 = new widget.Label();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnJawabanKonsul.setBackground(new java.awt.Color(250, 250, 250));
        MnJawabanKonsul.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnJawabanKonsul.setForeground(new java.awt.Color(50, 50, 50));
        MnJawabanKonsul.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cetakrm1.png"))); // NOI18N
        MnJawabanKonsul.setText("Cetak Permintaan Konsul DPJP");
        MnJawabanKonsul.setName("MnJawabanKonsul"); // NOI18N
        MnJawabanKonsul.setPreferredSize(new java.awt.Dimension(200, 26));
        MnJawabanKonsul.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnJawabanKonsulActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnJawabanKonsul);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Lembar Konsultasi Antar DPJP");
        setBackground(new java.awt.Color(255, 255, 255));
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(1400, 750));
        setResizable(false);
        setSize(new java.awt.Dimension(1400, 750));
        setType(java.awt.Window.Type.POPUP);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setMinimumSize(new java.awt.Dimension(1400, 750));
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1400, 750));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setAutoCreateRowSorter(true);
        tbObat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.setPreferredScrollableViewportSize(new java.awt.Dimension(450, 200));
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
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setMinimumSize(new java.awt.Dimension(603, 50));
        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(55, 50));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        // BtnSimpan.setGlassColor(new java.awt.Color(255, 255, 255));
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
        // BtnBatal.setGlassColor(new java.awt.Color(255, 255, 255));
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
        // BtnHapus.setGlassColor(new java.awt.Color(255, 255, 255));
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
        BtnEdit.setText("Simpan Jawaban");
        BtnEdit.setToolTipText("Alt+G");
        // BtnEdit.setGlassColor(new java.awt.Color(255, 255, 255));
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(150, 30));
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
        // BtnPrint.setGlassColor(new java.awt.Color(255, 255, 255));
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
        // BtnAll.setGlassColor(new java.awt.Color(255, 255, 255));
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
        // BtnKeluar.setGlassColor(new java.awt.Color(255, 255, 255));
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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.SOUTH);
        panelGlass8.getAccessibleContext().setAccessibleName("");

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

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-05-2024" }));
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

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-05-2024" }));
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

        DTPCari3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-05-2024" }));
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

        DTPCari4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-05-2024" }));
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

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelCari.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(450, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelCari.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        // BtnCari.setGlassColor(new java.awt.Color(255, 255, 255));
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
        panelCari.add(BtnCari);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        panelCari.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelCari.add(LCount);

        jPanel3.add(panelCari, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setMinimumSize(new java.awt.Dimension(1360, 507));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(1360, 507));
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

        FormInput.setMaximumSize(new java.awt.Dimension(1400, 550));
        FormInput.setMinimumSize(new java.awt.Dimension(1400, 500));
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(1400, 500));
        FormInput.setLayout(null);

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("Pasien");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(20, 40, 40, 23);

        TNoRM.setEditable(false);
        TNoRM.setForeground(new java.awt.Color(0, 0, 0));
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(190, 40, 87, 23);

        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("Kepada Yth dr.");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(20, 100, 110, 23);

        NmDokterTujuan.setEditable(false);
        NmDokterTujuan.setForeground(new java.awt.Color(0, 0, 0));
        NmDokterTujuan.setHighlighter(null);
        NmDokterTujuan.setName("NmDokterTujuan"); // NOI18N
        FormInput.add(NmDokterTujuan);
        NmDokterTujuan.setBounds(210, 100, 260, 23);

        TPasien.setEditable(false);
        TPasien.setForeground(new java.awt.Color(0, 0, 0));
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(280, 40, 190, 23);

        TanggalSurat.setForeground(new java.awt.Color(0, 0, 0));
        TanggalSurat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-05-2024 09:39:45" }));
        TanggalSurat.setDisplayFormat("dd-MM-yyyy hh:mm:ss");
        TanggalSurat.setName("TanggalSurat"); // NOI18N
        TanggalSurat.setOpaque(false);
        TanggalSurat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalSuratKeyPressed(evt);
            }
        });
        FormInput.add(TanggalSurat);
        TanggalSurat.setBounds(790, 130, 132, 23);

        Status.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        Status.setForeground(new java.awt.Color(0, 0, 0));
        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Menunggu", "Selesai" }));
        Status.setName("Status"); // NOI18N
        Status.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusKeyPressed(evt);
            }
        });
        FormInput.add(Status);
        Status.setBounds(1040, 130, 130, 23);

        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel10.setText("Tanggal Permintaan");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(680, 130, 110, 23);

        KdDokterTujuan.setEditable(false);
        KdDokterTujuan.setForeground(new java.awt.Color(0, 0, 0));
        KdDokterTujuan.setHighlighter(null);
        KdDokterTujuan.setName("KdDokterTujuan"); // NOI18N
        FormInput.add(KdDokterTujuan);
        KdDokterTujuan.setBounds(108, 100, 100, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('X');
        BtnDokter.setToolTipText("Alt+X");
        // BtnDokter.setGlassColor(new java.awt.Color(255, 255, 255));
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
        BtnDokter.setBounds(470, 100, 30, 23);

        jLabel37.setForeground(new java.awt.Color(0, 0, 0));
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel37.setText("Status ");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(960, 130, 70, 23);

        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("Spesialis");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(510, 100, 50, 23);

        KdPoliTujuan.setEditable(false);
        KdPoliTujuan.setForeground(new java.awt.Color(0, 0, 0));
        KdPoliTujuan.setHighlighter(null);
        KdPoliTujuan.setName("KdPoliTujuan"); // NOI18N
        FormInput.add(KdPoliTujuan);
        KdPoliTujuan.setBounds(560, 100, 100, 23);

        NmPoliTujuan.setEditable(false);
        NmPoliTujuan.setForeground(new java.awt.Color(0, 0, 0));
        NmPoliTujuan.setHighlighter(null);
        NmPoliTujuan.setName("NmPoliTujuan"); // NOI18N
        NmPoliTujuan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NmPoliTujuanActionPerformed(evt);
            }
        });
        FormInput.add(NmPoliTujuan);
        NmPoliTujuan.setBounds(663, 100, 260, 23);

        BtnPoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli.setMnemonic('X');
        BtnPoli.setToolTipText("Alt+X");
        // BtnPoli.setGlassColor(new java.awt.Color(255, 255, 255));
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
        BtnPoli.setBounds(930, 100, 20, 23);

        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Konsultasi yang diminta :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(690, 190, 650, 23);

        jLabel15.setForeground(new java.awt.Color(0, 0, 0));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel15.setText("No. Surat");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(510, 40, 60, 23);

        NoSurat.setForeground(new java.awt.Color(0, 0, 0));
        NoSurat.setHighlighter(null);
        NoSurat.setName("NoSurat"); // NOI18N
        NoSurat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoSuratKeyPressed(evt);
            }
        });
        FormInput.add(NoSurat);
        NoSurat.setBounds(560, 40, 363, 23);

        TglLahir.setForeground(new java.awt.Color(0, 0, 0));
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        TglLahir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglLahirKeyPressed(evt);
            }
        });
        FormInput.add(TglLahir);
        TglLahir.setBounds(1040, 40, 130, 23);

        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel19.setText("Tanggal Lahir");
        jLabel19.setName("jLabel19"); // NOI18N
        FormInput.add(jLabel19);
        jLabel19.setBounds(960, 40, 80, 23);

        jLabel20.setForeground(new java.awt.Color(0, 0, 0));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setText("Umur");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(960, 70, 80, 23);

        Umur.setForeground(new java.awt.Color(0, 0, 0));
        Umur.setHighlighter(null);
        Umur.setName("Umur"); // NOI18N
        Umur.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                UmurKeyPressed(evt);
            }
        });
        FormInput.add(Umur);
        Umur.setBounds(1040, 70, 130, 23);

        jLabel21.setForeground(new java.awt.Color(0, 0, 0));
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel21.setText("Kelamin");
        jLabel21.setName("jLabel21"); // NOI18N
        FormInput.add(jLabel21);
        jLabel21.setBounds(960, 100, 80, 23);

        JK.setForeground(new java.awt.Color(0, 0, 0));
        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N
        JK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JKKeyPressed(evt);
            }
        });
        FormInput.add(JK);
        JK.setBounds(1040, 100, 130, 23);

        jLabel23.setForeground(new java.awt.Color(0, 0, 0));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("Dokter Perujuk");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(20, 70, 90, 23);

        KdDokterPerujuk.setEditable(false);
        KdDokterPerujuk.setForeground(new java.awt.Color(0, 0, 0));
        KdDokterPerujuk.setHighlighter(null);
        KdDokterPerujuk.setName("KdDokterPerujuk"); // NOI18N
        FormInput.add(KdDokterPerujuk);
        KdDokterPerujuk.setBounds(108, 70, 100, 23);

        NmDokterPerujuk.setEditable(false);
        NmDokterPerujuk.setForeground(new java.awt.Color(0, 0, 0));
        NmDokterPerujuk.setHighlighter(null);
        NmDokterPerujuk.setName("NmDokterPerujuk"); // NOI18N
        FormInput.add(NmDokterPerujuk);
        NmDokterPerujuk.setBounds(210, 70, 260, 23);

        BtnDokter1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter1.setMnemonic('X');
        BtnDokter1.setToolTipText("Alt+X");
        // BtnDokter1.setGlassColor(new java.awt.Color(255, 255, 255));
        BtnDokter1.setName("BtnDokter1"); // NOI18N
        BtnDokter1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokter1ActionPerformed(evt);
            }
        });
        BtnDokter1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnDokter1KeyPressed(evt);
            }
        });
        FormInput.add(BtnDokter1);
        BtnDokter1.setBounds(470, 70, 30, 23);

        scrollPane8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane8.setName("scrollPane8"); // NOI18N

        permintaankonsul.setColumns(20);
        permintaankonsul.setForeground(new java.awt.Color(0, 0, 0));
        permintaankonsul.setRows(5);
        permintaankonsul.setName("permintaankonsul"); // NOI18N
        scrollPane8.setViewportView(permintaankonsul);

        FormInput.add(scrollPane8);
        scrollPane8.setBounds(692, 220, 650, 90);

        scrollPane9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane9.setName("scrollPane9"); // NOI18N

        klinikdanlab.setColumns(20);
        klinikdanlab.setForeground(new java.awt.Color(0, 0, 0));
        klinikdanlab.setRows(5);
        klinikdanlab.setName("klinikdanlab"); // NOI18N
        scrollPane9.setViewportView(klinikdanlab);

        FormInput.add(scrollPane9);
        scrollPane9.setBounds(20, 220, 660, 90);

        jLabel14.setForeground(new java.awt.Color(0, 0, 0));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Kategori Konsul");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(20, 130, 90, 23);

        kategori_konsul.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        kategori_konsul.setForeground(new java.awt.Color(0, 0, 0));
        kategori_konsul.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Cito", "Biasa", "Bed Consult" }));
        kategori_konsul.setName("kategori_konsul"); // NOI18N
        kategori_konsul.setPreferredSize(new java.awt.Dimension(50, 23));
        FormInput.add(kategori_konsul);
        kategori_konsul.setBounds(110, 130, 100, 23);

        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 260, 930, 0);

        jSeparator2.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 240, 930, 0);

        jLabel27.setForeground(new java.awt.Color(0, 0, 0));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("Kamar");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(510, 70, 50, 23);

        KdPoliAsal.setEditable(false);
        KdPoliAsal.setForeground(new java.awt.Color(0, 0, 0));
        KdPoliAsal.setHighlighter(null);
        KdPoliAsal.setName("KdPoliAsal"); // NOI18N
        FormInput.add(KdPoliAsal);
        KdPoliAsal.setBounds(560, 70, 100, 23);

        NmPoliAsal.setEditable(false);
        NmPoliAsal.setForeground(new java.awt.Color(0, 0, 0));
        NmPoliAsal.setHighlighter(null);
        NmPoliAsal.setName("NmPoliAsal"); // NOI18N
        NmPoliAsal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NmPoliAsalActionPerformed(evt);
            }
        });
        FormInput.add(NmPoliAsal);
        NmPoliAsal.setBounds(663, 70, 260, 23);

        BtnPoli1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli1.setMnemonic('X');
        BtnPoli1.setToolTipText("Alt+X");
        // BtnPoli1.setGlassColor(new java.awt.Color(255, 255, 255));
        BtnPoli1.setName("BtnPoli1"); // NOI18N
        BtnPoli1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPoli1ActionPerformed(evt);
            }
        });
        BtnPoli1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPoli1KeyPressed(evt);
            }
        });
        FormInput.add(BtnPoli1);
        BtnPoli1.setBounds(930, 70, 20, 23);

        TanggalJawaban.setForeground(new java.awt.Color(0, 0, 0));
        TanggalJawaban.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15-05-2024 09:39:45" }));
        TanggalJawaban.setDisplayFormat("dd-MM-yyyy hh:mm:ss");
        TanggalJawaban.setName("TanggalJawaban"); // NOI18N
        TanggalJawaban.setOpaque(false);
        TanggalJawaban.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalJawabanKeyPressed(evt);
            }
        });
        FormInput.add(TanggalJawaban);
        TanggalJawaban.setBounds(1210, 350, 132, 23);

        jLabel28.setForeground(new java.awt.Color(0, 0, 0));
        jLabel28.setText("Tanggal Jawaban");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(1100, 350, 100, 23);

        jLabel29.setForeground(new java.awt.Color(0, 0, 0));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel29.setText(" Berdasarkan pemeriksaan, kami menemukan hal-hal berikut :");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(20, 370, 450, 30);

        scrollPane10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane10.setName("scrollPane10"); // NOI18N

        halhaltemuan.setColumns(20);
        halhaltemuan.setForeground(new java.awt.Color(0, 0, 0));
        halhaltemuan.setRows(5);
        halhaltemuan.setName("halhaltemuan"); // NOI18N
        scrollPane10.setViewportView(halhaltemuan);

        FormInput.add(scrollPane10);
        scrollPane10.setBounds(20, 400, 480, 90);

        TNoRw.setEditable(false);
        TNoRw.setForeground(new java.awt.Color(0, 0, 0));
        TNoRw.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(68, 40, 120, 23);

        diagnosis.setForeground(new java.awt.Color(0, 0, 0));
        diagnosis.setHighlighter(null);
        diagnosis.setName("diagnosis"); // NOI18N
        diagnosis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diagnosisKeyPressed(evt);
            }
        });
        FormInput.add(diagnosis);
        diagnosis.setBounds(108, 160, 815, 23);

        jLabel17.setForeground(new java.awt.Color(0, 0, 0));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText("Diagnosis");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(20, 160, 80, 23);

        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel18.setText("Ikhtiar Klinik + Laboratorium singkat :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(20, 190, 190, 23);

        scrollPane11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane11.setName("scrollPane11"); // NOI18N

        anjuran.setColumns(20);
        anjuran.setForeground(new java.awt.Color(0, 0, 0));
        anjuran.setRows(5);
        anjuran.setName("anjuran"); // NOI18N
        scrollPane11.setViewportView(anjuran);

        FormInput.add(scrollPane11);
        scrollPane11.setBounds(510, 400, 412, 90);

        scrollPane12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        scrollPane12.setName("scrollPane12"); // NOI18N

        lainlain.setColumns(20);
        lainlain.setForeground(new java.awt.Color(0, 0, 0));
        lainlain.setRows(5);
        lainlain.setName("lainlain"); // NOI18N
        scrollPane12.setViewportView(lainlain);

        FormInput.add(scrollPane12);
        scrollPane12.setBounds(930, 400, 412, 90);

        jSeparator12.setBackground(new java.awt.Color(204, 204, 204));
        jSeparator12.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jSeparator12.setName("jSeparator12"); // NOI18N
        jSeparator12.setPreferredSize(new java.awt.Dimension(0, 1));
        FormInput.add(jSeparator12);
        jSeparator12.setBounds(20, 325, 1320, 1);

        jLabel32.setForeground(new java.awt.Color(0, 0, 0));
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel32.setText("Permintaan Konsultasi Antar DPJP");
        jLabel32.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(20, 0, 330, 40);

        jLabel30.setForeground(new java.awt.Color(0, 0, 0));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setText("Anjuran :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(510, 370, 420, 30);

        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel31.setText("Lain-lain :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(930, 370, 410, 30);

        jLabel33.setForeground(new java.awt.Color(0, 0, 0));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel33.setText("Jawab Konsultasi Antar DPJP");
        jLabel33.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(20, 330, 330, 40);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        Valid.pindah(evt,Status,KdDokterTujuan);
        
}//GEN-LAST:event_TNoRMKeyPressed

    private void TanggalSuratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalSuratKeyPressed
        Valid.pindah(evt,TCari,Status);
}//GEN-LAST:event_TanggalSuratKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||(TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals(""))){
            Valid.textKosong(TNoRM,"Pasien");
        }else if(NoSurat.getText().trim().equals("")){
            Valid.textKosong(NoSurat,"No Surat");
        }else if(KdDokterPerujuk.getText().trim().equals("")||NmDokterPerujuk.getText().trim().equals("")){
            Valid.textKosong(NmDokterPerujuk,"Nama Dokter Perujuk");
        }else if(KdPoliAsal.getText().trim().equals("")||NmPoliAsal.getText().trim().equals("")){
            Valid.textKosong(NmPoliAsal,"Poliklinik Asal");
        }else if(KdDokterTujuan.getText().trim().equals("")||NmDokterTujuan.getText().trim().equals("")){
            Valid.textKosong(NmDokterTujuan,"Nama Dokter Tujuan");
        }else if(KdPoliTujuan.getText().trim().equals("")||NmPoliTujuan.getText().trim().equals("")){
            Valid.textKosong(NmPoliTujuan,"Poliklinik Tujuan");
        }else if(klinikdanlab.getText().trim().equals("")){
            Valid.textKosong(klinikdanlab,"Isi Konsul");
        }else{          
            if(Sequel.menyimpantf("surat_konsul","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","Surat Konsul",21,new String[]{
                TNoRw.getText(),
                NoSurat.getText(),
                Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),
                KdDokterPerujuk.getText(),
                NmDokterPerujuk.getText(),
                KdPoliAsal.getText(),
                NmPoliAsal.getText(),
                KdDokterTujuan.getText(),
                NmDokterTujuan.getText(),
                KdPoliTujuan.getText(),
                NmPoliTujuan.getText(),
                Umur.getText(),
                permintaankonsul.getText(),
                "Menunggu",
                klinikdanlab.getText(),
                "0000-00-00 00:00:00",
                halhaltemuan.getText(),
                kategori_konsul.getSelectedItem().toString(),
                anjuran.getText(),
                diagnosis.getText(),
                lainlain.getText()
                })==true){
                Sequel.menyimpan2("dpjp_ranap","?,?",2,new String[]{TNoRw.getText(),KdDokterTujuan.getText()});                
                if(Sequel.cariInteger("select count(kamar_inap.no_rawat) from kamar_inap where kamar_inap.no_rawat=?",TNoRw.getText())>0){                    
                    Sequel.menyimpantf("rujukan_internal_ranap","?,?,?,?,?","Rujukan Sama",5,new String[]{
                    TNoRw.getText(),KdDokterTujuan.getText(),KdPoliTujuan.getText(),Valid.SetTgl(TanggalSurat.getSelectedItem()+""),
                    TanggalSurat.getSelectedItem().toString().substring(11,19)});
                } else {
                    Sequel.menyimpantf("rujukan_internal_poli","?,?,?","Rujukan Sama",3,new String[]{
                    TNoRw.getText(),KdDokterTujuan.getText(),KdPoliTujuan.getText()});
                }
                JOptionPane.showMessageDialog(null,"Berhasil Tersimpan");
                tampil();
                emptTeks();
                NoSurat.requestFocus();
                autoSK();
            }                      
        }  
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
//            Valid.pindah(evt,NoReg,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        autoSK();
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
        }else if(Status.getSelectedItem().equals("Selesai")){
           JOptionPane.showMessageDialog(null,"Surat konsul tidak bisa dihapus karena sudah dijawab");
        }else if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal menghapus. Pilih dulu data yang mau dihapus.\nKlik data pada table untuk memilih...!!!!");
        }else if(!(TPasien.getText().trim().equals(""))){
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.queryu2tf("delete from surat_konsul where no_surat=? ",1,new String[]{
                    tbObat.getValueAt(tbObat.getSelectedRow(),3).toString()
                })==true){
                if(Sequel.cariInteger("select count(kamar_inap.no_rawat) from kamar_inap where kamar_inap.no_rawat=?",TNoRw.getText())>0){
                   Sequel.queryu2tf("delete from rujukan_internal_ranap where no_rawat=?",1,new String[]{
                    TNoRw.getText()
                    });
                }else {
                    Sequel.queryu2tf("delete from rujukan_internal_poli where no_rawat=? ",1,new String[]{
                    TNoRw.getText()
                });
                }
                JOptionPane.showMessageDialog(null,"Berhasil Terhapus");
                tampil();
                emptTeks();
                NoSurat.requestFocus();
                autoSK();  
                    
                }
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
    isForm2();    
    getData();
    //IsiKonsul.setEnabled(akses.getadmin());
    //BtnDokter1.setEnabled(akses.getadmin());
    //BtnDokter.setEnabled(akses.getadmin());
    //BtnPoli1.setEnabled(akses.getadmin());
    //BtnPoli.setEnabled(akses.getadmin());
    //BtnHapus.setEnabled(akses.getadmin());
    //BtnEdit.setEnabled(akses.getadmin());
    //BtnSimpan.setEnabled(akses.getadmin());
    //BtnSimpan1.setEnabled(akses.getadmin());
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


//    if(tabMode.getRowCount()!=0){
//            try {
//                getData();
//                isForm2();  

//    TCari.setText(KdDokterTujuan.getText());
//            } catch (java.lang.NullPointerException e) {
//            }
//        }
}//GEN-LAST:event_tbObatMouseClicked

private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        akses.setform("DlgSuratKonsul");
        dokter3.emptTeks();
        dokter3.isCek();
        dokter3.TCari.requestFocus();
        dokter3.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter3.setLocationRelativeTo(internalFrame1);
        dokter3.setVisible(true);

}//GEN-LAST:event_BtnDokterActionPerformed

private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
    if(evt.getKeyCode()==KeyEvent.VK_SPACE){
        BtnDokterActionPerformed(null);
    }else{
//        Valid.pindah(evt,Rtl2,BtnPoli);
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
        if(TNoRw.getText().trim().equals("")||(TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals(""))){
            Valid.textKosong(TNoRM,"Pasien");
        }else if(halhaltemuan.getText().trim().equals("")){
            Valid.textKosong(halhaltemuan,"Jawaban Konsul");
        }else{    
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.mengedittf("surat_konsul","no_surat=?",
                        "no_rawat=?,"
                                + "no_surat=?,"
                                + "tanggalsurat=?,"
                                + "kd_dokter_perujuk=?,"
                                + "dokterperujuk=?,"
                                + "kd_poli_perujuk=?,"
                                + "poliperujuk=?,"
                                + "kd_dokter_tujuan=?,"
                                + "doktertujuan=?,"
                                + "kd_poli_tujuan=?,"
                                + "politujuan=?,"
                                + "umur=?,"
                                + "permintaankonsul=?,"
                                + "status=?,"
                                + "klinikdanlab=?,"
                                + "tanggaljawaban=?,"
                                + "halhaltemuan=?,"
                                + "kategori_konsul=?,"
                                + "anjuran=?,"
                                + "diagnosis=?,"                                
                                + "lainlain=?",22,new String[]{
                TNoRw.getText(),
                NoSurat.getText(),
                Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),
                KdDokterPerujuk.getText(),
                NmDokterPerujuk.getText(),
                KdPoliAsal.getText(),
                NmPoliAsal.getText(),
                KdDokterTujuan.getText(),
                NmDokterTujuan.getText(),
                KdPoliTujuan.getText(),
                NmPoliTujuan.getText(),
                Umur.getText(),
                permintaankonsul.getText(),
                "Selesai",
                klinikdanlab.getText(),
                Valid.SetTgl(TanggalJawaban.getSelectedItem()+"")+" "+TanggalJawaban.getSelectedItem().toString().substring(11,19),
                halhaltemuan.getText(),
                kategori_konsul.getSelectedItem().toString(),
                anjuran.getText(),
                diagnosis.getText(),
                lainlain.getText(),
                tbObat.getValueAt(tbObat.getSelectedRow(),3).toString()                                                              
                })==true){
                JOptionPane.showMessageDialog(null,"Jawaban Konsultasi Berhasil Tersimpan");
                tampil();
//                emptTeks();
                }
                
            }
        }        

//        if(TNoRw.getText().trim().equals("")||(TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals(""))){
//            Valid.textKosong(TNoRM,"Pasien");
//        }else if(IsiKonsul.getText().trim().equals("")){
//            Valid.textKosong(IsiKonsul,"Isi Konsul");
//        }else{    
//            if(tbObat.getSelectedRow()!= -1){
//                if(Sequel.mengedittf("surat_konsul","no_surat=?","no_rawat=?,no_surat=?,tanggalsurat=?,kd_dokter_perujuk=?,dokterperujuk=?,kd_poli_perujuk=?,poliperujuk=?,kd_dokter_tujuan=?,"+
//                "doktertujuan=?,kd_poli_tujuan=?,politujuan=?,umur=?,permintaankonsul=?,status=?,klinikdanlab=?,tanggaljawaban=?,"+
//                "halhaltemuan=?",18,new String[]{
//                TNoRw.getText(),NoSurat.getText(),Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),
//                KdDokterPerujuk.getText(),NmDokterPerujuk.getText(),KdPoliAsal.getText(),NmPoliAsal.getText(),KdDokterTujuan.getText(),NmDokterTujuan.getText(),KdPoliTujuan.getText(),NmPoliTujuan.getText(),
//                Umur.getText(),kategori_konsul.getSelectedItem().toString(),"Menunggu",IsiKonsul.getText(),"0000:00:00 00:00:00",
//                halhaltemuan.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),3).toString()
//                })==true){
//                JOptionPane.showMessageDialog(null,"Berhasil Tersimpan");
//                tampil();
//                emptTeks();
//                }
//                
//            }
//        } 

    }//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnKeluar);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void StatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusKeyPressed
       
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

    private void NoSuratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoSuratKeyPressed
//        Valid.pindah(evt,TanggalPeriksa,NoReg);
    }//GEN-LAST:event_NoSuratKeyPressed

    private void BtnPoliKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPoliKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPoliActionPerformed(null);
        }else{
//            Valid.pindah(evt,BtnDokter,TanggalPeriksa);
        }
    }//GEN-LAST:event_BtnPoliKeyPressed

    private void BtnPoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoliActionPerformed
                poli3.isCek();        
                poli3.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                poli3.setLocationRelativeTo(internalFrame1);
                poli3.setVisible(true);
    }//GEN-LAST:event_BtnPoliActionPerformed

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

    private void TglLahirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglLahirKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TglLahirKeyPressed

    private void UmurKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_UmurKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_UmurKeyPressed

    private void JKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JKKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_JKKeyPressed

    private void BtnDokter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokter1ActionPerformed
        akses.setform("DlgSuratKonsul");
        dokter.emptTeks();
        dokter.isCek();
        dokter.TCari.requestFocus();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokter1ActionPerformed

    private void BtnDokter1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokter1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnDokter1KeyPressed

    private void NmPoliTujuanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NmPoliTujuanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NmPoliTujuanActionPerformed

    private void NmPoliAsalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NmPoliAsalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NmPoliAsalActionPerformed

    private void BtnPoli1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoli1ActionPerformed
     if(Sequel.cariInteger("select count(kamar_inap.no_rawat) from kamar_inap where kamar_inap.no_rawat=?",TNoRw.getText())>0){
        kamar.load();
        kamar.isCek();
        kamar.emptTeks();
        kamar.tampil();
        kamar.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kamar.setLocationRelativeTo(internalFrame1);
        kamar.setVisible(true);  
      }else{
        poli.isCek();        
        poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        poli.setLocationRelativeTo(internalFrame1);
        poli.setVisible(true);
        }
    }//GEN-LAST:event_BtnPoli1ActionPerformed

    private void BtnPoli1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPoli1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPoli1KeyPressed

    private void TanggalJawabanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalJawabanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalJawabanKeyPressed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TNoRwKeyPressed

    private void MnJawabanKonsulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnJawabanKonsulActionPerformed
if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();    
            param.put("kd_dokter2",Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?",KdDokterPerujuk.getText()));            
            param.put("ttd","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/tandatangandokter/pages/upload/"+KdDokterPerujuk.getText()+".png");
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            param.put("norawat",tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            try {
                ps=koneksi.prepareStatement("select * from riwayat_persalinan_pasien where no_rkm_medis=? order by tgl_thn");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
                    rs=ps.executeQuery();
                    i=1;
                    while(rs.next()){
                        param.put("no"+i,i+"");  
                        param.put("tgl"+i,rs.getString("tgl_thn"));
                        param.put("tempatpersalinan"+i,rs.getString("tempat_persalinan"));
                        param.put("usiahamil"+i,rs.getString("usia_hamil"));
                        param.put("jenispersalinan"+i,rs.getString("jenis_persalinan"));
                        param.put("penolong"+i,rs.getString("penolong"));
                        param.put("penyulit"+i,rs.getString("penyulit"));
                        param.put("jk"+i,rs.getString("jk"));
                        param.put("bbpb"+i,rs.getString("bbpb"));
                        param.put("keadaan"+i,rs.getString("keadaan"));
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
               try {
                konsultasiparamput="";
                ps2=koneksi.prepareStatement(
                    "select master_konsultasi.kode_masalah,master_konsultasi.nama_masalah from master_konsultasi "+
                    "inner join konsultasi on konsultasi.kode_masalah=master_konsultasi.kode_masalah "+
                    "where konsultasi.no_rawat=? order by kode_masalah");
                try {
                    ps2.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs2=ps2.executeQuery();
                    while(rs2.next()){
                        konsultasiparamput=rs2.getString("nama_masalah")+", "+konsultasiparamput;
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
            Valid.MyReportqry("rptLembarpermintaankonsuldpjp.jasper","report","::[ Cetakan Konsultasi Antar DPJP ]::",              
        "select "
        + "reg_periksa.no_rkm_medis,"//1
        + "rujukan_internal_poli.no_rawat,"//2
        + "pasien.nm_pasien,"//3
        + "pasien.tgl_lahir,"//3
//        + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"//4
        + "rujukan_internal_poli.kd_poli,"
        + "poliklinik.nm_poli,"      //5
        + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur)as umur,"
        + "rujukan_internal_poli.kd_dokter,"//6
        + "dokter.nm_dokter,"//7
        + "rujukan_internal_poli.kd_dokter2,"//8
        + "rujukan_internal_poli.nama_dokter2,"//9
        + "rujukan_internal_poli.catatan,"//10 "terapi yang diberikan"
        + "rujukan_internal_poli.diagnosa,"//10 "diagnosa"        
        + "rujukan_internal_poli.tanggal,"//11 "tanggal konsul"
        + "rujukan_internal_poli.tanggal_jawab,"//12 "tanggal jawab"
        + "rujukan_internal_poli.jawab_permintaan," //14 "PENGOBATAN"
        + "rujukan_internal_poli.saran_tindakan," //15  "USUL"
        + "rujukan_internal_poli.isi_permintaan," //16 "pemeriksaan yang dilakukan"
        + "rujukan_internal_poli.hasil," //16 "hasil"        
        + "rujukan_internal_poli.diagnosa_jawaban " //17        
        + "from reg_periksa "
        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
        + "inner join rujukan_internal_poli on reg_periksa.no_rawat=rujukan_internal_poli.no_rawat "
        + "inner join dokter on rujukan_internal_poli.kd_dokter=dokter.kd_dokter "
        + "inner join poliklinik on rujukan_internal_poli.kd_poli=poliklinik.kd_poli "
        + "where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);                                             
        }
    }//GEN-LAST:event_MnJawabanKonsulActionPerformed

    private void diagnosisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosisKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_diagnosisKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SuratKonsul dialog = new SuratKonsul(new javax.swing.JFrame(), true);
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
    private widget.Button BtnDokter;
    private widget.Button BtnDokter1;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPoli;
    private widget.Button BtnPoli1;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Tanggal DTPCari3;
    private widget.Tanggal DTPCari4;
    private widget.PanelBiasa FormInput;
    private widget.TextBox JK;
    private widget.TextBox KdDokterPerujuk;
    private widget.TextBox KdDokterTujuan;
    private widget.TextBox KdPoliAsal;
    private widget.TextBox KdPoliTujuan;
    private widget.Label LCount;
    private javax.swing.JMenuItem MnJawabanKonsul;
    private widget.TextBox NmDokterPerujuk;
    private widget.TextBox NmDokterTujuan;
    private widget.TextBox NmPoliAsal;
    private widget.TextBox NmPoliTujuan;
    private widget.TextBox NoSurat;
    private javax.swing.JPanel PanelInput;
    private widget.RadioButton R1;
    private widget.RadioButton R2;
    private widget.RadioButton R3;
    private widget.ScrollPane Scroll;
    private widget.ComboBox Status;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal TanggalJawaban;
    private widget.Tanggal TanggalSurat;
    private widget.TextBox TglLahir;
    private widget.TextBox Umur;
    private widget.TextArea anjuran;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.TextBox diagnosis;
    private widget.TextArea halhaltemuan;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel25;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel37;
    private widget.Label jLabel4;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator2;
    private widget.ComboBox kategori_konsul;
    private widget.TextArea klinikdanlab;
    private widget.TextArea lainlain;
    private widget.panelisi panelCari;
    private widget.panelisi panelGlass8;
    private widget.TextArea permintaankonsul;
    private widget.ScrollPane scrollPane10;
    private widget.ScrollPane scrollPane11;
    private widget.ScrollPane scrollPane12;
    private widget.ScrollPane scrollPane8;
    private widget.ScrollPane scrollPane9;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    private void tampil() {     
        if(R1.isSelected()==true){
            status=" surat_konsul.status='Menunggu' ";
        }else if(R2.isSelected()==true){
            status=" surat_konsul.status='Menunggu' and tanggalsurat between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' ";
        }else if(R3.isSelected()==true){
            status=" surat_konsul.status='Selesai' and surat_konsul.tanggalsurat between '"+Valid.SetTgl(DTPCari3.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari4.getSelectedItem()+"")+"' ";           
        }
        Valid.tabelKosong(tabMode);
        try {
            ps=koneksi.prepareStatement(
                    "select surat_konsul.no_rawat,reg_periksa.no_rkm_medis,surat_konsul.no_surat,surat_konsul.tanggalsurat,surat_konsul.kd_dokter_perujuk,surat_konsul.dokterperujuk,surat_konsul.kd_poli_perujuk,surat_konsul.poliperujuk,surat_konsul.kd_dokter_tujuan,"+
                    "surat_konsul.doktertujuan,surat_konsul.politujuan,pasien.jk,surat_konsul.kd_poli_tujuan,surat_konsul.permintaankonsul,surat_konsul.status,surat_konsul.klinikdanlab,surat_konsul.tanggaljawaban,surat_konsul.halhaltemuan,surat_konsul.kategori_konsul,surat_konsul.anjuran,surat_konsul.diagnosis,surat_konsul.lainlain,"+ 
                    "surat_konsul.halhaltemuan,pasien.nm_pasien,dokter.nm_dokter,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y')as tgl_lahir,surat_konsul.doktertujuan,surat_konsul.politujuan,"+
                    "surat_konsul.umur from reg_periksa inner join surat_konsul inner join pasien inner join dokter "+
                    "on surat_konsul.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and surat_konsul.kd_dokter_tujuan=dokter.kd_dokter "+
                    "where "+status+" and reg_periksa.no_rkm_medis like ? or "+
                    status+" and surat_konsul.no_rawat like ? or "+
                    status+" and pasien.nm_pasien like ? or "+
                    status+" and surat_konsul.kd_dokter_tujuan like ? or "+
                    status+" and dokter.nm_dokter like ? order by surat_konsul.no_rawat");
            try {
                ps.setString(1,"%"+TCari.getText().trim()+"%");
                ps.setString(2,"%"+TCari.getText().trim()+"%");
                ps.setString(3,"%"+TCari.getText().trim()+"%");
                ps.setString(4,"%"+TCari.getText().trim()+"%");
                ps.setString(5,"%"+TCari.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{   
                    rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("no_surat"),rs.getString("jk"),rs.getString("tanggalsurat"),
                    rs.getString("kd_dokter_perujuk"),rs.getString("dokterperujuk"),rs.getString("kd_poli_perujuk"),rs.getString("poliperujuk"),rs.getString("tgl_lahir"),
                    rs.getString("kd_dokter_tujuan"),rs.getString("doktertujuan"),rs.getString("kd_poli_tujuan"),rs.getString("politujuan"),rs.getString("umur"),rs.getString("permintaankonsul"),rs.getString("status"),rs.getString("klinikdanlab"),
                    rs.getString("tanggaljawaban"),rs.getString("halhaltemuan"),rs.getString("kategori_konsul"),rs.getString("anjuran"),rs.getString("diagnosis"),rs.getString("lainlain")
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
        TNoRw.setText("");
        KdDokterTujuan.setText("");
        NmDokterTujuan.setText("");
        KdPoliTujuan.setText("");
        NmPoliTujuan.setText("");
        TanggalSurat.setDate(new Date());
        TanggalSurat.requestFocus();
        NoSurat.setText("");
        autoSK();
    }
    


    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            NoSurat.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            JK.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());
            Valid.SetTgl2(TanggalSurat,tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            KdDokterPerujuk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            NmDokterPerujuk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
            KdPoliAsal.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            NmPoliAsal.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            KdDokterTujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            NmDokterTujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            KdPoliTujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            NmPoliTujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            Umur.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            permintaankonsul.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());            
            Status.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            klinikdanlab.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
    //      Valid.SetTgl2(TanggalJawaban,tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            halhaltemuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
            kategori_konsul.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
            anjuran.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
            diagnosis.setText(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString());
            lainlain.setText(tbObat.getValueAt(tbObat.getSelectedRow(),24).toString());
        }
    }
    
    public void getData2() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            NoSurat.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            JK.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());
            Valid.SetTgl2(TanggalSurat,tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            KdDokterPerujuk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            NmDokterPerujuk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
            KdPoliAsal.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            NmPoliAsal.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            KdDokterTujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            NmDokterTujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            KdPoliTujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            NmPoliTujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            Umur.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            permintaankonsul.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());            
            Status.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            klinikdanlab.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
            Valid.SetTgl2(TanggalJawaban,tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            halhaltemuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
            kategori_konsul.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
            anjuran.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
            diagnosis.setText(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString());
            lainlain.setText(tbObat.getValueAt(tbObat.getSelectedRow(),24).toString());
        }
    }
    
    public void setNoRm(String norm,String nama) {
        KdDokterPerujuk.setText(akses.getkode());
        TNoRM.setText(norm);
        TPasien.setText(nama);
        
//        TCari.setText(norm);
            TCari.setText(TNoRw.getText());
        ChkInput.setSelected(true);
        isForm();
        autoSK();
        isRawat();
        tampil();
        
        
    }
    
    public void setNoRm(String norawat,String norm,String nama,String kodepoli,String namapoli,String kodedokter,String namadokter) {
        TNoRw.setText(norawat);
        TNoRM.setText(norm);
        TPasien.setText(nama);
        JK.setText(Sequel.cariIsi("select jk from pasien where no_rkm_medis=?",TNoRM.getText()));
        TglLahir.setText(Sequel.cariIsi("select DATE_FORMAT(tgl_lahir,'%d-%m-%Y') tgl_lahir from pasien where no_rkm_medis=?",TNoRM.getText()));
        Umur.setText(Sequel.cariIsi("select umur from pasien where no_rkm_medis=?",TNoRM.getText()));
        KdPoliAsal.setText(kodepoli);
        NmPoliAsal.setText(namapoli);
        KdDokterPerujuk.setText(kodedokter);
        NmDokterPerujuk.setText(namadokter);
//        TCari.setText(norm);
        TCari.setText(TNoRw.getText());
        ChkInput.setSelected(true);
        isForm();
        autoSK();
        isRawat();
        tampil();
    }
    
        public void setNoRm2(String norawat,String norm,String nama,String kodedokter,String namadokter) {
        TNoRw.setText(norawat);
        TNoRM.setText(norm);
        TPasien.setText(nama);
        JK.setText(Sequel.cariIsi("select jk from pasien where no_rkm_medis=?",TNoRM.getText()));
        TglLahir.setText(Sequel.cariIsi("select DATE_FORMAT(tgl_lahir,'%d-%m-%Y') tgl_lahir from pasien where no_rkm_medis=?",TNoRM.getText()));
        Umur.setText(Sequel.cariIsi("select umur from pasien where no_rkm_medis=?",TNoRM.getText()));
        kamarasal=Sequel.cariIsi("select ifnull(kd_kamar,'') from kamar_inap where no_rawat='"+TNoRw.getText()+"'");
        namakamar=Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar='"+kamarasal+"' "); 
        KdPoliAsal.setText(kamarasal);
        NmPoliAsal.setText(namakamar);
        //KdDokterPerujuk.setText(kodedokter);
        //NmDokterPerujuk.setText(namadokter);
//        TCari.setText(norm);
    TCari.setText(TNoRw.getText());
        ChkInput.setSelected(true);
        isForm();
        autoSK();
        isRawat();
        tampil();
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(1400, 550));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(1400,50));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }
    
    private void isForm2(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(1400, 50));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(1400,50));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }
    public void isCek(){
        BtnSimpan.setEnabled(akses.getskdp_bpjs());
        BtnHapus.setEnabled(akses.getadmin());
        BtnPrint.setEnabled(akses.getadmin());
        BtnBatal.setEnabled(akses.getadmin());
        BtnEdit.setEnabled(akses.getskdp_bpjs());
        if(akses.getjml2()>=1){
            KdDokterPerujuk.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokterPerujuk.setText(akses.getkode());
            NmDokterPerujuk.setText(dokter.tampil3(akses.getkode()));
            if(NmDokterPerujuk.getText().equals("")){
                KdDokterPerujuk.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan dokter...!!");
            }
        }     
    }
    
    private void autoSK() {
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(surat_konsul.no_surat,3),signed)),0) from surat_konsul where left(surat_konsul.tanggalsurat,10)='"+Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+"' ",
                "SK"+TanggalSurat.getSelectedItem().toString().substring(6,10)+TanggalSurat.getSelectedItem().toString().substring(3,5)+TanggalSurat.getSelectedItem().toString().substring(0,2),3,NoSurat); 
    }

    public JTable getTable(){
        return tbObat;
    }      

    private void isRawat(){
    KdDokterPerujuk.setText(akses.getkode());
    }    
    

    
}
