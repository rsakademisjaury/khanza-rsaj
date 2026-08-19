/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgLhtBiaya.java
 *
 * Created on 12 Jul 10, 16:21:34
 */

package laporan;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import simrskhanza.DlgCariPoli;
import simrskhanza.DlgKabupaten;
import simrskhanza.DlgKecamatan;
import simrskhanza.DlgKelurahan;
import simrskhanza.DlgCariCaraBayar;

/**
 *
 * @author perpustakaan
 */
public final class DlgKunjunganRalan extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabMode2, tabMode3, tabMode4, tabMode5, tabMode6, tabMode7, tabMode8, tabMode9, tabMode10;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private DlgCariPoli poli=new DlgCariPoli(null,false);
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private DlgKabupaten kabupaten=new DlgKabupaten(null,false);
    private DlgKecamatan kecamatan=new DlgKecamatan(null,false);
    private DlgKelurahan kelurahan=new DlgKelurahan(null,false);
    private DlgCariCaraBayar penjab=new DlgCariCaraBayar(null,false);
    private int i=0,lama=0,baru=0,laki=0,per=0;   
    private String setbaru="",setlama="",umurlk="",umurpr="",kddiangnosa="",diagnosa="",pilihan="",status="";
    private StringBuilder htmlContent;
    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgKunjunganRalan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new Object[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Dokter Jaga"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        table1.setModel(tabMode);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        table1.setPreferredScrollableViewportSize(new Dimension(500,500));
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 10; i++) {
            TableColumn column = table1.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(35);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(40);
            }else if(i==8){
                column.setPreferredWidth(200);
            }else if(i==9){
                column.setPreferredWidth(200);
            }
        }
        terapkanGayaTabelModern(table1,false);
        
        tabMode2=new DefaultTableModel(null,new Object[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Dokter Jaga"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        table2.setModel(tabMode2);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        table2.setPreferredScrollableViewportSize(new Dimension(500,500));
        table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 10; i++) {
            TableColumn column = table2.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(35);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(40);
            }else if(i==8){
                column.setPreferredWidth(200);
            }else if(i==9){
                column.setPreferredWidth(200);
            }
        }
        terapkanGayaTabelModern(table2,false);
        
        tabMode3=new DefaultTableModel(null,new Object[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Dokter Jaga"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        table3.setModel(tabMode3);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        table3.setPreferredScrollableViewportSize(new Dimension(500,500));
        table3.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 10; i++) {
            TableColumn column = table3.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(35);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(40);
            }else if(i==8){
                column.setPreferredWidth(200);
            }else if(i==9){
                column.setPreferredWidth(200);
            }
        }
        terapkanGayaTabelModern(table3,false);

        tabMode4=new DefaultTableModel(null,new Object[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Dokter Jaga"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        inisialisasiTabelIGD(table4,tabMode4);

        tabMode5=new DefaultTableModel(null,new Object[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Dokter Jaga"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        inisialisasiTabelIGD(table5,tabMode5);

        tabMode6=new DefaultTableModel(null,new Object[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Dokter Jaga"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        inisialisasiTabelIGD(table6,tabMode6);

        tabMode7=new DefaultTableModel(null,new Object[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Dokter Jaga"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        inisialisasiTabelIGD(table7,tabMode7);

        tabMode8=new DefaultTableModel(null,new Object[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Dokter Jaga"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        inisialisasiTabelIGD(table8,tabMode8);

        tabMode9=new DefaultTableModel(null,new Object[]{"Keterangan","Jumlah","Keterangan","Jumlah"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        inisialisasiTabelRekapIGD(table9,tabMode9);

        tabMode10=new DefaultTableModel(null,new Object[]{"No.","Poliklinik / Unit","Lama","Baru","Jumlah","BPJS","Umum","Instansi"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        inisialisasiTabelRekapRawatJalan(table10,tabMode10);

        TCari.setDocument(new batasInput((int)90).getKata(TCari));
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampilSesuaiTabAktif();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampilSesuaiTabAktif();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampilSesuaiTabAktif();
                    }
                }
            });
        }  
        
        poli.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli.getTable().getSelectedRow()!= -1){
                    kdpoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),0).toString());
                    nmpoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),1).toString());
                }      
                kdpoli.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {poli.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });   
        
        penjab.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(penjab.getTable().getSelectedRow()!= -1){
                    kdpenjab.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),1).toString());
                    nmpenjab.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),2).toString());
                }      
                kdpenjab.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {penjab.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });   
        
        penjab.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    penjab.dispose();
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
                    nmkabupaten.setText(kabupaten.getTable().getValueAt(kabupaten.getTable().getSelectedRow(),0).toString());
                }      
                nmkabupaten.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {kabupaten.emptTeks();}
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
                    nmkecamatan.setText(kecamatan.getTable().getValueAt(kecamatan.getTable().getSelectedRow(),0).toString());
                }      
                nmkecamatan.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {kecamatan.emptTeks();}
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
                    nmkelurahan.setText(kelurahan.getTable().getValueAt(kelurahan.getTable().getSelectedRow(),0).toString());
                }      
                nmkelurahan.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {kelurahan.emptTeks();}
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
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){
                    kddokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    nmdokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                }      
                kddokter.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {dokter.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });   
        
        dokter.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    dokter.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
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

        TKd = new widget.TextBox();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        ppTampilkanBaru = new javax.swing.JMenuItem();
        ppTampilkanLama = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel7 = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.panelisi();
        label17 = new widget.Label();
        kdpoli = new widget.TextBox();
        nmpoli = new widget.TextBox();
        BtnSeek2 = new widget.Button();
        label19 = new widget.Label();
        kdpenjab = new widget.TextBox();
        nmpenjab = new widget.TextBox();
        BtnSeek3 = new widget.Button();
        label20 = new widget.Label();
        kddokter = new widget.TextBox();
        nmdokter = new widget.TextBox();
        BtnSeek4 = new widget.Button();
        label21 = new widget.Label();
        nmkabupaten = new widget.TextBox();
        BtnSeek5 = new widget.Button();
        label22 = new widget.Label();
        nmkecamatan = new widget.TextBox();
        BtnSeek6 = new widget.Button();
        BtnSeek7 = new widget.Button();
        nmkelurahan = new widget.TextBox();
        label23 = new widget.Label();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        table1 = new widget.Table();
        internalFrame3 = new widget.InternalFrame();
        Scroll1 = new widget.ScrollPane();
        table2 = new widget.Table();
        internalFrame4 = new widget.InternalFrame();
        Scroll2 = new widget.ScrollPane();
        table3 = new widget.Table();
        internalFrame10 = new widget.InternalFrame();
        Scroll8 = new widget.ScrollPane();
        table9 = new widget.Table();
        internalFrame11 = new widget.InternalFrame();
        Scroll9 = new widget.ScrollPane();
        table10 = new widget.Table();
        internalFrame5 = new widget.InternalFrame();
        Scroll3 = new widget.ScrollPane();
        table4 = new widget.Table();
        internalFrame6 = new widget.InternalFrame();
        Scroll4 = new widget.ScrollPane();
        table5 = new widget.Table();
        internalFrame7 = new widget.InternalFrame();
        Scroll5 = new widget.ScrollPane();
        table6 = new widget.Table();
        internalFrame8 = new widget.InternalFrame();
        Scroll6 = new widget.ScrollPane();
        table7 = new widget.Table();
        internalFrame9 = new widget.InternalFrame();
        Scroll7 = new widget.ScrollPane();
        table8 = new widget.Table();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        ppTampilkanBaru.setBackground(new java.awt.Color(255, 255, 254));
        ppTampilkanBaru.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppTampilkanBaru.setForeground(java.awt.Color.darkGray);
        ppTampilkanBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppTampilkanBaru.setText("Tampilkan Pasien Baru");
        ppTampilkanBaru.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppTampilkanBaru.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppTampilkanBaru.setName("ppTampilkanBaru"); // NOI18N
        ppTampilkanBaru.setPreferredSize(new java.awt.Dimension(175, 25));
        ppTampilkanBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppTampilkanBaruBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppTampilkanBaru);

        ppTampilkanLama.setBackground(new java.awt.Color(255, 255, 254));
        ppTampilkanLama.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppTampilkanLama.setForeground(java.awt.Color.darkGray);
        ppTampilkanLama.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppTampilkanLama.setText("Tampilkan Pasien Lama");
        ppTampilkanLama.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppTampilkanLama.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppTampilkanLama.setName("ppTampilkanLama"); // NOI18N
        ppTampilkanLama.setPreferredSize(new java.awt.Dimension(175, 25));
        ppTampilkanLama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppTampilkanLamaBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppTampilkanLama);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Kunjungan Rawat Jalan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass5.add(label11);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(25, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass5.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(155, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass5.add(TCari);

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
        panelGlass5.add(BtnCari);

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
        panelGlass5.add(BtnAll);

        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(30, 23));
        panelGlass5.add(jLabel7);

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
        panelGlass5.add(BtnPrint);

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
        panelGlass5.add(BtnKeluar);

        internalFrame1.add(panelGlass5, java.awt.BorderLayout.PAGE_END);

        PanelInput.setBackground(new java.awt.Color(255, 255, 255));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
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
        FormInput.setPreferredSize(new java.awt.Dimension(100, 104));
        FormInput.setLayout(null);

        label17.setText("Unit/Poli :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(35, 23));
        FormInput.add(label17);
        label17.setBounds(0, 10, 75, 23);

        kdpoli.setEditable(false);
        kdpoli.setName("kdpoli"); // NOI18N
        kdpoli.setPreferredSize(new java.awt.Dimension(75, 23));
        kdpoli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpoliKeyPressed(evt);
            }
        });
        FormInput.add(kdpoli);
        kdpoli.setBounds(78, 10, 85, 23);

        nmpoli.setEditable(false);
        nmpoli.setName("nmpoli"); // NOI18N
        nmpoli.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmpoli);
        nmpoli.setBounds(165, 10, 228, 23);

        BtnSeek2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek2.setMnemonic('3');
        BtnSeek2.setToolTipText("Alt+3");
        BtnSeek2.setName("BtnSeek2"); // NOI18N
        BtnSeek2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek2ActionPerformed(evt);
            }
        });
        BtnSeek2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek2KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek2);
        BtnSeek2.setBounds(396, 10, 28, 23);

        label19.setText("Cara Bayar :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label19);
        label19.setBounds(0, 70, 75, 23);

        kdpenjab.setEditable(false);
        kdpenjab.setName("kdpenjab"); // NOI18N
        kdpenjab.setPreferredSize(new java.awt.Dimension(75, 23));
        kdpenjab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpenjabKeyPressed(evt);
            }
        });
        FormInput.add(kdpenjab);
        kdpenjab.setBounds(78, 70, 85, 23);

        nmpenjab.setEditable(false);
        nmpenjab.setName("nmpenjab"); // NOI18N
        nmpenjab.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmpenjab);
        nmpenjab.setBounds(165, 70, 228, 23);

        BtnSeek3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek3.setMnemonic('3');
        BtnSeek3.setToolTipText("Alt+3");
        BtnSeek3.setName("BtnSeek3"); // NOI18N
        BtnSeek3.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek3ActionPerformed(evt);
            }
        });
        BtnSeek3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek3KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek3);
        BtnSeek3.setBounds(396, 70, 28, 23);

        label20.setText("Dokter :");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(35, 23));
        FormInput.add(label20);
        label20.setBounds(0, 40, 75, 23);

        kddokter.setEditable(false);
        kddokter.setName("kddokter"); // NOI18N
        kddokter.setPreferredSize(new java.awt.Dimension(75, 23));
        kddokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kddokterKeyPressed(evt);
            }
        });
        FormInput.add(kddokter);
        kddokter.setBounds(78, 40, 85, 23);

        nmdokter.setEditable(false);
        nmdokter.setName("nmdokter"); // NOI18N
        nmdokter.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmdokter);
        nmdokter.setBounds(165, 40, 228, 23);

        BtnSeek4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek4.setMnemonic('3');
        BtnSeek4.setToolTipText("Alt+3");
        BtnSeek4.setName("BtnSeek4"); // NOI18N
        BtnSeek4.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek4ActionPerformed(evt);
            }
        });
        BtnSeek4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek4KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek4);
        BtnSeek4.setBounds(396, 40, 28, 23);

        label21.setText("Kab/Kota :");
        label21.setName("label21"); // NOI18N
        label21.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label21);
        label21.setBounds(429, 10, 87, 23);

        nmkabupaten.setEditable(false);
        nmkabupaten.setName("nmkabupaten"); // NOI18N
        nmkabupaten.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkabupaten);
        nmkabupaten.setBounds(519, 10, 260, 23);

        BtnSeek5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek5.setMnemonic('3');
        BtnSeek5.setToolTipText("Alt+3");
        BtnSeek5.setName("BtnSeek5"); // NOI18N
        BtnSeek5.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek5ActionPerformed(evt);
            }
        });
        BtnSeek5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek5KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek5);
        BtnSeek5.setBounds(782, 10, 28, 23);

        label22.setText("Kecamatan :");
        label22.setName("label22"); // NOI18N
        label22.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label22);
        label22.setBounds(429, 40, 87, 23);

        nmkecamatan.setEditable(false);
        nmkecamatan.setName("nmkecamatan"); // NOI18N
        nmkecamatan.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkecamatan);
        nmkecamatan.setBounds(519, 40, 260, 23);

        BtnSeek6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek6.setMnemonic('3');
        BtnSeek6.setToolTipText("Alt+3");
        BtnSeek6.setName("BtnSeek6"); // NOI18N
        BtnSeek6.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek6ActionPerformed(evt);
            }
        });
        BtnSeek6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek6KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek6);
        BtnSeek6.setBounds(782, 40, 28, 23);

        BtnSeek7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek7.setMnemonic('3');
        BtnSeek7.setToolTipText("Alt+3");
        BtnSeek7.setName("BtnSeek7"); // NOI18N
        BtnSeek7.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek7ActionPerformed(evt);
            }
        });
        BtnSeek7.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek7KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek7);
        BtnSeek7.setBounds(782, 70, 28, 23);

        nmkelurahan.setEditable(false);
        nmkelurahan.setName("nmkelurahan"); // NOI18N
        nmkelurahan.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkelurahan);
        nmkelurahan.setBounds(519, 70, 260, 23);

        label23.setText("Kelurahan :");
        label23.setName("label23"); // NOI18N
        label23.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label23);
        label23.setBounds(429, 70, 87, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        TabRawat.setBackground(new java.awt.Color(255, 255, 254));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        table1.setComponentPopupMenu(jPopupMenu1);
        table1.setName("table1"); // NOI18N
        Scroll.setViewportView(table1);

        internalFrame2.add(Scroll, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Seluruh Kunjungan", internalFrame2);

        internalFrame3.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll1.setComponentPopupMenu(jPopupMenu1);
        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);

        table2.setComponentPopupMenu(jPopupMenu1);
        table2.setName("table2"); // NOI18N
        Scroll1.setViewportView(table2);

        internalFrame3.add(Scroll1, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Kunjungan Non Batal", internalFrame3);

        internalFrame4.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame4.setBorder(null);
        internalFrame4.setName("internalFrame4"); // NOI18N
        internalFrame4.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll2.setComponentPopupMenu(jPopupMenu1);
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        table3.setComponentPopupMenu(jPopupMenu1);
        table3.setName("table3"); // NOI18N
        Scroll2.setViewportView(table3);

        internalFrame4.add(Scroll2, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Pasien Geriatri", internalFrame4);

        internalFrame10.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame10.setBorder(null);
        internalFrame10.setName("internalFrame10"); // NOI18N
        internalFrame10.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll8.setComponentPopupMenu(jPopupMenu1);
        Scroll8.setName("Scroll8"); // NOI18N
        Scroll8.setOpaque(true);

        table9.setComponentPopupMenu(jPopupMenu1);
        table9.setName("table9"); // NOI18N
        Scroll8.setViewportView(table9);

        internalFrame10.add(Scroll8, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Rekap IGD", internalFrame10);

        internalFrame5.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame5.setBorder(null);
        internalFrame5.setName("internalFrame5"); // NOI18N
        internalFrame5.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll3.setComponentPopupMenu(jPopupMenu1);
        Scroll3.setName("Scroll3"); // NOI18N
        Scroll3.setOpaque(true);

        table4.setComponentPopupMenu(jPopupMenu1);
        table4.setName("table4"); // NOI18N
        Scroll3.setViewportView(table4);

        internalFrame5.add(Scroll3, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("IGD Bedah", internalFrame5);

        internalFrame6.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame6.setBorder(null);
        internalFrame6.setName("internalFrame6"); // NOI18N
        internalFrame6.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll4.setComponentPopupMenu(jPopupMenu1);
        Scroll4.setName("Scroll4"); // NOI18N
        Scroll4.setOpaque(true);

        table5.setComponentPopupMenu(jPopupMenu1);
        table5.setName("table5"); // NOI18N
        Scroll4.setViewportView(table5);

        internalFrame6.add(Scroll4, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("IGD Non Bedah", internalFrame6);

        internalFrame7.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame7.setBorder(null);
        internalFrame7.setName("internalFrame7"); // NOI18N
        internalFrame7.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll5.setComponentPopupMenu(jPopupMenu1);
        Scroll5.setName("Scroll5"); // NOI18N
        Scroll5.setOpaque(true);

        table6.setComponentPopupMenu(jPopupMenu1);
        table6.setName("table6"); // NOI18N
        Scroll5.setViewportView(table6);

        internalFrame7.add(Scroll5, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("IGD BPJS", internalFrame7);

        internalFrame8.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame8.setBorder(null);
        internalFrame8.setName("internalFrame8"); // NOI18N
        internalFrame8.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll6.setComponentPopupMenu(jPopupMenu1);
        Scroll6.setName("Scroll6"); // NOI18N
        Scroll6.setOpaque(true);

        table7.setComponentPopupMenu(jPopupMenu1);
        table7.setName("table7"); // NOI18N
        Scroll6.setViewportView(table7);

        internalFrame8.add(Scroll6, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("IGD Umum", internalFrame8);

        internalFrame9.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame9.setBorder(null);
        internalFrame9.setName("internalFrame9"); // NOI18N
        internalFrame9.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll7.setComponentPopupMenu(jPopupMenu1);
        Scroll7.setName("Scroll7"); // NOI18N
        Scroll7.setOpaque(true);

        table8.setComponentPopupMenu(jPopupMenu1);
        table8.setName("table8"); // NOI18N
        Scroll7.setViewportView(table8);

        internalFrame9.add(Scroll7, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("IGD Instansi", internalFrame9);


        internalFrame11.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame11.setBorder(null);
        internalFrame11.setName("internalFrame11"); // NOI18N
        internalFrame11.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll9.setComponentPopupMenu(jPopupMenu1);
        Scroll9.setName("Scroll9"); // NOI18N
        Scroll9.setOpaque(true);

        table10.setComponentPopupMenu(jPopupMenu1);
        table10.setName("table10"); // NOI18N
        Scroll9.setViewportView(table10);

        internalFrame11.add(Scroll9, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Rekap Rawat Jalan", internalFrame11);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {            
            File g = new File("file2.css");            
            BufferedWriter bg = new BufferedWriter(new FileWriter(g));
            bg.write(
                    ".isi td{border-right: 1px solid #e2e7dd;font: 11px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                    ".isi2 td{font: 11px tahoma;height:12px;background: #ffffff;color:#323232;}"+                    
                    ".isi3 td{border-right: 1px solid #e2e7dd;font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                    ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
            );
            bg.close();            File f;            
            BufferedWriter bw; 
            DefaultTableModel modelAktif = getModelAktif();
            JTable tableAktif = getTableAktif();
            String judulLaporan = getJudulLaporanAktif();
            String judulDialog = getJudulDialogLaporanAktif();
            int tipeCetak = getTipeCetakAktif();

            pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih laporan..!","Pilihan Cetak",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Laporan 1 (HTML)","Laporan 2 (WPS)","Laporan 3 (CSV)","Laporan 4 (Jasper)"},"Laporan 1 (HTML)");
            if(pilihan==null){
                this.setCursor(Cursor.getDefaultCursor());
                return;
            }
            switch (pilihan) {
                case "Laporan 1 (HTML)":
                        htmlContent = new StringBuilder();
                        appendHtmlRows(modelAktif, htmlContent, tipeCetak);

                        f = new File("KunjunganRalan.html");            
                        bw = new BufferedWriter(new FileWriter(f));            
                        bw.write("<html>"+
                                    "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                    "<body>"+
                                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                            "<tr class='isi2'>"+
                                                "<td valign='top' align='center'>"+
                                                    "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                                    akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                                    akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                                    "<font size='2' face='Tahoma'>"+judulLaporan+" PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+        
                                                "</td>"+
                                           "</tr>"+
                                        "</table>"+
                                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                            htmlContent.toString()+
                                        "</table>"+
                                    "</body>"+                   
                                 "</html>"
                        );

                        bw.close();                         
                        Desktop.getDesktop().browse(f.toURI());
                    break;
                case "Laporan 2 (WPS)":
                        htmlContent = new StringBuilder();
                        appendHtmlRows(modelAktif, htmlContent, tipeCetak);
                        f = new File("KunjunganRalan.wps");            
                        bw = new BufferedWriter(new FileWriter(f));            
                        bw.write("<html>"+
                                    "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                    "<body>"+
                                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                            "<tr class='isi2'>"+
                                                "<td valign='top' align='center'>"+
                                                    "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                                    akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                                    akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                                    "<font size='2' face='Tahoma'>"+judulLaporan+" PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+        
                                                "</td>"+
                                           "</tr>"+
                                        "</table>"+
                                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                            htmlContent.toString()+
                                        "</table>"+
                                    "</body>"+                   
                                 "</html>"
                        );

                        bw.close();                         
                        Desktop.getDesktop().browse(f.toURI());
                    break;
                case "Laporan 3 (CSV)":
                        htmlContent = new StringBuilder();
                        appendCsvRows(modelAktif, htmlContent, tipeCetak);

                        f = new File("KunjunganRalan.csv");            
                        bw = new BufferedWriter(new FileWriter(f));            
                        bw.write(htmlContent.toString());

                        bw.close();                         
                        Desktop.getDesktop().browse(f.toURI());
                    break; 
                case "Laporan 4 (Jasper)":
                        if(tipeCetak!=0){
                            JOptionPane.showMessageDialog(null,"Laporan Jasper untuk tab rekap belum digunakan. Silakan gunakan HTML/WPS/CSV.");
                            tableAktif.requestFocus();
                            break;
                        }
                        Map<String, Object> param = new HashMap<>();         
                        param.put("namars",akses.getnamars());
                        param.put("alamatrs",akses.getalamatrs());
                        param.put("kotars",akses.getkabupatenrs());
                        param.put("propinsirs",akses.getpropinsirs());
                        param.put("kontakrs",akses.getkontakrs());
                        param.put("emailrs",akses.getemailrs());   
                        param.put("periode",Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem());   
                        param.put("lama",lama);   
                        param.put("baru",baru);   
                        param.put("total",(lama+baru));   
                        param.put("laki",laki);   
                        param.put("perempuan",per);   
                        param.put("tanggal",Tgl2.getDate());   
                        Sequel.queryu("delete from temporary where temp37='"+akses.getalamatip()+"'");
                        if(modelAktif.getRowCount()>0){
                            for(int r=0;r<modelAktif.getRowCount();r++){ 
                                Sequel.menyimpan("temporary","'"+r+"','"+
                                                modelAktif.getValueAt(r,0).toString()+"','"+
                                                modelAktif.getValueAt(r,1).toString()+"','"+
                                                modelAktif.getValueAt(r,2).toString()+"','"+
                                                modelAktif.getValueAt(r,3).toString()+"','"+
                                                modelAktif.getValueAt(r,4).toString()+"','"+
                                                modelAktif.getValueAt(r,5).toString()+"','"+
                                                modelAktif.getValueAt(r,6).toString()+"','"+
                                                modelAktif.getValueAt(r,7).toString()+"','"+
                                                modelAktif.getValueAt(r,8).toString()+"','"+
                                                modelAktif.getValueAt(r,9).toString()+"','','','','','','','','','','','','','','','','','','','','','','','','','','','"+akses.getalamatip()+"'","Rekap Nota Pembayaran");
                            }
                            Valid.MyReportqry("rptKunjunganRalan.jasper","report",judulDialog,"select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
                        }else{
                            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                            tableAktif.requestFocus();
                        }
                    break; 
            }                 
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }     
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            //Valid.pindah(evt, BtnHapus, BtnAll);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnKeluar,TKd);}
}//GEN-LAST:event_BtnKeluarKeyPressed

private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
       tampilSesuaiTabAktif();
}//GEN-LAST:event_BtnCariActionPerformed

private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            tampilSesuaiTabAktif();
            this.setCursor(Cursor.getDefaultCursor());
        }else{
            Valid.pindah(evt, TKd, BtnPrint);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        kdpoli.setText("");
        nmpoli.setText("");
        kddokter.setText("");
        nmdokter.setText("");
        kdpenjab.setText("");
        nmpenjab.setText("");
        nmkabupaten.setText("");
        nmkecamatan.setText("");
        nmkelurahan.setText("");
        status="";
        tampilSesuaiTabAktif();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void kdpoliKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpoliKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select poliklinik.nm_poli from poliklinik where poliklinik.kd_poli=?", nmpoli,kdpoli.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdpoliKeyPressed

    private void BtnSeek2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek2ActionPerformed
        poli.isCek();
        poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        poli.setLocationRelativeTo(internalFrame1);
        poli.setAlwaysOnTop(false);
        poli.setVisible(true);
    }//GEN-LAST:event_BtnSeek2ActionPerformed

    private void BtnSeek2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek2KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek2KeyPressed

    private void kdpenjabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpenjabKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdpenjabKeyPressed

    private void BtnSeek3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek3ActionPerformed
        penjab.isCek();
        penjab.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penjab.setLocationRelativeTo(internalFrame1);
        penjab.setAlwaysOnTop(false);
        penjab.setVisible(true);
    }//GEN-LAST:event_BtnSeek3ActionPerformed

    private void BtnSeek3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek3KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek3KeyPressed

    private void kddokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kddokterKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kddokterKeyPressed

    private void BtnSeek4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek4ActionPerformed
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnSeek4ActionPerformed

    private void BtnSeek4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSeek4KeyPressed

    private void BtnSeek5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek5ActionPerformed
        kabupaten.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kabupaten.setLocationRelativeTo(internalFrame1);
        kabupaten.setAlwaysOnTop(false);
        kabupaten.setVisible(true);
    }//GEN-LAST:event_BtnSeek5ActionPerformed

    private void BtnSeek5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek5KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSeek5KeyPressed

    private void BtnSeek6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek6ActionPerformed
        kecamatan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kecamatan.setLocationRelativeTo(internalFrame1);
        kecamatan.setAlwaysOnTop(false);
        kecamatan.setVisible(true);
    }//GEN-LAST:event_BtnSeek6ActionPerformed

    private void BtnSeek6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek6KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSeek6KeyPressed

    private void BtnSeek7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek7ActionPerformed
        kelurahan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kelurahan.setLocationRelativeTo(internalFrame1);
        kelurahan.setAlwaysOnTop(false);
        kelurahan.setVisible(true);
    }//GEN-LAST:event_BtnSeek7ActionPerformed

    private void BtnSeek7KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek7KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSeek7KeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        tampilSesuaiTabAktif();
    }//GEN-LAST:event_TabRawatMouseClicked

    private void ppTampilkanBaruBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppTampilkanBaruBtnPrintActionPerformed
        status="Baru";
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_ppTampilkanBaruBtnPrintActionPerformed

    private void ppTampilkanLamaBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppTampilkanLamaBtnPrintActionPerformed
        status="Lama";
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_ppTampilkanLamaBtnPrintActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgKunjunganRalan dialog = new DlgKunjunganRalan(new javax.swing.JFrame(), true);
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
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSeek2;
    private widget.Button BtnSeek3;
    private widget.Button BtnSeek4;
    private widget.Button BtnSeek5;
    private widget.Button BtnSeek6;
    private widget.Button BtnSeek7;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll2;
    private widget.ScrollPane Scroll3;
    private widget.ScrollPane Scroll4;
    private widget.ScrollPane Scroll5;
    private widget.ScrollPane Scroll6;
    private widget.ScrollPane Scroll7;
    private widget.ScrollPane Scroll8;
    private widget.ScrollPane Scroll9;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.InternalFrame internalFrame4;
    private widget.InternalFrame internalFrame5;
    private widget.InternalFrame internalFrame6;
    private widget.InternalFrame internalFrame7;
    private widget.InternalFrame internalFrame8;
    private widget.InternalFrame internalFrame9;
    private widget.InternalFrame internalFrame10;
    private widget.InternalFrame internalFrame11;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox kddokter;
    private widget.TextBox kdpenjab;
    private widget.TextBox kdpoli;
    private widget.Label label11;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.Label label21;
    private widget.Label label22;
    private widget.Label label23;
    private widget.TextBox nmdokter;
    private widget.TextBox nmkabupaten;
    private widget.TextBox nmkecamatan;
    private widget.TextBox nmkelurahan;
    private widget.TextBox nmpenjab;
    private widget.TextBox nmpoli;
    private widget.panelisi panelGlass5;
    private javax.swing.JMenuItem ppTampilkanBaru;
    private javax.swing.JMenuItem ppTampilkanLama;
    private widget.Table table1;
    private widget.Table table2;
    private widget.Table table3;
    private widget.Table table4;
    private widget.Table table5;
    private widget.Table table6;
    private widget.Table table7;
    private widget.Table table8;
    private widget.Table table9;
    private widget.Table table10;
    // End of variables declaration//GEN-END:variables

    private class TabelModernRenderer extends DefaultTableCellRenderer {
        private final boolean rekap;
        TabelModernRenderer(boolean rekap){
            this.rekap=rekap;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl=(JLabel)super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            lbl.setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
            lbl.setFont(new Font("Segoe UI",Font.PLAIN,12));
            lbl.setForeground(new Color(60,72,88));
            lbl.setBackground(Color.WHITE);

            if(rekap){
                if(table.getColumnCount()==4){
                    String keterangan = (column<2)
                            ? (table.getValueAt(row,0)!=null ? table.getValueAt(row,0).toString().trim() : "")
                            : (table.getValueAt(row,2)!=null ? table.getValueAt(row,2).toString().trim() : "");
                    boolean levelUtama = keterangan.startsWith("• IGD");
                    boolean levelSub = keterangan.startsWith("◦ ");
                    boolean levelDetail = keterangan.startsWith("▪ ");

                    if(levelUtama){
                        lbl.setBackground(new Color(245,247,250));
                        lbl.setForeground(new Color(27,43,65));
                        lbl.setFont(new Font("Segoe UI Semibold",Font.PLAIN,13));
                    }else if(levelSub){
                        lbl.setBackground(row%2==0 ? Color.WHITE : new Color(249,250,252));
                        lbl.setForeground(new Color(55,65,81));
                        lbl.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
                    }else if(levelDetail){
                        lbl.setBackground(row%2==0 ? Color.WHITE : new Color(249,250,252));
                        lbl.setForeground(new Color(75,85,99));
                        lbl.setFont(new Font("Segoe UI",Font.PLAIN,12));
                    }else if(row%2==0){
                        lbl.setBackground(Color.WHITE);
                    }else{
                        lbl.setBackground(new Color(249,250,252));
                    }

                    if(column==0 || column==2){
                        int kiri = 12;
                        if(levelUtama){
                            kiri = 18;
                        }else if(levelSub){
                            kiri = 38;
                        }else if(levelDetail){
                            kiri = 64;
                        }
                        int kanan = (column==0)?16:8;
                        lbl.setBorder(BorderFactory.createEmptyBorder(0,kiri,0,kanan));
                        lbl.setHorizontalAlignment(JLabel.LEFT);
                    }else{
                        int kiri = (column==1)?8:16;
                        lbl.setBorder(BorderFactory.createEmptyBorder(0,kiri,0,12));
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                    }
                }else if(table.getColumnCount()==2){
                    String keterangan = table.getValueAt(row,0)!=null ? table.getValueAt(row,0).toString().trim() : "";
                    boolean levelUtama = keterangan.startsWith("• IGD");
                    boolean levelSub = keterangan.startsWith("◦ ");
                    boolean levelDetail = keterangan.startsWith("▪ ");
                    boolean pemisah = keterangan.replace("-","").trim().isEmpty();

                    if(pemisah){
                        lbl.setBackground(new Color(250,251,253));
                        lbl.setForeground(new Color(160,174,192));
                        lbl.setFont(new Font("Segoe UI",Font.PLAIN,12));
                    }else if(levelUtama){
                        lbl.setBackground(new Color(245,247,250));
                        lbl.setForeground(new Color(27,43,65));
                        lbl.setFont(new Font("Segoe UI Semibold",Font.PLAIN,13));
                    }else if(levelSub){
                        lbl.setBackground(row%2==0 ? Color.WHITE : new Color(249,250,252));
                        lbl.setForeground(new Color(55,65,81));
                        lbl.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
                    }else if(levelDetail){
                        lbl.setBackground(row%2==0 ? Color.WHITE : new Color(249,250,252));
                        lbl.setForeground(new Color(75,85,99));
                        lbl.setFont(new Font("Segoe UI",Font.PLAIN,12));
                    }else if(row%2==0){
                        lbl.setBackground(Color.WHITE);
                    }else{
                        lbl.setBackground(new Color(249,250,252));
                    }

                    if(column==0){
                        int kiri = 12;
                        if(levelUtama){
                            kiri = 18;
                        }else if(levelSub){
                            kiri = 38;
                        }else if(levelDetail){
                            kiri = 64;
                        }
                        lbl.setBorder(BorderFactory.createEmptyBorder(0,kiri,0,8));
                        lbl.setHorizontalAlignment(JLabel.LEFT);
                    }else{
                        lbl.setBorder(BorderFactory.createEmptyBorder(0,8,0,12));
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                    }
                }else{
                    String namaKolom1 = table.getColumnCount()>1 && table.getValueAt(row,1)!=null ? table.getValueAt(row,1).toString().trim() : "";
                    String nilaiKolom2 = table.getColumnCount()>2 && table.getValueAt(row,2)!=null ? table.getValueAt(row,2).toString().trim() : "";
                    String nilaiKolom5 = table.getColumnCount()>5 && table.getValueAt(row,5)!=null ? table.getValueAt(row,5).toString().trim() : "";
                    boolean barisKategori = !namaKolom1.equals("") && nilaiKolom2.equals("") && nilaiKolom5.equals("");
                    boolean barisJumlah = namaKolom1.startsWith("Jumlah") || namaKolom1.startsWith("TOTAL");

                    if(barisKategori){
                        lbl.setBackground(new Color(245,247,250));
                        lbl.setForeground(new Color(42,52,64));
                        lbl.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
                    }else if(barisJumlah){
                        lbl.setBackground(new Color(237,242,255));
                        lbl.setForeground(new Color(46,74,160));
                        lbl.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
                    }else if(row%2==0){
                        lbl.setBackground(Color.WHITE);
                    }else{
                        lbl.setBackground(new Color(249,250,252));
                    }

                    if(column==1){
                        lbl.setHorizontalAlignment(JLabel.LEFT);
                    }else{
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                    }
                }
            }else{
                String kolom0 = table.getValueAt(row,0)!=null ? table.getValueAt(row,0).toString().trim() : "";
                boolean barisTotal = kolom0.equalsIgnoreCase("TOTAL");
                if(barisTotal){
                    lbl.setBackground(new Color(237,242,255));
                    lbl.setForeground(new Color(46,74,160));
                    lbl.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
                }else if(row%2==0){
                    lbl.setBackground(Color.WHITE);
                }else{
                    lbl.setBackground(new Color(249,250,252));
                }
                if(column==0 || column==1 || column==2 || column==4 || column==5 || column==7){
                    lbl.setHorizontalAlignment(JLabel.CENTER);
                }else{
                    lbl.setHorizontalAlignment(JLabel.LEFT);
                }
            }

            if(isSelected){
                lbl.setBackground(new Color(231,236,255));
                lbl.setForeground(new Color(36,54,92));
            }
            return lbl;
        }
    }

    private void terapkanGayaTabelModern(JTable table, boolean rekap){
        table.setRowHeight(rekap?34:32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(229,231,235));
        table.setIntercellSpacing(new Dimension(0,1));
        table.setSelectionBackground(new Color(231,236,255));
        table.setSelectionForeground(new Color(36,54,92));
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(60,72,88));
        table.setFont(new Font("Segoe UI",Font.PLAIN,12));
        table.setDefaultRenderer(Object.class, new TabelModernRenderer(rekap));
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width,38));
        header.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
        header.setDefaultRenderer(new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                JLabel lbl=(JLabel)super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                lbl.setOpaque(true);
                lbl.setBackground(new Color(245,247,250));
                lbl.setForeground(new Color(75,85,99));
                lbl.setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
                lbl.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                return lbl;
            }
        });
    }

    private LinkedHashMap<String,List<Object[]>> buatUrutanGrupPoli(){
        LinkedHashMap<String,List<Object[]>> grup = new LinkedHashMap<String,List<Object[]>>();
        grup.put("Poliklinik Bedah", new ArrayList<Object[]>());
        grup.put("Poliklinik Interna / Penyakit Dalam", new ArrayList<Object[]>());
        grup.put("Poliklinik Anak", new ArrayList<Object[]>());
        grup.put("Poliklinik Kebidanan & Kandungan", new ArrayList<Object[]>());
        grup.put("Poliklinik Jantung & Pembuluh Darah", new ArrayList<Object[]>());
        grup.put("Poliklinik Saraf", new ArrayList<Object[]>());
        grup.put("Poliklinik Kulit & Kelamin", new ArrayList<Object[]>());
        grup.put("Poliklinik THT", new ArrayList<Object[]>());
        grup.put("Poliklinik Mata", new ArrayList<Object[]>());
        grup.put("Poliklinik Gigi & Mulut", new ArrayList<Object[]>());
        grup.put("Poliklinik Jiwa", new ArrayList<Object[]>());
        grup.put("Poliklinik Rehabilitasi Medik", new ArrayList<Object[]>());
        grup.put("Penunjang / Diagnostik", new ArrayList<Object[]>());
        grup.put("Poliklinik / Unit Lainnya", new ArrayList<Object[]>());
        return grup;
    }

    private String tentukanGrupPoli(String kdPoli, String nmPoli){
        String nama = (nmPoli==null?"":nmPoli).toLowerCase();
        String kode = (kdPoli==null?"":kdPoli).toLowerCase();

        if(nama.contains("bedah") || nama.contains("urolog") || nama.contains("orthop") || nama.contains("ortho") || nama.contains("thorax") || nama.contains("digest") || nama.contains("onkolog") || nama.contains("vaskul") || nama.contains("plastik") || nama.contains("rekontr") || kode.equals("bed") || kode.equals("bds") || kode.equals("bsy") || kode.equals("bdh") || kode.equals("btr") || kode.equals("uro")){
            return "Poliklinik Bedah";
        }
        if(nama.contains("interna") || nama.contains("penyakit dalam") || nama.contains("endokrin") || nama.contains("paru") || nama.contains("ginjal") || nama.contains("hematolog") || nama.contains("gastro") || nama.contains("hepato") || nama.contains("reumat") || nama.contains("tropik") || nama.contains("infeksi") || nama.contains("nefro") || nama.contains("diabetes") || kode.equals("int") || kode.equals("ipd")){
            return "Poliklinik Interna / Penyakit Dalam";
        }
        if(nama.contains("anak") || kode.equals("ana")){
            return "Poliklinik Anak";
        }
        if(nama.contains("kebidanan") || nama.contains("kandung") || nama.contains("obg") || nama.contains("obstetri") || nama.contains("perinatologi") || kode.equals("obg")){
            return "Poliklinik Kebidanan & Kandungan";
        }
        if(nama.contains("jantung") || nama.contains("kardiolog") || nama.contains("pembuluh darah") || kode.equals("jan")){
            return "Poliklinik Jantung & Pembuluh Darah";
        }
        if(nama.contains("syaraf") || nama.contains("neuro") || nama.contains("stroke")){
            return "Poliklinik Saraf";
        }
        if(nama.contains("kulit") || nama.contains("kelamin")){
            return "Poliklinik Kulit & Kelamin";
        }
        if(nama.contains("tht") || nama.contains("otologi")){
            return "Poliklinik THT";
        }
        if(nama.contains("mata")){
            return "Poliklinik Mata";
        }
        if(nama.contains("gigi") || nama.contains("mulut") || nama.contains("gimul") || kode.equals("gig")){
            return "Poliklinik Gigi & Mulut";
        }
        if(nama.contains("jiwa") || nama.contains("psiki") || kode.equals("psi")){
            return "Poliklinik Jiwa";
        }
        if(nama.contains("rehab") || nama.contains("fisio") || nama.contains("fisiotherapi") || nama.contains("fisioterapi")){
            return "Poliklinik Rehabilitasi Medik";
        }
        if(nama.contains("labor") || nama.contains("radiologi") || nama.contains("diagnosa") || nama.contains("diagnost") || nama.contains("ct scan") || nama.contains("usg") || nama.contains("echo") || nama.contains("hemodialisa") || nama.contains("haemodialisa") || nama.contains("eswl") || nama.contains("endoscopy") || nama.contains("bronchoscopy") || nama.contains("treadmill") || nama.contains("ekg") || nama.contains("faal paru")){
            return "Penunjang / Diagnostik";
        }
        return "Poliklinik / Unit Lainnya";
    }

    private void tampilSesuaiTabAktif(){
        switch (TabRawat.getSelectedIndex()) {
            case 0:
                tampil();
                break;
            case 1:
                tampil2();
                break;
            case 2:
                tampil3();
                break;
            case 3:
                tampil9();
                break;
            case 4:
                tampil4();
                break;
            case 5:
                tampil5();
                break;
            case 6:
                tampil6();
                break;
            case 7:
                tampil7();
                break;
            case 8:
                tampil8();
                break;
            case 9:
                tampil10();
                break;
            default:
                tampil();
                break;
        }
    }

    private DefaultTableModel getModelAktif(){
        switch (TabRawat.getSelectedIndex()) {
            case 0:
                return tabMode;
            case 1:
                return tabMode2;
            case 2:
                return tabMode3;
            case 3:
                return tabMode9;
            case 4:
                return tabMode4;
            case 5:
                return tabMode5;
            case 6:
                return tabMode6;
            case 7:
                return tabMode7;
            case 8:
                return tabMode8;
            case 9:
                return tabMode10;
            default:
                return tabMode;
        }
    }

    private JTable getTableAktif(){
        switch (TabRawat.getSelectedIndex()) {
            case 0:
                return table1;
            case 1:
                return table2;
            case 2:
                return table3;
            case 3:
                return table9;
            case 4:
                return table4;
            case 5:
                return table5;
            case 6:
                return table6;
            case 7:
                return table7;
            case 8:
                return table8;
            case 9:
                return table10;
            default:
                return table1;
        }
    }

    private String getJudulLaporanAktif(){
        switch (TabRawat.getSelectedIndex()) {
            case 1:
                return "LAPORAN KUNJUNGAN RAWAT JALAN NON BATAL";
            case 2:
                return "LAPORAN KUNJUNGAN PASIEN GERIATRI";
            case 3:
                return "REKAP KUNJUNGAN IGD";
            case 4:
                return "LAPORAN KUNJUNGAN IGD BEDAH";
            case 5:
                return "LAPORAN KUNJUNGAN IGD NON BEDAH";
            case 6:
                return "LAPORAN KUNJUNGAN IGD BPJS";
            case 7:
                return "LAPORAN KUNJUNGAN IGD UMUM";
            case 8:
                return "LAPORAN KUNJUNGAN IGD INSTANSI";
            case 9:
                return "REKAP PASIEN RAWAT JALAN";
            default:
                return "LAPORAN KUNJUNGAN RAWAT JALAN";
        }
    }

    private String getJudulDialogLaporanAktif(){
        switch (TabRawat.getSelectedIndex()) {
            case 1:
                return "::[ Laporan Kunjungan Rawat Jalan Non Batal ]::";
            case 2:
                return "::[ Laporan Kunjungan Pasien Geriatri ]::";
            case 3:
                return "::[ Rekap Kunjungan IGD ]::";
            case 4:
                return "::[ Laporan Kunjungan IGD Bedah ]::";
            case 5:
                return "::[ Laporan Kunjungan IGD Non Bedah ]::";
            case 6:
                return "::[ Laporan Kunjungan IGD BPJS ]::";
            case 7:
                return "::[ Laporan Kunjungan IGD Umum ]::";
            case 8:
                return "::[ Laporan Kunjungan IGD Instansi ]::";
            case 9:
                return "::[ Rekap Pasien Rawat Jalan ]::";
            default:
                return "::[ Laporan Kunjungan Rawat Jalan ]::";
        }
    }

    private boolean isTabRekapIGD(){
        return TabRawat.getSelectedIndex()==3;
    }

    private boolean isTabRekapRawatJalan(){
        return TabRawat.getSelectedIndex()==9;
    }

    private int getTipeCetakAktif(){
        if(isTabRekapIGD()){
            return 1;
        }
        if(isTabRekapRawatJalan()){
            return 2;
        }
        return 0;
    }

    private void appendHtmlRows(DefaultTableModel modelAktif, StringBuilder builder, int tipeCetak){
        if(tipeCetak==1){
            builder.append(
                "<tr class='isi'>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Jumlah</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Jumlah</b></td>"+
                "</tr>"
            );
            for(i=0;i<modelAktif.getRowCount();i++){
                builder.append(
                    "<tr class='isi'>"+
                        "<td valign='top'>"+modelAktif.getValueAt(i,0)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,1)+"</td>"+
                        "<td valign='top'>"+modelAktif.getValueAt(i,2)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,3)+"</td>"+
                    "</tr>"
                );
            }
        }else if(tipeCetak==2){
            builder.append(
                "<tr class='isi'>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Poliklinik / Unit</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lama</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Baru</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Jumlah</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>BPJS</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Umum</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Instansi</b></td>"+
                "</tr>"
            );
            for(i=0;i<modelAktif.getRowCount();i++){
                builder.append(
                    "<tr class='isi'>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,0)+"</td>"+
                        "<td valign='top'>"+modelAktif.getValueAt(i,1)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,2)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,3)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,4)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,5)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,6)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,7)+"</td>"+
                    "</tr>"
                );
            }
        }else{
            builder.append(                             
                "<tr class='isi'>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lama</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Baru</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pasien</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>L</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>P</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Alamat</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Diagnosa</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Dokter Jaga</b></td>"+
                "</tr>"
            );
            for(i=0;i<modelAktif.getRowCount();i++){  
                builder.append(                             
                    "<tr class='isi'>"+
                        "<td valign='top'>"+modelAktif.getValueAt(i,0)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,1)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,2)+"</td>"+
                        "<td valign='top'>"+modelAktif.getValueAt(i,3)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,4)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,5)+"</td>"+
                        "<td valign='top'>"+modelAktif.getValueAt(i,6)+"</td>"+
                        "<td valign='top' align='center'>"+modelAktif.getValueAt(i,7)+"</td>"+
                        "<td valign='top'>"+modelAktif.getValueAt(i,8)+"</td>"+
                        "<td valign='top'>"+modelAktif.getValueAt(i,9)+"</td>"+
                    "</tr>"
                ); 
            }
        }
    }

    private void appendCsvRows(DefaultTableModel modelAktif, StringBuilder builder, int tipeCetak){
        if(tipeCetak==1){
            builder.append("\"Keterangan\";\"Jumlah\";\"Keterangan\";\"Jumlah\"\n");
            for(i=0;i<modelAktif.getRowCount();i++){
                builder.append("\""+modelAktif.getValueAt(i,0)+"\";\""+modelAktif.getValueAt(i,1)+"\";\""+modelAktif.getValueAt(i,2)+"\";\""+modelAktif.getValueAt(i,3)+"\"\n");
            }
        }else if(tipeCetak==2){
            builder.append("\"No.\";\"Poliklinik / Unit\";\"Lama\";\"Baru\";\"Jumlah\";\"BPJS\";\"Umum\";\"Instansi\"\n");
            for(i=0;i<modelAktif.getRowCount();i++){
                builder.append("\""+modelAktif.getValueAt(i,0)+"\";\""+modelAktif.getValueAt(i,1)+"\";\""+modelAktif.getValueAt(i,2)+"\";\""+modelAktif.getValueAt(i,3)+"\";\""+modelAktif.getValueAt(i,4)+"\";\""+modelAktif.getValueAt(i,5)+"\";\""+modelAktif.getValueAt(i,6)+"\";\""+modelAktif.getValueAt(i,7)+"\"\n");
            }
        }else{
            builder.append("\"No.\";\"Lama\";\"Baru\";\"Nama Pasien\";\"L\";\"P\";\"Alamat\";\"Kode\";\"Diagnosa\";\"Dokter Jaga\"\n");
            for(i=0;i<modelAktif.getRowCount();i++){
                builder.append("\""+modelAktif.getValueAt(i,0)+"\";\""+modelAktif.getValueAt(i,1)+"\";\""+modelAktif.getValueAt(i,2)+"\";\""+modelAktif.getValueAt(i,3)+"\";\""+modelAktif.getValueAt(i,4)+"\";\""+modelAktif.getValueAt(i,5)+"\";\""+modelAktif.getValueAt(i,6)+"\";\""+modelAktif.getValueAt(i,7)+"\";\""+modelAktif.getValueAt(i,8)+"\";\""+modelAktif.getValueAt(i,9)+"\"\n");
            }
        }
    }

    private void inisialisasiTabelRekapIGD(JTable table, DefaultTableModel model){
        table.setModel(model);
        table.setPreferredScrollableViewportSize(new Dimension(500,500));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(330);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(330);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        terapkanGayaTabelModern(table,true);
    }

    private void inisialisasiTabelRekapRawatJalan(JTable table, DefaultTableModel model){
        table.setModel(model);
        table.setPreferredScrollableViewportSize(new Dimension(500,500));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(360);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        terapkanGayaTabelModern(table,true);
    }

    private void inisialisasiTabelIGD(JTable table, DefaultTableModel model){
        table.setModel(model);
        table.setPreferredScrollableViewportSize(new Dimension(500,500));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 10; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(60);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(260);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(220);
            }else if(i==7){
                column.setPreferredWidth(80);
            }else if(i==8){
                column.setPreferredWidth(220);
            }else if(i==9){
                column.setPreferredWidth(180);
            }
        }
        terapkanGayaTabelModern(table,false);
    }

    private void tambahRingkasan(int[] data, String sttsDaftar, String jk){
        if("Baru".equals(sttsDaftar)){
            data[1]++;
            baru++;
        }else if("Lama".equals(sttsDaftar)){
            data[0]++;
            lama++;
        }
        if("L".equals(jk)){
            data[2]++;
            laki++;
        }else if("P".equals(jk)){
            data[3]++;
            per++;
        }
        data[4]++;
    }

    private void tambahBarisRingkasanIGD(DefaultTableModel model, String label, int[] data){
        model.addRow(new Object[]{
            "REKAP",
            data[0],
            data[1],
            label,
            data[2],
            data[3],
            "",
            "",
            "Total",
            data[4]
        });
    }

    private int tambahBarisRincianIGD(DefaultTableModel model, String label, int[] data, List<Object[]> rows, int nomorAwal){
        model.addRow(new Object[]{
            "RINCIAN",
            data[0],
            data[1],
            label,
            data[2],
            data[3],
            "",
            "",
            "Total",
            data[4]
        });
        int nomor = nomorAwal;
        for(Object[] row : rows){
            model.addRow(new Object[]{
                nomor++, row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8]
            });
        }
        return nomor;
    }

    public void tampil(){        
        try{   
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            Valid.tabelKosong(tabMode);   
            if(nmpoli.getText().trim().equals("")&&nmdokter.getText().trim().equals("")&&nmpenjab.getText().trim().equals("")&&nmkabupaten.getText().trim().equals("")&&nmkecamatan.getText().trim().equals("")&&nmkelurahan.getText().trim().equals("")&&TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.stts_daftar," +
                        "dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,poliklinik.nm_poli,"+
                        "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar " +
                        "from reg_periksa inner join dokter inner join pasien inner join poliklinik inner join penjab " +
                        "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.kd_dokter=dokter.kd_dokter " +
                        "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj " +
                        "and reg_periksa.kd_poli=poliklinik.kd_poli and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel " +
                        "where reg_periksa.stts_daftar like '%"+status+"%' and reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.tgl_registrasi between ? and ? order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg");
            }else{
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.stts_daftar," +
                        "dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,poliklinik.nm_poli,"+
                        "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar " +
                        "from reg_periksa inner join dokter inner join pasien inner join poliklinik inner join penjab " +
                        "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.kd_dokter=dokter.kd_dokter " +
                        "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj " +
                        "and reg_periksa.kd_poli=poliklinik.kd_poli and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel " +
                        "where reg_periksa.stts_daftar like '%"+status+"%' and reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.tgl_registrasi between ? and ? and poliklinik.nm_poli like ? and dokter.nm_dokter like ? and penjab.png_jawab like ? and kabupaten.nm_kab like ? and kecamatan.nm_kec like ? and kelurahan.nm_kel like ? and "+
                        "(poliklinik.nm_poli like ? or dokter.nm_dokter like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.alamat like ?) order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg");
            }
                
            try {
                if(nmpoli.getText().trim().equals("")&&nmdokter.getText().trim().equals("")&&nmpenjab.getText().trim().equals("")&&nmkabupaten.getText().trim().equals("")&&nmkecamatan.getText().trim().equals("")&&nmkelurahan.getText().trim().equals("")&&TCari.getText().trim().equals("")){
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                }else{
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                    ps.setString(3,"%"+nmpoli.getText().trim()+"%");
                    ps.setString(4,"%"+nmdokter.getText().trim()+"%");
                    ps.setString(5,"%"+nmpenjab.getText().trim()+"%");
                    ps.setString(6,"%"+nmkabupaten.getText().trim()+"%");
                    ps.setString(7,"%"+nmkecamatan.getText().trim()+"%");
                    ps.setString(8,"%"+nmkelurahan.getText().trim()+"%");
                    ps.setString(9,"%"+TCari.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                    ps.setString(12,"%"+TCari.getText().trim()+"%");
                    ps.setString(13,"%"+TCari.getText().trim()+"%");
                }
                    
                rs=ps.executeQuery();
                i=1;   
                lama=0;baru=0;laki=0;per=0;
                while(rs.next()){
                    setbaru="";
                    setlama="";
                    if(rs.getString("stts_daftar").equals("Baru")){
                        setbaru=rs.getString("no_rkm_medis");
                        baru++;
                    }else if(rs.getString("stts_daftar").equals("Lama")){
                        setlama=rs.getString("no_rkm_medis");
                        lama++;
                    }
                    umurlk="";
                    umurpr="";
                    switch (rs.getString("jk")) {
                        case "L":
                            umurlk=rs.getString("umur");
                            laki++;
                            break;
                        case "P":
                            umurpr=rs.getString("umur");
                            per++;
                            break;
                    }
                    diagnosa="";
                    kddiangnosa="";
                    ps2=koneksi.prepareStatement("select penyakit.kd_penyakit,penyakit.nm_penyakit from penyakit inner join diagnosa_pasien " +
                        "on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                        "where diagnosa_pasien.no_rawat=? order by prioritas asc limit 1");
                    try {
                        ps2.setString(1,rs.getString("no_rawat"));
                        rs2=ps2.executeQuery();
                        if(rs2.next()){
                            kddiangnosa=rs2.getString(1);
                            diagnosa=rs2.getString(2);
                        }
                    } catch (Exception e) {
                        System.out.println("laporan.DlgKunjunganRalan.tampil() 2 :"+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }                        
                    tabMode.addRow(new Object[]{
                        i,setlama,setbaru,rs.getString("nm_pasien"),umurlk,umurpr,rs.getString("almt_pj"),kddiangnosa,diagnosa,rs.getString("nm_dokter")
                    });                
                    i++;
                }
                if(i>=2){
                    tabMode.addRow(new Object[]{
                        ">>",lama,baru,"",laki,per,"","","",""
                    });
                }
            } catch (Exception e) {
                System.out.println("laporan.DlgKunjunganRalan.tampil() : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }       
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public void tampil2(){        
        try{   
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            Valid.tabelKosong(tabMode2);   
            if(nmpoli.getText().trim().equals("")&&nmdokter.getText().trim().equals("")&&nmpenjab.getText().trim().equals("")&&nmkabupaten.getText().trim().equals("")&&nmkecamatan.getText().trim().equals("")&&nmkelurahan.getText().trim().equals("")&&TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.stts_daftar," +
                        "dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,poliklinik.nm_poli,"+
                        "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar " +
                        "from reg_periksa inner join dokter inner join pasien inner join poliklinik inner join penjab " +
                        "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.kd_dokter=dokter.kd_dokter " +
                        "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj " +
                        "and reg_periksa.kd_poli=poliklinik.kd_poli and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel " +
                        "where reg_periksa.stts_daftar like '%"+status+"%' and reg_periksa.status_lanjut='Ralan' and reg_periksa.stts<>'Batal' and reg_periksa.tgl_registrasi between ? and ? order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg");
            }else{
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.stts_daftar," +
                        "dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,poliklinik.nm_poli,"+
                        "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar " +
                        "from reg_periksa inner join dokter inner join pasien inner join poliklinik inner join penjab " +
                        "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.kd_dokter=dokter.kd_dokter " +
                        "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj " +
                        "and reg_periksa.kd_poli=poliklinik.kd_poli and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel " +
                        "where reg_periksa.stts_daftar like '%"+status+"%' and reg_periksa.status_lanjut='Ralan' and reg_periksa.stts<>'Batal' and reg_periksa.tgl_registrasi between ? and ? and poliklinik.nm_poli like ? and dokter.nm_dokter like ? and penjab.png_jawab like ? and kabupaten.nm_kab like ? and kecamatan.nm_kec like ? and kelurahan.nm_kel like ? and "+
                        "(poliklinik.nm_poli like ? or dokter.nm_dokter like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.alamat like ?) order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg");
            }
                
            try {
                if(nmpoli.getText().trim().equals("")&&nmdokter.getText().trim().equals("")&&nmpenjab.getText().trim().equals("")&&nmkabupaten.getText().trim().equals("")&&nmkecamatan.getText().trim().equals("")&&nmkelurahan.getText().trim().equals("")&&TCari.getText().trim().equals("")){
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                }else{
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                    ps.setString(3,"%"+nmpoli.getText().trim()+"%");
                    ps.setString(4,"%"+nmdokter.getText().trim()+"%");
                    ps.setString(5,"%"+nmpenjab.getText().trim()+"%");
                    ps.setString(6,"%"+nmkabupaten.getText().trim()+"%");
                    ps.setString(7,"%"+nmkecamatan.getText().trim()+"%");
                    ps.setString(8,"%"+nmkelurahan.getText().trim()+"%");
                    ps.setString(9,"%"+TCari.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                    ps.setString(12,"%"+TCari.getText().trim()+"%");
                    ps.setString(13,"%"+TCari.getText().trim()+"%");
                }
                    
                rs=ps.executeQuery();
                i=1;   
                lama=0;baru=0;laki=0;per=0;
                while(rs.next()){
                    setbaru="";
                    setlama="";
                    if(rs.getString("stts_daftar").equals("Baru")){
                        setbaru=rs.getString("no_rkm_medis");
                        baru++;
                    }else if(rs.getString("stts_daftar").equals("Lama")){
                        setlama=rs.getString("no_rkm_medis");
                        lama++;
                    }
                    umurlk="";
                    umurpr="";
                    switch (rs.getString("jk")) {
                        case "L":
                            umurlk=rs.getString("umur");
                            laki++;
                            break;
                        case "P":
                            umurpr=rs.getString("umur");
                            per++;
                            break;
                    }
                    diagnosa="";
                    kddiangnosa="";
                    ps2=koneksi.prepareStatement("select penyakit.kd_penyakit,penyakit.nm_penyakit from penyakit inner join diagnosa_pasien " +
                        "on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                        "where diagnosa_pasien.no_rawat=? order by prioritas asc limit 1");
                    try {
                        ps2.setString(1,rs.getString("no_rawat"));
                        rs2=ps2.executeQuery();
                        if(rs2.next()){
                            kddiangnosa=rs2.getString(1);
                            diagnosa=rs2.getString(2);
                        }
                    } catch (Exception e) {
                        System.out.println("laporan.DlgKunjunganRalan.tampil() 2 :"+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }                        
                    tabMode2.addRow(new Object[]{
                        i,setlama,setbaru,rs.getString("nm_pasien"),umurlk,umurpr,rs.getString("almt_pj"),kddiangnosa,diagnosa,rs.getString("nm_dokter")
                    });                
                    i++;
                }
                if(i>=2){
                    tabMode2.addRow(new Object[]{
                        ">>",lama,baru,"",laki,per,"","","",""
                    });
                }
            } catch (Exception e) {
                System.out.println("laporan.DlgKunjunganRalan.tampil() : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }       
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
public void tampil3(){        
        try{   
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            Valid.tabelKosong(tabMode3);   
            if(nmpoli.getText().trim().equals("")&&nmdokter.getText().trim().equals("")&&nmpenjab.getText().trim().equals("")&&nmkabupaten.getText().trim().equals("")&&nmkecamatan.getText().trim().equals("")&&nmkelurahan.getText().trim().equals("")&&TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.stts_daftar," +
                        "dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,poliklinik.nm_poli,"+
                        "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar " +
                        "from reg_periksa inner join dokter inner join pasien inner join poliklinik inner join penjab " +
                        "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.kd_dokter=dokter.kd_dokter " +
                        "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj " +
                        "and reg_periksa.kd_poli=poliklinik.kd_poli and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel " +
                        "where reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.tgl_registrasi between ? and ? and reg_periksa.sttsumur='Th' and reg_periksa.umurdaftar >= 60 order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg");
            }else{
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.stts_daftar," +
                        "dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,poliklinik.nm_poli,"+
                        "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar " +
                        "from reg_periksa inner join dokter inner join pasien inner join poliklinik inner join penjab " +
                        "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.kd_dokter=dokter.kd_dokter " +
                        "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj " +
                        "and reg_periksa.kd_poli=poliklinik.kd_poli and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel " +
                        "where reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.tgl_registrasi between ? and ? and reg_periksa.sttsumur='Th' and reg_periksa.umurdaftar >= 60 and poliklinik.nm_poli like ? and dokter.nm_dokter like ? and penjab.png_jawab like ? and kabupaten.nm_kab like ? and kecamatan.nm_kec like ? and kelurahan.nm_kel like ? and "+
                        "(poliklinik.nm_poli like ? or dokter.nm_dokter like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.alamat like ?) order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg");
            }
                
            try {
                if(nmpoli.getText().trim().equals("")&&nmdokter.getText().trim().equals("")&&nmpenjab.getText().trim().equals("")&&nmkabupaten.getText().trim().equals("")&&nmkecamatan.getText().trim().equals("")&&nmkelurahan.getText().trim().equals("")&&TCari.getText().trim().equals("")){
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                }else{
                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                    ps.setString(3,"%"+nmpoli.getText().trim()+"%");
                    ps.setString(4,"%"+nmdokter.getText().trim()+"%");
                    ps.setString(5,"%"+nmpenjab.getText().trim()+"%");
                    ps.setString(6,"%"+nmkabupaten.getText().trim()+"%");
                    ps.setString(7,"%"+nmkecamatan.getText().trim()+"%");
                    ps.setString(8,"%"+nmkelurahan.getText().trim()+"%");
                    ps.setString(9,"%"+TCari.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                    ps.setString(12,"%"+TCari.getText().trim()+"%");
                    ps.setString(13,"%"+TCari.getText().trim()+"%");
                }
                    
                rs=ps.executeQuery();
                i=1;   
                lama=0;baru=0;laki=0;per=0;
                while(rs.next()){
                    setbaru="";
                    setlama="";
                    if(rs.getString("stts_daftar").equals("Baru")){
                        setbaru=rs.getString("no_rkm_medis");
                        baru++;
                    }else if(rs.getString("stts_daftar").equals("Lama")){
                        setlama=rs.getString("no_rkm_medis");
                        lama++;
                    }
                    umurlk="";
                    umurpr="";
                    switch (rs.getString("jk")) {
                        case "L":
                            umurlk=rs.getString("umur");
                            laki++;
                            break;
                        case "P":
                            umurpr=rs.getString("umur");
                            per++;
                            break;
                    }
                    diagnosa="";
                    kddiangnosa="";
                    ps2=koneksi.prepareStatement("select penyakit.kd_penyakit,penyakit.nm_penyakit from penyakit inner join diagnosa_pasien " +
                        "on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                        "where diagnosa_pasien.no_rawat=? order by prioritas asc limit 1");
                    try {
                        ps2.setString(1,rs.getString("no_rawat"));
                        rs2=ps2.executeQuery();
                        if(rs2.next()){
                            kddiangnosa=rs2.getString(1);
                            diagnosa=rs2.getString(2);
                        }
                    } catch (Exception e) {
                        System.out.println("laporan.DlgKunjunganRalan.tampil() 2 :"+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }                        
                    tabMode3.addRow(new Object[]{
                        i,setlama,setbaru,rs.getString("nm_pasien"),umurlk,umurpr,rs.getString("almt_pj"),kddiangnosa,diagnosa,rs.getString("nm_dokter")
                    });                
                    i++;
                }
                if(i>=2){
                    tabMode3.addRow(new Object[]{
                        ">>",lama,baru,"",laki,per,"","","",""
                    });
                }
            } catch (Exception e) {
                System.out.println("laporan.DlgKunjunganRalan.tampil() : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }       
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
}    


    public void tampil9(){
        tampilRekapIGD();
    }

    public void tampil10(){
        tampilRekapRawatJalan();
    }

    public void tampil4(){
        tampilIGD(tabMode4,"BEDAH","IGD Bedah");
    }

    public void tampil5(){
        tampilIGD(tabMode5,"NON_BEDAH","IGD Non Bedah");
    }

    public void tampil6(){
        tampilIGD(tabMode6,"BPJS","IGD BPJS");
    }

    public void tampil7(){
        tampilIGD(tabMode7,"UMUM","IGD Umum");
    }

    public void tampil8(){
        tampilIGD(tabMode8,"INSTANSI","IGD Instansi");
    }

    private int[] hitungKunjungan(String kondisiTambahan){
        int[] data = new int[]{0,0,0,0,0,0};
        PreparedStatement psLocal=null;
        ResultSet rsLocal=null;
        try{
            String sql =
                "select "+
                "sum(case when reg_periksa.stts_daftar='Lama' then 1 else 0 end) as lama,"+
                "sum(case when reg_periksa.stts_daftar='Baru' then 1 else 0 end) as baru,"+
                "count(*) as jumlah,"+
                "sum(case when reg_periksa.kd_pj='BPJ' then 1 else 0 end) as bpjs,"+
                "sum(case when reg_periksa.kd_pj='001' then 1 else 0 end) as umum,"+
                "sum(case when reg_periksa.kd_pj not in ('BPJ','001') then 1 else 0 end) as instansi "+
                "from reg_periksa where reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.stts_daftar like ? and reg_periksa.tgl_registrasi between ? and ? "+kondisiTambahan;
            psLocal=koneksi.prepareStatement(sql);
            psLocal.setString(1,"%"+status+"%");
            psLocal.setString(2,Valid.SetTgl(Tgl1.getSelectedItem()+""));
            psLocal.setString(3,Valid.SetTgl(Tgl2.getSelectedItem()+""));
            rsLocal=psLocal.executeQuery();
            if(rsLocal.next()){
                data[0]=rsLocal.getInt("lama");
                data[1]=rsLocal.getInt("baru");
                data[2]=rsLocal.getInt("jumlah");
                data[3]=rsLocal.getInt("bpjs");
                data[4]=rsLocal.getInt("umum");
                data[5]=rsLocal.getInt("instansi");
            }
        }catch(Exception e){
            System.out.println("Notifikasi hitungKunjungan : "+e);
        }finally{
            try{ if(rsLocal!=null){ rsLocal.close(); } }catch(Exception e){}
            try{ if(psLocal!=null){ psLocal.close(); } }catch(Exception e){}
        }
        return data;
    }

    private LinkedHashMap<String,List<Object[]>> getRekapPoliRawatJalan(){
        LinkedHashMap<String,List<Object[]>> hasil = buatUrutanGrupPoli();
        PreparedStatement psLocal=null;
        ResultSet rsLocal=null;
        try{
            String sql =
                "select poliklinik.kd_poli, poliklinik.nm_poli, "+
                "sum(case when reg_periksa.stts_daftar='Lama' then 1 else 0 end) as lama, "+
                "sum(case when reg_periksa.stts_daftar='Baru' then 1 else 0 end) as baru, "+
                "count(reg_periksa.no_rawat) as jumlah, "+
                "sum(case when reg_periksa.kd_pj='BPJ' then 1 else 0 end) as bpjs, "+
                "sum(case when reg_periksa.kd_pj='001' then 1 else 0 end) as umum, "+
                "sum(case when reg_periksa.kd_pj not in ('BPJ','001') then 1 else 0 end) as instansi "+
                "from poliklinik left join reg_periksa on reg_periksa.kd_poli=poliklinik.kd_poli "+
                "and reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.stts_daftar like ? and reg_periksa.tgl_registrasi between ? and ? "+
                "where poliklinik.status='1' and poliklinik.kd_poli<>'-' and poliklinik.kd_poli not in ('IGDK','UGD','U0013','2') "+
                "and lower(poliklinik.nm_poli) not like '%gawat darurat%' and lower(poliklinik.nm_poli) not like '%igd%' "+
                "group by poliklinik.kd_poli, poliklinik.nm_poli order by poliklinik.nm_poli";
            psLocal=koneksi.prepareStatement(sql);
            psLocal.setString(1,"%"+status+"%");
            psLocal.setString(2,Valid.SetTgl(Tgl1.getSelectedItem()+""));
            psLocal.setString(3,Valid.SetTgl(Tgl2.getSelectedItem()+""));
            rsLocal=psLocal.executeQuery();
            while(rsLocal.next()){
                String grup = tentukanGrupPoli(rsLocal.getString("kd_poli"), rsLocal.getString("nm_poli"));
                hasil.get(grup).add(new Object[]{
                    rsLocal.getString("nm_poli")+" ("+rsLocal.getString("kd_poli")+")",
                    new int[]{
                        rsLocal.getInt("lama"),
                        rsLocal.getInt("baru"),
                        rsLocal.getInt("jumlah"),
                        rsLocal.getInt("bpjs"),
                        rsLocal.getInt("umum"),
                        rsLocal.getInt("instansi")
                    }
                });
            }
        }catch(Exception e){
            System.out.println("Notifikasi getRekapPoliRawatJalan : "+e);
        }finally{
            try{ if(rsLocal!=null){ rsLocal.close(); } }catch(Exception e){}
            try{ if(psLocal!=null){ psLocal.close(); } }catch(Exception e){}
        }
        return hasil;
    }

    private int[] hitungTindakanKhusus(String kondisiNama){
        int[] data = new int[]{0,0,0,0,0,0};
        PreparedStatement psLocal=null;
        ResultSet rsLocal=null;
        try{
            String sql =
                "select "+
                "sum(case when data_tindakan.stts_daftar='Lama' then 1 else 0 end) as lama, "+
                "sum(case when data_tindakan.stts_daftar='Baru' then 1 else 0 end) as baru, "+
                "count(*) as jumlah, "+
                "sum(case when data_tindakan.kd_pj='BPJ' then 1 else 0 end) as bpjs, "+
                "sum(case when data_tindakan.kd_pj='001' then 1 else 0 end) as umum, "+
                "sum(case when data_tindakan.kd_pj not in ('BPJ','001') then 1 else 0 end) as instansi "+
                "from ("+
                "select distinct reg_periksa.no_rawat, reg_periksa.stts_daftar, reg_periksa.kd_pj "+
                "from reg_periksa inner join ("+
                "select no_rawat,kd_jenis_prw from rawat_jl_dr union all "+
                "select no_rawat,kd_jenis_prw from rawat_jl_pr union all "+
                "select no_rawat,kd_jenis_prw from rawat_jl_drpr"+
                ") tindakan on tindakan.no_rawat=reg_periksa.no_rawat "+
                "inner join jns_perawatan on jns_perawatan.kd_jenis_prw=tindakan.kd_jenis_prw "+
                "where reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.stts_daftar like ? and reg_periksa.tgl_registrasi between ? and ? and ("+kondisiNama+")"+
                ") data_tindakan";
            psLocal=koneksi.prepareStatement(sql);
            psLocal.setString(1,"%"+status+"%");
            psLocal.setString(2,Valid.SetTgl(Tgl1.getSelectedItem()+""));
            psLocal.setString(3,Valid.SetTgl(Tgl2.getSelectedItem()+""));
            rsLocal=psLocal.executeQuery();
            if(rsLocal.next()){
                data[0]=rsLocal.getInt("lama");
                data[1]=rsLocal.getInt("baru");
                data[2]=rsLocal.getInt("jumlah");
                data[3]=rsLocal.getInt("bpjs");
                data[4]=rsLocal.getInt("umum");
                data[5]=rsLocal.getInt("instansi");
            }
        }catch(Exception e){
            System.out.println("Notifikasi hitungTindakanKhusus : "+e);
        }finally{
            try{ if(rsLocal!=null){ rsLocal.close(); } }catch(Exception e){}
            try{ if(psLocal!=null){ psLocal.close(); } }catch(Exception e){}
        }
        return data;
    }

    private void tambahBarisRekapRawatJalan(Object no, String nama, Object lama, Object baru, Object jumlah, Object bpjs, Object umum, Object instansi){
        tabMode10.addRow(new Object[]{no,nama,lama,baru,jumlah,bpjs,umum,instansi});
    }

    private int[] jumlahkan(int[] a, int[] b){
        return new int[]{a[0]+b[0],a[1]+b[1],a[2]+b[2],a[3]+b[3],a[4]+b[4],a[5]+b[5]};
    }

    private void tampilRekapRawatJalan(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode10);

            int[] igdBedah = hitungKunjungan(" and reg_periksa.kd_poli='IGDK' and reg_periksa.kd_dokter='D0000173' ");
            int[] igdNonBedah = hitungKunjungan(" and reg_periksa.kd_poli='IGDK' and reg_periksa.kd_dokter<>'D0000173' ");
            int[] subtotalUGD = jumlahkan(igdBedah,igdNonBedah);

            tambahBarisRekapRawatJalan("1","UGD","","","","","","");
            tambahBarisRekapRawatJalan("","- Bedah",igdBedah[0],igdBedah[1],igdBedah[2],igdBedah[3],igdBedah[4],igdBedah[5]);
            tambahBarisRekapRawatJalan("","- Non Bedah",igdNonBedah[0],igdNonBedah[1],igdNonBedah[2],igdNonBedah[3],igdNonBedah[4],igdNonBedah[5]);
            tambahBarisRekapRawatJalan("","Jumlah UGD",subtotalUGD[0],subtotalUGD[1],subtotalUGD[2],subtotalUGD[3],subtotalUGD[4],subtotalUGD[5]);

            tambahBarisRekapRawatJalan("2","POLIKLINIK / UNIT AKTIF","","","","","","");
            int[] subtotalPoli = new int[]{0,0,0,0,0,0};
            LinkedHashMap<String,List<Object[]>> daftarPoli = getRekapPoliRawatJalan();
            for(Map.Entry<String,List<Object[]>> entry : daftarPoli.entrySet()){
                if(entry.getValue().isEmpty()){
                    continue;
                }
                tambahBarisRekapRawatJalan("",entry.getKey(),"","","","","","");
                int[] subtotalGrup = new int[]{0,0,0,0,0,0};
                for(Object[] item : entry.getValue()){
                    int[] data = (int[])item[1];
                    tambahBarisRekapRawatJalan("","- "+item[0].toString(),data[0],data[1],data[2],data[3],data[4],data[5]);
                    subtotalGrup = jumlahkan(subtotalGrup,data);
                    subtotalPoli = jumlahkan(subtotalPoli,data);
                }
                tambahBarisRekapRawatJalan("","Jumlah "+entry.getKey(),subtotalGrup[0],subtotalGrup[1],subtotalGrup[2],subtotalGrup[3],subtotalGrup[4],subtotalGrup[5]);
            }
            tambahBarisRekapRawatJalan("","Jumlah Poliklinik Aktif",subtotalPoli[0],subtotalPoli[1],subtotalPoli[2],subtotalPoli[3],subtotalPoli[4],subtotalPoli[5]);

            tambahBarisRekapRawatJalan("3","TINDAKAN KHUSUS (distinct no_rawat)","","","","","","");
            LinkedHashMap<String,String> daftarTindakan = new LinkedHashMap<String,String>();
            daftarTindakan.put("Konsultasi Gizi","lower(jns_perawatan.nm_perawatan) like '%konsultasi gizi%'");
            daftarTindakan.put("TCD","lower(jns_perawatan.nm_perawatan) like '%tcd%'");
            daftarTindakan.put("ECHO","lower(jns_perawatan.nm_perawatan) like '%echo%'");
            daftarTindakan.put("USG OBGYN","lower(jns_perawatan.nm_perawatan) like '%usg%' and (lower(jns_perawatan.nm_perawatan) like '%obg%' or lower(jns_perawatan.nm_perawatan) like '%kand%')");
            daftarTindakan.put("ESWL","lower(jns_perawatan.nm_perawatan) like '%eswl%'");
            daftarTindakan.put("DOPPLER","lower(jns_perawatan.nm_perawatan) like '%dopler%' or lower(jns_perawatan.nm_perawatan) like '%doppler%'");
            daftarTindakan.put("TREADMILL","lower(jns_perawatan.nm_perawatan) like '%treadmill%'");
            daftarTindakan.put("EKG","lower(jns_perawatan.nm_perawatan) like '%ekg%'");
            daftarTindakan.put("FAAL PARU","lower(jns_perawatan.nm_perawatan) like '%faal paru%'");
            daftarTindakan.put("CHECK UP","lower(jns_perawatan.nm_perawatan) like '%check up%' or lower(jns_perawatan.nm_perawatan) like '%medical check up%'");
            int[] subtotalTindakan = new int[]{0,0,0,0,0,0};
            for(Map.Entry<String,String> entry : daftarTindakan.entrySet()){
                int[] data = hitungTindakanKhusus(entry.getValue());
                tambahBarisRekapRawatJalan("",entry.getKey(),data[0],data[1],data[2],data[3],data[4],data[5]);
                subtotalTindakan = jumlahkan(subtotalTindakan,data);
            }
            tambahBarisRekapRawatJalan("","Jumlah Tindakan Khusus",subtotalTindakan[0],subtotalTindakan[1],subtotalTindakan[2],subtotalTindakan[3],subtotalTindakan[4],subtotalTindakan[5]);

            int[] totalKunjungan = jumlahkan(subtotalUGD,subtotalPoli);
            tambahBarisRekapRawatJalan("","TOTAL KUNJUNGAN RAWAT JALAN (UGD + POLIKLINIK)",totalKunjungan[0],totalKunjungan[1],totalKunjungan[2],totalKunjungan[3],totalKunjungan[4],totalKunjungan[5]);

            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi tampilRekapRawatJalan : "+e);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void tampilRekapIGD(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode9);

            String sql =
                    "select " +
                    "sum(case when reg_periksa.kd_dokter='D0000173' then 1 else 0 end) as bedah_total," +
                    "sum(case when reg_periksa.kd_dokter='D0000173' and reg_periksa.stts_daftar='Lama' then 1 else 0 end) as bedah_lama," +
                    "sum(case when reg_periksa.kd_dokter='D0000173' and reg_periksa.stts_daftar='Lama' and reg_periksa.kd_pj='BPJ' then 1 else 0 end) as bedah_lama_bpjs," +
                    "sum(case when reg_periksa.kd_dokter='D0000173' and reg_periksa.stts_daftar='Lama' and reg_periksa.kd_pj='001' then 1 else 0 end) as bedah_lama_umum," +
                    "sum(case when reg_periksa.kd_dokter='D0000173' and reg_periksa.stts_daftar='Lama' and reg_periksa.kd_pj not in ('BPJ','001') then 1 else 0 end) as bedah_lama_instansi," +
                    "sum(case when reg_periksa.kd_dokter='D0000173' and reg_periksa.stts_daftar='Baru' then 1 else 0 end) as bedah_baru," +
                    "sum(case when reg_periksa.kd_dokter='D0000173' and reg_periksa.stts_daftar='Baru' and reg_periksa.kd_pj='BPJ' then 1 else 0 end) as bedah_baru_bpjs," +
                    "sum(case when reg_periksa.kd_dokter='D0000173' and reg_periksa.stts_daftar='Baru' and reg_periksa.kd_pj='001' then 1 else 0 end) as bedah_baru_umum," +
                    "sum(case when reg_periksa.kd_dokter='D0000173' and reg_periksa.stts_daftar='Baru' and reg_periksa.kd_pj not in ('BPJ','001') then 1 else 0 end) as bedah_baru_instansi," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' then 1 else 0 end) as nonbedah_total," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' and reg_periksa.stts_daftar='Lama' then 1 else 0 end) as nonbedah_lama," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' and reg_periksa.stts_daftar='Lama' and reg_periksa.kd_pj='BPJ' then 1 else 0 end) as nonbedah_lama_bpjs," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' and reg_periksa.stts_daftar='Lama' and reg_periksa.kd_pj='001' then 1 else 0 end) as nonbedah_lama_umum," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' and reg_periksa.stts_daftar='Lama' and reg_periksa.kd_pj not in ('BPJ','001') then 1 else 0 end) as nonbedah_lama_instansi," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' and reg_periksa.stts_daftar='Baru' then 1 else 0 end) as nonbedah_baru," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' and reg_periksa.stts_daftar='Baru' and reg_periksa.kd_pj='BPJ' then 1 else 0 end) as nonbedah_baru_bpjs," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' and reg_periksa.stts_daftar='Baru' and reg_periksa.kd_pj='001' then 1 else 0 end) as nonbedah_baru_umum," +
                    "sum(case when reg_periksa.kd_dokter<>'D0000173' and reg_periksa.stts_daftar='Baru' and reg_periksa.kd_pj not in ('BPJ','001') then 1 else 0 end) as nonbedah_baru_instansi " +
                    "from reg_periksa inner join dokter inner join pasien inner join poliklinik inner join penjab " +
                    "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.kd_dokter=dokter.kd_dokter " +
                    "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj " +
                    "and reg_periksa.kd_poli=poliklinik.kd_poli and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel " +
                    "where reg_periksa.stts_daftar like ? and reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.kd_poli='IGDK' " +
                    "and reg_periksa.tgl_registrasi between ? and ? ";

            boolean adaFilter = !(nmpoli.getText().trim().equals("")&&nmdokter.getText().trim().equals("")&&nmpenjab.getText().trim().equals("")&&nmkabupaten.getText().trim().equals("")&&nmkecamatan.getText().trim().equals("")&&nmkelurahan.getText().trim().equals("")&&TCari.getText().trim().equals(""));
            if(adaFilter){
                sql = sql + "and poliklinik.nm_poli like ? and dokter.nm_dokter like ? and penjab.png_jawab like ? and kabupaten.nm_kab like ? and kecamatan.nm_kec like ? and kelurahan.nm_kel like ? and "+
                      "(poliklinik.nm_poli like ? or dokter.nm_dokter like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.alamat like ?) ";
            }
            ps = koneksi.prepareStatement(sql);
            try {
                ps.setString(1,"%"+status+"%");
                ps.setString(2,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(3,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                if(adaFilter){
                    ps.setString(4,"%"+nmpoli.getText().trim()+"%");
                    ps.setString(5,"%"+nmdokter.getText().trim()+"%");
                    ps.setString(6,"%"+nmpenjab.getText().trim()+"%");
                    ps.setString(7,"%"+nmkabupaten.getText().trim()+"%");
                    ps.setString(8,"%"+nmkecamatan.getText().trim()+"%");
                    ps.setString(9,"%"+nmkelurahan.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                    ps.setString(12,"%"+TCari.getText().trim()+"%");
                    ps.setString(13,"%"+TCari.getText().trim()+"%");
                    ps.setString(14,"%"+TCari.getText().trim()+"%");
                }
                rs = ps.executeQuery();
                if(rs.next()){
                    Object[][] baris = new Object[][]{
                        {"• IGD Bedah :", rs.getInt("bedah_total"), "• IGD Non Bedah :", rs.getInt("nonbedah_total")},
                        {"◦ Lama", rs.getInt("bedah_lama"), "◦ Lama", rs.getInt("nonbedah_lama")},
                        {"▪ BPJS", rs.getInt("bedah_lama_bpjs"), "▪ BPJS", rs.getInt("nonbedah_lama_bpjs")},
                        {"▪ Umum", rs.getInt("bedah_lama_umum"), "▪ Umum", rs.getInt("nonbedah_lama_umum")},
                        {"▪ Instansi", rs.getInt("bedah_lama_instansi"), "▪ Instansi", rs.getInt("nonbedah_lama_instansi")},
                        {"◦ Baru", rs.getInt("bedah_baru"), "◦ Baru", rs.getInt("nonbedah_baru")},
                        {"▪ BPJS", rs.getInt("bedah_baru_bpjs"), "▪ BPJS", rs.getInt("nonbedah_baru_bpjs")},
                        {"▪ Umum", rs.getInt("bedah_baru_umum"), "▪ Umum", rs.getInt("nonbedah_baru_umum")},
                        {"▪ Instansi", rs.getInt("bedah_baru_instansi"), "▪ Instansi", rs.getInt("nonbedah_baru_instansi")}
                    };
                    for(Object[] barisData : baris){
                        tabMode9.addRow(barisData);
                    }
                }
            } catch (Exception e) {
                System.out.println("laporan.DlgKunjunganRalan.tampilRekapIGD() : "+e);
            } finally {
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi tampilRekapIGD : "+e);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void tampilIGD(DefaultTableModel model, String kategori, String labelTotal){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(model);
            lama=0;baru=0;laki=0;per=0;

            String kategoriSql = "";
            if(kategori.equals("BEDAH")){
                kategoriSql = " and reg_periksa.kd_dokter='D0000173' ";
            }else if(kategori.equals("NON_BEDAH")){
                kategoriSql = " and reg_periksa.kd_dokter<>'D0000173' ";
            }else if(kategori.equals("BPJS")){
                kategoriSql = " and reg_periksa.kd_pj='BPJ' ";
            }else if(kategori.equals("UMUM")){
                kategoriSql = " and reg_periksa.kd_pj='001' ";
            }else if(kategori.equals("INSTANSI")){
                kategoriSql = " and reg_periksa.kd_pj not in ('BPJ','001') ";
            }

            String sql =
                    "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.stts_daftar," +
                    "dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,poliklinik.nm_poli,"+
                    "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar " +
                    "from reg_periksa inner join dokter inner join pasien inner join poliklinik inner join penjab " +
                    "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.kd_dokter=dokter.kd_dokter " +
                    "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj " +
                    "and reg_periksa.kd_poli=poliklinik.kd_poli and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel " +
                    "where reg_periksa.stts_daftar like '%"+status+"%' and reg_periksa.status_lanjut='Ralan' and ifnull(reg_periksa.stts,'')<>'Batal' and reg_periksa.kd_poli='IGDK' " + kategoriSql +
                    "and reg_periksa.tgl_registrasi between ? and ? ";

            boolean adaFilter = !(nmpoli.getText().trim().equals("")&&nmdokter.getText().trim().equals("")&&nmpenjab.getText().trim().equals("")&&nmkabupaten.getText().trim().equals("")&&nmkecamatan.getText().trim().equals("")&&nmkelurahan.getText().trim().equals("")&&TCari.getText().trim().equals(""));
            if(adaFilter){
                sql = sql + "and poliklinik.nm_poli like ? and dokter.nm_dokter like ? and penjab.png_jawab like ? and kabupaten.nm_kab like ? and kecamatan.nm_kec like ? and kelurahan.nm_kel like ? and "+
                      "(poliklinik.nm_poli like ? or dokter.nm_dokter like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.alamat like ?) ";
            }
            sql = sql + "order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg";
            ps=koneksi.prepareStatement(sql);

            try {
                ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                if(adaFilter){
                    ps.setString(3,"%"+nmpoli.getText().trim()+"%");
                    ps.setString(4,"%"+nmdokter.getText().trim()+"%");
                    ps.setString(5,"%"+nmpenjab.getText().trim()+"%");
                    ps.setString(6,"%"+nmkabupaten.getText().trim()+"%");
                    ps.setString(7,"%"+nmkecamatan.getText().trim()+"%");
                    ps.setString(8,"%"+nmkelurahan.getText().trim()+"%");
                    ps.setString(9,"%"+TCari.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                    ps.setString(12,"%"+TCari.getText().trim()+"%");
                    ps.setString(13,"%"+TCari.getText().trim()+"%");
                }

                rs=ps.executeQuery();
                int nomor = 1;
                while(rs.next()){
                    setbaru="";
                    setlama="";
                    if(rs.getString("stts_daftar").equals("Baru")){
                        setbaru=rs.getString("no_rkm_medis");
                        baru++;
                    }else if(rs.getString("stts_daftar").equals("Lama")){
                        setlama=rs.getString("no_rkm_medis");
                        lama++;
                    }
                    umurlk="";
                    umurpr="";
                    if(rs.getString("jk").equals("L")){
                        umurlk=rs.getString("umur");
                        laki++;
                    }else if(rs.getString("jk").equals("P")){
                        umurpr=rs.getString("umur");
                        per++;
                    }
                    diagnosa="";
                    kddiangnosa="";
                    ps2=koneksi.prepareStatement("select penyakit.kd_penyakit,penyakit.nm_penyakit from penyakit inner join diagnosa_pasien " +
                        "on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                        "where diagnosa_pasien.no_rawat=? order by prioritas asc limit 1");
                    try {
                        ps2.setString(1,rs.getString("no_rawat"));
                        rs2=ps2.executeQuery();
                        if(rs2.next()){
                            kddiangnosa=rs2.getString(1);
                            diagnosa=rs2.getString(2);
                        }
                    } catch (Exception e) {
                        System.out.println("laporan.DlgKunjunganRalan.tampilIGD() 2 :"+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }

                    model.addRow(new Object[]{
                        nomor++,
                        setlama,
                        setbaru,
                        rs.getString("nm_pasien"),
                        umurlk,
                        umurpr,
                        rs.getString("almt_pj"),
                        kddiangnosa,
                        diagnosa,
                        rs.getString("nm_dokter")
                    });
                }

                if(model.getRowCount()>0){
                    model.addRow(new Object[]{
                        "TOTAL",
                        lama,
                        baru,
                        labelTotal,
                        laki,
                        per,
                        "",
                        "",
                        "Total",
                        (lama+baru)
                    });
                }
            } catch (Exception e) {
                System.out.println("laporan.DlgKunjunganRalan.tampilIGD() : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi tampilIGD : "+e);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void getData() {
        int row=table1.getSelectedRow();
        if(row!= -1){
            TKd.setText(tabMode.getValueAt(row,0).toString());
        }
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,126));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }

}
