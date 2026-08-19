/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgRujuk.java
 *
 * Created on 31 Mei 10, 20:19:56
 */

package rekammedis;

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
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import laporan.DlgBerkasRawat;
import laporan.DlgCariPenyakit;
import laporan.DlgDiagnosaPenyakit;


/**
 *
 * @author perpustakaan
 */
public final class DlgOdontogram extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;    
    private String bagian;
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private RMCariKeluhan carikeluhan=new RMCariKeluhan(null,false);
    private RMCariPemeriksaan caripemeriksaan=new RMCariPemeriksaan(null,false);
    private RMCariHasilRadiologi cariradiologi=new RMCariHasilRadiologi(null,false);
    private RMCariHasilLaborat carilaborat=new RMCariHasilLaborat(null,false);
    private RMCariJumlahObat cariobat=new RMCariJumlahObat(null,false);
    private DlgDiagnosaPenyakit penyakit=new DlgDiagnosaPenyakit(null,false);
    private RMCariDiagnosa1 rmcaridiagnosa1=new RMCariDiagnosa1(null,false);
    private RMCariDiagnosa2 rmcaridiagnosa2=new RMCariDiagnosa2(null,false);
    private RMCariDiagnosa3 rmcaridiagnosa3=new RMCariDiagnosa3(null,false);
    private RMCariDiagnosa4 rmcaridiagnosa4=new RMCariDiagnosa4(null,false);
    private RMCariDiagnosa5 rmcaridiagnosa5=new RMCariDiagnosa5(null,false);
    private RMCariProsedur1 rmcariprosedur1=new RMCariProsedur1(null,false);
    private RMCariProsedur2 rmcariprosedur2=new RMCariProsedur2(null,false);
    private RMCariProsedur3 rmcariprosedur3=new RMCariProsedur3(null,false);
    private RMCariProsedur4 rmcariprosedur4=new RMCariProsedur4(null,false);
    private RMCariRencana carirencana=new RMCariRencana(null,false);
    private RMCariTerapi cariterapi=new RMCariTerapi(null,false);
    private DlgCariPenyakit penyakit1=new DlgCariPenyakit(null,false);
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public DlgOdontogram(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        tabMode=new DefaultTableModel(null,new Object[]{
            "Tgl.Rawat","Status","No.Rawat","No.RM","Nama Pasien","Kode Dokter","Dokter Gigi","Bagian Gigi","Diagnosa Gigi","ICD10",
            "Hasil Pemeriksaan","Catatan Pemeriksaan","Tanggal Permintaan","No Permintaan","Rahang"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 15; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(65);
            }else if(i==1){
                column.setPreferredWidth(100);
            }else if(i==2){
                column.setPreferredWidth(105);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(150);
            }else if(i==5){
                column.setPreferredWidth(90);
            }else if(i==6){
                column.setPreferredWidth(150);
            }else if(i==7){
                column.setPreferredWidth(60);
            }else if(i==8){
                column.setPreferredWidth(250);
            }else if(i==9){
                column.setPreferredWidth(250);
            }else if(i==10){
                column.setPreferredWidth(250);
            }else if(i==11){
                column.setPreferredWidth(250);
            }else if(i==12){
                column.setPreferredWidth(250);
            }else if(i==13){
                column.setPreferredWidth(250);
            }else if(i==14){
                column.setPreferredWidth(250);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoPermintaan.setDocument(new batasInput((byte)15).getKata(TNoPermintaan));
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        DiagnosaUtama.setDocument(new batasInput((int)80).getKata(DiagnosaUtama));
        KodeDiagnosaUtama.setDocument(new batasInput((int)10).getKata(KodeDiagnosaUtama));
        Catatan.setDocument(new batasInput((int)1000).getKata(Catatan));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        
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
                    KodeDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NamaDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    KodeDokter.requestFocus();
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
        
        cariobat.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(cariobat.getTable().getSelectedRow()!= -1){
                    Catatan.append(cariobat.getTable().getValueAt(cariobat.getTable().getSelectedRow(),2).toString()+", ");
                    Catatan.requestFocus();
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
        
        penyakit.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                tampil();
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
        
        rmcaridiagnosa1.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(rmcaridiagnosa1.getTable().getSelectedRow()!= -1){
                    KodeDiagnosaUtama.setText(rmcaridiagnosa1.getTable().getValueAt(rmcaridiagnosa1.getTable().getSelectedRow(),0).toString());
                    DiagnosaUtama.setText(rmcaridiagnosa1.getTable().getValueAt(rmcaridiagnosa1.getTable().getSelectedRow(),1).toString());
                    KodeDiagnosaUtama.requestFocus();
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
        
        carirencana.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(carirencana.getTable().getSelectedRow()!= -1){
                    Catatan.append(carirencana.getTable().getValueAt(carirencana.getTable().getSelectedRow(),2).toString()+", ");
                    Catatan.requestFocus();
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
                    KodeDiagnosaUtama.setText(penyakit1.getTable().getValueAt(penyakit1.getTable().getSelectedRow(),0).toString());
                    DiagnosaUtama.setText(penyakit1.getTable().getValueAt(penyakit1.getTable().getSelectedRow(),1).toString());
                }  
                DiagnosaUtama.requestFocus();
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
        
        ChkInput.setSelected(false);
        isForm();
      
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
        MnLaporanResume = new javax.swing.JMenuItem();
        MnInputDiagnosa = new javax.swing.JMenuItem();
        ppBerkasDigital = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
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
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        DiagnosaUtama = new widget.TextBox();
        jLabel27 = new widget.Label();
        jLabel5 = new widget.Label();
        jLabel11 = new widget.Label();
        scrollPane6 = new widget.ScrollPane();
        Catatan = new widget.TextArea();
        jLabel30 = new widget.Label();
        jLabel31 = new widget.Label();
        KodeDiagnosaUtama = new widget.TextBox();
        KodeDokter = new widget.TextBox();
        NamaDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        BtnDokter6 = new widget.Button();
        PanelWall = new usu.widget.glass.PanelGlass();
        PanelWall1 = new usu.widget.glass.PanelGlass();
        PanelWall2 = new usu.widget.glass.PanelGlass();
        PanelWall3 = new usu.widget.glass.PanelGlass();
        PanelWall4 = new usu.widget.glass.PanelGlass();
        PanelWall5 = new usu.widget.glass.PanelGlass();
        PanelWall6 = new usu.widget.glass.PanelGlass();
        PanelWall7 = new usu.widget.glass.PanelGlass();
        PanelWall10 = new usu.widget.glass.PanelGlass();
        PanelWall9 = new usu.widget.glass.PanelGlass();
        PanelWall8 = new usu.widget.glass.PanelGlass();
        PanelWall11 = new usu.widget.glass.PanelGlass();
        PanelWall12 = new usu.widget.glass.PanelGlass();
        PanelWall13 = new usu.widget.glass.PanelGlass();
        PanelWall14 = new usu.widget.glass.PanelGlass();
        PanelWall15 = new usu.widget.glass.PanelGlass();
        PanelWall16 = new usu.widget.glass.PanelGlass();
        PanelWall17 = new usu.widget.glass.PanelGlass();
        PanelWall18 = new usu.widget.glass.PanelGlass();
        PanelWall19 = new usu.widget.glass.PanelGlass();
        PanelWall20 = new usu.widget.glass.PanelGlass();
        PanelWall21 = new usu.widget.glass.PanelGlass();
        PanelWall22 = new usu.widget.glass.PanelGlass();
        PanelWall23 = new usu.widget.glass.PanelGlass();
        PanelWall24 = new usu.widget.glass.PanelGlass();
        PanelWall25 = new usu.widget.glass.PanelGlass();
        PanelWall26 = new usu.widget.glass.PanelGlass();
        PanelWall27 = new usu.widget.glass.PanelGlass();
        PanelWall28 = new usu.widget.glass.PanelGlass();
        PanelWall29 = new usu.widget.glass.PanelGlass();
        PanelWall30 = new usu.widget.glass.PanelGlass();
        PanelWall31 = new usu.widget.glass.PanelGlass();
        PanelWall32 = new usu.widget.glass.PanelGlass();
        PanelWall33 = new usu.widget.glass.PanelGlass();
        PanelWall34 = new usu.widget.glass.PanelGlass();
        PanelWall35 = new usu.widget.glass.PanelGlass();
        PanelWall36 = new usu.widget.glass.PanelGlass();
        PanelWall37 = new usu.widget.glass.PanelGlass();
        PanelWall38 = new usu.widget.glass.PanelGlass();
        PanelWall39 = new usu.widget.glass.PanelGlass();
        PanelWall40 = new usu.widget.glass.PanelGlass();
        PanelWall41 = new usu.widget.glass.PanelGlass();
        PanelWall42 = new usu.widget.glass.PanelGlass();
        PanelWall43 = new usu.widget.glass.PanelGlass();
        PanelWall44 = new usu.widget.glass.PanelGlass();
        PanelWall45 = new usu.widget.glass.PanelGlass();
        PanelWall46 = new usu.widget.glass.PanelGlass();
        PanelWall47 = new usu.widget.glass.PanelGlass();
        PanelWall48 = new usu.widget.glass.PanelGlass();
        PanelWall49 = new usu.widget.glass.PanelGlass();
        PanelWall50 = new usu.widget.glass.PanelGlass();
        PanelWall51 = new usu.widget.glass.PanelGlass();
        label15 = new widget.Label();
        gigi18 = new widget.RadioButton();
        gigi17 = new widget.RadioButton();
        gigi28 = new widget.RadioButton();
        gigi16 = new widget.RadioButton();
        gigi15 = new widget.RadioButton();
        gigi14 = new widget.RadioButton();
        gigi13 = new widget.RadioButton();
        gigi12 = new widget.RadioButton();
        gigi11 = new widget.RadioButton();
        gigi21 = new widget.RadioButton();
        gigi22 = new widget.RadioButton();
        gigi23 = new widget.RadioButton();
        gigi24 = new widget.RadioButton();
        gigi25 = new widget.RadioButton();
        gigi26 = new widget.RadioButton();
        gigi27 = new widget.RadioButton();
        gigi31 = new widget.RadioButton();
        gigi32 = new widget.RadioButton();
        gigi33 = new widget.RadioButton();
        gigi34 = new widget.RadioButton();
        gigi35 = new widget.RadioButton();
        gigi36 = new widget.RadioButton();
        gigi37 = new widget.RadioButton();
        gigi38 = new widget.RadioButton();
        gigi41 = new widget.RadioButton();
        gigi42 = new widget.RadioButton();
        gigi43 = new widget.RadioButton();
        gigi44 = new widget.RadioButton();
        gigi45 = new widget.RadioButton();
        gigi46 = new widget.RadioButton();
        gigi47 = new widget.RadioButton();
        gigi48 = new widget.RadioButton();
        gigi51 = new widget.RadioButton();
        gigi52 = new widget.RadioButton();
        gigi53 = new widget.RadioButton();
        gigi54 = new widget.RadioButton();
        gigi55 = new widget.RadioButton();
        gigi61 = new widget.RadioButton();
        gigi62 = new widget.RadioButton();
        gigi63 = new widget.RadioButton();
        gigi64 = new widget.RadioButton();
        gigi65 = new widget.RadioButton();
        gigi71 = new widget.RadioButton();
        gigi72 = new widget.RadioButton();
        gigi73 = new widget.RadioButton();
        gigi74 = new widget.RadioButton();
        gigi75 = new widget.RadioButton();
        gigi81 = new widget.RadioButton();
        gigi82 = new widget.RadioButton();
        gigi83 = new widget.RadioButton();
        gigi84 = new widget.RadioButton();
        gigi85 = new widget.RadioButton();
        Hasil = new widget.ComboBox();
        jLabel4 = new widget.Label();
        TNoPermintaan = new widget.TextBox();
        jLabel15 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jLabel32 = new widget.Label();
        Rahang = new widget.ComboBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnLaporanResume.setBackground(new java.awt.Color(255, 255, 254));
        MnLaporanResume.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnLaporanResume.setForeground(new java.awt.Color(50, 50, 50));
        MnLaporanResume.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnLaporanResume.setText("Laporan Resume Pasien");
        MnLaporanResume.setName("MnLaporanResume"); // NOI18N
        MnLaporanResume.setPreferredSize(new java.awt.Dimension(220, 26));
        MnLaporanResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnLaporanResumeActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnLaporanResume);

        MnInputDiagnosa.setBackground(new java.awt.Color(255, 255, 254));
        MnInputDiagnosa.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnInputDiagnosa.setForeground(new java.awt.Color(50, 50, 50));
        MnInputDiagnosa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnInputDiagnosa.setText("Input Diagnosa Pasien");
        MnInputDiagnosa.setName("MnInputDiagnosa"); // NOI18N
        MnInputDiagnosa.setPreferredSize(new java.awt.Dimension(220, 26));
        MnInputDiagnosa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnInputDiagnosaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnInputDiagnosa);

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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pemeriksaan Odontogram ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 10), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

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
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Rawat :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(67, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-06-2021" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-06-2021" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(310, 23));
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

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 390));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
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

        scrollInput.setName("scrollInput"); // NOI18N

        FormInput.setBackground(new java.awt.Color(250, 255, 245));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 350));
        FormInput.setLayout(null);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(104, 10, 141, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(361, 10, 424, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(247, 10, 112, 23);

        DiagnosaUtama.setHighlighter(null);
        DiagnosaUtama.setName("DiagnosaUtama"); // NOI18N
        DiagnosaUtama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosaUtamaKeyPressed(evt);
            }
        });
        FormInput.add(DiagnosaUtama);
        DiagnosaUtama.setBounds(670, 150, 520, 23);

        jLabel27.setText("Diagnosa Gigi :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(540, 150, 120, 23);

        jLabel5.setText("No.Rawat :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(0, 10, 100, 23);

        jLabel11.setText("Catatan Pemeriksaan :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(540, 230, 120, 23);

        scrollPane6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane6.setName("scrollPane6"); // NOI18N

        Catatan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Catatan.setColumns(20);
        Catatan.setRows(5);
        Catatan.setName("Catatan"); // NOI18N
        Catatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CatatanKeyPressed(evt);
            }
        });
        scrollPane6.setViewportView(Catatan);

        FormInput.add(scrollPane6);
        scrollPane6.setBounds(670, 230, 541, 50);

        jLabel30.setText("Hasil Pemeriksaan :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(540, 190, 120, 23);

        jLabel31.setText("Kode ICD :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(1040, 130, 210, 23);

        KodeDiagnosaUtama.setHighlighter(null);
        KodeDiagnosaUtama.setName("KodeDiagnosaUtama"); // NOI18N
        KodeDiagnosaUtama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KodeDiagnosaUtamaKeyPressed(evt);
            }
        });
        FormInput.add(KodeDiagnosaUtama);
        KodeDiagnosaUtama.setBounds(1190, 150, 75, 23);

        KodeDokter.setEditable(false);
        KodeDokter.setName("KodeDokter"); // NOI18N
        KodeDokter.setPreferredSize(new java.awt.Dimension(80, 23));
        KodeDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KodeDokterKeyPressed(evt);
            }
        });
        FormInput.add(KodeDokter);
        KodeDokter.setBounds(104, 40, 141, 23);

        NamaDokter.setEditable(false);
        NamaDokter.setName("NamaDokter"); // NOI18N
        NamaDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NamaDokter);
        NamaDokter.setBounds(247, 40, 270, 23);

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
        BtnDokter.setBounds(519, 40, 28, 23);

        BtnDokter6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter6.setMnemonic('2');
        BtnDokter6.setToolTipText("Alt+2");
        BtnDokter6.setName("BtnDokter6"); // NOI18N
        BtnDokter6.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokter6ActionPerformed(evt);
            }
        });
        FormInput.add(BtnDokter6);
        BtnDokter6.setBounds(1270, 150, 28, 23);

        PanelWall.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall.setRound(false);
        PanelWall.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall.setLayout(null);
        FormInput.add(PanelWall);
        PanelWall.setBounds(290, 100, 40, 30);

        PanelWall1.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall1.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall1.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall1.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall1.setRound(false);
        PanelWall1.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall1.setLayout(null);
        FormInput.add(PanelWall1);
        PanelWall1.setBounds(10, 100, 40, 30);

        PanelWall2.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall2.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall2.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall2.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall2.setRound(false);
        PanelWall2.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall2.setLayout(null);
        FormInput.add(PanelWall2);
        PanelWall2.setBounds(50, 100, 40, 30);

        PanelWall3.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall3.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall3.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall3.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall3.setRound(false);
        PanelWall3.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall3.setLayout(null);
        FormInput.add(PanelWall3);
        PanelWall3.setBounds(90, 100, 40, 30);

        PanelWall4.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall4.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall4.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall4.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall4.setRound(false);
        PanelWall4.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall4.setLayout(null);
        FormInput.add(PanelWall4);
        PanelWall4.setBounds(130, 100, 40, 30);

        PanelWall5.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall5.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall5.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall5.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall5.setRound(false);
        PanelWall5.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall5.setLayout(null);
        FormInput.add(PanelWall5);
        PanelWall5.setBounds(170, 100, 40, 30);

        PanelWall6.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall6.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall6.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall6.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall6.setRound(false);
        PanelWall6.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall6.setLayout(null);
        FormInput.add(PanelWall6);
        PanelWall6.setBounds(210, 100, 40, 30);

        PanelWall7.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall7.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall7.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall7.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall7.setRound(false);
        PanelWall7.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall7.setLayout(null);
        FormInput.add(PanelWall7);
        PanelWall7.setBounds(250, 100, 40, 30);

        PanelWall10.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall10.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall10.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall10.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall10.setRound(false);
        PanelWall10.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall10.setLayout(null);
        FormInput.add(PanelWall10);
        PanelWall10.setBounds(410, 100, 40, 30);

        PanelWall9.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall9.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall9.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall9.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall9.setRound(false);
        PanelWall9.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall9.setLayout(null);
        FormInput.add(PanelWall9);
        PanelWall9.setBounds(370, 100, 40, 30);

        PanelWall8.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall8.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall8.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall8.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall8.setRound(false);
        PanelWall8.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall8.setLayout(null);
        FormInput.add(PanelWall8);
        PanelWall8.setBounds(330, 100, 40, 30);

        PanelWall11.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall11.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall11.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall11.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall11.setRound(false);
        PanelWall11.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall11.setLayout(null);
        FormInput.add(PanelWall11);
        PanelWall11.setBounds(450, 100, 40, 30);

        PanelWall12.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall12.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall12.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall12.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall12.setRound(false);
        PanelWall12.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall12.setLayout(null);
        FormInput.add(PanelWall12);
        PanelWall12.setBounds(490, 100, 40, 30);

        PanelWall13.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall13.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall13.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall13.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall13.setRound(false);
        PanelWall13.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall13.setLayout(null);
        FormInput.add(PanelWall13);
        PanelWall13.setBounds(530, 100, 40, 30);

        PanelWall14.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall14.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall14.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall14.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall14.setRound(false);
        PanelWall14.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall14.setLayout(null);
        FormInput.add(PanelWall14);
        PanelWall14.setBounds(570, 100, 40, 30);

        PanelWall15.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall15.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall15.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall15.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall15.setRound(false);
        PanelWall15.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall15.setLayout(null);
        FormInput.add(PanelWall15);
        PanelWall15.setBounds(610, 100, 40, 30);

        PanelWall16.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall16.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall16.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall16.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall16.setRound(false);
        PanelWall16.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall16.setLayout(null);
        FormInput.add(PanelWall16);
        PanelWall16.setBounds(210, 140, 40, 30);

        PanelWall17.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall17.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall17.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall17.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall17.setRound(false);
        PanelWall17.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall17.setLayout(null);
        FormInput.add(PanelWall17);
        PanelWall17.setBounds(250, 140, 40, 30);

        PanelWall18.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall18.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall18.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall18.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall18.setRound(false);
        PanelWall18.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall18.setLayout(null);
        FormInput.add(PanelWall18);
        PanelWall18.setBounds(290, 140, 40, 30);

        PanelWall19.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall19.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall19.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall19.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall19.setRound(false);
        PanelWall19.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall19.setLayout(null);
        FormInput.add(PanelWall19);
        PanelWall19.setBounds(330, 140, 40, 30);

        PanelWall20.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall20.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall20.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall20.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall20.setRound(false);
        PanelWall20.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall20.setLayout(null);
        FormInput.add(PanelWall20);
        PanelWall20.setBounds(370, 140, 40, 30);

        PanelWall21.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall21.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall21.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall21.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall21.setRound(false);
        PanelWall21.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall21.setLayout(null);
        FormInput.add(PanelWall21);
        PanelWall21.setBounds(410, 140, 40, 30);

        PanelWall22.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall22.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall22.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall22.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall22.setRound(false);
        PanelWall22.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall22.setLayout(null);
        FormInput.add(PanelWall22);
        PanelWall22.setBounds(450, 140, 40, 30);

        PanelWall23.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall23.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall23.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall23.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall23.setRound(false);
        PanelWall23.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall23.setLayout(null);
        FormInput.add(PanelWall23);
        PanelWall23.setBounds(490, 140, 40, 30);

        PanelWall24.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall24.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall24.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall24.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall24.setRound(false);
        PanelWall24.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall24.setLayout(null);
        FormInput.add(PanelWall24);
        PanelWall24.setBounds(130, 140, 40, 30);

        PanelWall25.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall25.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall25.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall25.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall25.setRound(false);
        PanelWall25.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall25.setLayout(null);
        FormInput.add(PanelWall25);
        PanelWall25.setBounds(170, 140, 40, 30);

        PanelWall26.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall26.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall26.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall26.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall26.setRound(false);
        PanelWall26.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall26.setLayout(null);
        FormInput.add(PanelWall26);
        PanelWall26.setBounds(130, 230, 40, 30);

        PanelWall27.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall27.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall27.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall27.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall27.setRound(false);
        PanelWall27.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall27.setLayout(null);
        FormInput.add(PanelWall27);
        PanelWall27.setBounds(170, 230, 40, 30);

        PanelWall28.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall28.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall28.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall28.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall28.setRound(false);
        PanelWall28.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall28.setLayout(null);
        FormInput.add(PanelWall28);
        PanelWall28.setBounds(210, 230, 40, 30);

        PanelWall29.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall29.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall29.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall29.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall29.setRound(false);
        PanelWall29.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall29.setLayout(null);
        FormInput.add(PanelWall29);
        PanelWall29.setBounds(250, 230, 40, 30);

        PanelWall30.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall30.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall30.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall30.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall30.setRound(false);
        PanelWall30.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall30.setLayout(null);
        FormInput.add(PanelWall30);
        PanelWall30.setBounds(290, 230, 40, 30);

        PanelWall31.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall31.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall31.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall31.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall31.setRound(false);
        PanelWall31.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall31.setLayout(null);
        FormInput.add(PanelWall31);
        PanelWall31.setBounds(330, 230, 40, 30);

        PanelWall32.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall32.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall32.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall32.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall32.setRound(false);
        PanelWall32.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall32.setLayout(null);
        FormInput.add(PanelWall32);
        PanelWall32.setBounds(370, 230, 40, 30);

        PanelWall33.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall33.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall33.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall33.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall33.setRound(false);
        PanelWall33.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall33.setLayout(null);
        FormInput.add(PanelWall33);
        PanelWall33.setBounds(410, 230, 40, 30);

        PanelWall34.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall34.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall34.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall34.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall34.setRound(false);
        PanelWall34.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall34.setLayout(null);
        FormInput.add(PanelWall34);
        PanelWall34.setBounds(450, 230, 40, 30);

        PanelWall35.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall35.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall35.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall35.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall35.setRound(false);
        PanelWall35.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall35.setLayout(null);
        FormInput.add(PanelWall35);
        PanelWall35.setBounds(490, 230, 40, 30);

        PanelWall36.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall36.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall36.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall36.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall36.setRound(false);
        PanelWall36.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall36.setLayout(null);
        FormInput.add(PanelWall36);
        PanelWall36.setBounds(10, 270, 40, 30);

        PanelWall37.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall37.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall37.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall37.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall37.setRound(false);
        PanelWall37.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall37.setLayout(null);
        FormInput.add(PanelWall37);
        PanelWall37.setBounds(50, 270, 40, 30);

        PanelWall38.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall38.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall38.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall38.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall38.setRound(false);
        PanelWall38.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall38.setLayout(null);
        FormInput.add(PanelWall38);
        PanelWall38.setBounds(90, 270, 40, 30);

        PanelWall39.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall39.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall39.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall39.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall39.setRound(false);
        PanelWall39.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall39.setLayout(null);
        FormInput.add(PanelWall39);
        PanelWall39.setBounds(130, 270, 40, 30);

        PanelWall40.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall40.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall40.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall40.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall40.setRound(false);
        PanelWall40.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall40.setLayout(null);
        FormInput.add(PanelWall40);
        PanelWall40.setBounds(170, 270, 40, 30);

        PanelWall41.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall41.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall41.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall41.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall41.setRound(false);
        PanelWall41.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall41.setLayout(null);
        FormInput.add(PanelWall41);
        PanelWall41.setBounds(210, 270, 40, 30);

        PanelWall42.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall42.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall42.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall42.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall42.setRound(false);
        PanelWall42.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall42.setLayout(null);
        FormInput.add(PanelWall42);
        PanelWall42.setBounds(250, 270, 40, 30);

        PanelWall43.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall43.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall43.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall43.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall43.setRound(false);
        PanelWall43.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall43.setLayout(null);
        FormInput.add(PanelWall43);
        PanelWall43.setBounds(290, 270, 40, 30);

        PanelWall44.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall44.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall44.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall44.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall44.setRound(false);
        PanelWall44.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall44.setLayout(null);
        FormInput.add(PanelWall44);
        PanelWall44.setBounds(330, 270, 40, 30);

        PanelWall45.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall45.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall45.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall45.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall45.setRound(false);
        PanelWall45.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall45.setLayout(null);
        FormInput.add(PanelWall45);
        PanelWall45.setBounds(370, 270, 40, 30);

        PanelWall46.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall46.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/anterior.png"))); // NOI18N
        PanelWall46.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall46.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall46.setRound(false);
        PanelWall46.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall46.setLayout(null);
        FormInput.add(PanelWall46);
        PanelWall46.setBounds(410, 270, 40, 30);

        PanelWall47.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall47.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall47.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall47.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall47.setRound(false);
        PanelWall47.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall47.setLayout(null);
        FormInput.add(PanelWall47);
        PanelWall47.setBounds(450, 270, 40, 30);

        PanelWall48.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall48.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall48.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall48.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall48.setRound(false);
        PanelWall48.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall48.setLayout(null);
        FormInput.add(PanelWall48);
        PanelWall48.setBounds(490, 270, 40, 30);

        PanelWall49.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall49.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall49.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall49.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall49.setRound(false);
        PanelWall49.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall49.setLayout(null);
        FormInput.add(PanelWall49);
        PanelWall49.setBounds(530, 270, 40, 30);

        PanelWall50.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall50.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall50.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall50.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall50.setRound(false);
        PanelWall50.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall50.setLayout(null);
        FormInput.add(PanelWall50);
        PanelWall50.setBounds(570, 270, 40, 30);

        PanelWall51.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall51.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/posterior.png"))); // NOI18N
        PanelWall51.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall51.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall51.setRound(false);
        PanelWall51.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall51.setLayout(null);
        FormInput.add(PanelWall51);
        PanelWall51.setBounds(610, 270, 40, 30);

        label15.setText("Dokter Gigi :");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label15);
        label15.setBounds(0, 40, 100, 23);

        buttonGroup1.add(gigi18);
        gigi18.setText("18");
        gigi18.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi18.setName("gigi18"); // NOI18N
        gigi18.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi18ActionPerformed(evt);
            }
        });
        FormInput.add(gigi18);
        gigi18.setBounds(10, 80, 40, 23);

        buttonGroup1.add(gigi17);
        gigi17.setText("17");
        gigi17.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi17.setName("gigi17"); // NOI18N
        gigi17.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi17ActionPerformed(evt);
            }
        });
        FormInput.add(gigi17);
        gigi17.setBounds(50, 80, 40, 23);

        buttonGroup1.add(gigi28);
        gigi28.setText("28");
        gigi28.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi28.setName("gigi28"); // NOI18N
        gigi28.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi28ActionPerformed(evt);
            }
        });
        FormInput.add(gigi28);
        gigi28.setBounds(610, 80, 40, 23);

        buttonGroup1.add(gigi16);
        gigi16.setText("16");
        gigi16.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi16.setName("gigi16"); // NOI18N
        gigi16.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi16ActionPerformed(evt);
            }
        });
        FormInput.add(gigi16);
        gigi16.setBounds(90, 80, 40, 23);

        buttonGroup1.add(gigi15);
        gigi15.setText("15");
        gigi15.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi15.setName("gigi15"); // NOI18N
        gigi15.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi15ActionPerformed(evt);
            }
        });
        FormInput.add(gigi15);
        gigi15.setBounds(130, 80, 40, 23);

        buttonGroup1.add(gigi14);
        gigi14.setText("14");
        gigi14.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi14.setName("gigi14"); // NOI18N
        gigi14.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi14ActionPerformed(evt);
            }
        });
        FormInput.add(gigi14);
        gigi14.setBounds(170, 80, 40, 23);

        buttonGroup1.add(gigi13);
        gigi13.setText("13");
        gigi13.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi13.setName("gigi13"); // NOI18N
        gigi13.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi13ActionPerformed(evt);
            }
        });
        FormInput.add(gigi13);
        gigi13.setBounds(210, 80, 40, 23);

        buttonGroup1.add(gigi12);
        gigi12.setText("12");
        gigi12.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi12.setName("gigi12"); // NOI18N
        gigi12.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi12ActionPerformed(evt);
            }
        });
        FormInput.add(gigi12);
        gigi12.setBounds(250, 80, 40, 23);

        buttonGroup1.add(gigi11);
        gigi11.setText("11");
        gigi11.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi11.setName("gigi11"); // NOI18N
        gigi11.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi11ActionPerformed(evt);
            }
        });
        FormInput.add(gigi11);
        gigi11.setBounds(290, 80, 40, 23);

        buttonGroup1.add(gigi21);
        gigi21.setText("21");
        gigi21.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi21.setName("gigi21"); // NOI18N
        gigi21.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi21ActionPerformed(evt);
            }
        });
        FormInput.add(gigi21);
        gigi21.setBounds(330, 80, 40, 23);

        buttonGroup1.add(gigi22);
        gigi22.setText("22");
        gigi22.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi22.setName("gigi22"); // NOI18N
        gigi22.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi22ActionPerformed(evt);
            }
        });
        FormInput.add(gigi22);
        gigi22.setBounds(370, 80, 40, 23);

        buttonGroup1.add(gigi23);
        gigi23.setText("23");
        gigi23.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi23.setName("gigi23"); // NOI18N
        gigi23.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi23ActionPerformed(evt);
            }
        });
        FormInput.add(gigi23);
        gigi23.setBounds(410, 80, 40, 23);

        buttonGroup1.add(gigi24);
        gigi24.setText("24");
        gigi24.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi24.setName("gigi24"); // NOI18N
        gigi24.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi24ActionPerformed(evt);
            }
        });
        FormInput.add(gigi24);
        gigi24.setBounds(450, 80, 40, 23);

        buttonGroup1.add(gigi25);
        gigi25.setText("25");
        gigi25.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi25.setName("gigi25"); // NOI18N
        gigi25.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi25ActionPerformed(evt);
            }
        });
        FormInput.add(gigi25);
        gigi25.setBounds(490, 80, 40, 23);

        buttonGroup1.add(gigi26);
        gigi26.setText("26");
        gigi26.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi26.setName("gigi26"); // NOI18N
        gigi26.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi26ActionPerformed(evt);
            }
        });
        FormInput.add(gigi26);
        gigi26.setBounds(530, 80, 40, 23);

        buttonGroup1.add(gigi27);
        gigi27.setText("27");
        gigi27.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi27.setName("gigi27"); // NOI18N
        gigi27.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi27ActionPerformed(evt);
            }
        });
        FormInput.add(gigi27);
        gigi27.setBounds(570, 80, 40, 23);

        buttonGroup1.add(gigi31);
        gigi31.setText("31");
        gigi31.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi31.setName("gigi31"); // NOI18N
        gigi31.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi31ActionPerformed(evt);
            }
        });
        FormInput.add(gigi31);
        gigi31.setBounds(330, 300, 40, 23);

        buttonGroup1.add(gigi32);
        gigi32.setText("32");
        gigi32.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi32.setName("gigi32"); // NOI18N
        gigi32.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi32ActionPerformed(evt);
            }
        });
        FormInput.add(gigi32);
        gigi32.setBounds(370, 300, 40, 23);

        buttonGroup1.add(gigi33);
        gigi33.setText("33");
        gigi33.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi33.setName("gigi33"); // NOI18N
        gigi33.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi33ActionPerformed(evt);
            }
        });
        FormInput.add(gigi33);
        gigi33.setBounds(410, 300, 40, 23);

        buttonGroup1.add(gigi34);
        gigi34.setText("34");
        gigi34.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi34.setName("gigi34"); // NOI18N
        gigi34.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi34ActionPerformed(evt);
            }
        });
        FormInput.add(gigi34);
        gigi34.setBounds(450, 300, 40, 23);

        buttonGroup1.add(gigi35);
        gigi35.setText("35");
        gigi35.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi35.setName("gigi35"); // NOI18N
        gigi35.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi35ActionPerformed(evt);
            }
        });
        FormInput.add(gigi35);
        gigi35.setBounds(490, 300, 40, 23);

        buttonGroup1.add(gigi36);
        gigi36.setText("36");
        gigi36.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi36.setName("gigi36"); // NOI18N
        gigi36.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi36ActionPerformed(evt);
            }
        });
        FormInput.add(gigi36);
        gigi36.setBounds(530, 300, 40, 23);

        buttonGroup1.add(gigi37);
        gigi37.setText("37");
        gigi37.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi37.setName("gigi37"); // NOI18N
        gigi37.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi37ActionPerformed(evt);
            }
        });
        FormInput.add(gigi37);
        gigi37.setBounds(570, 300, 40, 23);

        buttonGroup1.add(gigi38);
        gigi38.setText("38");
        gigi38.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi38.setName("gigi38"); // NOI18N
        gigi38.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi38ActionPerformed(evt);
            }
        });
        FormInput.add(gigi38);
        gigi38.setBounds(610, 300, 40, 23);

        buttonGroup1.add(gigi41);
        gigi41.setText("41");
        gigi41.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi41.setName("gigi41"); // NOI18N
        gigi41.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi41ActionPerformed(evt);
            }
        });
        FormInput.add(gigi41);
        gigi41.setBounds(290, 300, 40, 23);

        buttonGroup1.add(gigi42);
        gigi42.setText("42");
        gigi42.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi42.setName("gigi42"); // NOI18N
        gigi42.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi42.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi42ActionPerformed(evt);
            }
        });
        FormInput.add(gigi42);
        gigi42.setBounds(250, 300, 40, 23);

        buttonGroup1.add(gigi43);
        gigi43.setText("43");
        gigi43.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi43.setName("gigi43"); // NOI18N
        gigi43.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi43.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi43ActionPerformed(evt);
            }
        });
        FormInput.add(gigi43);
        gigi43.setBounds(210, 300, 40, 23);

        buttonGroup1.add(gigi44);
        gigi44.setText("44");
        gigi44.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi44.setName("gigi44"); // NOI18N
        gigi44.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi44.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi44ActionPerformed(evt);
            }
        });
        FormInput.add(gigi44);
        gigi44.setBounds(170, 300, 40, 23);

        buttonGroup1.add(gigi45);
        gigi45.setText("45");
        gigi45.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi45.setName("gigi45"); // NOI18N
        gigi45.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi45.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi45ActionPerformed(evt);
            }
        });
        FormInput.add(gigi45);
        gigi45.setBounds(130, 300, 40, 23);

        buttonGroup1.add(gigi46);
        gigi46.setText("46");
        gigi46.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi46.setName("gigi46"); // NOI18N
        gigi46.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi46.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi46ActionPerformed(evt);
            }
        });
        FormInput.add(gigi46);
        gigi46.setBounds(90, 300, 40, 23);

        buttonGroup1.add(gigi47);
        gigi47.setText("47");
        gigi47.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi47.setName("gigi47"); // NOI18N
        gigi47.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi47.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi47ActionPerformed(evt);
            }
        });
        FormInput.add(gigi47);
        gigi47.setBounds(50, 300, 40, 23);

        buttonGroup1.add(gigi48);
        gigi48.setText("48");
        gigi48.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi48.setName("gigi48"); // NOI18N
        gigi48.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi48.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi48ActionPerformed(evt);
            }
        });
        FormInput.add(gigi48);
        gigi48.setBounds(10, 300, 40, 23);

        buttonGroup1.add(gigi51);
        gigi51.setText("51");
        gigi51.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi51.setName("gigi51"); // NOI18N
        gigi51.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi51.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi51ActionPerformed(evt);
            }
        });
        FormInput.add(gigi51);
        gigi51.setBounds(290, 170, 40, 23);

        buttonGroup1.add(gigi52);
        gigi52.setText("52");
        gigi52.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi52.setName("gigi52"); // NOI18N
        gigi52.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi52.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi52ActionPerformed(evt);
            }
        });
        FormInput.add(gigi52);
        gigi52.setBounds(250, 170, 40, 23);

        buttonGroup1.add(gigi53);
        gigi53.setText("53");
        gigi53.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi53.setName("gigi53"); // NOI18N
        gigi53.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi53.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi53ActionPerformed(evt);
            }
        });
        FormInput.add(gigi53);
        gigi53.setBounds(210, 170, 40, 23);

        buttonGroup1.add(gigi54);
        gigi54.setText("54");
        gigi54.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi54.setName("gigi54"); // NOI18N
        gigi54.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi54.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi54ActionPerformed(evt);
            }
        });
        FormInput.add(gigi54);
        gigi54.setBounds(170, 170, 40, 23);

        buttonGroup1.add(gigi55);
        gigi55.setText("55");
        gigi55.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi55.setName("gigi55"); // NOI18N
        gigi55.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi55.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi55ActionPerformed(evt);
            }
        });
        FormInput.add(gigi55);
        gigi55.setBounds(130, 170, 40, 23);

        buttonGroup1.add(gigi61);
        gigi61.setText("61");
        gigi61.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi61.setName("gigi61"); // NOI18N
        gigi61.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi61.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi61ActionPerformed(evt);
            }
        });
        FormInput.add(gigi61);
        gigi61.setBounds(330, 170, 40, 23);

        buttonGroup1.add(gigi62);
        gigi62.setText("62");
        gigi62.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi62.setName("gigi62"); // NOI18N
        gigi62.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi62.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi62ActionPerformed(evt);
            }
        });
        FormInput.add(gigi62);
        gigi62.setBounds(370, 170, 40, 23);

        buttonGroup1.add(gigi63);
        gigi63.setText("63");
        gigi63.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi63.setName("gigi63"); // NOI18N
        gigi63.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi63.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi63ActionPerformed(evt);
            }
        });
        FormInput.add(gigi63);
        gigi63.setBounds(410, 170, 40, 23);

        buttonGroup1.add(gigi64);
        gigi64.setText("64");
        gigi64.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi64.setName("gigi64"); // NOI18N
        gigi64.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi64.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi64ActionPerformed(evt);
            }
        });
        FormInput.add(gigi64);
        gigi64.setBounds(450, 170, 40, 23);

        buttonGroup1.add(gigi65);
        gigi65.setText("65");
        gigi65.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi65.setName("gigi65"); // NOI18N
        gigi65.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi65.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi65ActionPerformed(evt);
            }
        });
        FormInput.add(gigi65);
        gigi65.setBounds(490, 170, 40, 23);

        buttonGroup1.add(gigi71);
        gigi71.setText("71");
        gigi71.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi71.setName("gigi71"); // NOI18N
        gigi71.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi71.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi71ActionPerformed(evt);
            }
        });
        FormInput.add(gigi71);
        gigi71.setBounds(330, 210, 40, 23);

        buttonGroup1.add(gigi72);
        gigi72.setText("72");
        gigi72.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi72.setName("gigi72"); // NOI18N
        gigi72.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi72.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi72ActionPerformed(evt);
            }
        });
        FormInput.add(gigi72);
        gigi72.setBounds(370, 210, 40, 23);

        buttonGroup1.add(gigi73);
        gigi73.setText("73");
        gigi73.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi73.setName("gigi73"); // NOI18N
        gigi73.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi73.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi73ActionPerformed(evt);
            }
        });
        FormInput.add(gigi73);
        gigi73.setBounds(410, 210, 40, 23);

        buttonGroup1.add(gigi74);
        gigi74.setText("74");
        gigi74.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi74.setName("gigi74"); // NOI18N
        gigi74.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi74.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi74ActionPerformed(evt);
            }
        });
        FormInput.add(gigi74);
        gigi74.setBounds(450, 210, 40, 23);

        buttonGroup1.add(gigi75);
        gigi75.setText("75");
        gigi75.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi75.setName("gigi75"); // NOI18N
        gigi75.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi75.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi75ActionPerformed(evt);
            }
        });
        FormInput.add(gigi75);
        gigi75.setBounds(490, 210, 40, 23);

        buttonGroup1.add(gigi81);
        gigi81.setText("81");
        gigi81.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi81.setName("gigi81"); // NOI18N
        gigi81.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi81.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi81ActionPerformed(evt);
            }
        });
        FormInput.add(gigi81);
        gigi81.setBounds(290, 210, 40, 23);

        buttonGroup1.add(gigi82);
        gigi82.setText("82");
        gigi82.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi82.setName("gigi82"); // NOI18N
        gigi82.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi82.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi82ActionPerformed(evt);
            }
        });
        FormInput.add(gigi82);
        gigi82.setBounds(250, 210, 40, 23);

        buttonGroup1.add(gigi83);
        gigi83.setText("83");
        gigi83.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi83.setName("gigi83"); // NOI18N
        gigi83.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi83.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi83ActionPerformed(evt);
            }
        });
        FormInput.add(gigi83);
        gigi83.setBounds(210, 210, 40, 23);

        buttonGroup1.add(gigi84);
        gigi84.setText("84");
        gigi84.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi84.setName("gigi84"); // NOI18N
        gigi84.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi84.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi84ActionPerformed(evt);
            }
        });
        FormInput.add(gigi84);
        gigi84.setBounds(170, 210, 40, 23);

        buttonGroup1.add(gigi85);
        gigi85.setText("85");
        gigi85.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gigi85.setName("gigi85"); // NOI18N
        gigi85.setPreferredSize(new java.awt.Dimension(40, 20));
        gigi85.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gigi85ActionPerformed(evt);
            }
        });
        FormInput.add(gigi85);
        gigi85.setBounds(130, 210, 40, 23);

        Hasil.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Erupsi", "Hilang", "Karies", "Sisa Akar", "Tumpatan", "Goyang", "Calculus", "Abces", "Impaksi", "Protesa", "Hiperemia", "Persistensi", "Malposisi" }));
        Hasil.setName("Hasil"); // NOI18N
        Hasil.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HasilKeyPressed(evt);
            }
        });
        FormInput.add(Hasil);
        Hasil.setBounds(670, 190, 130, 23);

        jLabel4.setText("No.Permintaan :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(700, 40, 98, 23);

        TNoPermintaan.setHighlighter(null);
        TNoPermintaan.setName("TNoPermintaan"); // NOI18N
        TNoPermintaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoPermintaanKeyPressed(evt);
            }
        });
        FormInput.add(TNoPermintaan);
        TNoPermintaan.setBounds(800, 40, 130, 23);

        jLabel15.setText("Tanggal :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(540, 40, 67, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-06-2021" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TanggalItemStateChanged(evt);
            }
        });
        Tanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalActionPerformed(evt);
            }
        });
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(610, 40, 90, 23);

        jLabel32.setText("Rahang :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(800, 190, 60, 23);

        Rahang.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Rahang Atas", "Rahang Bawah" }));
        Rahang.setName("Rahang"); // NOI18N
        Rahang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RahangKeyPressed(evt);
            }
        });
        FormInput.add(Rahang);
        Rahang.setBounds(870, 190, 130, 23);

        scrollInput.setViewportView(FormInput);

        PanelInput.add(scrollInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
            isPsien();
        }else{            
            Valid.pindah(evt,TCari,BtnDokter);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt,TCari,BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

private void autoNomor() {
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(noorder,4),signed)),0) from pemeriksaan_gigi where tanggal='"+Valid.SetTgl(Tanggal.getSelectedItem()+"")+"' ","OD"+Valid.SetTgl(Tanggal.getSelectedItem()+"").replaceAll("-",""),5,TNoPermintaan);           
    }    
    
    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().equals("")||TNoRM.getText().equals("")||TPasien.getText().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KodeDokter.getText().equals("")||NamaDokter.getText().equals("")){
            Valid.textKosong(BtnDokter,"Dokter Gigi");
        }else if(TNoPermintaan.getText().equals("")){
            Valid.textKosong(TNoPermintaan,"No Permintaan");
        }else if(DiagnosaUtama.getText().equals("")){
            Valid.textKosong(DiagnosaUtama,"Diagnosa Utama");
        }else if(Catatan.getText().equals("")){
            Valid.textKosong(Catatan,"Catatan Pemeriksaan");
        }else{
        if(gigi11.isSelected()==true){
                bagian="1.1";
            }
        if(gigi12.isSelected()==true){
                bagian="1.2";
            }
        if(gigi13.isSelected()==true){
                bagian="1.3";
            }
        if(gigi14.isSelected()==true){
                bagian="1.4";
            }
        if(gigi15.isSelected()==true){
                bagian="1.5";
            }
        if(gigi16.isSelected()==true){
                bagian="1.6";
            }
        if(gigi17.isSelected()==true){
                bagian="1.7";
            }
        if(gigi18.isSelected()==true){
                bagian="1.8";
            }
        if(gigi21.isSelected()==true){
                bagian="2.1";
            }
        if(gigi22.isSelected()==true){
                bagian="2.2";
            }
        if(gigi23.isSelected()==true){
                bagian="2.3";
            }
        if(gigi24.isSelected()==true){
                bagian="2.4";
            }
        if(gigi25.isSelected()==true){
                bagian="2.5";
            }
        if(gigi26.isSelected()==true){
                bagian="2.6";
            }
        if(gigi27.isSelected()==true){
                bagian="2.7";
            }
        if(gigi28.isSelected()==true){
                bagian="2.8";
            }
        if(gigi31.isSelected()==true){
                bagian="3.1";
            }
        if(gigi32.isSelected()==true){
                bagian="3.2";
            }
        if(gigi33.isSelected()==true){
                bagian="3.3";
            }
        if(gigi34.isSelected()==true){
                bagian="3.4";
            }
        if(gigi35.isSelected()==true){
                bagian="3.5";
            }
        if(gigi36.isSelected()==true){
                bagian="3.6";
            }
        if(gigi37.isSelected()==true){
                bagian="3.7";
            }
        if(gigi38.isSelected()==true){
                bagian="3.8";
            }
        if(gigi41.isSelected()==true){
                bagian="4.1";
            }
        if(gigi42.isSelected()==true){
                bagian="4.2";
            }
        if(gigi43.isSelected()==true){
                bagian="4.3";
            }
        if(gigi44.isSelected()==true){
                bagian="4.4";
            }
        if(gigi45.isSelected()==true){
                bagian="4.5";
            }
        if(gigi46.isSelected()==true){
                bagian="4.6";
            }
        if(gigi47.isSelected()==true){
                bagian="4.7";
            }
        if(gigi48.isSelected()==true){
                bagian="4.8";
            }
        if(gigi51.isSelected()==true){
                bagian="5.1";
            }
        if(gigi52.isSelected()==true){
                bagian="5.2";
            }
        if(gigi53.isSelected()==true){
                bagian="5.3";
            }
        if(gigi54.isSelected()==true){
                bagian="5.4";
            }
        if(gigi55.isSelected()==true){
                bagian="5.5";
            }
        if(gigi61.isSelected()==true){
                bagian="6.1";
            }
        if(gigi62.isSelected()==true){
                bagian="6.2";
            }
        if(gigi63.isSelected()==true){
                bagian="6.3";
            }
        if(gigi64.isSelected()==true){
                bagian="6.4";
            }
        if(gigi65.isSelected()==true){
                bagian="6.5";
            }
        if(gigi71.isSelected()==true){
                bagian="7.1";
            }
        if(gigi72.isSelected()==true){
                bagian="7.2";
            }
        if(gigi73.isSelected()==true){
                bagian="7.3";
            }
        if(gigi74.isSelected()==true){
                bagian="7.4";
            }
        if(gigi75.isSelected()==true){
                bagian="7.5";
            }
        if(gigi81.isSelected()==true){
                bagian="8.1";
            }
        if(gigi82.isSelected()==true){
                bagian="8.2";
            }
        if(gigi83.isSelected()==true){
                bagian="8.3";
            }
        if(gigi84.isSelected()==true){
                bagian="8.4";
            }
        if(gigi85.isSelected()==true){
                bagian="8.5";
            }
//        }else if(DiagnosaUtama.getText().equals("")){
//            Valid.textKosong(DiagnosaUtama,"Diagnosa Utama");
//        }else if(Catatan.getText().equals("")){
//            Valid.textKosong(Catatan,"Catatan Pemeriksaan");
//        }else{
            if(Sequel.menyimpantf("pemeriksaan_gigi","?,?,?,?,?,?,?,?,?,?","No.Rawat",10,new String[]{
                    TNoRw.getText(),KodeDokter.getText(),bagian,DiagnosaUtama.getText(),KodeDiagnosaUtama.getText(),
                    Hasil.getSelectedItem().toString(),Catatan.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+""),
                    TNoPermintaan.getText(),Rahang.getSelectedItem().toString()
                })==true){
                    tampil();
                    emptTeks();
            }
        }
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
        if(tbObat.getSelectedRow()>-1){
            if(Sequel.queryu2tf("delete from pemeriksaan_gigi where noorder=?",1,new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(),13).toString()
            })==true){
                tampil();
                emptTeks();
            }else{
                JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
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
        if(TNoRw.getText().equals("")||TNoRM.getText().equals("")||TPasien.getText().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KodeDokter.getText().equals("")||NamaDokter.getText().equals("")){
            Valid.textKosong(BtnDokter,"Dokter Gigi");
        }else{
        if(tbObat.getSelectedRow()>-1){
        if(gigi11.isSelected()==true){
                bagian="1.1";
            }
        if(gigi12.isSelected()==true){
                bagian="1.2";
            }
        if(gigi13.isSelected()==true){
                bagian="1.3";
            }
        if(gigi14.isSelected()==true){
                bagian="1.4";
            }
        if(gigi15.isSelected()==true){
                bagian="1.5";
            }
        if(gigi16.isSelected()==true){
                bagian="1.6";
            }
        if(gigi17.isSelected()==true){
                bagian="1.7";
            }
        if(gigi18.isSelected()==true){
                bagian="1.8";
            }
        if(gigi21.isSelected()==true){
                bagian="2.1";
            }
        if(gigi22.isSelected()==true){
                bagian="2.2";
            }
        if(gigi23.isSelected()==true){
                bagian="2.3";
            }
        if(gigi24.isSelected()==true){
                bagian="2.4";
            }
        if(gigi25.isSelected()==true){
                bagian="2.5";
            }
        if(gigi26.isSelected()==true){
                bagian="2.6";
            }
        if(gigi27.isSelected()==true){
                bagian="2.7";
            }
        if(gigi28.isSelected()==true){
                bagian="2.8";
            }
        if(gigi31.isSelected()==true){
                bagian="3.1";
            }
        if(gigi32.isSelected()==true){
                bagian="3.2";
            }
        if(gigi33.isSelected()==true){
                bagian="3.3";
            }
        if(gigi34.isSelected()==true){
                bagian="3.4";
            }
        if(gigi35.isSelected()==true){
                bagian="3.5";
            }
        if(gigi36.isSelected()==true){
                bagian="3.6";
            }
        if(gigi37.isSelected()==true){
                bagian="3.7";
            }
        if(gigi38.isSelected()==true){
                bagian="3.8";
            }
        if(gigi41.isSelected()==true){
                bagian="4.1";
            }
        if(gigi42.isSelected()==true){
                bagian="4.2";
            }
        if(gigi43.isSelected()==true){
                bagian="4.3";
            }
        if(gigi44.isSelected()==true){
                bagian="4.4";
            }
        if(gigi45.isSelected()==true){
                bagian="4.5";
            }
        if(gigi46.isSelected()==true){
                bagian="4.6";
            }
        if(gigi47.isSelected()==true){
                bagian="4.7";
            }
        if(gigi48.isSelected()==true){
                bagian="4.8";
            }
        if(gigi51.isSelected()==true){
                bagian="5.1";
            }
        if(gigi52.isSelected()==true){
                bagian="5.2";
            }
        if(gigi53.isSelected()==true){
                bagian="5.3";
            }
        if(gigi54.isSelected()==true){
                bagian="5.4";
            }
        if(gigi55.isSelected()==true){
                bagian="5.5";
            }
        if(gigi61.isSelected()==true){
                bagian="6.1";
            }
        if(gigi62.isSelected()==true){
                bagian="6.2";
            }
        if(gigi63.isSelected()==true){
                bagian="6.3";
            }
        if(gigi64.isSelected()==true){
                bagian="6.4";
            }
        if(gigi65.isSelected()==true){
                bagian="6.5";
            }
        if(gigi71.isSelected()==true){
                bagian="7.1";
            }
        if(gigi72.isSelected()==true){
                bagian="7.2";
            }
        if(gigi73.isSelected()==true){
                bagian="7.3";
            }
        if(gigi74.isSelected()==true){
                bagian="7.4";
            }
        if(gigi75.isSelected()==true){
                bagian="7.5";
            }
        if(gigi81.isSelected()==true){
                bagian="8.1";
            }
        if(gigi82.isSelected()==true){
                bagian="8.2";
            }
        if(gigi83.isSelected()==true){
                bagian="8.3";
            }
        if(gigi84.isSelected()==true){
                bagian="8.4";
            }
        if(gigi85.isSelected()==true){
                bagian="8.5";
            }
        }else if(DiagnosaUtama.getText().equals("")){
            Valid.textKosong(DiagnosaUtama,"Diagnosa Gigi");
        }else if(Catatan.getText().equals("")){
            Valid.textKosong(Catatan,"Catatan Pemeriksaan");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(Sequel.mengedittf("pemeriksaan_gigi","no_rawat=?","no_rawat=?,kd_dokter=?,bagian=?,diagnosa=?,kd_diagnosa=?,hasil=?,catatan=?,tanggal=?,rahang=?",10,new String[]{
                    TNoRw.getText(),KodeDokter.getText(),bagian,DiagnosaUtama.getText(),KodeDiagnosaUtama.getText(),
                    Hasil.getSelectedItem().toString(),Catatan.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+""),
                    Rahang.getSelectedItem().toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()
                    })==true){
                       tampil();
                       emptTeks();
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
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
        dokter.dispose();
        carikeluhan.dispose();
        caripemeriksaan.dispose();
        carilaborat.dispose();
        cariobat.dispose();
        cariradiologi.dispose();
        penyakit.dispose();
        rmcaridiagnosa1.dispose();
        rmcaridiagnosa2.dispose();
        rmcaridiagnosa3.dispose();
        rmcaridiagnosa4.dispose();
        rmcaridiagnosa5.dispose();
        rmcariprosedur1.dispose();
        rmcariprosedur2.dispose();
        rmcariprosedur3.dispose();
        rmcariprosedur4.dispose();
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit,TCari);}
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
                param.put("logo",Sequel.cariGambar("select logo from setting")); 
                if(TCari.getText().equals("")){
                    Valid.MyReportqry("rptDataResumePasien.jasper","report","::[ Data Resume Pasien Rawat Jalan]::",
                        "select reg_periksa.tgl_registrasi,reg_periksa.no_rawat,reg_periksa.status_lanjut,reg_periksa.no_rkm_medis,pasien.nm_pasien, "+
                        "resume_pasien.kd_dokter,dokter.nm_dokter,resume_pasien.kondisi_pulang,resume_pasien.keluhan_utama,resume_pasien.jalannya_penyakit, "+
                        "resume_pasien.pemeriksaan_penunjang,resume_pasien.hasil_laborat,resume_pasien.diagnosa_utama,resume_pasien.kd_diagnosa_utama, "+
                        "resume_pasien.diagnosa_sekunder,resume_pasien.kd_diagnosa_sekunder,resume_pasien.diagnosa_sekunder2,resume_pasien.kd_diagnosa_sekunder2, "+
                        "resume_pasien.diagnosa_sekunder3,resume_pasien.kd_diagnosa_sekunder3,resume_pasien.diagnosa_sekunder4,resume_pasien.kd_diagnosa_sekunder4, "+
                        "resume_pasien.prosedur_utama,resume_pasien.kd_prosedur_utama,resume_pasien.prosedur_sekunder,resume_pasien.kd_prosedur_sekunder, "+
                        "resume_pasien.prosedur_sekunder2,resume_pasien.kd_prosedur_sekunder2,resume_pasien.prosedur_sekunder3,resume_pasien.kd_prosedur_sekunder3, "+
                        "resume_pasien.obat_pulang,resume_pasien.tindak_lanjut,resume_pasien.asal_pasien from resume_pasien inner join reg_periksa on resume_pasien.no_rawat=reg_periksa.no_rawat  "+
                        "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join dokter on resume_pasien.kd_dokter=dokter.kd_dokter "+
                        "where reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' order by reg_periksa.tgl_registrasi,reg_periksa.status_lanjut",param);
                }else{
                    Valid.MyReportqry("rptDataResumePasien.jasper","report","::[ Data Resume Pasien ]::",
                        "select reg_periksa.tgl_registrasi,reg_periksa.no_rawat,reg_periksa.status_lanjut,reg_periksa.no_rkm_medis,pasien.nm_pasien, "+
                        "resume_pasien.kd_dokter,dokter.nm_dokter,resume_pasien.kondisi_pulang,resume_pasien.keluhan_utama,resume_pasien.jalannya_penyakit, "+
                        "resume_pasien.pemeriksaan_penunjang,resume_pasien.hasil_laborat,resume_pasien.diagnosa_utama,resume_pasien.kd_diagnosa_utama, "+
                        "resume_pasien.diagnosa_sekunder,resume_pasien.kd_diagnosa_sekunder,resume_pasien.diagnosa_sekunder2,resume_pasien.kd_diagnosa_sekunder2, "+
                        "resume_pasien.diagnosa_sekunder3,resume_pasien.kd_diagnosa_sekunder3,resume_pasien.diagnosa_sekunder4,resume_pasien.kd_diagnosa_sekunder4, "+
                        "resume_pasien.prosedur_utama,resume_pasien.kd_prosedur_utama,resume_pasien.prosedur_sekunder,resume_pasien.kd_prosedur_sekunder, "+
                        "resume_pasien.prosedur_sekunder2,resume_pasien.kd_prosedur_sekunder2,resume_pasien.prosedur_sekunder3,resume_pasien.kd_prosedur_sekunder3, "+
                        "resume_pasien.obat_pulang,resume_pasien.tindak_lanjut,resume_pasien.asal_pasien from resume_pasien inner join reg_periksa on resume_pasien.no_rawat=reg_periksa.no_rawat  "+
                        "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join dokter on resume_pasien.kd_dokter=dokter.kd_dokter "+
                        "where reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and reg_periksa.status_lanjut like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and reg_periksa.no_rkm_medis like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and pasien.nm_pasien like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and resume_pasien.kd_dokter like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and dokter.nm_dokter like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and resume_pasien.kondisi_pulang like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and resume_pasien.kd_diagnosa_utama like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and resume_pasien.diagnosa_utama like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and resume_pasien.prosedur_utama like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and reg_periksa.no_rawat like '%"+TCari.getText().trim()+"%' or "+
                        "reg_periksa.tgl_registrasi between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and resume_pasien.kd_prosedur_utama like '%"+TCari.getText().trim()+"%' "+
                        "order by reg_periksa.tgl_registrasi,reg_periksa.status_lanjut",param);
                }
                    
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
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

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        // Valid.pindah(evt, TNm, BtnSimpan);
}//GEN-LAST:event_TNoRMKeyPressed

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
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    ChkInput.setSelected(true);
                    isForm(); 
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void DiagnosaUtamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosaUtamaKeyPressed
       Valid.pindah(evt,Hasil,KodeDiagnosaUtama);
    }//GEN-LAST:event_DiagnosaUtamaKeyPressed

    private void KodeDiagnosaUtamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KodeDiagnosaUtamaKeyPressed
        Valid.pindah(evt,DiagnosaUtama,Hasil);
    }//GEN-LAST:event_KodeDiagnosaUtamaKeyPressed

    private void KodeDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KodeDokterKeyPressed
        Valid.pindah(evt,TCari,DiagnosaUtama);
    }//GEN-LAST:event_KodeDokterKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
       Valid.pindah(evt,TCari,DiagnosaUtama);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void CatatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CatatanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(evt.isShiftDown()){
                BtnSimpan.requestFocus();
            }
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Hasil.requestFocus();
        }
    }//GEN-LAST:event_CatatanKeyPressed

    private void MnLaporanResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnLaporanResumeActionPerformed
        if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();    
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select logo from setting")); 
            param.put("norawat",tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            param.put("finger",Sequel.cariIsi("select sha1(sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())); 
            if(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString().equals("Ralan")){
                param.put("ruang",Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on reg_periksa.kd_poli=poliklinik.kd_poli where reg_periksa.no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()));
                param.put("tanggalkeluar",Sequel.cariIsi("select DATE_FORMAT(tgl_registrasi, '%d-%m-%Y') from reg_periksa where no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()));
            }else{
                param.put("ruang",Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar inner join kamar_inap on bangsal.kd_bangsal=kamar.kd_bangsal and kamar_inap.kd_kamar=kamar.kd_kamar where no_rawat=? order by tgl_masuk desc limit 1 ",tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()));
                param.put("tanggalkeluar",Sequel.cariIsi("select DATE_FORMAT(tgl_keluar, '%d-%m-%Y') from kamar_inap where no_rawat=? order by tgl_keluar desc limit 1 ",tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()));
            }
            Valid.MyReport("rptLaporanResumeRajal.jasper","report","::[ Laporan Resume Pasien Rawat Jalan ]::",param);
        }
    }//GEN-LAST:event_MnLaporanResumeActionPerformed

    private void MnInputDiagnosaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnInputDiagnosaActionPerformed
        if(TNoRw.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
            TCari.requestFocus();
        }else{
            penyakit.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            penyakit.setLocationRelativeTo(internalFrame1);
            penyakit.isCek();
            penyakit.setNoRm(TNoRw.getText(),DTPCari1.getDate(),DTPCari2.getDate(),Sequel.cariIsi("select status_lanjut from reg_periksa where no_rawat=?",TNoRw.getText()));
            penyakit.panelDiagnosa1.tampil();
            penyakit.setVisible(true);
        }
    }//GEN-LAST:event_MnInputDiagnosaActionPerformed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void ppBerkasDigitalBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBerkasDigitalBtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(!tbObat.getValueAt(tbObat.getSelectedRow(),1).toString().equals("")){
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    DlgBerkasRawat berkas=new DlgBerkasRawat(null,true);
                    berkas.setJudul("::[ Berkas Digital Perawatan ]::","berkasrawat/pages");
                    try {
                        berkas.loadURL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/"+"berkasrawat/login2.php?act=login&usere=admin&passwordte=akusayangsamakamu&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
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
    }//GEN-LAST:event_ppBerkasDigitalBtnPrintActionPerformed

    private void BtnDokter6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokter6ActionPerformed
        penyakit1.isCek();
        penyakit1.emptTeks();
        penyakit1.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penyakit1.setLocationRelativeTo(internalFrame1);
        penyakit1.setVisible(true);    // TODO add your handling code here:
    }//GEN-LAST:event_BtnDokter6ActionPerformed

    private void gigi18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi18ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi18ActionPerformed

    private void gigi17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi17ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi17ActionPerformed

    private void gigi28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi28ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi28ActionPerformed

    private void gigi16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi16ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi16ActionPerformed

    private void gigi15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi15ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi15ActionPerformed

    private void gigi14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi14ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi14ActionPerformed

    private void gigi13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi13ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi13ActionPerformed

    private void gigi12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi12ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi12ActionPerformed

    private void gigi11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi11ActionPerformed

    private void gigi21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi21ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi21ActionPerformed

    private void gigi22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi22ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi22ActionPerformed

    private void gigi23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi23ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi23ActionPerformed

    private void gigi24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi24ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi24ActionPerformed

    private void gigi25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi25ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi25ActionPerformed

    private void gigi26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi26ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi26ActionPerformed

    private void gigi27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi27ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi27ActionPerformed

    private void gigi31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi31ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi31ActionPerformed

    private void gigi32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi32ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi32ActionPerformed

    private void gigi33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi33ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi33ActionPerformed

    private void gigi34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi34ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi34ActionPerformed

    private void gigi35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi35ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi35ActionPerformed

    private void gigi36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi36ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi36ActionPerformed

    private void gigi37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi37ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi37ActionPerformed

    private void gigi38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi38ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi38ActionPerformed

    private void gigi41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi41ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi41ActionPerformed

    private void gigi42ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi42ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi42ActionPerformed

    private void gigi43ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi43ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi43ActionPerformed

    private void gigi44ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi44ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi44ActionPerformed

    private void gigi45ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi45ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi45ActionPerformed

    private void gigi46ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi46ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi46ActionPerformed

    private void gigi47ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi47ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi47ActionPerformed

    private void gigi48ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi48ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi48ActionPerformed

    private void gigi51ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi51ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi51ActionPerformed

    private void gigi52ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi52ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi52ActionPerformed

    private void gigi53ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi53ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi53ActionPerformed

    private void gigi54ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi54ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi54ActionPerformed

    private void gigi55ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi55ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi55ActionPerformed

    private void gigi61ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi61ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi61ActionPerformed

    private void gigi62ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi62ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi62ActionPerformed

    private void gigi63ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi63ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi63ActionPerformed

    private void gigi64ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi64ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi64ActionPerformed

    private void gigi65ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi65ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi65ActionPerformed

    private void gigi71ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi71ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi71ActionPerformed

    private void gigi72ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi72ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi72ActionPerformed

    private void gigi73ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi73ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi73ActionPerformed

    private void gigi74ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi74ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi74ActionPerformed

    private void gigi75ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi75ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi75ActionPerformed

    private void gigi81ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi81ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi81ActionPerformed

    private void gigi82ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi82ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi82ActionPerformed

    private void gigi83ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi83ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi83ActionPerformed

    private void gigi84ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi84ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi84ActionPerformed

    private void gigi85ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gigi85ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gigi85ActionPerformed

    private void HasilKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HasilKeyPressed
        Valid.pindah(evt,Hasil,Catatan);
    }//GEN-LAST:event_HasilKeyPressed

    private void TNoPermintaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoPermintaanKeyPressed
        //Valid.pindah(evt,TNoReg,DTPReg);
    }//GEN-LAST:event_TNoPermintaanKeyPressed

    private void TanggalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TanggalItemStateChanged
        try {
            autoNomor();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_TanggalItemStateChanged

    private void TanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalActionPerformed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt, Hasil, Catatan);
    }//GEN-LAST:event_TanggalKeyPressed

    private void RahangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RahangKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_RahangKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgOdontogram dialog = new DlgOdontogram(new javax.swing.JFrame(), true);
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
    private widget.Button BtnDokter6;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.TextArea Catatan;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextBox DiagnosaUtama;
    private widget.PanelBiasa FormInput;
    private widget.ComboBox Hasil;
    private widget.TextBox KodeDiagnosaUtama;
    private widget.TextBox KodeDokter;
    private widget.Label LCount;
    private javax.swing.JMenuItem MnInputDiagnosa;
    private javax.swing.JMenuItem MnLaporanResume;
    private widget.TextBox NamaDokter;
    private javax.swing.JPanel PanelInput;
    private usu.widget.glass.PanelGlass PanelWall;
    private usu.widget.glass.PanelGlass PanelWall1;
    private usu.widget.glass.PanelGlass PanelWall10;
    private usu.widget.glass.PanelGlass PanelWall11;
    private usu.widget.glass.PanelGlass PanelWall12;
    private usu.widget.glass.PanelGlass PanelWall13;
    private usu.widget.glass.PanelGlass PanelWall14;
    private usu.widget.glass.PanelGlass PanelWall15;
    private usu.widget.glass.PanelGlass PanelWall16;
    private usu.widget.glass.PanelGlass PanelWall17;
    private usu.widget.glass.PanelGlass PanelWall18;
    private usu.widget.glass.PanelGlass PanelWall19;
    private usu.widget.glass.PanelGlass PanelWall2;
    private usu.widget.glass.PanelGlass PanelWall20;
    private usu.widget.glass.PanelGlass PanelWall21;
    private usu.widget.glass.PanelGlass PanelWall22;
    private usu.widget.glass.PanelGlass PanelWall23;
    private usu.widget.glass.PanelGlass PanelWall24;
    private usu.widget.glass.PanelGlass PanelWall25;
    private usu.widget.glass.PanelGlass PanelWall26;
    private usu.widget.glass.PanelGlass PanelWall27;
    private usu.widget.glass.PanelGlass PanelWall28;
    private usu.widget.glass.PanelGlass PanelWall29;
    private usu.widget.glass.PanelGlass PanelWall3;
    private usu.widget.glass.PanelGlass PanelWall30;
    private usu.widget.glass.PanelGlass PanelWall31;
    private usu.widget.glass.PanelGlass PanelWall32;
    private usu.widget.glass.PanelGlass PanelWall33;
    private usu.widget.glass.PanelGlass PanelWall34;
    private usu.widget.glass.PanelGlass PanelWall35;
    private usu.widget.glass.PanelGlass PanelWall36;
    private usu.widget.glass.PanelGlass PanelWall37;
    private usu.widget.glass.PanelGlass PanelWall38;
    private usu.widget.glass.PanelGlass PanelWall39;
    private usu.widget.glass.PanelGlass PanelWall4;
    private usu.widget.glass.PanelGlass PanelWall40;
    private usu.widget.glass.PanelGlass PanelWall41;
    private usu.widget.glass.PanelGlass PanelWall42;
    private usu.widget.glass.PanelGlass PanelWall43;
    private usu.widget.glass.PanelGlass PanelWall44;
    private usu.widget.glass.PanelGlass PanelWall45;
    private usu.widget.glass.PanelGlass PanelWall46;
    private usu.widget.glass.PanelGlass PanelWall47;
    private usu.widget.glass.PanelGlass PanelWall48;
    private usu.widget.glass.PanelGlass PanelWall49;
    private usu.widget.glass.PanelGlass PanelWall5;
    private usu.widget.glass.PanelGlass PanelWall50;
    private usu.widget.glass.PanelGlass PanelWall51;
    private usu.widget.glass.PanelGlass PanelWall6;
    private usu.widget.glass.PanelGlass PanelWall7;
    private usu.widget.glass.PanelGlass PanelWall8;
    private usu.widget.glass.PanelGlass PanelWall9;
    private widget.ComboBox Rahang;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TNoPermintaan;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.RadioButton gigi11;
    private widget.RadioButton gigi12;
    private widget.RadioButton gigi13;
    private widget.RadioButton gigi14;
    private widget.RadioButton gigi15;
    private widget.RadioButton gigi16;
    private widget.RadioButton gigi17;
    private widget.RadioButton gigi18;
    private widget.RadioButton gigi21;
    private widget.RadioButton gigi22;
    private widget.RadioButton gigi23;
    private widget.RadioButton gigi24;
    private widget.RadioButton gigi25;
    private widget.RadioButton gigi26;
    private widget.RadioButton gigi27;
    private widget.RadioButton gigi28;
    private widget.RadioButton gigi31;
    private widget.RadioButton gigi32;
    private widget.RadioButton gigi33;
    private widget.RadioButton gigi34;
    private widget.RadioButton gigi35;
    private widget.RadioButton gigi36;
    private widget.RadioButton gigi37;
    private widget.RadioButton gigi38;
    private widget.RadioButton gigi41;
    private widget.RadioButton gigi42;
    private widget.RadioButton gigi43;
    private widget.RadioButton gigi44;
    private widget.RadioButton gigi45;
    private widget.RadioButton gigi46;
    private widget.RadioButton gigi47;
    private widget.RadioButton gigi48;
    private widget.RadioButton gigi51;
    private widget.RadioButton gigi52;
    private widget.RadioButton gigi53;
    private widget.RadioButton gigi54;
    private widget.RadioButton gigi55;
    private widget.RadioButton gigi61;
    private widget.RadioButton gigi62;
    private widget.RadioButton gigi63;
    private widget.RadioButton gigi64;
    private widget.RadioButton gigi65;
    private widget.RadioButton gigi71;
    private widget.RadioButton gigi72;
    private widget.RadioButton gigi73;
    private widget.RadioButton gigi74;
    private widget.RadioButton gigi75;
    private widget.RadioButton gigi81;
    private widget.RadioButton gigi82;
    private widget.RadioButton gigi83;
    private widget.RadioButton gigi84;
    private widget.RadioButton gigi85;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel11;
    private widget.Label jLabel15;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel27;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.Label label15;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JMenuItem ppBerkasDigital;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane6;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            if(TCari.getText().equals("")){
                ps=koneksi.prepareStatement(
                    "select reg_periksa.tgl_registrasi,reg_periksa.no_rawat,reg_periksa.status_lanjut,reg_periksa.no_rkm_medis,pasien.nm_pasien, "+
                    "pemeriksaan_gigi.kd_dokter,dokter.nm_dokter,pemeriksaan_gigi.bagian,pemeriksaan_gigi.diagnosa,pemeriksaan_gigi.kd_diagnosa, "+
                    "pemeriksaan_gigi.hasil,pemeriksaan_gigi.catatan,pemeriksaan_gigi.tanggal,pemeriksaan_gigi.noorder,pemeriksaan_gigi.rahang from pemeriksaan_gigi inner join reg_periksa on pemeriksaan_gigi.no_rawat=reg_periksa.no_rawat  "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join dokter on pemeriksaan_gigi.kd_dokter=dokter.kd_dokter "+
                    "where reg_periksa.tgl_registrasi between ? and ? order by reg_periksa.tgl_registrasi,reg_periksa.status_lanjut");
            }else{
                ps=koneksi.prepareStatement(
                    "select reg_periksa.tgl_registrasi,reg_periksa.no_rawat,reg_periksa.status_lanjut,reg_periksa.no_rkm_medis,pasien.nm_pasien, "+
                    "pemeriksaan_gigi.kd_dokter,dokter.nm_dokter,pemeriksaan_gigi.bagian,pemeriksaan_gigi.diagnosa,pemeriksaan_gigi.kd_diagnosa, "+
                    "pemeriksaan_gigi.hasil,pemeriksaan_gigi.catatan,pemeriksaan_gigi.tanggal,pemeriksaan_gigi.noorder,pemeriksaan_gigi.rahang from pemeriksaan_gigi inner join reg_periksa on pemeriksaan_gigi.no_rawat=reg_periksa.no_rawat  "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join dokter on pemeriksaan_gigi.kd_dokter=dokter.kd_dokter "+
                    "where reg_periksa.tgl_registrasi between ? and ? and reg_periksa.status_lanjut like ? or "+
                    "reg_periksa.tgl_registrasi between ? and ? and reg_periksa.no_rkm_medis like ? or "+
                    "reg_periksa.tgl_registrasi between ? and ? and pasien.nm_pasien like ? or "+
                    "reg_periksa.tgl_registrasi between ? and ? and pemeriksaan_gigi.kd_dokter like ? or "+
                    "reg_periksa.tgl_registrasi between ? and ? and dokter.nm_dokter like ? or "+
                    "reg_periksa.tgl_registrasi between ? and ? and pemeriksaan_gigi.kd_diagnosa like ? or "+
                    "reg_periksa.tgl_registrasi between ? and ? and pemeriksaan_gigi.diagnosa like ? or "+
                    "reg_periksa.tgl_registrasi between ? and ? and reg_periksa.no_rawat like ? "+
                    "order by reg_periksa.tgl_registrasi,reg_periksa.status_lanjut");
            }
            try {
                if(TCari.getText().equals("")){
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                }else{
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(5,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(8,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(9,"%"+TCari.getText()+"%");
                    ps.setString(10,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(11,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(12,"%"+TCari.getText()+"%");
                    ps.setString(13,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(14,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(15,"%"+TCari.getText()+"%");
                    ps.setString(16,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(17,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(18,"%"+TCari.getText()+"%");
                    ps.setString(19,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(20,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(21,"%"+TCari.getText()+"%");
                    ps.setString(22,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(23,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(24,"%"+TCari.getText()+"%");
//                    ps.setString(25,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
//                    ps.setString(26,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
//                    ps.setString(27,"%"+TCari.getText()+"%");
//                    ps.setString(28,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
//                    ps.setString(29,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
//                    ps.setString(30,"%"+TCari.getText()+"%");
//                    ps.setString(31,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
//                    ps.setString(32,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
//                    ps.setString(33,"%"+TCari.getText()+"%");
//                    ps.setString(34,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
//                    ps.setString(35,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
//                    ps.setString(36,"%"+TCari.getText()+"%");
                }   
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("tgl_registrasi"),rs.getString("status_lanjut"),rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("kd_dokter"),rs.getString("nm_dokter"),rs.getString("bagian"),rs.getString("diagnosa"),
                        rs.getString("kd_diagnosa"),rs.getString("hasil"),rs.getString("catatan"),rs.getString("tanggal"),rs.getString("noorder"),rs.getString("rahang")
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
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(noorder,4),signed)),0) from pemeriksaan_gigi where tanggal='"+Valid.SetTgl(Tanggal.getSelectedItem()+"")+"' ","OD"+Valid.SetTgl(Tanggal.getSelectedItem()+"").replaceAll("-",""),5,TNoPermintaan);
        Catatan.setText("");
        DiagnosaUtama.setText("");
        KodeDiagnosaUtama.setText("");
        Catatan.setText("");
        Hasil.requestFocus();
//        gigi11.setSelected(true);
//        gigi12.setSelected(true);
//        gigi13.setSelected(true);
//        gigi14.setSelected(true);
//        gigi15.setSelected(true);
//        gigi16.setSelected(true);
//        gigi17.setSelected(true);
//        gigi18.setSelected(true);
//        gigi21.setSelected(true);
//        gigi22.setSelected(true);
//        gigi23.setSelected(true);
//        gigi24.setSelected(true);
//        gigi25.setSelected(true);
//        gigi26.setSelected(true);
//        gigi27.setSelected(true);
//        gigi28.setSelected(true);
//        gigi31.setSelected(true);
//        gigi32.setSelected(true);
//        gigi33.setSelected(true);
//        gigi34.setSelected(true);
//        gigi35.setSelected(true);
//        gigi36.setSelected(true);
//        gigi37.setSelected(true);
//        gigi38.setSelected(true);
//        gigi41.setSelected(true);
//        gigi42.setSelected(true);
//        gigi43.setSelected(true);
//        gigi44.setSelected(true);
//        gigi45.setSelected(true);
//        gigi46.setSelected(true);
//        gigi47.setSelected(true);
//        gigi48.setSelected(true);
//        gigi51.setSelected(true);
//        gigi52.setSelected(true);
//        gigi53.setSelected(true);
//        gigi54.setSelected(true);
//        gigi55.setSelected(true);
//        gigi61.setSelected(true);
//        gigi62.setSelected(true);
//        gigi63.setSelected(true);
//        gigi64.setSelected(true);
//        gigi65.setSelected(true);
//        gigi71.setSelected(true);
//        gigi72.setSelected(true);
//        gigi73.setSelected(true);
//        gigi74.setSelected(true);
//        gigi75.setSelected(true);
//        gigi81.setSelected(true);
//        gigi82.setSelected(true);
//        gigi83.setSelected(true);
//        gigi84.setSelected(true);
//        gigi85.setSelected(true);
        
    } 

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());  
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());  
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());  
            KodeDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());  
            NamaDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("1.1")){
                gigi11.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("1.2")){
                gigi12.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("1.3")){
                gigi13.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("1.4")){
                gigi14.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("1.5")){
                gigi15.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("1.6")){
                gigi16.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("1.7")){
                gigi17.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("1.8")){
                gigi18.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("2.1")){
                gigi21.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("2.2")){
                gigi22.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("2.3")){
                gigi23.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("2.4")){
                gigi24.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("2.5")){
                gigi25.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("2.6")){
                gigi26.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("2.7")){
                gigi27.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("2.8")){
                gigi28.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("3.1")){
                gigi31.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("3.2")){
                gigi32.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("3.3")){
                gigi33.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("3.4")){
                gigi34.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("3.5")){
                gigi35.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("3.6")){
                gigi36.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("3.7")){
                gigi37.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("3.8")){
                gigi38.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("4.1")){
                gigi41.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("4.2")){
                gigi42.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("4.3")){
                gigi43.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("4.4")){
                gigi44.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("4.5")){
                gigi45.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("4.6")){
                gigi46.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("4.7")){
                gigi47.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("4.8")){
                gigi48.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("5.1")){
                gigi51.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("5.2")){
                gigi52.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("5.3")){
                gigi53.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("5.4")){
                gigi54.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("5.5")){
                gigi55.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("6.1")){
                gigi61.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("6.2")){
                gigi62.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("6.3")){
                gigi63.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("6.4")){
                gigi64.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("6.5")){
                gigi65.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("7.1")){
                gigi71.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("7.2")){
                gigi72.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("7.3")){
                gigi73.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("7.4")){
                gigi74.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("7.5")){
                gigi75.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("8.1")){
                gigi81.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("8.2")){
                gigi82.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("8.3")){
                gigi83.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("8.4")){
                gigi84.setSelected(true);
            }
            if(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().equals("8.5")){
                gigi85.setSelected(true);
            }
            DiagnosaUtama.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());  
            KodeDiagnosaUtama.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            Hasil.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            Catatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            Valid.SetTgl2(Tanggal,tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            TNoPermintaan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            Rahang.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            
        }
    }

    private void isRawat() {
         Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat='"+TNoRw.getText()+"' ",TNoRM);
    }

    private void isPsien() {
        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis='"+TNoRM.getText()+"' ",TPasien);
    }
    
    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        Sequel.cariIsi("select tgl_registrasi from reg_periksa where no_rawat='"+norwt+"'", DTPCari1);
        DTPCari2.setDate(tgl2);    
        isRawat();
        isPsien();              
        ChkInput.setSelected(true);
        isForm();
        Hasil.requestFocus();
        try {
            ps=koneksi.prepareStatement(
                    "select diagnosa_pasien.kd_penyakit,penyakit.nm_penyakit,diagnosa_pasien.prioritas "+
                    "from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "+
                    "where diagnosa_pasien.no_rawat=? order by diagnosa_pasien.prioritas ");
            try {
                ps.setString(1,norwt);
                rs=ps.executeQuery();
                while(rs.next()){
                    if(rs.getInt("prioritas")==1){
                        KodeDiagnosaUtama.setText(rs.getString("kd_penyakit"));
                        DiagnosaUtama.setText(rs.getString("nm_penyakit"));
                    }
                    
//                    if(rs.getInt("prioritas")==2){
//                        KodeDiagnosaSekunder1.setText(rs.getString("kd_penyakit"));
//                        DiagnosaSekunder1.setText(rs.getString("nm_penyakit"));
//                    }
//                    
//                    if(rs.getInt("prioritas")==3){
//                        KodeDiagnosaSekunder2.setText(rs.getString("kd_penyakit"));
//                        DiagnosaSekunder2.setText(rs.getString("nm_penyakit"));
//                    }
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
            ps=koneksi.prepareStatement(
                    "select prosedur_pasien.kode,icd9.deskripsi_panjang, prosedur_pasien.prioritas "+
                    "from prosedur_pasien inner join icd9 on prosedur_pasien.kode=icd9.kode "+
                    "where prosedur_pasien.no_rawat=? order by prosedur_pasien.prioritas ");
            try {
                ps.setString(1,norwt);
                rs=ps.executeQuery();
                while(rs.next()){
//                    if(rs.getInt("prioritas")==1){
//                        KodeProsedurUtama.setText(rs.getString("kode"));
//                        ProsedurUtama.setText(rs.getString("deskripsi_panjang"));
//                    }
//                    
//                    if(rs.getInt("prioritas")==2){
//                        KodeProsedurSekunder1.setText(rs.getString("kode"));
//                        ProsedurSekunder1.setText(rs.getString("deskripsi_panjang"));
//                    }
//                    
//                    if(rs.getInt("prioritas")==3){
//                        KodeProsedurSekunder2.setText(rs.getString("kode"));
//                        ProsedurSekunder2.setText(rs.getString("deskripsi_panjang"));
//                    }
//                    
//                    if(rs.getInt("prioritas")==4){
//                        KodeProsedurSekunder3.setText(rs.getString("kode"));
//                        ProsedurSekunder3.setText(rs.getString("deskripsi_panjang"));
//                    }
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
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
//            PanelInput.setPreferredSize(new Dimension(WIDTH,this.getHeight()-122));
            PanelInput.setPreferredSize(new Dimension(WIDTH,350));
            scrollInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            scrollInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getdata_resume_pasien());
        BtnHapus.setEnabled(akses.getdata_resume_pasien());
        BtnEdit.setEnabled(akses.getdata_resume_pasien());
        BtnPrint.setEnabled(akses.getdata_resume_pasien()); 
        MnInputDiagnosa.setEnabled(akses.getdiagnosa_pasien());   
        ppBerkasDigital.setEnabled(akses.getberkas_digital_perawatan());    
        if(akses.getjml2()>=1){
            KodeDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KodeDokter.setText(akses.getkode());
            Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", NamaDokter,KodeDokter.getText());
            if(NamaDokter.getText().equals("")){
                KodeDokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan dokter...!!");
            }
        }            
    }

    
}
