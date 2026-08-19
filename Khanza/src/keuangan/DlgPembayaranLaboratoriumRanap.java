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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import simrskhanza.DlgCariPoli;
import simrskhanza.DlgCariCaraBayar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumnModel;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.FileOutputStream;
import java.awt.Desktop;


/**
 *
 * @author perpustakaan
 */
public final class DlgPembayaranLaboratoriumRanap extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private DlgCariPoli poli=new DlgCariPoli(null,false);
    private DlgCariCaraBayar penjab=new DlgCariCaraBayar(null,false);
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private double all=0,Laborat=0,Radiologi=0,Obat=0,Ralan_Dokter=0,Ralan_Dokter_paramedis=0,Ralan_Paramedis=0,Tambahan=0,Potongan=0,Registrasi=0,Echo=0,
                    ttlLaborat=0,ttlRadiologi=0,ttlObat=0,ttlRalan_Dokter=0,ttlRalan_Paramedis=0,ttlTambahan=0,ttlPotongan=0,ttlRegistrasi=0,ttlEcho=0,
                   Operasi=0,ttlOperasi=0;
    private String Keterangan="Belum Lunas",pilihan="",tampilkan="Semua";
    private StringBuilder htmlContent;
    private int i=0;

    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgPembayaranLaboratoriumRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        // ===== HEADER: 10 kolom termasuk "Laboratorium" =====
        Object[] kolom = {
            "Tgl. Inap",
            "No. Nota",
            "No. RM",
            "Nama Pasien",
            "Tgl. Bayar",
            "Ruangan",
            "Laboratorium",
            "Cara Bayar",
            "Dokter",
            "Status Bayar"
        };

        tabMode = new DefaultTableModel(null, kolom){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){ return false; }
        };
        tbBangsal.setModel(tabMode);

        tbBangsal.getTableHeader().setReorderingAllowed(false);
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // ===== SET LEBAR: dinamis berdasar NAMA kolom (tidak pakai index angka) =====
        TableColumnModel cm = tbBangsal.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
            TableColumn column = cm.getColumn(i);
            String name = String.valueOf(column.getHeaderValue()).trim();

            if ("Tgl. Ralan".equalsIgnoreCase(name)) {
                column.setPreferredWidth(90);
            } else if ("No. Nota".equalsIgnoreCase(name)) {
                column.setPreferredWidth(120);
            } else if ("No. RM".equalsIgnoreCase(name)) {
                column.setPreferredWidth(70);
            } else if ("Nama Pasien".equalsIgnoreCase(name)) {
                column.setPreferredWidth(220);
            } else if ("Tgl. Bayar".equalsIgnoreCase(name)) {
                column.setPreferredWidth(130);
            } else if ("Ruangan".equalsIgnoreCase(name)) {
                column.setPreferredWidth(200);
            } else if ("Laboratorium".equalsIgnoreCase(name)) {
                column.setPreferredWidth(110);
            } else if ("Cara Bayar".equalsIgnoreCase(name)) {
                column.setPreferredWidth(120);
            } else if ("Dokter".equalsIgnoreCase(name)) {
                column.setPreferredWidth(250);
            } else if ("Status Bayar".equalsIgnoreCase(name)) {
                column.setPreferredWidth(120);
            }
        }
        
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());
        
        // default untuk non-numerik (kiri + padding)
        class WarnaPadLeft extends WarnaTable {
            private final Border pad = new EmptyBorder(0,6,0,6);
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
            private final Border pad = new EmptyBorder(0,6,0,6);
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
            private final Border pad = new EmptyBorder(0,6,0,6);
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
        for (int col = 6; col <= 6; col++) {
            tbBangsal.getColumnModel().getColumn(col).setCellRenderer(padRight);
        }

        // 3) kolom center: No.(0), Tgl. Ralan(1), No. Nota(2), No. RM(3), Status Bayar(35)
        WarnaPadCenter padCenter = new WarnaPadCenter();
        int[] centerCols = {0,1,2,4,9};
        for (int col : centerCols) {
            tbBangsal.getColumnModel().getColumn(col).setCellRenderer(padCenter);
        }

        // (opsional) baris sedikit lebih tinggi biar lega
        tbBangsal.setRowHeight(30);
        
        TKd.setDocument(new batasInput((byte)20).getKata(TKd));
        
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
                }      
                BtnCaraBayar.requestFocus();
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
        
        poli.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    poli.dispose();
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
                    KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    BtnDokter.requestFocus();
                }      
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
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
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
        
        KdCaraBayar.setText("001");        
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

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnBilling = new javax.swing.JMenuItem();
        MnSudahBayar = new javax.swing.JMenuItem();
        MnBelumBayar = new javax.swing.JMenuItem();
        MnSemuaStatusBayar = new javax.swing.JMenuItem();
        TKd = new widget.TextBox();
        KdDokter = new widget.TextBox();
        KdPoli = new widget.TextBox();
        KdCaraBayar = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
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
        label17 = new widget.Label();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        label19 = new widget.Label();
        NmCaraBayar = new widget.TextBox();
        BtnCaraBayar = new widget.Button();
        label20 = new widget.Label();
        NmPoli = new widget.TextBox();
        BtnPoli = new widget.Button();

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

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.setPreferredSize(new java.awt.Dimension(80, 23));

        KdPoli.setEditable(false);
        KdPoli.setName("KdPoli"); // NOI18N
        KdPoli.setPreferredSize(new java.awt.Dimension(50, 23));

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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Pembayaran Laboratorium Pasien Umum ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

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
        panelGlass5.setPreferredSize(new java.awt.Dimension(100, 43));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(53, 23));
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

        label9.setText("Key Word :");
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(68, 23));
        panelGlass5.add(label9);

        TCari.setToolTipText("Alt+C");
        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(110, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass5.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setText("Cari ");
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setIconTextGap(10);
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(70, 23));
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
        BtnAll.setText("Tampilkan Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setIconTextGap(10);
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(140, 23));
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
        jLabel10.setText("Total :");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(40, 23));
        panelGlass5.add(jLabel10);

        LCount.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        LCount.setForeground(new java.awt.Color(50, 50, 50));
        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(110, 23));
        panelGlass5.add(LCount);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak ");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setIconTextGap(10);
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(85, 23));
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
        BtnKeluar.setIconTextGap(10);
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(85, 23));
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

        label17.setText("Dokter :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(50, 23));
        FormInput.add(label17);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        NmDokter.setPreferredSize(new java.awt.Dimension(170, 23));
        FormInput.add(NmDokter);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('3');
        BtnDokter.setToolTipText("Alt+3");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        FormInput.add(BtnDokter);

        label19.setText("Cara Bayar :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(85, 23));
        FormInput.add(label19);

        NmCaraBayar.setEditable(false);
        NmCaraBayar.setName("NmCaraBayar"); // NOI18N
        NmCaraBayar.setPreferredSize(new java.awt.Dimension(170, 23));
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

        label20.setText("Unit/Poli :");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(75, 23));
        FormInput.add(label20);

        NmPoli.setEditable(false);
        NmPoli.setName("NmPoli"); // NOI18N
        NmPoli.setPreferredSize(new java.awt.Dimension(170, 23));
        FormInput.add(NmPoli);

        BtnPoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli.setMnemonic('3');
        BtnPoli.setToolTipText("Alt+3");
        BtnPoli.setName("BtnPoli"); // NOI18N
        BtnPoli.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPoliActionPerformed(evt);
            }
        });
        FormInput.add(BtnPoli);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
                                   
    if (tabMode.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null,
            "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
        BtnPrint.requestFocus();
        return;
    }

    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try {
        // === Halaman: 8.5in x 13in (landscape), margin 1 cm ===
        final float PT_PER_INCH = 72f;
        final float CM_TO_PT = 72f / 2.54f; // 1 cm = 28.3465 pt
        final float MARGIN = 1.0f * CM_TO_PT; // 1 cm

        com.lowagie.text.Rectangle pageSize =
            new com.lowagie.text.Rectangle(8.5f * PT_PER_INCH, 13f * PT_PER_INCH).rotate();
        com.lowagie.text.Document doc = new com.lowagie.text.Document(pageSize, MARGIN, MARGIN, MARGIN, MARGIN);

        java.io.File out = new java.io.File("RekapPembayaranLaboratorium.pdf");
        com.lowagie.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(out));
        doc.open();

        // === Judul (tanpa kop RS) ===
        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(
            "REKAP PEMBAYARAN PASIEN LABORATORIUM RAWAT INAP \n PERIODE " + Tgl1.getSelectedItem() + " s.d. " + Tgl2.getSelectedItem(),
            com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 11)
        );
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(8f);
        doc.add(title);

        // === Tabel: 1 kolom No. + semua kolom JTable ===
        javax.swing.table.TableColumnModel cm = tbBangsal.getColumnModel();
        int colCount = cm.getColumnCount();       // ekspektasi 10
        int totalCols = colCount + 1;             // + kolom "No."
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(totalCols);
        table.setWidthPercentage(100f);
        // >>> PENTING: cegah baris terbelah antar halaman
        table.setSplitRows(false);   // jangan pernah split satu baris
        table.setSplitLate(true);    // kalau tidak muat, pindahkan baris utuh ke halaman berikutnya

        // Lebar relatif kolom (No. Nota diperlebar, Cara/Status dipersempit)
        float[] widths = new float[totalCols];
        widths[0] = 4f; // No.
        for (int c = 0; c < colCount; c++) {
            String h = String.valueOf(cm.getColumn(c).getHeaderValue());
            String hl = h.toLowerCase();
            float w = 10f; // default

            if (hl.contains("tgl"))                               w = 9f;
            else if (h.equalsIgnoreCase("No. Nota"))              w = 14f; // lebar
            else if (h.equalsIgnoreCase("No. RM"))                w = 8f;
            else if (hl.contains("nama"))                         w = 18f;
            else if (h.equalsIgnoreCase("Tgl. Bayar"))            w = 10f;
            else if (hl.contains("unit") || h.equalsIgnoreCase("Bangsal")) w = 15f;
            else if (h.equalsIgnoreCase("Laboratorium"))          w = 10f;
            else if (h.equalsIgnoreCase("Cara Bayar"))            w = 8f;  // kecil
            else if (h.equalsIgnoreCase("Dokter"))                w = 18f;
            else if (hl.contains("status"))                       w = 8f;  // kecil

            widths[c + 1] = w;
        }
        table.setWidths(widths);

        // === Header tabel (diulang di tiap halaman) ===
        com.lowagie.text.Font fHeader = com.lowagie.text.FontFactory.getFont(
            com.lowagie.text.FontFactory.HELVETICA_BOLD, 9);

        com.lowagie.text.pdf.PdfPCell hNo = new com.lowagie.text.pdf.PdfPCell(
            new com.lowagie.text.Phrase("No.", fHeader));
        hNo.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        hNo.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
        hNo.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        hNo.setPadding(4f);
        table.addCell(hNo);

        for (int c = 0; c < colCount; c++) {
            String h = String.valueOf(cm.getColumn(c).getHeaderValue());
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(h, fHeader));
            cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            cell.setPadding(4f);
            if (h.equalsIgnoreCase("No. Nota")) {
                cell.setNoWrap(true); // paksa 1 baris di header
            }
            table.addCell(cell);
        }
        table.setHeaderRows(1); // ulang header di tiap halaman

        // === Isi data ===
        com.lowagie.text.Font fCell = com.lowagie.text.FontFactory.getFont(
            com.lowagie.text.FontFactory.HELVETICA, 9);

        for (int r = 0; r < tabMode.getRowCount(); r++) {
            // Kolom No.
            com.lowagie.text.pdf.PdfPCell cNo = new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(String.valueOf(r + 1), fCell));
            cNo.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            cNo.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
            cNo.setPadding(4f);
            table.addCell(cNo);

            for (int c = 0; c < colCount; c++) {
                Object val = tabMode.getValueAt(r, c);
                String text = (val == null) ? "" : val.toString();

                String h = String.valueOf(cm.getColumn(c).getHeaderValue());
                String hl = h.toLowerCase();
                int align = com.lowagie.text.Element.ALIGN_LEFT;
                if (h.equalsIgnoreCase("Laboratorium")) align = com.lowagie.text.Element.ALIGN_RIGHT;
                if (h.equalsIgnoreCase("No. RM") || hl.contains("tgl") || hl.contains("status"))
                    align = com.lowagie.text.Element.ALIGN_CENTER;

                com.lowagie.text.pdf.PdfPCell cData = new com.lowagie.text.pdf.PdfPCell(
                    new com.lowagie.text.Phrase(text, fCell));
                cData.setHorizontalAlignment(align);
                cData.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                cData.setPadding(4f);

                if (h.equalsIgnoreCase("No. Nota")) {
                    cData.setNoWrap(true); // jangan pecah baris
                    cData.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                }

                table.addCell(cData);
            }
        }

        doc.add(table);
        doc.close();

        // Buka PDF
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().open(out);
        } else {
            java.awt.Desktop.getDesktop().browse(out.toURI());
        }

    } catch (Exception e) {
        System.out.println("Notif cetak PDF: " + e);
        JOptionPane.showMessageDialog(null, "Gagal mencetak PDF: " + e.getMessage());
    } finally {
        this.setCursor(Cursor.getDefaultCursor());
    }

    }//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, Tgl2,BtnKeluar);
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

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            tampil();
            this.setCursor(Cursor.getDefaultCursor());
        }else{
            Valid.pindah(evt, TKd, BtnPrint);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        KdCaraBayar.setText("");
        NmCaraBayar.setText("");
        KdPoli.setText("");
        NmPoli.setText("");
        KdDokter.setText("");
        NmDokter.setText("");
        TCari.setText("");
        tampilkan="Semua";
        tampil();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCaraBayar, BtnPrint);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void MnBillingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnBillingActionPerformed
        if(TKd.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu dengan menklik data pada table...!!!");
        }else{
            DlgBilingRalan billing=new DlgBilingRalan(null,false);
            billing.TNoRw.setText(Sequel.cariIsi("select no_rawat from nota_jalan where no_nota=?",TKd.getText()));
            billing.isCek();
            billing.isRawat();
            if(Sequel.cariInteger("select count(no_rawat) from piutang_pasien where no_rawat=?",billing.TNoRw.getText())>0){
                billing.setPiutang();
            }
            billing.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            billing.setLocationRelativeTo(internalFrame1);
            billing.setVisible(true);         
        }
    }//GEN-LAST:event_MnBillingActionPerformed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnCaraBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCaraBayarActionPerformed
        penjab.isCek();
        penjab.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penjab.setLocationRelativeTo(internalFrame1);
        penjab.setAlwaysOnTop(false);
        penjab.setVisible(true);
    }//GEN-LAST:event_BtnCaraBayarActionPerformed

    private void BtnPoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoliActionPerformed
        poli.isCek();
        poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        poli.setLocationRelativeTo(internalFrame1);
        poli.setAlwaysOnTop(false);
        poli.setVisible(true);
    }//GEN-LAST:event_BtnPoliActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbBangsal.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

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
            DlgPembayaranLaboratoriumRanap dialog = new DlgPembayaranLaboratoriumRanap(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnKeluar;
    private widget.Button BtnPoli;
    private widget.Button BtnPrint;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private widget.TextBox KdCaraBayar;
    private widget.TextBox KdDokter;
    private widget.TextBox KdPoli;
    private javax.swing.JLabel LCount;
    private widget.Label LCount2;
    private javax.swing.JMenuItem MnBelumBayar;
    private javax.swing.JMenuItem MnBilling;
    private javax.swing.JMenuItem MnSemuaStatusBayar;
    private javax.swing.JMenuItem MnSudahBayar;
    private widget.TextBox NmCaraBayar;
    private widget.TextBox NmDokter;
    private widget.TextBox NmPoli;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.Label label10;
    private widget.Label label11;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.Label label9;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    // End of variables declaration//GEN-END:variables

//    public void tampil(){
//        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
//        Valid.tabelKosong(tabMode);
//        try{      
//            if(NmPoli.getText().equals("")&&NmCaraBayar.getText().equals("")&&NmDokter.getText().equals("")&&TCari.getText().equals("")){
//                ps= koneksi.prepareStatement(
//                    "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,reg_periksa.tgl_registrasi,dokter.nm_dokter,poliklinik.nm_poli "+
//                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
//                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
//                    "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
//                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
//                    "where reg_periksa.status_lanjut='Ralan' and reg_periksa.no_rawat not in (select piutang_pasien.no_rawat from piutang_pasien where piutang_pasien.no_rawat=reg_periksa.no_rawat) and "+
//                    "reg_periksa.tgl_registrasi between ? and ? order by reg_periksa.kd_dokter,reg_periksa.tgl_registrasi");
//            }else{
//                ps= koneksi.prepareStatement(
//                    "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,reg_periksa.tgl_registrasi,dokter.nm_dokter,poliklinik.nm_poli "+
//                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
//                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
//                    "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
//                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
//                    "where reg_periksa.status_lanjut='Ralan' and reg_periksa.no_rawat not in (select piutang_pasien.no_rawat from piutang_pasien where piutang_pasien.no_rawat=reg_periksa.no_rawat) and "+
//                    "reg_periksa.tgl_registrasi between ? and ? and concat(reg_periksa.kd_poli,poliklinik.nm_poli) like ? and concat(reg_periksa.kd_dokter,dokter.nm_dokter) like ? and concat(reg_periksa.kd_pj,penjab.png_jawab) like ? "+
//                    "and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ?) order by reg_periksa.kd_dokter,reg_periksa.tgl_registrasi");
//            }
//                
//            try {
//                if(NmPoli.getText().equals("")&&NmCaraBayar.getText().equals("")&&NmDokter.getText().equals("")&&TCari.getText().equals("")){
//                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                }else{
//                    ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
//                    ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
//                    ps.setString(3,"%"+KdPoli.getText()+NmPoli.getText()+"%");
//                    ps.setString(4,"%"+KdDokter.getText()+NmDokter.getText()+"%");
//                    ps.setString(5,"%"+KdCaraBayar.getText()+NmCaraBayar.getText()+"%");
//                    ps.setString(6,"%"+TCari.getText().trim()+"%");
//                    ps.setString(7,"%"+TCari.getText().trim()+"%");
//                    ps.setString(8,"%"+TCari.getText().trim()+"%");
//                }
//                    
//                rs=ps.executeQuery();
//                all=0;
//                ttlLaborat=0;ttlRadiologi=0;ttlOperasi=0;ttlObat=0;ttlRalan_Dokter=0;ttlRalan_Paramedis=0;ttlTambahan=0;ttlPotongan=0;ttlRegistrasi=0;ttlEcho=0;
//                while(rs.next()){
//                    Operasi=0;Laborat=0;Radiologi=0;Obat=0;Ralan_Dokter=0;Ralan_Dokter_paramedis=0;Ralan_Paramedis=0;Tambahan=0;Potongan=0;Registrasi=0;Echo=0;
//                    Keterangan="Belum Bayar";
//                    ps2=koneksi.prepareStatement(
//                        "select billing.nm_perawatan,billing.totalbiaya,billing.status from billing where billing.no_rawat=? ");
//                    try{
//                        ps2.setString(1,rs.getString("no_rawat"));
//                        rs2=ps2.executeQuery();                
//                        while(rs2.next()){
//                            switch (rs2.getString("status")) {
//                                case "Laborat":
//                                    ttlLaborat=ttlLaborat+rs2.getDouble("totalbiaya");
//                                    Laborat=Laborat+rs2.getDouble("totalbiaya");
//                                    break;
//                                case "Radiologi":
//                                    ttlRadiologi=ttlRadiologi+rs2.getDouble("totalbiaya");
//                                    Radiologi=Radiologi+rs2.getDouble("totalbiaya");
//                                    break;
//                                case "Obat":
//                                    ttlObat=ttlObat+rs2.getDouble("totalbiaya");
//                                    Obat=Obat+rs2.getDouble("totalbiaya");
//                                    break;
//                                case "Ralan Dokter":
//                                    ttlRalan_Dokter=ttlRalan_Dokter+rs2.getDouble("totalbiaya");
//                                    Ralan_Dokter=Ralan_Dokter+rs2.getDouble("totalbiaya");
//                                    break;     
//                                case "Ralan Dokter Paramedis":
//                                    ttlRalan_Dokter=ttlRalan_Dokter+rs2.getDouble("totalbiaya");
//                                    Ralan_Dokter_paramedis=Ralan_Dokter_paramedis+rs2.getDouble("totalbiaya");
//                                    break;    
//                                case "Ralan Paramedis":
//                                    ttlRalan_Paramedis=ttlRalan_Paramedis+rs2.getDouble("totalbiaya");
//                                    Ralan_Paramedis=Ralan_Paramedis+rs2.getDouble("totalbiaya");
//                                    break;
//                                case "Tambahan":
//                                    ttlTambahan=ttlTambahan+rs2.getDouble("totalbiaya");
//                                    Tambahan=Tambahan+rs2.getDouble("totalbiaya");
//                                    break;
//                                case "Potongan":
//                                    ttlPotongan=ttlPotongan+rs2.getDouble("totalbiaya");
//                                    Potongan=Potongan+rs2.getDouble("totalbiaya");
//                                    break;
//                                case "Registrasi":
//                                    ttlRegistrasi=ttlRegistrasi+rs2.getDouble("totalbiaya");
//                                    Registrasi=Registrasi+rs2.getDouble("totalbiaya");
//                                    break;
//                                case "Operasi":
//                                    ttlOperasi=ttlOperasi+rs2.getDouble("totalbiaya");
//                                    Operasi=Operasi+rs2.getDouble("totalbiaya");
//                                    break;
//                                case "Echo Doopler":
//                                    ttlEcho=ttlEcho+rs2.getDouble("totalbiaya");
//                                    Echo=Echo+rs2.getDouble("totalbiaya");
//                                    break;    
//                            }                                
//                        }
//                    }catch(Exception e){
//                        System.out.println("Notif 2 : "+e);
//                    } finally{
//                        if(rs2!=null){
//                            rs2.close();
//                        }
//                        if(ps2!=null){
//                            ps2.close();
//                        }
//                    }
//                    all=all+Operasi+Laborat+Radiologi+Obat+Ralan_Dokter+Ralan_Dokter_paramedis+Ralan_Paramedis+Tambahan+Potongan+Registrasi+Echo;
//
//                    if((Laborat+Operasi+Radiologi+Obat+Ralan_Dokter+Ralan_Paramedis+Ralan_Dokter_paramedis+Tambahan+Potongan+Registrasi+Echo)>0){
//                        Keterangan="Sudah Bayar";
//                    }                
//
//                    if(tampilkan.equals("Belum Bayar")&&Keterangan.equals("Belum Bayar")){
//                        tabMode.addRow(new Object[] {
//                            rs.getString("tgl_registrasi"),
//                            Sequel.cariIsi("select no_nota from nota_jalan where no_rawat=?",rs.getString("no_rawat")),
//                            rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
//                            rs.getString("nm_poli"),
//                            Sequel.cariIsi("select perujuk from rujuk_masuk where no_rawat=?",rs.getString("no_rawat")),
//                            Valid.SetAngka(Registrasi),
//                            Valid.SetAngka(Obat),
//                            Valid.SetAngka(Ralan_Dokter+Ralan_Paramedis+Ralan_Dokter_paramedis),
//                            Valid.SetAngka(Operasi),
//                            Valid.SetAngka(Laborat),
//                            Valid.SetAngka(Radiologi),
//                            Valid.SetAngka(Echo),
//                            Valid.SetAngka(Tambahan),
//                            Valid.SetAngka(Potongan),
//                            Valid.SetAngka(Operasi+Laborat+Radiologi+Obat+Ralan_Dokter+Ralan_Paramedis+Ralan_Dokter_paramedis+Tambahan+Potongan+Registrasi+Echo),
//                            rs.getString("nm_dokter"),Keterangan
//                        });
//                    }else if(tampilkan.equals("Sudah Bayar")&&Keterangan.equals("Sudah Bayar")){
//                        tabMode.addRow(new Object[] {
//                            rs.getString("tgl_registrasi"),
//                            Sequel.cariIsi("select no_nota from nota_jalan where no_rawat=?",rs.getString("no_rawat")),
//                            rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
//                            rs.getString("nm_poli"),
//                            Sequel.cariIsi("select perujuk from rujuk_masuk where no_rawat=?",rs.getString("no_rawat")),
//                            Valid.SetAngka(Registrasi),
//                            Valid.SetAngka(Obat),
//                            Valid.SetAngka(Ralan_Dokter+Ralan_Paramedis+Ralan_Dokter_paramedis),
//                            Valid.SetAngka(Operasi),
//                            Valid.SetAngka(Laborat),
//                            Valid.SetAngka(Radiologi),
//                            Valid.SetAngka(Echo),
//                            Valid.SetAngka(Tambahan),
//                            Valid.SetAngka(Potongan),
//                            Valid.SetAngka(Operasi+Laborat+Radiologi+Obat+Ralan_Dokter+Ralan_Paramedis+Ralan_Dokter_paramedis+Tambahan+Potongan+Registrasi+Echo),
//                            rs.getString("nm_dokter"),Keterangan
//                        });
//                    }else if(tampilkan.equals("Semua")){
//                        tabMode.addRow(new Object[] {
//                            rs.getString("tgl_registrasi"),
//                            Sequel.cariIsi("select no_nota from nota_jalan where no_rawat=?",rs.getString("no_rawat")),
//                            rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
//                            rs.getString("nm_poli"),
//                            Sequel.cariIsi("select perujuk from rujuk_masuk where no_rawat=?",rs.getString("no_rawat")),
//                            Valid.SetAngka(Registrasi),
//                            Valid.SetAngka(Obat),
//                            Valid.SetAngka(Ralan_Dokter+Ralan_Paramedis+Ralan_Dokter_paramedis),
//                            Valid.SetAngka(Operasi),
//                            Valid.SetAngka(Laborat),
//                            Valid.SetAngka(Radiologi),
//                            Valid.SetAngka(Tambahan),
//                            Valid.SetAngka(Potongan),
//                            Valid.SetAngka(Operasi+Laborat+Radiologi+Obat+Ralan_Dokter+Ralan_Paramedis+Ralan_Dokter_paramedis+Tambahan+Potongan+Registrasi),
//                            rs.getString("nm_dokter"),Keterangan
//                        });
//                    }
//                }
//                
//                LCount2.setText(""+tabMode.getRowCount());
//                if(!tampilkan.equals("Belum Bayar")){
//                    tabMode.addRow(new Object[] {
//                                ">> Total",":","","","","",
//                                Valid.SetAngka(ttlRegistrasi),
//                                Valid.SetAngka(ttlObat),
//                                Valid.SetAngka(ttlRalan_Dokter+ttlRalan_Paramedis),
//                                Valid.SetAngka(ttlOperasi),
//                                Valid.SetAngka(ttlLaborat),
//                                Valid.SetAngka(ttlRadiologi),
//                                Valid.SetAngka(ttlEcho),
//                                Valid.SetAngka(ttlTambahan),
//                                Valid.SetAngka(ttlPotongan),
//                                Valid.SetAngka(ttlLaborat+ttlRadiologi+ttlObat+ttlRalan_Dokter+ttlRalan_Paramedis+
//                                        ttlTambahan+ttlPotongan+ttlRegistrasi+ttlOperasi+ttlEcho),"",""
//                    });
//                    LCount.setText(Valid.SetAngka(all));
//                }else{
//                    LCount.setText(""+0);
//                }    
//            } catch (Exception e) {
//                System.out.println("Notif 1 :"+e);
//            } finally{
//                if(rs!=null){
//                    rs.close();
//                }
//                if(ps!=null){
//                    ps.close();
//                }
//            }
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//        }
//        this.setCursor(Cursor.getDefaultCursor());
//    }

//public void tampil() {
//
//    tabMode.setRowCount(0);
//    try {
//        String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem() + "");
//        String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem() + "");
//
//        // --- tmp_rp: pakai tanggal dari tagihan_sadewa.tgl_bayar ---
//        try (Statement st = koneksi.createStatement()) {
//            st.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//            st.execute("CREATE TEMPORARY TABLE tmp_rp (no_rawat VARCHAR(17) PRIMARY KEY) ENGINE=MEMORY");
//        }
//
//        String insertTmp =
//            "INSERT IGNORE INTO tmp_rp(no_rawat) " +
//            "SELECT DISTINCT rp.no_rawat " +
//            "FROM reg_periksa rp " +
//            "JOIN tagihan_sadewa ts ON ts.no_nota = rp.no_rawat " +     // <-- RANGE tanggal dari sini
//            "JOIN kamar_inap ki ON ki.no_rawat = rp.no_rawat " +
//            "JOIN kamar k       ON k.kd_kamar  = ki.kd_kamar " +
//            "JOIN bangsal b     ON b.kd_bangsal= k.kd_bangsal " +
//            "LEFT JOIN dokter d ON d.kd_dokter = rp.kd_dokter " +
//            "LEFT JOIN penjab pj ON pj.kd_pj   = rp.kd_pj " +
//            "LEFT JOIN pasien p  ON p.no_rkm_medis = rp.no_rkm_medis " +
//            "WHERE rp.status_lanjut='Ranap' AND rp.status_bayar='Sudah Bayar' " +
//            "  AND ts.tgl_bayar BETWEEN ? AND ? " +                      // <-- pakai ts.tgl_bayar
//            "  AND CONCAT(b.kd_bangsal,b.nm_bangsal) LIKE ? " +
//            "  AND CONCAT(rp.kd_dokter,d.nm_dokter) LIKE ? " +
//            "  AND CONCAT(rp.kd_pj, pj.png_jawab)   LIKE ? " +
//            "  AND (rp.no_rawat LIKE ? OR rp.no_rkm_medis LIKE ? OR p.nm_pasien LIKE ?)";
//
//        try (PreparedStatement psTmp = koneksi.prepareStatement(insertTmp)) {
//            psTmp.setString(1, tgl1);
//            psTmp.setString(2, tgl2);
//            psTmp.setString(3, "%" + KdPoli.getText() + "%");     // dipakai utk filter bangsal
//            psTmp.setString(4, "%" + KdDokter.getText() + "%");
//            psTmp.setString(5, "%" + KdCaraBayar.getText() + "%");
//            psTmp.setString(6, "%" + TCari.getText() + "%");
//            psTmp.setString(7, "%" + TCari.getText() + "%");
//            psTmp.setString(8, "%" + TCari.getText() + "%");
//            psTmp.executeUpdate();
//        }
//
//        // --- Ambil data akhir: tgl_bayar dari tagihan_sadewa (agregat), tgl_masuk_min mengikuti ---
//        String sql =
//            "SELECT " +
//            "  km.tgl_masuk_min           AS tgl_ranap, " +
//            "  ni.no_nota                 AS no_nota, " +               // nota inap
//            "  rp.no_rkm_medis            AS no_rkm_medis, " +
//            "  p.nm_pasien                AS nm_pasien, " +
//            "  ts.tgl_bayar               AS tgl_bayar, " +             // tgl bayar dari tagihan_sadewa
//            "  b.nm_bangsal               AS nm_bangsal, " +
//            "  COALESCE(lab.lab_total,0)  AS laboratorium, " +
//            "  pj.png_jawab               AS cara_bayar, " +
//            "  d.nm_dokter                AS dokter, " +
//            "  rp.status_bayar            AS status_bayar " +
//            "FROM tmp_rp tr " +
//            "JOIN reg_periksa rp ON rp.no_rawat = tr.no_rawat " +
//            "JOIN pasien p       ON p.no_rkm_medis = rp.no_rkm_medis " +
//            // tgl masuk paling awal
//            "LEFT JOIN ( " +
//            "  SELECT no_rawat, MIN(tgl_masuk) AS tgl_masuk_min " +
//            "  FROM kamar_inap GROUP BY no_rawat " +
//            ") km ON km.no_rawat = rp.no_rawat " +
//            "LEFT JOIN kamar_inap ki ON ki.no_rawat = rp.no_rawat AND ki.tgl_masuk = km.tgl_masuk_min " +
//            "LEFT JOIN kamar k       ON k.kd_kamar = ki.kd_kamar " +
//            "LEFT JOIN bangsal b     ON b.kd_bangsal = k.kd_bangsal " +
//            // no_nota nota_inap (ambil satu—pakai MAX untuk aman)
//            "LEFT JOIN ( " +
//            "  SELECT no_rawat, MAX(no_nota) AS no_nota " +
//            "  FROM nota_inap GROUP BY no_rawat " +
//            ") ni ON ni.no_rawat = rp.no_rawat " +
//            // tgl_bayar dari tagihan_sadewa, 1 baris per no_rawat
//            "LEFT JOIN ( " +
//            "  SELECT no_nota, MAX(tgl_bayar) AS tgl_bayar " +
//            "  FROM tagihan_sadewa GROUP BY no_nota " +
//            ") ts ON ts.no_nota = rp.no_rawat " +
//            "LEFT JOIN dokter d ON d.kd_dokter = rp.kd_dokter " +
//            "LEFT JOIN penjab pj ON pj.kd_pj = rp.kd_pj " +
//            "LEFT JOIN ( " +
//            "   SELECT dpl.no_rawat, SUM(dpl.biaya_item) AS lab_total " +
//            "   FROM detail_periksa_lab dpl GROUP BY dpl.no_rawat " +
//            ") lab ON lab.no_rawat = rp.no_rawat " +
//            "WHERE COALESCE(lab.lab_total,0) > 0 " +
//            "GROUP BY rp.no_rawat " +
//            "ORDER BY ts.tgl_bayar ASC, km.tgl_masuk_min ASC";
//
//        final java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
//
//        try (PreparedStatement ps = koneksi.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                Object[] row = new Object[10];
//                int c = 0;
//                row[c++] = rs.getString("tgl_ranap");                 // Tgl. Ranap (min tgl_masuk)
//                row[c++] = nz(rs.getString("no_nota"));               // No. Nota (nota_inap)
//                row[c++] = rs.getString("no_rkm_medis");              // No. RM
//                row[c++] = rs.getString("nm_pasien");                 // Nama Pasien
//                row[c++] = nz(rs.getString("tgl_bayar"));             // Tgl. Bayar (tagihan_sadewa)
//                row[c++] = rs.getString("nm_bangsal");                // Bangsal
//                row[c++] = df.format(rs.getDouble("laboratorium"));   // Laboratorium
//                row[c++] = rs.getString("cara_bayar");                // Cara Bayar
//                row[c++] = rs.getString("dokter");                    // Dokter
//                row[c++] = rs.getString("status_bayar");              // Status Bayar
//                tabMode.addRow(row);
//            }
//        }
//
//        try (Statement st2 = koneksi.createStatement()) {
//            st2.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//        }
//
//    } catch (Exception e) {
//        System.out.println("Notif tampil: " + e);
//        try (Statement st2 = koneksi.createStatement()) {
//            st2.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//        } catch (Exception ignore) {}
//    }
//}

public void tampil() {

//    tabMode.setRowCount(0);
//    try {
//        String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem() + "");
//        String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem() + "");
//
//        // --- tmp_rp: pakai tanggal dari tagihan_sadewa.tgl_bayar ---
//        try (Statement st = koneksi.createStatement()) {
//            st.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//            st.execute("CREATE TEMPORARY TABLE tmp_rp (no_rawat VARCHAR(17) PRIMARY KEY) ENGINE=MEMORY");
//        }
//
//        String insertTmp =
//            "INSERT IGNORE INTO tmp_rp(no_rawat) " +
//            "SELECT DISTINCT rp.no_rawat " +
//            "FROM reg_periksa rp " +
//            "JOIN tagihan_sadewa ts ON ts.no_nota = rp.no_rawat " +     // <-- RANGE tanggal dari sini
//            "JOIN kamar_inap ki ON ki.no_rawat = rp.no_rawat " +
//            "JOIN kamar k       ON k.kd_kamar  = ki.kd_kamar " +
//            "JOIN bangsal b     ON b.kd_bangsal= k.kd_bangsal " +
//            "LEFT JOIN dokter d ON d.kd_dokter = rp.kd_dokter " +
//            "LEFT JOIN penjab pj ON pj.kd_pj   = rp.kd_pj " +
//            "LEFT JOIN pasien p  ON p.no_rkm_medis = rp.no_rkm_medis " +
//            "WHERE rp.status_lanjut='Ranap' AND rp.status_bayar='Sudah Bayar' " +
//            "  AND ts.tgl_bayar BETWEEN ? AND ? " +                      // <-- pakai ts.tgl_bayar
//            "  AND CONCAT(b.kd_bangsal,b.nm_bangsal) LIKE ? " +
//            "  AND CONCAT(rp.kd_dokter,d.nm_dokter) LIKE ? " +
//            "  AND CONCAT(rp.kd_pj, pj.png_jawab)   LIKE ? " +
//            "  AND (rp.no_rawat LIKE ? OR rp.no_rkm_medis LIKE ? OR p.nm_pasien LIKE ?)";
//
//        try (PreparedStatement psTmp = koneksi.prepareStatement(insertTmp)) {
//            psTmp.setString(1, tgl1);
//            psTmp.setString(2, tgl2);
//            psTmp.setString(3, "%" + KdPoli.getText() + "%");     // dipakai utk filter bangsal
//            psTmp.setString(4, "%" + KdDokter.getText() + "%");
//            psTmp.setString(5, "%" + KdCaraBayar.getText() + "%");
//            psTmp.setString(6, "%" + TCari.getText() + "%");
//            psTmp.setString(7, "%" + TCari.getText() + "%");
//            psTmp.setString(8, "%" + TCari.getText() + "%");
//            psTmp.executeUpdate();
//        }
//
//        // --- Ambil data akhir: tgl_bayar dari tagihan_sadewa (agregat), tgl_masuk_min mengikuti ---
//        String sql =
//            "SELECT " +
//            "  km.tgl_masuk_min           AS tgl_ranap, " +
//            "  ni.no_nota                 AS no_nota, " +               // nota inap
//            "  rp.no_rkm_medis            AS no_rkm_medis, " +
//            "  p.nm_pasien                AS nm_pasien, " +
//            "  ts.tgl_bayar               AS tgl_bayar, " +             // tgl bayar dari tagihan_sadewa
//            "  b.nm_bangsal               AS nm_bangsal, " +
//            "  COALESCE(lab.lab_total,0)  AS laboratorium, " +
//            "  pj.png_jawab               AS cara_bayar, " +
//            "  d.nm_dokter                AS dokter, " +
//            "  rp.status_bayar            AS status_bayar " +
//            "FROM tmp_rp tr " +
//            "JOIN reg_periksa rp ON rp.no_rawat = tr.no_rawat " +
//            "JOIN pasien p       ON p.no_rkm_medis = rp.no_rkm_medis " +
//            "LEFT JOIN ( " +
//            "  SELECT no_rawat, MIN(tgl_masuk) AS tgl_masuk_min " +
//            "  FROM kamar_inap GROUP BY no_rawat " +
//            ") km ON km.no_rawat = rp.no_rawat " +
//            "LEFT JOIN kamar_inap ki ON ki.no_rawat = rp.no_rawat AND ki.tgl_masuk = km.tgl_masuk_min " +
//            "LEFT JOIN kamar k       ON k.kd_kamar = ki.kd_kamar " +
//            "LEFT JOIN bangsal b     ON b.kd_bangsal = k.kd_bangsal " +
//            "LEFT JOIN ( " +
//            "  SELECT no_rawat, MAX(no_nota) AS no_nota " +
//            "  FROM nota_inap GROUP BY no_rawat " +
//            ") ni ON ni.no_rawat = rp.no_rawat " +
//            "LEFT JOIN ( " +
//            "  SELECT no_nota, MAX(tgl_bayar) AS tgl_bayar " +
//            "  FROM tagihan_sadewa GROUP BY no_nota " +
//            ") ts ON ts.no_nota = rp.no_rawat " +
//            "LEFT JOIN dokter d ON d.kd_dokter = rp.kd_dokter " +
//            "LEFT JOIN penjab pj ON pj.kd_pj = rp.kd_pj " +
//            "LEFT JOIN ( " +
//            "   SELECT dpl.no_rawat, SUM(dpl.biaya_item) AS lab_total " +
//            "   FROM detail_periksa_lab dpl GROUP BY dpl.no_rawat " +
//            ") lab ON lab.no_rawat = rp.no_rawat " +
//            "WHERE COALESCE(lab.lab_total,0) > 0 " +
//            "GROUP BY rp.no_rawat " +
//            "ORDER BY ts.tgl_bayar ASC, km.tgl_masuk_min ASC";
//
//        final java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
//        double totalLaboratorium = 0.0; // <-- penampung total
//
//        try (PreparedStatement ps = koneksi.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                Object[] row = new Object[10];
//                int c = 0;
//
//                double labVal = rs.getDouble("laboratorium");
//                totalLaboratorium += labVal; // akumulasi total
//
//                row[c++] = rs.getString("tgl_ranap");               // Tgl. Ranap (min tgl_masuk)
//                row[c++] = nz(rs.getString("no_nota"));             // No. Nota (nota_inap)
//                row[c++] = rs.getString("no_rkm_medis");            // No. RM
//                row[c++] = rs.getString("nm_pasien");               // Nama Pasien
//                row[c++] = nz(rs.getString("tgl_bayar"));           // Tgl. Bayar (tagihan_sadewa)
//                row[c++] = rs.getString("nm_bangsal");              // Bangsal
//                row[c++] = df.format(labVal);                       // Laboratorium
//                row[c++] = rs.getString("cara_bayar");              // Cara Bayar
//                row[c++] = rs.getString("dokter");                  // Dokter
//                row[c++] = rs.getString("status_bayar");            // Status Bayar
//                tabMode.addRow(row);
//            }
//        }
//
//        // ---- Tambah baris TOTAL di paling bawah ----
//        if (tabMode.getRowCount() > 0) {
//            // Baris pemisah (opsional)
////            tabMode.addRow(new Object[]{"","","","","","","","","",""});
//
//            Object[] totalRow = new Object[10];
//            totalRow[0] = "";                 // tgl ranap
//            totalRow[1] = "";                 // no nota
//            totalRow[2] = "";                 // rm
//            totalRow[3] = "";                 // nama
//            totalRow[4] = "";                 // tgl bayar
//            totalRow[5] = "TOTAL";            // label di kolom Bangsal
//            totalRow[6] = df.format(totalLaboratorium); // jumlah Lab
//            totalRow[7] = "";                 // cara bayar
//            totalRow[8] = "";                 // dokter
//            totalRow[9] = "";                 // status
//            tabMode.addRow(totalRow);
//        }
//
//        try (Statement st2 = koneksi.createStatement()) {
//            st2.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//        }
//
//    } catch (Exception e) {
//        System.out.println("Notif tampil: " + e);
//        try (Statement st2 = koneksi.createStatement()) {
//            st2.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//        } catch (Exception ignore) {}
//    }


    tabMode.setRowCount(0);
    try {
        String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem() + "");
        String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem() + "");

        // --- tmp_rp: pakai tanggal dari tagihan_sadewa.tgl_bayar ---
        try (Statement st = koneksi.createStatement()) {
            st.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
            st.execute("CREATE TEMPORARY TABLE tmp_rp (no_rawat VARCHAR(17) PRIMARY KEY) ENGINE=MEMORY");
        }

        String insertTmp =
            "INSERT IGNORE INTO tmp_rp(no_rawat) " +
            "SELECT DISTINCT rp.no_rawat " +
            "FROM reg_periksa rp " +
            "JOIN tagihan_sadewa ts ON ts.no_nota = rp.no_rawat " +     // <-- RANGE tanggal dari sini
            "JOIN kamar_inap ki ON ki.no_rawat = rp.no_rawat " +
            "JOIN kamar k       ON k.kd_kamar  = ki.kd_kamar " +
            "JOIN bangsal b     ON b.kd_bangsal= k.kd_bangsal " +
            "LEFT JOIN dokter d ON d.kd_dokter = rp.kd_dokter " +
            "LEFT JOIN penjab pj ON pj.kd_pj   = rp.kd_pj " +
            "LEFT JOIN pasien p  ON p.no_rkm_medis = rp.no_rkm_medis " +
            "WHERE rp.status_lanjut='Ranap' AND rp.status_bayar='Sudah Bayar' " +
            "  AND ts.tgl_bayar BETWEEN ? AND ? " +                      // <-- pakai ts.tgl_bayar
            "  AND CONCAT(b.kd_bangsal,b.nm_bangsal) LIKE ? " +
            "  AND CONCAT(rp.kd_dokter,d.nm_dokter) LIKE ? " +
            "  AND CONCAT(rp.kd_pj, pj.png_jawab)   LIKE ? " +
            "  AND (rp.no_rawat LIKE ? OR rp.no_rkm_medis LIKE ? OR p.nm_pasien LIKE ?)";

        try (PreparedStatement psTmp = koneksi.prepareStatement(insertTmp)) {
            psTmp.setString(1, tgl1);
            psTmp.setString(2, tgl2);
            psTmp.setString(3, "%" + KdPoli.getText() + "%");     // dipakai utk filter bangsal
            psTmp.setString(4, "%" + KdDokter.getText() + "%");
            psTmp.setString(5, "%" + KdCaraBayar.getText() + "%");
            psTmp.setString(6, "%" + TCari.getText() + "%");
            psTmp.setString(7, "%" + TCari.getText() + "%");
            psTmp.setString(8, "%" + TCari.getText() + "%");
            psTmp.executeUpdate();
        }

        // --- Ambil data akhir: tgl_bayar dari tagihan_sadewa (agregat), tgl_masuk_min mengikuti ---
        String sql =
            "SELECT " +
            "  km.tgl_masuk_min           AS tgl_ranap, " +
            "  ni.no_nota                 AS no_nota, " +               // nota inap
            "  rp.no_rkm_medis            AS no_rkm_medis, " +
            "  p.nm_pasien                AS nm_pasien, " +
            "  ts.tgl_bayar               AS tgl_bayar, " +             // tgl bayar dari tagihan_sadewa
            "  b.nm_bangsal               AS nm_bangsal, " +
            "  COALESCE(lab.lab_total,0)  AS laboratorium, " +
            "  pj.png_jawab               AS cara_bayar, " +
            "  d.nm_dokter                AS dokter, " +
            "  rp.status_bayar            AS status_bayar " +
            "FROM tmp_rp tr " +
            "JOIN reg_periksa rp ON rp.no_rawat = tr.no_rawat " +
            "JOIN pasien p       ON p.no_rkm_medis = rp.no_rkm_medis " +
            "LEFT JOIN ( " +
            "  SELECT no_rawat, MIN(tgl_masuk) AS tgl_masuk_min " +
            "  FROM kamar_inap GROUP BY no_rawat " +
            ") km ON km.no_rawat = rp.no_rawat " +
            "LEFT JOIN kamar_inap ki ON ki.no_rawat = rp.no_rawat AND ki.tgl_masuk = km.tgl_masuk_min " +
            "LEFT JOIN kamar k       ON k.kd_kamar = ki.kd_kamar " +
            "LEFT JOIN bangsal b     ON b.kd_bangsal = k.kd_bangsal " +
            "LEFT JOIN ( " +
            "  SELECT no_rawat, MAX(no_nota) AS no_nota " +
            "  FROM nota_inap GROUP BY no_rawat " +
            ") ni ON ni.no_rawat = rp.no_rawat " +
            "LEFT JOIN ( " +
            "  SELECT no_nota, MAX(tgl_bayar) AS tgl_bayar " +
            "  FROM tagihan_sadewa GROUP BY no_nota " +
            ") ts ON ts.no_nota = rp.no_rawat " +
            "LEFT JOIN dokter d ON d.kd_dokter = rp.kd_dokter " +
            "LEFT JOIN penjab pj ON pj.kd_pj = rp.kd_pj " +
            // ===== Laboratorium: PK/MB (detail) + PA (header total_byr) =====
            "LEFT JOIN ( " +
            "   SELECT base.no_rawat, COALESCE(pkmb.pkmb_total,0) + COALESCE(pa.pa_total,0) AS lab_total " +
            "   FROM tmp_rp base " +
            "   LEFT JOIN ( " +
            "       SELECT dpl.no_rawat, SUM(dpl.biaya_item) AS pkmb_total " +
            "       FROM detail_periksa_lab dpl " +
            "       JOIN jns_perawatan_lab jpl1 ON jpl1.kd_jenis_prw = dpl.kd_jenis_prw " +
            "       WHERE jpl1.kategori IN ('PK','MB') " +
            "       GROUP BY dpl.no_rawat " +
            "   ) pkmb ON pkmb.no_rawat = base.no_rawat " +
            "   LEFT JOIN ( " +
            "       SELECT pl.no_rawat, SUM(jpl2.total_byr) AS pa_total " +
            "       FROM periksa_lab pl " +
            "       JOIN jns_perawatan_lab jpl2 ON jpl2.kd_jenis_prw = pl.kd_jenis_prw " +
            "       WHERE jpl2.kategori = 'PA' " +
            "       GROUP BY pl.no_rawat " +
            "   ) pa ON pa.no_rawat = base.no_rawat " +
            ") lab ON lab.no_rawat = rp.no_rawat " +
            "WHERE COALESCE(lab.lab_total,0) > 0 " +
            "GROUP BY rp.no_rawat " +
            "ORDER BY ts.tgl_bayar ASC, km.tgl_masuk_min ASC";

        final java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
        double totalLaboratorium = 0.0; // <-- penampung total

        try (PreparedStatement ps = koneksi.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[10];
                int c = 0;

                double labVal = rs.getDouble("laboratorium");
                totalLaboratorium += labVal; // akumulasi total

                row[c++] = rs.getString("tgl_ranap");               // Tgl. Ranap (min tgl_masuk)
                row[c++] = nz(rs.getString("no_nota"));             // No. Nota (nota_inap)
                row[c++] = rs.getString("no_rkm_medis");            // No. RM
                row[c++] = rs.getString("nm_pasien");               // Nama Pasien
                row[c++] = nz(rs.getString("tgl_bayar"));           // Tgl. Bayar (tagihan_sadewa)
                row[c++] = rs.getString("nm_bangsal");              // Bangsal
                row[c++] = df.format(labVal);                       // Laboratorium = PK/MB + PA
                row[c++] = rs.getString("cara_bayar");              // Cara Bayar
                row[c++] = rs.getString("dokter");                  // Dokter
                row[c++] = rs.getString("status_bayar");            // Status Bayar
                tabMode.addRow(row);
            }
        }

        // ---- Tambah baris TOTAL di paling bawah ----
        if (tabMode.getRowCount() > 0) {
            Object[] totalRow = new Object[10];
            totalRow[0] = ""; totalRow[1] = ""; totalRow[2] = ""; totalRow[3] = "";
            totalRow[4] = ""; totalRow[5] = "TOTAL";
            totalRow[6] = df.format(totalLaboratorium);
            totalRow[7] = ""; totalRow[8] = ""; totalRow[9] = "";
            tabMode.addRow(totalRow);
        }

        try (Statement st2 = koneksi.createStatement()) {
            st2.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
        }

    } catch (Exception e) {
        System.out.println("Notif tampil: " + e);
        try (Statement st2 = koneksi.createStatement()) {
            st2.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
        } catch (Exception ignore) {}
    }
}
    
    
private static String nz(String s){ return (s==null? "": s); }

    
    
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
