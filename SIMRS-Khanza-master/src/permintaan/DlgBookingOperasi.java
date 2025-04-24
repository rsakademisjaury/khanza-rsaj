package permintaan;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import inventory.DlgPeresepanDokter;
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
import keuangan.DlgCariDaftarOperasi;
import rekammedis.RMChecklistPostOperasi;
import rekammedis.RMChecklistPreOperasi;
import rekammedis.RMPenilaianPreAnastesi;
import rekammedis.RMPenilaianPreOperasi;
import simrskhanza.DlgKamarInap;
import rekammedis.RMRiwayatPerawatan;
import rekammedis.RMSignInSebelumAnastesi;
import rekammedis.RMSignOutSebelumMenutupLuka;
import rekammedis.RMTimeOutSebelumInsisi;
import rekammedis.RMTransferPasienAntarRuang;
import setting.DlgCariRuangOperasi;
import simrskhanza.DlgTagihanOperasi;

/**
 *
 * @author dosen
 */
public class DlgBookingOperasi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs,rs2;
    private int i=0;
    private String status="",bangsal="",lokasistok="",kamar="",diagnosa="",order="",kelas="",penjab="",norawatibu="",posisi="",norm="";
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private DlgCariRuangOperasi ruangok=new DlgCariRuangOperasi(null,false);
    private DlgCariDaftarOperasi operasi=new DlgCariDaftarOperasi(null,false);
    
    

    /** Creates new form DlgPemberianInfus
     * @param parent
     * @param modal */
    public DlgBookingOperasi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        tabMode=new DefaultTableModel(null,new Object[]{
                "No.","No.Rawat","Nama Pasien","Umur","J.K.","Tanggal","Mulai",
                "Selesai","Status","Rujukan Dari","Diagnosa","Kode Operasi","Operasi",
                "Kode Operator","Operator","Order","Kode OK","Nama Ruang Operasi","Dokter Anastesi","Perawat"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 20; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(28);
            }else if(i==1){
                column.setPreferredWidth(105);
            }else if(i==2){
                column.setPreferredWidth(170);
            }else if(i==3){
                column.setPreferredWidth(50);
            }else if(i==4){
                column.setPreferredWidth(30);
            }else if(i==5){
                column.setPreferredWidth(65);
            }else if(i==6){
                column.setPreferredWidth(50);
            }else if(i==7){
                column.setPreferredWidth(50);
            }else if(i==8){
                column.setPreferredWidth(60);
            }else if(i==9){
                column.setPreferredWidth(120);
            }else if(i==10){
                column.setPreferredWidth(150);
            }else if(i==11){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==12){
                column.setPreferredWidth(150);
            }else if(i==13){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==14){
                column.setPreferredWidth(150);
            }else if(i==15){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==16){
                column.setPreferredWidth(50);
            }else if(i==17){
                column.setPreferredWidth(130);
            }else if(i==18){
                column.setPreferredWidth(130);
            }else if(i==19){
                column.setPreferredWidth(130);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());


        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        KdDokter.setDocument(new batasInput((byte)3).getKata(KdDokter));
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
        
        ChkInput.setSelected(true);
        isForm();
        
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
        
        ruangok.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {;}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(ruangok.getTable().getSelectedRow()!= -1){                    
                    KdRuangOperasi.setText(ruangok.getTable().getValueAt(ruangok.getTable().getSelectedRow(),0).toString());
                    NmRuangOperasi.setText(ruangok.getTable().getValueAt(ruangok.getTable().getSelectedRow(),1).toString());
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
        
        operasi.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {;}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(operasi.getTable().getSelectedRow()!= -1){                    
                    KdOperasi.setText(operasi.getTable().getValueAt(operasi.getTable().getSelectedRow(),0).toString());
                    NmOperasi.setText(operasi.getTable().getValueAt(operasi.getTable().getSelectedRow(),1).toString());
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
        WindowLaporan = new javax.swing.JDialog();
        internalFrame6 = new widget.InternalFrame();
        panelGlass6 = new widget.panelisi();
        BtnSimpan1 = new widget.Button();
        BtnPrint1 = new widget.Button();
        BtnCloseIn5 = new widget.Button();
        Scroll3 = new widget.ScrollPane();
        Laporan = new widget.TextArea();
        panelGlass7 = new widget.panelisi();
        label12 = new widget.Label();
        tgl2 = new widget.Tanggal();
        jLabel8 = new widget.Label();
        PreOp = new widget.TextBox();
        PostOp = new widget.TextBox();
        jLabel15 = new widget.Label();
        jLabel16 = new widget.Label();
        Jaringan = new widget.TextBox();
        DikirimPA = new widget.ComboBox();
        jLabel17 = new widget.Label();
        label11 = new widget.Label();
        tgl = new widget.Tanggal();
        jLabel18 = new widget.Label();
        Komplikasi = new widget.TextBox();
        jLabel19 = new widget.Label();
        Pendarahan = new widget.TextBox();
        jLabel20 = new widget.Label();
        Transfusi = new widget.TextBox();
        jLabel21 = new widget.Label();
        Implant = new widget.TextBox();
        jLabel23 = new widget.Label();
        Residen = new widget.TextBox();
        Tindakan = new widget.TextBox();
        jLabel24 = new widget.Label();
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
        panelGlass10 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        panelCari = new widget.panelisi();
        R5 = new widget.RadioButton();
        R1 = new widget.RadioButton();
        R2 = new widget.RadioButton();
        DTPCari1 = new widget.Tanggal();
        jLabel22 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        R4 = new widget.RadioButton();
        R3 = new widget.RadioButton();
        DTPCari3 = new widget.Tanggal();
        jLabel25 = new widget.Label();
        DTPCari4 = new widget.Tanggal();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        jLabel9 = new widget.Label();
        NmDokter = new widget.TextBox();
        TPasien = new widget.TextBox();
        DTPTgl = new widget.Tanggal();
        Status = new widget.ComboBox();
        jLabel10 = new widget.Label();
        KdDokter = new widget.TextBox();
        BtnOperator = new widget.Button();
        Kamar = new widget.TextBox();
        jLabel35 = new widget.Label();
        JamMulai = new widget.ComboBox();
        MenitMulai = new widget.ComboBox();
        DetikMulai = new widget.ComboBox();
        DetikSelesai = new widget.ComboBox();
        MenitSelesai = new widget.ComboBox();
        JamSelesai = new widget.ComboBox();
        jLabel36 = new widget.Label();
        jLabel37 = new widget.Label();
        jLabel11 = new widget.Label();
        KdOperasi = new widget.TextBox();
        NmOperasi = new widget.TextBox();
        BtnOperasi = new widget.Button();
        jLabel12 = new widget.Label();
        KdRuangOperasi = new widget.TextBox();
        NmRuangOperasi = new widget.TextBox();
        BtnRuangOperasi = new widget.Button();
        jLabel13 = new widget.Label();
        NmDokterAnastesi = new widget.TextBox();
        jLabel14 = new widget.Label();
        Perawat = new widget.TextBox();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        ScrollMenu = new widget.ScrollPane();
        FormMenu = new widget.PanelBiasa();
        BtnKamarInap = new widget.Button();
        BtnPermintaanLab = new widget.Button();
        BtnRiwayatPasien = new widget.Button();
        BtnChecklistPreOperasi = new widget.Button();
        BtnSignInSebelumAnestesi = new widget.Button();
        BtnTimeOutSebelumInsisi = new widget.Button();
        BtnSignOutSebelumMenutupLuka = new widget.Button();
        BtnPreOperasi = new widget.Button();
        BtnPreAnastesi = new widget.Button();
        BtnLaporanOperasi = new widget.Button();
        BtnTagihanOperasi = new widget.Button();
        BtnObatBhp = new widget.Button();
        BtnTransferAntarRuang = new widget.Button();
        BtnChecklistPostOperasi = new widget.Button();

        WindowLaporan.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindowLaporan.setName("WindowLaporan"); // NOI18N
        WindowLaporan.setUndecorated(true);
        WindowLaporan.setResizable(false);
        WindowLaporan.getContentPane().setLayout(new java.awt.BorderLayout(1, 1));

        internalFrame6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Laporan Operasi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame6.setName("internalFrame6"); // NOI18N
        internalFrame6.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass6.setName("panelGlass6"); // NOI18N
        panelGlass6.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan1.setMnemonic('U');
        BtnSimpan1.setText("Simpan");
        BtnSimpan1.setToolTipText("Alt+U");
        BtnSimpan1.setName("BtnSimpan1"); // NOI18N
        BtnSimpan1.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpan1ActionPerformed(evt);
            }
        });
        BtnSimpan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpan1KeyPressed(evt);
            }
        });
        panelGlass6.add(BtnSimpan1);

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

        BtnCloseIn5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnCloseIn5.setMnemonic('U');
        BtnCloseIn5.setText("Tutup");
        BtnCloseIn5.setToolTipText("Alt+U");
        BtnCloseIn5.setName("BtnCloseIn5"); // NOI18N
        BtnCloseIn5.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnCloseIn5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseIn5ActionPerformed(evt);
            }
        });
        panelGlass6.add(BtnCloseIn5);

        internalFrame6.add(panelGlass6, java.awt.BorderLayout.PAGE_END);

        Scroll3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Laporan :", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        Scroll3.setName("Scroll3"); // NOI18N

        Laporan.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        Laporan.setColumns(20);
        Laporan.setForeground(new java.awt.Color(0, 0, 0));
        Laporan.setRows(5);
        Laporan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        Laporan.setName("Laporan"); // NOI18N
        Scroll3.setViewportView(Laporan);

        internalFrame6.add(Scroll3, java.awt.BorderLayout.CENTER);

        panelGlass7.setName("panelGlass7"); // NOI18N
        panelGlass7.setPreferredSize(new java.awt.Dimension(300, 200));
        panelGlass7.setLayout(null);

        label12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label12.setText("Selesai Operasi :");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass7.add(label12);
        label12.setBounds(15, 40, 110, 23);

        tgl2.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        tgl2.setName("tgl2"); // NOI18N
        panelGlass7.add(tgl2);
        tgl2.setBounds(170, 40, 140, 23);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("Diagnosis Pre-operatif :");
        jLabel8.setName("jLabel8"); // NOI18N
        panelGlass7.add(jLabel8);
        jLabel8.setBounds(15, 100, 200, 23);

        PreOp.setHighlighter(null);
        PreOp.setName("PreOp"); // NOI18N
        PreOp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PreOpKeyPressed(evt);
            }
        });
        panelGlass7.add(PreOp);
        PreOp.setBounds(170, 100, 400, 23);

        PostOp.setHighlighter(null);
        PostOp.setName("PostOp"); // NOI18N
        PostOp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PostOpKeyPressed(evt);
            }
        });
        panelGlass7.add(PostOp);
        PostOp.setBounds(170, 130, 400, 23);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel15.setText("Diagnosis Post-operatif :");
        jLabel15.setName("jLabel15"); // NOI18N
        panelGlass7.add(jLabel15);
        jLabel15.setBounds(15, 130, 160, 23);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("Residen :");
        jLabel16.setName("jLabel16"); // NOI18N
        panelGlass7.add(jLabel16);
        jLabel16.setBounds(590, 130, 130, 23);

        Jaringan.setHighlighter(null);
        Jaringan.setName("Jaringan"); // NOI18N
        Jaringan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JaringanKeyPressed(evt);
            }
        });
        panelGlass7.add(Jaringan);
        Jaringan.setBounds(170, 160, 400, 23);

        DikirimPA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ya", "Tidak" }));
        DikirimPA.setName("DikirimPA"); // NOI18N
        DikirimPA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DikirimPAKeyPressed(evt);
            }
        });
        panelGlass7.add(DikirimPA);
        DikirimPA.setBounds(170, 70, 140, 23);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText("Pemeriksaan PA :");
        jLabel17.setName("jLabel17"); // NOI18N
        panelGlass7.add(jLabel17);
        jLabel17.setBounds(15, 70, 120, 23);

        label11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label11.setText("Mulai Operasi :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass7.add(label11);
        label11.setBounds(15, 10, 110, 23);

        tgl.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        tgl.setName("tgl"); // NOI18N
        tgl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tglKeyPressed(evt);
            }
        });
        panelGlass7.add(tgl);
        tgl.setBounds(170, 10, 140, 23);

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel18.setText("Jaringan di-Eksisi / -Insisi :");
        jLabel18.setName("jLabel18"); // NOI18N
        panelGlass7.add(jLabel18);
        jLabel18.setBounds(15, 160, 160, 23);

        Komplikasi.setHighlighter(null);
        Komplikasi.setName("Komplikasi"); // NOI18N
        Komplikasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KomplikasiActionPerformed(evt);
            }
        });
        Komplikasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KomplikasiKeyPressed(evt);
            }
        });
        panelGlass7.add(Komplikasi);
        Komplikasi.setBounds(720, 10, 400, 23);

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel19.setText("Komplikasi :");
        jLabel19.setName("jLabel19"); // NOI18N
        panelGlass7.add(jLabel19);
        jLabel19.setBounds(590, 10, 130, 23);

        Pendarahan.setHighlighter(null);
        Pendarahan.setName("Pendarahan"); // NOI18N
        Pendarahan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PendarahanActionPerformed(evt);
            }
        });
        Pendarahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PendarahanKeyPressed(evt);
            }
        });
        panelGlass7.add(Pendarahan);
        Pendarahan.setBounds(720, 40, 400, 23);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setText("Jumlah Pendarahan :");
        jLabel20.setName("jLabel20"); // NOI18N
        panelGlass7.add(jLabel20);
        jLabel20.setBounds(590, 40, 130, 23);

        Transfusi.setHighlighter(null);
        Transfusi.setName("Transfusi"); // NOI18N
        Transfusi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TransfusiActionPerformed(evt);
            }
        });
        Transfusi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TransfusiKeyPressed(evt);
            }
        });
        panelGlass7.add(Transfusi);
        Transfusi.setBounds(720, 70, 400, 23);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel21.setText("Jumlah Transfusi :");
        jLabel21.setName("jLabel21"); // NOI18N
        panelGlass7.add(jLabel21);
        jLabel21.setBounds(590, 70, 130, 23);

        Implant.setHighlighter(null);
        Implant.setName("Implant"); // NOI18N
        Implant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImplantActionPerformed(evt);
            }
        });
        Implant.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ImplantKeyPressed(evt);
            }
        });
        panelGlass7.add(Implant);
        Implant.setBounds(720, 100, 400, 23);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("Nomor Implant :");
        jLabel23.setName("jLabel23"); // NOI18N
        panelGlass7.add(jLabel23);
        jLabel23.setBounds(590, 100, 130, 23);

        Residen.setHighlighter(null);
        Residen.setName("Residen"); // NOI18N
        Residen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResidenActionPerformed(evt);
            }
        });
        Residen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ResidenKeyPressed(evt);
            }
        });
        panelGlass7.add(Residen);
        Residen.setBounds(720, 130, 400, 23);

        Tindakan.setHighlighter(null);
        Tindakan.setName("Tindakan"); // NOI18N
        Tindakan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TindakanActionPerformed(evt);
            }
        });
        Tindakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TindakanKeyPressed(evt);
            }
        });
        panelGlass7.add(Tindakan);
        Tindakan.setBounds(720, 160, 400, 23);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Tindakan Operasi :");
        jLabel24.setName("jLabel24"); // NOI18N
        panelGlass7.add(jLabel24);
        jLabel24.setBounds(590, 160, 130, 23);

        internalFrame6.add(panelGlass7, java.awt.BorderLayout.NORTH);

        WindowLaporan.getContentPane().add(internalFrame6, java.awt.BorderLayout.CENTER);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Jadwal Operasi Pasien ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(120, 23));
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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        panelGlass10.setName("panelGlass10"); // NOI18N
        panelGlass10.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass10.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(685, 23));
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

        jPanel3.add(panelGlass10, java.awt.BorderLayout.CENTER);

        panelCari.setName("panelCari"); // NOI18N
        panelCari.setPreferredSize(new java.awt.Dimension(44, 43));
        panelCari.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 9));

        buttonGroup1.add(R5);
        R5.setSelected(true);
        R5.setText("Permintaan");
        R5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        R5.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R5.setName("R5"); // NOI18N
        R5.setPreferredSize(new java.awt.Dimension(122, 23));
        panelCari.add(R5);

        buttonGroup1.add(R1);
        R1.setText("Menunggu");
        R1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        R1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R1.setName("R1"); // NOI18N
        R1.setPreferredSize(new java.awt.Dimension(105, 23));
        panelCari.add(R1);

        buttonGroup1.add(R2);
        R2.setText("Tanggal :");
        R2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R2.setName("R2"); // NOI18N
        R2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelCari.add(R2);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "30-01-2025" }));
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

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("s.d");
        jLabel22.setName("jLabel22"); // NOI18N
        jLabel22.setPreferredSize(new java.awt.Dimension(30, 23));
        panelCari.add(jLabel22);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "30-01-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        DTPCari2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari2KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari2);

        buttonGroup1.add(R4);
        R4.setText("Proses");
        R4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        R4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R4.setName("R4"); // NOI18N
        R4.setPreferredSize(new java.awt.Dimension(122, 23));
        panelCari.add(R4);

        buttonGroup1.add(R3);
        R3.setText("Selesai :");
        R3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R3.setName("R3"); // NOI18N
        R3.setPreferredSize(new java.awt.Dimension(75, 23));
        panelCari.add(R3);

        DTPCari3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "30-01-2025" }));
        DTPCari3.setDisplayFormat("dd-MM-yyyy");
        DTPCari3.setName("DTPCari3"); // NOI18N
        DTPCari3.setOpaque(false);
        DTPCari3.setPreferredSize(new java.awt.Dimension(90, 23));
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
        jLabel25.setPreferredSize(new java.awt.Dimension(30, 23));
        panelCari.add(jLabel25);

        DTPCari4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "30-01-2025" }));
        DTPCari4.setDisplayFormat("dd-MM-yyyy");
        DTPCari4.setName("DTPCari4"); // NOI18N
        DTPCari4.setOpaque(false);
        DTPCari4.setPreferredSize(new java.awt.Dimension(90, 23));
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
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 200));
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

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 70, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(73, 10, 120, 23);

        jLabel9.setText("Operator :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(0, 70, 70, 23);

        NmDokter.setEditable(false);
        NmDokter.setHighlighter(null);
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(155, 70, 200, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(195, 10, 208, 23);

        DTPTgl.setForeground(new java.awt.Color(50, 70, 50));
        DTPTgl.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "30-01-2025" }));
        DTPTgl.setDisplayFormat("dd-MM-yyyy");
        DTPTgl.setName("DTPTgl"); // NOI18N
        DTPTgl.setOpaque(false);
        DTPTgl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPTglKeyPressed(evt);
            }
        });
        FormInput.add(DTPTgl);
        DTPTgl.setBounds(73, 40, 90, 23);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Permintaan", "Menunggu", "Proses Operasi", "Selesai" }));
        Status.setName("Status"); // NOI18N
        Status.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusKeyPressed(evt);
            }
        });
        FormInput.add(Status);
        Status.setBounds(703, 40, 140, 23);

        jLabel10.setText("Tanggal :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 40, 70, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        FormInput.add(KdDokter);
        KdDokter.setBounds(73, 70, 80, 23);

        BtnOperator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnOperator.setMnemonic('X');
        BtnOperator.setToolTipText("Alt+X");
        BtnOperator.setName("BtnOperator"); // NOI18N
        BtnOperator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnOperatorActionPerformed(evt);
            }
        });
        BtnOperator.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnOperatorKeyPressed(evt);
            }
        });
        FormInput.add(BtnOperator);
        BtnOperator.setBounds(357, 70, 28, 23);

        Kamar.setEditable(false);
        Kamar.setHighlighter(null);
        Kamar.setName("Kamar"); // NOI18N
        FormInput.add(Kamar);
        Kamar.setBounds(405, 10, 160, 23);

        jLabel35.setText("Mulai :");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(165, 40, 40, 23);

        JamMulai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        JamMulai.setName("JamMulai"); // NOI18N
        JamMulai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamMulaiKeyPressed(evt);
            }
        });
        FormInput.add(JamMulai);
        JamMulai.setBounds(208, 40, 62, 23);

        MenitMulai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        MenitMulai.setName("MenitMulai"); // NOI18N
        MenitMulai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitMulaiKeyPressed(evt);
            }
        });
        FormInput.add(MenitMulai);
        MenitMulai.setBounds(273, 40, 62, 23);

        DetikMulai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        DetikMulai.setName("DetikMulai"); // NOI18N
        DetikMulai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikMulaiKeyPressed(evt);
            }
        });
        FormInput.add(DetikMulai);
        DetikMulai.setBounds(338, 40, 62, 23);

        DetikSelesai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        DetikSelesai.setName("DetikSelesai"); // NOI18N
        DetikSelesai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikSelesaiKeyPressed(evt);
            }
        });
        FormInput.add(DetikSelesai);
        DetikSelesai.setBounds(557, 40, 62, 23);

        MenitSelesai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        MenitSelesai.setName("MenitSelesai"); // NOI18N
        MenitSelesai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitSelesaiKeyPressed(evt);
            }
        });
        FormInput.add(MenitSelesai);
        MenitSelesai.setBounds(492, 40, 62, 23);

        JamSelesai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        JamSelesai.setName("JamSelesai"); // NOI18N
        JamSelesai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamSelesaiKeyPressed(evt);
            }
        });
        FormInput.add(JamSelesai);
        JamSelesai.setBounds(427, 40, 62, 23);

        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("s.d.");
        jLabel36.setName("jLabel36"); // NOI18N
        FormInput.add(jLabel36);
        jLabel36.setBounds(400, 40, 25, 23);

        jLabel37.setText("Status :");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(650, 40, 50, 23);

        jLabel11.setText("Operasi :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(385, 70, 60, 23);

        KdOperasi.setEditable(false);
        KdOperasi.setHighlighter(null);
        KdOperasi.setName("KdOperasi"); // NOI18N
        FormInput.add(KdOperasi);
        KdOperasi.setBounds(448, 70, 80, 23);

        NmOperasi.setEditable(false);
        NmOperasi.setHighlighter(null);
        NmOperasi.setName("NmOperasi"); // NOI18N
        FormInput.add(NmOperasi);
        NmOperasi.setBounds(530, 70, 283, 23);

        BtnOperasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnOperasi.setMnemonic('X');
        BtnOperasi.setToolTipText("Alt+X");
        BtnOperasi.setName("BtnOperasi"); // NOI18N
        BtnOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnOperasiActionPerformed(evt);
            }
        });
        BtnOperasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnOperasiKeyPressed(evt);
            }
        });
        FormInput.add(BtnOperasi);
        BtnOperasi.setBounds(815, 70, 28, 23);

        jLabel12.setText("Ruang OK :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(570, 10, 60, 23);

        KdRuangOperasi.setEditable(false);
        KdRuangOperasi.setHighlighter(null);
        KdRuangOperasi.setName("KdRuangOperasi"); // NOI18N
        FormInput.add(KdRuangOperasi);
        KdRuangOperasi.setBounds(633, 10, 40, 23);

        NmRuangOperasi.setEditable(false);
        NmRuangOperasi.setHighlighter(null);
        NmRuangOperasi.setName("NmRuangOperasi"); // NOI18N
        FormInput.add(NmRuangOperasi);
        NmRuangOperasi.setBounds(675, 10, 138, 23);

        BtnRuangOperasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnRuangOperasi.setMnemonic('X');
        BtnRuangOperasi.setToolTipText("Alt+X");
        BtnRuangOperasi.setName("BtnRuangOperasi"); // NOI18N
        BtnRuangOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRuangOperasiActionPerformed(evt);
            }
        });
        BtnRuangOperasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnRuangOperasiKeyPressed(evt);
            }
        });
        FormInput.add(BtnRuangOperasi);
        BtnRuangOperasi.setBounds(815, 10, 28, 23);

        jLabel13.setText("Dokter Anastesi:");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(0, 100, 80, 23);

        NmDokterAnastesi.setHighlighter(null);
        NmDokterAnastesi.setName("NmDokterAnastesi"); // NOI18N
        NmDokterAnastesi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NmDokterAnastesiActionPerformed(evt);
            }
        });
        FormInput.add(NmDokterAnastesi);
        NmDokterAnastesi.setBounds(160, 100, 200, 23);

        jLabel14.setText("Perawat :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(450, 100, 60, 23);

        Perawat.setHighlighter(null);
        Perawat.setName("Perawat"); // NOI18N
        FormInput.add(Perawat);
        Perawat.setBounds(530, 100, 290, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(155, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout());

        ChkAccor.setBackground(new java.awt.Color(255, 250, 248));
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
        ScrollMenu.setOpaque(true);

        FormMenu.setBackground(new java.awt.Color(255, 255, 255));
        FormMenu.setBorder(null);
        FormMenu.setName("FormMenu"); // NOI18N
        FormMenu.setPreferredSize(new java.awt.Dimension(115, 43));
        FormMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 1));

        BtnKamarInap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnKamarInap.setText("Kamar Inap");
        BtnKamarInap.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnKamarInap.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnKamarInap.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnKamarInap.setName("BtnKamarInap"); // NOI18N
        BtnKamarInap.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnKamarInap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKamarInapActionPerformed(evt);
            }
        });
        FormMenu.add(BtnKamarInap);

        BtnPermintaanLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnPermintaanLab.setText("Permintaan Lab");
        BtnPermintaanLab.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnPermintaanLab.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnPermintaanLab.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnPermintaanLab.setName("BtnPermintaanLab"); // NOI18N
        BtnPermintaanLab.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnPermintaanLab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPermintaanLabActionPerformed(evt);
            }
        });
        FormMenu.add(BtnPermintaanLab);

        BtnRiwayatPasien.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnRiwayatPasien.setText("Riwayat Perawatan");
        BtnRiwayatPasien.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnRiwayatPasien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnRiwayatPasien.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnRiwayatPasien.setName("BtnRiwayatPasien"); // NOI18N
        BtnRiwayatPasien.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnRiwayatPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRiwayatPasienActionPerformed(evt);
            }
        });
        FormMenu.add(BtnRiwayatPasien);

        BtnChecklistPreOperasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnChecklistPreOperasi.setText("Checklist Pre Operasi");
        BtnChecklistPreOperasi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnChecklistPreOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnChecklistPreOperasi.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnChecklistPreOperasi.setName("BtnChecklistPreOperasi"); // NOI18N
        BtnChecklistPreOperasi.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnChecklistPreOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnChecklistPreOperasiActionPerformed(evt);
            }
        });
        FormMenu.add(BtnChecklistPreOperasi);

        BtnSignInSebelumAnestesi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnSignInSebelumAnestesi.setText("Sign-In Sebelum Anestesi");
        BtnSignInSebelumAnestesi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnSignInSebelumAnestesi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnSignInSebelumAnestesi.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnSignInSebelumAnestesi.setName("BtnSignInSebelumAnestesi"); // NOI18N
        BtnSignInSebelumAnestesi.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnSignInSebelumAnestesi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSignInSebelumAnestesiActionPerformed(evt);
            }
        });
        FormMenu.add(BtnSignInSebelumAnestesi);

        BtnTimeOutSebelumInsisi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnTimeOutSebelumInsisi.setText("Time-Out Sebelum Insisi");
        BtnTimeOutSebelumInsisi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnTimeOutSebelumInsisi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnTimeOutSebelumInsisi.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnTimeOutSebelumInsisi.setName("BtnTimeOutSebelumInsisi"); // NOI18N
        BtnTimeOutSebelumInsisi.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnTimeOutSebelumInsisi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTimeOutSebelumInsisiActionPerformed(evt);
            }
        });
        FormMenu.add(BtnTimeOutSebelumInsisi);

        BtnSignOutSebelumMenutupLuka.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnSignOutSebelumMenutupLuka.setText("Sign-Out Sebelum Menutup Luka");
        BtnSignOutSebelumMenutupLuka.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnSignOutSebelumMenutupLuka.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnSignOutSebelumMenutupLuka.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnSignOutSebelumMenutupLuka.setName("BtnSignOutSebelumMenutupLuka"); // NOI18N
        BtnSignOutSebelumMenutupLuka.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnSignOutSebelumMenutupLuka.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSignOutSebelumMenutupLukaActionPerformed(evt);
            }
        });
        FormMenu.add(BtnSignOutSebelumMenutupLuka);

        BtnPreOperasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnPreOperasi.setText("Penilaian Pre Operasi");
        BtnPreOperasi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnPreOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnPreOperasi.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnPreOperasi.setName("BtnPreOperasi"); // NOI18N
        BtnPreOperasi.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnPreOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPreOperasiActionPerformed(evt);
            }
        });
        FormMenu.add(BtnPreOperasi);

        BtnPreAnastesi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnPreAnastesi.setText("Penilaian Pre Anestesi");
        BtnPreAnastesi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnPreAnastesi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnPreAnastesi.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnPreAnastesi.setName("BtnPreAnastesi"); // NOI18N
        BtnPreAnastesi.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnPreAnastesi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPreAnastesiActionPerformed(evt);
            }
        });
        FormMenu.add(BtnPreAnastesi);

        BtnLaporanOperasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnLaporanOperasi.setText("Laporan Operasi");
        BtnLaporanOperasi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnLaporanOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnLaporanOperasi.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnLaporanOperasi.setName("BtnLaporanOperasi"); // NOI18N
        BtnLaporanOperasi.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnLaporanOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnLaporanOperasiActionPerformed(evt);
            }
        });
        FormMenu.add(BtnLaporanOperasi);

        BtnTagihanOperasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnTagihanOperasi.setText("Tagihan Operasi/VK");
        BtnTagihanOperasi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnTagihanOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnTagihanOperasi.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnTagihanOperasi.setName("BtnTagihanOperasi"); // NOI18N
        BtnTagihanOperasi.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnTagihanOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTagihanOperasiActionPerformed(evt);
            }
        });
        FormMenu.add(BtnTagihanOperasi);

        BtnObatBhp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnObatBhp.setText("Permintaan Resep");
        BtnObatBhp.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnObatBhp.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnObatBhp.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnObatBhp.setName("BtnObatBhp"); // NOI18N
        BtnObatBhp.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnObatBhp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnObatBhpActionPerformed(evt);
            }
        });
        FormMenu.add(BtnObatBhp);

        BtnTransferAntarRuang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnTransferAntarRuang.setText("Transfer Antar Ruang");
        BtnTransferAntarRuang.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnTransferAntarRuang.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnTransferAntarRuang.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnTransferAntarRuang.setName("BtnTransferAntarRuang"); // NOI18N
        BtnTransferAntarRuang.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnTransferAntarRuang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTransferAntarRuangActionPerformed(evt);
            }
        });
        FormMenu.add(BtnTransferAntarRuang);

        BtnChecklistPostOperasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        BtnChecklistPostOperasi.setText("Check List Post Operasi");
        BtnChecklistPostOperasi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        BtnChecklistPostOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnChecklistPostOperasi.setMargin(new java.awt.Insets(1, 1, 1, 1));
        BtnChecklistPostOperasi.setName("BtnChecklistPostOperasi"); // NOI18N
        BtnChecklistPostOperasi.setPreferredSize(new java.awt.Dimension(190, 23));
        BtnChecklistPostOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnChecklistPostOperasiActionPerformed(evt);
            }
        });
        FormMenu.add(BtnChecklistPostOperasi);

        ScrollMenu.setViewportView(FormMenu);

        PanelAccor.add(ScrollMenu, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelAccor, java.awt.BorderLayout.WEST);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        Valid.pindah(evt,Status,KdDokter);
        
}//GEN-LAST:event_TNoRwKeyPressed

    private void DTPTglKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPTglKeyPressed
        Valid.pindah(evt,TCari,JamMulai);
}//GEN-LAST:event_DTPTglKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Operator");
        }else if(NmOperasi.getText().trim().equals("")){
            Valid.textKosong(KdOperasi,"Operasi");
        }else if(NmRuangOperasi.getText().trim().equals("")){
            Valid.textKosong(BtnRuangOperasi,"Ruang Operasi");
        }else if((!(JamMulai.getSelectedItem().toString()+MenitMulai.getSelectedItem().toString()+DetikMulai.getSelectedItem().toString()).equals("000000"))&&
            (Sequel.cariInteger("select count(booking_operasi.no_rawat) from booking_operasi where booking_operasi.tanggal='"+Valid.SetTgl(DTPTgl.getSelectedItem()+"")+"' "+
            "and booking_operasi.kd_ruang_ok='"+KdRuangOperasi.getText()+"' and booking_operasi.no_rawat<>'"+TNoRw.getText()+"' and "+
            "booking_operasi.jam_mulai between '"+JamMulai.getSelectedItem()+":"+MenitMulai.getSelectedItem()+":"+DetikMulai.getSelectedItem()+"' and '"+JamSelesai.getSelectedItem()+":"+MenitSelesai.getSelectedItem()+":"+DetikSelesai.getSelectedItem()+"'")>0)){
            JOptionPane.showMessageDialog(rootPane,"Jadwal bentrok dengan jam mulai operasi yang lain..!!");
            JamMulai.requestFocus();
        }else{
            if(Sequel.cariRegistrasi(TNoRw.getText())>0){
                JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi.\nSilahkan hubungi bagian kasir/keuangan ..!!");
                TCari.requestFocus();
            }else{
                if(Sequel.menyimpantf("booking_operasi","?,?,?,?,?,?,?,?,?,?","data", 10,new String[]{
                    TNoRw.getText(),KdOperasi.getText(),Valid.SetTgl(DTPTgl.getSelectedItem()+""),
                    JamMulai.getSelectedItem()+":"+MenitMulai.getSelectedItem()+":"+DetikMulai.getSelectedItem(),
                    JamSelesai.getSelectedItem()+":"+MenitSelesai.getSelectedItem()+":"+DetikSelesai.getSelectedItem(),
                    Status.getSelectedItem().toString(),KdDokter.getText(),KdRuangOperasi.getText(),NmDokterAnastesi.getText(),
                    Perawat.getText()
                    })==true){
                    tampil();
                    emptTeks();
                }
            }   
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnOperasi,BtnBatal);
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
            DTPTgl.requestFocus();
        }else if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal menghapus. Pilih dulu data yang mau dihapus.\nKlik data pada table untuk memilih...!!!!");
        }else if(!(TPasien.getText().trim().equals(""))){
            if(tbObat.getSelectedRow()!= -1){
                try{
                    Sequel.queryu("delete from booking_operasi " +
                            "where no_rawat='"+TNoRw.getText()+"' " +
                            "and tanggal='"+Valid.SetTgl(DTPTgl.getSelectedItem()+"")+"' " +
                            "and jam_mulai='"+JamMulai.getSelectedItem()+":"+MenitMulai.getSelectedItem()+":"+DetikMulai.getSelectedItem()+"' " +
                            "and jam_selesai='"+JamSelesai.getSelectedItem()+":"+MenitSelesai.getSelectedItem()+":"+DetikSelesai.getSelectedItem()+"' "+
                            "and kd_dokter='"+KdDokter.getText()+"' "+
                            "and status='"+Status.getSelectedItem()+"' "+
                            "and kode_paket='"+KdOperasi.getText()+"'");
                    tampil();
                    emptTeks();
                }catch(Exception e){
                    JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau anda hapus...\n Klik data pada table untuk memilih data...!!!!");
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
            
            Sequel.queryu("delete from temporary where temp37='"+akses.getalamatip()+"'");
            for(i=0;i<tabMode.getRowCount();i++){ 
                Sequel.menyimpan("temporary","'"+i+"','"+
                    tabMode.getValueAt(i,0).toString()+"','"+
                    tabMode.getValueAt(i,1).toString()+"','"+
                    tabMode.getValueAt(i,2).toString()+"','"+
                    tabMode.getValueAt(i,3).toString()+"','"+
                    tabMode.getValueAt(i,4).toString()+"','"+
                    tabMode.getValueAt(i,5).toString()+"','"+
                    tabMode.getValueAt(i,6).toString()+"','"+
                    tabMode.getValueAt(i,7).toString()+"','"+
                    tabMode.getValueAt(i,8).toString()+"','"+
                    tabMode.getValueAt(i,9).toString()+"','"+
                    tabMode.getValueAt(i,10).toString()+"','"+
                    tabMode.getValueAt(i,11).toString()+"','"+
                    tabMode.getValueAt(i,12).toString()+"','"+
                    tabMode.getValueAt(i,13).toString()+"','"+
                    tabMode.getValueAt(i,14).toString()+"','"+
                    tabMode.getValueAt(i,15).toString()+"','"+
                    tabMode.getValueAt(i,16).toString()+"','"+
                    tabMode.getValueAt(i,17).toString()+"','','','','','','','','','','','','','','','','','','','"+akses.getalamatip()+"'","Rekap Nota Pembayaran");
            }
             
            Valid.MyReportqry("rptJadwalOperasi.jasper","report","::[ Laporan Daftar Jadwal Operasi ]::","select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
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

private void BtnOperatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnOperatorActionPerformed
    dokter.isCek();
    dokter.TCari.requestFocus();
    dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
    dokter.setLocationRelativeTo(internalFrame1);
    dokter.setVisible(true);
}//GEN-LAST:event_BtnOperatorActionPerformed

private void BtnOperatorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnOperatorKeyPressed
    if(evt.getKeyCode()==KeyEvent.VK_SPACE){
        BtnOperatorActionPerformed(null);
    }else{
        Valid.pindah(evt,Status,BtnOperasi);
    }        
}//GEN-LAST:event_BtnOperatorKeyPressed

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
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Operator");
        }else if(NmOperasi.getText().trim().equals("")){
            Valid.textKosong(KdOperasi,"Operasi");
        }else if(NmRuangOperasi.getText().trim().equals("")){
            Valid.textKosong(BtnRuangOperasi,"Ruang Operasi");
        }else if((!(JamMulai.getSelectedItem().toString()+MenitMulai.getSelectedItem().toString()+DetikMulai.getSelectedItem().toString()).equals("000000"))&&
            (Sequel.cariInteger("select count(booking_operasi.no_rawat) from booking_operasi where booking_operasi.tanggal='"+Valid.SetTgl(DTPTgl.getSelectedItem()+"")+"' "+
            "and booking_operasi.kd_ruang_ok='"+KdRuangOperasi.getText()+"' and booking_operasi.no_rawat<>'"+TNoRw.getText()+"' and "+
            "booking_operasi.jam_mulai between '"+JamMulai.getSelectedItem()+":"+MenitMulai.getSelectedItem()+":"+DetikMulai.getSelectedItem()+"' and '"+JamSelesai.getSelectedItem()+":"+MenitSelesai.getSelectedItem()+":"+DetikSelesai.getSelectedItem()+"'")>0)){
            JOptionPane.showMessageDialog(rootPane,"Jadwal bentrok dengan jam mulai operasi yang lain..!!");
            JamMulai.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.mengedittf("booking_operasi","no_rawat=? and kode_paket=? and tanggal=? and jam_mulai=? and jam_selesai=? and status=? and kd_dokter=? and kd_ruang_ok=? and dokteranastesi=? and perawat=? ",
                        "no_rawat=?,kode_paket=?,tanggal=?,jam_mulai=?,jam_selesai=?,status=?,kd_dokter=?,kd_ruang_ok=?,dokteranastesi=?,perawat=?",20,new String[]{
                        TNoRw.getText(),KdOperasi.getText(),Valid.SetTgl(DTPTgl.getSelectedItem()+""),
                        JamMulai.getSelectedItem()+":"+MenitMulai.getSelectedItem()+":"+DetikMulai.getSelectedItem(),
                        JamSelesai.getSelectedItem()+":"+MenitSelesai.getSelectedItem()+":"+DetikSelesai.getSelectedItem(),
                        Status.getSelectedItem().toString(),KdDokter.getText(),KdRuangOperasi.getText(),NmDokterAnastesi.getText(),Perawat.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),1).toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(),11).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),5).toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(),8).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),13).toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(),16).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),18).toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(),19).toString()   
                     })==true){
                    tampil();
                    emptTeks();
                }
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
        Valid.pindah(evt,DetikSelesai,BtnOperator);
    }//GEN-LAST:event_StatusKeyPressed

    private void BtnOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnOperasiActionPerformed
        penjab=Sequel.cariIsi("select kd_pj from reg_periksa where no_rawat=?",TNoRw.getText());        
        if(posisi.equals("Ranap")){
            norawatibu=Sequel.cariIsi("select ranap_gabung.no_rawat from ranap_gabung where ranap_gabung.no_rawat2=?",TNoRw.getText());
        
            if(!norawatibu.equals("")){
                kelas=Sequel.cariIsi(
                    "select kamar.kelas from kamar inner join kamar_inap "+
                    "on kamar.kd_kamar=kamar_inap.kd_kamar where no_rawat=? "+
                    "and stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1",norawatibu);
            }else{
                kelas=Sequel.cariIsi(
                    "select kamar.kelas from kamar inner join kamar_inap "+
                    "on kamar.kd_kamar=kamar_inap.kd_kamar where no_rawat=? "+
                    "and stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1",TNoRw.getText());
            } 
        }else if(posisi.equals("Ralan")){
            kelas="Rawat Jalan";
        }
    
        operasi.setBayar(penjab, kelas);
        operasi.isCek();
        operasi.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        operasi.setLocationRelativeTo(internalFrame1);
        operasi.setVisible(true);
    }//GEN-LAST:event_BtnOperasiActionPerformed

    private void BtnOperasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnOperasiKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnOperasiActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnOperator,BtnSimpan);
        }  
    }//GEN-LAST:event_BtnOperasiKeyPressed

    private void JamMulaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamMulaiKeyPressed
        Valid.pindah(evt,DTPTgl,MenitMulai);
    }//GEN-LAST:event_JamMulaiKeyPressed

    private void MenitMulaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitMulaiKeyPressed
        Valid.pindah(evt,JamMulai,DetikMulai);
    }//GEN-LAST:event_MenitMulaiKeyPressed

    private void DetikMulaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikMulaiKeyPressed
        Valid.pindah(evt,MenitMulai,JamSelesai);
    }//GEN-LAST:event_DetikMulaiKeyPressed

    private void JamSelesaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamSelesaiKeyPressed
        Valid.pindah(evt,DetikMulai,MenitSelesai);
    }//GEN-LAST:event_JamSelesaiKeyPressed

    private void MenitSelesaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitSelesaiKeyPressed
        Valid.pindah(evt,JamSelesai,DetikSelesai);
    }//GEN-LAST:event_MenitSelesaiKeyPressed

    private void DetikSelesaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikSelesaiKeyPressed
        Valid.pindah(evt,MenitSelesai,Status);
    }//GEN-LAST:event_DetikSelesaiKeyPressed

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

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        isMenu();
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void BtnKamarInapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKamarInapActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.cariRegistrasi(TNoRw.getText())>0){
                    JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi..!!");
                }else{
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    akses.setstatus(true);
                    DlgKamarInap kamarinap=new DlgKamarInap(null,false);
                    kamarinap.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                    kamarinap.setLocationRelativeTo(internalFrame1);
                    kamarinap.emptTeks();
                    kamarinap.isCek();
                    norm=Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText());
                    kamarinap.setNoRm(TNoRw.getText(),norm,TPasien.getText());
                    kamarinap.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                }
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnKamarInapActionPerformed

    private void BtnPermintaanLabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPermintaanLabActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                DlgPermintaanLaboratorium dlgro=new DlgPermintaanLaboratorium(null,false);
                dlgro.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                dlgro.setLocationRelativeTo(internalFrame1);
                dlgro.emptTeks();
                dlgro.isCek();
                dlgro.setNoRm(TNoRw.getText(),Sequel.cariIsi("select reg_periksa.status_lanjut from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                dlgro.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnPermintaanLabActionPerformed

    private void BtnRiwayatPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRiwayatPasienActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMRiwayatPerawatan resume=new RMRiwayatPerawatan(null,true);
                resume.setNoRm(Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()),tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
                resume.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                resume.setLocationRelativeTo(internalFrame1);
                resume.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnRiwayatPasienActionPerformed

    private void BtnTagihanOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTagihanOperasiActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.cariRegistrasi(TNoRw.getText())>0){
                    JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi, data tidak boleh dihapus.\nSilahkan hubungi bagian kasir/keuangan ..!!");
                    TCari.requestFocus();
                }else{ 
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    DlgTagihanOperasi dlgro=new DlgTagihanOperasi(null,false);
                    dlgro.SetCariOperasi(KdOperasi.getText(),KdDokter.getText(),NmDokter.getText());
                    dlgro.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                    dlgro.setLocationRelativeTo(internalFrame1);
                    dlgro.setNoRm(TNoRw.getText(),TPasien.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
                    dlgro.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                }
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnTagihanOperasiActionPerformed

    private void BtnObatBhpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnObatBhpActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.cariRegistrasi(TNoRw.getText())>0){
                    JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi ..!!");
                }else{
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    if(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString().equals("Ranap")){
                        bangsal=Sequel.cariIsi(
                            "select bangsal.kd_bangsal from bangsal inner join kamar inner join kamar_inap "+
                            "on bangsal.kd_bangsal=kamar.kd_bangsal and kamar.kd_kamar=kamar_inap.kd_kamar "+
                            "where kamar_inap.no_rawat=? and kamar_inap.stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1",TNoRw.getText());
                        lokasistok=Sequel.cariIsi("select kd_depo from set_depo_ranap where kd_bangsal=?",bangsal);
                        if(lokasistok.equals("")){
                            if(Sequel.cariIsi("select asal_stok from set_lokasi").equals("Gunakan Stok Bangsal")){
                                lokasistok=bangsal;
                            }else{
                                lokasistok=Sequel.cariIsi("select kd_bangsal from set_lokasi");
                            }
                        }
                        akses.setkdbangsal(lokasistok);
                    }

                    DlgPeresepanDokter resep=new DlgPeresepanDokter(null,false);
                    resep.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
                    resep.setLocationRelativeTo(internalFrame1);
                    resep.setNoRm(TNoRw.getText(),DTPTgl.getDate(),JamSelesai.getSelectedItem().toString(),MenitSelesai.getSelectedItem().toString(),
                            DetikSelesai.getSelectedItem().toString(),KdDokter.getText(),NmDokter.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),15).toString().replaceAll("R","r"));
                    resep.isCek();
                    resep.tampilobat();
                    resep.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                }
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }   
        }
    }//GEN-LAST:event_BtnObatBhpActionPerformed

    private void BtnRuangOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRuangOperasiActionPerformed
        ruangok.isCek();
        ruangok.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        ruangok.setLocationRelativeTo(internalFrame1);
        ruangok.setVisible(true);
    }//GEN-LAST:event_BtnRuangOperasiActionPerformed

    private void BtnRuangOperasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnRuangOperasiKeyPressed
        Valid.pindah(evt,DTPTgl,Status);
    }//GEN-LAST:event_BtnRuangOperasiKeyPressed

    private void BtnPreOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPreOperasiActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMPenilaianPreOperasi form=new RMPenilaianPreOperasi(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(TNoRw.getText(),DTPCari2.getDate());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnPreOperasiActionPerformed

    private void BtnPreAnastesiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPreAnastesiActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMPenilaianPreAnastesi form=new RMPenilaianPreAnastesi(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(TNoRw.getText(),DTPCari2.getDate());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnPreAnastesiActionPerformed

    private void BtnTransferAntarRuangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTransferAntarRuangActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMTransferPasienAntarRuang form=new RMTransferPasienAntarRuang(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(TNoRw.getText(),DTPCari2.getDate());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnTransferAntarRuangActionPerformed

    private void BtnChecklistPreOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnChecklistPreOperasiActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{            
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMChecklistPreOperasi form=new RMChecklistPreOperasi(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(TNoRw.getText(),DTPCari2.getDate());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnChecklistPreOperasiActionPerformed

    private void NmDokterAnastesiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NmDokterAnastesiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NmDokterAnastesiActionPerformed

    private void BtnLaporanOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnLaporanOperasiActionPerformed
         if(tbObat.getSelectedRow()>-1){
            if(!tbObat.getValueAt(tbObat.getSelectedRow(),1).toString().equals("")){
                try {
                    rs2=koneksi.prepareStatement(
                        "select diagnosa_preop, diagnosa_postop, jaringan_dieksekusi, selesaioperasi, permintaan_pa, laporan_operasi from laporan_operasi "+
                        "where no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),2)+"' and tanggal='"+tbObat.getValueAt(tbObat.getSelectedRow(),5)+"'").executeQuery();
                    if(rs2.next()){
                        PreOp.setText(rs2.getString("diagnosa_preop"));
                        PostOp.setText(rs2.getString("diagnosa_postop"));
                        Jaringan.setText(rs2.getString("jaringan_dieksekusi"));
                        tgl2.setDate(rs2.getDate("selesaioperasi"));
                        DikirimPA.setSelectedItem(rs2.getString("permintaan_pa"));
                        Laporan.setText(rs2.getString("laporan_operasi"));
                    }
                    if(rs2!=null){
                        rs2.close();
                    }
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
                } 
                WindowLaporan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                WindowLaporan.setLocationRelativeTo(internalFrame1);
                WindowLaporan.setVisible(true);
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan pilih data, klik pada No.Rawat ..!!");
            } 
        }
    }//GEN-LAST:event_BtnLaporanOperasiActionPerformed

    private void BtnSimpan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpan1ActionPerformed
        if(!Laporan.getText().equals("")){
            Sequel.queryu("delete from laporan_operasi where no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),1)+"' and tanggal='"+tbObat.getValueAt(tbObat.getSelectedRow(),0) +"'");
//            Sequel.menyimpan("laporan_operasi","?,?,?,?,?,?,?,?","laporan operasi",8,new String[]{
//                tbObat.getValueAt(tbObat.getSelectedRow(),1).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString(),PreOp.getText(),
//                PostOp.getText(),Jaringan.getText(),Valid.SetTgl(tgl2.getSelectedItem()+"")+" "+tgl2.getSelectedItem().toString().substring(11,19),
//                DikirimPA.getSelectedItem().toString(),Laporan.getText()
             Sequel.menyimpantf2("laporan_operasi","?,?,?,?,?,?,?,?,?,?,?,?,?,?","laporan operasi",14,new String[]{
              TNoRw.getText(),Valid.SetTgl(tgl.getSelectedItem()+"")+" "+tgl.getSelectedItem().toString().substring(11,19),PreOp.getText(),
              PostOp.getText(),Jaringan.getText(),Valid.SetTgl(tgl2.getSelectedItem()+"")+" "+tgl2.getSelectedItem().toString().substring(11,19),
              DikirimPA.getSelectedItem().toString(),Laporan.getText(),Komplikasi.getText(),Pendarahan.getText(),Transfusi.getText(),Implant.getText(),Residen.getText(),Tindakan.getText()
            });
            JOptionPane.showMessageDialog(null,"Proses update selesai...!!!!");
        }
    }//GEN-LAST:event_BtnSimpan1ActionPerformed

    private void BtnSimpan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpan1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }
    }//GEN-LAST:event_BtnSimpan1KeyPressed

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrint1ActionPerformed
//       if(tbObat.getSelectedRow()>-1){
//            if(!tbObat.getValueAt(tbObat.getSelectedRow(),1).toString().equals("")){
//                Map<String, Object> param = new HashMap<>();    
//                param.put("namars",akses.getnamars());
//                param.put("alamatrs",akses.getalamatrs());
//                param.put("kotars",akses.getkabupatenrs());
//                param.put("propinsirs",akses.getpropinsirs());
//                param.put("kontakrs",akses.getkontakrs());
//                param.put("emailrs",akses.getemailrs());   
//                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
//                param.put("norawat",tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());  
//                param.put("tanggaloperasi",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());  
//                kodeoperator=Sequel.cariIsi("select operasi.operator1 from operasi where operasi.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()+"' and tgl_operasi='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'");
//                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",kodeoperator);
//                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow()+1,5).toString()+"\nID "+(finger.equals("")?kodeoperator:finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()));  
//            
//                if(Sequel.cariIsi("select reg_periksa.status_lanjut from reg_periksa where reg_periksa.no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()).equals("Ralan")){
//                    try {
//                        try {
//                            rs=koneksi.prepareStatement(
//                                    "select no_rawat, tgl_perawatan, jam_rawat, suhu_tubuh, tensi, nadi, respirasi, tinggi, berat, gcs, keluhan, pemeriksaan, alergi, "+
//                                    "rtl, penilaian from pemeriksaan_ralan where no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()+"' "+
//                                    "order by tgl_perawatan, jam_rawat desc limit 1").executeQuery();
//                            if(rs.next()){
//                                param.put("tgl_perawatan",rs.getDate("tgl_perawatan"));
//                                param.put("jam_rawat",rs.getString("jam_rawat"));
//                                param.put("alergi",rs.getString("alergi"));
//                                param.put("keluhan",rs.getString("keluhan"));
//                                param.put("pemeriksaan",rs.getString("pemeriksaan"));
//                                param.put("penilaian",rs.getString("penilaian"));
//                                param.put("rtl",rs.getString("rtl"));
//                                param.put("ruang",Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on reg_periksa.kd_poli=poliklinik.kd_poli where reg_periksa.no_rawat=?",rs.getString("no_rawat")));
//                                param.put("suhu_tubuh",rs.getString("suhu_tubuh"));
//                                param.put("tensi",rs.getString("tensi"));
//                                param.put("tinggi",rs.getString("tinggi"));
//                                param.put("berat",rs.getString("berat"));
//                                param.put("nadi",rs.getString("nadi"));
//                                param.put("respirasi",rs.getString("respirasi"));
//                                param.put("gcs",rs.getString("gcs"));
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif : "+e);
//                        } finally{
//                            if(rs!=null){
//                                rs.close();
//                            }
//                        }
//                    } catch (Exception e) {
//                        System.out.println("Notif : "+e);
//                    }
//                }else{
//                    try {
//                        try {
//                            rs=koneksi.prepareStatement(
//                                    "select no_rawat, tgl_perawatan, jam_rawat, suhu_tubuh, tensi, nadi, respirasi, tinggi, berat, gcs, keluhan, pemeriksaan, alergi, "+
//                                    "rtl, penilaian from pemeriksaan_ranap where no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()+"' "+
//                                    "order by tgl_perawatan, jam_rawat desc limit 1").executeQuery();
//                            if(rs.next()){
//                                param.put("tgl_perawatan",rs.getDate("tgl_perawatan"));
//                                param.put("jam_rawat",rs.getString("jam_rawat"));
//                                param.put("alergi",rs.getString("alergi"));
//                                param.put("keluhan",rs.getString("keluhan"));
//                                param.put("pemeriksaan",rs.getString("pemeriksaan"));
//                                param.put("penilaian",rs.getString("penilaian"));
//                                param.put("rtl",rs.getString("rtl"));
//                                param.put("ruang",Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar inner join kamar_inap on bangsal.kd_bangsal=kamar.kd_bangsal and kamar_inap.kd_kamar=kamar.kd_kamar where no_rawat=? order by tgl_masuk desc limit 1 ",rs.getString("no_rawat")));
//                                param.put("suhu_tubuh",rs.getString("suhu_tubuh"));
//                                param.put("tensi",rs.getString("tensi"));
//                                param.put("tinggi",rs.getString("tinggi"));
//                                param.put("berat",rs.getString("berat"));
//                                param.put("nadi",rs.getString("nadi"));
//                                param.put("respirasi",rs.getString("respirasi"));
//                                param.put("gcs",rs.getString("gcs"));
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif : "+e);
//                        } finally{
//                            if(rs!=null){
//                                rs.close();
//                            }
//                        }
//                    } catch (Exception e) {
//                        System.out.println("Notif : "+e);
//                    }
//                }
//                Valid.MyReport("rptLaporanOperasi.jasper","report","::[ Laporan Operasi ]::",param);
//            }else{
//                JOptionPane.showMessageDialog(rootPane,"Silahkan pilih data, klik pada No.Rawat ..!!");
//            } 
//        }
    }//GEN-LAST:event_BtnPrint1ActionPerformed

    private void BtnPrint1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrint1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnKeluar,BtnCari);
        }
    }//GEN-LAST:event_BtnPrint1KeyPressed

    private void BtnCloseIn5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseIn5ActionPerformed
        WindowLaporan.dispose();
    }//GEN-LAST:event_BtnCloseIn5ActionPerformed

    private void PreOpKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PreOpKeyPressed
        Valid.pindah(evt,tgl2,PostOp);
    }//GEN-LAST:event_PreOpKeyPressed

    private void PostOpKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PostOpKeyPressed
        Valid.pindah(evt,PreOp,Jaringan);
    }//GEN-LAST:event_PostOpKeyPressed

    private void JaringanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JaringanKeyPressed
        Valid.pindah(evt,PostOp,DikirimPA);
    }//GEN-LAST:event_JaringanKeyPressed

    private void DikirimPAKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DikirimPAKeyPressed
        Valid.pindah(evt,Jaringan,Laporan);
    }//GEN-LAST:event_DikirimPAKeyPressed

    private void tglKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tglKeyPressed
  //      Valid.pindah(evt,jenis,BtnOperator1);
    }//GEN-LAST:event_tglKeyPressed

    private void BtnSignInSebelumAnestesiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSignInSebelumAnestesiActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMSignInSebelumAnastesi form=new RMSignInSebelumAnastesi(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(TNoRw.getText(),DTPCari2.getDate(),KdDokter.getText(),NmDokter.getText(),NmOperasi.getText());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnSignInSebelumAnestesiActionPerformed

    private void BtnTimeOutSebelumInsisiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTimeOutSebelumInsisiActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMTimeOutSebelumInsisi form=new RMTimeOutSebelumInsisi(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(TNoRw.getText(),DTPCari2.getDate(),KdDokter.getText(),NmDokter.getText(),NmOperasi.getText());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnTimeOutSebelumInsisiActionPerformed

    private void BtnSignOutSebelumMenutupLukaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSignOutSebelumMenutupLukaActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMSignOutSebelumMenutupLuka form=new RMSignOutSebelumMenutupLuka(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(TNoRw.getText(),DTPCari2.getDate(),KdDokter.getText(),NmDokter.getText(),NmOperasi.getText());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnSignOutSebelumMenutupLukaActionPerformed

    private void BtnChecklistPostOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnChecklistPostOperasiActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                RMChecklistPostOperasi form=new RMChecklistPostOperasi(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(TNoRw.getText(),DTPCari2.getDate(),KdDokter.getText(),NmDokter.getText(),NmOperasi.getText());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data...!!!!");
            }
        }
    }//GEN-LAST:event_BtnChecklistPostOperasiActionPerformed

    private void KomplikasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KomplikasiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KomplikasiKeyPressed

    private void KomplikasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KomplikasiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_KomplikasiActionPerformed

    private void PendarahanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PendarahanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PendarahanActionPerformed

    private void PendarahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PendarahanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PendarahanKeyPressed

    private void TransfusiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TransfusiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TransfusiActionPerformed

    private void TransfusiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TransfusiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TransfusiKeyPressed

    private void ImplantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImplantActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ImplantActionPerformed

    private void ImplantKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ImplantKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ImplantKeyPressed

    private void ResidenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResidenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ResidenActionPerformed

    private void ResidenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ResidenKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ResidenKeyPressed

    private void TindakanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TindakanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TindakanActionPerformed

    private void TindakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TindakanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TindakanKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgBookingOperasi dialog = new DlgBookingOperasi(new javax.swing.JFrame(), true);
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
    private widget.Button BtnChecklistPostOperasi;
    private widget.Button BtnChecklistPreOperasi;
    private widget.Button BtnCloseIn5;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKamarInap;
    private widget.Button BtnKeluar;
    private widget.Button BtnLaporanOperasi;
    private widget.Button BtnObatBhp;
    private widget.Button BtnOperasi;
    private widget.Button BtnOperator;
    private widget.Button BtnPermintaanLab;
    private widget.Button BtnPreAnastesi;
    private widget.Button BtnPreOperasi;
    private widget.Button BtnPrint;
    private widget.Button BtnPrint1;
    private widget.Button BtnRiwayatPasien;
    private widget.Button BtnRuangOperasi;
    private widget.Button BtnSignInSebelumAnestesi;
    private widget.Button BtnSignOutSebelumMenutupLuka;
    private widget.Button BtnSimpan;
    private widget.Button BtnSimpan1;
    private widget.Button BtnTagihanOperasi;
    private widget.Button BtnTimeOutSebelumInsisi;
    private widget.Button BtnTransferAntarRuang;
    private widget.CekBox ChkAccor;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Tanggal DTPCari3;
    private widget.Tanggal DTPCari4;
    private widget.Tanggal DTPTgl;
    private widget.ComboBox DetikMulai;
    private widget.ComboBox DetikSelesai;
    private widget.ComboBox DikirimPA;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormMenu;
    private widget.TextBox Implant;
    private widget.ComboBox JamMulai;
    private widget.ComboBox JamSelesai;
    private widget.TextBox Jaringan;
    private widget.TextBox Kamar;
    private widget.TextBox KdDokter;
    private widget.TextBox KdOperasi;
    private widget.TextBox KdRuangOperasi;
    private widget.TextBox Komplikasi;
    private widget.Label LCount;
    private widget.TextArea Laporan;
    private widget.ComboBox MenitMulai;
    private widget.ComboBox MenitSelesai;
    private widget.TextBox NmDokter;
    private widget.TextBox NmDokterAnastesi;
    private widget.TextBox NmOperasi;
    private widget.TextBox NmRuangOperasi;
    private widget.PanelBiasa PanelAccor;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox Pendarahan;
    private widget.TextBox Perawat;
    private widget.TextBox PostOp;
    private widget.TextBox PreOp;
    private widget.RadioButton R1;
    private widget.RadioButton R2;
    private widget.RadioButton R3;
    private widget.RadioButton R4;
    private widget.RadioButton R5;
    private widget.TextBox Residen;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll3;
    private widget.ScrollPane ScrollMenu;
    private widget.ComboBox Status;
    private widget.TextBox TCari;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TextBox Tindakan;
    private widget.TextBox Transfusi;
    private javax.swing.JDialog WindowLaporan;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame6;
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
    private widget.Label jLabel35;
    private widget.Label jLabel36;
    private widget.Label jLabel37;
    private widget.Label jLabel4;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private widget.Label label11;
    private widget.Label label12;
    private widget.panelisi panelCari;
    private widget.panelisi panelGlass10;
    private widget.panelisi panelGlass6;
    private widget.panelisi panelGlass7;
    private widget.panelisi panelGlass8;
    private widget.Table tbObat;
    private widget.Tanggal tgl;
    private widget.Tanggal tgl2;
    // End of variables declaration//GEN-END:variables

    public void tampil() {     
        if(R1.isSelected()==true){
            status=" booking_operasi.status='Menunggu' ";
        }else if(R4.isSelected()==true){
            status=" booking_operasi.status='Proses Operasi' ";
        }else if(R5.isSelected()==true){
            status=" booking_operasi.status='Permintaan' ";
        }else if(R2.isSelected()==true){
            status=" booking_operasi.tanggal between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' ";
        }else if(R3.isSelected()==true){
            status=" booking_operasi.status='Selesai' and booking_operasi.tanggal between '"+Valid.SetTgl(DTPCari3.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari4.getSelectedItem()+"")+"' ";           
        }
        Valid.tabelKosong(tabMode);
        try {
            ps=koneksi.prepareStatement(
                    "select booking_operasi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,booking_operasi.tanggal,"+
                    "booking_operasi.jam_mulai,booking_operasi.jam_selesai,booking_operasi.status,booking_operasi.kd_dokter,booking_operasi.dokteranastesi,"+
                    "booking_operasi.perawat,dokter.nm_dokter,booking_operasi.kode_paket,paket_operasi.nm_perawatan,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,"+
                    "pasien.jk,poliklinik.nm_poli,booking_operasi.kd_ruang_ok,ruang_ok.nm_ruang_ok "+
                    "from booking_operasi inner join reg_periksa on booking_operasi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join paket_operasi on booking_operasi.kode_paket=paket_operasi.kode_paket "+
                    "inner join dokter on booking_operasi.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join ruang_ok on booking_operasi.kd_ruang_ok=ruang_ok.kd_ruang_ok "+
                    "where "+status+(TCari.getText().trim().equals("")?"":" and (booking_operasi.no_rawat like ? or reg_periksa.no_rkm_medis like ? "+
                    "or pasien.nm_pasien like ? or booking_operasi.status like ? or dokter.nm_dokter like ? or paket_operasi.nm_perawatan like ? "+
                    "or ruang_ok.nm_ruang_ok like ?) ")+"order by booking_operasi.tanggal,booking_operasi.jam_mulai");
            try {
                if(!TCari.getText().trim().equals("")){
                    ps.setString(1,"%"+TCari.getText().trim()+"%");
                    ps.setString(2,"%"+TCari.getText().trim()+"%");
                    ps.setString(3,"%"+TCari.getText().trim()+"%");
                    ps.setString(4,"%"+TCari.getText().trim()+"%");
                    ps.setString(5,"%"+TCari.getText().trim()+"%");
                    ps.setString(6,"%"+TCari.getText().trim()+"%");
                    ps.setString(7,"%"+TCari.getText().trim()+"%");
                }
                    
                rs=ps.executeQuery();
                i=1;
                while(rs.next()){
                    order="Ranap";
                    kamar=Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar inner join kamar_inap on bangsal.kd_bangsal=kamar.kd_bangsal "+
                            " and kamar_inap.kd_kamar=kamar.kd_kamar where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1 ",rs.getString("no_rawat"));  
                    if(kamar.equals("")){
                        kamar=rs.getString("nm_poli");
                        order="Ralan";
                    }
                    
                    diagnosa=Sequel.cariIsi("select concat(diagnosa_pasien.kd_penyakit,' ',penyakit.nm_penyakit) from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit where diagnosa_pasien.no_rawat=? limit 1",rs.getString("no_rawat"));
                    
                    tabMode.addRow(new Object[]{
                        i+".",rs.getString("no_rawat"),rs.getString("nm_pasien"),rs.getString("umur"),
                        rs.getString("jk"),rs.getString("tanggal"),rs.getString("jam_mulai"),rs.getString("jam_selesai"),
                        rs.getString("status"),kamar,diagnosa,rs.getString("kode_paket"),rs.getString("nm_perawatan"),
                        rs.getString("kd_dokter"),rs.getString("nm_dokter"),order,rs.getString("kd_ruang_ok"),
                        rs.getString("nm_ruang_ok"),rs.getString("dokteranastesi"),rs.getString("perawat")
                    });
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
        LCount.setText(""+tabMode.getRowCount());
    }


    public void emptTeks() {
        KdDokter.setText("");
        NmDokter.setText("");
        KdOperasi.setText("");
        NmOperasi.setText("");
        KdRuangOperasi.setText("");
        NmRuangOperasi.setText("");
        JamMulai.setSelectedItem("00");
        MenitMulai.setSelectedItem("00");
        DetikMulai.setSelectedItem("00");
        JamSelesai.setSelectedItem("00");
        MenitSelesai.setSelectedItem("00");
        DetikSelesai.setSelectedItem("00");
        DTPTgl.setDate(new Date());
        DTPTgl.requestFocus();
        NmDokterAnastesi.setText("");
        Perawat.setText("");
    }

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()); 
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            JamMulai.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(0,2));
            MenitMulai.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(3,5));
            DetikMulai.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(6,8));
            JamSelesai.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().substring(0,2));
            DetikSelesai.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().substring(3,5));
            DetikSelesai.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().substring(6,8));
            Status.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            Kamar.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            KdOperasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            NmOperasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            KdDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            NmDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            KdRuangOperasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            NmRuangOperasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            Valid.SetTgl(DTPTgl,tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            NmDokterAnastesi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
            Perawat.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
        }
    }
    
    public void setNoRm(String norwt,String norm,String nama,String lokasi,String posisi) {
        TNoRw.setText(norwt);
        TPasien.setText(norm+" "+nama);
        Kamar.setText(lokasi);
        this.posisi=posisi;
        TCari.setText(norwt);
        ChkInput.setSelected(true);
        isForm();
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,150));
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
        BtnSimpan.setEnabled(akses.getbooking_operasi());
        BtnHapus.setEnabled(akses.getbooking_operasi());
        BtnPrint.setEnabled(akses.getbooking_operasi());
        BtnKamarInap.setVisible(akses.getkamar_inap());
        BtnRiwayatPasien.setVisible(akses.getresume_pasien());
        BtnTagihanOperasi.setVisible(akses.getoperasi());
        BtnEdit.setEnabled(akses.getoperasi()); 
        Status.setEnabled(akses.getoperasi());
        BtnObatBhp.setVisible(akses.getresep_dokter());
        BtnPermintaanLab.setVisible(akses.getpermintaan_lab());
        BtnPreAnastesi.setVisible(akses.getpenilaian_pre_anestesi());
        BtnPreOperasi.setVisible(akses.getpenilaian_pre_operasi());
        BtnTransferAntarRuang.setVisible(akses.gettransfer_pasien_antar_ruang());
    }

    private void isMenu(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(250,HEIGHT));
            FormMenu.setVisible(true); 
            ChkAccor.setVisible(true);
        }else if(ChkAccor.isSelected()==false){  
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
            FormMenu.setVisible(false);    
            ChkAccor.setVisible(true);
        }
    }
}
