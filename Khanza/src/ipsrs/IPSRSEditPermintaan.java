package ipsrs;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable2;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDepartemen;
import kepegawaian.DlgCariPegawai;

public class IPSRSEditPermintaan extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private int jml=0,i=0,row=0,index=0;
    private String[] jumlah,kodebarang,namabarang,satuan,jenis,keterangan;
    private WarnaTable2 warna=new WarnaTable2();
    private DlgCariPegawai pegawai=new DlgCariPegawai(null,false);
    private IPSRSCariPermintaan form=new IPSRSCariPermintaan(null,false);
    private IPSRSBarang barang=new IPSRSBarang(null,false);
    private boolean sukses=true;
    private File file;
    private FileWriter fileWriter;
    private String iyem;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private FileReader myObj;
    private DlgCariDepartemen departemen=new DlgCariDepartemen(null,false);
    // --- simpan daftar kode asal (di level class) ---
    private final java.util.List<String> kodeAsal = new java.util.ArrayList<>();
    // daftar kode yang SUDAH ada pada detail permintaan (dipakai saat simpan)
    private final java.util.Set<String> kodeExisting = new java.util.HashSet<>();


    /** Creates new form DlgProgramStudi
     * @param parent
     * @param modal */
    public IPSRSEditPermintaan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Object[] judul={
            "Jml",
            "Kode Barang",
            "Nama Barang",
            "Satuan",
            "Jenis Barang",
            "Keterangan"
        };
        
        tabMode=new DefaultTableModel(null,judul){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if ( (colIndex==0) || (colIndex==1) || (colIndex==5) ) {
                    a=true;
                }
                return a;
             }
        };
        tbDokter.setModel(tabMode);

        tbDokter.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 6; i++) {
            TableColumn column = tbDokter.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(42);
            }else if(i==1){
                column.setPreferredWidth(90);
            }else if(i==2){
                column.setPreferredWidth(290);
            }else if(i==3){
                column.setPreferredWidth(50);
            }else if(i==4){
                column.setPreferredWidth(100);
            }else if(i==5){
                column.setPreferredWidth(200);
            }
        }
        warna.kolom=0;
        tbDokter.setDefaultRenderer(Object.class,warna);
        
        tbDokter.getColumnModel().getColumn(1)
       .setCellEditor(new AutoCompleteBarangEditor(koneksi, tbDokter));

        NoPermintaan.setDocument(new batasInput((byte)15).getKata(NoPermintaan));
        NmDepartemen.setDocument(new batasInput((byte)50).getKata(NmDepartemen));
        kdptg.setDocument(new batasInput((byte)25).getKata(kdptg));        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil2();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil2();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil2();
                    }
                }
            });
        }
        
        pegawai.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(pegawai.getTable().getSelectedRow()!= -1){                   
                    kdptg.setText(pegawai.tbKamar.getValueAt(pegawai.tbKamar.getSelectedRow(),0).toString());
                    nmptg.setText(pegawai.tbKamar.getValueAt(pegawai.tbKamar.getSelectedRow(),1).toString());
                    Departemen.setText(pegawai.tbKamar.getValueAt(pegawai.tbKamar.getSelectedRow(),5).toString());
                    NmDepartemen.setText(Sequel.cariIsi("Select nama from departemen where dep_id =?", Departemen.getText()));
                }   
                kdptg.requestFocus();
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
        
        pegawai.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    pegawai.dispose();
                }                
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        departemen.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(departemen.getTable().getSelectedRow()!= -1){
                    kdDepartemen.setText(departemen.getTable().getValueAt(departemen.getTable().getSelectedRow(),0).toString());
                    NmDepartemen.setText(departemen.getTable().getValueAt(departemen.getTable().getSelectedRow(),1).toString());
                    BtnDepartemen.requestFocus();
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

        Popup = new javax.swing.JPopupMenu();
        ppBersihkan = new javax.swing.JMenuItem();
        kdDepartemen = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        scrollPane1 = new widget.ScrollPane();
        tbDokter = new widget.Table();
        panelisi1 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        label10 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari1 = new widget.Button();
        BtnAll = new widget.Button();
        BtnTambah = new widget.Button();
        label12 = new widget.Label();
        BtnCari = new widget.Button();
        BtnKeluar = new widget.Button();
        panelisi3 = new widget.panelisi();
        label15 = new widget.Label();
        NoPermintaan = new widget.TextBox();
        label11 = new widget.Label();
        Tanggal = new widget.Tanggal();
        label13 = new widget.Label();
        label16 = new widget.Label();
        kdptg = new widget.TextBox();
        nmptg = new widget.TextBox();
        btnPetugas = new widget.Button();
        label14 = new widget.Label();
        Departemen = new widget.TextBox();
        NmDepartemen = new widget.TextBox();
        BtnDepartemen = new widget.Button();

        Popup.setName("Popup"); // NOI18N

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Bersihkan Jumlah");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(200, 25));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        Popup.add(ppBersihkan);

        kdDepartemen.setName("kdDepartemen"); // NOI18N
        kdDepartemen.setPreferredSize(new java.awt.Dimension(70, 23));
        kdDepartemen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdDepartemenKeyPressed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Permintaan Barang Non Medis dan Penunjang ( Lab & RO ) ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        scrollPane1.setComponentPopupMenu(Popup);
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
        tbDokter.setToolTipText("Masukkan jumlah pengajuan di ujung paling kiri pada warna biru kemudian geser kanan");
        tbDokter.setComponentPopupMenu(Popup);
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

        panelisi1.setName("panelisi1"); // NOI18N
        panelisi1.setPreferredSize(new java.awt.Dimension(100, 56));
        panelisi1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan Perubahan ");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(150, 30));
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
        panelisi1.add(BtnSimpan);

        label10.setText("Key Word :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(75, 23));
        panelisi1.add(label10);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(245, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi1.add(TCari);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari1.setMnemonic('1');
        BtnCari1.setToolTipText("Alt+1");
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
        panelisi1.add(BtnCari1);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('2');
        BtnAll.setToolTipText("2Alt+2");
        BtnAll.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
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
        panelisi1.add(BtnAll);

        BtnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambah.setMnemonic('3');
        BtnTambah.setToolTipText("Alt+3");
        BtnTambah.setName("BtnTambah"); // NOI18N
        BtnTambah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahActionPerformed(evt);
            }
        });
        panelisi1.add(BtnTambah);

        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(25, 23));
        panelisi1.add(label12);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnCari.setMnemonic('C');
        BtnCari.setText("Cari");
        BtnCari.setToolTipText("Alt+C");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(100, 30));
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

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 65));
        panelisi3.setLayout(null);

        label15.setText("No .Permintaan  :");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(80, 23));
        panelisi3.add(label15);
        label15.setBounds(0, 20, 110, 30);

        NoPermintaan.setName("NoPermintaan"); // NOI18N
        NoPermintaan.setPreferredSize(new java.awt.Dimension(207, 23));
        NoPermintaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoPermintaanKeyPressed(evt);
            }
        });
        panelisi3.add(NoPermintaan);
        NoPermintaan.setBounds(120, 20, 120, 30);

        label11.setText("Tanggal   :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(80, 23));
        panelisi3.add(label11);
        label11.setBounds(40, 210, 70, 30);

        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TanggalItemStateChanged(evt);
            }
        });
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        panelisi3.add(Tanggal);
        Tanggal.setBounds(120, 210, 100, 30);

        label13.setText("Pegawai  :");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label13);
        label13.setBounds(0, 90, 110, 30);

        label16.setText("Ruangan  :");
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label16);
        label16.setBounds(20, 170, 90, 30);

        kdptg.setEditable(false);
        kdptg.setText("20101730");
        kdptg.setName("kdptg"); // NOI18N
        kdptg.setPreferredSize(new java.awt.Dimension(80, 23));
        kdptg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdptgKeyPressed(evt);
            }
        });
        panelisi3.add(kdptg);
        kdptg.setBounds(120, 90, 76, 30);

        nmptg.setEditable(false);
        nmptg.setName("nmptg"); // NOI18N
        nmptg.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi3.add(nmptg);
        nmptg.setBounds(200, 90, 390, 30);

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
        btnPetugas.setBounds(600, 90, 30, 30);

        label14.setText(" Departemen  :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label14);
        label14.setBounds(0, 130, 110, 30);

        Departemen.setEditable(false);
        Departemen.setName("Departemen"); // NOI18N
        Departemen.setPreferredSize(new java.awt.Dimension(207, 23));
        Departemen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DepartemenActionPerformed(evt);
            }
        });
        panelisi3.add(Departemen);
        Departemen.setBounds(120, 130, 300, 30);

        NmDepartemen.setEditable(false);
        NmDepartemen.setName("NmDepartemen"); // NOI18N
        NmDepartemen.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi3.add(NmDepartemen);
        NmDepartemen.setBounds(120, 170, 300, 30);

        BtnDepartemen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh1.png"))); // NOI18N
        BtnDepartemen.setMnemonic('2');
        BtnDepartemen.setText("Ganti Ruangan");
        BtnDepartemen.setToolTipText("Alt+2");
        BtnDepartemen.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnDepartemen.setName("BtnDepartemen"); // NOI18N
        BtnDepartemen.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDepartemen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDepartemenActionPerformed(evt);
            }
        });
        panelisi3.add(BtnDepartemen);
        BtnDepartemen.setBounds(430, 170, 130, 30);

        internalFrame1.add(panelisi3, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        form.emptTeks(); 
        form.isCek();
        form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.setLocationRelativeTo(internalFrame1);
        form.setAlwaysOnTop(false);
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        form.dispose();
        pegawai.dispose();
        dispose();  
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){            
            dispose();              
        }else{Valid.pindah(evt,BtnSimpan,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed
/*
private void KdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TKdKeyPressed
    Valid.pindah(evt,BtnCari,Nm);
}//GEN-LAST:event_TKdKeyPressed
*/

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        simpanPerubahan();
    }//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnKeluar,TCari);
        }
    }//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnSimpan,BtnKeluar);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampil2();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari1.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            NmDepartemen.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbDokter.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        tampil2();
}//GEN-LAST:event_BtnCari1ActionPerformed

private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            tampil2();
        }else{
            Valid.pindah(evt, BtnSimpan, BtnKeluar);
        }
}//GEN-LAST:event_BtnCari1KeyPressed

private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
            for(i=0;i<tbDokter.getRowCount();i++){ 
                tbDokter.setValueAt("",i,0);
                tbDokter.setValueAt("",i,5);
            }
}//GEN-LAST:event_ppBersihkanActionPerformed

private void tbDokterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDokterMouseClicked
        if(tbDokter.getRowCount()!=0){
            
        }
}//GEN-LAST:event_tbDokterMouseClicked

private void tbDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDokterKeyPressed
        if(tbDokter.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                if((tbDokter.getSelectedColumn()==1)||(tbDokter.getSelectedColumn()==5)){
                    TCari.setText("");
                    TCari.requestFocus();
                }                
            }else if(evt.getKeyCode()==KeyEvent.VK_DELETE){
                i=tbDokter.getSelectedRow();
                if(i!= -1){
                    tbDokter.setValueAt("", i,0);
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCari.setText("");
                TCari.requestFocus();
            }
        }
}//GEN-LAST:event_tbDokterKeyPressed

private void NoPermintaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoPermintaanKeyPressed
        Valid.pindah(evt, BtnSimpan, NmDepartemen);
}//GEN-LAST:event_NoPermintaanKeyPressed

private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt,NoPermintaan,NmDepartemen);
}//GEN-LAST:event_TanggalKeyPressed

private void kdptgKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdptgKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            nmptg.setText(pegawai.tampil3(kdptg.getText()));        
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            NmDepartemen.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnSimpan.requestFocus();  
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnPetugasActionPerformed(null);
        }
}//GEN-LAST:event_kdptgKeyPressed

private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        pegawai.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        pegawai.setLocationRelativeTo(internalFrame1);
        pegawai.setAlwaysOnTop(false);
        pegawai.setVisible(true);
}//GEN-LAST:event_btnPetugasActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            if(Valid.daysOld("./cache/permintaanipsrs.iyem")<8){
                tampil2();
            }else{
                tampil();
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_formWindowOpened

    private void BtnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));        
        barang.emptTeks();
        barang.isCek();
        barang.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        barang.setLocationRelativeTo(internalFrame1);
        barang.setAlwaysOnTop(false);
        barang.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnTambahActionPerformed

    private void TanggalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TanggalItemStateChanged
        try {
            autoNomor();
        } catch (Exception e) {
        }   
    }//GEN-LAST:event_TanggalItemStateChanged

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        tampil();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCari, TCari);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void DepartemenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DepartemenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DepartemenActionPerformed

    private void BtnDepartemenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDepartemenActionPerformed
        departemen.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        departemen.setLocationRelativeTo(internalFrame1);
        departemen.setAlwaysOnTop(false);
        departemen.setVisible(true);
    }//GEN-LAST:event_BtnDepartemenActionPerformed

    private void kdDepartemenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdDepartemenKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kdDepartemenKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            IPSRSEditPermintaan dialog = new IPSRSEditPermintaan(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari1;
    private widget.Button BtnDepartemen;
    private widget.Button BtnKeluar;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambah;
    private widget.TextBox Departemen;
    private widget.TextBox NmDepartemen;
    private widget.TextBox NoPermintaan;
    private javax.swing.JPopupMenu Popup;
    private widget.TextBox TCari;
    private widget.Tanggal Tanggal;
    private widget.Button btnPetugas;
    private widget.InternalFrame internalFrame1;
    private widget.TextBox kdDepartemen;
    private widget.TextBox kdptg;
    private widget.Label label10;
    private widget.Label label11;
    private widget.Label label12;
    private widget.Label label13;
    private widget.Label label14;
    private widget.Label label15;
    private widget.Label label16;
    private widget.TextBox nmptg;
    private widget.panelisi panelisi1;
    private widget.panelisi panelisi3;
    private javax.swing.JMenuItem ppBersihkan;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbDokter;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        try{
            Valid.tabelKosong(tabMode);
            file=new File("./cache/permintaanipsrs.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            iyem="";
            
            ps=koneksi.prepareStatement(
                "select ipsrsbarang.kode_brng,ipsrsbarang.nama_brng,ipsrsbarang.kode_sat,ipsrsjenisbarang.nm_jenis "+
                " from ipsrsbarang inner join ipsrsjenisbarang on ipsrsbarang.jenis=ipsrsjenisbarang.kd_jenis "+
                " where ipsrsbarang.status='1' order by ipsrsbarang.nama_brng");
            try {
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        "",rs.getString(1),rs.getString(2),rs.getString(3),
                        rs.getString(4),""
                    });
                    iyem=iyem+"{\"KodeBarang\":\""+rs.getString(1)+"\",\"NamaBarang\":\""+rs.getString(2).replaceAll("\"","")+"\",\"Satuan\":\""+rs.getString(3)+"\",\"Jenis\":\""+rs.getString(4)+"\"},";
                } 
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }      
            fileWriter.write("{\"permintaanipsrs\":["+iyem.substring(0,iyem.length()-1)+"]}");
            fileWriter.flush();
            fileWriter.close();
            iyem=null;          
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        
    }

    public void tampil2() {
        
    try {
        // --- 0) Simpan sementara input jumlah/keterangan yang sudah diketik user agar tidak hilang ---
        int row = tbDokter.getRowCount();
        java.util.Map<String,String> jumlahTyped = new java.util.HashMap<>();
        java.util.Map<String,String> ketTyped    = new java.util.HashMap<>();
        for (int i = 0; i < row; i++) {
            Object okode = tbDokter.getValueAt(i, 1);
            if (okode == null) continue;
            String kode = okode.toString().trim();
            if (kode.isEmpty()) continue;

            Object ojml = tbDokter.getValueAt(i, 0);
            String jstr = (ojml == null) ? "" : ojml.toString().trim();
            if (!jstr.isEmpty()) { // hanya simpan yang ada jumlahnya
                jumlahTyped.put(kode, jstr);
                Object oket = tbDokter.getValueAt(i, 5);
                ketTyped.put(kode, oket == null ? "" : oket.toString());
            }
        }

        Valid.tabelKosong(tabMode);
        kodeExisting.clear();

        String no = (NoPermintaan.getText() == null) ? "" : NoPermintaan.getText().trim();
        if (no.isEmpty()) {
//            JOptionPane.showMessageDialog(this, "No. Permintaan belum diisi.");
            return;
        }

        // --- 1) Taruh barang yang SUDAH ada di permintaan (paling atas) ---
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlDetail =
            "SELECT d.jumlah, b.kode_brng, b.nama_brng, " +
            "       IFNULL(d.kode_sat,b.kode_sat) AS kode_sat, IFNULL(j.nm_jenis,'') AS jenis, " +
            "       IFNULL(d.keterangan,'') AS keterangan " +
            "FROM detail_permintaan_non_medis d " +
            "JOIN ipsrsbarang b ON b.kode_brng=d.kode_brng " +
            "LEFT JOIN ipsrsjenisbarang j ON j.kd_jenis=b.jenis " +
            "WHERE d.no_permintaan=? " +
            "ORDER BY b.nama_brng";
        try {
            ps = koneksi.prepareStatement(sqlDetail);
            ps.setString(1, no);
            rs = ps.executeQuery();
            while (rs.next()) {
                String kode   = rs.getString("kode_brng");
                String nama   = rs.getString("nama_brng");
                String satuan = rs.getString("kode_sat");
                String jenis  = rs.getString("jenis");

                // pertahankan jumlah/keterangan yang sudah diketik user jika ada
                String jml = jumlahTyped.getOrDefault(kode, String.valueOf(rs.getInt("jumlah")));
                String ket = ketTyped.getOrDefault(kode, rs.getString("keterangan"));

                tabMode.addRow(new Object[]{ jml, kode, nama, satuan, jenis, ket });
                kodeExisting.add(kode);
            }
        } catch (Exception e) {
            System.out.println("Notifikasi (detail): " + e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ig) {}
            try { if (ps != null) ps.close(); } catch (Exception ig) {}
        }

        // --- 2) Tambahkan barang aktif lain (yang belum ada di permintaan) dengan jumlah kosong ---
        java.io.FileReader myObj = null;
        try {
            myObj = new java.io.FileReader("./cache/permintaanipsrs.iyem");
            root = mapper.readTree(myObj);
            response = root.path("permintaanipsrs");
            String q = (TCari.getText() == null) ? "" : TCari.getText().toLowerCase();

            if (response.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode list : response) {
                    String kode  = list.path("KodeBarang").asText();
                    String nama  = list.path("NamaBarang").asText();
                    String satu  = list.path("Satuan").asText();
                    String jenis = list.path("Jenis").asText();

                    if (kodeExisting.contains(kode)) continue; // sudah ditaruh di atas

                    // filter sesuai TCari (kode/nama/jenis)
                    if (q.isEmpty()
                        || kode.toLowerCase().contains(q)
                        || nama.toLowerCase().contains(q)
                        || jenis.toLowerCase().contains(q)) {

                        tabMode.addRow(new Object[]{ "", kode, nama, satu, jenis, "" });
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi (cache): " + e);
        } finally {
            try { if (myObj != null) myObj.close(); } catch (Exception ig) {}
        }

    } catch (Exception e) {
        System.out.println("Notifikasi : " + e);
    }
    
    }
    
    public void isCek(){
        autoNomor();
        TCari.requestFocus();
        if(akses.getjml2()>=1){
            kdptg.setEditable(false);
            btnPetugas.setEnabled(false);
            kdptg.setText(akses.getkode());
            BtnSimpan.setEnabled(akses.getpermintaan_non_medis());
            BtnTambah.setEnabled(akses.getipsrs_barang());
            nmptg.setText(pegawai.tampil3(kdptg.getText()));
            Departemen.setText(pegawai.tampilDepartemen(kdptg.getText()));
            NmDepartemen.setText(Sequel.cariIsi("Select nama from departemen where dep_id =?", Departemen.getText()));
        }        
    }
    
    private void autoNomor() {
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(permintaan_non_medis.no_permintaan,3),signed)),0) from permintaan_non_medis where permintaan_non_medis.tanggal='"+Valid.SetTgl(Tanggal.getSelectedItem()+"")+"' ",
                "PN"+Tanggal.getSelectedItem().toString().substring(8,10)+Tanggal.getSelectedItem().toString().substring(3,5)+Tanggal.getSelectedItem().toString().substring(0,2),3,NoPermintaan); 
    }

 private void simpanPerubahan() {
    String no = NoPermintaan.getText()==null ? "" : NoPermintaan.getText().trim();
    if (no.isEmpty()) {
        JOptionPane.showMessageDialog(this,"No. Permintaan belum diisi.");
        return;
    }

    PreparedStatement psSat = null, psIns = null, psUpd = null, psDel = null;
    ResultSet rs = null;

    final String sqlSat = "SELECT IFNULL(kode_sat,'') FROM ipsrsbarang WHERE kode_brng=? LIMIT 1";
    final String sqlIns = "INSERT INTO detail_permintaan_non_medis(no_permintaan,kode_brng,kode_sat,jumlah,keterangan) VALUES(?,?,?,?,?)";
    final String sqlUpd = "UPDATE detail_permintaan_non_medis SET jumlah=?, keterangan=?, kode_sat=? WHERE no_permintaan=? AND kode_brng=? LIMIT 1";
    final String sqlDel = "DELETE FROM detail_permintaan_non_medis WHERE no_permintaan=? AND kode_brng=? LIMIT 1";

    try {
        koneksi.setAutoCommit(false);

        psSat = koneksi.prepareStatement(sqlSat);
        psIns = koneksi.prepareStatement(sqlIns);
        psUpd = koneksi.prepareStatement(sqlUpd);
        psDel = koneksi.prepareStatement(sqlDel);

        for (int i=0; i<tabMode.getRowCount(); i++) {
            String kode = String.valueOf(tabMode.getValueAt(i, 1));
            if (kode==null || kode.trim().isEmpty()) continue;

            Object oJumlah = tabMode.getValueAt(i, 0);
            int jumlahBaru = 0;
            if (oJumlah != null && !oJumlah.toString().trim().isEmpty()) {
                try { jumlahBaru = Integer.parseInt(oJumlah.toString().trim()); } catch (Exception ignore) { jumlahBaru = 0; }
            }
            String ketBaru = tabMode.getValueAt(i, 5)==null ? "" : tabMode.getValueAt(i, 5).toString();

            // dapatkan satuan dari master (agar konsisten)
            String sat = "";
            psSat.setString(1, kode);
            rs = psSat.executeQuery();
            if (rs.next()) sat = rs.getString(1);
            if (rs != null) { rs.close(); rs = null; }

            if (kodeExisting.contains(kode)) {
                // baris lama
                if (jumlahBaru <= 0) {
                    // hapus
                    psDel.setString(1, no);
                    psDel.setString(2, kode);
                    psDel.executeUpdate();
                } else {
                    // update
                    psUpd.setInt(1, jumlahBaru);
                    psUpd.setString(2, ketBaru);
                    psUpd.setString(3, sat);
                    psUpd.setString(4, no);
                    psUpd.setString(5, kode);
                    psUpd.executeUpdate();
                }
            } else {
                // baris baru
                if (jumlahBaru > 0) {
                    psIns.setString(1, no);
                    psIns.setString(2, kode);
                    psIns.setString(3, sat);
                    psIns.setInt(4, jumlahBaru);
                    psIns.setString(5, ketBaru);
                    psIns.executeUpdate();
                }
            }
        }

        koneksi.commit();
        JOptionPane.showMessageDialog(this,"Perubahan detail permintaan tersimpan.");
        // reload tampilan untuk refresh kodeExisting dan data
        tampil();

    } catch (Exception e) {
        try { koneksi.rollback(); } catch (Exception ig) {}
        JOptionPane.showMessageDialog(this,"Gagal menyimpan: "+e.getMessage());
        e.printStackTrace();
    } finally {
        try { if (rs != null) rs.close(); } catch (Exception ig) {}
        try { if (psSat != null) psSat.close(); } catch (Exception ig) {}
        try { if (psIns != null) psIns.close(); } catch (Exception ig) {}
        try { if (psUpd != null) psUpd.close(); } catch (Exception ig) {}
        try { if (psDel != null) psDel.close(); } catch (Exception ig) {}
        try { koneksi.setAutoCommit(true); } catch (Exception ig) {}
    }
}

    public void setNoRM(String nopermintaan) {
     NoPermintaan.setText(nopermintaan);
    }
 

// === Editor kolom 'Kode Barang' dengan autocomplete sederhana (ComboBox) ===
private class AutoCompleteBarangEditor extends javax.swing.DefaultCellEditor {
    private final javax.swing.JComboBox<String> combo;
    private final javax.swing.JTextField editor;
    private final Connection conn;
    private final javax.swing.JTable tableRef;
    private int editingRow = -1;

    public AutoCompleteBarangEditor(Connection conn, javax.swing.JTable tableRef) {
        super(new javax.swing.JComboBox<>());
        this.conn = conn;
        this.tableRef = tableRef;
        this.combo = (javax.swing.JComboBox<String>) getComponent();
        this.combo.setEditable(true);
        this.editor = (javax.swing.JTextField) combo.getEditor().getEditorComponent();

        // Listener: ketik untuk memuat pilihan
        this.editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = editor.getText() == null ? "" : editor.getText().trim();
                loadSuggestions(q);
                editor.setText(q);
                editor.setCaretPosition(q.length());
            }
        });
    }

    private void loadSuggestions(String q) {
        javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>();
        String sql = "SELECT kode_brng, nama_brng FROM ipsrsbarang "
                   + "WHERE status='1' AND (kode_brng LIKE ? OR nama_brng LIKE ?) "
                   + "ORDER BY nama_brng LIMIT 20";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + q + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String kode = rs.getString(1);
                    String nama = rs.getString(2);
                    model.addElement(kode + " - " + nama);
                }
            }
        } catch (Exception ex) {
            // diamkan agar editing tetap jalan
        }
        combo.setModel(model);
        combo.setSelectedItem(q);
    }

    @Override
    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
        this.editingRow = row;
        String text = value == null ? "" : value.toString();
        combo.setSelectedItem(text);
        return combo;
    }

    @Override
    public Object getCellEditorValue() {
        Object sel = combo.getEditor().getItem();
        String text = sel == null ? "" : sel.toString().trim();
        // dukung format "KODE - Nama"
        String kode = text;
        int idx = text.indexOf(" - ");
        if (idx > 0) {
            kode = text.substring(0, idx).trim();
        }
        // saat user memilih, update kolom Nama & Satuan otomatis
        updateNamaDanSatuan(kode);
        return kode;
    }

    private void updateNamaDanSatuan(String kode) {
        if (editingRow < 0) return;
        String sql = "SELECT nama_brng, IFNULL(kode_sat,'') FROM ipsrsbarang WHERE kode_brng=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nama   = rs.getString(1);
                    String satuan = rs.getString(2);
                    tableRef.setValueAt(kode, editingRow, 1);
                    tableRef.setValueAt(nama, editingRow, 2);
                    tableRef.setValueAt(satuan, editingRow, 3);
                }
            }
        } catch (Exception ex) {
            // abaikan bila gagal lookup
        }
    }
}

}
