/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgPasienMeninggal.java
 *
 * Created on 16 Desember, 2010, 10:47:01 AM by Mustafa Daeng Muma
 */

package simrskhanza;
import fungsi.validasi2;
import java.sql.SQLException;
import kepegawaian.DlgCariDokter;
import laporan.DlgCariPenyakit;
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import laporan.DlgBerkasRawat;



/**
 *
 * @author Modifikasi oleh Mustafa Daeng Muma
 */
public class DlgPasienMati extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();    
    private validasi Valid=new validasi();
    private validasi2 Valid1=new validasi2();
    private DlgPasien pasien=new DlgPasien(null,true);
    private PreparedStatement ps;
    private ResultSet rs;
    private DlgCariDokter dokter=new DlgCariDokter(null,true);
    private String finger="", bln_angka="", bln_romawi="",nomorSuratGenerated = "";
    private DlgCariPenyakit penyakit1=new DlgCariPenyakit(null,true);    
    
    
    /** Creates new form DlgPasienMati
     * @param parent
     * @param modal */
    public DlgPasienMati(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();       
        
    tabMode = new DefaultTableModel(null, new Object[] {        
        "No. Surat",               //1
        "Tanggal",                 //2
        "Jam",                     //3
        "No. Rawat",               //4
        "No.RM",                   //5
        "Nama Pasien",             //6
        "Lahir Mati",              //7
        "D.O.A",                   //8
        "Keadaan Meninggal",       //9
        "Lainnya",                 //10
        "Diagnosa Utama",          //11
        "Diagnosa Sekunder",       //12
        "Penyebab Kematian",       //13
        "Penyakit Lain",           //14
        "Penyebab Utama Bayi",     //15
        "Penyebab Lain Bayi",      //16
        "Penyebab Utama Ibu",      //17
        "Penyebab Lain Ibu",       //18
        "Kd Dokter",               //19
        "Nama Dokter",             //20
        "Keluarga Pasien",         //21
        "Hubungan Keluarga",       //22
        "Tgl. Surat",              //23
        "Petugas"                  //24
    }) {
        @Override
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return colIndex == 0; // Hanya kolom pertama (checkbox) yang editable
        }

        Class[] types = new Class[] {            
            java.lang.Object.class,  // Kolom lainnya default Object
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class
        };

        @Override
        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }
    };
    tbMati.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbMati.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbMati.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 21; i++) {
            TableColumn column = tbMati.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(150);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(60);
            }else if(i==3){
                column.setPreferredWidth(150);
            }else if(i==4){
                column.setPreferredWidth(50);
            }else if(i==5){
                column.setPreferredWidth(200);
            }else if(i==6){
                column.setPreferredWidth(75);
            }else if(i==7){
                column.setPreferredWidth(75);
            }else if(i==8){
                column.setPreferredWidth(90);
            }else if(i==9){
                column.setPreferredWidth(90);
            }else if(i==10){
                column.setPreferredWidth(120);
            }else if(i==11){
                column.setPreferredWidth(120);
            }else if(i==12){
                column.setPreferredWidth(100);
            }else if(i==13){
                column.setPreferredWidth(100);
            }else if(i==14){
                column.setPreferredWidth(100);
            }else if(i==15){
                column.setPreferredWidth(100);
            }else if(i==16){
                column.setPreferredWidth(90);
            }else if(i==17){
                column.setPreferredWidth(150);
            }else if(i==18){
                column.setPreferredWidth(100);
            }else if(i==19){
                column.setPreferredWidth(150);
            }else if(i==20){
                column.setPreferredWidth(150);
            }
        }
        tbMati.setDefaultRenderer(Object.class, new WarnaTable());


        TNoRM.setDocument(new batasInput((byte)15).getKata(TNoRM));
//        penyebab_kematian.setDocument(new batasInput((int)80).getKata(penyebab_kematian));
//        KodeDiagnosaUtama.setDocument(new batasInput((int)10).getKata(KodeDiagnosaUtama));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        lainnya.setDocument(new batasInput((byte)100).getKata(lainnya));
        no_rawat.setDocument(new batasInput((int)100).getKata(no_rawat));
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
        
        pasien.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(akses.getform().equals("DlgPasienMati")){
                    if(pasien.getTable().getSelectedRow()!= -1){                   
                        TNoRM.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),1).toString());
                        TPasien.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),2).toString());
                    }  
                    if(pasien.getTable2().getSelectedRow()!= -1){                   
                        TNoRM.setText(pasien.getTable2().getValueAt(pasien.getTable2().getSelectedRow(),1).toString());
                        TPasien.setText(pasien.getTable2().getValueAt(pasien.getTable2().getSelectedRow(),2).toString());
                    }  
                    if(pasien.getTable3().getSelectedRow()!= -1){                   
                        TNoRM.setText(pasien.getTable3().getValueAt(pasien.getTable3().getSelectedRow(),1).toString());
                        TPasien.setText(pasien.getTable3().getValueAt(pasien.getTable3().getSelectedRow(),2).toString());
                    }  
                    TNoRM.requestFocus();
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
        
        pasien.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(akses.getform().equals("DlgPasienMati")){
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        pasien.dispose();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        pasien.getTable2().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(akses.getform().equals("DlgPasienMati")){
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        pasien.dispose();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        pasien.getTable3().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(akses.getform().equals("DlgPasienMati")){
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        pasien.dispose();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
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
        
        penyakit1.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if( penyakit1.getTable().getSelectedRow()!= -1){                   
//                    KodeDiagnosaUtama.setText(penyakit1.getTable().getValueAt(penyakit1.getTable().getSelectedRow(),0).toString());
                    penyebab_kematian.setText(penyakit1.getTable().getValueAt(penyakit1.getTable().getSelectedRow(),1).toString());
                }  
                penyebab_kematian.requestFocus();
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
        
        ChkAccor.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (ChkAccor.isSelected()) {
                    // Ketika ChkAccor dicentang, PanelAccor diperluas
                    PanelAccor.setPreferredSize(new Dimension(245, HEIGHT));
                    FormMenu.setVisible(true);
                    FormPhotoPass.setVisible(true);
                } else {
                    // Ketika ChkAccor tidak dicentang, PanelAccor disembunyikan
                    PanelAccor.setPreferredSize(new Dimension(15, HEIGHT));
                    FormMenu.setVisible(false);
                    FormPhotoPass.setVisible(false);
                }
                // Refresh ulang tampilan agar perubahan terlihat langsung
                PanelAccor.revalidate();
                PanelAccor.repaint();
            }
        });
        
        keadaan_meninggal.addActionListener(e -> {
            updateLabelVisibility();
        });   
        
        keadaan_meninggal.addActionListener(e -> {
            updateComboBoxVisibility();
        });
        
        keadaan_meninggal.addActionListener(e -> {
            updateComboBoxhariVisibility();
        });
        
        keadaan_meninggal.addActionListener(e -> {
            updatelabelhariVisibility();
        });
        

        lainnya.setEnabled(false); // Awalnya tidak aktif
        lainnya.setVisible(false); // Awalnya tidak terlihat
        
        // Menambahkan listener ke combobox
        keadaan_meninggal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Periksa nilai yang dipilih
                String selectedValue = (String) keadaan_meninggal.getSelectedItem();
                if ("Lainnya".equals(selectedValue)) {
                    lainnya.setEnabled(true);  // Aktifkan
                    lainnya.setVisible(true); // Tampilkan
                    lainnya.requestFocus();
                } else {
                    lainnya.setEnabled(false); // Nonaktifkan
                    lainnya.setVisible(false); // Sembunyikan                    
                }
            }
        });

        ChkInput.setSelected(true);
        isForm();
        
        
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

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakSuratKematian = new javax.swing.JMenuItem();
        MnCetakSuratSebabKematian = new javax.swing.JMenuItem();
        MnBerkasDigitalKeperawatan = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbMati = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus1 = new widget.Button();
        BtnHapus = new widget.Button();
        BtnPrint = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.panelisi();
        jLabel8 = new widget.Label();
        lainnya = new widget.TextBox();
        TPasien = new widget.TextBox();
        DTPTgl = new widget.Tanggal();
        TNoRM = new widget.TextBox();
        BtnSeek = new widget.Button();
        cmbJam = new widget.ComboBox();
        cmbMnt = new widget.ComboBox();
        cmbDtk = new widget.ComboBox();
        jLabel10 = new widget.Label();
        jLabel5 = new widget.Label();
        jLabel12 = new widget.Label();
        jLabel13 = new widget.Label();
        jLabel14 = new widget.Label();
        diagnosa_utama = new widget.TextBox();
        diagnosa_sekunder = new widget.TextBox();
        penyebab_lain_bayi = new widget.TextBox();
        doa = new widget.ComboBox();
        jLabel16 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        no_rawat = new widget.TextBox();
        jLabel19 = new widget.Label();
        lahir_mati = new widget.ComboBox();
        jLabel20 = new widget.Label();
        keadaan_meninggal = new widget.ComboBox();
        jLabel21 = new widget.Label();
        jLabel22 = new widget.Label();
        scrollPane1 = new widget.ScrollPane();
        penyebab_kematian = new widget.TextArea();
        jLabel23 = new widget.Label();
        jLabel24 = new widget.Label();
        scrollPane2 = new widget.ScrollPane();
        penyakit_lain = new widget.TextArea();
        jLabel25 = new widget.Label();
        jLabel27 = new widget.Label();
        jLabel28 = new widget.Label();
        jLabel29 = new widget.Label();
        jLabel30 = new widget.Label();
        jLabel31 = new widget.Label();
        jLabel32 = new widget.Label();
        jLabel33 = new widget.Label();
        penyebab_utama_bayi = new widget.TextBox();
        penyebab_utama_ibu = new widget.TextBox();
        penyebab_lain_ibu = new widget.TextBox();
        jLabel11 = new widget.Label();
        jLabel34 = new widget.Label();
        jLabel35 = new widget.Label();
        jLabel36 = new widget.Label();
        jLabel37 = new widget.Label();
        penerima = new widget.TextBox();
        jLabel38 = new widget.Label();
        jLabel39 = new widget.Label();
        hubungan_pasien = new widget.ComboBox();
        no_surat = new widget.TextBox();
        tgl_pembuatan = new widget.Tanggal();
        petugas = new widget.TextBox();
        jLabel40 = new widget.Label();
        jLabel17 = new widget.Label();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        FormMenu = new widget.PanelBiasa();
        jLabel42 = new widget.Label();
        NoRekamMedisDipilih = new widget.TextBox();
        NamaPasienDipilih = new widget.TextBox();
        FormPhotoPass = new widget.PanelBiasa();
        FormPhoto = new widget.PanelBiasa();
        FormPass2 = new widget.PanelBiasa();
        btnAmbilPhoto = new widget.Button();
        BtnRefreshPhoto = new widget.Button();
        Scroll4 = new widget.ScrollPane();
        LoadHTML = new widget.editorpane();
        FormPass = new widget.PanelBiasa();
        FormPass1 = new widget.PanelBiasa();
        btnUbahPassword = new widget.Button();
        PasswordPasien = new widget.TextArea();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCetakSuratKematian.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakSuratKematian.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSuratKematian.setForeground(java.awt.Color.darkGray);
        MnCetakSuratKematian.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSuratKematian.setText("Cetak Surat Kematian");
        MnCetakSuratKematian.setName("MnCetakSuratKematian"); // NOI18N
        MnCetakSuratKematian.setPreferredSize(new java.awt.Dimension(190, 28));
        MnCetakSuratKematian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSuratKematianActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSuratKematian);

        MnCetakSuratSebabKematian.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakSuratSebabKematian.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSuratSebabKematian.setForeground(java.awt.Color.darkGray);
        MnCetakSuratSebabKematian.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSuratSebabKematian.setText("Surat Sebab Kematian");
        MnCetakSuratSebabKematian.setName("MnCetakSuratSebabKematian"); // NOI18N
        MnCetakSuratSebabKematian.setPreferredSize(new java.awt.Dimension(190, 28));
        MnCetakSuratSebabKematian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSuratSebabKematianActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSuratSebabKematian);

        MnBerkasDigitalKeperawatan.setBackground(new java.awt.Color(255, 255, 254));
        MnBerkasDigitalKeperawatan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnBerkasDigitalKeperawatan.setForeground(java.awt.Color.darkGray);
        MnBerkasDigitalKeperawatan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnBerkasDigitalKeperawatan.setText("Berkas Digital Keperawatan");
        MnBerkasDigitalKeperawatan.setName("MnBerkasDigitalKeperawatan"); // NOI18N
        MnBerkasDigitalKeperawatan.setPreferredSize(new java.awt.Dimension(190, 28));
        MnBerkasDigitalKeperawatan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnBerkasDigitalKeperawatanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnBerkasDigitalKeperawatan);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pasien Meninggal ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N

        tbMati.setAutoCreateRowSorter(true);
        tbMati.setComponentPopupMenu(jPopupMenu1);
        tbMati.setName("tbMati"); // NOI18N
        tbMati.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbMatiMouseClicked(evt);
            }
        });
        tbMati.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbMatiKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbMati);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(85, 30));
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
        BtnBatal.setPreferredSize(new java.awt.Dimension(85, 30));
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

        BtnHapus1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus1.setMnemonic('H');
        BtnHapus1.setText("Edit");
        BtnHapus1.setToolTipText("Alt+H");
        BtnHapus1.setName("BtnHapus1"); // NOI18N
        BtnHapus1.setPreferredSize(new java.awt.Dimension(85, 30));
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
        panelGlass8.add(BtnHapus1);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(85, 30));
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

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(85, 30));
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass8.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(85, 30));
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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(500, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

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
        panelGlass9.add(BtnCari);

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
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setBackground(new java.awt.Color(255, 255, 255));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 330));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('M');
        ChkInput.setText(".: Filter Data");
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
        FormInput.setPreferredSize(new java.awt.Dimension(150, 250));
        FormInput.setLayout(null);

        jLabel8.setText("Jam :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(243, 10, 30, 23);

        lainnya.setForeground(new java.awt.Color(0, 0, 0));
        lainnya.setText("-");
        lainnya.setHighlighter(null);
        lainnya.setName("lainnya"); // NOI18N
        lainnya.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lainnyaKeyPressed(evt);
            }
        });
        FormInput.add(lainnya);
        lainnya.setBounds(920, 40, 350, 23);

        TPasien.setEditable(false);
        TPasien.setForeground(new java.awt.Color(0, 0, 0));
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(335, 40, 265, 23);

        DTPTgl.setForeground(new java.awt.Color(50, 70, 50));
        DTPTgl.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "13-01-2025" }));
        DTPTgl.setDisplayFormat("dd-MM-yyyy");
        DTPTgl.setName("DTPTgl"); // NOI18N
        DTPTgl.setOpaque(false);
        DTPTgl.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPTglItemStateChanged(evt);
            }
        });
        DTPTgl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPTglKeyPressed(evt);
            }
        });
        FormInput.add(DTPTgl);
        DTPTgl.setBounds(140, 10, 90, 23);

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
        TNoRM.setBounds(265, 40, 65, 23);

        BtnSeek.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek.setMnemonic('1');
        BtnSeek.setToolTipText("Alt+1");
        BtnSeek.setName("BtnSeek"); // NOI18N
        BtnSeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeekActionPerformed(evt);
            }
        });
        BtnSeek.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeekKeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek);
        BtnSeek.setBounds(610, 40, 20, 23);

        cmbJam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cmbJam.setName("cmbJam"); // NOI18N
        cmbJam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbJamKeyPressed(evt);
            }
        });
        FormInput.add(cmbJam);
        cmbJam.setBounds(280, 10, 40, 23);

        cmbMnt.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cmbMnt.setName("cmbMnt"); // NOI18N
        cmbMnt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbMntKeyPressed(evt);
            }
        });
        FormInput.add(cmbMnt);
        cmbMnt.setBounds(325, 10, 40, 23);

        cmbDtk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cmbDtk.setName("cmbDtk"); // NOI18N
        cmbDtk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbDtkKeyPressed(evt);
            }
        });
        FormInput.add(cmbDtk);
        cmbDtk.setBounds(370, 10, 40, 23);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel10.setText("Tgl.  Meninggal");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(15, 10, 110, 23);

        jLabel5.setText("Dead On Arrival ( DOA ) :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(400, 100, 140, 23);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Penyebab Kematian :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(15, 190, 115, 23);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("Diagnosa Utama");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(15, 130, 110, 23);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Diagnosa Sekunder");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(15, 160, 110, 23);

        diagnosa_utama.setForeground(new java.awt.Color(0, 0, 0));
        diagnosa_utama.setHighlighter(null);
        diagnosa_utama.setName("diagnosa_utama"); // NOI18N
        diagnosa_utama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diagnosa_utamaKeyPressed(evt);
            }
        });
        FormInput.add(diagnosa_utama);
        diagnosa_utama.setBounds(140, 130, 490, 23);

        diagnosa_sekunder.setForeground(new java.awt.Color(0, 0, 0));
        diagnosa_sekunder.setHighlighter(null);
        diagnosa_sekunder.setName("diagnosa_sekunder"); // NOI18N
        diagnosa_sekunder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diagnosa_sekunderKeyPressed(evt);
            }
        });
        FormInput.add(diagnosa_sekunder);
        diagnosa_sekunder.setBounds(140, 160, 490, 23);

        penyebab_lain_bayi.setForeground(new java.awt.Color(0, 0, 0));
        penyebab_lain_bayi.setHighlighter(null);
        penyebab_lain_bayi.setName("penyebab_lain_bayi"); // NOI18N
        penyebab_lain_bayi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                penyebab_lain_bayiKeyPressed(evt);
            }
        });
        FormInput.add(penyebab_lain_bayi);
        penyebab_lain_bayi.setBounds(800, 130, 470, 23);

        doa.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "YA", "TIDAK" }));
        doa.setName("doa"); // NOI18N
        doa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                doaKeyPressed(evt);
            }
        });
        FormInput.add(doa);
        doa.setBounds(550, 100, 80, 23);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("Dokter");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(15, 70, 110, 23);

        KdDokter.setEditable(false);
        KdDokter.setForeground(new java.awt.Color(0, 0, 0));
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        FormInput.add(KdDokter);
        KdDokter.setBounds(140, 70, 87, 23);

        NmDokter.setEditable(false);
        NmDokter.setForeground(new java.awt.Color(0, 0, 0));
        NmDokter.setHighlighter(null);
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(230, 70, 370, 23);

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
        BtnDokter.setBounds(610, 70, 20, 23);

        no_rawat.setEditable(false);
        no_rawat.setForeground(new java.awt.Color(0, 0, 0));
        no_rawat.setText("2024/12/10/000269");
        no_rawat.setHighlighter(null);
        no_rawat.setName("no_rawat"); // NOI18N
        no_rawat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                no_rawatKeyPressed(evt);
            }
        });
        FormInput.add(no_rawat);
        no_rawat.setBounds(140, 40, 120, 23);

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel19.setText("Pasien Lahir Mati");
        jLabel19.setName("jLabel19"); // NOI18N
        FormInput.add(jLabel19);
        jLabel19.setBounds(15, 100, 110, 23);

        lahir_mati.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "YA", "TIDAK" }));
        lahir_mati.setName("lahir_mati"); // NOI18N
        lahir_mati.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lahir_matiKeyPressed(evt);
            }
        });
        FormInput.add(lahir_mati);
        lahir_mati.setBounds(140, 100, 90, 23);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setText(":");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(125, 40, 10, 23);

        keadaan_meninggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Hamil", "Bersalin", "Nifas ( masa sampai 2 bulan setelah bersalin / abortus )", "Lainnya" }));
        keadaan_meninggal.setName("keadaan_meninggal"); // NOI18N
        keadaan_meninggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                keadaan_meninggalKeyPressed(evt);
            }
        });
        FormInput.add(keadaan_meninggal);
        keadaan_meninggal.setBounds(920, 10, 350, 23);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel21.setText("Bila yang meninggal wanita umur 10 - 59 tahun, ");
        jLabel21.setName("jLabel21"); // NOI18N
        FormInput.add(jLabel21);
        jLabel21.setBounds(660, 10, 270, 20);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel22.setText("Almarhumah dalam keadaan :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(660, 30, 550, 20);

        scrollPane1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane1.setName("scrollPane1"); // NOI18N

        penyebab_kematian.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        penyebab_kematian.setForeground(new java.awt.Color(0, 0, 0));
        penyebab_kematian.setName("penyebab_kematian"); // NOI18N
        penyebab_kematian.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                penyebab_kematianKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(penyebab_kematian);

        FormInput.add(scrollPane1);
        scrollPane1.setBounds(15, 220, 300, 70);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("Penyebab Utama Bayi");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(660, 100, 130, 23);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Kematian 0-6 hari, termasuk lahir mati :");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(660, 70, 610, 20);

        scrollPane2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        scrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane2.setName("scrollPane2"); // NOI18N

        penyakit_lain.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        penyakit_lain.setForeground(new java.awt.Color(0, 0, 0));
        penyakit_lain.setName("penyakit_lain"); // NOI18N
        penyakit_lain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                penyakit_lainKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(penyakit_lain);

        FormInput.add(scrollPane2);
        scrollPane2.setBounds(323, 220, 305, 70);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setText("Nama Pasien");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(15, 40, 110, 23);

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText(":");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(125, 100, 10, 20);

        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel28.setText(":");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(125, 130, 10, 23);

        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel29.setText(":");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(125, 160, 10, 23);

        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setText("Penyakit Lain tidak berhubungan dgn sebab kematian :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(325, 190, 310, 23);

        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel31.setText("Penyebab Lain Bayi");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(660, 130, 130, 23);

        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel32.setText("Penyebab Utama Ibu");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(660, 160, 130, 23);

        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel33.setText("Penyebab Lain Ibu");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(660, 190, 130, 23);

        penyebab_utama_bayi.setForeground(new java.awt.Color(0, 0, 0));
        penyebab_utama_bayi.setHighlighter(null);
        penyebab_utama_bayi.setName("penyebab_utama_bayi"); // NOI18N
        penyebab_utama_bayi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                penyebab_utama_bayiKeyPressed(evt);
            }
        });
        FormInput.add(penyebab_utama_bayi);
        penyebab_utama_bayi.setBounds(800, 100, 470, 23);

        penyebab_utama_ibu.setForeground(new java.awt.Color(0, 0, 0));
        penyebab_utama_ibu.setHighlighter(null);
        penyebab_utama_ibu.setName("penyebab_utama_ibu"); // NOI18N
        penyebab_utama_ibu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                penyebab_utama_ibuKeyPressed(evt);
            }
        });
        FormInput.add(penyebab_utama_ibu);
        penyebab_utama_ibu.setBounds(800, 160, 470, 23);

        penyebab_lain_ibu.setForeground(new java.awt.Color(0, 0, 0));
        penyebab_lain_ibu.setHighlighter(null);
        penyebab_lain_ibu.setName("penyebab_lain_ibu"); // NOI18N
        penyebab_lain_ibu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                penyebab_lain_ibuKeyPressed(evt);
            }
        });
        FormInput.add(penyebab_lain_ibu);
        penyebab_lain_ibu.setBounds(800, 190, 470, 23);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText(":");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(125, 10, 10, 23);

        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel34.setText(":");
        jLabel34.setName("jLabel34"); // NOI18N
        FormInput.add(jLabel34);
        jLabel34.setBounds(790, 100, 10, 23);

        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel35.setText(":");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(790, 130, 10, 23);

        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel36.setText(":");
        jLabel36.setName("jLabel36"); // NOI18N
        FormInput.add(jLabel36);
        jLabel36.setBounds(790, 160, 10, 23);

        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel37.setText(":");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(790, 190, 10, 23);

        penerima.setForeground(new java.awt.Color(0, 0, 0));
        penerima.setHighlighter(null);
        penerima.setName("penerima"); // NOI18N
        penerima.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                penerimaKeyPressed(evt);
            }
        });
        FormInput.add(penerima);
        penerima.setBounds(800, 260, 350, 23);

        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel38.setText("Penerima Surat");
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(660, 260, 110, 23);

        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel39.setText(":");
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(790, 260, 10, 23);

        hubungan_pasien.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "AYAH", "IBU", "SUAMI", "ISTRI", "SAUDARA", "SEPUPU", "ANAK", "CUCU", "KAKEK", "NENEK", "PAMAN", "BIBI", "TEMAN", "REKAN KERJA", "MERTUA", "MENANTU", "KEMANAKAN", "TETANGGA", "DIRI SENDIRI" }));
        hubungan_pasien.setName("hubungan_pasien"); // NOI18N
        hubungan_pasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                hubungan_pasienKeyPressed(evt);
            }
        });
        FormInput.add(hubungan_pasien);
        hubungan_pasien.setBounds(1160, 260, 110, 23);

        no_surat.setEditable(false);
        no_surat.setForeground(new java.awt.Color(0, 0, 0));
        no_surat.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        no_surat.setEnabled(false);
        no_surat.setHighlighter(null);
        no_surat.setName("no_surat"); // NOI18N
        no_surat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                no_suratKeyPressed(evt);
            }
        });
        FormInput.add(no_surat);
        no_surat.setBounds(500, 10, 130, 23);

        tgl_pembuatan.setForeground(new java.awt.Color(50, 70, 50));
        tgl_pembuatan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "13-01-2025" }));
        tgl_pembuatan.setDisplayFormat("dd-MM-yyyy");
        tgl_pembuatan.setName("tgl_pembuatan"); // NOI18N
        tgl_pembuatan.setOpaque(false);
        tgl_pembuatan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tgl_pembuatanItemStateChanged(evt);
            }
        });
        tgl_pembuatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tgl_pembuatanKeyPressed(evt);
            }
        });
        FormInput.add(tgl_pembuatan);
        tgl_pembuatan.setBounds(40, 510, 100, 23);

        petugas.setForeground(new java.awt.Color(0, 0, 0));
        petugas.setHighlighter(null);
        petugas.setName("petugas"); // NOI18N
        petugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                petugasKeyPressed(evt);
            }
        });
        FormInput.add(petugas);
        petugas.setBounds(150, 510, 380, 23);

        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel40.setText("No. Surat  :");
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(430, 10, 70, 23);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText(":");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(125, 70, 10, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(245, 43));
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

        FormMenu.setBackground(new java.awt.Color(255, 255, 255));
        FormMenu.setBorder(null);
        FormMenu.setName("FormMenu"); // NOI18N
        FormMenu.setPreferredSize(new java.awt.Dimension(115, 73));
        FormMenu.setLayout(null);

        jLabel42.setText("No.Rekam Medis :");
        jLabel42.setName("jLabel42"); // NOI18N
        jLabel42.setPreferredSize(new java.awt.Dimension(95, 23));
        FormMenu.add(jLabel42);
        jLabel42.setBounds(4, 10, 95, 23);

        NoRekamMedisDipilih.setEditable(false);
        NoRekamMedisDipilih.setHighlighter(null);
        NoRekamMedisDipilih.setName("NoRekamMedisDipilih"); // NOI18N
        NoRekamMedisDipilih.setPreferredSize(new java.awt.Dimension(130, 23));
        FormMenu.add(NoRekamMedisDipilih);
        NoRekamMedisDipilih.setBounds(103, 10, 235, 23);

        NamaPasienDipilih.setEditable(false);
        NamaPasienDipilih.setHighlighter(null);
        NamaPasienDipilih.setName("NamaPasienDipilih"); // NOI18N
        NamaPasienDipilih.setPreferredSize(new java.awt.Dimension(130, 23));
        FormMenu.add(NamaPasienDipilih);
        NamaPasienDipilih.setBounds(11, 40, 222, 23);

        PanelAccor.add(FormMenu, java.awt.BorderLayout.NORTH);

        FormPhotoPass.setBackground(new java.awt.Color(255, 255, 255));
        FormPhotoPass.setBorder(null);
        FormPhotoPass.setName("FormPhotoPass"); // NOI18N
        FormPhotoPass.setPreferredSize(new java.awt.Dimension(115, 73));
        FormPhotoPass.setLayout(new java.awt.GridLayout(2, 0));

        FormPhoto.setBackground(new java.awt.Color(255, 255, 255));
        FormPhoto.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), " Photo Pasien : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        FormPhoto.setName("FormPhoto"); // NOI18N
        FormPhoto.setPreferredSize(new java.awt.Dimension(115, 73));
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

        FormPhoto.add(FormPass2, java.awt.BorderLayout.PAGE_END);

        Scroll4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll4.setName("Scroll4"); // NOI18N
        Scroll4.setPreferredSize(new java.awt.Dimension(200, 200));

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N
        Scroll4.setViewportView(LoadHTML);

        FormPhoto.add(Scroll4, java.awt.BorderLayout.CENTER);

        FormPhotoPass.add(FormPhoto);

        FormPass.setBackground(new java.awt.Color(255, 255, 255));
        FormPass.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), " Password EPasien : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        FormPass.setName("FormPass"); // NOI18N
        FormPass.setPreferredSize(new java.awt.Dimension(115, 73));
        FormPass.setLayout(new java.awt.BorderLayout());

        FormPass1.setBackground(new java.awt.Color(255, 255, 255));
        FormPass1.setBorder(null);
        FormPass1.setName("FormPass1"); // NOI18N
        FormPass1.setPreferredSize(new java.awt.Dimension(115, 40));

        btnUbahPassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        btnUbahPassword.setMnemonic('U');
        btnUbahPassword.setText("Ubah");
        btnUbahPassword.setToolTipText("Alt+U");
        btnUbahPassword.setName("btnUbahPassword"); // NOI18N
        btnUbahPassword.setPreferredSize(new java.awt.Dimension(100, 30));
        btnUbahPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahPasswordActionPerformed(evt);
            }
        });
        FormPass1.add(btnUbahPassword);

        FormPass.add(FormPass1, java.awt.BorderLayout.PAGE_END);

        PasswordPasien.setColumns(20);
        PasswordPasien.setRows(5);
        PasswordPasien.setName("PasswordPasien"); // NOI18N
        PasswordPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PasswordPasienKeyPressed(evt);
            }
        });
        FormPass.add(PasswordPasien, java.awt.BorderLayout.CENTER);

        FormPhotoPass.add(FormPass);

        PanelAccor.add(FormPhotoPass, java.awt.BorderLayout.CENTER);

        getContentPane().add(PanelAccor, java.awt.BorderLayout.EAST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void DTPTglKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPTglKeyPressed
        Valid.pindah(evt,TCari,cmbJam);
}//GEN-LAST:event_DTPTglKeyPressed

    private void BtnSeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeekActionPerformed
        akses.setform("DlgPasienMati");
        pasien.emptTeks();
        pasien.isCek();
        pasien.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        pasien.setLocationRelativeTo(internalFrame1);
        pasien.setVisible(true);
}//GEN-LAST:event_BtnSeekActionPerformed

    private void BtnSeekKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeekKeyPressed
        Valid.pindah(evt,TNoRM,lainnya);
}//GEN-LAST:event_BtnSeekKeyPressed

    private void cmbJamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbJamKeyPressed
        Valid.pindah(evt,DTPTgl,cmbMnt);
}//GEN-LAST:event_cmbJamKeyPressed

    private void cmbMntKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbMntKeyPressed
        Valid.pindah(evt,cmbJam,cmbDtk);
}//GEN-LAST:event_cmbMntKeyPressed

    private void cmbDtkKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbDtkKeyPressed
        Valid.pindah(evt,cmbMnt,doa);
}//GEN-LAST:event_cmbDtkKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRM,"No. RM Pasien");
        }else if (lahir_mati.getSelectedItem().toString().equals("-")){
            Valid.textKosong(lahir_mati, "Pasien Lahir Mati belum dipilih");
        }else if (doa.getSelectedItem().toString().equals("-")){
            Valid.textKosong(doa,"DOA belum dipilih");
        }else if (diagnosa_utama.getText().trim().equals("")){
            Valid.textKosong(diagnosa_utama, "Diagnosa Utama belum diisi");
        }else if (diagnosa_sekunder.getText().trim().equals("")){
            Valid.textKosong(diagnosa_sekunder, "Diagnosa sekunder belum diisi");
        }else if (penyebab_kematian.getText().trim().equals("")){    
            Valid.textKosong(penyebab_kematian, "Penyebab Kematian belum diisi");
        }else if (penyakit_lain.getText().trim().equals("")){
            Valid.textKosong(penyakit_lain, "Penyakit lain yang tidak berhubungan dengan penyebab kematian belum diisi");                
        }else if(penyebab_kematian.getText().equals("")){
            Valid.textKosong(penyebab_kematian,"Diagnosa Utama");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter DPJP");
        }else{
                    
            //modif mustafa
            try {
            // Mulai transaksi
            koneksi.setAutoCommit(false);

            // Panggil metode untuk generate nomor surat
            nomorSurat();

            int maxAttempts = 10; // Batas maksimum pengulangan
            int attempts = 0;

            while (Integer.parseInt(Sequel.cariIsi("SELECT COUNT(*) FROM pasien_meninggal WHERE no_surat = ?", nomorSuratGenerated)) > 0) {
                nomorSurat(); // Regenerate nomor surat
                attempts++;

                if (attempts >= maxAttempts) {
                    JOptionPane.showMessageDialog(null, "Gagal menghasilkan nomor surat unik setelah 10 kali percobaan.");
                    return; // Keluar dari metode jika tidak bisa menghasilkan nomor unik
                }
            }
            
            //end modif mustafa 
            
            if (Sequel.cariIsi("SELECT COUNT(*) FROM pasien_meninggal WHERE no_rkm_medis = ?", TNoRM.getText()).equals("0")) {
                // Data tidak ditemukan, lanjutkan proses simpan
                if(Sequel.menyimpantf("pasien_meninggal",
                        "'"+nomorSuratGenerated+"','"+Valid.SetTgl(DTPTgl.getSelectedItem()+"")+"','"+
                        cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem()+"','"+
                        no_rawat.getText()+"','"+TNoRM.getText()+"','"+lahir_mati.getSelectedItem()+"','"+doa.getSelectedItem()+"','"+
                        keadaan_meninggal.getSelectedItem()+"','"+lainnya.getText()+"','"+diagnosa_utama.getText()+"','"+diagnosa_sekunder.getText()+"','"+penyebab_kematian.getText()+"','"+penyakit_lain.getText()+"','"+penyebab_utama_bayi.getText()+"','"+
                        penyebab_lain_bayi.getText()+"','"+penyebab_utama_ibu.getText()+"','"+penyebab_lain_ibu.getText()+"','"+KdDokter.getText()+"','"+penerima.getText()+"','"+hubungan_pasien.getSelectedItem()+"',NOW(),'"+akses.getkode()+"'","pasien")==true){ 
                    tampil();
                    emptTeks();
                }
            } else {
                // Data ditemukan, tampilkan pesan validasi
                JOptionPane.showMessageDialog(null, "Maaf, pasien sudah dibuatkan Surat Kematian");
            }
            
        // Commit transaksi
            koneksi.commit();
        } catch (Exception e) {
            try {
                // Rollback jika terjadi error
                koneksi.rollback();
            } catch (SQLException ex) {
                System.out.println("Error rollback: " + ex);
            }
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
        } finally {
            try {
                koneksi.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Error set auto commit: " + ex);
            }
        }
    }        
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,lainnya,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed

    // Memastikan hanya petugas yang membuat atau Admin Utama yang dapat menghapus
    if (akses.getkode().equals("Admin Utama") || petugas.equals(akses.getkode())) {
        // Tampilkan dialog konfirmasi untuk menghapus
        int response = JOptionPane.showConfirmDialog(null, 
                "Yakin ingin menghapus Surat Kematian ini? klik Yes untuk menghapus dan No untuk membatalkan", 
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        
        if (response == JOptionPane.YES_OPTION) {
            // Menghapus data menggunakan metode menghapustf
            String field = "no_surat";
            String nilai_field = no_surat.getText();
            
            try {
                // Menyimpan pelacakan SQL untuk penghapusan
                Sequel.SimpanTrack("delete from pasien_meninggal where no_surat='" + nilai_field + "'");

                // Menghapus data dari tabel pasien_meninggal
                boolean berhasilDihapus = Sequel.meghapustf("pasien_meninggal", field, nilai_field);
                
                // Mengecek apakah data berhasil dihapus
                if (berhasilDihapus) {
                    tampil();  // Tampilkan data setelah penghapusan
                    emptTeks(); // Kosongkan form
                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus");
                } else {
                    JOptionPane.showMessageDialog(null, "Data tidak ditemukan atau gagal dihapus");
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
            }
        }
    } else {
        JOptionPane.showMessageDialog(null, "Anda tidak memiliki hak untuk menghapus data ini.");
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
        if(! TCari.getText().trim().equals("")){
            BtnCariActionPerformed(evt);
        }
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
                Valid.MyReportqry("rptPasienMati.jasper","report","::[ Data Pasien Meninggal ]::",
                        "SELECT pm.no_surat AS `No. Surat`, " +
                        "p.no_rkm_medis AS `No. Rekam Medis`, " +
                        "p.nm_pasien AS `Nama Pasien`, " +
                        "p.no_ktp AS `No. KTP`, " +
                        "p.jk AS `Jenis Kelamin`, " +
                        "CONCAT(p.tmpt_lahir, ', ', DATE_FORMAT(p.tgl_lahir, '%d-%m-%Y')) AS `Tempat dan Tanggal Lahir`, " +
                        "p.alamat AS `Alamat`, " +
                        "CASE WHEN rp.status_lanjut = 'Ralan' THEN rp.tgl_registrasi ELSE ki.tgl_masuk END AS `Tanggal Masuk RS`, " +
                        "CASE WHEN rp.status_lanjut = 'Ralan' THEN rp.jam_reg ELSE ki.jam_masuk END AS `Jam Masuk RS`, " +
                        "pm.tanggal AS `Tanggal Meninggal`, " +
                        "pm.jam AS `Jam Meninggal`, " +
                        "FLOOR(DATEDIFF(pm.tanggal, p.tgl_lahir) / 365) AS `Umur Saat Meninggal`, " +
                        "pm.lahir_mati AS `Lahir Mati`, " +
                        "pm.doa AS `Doa`, " +
                        "CASE WHEN TIMESTAMPDIFF(HOUR, CASE WHEN rp.status_lanjut = 'Ralan' THEN CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) ELSE CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) END, CONCAT(pm.tanggal, ' ', pm.jam)) < 48 THEN CONCAT(TIMESTAMPDIFF(HOUR, CASE WHEN rp.status_lanjut = 'Ralan' THEN CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) ELSE CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) END, CONCAT(pm.tanggal, ' ', pm.jam)), ' Jam') ELSE CONCAT(TIMESTAMPDIFF(DAY, CASE WHEN rp.status_lanjut = 'Ralan' THEN CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) ELSE CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) END, CONCAT(pm.tanggal, ' ', pm.jam)), ' Hari') END AS `Lama Dirawat di RS`, " +
                        "pm.tgl_pembutan AS `Tanggal Pembuatan`, " +
                        "pm.penerima AS `Keluarga Pasien`, " +
                        "pm.hubungan_pasien AS `Hubungan Keluarga`, " +
                        "pm.nm_dokter AS `Nama Dokter` " +
                        "FROM pasien_meninggal pm " +
                        "JOIN pasien p ON pm.no_rkm_medis = p.no_rkm_medis " +
                        "JOIN reg_periksa rp ON pm.no_rawat = rp.no_rawat " +
                        "LEFT JOIN kamar_inap ki ON rp.no_rawat = ki.no_rawat "+
                        (TCari.getText().trim().equals("")?"":
                        "where pasien_meninggal.tanggal like '%"+TCari.getText().trim()+"%' or "+
                        "pasien_meninggal.no_rkm_medis like '%"+TCari.getText().trim()+"%' or "+
                        "pasien.nm_pasien like '%"+TCari.getText().trim()+"%' or "+
                        "pasien.stts_nikah like '%"+TCari.getText().trim()+"%' or "+
                        "pasien.agama like '%"+TCari.getText().trim()+"%' or "+
                        "pasien_meninggal.kd_dokter like '%"+TCari.getText().trim()+"%' or "+
                        "dokter.nm_dokter like '%"+TCari.getText().trim()+"%' or "+
                        "pasien_meninggal.icd1 like '%"+TCari.getText().trim()+"%' or "+
                        "pasien_meninggal.keterangan like '%"+TCari.getText().trim()+"%' ")+
                        "order by pasien_meninggal.tanggal ",param);            
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
            TCari.setText("");
            tampil();
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

private void MnCetakSuratKematianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSuratKematianActionPerformed
      if(TPasien.getText().trim().equals("")){
          JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");                
      }else{
          Map<String, Object> param = new HashMap<>(); 
          param.put("namars",akses.getnamars());
          param.put("alamatrs",akses.getalamatrs());
          param.put("kotars",akses.getkabupatenrs());
          param.put("propinsirs",akses.getpropinsirs());
          param.put("kontakrs",akses.getkontakrs());
          param.put("emailrs",akses.getemailrs());   
          param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
          String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
          String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
          param.put("logo2", logoPath);
          finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",KdDokter.getText());
          param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+NmDokter.getText()+"\nID "+(finger.equals("")?KdDokter.getText():finger)+"\n"+DTPTgl.getSelectedItem());  
          Valid.MyReportqry("rptSuratKematian.jasper","report","::[ Surat Kematian ]::",                            
                            "SELECT pm.no_surat AS NoSurat, " +
                            "p.no_rkm_medis AS NoRekamMedis, " +
                            "p.nm_pasien AS NamaPasien, " +
                            "p.no_ktp AS NoKTP, " +
                            "CASE WHEN p.jk ='L' THEN 'Laki-laki' ELSE 'Perempuan' END AS JenisKelamin, " +
                            "CONCAT(p.tmp_lahir, ', ', DATE_FORMAT(p.tgl_lahir, '%d %M %Y')) AS TempatdanTanggalLahir, " +
                            "p.alamat AS Alamat, " +
                            "CASE WHEN rp.status_lanjut = 'Ralan' THEN DATE_FORMAT(rp.tgl_registrasi, '%d %M %Y') ELSE DATE_FORMAT(ki.tgl_masuk, '%d %M %Y') END AS TanggalMasukRS, " +
                            "CASE "+
                            "    WHEN rp.status_lanjut = 'Ralan' "+
                            "        THEN poliklinik.nm_poli "+
                            "    ELSE "+
                            "        (SELECT bangsal.nm_bangsal "+
                            "         FROM kamar_inap "+
                            "         JOIN kamar ON kamar.kd_kamar = kamar_inap.kd_kamar "+
                            "         JOIN bangsal ON bangsal.kd_bangsal = kamar.kd_bangsal "+
                            "         WHERE kamar_inap.no_rawat = rp.no_rawat "+
                            "           AND kamar_inap.stts_pulang = 'Meninggal' "+
                            "         LIMIT 1) "+
                            "END AS Ruangan, "+
                            "CASE WHEN rp.status_lanjut = 'Ralan' THEN rp.jam_reg ELSE ki.jam_masuk END AS JamMasukRS, " +
                            "DATE_FORMAT(pm.tanggal, '%d %M %Y') AS TanggalMeninggal, " +
                            "pm.jam AS JamMeninggal, " +
                            "CASE " +
                            "    WHEN TIMESTAMPDIFF(YEAR, p.tgl_lahir, pm.tanggal) > 0 THEN CONCAT(TIMESTAMPDIFF(YEAR, p.tgl_lahir, pm.tanggal), ' Tahun') " +
                            "    WHEN TIMESTAMPDIFF(MONTH, p.tgl_lahir, pm.tanggal) > 0 THEN CONCAT(TIMESTAMPDIFF(MONTH, p.tgl_lahir, pm.tanggal), ' Bulan') " +
                            "    WHEN DATEDIFF(pm.tanggal, p.tgl_lahir) > 0 THEN CONCAT(DATEDIFF(pm.tanggal, p.tgl_lahir), ' Hari') " +
                            "    ELSE CONCAT(TIMESTAMPDIFF(HOUR, p.tgl_lahir, pm.tanggal), ' Jam') " +
                            "END AS UmurSaatMeninggal, " +
                            "pm.lahir_mati AS LahirMati, " +
                            "pm.doa AS Doa, " +
                            "CASE " +
                            "    WHEN TIMESTAMPDIFF(HOUR, " +
                            "            CASE " +
                            "                WHEN rp.status_lanjut = 'Ralan' THEN " +
                            "                    CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) " +
                            "                ELSE " +
                            "                    CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) " +
                            "            END, " +
                            "            CONCAT(pm.tanggal, ' ', pm.jam) " +
                            "        ) < 48 " +
                            "    THEN " +
                            "        CONCAT( " +
                            "            TIMESTAMPDIFF(HOUR, " +
                            "                CASE " +
                            "                    WHEN rp.status_lanjut = 'Ralan' THEN " +
                            "                        CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) " +
                            "                    ELSE " +
                            "                        CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) " +
                            "                END, " +
                            "                CONCAT(pm.tanggal, ' ', pm.jam) " +
                            "            ), " +
                            "            ' Jam' " +
                            "        ) " +
                            "    ELSE " +
                            "        CONCAT( " +
                            "            DATEDIFF( " +
                            "                CONCAT(pm.tanggal, ' ', pm.jam), " +
                            "                CASE " +
                            "                    WHEN rp.status_lanjut = 'Ralan' THEN " +
                            "                        CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) " +
                            "                    ELSE " +
                            "                        CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) " +
                            "                END " +
                            "            ) + " +
                            "            CASE " +
                            "                WHEN TIME(CONCAT(pm.tanggal, ' ', pm.jam)) > TIME( " +
                            "                    CASE " +
                            "                        WHEN rp.status_lanjut = 'Ralan' THEN " +
                            "                            CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) " +
                            "                        ELSE " +
                            "                            CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) " +
                            "                    END " +
                            "                ) THEN 1 " +
                            "                ELSE 0 " +
                            "            END, " +
                            "            ' Hari' " +
                            "        ) " +
                            "END AS LamaDirawatdiRS,"+
                            "DATE_FORMAT(pm.tgl_pembuatan, '%d %M %Y') AS TanggalPembuatan, " +
                            "pm.penerima AS KeluargaPasien, " +
                            "pm.hubungan_pasien AS HubunganKeluarga, " +
                            "d.nm_dokter AS NamaDokter, " +  
                            "CASE " +
                            "    WHEN TIMESTAMPDIFF(YEAR, p.tgl_lahir, pm.tanggal) BETWEEN 10 AND 59 " +
                            "         AND p.jk = 'P' THEN " +
                            "         CASE " +
                            "             WHEN pm.keadaan_meninggal = 'Lainnya' THEN pm.lainnya " +
                            "             ELSE pm.keadaan_meninggal " +
                            "         END " +
                            "    ELSE '-' " +
                            "END AS KeadaanMeninggal " +     
                            "FROM pasien_meninggal pm " +
                            "INNER JOIN dokter d on pm.kd_dokter=d.kd_dokter " +        
                            "INNER JOIN pasien p ON pm.no_rkm_medis = p.no_rkm_medis " +
                            "INNER JOIN reg_periksa rp ON pm.no_rawat = rp.no_rawat " +
                            "LEFT JOIN kamar_inap ki ON rp.no_rawat = ki.no_rawat " +
                            "LEFT JOIN poliklinik ON poliklinik.kd_poli = rp.kd_poli "+                            
                            "LEFT JOIN kamar ON kamar.kd_kamar = ki.kd_kamar " +
                            "LEFT JOIN bangsal ON bangsal.kd_bangsal = kamar.kd_bangsal " +
                            "where pm.no_rkm_medis='"+TNoRM.getText()+"' ",param);
      }
}//GEN-LAST:event_MnCetakSuratKematianActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
        PanelAccor.setPreferredSize(new Dimension(15, HEIGHT)); // Ukuran kecil (tersembunyi)
        FormMenu.setVisible(false);
        FormPhotoPass.setVisible(false);
        ChkAccor.setSelected(false); // Checkbox tidak tercentang secara default
        updatelabelhariVisibility();
        updateComboBoxhariVisibility();
        updateComboBoxVisibility();
        updateLabelVisibility();
    }//GEN-LAST:event_formWindowOpened

    private void MnBerkasDigitalKeperawatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnBerkasDigitalKeperawatanActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else{
            if(tbMati.getSelectedRow()>-1){
                if(!tbMati.getValueAt(tbMati.getSelectedRow(),3).toString().equals("")){
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    DlgBerkasRawat berkas=new DlgBerkasRawat(null,true);
                    berkas.setJudul("::[ Berkas Digital Perawatan ]::","berkasrawat/pages");
                    try {
                        if(akses.gethapus_berkas_digital_perawatan()==true){
                            berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbMati.getValueAt(tbMati.getSelectedRow(),3).toString());
                        }else{
                            berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2nonhapus.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbMati.getValueAt(tbMati.getSelectedRow(),3).toString());
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
    }//GEN-LAST:event_MnBerkasDigitalKeperawatanActionPerformed

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=? ",TPasien,TNoRM.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeekActionPerformed(null);
        }else{
            Valid.pindah(evt,doa,diagnosa_utama);
        }
    }//GEN-LAST:event_TNoRMKeyPressed

    private void diagnosa_utamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosa_utamaKeyPressed
        Valid.pindah(evt,TNoRM,diagnosa_sekunder);
    }//GEN-LAST:event_diagnosa_utamaKeyPressed

    private void diagnosa_sekunderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosa_sekunderKeyPressed
        Valid.pindah(evt,diagnosa_utama,penyebab_lain_bayi);
    }//GEN-LAST:event_diagnosa_sekunderKeyPressed

    private void penyebab_lain_bayiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_penyebab_lain_bayiKeyPressed
        Valid.pindah(evt,diagnosa_sekunder,diagnosa_utama);
    }//GEN-LAST:event_penyebab_lain_bayiKeyPressed

    private void doaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_doaKeyPressed
        Valid.pindah(evt,cmbDtk,TNoRM);
    }//GEN-LAST:event_doaKeyPressed

    private void lainnyaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lainnyaKeyPressed
        Valid.pindah(evt,keadaan_meninggal,BtnSimpan);
    }//GEN-LAST:event_lainnyaKeyPressed

    private void tbMatiKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbMatiKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbMatiKeyReleased

    private void MnCetakSuratSebabKematianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSuratSebabKematianActionPerformed
      if(TPasien.getText().trim().equals("")){
          JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");                
      }else{
          Map<String, Object> param = new HashMap<>(); 
          param.put("namars",akses.getnamars());
          param.put("alamatrs",akses.getalamatrs());
          param.put("kotars",akses.getkabupatenrs());
          param.put("propinsirs",akses.getpropinsirs());
          param.put("kontakrs",akses.getkontakrs());
          param.put("emailrs",akses.getemailrs());   
          param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
          String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
          String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
          param.put("logo2", logoPath);
          finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",KdDokter.getText());
          param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+NmDokter.getText()+"\nID "+(finger.equals("")?KdDokter.getText():finger)+"\n"+DTPTgl.getSelectedItem());  
          Valid.MyReportqry("rptSebabKematian.jasper","report","::[ Surat Kematian ]::",                            
                            "SELECT pm.no_surat AS NoSurat, " +
                            "p.no_rkm_medis AS NoRekamMedis, " +
                            "p.nm_pasien AS NamaPasien, " +
                            "p.no_ktp AS NoKTP, " +
                            "CASE WHEN p.jk ='L' THEN 'Laki-laki' ELSE 'Perempuan' END AS JenisKelamin, " +
                            "CONCAT(p.tmp_lahir, ', ', DATE_FORMAT(p.tgl_lahir, '%d %M %Y')) AS TempatdanTanggalLahir, " +
                            "p.alamat AS Alamat, " +
                            "CASE WHEN rp.status_lanjut = 'Ralan' THEN DATE_FORMAT(rp.tgl_registrasi, '%d %M %Y') ELSE DATE_FORMAT(ki.tgl_masuk, '%d %M %Y') END AS TanggalMasukRS, " +
                            "CASE "+
                            "    WHEN rp.status_lanjut = 'Ralan' "+
                            "        THEN poliklinik.nm_poli "+
                            "    ELSE "+
                            "        (SELECT bangsal.nm_bangsal "+
                            "         FROM kamar_inap "+
                            "         JOIN kamar ON kamar.kd_kamar = kamar_inap.kd_kamar "+
                            "         JOIN bangsal ON bangsal.kd_bangsal = kamar.kd_bangsal "+
                            "         WHERE kamar_inap.no_rawat = rp.no_rawat "+
                            "           AND kamar_inap.stts_pulang = 'Meninggal' "+
                            "         LIMIT 1) "+
                            "END AS Ruangan, "+
                            "CASE WHEN rp.status_lanjut = 'Ralan' THEN rp.jam_reg ELSE ki.jam_masuk END AS JamMasukRS, " +
                            "DATE_FORMAT(pm.tanggal, '%d %M %Y') AS TanggalMeninggal, " +
                            "pm.jam AS JamMeninggal, " +
                            "CASE " +
                            "    WHEN TIMESTAMPDIFF(YEAR, p.tgl_lahir, pm.tanggal) > 0 THEN CONCAT(TIMESTAMPDIFF(YEAR, p.tgl_lahir, pm.tanggal), ' Tahun') " +
                            "    WHEN TIMESTAMPDIFF(MONTH, p.tgl_lahir, pm.tanggal) > 0 THEN CONCAT(TIMESTAMPDIFF(MONTH, p.tgl_lahir, pm.tanggal), ' Bulan') " +
                            "    WHEN DATEDIFF(pm.tanggal, p.tgl_lahir) > 0 THEN CONCAT(DATEDIFF(pm.tanggal, p.tgl_lahir), ' Hari') " +
                            "    ELSE CONCAT(TIMESTAMPDIFF(HOUR, p.tgl_lahir, pm.tanggal), ' Jam') " +
                            "END AS UmurSaatMeninggal, " +
                            "pm.lahir_mati AS LahirMati, " +
                            "pm.doa AS Doa, " +
                            "CASE WHEN TIMESTAMPDIFF(HOUR, CASE WHEN rp.status_lanjut = 'Ralan' THEN CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) ELSE CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) END, CONCAT(pm.tanggal, ' ', pm.jam)) < 48 THEN CONCAT(TIMESTAMPDIFF(HOUR, CASE WHEN rp.status_lanjut = 'Ralan' THEN CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) ELSE CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) END, CONCAT(pm.tanggal, ' ', pm.jam)), ' Jam') ELSE CONCAT(TIMESTAMPDIFF(DAY, CASE WHEN rp.status_lanjut = 'Ralan' THEN CONCAT(rp.tgl_registrasi, ' ', rp.jam_reg) ELSE CONCAT(ki.tgl_masuk, ' ', ki.jam_masuk) END, CONCAT(pm.tanggal, ' ', pm.jam)), ' Hari') END AS LamaDirawatdiRS, " +
                            "DATE_FORMAT(pm.tgl_pembuatan, '%d %M %Y') AS TanggalPembuatan, " +
                            "pm.penerima AS KeluargaPasien, " +
                            "pm.hubungan_pasien AS HubunganKeluarga, " +
                            "d.nm_dokter AS NamaDokter, " +
                            "pm.penyebab_kematian AS SebabKematian, "+        
                            "pm.penyakit_lain AS PenyakitLain, "+        
                            "pm.penyebab_utama_bayi AS UtamaBayi, "+                
                            "pm.penyebab_lain_bayi AS LainBayi, "+                        
                            "pm.penyebab_utama_ibu AS UtamaIbu, "+                
                            "pm.penyebab_lain_ibu AS LainIbu, "+                                
                            "CASE " +
                            "    WHEN TIMESTAMPDIFF(YEAR, p.tgl_lahir, pm.tanggal) BETWEEN 10 AND 59 " +
                            "         AND p.jk = 'P' THEN " +
                            "         CASE " +
                            "             WHEN pm.keadaan_meninggal = 'Lainnya' THEN pm.lainnya " +
                            "             ELSE pm.keadaan_meninggal " +
                            "         END " +
                            "    ELSE '-' " +
                            "END AS KeadaanMeninggal " +
                            "FROM pasien_meninggal pm " +
                            "INNER JOIN dokter d on pm.kd_dokter=d.kd_dokter " +        
                            "INNER JOIN pasien p ON pm.no_rkm_medis = p.no_rkm_medis " +
                            "INNER JOIN reg_periksa rp ON pm.no_rawat = rp.no_rawat " +
                            "LEFT JOIN kamar_inap ki ON rp.no_rawat = ki.no_rawat " +
                            "LEFT JOIN poliklinik ON poliklinik.kd_poli = rp.kd_poli "+                            
                            "LEFT JOIN kamar ON kamar.kd_kamar = ki.kd_kamar " +
                            "LEFT JOIN bangsal ON bangsal.kd_bangsal = kamar.kd_bangsal " +
                            "where pm.no_rkm_medis='"+TNoRM.getText()+"' ",param);
      }
    }//GEN-LAST:event_MnCetakSuratSebabKematianActionPerformed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.isCek();
        dokter.TCari.requestFocus();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnDokterActionPerformed(null);
        }else{
            Valid.pindah(evt,lainnya,BtnSimpan);
        }
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void no_rawatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_no_rawatKeyPressed
        Valid.pindah(evt,TCari,DTPTgl);
    }//GEN-LAST:event_no_rawatKeyPressed

    private void DTPTglItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPTglItemStateChanged
//        nomorsurat();        // TODO add your handling code here:
    }//GEN-LAST:event_DTPTglItemStateChanged

    private void lahir_matiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lahir_matiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_lahir_matiKeyPressed

    private void keadaan_meninggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keadaan_meninggalKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_keadaan_meninggalKeyPressed

    private void penyebab_kematianKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_penyebab_kematianKeyPressed
        Valid.pindah2(evt,diagnosa_sekunder,penyakit_lain);
    }//GEN-LAST:event_penyebab_kematianKeyPressed

    private void penyakit_lainKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_penyakit_lainKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_penyakit_lainKeyPressed

    private void penyebab_utama_bayiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_penyebab_utama_bayiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_penyebab_utama_bayiKeyPressed

    private void penyebab_utama_ibuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_penyebab_utama_ibuKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_penyebab_utama_ibuKeyPressed

    private void penyebab_lain_ibuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_penyebab_lain_ibuKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_penyebab_lain_ibuKeyPressed

    private void penerimaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_penerimaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_penerimaKeyPressed

    private void hubungan_pasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hubungan_pasienKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_hubungan_pasienKeyPressed

    private void no_suratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_no_suratKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_no_suratKeyPressed

    private void tgl_pembuatanItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tgl_pembuatanItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tgl_pembuatanItemStateChanged

    private void tgl_pembuatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tgl_pembuatanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tgl_pembuatanKeyPressed

    private void petugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_petugasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_petugasKeyPressed

    private void BtnHapus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapus1ActionPerformed
    if (TNoRM.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
        Valid.textKosong(TNoRM, "No. RM Pasien");
    } else if (no_surat.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(null, "No. Surat tidak boleh kosong.");
    } else {
            try {
                koneksi.setAutoCommit(false);

                // Validasi: Pastikan TNoRM valid dan petugas yang membuat data
                String petugasSebelumnya = Sequel.cariIsi(
                    "SELECT petugas FROM pasien_meninggal WHERE no_surat=?", 
                    no_surat.getText()
                );

                if (petugasSebelumnya == null || petugasSebelumnya.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Data pasien tidak ditemukan di database.");
                } else if (!petugasSebelumnya.equals(akses.getkode())) {
                    JOptionPane.showMessageDialog(null, "Maaf, hanya petugas yang membuat data ini yang dapat mengeditnya.");
                } else {
                    // Lakukan update
                    String update = 
                          "tanggal=?, " +
                          "jam=?, " +
                          "lahir_mati=?, " +
                          "doa=?, " +
                          "keadaan_meninggal=?, " +
                          "lainnya=?, " +
                          "diagnosa_utama=?, " +
                          "diagnosa_sekunder=?, " +
                          "penyebab_kematian=?, " +
                          "penyakit_lain=?, " +
                          "penyebab_utama_bayi=?, " +
                          "penyebab_lain_bayi=?, " +
                          "penyebab_utama_ibu=?, " +
                          "penyebab_lain_ibu=?, " +
                          "kd_dokter=?, " +
                          "penerima=?, " +
                          "hubungan_pasien=?";

                    // Gunakan prepared statement untuk menghindari error
                    ps = koneksi.prepareStatement(
                        "UPDATE pasien_meninggal SET " + update + " WHERE no_surat=?"
                    );

                    // Isi parameter untuk prepared statement
                    ps.setString(1, Valid.SetTgl(DTPTgl.getSelectedItem() + ""));
                    ps.setString(2, cmbJam.getSelectedItem() + ":" + cmbMnt.getSelectedItem() + ":" + cmbDtk.getSelectedItem());
                    ps.setString(3, lahir_mati.getSelectedItem().toString());
                    ps.setString(4, doa.getSelectedItem().toString());
                    ps.setString(5, keadaan_meninggal.getSelectedItem().toString());
                    ps.setString(6, lainnya.getText());
                    ps.setString(7, diagnosa_utama.getText());
                    ps.setString(8, diagnosa_sekunder.getText());
                    ps.setString(9, penyebab_kematian.getText());
                    ps.setString(10, penyakit_lain.getText());
                    ps.setString(11, penyebab_utama_bayi.getText());
                    ps.setString(12, penyebab_lain_bayi.getText());
                    ps.setString(13, penyebab_utama_ibu.getText());
                    ps.setString(14, penyebab_lain_ibu.getText());
                    ps.setString(15, KdDokter.getText());
                    ps.setString(16, penerima.getText());
                    ps.setString(17, hubungan_pasien.getSelectedItem().toString());
                    ps.setString(18, no_surat.getText()); // Parameter terakhir adalah TNoRM

                    // Eksekusi update
                    int rowsUpdated = ps.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(null, "Data berhasil diperbarui.");
                        tampil();
                        emptTeks();
                    } else {
                        JOptionPane.showMessageDialog(null, "Gagal memperbarui data.");
                    }
                }

                koneksi.commit();
            } catch (Exception e) {
                try {
                    koneksi.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error rollback: " + ex);
                }
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
            } finally {
                try {
                    koneksi.setAutoCommit(true);
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException ex) {
                    System.out.println("Error set auto commit: " + ex);
                }
            }
        }
    }//GEN-LAST:event_BtnHapus1ActionPerformed

    private void BtnHapus1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapus1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnHapus1KeyPressed

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
    if(tbMati.getSelectedRow() != -1){ // Periksa apakah ada baris yang dipilih di tbMati
        isPhoto(); // Panggil fungsi untuk memproses foto
        panggilPhoto(); // Panggil fungsi untuk menampilkan foto
    } else {
        ChkAccor.setSelected(false); // Batalkan pilihan jika tidak ada data yang dipilih
        JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
    }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void btnAmbilPhotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAmbilPhotoActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(NoRekamMedisDipilih.getText().equals("")||NamaPasienDipilih.getText().equals("")){
           JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data pasien terlebih dahulu...!!!!"); 
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Sequel.menyimpan2("personal_pasien","?,'',''",1,new String[]{NoRekamMedisDipilih.getText()});
            Valid.panggilUrl("photopasien/login.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&norm="+NoRekamMedisDipilih.getText());
            this.setCursor(Cursor.getDefaultCursor()); 
        }   
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnAmbilPhotoActionPerformed

    private void BtnRefreshPhotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRefreshPhotoActionPerformed
        panggilPhoto();
    }//GEN-LAST:event_BtnRefreshPhotoActionPerformed

    private void btnUbahPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahPasswordActionPerformed
        if(NoRekamMedisDipilih.getText().equals("")||NamaPasienDipilih.getText().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data pasien terlebih dahulu...!!!!");
        }else{
            if(Sequel.menyimpantf("personal_pasien","?,'',aes_encrypt(?,'windi')",2,new String[]{NoRekamMedisDipilih.getText(),PasswordPasien.getText()},"no_rkm_medis=?","password=aes_encrypt(?,'windi')",2,new String[]{PasswordPasien.getText(),NoRekamMedisDipilih.getText()})==true){
                JOptionPane.showMessageDialog(null,"Update password pasien berhasil..!!");
            }else{
                JOptionPane.showMessageDialog(null,"Update password pasien gagal..!!");
                PasswordPasien.requestFocus();
            }
        }
    }//GEN-LAST:event_btnUbahPasswordActionPerformed

    private void PasswordPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PasswordPasienKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PasswordPasienKeyPressed

    private void tbMatiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbMatiMouseClicked
    if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if((evt.getClickCount()==2)&&(tbMati.getSelectedColumn()==1)){
//                TabRawat.setSelectedIndex(0);
            }
        }      
    }//GEN-LAST:event_tbMatiMouseClicked

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgPasienMati dialog = new DlgPasienMati(new javax.swing.JFrame(), true);
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
    private widget.Button BtnHapus;
    private widget.Button BtnHapus1;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnRefreshPhoto;
    private widget.Button BtnSeek;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkAccor;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPTgl;
    private widget.panelisi FormInput;
    private widget.PanelBiasa FormMenu;
    private widget.PanelBiasa FormPass;
    private widget.PanelBiasa FormPass1;
    private widget.PanelBiasa FormPass2;
    private widget.PanelBiasa FormPhoto;
    private widget.PanelBiasa FormPhotoPass;
    private widget.TextBox KdDokter;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private javax.swing.JMenuItem MnBerkasDigitalKeperawatan;
    private javax.swing.JMenuItem MnCetakSuratKematian;
    private javax.swing.JMenuItem MnCetakSuratSebabKematian;
    private widget.TextBox NamaPasienDipilih;
    private widget.TextBox NmDokter;
    private widget.TextBox NoRekamMedisDipilih;
    private widget.PanelBiasa PanelAccor;
    private javax.swing.JPanel PanelInput;
    private widget.TextArea PasswordPasien;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll4;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TPasien;
    private widget.Button btnAmbilPhoto;
    private widget.Button btnUbahPassword;
    private widget.ComboBox cmbDtk;
    private widget.ComboBox cmbJam;
    private widget.ComboBox cmbMnt;
    private widget.TextBox diagnosa_sekunder;
    private widget.TextBox diagnosa_utama;
    private widget.ComboBox doa;
    private widget.ComboBox hubungan_pasien;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
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
    private widget.Label jLabel40;
    private widget.Label jLabel42;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.ComboBox keadaan_meninggal;
    private widget.ComboBox lahir_mati;
    private widget.TextBox lainnya;
    private widget.TextBox no_rawat;
    private widget.TextBox no_surat;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.TextBox penerima;
    private widget.TextArea penyakit_lain;
    private widget.TextArea penyebab_kematian;
    private widget.TextBox penyebab_lain_bayi;
    private widget.TextBox penyebab_lain_ibu;
    private widget.TextBox penyebab_utama_bayi;
    private widget.TextBox penyebab_utama_ibu;
    private widget.TextBox petugas;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.Table tbMati;
    private widget.Tanggal tgl_pembuatan;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
                                     "select pasien_meninggal.no_surat," //0
                                   + "pasien_meninggal.tanggal," //1
                                   + "pasien_meninggal.jam," //2
                                   + "pasien_meninggal.no_rawat," //3
                                   + "pasien_meninggal.no_rkm_medis," //4
                                   + "pasien.nm_pasien," //5
                                   + "pasien_meninggal.lahir_mati," //6
                                   + "pasien_meninggal.doa," //7
                                   + "pasien_meninggal.keadaan_meninggal," //8
                                   + "pasien_meninggal.lainnya," //9
                                   + "pasien_meninggal.diagnosa_utama," //10
                                   + "pasien_meninggal.diagnosa_sekunder," //11
                                   + "pasien_meninggal.penyebab_kematian," //12
                                   + "pasien_meninggal.penyakit_lain," //13
                                   + "pasien_meninggal.penyebab_utama_bayi," //14
                                   + "pasien_meninggal.penyebab_lain_bayi," //15
                                   + "pasien_meninggal.penyebab_utama_ibu," //16
                                   + "pasien_meninggal.penyebab_lain_ibu," //17
                                   + "pasien_meninggal.kd_dokter," //18
                                   + "dokter.nm_dokter," //19
                                   + "pasien_meninggal.penerima," //20
                                   + "pasien_meninggal.hubungan_pasien," //21
                                   + "pasien_meninggal.tgl_pembuatan," //22
                                   + "pasien_meninggal.petugas " //23
                                   + "from pasien_meninggal " 
                                   + "inner join pasien on pasien_meninggal.no_rkm_medis=pasien.no_rkm_medis "
                                   + "inner join dokter on pasien_meninggal.kd_dokter=dokter.kd_dokter "+
                   (TCari.getText().trim().equals("")?"":
                   "where pasien_meninggal.tanggal like '%"+TCari.getText().trim()+"%' or "+
                   "pasien_meninggal.no_rkm_medis like '%"+TCari.getText().trim()+"%' or "+
                   "pasien.nm_pasien like '%"+TCari.getText().trim()+"%' or "+
                   "pasien_meninggal.petugas like '%"+TCari.getText().trim()+"%' or "+
                   "pasien_meninggal.no_rawat like '%"+TCari.getText().trim()+"%' or "+
                   "pasien_meninggal.kd_dokter like '%"+TCari.getText().trim()+"%' or "+
                   "dokter.nm_dokter like '%"+TCari.getText().trim()+"%' or "+
                   "pasien_meninggal.diagnosa_utama like '%"+TCari.getText().trim()+"%' or "+
                   "pasien_meninggal.diagnosa_sekunder like '%"+TCari.getText().trim()+"%' ")+
                   "order by pasien_meninggal.no_surat ");
            try {
                rs=ps.executeQuery();
                while(rs.next()){               
                    tabMode.addRow(new Object[]{
                        rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                        rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),
                        rs.getString(9),rs.getString(10),rs.getString(11),rs.getString(12),
                        rs.getString(13),rs.getString(14),rs.getString(15),rs.getString(16),
                        rs.getString(17),rs.getString(18),rs.getString(19),rs.getString(20),
                        rs.getString(21),rs.getString(22),rs.getString(23),rs.getString(24)
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
        }catch(SQLException e){
            System.out.println("Notifikasi : "+e);
        }
        int b=tabMode.getRowCount();
        LCount.setText(""+b);
    }

    public void emptTeks() {
        no_surat.setText("");
        TNoRM.setText("");  
        TPasien.setText("");
        no_rawat.setText("");
        lahir_mati.setSelectedItem("-");
        doa.setSelectedItem("-");
        lainnya.setText("");
        diagnosa_utama.setText("");
        diagnosa_sekunder.setText("");
        KdDokter.setText("");
        NmDokter.setText("");
        penyebab_kematian.setText("");
        penyakit_lain.setText("-");
        penyebab_utama_bayi.setText("-");
        penyebab_lain_bayi.setText("-");
        penyebab_utama_ibu.setText("-");
        penyebab_lain_ibu.setText("-");                       
        penerima.setText("");
        hubungan_pasien.setSelectedItem("-");
        DTPTgl.setDate(new Date());
        DTPTgl.requestFocus();        
    } 

    private void getData() {
        if(tbMati.getSelectedRow()!= -1){
            no_surat.setText(tbMati.getValueAt(tbMati.getSelectedRow(),0).toString());
            Valid.SetTgl(DTPTgl,tbMati.getValueAt(tbMati.getSelectedRow(),1).toString());
            cmbJam.setSelectedItem(tbMati.getValueAt(tbMati.getSelectedRow(),2).toString().substring(0,2));
            cmbMnt.setSelectedItem(tbMati.getValueAt(tbMati.getSelectedRow(),2).toString().substring(3,5));
            cmbDtk.setSelectedItem(tbMati.getValueAt(tbMati.getSelectedRow(),2).toString().substring(6,8));
            no_rawat.setText(tbMati.getValueAt(tbMati.getSelectedRow(),3).toString());            
            TNoRM.setText(tbMati.getValueAt(tbMati.getSelectedRow(),4).toString());
            TPasien.setText(tbMati.getValueAt(tbMati.getSelectedRow(),5).toString());
            lahir_mati.setSelectedItem(tbMati.getValueAt(tbMati.getSelectedRow(),6).toString());
            doa.setSelectedItem(tbMati.getValueAt(tbMati.getSelectedRow(),7).toString());
            keadaan_meninggal.setSelectedItem(tbMati.getValueAt(tbMati.getSelectedRow(),8).toString());
            lainnya.setText(tbMati.getValueAt(tbMati.getSelectedRow(),9).toString());                        
            diagnosa_utama.setText(tbMati.getValueAt(tbMati.getSelectedRow(),10).toString());
            diagnosa_sekunder.setText(tbMati.getValueAt(tbMati.getSelectedRow(),11).toString());
            penyebab_kematian.setText(tbMati.getValueAt(tbMati.getSelectedRow(),12).toString());
            penyakit_lain.setText(tbMati.getValueAt(tbMati.getSelectedRow(),13).toString());
            penyebab_utama_bayi.setText(tbMati.getValueAt(tbMati.getSelectedRow(),14).toString());
            penyebab_lain_bayi.setText(tbMati.getValueAt(tbMati.getSelectedRow(),15).toString());            
            penyebab_utama_ibu.setText(tbMati.getValueAt(tbMati.getSelectedRow(),16).toString());            
            penyebab_lain_ibu.setText(tbMati.getValueAt(tbMati.getSelectedRow(),17).toString());            
            KdDokter.setText(tbMati.getValueAt(tbMati.getSelectedRow(),18).toString());
            NmDokter.setText(tbMati.getValueAt(tbMati.getSelectedRow(),19).toString());            
            penerima.setText(tbMati.getValueAt(tbMati.getSelectedRow(), 20).toString());
            hubungan_pasien.setSelectedItem(tbMati.getValueAt(tbMati.getSelectedRow(),21).toString());
            Valid.SetTgl(tgl_pembuatan,tbMati.getValueAt(tbMati.getSelectedRow(),22).toString());
            petugas.setText(tbMati.getValueAt(tbMati.getSelectedRow(), 23).toString());
            NoRekamMedisDipilih.setText(tbMati.getValueAt(tbMati.getSelectedRow(),4).toString());
            NamaPasienDipilih.setText(tbMati.getValueAt(tbMati.getSelectedRow(),5).toString());
            
        }
    }
    
public JTable getTable(){
        return tbMati;
    }

    public void isCek(){
        BtnSimpan.setEnabled(akses.getpasien_meninggal());
        BtnHapus1.setEnabled(akses.getpasien_meninggal());
        BtnHapus.setEnabled(akses.getadmin());
        BtnPrint.setEnabled(akses.getpasien_meninggal());
    }
    
    public void setNoRm(String norm,String nama,String norawat) {
    TNoRM.setText(norm);  
    TPasien.setText(nama);
    no_rawat.setText(norawat);            
    ChkInput.setSelected(true);
    isForm();
    TCari.setText(norm);
    }    
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,330));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }
    
    private void nomorSurat(){
//    //MODIF MUSTAFA
    
    bln_angka = "";
    bln_romawi = "";

    bln_angka = DTPTgl.getSelectedItem().toString().substring(3, 5);

    switch (bln_angka) {
        case "01": bln_romawi = "I"; break;
        case "02": bln_romawi = "II"; break;
        case "03": bln_romawi = "III"; break;
        case "04": bln_romawi = "IV"; break;
        case "05": bln_romawi = "V"; break;
        case "06": bln_romawi = "VI"; break;
        case "07": bln_romawi = "VII"; break;
        case "08": bln_romawi = "VIII"; break;
        case "09": bln_romawi = "IX"; break;
        case "10": bln_romawi = "X"; break;
        case "11": bln_romawi = "XI"; break;
        case "12": bln_romawi = "XII"; break;
    }

    // Ambil nomor terakhir dari database
    String lastNumber = Sequel.cariIsi(
        "SELECT MAX(CONVERT(LEFT(no_surat, 3), SIGNED)) FROM pasien_meninggal WHERE " +
        "no_surat LIKE '%/MR.RSA/" + bln_romawi + "/" + tgl_pembuatan.getSelectedItem().toString().substring(6, 10) + "'"
    );

    int nextNumber = 1; // Default jika belum ada nomor di database
    if (lastNumber != null && !lastNumber.isEmpty()) {
        nextNumber = Integer.parseInt(lastNumber) + 1; // Tambahkan 1 dari nomor terakhir
    }

    // Format nomor surat baru
    nomorSuratGenerated = String.format("%03d", nextNumber) + "/MR.RSA/" + bln_romawi + "/" + 
                          tgl_pembuatan.getSelectedItem().toString().substring(6, 10);
    
    // END MODIF MUSTAFA
    }
    
    private void isPhoto(){
       if (ChkAccor.isSelected()) {
            PanelAccor.setPreferredSize(new Dimension(245, HEIGHT));
            FormMenu.setVisible(true);
            FormPhotoPass.setVisible(true);
        } else {
            PanelAccor.setPreferredSize(new Dimension(15, HEIGHT));
            FormMenu.setVisible(false);
            FormPhotoPass.setVisible(false);
        }
    }

    private void panggilPhoto() {
        if(FormPhotoPass.isVisible()==true){
            try {
                ps=koneksi.prepareStatement("select personal_pasien.gambar,aes_decrypt(personal_pasien.password,'windi') as password from personal_pasien where personal_pasien.no_rkm_medis=?");
                try {
                    ps.setString(1,NoRekamMedisDipilih.getText());
                    rs=ps.executeQuery();
                    if(rs.next()){
                        if(rs.getString("gambar").equals("")||rs.getString("gambar").equals("-")){
                            LoadHTML.setText("<html><body><center><br><br><font face='tahoma' size='2' color='#434343'>Kosong</font></center></body></html>");
                        }else{
                            LoadHTML.setText("<html><body><center><img src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/photopasien/"+rs.getString("gambar")+"' alt='photo' width='200' height='200'/></center></body></html>");
                        }  
                        PasswordPasien.setText(rs.getString("password"));
                    }else{
                        LoadHTML.setText("<html><body><center><br><br><font face='tahoma' size='2' color='#434343'>Kosong</font></center></body></html>");
                        PasswordPasien.setText("");
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
    
public int hitungUmur(String tglLahir) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate tanggalLahir = LocalDate.parse(tglLahir, formatter);
    LocalDate tanggalSekarang = LocalDate.now();
    return Period.between(tanggalLahir, tanggalSekarang).getYears();
}

private int hitungUmurDalamHari(String tglLahir) {
    LocalDate tanggalLahir = LocalDate.parse(tglLahir); // Pastikan format tglLahir sesuai dengan format yang diterima
    LocalDate today = LocalDate.now();
    return (int) ChronoUnit.DAYS.between(tanggalLahir, today); // Menghitung selisih hari
}

private void updateLabelVisibility() {
    String tglLahirPasien = Sequel.cariIsi("select pasien.tgl_lahir from pasien where pasien.no_rkm_medis=?", TNoRM.getText());
    String jkPasien = Sequel.cariIsi("select pasien.jk from pasien where pasien.no_rkm_medis=?", TNoRM.getText());
    int umurPasien = hitungUmur(tglLahirPasien); // Hitung umur
    
    // Cek apakah kondisi usia dan jenis kelamin terpenuhi
    boolean tampilkanLabel = umurPasien >= 10 && umurPasien <= 59 && "P".equals(jkPasien);
    
    // Set visibilitas dan enable/disable label berdasarkan kondisi
    jLabel21.setVisible(tampilkanLabel);
    jLabel21.setEnabled(tampilkanLabel);
    
    jLabel22.setVisible(tampilkanLabel);
    jLabel22.setEnabled(tampilkanLabel);  
}

private void updateComboBoxVisibility() {
    String tglLahirPasien = Sequel.cariIsi("select pasien.tgl_lahir from pasien where pasien.no_rkm_medis=?", TNoRM.getText());
    String jkPasien = Sequel.cariIsi("select pasien.jk from pasien where pasien.no_rkm_medis=?", TNoRM.getText());
    int umurPasien = hitungUmur(tglLahirPasien); // Hitung umur
    
    // Cek apakah kondisi usia dan jenis kelamin terpenuhi
    boolean tampilkanComboBox = umurPasien >= 10 && umurPasien <= 59 && "P".equals(jkPasien);
    
    // Set visibilitas dan enable/disable ComboBox berdasarkan kondisi
    keadaan_meninggal.setVisible(tampilkanComboBox);
    keadaan_meninggal.setEnabled(tampilkanComboBox);
    
//    lainnya.setVisible(tampilkanComboBox);
//    lainnya.setEnabled(tampilkanComboBox);    
}

private void updateComboBoxhariVisibility() {
    String tglLahirPasien = Sequel.cariIsi("select pasien.tgl_lahir from pasien where pasien.no_rkm_medis=?", TNoRM.getText());    
    
    // Hitung umur dalam hari
    int umurPasien = hitungUmurDalamHari(tglLahirPasien); 
    
    // Cek apakah umur pasien berada dalam rentang 0-6 hari
    boolean tampilkanComboBoxhari = umurPasien >= 0 && umurPasien <= 6;
    
    // Set visibilitas dan enable/disable ComboBox berdasarkan kondisi
    penyebab_utama_bayi.setVisible(tampilkanComboBoxhari);
    penyebab_utama_bayi.setEnabled(tampilkanComboBoxhari);
    
    penyebab_lain_bayi.setVisible(tampilkanComboBoxhari);
    penyebab_lain_bayi.setEnabled(tampilkanComboBoxhari);
    
    penyebab_utama_ibu.setVisible(tampilkanComboBoxhari);
    penyebab_utama_ibu.setEnabled(tampilkanComboBoxhari);
    
    penyebab_lain_ibu.setVisible(tampilkanComboBoxhari);
    penyebab_lain_ibu.setEnabled(tampilkanComboBoxhari);   
    
}

private void updatelabelhariVisibility() {
    String tglLahirPasien = Sequel.cariIsi("select pasien.tgl_lahir from pasien where pasien.no_rkm_medis=?", TNoRM.getText());    
    
    // Hitung umur dalam hari
    int umurPasien = hitungUmurDalamHari(tglLahirPasien); 
    
    // Cek apakah umur pasien berada dalam rentang 0-6 hari
    boolean tampilkanLabelhari = umurPasien >= 0 && umurPasien <= 6;
    
    // Set visibilitas dan enable/disable ComboBox berdasarkan kondisi
    jLabel23.setVisible(tampilkanLabelhari);
    jLabel23.setEnabled(tampilkanLabelhari);
    
    jLabel34.setVisible(tampilkanLabelhari);
    jLabel34.setEnabled(tampilkanLabelhari);
    
    jLabel31.setVisible(tampilkanLabelhari);
    jLabel31.setEnabled(tampilkanLabelhari);
    
    jLabel35.setVisible(tampilkanLabelhari);
    jLabel35.setEnabled(tampilkanLabelhari);
    
    jLabel32.setVisible(tampilkanLabelhari);
    jLabel32.setEnabled(tampilkanLabelhari);
    
    jLabel36.setVisible(tampilkanLabelhari);
    jLabel36.setEnabled(tampilkanLabelhari);
    
    jLabel33.setVisible(tampilkanLabelhari);
    jLabel33.setEnabled(tampilkanLabelhari);
    
    jLabel37.setVisible(tampilkanLabelhari);
    jLabel37.setEnabled(tampilkanLabelhari);
    
    jLabel24.setVisible(tampilkanLabelhari);
    jLabel24.setEnabled(tampilkanLabelhari);
}
}
