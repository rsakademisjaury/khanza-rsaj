/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgLhtBiaya.java
 *
 * Created on 12 Jul 10, 16:21:34
 */

package keuangan;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import simrskhanza.DlgCariBangsal;
import simrskhanza.DlgCariCaraBayar;

/**
 *
 * @author perpustakaan
 */
public final class DlgPembayaranRanap extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2,ps3;
    private ResultSet rs,rs2,rs3;
    private DlgCariBangsal bangsal=new DlgCariBangsal(null,false);
    private DlgCariCaraBayar penjab=new DlgCariCaraBayar(null,false);
    private double all=0,Laborat=0,Radiologi=0,Diagnostik=0,Operasi=0,Obat=0,Ranap_Dokter=0,Ranap_Paramedis=0,Ranap_Dokter_Paramedis=0,Ralan_Dokter=0,
             Ralan_Paramedis=0,Ralan_Dokter_Paramedis=0,Tambahan=0,Potongan=0,Kamar=0,Registrasi=0,Harian=0,Retur_Obat=0,Resep_Pulang=0,Anastesi=0,KamarBedah=0,TimOK=0,Oksigen=0,Urs=0,Mikroskop=0,Telescope=0,MonitorOk=0,Deposit=0,Dialisis=0,Tsaraf=0,RehabMedik=0,Eswl=0,Ambulans=0,USGK=0,Spirometri=0,Visite=0,Kemo=0,Dr_persalinan=0,Prwt_persalinan=0,Kmr_persalinan=0,Echo_Doopler=0,
             Service=0,UgdBedah=0,UgdNonBedah=0,Monitor=0,ttlLaborat=0,ttlRadiologi=0,ttlDiagnostik=0,ttlOperasi=0,ttlAnastesi=0,ttlKamarBedah=0,ttlTimOK=0,ttlObat=0,ttlRanap_Dokter=0,ttlRanap_Paramedis=0,ttlRalan_Dokter=0,ttlVisite=0,
             ttlRalan_Paramedis=0,ttlTambahan=0,ttlPotongan=0,ttlKamar=0,ttlRegistrasi=0,ttlHarian=0,ttlRetur_Obat=0,ttlResep_Pulang=0,ttlService=0,ttlUgdBedah=0,ttlUgdNonBedah=0,ttlMonitor=0,ttlOksigen=0,ttlUrs=0,ttlMikroskop=0,ttlTelescope=0,ttlMonitorOk=0,ttlDeposit=0,ttlDialisis=0,ttlTsaraf=0,ttlRehabMedik=0,ttlEswl=0,ttlAmbulans=0,ttlUSGK=0,ttlSpirometri=0,ttlKemo=0,ttlDr_persalinan=0,ttlPrwt_persalinan=0,ttlKmr_persalinan=0,ttlEcho_Doopler=0;
    private String sqlps2 = "select sum(totalbiaya) from billing "
        + "where nm_perawatan <> 'Konsultasi dokter(BPJS)' "
        + "and lower(nm_perawatan) not like '%partus%' "
        + "and lower(nm_perawatan) not like '%spirometri%' "
        + "and lower(nm_perawatan) not like '%kemo%' "
        + "and nm_perawatan <> 'Konsultasi Malam' "
        + "and lower(nm_perawatan) not like '%intra%mus%' "
        + "and lower(nm_perawatan) not like '%zyga%' "
        + "and lower(nm_perawatan) not like '%bursitis%' "
        + "and lower(nm_perawatan) not like '%vis%' "
        + "and lower(nm_perawatan) not like '%pem%oksigen%' "
        + "and lower(nm_perawatan) not like '%biaya%onloop%' "
        + "and lower(nm_perawatan) not like '%biaya%sewa%ok%' "
        + "and lower(nm_perawatan) not like '%biaya%anastesi%' "
        + "and lower(nm_perawatan) not like '%ipm%' "
        + "and lower(nm_perawatan) not like '%biaya%operator1%' "
        + "and lower(nm_perawatan) not like '%dialisa%' "
        + "and lower(status) not like '%ralan%paramedis%' "
        + "and nm_perawatan not in ("
        + "'INJEKSI INTRA-ARTICULAR',"
        + "'ASSESSMENT & PRESKRIPSI ORTOTIK PROSTETIK',"
        + "'6 MINUTE WALKING TEST',"
        + "'ANALISA GAIT',"
        + "'LOW LEVEL LASER THERAPY/SITE',"
        + "'BIOFEEDBACK',"
        + "'KINESIOTAPING',"
        + "'EXERCISE RINGAN',"
        + "'EXERCISE  MODERAT',"
        + "'EXERCISE BERAT',"
        + "'SWD MODALITAS',"
        + "'USD MODALITAS',"
        + "'IR MODALITAS',"
        + "'ELECTRICAL STIMULATION MODALITAS',"
        + "'MODALITAS + EXERCISE',"
        + "'2 MODALITAS + 1 EXERCISE',"
        + "'3 EXERCISE'"
        + ") "
        + "and lower(nm_perawatan) not like '%echo%' "
        + "and lower(nm_perawatan) not like '%doppler%' "
        + "and no_rawat=? and status=?",
        sqlps3 = "select min(tgl_byr) from billing where no_rawat=?",
        pilihan = "", tampilkan = "Sudah Bayar", Keterangan = "Belum Bayar",
        tgl_byr_billing = "";
    private StringBuilder htmlContent;
    private int i=0;
    
    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgPembayaranRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        Object[] rowRwJlDr={
            "Tgl.Pulang",
            "No.Nota",
            "No.RM",
            "Nama Pasien",
            "Tgl Lunas",
            "Kamar/Bangsal",
            "Administrasi",
            "Konsul",
            "Visite",
            "Tindakan",
            "Obt+Emb+Tsl",
            "Retur Obat",
            "Resep Pulang",
            "Laborat",
            "Radiologi",
            "Diagnostik",
            "Potongan",
            "Tambahan",
            "Kamar+Service",
            "Operasi",
            "Anastesi",
            "Kamar Bedah",
            "Tim OK",
            "Harian",
            "UGD Bedah",
            "UGD NonBedah",
            "Monitor",
            "Oksigen",
            "URS",
            "Mikroskop",
            "Telescope",
            "Monitor OK",
            "Dialisis",
            "T-saraf",
            "Rehab Medik",
            "Alat ESWL",
            "Ambulans",
            "USG Obgyn",
            "Spirometri",
            "Kemoterapi",
            "dr. Persalinan",
            "Tim Persalinan",
            "Kamar Persalinan",
            "Echo / Doopler",            
            "Total",
            "Deposit",
            "Sisa"
        };
        tabMode=new DefaultTableModel(null,rowRwJlDr){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal.setModel(tabMode);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 46; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(75);
            }else if(i==1){
                column.setPreferredWidth(110);
            }else if(i==2){
                column.setPreferredWidth(60);
            }else if(i==3){
                column.setPreferredWidth(170);
            }else if(i==4){
                column.setPreferredWidth(150);
            }else if(i==5){
                column.setPreferredWidth(150);
            }else if(i==6){
                column.setPreferredWidth(100);
            }else if(i==19){
                column.setPreferredWidth(110);
            }else{
                column.setPreferredWidth(75);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());
        
        // default untuk non-numerik (kiri + padding)
        class WarnaPadLeft extends WarnaTable {
            private final Border pad = new EmptyBorder(0,0,0,0);
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(new CompoundBorder(getBorder(), pad));
                return c;
            }
        }

        // numerik: kanan + padding 6px
        class WarnaPadRight extends WarnaTable {
            private final Border pad = new EmptyBorder(0,0,0,0);
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(new CompoundBorder(getBorder(), pad));
                // opsional: merah jika negatif
                if (!isSelected && value != null) {
                    try {
                        double d = Double.parseDouble(value.toString().replace(",", ""));
                        setForeground(d < 0 ? java.awt.Color.RED : new java.awt.Color(0x22,0x22,0x22));
                    } catch(Exception ignore){}
                }
                return c;
            }
        }

        // tengah + padding (untuk kolom tertentu)
        class WarnaPadCenter extends WarnaTable {
            private final Border pad = new EmptyBorder(0,0,0,0);
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new CompoundBorder(getBorder(), pad));
                return c;
            }
        }

        // pasang renderer:
        // 1) default untuk semua kolom = kiri + padding
        tbBangsal.setDefaultRenderer(Object.class, new WarnaPadLeft());

        // 2) kolom numerik 7..32 = kanan + padding
        WarnaPadRight padRight = new WarnaPadRight();
        for (int col = 6; col <= 46; col++) {
            tbBangsal.getColumnModel().getColumn(col).setCellRenderer(padRight);
        }

        // 3) kolom center: No.(0), Tgl. Ralan(1), No. Nota(2), No. RM(3), Status Bayar(35)
        WarnaPadCenter padCenter = new WarnaPadCenter();
        int[] centerCols = {0,1,2};
        for (int col : centerCols) {
            tbBangsal.getColumnModel().getColumn(col).setCellRenderer(padCenter);
        }

        // (opsional) baris sedikit lebih tinggi biar lega
        tbBangsal.setRowHeight(30);

        TKd.setDocument(new batasInput((byte)20).getKata(TKd));
        
        bangsal.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(bangsal.getTable().getSelectedRow()!= -1){
                    kdbangsal.setText(bangsal.getTable().getValueAt(bangsal.getTable().getSelectedRow(),0).toString());
                    nmbangsal.setText(bangsal.getTable().getValueAt(bangsal.getTable().getSelectedRow(),1).toString());
                }      
                kdbangsal.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {bangsal.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        
        bangsal.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    bangsal.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        penjab.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(penjab.getTable().getSelectedRow()!= -1){
                    KdCaraBayar.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),1).toString());
                    NmCaraBayar.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),2).toString());
                    BtnCaraBayar.requestFocus();
                }      
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
        
        ChkInput.setSelected(true);
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
        MnBilling = new javax.swing.JMenuItem();
        MnSudahBayar = new javax.swing.JMenuItem();
        MnBelumBayar = new javax.swing.JMenuItem();
        MnSemuaStatusBayar = new javax.swing.JMenuItem();
        kdbangsal = new widget.TextBox();
        KdCaraBayar = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        BtnCari1 = new widget.Button();
        BtnAll = new widget.Button();
        label10 = new widget.Label();
        LCount2 = new widget.Label();
        jLabel10 = new javax.swing.JLabel();
        LCount = new javax.swing.JLabel();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.panelisi();
        label20 = new widget.Label();
        NmCaraBayar = new widget.TextBox();
        BtnCaraBayar = new widget.Button();
        label17 = new widget.Label();
        nmbangsal = new widget.TextBox();
        BtnSeek2 = new widget.Button();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnBilling.setBackground(new java.awt.Color(255, 255, 254));
        MnBilling.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnBilling.setForeground(new java.awt.Color(50, 50, 50));
        MnBilling.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnBilling.setText("Billing/Pembayaran Pasien");
        MnBilling.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnBilling.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnBilling.setName("MnBilling"); // NOI18N
        MnBilling.setPreferredSize(new java.awt.Dimension(210, 28));
        MnBilling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnBillingActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnBilling);

        MnSudahBayar.setBackground(new java.awt.Color(255, 255, 254));
        MnSudahBayar.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnSudahBayar.setForeground(new java.awt.Color(50, 50, 50));
        MnSudahBayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnSudahBayar.setText("Tampilkan Sudah Bayar");
        MnSudahBayar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnSudahBayar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnSudahBayar.setName("MnSudahBayar"); // NOI18N
        MnSudahBayar.setPreferredSize(new java.awt.Dimension(210, 28));
        MnSudahBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnSudahBayarActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnSudahBayar);

        MnBelumBayar.setBackground(new java.awt.Color(255, 255, 254));
        MnBelumBayar.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnBelumBayar.setForeground(new java.awt.Color(50, 50, 50));
        MnBelumBayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnBelumBayar.setText("Tampilkan Belum Bayar");
        MnBelumBayar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnBelumBayar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnBelumBayar.setName("MnBelumBayar"); // NOI18N
        MnBelumBayar.setPreferredSize(new java.awt.Dimension(210, 28));
        MnBelumBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnBelumBayarActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnBelumBayar);

        MnSemuaStatusBayar.setBackground(new java.awt.Color(255, 255, 254));
        MnSemuaStatusBayar.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnSemuaStatusBayar.setForeground(new java.awt.Color(50, 50, 50));
        MnSemuaStatusBayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnSemuaStatusBayar.setText("Tampilkan Semua Status Bayar");
        MnSemuaStatusBayar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnSemuaStatusBayar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnSemuaStatusBayar.setName("MnSemuaStatusBayar"); // NOI18N
        MnSemuaStatusBayar.setPreferredSize(new java.awt.Dimension(210, 28));
        MnSemuaStatusBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnSemuaStatusBayarActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnSemuaStatusBayar);

        kdbangsal.setName("kdbangsal"); // NOI18N
        kdbangsal.setPreferredSize(new java.awt.Dimension(70, 23));
        kdbangsal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdbangsalKeyPressed(evt);
            }
        });

        KdCaraBayar.setEditable(false);
        KdCaraBayar.setName("KdCaraBayar"); // NOI18N
        KdCaraBayar.setPreferredSize(new java.awt.Dimension(50, 23));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Pembayaran Pasien Ranap Per Tanggal Bayar/Tanggal Pulang ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbBangsal.setComponentPopupMenu(jPopupMenu1);
        tbBangsal.setName("tbBangsal"); // NOI18N
        tbBangsal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbBangsalMouseClicked(evt);
            }
        });
        tbBangsal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbBangsalKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbBangsal);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass5.add(label11);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(27, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl2);

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
        panelGlass5.add(BtnCari1);

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

        label10.setText("Record :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass5.add(label10);

        LCount2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount2.setText("0");
        LCount2.setName("LCount2"); // NOI18N
        LCount2.setPreferredSize(new java.awt.Dimension(40, 23));
        panelGlass5.add(LCount2);

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(50, 50, 50));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Total");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(40, 23));
        panelGlass5.add(jLabel10);

        LCount.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        LCount.setForeground(new java.awt.Color(50, 50, 50));
        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(120, 23));
        panelGlass5.add(LCount);

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
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 66));
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
        FormInput.setPreferredSize(new java.awt.Dimension(100, 74));
        FormInput.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label20.setText("Cara Bayar :");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(75, 23));
        FormInput.add(label20);

        NmCaraBayar.setEditable(false);
        NmCaraBayar.setName("NmCaraBayar"); // NOI18N
        NmCaraBayar.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(NmCaraBayar);

        BtnCaraBayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnCaraBayar.setMnemonic('3');
        BtnCaraBayar.setToolTipText("Alt+3");
        BtnCaraBayar.setName("BtnCaraBayar"); // NOI18N
        BtnCaraBayar.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCaraBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCaraBayarActionPerformed(evt);
            }
        });
        FormInput.add(BtnCaraBayar);

        label17.setText("Kamar/Bangsal :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(170, 23));
        FormInput.add(label17);

        nmbangsal.setEditable(false);
        nmbangsal.setName("nmbangsal"); // NOI18N
        nmbangsal.setPreferredSize(new java.awt.Dimension(280, 23));
        FormInput.add(nmbangsal);

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

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        internalFrame1.getAccessibleContext().setAccessibleName("::[ Data Pembayaran Pasien Ranap Per Tanggal Bayar ]::");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnPrint.requestFocus();
        }else if(tabMode.getRowCount()!=0){
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
                bg.close();

                File f;            
                BufferedWriter bw; 

                pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih laporan..!","Pilihan Cetak",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Laporan 1 (HTML)","Laporan 2 (WPS)","Laporan 3 (CSV)","Laporan 4 (Jasper)"},"Laporan 1 (HTML)");
                switch (pilihan) {
                    case "Laporan 1 (HTML)":
                            htmlContent = new StringBuilder();
                            htmlContent.append(                             
                                "<tr class='isi'>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Tgl.Pulang</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='6%'>No.Nota</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='3%'>No.RM</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='15%'>Nama Pasien</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='9%'>Tgl. Lunas</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='9%'>Kamar/Bangsal</td>"+                                    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Administrasi</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Konsul</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Visite</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Tindakan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Obt+Emb+Tsl</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Retur Obat</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Resep Pulang</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Laborat</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Radiologi</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Diagnostik</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Potongan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Tambahan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Kamar+Service</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Operasi</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Anastesi</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Kamar Bedah</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Tim OK</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Harian</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>UGD Bedah</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>UGD Non Bedah</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Monitor</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Oksigen</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>URS</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Mikroskop</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Telescope</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Monitor OK</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Dialisis</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>T-Saraf</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Rehab Medik</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Alat ESWL</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Ambulans</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>USG Obgyn</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Spirometri</td>"+  
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Kemoterapi</td>"+ 
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>dr. Persalinan</td>"+ 
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Tim Persalinan</td>"+ 
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Kamar Persalinan</td>"+     
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Echo Doopler</td>"+         
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='9%'>Total</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='9%'>Deposit</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='10%'>Sisa</td>"+
                                "</tr>"
                            ); 
                            for(i=0;i<tabMode.getRowCount();i++){  
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,0)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,1)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,2)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,3)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,4)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,5)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,6)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,7)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,8)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,9)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,10)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,11)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,12)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,13)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,14)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,15)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,16)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,17)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,18)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,19)+"</td>"+                                        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,20)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,21)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,22)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,23)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,24)+"</td>"+       
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,25)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,26)+"</td>"+                                                        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,27)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,28)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,29)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,30)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,31)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,32)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,33)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,34)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,35)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,36)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,37)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,38)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,39)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,40)+"</td>"+                
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,41)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,42)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,43)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,44)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,45)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,46)+"</td>"+        
                                    "</tr>"
                                ); 
                            }            

                            f = new File("PembayaranRanap.html");            
                            bw = new BufferedWriter(new FileWriter(f));            
                            bw.write("<html>"+
                                        "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                        "<body>"+
                                            "<table width='1900px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                "<tr class='isi2'>"+
                                                    "<td valign='top' align='center'>"+
                                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                                        "<font size='2' face='Tahoma'>REKAP PEMBAYARAN RAWAT RANAP PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+        
                                                    "</td>"+
                                               "</tr>"+
                                            "</table>"+
                                            "<table width='1900px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
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
                            htmlContent.append(                             
                                "<tr class='isi'>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Tgl.Pulang</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='6%'>No.Nota</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>No.RM</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='12%'>Nama Pasien</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='12%'>Tgl. Lunas</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='8%'>Kamar/Bangsal</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='8%'>Deposit</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Administrasi</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Tindakan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Obt+Emb+Tsl</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Retur Obat</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Resep Pulang</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Laborat</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Radiologi</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Diagnostik</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Potongan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Tambahan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Kamar+Service</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Operasi</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Anastesi</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Kamar Bedah</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Tim OK</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Harian</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>UGD Bedah</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>UGD Non Bedah</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Monitor</td>"+  
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Oksigen</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>URS</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Mikroskop</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Telescope</td>"+    
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='4%'>Monitor OK</td>"+
                                    "<td valign='middle' bgcolor='#FFFAF8' align='center' width='6%'>Total</td>"+
                                "</tr>"
                            ); 
                            for(i=0;i<tabMode.getRowCount();i++){  
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,0)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,1)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,2)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,3)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,4)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,5)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,6)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,7)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,8)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,9)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,10)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,11)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,12)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,13)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,14)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,15)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,16)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,17)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,18)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,19)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,20)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,21)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,22)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,23)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,24)+"</td>"+       
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,25)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,26)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,27)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,28)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,29)+"</td>"+
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,30)+"</td>"+        
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,31)+"</td>"+              
                                        "<td valign='top' align='right'>"+tabMode.getValueAt(i,32)+"</td>"+        
                                    "</tr>"
                                ); 
                            }            

                            f = new File("PembayaranRanap.wps");            
                            bw = new BufferedWriter(new FileWriter(f));            
                            bw.write("<html>"+
                                        "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                        "<body>"+
                                            "<table width='1900px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                "<tr class='isi2'>"+
                                                    "<td valign='top' align='center'>"+
                                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                                        "<font size='2' face='Tahoma'>DETAIL JM DOKTER PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+        
                                                    "</td>"+
                                               "</tr>"+
                                            "</table>"+
                                            "<table width='1900px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
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
                            htmlContent.append(                             
                                "\"Tgl.Pulang\";\"No.Nota\";\"No.RM\";\"Nama Pasien\";\"Kamar/Bangsal\";\"Administrasi\";\"Tindakan\";\"Obt+Emb+Tsl\";\"Retur Obat\";\"Resep Pulang\";\"Laborat\";\"Radiologi\";\"Diagnostik\";\"Potongan\";\"Tambahan\";\"Kamar+Service\";\"Operasi\";\"Harian\";\"IGD Bedah\";\"IGD Non Bedah\";\"Total\"\n"
                            ); 
                            for(i=0;i<tabMode.getRowCount();i++){  
                                htmlContent.append(                             
                                    "\""+tabMode.getValueAt(i,0)+"\";\""+tabMode.getValueAt(i,1)+"\";\""+tabMode.getValueAt(i,2)+"\";\""+tabMode.getValueAt(i,3)+"\";\""+tabMode.getValueAt(i,4)+"\";\""+tabMode.getValueAt(i,5)+"\";\""+tabMode.getValueAt(i,6)+"\";\""+tabMode.getValueAt(i,7)+"\";\""+tabMode.getValueAt(i,8)+"\";\""+tabMode.getValueAt(i,9)+"\";\""+tabMode.getValueAt(i,10)+"\";\""+tabMode.getValueAt(i,11)+"\";\""+tabMode.getValueAt(i,12)+"\";\""+tabMode.getValueAt(i,13)+"\";\""+tabMode.getValueAt(i,14)+"\";\""+tabMode.getValueAt(i,15)+"\";\""+tabMode.getValueAt(i,16)+"\";\""+tabMode.getValueAt(i,17)+"\";\""+tabMode.getValueAt(i,18)+"\"\n"
                                ); 
                            }            

                            f = new File("PembayaranRanap.csv");            
                            bw = new BufferedWriter(new FileWriter(f));            
                            bw.write(htmlContent.toString());

                            bw.close();                         
                            Desktop.getDesktop().browse(f.toURI());
                        break; 
                    case "Laporan 4 (Jasper)":
                            Sequel.queryu("delete from temporary where temp37='"+akses.getalamatip()+"'");
                            for(int r=0;r<tabMode.getRowCount();r++){  
                                    Sequel.menyimpan("temporary","'"+r+"','"+
                                                    tabMode.getValueAt(r,0).toString().replaceAll("'","`") +"','"+
                                                    tabMode.getValueAt(r,1).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,2).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,3).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,4).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,5).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,6).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,7).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,8).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,9).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,10).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,11).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,12).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,13).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,14).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,15).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,16).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,17).toString().replaceAll("'","`")+"','"+
                                                    tabMode.getValueAt(r,18).toString().replaceAll("'","`")+"','','','','','','','','','','','','','','','','','','','','','"+akses.getalamatip()+"'","Rekap Nota Pembayaran");
                            }

                            Map<String, Object> param = new HashMap<>();                 
                            param.put("namars",akses.getnamars());
                            param.put("alamatrs",akses.getalamatrs());
                            param.put("kotars",akses.getkabupatenrs());
                            param.put("propinsirs",akses.getpropinsirs());
                            param.put("kontakrs",akses.getkontakrs());
                            param.put("emailrs",akses.getemailrs());   
                            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                            Valid.MyReportqry("rptRTagihanRanap.jasper","report","::[ Rekap Tagihan Ranap Masuk ]::","select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
                        break; 
                }                 
            } catch (Exception e) {
            }     
            this.setCursor(Cursor.getDefaultCursor());
        }
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

    private void tbBangsalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbBangsalMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbBangsalMouseClicked

    private void tbBangsalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbBangsalKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbBangsalKeyPressed

private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
    tampil();
}//GEN-LAST:event_BtnCari1ActionPerformed

private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            tampil();
            this.setCursor(Cursor.getDefaultCursor());
        }else{
            Valid.pindah(evt, TKd, BtnPrint);
        }
}//GEN-LAST:event_BtnCari1KeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void kdbangsalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdbangsalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmbangsal,kdbangsal.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmbangsal,kdbangsal.getText());
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmbangsal,kdbangsal.getText());
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdbangsalKeyPressed

    private void BtnSeek2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek2ActionPerformed
        bangsal.isCek();
        bangsal.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        bangsal.setLocationRelativeTo(internalFrame1);
        bangsal.setAlwaysOnTop(false);
        bangsal.setVisible(true);
    }//GEN-LAST:event_BtnSeek2ActionPerformed

    private void BtnSeek2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek2KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek2KeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        kdbangsal.setText("");
        nmbangsal.setText("");
        KdCaraBayar.setText("");
        NmCaraBayar.setText("");
        tampilkan="Semua";
        tampil();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, kdbangsal, BtnPrint);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void MnBillingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnBillingActionPerformed
        if(TKd.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu dengan menklik data pada table...!!!");
        }else{
            DlgBilingRanap billing=new DlgBilingRanap(null,false);
            billing.TNoRw.setText(Sequel.cariIsi("select no_rawat from nota_inap where no_nota=?",TKd.getText()));            
            billing.isCek();
            billing.isRawat();
            billing.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            billing.setLocationRelativeTo(internalFrame1);
            billing.setVisible(true); 
        }
    }//GEN-LAST:event_MnBillingActionPerformed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void BtnCaraBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCaraBayarActionPerformed
        penjab.isCek();
        penjab.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penjab.setLocationRelativeTo(internalFrame1);
        penjab.setAlwaysOnTop(false);
        penjab.setVisible(true);
    }//GEN-LAST:event_BtnCaraBayarActionPerformed

    private void MnSudahBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnSudahBayarActionPerformed
        tampilkan="Sudah Bayar";
        tampil();
    }//GEN-LAST:event_MnSudahBayarActionPerformed

    private void MnBelumBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnBelumBayarActionPerformed
        tampilkan="Belum Bayar";
        tampil();
    }//GEN-LAST:event_MnBelumBayarActionPerformed

    private void MnSemuaStatusBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnSemuaStatusBayarActionPerformed
        tampilkan="Semua";
        tampil();
    }//GEN-LAST:event_MnSemuaStatusBayarActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgPembayaranRanap dialog = new DlgPembayaranRanap(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCaraBayar;
    private widget.Button BtnCari1;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSeek2;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private widget.TextBox KdCaraBayar;
    private javax.swing.JLabel LCount;
    private widget.Label LCount2;
    private javax.swing.JMenuItem MnBelumBayar;
    private javax.swing.JMenuItem MnBilling;
    private javax.swing.JMenuItem MnSemuaStatusBayar;
    private javax.swing.JMenuItem MnSudahBayar;
    private widget.TextBox NmCaraBayar;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox TKd;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox kdbangsal;
    private widget.Label label10;
    private widget.Label label11;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label20;
    private widget.TextBox nmbangsal;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    // End of variables declaration//GEN-END:variables

//    public void tampil(){
//        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
//        Valid.tabelKosong(tabMode);
//        try{
//            if(tampilkan.equals("Sudah Bayar")){
//                ps= koneksi.prepareStatement(
//                    "select kamar_inap.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
//                    "(case when piutang_pasien.status is not null then \"Piutang\" else \"Non Piutang\" end) as cara_bayar," +
//                    "(case when bp.tgl_byr is not null then bp.tgl_byr when piutang_pasien.status is null then blg.tgl_byr else null end) as tgl_lunas, kamar_inap.tgl_keluar,bangsal.nm_bangsal,kamar_inap.stts_pulang, \n" +
//                    "kamar.kd_kamar from kamar_inap inner join reg_periksa on kamar_inap.no_rawat=reg_periksa.no_rawat " +
//                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
//                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar " +
//                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
//                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
//                    "left join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat " +
//                    "left join (SELECT max(tgl_bayar) as tgl_byr, no_rawat from bayar_piutang group by no_rawat) as bp on bp.no_rawat = reg_periksa.no_rawat\n" +
//                    "left join (SELECT max(tgl_byr) as tgl_byr, no_rawat from billing group by no_rawat) as blg on blg.no_rawat = reg_periksa.no_rawat\n" +
//                    "where kamar_inap.stts_pulang<>'Pindah Kamar'\n" +
//                    "and ((piutang_pasien.status = \"Lunas\" and bp.tgl_byr between ? and ?) or (piutang_pasien.status is null and blg.tgl_byr between ? and ?))\n" +
//                    "and concat(kamar.kd_bangsal,bangsal.nm_bangsal) like ? and concat(reg_periksa.kd_pj,penjab.png_jawab) like ? " + 
//                    "order by kamar_inap.tgl_keluar desc,kamar_inap.jam_keluar");
//            }else{
//                ps= koneksi.prepareStatement(
//                    "select kamar_inap.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,kamar_inap.tgl_keluar,bangsal.nm_bangsal,kamar_inap.stts_pulang, "+
//                    "kamar.kd_kamar from kamar_inap inner join reg_periksa on kamar_inap.no_rawat=reg_periksa.no_rawat "+
//                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
//                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "+
//                    "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
//                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
//                    "where kamar_inap.stts_pulang<>'Pindah Kamar' and kamar_inap.tgl_keluar between ? and ? and concat(kamar.kd_bangsal,bangsal.nm_bangsal) like ? and concat(reg_periksa.kd_pj,penjab.png_jawab) like ? "+
//                    "and reg_periksa.no_rawat not in (select piutang_pasien.no_rawat from piutang_pasien where piutang_pasien.no_rawat=reg_periksa.no_rawat) "+
//                    "order by kamar_inap.tgl_keluar,kamar_inap.jam_keluar");
//            }
//                
//            try {
//                if(tampilkan.equals("Sudah Bayar")){
//                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                    ps.setString(3,Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                    ps.setString(4,Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                    ps.setString(5,"%"+kdbangsal.getText()+nmbangsal.getText()+"%");
//                    ps.setString(6,"%"+KdCaraBayar.getText()+NmCaraBayar.getText()+"%");
//                }else{
//                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                    ps.setString(3,"%"+kdbangsal.getText()+nmbangsal.getText()+"%");
//                    ps.setString(4,"%"+KdCaraBayar.getText()+NmCaraBayar.getText()+"%");
//                }
//                    
//                rs=ps.executeQuery();
//                all=0;ttlLaborat=0;ttlDiagnostik=0;ttlRadiologi=0;ttlOperasi=0;ttlAnastesi=0;ttlKamarBedah=0;ttlTimOK=0;ttlObat=0;
//                ttlRanap_Dokter=0;ttlRanap_Paramedis=0;ttlRalan_Dokter=0;ttlVisite=0;
//                ttlRalan_Paramedis=0;ttlTambahan=0;ttlPotongan=0;ttlKamar=0;
//                ttlRegistrasi=0;ttlHarian=0;ttlUgdBedah=0;ttlUgdNonBedah=0;ttlMonitor=0;ttlOksigen=0;ttlUrs=0;ttlMikroskop=0;ttlTelescope=0;ttlMonitorOk=0;ttlDialisis=0;ttlTsaraf=0;ttlRehabMedik=0;ttlEswl=0;ttlAmbulans=0;ttlRetur_Obat=0;ttlResep_Pulang=0;ttlDeposit=0;ttlUSGK=0;ttlSpirometri=0;ttlKemo=0;ttlDr_persalinan=0;ttlPrwt_persalinan=0;ttlKmr_persalinan=0;ttlEcho_Doopler=0;
//                ttlService=0;
//                
//                while(rs.next()){
//                    if(!rs.getString("stts_pulang").equals("-")){
//                    if(!rs.getString("stts_pulang").equals("Pindah Kamar")){
//                        Keterangan="Belum Bayar";
//                        Laborat=0;   
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Laborat");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlLaborat=ttlLaborat+rs2.getDouble(1);
//                                Laborat=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                    
//                        Diagnostik=0;
//                        ps2=koneksi.prepareStatement("Select biaya from periksa_radiologi where no_rawat = ? and kd_jenis_prw like 'PDGT%'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlDiagnostik=ttlDiagnostik+rs2.getDouble(1);
//                                Diagnostik=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                               rs2.close();
//                            }
//                            if(ps2!=null){
//                               ps2.close();
//                            }
//                        }
//                        
//                        Radiologi=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Radiologi");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRadiologi=ttlRadiologi+rs2.getDouble(1)-Diagnostik;
//                                Radiologi=rs2.getDouble(1)-Diagnostik;
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Anastesi=0;
//                        ps2=koneksi.prepareStatement("Select sum(biayadokter_anestesi) from operasi where no_rawat = ?");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlAnastesi=ttlAnastesi+rs2.getDouble(1);
//                                Anastesi=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        KamarBedah=0;
//                        ps2=koneksi.prepareStatement("Select sum(biayasewaok) from operasi where no_rawat = ?");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlKamarBedah=ttlKamarBedah+rs2.getDouble(1);
//                                KamarBedah=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        TimOK=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_omloop) from operasi where no_rawat = ?");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlTimOK=ttlTimOK+rs2.getDouble(1);
//                                TimOK=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Operasi=0;
//                        ps2=koneksi.prepareStatement("Select sum(biayaoperator1) from operasi where no_rawat = ?");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlOperasi=ttlOperasi+rs2.getDouble(1);
//                                Operasi=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Oksigen=0;
//                        ps2=koneksi.prepareStatement("Select sum(total) from detail_pemberian_obat where no_rawat = ? and kode_brng='B000003106' and status='Ranap'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlOksigen=ttlOksigen+rs2.getDouble(1);
//                                Oksigen=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Urs=0;
//                        ps2=koneksi.prepareStatement("Select hargasatuan from beri_obat_operasi where no_rawat = ? and kd_obat='OK00003138'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlUrs=ttlUrs+rs2.getDouble(1);
//                                Urs=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Mikroskop=0;
//                        ps2=koneksi.prepareStatement("Select hargasatuan from beri_obat_operasi where no_rawat = ? and kd_obat='OK00003142'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlMikroskop=ttlMikroskop+rs2.getDouble(1);
//                                Mikroskop=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Telescope=0;
//                        ps2=koneksi.prepareStatement("Select hargasatuan from beri_obat_operasi where no_rawat = ? and kd_obat='OK00003143'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlTelescope=ttlTelescope+rs2.getDouble(1);
//                                Telescope=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        MonitorOk=0;
//                        ps2=koneksi.prepareStatement("Select hargasatuan from beri_obat_operasi where no_rawat = ? and kd_obat='OK00003133'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlMonitorOk=ttlMonitorOk+rs2.getDouble(1);
//                                MonitorOk=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Obat=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Obat");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlObat=ttlObat+rs2.getDouble(1)-Urs-Mikroskop-Telescope-MonitorOk;
//                                Obat=rs2.getDouble(1)-Urs-Mikroskop-Telescope-MonitorOk;
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Spirometri=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_drpr where no_rawat = ? and kd_jenis_prw in('RI00134')");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlSpirometri=ttlSpirometri+rs2.getDouble(1);
//                                Spirometri=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Kemo=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_drpr where  no_rawat = ? and lower(kd_jenis_prw) like '%kemo%' ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlKemo=ttlKemo+rs2.getDouble(1);
//                                Kemo=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Dr_persalinan=0;
//                        ps2=koneksi.prepareStatement("Select sum(tarif_tindakandr) from rawat_inap_drpr where  no_rawat = ? and kd_jenis_prw in('RI00275','RI00276','RI00277','RI00278','RI00279','RI00280','RI00281','RI00282','RI00283','RI00284') ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlDr_persalinan=ttlDr_persalinan+rs2.getDouble(1);
//                                Dr_persalinan=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        Prwt_persalinan=0;
//                        ps2=koneksi.prepareStatement("Select sum(tarif_tindakanpr) from rawat_inap_drpr where  no_rawat = ? and kd_jenis_prw in('RI00275','RI00276','RI00277','RI00278','RI00279','RI00280','RI00281','RI00282','RI00283','RI00284') ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlPrwt_persalinan=ttlPrwt_persalinan+rs2.getDouble(1);
//                                Prwt_persalinan=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Kmr_persalinan=0;
//                        ps2=koneksi.prepareStatement("Select sum(material) from rawat_inap_drpr where  no_rawat = ? and kd_jenis_prw in('RI00275','RI00276','RI00277','RI00278','RI00279','RI00280','RI00281','RI00282','RI00283','RI00284') ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlKmr_persalinan=ttlKmr_persalinan+rs2.getDouble(1);
//                                Kmr_persalinan=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Echo_Doopler=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_drpr where no_rawat = ? and kd_jenis_prw in('RI00137','RI00138','RI00139','RI00240','RI00254','RI00255','RI00256','RI00257') ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlEcho_Doopler=ttlEcho_Doopler+rs2.getDouble(1);
//                                Echo_Doopler=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Deposit=0;
//                        ps2=koneksi.prepareStatement("Select besar_deposit from deposit where no_rawat = ?");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlDeposit=ttlDeposit+rs2.getDouble(1);
//                                Deposit=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Registrasi=0;
//                        ps2=koneksi.prepareStatement("Select biaya_rawat from rawat_inap_pr where no_rawat = ? and kd_jenis_prw like 'adm%'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRegistrasi=ttlRegistrasi+rs2.getDouble(1);
//                                Registrasi=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Ranap_Dokter=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_dr where  no_rawat = ? and lower(kd_jenis_prw) like '%kon%' ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRanap_Dokter=ttlRanap_Dokter+rs2.getDouble(1);
//                                Ranap_Dokter=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }                        
//
//                        Visite=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_dr where  no_rawat = ? and lower(kd_jenis_prw) like '%visit%' ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlVisite=ttlVisite+rs2.getDouble(1);
//                                Visite=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                                                                  
//                        Ranap_Dokter_Paramedis=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Ranap Dokter Paramedis");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRanap_Paramedis=ttlRanap_Dokter+rs2.getDouble(1)-Registrasi;
//                                Ranap_Dokter_Paramedis=rs2.getDouble(1)-Registrasi;
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Monitor=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_pr where no_rawat = ? and kd_jenis_prw in "
//                                + "('RI00016','ugd018')");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlMonitor=ttlMonitor+rs2.getDouble(1);
//                                Monitor=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Dialisis=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_drpr where no_rawat = ? and kd_jenis_prw in('RI00252','RI00253','RJ93179')");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlDialisis=ttlDialisis+rs2.getDouble(1);
//                                Dialisis=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Tsaraf=0;
//                        ps2=koneksi.prepareStatement("Select biaya_rawat from rawat_inap_dr where no_rawat = ? and lower(kd_jenis_prw) like '%ipm%'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlTsaraf=ttlTsaraf+rs2.getDouble(1);
//                                Tsaraf=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        RehabMedik=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_drpr where no_rawat = ? and lower(kd_jenis_prw) like '%rhb%' ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRehabMedik=ttlRehabMedik+rs2.getDouble(1);
//                                RehabMedik=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }        
//                        
//                        Ambulans=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_pr where no_rawat = ? and kd_jenis_prw in ('RI00296','RI00297','RI00298','RI00299')");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlAmbulans=ttlAmbulans+rs2.getDouble(1);
//                                Ambulans=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Eswl=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_inap_drpr where no_rawat = ? and lower(kd_jenis_prw) like '%eswl%' ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlEswl=ttlEswl+rs2.getDouble(1);
//                                Eswl=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        UgdNonBedah=0;
//                        ps2=koneksi.prepareStatement("SELECT sum( total.biaya_rawat ) AS total_biaya FROM(SELECT biaya_rawat FROM rawat_jl_dr WHERE lower( kd_jenis_prw ) LIKE '%ugd%' and kd_dokter Not LIKE '%D0000173%' and no_rawat = ? UNION ALL SELECT biaya_rawat FROM rawat_jl_drpr  WHERE lower( kd_jenis_prw ) LIKE '%ugd%' and kd_dokter Not LIKE '%D0000173%' and no_rawat = ? UNION ALL SELECT biaya_rawat FROM rawat_jl_pr WHERE lower(kd_jenis_prw) LIKE '%ugd%' and no_rawat = ?) total ");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,rs.getString("no_rawat"));
//                            ps2.setString(3,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlUgdNonBedah=ttlUgdNonBedah+rs2.getDouble(1);
//                                UgdNonBedah=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }  
//                        
//                        Ranap_Paramedis=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Ranap Paramedis");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRanap_Paramedis=ttlRanap_Paramedis+rs2.getDouble(1)-Monitor;
//                                Ranap_Paramedis=rs2.getDouble(1)-Monitor;
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Ralan_Dokter=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Ralan Dokter");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRalan_Dokter=ttlRalan_Dokter+rs2.getDouble(1);
//                                Ralan_Dokter=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Ralan_Dokter_Paramedis=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Ralan Dokter Paramedis");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRalan_Dokter=ttlRalan_Dokter+rs2.getDouble(1);
//                                Ralan_Dokter_Paramedis=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Ralan_Paramedis=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Ralan Paramedis");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRalan_Paramedis=ttlRalan_Paramedis+rs2.getDouble(1);
//                                Ralan_Paramedis=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Tambahan=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Tambahan");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlTambahan=ttlTambahan+rs2.getDouble(1);
//                                Tambahan=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Potongan=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Potongan");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlPotongan=ttlPotongan+rs2.getDouble(1);
//                                Potongan=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Kamar=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Kamar");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlKamar=ttlKamar+rs2.getDouble(1);
//                                Kamar=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Harian=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Harian");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlHarian=ttlHarian+rs2.getDouble(1);
//                                Harian=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        UgdBedah=0;
//                        ps2=koneksi.prepareStatement("SELECT sum(total.biaya_rawat) AS total_biaya FROM (SELECT biaya_rawat FROM rawat_jl_dr WHERE lower(kd_jenis_prw) LIKE '%ugd%' and kd_dokter LIKE '%D0000173%' and no_rawat = ? UNION ALL SELECT biaya_rawat FROM rawat_jl_drpr WHERE lower(kd_jenis_prw) LIKE '%ugd%' and kd_dokter LIKE '%D0000173%' and no_rawat = ?) total");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,rs.getString("no_rawat"));                            
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlUgdBedah=ttlUgdBedah+rs2.getDouble(1);
//                                UgdBedah=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        USGK=0;
//                        ps2=koneksi.prepareStatement("Select sum(biaya_rawat) from rawat_jl_drpr where no_rawat = ? and kd_jenis_prw='RJ93310'");
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlUSGK=ttlUSGK+rs2.getDouble(1);
//                                USGK=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//                        
//                        Retur_Obat=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Retur Obat");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlRetur_Obat=ttlRetur_Obat+rs2.getDouble(1);
//                                Retur_Obat=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Resep_Pulang=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Resep Pulang");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlResep_Pulang=ttlResep_Pulang+rs2.getDouble(1);
//                                Resep_Pulang=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        Service=0;
//                        ps2=koneksi.prepareStatement(sqlps2);
//                        try {
//                            ps2.setString(1,rs.getString("no_rawat"));
//                            ps2.setString(2,"Service");
//                            rs2=ps2.executeQuery();
//                            while(rs2.next()){
//                                ttlService=ttlService+rs2.getDouble(1);
//                                Service=rs2.getDouble(1);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Notif 2: "+e);
//                        } finally{
//                            if(rs2!=null){
//                                rs2.close();
//                            }
//                            if(ps2!=null){
//                                ps2.close();
//                            }
//                        }
//
//                        all=all+Laborat+Radiologi+Diagnostik+Operasi+Anastesi+KamarBedah+TimOK+Obat+Ranap_Dokter+Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter+Ralan_Dokter_Paramedis+Ralan_Paramedis+Tambahan+Potongan+Kamar+Registrasi+Harian+UgdBedah+UgdNonBedah+Monitor+Oksigen+Urs+Mikroskop+Telescope+MonitorOk+Deposit+Dialisis+Tsaraf+RehabMedik+Eswl+Ambulans+USGK+Spirometri+Retur_Obat+Resep_Pulang+Service+Visite+Kemo+Dr_persalinan+Prwt_persalinan+Kmr_persalinan+Echo_Doopler;
//                        
//                        if((Laborat+Radiologi+Diagnostik+Operasi+Anastesi+KamarBedah+TimOK+Obat+Ranap_Dokter+Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter+Ralan_Dokter_Paramedis+Ralan_Paramedis+Tambahan+Potongan+Kamar+Registrasi+Harian+UgdBedah+UgdNonBedah+Monitor+Oksigen+Urs+Mikroskop+Telescope+MonitorOk+Deposit+Dialisis+Tsaraf+RehabMedik+Eswl+Ambulans+USGK+Spirometri+Retur_Obat+Resep_Pulang+Service+Visite+Kemo+Dr_persalinan+Prwt_persalinan+Kmr_persalinan+Echo_Doopler)>0){
//                            Keterangan="Sudah Bayar";
//                        }
//                        
//                        if(tampilkan.equals("Belum Bayar")&&Keterangan.equals("Belum Bayar")){
//                            tabMode.addRow(new Object[]{
//                                rs.getString("tgl_keluar"),Sequel.cariIsi("select no_nota from nota_inap where no_rawat=?",rs.getString("no_rawat")),
//                                rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),"",rs.getString("kd_kamar")+" "+rs.getString("nm_bangsal"),                                
//                                Valid.SetAngka(Registrasi),
//                                Valid.SetAngka(Ranap_Dokter+Ralan_Dokter),
//                                Valid.SetAngka(Visite),
//                                Valid.SetAngka(Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter_Paramedis+Ralan_Paramedis),
//                                Valid.SetAngka(Obat),Valid.SetAngka(Retur_Obat),Valid.SetAngka(Resep_Pulang),Valid.SetAngka(Laborat),Valid.SetAngka(Radiologi),Valid.SetAngka(Diagnostik),Valid.SetAngka(Potongan),
//                                Valid.SetAngka(Tambahan),Valid.SetAngka(Kamar+Service),Valid.SetAngka(Operasi),Valid.SetAngka(Anastesi),Valid.SetAngka(KamarBedah),Valid.SetAngka(TimOK),Valid.SetAngka(Harian),Valid.SetAngka(UgdBedah),Valid.SetAngka(UgdNonBedah),Valid.SetAngka(Monitor),Valid.SetAngka(Oksigen),Valid.SetAngka(Urs),Valid.SetAngka(Mikroskop),Valid.SetAngka(Telescope),Valid.SetAngka(MonitorOk),Valid.SetAngka(Dialisis),Valid.SetAngka(Tsaraf),Valid.SetAngka(RehabMedik),Valid.SetAngka(Eswl),Valid.SetAngka(Ambulans),Valid.SetAngka(USGK),Valid.SetAngka(Spirometri),Valid.SetAngka(Kemo),Valid.SetAngka(Dr_persalinan),Valid.SetAngka(Prwt_persalinan),Valid.SetAngka(Kmr_persalinan),Valid.SetAngka(Echo_Doopler),Valid.SetAngka(Laborat+Radiologi+Diagnostik+Operasi+Anastesi+KamarBedah+TimOK+Obat+Ranap_Dokter+
//                                        Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter+Ralan_Dokter_Paramedis+Ralan_Paramedis+UgdBedah+UgdNonBedah+Tambahan+Potongan+Kamar+Registrasi+Harian+Monitor+Oksigen+Urs+Mikroskop+Telescope+MonitorOk+Dialisis+Tsaraf+RehabMedik+Eswl+Ambulans+Spirometri+Retur_Obat+Resep_Pulang+Service+Visite+Kemo+Dr_persalinan+Prwt_persalinan+Kmr_persalinan+Echo_Doopler),Valid.SetAngka(Deposit),Valid.SetAngka(Laborat+Radiologi+Diagnostik+Operasi+Anastesi+KamarBedah+TimOK+Obat+Ranap_Dokter+
//                                        Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter+Ralan_Dokter_Paramedis+Ralan_Paramedis+UgdBedah+UgdNonBedah+Tambahan+Potongan+Kamar+Registrasi+Harian+Monitor+Oksigen+Urs+Mikroskop+Telescope+MonitorOk+Dialisis+Tsaraf+RehabMedik+Eswl+Ambulans+Spirometri+Retur_Obat+Resep_Pulang+Service+Visite+Kemo+Dr_persalinan+Prwt_persalinan+Kmr_persalinan+Echo_Doopler-Deposit)
//                            });
//                        }else if(tampilkan.equals("Sudah Bayar")&&Keterangan.equals("Sudah Bayar")){
//                            tabMode.addRow(new Object[]{
//                                rs.getString("tgl_keluar"),Sequel.cariIsi("select no_nota from nota_inap where no_rawat=?",rs.getString("no_rawat")),
//                                rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_lunas") + " - " + rs.getString("cara_bayar"),rs.getString("kd_kamar")+" "+rs.getString("nm_bangsal"),
//                                Valid.SetAngka(Registrasi),
//                                Valid.SetAngka(Ranap_Dokter+Ralan_Dokter),
//                                Valid.SetAngka(Visite),
//                                Valid.SetAngka(Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter_Paramedis+Ralan_Paramedis),
//                                Valid.SetAngka(Obat),Valid.SetAngka(Retur_Obat),Valid.SetAngka(Resep_Pulang),Valid.SetAngka(Laborat),Valid.SetAngka(Radiologi),Valid.SetAngka(Diagnostik),Valid.SetAngka(Potongan),
//                                Valid.SetAngka(Tambahan),Valid.SetAngka(Kamar+Service),Valid.SetAngka(Operasi),Valid.SetAngka(Anastesi),Valid.SetAngka(KamarBedah),Valid.SetAngka(TimOK),Valid.SetAngka(Harian),Valid.SetAngka(UgdBedah),Valid.SetAngka(UgdNonBedah),Valid.SetAngka(Monitor),Valid.SetAngka(Oksigen),Valid.SetAngka(Urs),Valid.SetAngka(Mikroskop),Valid.SetAngka(Telescope),Valid.SetAngka(MonitorOk),Valid.SetAngka(Dialisis),Valid.SetAngka(Tsaraf),Valid.SetAngka(RehabMedik),Valid.SetAngka(Eswl),Valid.SetAngka(Ambulans),Valid.SetAngka(USGK),Valid.SetAngka(Spirometri),Valid.SetAngka(Kemo),Valid.SetAngka(Dr_persalinan),Valid.SetAngka(Prwt_persalinan),Valid.SetAngka(Kmr_persalinan),Valid.SetAngka(Echo_Doopler),Valid.SetAngka(Laborat+Radiologi+Diagnostik+Operasi+Anastesi+KamarBedah+TimOK+Obat+Ranap_Dokter+
//                                        Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter+Ralan_Dokter_Paramedis+Ralan_Paramedis+UgdBedah+UgdNonBedah+Tambahan+Potongan+Kamar+Registrasi+Harian+Monitor+Oksigen+Urs+Mikroskop+Telescope+MonitorOk+Dialisis+Tsaraf+RehabMedik+Eswl+Ambulans+Spirometri+Retur_Obat+Resep_Pulang+Service+Visite+Kemo+Dr_persalinan+Prwt_persalinan+Kmr_persalinan+Echo_Doopler),Valid.SetAngka(Deposit),Valid.SetAngka(Laborat+Radiologi+Diagnostik+Operasi+Anastesi+KamarBedah+TimOK+Obat+Ranap_Dokter+
//                                        Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter+Ralan_Dokter_Paramedis+Ralan_Paramedis+UgdBedah+UgdNonBedah+Tambahan+Potongan+Kamar+Registrasi+Harian+Monitor+Oksigen+Urs+Mikroskop+Telescope+MonitorOk+Dialisis+Tsaraf+RehabMedik+Eswl+Ambulans+Spirometri+Retur_Obat+Resep_Pulang+Service+Visite+Kemo+Dr_persalinan+Prwt_persalinan+Kmr_persalinan+Echo_Doopler-Deposit)
//                            });
//                        }else if(tampilkan.equals("Semua")){
//                            tabMode.addRow(new Object[]{
//                                rs.getString("tgl_keluar"),Sequel.cariIsi("select no_nota from nota_inap where no_rawat=?",rs.getString("no_rawat")),
//                                rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),"",rs.getString("kd_kamar")+" "+rs.getString("nm_bangsal"),
//                                Valid.SetAngka(Registrasi),
//                                Valid.SetAngka(Ranap_Dokter+Ralan_Dokter),
//                                Valid.SetAngka(Visite),
//                                Valid.SetAngka(Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter_Paramedis+Ralan_Paramedis),
//                                Valid.SetAngka(Obat),Valid.SetAngka(Retur_Obat),Valid.SetAngka(Resep_Pulang),Valid.SetAngka(Laborat),Valid.SetAngka(Radiologi),Valid.SetAngka(Diagnostik),Valid.SetAngka(Potongan),
//                                Valid.SetAngka(Tambahan),Valid.SetAngka(Kamar+Service),Valid.SetAngka(Operasi),Valid.SetAngka(Anastesi),Valid.SetAngka(KamarBedah),Valid.SetAngka(TimOK),Valid.SetAngka(Harian),Valid.SetAngka(UgdBedah),Valid.SetAngka(UgdNonBedah),Valid.SetAngka(Monitor),Valid.SetAngka(Oksigen),Valid.SetAngka(Urs),Valid.SetAngka(Mikroskop),Valid.SetAngka(Telescope),Valid.SetAngka(MonitorOk),Valid.SetAngka(Dialisis),Valid.SetAngka(Tsaraf),Valid.SetAngka(RehabMedik),Valid.SetAngka(Eswl),Valid.SetAngka(Ambulans),Valid.SetAngka(USGK),Valid.SetAngka(Spirometri),Valid.SetAngka(Kemo),Valid.SetAngka(Dr_persalinan),Valid.SetAngka(Prwt_persalinan),Valid.SetAngka(Kmr_persalinan),Valid.SetAngka(Echo_Doopler),Valid.SetAngka(Laborat+Radiologi+Diagnostik+Operasi+Anastesi+KamarBedah+TimOK+Obat+Ranap_Dokter+
//                                        Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter+Ralan_Dokter_Paramedis+Ralan_Paramedis+UgdBedah+UgdNonBedah+Tambahan+Potongan+Kamar+Registrasi+Harian+Monitor+Oksigen+Urs+Mikroskop+Telescope+MonitorOk+Dialisis+Tsaraf+RehabMedik+Eswl+Ambulans+Spirometri+Retur_Obat+Resep_Pulang+Service+Visite+Kemo+Dr_persalinan+Prwt_persalinan+Kmr_persalinan+Echo_Doopler),Valid.SetAngka(Deposit),Valid.SetAngka(Laborat+Radiologi+Diagnostik+Operasi+Anastesi+KamarBedah+TimOK+Obat+Ranap_Dokter+
//                                        Ranap_Dokter_Paramedis+Ranap_Paramedis+Ralan_Dokter+Ralan_Dokter_Paramedis+Ralan_Paramedis+UgdBedah+UgdNonBedah+Tambahan+Potongan+Kamar+Registrasi+Harian+Monitor+Oksigen+Urs+Mikroskop+Telescope+MonitorOk+Dialisis+Tsaraf+RehabMedik+Eswl+Ambulans+Spirometri+Retur_Obat+Resep_Pulang+Service+Visite+Kemo+Dr_persalinan+Prwt_persalinan+Kmr_persalinan+Echo_Doopler-Deposit)
//                            });
//                        }
//                            
//                        
//                    }}                
//                }
//                LCount2.setText(""+tabMode.getRowCount());
//                if(!tampilkan.equals("Belum Bayar")){
//                    tabMode.addRow(new Object[]{
//                            ">> Total ",":","","","","",Valid.SetAngka(ttlRegistrasi),Valid.SetAngka(ttlRanap_Dokter+ttlRalan_Dokter),Valid.SetAngka(ttlVisite),Valid.SetAngka(ttlRanap_Paramedis+ttlRalan_Paramedis),
//                            Valid.SetAngka(ttlObat),Valid.SetAngka(ttlRetur_Obat),Valid.SetAngka(ttlResep_Pulang),Valid.SetAngka(ttlLaborat),Valid.SetAngka(ttlRadiologi),Valid.SetAngka(ttlDiagnostik),Valid.SetAngka(ttlPotongan),
//                            Valid.SetAngka(ttlTambahan),Valid.SetAngka(ttlKamar+ttlService),Valid.SetAngka(ttlOperasi),Valid.SetAngka(ttlAnastesi),Valid.SetAngka(ttlKamarBedah),Valid.SetAngka(ttlTimOK),Valid.SetAngka(ttlHarian),Valid.SetAngka(ttlUgdBedah),Valid.SetAngka(ttlUgdNonBedah),Valid.SetAngka(ttlMonitor),Valid.SetAngka(ttlOksigen),Valid.SetAngka(ttlUrs),Valid.SetAngka(ttlMikroskop),Valid.SetAngka(ttlTelescope),Valid.SetAngka(ttlMonitorOk),Valid.SetAngka(ttlDialisis),Valid.SetAngka(ttlTsaraf),Valid.SetAngka(ttlRehabMedik),Valid.SetAngka(ttlEswl),Valid.SetAngka(ttlAmbulans),Valid.SetAngka(ttlUSGK),Valid.SetAngka(ttlSpirometri),Valid.SetAngka(ttlKemo),Valid.SetAngka(ttlDr_persalinan),Valid.SetAngka(ttlPrwt_persalinan),Valid.SetAngka(ttlKmr_persalinan),Valid.SetAngka(ttlEcho_Doopler),Valid.SetAngka(all),Valid.SetAngka(ttlDeposit),Valid.SetAngka(all-ttlDeposit),
//                    });
//                    LCount.setText(Valid.SetAngka(all));
//                }else{
//                    LCount.setText(""+0);
//                } 
//            } catch (Exception e) {
//                System.out.println("Notif 1 : "+e);
//            } finally{
//                if(rs!=null){
//                    rs.close();
//                }
//                if(ps!=null){
//                    ps.close();
//                }
//            }
//            
//            this.setCursor(Cursor.getDefaultCursor());
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//        }
//    }

//revisi metod tampil agar lebih kencang tahap 2    
public void tampil() {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    Valid.tabelKosong(tabMode);

    class BaseData {
        String no_rawat = "";
        String no_rkm_medis = "";
        String nm_pasien = "";
        String tgl_keluar = "";
        String nm_bangsal = "";
        String stts_pulang = "";
        String kd_kamar = "";
        String no_nota = "";
        String cara_bayar = "";
        String tgl_lunas = "";
    }

    PreparedStatement psBatch = null;
    ResultSet rsBatch = null;

    try {
        java.util.List<BaseData> listData = new java.util.ArrayList<BaseData>();
        java.util.List<String> listNoRawat = new java.util.ArrayList<String>();

        if (tampilkan.equals("Sudah Bayar")) {
            ps = koneksi.prepareStatement(
                "select kamar_inap.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
                "(case when piutang_pasien.status is not null then 'Piutang' else 'Non Piutang' end) as cara_bayar," +
                "(case when bp.tgl_byr is not null then bp.tgl_byr when piutang_pasien.status is null then blg.tgl_byr else null end) as tgl_lunas," +
                "kamar_inap.tgl_keluar,bangsal.nm_bangsal,kamar_inap.stts_pulang,kamar.kd_kamar,ifnull(ni.no_nota,'') as no_nota " +
                "from kamar_inap " +
                "inner join reg_periksa on kamar_inap.no_rawat=reg_periksa.no_rawat " +
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar " +
                "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
                "left join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat " +
                "left join (select max(tgl_bayar) as tgl_byr, no_rawat from bayar_piutang group by no_rawat) as bp on bp.no_rawat=reg_periksa.no_rawat " +
                "left join (select max(tgl_byr) as tgl_byr, no_rawat from billing group by no_rawat) as blg on blg.no_rawat=reg_periksa.no_rawat " +
                "left join (select max(no_nota) as no_nota, no_rawat from nota_inap group by no_rawat) as ni on ni.no_rawat=reg_periksa.no_rawat " +
                "where kamar_inap.stts_pulang<>'Pindah Kamar' " +
                "and ((piutang_pasien.status='Lunas' and bp.tgl_byr between ? and ?) or (piutang_pasien.status is null and blg.tgl_byr between ? and ?)) " +
                "and concat(kamar.kd_bangsal,bangsal.nm_bangsal) like ? " +
                "and concat(reg_periksa.kd_pj,penjab.png_jawab) like ? " +
                "order by kamar_inap.tgl_keluar desc,kamar_inap.jam_keluar"
            );

            ps.setString(1, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
            ps.setString(2, Valid.SetTgl(Tgl2.getSelectedItem() + ""));
            ps.setString(3, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
            ps.setString(4, Valid.SetTgl(Tgl2.getSelectedItem() + ""));
            ps.setString(5, "%" + kdbangsal.getText() + nmbangsal.getText() + "%");
            ps.setString(6, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
        } else {
            ps = koneksi.prepareStatement(
                "select kamar_inap.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
                "'' as cara_bayar,'' as tgl_lunas,kamar_inap.tgl_keluar,bangsal.nm_bangsal,kamar_inap.stts_pulang,kamar.kd_kamar,ifnull(ni.no_nota,'') as no_nota " +
                "from kamar_inap " +
                "inner join reg_periksa on kamar_inap.no_rawat=reg_periksa.no_rawat " +
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar " +
                "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj " +
                "left join (select max(no_nota) as no_nota, no_rawat from nota_inap group by no_rawat) as ni on ni.no_rawat=reg_periksa.no_rawat " +
                "where kamar_inap.stts_pulang<>'Pindah Kamar' " +
                "and kamar_inap.tgl_keluar between ? and ? " +
                "and concat(kamar.kd_bangsal,bangsal.nm_bangsal) like ? " +
                "and concat(reg_periksa.kd_pj,penjab.png_jawab) like ? " +
                "and reg_periksa.no_rawat not in (select piutang_pasien.no_rawat from piutang_pasien where piutang_pasien.no_rawat=reg_periksa.no_rawat) " +
                "order by kamar_inap.tgl_keluar,kamar_inap.jam_keluar"
            );

            ps.setString(1, Valid.SetTgl(Tgl1.getSelectedItem() + ""));
            ps.setString(2, Valid.SetTgl(Tgl2.getSelectedItem() + ""));
            ps.setString(3, "%" + kdbangsal.getText() + nmbangsal.getText() + "%");
            ps.setString(4, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
        }

        rs = ps.executeQuery();
        while (rs.next()) {
            BaseData d = new BaseData();
            d.no_rawat = rs.getString("no_rawat");
            d.no_rkm_medis = rs.getString("no_rkm_medis");
            d.nm_pasien = rs.getString("nm_pasien");
            d.tgl_keluar = rs.getString("tgl_keluar");
            d.nm_bangsal = rs.getString("nm_bangsal");
            d.stts_pulang = rs.getString("stts_pulang");
            d.kd_kamar = rs.getString("kd_kamar");
            d.no_nota = rs.getString("no_nota");
            d.cara_bayar = rs.getString("cara_bayar");
            d.tgl_lunas = rs.getString("tgl_lunas");
            listData.add(d);
            listNoRawat.add(d.no_rawat);
        }
        rs.close();
        rs = null;
        ps.close();
        ps = null;

        all = 0;
        ttlLaborat = 0; ttlDiagnostik = 0; ttlRadiologi = 0; ttlOperasi = 0; ttlAnastesi = 0; ttlKamarBedah = 0; ttlTimOK = 0; ttlObat = 0;
        ttlRanap_Dokter = 0; ttlRanap_Paramedis = 0; ttlRalan_Dokter = 0; ttlVisite = 0;
        ttlRalan_Paramedis = 0; ttlTambahan = 0; ttlPotongan = 0; ttlKamar = 0;
        ttlRegistrasi = 0; ttlHarian = 0; ttlUgdBedah = 0; ttlUgdNonBedah = 0; ttlMonitor = 0; ttlOksigen = 0; ttlUrs = 0; ttlMikroskop = 0; ttlTelescope = 0; ttlMonitorOk = 0; ttlDialisis = 0; ttlTsaraf = 0; ttlRehabMedik = 0; ttlEswl = 0; ttlAmbulans = 0; ttlRetur_Obat = 0; ttlResep_Pulang = 0; ttlDeposit = 0; ttlUSGK = 0; ttlSpirometri = 0; ttlKemo = 0; ttlDr_persalinan = 0; ttlPrwt_persalinan = 0; ttlKmr_persalinan = 0; ttlEcho_Doopler = 0;
        ttlService = 0;

        java.util.Map<String, Double> mapLaboratBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRadiologiBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapObatBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRanapDokterParamedisBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRanapParamedisBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRalanDokterBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRalanDokterParamedisBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRalanParamedisBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapTambahanBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapPotonganBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapKamarBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapHarianBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapReturObatBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapResepPulangBilling = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapServiceBilling = new java.util.HashMap<String, Double>();

        java.util.Map<String, Double> mapDiagnostik = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapAnastesi = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapKamarBedah = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapTimOK = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapOperasi = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapOksigen = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapUrs = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapMikroskop = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapTelescope = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapMonitorOk = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapSpirometri = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapKemo = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapDrPersalinan = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapPrwtPersalinan = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapKmrPersalinan = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapEchoDoopler = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapDeposit = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRegistrasi = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRanapDokter = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapVisite = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapMonitor = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapDialisis = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapTsaraf = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRehabMedik = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapEswl = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapAmbulans = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapUgdNonBedah = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapUgdBedah = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapUSGK = new java.util.HashMap<String, Double>();

        java.util.Map<String, Double> mapDiagnostikTotal = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapUrsTotal = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapMikroskopTotal = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapTelescopeTotal = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapMonitorOkTotal = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapDepositTotal = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapRegistrasiTotal = new java.util.HashMap<String, Double>();
        java.util.Map<String, Double> mapTsarafTotal = new java.util.HashMap<String, Double>();

        if (!listNoRawat.isEmpty()) {
            StringBuilder inBuilder = new StringBuilder();
            for (int x = 0; x < listNoRawat.size(); x++) {
                if (x > 0) {
                    inBuilder.append(",");
                }
                inBuilder.append("?");
            }
            String inClause = inBuilder.toString();

            String billingFilter =
                " nm_perawatan not in ('Konsultasi dokter(BPJS)','Konsultasi Malam'," +
                "'INJEKSI INTRA-ARTICULAR','ASSESSMENT & PRESKRIPSI ORTOTIK PROSTETIK'," +
                "'6 MINUTE WALKING TEST','ANALISA GAIT','LOW LEVEL LASER THERAPY/SITE','BIOFEEDBACK','KINESIOTAPING'," +
                "'EXERCISE RINGAN','EXERCISE  MODERAT','EXERCISE BERAT','SWD MODALITAS','USD MODALITAS'," +
                "'IR MODALITAS','ELECTRICAL STIMULATION MODALITAS','MODALITAS + EXERCISE','2 MODALITAS + 1 EXERCISE','3 EXERCISE') " +
                " and lower(nm_perawatan) not like '%partus%' " +
                " and lower(nm_perawatan) not like '%spirometri%' " +
                " and lower(nm_perawatan) not like '%kemo%' " +
                " and lower(nm_perawatan) not like '%intra%mus%' " +
                " and lower(nm_perawatan) not like '%zyga%' " +
                " and lower(nm_perawatan) not like '%bursitis%' " +
                " and lower(nm_perawatan) not like '%vis%' " +
                " and lower(nm_perawatan) not like '%pem%oksigen%' " +
                " and lower(nm_perawatan) not like '%biaya%onloop%' " +
                " and lower(nm_perawatan) not like '%biaya%sewa%ok%' " +
                " and lower(nm_perawatan) not like '%biaya%anastesi%' " +
                " and lower(nm_perawatan) not like '%ipm%' " +
                " and lower(nm_perawatan) not like '%biaya%operator1%' " +
                " and lower(nm_perawatan) not like '%dialisa%' " +
                " and lower(status) not like '%ralan%paramedis%' ";

            // BILLING
            psBatch = koneksi.prepareStatement(
                "select no_rawat,status,sum(totalbiaya) as total " +
                "from billing " +
                "where no_rawat in (" + inClause + ") and " + billingFilter +
                "group by no_rawat,status"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                String noRawat = rsBatch.getString("no_rawat");
                String status = rsBatch.getString("status");
                double total = rsBatch.getDouble("total");

                if ("Laborat".equals(status)) {
                    mapLaboratBilling.put(noRawat, total);
                } else if ("Radiologi".equals(status)) {
                    mapRadiologiBilling.put(noRawat, total);
                } else if ("Obat".equals(status)) {
                    mapObatBilling.put(noRawat, total);
                } else if ("Ranap Dokter Paramedis".equals(status)) {
                    mapRanapDokterParamedisBilling.put(noRawat, total);
                } else if ("Ranap Paramedis".equals(status)) {
                    mapRanapParamedisBilling.put(noRawat, total);
                } else if ("Ralan Dokter".equals(status)) {
                    mapRalanDokterBilling.put(noRawat, total);
                } else if ("Ralan Dokter Paramedis".equals(status)) {
                    mapRalanDokterParamedisBilling.put(noRawat, total);
                } else if ("Ralan Paramedis".equals(status)) {
                    mapRalanParamedisBilling.put(noRawat, total);
                } else if ("Tambahan".equals(status)) {
                    mapTambahanBilling.put(noRawat, total);
                } else if ("Potongan".equals(status)) {
                    mapPotonganBilling.put(noRawat, total);
                } else if ("Kamar".equals(status)) {
                    mapKamarBilling.put(noRawat, total);
                } else if ("Harian".equals(status)) {
                    mapHarianBilling.put(noRawat, total);
                } else if ("Retur Obat".equals(status)) {
                    mapReturObatBilling.put(noRawat, total);
                } else if ("Resep Pulang".equals(status)) {
                    mapResepPulangBilling.put(noRawat, total);
                } else if ("Service".equals(status)) {
                    mapServiceBilling.put(noRawat, total);
                }
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // DIAGNOSTIK
            psBatch = koneksi.prepareStatement(
                "select no_rawat,biaya from periksa_radiologi " +
                "where no_rawat in (" + inClause + ") and kd_jenis_prw like 'PDGT%' " +
                "order by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                String noRawat = rsBatch.getString("no_rawat");
                double biaya = rsBatch.getDouble("biaya");

                mapDiagnostik.put(noRawat, biaya);
                mapDiagnostikTotal.put(noRawat, (mapDiagnostikTotal.get(noRawat) == null ? 0 : mapDiagnostikTotal.get(noRawat)) + biaya);
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // OPERASI
            psBatch = koneksi.prepareStatement(
                "select no_rawat," +
                "sum(biayadokter_anestesi) as Anastesi," +
                "sum(biayasewaok) as KamarBedah," +
                "sum(biaya_omloop) as TimOK," +
                "sum(biayaoperator1) as Operasi " +
                "from operasi where no_rawat in (" + inClause + ") group by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                String noRawat = rsBatch.getString("no_rawat");
                mapAnastesi.put(noRawat, rsBatch.getDouble("Anastesi"));
                mapKamarBedah.put(noRawat, rsBatch.getDouble("KamarBedah"));
                mapTimOK.put(noRawat, rsBatch.getDouble("TimOK"));
                mapOperasi.put(noRawat, rsBatch.getDouble("Operasi"));
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // OKSIGEN
            psBatch = koneksi.prepareStatement(
                "select no_rawat,sum(total) as Oksigen " +
                "from detail_pemberian_obat " +
                "where no_rawat in (" + inClause + ") and kode_brng='B000003106' and status='Ranap' " +
                "group by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                mapOksigen.put(rsBatch.getString("no_rawat"), rsBatch.getDouble("Oksigen"));
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // BERI OBAT OPERASI
            psBatch = koneksi.prepareStatement(
                "select no_rawat,kd_obat,hargasatuan from beri_obat_operasi " +
                "where no_rawat in (" + inClause + ") and kd_obat in ('OK00003138','OK00003142','OK00003143','OK00003133') " +
                "order by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                String noRawat = rsBatch.getString("no_rawat");
                String kdObat = rsBatch.getString("kd_obat");
                double harga = rsBatch.getDouble("hargasatuan");

                if ("OK00003138".equals(kdObat)) {
                    mapUrs.put(noRawat, harga);
                    mapUrsTotal.put(noRawat, (mapUrsTotal.get(noRawat) == null ? 0 : mapUrsTotal.get(noRawat)) + harga);
                } else if ("OK00003142".equals(kdObat)) {
                    mapMikroskop.put(noRawat, harga);
                    mapMikroskopTotal.put(noRawat, (mapMikroskopTotal.get(noRawat) == null ? 0 : mapMikroskopTotal.get(noRawat)) + harga);
                } else if ("OK00003143".equals(kdObat)) {
                    mapTelescope.put(noRawat, harga);
                    mapTelescopeTotal.put(noRawat, (mapTelescopeTotal.get(noRawat) == null ? 0 : mapTelescopeTotal.get(noRawat)) + harga);
                } else if ("OK00003133".equals(kdObat)) {
                    mapMonitorOk.put(noRawat, harga);
                    mapMonitorOkTotal.put(noRawat, (mapMonitorOkTotal.get(noRawat) == null ? 0 : mapMonitorOkTotal.get(noRawat)) + harga);
                }
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // RAWAT INAP DRPR
            psBatch = koneksi.prepareStatement(
                "select no_rawat," +
                "sum(case when kd_jenis_prw='RI00134' then biaya_rawat else 0 end) as Spirometri," +
                "sum(case when lower(kd_jenis_prw) like '%kemo%' then biaya_rawat else 0 end) as Kemo," +
                "sum(case when kd_jenis_prw in('RI00275','RI00276','RI00277','RI00278','RI00279','RI00280','RI00281','RI00282','RI00283','RI00284') then tarif_tindakandr else 0 end) as DrPersalinan," +
                "sum(case when kd_jenis_prw in('RI00275','RI00276','RI00277','RI00278','RI00279','RI00280','RI00281','RI00282','RI00283','RI00284') then tarif_tindakanpr else 0 end) as PrwtPersalinan," +
                "sum(case when kd_jenis_prw in('RI00275','RI00276','RI00277','RI00278','RI00279','RI00280','RI00281','RI00282','RI00283','RI00284') then material else 0 end) as KmrPersalinan," +
                "sum(case when kd_jenis_prw in('RI00137','RI00138','RI00139','RI00240','RI00254','RI00255','RI00256','RI00257') then biaya_rawat else 0 end) as EchoDoopler," +
                "sum(case when kd_jenis_prw in('RI00252','RI00253','RJ93179') then biaya_rawat else 0 end) as Dialisis," +
                "sum(case when lower(kd_jenis_prw) like '%rhb%' then biaya_rawat else 0 end) as RehabMedik," +
                "sum(case when lower(kd_jenis_prw) like '%eswl%' then biaya_rawat else 0 end) as Eswl " +
                "from rawat_inap_drpr where no_rawat in (" + inClause + ") group by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                String noRawat = rsBatch.getString("no_rawat");
                mapSpirometri.put(noRawat, rsBatch.getDouble("Spirometri"));
                mapKemo.put(noRawat, rsBatch.getDouble("Kemo"));
                mapDrPersalinan.put(noRawat, rsBatch.getDouble("DrPersalinan"));
                mapPrwtPersalinan.put(noRawat, rsBatch.getDouble("PrwtPersalinan"));
                mapKmrPersalinan.put(noRawat, rsBatch.getDouble("KmrPersalinan"));
                mapEchoDoopler.put(noRawat, rsBatch.getDouble("EchoDoopler"));
                mapDialisis.put(noRawat, rsBatch.getDouble("Dialisis"));
                mapRehabMedik.put(noRawat, rsBatch.getDouble("RehabMedik"));
                mapEswl.put(noRawat, rsBatch.getDouble("Eswl"));
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // DEPOSIT
            psBatch = koneksi.prepareStatement(
                "select no_rawat,besar_deposit from deposit " +
                "where no_rawat in (" + inClause + ") order by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                String noRawat = rsBatch.getString("no_rawat");
                double besar = rsBatch.getDouble("besar_deposit");

                mapDeposit.put(noRawat, besar);
                mapDepositTotal.put(noRawat, (mapDepositTotal.get(noRawat) == null ? 0 : mapDepositTotal.get(noRawat)) + besar);
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // RAWAT INAP PR
            psBatch = koneksi.prepareStatement(
                "select no_rawat,kd_jenis_prw,biaya_rawat from rawat_inap_pr " +
                "where no_rawat in (" + inClause + ") " +
                "and (kd_jenis_prw like 'adm%' or kd_jenis_prw in('RI00016','ugd018','RI00296','RI00297','RI00298','RI00299')) " +
                "order by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                String noRawat = rsBatch.getString("no_rawat");
                String kd = rsBatch.getString("kd_jenis_prw");
                double biaya = rsBatch.getDouble("biaya_rawat");

                if (kd != null && kd.toLowerCase().startsWith("adm")) {
                    mapRegistrasi.put(noRawat, biaya);
                    mapRegistrasiTotal.put(noRawat, (mapRegistrasiTotal.get(noRawat) == null ? 0 : mapRegistrasiTotal.get(noRawat)) + biaya);
                }
                if ("RI00016".equals(kd) || "ugd018".equalsIgnoreCase(kd)) {
                    mapMonitor.put(noRawat, (mapMonitor.get(noRawat) == null ? 0 : mapMonitor.get(noRawat)) + biaya);
                }
                if ("RI00296".equals(kd) || "RI00297".equals(kd) || "RI00298".equals(kd) || "RI00299".equals(kd)) {
                    mapAmbulans.put(noRawat, (mapAmbulans.get(noRawat) == null ? 0 : mapAmbulans.get(noRawat)) + biaya);
                }
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // RAWAT INAP DR
            psBatch = koneksi.prepareStatement(
                "select no_rawat,kd_jenis_prw,biaya_rawat from rawat_inap_dr " +
                "where no_rawat in (" + inClause + ") " +
                "and (lower(kd_jenis_prw) like '%kon%' or lower(kd_jenis_prw) like '%visit%' or lower(kd_jenis_prw) like '%ipm%') " +
                "order by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                String noRawat = rsBatch.getString("no_rawat");
                String kd = rsBatch.getString("kd_jenis_prw");
                String kdLower = kd == null ? "" : kd.toLowerCase();
                double biaya = rsBatch.getDouble("biaya_rawat");

                if (kdLower.contains("kon")) {
                    mapRanapDokter.put(noRawat, (mapRanapDokter.get(noRawat) == null ? 0 : mapRanapDokter.get(noRawat)) + biaya);
                }
                if (kdLower.contains("visit")) {
                    mapVisite.put(noRawat, (mapVisite.get(noRawat) == null ? 0 : mapVisite.get(noRawat)) + biaya);
                }
                if (kdLower.contains("ipm")) {
                    mapTsaraf.put(noRawat, biaya);
                    mapTsarafTotal.put(noRawat, (mapTsarafTotal.get(noRawat) == null ? 0 : mapTsarafTotal.get(noRawat)) + biaya);
                }
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // UGD NON BEDAH
            psBatch = koneksi.prepareStatement(
                "select no_rawat,sum(biaya_rawat) as total_biaya from ( " +
                "select no_rawat,biaya_rawat from rawat_jl_dr where no_rawat in (" + inClause + ") and lower(kd_jenis_prw) like '%ugd%' and kd_dokter not like '%D0000173%' " +
                "union all " +
                "select no_rawat,biaya_rawat from rawat_jl_drpr where no_rawat in (" + inClause + ") and lower(kd_jenis_prw) like '%ugd%' and kd_dokter not like '%D0000173%' " +
                "union all " +
                "select no_rawat,biaya_rawat from rawat_jl_pr where no_rawat in (" + inClause + ") and lower(kd_jenis_prw) like '%ugd%' " +
                ") x group by no_rawat"
            );
            int p = 1;
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(p++, listNoRawat.get(x));
            }
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(p++, listNoRawat.get(x));
            }
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(p++, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                mapUgdNonBedah.put(rsBatch.getString("no_rawat"), rsBatch.getDouble("total_biaya"));
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // UGD BEDAH
            psBatch = koneksi.prepareStatement(
                "select no_rawat,sum(biaya_rawat) as total_biaya from ( " +
                "select no_rawat,biaya_rawat from rawat_jl_dr where no_rawat in (" + inClause + ") and lower(kd_jenis_prw) like '%ugd%' and kd_dokter like '%D0000173%' " +
                "union all " +
                "select no_rawat,biaya_rawat from rawat_jl_drpr where no_rawat in (" + inClause + ") and lower(kd_jenis_prw) like '%ugd%' and kd_dokter like '%D0000173%' " +
                ") x group by no_rawat"
            );
            p = 1;
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(p++, listNoRawat.get(x));
            }
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(p++, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                mapUgdBedah.put(rsBatch.getString("no_rawat"), rsBatch.getDouble("total_biaya"));
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;

            // USGK
            psBatch = koneksi.prepareStatement(
                "select no_rawat,sum(biaya_rawat) as total_biaya from rawat_jl_drpr " +
                "where no_rawat in (" + inClause + ") and kd_jenis_prw='RJ93310' group by no_rawat"
            );
            for (int x = 0; x < listNoRawat.size(); x++) {
                psBatch.setString(x + 1, listNoRawat.get(x));
            }
            rsBatch = psBatch.executeQuery();
            while (rsBatch.next()) {
                mapUSGK.put(rsBatch.getString("no_rawat"), rsBatch.getDouble("total_biaya"));
            }
            rsBatch.close();
            rsBatch = null;
            psBatch.close();
            psBatch = null;
        }

        for (int idx = 0; idx < listData.size(); idx++) {
            BaseData data = listData.get(idx);

            if (data.stts_pulang.equals("-")) {
                continue;
            }

            Keterangan = "Belum Bayar";

            Laborat = mapLaboratBilling.get(data.no_rawat) == null ? 0 : mapLaboratBilling.get(data.no_rawat);
            Diagnostik = mapDiagnostik.get(data.no_rawat) == null ? 0 : mapDiagnostik.get(data.no_rawat);
            Radiologi = (mapRadiologiBilling.get(data.no_rawat) == null ? 0 : mapRadiologiBilling.get(data.no_rawat)) - Diagnostik;

            Anastesi = mapAnastesi.get(data.no_rawat) == null ? 0 : mapAnastesi.get(data.no_rawat);
            KamarBedah = mapKamarBedah.get(data.no_rawat) == null ? 0 : mapKamarBedah.get(data.no_rawat);
            TimOK = mapTimOK.get(data.no_rawat) == null ? 0 : mapTimOK.get(data.no_rawat);
            Operasi = mapOperasi.get(data.no_rawat) == null ? 0 : mapOperasi.get(data.no_rawat);

            Oksigen = mapOksigen.get(data.no_rawat) == null ? 0 : mapOksigen.get(data.no_rawat);

            Urs = mapUrs.get(data.no_rawat) == null ? 0 : mapUrs.get(data.no_rawat);
            Mikroskop = mapMikroskop.get(data.no_rawat) == null ? 0 : mapMikroskop.get(data.no_rawat);
            Telescope = mapTelescope.get(data.no_rawat) == null ? 0 : mapTelescope.get(data.no_rawat);
            MonitorOk = mapMonitorOk.get(data.no_rawat) == null ? 0 : mapMonitorOk.get(data.no_rawat);

            Obat = (mapObatBilling.get(data.no_rawat) == null ? 0 : mapObatBilling.get(data.no_rawat)) - Urs - Mikroskop - Telescope - MonitorOk;

            Spirometri = mapSpirometri.get(data.no_rawat) == null ? 0 : mapSpirometri.get(data.no_rawat);
            Kemo = mapKemo.get(data.no_rawat) == null ? 0 : mapKemo.get(data.no_rawat);
            Dr_persalinan = mapDrPersalinan.get(data.no_rawat) == null ? 0 : mapDrPersalinan.get(data.no_rawat);
            Prwt_persalinan = mapPrwtPersalinan.get(data.no_rawat) == null ? 0 : mapPrwtPersalinan.get(data.no_rawat);
            Kmr_persalinan = mapKmrPersalinan.get(data.no_rawat) == null ? 0 : mapKmrPersalinan.get(data.no_rawat);
            Echo_Doopler = mapEchoDoopler.get(data.no_rawat) == null ? 0 : mapEchoDoopler.get(data.no_rawat);

            Deposit = mapDeposit.get(data.no_rawat) == null ? 0 : mapDeposit.get(data.no_rawat);
            Registrasi = mapRegistrasi.get(data.no_rawat) == null ? 0 : mapRegistrasi.get(data.no_rawat);

            Ranap_Dokter = mapRanapDokter.get(data.no_rawat) == null ? 0 : mapRanapDokter.get(data.no_rawat);
            Visite = mapVisite.get(data.no_rawat) == null ? 0 : mapVisite.get(data.no_rawat);

            Ranap_Dokter_Paramedis = (mapRanapDokterParamedisBilling.get(data.no_rawat) == null ? 0 : mapRanapDokterParamedisBilling.get(data.no_rawat)) - Registrasi;

            Monitor = mapMonitor.get(data.no_rawat) == null ? 0 : mapMonitor.get(data.no_rawat);
            Dialisis = mapDialisis.get(data.no_rawat) == null ? 0 : mapDialisis.get(data.no_rawat);
            Tsaraf = mapTsaraf.get(data.no_rawat) == null ? 0 : mapTsaraf.get(data.no_rawat);
            RehabMedik = mapRehabMedik.get(data.no_rawat) == null ? 0 : mapRehabMedik.get(data.no_rawat);
            Eswl = mapEswl.get(data.no_rawat) == null ? 0 : mapEswl.get(data.no_rawat);
            Ambulans = mapAmbulans.get(data.no_rawat) == null ? 0 : mapAmbulans.get(data.no_rawat);
            UgdNonBedah = mapUgdNonBedah.get(data.no_rawat) == null ? 0 : mapUgdNonBedah.get(data.no_rawat);

            Ranap_Paramedis = (mapRanapParamedisBilling.get(data.no_rawat) == null ? 0 : mapRanapParamedisBilling.get(data.no_rawat)) - Monitor;

            Ralan_Dokter = mapRalanDokterBilling.get(data.no_rawat) == null ? 0 : mapRalanDokterBilling.get(data.no_rawat);
            Ralan_Dokter_Paramedis = mapRalanDokterParamedisBilling.get(data.no_rawat) == null ? 0 : mapRalanDokterParamedisBilling.get(data.no_rawat);
            Ralan_Paramedis = mapRalanParamedisBilling.get(data.no_rawat) == null ? 0 : mapRalanParamedisBilling.get(data.no_rawat);

            Tambahan = mapTambahanBilling.get(data.no_rawat) == null ? 0 : mapTambahanBilling.get(data.no_rawat);
            Potongan = mapPotonganBilling.get(data.no_rawat) == null ? 0 : mapPotonganBilling.get(data.no_rawat);
            Kamar = mapKamarBilling.get(data.no_rawat) == null ? 0 : mapKamarBilling.get(data.no_rawat);
            Harian = mapHarianBilling.get(data.no_rawat) == null ? 0 : mapHarianBilling.get(data.no_rawat);
            UgdBedah = mapUgdBedah.get(data.no_rawat) == null ? 0 : mapUgdBedah.get(data.no_rawat);
            USGK = mapUSGK.get(data.no_rawat) == null ? 0 : mapUSGK.get(data.no_rawat);
            Retur_Obat = mapReturObatBilling.get(data.no_rawat) == null ? 0 : mapReturObatBilling.get(data.no_rawat);
            Resep_Pulang = mapResepPulangBilling.get(data.no_rawat) == null ? 0 : mapResepPulangBilling.get(data.no_rawat);
            Service = mapServiceBilling.get(data.no_rawat) == null ? 0 : mapServiceBilling.get(data.no_rawat);

            double totalBaris =
                    Laborat + Radiologi + Diagnostik + Operasi + Anastesi + KamarBedah + TimOK + Obat +
                    Ranap_Dokter + Ranap_Dokter_Paramedis + Ranap_Paramedis + Ralan_Dokter + Ralan_Dokter_Paramedis + Ralan_Paramedis +
                    Tambahan + Potongan + Kamar + Registrasi + Harian + UgdBedah + UgdNonBedah + Monitor + Oksigen +
                    Urs + Mikroskop + Telescope + MonitorOk + Deposit + Dialisis + Tsaraf + RehabMedik + Eswl + Ambulans +
                    USGK + Spirometri + Retur_Obat + Resep_Pulang + Service + Visite + Kemo + Dr_persalinan + Prwt_persalinan + Kmr_persalinan + Echo_Doopler;

            if (totalBaris > 0) {
                Keterangan = "Sudah Bayar";
            }

            ttlLaborat = ttlLaborat + Laborat;
            ttlDiagnostik = ttlDiagnostik + (mapDiagnostikTotal.get(data.no_rawat) == null ? 0 : mapDiagnostikTotal.get(data.no_rawat));
            ttlRadiologi = ttlRadiologi + Radiologi;
            ttlOperasi = ttlOperasi + Operasi;
            ttlAnastesi = ttlAnastesi + Anastesi;
            ttlKamarBedah = ttlKamarBedah + KamarBedah;
            ttlTimOK = ttlTimOK + TimOK;
            ttlObat = ttlObat + Obat;
            ttlRegistrasi = ttlRegistrasi + (mapRegistrasiTotal.get(data.no_rawat) == null ? 0 : mapRegistrasiTotal.get(data.no_rawat));
            ttlOksigen = ttlOksigen + Oksigen;
            ttlUrs = ttlUrs + (mapUrsTotal.get(data.no_rawat) == null ? 0 : mapUrsTotal.get(data.no_rawat));
            ttlMikroskop = ttlMikroskop + (mapMikroskopTotal.get(data.no_rawat) == null ? 0 : mapMikroskopTotal.get(data.no_rawat));
            ttlTelescope = ttlTelescope + (mapTelescopeTotal.get(data.no_rawat) == null ? 0 : mapTelescopeTotal.get(data.no_rawat));
            ttlMonitorOk = ttlMonitorOk + (mapMonitorOkTotal.get(data.no_rawat) == null ? 0 : mapMonitorOkTotal.get(data.no_rawat));
            ttlDeposit = ttlDeposit + (mapDepositTotal.get(data.no_rawat) == null ? 0 : mapDepositTotal.get(data.no_rawat));
            ttlTsaraf = ttlTsaraf + (mapTsarafTotal.get(data.no_rawat) == null ? 0 : mapTsarafTotal.get(data.no_rawat));
            ttlSpirometri = ttlSpirometri + Spirometri;
            ttlKemo = ttlKemo + Kemo;
            ttlDr_persalinan = ttlDr_persalinan + Dr_persalinan;
            ttlPrwt_persalinan = ttlPrwt_persalinan + Prwt_persalinan;
            ttlKmr_persalinan = ttlKmr_persalinan + Kmr_persalinan;
            ttlEcho_Doopler = ttlEcho_Doopler + Echo_Doopler;
            ttlRanap_Dokter = ttlRanap_Dokter + Ranap_Dokter;
            ttlVisite = ttlVisite + Visite;
            ttlRanap_Paramedis = ttlRanap_Dokter + Ranap_Dokter_Paramedis; // ikuti source asli
            ttlMonitor = ttlMonitor + Monitor;
            ttlDialisis = ttlDialisis + Dialisis;
            ttlRehabMedik = ttlRehabMedik + RehabMedik;
            ttlAmbulans = ttlAmbulans + Ambulans;
            ttlEswl = ttlEswl + Eswl;
            ttlUgdNonBedah = ttlUgdNonBedah + UgdNonBedah;
            ttlRanap_Paramedis = ttlRanap_Paramedis + Ranap_Paramedis; // ikuti source asli
            ttlRalan_Dokter = ttlRalan_Dokter + Ralan_Dokter;
            ttlRalan_Dokter = ttlRalan_Dokter + Ralan_Dokter_Paramedis; // ikuti source asli
            ttlRalan_Paramedis = ttlRalan_Paramedis + Ralan_Paramedis;
            ttlTambahan = ttlTambahan + Tambahan;
            ttlPotongan = ttlPotongan + Potongan;
            ttlKamar = ttlKamar + Kamar;
            ttlHarian = ttlHarian + Harian;
            ttlUgdBedah = ttlUgdBedah + UgdBedah;
            ttlUSGK = ttlUSGK + USGK;
            ttlRetur_Obat = ttlRetur_Obat + Retur_Obat;
            ttlResep_Pulang = ttlResep_Pulang + Resep_Pulang;
            ttlService = ttlService + Service;

            all = all + (totalBaris - Echo_Doopler);

            if (tampilkan.equals("Belum Bayar") && Keterangan.equals("Belum Bayar")) {
                tabMode.addRow(new Object[]{
                    data.tgl_keluar, data.no_nota,
                    data.no_rkm_medis, data.nm_pasien, "", data.kd_kamar + " " + data.nm_bangsal,
                    Valid.SetAngka(Registrasi),
                    Valid.SetAngka(Ranap_Dokter + Ralan_Dokter),
                    Valid.SetAngka(Visite),
                    Valid.SetAngka(Ranap_Dokter_Paramedis + Ranap_Paramedis + Ralan_Dokter_Paramedis + Ralan_Paramedis),
                    Valid.SetAngka(Obat), Valid.SetAngka(Retur_Obat), Valid.SetAngka(Resep_Pulang), Valid.SetAngka(Laborat), Valid.SetAngka(Radiologi), Valid.SetAngka(Diagnostik), Valid.SetAngka(Potongan),
                    Valid.SetAngka(Tambahan), Valid.SetAngka(Kamar + Service), Valid.SetAngka(Operasi), Valid.SetAngka(Anastesi), Valid.SetAngka(KamarBedah), Valid.SetAngka(TimOK), Valid.SetAngka(Harian), Valid.SetAngka(UgdBedah), Valid.SetAngka(UgdNonBedah), Valid.SetAngka(Monitor), Valid.SetAngka(Oksigen), Valid.SetAngka(Urs), Valid.SetAngka(Mikroskop), Valid.SetAngka(Telescope), Valid.SetAngka(MonitorOk), Valid.SetAngka(Dialisis), Valid.SetAngka(Tsaraf), Valid.SetAngka(RehabMedik), Valid.SetAngka(Eswl), Valid.SetAngka(Ambulans), Valid.SetAngka(USGK), Valid.SetAngka(Spirometri), Valid.SetAngka(Kemo), Valid.SetAngka(Dr_persalinan), Valid.SetAngka(Prwt_persalinan), Valid.SetAngka(Kmr_persalinan), Valid.SetAngka(Echo_Doopler), Valid.SetAngka(totalBaris), Valid.SetAngka(Deposit), Valid.SetAngka(totalBaris - Deposit)
                });
            } else if (tampilkan.equals("Sudah Bayar") && Keterangan.equals("Sudah Bayar")) {
                tabMode.addRow(new Object[]{
                    data.tgl_keluar, data.no_nota,
                    data.no_rkm_medis, data.nm_pasien, data.tgl_lunas + " - " + data.cara_bayar, data.kd_kamar + " " + data.nm_bangsal,
                    Valid.SetAngka(Registrasi),
                    Valid.SetAngka(Ranap_Dokter + Ralan_Dokter),
                    Valid.SetAngka(Visite),
                    Valid.SetAngka(Ranap_Dokter_Paramedis + Ranap_Paramedis + Ralan_Dokter_Paramedis + Ralan_Paramedis),
                    Valid.SetAngka(Obat), Valid.SetAngka(Retur_Obat), Valid.SetAngka(Resep_Pulang), Valid.SetAngka(Laborat), Valid.SetAngka(Radiologi), Valid.SetAngka(Diagnostik), Valid.SetAngka(Potongan),
                    Valid.SetAngka(Tambahan), Valid.SetAngka(Kamar + Service), Valid.SetAngka(Operasi), Valid.SetAngka(Anastesi), Valid.SetAngka(KamarBedah), Valid.SetAngka(TimOK), Valid.SetAngka(Harian), Valid.SetAngka(UgdBedah), Valid.SetAngka(UgdNonBedah), Valid.SetAngka(Monitor), Valid.SetAngka(Oksigen), Valid.SetAngka(Urs), Valid.SetAngka(Mikroskop), Valid.SetAngka(Telescope), Valid.SetAngka(MonitorOk), Valid.SetAngka(Dialisis), Valid.SetAngka(Tsaraf), Valid.SetAngka(RehabMedik), Valid.SetAngka(Eswl), Valid.SetAngka(Ambulans), Valid.SetAngka(USGK), Valid.SetAngka(Spirometri), Valid.SetAngka(Kemo), Valid.SetAngka(Dr_persalinan), Valid.SetAngka(Prwt_persalinan), Valid.SetAngka(Kmr_persalinan), Valid.SetAngka(Echo_Doopler), Valid.SetAngka(totalBaris), Valid.SetAngka(Deposit), Valid.SetAngka(totalBaris - Deposit)
                });
            } else if (tampilkan.equals("Semua")) {
                tabMode.addRow(new Object[]{
                    data.tgl_keluar, data.no_nota,
                    data.no_rkm_medis, data.nm_pasien, "", data.kd_kamar + " " + data.nm_bangsal,
                    Valid.SetAngka(Registrasi),
                    Valid.SetAngka(Ranap_Dokter + Ralan_Dokter),
                    Valid.SetAngka(Visite),
                    Valid.SetAngka(Ranap_Dokter_Paramedis + Ranap_Paramedis + Ralan_Dokter_Paramedis + Ralan_Paramedis),
                    Valid.SetAngka(Obat), Valid.SetAngka(Retur_Obat), Valid.SetAngka(Resep_Pulang), Valid.SetAngka(Laborat), Valid.SetAngka(Radiologi), Valid.SetAngka(Diagnostik), Valid.SetAngka(Potongan),
                    Valid.SetAngka(Tambahan), Valid.SetAngka(Kamar + Service), Valid.SetAngka(Operasi), Valid.SetAngka(Anastesi), Valid.SetAngka(KamarBedah), Valid.SetAngka(TimOK), Valid.SetAngka(Harian), Valid.SetAngka(UgdBedah), Valid.SetAngka(UgdNonBedah), Valid.SetAngka(Monitor), Valid.SetAngka(Oksigen), Valid.SetAngka(Urs), Valid.SetAngka(Mikroskop), Valid.SetAngka(Telescope), Valid.SetAngka(MonitorOk), Valid.SetAngka(Dialisis), Valid.SetAngka(Tsaraf), Valid.SetAngka(RehabMedik), Valid.SetAngka(Eswl), Valid.SetAngka(Ambulans), Valid.SetAngka(USGK), Valid.SetAngka(Spirometri), Valid.SetAngka(Kemo), Valid.SetAngka(Dr_persalinan), Valid.SetAngka(Prwt_persalinan), Valid.SetAngka(Kmr_persalinan), Valid.SetAngka(Echo_Doopler), Valid.SetAngka(totalBaris), Valid.SetAngka(Deposit), Valid.SetAngka(totalBaris - Deposit)
                });
            }
        }

        LCount2.setText("" + tabMode.getRowCount());
        if (!tampilkan.equals("Belum Bayar")) {
            tabMode.addRow(new Object[]{
                ">> Total ", ":", "", "", "", "",
                Valid.SetAngka(ttlRegistrasi), Valid.SetAngka(ttlRanap_Dokter + ttlRalan_Dokter), Valid.SetAngka(ttlVisite), Valid.SetAngka(ttlRanap_Paramedis + ttlRalan_Paramedis),
                Valid.SetAngka(ttlObat), Valid.SetAngka(ttlRetur_Obat), Valid.SetAngka(ttlResep_Pulang), Valid.SetAngka(ttlLaborat), Valid.SetAngka(ttlRadiologi), Valid.SetAngka(ttlDiagnostik), Valid.SetAngka(ttlPotongan),
                Valid.SetAngka(ttlTambahan), Valid.SetAngka(ttlKamar + ttlService), Valid.SetAngka(ttlOperasi), Valid.SetAngka(ttlAnastesi), Valid.SetAngka(ttlKamarBedah), Valid.SetAngka(ttlTimOK), Valid.SetAngka(ttlHarian), Valid.SetAngka(ttlUgdBedah), Valid.SetAngka(ttlUgdNonBedah), Valid.SetAngka(ttlMonitor), Valid.SetAngka(ttlOksigen), Valid.SetAngka(ttlUrs), Valid.SetAngka(ttlMikroskop), Valid.SetAngka(ttlTelescope), Valid.SetAngka(ttlMonitorOk), Valid.SetAngka(ttlDialisis), Valid.SetAngka(ttlTsaraf), Valid.SetAngka(ttlRehabMedik), Valid.SetAngka(ttlEswl), Valid.SetAngka(ttlAmbulans), Valid.SetAngka(ttlUSGK), Valid.SetAngka(ttlSpirometri), Valid.SetAngka(ttlKemo), Valid.SetAngka(ttlDr_persalinan), Valid.SetAngka(ttlPrwt_persalinan), Valid.SetAngka(ttlKmr_persalinan), Valid.SetAngka(ttlEcho_Doopler), Valid.SetAngka(all), Valid.SetAngka(ttlDeposit), Valid.SetAngka(all - ttlDeposit)
            });
            LCount.setText(Valid.SetAngka(all));
        } else {
            LCount.setText("" + 0);
        }

    } catch (Exception e) {
        System.out.println("Notif 1 : " + e);
    } finally {
        try {
            if (rsBatch != null) rsBatch.close();
        } catch (Exception e) {
            System.out.println("Notif Batch RS : " + e);
        }
        try {
            if (psBatch != null) psBatch.close();
        } catch (Exception e) {
            System.out.println("Notif Batch PS : " + e);
        }
        try {
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Notif RS : " + e);
        }
        try {
            if (ps != null) ps.close();
        } catch (Exception e) {
            System.out.println("Notif PS : " + e);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }
}

    private void getData() {
        int row=tbBangsal.getSelectedRow();
        if(row!= -1){
            TKd.setText(tabMode.getValueAt(row,1).toString());
        }
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,65));
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
