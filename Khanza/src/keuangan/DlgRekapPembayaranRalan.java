package keuangan;

import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import simrskhanza.DlgCariCaraBayar;
import simrskhanza.DlgCariPoli;

public class DlgRekapPembayaranRalan extends javax.swing.JDialog {
    private DefaultTableModel tabMode;
    private final validasi Valid=new validasi();
    private final Connection koneksi=koneksiDB.condb();
    private DlgCariDokter dokter=new DlgCariDokter(null,false);      
    private DlgCariPoli poli=new DlgCariPoli(null,false);
    private DlgCariCaraBayar penjab=new DlgCariCaraBayar(null,false);
    // di class DlgRekapPembayaranRalan
    private JTable tbRekap;
//    private DefaultTableModel modelRekap;

        
    /** Creates new form DlgProgramStudi
     * @param parent
     * @param modal */
    public DlgRekapPembayaranRalan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initRekapTable();
        
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);        
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);       
        
        LoadHTML1.setEditable(true);
        LoadHTML1.setEditorKit(kit);        
//        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
        );
//        Document doc = kit.createDefaultDocument();
        LoadHTML1.setDocument(doc);       
        
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
//        this.tabMode = tabMode;
    }
    private Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
    private int i=0;

    private DlgRekapPembayaranRalan(JFrame jFrame, boolean b, Object object) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Kd2 = new widget.TextBox();
        kddokter = new widget.TextBox();
        TKd = new widget.TextBox();
        KdDokter = new widget.TextBox();
        KdPoli = new widget.TextBox();
        KdCaraBayar = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        panelisi1 = new widget.panelisi();
        label17 = new widget.Label();
        nmdokter = new widget.TextBox();
        btnDokter = new widget.Button();
        label19 = new widget.Label();
        NmCaraBayar = new widget.TextBox();
        BtnCaraBayar = new widget.Button();
        label20 = new widget.Label();
        NmPoli = new widget.TextBox();
        BtnPoli = new widget.Button();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        btnCari = new widget.Button();
        label9 = new widget.Label();
        btnCetak = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        Scroll = new widget.ScrollPane();
        LoadHTML = new widget.editorpane();
        Scroll1 = new widget.ScrollPane();
        LoadHTML1 = new widget.editorpane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        Kd2.setName("Kd2"); // NOI18N
        Kd2.setPreferredSize(new java.awt.Dimension(207, 23));

        kddokter.setName("kddokter"); // NOI18N
        kddokter.setPreferredSize(new java.awt.Dimension(70, 23));

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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), " ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi1.setName("panelisi1"); // NOI18N
        panelisi1.setPreferredSize(new java.awt.Dimension(100, 80));

        label17.setText("Dokter :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(50, 23));

        nmdokter.setEditable(false);
        nmdokter.setName("nmdokter"); // NOI18N
        nmdokter.setPreferredSize(new java.awt.Dimension(230, 23));

        btnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnDokter.setMnemonic('3');
        btnDokter.setToolTipText("Alt+3");
        btnDokter.setName("btnDokter"); // NOI18N
        btnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        btnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDokterActionPerformed(evt);
            }
        });
        btnDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDokterKeyPressed(evt);
            }
        });

        label19.setText("Cara Bayar :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(85, 23));

        NmCaraBayar.setEditable(false);
        NmCaraBayar.setName("NmCaraBayar"); // NOI18N
        NmCaraBayar.setPreferredSize(new java.awt.Dimension(170, 23));

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

        label20.setText("Unit/Poli :");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(75, 23));

        NmPoli.setEditable(false);
        NmPoli.setName("NmPoli"); // NOI18N
        NmPoli.setPreferredSize(new java.awt.Dimension(170, 23));

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

        label11.setText("Tanggal  :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(55, 23));

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(85, 23));

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(30, 23));

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(85, 23));

        btnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        btnCari.setMnemonic('2');
        btnCari.setText("Cari ");
        btnCari.setToolTipText("Alt+2");
        btnCari.setName("btnCari"); // NOI18N
        btnCari.setPreferredSize(new java.awt.Dimension(70, 23));
        btnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCariActionPerformed(evt);
            }
        });
        btnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCariKeyPressed(evt);
            }
        });

        label9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(100, 30));

        btnCetak.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        btnCetak.setMnemonic('2');
        btnCetak.setText("Cetak");
        btnCetak.setToolTipText("Alt+2");
        btnCetak.setName("btnCetak"); // NOI18N
        btnCetak.setPreferredSize(new java.awt.Dimension(70, 23));
        btnCetak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakActionPerformed(evt);
            }
        });
        btnCetak.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCetakKeyPressed(evt);
            }
        });

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

        javax.swing.GroupLayout panelisi1Layout = new javax.swing.GroupLayout(panelisi1);
        panelisi1.setLayout(panelisi1Layout);
        panelisi1Layout.setHorizontalGroup(
            panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelisi1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelisi1Layout.createSequentialGroup()
                        .addComponent(label17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nmdokter, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(panelisi1Layout.createSequentialGroup()
                        .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(Tgl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(label18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(Tgl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelisi1Layout.createSequentialGroup()
                        .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCetak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(BtnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(101, 101, 101)
                        .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelisi1Layout.createSequentialGroup()
                        .addComponent(btnDokter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NmCaraBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BtnCaraBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(label20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NmPoli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BtnPoli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(332, Short.MAX_VALUE))
        );
        panelisi1Layout.setVerticalGroup(
            panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelisi1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nmdokter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDokter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(NmCaraBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(label19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(BtnCaraBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(NmPoli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(label20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(BtnPoli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelisi1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCetak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(BtnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelisi1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(panelisi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Tgl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Tgl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );

        internalFrame1.add(panelisi1, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(255, 255, 253));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        Scroll.setBorder(null);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N
        Scroll.setViewportView(LoadHTML);

        TabRawat.addTab("Rekap Pembayaran Per Pasien", Scroll);

        Scroll1.setBorder(null);
        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);

        LoadHTML1.setBorder(null);
        LoadHTML1.setName("LoadHTML1"); // NOI18N
        Scroll1.setViewportView(LoadHTML1);

        TabRawat.addTab("Rekap Pembayaran Per Bulan", Scroll1);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 50));
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/48x48/report24.png"))); // NOI18N
        jLabel1.setText("Tabelaris Rekap Pembayaran Per Dokter ");
        jLabel1.setIconTextGap(10);
        jLabel1.setName("jLabel1"); // NOI18N
        jPanel1.add(jLabel1);

        internalFrame1.add(jPanel1, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
/*
private void KdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TKdKeyPressed
    Valid.pindah(evt,BtnCari,Nm);
}//GEN-LAST:event_TKdKeyPressed
*/

private void initRekapTable() {
    // header kolom – urutannya match dengan data dari prosesCari()
    Object[] kolom = {
        "No.","Tgl.Kunjungan","No. Nota","No. RM","Nama Pasien","Tgl. Bayar","Unit Poli",
        "Registrasi","Tambahan",
        "Konsul","Echo-Doopler","Obat-Emb-Tsl","Spirometri","Kemo","Monitor","TSaraf","RehabMedik","Ambulans","Eswl","USG Obgyn",
        "Laboratorium","Radiologi",
        "Biaya Operasi","Anastesi","Kamar Bedah","TIM OK","Monitor OK","URS","Mikroskop","Telescope",
        "IGD Bedah","IGD NonBedah",
        "Total",
        "Cara Bayar","Dokter","Status Bayar"
    };

    // === model ala DlgPembayaranRalan: non-editable, pakai tabMode ===
    tabMode = new DefaultTableModel(null, kolom) {
        @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
    };

    // JTable + properti dasar (sesuai pola DlgPembayaranRalan)
    tbRekap = new JTable(tabMode);
    tbRekap.setPreferredScrollableViewportSize(new java.awt.Dimension(500, 500));
    tbRekap.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    tbRekap.getTableHeader().setReorderingAllowed(false);

    // lebar kolom – pola if-else seperti form referensi
    for (int i = 0; i < tbRekap.getColumnModel().getColumnCount(); i++) {
        TableColumn column = tbRekap.getColumnModel().getColumn(i);
        if (i == 0) {                // No.
            column.setPreferredWidth(45);
        } else if (i == 1) {         // Tgl.Kunjungan
            column.setPreferredWidth(95);
        } else if (i == 2) {         // No. Nota
            column.setPreferredWidth(95);
        } else if (i == 3) {         // No. RM
            column.setPreferredWidth(75);
        } else if (i == 4) {         // Nama Pasien
            column.setPreferredWidth(180);
        } else if (i == 5) {         // Tgl. Bayar
            column.setPreferredWidth(95);
        } else if (i == 6) {         // Unit Poli
            column.setPreferredWidth(130);
        } else if (i >= 7 && i <= 32) { // semua kolom numerik (26 kolom, termasuk Total)
            column.setPreferredWidth(100);
        } else if (i == 33) {        // Cara Bayar
            column.setPreferredWidth(140);
        } else if (i == 34) {        // Dokter
            column.setPreferredWidth(170);
        } else if (i == 35) {        // Status Bayar
            column.setPreferredWidth(120);
        } else {
            column.setPreferredWidth(90);
        }
    }

    // warna tabel sesuai aplikasi kamu
    tbRekap.setDefaultRenderer(Object.class, new WarnaTable());

    // taruh JTable ke JScrollPane "Scroll" di form kamu
    Scroll.setViewportView(tbRekap);
}

    
    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        prosesCari();
    }//GEN-LAST:event_formWindowOpened

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==0){
            prosesCari();
        }else if(TabRawat.getSelectedIndex()==1){
            prosesCari2();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnKeluar,Tgl1);}
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void btnCetakKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCetakKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCetakKeyPressed

    private void btnCetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakActionPerformed
        //        try {
            //            // Ambil tanggal dan nama dokter
            //            String tglAwal = Valid.SetTgl(Tgl1.getSelectedItem() + "");
            //            String tglAkhir = Valid.SetTgl(Tgl2.getSelectedItem() + "");
            //            String periode = tglAwal + " s.d " + tglAkhir;
            //            String namaDokter = nmdokter.getText();
            //
            //            // Bungkus ulang HTML dengan header tambahan dan style
            //            StringBuilder htmlCetak = new StringBuilder();
            //            htmlCetak.append("<html><head><style>")
            //                     .append("@page { size: 33.02cm 21.59cm landscape; margin: 1cm; }")
            //                     .append("body { font-family: Arial, sans-serif; font-size: 10px; }")
            //                     .append("h2, h4 { text-align: center; margin: 0; }")
            //                     .append("table { width: 100%; border-collapse: collapse; }")
            //                     .append("th { border: 1px solid #999; padding: 4px; text-align: center; }") // semua header center
            //                     .append("td { border: 1px solid #999; padding: 4px; }")
            //                     .append("td:nth-child(1), td:nth-child(2) { text-align: center; }")
            //                     .append("td:nth-child(3), td:nth-child(4) { text-align: left; }")
            //                     .append("td:nth-child(n+5):nth-child(-n+20) { text-align: right; }")
            //                     .append("</style></head><body>")
            //                     .append("<h2>REKAP LAPORAN PEMBAYARAN</h2>")
            //                     .append("<h4>Periode : ").append(periode).append("</h4>")
            //                     .append("<h4>").append(namaDokter).append("</h4><br>");
            //
            //            // Tambahkan isi tabel dari LoadHTML
            //            htmlCetak.append(LoadHTML.getText());
            //
            //            htmlCetak.append("</body></html>");
            //
            //            // Simpan ke file HTML sementara
            //            File file = new File("rekap-pembayaran.html");
            //            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            //            bw.write(htmlCetak.toString());
            //            bw.close();
            //
            //            // Buka di browser default
            //            Desktop desktop = Desktop.getDesktop();
            //            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                //                desktop.browse(file.toURI());
                //            } else {
                //                JOptionPane.showMessageDialog(null, "Browser tidak tersedia di sistem ini.");
                //            }
            //        } catch (IOException e) {
            //            JOptionPane.showMessageDialog(null, "Gagal mencetak: " + e.getMessage());
            //        }

        try {
            String tglAwal = Valid.SetTgl(Tgl1.getSelectedItem() + "");
            String tglAkhir = Valid.SetTgl(Tgl2.getSelectedItem() + "");
            String periode = tglAwal + " s.d " + tglAkhir;
            String namaDokter = nmdokter.getText();

            // Ambil konten HTML berdasarkan yang aktif
            String isiHTML = "";
            if (LoadHTML.isVisible()) {
                isiHTML = LoadHTML.getText();
            } else if (LoadHTML1.isVisible()) {
                isiHTML = LoadHTML1.getText();
            } else {
                JOptionPane.showMessageDialog(null, "Tidak ada tampilan laporan yang aktif.");
                return;
            }

            // HTML cetak lengkap
            StringBuilder htmlCetak = new StringBuilder();
            htmlCetak.append("<html><head><style>")
            .append("@page { size: 33.02cm 21.59cm landscape; margin: 1cm; }")
            .append("body { font-family: Arial, sans-serif; font-size: 10px; }")
            .append("h2, h4 { text-align: center; margin: 0; }")
            .append("table { width: 100%; border-collapse: collapse; }")
            .append("th { border: 1px solid #999; padding: 4px; text-align: center; }")
            .append("td { border: 1px solid #999; padding: 4px; }")
            .append("td:nth-child(1), td:nth-child(2) { text-align: center; }")
            .append("td:nth-child(3), td:nth-child(4) { text-align: left; }")
            .append("td:nth-child(n+5):nth-child(-n+20) { text-align: right; }")
            .append("</style></head><body>")
            .append("<h2>REKAP LAPORAN PEMBAYARAN</h2>")
            .append("<h4>Periode : ").append(periode).append("</h4>")
            .append("<h4>").append(namaDokter).append("</h4><br>")
            .append(isiHTML)
            .append("</body></html>");

            // Simpan ke file
            File file = new File("rekap-pembayaran.html");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(htmlCetak.toString());
            bw.close();

            // Buka file
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(file.toURI());
            } else {
                JOptionPane.showMessageDialog(null, "Browser tidak tersedia di sistem ini.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal mencetak: " + e.getMessage());
        }
    }//GEN-LAST:event_btnCetakActionPerformed

    private void btnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            btnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, Tgl2, BtnKeluar);
        }
    }//GEN-LAST:event_btnCariKeyPressed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        if(TabRawat.getSelectedIndex()==0){
            prosesCari();
        }else if(TabRawat.getSelectedIndex()==1){
            prosesCari2();
        }
    }//GEN-LAST:event_btnCariActionPerformed

    private void BtnPoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoliActionPerformed
        poli.isCek();
        poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        poli.setLocationRelativeTo(internalFrame1);
        poli.setAlwaysOnTop(false);
        poli.setVisible(true);
    }//GEN-LAST:event_BtnPoliActionPerformed

    private void BtnCaraBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCaraBayarActionPerformed
        penjab.isCek();
        penjab.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penjab.setLocationRelativeTo(internalFrame1);
        penjab.setAlwaysOnTop(false);
        penjab.setVisible(true);
    }//GEN-LAST:event_BtnCaraBayarActionPerformed

    private void btnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDokterKeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_btnDokterKeyPressed

    private void btnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDokterActionPerformed
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_btnDokterActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgRekapPembayaranRalan dialog = new DlgRekapPembayaranRalan(new javax.swing.JFrame(), true, null);
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
    private widget.Button BtnCaraBayar;
    private widget.Button BtnKeluar;
    private widget.Button BtnPoli;
    private widget.TextBox Kd2;
    private widget.TextBox KdCaraBayar;
    private widget.TextBox KdDokter;
    private widget.TextBox KdPoli;
    private widget.editorpane LoadHTML;
    private widget.editorpane LoadHTML1;
    private widget.TextBox NmCaraBayar;
    private widget.TextBox NmPoli;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll1;
    private widget.TextBox TKd;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.Button btnCari;
    private widget.Button btnCetak;
    private widget.Button btnDokter;
    private widget.InternalFrame internalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private widget.TextBox kddokter;
    private widget.Label label11;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.Label label9;
    private widget.TextBox nmdokter;
    private widget.panelisi panelisi1;
    // End of variables declaration//GEN-END:variables

private void prosesCari() {
    StringBuilder html = new StringBuilder();

    html.append(
        "<html><head><style>"
      + "body{font-family:Segoe UI,Arial,sans-serif;font-size:12px;color:#222;}"
      + ".card{background:#fff;border:1px solid #e6e8eb;border-radius:10px;box-shadow:0 2px 6px rgba(0,0,0,.05);padding:10px;}"
      + "table{border-collapse:collapse;width:100%;}"
      + "th,td{border:1px solid #eef0f2;padding:6px 8px;}"
      + "th{background:#f7f9fc;font-weight:600;letter-spacing:.3px;}"
      + "td.num{text-align:right;}"
      + "td.txt{text-align:left;}"
      + ".total-row{background:#eef7ff;font-weight:700;}"
      + "</style></head><body>"
    );
    html.append("<div class='card'>");
    html.append("<table>");
    html.append("<thead>");

    // Header baris 1
    html.append(
        "<tr>"
      + "<th rowspan='2'>No.</th>"
      + "<th rowspan='2'>Tgl.Kunjungan</th>"
      + "<th rowspan='2'>No. Nota</th>"
      + "<th rowspan='2'>No. RM</th>"
      + "<th rowspan='2'>Nama Pasien</th>"
      + "<th rowspan='2'>Tgl. Bayar</th>"
      + "<th rowspan='2'>Unit Poli</th>"
      + "<th colspan='2'>ADMINISTRASI</th>"
      + "<th colspan='11'>TINDAKAN</th>"
      + "<th colspan='2'>PENUNJANG</th>"
      + "<th colspan='8'>OPERASI</th>"
      + "<th colspan='2'>IGD</th>"
      + "<th rowspan='2'>Total</th>"
      + "<th rowspan='2'>Cara Bayar</th>"
      + "<th rowspan='2'>Dokter</th>"
      + "<th rowspan='2'>Status Bayar</th>"
      + "</tr>"
    );

    // Header baris 2 (rincian grup numerik)
    html.append(
        "<tr>"
      + "<th>Registrasi</th>"
      + "<th>Tambahan</th>"
      + "<th>Konsul</th>"
      + "<th>Echo-Doopler</th>"
      + "<th>Obat-Emb-Tsl</th>"
      + "<th>Spirometri</th>"
      + "<th>Kemo</th>"
      + "<th>Monitor</th>"
      + "<th>TSaraf</th>"
      + "<th>RehabMedik</th>"
      + "<th>Ambulans</th>"
      + "<th>Eswl</th>"
      + "<th>USG Obgyn</th>"
      + "<th>Laboratorium</th>"
      + "<th>Radiologi</th>"
      + "<th>Biaya Operasi</th>"
      + "<th>Anastesi</th>"
      + "<th>Kamar Bedah</th>"
      + "<th>TIM OK</th>"
      + "<th>Monitor OK</th>"
      + "<th>URS</th>"
      + "<th>Mikroskop</th>"
      + "<th>Telescope</th>"
      + "<th>IGD Bedah</th>"
      + "<th>IGD NonBedah</th>"
      + "</tr>"
    );
    html.append("</thead><tbody>");

    int i = 1;
    // 25 kolom numerik + 1 "Total" = 26
    double[] total = new double[26];

    try {
        String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem() + "");
        String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem() + "");

        String sql =
        "SELECT "
        + " rp.tgl_registrasi, nj.no_nota, rp.no_rkm_medis, p.nm_pasien, pb.tgl_bayar, pol.nm_poli, "
        + " IFNULL(rp.biaya_reg,0) AS Registrasi, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Tambahan' THEN cs.total END),0) AS Tambahan, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Konsul' THEN cs.total END),0) AS Konsul, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Echo' THEN cs.total END),0) AS Echo, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Obat' THEN cs.total END),0) AS Obat, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Spirometri' THEN cs.total END),0) AS Spirometri, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Kemo' THEN cs.total END),0) AS Kemo, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Monitor' THEN cs.total END),0) AS Monitor, "
        + " IFNULL(SUM(CASE WHEN cs.cat='TSaraf' THEN cs.total END),0) AS TSaraf, "
        + " IFNULL(SUM(CASE WHEN cs.cat='RehabMedik' THEN cs.total END),0) AS RehabMedik, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Ambulans' THEN cs.total END),0) AS Ambulans, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Eswl' THEN cs.total END),0) AS Eswl, "
        + " IFNULL(SUM(CASE WHEN cs.cat='USG' THEN cs.total END),0) AS USG, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Laboratorium' THEN cs.total END),0) AS Laboratorium, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Radiologi' THEN cs.total END),0) AS Radiologi, "
        + " IFNULL(SUM(CASE WHEN cs.cat IN('Anastesi','Kamar_Bedah','TIM_OK','Monitor_OK','URS','Mikroskop','Telescope') THEN cs.total END),0) AS Biaya_Operasi, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Anastesi' THEN cs.total END),0) AS Anastesi, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Kamar_Bedah' THEN cs.total END),0) AS Kamar_Bedah, "
        + " IFNULL(SUM(CASE WHEN cs.cat='TIM_OK' THEN cs.total END),0) AS TIM_OK, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Monitor_OK' THEN cs.total END),0) AS Monitor_OK, "
        + " IFNULL(SUM(CASE WHEN cs.cat='URS' THEN cs.total END),0) AS URS, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Mikroskop' THEN cs.total END),0) AS Mikroskop, "
        + " IFNULL(SUM(CASE WHEN cs.cat='Telescope' THEN cs.total END),0) AS Telescope, "
        + " IFNULL(SUM(CASE WHEN cs.cat='IGD_Bedah' THEN cs.total END),0) AS IGD_Bedah, "
        + " IFNULL(SUM(CASE WHEN cs.cat='IGD_NonBedah' THEN cs.total END),0) AS IGD_NonBedah, "

        // Tambahan 3 kolom (setelah Total di output)
        + " pj.png_jawab AS Cara_Bayar, "
        + " d.nm_dokter  AS Dokter, "
        + " rp.status_bayar AS Status_Bayar "

        + "FROM reg_periksa rp "
        + "JOIN pasien p           ON p.no_rkm_medis = rp.no_rkm_medis "
        + "LEFT JOIN nota_jalan nj ON nj.no_rawat = rp.no_rawat "
        + "LEFT JOIN poliklinik pol ON pol.kd_poli = rp.kd_poli "
        + "LEFT JOIN dokter d       ON d.kd_dokter = rp.kd_dokter "
        + "LEFT JOIN penjab pj      ON pj.kd_pj = rp.kd_pj "
        + "LEFT JOIN (SELECT no_rawat, MAX(tgl_bayar) AS tgl_bayar FROM bayar_piutang GROUP BY no_rawat) pb ON pb.no_rawat=rp.no_rawat "

        + "LEFT JOIN ( "
        + "  SELECT no_rawat, cat, SUM(total) AS total "
        + "  FROM ( "
        + "    SELECT no_rawat,'Tambahan' AS cat, SUM(besar_biaya) AS total FROM tambahan_biaya GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'Konsul',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr JOIN jns_perawatan jp ON jp.kd_jenis_prw=rdr.kd_jenis_prw WHERE LOWER(jp.nm_perawatan) LIKE '%konsultasi%' GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'Konsul',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp JOIN jns_perawatan jp ON jp.kd_jenis_prw=rdp.kd_jenis_prw WHERE LOWER(jp.nm_perawatan) LIKE '%konsultasi%' GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'Echo',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr   WHERE rdr.kd_jenis_prw IN('70766','RJ93207','RJ93208','RJ93209','RJ93210') GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'Echo',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE rdp.kd_jenis_prw IN('70766','RJ93207','RJ93208','RJ93209','RJ93210') GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'Obat',SUM(total) FROM detail_pemberian_obat GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'Spirometri',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr   WHERE rdr.kd_jenis_prw IN('RJ93182','RJ93303') GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'Spirometri',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE rdp.kd_jenis_prw IN('RJ93182','RJ93303') GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'Kemo',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr   WHERE rdr.kd_jenis_prw IN('RJ93194','RJ93195','RJ931957','RJ94007','RJ94025','RJ94032') GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'Kemo',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE rdp.kd_jenis_prw IN('RJ93194','RJ93195','RJ931957','RJ94007','RJ94025','RJ94032') GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'Monitor',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE LOWER(rdp.kd_jenis_prw)='ugd018' GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'TSaraf',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr   WHERE LOWER(rdr.kd_jenis_prw) LIKE '%ipm%' GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'TSaraf',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE LOWER(rdp.kd_jenis_prw) LIKE '%ipm%' GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'RehabMedik',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr   WHERE LOWER(rdr.kd_jenis_prw) LIKE '%rhb%' GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'RehabMedik',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE LOWER(rdp.kd_jenis_prw) LIKE '%rhb%' GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'Ambulans',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr   WHERE rdr.kd_jenis_prw IN('64368','RJ93299','RJ93300','RJ93301') GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'Ambulans',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE rdp.kd_jenis_prw IN('64368','RJ93299','RJ93300','RJ93301') GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'Eswl',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr   WHERE LOWER(rdr.kd_jenis_prw) LIKE '%eswl%' GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'Eswl',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE LOWER(rdp.kd_jenis_prw) LIKE '%eswl%' GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdr.no_rawat,'USG',SUM(rdr.biaya_rawat) FROM rawat_jl_dr rdr   WHERE rdr.kd_jenis_prw IN('70751','RJ93310') GROUP BY rdr.no_rawat "
        + "    UNION ALL "
        + "    SELECT rdp.no_rawat,'USG',SUM(rdp.biaya_rawat) FROM rawat_jl_drpr rdp WHERE rdp.kd_jenis_prw IN('70751','RJ93310') GROUP BY rdp.no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'Laboratorium',SUM(biaya) FROM periksa_lab GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'Radiologi',SUM(biaya) FROM periksa_radiologi GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'Anastesi',SUM(biayadokter_anestesi) FROM operasi GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'Kamar_Bedah',SUM(biayasewaok) FROM operasi GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'TIM_OK',SUM(biaya_omloop) FROM operasi GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'Monitor_OK',SUM(hargasatuan) FROM beri_obat_operasi WHERE kd_obat='OK00003133' GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'URS',SUM(hargasatuan) FROM beri_obat_operasi WHERE kd_obat='OK00003138' GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'Mikroskop',SUM(hargasatuan) FROM beri_obat_operasi WHERE kd_obat='OK00003142' GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'Telescope',SUM(hargasatuan) FROM beri_obat_operasi WHERE kd_obat='OK00003143' GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'IGD_Bedah',SUM(biaya_rawat) FROM rawat_jl_dr   WHERE LOWER(kd_jenis_prw) LIKE '%ugd%' AND kd_dokter LIKE '%D0000173%' GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'IGD_Bedah',SUM(biaya_rawat) FROM rawat_jl_drpr WHERE LOWER(kd_jenis_prw) LIKE '%ugd%' AND kd_dokter LIKE '%D0000173%' GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'IGD_NonBedah',SUM(biaya_rawat) FROM rawat_jl_dr   WHERE LOWER(kd_jenis_prw) LIKE '%ugd%' AND kd_dokter NOT LIKE '%D0000173%' GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'IGD_NonBedah',SUM(biaya_rawat) FROM rawat_jl_drpr WHERE LOWER(kd_jenis_prw) LIKE '%ugd%' AND kd_dokter NOT LIKE '%D0000173%' GROUP BY no_rawat "
        + "    UNION ALL "
        + "    SELECT no_rawat,'IGD_NonBedah',SUM(biaya_rawat) FROM rawat_jl_pr   WHERE LOWER(kd_jenis_prw) LIKE '%ugd%' GROUP BY no_rawat "
        + "  ) x GROUP BY no_rawat,cat "
        + ") cs ON cs.no_rawat = rp.no_rawat "

        + "WHERE rp.status_lanjut='Ralan' "
        + "  AND rp.tgl_registrasi BETWEEN ? AND ? "
        + "  AND CONCAT(rp.kd_poli,  pol.nm_poli)   LIKE ? "
        + "  AND CONCAT(rp.kd_dokter,d.nm_dokter)   LIKE ? "
        + "  AND CONCAT(rp.kd_pj,    pj.png_jawab)  LIKE ? "

        + "GROUP BY rp.no_rawat";

        PreparedStatement ps = koneksi.prepareStatement(sql);
        ps.setString(1, tgl1);
        ps.setString(2, tgl2);
        ps.setString(3, "%" + KdPoli.getText() + "%");
        ps.setString(4, "%" + kddokter.getText() + "%");
        ps.setString(5, "%" + KdCaraBayar.getText() + "%");

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String bg = (i % 2 == 0) ? "#fafbfc" : "#ffffff";
            html.append("<tr style='background:").append(bg).append("'>");
            html.append("<td>").append(i).append("</td>");
            html.append("<td>").append(rs.getString("tgl_registrasi")).append("</td>");
            html.append("<td>").append(rs.getString("no_nota")==null? "":rs.getString("no_nota")).append("</td>");
            html.append("<td>").append(rs.getString("no_rkm_medis")).append("</td>");
            html.append("<td class='txt'>").append(rs.getString("nm_pasien")).append("</td>");
            html.append("<td>").append(rs.getString("tgl_bayar")==null? "":rs.getString("tgl_bayar")).append("</td>");
            html.append("<td class='txt'>").append(rs.getString("nm_poli")).append("</td>");

            // ambil numerik sesuai urutan
            double registrasi   = rs.getDouble("Registrasi");
            double tambahan     = rs.getDouble("Tambahan");
            double konsul       = rs.getDouble("Konsul");
            double echo         = rs.getDouble("Echo");
            double obat         = rs.getDouble("Obat");
            double spiro        = rs.getDouble("Spirometri");
            double kemo         = rs.getDouble("Kemo");
            double monitor      = rs.getDouble("Monitor");
            double tsaraf       = rs.getDouble("TSaraf");
            double rehab        = rs.getDouble("RehabMedik");
            double ambulans     = rs.getDouble("Ambulans");
            double eswl         = rs.getDouble("Eswl");
            double usg          = rs.getDouble("USG");
            double lab          = rs.getDouble("Laboratorium");
            double rad          = rs.getDouble("Radiologi");
            double biayaOp      = rs.getDouble("Biaya_Operasi");
            double anest        = rs.getDouble("Anastesi");
            double sewaok       = rs.getDouble("Kamar_Bedah");
            double omloop       = rs.getDouble("TIM_OK");
            double okmon        = rs.getDouble("Monitor_OK");
            double okurs        = rs.getDouble("URS");
            double okmikro      = rs.getDouble("Mikroskop");
            double okteles      = rs.getDouble("Telescope");
            double igdBedah     = rs.getDouble("IGD_Bedah");
            double igdNonBedah  = rs.getDouble("IGD_NonBedah");

            double grandRow = registrasi + tambahan + konsul + echo + obat + spiro + kemo + monitor
                            + tsaraf + rehab + ambulans + eswl + usg + lab + rad
                            + biayaOp + igdBedah + igdNonBedah;

            double[] vals = new double[]{
                registrasi, tambahan,
                konsul, echo, obat, spiro, kemo, monitor, tsaraf, rehab, ambulans, eswl, usg,
                lab, rad,
                biayaOp, anest, sewaok, omloop, okmon, okurs, okmikro, okteles,
                igdBedah, igdNonBedah,
                grandRow
            };

            for (double v : vals) {
                html.append("<td class='num'>").append(Valid.SetAngka(v)).append("</td>");
            }
            for (int j=0; j<vals.length; j++) total[j]+=vals[j];

            // Tambahan 3 kolom setelah Total
            html.append("<td class='txt'>").append(rs.getString("Cara_Bayar")).append("</td>");
            html.append("<td class='txt'>").append(rs.getString("Dokter")).append("</td>");
            html.append("<td>").append(rs.getString("Status_Bayar")).append("</td>");

            html.append("</tr>");
            i++;
        }
        rs.close();
        ps.close();

        // Baris TOTAL: label di kiri (7 kolom non-numerik), lalu 26 numerik, lalu 3 kolom text kosong
        html.append("<tr class='total-row'>");
        html.append("<td colspan='7' class='txt'>TOTAL</td>");
        for (int j=0; j<total.length; j++) {
            html.append("<td class='num'>").append(Valid.SetAngka(total[j])).append("</td>");
        }
        html.append("<td></td><td></td><td></td>"); // placeholder untuk 3 kolom text
        html.append("</tr>");

    } catch (Exception e) {
        System.out.println("Notif prosesCari: " + e);
    }

    html.append("</tbody></table></div>");
    html.append("</body></html>");
    LoadHTML.setText(html.toString());
}

    
    
//private void prosesCari() {
//    StringBuilder htmlContent = new StringBuilder();
//
//    htmlContent.append(
//        "<html><head><style>" +
//        "table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; }" +        
//        "th, td { border: 1px dotted #d3d3d3; padding: 5px; text-align: center; font-size:9px; }" +
//        "th { background-color: #f2f2f2; }" +
//        "</style></head><body>"
//    );
//  
//    htmlContent.append("<table>");
//    htmlContent.append("<thead>");
//    htmlContent.append(
//        "<tr>" +
//        "<th rowspan='2'>No.</th>" +
//        "<th rowspan='2'>Tgl.Kunjungan</th>" +
//        "<th rowspan='2'>No. Nota</th>" +
//        "<th rowspan='2'>No. RM</th>" +
//        "<th rowspan='2'>Nama Pasien</th>" +
//        "<th rowspan='2'>Tgl. Bayar</th>" +
//        "<th rowspan='2'>Unit Poli</th>" +
//        "<th colspan='2'>Administrasi</th>" +        
//        "<th colspan='11'>TINDAKAN</th>" +
//        "<th colspan='2'>PENUNJANG</th>" +
//        "<th colspan='8'>OPERASI</th>" +        
//        "<th colspan='2'>IGD</th>" +                        
//        "<th rowspan='2'>Total</th>" +        
//        "</tr>"
//    );
//
//    htmlContent.append(
//        "<tr>" +
//        "<th>Registrasi</th>" +                       
//        "<th>Tambahan</th>" +        
//                
//        "<th>Konsul</th>" +        
//        "<th>Echo-Doopler</th>" +
//        "<th>Obat-Emb-Tsl</th>" +        
//        "<th>Spirometri</th>" +
//        "<th>Kemo</th>" +
//        "<th>Monitor</th>" +        
//        "<th>Tsaraf</th>" +
//        "<th>RehabMedik</th>" +        
//        "<th>Ambulans</th>" +  
//        "<th>Eswl</th>" +         
//        "<th>USG Obgyn</th>" +                         
//                
//        "<th>Laboratorium</th>" +
//        "<th>Radiologi</th>" +
//                
//        "<th>Biaya Opreasi</th>" +
//        "<th>Anastesi</th>" +        
//        "<th>Kamar Bedah</th>" +        
//        "<th>TIM OK</th>" +                
//        "<th>Monitor OK</th>" +             
//        "<th>URS</th>" +        
//        "<th>Mikroskop</th>" +                        
//        "<th>Telescope</th>" +                                
//                
//        "<th>IGD Bedah</th>" +
//        "<th>IGD NonBedah</th>" +                      
//        
//        "</tr>"
//    );
//    htmlContent.append("</thead><tbody>");
//
//    int i = 1;
//
//    double[] total = new double[24]; // untuk menjumlahkan semua nilai numerik per kolom
//
//    try {
//        String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem() + "");
//        String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem() + "");
//
//                String sql =                 
//                        
//                        "SELECT "
//                        + "rp.tgl_registrasi, "
//                        + "nj.no_nota, "
//                        + "rp.no_rkm_medis, "
//                        + "p.nm_pasien, "
//                        + "bp.tgl_bayar, "
//                        + "pol.nm_poli, "
//                        + "IFNULL(rp.biaya_reg, 0) AS Registrasi, "
//                        + "IFNULL(tb.besar_biaya, 0) AS Tambahan, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND LOWER(jp.nm_perawatan) LIKE '%konsultasi%' "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND LOWER(jp.nm_perawatan) LIKE '%konsultasi%' "
//                        + "  ), 0) "
//                        + ") AS Konsul, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND rdr.kd_jenis_prw IN('70766','RJ93207','RJ93208','RJ93209','RJ93210') "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND rdp.kd_jenis_prw IN('70766','RJ93207','RJ93208','RJ93209','RJ93210') "
//                        + "  ), 0) "
//                        + ") AS Echo, "
//
//                        + "IFNULL((SELECT SUM(total) FROM detail_pemberian_obat WHERE no_rawat=rp.no_rawat), 0) AS Obat, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND rdr.kd_jenis_prw IN('RJ93182','RJ93303') "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND rdp.kd_jenis_prw IN('RJ93182','RJ93303') "
//                        + "  ), 0) "
//                        + ") AS Spirometri, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND rdr.kd_jenis_prw IN('RJ93194','RJ93195','RJ931957','RJ94007','RJ94025','RJ94032') "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND rdp.kd_jenis_prw IN('RJ93194','RJ93195','RJ931957','RJ94007','RJ94025','RJ94032') "
//                        + "  ), 0) "
//                        + ") AS Kemo, "
//
//                        + "IFNULL(( "
//                        + "  SELECT SUM(rdp.biaya_rawat) "
//                        + "  FROM rawat_jl_drpr rdp "
//                        + "  WHERE rdp.no_rawat = rp.no_rawat "
//                        + "    AND LOWER(rdp.kd_jenis_prw) = 'ugd018' "
//                        + "), 0) AS Monitor, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND LOWER(rdr.kd_jenis_prw) LIKE '%ipm%' "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND LOWER(rdp.kd_jenis_prw) LIKE '%ipm%' "
//                        + "  ), 0) "
//                        + ") AS TSaraf, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND LOWER(rdr.kd_jenis_prw) LIKE '%rhb%' "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND LOWER(rdp.kd_jenis_prw) LIKE '%rhb%' "
//                        + "  ), 0) "
//                        + ") AS RehabMedik, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND rdr.kd_jenis_prw IN('64368','RJ93299','RJ93300','RJ93301') "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND rdp.kd_jenis_prw IN('64368','RJ93299','RJ93300','RJ93301') "
//                        + "  ), 0) "
//                        + ") AS Ambulans, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND LOWER(rdr.kd_jenis_prw) LIKE '%eswl%' "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND LOWER(rdp.kd_jenis_prw) LIKE '%eswl%' "
//                        + "  ), 0) "
//                        + ") AS Eswl, "
//
//                        + "( "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdr.biaya_rawat) "
//                        + "    FROM rawat_jl_dr AS rdr "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdr.kd_jenis_prw "
//                        + "    WHERE rdr.no_rawat = rp.no_rawat "
//                        + "      AND rdr.kd_jenis_prw IN('70751','RJ93310') "
//                        + "  ), 0) + "
//                        + "  IFNULL(( "
//                        + "    SELECT SUM(rdp.biaya_rawat) "
//                        + "    FROM rawat_jl_drpr AS rdp "
//                        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw = rdp.kd_jenis_prw "
//                        + "    WHERE rdp.no_rawat = rp.no_rawat "
//                        + "      AND rdp.kd_jenis_prw IN('70751','RJ93310') "
//                        + "  ), 0) "
//                        + ") AS USG, "
//
//                        + "IFNULL((SELECT SUM(biaya) FROM periksa_lab WHERE no_rawat=rp.no_rawat), 0) AS Laboratorium, "
//                        + "IFNULL((SELECT SUM(biaya) FROM periksa_radiologi WHERE no_rawat=rp.no_rawat), 0) AS Radiologi, "
//
//                        + "IFNULL((SELECT SUM(biayadokter_anestesi) from operasi where no_rawat=rp.no_rawat), 0) AS Anastesi, "
//                        + "IFNULL((SELECT SUM(biayasewaok) from operasi where no_rawat=rp.no_rawat), 0) AS Kamar_Bedah, "
//                        + "IFNULL((SELECT SUM(biaya_omloop) from operasi where no_rawat=rp.no_rawat), 0) AS TIM_OK, "
//                        + "IFNULL((SELECT SUM(hargasatuan) from beri_obat_operasi where no_rawat=rp.no_rawat and kd_obat='OK00003133'), 0) AS Monitor_OK, "
//                        + "IFNULL((SELECT SUM(hargasatuan) from beri_obat_operasi where no_rawat=rp.no_rawat and kd_obat='OK00003138'), 0) AS URS, "
//                        + "IFNULL((SELECT SUM(hargasatuan) from beri_obat_operasi where no_rawat=rp.no_rawat and kd_obat='OK00003142'), 0) AS Mikroskop, "
//                        + "IFNULL((SELECT SUM(hargasatuan) from beri_obat_operasi where no_rawat=rp.no_rawat and kd_obat='OK00003143'), 0) AS Telescope, "
//
//                        + "IFNULL( ("
//                        + "  IFNULL((SELECT SUM(biaya_rawat) FROM rawat_jl_dr "
//                        + "          WHERE LOWER(rawat_jl_dr.kd_jenis_prw) LIKE '%ugd%' "
//                        + "            AND rawat_jl_dr.kd_dokter NOT LIKE '%D0000173%' "
//                        + "            AND rawat_jl_dr.no_rawat = rp.no_rawat), 0) "
//                        + "  + IFNULL((SELECT SUM(biaya_rawat) FROM rawat_jl_drpr "
//                        + "            WHERE LOWER(rawat_jl_drpr.kd_jenis_prw) LIKE '%ugd%' "
//                        + "              AND rawat_jl_drpr.kd_dokter NOT LIKE '%D0000173%' "
//                        + "              AND rawat_jl_drpr.no_rawat = rp.no_rawat), 0) "
//                        + "  + IFNULL((SELECT SUM(biaya_rawat) FROM rawat_jl_pr "
//                        + "            WHERE LOWER(rawat_jl_pr.kd_jenis_prw) LIKE '%ugd%' "
//                        + "              AND rawat_jl_pr.no_rawat = rp.no_rawat), 0) "
//                        + "), 0) AS IGDNonBedah, "
//
//                        + "IFNULL( ("
//                        + "  IFNULL((SELECT SUM(biaya_rawat) FROM rawat_jl_dr "
//                        + "          WHERE LOWER(rawat_jl_dr.kd_jenis_prw) LIKE '%ugd%' "
//                        + "            AND rawat_jl_dr.kd_dokter LIKE '%D0000173%' "
//                        + "            AND rawat_jl_dr.no_rawat = rp.no_rawat), 0) "
//                        + "  + IFNULL((SELECT SUM(biaya_rawat) FROM rawat_jl_drpr "
//                        + "            WHERE LOWER(rawat_jl_drpr.kd_jenis_prw) LIKE '%ugd%' "
//                        + "              AND rawat_jl_drpr.kd_dokter LIKE '%D0000173%' "
//                        + "              AND rawat_jl_drpr.no_rawat = rp.no_rawat), 0) "
//                        + "), 0) AS IGDBedah "
//
//                        + "FROM reg_periksa rp "
//                        + "JOIN pasien p ON p.no_rkm_medis = rp.no_rkm_medis "
//                        + "LEFT JOIN nota_jalan nj ON nj.no_rawat = rp.no_rawat "
//                        + "LEFT JOIN bayar_piutang bp ON bp.no_rawat = rp.no_rawat "
//                        + "LEFT JOIN poliklinik pol ON pol.kd_poli = rp.kd_poli "
//                        + "LEFT JOIN dokter d ON d.kd_dokter = rp.kd_dokter "
//                        + "LEFT JOIN penjab pj ON pj.kd_pj = rp.kd_pj "
//                        + "LEFT JOIN ( "
//                        + "  SELECT no_rawat, SUM(besar_biaya) AS besar_biaya "
//                        + "  FROM tambahan_biaya "
//                        + "  GROUP BY no_rawat "
//                        + ") tb ON tb.no_rawat = rp.no_rawat "
//
//                        + "WHERE rp.status_lanjut = 'Ralan' "
//                        + "  AND rp.tgl_registrasi BETWEEN ? AND ? "
//                        + "  AND CONCAT(rp.kd_poli, pol.nm_poli) LIKE ? "
//                        + "  AND CONCAT(rp.kd_dokter, d.nm_dokter) LIKE ? "
//                        + "  AND CONCAT(rp.kd_pj, pj.png_jawab) LIKE ? "
//
//                        + "GROUP BY rp.no_rawat;";
//
//
//        PreparedStatement ps = koneksi.prepareStatement(sql);        
//        ps.setString(1, tgl1);
//        ps.setString(2, tgl2);
//        ps.setString(3, "%" + KdPoli.getText() + "%");
//        ps.setString(4, "%" + kddokter.getText() + "%");
//        ps.setString(5, "%" + KdCaraBayar.getText() + "%");
//
//
//        ResultSet rs = ps.executeQuery();
//
//        while (rs.next()) {
//            String rowColor = (i % 2 == 0) ? "#f2f2f2" : "#ffffff";
//            htmlContent.append("<tr style='background-color:" + rowColor + ";'>");
//            htmlContent.append("<td style='text-align:center;'>").append(i).append("</td>");
//            htmlContent.append("<td style='text-align:center;'>").append(rs.getString("tgl_registrasi")).append("</td>");
//                    htmlContent.append("<td style='text-align:center;'>").append(rs.getString("no_nota")).append("</td>");
//            htmlContent.append("<td style='text-align:center;'>").append(rs.getString("no_rkm_medis")).append("</td>");
//            htmlContent.append("<td style='text-align:left;'>").append(rs.getString("nm_pasien")).append("</td>");
//            htmlContent.append("<td style='text-align:center;'>").append(rs.getString("tgl_bayar")).append("</td>");
//            htmlContent.append("<td style='text-align:left;'>").append(rs.getString("nm_poli")).append("</td>");
//
//            double[] nilaiNumerik = {
//                rs.getDouble("Registrasi"),
//                rs.getDouble("Tambahan"),
//                rs.getDouble("Konsul"),
//                rs.getDouble("Echo"),
//                rs.getDouble("Obat"),
//                rs.getDouble("Spirometri"),
//                rs.getDouble("Kemo"),
//                rs.getDouble("Monitor"),
//                rs.getDouble("Tsaraf"),
//                rs.getDouble("RehabMedik"),
//                rs.getDouble("Ambulans"),
//                rs.getDouble("Eswl"),
//                rs.getDouble("USG"),
//                rs.getDouble("Laboratorium"),
//                rs.getDouble("Radiologi"),
//                rs.getDouble("Anastesi"),
//                rs.getDouble("Kamar_Bedah"),
//                rs.getDouble("TIM_OK"),
//                rs.getDouble("Monitor_OK"),
//                rs.getDouble("URS"),
//                rs.getDouble("Mikroskop"),
//                rs.getDouble("Telescope"),
//                rs.getDouble("IGDNonBedah"),
//                rs.getDouble("IGDBedah")                               
//            };
//
//            for (int j = 0; j < nilaiNumerik.length; j++) {
//                total[j] += nilaiNumerik[j]; // Akumulasi total
//                String style = "text-align:right;";
//                if (nilaiNumerik[j] < 0) style += "color:red;";
//                htmlContent.append("<td style='" + style + "'>")
//                           .append(Valid.SetAngka(nilaiNumerik[j]))
//                           .append("</td>");
//            }
//
//            htmlContent.append("</tr>");
//            i++;
//        }
//
//        // Tambahkan baris total
//        htmlContent.append("<tr style='background-color:#d9edf7; font-weight:bold;'>");
//        htmlContent.append("<td colspan='4' style='text-align:right;'>TOTAL</td>");
//        for (int j = 0; j < total.length; j++) {
//            String style = "text-align:right;";
//            if (total[j] < 0) style += "color:red;";
//            htmlContent.append("<td style='" + style + "'>")
//                       .append(Valid.SetAngka(total[j]))
//                       .append("</td>");
//        }
//        htmlContent.append("</tr>");
//
//        rs.close();
//        ps.close();
//    } catch (Exception e) {
//        System.out.println("Notif prosesCari: " + e);
//    }
//
//    htmlContent.append("</tbody></table>");
//    htmlContent.append("</body></html>");
//    LoadHTML.setText(htmlContent.toString());
//    
//    htmlContent.append(
//        "<html><head><style>" +
//        "table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; }" +
//        "th, td { border: 1px dotted #d3d3d3; padding: 5px; text-align: center; font-size:9px; }" +
//        "th { background-color: #f2f2f2; }" +
//        "@page { size: 33.02cm 21.59cm landscape; margin: 1cm; }" + // dibalik: width height
//        "body { margin: 0; }" +
//        "</style></head><body>"
//    );
//}

//private void prosesCari() {
//    StringBuilder html = new StringBuilder();
//
//    html.append(
//        "<html><head><style>"
//      + "body{font-family:Segoe UI,Arial,sans-serif;font-size:12px;color:#222;margin:0;}"
//      + ".card{background:#fff;border:1px solid #e6e8eb;border-radius:10px;"
//      + "box-shadow:0 2px 6px rgba(0,0,0,.05);padding:10px;}"
//      + "table{border-collapse:collapse;width:100%;table-layout:fixed;}"
//      + "th,td{border:1px dotted #d0d7de;padding:6px 8px;vertical-align:middle;}"
//      + "th{background:#f7f9fc;font-weight:600;letter-spacing:.3px;}"
//      + "td.num{text-align:right;}"
//      + "td.txt{text-align:left;}"
//      + "thead th,tbody td{white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}"
//      + "tbody tr:nth-child(odd){background:#ffffff;}"   // baris data pertama putih
//      + "tbody tr:nth-child(even){background:#f6f8fa;}"  // baris data kedua abu muda
//      + ".total-row{background:#eef7ff;font-weight:700;}"
//      + "</style></head><body>"
//    );
//    html.append("<div class='card'><table><thead>");
//
//    // Header baris 1
//    html.append(
//        "<tr>"
//      + "<th rowspan='2'>No.</th>"
//      + "<th rowspan='2'>Tgl.Kunjungan</th>"
//      + "<th rowspan='2'>No. Nota</th>"
//      + "<th rowspan='2'>No. RM</th>"
//      + "<th rowspan='2'>Nama Pasien</th>"
//      + "<th rowspan='2'>Tgl. Bayar</th>"
//      + "<th rowspan='2'>Unit Poli</th>"
//      + "<th colspan='2'>ADMINISTRASI</th>"
//      + "<th colspan='11'>TINDAKAN</th>"
//      + "<th colspan='2'>PENUNJANG</th>"
//      + "<th colspan='8'>OPERASI</th>"
//      + "<th colspan='2'>IGD</th>"
//      + "<th rowspan='2'>Total</th>"
//      + "<th rowspan='2'>Cara Bayar</th>"
//      + "<th rowspan='2'>Dokter</th>"
//      + "<th rowspan='2'>Status Bayar</th>"
//      + "</tr>"
//    );
//
//    // Header baris 2
//    html.append(
//        "<tr>"
//      + "<th>Registrasi</th>"
//      + "<th>Tambahan</th>"
//      + "<th>Konsul</th>"
//      + "<th>Echo-Doopler</th>"
//      + "<th>Obat-Emb-Tsl</th>"
//      + "<th>Spirometri</th>"
//      + "<th>Kemo</th>"
//      + "<th>Monitor</th>"
//      + "<th>TSaraf</th>"
//      + "<th>RehabMedik</th>"
//      + "<th>Ambulans</th>"
//      + "<th>Eswl</th>"
//      + "<th>USG Obgyn</th>"
//      + "<th>Laboratorium</th>"
//      + "<th>Radiologi</th>"
//      + "<th>Biaya Operasi</th>"
//      + "<th>Anastesi</th>"
//      + "<th>Kamar Bedah</th>"
//      + "<th>TIM OK</th>"
//      + "<th>Monitor OK</th>"
//      + "<th>URS</th>"
//      + "<th>Mikroskop</th>"
//      + "<th>Telescope</th>"
//      + "<th>IGD Bedah</th>"
//      + "<th>IGD NonBedah</th>"
//      + "</tr>"
//    );
//    html.append("</thead><tbody>");
//
//    int i = 1;
//    // 25 kolom numerik + 1 "Total" = 26
//    double[] total = new double[26];
//
//    try {
//        String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem() + "");
//        String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem() + "");
//
//        // 1) Siapkan TEMPORARY TABLE kandidat no_rawat (supaya semua agregasi super cepat)
//        try (Statement st = koneksi.createStatement()) {
//            st.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//        }
//        String createTmp =
//            "CREATE TEMPORARY TABLE tmp_rp (no_rawat VARCHAR(17) PRIMARY KEY) ENGINE=MEMORY "
//          + "SELECT rp.no_rawat "
//          + "FROM reg_periksa rp "
//          + "LEFT JOIN poliklinik pol ON pol.kd_poli = rp.kd_poli "
//          + "LEFT JOIN dokter d       ON d.kd_dokter = rp.kd_dokter "
//          + "LEFT JOIN penjab pj      ON pj.kd_pj = rp.kd_pj "
//          + "WHERE rp.status_lanjut='Ralan' "
//          + "  AND rp.tgl_registrasi BETWEEN ? AND ? "
//          + "  AND CONCAT(rp.kd_poli,  pol.nm_poli)   LIKE ? "
//          + "  AND CONCAT(rp.kd_dokter,d.nm_dokter)   LIKE ? "
//          + "  AND CONCAT(rp.kd_pj,    pj.png_jawab)  LIKE ? ";
//        PreparedStatement psTmp = koneksi.prepareStatement(createTmp);
//        psTmp.setString(1, tgl1);
//        psTmp.setString(2, tgl2);
//        psTmp.setString(3, "%" + KdPoli.getText() + "%");
//        psTmp.setString(4, "%" + kddokter.getText() + "%");
//        psTmp.setString(5, "%" + KdCaraBayar.getText() + "%");
//        psTmp.executeUpdate();
//        psTmp.close();
//
//        // 2) Query utama (semua agregasi join ke tmp_rp → jauh lebih sedikit baris diproses)
//        String sql =
//        "SELECT "
//        + " rp.tgl_registrasi, nj.no_nota, rp.no_rkm_medis, p.nm_pasien, pb.tgl_bayar, pol.nm_poli, "
//        + " IFNULL(rp.biaya_reg,0) AS Registrasi, "
//
//        + " IFNULL(SUM(CASE WHEN cs.cat='Tambahan' THEN cs.total END),0) AS Tambahan, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Konsul' THEN cs.total END),0) AS Konsul, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Echo' THEN cs.total END),0) AS Echo, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Obat' THEN cs.total END),0) AS Obat, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Spirometri' THEN cs.total END),0) AS Spirometri, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Kemo' THEN cs.total END),0) AS Kemo, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Monitor' THEN cs.total END),0) AS Monitor, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='TSaraf' THEN cs.total END),0) AS TSaraf, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='RehabMedik' THEN cs.total END),0) AS RehabMedik, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Ambulans' THEN cs.total END),0) AS Ambulans, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Eswl' THEN cs.total END),0) AS Eswl, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='USG' THEN cs.total END),0) AS USG, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Laboratorium' THEN cs.total END),0) AS Laboratorium, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Radiologi' THEN cs.total END),0) AS Radiologi, "
//
//        + " IFNULL(SUM(CASE WHEN cs.cat IN('Anastesi','Kamar_Bedah','TIM_OK','Monitor_OK','URS','Mikroskop','Telescope') THEN cs.total END),0) AS Biaya_Operasi, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Anastesi' THEN cs.total END),0) AS Anastesi, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Kamar_Bedah' THEN cs.total END),0) AS Kamar_Bedah, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='TIM_OK' THEN cs.total END),0) AS TIM_OK, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Monitor_OK' THEN cs.total END),0) AS Monitor_OK, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='URS' THEN cs.total END),0) AS URS, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Mikroskop' THEN cs.total END),0) AS Mikroskop, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Telescope' THEN cs.total END),0) AS Telescope, "
//
//        + " IFNULL(SUM(CASE WHEN cs.cat='IGD_Bedah' THEN cs.total END),0) AS IGD_Bedah, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='IGD_NonBedah' THEN cs.total END),0) AS IGD_NonBedah, "
//
//        + " pj.png_jawab AS Cara_Bayar, d.nm_dokter AS Dokter, rp.status_bayar AS Status_Bayar "
//
//        + "FROM tmp_rp tr "
//        + "JOIN reg_periksa rp ON rp.no_rawat = tr.no_rawat "
//        + "JOIN pasien p       ON p.no_rkm_medis = rp.no_rkm_medis "
//        + "LEFT JOIN nota_jalan nj ON nj.no_rawat = rp.no_rawat "
//        + "LEFT JOIN poliklinik pol ON pol.kd_poli = rp.kd_poli "
//        + "LEFT JOIN dokter d   ON d.kd_dokter = rp.kd_dokter "
//        + "LEFT JOIN penjab pj  ON pj.kd_pj = rp.kd_pj "
//
//        + "LEFT JOIN (SELECT bp.no_rawat, MAX(bp.tgl_bayar) AS tgl_bayar "
//        + "           FROM bayar_piutang bp JOIN tmp_rp t2 ON t2.no_rawat=bp.no_rawat "
//        + "           GROUP BY bp.no_rawat) pb ON pb.no_rawat=rp.no_rawat "
//
//        + "LEFT JOIN ( "
//        + "  SELECT x.no_rawat, x.cat, SUM(x.total) AS total "
//        + "  FROM ( "
//        + "    SELECT tr.no_rawat,'Tambahan' AS cat, SUM(tb.besar_biaya) AS total "
//        + "    FROM tmp_rp tr JOIN tambahan_biaya tb ON tb.no_rawat=tr.no_rawat "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Konsul',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw=rdr.kd_jenis_prw "
//        + "    WHERE LOWER(jp.nm_perawatan) LIKE '%konsultasi%' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Konsul',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    JOIN jns_perawatan jp ON jp.kd_jenis_prw=rdp.kd_jenis_prw "
//        + "    WHERE LOWER(jp.nm_perawatan) LIKE '%konsultasi%' "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Echo',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE rdr.kd_jenis_prw IN('70766','RJ93207','RJ93208','RJ93209','RJ93210') "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Echo',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE rdp.kd_jenis_prw IN('70766','RJ93207','RJ93208','RJ93209','RJ93210') "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Obat',SUM(dpo.total) "
//        + "    FROM tmp_rp tr JOIN detail_pemberian_obat dpo ON dpo.no_rawat=tr.no_rawat "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Spirometri',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE rdr.kd_jenis_prw IN('RJ93182','RJ93303') "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Spirometri',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE rdp.kd_jenis_prw IN('RJ93182','RJ93303') "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Kemo',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE rdr.kd_jenis_prw IN('RJ93194','RJ93195','RJ931957','RJ94007','RJ94025','RJ94032') "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Kemo',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE rdp.kd_jenis_prw IN('RJ93194','RJ93195','RJ931957','RJ94007','RJ94025','RJ94032') "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Monitor',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdp.kd_jenis_prw)='ugd018' "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'TSaraf',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdr.kd_jenis_prw) LIKE '%ipm%' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'TSaraf',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdp.kd_jenis_prw) LIKE '%ipm%' "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'RehabMedik',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdr.kd_jenis_prw) LIKE '%rhb%' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'RehabMedik',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdp.kd_jenis_prw) LIKE '%rhb%' "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Ambulans',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE rdr.kd_jenis_prw IN('64368','RJ93299','RJ93300','RJ93301') "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Ambulans',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE rdp.kd_jenis_prw IN('64368','RJ93299','RJ93300','RJ93301') "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Eswl',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdr.kd_jenis_prw) LIKE '%eswl%' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Eswl',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdp.kd_jenis_prw) LIKE '%eswl%' "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'USG',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE rdr.kd_jenis_prw IN('70751','RJ93310') "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'USG',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE rdp.kd_jenis_prw IN('70751','RJ93310') "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Laboratorium',SUM(pl.biaya) "
//        + "    FROM tmp_rp tr JOIN periksa_lab pl ON pl.no_rawat=tr.no_rawat "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Radiologi',SUM(pr.biaya) "
//        + "    FROM tmp_rp tr JOIN periksa_radiologi pr ON pr.no_rawat=tr.no_rawat "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Anastesi',SUM(op.biayadokter_anestesi) "
//        + "    FROM tmp_rp tr JOIN operasi op ON op.no_rawat=tr.no_rawat "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Kamar_Bedah',SUM(op.biayasewaok) "
//        + "    FROM tmp_rp tr JOIN operasi op ON op.no_rawat=tr.no_rawat "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'TIM_OK',SUM(op.biaya_omloop) "
//        + "    FROM tmp_rp tr JOIN operasi op ON op.no_rawat=tr.no_rawat "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Monitor_OK',SUM(boo.hargasatuan) "
//        + "    FROM tmp_rp tr JOIN beri_obat_operasi boo ON boo.no_rawat=tr.no_rawat AND boo.kd_obat='OK00003133' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'URS',SUM(boo.hargasatuan) "
//        + "    FROM tmp_rp tr JOIN beri_obat_operasi boo ON boo.no_rawat=tr.no_rawat AND boo.kd_obat='OK00003138' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Mikroskop',SUM(boo.hargasatuan) "
//        + "    FROM tmp_rp tr JOIN beri_obat_operasi boo ON boo.no_rawat=tr.no_rawat AND boo.kd_obat='OK00003142' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'Telescope',SUM(boo.hargasatuan) "
//        + "    FROM tmp_rp tr JOIN beri_obat_operasi boo ON boo.no_rawat=tr.no_rawat AND boo.kd_obat='OK00003143' "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'IGD_Bedah',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdr.kd_jenis_prw) LIKE '%ugd%' AND rdr.kd_dokter LIKE '%D0000173%' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'IGD_Bedah',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdp.kd_jenis_prw) LIKE '%ugd%' AND rdp.kd_dokter LIKE '%D0000173%' "
//        + "    GROUP BY tr.no_rawat "
//
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'IGD_NonBedah',SUM(rdr.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_dr rdr ON rdr.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdr.kd_jenis_prw) LIKE '%ugd%' AND rdr.kd_dokter NOT LIKE '%D0000173%' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'IGD_NonBedah',SUM(rdp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rdp.kd_jenis_prw) LIKE '%ugd%' AND rdp.kd_dokter NOT LIKE '%D0000173%' "
//        + "    GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat,'IGD_NonBedah',SUM(rjp.biaya_rawat) "
//        + "    FROM tmp_rp tr JOIN rawat_jl_pr rjp ON rjp.no_rawat=tr.no_rawat "
//        + "    WHERE LOWER(rjp.kd_jenis_prw) LIKE '%ugd%' "
//        + "    GROUP BY tr.no_rawat "
//        + "  ) x "
//        + "  GROUP BY x.no_rawat,x.cat "
//        + ") cs ON cs.no_rawat = rp.no_rawat "
//
//        + "GROUP BY rp.no_rawat";
//
//        PreparedStatement ps = koneksi.prepareStatement(sql);
//        ResultSet rs = ps.executeQuery();
//
//        while (rs.next()) {
//            html.append("<tr>");
//            html.append("<td>").append(i).append("</td>");
//            html.append("<td>").append(rs.getString("tgl_registrasi")).append("</td>");
//            html.append("<td>").append(rs.getString("no_nota")==null? "":rs.getString("no_nota")).append("</td>");
//            html.append("<td>").append(rs.getString("no_rkm_medis")).append("</td>");
//            html.append("<td class='txt'>").append(rs.getString("nm_pasien")).append("</td>");
//            html.append("<td>").append(rs.getString("tgl_bayar")==null? "":rs.getString("tgl_bayar")).append("</td>");
//            html.append("<td class='txt'>").append(rs.getString("nm_poli")).append("</td>");
//
//            double registrasi   = rs.getDouble("Registrasi");
//            double tambahan     = rs.getDouble("Tambahan");
//            double konsul       = rs.getDouble("Konsul");
//            double echo         = rs.getDouble("Echo");
//            double obat         = rs.getDouble("Obat");
//            double spiro        = rs.getDouble("Spirometri");
//            double kemo         = rs.getDouble("Kemo");
//            double monitor      = rs.getDouble("Monitor");
//            double tsaraf       = rs.getDouble("TSaraf");
//            double rehab        = rs.getDouble("RehabMedik");
//            double ambulans     = rs.getDouble("Ambulans");
//            double eswl         = rs.getDouble("Eswl");
//            double usg          = rs.getDouble("USG");
//            double lab          = rs.getDouble("Laboratorium");
//            double rad          = rs.getDouble("Radiologi");
//            double biayaOp      = rs.getDouble("Biaya_Operasi");
//            double anest        = rs.getDouble("Anastesi");
//            double sewaok       = rs.getDouble("Kamar_Bedah");
//            double omloop       = rs.getDouble("TIM_OK");
//            double okmon        = rs.getDouble("Monitor_OK");
//            double okurs        = rs.getDouble("URS");
//            double okmikro      = rs.getDouble("Mikroskop");
//            double okteles      = rs.getDouble("Telescope");
//            double igdBedah     = rs.getDouble("IGD_Bedah");
//            double igdNonBedah  = rs.getDouble("IGD_NonBedah");
//
//            double grandRow = registrasi + tambahan + konsul + echo + obat + spiro + kemo + monitor
//                            + tsaraf + rehab + ambulans + eswl + usg + lab + rad
//                            + biayaOp + igdBedah + igdNonBedah;
//
//            double[] vals = new double[]{
//                registrasi, tambahan,
//                konsul, echo, obat, spiro, kemo, monitor, tsaraf, rehab, ambulans, eswl, usg,
//                lab, rad,
//                biayaOp, anest, sewaok, omloop, okmon, okurs, okmikro, okteles,
//                igdBedah, igdNonBedah,
//                grandRow
//            };
//
//            for (double v : vals) {
//                html.append("<td class='num'>").append(Valid.SetAngka(v)).append("</td>");
//            }
//            for (int j=0; j<vals.length; j++) total[j]+=vals[j];
//
//            html.append("<td class='txt'>").append(rs.getString("Cara_Bayar")).append("</td>");
//            html.append("<td class='txt'>").append(rs.getString("Dokter")).append("</td>");
//            html.append("<td>").append(rs.getString("Status_Bayar")).append("</td>");
//            html.append("</tr>");
//            i++;
//        }
//        rs.close();
//        ps.close();
//
//        // 3) Total baris bawah
//        html.append("<tr class='total-row'>");
//        html.append("<td colspan='7' class='txt'>TOTAL</td>");
//        for (int j=0; j<total.length; j++) {
//            html.append("<td class='num'>").append(Valid.SetAngka(total[j])).append("</td>");
//        }
//        html.append("<td></td><td></td><td></td>");
//        html.append("</tr>");
//
//        // (Opsional) bersihkan temp table lebih awal
//        try (Statement st2 = koneksi.createStatement()) {
//            st2.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//        }
//
//    } catch (Exception e) {
//        System.out.println("Notif prosesCari: " + e);
//    }
//
//    html.append("</tbody></table></div></body></html>");
//    LoadHTML.setText(html.toString());
//}
    
    
//private void prosesCari() {
//    // bersihkan data lama
//    tabMode.setRowCount(0);
//    double[] grandTotals = new double[26]; // 26 kolom numerik (termasuk "Total")
//    int no = 1;
//
//    try {
//        String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem()+"");
//        String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem()+"");
//
//        // drop temp lama
//        try (Statement st = koneksi.createStatement()) {
//            st.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//        }
//
//        // buat kandidat no_rawat dengan filter (super cepat)
//        String createTmp =
//            "CREATE TEMPORARY TABLE tmp_rp (no_rawat VARCHAR(17) PRIMARY KEY) ENGINE=MEMORY "
//          + "SELECT rp.no_rawat "
//          + "FROM reg_periksa rp "
//          + "LEFT JOIN poliklinik pol ON pol.kd_poli = rp.kd_poli "
//          + "LEFT JOIN dokter d       ON d.kd_dokter = rp.kd_dokter "
//          + "LEFT JOIN penjab pj      ON pj.kd_pj = rp.kd_pj "
//          + "WHERE rp.status_lanjut='Ralan' "
//          + "  AND rp.tgl_registrasi BETWEEN ? AND ? "
//          + "  AND CONCAT(rp.kd_poli,  pol.nm_poli)   LIKE ? "
//          + "  AND CONCAT(rp.kd_dokter,d.nm_dokter)   LIKE ? "
//          + "  AND CONCAT(rp.kd_pj,    pj.png_jawab)  LIKE ? ";
//        try (PreparedStatement psTmp = koneksi.prepareStatement(createTmp)) {
//            psTmp.setString(1, tgl1);
//            psTmp.setString(2, tgl2);
//            psTmp.setString(3, "%" + KdPoli.getText() + "%");
//            psTmp.setString(4, "%" + kddokter.getText() + "%");
//            psTmp.setString(5, "%" + KdCaraBayar.getText() + "%");
//            psTmp.executeUpdate();
//        }
//
//        // query utama pivot
//        String sql =
//        "SELECT "
//        + " rp.tgl_registrasi, nj.no_nota, rp.no_rkm_medis, p.nm_pasien, pb.tgl_bayar, pol.nm_poli, "
//        + " IFNULL(rp.biaya_reg,0) AS Registrasi, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Tambahan' THEN cs.total END),0) AS Tambahan, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Konsul' THEN cs.total END),0) AS Konsul, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Echo' THEN cs.total END),0) AS Echo, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Obat' THEN cs.total END),0) AS Obat, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Spirometri' THEN cs.total END),0) AS Spirometri, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Kemo' THEN cs.total END),0) AS Kemo, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Monitor' THEN cs.total END),0) AS Monitor, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='TSaraf' THEN cs.total END),0) AS TSaraf, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='RehabMedik' THEN cs.total END),0) AS RehabMedik, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Ambulans' THEN cs.total END),0) AS Ambulans, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Eswl' THEN cs.total END),0) AS Eswl, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='USG' THEN cs.total END),0) AS USG, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Laboratorium' THEN cs.total END),0) AS Laboratorium, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Radiologi' THEN cs.total END),0) AS Radiologi, "
//        + " IFNULL(SUM(CASE WHEN cs.cat IN('Anastesi','Kamar_Bedah','TIM_OK','Monitor_OK','URS','Mikroskop','Telescope') THEN cs.total END),0) AS Biaya_Operasi, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Anastesi' THEN cs.total END),0) AS Anastesi, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Kamar_Bedah' THEN cs.total END),0) AS Kamar_Bedah, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='TIM_OK' THEN cs.total END),0) AS TIM_OK, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Monitor_OK' THEN cs.total END),0) AS Monitor_OK, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='URS' THEN cs.total END),0) AS URS, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Mikroskop' THEN cs.total END),0) AS Mikroskop, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='Telescope' THEN cs.total END),0) AS Telescope, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='IGD_Bedah' THEN cs.total END),0) AS IGD_Bedah, "
//        + " IFNULL(SUM(CASE WHEN cs.cat='IGD_NonBedah' THEN cs.total END),0) AS IGD_NonBedah, "
//        + " pj.png_jawab AS Cara_Bayar, d.nm_dokter AS Dokter, rp.status_bayar AS Status_Bayar "
//        + "FROM tmp_rp tr "
//        + "JOIN reg_periksa rp ON rp.no_rawat = tr.no_rawat "
//        + "JOIN pasien p       ON p.no_rkm_medis = rp.no_rkm_medis "
//        + "LEFT JOIN nota_jalan nj ON nj.no_rawat = rp.no_rawat "
//        + "LEFT JOIN poliklinik pol ON pol.kd_poli = rp.kd_poli "
//        + "LEFT JOIN dokter d   ON d.kd_dokter = rp.kd_dokter "
//        + "LEFT JOIN penjab pj  ON pj.kd_pj = rp.kd_pj "
//        + "LEFT JOIN (SELECT bp.no_rawat, MAX(bp.tgl_bayar) AS tgl_bayar "
//        + "           FROM bayar_piutang bp JOIN tmp_rp t2 ON t2.no_rawat=bp.no_rawat "
//        + "           GROUP BY bp.no_rawat) pb ON pb.no_rawat=rp.no_rawat "
//        + "LEFT JOIN ( "
//        + "  SELECT base.no_rawat, base.cat, SUM(base.total) AS total "
//        + "  FROM ( "
//        + "    SELECT tr.no_rawat AS no_rawat, 'Tambahan'   AS cat, SUM(tb.besar_biaya)   AS total "
//        + "      FROM tmp_rp tr JOIN tambahan_biaya tb ON tb.no_rawat=tr.no_rawat "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Konsul',    SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      JOIN jns_perawatan jp ON jp.kd_jenis_prw=rdr.kd_jenis_prw "
//        + "      WHERE LOWER(jp.nm_perawatan) LIKE '%konsultasi%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Konsul',    SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      JOIN jns_perawatan jp ON jp.kd_jenis_prw=rdp.kd_jenis_prw "
//        + "      WHERE LOWER(jp.nm_perawatan) LIKE '%konsultasi%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Echo',      SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE rdr.kd_jenis_prw IN('70766','RJ93207','RJ93208','RJ93209','RJ93210') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Echo',      SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE rdp.kd_jenis_prw IN('70766','RJ93207','RJ93208','RJ93209','RJ93210') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Obat',      SUM(dpo.total)       AS total "
//        + "      FROM tmp_rp tr JOIN detail_pemberian_obat dpo ON dpo.no_rawat=tr.no_rawat "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Spirometri',SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE rdr.kd_jenis_prw IN('RJ93182','RJ93303') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Spirometri',SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE rdp.kd_jenis_prw IN('RJ93182','RJ93303') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Kemo',      SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE rdr.kd_jenis_prw IN('RJ93194','RJ93195','RJ931957','RJ94007','RJ94025','RJ94032') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Kemo',      SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE rdp.kd_jenis_prw IN('RJ93194','RJ93195','RJ931957','RJ94007','RJ94025','RJ94032') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Monitor',   SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdp.kd_jenis_prw)='ugd018' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'TSaraf',    SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdr.kd_jenis_prw) LIKE '%ipm%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'TSaraf',    SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdp.kd_jenis_prw) LIKE '%ipm%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'RehabMedik',SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdr.kd_jenis_prw) LIKE '%rhb%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'RehabMedik',SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdp.kd_jenis_prw) LIKE '%rhb%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Ambulans',  SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE rdr.kd_jenis_prw IN('64368','RJ93299','RJ93300','RJ93301') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Ambulans',  SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE rdp.kd_jenis_prw IN('64368','RJ93299','RJ93300','RJ93301') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Eswl',      SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdr.kd_jenis_prw) LIKE '%eswl%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Eswl',      SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdp.kd_jenis_prw) LIKE '%eswl%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'USG',       SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE rdr.kd_jenis_prw IN('70751','RJ93310') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'USG',       SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE rdp.kd_jenis_prw IN('70751','RJ93310') "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Laboratorium', SUM(pl.biaya)     AS total "
//        + "      FROM tmp_rp tr JOIN periksa_lab pl ON pl.no_rawat=tr.no_rawat "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Radiologi', SUM(pr.biaya)        AS total "
//        + "      FROM tmp_rp tr JOIN periksa_radiologi pr ON pr.no_rawat=tr.no_rawat "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Anastesi',  SUM(op.biayadokter_anestesi) AS total "
//        + "      FROM tmp_rp tr JOIN operasi op ON op.no_rawat=tr.no_rawat "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Kamar_Bedah', SUM(op.biayasewaok) AS total "
//        + "      FROM tmp_rp tr JOIN operasi op ON op.no_rawat=tr.no_rawat "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'TIM_OK',    SUM(op.biaya_omloop) AS total "
//        + "      FROM tmp_rp tr JOIN operasi op ON op.no_rawat=tr.no_rawat "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Monitor_OK',SUM(boo.hargasatuan) AS total "
//        + "      FROM tmp_rp tr JOIN beri_obat_operasi boo ON boo.no_rawat=tr.no_rawat AND boo.kd_obat='OK00003133' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'URS',       SUM(boo.hargasatuan) AS total "
//        + "      FROM tmp_rp tr JOIN beri_obat_operasi boo ON boo.no_rawat=tr.no_rawat AND boo.kd_obat='OK00003138' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Mikroskop', SUM(boo.hargasatuan) AS total "
//        + "      FROM tmp_rp tr JOIN beri_obat_operasi boo ON boo.no_rawat=tr.no_rawat AND boo.kd_obat='OK00003142' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'Telescope', SUM(boo.hargasatuan) AS total "
//        + "      FROM tmp_rp tr JOIN beri_obat_operasi boo ON boo.no_rawat=tr.no_rawat AND boo.kd_obat='OK00003143' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'IGD_Bedah', SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdr.kd_jenis_prw) LIKE '%ugd%' AND rdr.kd_dokter LIKE '%D0000173%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'IGD_Bedah', SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdp.kd_jenis_prw) LIKE '%ugd%' AND rdp.kd_dokter LIKE '%D0000173%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'IGD_NonBedah', SUM(rdr.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_dr   rdr ON rdr.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdr.kd_jenis_prw) LIKE '%ugd%' AND rdr.kd_dokter NOT LIKE '%D0000173%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'IGD_NonBedah', SUM(rdp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_drpr rdp ON rdp.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rdp.kd_jenis_prw) LIKE '%ugd%' AND rdp.kd_dokter NOT LIKE '%D0000173%' "
//        + "      GROUP BY tr.no_rawat "
//        + "    UNION ALL "
//        + "    SELECT tr.no_rawat, 'IGD_NonBedah', SUM(rjp.biaya_rawat) AS total "
//        + "      FROM tmp_rp tr JOIN rawat_jl_pr   rjp ON rjp.no_rawat=tr.no_rawat "
//        + "      WHERE LOWER(rjp.kd_jenis_prw) LIKE '%ugd%' "
//        + "      GROUP BY tr.no_rawat "
//        + "  ) AS base "
//        + "  GROUP BY base.no_rawat, base.cat "
//        + ") cs ON cs.no_rawat = rp.no_rawat "
//        + "GROUP BY rp.no_rawat";        
//
//        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
//            // ps.setFetchSize(200); // boleh dicoba; untuk MySQL streaming: ps.setFetchSize(Integer.MIN_VALUE);
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    // ambil semua numerik
//                    double registrasi=rs.getDouble("Registrasi"), tambahan=rs.getDouble("Tambahan"),
//                           konsul=rs.getDouble("Konsul"), echo=rs.getDouble("Echo"), obat=rs.getDouble("Obat"),
//                           spiro=rs.getDouble("Spirometri"), kemo=rs.getDouble("Kemo"), monitor=rs.getDouble("Monitor"),
//                           tsaraf=rs.getDouble("TSaraf"), rehab=rs.getDouble("RehabMedik"), ambulans=rs.getDouble("Ambulans"),
//                           eswl=rs.getDouble("Eswl"), usg=rs.getDouble("USG"), lab=rs.getDouble("Laboratorium"),
//                           rad=rs.getDouble("Radiologi"), biayaOp=rs.getDouble("Biaya_Operasi"),
//                           anest=rs.getDouble("Anastesi"), sewaok=rs.getDouble("Kamar_Bedah"),
//                           omloop=rs.getDouble("TIM_OK"), okmon=rs.getDouble("Monitor_OK"),
//                           okurs=rs.getDouble("URS"), okmikro=rs.getDouble("Mikroskop"),
//                           okteles=rs.getDouble("Telescope"), igdBedah=rs.getDouble("IGD_Bedah"),
//                           igdNonBedah=rs.getDouble("IGD_NonBedah");
//
//                    double totalBaris = registrasi + tambahan + konsul + echo + obat + spiro + kemo + monitor
//                                      + tsaraf + rehab + ambulans + eswl + usg + lab + rad
//                                      + biayaOp + igdBedah + igdNonBedah;
//
//                    Object[] row = new Object[tabMode.getColumnCount()];
//                    int c = 0;
//                    row[c++] = no++;
//                    row[c++] = rs.getString("tgl_registrasi");
//                    row[c++] = rs.getString("no_nota")==null? "":rs.getString("no_nota");
//                    row[c++] = rs.getString("no_rkm_medis");
//                    row[c++] = rs.getString("nm_pasien");
//                    row[c++] = rs.getString("tgl_bayar")==null? "":rs.getString("tgl_bayar");
//                    row[c++] = rs.getString("nm_poli");
//
//                    double[] nums = new double[]{
//                        registrasi, tambahan,
//                        konsul, echo, obat, spiro, kemo, monitor, tsaraf, rehab, ambulans, eswl, usg,
//                        lab, rad,
//                        biayaOp, anest, sewaok, omloop, okmon, okurs, okmikro, okteles,
//                        igdBedah, igdNonBedah,
//                        totalBaris
//                    };
//                    for (int j=0;j<nums.length;j++) { row[c++] = nums[j]; grandTotals[j]+=nums[j]; }
//
//                    row[c++] = rs.getString("Cara_Bayar");
//                    row[c++] = rs.getString("Dokter");
//                    row[c++] = rs.getString("Status_Bayar");
//
//                    tabMode.addRow(row);
//                }
//            }
//        }
//
//        // tambah baris TOTAL
//        Object[] tot = new Object[tabMode.getColumnCount()];
//        int c = 0;
//        tot[c++] = null;           // No.
//        tot[c++] = "";             // tgl kunj
//        tot[c++] = "";             // no nota
//        tot[c++] = "";             // no rm
//        tot[c++] = "TOTAL";        // marker untuk renderer
//        tot[c++] = "";             // tgl bayar
//        tot[c++] = "";             // poli
//        for (int j=0;j<grandTotals.length;j++) tot[c++] = grandTotals[j];
//        tot[c++] = ""; tot[c++] = ""; tot[c++] = ""; // tail text
//        tabMode.addRow(tot);
//
//        // bersihkan temp (opsional)
//        try (Statement st2 = koneksi.createStatement()) {
//            st2.execute("DROP TEMPORARY TABLE IF EXISTS tmp_rp");
//        }
//
//    } catch (Exception e) {
//        System.out.println("Notif prosesCari: " + e);
//    }
//}
    
private void prosesCari2() {
    StringBuilder htmlContent = new StringBuilder();

    htmlContent.append(
        "<html><head><style>" +
        "table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; }" +        
        "th, td { border: 1px dotted #d3d3d3; padding: 5px; text-align: center; font-size:9px; }" +
        "th { background-color: #f2f2f2; }" +
        "</style></head><body>"
    );
  
    htmlContent.append("<table>");
    htmlContent.append("<thead>");
    htmlContent.append("<tr>" +
        "<th rowspan='2'>No.</th>" +
        "<th rowspan='2'>BULAN</th>" +        
        "<th rowspan='2'>Biaya RS</th>" +
        "<th rowspan='2'>Paket BPJS</th>" +
        "<th colspan='7'>JASA MEDIK</th>" +
        "<th colspan='4'>PENUNJANG</th>" +
        "<th rowspan='2'>Biaya Operasional RS</th>" +
        "<th rowspan='2'>Total Jasa+ Penunjang</th>" +
        "<th rowspan='2'>Selisih Paket BPJS - Biaya RS</th>" +
        "</tr>");

    htmlContent.append("<tr>" +
        "<th>Konsul & Visite Dokter</th>" +
        "<th>Jasa Operasi</th>" +
        "<th>Alat Medis</th>" +
        "<th>Konsul & Visite Anestesi</th>" +
        "<th>Jasa Anestesi</th>" +
        "<th>Tim OK</th>" +
        "<th>Tim UGD</th>" +
        "<th>Apotik</th>" +
        "<th>Lab</th>" +
        "<th>Radiologi</th>" +
        "<th>Darah PMI</th>" +
        "</tr>");
    htmlContent.append("</thead><tbody>");

    int i = 1;

    double[] total = new double[24]; // untuk menjumlahkan semua nilai numerik per kolom

    try {
        String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem() + "");
        String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem() + "");

        String sql = 
                
            "SELECT " +
            "DATE_FORMAT(kb.tanggal_rvp, '%M %Y') AS bulan, " +
            "SUM(kb.totalpiutang) AS biaya_rs, " +
            "SUM(kb.dibayarbpjs) AS paket_bpjs, " +
            "SUM(IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_inap_dr WHERE no_rawat=rp.no_rawat AND kd_dokter=?),0)) AS konsul_visite, " +
            "SUM(IFNULL(o.biayaoperator1,0)) AS jasa_operasi, " +
            "SUM(IFNULL((SELECT SUM(hargasatuan) FROM beri_obat_operasi WHERE no_rawat=rp.no_rawat AND kd_obat LIKE 'OK%' AND kd_obat NOT IN ('DOK0000','OK00003133')),0)) AS alat_medis, " +
            "SUM(IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_inap_dr WHERE no_rawat=rp.no_rawat AND kd_dokter=o.dokter_anestesi),0)) AS visite_anestesi, " +
            "SUM(IFNULL(o.biayadokter_anestesi,0)) AS jasa_anestesi, " +
            "SUM(IFNULL(o.biaya_omloop,0)) AS tim_ok, " +
            "SUM(IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_jl_dr WHERE no_rawat=rp.no_rawat),0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_jl_drpr WHERE no_rawat=rp.no_rawat AND kd_jenis_prw NOT IN ('RJ94004','RJ94005','ugd004','ugd005')),0)) AS tim_ugd, " +
            "SUM(IFNULL((SELECT SUM(total) FROM detail_pemberian_obat WHERE no_rawat=rp.no_rawat AND kode_brng NOT LIKE 'B000003106%'),0) + " +
                "IFNULL((SELECT SUM(subtotal) FROM detreturjual WHERE no_retur_jual=rp.no_rawat),0) + " +
                "IFNULL((SELECT SUM(total) FROM resep_pulang WHERE no_rawat=rp.no_rawat),0)) AS obat_total, " +
            "SUM(IFNULL((SELECT SUM(biaya_item) FROM detail_periksa_lab WHERE no_rawat=rp.no_rawat),0) + " +
                "IFNULL((SELECT SUM(biaya) FROM periksa_lab WHERE no_rawat=rp.no_rawat),0)) AS laboratorium, " +
            "SUM(IFNULL((SELECT SUM(biaya) FROM periksa_radiologi WHERE no_rawat=rp.no_rawat),0)) AS radiologi, " +
            "SUM(IFNULL((SELECT SUM(tb.besar_biaya) FROM tambahan_biaya tb WHERE tb.no_rawat=rp.no_rawat),0)) AS pmi, " +
            "SUM(kb.totalpiutang - (" +
                "IFNULL(o.biayaoperator1, 0) + " +
                "IFNULL((SELECT SUM(hargasatuan) FROM beri_obat_operasi WHERE no_rawat=rp.no_rawat AND kd_obat LIKE 'OK%' AND kd_obat NOT IN ('DOK0000','OK00003133')), 0) + " +
                "IFNULL(o.biayadokter_anestesi, 0) + " +
                "IFNULL(o.biaya_omloop, 0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_inap_dr WHERE no_rawat=rp.no_rawat AND kd_dokter=o.dokter_anestesi), 0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_inap_dr WHERE no_rawat=rp.no_rawat AND kd_dokter=?), 0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_jl_dr WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_jl_drpr WHERE no_rawat=rp.no_rawat AND kd_jenis_prw NOT IN ('RJ94004','RJ94005','ugd004','ugd005')), 0) + " +
                "IFNULL((SELECT SUM(total) FROM detail_pemberian_obat WHERE no_rawat=rp.no_rawat AND kode_brng NOT LIKE 'B000003106%'), 0) + " +
                "IFNULL((SELECT SUM(subtotal) FROM detreturjual WHERE no_retur_jual=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(total) FROM resep_pulang WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(biaya_item) FROM detail_periksa_lab WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(biaya) FROM periksa_lab WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(biaya) FROM periksa_radiologi WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(besar_biaya) FROM tambahan_biaya WHERE no_rawat=rp.no_rawat), 0))" +
            ") AS operasional, " +
            "SUM(kb.dibayarbpjs - kb.totalpiutang) AS selisih, " +
            "SUM(" +
                "IFNULL(o.biayaoperator1, 0) + " +
                "IFNULL((SELECT SUM(hargasatuan) FROM beri_obat_operasi WHERE no_rawat=rp.no_rawat AND kd_obat LIKE 'OK%' AND kd_obat NOT IN ('DOK0000','OK00003133')), 0) + " +
                "IFNULL(o.biayadokter_anestesi, 0) + " +
                "IFNULL(o.biaya_omloop, 0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_inap_dr WHERE no_rawat=rp.no_rawat AND kd_dokter=o.dokter_anestesi), 0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_inap_dr WHERE no_rawat=rp.no_rawat AND kd_dokter=?), 0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_jl_dr WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(tarif_tindakandr) FROM rawat_jl_drpr WHERE no_rawat=rp.no_rawat AND kd_jenis_prw NOT IN ('RJ94004','RJ94005','ugd004','ugd005')), 0) + " +
                "IFNULL((SELECT SUM(total) FROM detail_pemberian_obat WHERE no_rawat=rp.no_rawat AND kode_brng NOT LIKE 'B000003106%'), 0) + " +
                "IFNULL((SELECT SUM(subtotal) FROM detreturjual WHERE no_retur_jual=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(total) FROM resep_pulang WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(biaya_item) FROM detail_periksa_lab WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(biaya) FROM periksa_lab WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(biaya) FROM periksa_radiologi WHERE no_rawat=rp.no_rawat), 0) + " +
                "IFNULL((SELECT SUM(besar_biaya) FROM tambahan_biaya WHERE no_rawat=rp.no_rawat), 0)" +
            ") AS total_jasa_penunjang " +
            "FROM rvp_klaim_bpjs kb " +
            "JOIN reg_periksa rp ON kb.no_rawat = rp.no_rawat " +
            "LEFT JOIN operasi o ON o.no_rawat = rp.no_rawat " +
            "LEFT JOIN dpjp_ranap dp ON dp.no_rawat = rp.no_rawat " +
            "WHERE dp.kd_dokter = ? AND kb.tanggal_rvp BETWEEN ? AND ? " +
            "GROUP BY bulan " +
            "ORDER BY STR_TO_DATE(CONCAT('01-', bulan), '%d-%m-%Y');";

        PreparedStatement ps = koneksi.prepareStatement(sql);
        String kdDokter = kddokter.getText();
        ps.setString(1, kdDokter);
        ps.setString(2, kdDokter);
        ps.setString(3, kdDokter);
        ps.setString(4, kdDokter);
        ps.setString(5, tgl1);
        ps.setString(6, tgl2);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String rowColor = (i % 2 == 0) ? "#f2f2f2" : "#ffffff";
            htmlContent.append("<tr style='background-color:" + rowColor + ";'>");
            htmlContent.append("<td style='text-align:center;'>").append(i).append("</td>");
            htmlContent.append("<td style='text-align:center;'>").append(rs.getString("bulan")).append("</td>");
//            htmlContent.append("<td style='text-align:left;'>").append(rs.getString("nm_pasien")).append("</td>");
//            htmlContent.append("<td style='text-align:left;'>").append(rs.getString("nama_operasi") != null ? rs.getString("nama_operasi") : "-").append("</td>");

            double[] nilaiNumerik = {
                rs.getDouble("biaya_rs"),
                rs.getDouble("paket_bpjs"),
                rs.getDouble("konsul_visite"),
                rs.getDouble("jasa_operasi"),
                rs.getDouble("alat_medis"),
                rs.getDouble("visite_anestesi"),
                rs.getDouble("jasa_anestesi"),
                rs.getDouble("tim_ok"),
                rs.getDouble("tim_ugd"),
                rs.getDouble("obat_total"),
                rs.getDouble("laboratorium"),
                rs.getDouble("radiologi"),
                rs.getDouble("pmi"),
                rs.getDouble("operasional"),
                rs.getDouble("total_jasa_penunjang"),
                rs.getDouble("selisih")
            };

            for (int j = 0; j < nilaiNumerik.length; j++) {
                total[j] += nilaiNumerik[j]; // Akumulasi total
                String style = "text-align:right;";
                if (nilaiNumerik[j] < 0) style += "color:red;";
                htmlContent.append("<td style='" + style + "'>")
                           .append(Valid.SetAngka(nilaiNumerik[j]))
                           .append("</td>");
            }

            htmlContent.append("</tr>");
            i++;
        }

        // Tambahkan baris total
        htmlContent.append("<tr style='background-color:#d9edf7; font-weight:bold;'>");
        htmlContent.append("<td colspan='2' style='text-align:right;'>TOTAL</td>");
        for (int j = 0; j < total.length; j++) {
            String style = "text-align:right;";
            if (total[j] < 0) style += "color:red;";
            htmlContent.append("<td style='" + style + "'>")
                       .append(Valid.SetAngka(total[j]))
                       .append("</td>");
        }
        htmlContent.append("</tr>");

        rs.close();
        ps.close();
    } catch (Exception e) {
        System.out.println("Notif prosesCari2: " + e);
    }

    htmlContent.append("</tbody></table>");
    htmlContent.append("</body></html>");
    LoadHTML1.setText(htmlContent.toString());
    
    htmlContent.append(
        "<html><head><style>" +
        "table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; }" +
        "th, td { border: 1px dotted #d3d3d3; padding: 5px; text-align: center; font-size:9px; }" +
        "th { background-color: #f2f2f2; }" +
        "@page { size: 33.02cm 21.59cm landscape; margin: 1cm; }" + // dibalik: width height
        "body { margin: 0; }" +
        "</style></head><body>"
    );
}


    
}
    

    

