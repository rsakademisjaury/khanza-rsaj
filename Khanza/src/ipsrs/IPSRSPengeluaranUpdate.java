package ipsrs;


import fungsi.WarnaTable;
import fungsi.WarnaTable2;
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
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import keuangan.Jurnal;

public class IPSRSPengeluaranUpdate extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Jurnal jur=new Jurnal();
    private Connection koneksi=koneksiDB.condb();
    private riwayatnonmedis Trackbarang=new riwayatnonmedis();
    private PreparedStatement ps;
    private ResultSet rs;
    private IPSRSCariPengeluaran form=new IPSRSCariPengeluaran(null,false);
    private int jml=0,i=0,row=0,index=0;
    private double ttl,keluar;
    private String[] kodebarang,namabarang,satuan,jumlah,stok;
    private double[] harga,total;
    private WarnaTable2 warna=new WarnaTable2();
    public boolean tampilkanpermintaan=true;
    private boolean sukses=true;

    /** Creates new form DlgProgramStudi
     * @param parent
     * @param modal */
    public IPSRSPengeluaranUpdate(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Object[] judul={"Kode","Nama Barang","Satuan","Stok","Permintaan","Sudah Diberikan","Revisi Pemberian","Harga","Total Baru"};
        tabMode=new DefaultTableModel(null,judul){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){ return colIndex==6; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch(columnIndex){
                    case 4: case 5: case 6: case 7: case 8:
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        
            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (columnIndex >= 4 && columnIndex <= 8) {
                    Double d = 0.0;
                    if (aValue != null) {
                        String s = aValue.toString().trim();
                        if (!s.isEmpty()) {
                            try {
                                s = s.replaceAll("\\s+", "");
                                if (s.indexOf(',') >= 0 && s.indexOf('.') < 0) s = s.replace(',', '.');
                                else s = s.replace(",", "");
                                d = Double.valueOf(s);
                            } catch (Exception ex) { d = 0.0; }
                        }
                    }
                    super.setValueAt(d, rowIndex, columnIndex);
                    if (columnIndex == 6 || columnIndex == 7) {
                        try {
                            double qtyBaru = ((Number)getValueAt(rowIndex, 6)).doubleValue();
                            double harga = ((Number)getValueAt(rowIndex, 7)).doubleValue();
                            super.setValueAt(qtyBaru * harga, rowIndex, 8);
                        } catch (Exception ignore) {
                            super.setValueAt(0.0, rowIndex, 8);
                        }
                    }
                } else {
                    super.setValueAt(aValue, rowIndex, columnIndex);
                }
            }
    };
        tbDokter.setModel(tabMode);
        
        // --- UI tweaks (row height, right-align numbers, numeric editor) ---
        try {
            tbDokter.setRowHeight(28);
            tbDokter.setShowHorizontalLines(true);
            tbDokter.setShowVerticalLines(false);
            tbDokter.setIntercellSpacing(new java.awt.Dimension(0, 1));
            javax.swing.table.DefaultTableCellRenderer right = new javax.swing.table.DefaultTableCellRenderer();
            right.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            tbDokter.getColumnModel().getColumn(3).setCellRenderer(right);
            tbDokter.getColumnModel().getColumn(4).setCellRenderer(right);
            tbDokter.getColumnModel().getColumn(5).setCellRenderer(right);
            tbDokter.getColumnModel().getColumn(6).setCellRenderer(right);
            tbDokter.getColumnModel().getColumn(7).setCellRenderer(right);
            tbDokter.getColumnModel().getColumn(8).setCellRenderer(right);
            java.text.NumberFormat df = new java.text.DecimalFormat("#0.########");
            javax.swing.text.NumberFormatter nf = new javax.swing.text.NumberFormatter(df);
            nf.setValueClass(java.lang.Double.class);
            nf.setAllowsInvalid(false);
            nf.setMinimum(0.0);
            javax.swing.JFormattedTextField ftf = new javax.swing.JFormattedTextField(nf);
            ftf.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            tbDokter.setDefaultEditor(java.lang.Double.class, new javax.swing.DefaultCellEditor(ftf));
        } catch (Exception ignore) {}
        // ---------------------------------------------------------------
    

        tbDokter.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 9; i++) {
            TableColumn column = tbDokter.getColumnModel().getColumn(i);
            if(i==0){column.setPreferredWidth(90);}else if(i==1){column.setPreferredWidth(320);}else if(i==2){column.setPreferredWidth(70);}else if(i==3){column.setPreferredWidth(60);}else if(i==4){column.setPreferredWidth(70);}else if(i==5){column.setPreferredWidth(110);}else if(i==6){column.setPreferredWidth(110);}else if(i==7){column.setPreferredWidth(90);}else if(i==8){column.setPreferredWidth(110);}else if(i==1){
                column.setPreferredWidth(110);
            }else if(i==2){
                column.setPreferredWidth(370);
            }else if(i==3){
                column.setPreferredWidth(100);
            }else if(i==4){
                column.setPreferredWidth(50);
            }else if(i==5){
                column.setPreferredWidth(100);
            }else if(i==6){
                column.setPreferredWidth(100);
            }
        }
        warna.kolom=0;
        tbDokter.setDefaultRenderer(Object.class,warna);
        
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
        tbDokter.setDefaultRenderer(Object.class, new WarnaPadLeft());

        // 2) kolom numerik 7..32 = kanan + padding
        WarnaPadRight padRight = new WarnaPadRight();
        for (int col = 4; col <= 8; col++) {
            tbDokter.getColumnModel().getColumn(col).setCellRenderer(padRight);
        }

        // 3) kolom center: No.(0), Tgl. Ralan(1), No. Nota(2), No. RM(3), Status Bayar(35)
        WarnaPadCenter padCenter = new WarnaPadCenter();
        int[] centerCols = {3};
        for (int col : centerCols) {
            tbDokter.getColumnModel().getColumn(col).setCellRenderer(padCenter);
        }

        NoKeluar.setDocument(new batasInput((byte)15).getKata(NoKeluar));
        Keterangan.setDocument(new batasInput((byte)100).getKata(Keterangan));
        kdptg.setDocument(new batasInput((byte)25).getKata(kdptg));        
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
        
        form.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
//                autoNomor();
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
        
        form.petugas.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(akses.getform().equals("DlgPengeluaranIPSRS")){
                    if(form.petugas.getTable().getSelectedRow()!= -1){                   
                        kdptg.setText(form.petugas.getTable().getValueAt(form.petugas.getTable().getSelectedRow(),0).toString());
                        nmptg.setText(form.petugas.getTable().getValueAt(form.petugas.getTable().getSelectedRow(),1).toString());
                    }  
                    kdptg.requestFocus();
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

        Kd2 = new widget.TextBox();
        Popup = new javax.swing.JPopupMenu();
        ppBersihkan = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        scrollPane1 = new widget.ScrollPane();
        tbDokter = new widget.Table();
        panelisi1 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        label10 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari1 = new widget.Button();
        BtnTambah = new widget.Button();
        label12 = new widget.Label();
        LTotal = new widget.Label();
        BtnCari = new widget.Button();
        BtnKeluar = new widget.Button();
        panelisi3 = new widget.panelisi();
        label15 = new widget.Label();
        NoKeluar = new widget.TextBox();
        label11 = new widget.Label();
        TglKeluar = new widget.Tanggal();
        label13 = new widget.Label();
        kdptg = new widget.TextBox();
        nmptg = new widget.TextBox();
        btnPetugas = new widget.Button();
        label16 = new widget.Label();
        Keterangan = new widget.TextBox();
        NoPermintaan = new widget.TextBox();
        Ruangan = new widget.TextBox();
        Pegawai = new widget.TextBox();
        label17 = new widget.Label();
        label14 = new widget.Label();
        label18 = new widget.Label();

        Kd2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        Kd2.setName("Kd2"); // NOI18N
        Kd2.setPreferredSize(new java.awt.Dimension(207, 23));

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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Transaksi Stok Keluar Barang Non Medis dan Penunjang ( Lab & RO ) ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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
        tbDokter.setComponentPopupMenu(Popup);
        tbDokter.setName("tbDokter"); // NOI18N
        tbDokter.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tbDokterPropertyChange(evt);
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
        BtnSimpan.setText("Update Pemberian");
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
        label10.setPreferredSize(new java.awt.Dimension(65, 23));
        panelisi1.add(label10);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(100, 30));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi1.add(TCari);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari1.setMnemonic('1');
        BtnCari1.setText("Cari ");
        BtnCari1.setToolTipText("Alt+1");
        BtnCari1.setName("BtnCari1"); // NOI18N
        BtnCari1.setPreferredSize(new java.awt.Dimension(80, 30));
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

        BtnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambah.setMnemonic('3');
        BtnTambah.setToolTipText("Alt+3");
        BtnTambah.setName("BtnTambah"); // NOI18N
        BtnTambah.setPreferredSize(new java.awt.Dimension(40, 30));
        BtnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahActionPerformed(evt);
            }
        });
        panelisi1.add(BtnTambah);

        label12.setText("Total :");
        label12.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(45, 23));
        panelisi1.add(label12);

        LTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LTotal.setText("0");
        LTotal.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        LTotal.setName("LTotal"); // NOI18N
        LTotal.setPreferredSize(new java.awt.Dimension(115, 23));
        panelisi1.add(LTotal);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnCari.setMnemonic('C');
        BtnCari.setText("Cari");
        BtnCari.setToolTipText("Alt+C");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(85, 30));
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
        panelisi1.add(BtnKeluar);

        internalFrame1.add(panelisi1, java.awt.BorderLayout.PAGE_END);

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 270));
        panelisi3.setLayout(null);

        label15.setText("No. Keluar  : ");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label15);
        label15.setBounds(30, 20, 80, 30);

        NoKeluar.setName("NoKeluar"); // NOI18N
        NoKeluar.setPreferredSize(new java.awt.Dimension(207, 23));
        NoKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoKeluarKeyPressed(evt);
            }
        });
        panelisi3.add(NoKeluar);
        NoKeluar.setBounds(120, 20, 130, 30);

        label11.setText("Tgl. Keluar  : ");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label11);
        label11.setBounds(270, 20, 75, 30);

        TglKeluar.setDisplayFormat("dd-MM-yyyy");
        TglKeluar.setName("TglKeluar"); // NOI18N
        TglKeluar.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TglKeluarItemStateChanged(evt);
            }
        });
        TglKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglKeluarKeyPressed(evt);
            }
        });
        panelisi3.add(TglKeluar);
        TglKeluar.setBounds(350, 20, 100, 30);

        label13.setText("Petugas  : ");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label13);
        label13.setBounds(40, 220, 70, 30);

        kdptg.setName("kdptg"); // NOI18N
        kdptg.setPreferredSize(new java.awt.Dimension(80, 23));
        kdptg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdptgKeyPressed(evt);
            }
        });
        panelisi3.add(kdptg);
        kdptg.setBounds(120, 220, 105, 30);

        nmptg.setEditable(false);
        nmptg.setName("nmptg"); // NOI18N
        nmptg.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi3.add(nmptg);
        nmptg.setBounds(230, 220, 300, 30);

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
        btnPetugas.setBounds(540, 220, 28, 30);

        label16.setText("Keterangan  : ");
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label16);
        label16.setBounds(10, 180, 100, 30);

        Keterangan.setName("Keterangan"); // NOI18N
        Keterangan.setPreferredSize(new java.awt.Dimension(207, 23));
        Keterangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKeyPressed(evt);
            }
        });
        panelisi3.add(Keterangan);
        Keterangan.setBounds(120, 180, 410, 30);

        NoPermintaan.setName("NoPermintaan"); // NOI18N
        NoPermintaan.setPreferredSize(new java.awt.Dimension(207, 23));
        NoPermintaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoPermintaanKeyPressed(evt);
            }
        });
        panelisi3.add(NoPermintaan);
        NoPermintaan.setBounds(120, 60, 130, 30);

        Ruangan.setName("Ruangan"); // NOI18N
        Ruangan.setPreferredSize(new java.awt.Dimension(207, 23));
        Ruangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RuanganKeyPressed(evt);
            }
        });
        panelisi3.add(Ruangan);
        Ruangan.setBounds(120, 140, 410, 30);

        Pegawai.setName("Pegawai"); // NOI18N
        Pegawai.setPreferredSize(new java.awt.Dimension(207, 23));
        Pegawai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PegawaiKeyPressed(evt);
            }
        });
        panelisi3.add(Pegawai);
        Pegawai.setBounds(120, 100, 410, 30);

        label17.setText("No. Permintaan  : ");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label17);
        label17.setBounds(10, 60, 100, 30);

        label14.setText("Karyawan  : ");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label14);
        label14.setBounds(30, 100, 80, 30);

        label18.setText("Departemen  : ");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label18);
        label18.setBounds(0, 140, 110, 30);

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

    if (NoKeluar.getText().trim().equals("")) {
        Valid.textKosong(NoKeluar, "No.Faktur");
        return;
    }
    if (Keterangan.getText().trim().equals("")) {
        Valid.textKosong(Keterangan, "Keterangan");
        return;
    }
    if (nmptg.getText().trim().equals("")) {
        Valid.textKosong(kdptg, "Petugas");
        return;
    }
    if (tbDokter.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "Maaf, data sudah habis...!!!!");
        TCari.requestFocus();
        return;
    }
    if (ttl <= 0) {
        JOptionPane.showMessageDialog(null, "Maaf, Silahkan masukkan pengeluaran...!!!!");
        tbDokter.requestFocus();
        return;
    }

    int reply = JOptionPane.showConfirmDialog(
        rootPane,
        "Eeiiiiiits, udah bener belum data yang mau disimpan..??",
        "Konfirmasi",
        JOptionPane.YES_NO_OPTION
    );
    if (reply != JOptionPane.YES_OPTION) {
        return;
    }

    Sequel.AutoComitFalse();
    boolean sukses = true;

    try {
        int jml = tbDokter.getRowCount();
        for (int i = 0; i < jml; i++) {
            String kodeBrng = tbDokter.getValueAt(i, 0).toString(); // Kode
            String satuan   = tbDokter.getValueAt(i, 2).toString(); // Satuan

            // --- jumlah lama di DB ---
            double jumlahLama = Sequel.cariIsiAngka(
                "SELECT jumlah FROM ipsrsdetailpengeluaran WHERE no_keluar=? AND kode_brng=?",
                new String[]{ NoKeluar.getText(), kodeBrng }
            );

            // --- baca jumlah baru dari tabel; kalau kosong -> anggap sama dengan lama (tidak berubah) ---
            double jumlahBaru;
            Object vBaru = tbDokter.getValueAt(i, 6);
            String sBaru = (vBaru == null) ? "" : vBaru.toString().trim();
            if (sBaru.isEmpty()) {
                jumlahBaru = jumlahLama;
            } else {
                try { jumlahBaru = Double.parseDouble(sBaru); } catch (Exception ex) { jumlahBaru = jumlahLama; }
            }
            if (jumlahBaru < 0) jumlahBaru = 0;

            // harga
            double hargaVal = 0;
            try { hargaVal = Double.parseDouble(tbDokter.getValueAt(i, 7).toString()); } catch (Exception ex) { hargaVal = 0; }
            double subtotalVal = jumlahBaru * hargaVal;

            double selisih = jumlahBaru - jumlahLama;
            if (Math.abs(selisih) < 1e-9) {
                // tidak ada perubahan di baris ini
                continue;
            }

            // --- UPDATE detail kalau sudah ada ---
            boolean updated = Sequel.mengedittf(
                "ipsrsdetailpengeluaran",
                "no_keluar=? and kode_brng=?",
                "jumlah=?, harga=?, total=?",
                5,
                new String[]{
                    String.valueOf(jumlahBaru),
                    String.valueOf(hargaVal),
                    String.valueOf(subtotalVal),
                    NoKeluar.getText(),
                    kodeBrng
                }
            );

            // --- INSERT kalau belum ada ---
            boolean suksesItem = updated;
            if (!updated) {
                suksesItem = Sequel.menyimpantf2(
                    "ipsrsdetailpengeluaran",
                    "?,?,?,?,?,?",
                    "Transaksi Pengeluaran",
                    6,
                    new String[]{
                        NoKeluar.getText(),
                        kodeBrng,
                        satuan,
                        String.valueOf(jumlahBaru),
                        String.valueOf(hargaVal),
                        String.valueOf(subtotalVal)
                    }
                );
            }

            // --- koreksi stok hanya sebesar SELISIH ---
            if (suksesItem) {
                Trackbarang.catatRiwayat(kodeBrng, 0, Math.abs(selisih), "Stok Keluar", akses.getkode(), "Update");
                if (selisih > 0) {
                    // tambah jumlah → stok berkurang
                    suksesItem = Sequel.mengeditStok(kodeBrng, selisih, true);
                } else {
                    // kurangi jumlah → stok dikembalikan
                    suksesItem = Sequel.mengeditStok(kodeBrng, Math.abs(selisih), false);
                }
            }

            if (!suksesItem) { sukses = false; break; }
        }

        // --- Jurnal jika semua update berhasil ---
        if (sukses) {
            Sequel.queryu("delete from tampjurnal");
            Sequel.menyimpan("tampjurnal", "?,?,?,?", 4, new String[]{
                Sequel.cariIsi("select Stok_Keluar_Ipsrs from set_akun"),
                "PERSEDIAAN BARANG", "" + (ttl), "0"
            });
            Sequel.menyimpan("tampjurnal", "?,?,?,?", 4, new String[]{
                Sequel.cariIsi("select Kontra_Stok_Keluar_Ipsrs from set_akun"),
                "KAS DI TANGAN", "0", "" + (ttl)
            });
            sukses = jur.simpanJurnal(NoKeluar.getText(), "U",
                "UPDATE PENGELUARAN BARANG NON MEDIS OLEH " + akses.getkode());
        }

        if (sukses) {
            Sequel.Commit();
            getData();
        } else {
            JOptionPane.showMessageDialog(null,
                "Terjadi kesalahan saat pemrosesan data, transaksi dibatalkan.\nPeriksa kembali data sebelum melanjutkan menyimpan..!!");
            Sequel.RollBack();
        }
    } catch (Exception e) {
        Sequel.RollBack();
        JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
    } finally {
        Sequel.AutoComitTrue();
    }

    dispose();
    
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
            tampil();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari1.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            kdptg.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbDokter.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        tampil();
}//GEN-LAST:event_BtnCari1ActionPerformed

private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            tampil();
        }else{
            Valid.pindah(evt, BtnSimpan, BtnKeluar);
        }
}//GEN-LAST:event_BtnCari1KeyPressed

private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
            for(i=0;i<tbDokter.getRowCount();i++){ 
                tbDokter.setValueAt("",i,6);
                tbDokter.setValueAt(0,i,8);
            }
}//GEN-LAST:event_ppBersihkanActionPerformed

private void tbDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDokterKeyPressed
        if(tbDokter.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_ENTER){    
                getData();
                TCari.setText("");
                TCari.requestFocus();
            }else if(evt.getKeyCode()==KeyEvent.VK_DELETE){
                i=tbDokter.getSelectedRow();
                if(i!= -1){
                    tbDokter.setValueAt("", i,6);
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCari.setText("");
                TCari.requestFocus();
            }else if(evt.getKeyCode()==KeyEvent.VK_RIGHT){
                getData();
            }
        }
}//GEN-LAST:event_tbDokterKeyPressed

private void NoKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoKeluarKeyPressed
        Valid.pindah(evt, BtnSimpan, Keterangan);
}//GEN-LAST:event_NoKeluarKeyPressed

private void TglKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglKeluarKeyPressed
        Valid.pindah(evt,Keterangan,kdptg);
}//GEN-LAST:event_TglKeluarKeyPressed

private void kdptgKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdptgKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            nmptg.setText(form.petugas.tampil3(kdptg.getText()));          
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            nmptg.setText(form.petugas.tampil3(kdptg.getText()));
            TglKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            nmptg.setText(form.petugas.tampil3(kdptg.getText()));
            BtnSimpan.requestFocus();  
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnPetugasActionPerformed(null);
        }
}//GEN-LAST:event_kdptgKeyPressed

private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        akses.setform("DlgPengeluaranIPSRS");
        form.petugas.emptTeks();
        form.petugas.isCek();
        form.petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.petugas.setLocationRelativeTo(internalFrame1);
        form.petugas.setAlwaysOnTop(false);
        form.petugas.setVisible(true);
}//GEN-LAST:event_btnPetugasActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(tampilkanpermintaan==true){
            tampil();
        }            
    }//GEN-LAST:event_formWindowOpened

    private void KeteranganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKeyPressed
        Valid.pindah(evt, NoKeluar,TglKeluar);
    }//GEN-LAST:event_KeteranganKeyPressed

    private void BtnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        akses.setform("DlgPembelianIPSRS");
        IPSRSBarang barang=new IPSRSBarang(null,false);
        barang.emptTeks();
        barang.isCek();
        barang.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        barang.setLocationRelativeTo(internalFrame1);
        barang.setAlwaysOnTop(false);
        barang.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnTambahActionPerformed

    private void TglKeluarItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TglKeluarItemStateChanged
        try {
//            autoNomor();
        } catch (Exception e) {
        }        
    }//GEN-LAST:event_TglKeluarItemStateChanged

    private void tbDokterPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbDokterPropertyChange
        if(tbDokter.isEditing()){ return; }
        getData();
    }//GEN-LAST:event_tbDokterPropertyChange

    private void NoPermintaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoPermintaanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NoPermintaanKeyPressed

    private void RuanganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RuanganKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_RuanganKeyPressed

    private void PegawaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PegawaiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PegawaiKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            IPSRSPengeluaranUpdate dialog = new IPSRSPengeluaranUpdate(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari;
    private widget.Button BtnCari1;
    private widget.Button BtnKeluar;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambah;
    private widget.TextBox Kd2;
    private widget.TextBox Keterangan;
    private widget.Label LTotal;
    private widget.TextBox NoKeluar;
    private widget.TextBox NoPermintaan;
    private widget.TextBox Pegawai;
    private javax.swing.JPopupMenu Popup;
    private widget.TextBox Ruangan;
    private widget.TextBox TCari;
    private widget.Tanggal TglKeluar;
    private widget.Button btnPetugas;
    private widget.InternalFrame internalFrame1;
    private widget.TextBox kdptg;
    private widget.Label label10;
    private widget.Label label11;
    private widget.Label label12;
    private widget.Label label13;
    private widget.Label label14;
    private widget.Label label15;
    private widget.Label label16;
    private widget.Label label17;
    private widget.Label label18;
    private widget.TextBox nmptg;
    private widget.panelisi panelisi1;
    private widget.panelisi panelisi3;
    private javax.swing.JMenuItem ppBersihkan;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbDokter;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
    row = tbDokter.getRowCount();
    jml = 0;
    for (i = 0; i < row; i++) {
        if (!tbDokter.getValueAt(i, 0).toString().equals("")) {
            jml++;
        }
    }

    kodebarang = new String[jml];
    namabarang = new String[jml];
    satuan = new String[jml];
    jumlah = new String[jml];
    stok = new String[jml];
    harga = new double[jml];
    total = new double[jml];

    index = 0;
    for (i = 0; i < row; i++) {
        if (!tbDokter.getValueAt(i, 0).toString().equals("")) {
            // pertahankan nilai yang sudah ada di tabel (9 kolom)
            jumlah[index] = tbDokter.getValueAt(i, 6).toString();
            kodebarang[index] = tbDokter.getValueAt(i, 0).toString();
            namabarang[index] = tbDokter.getValueAt(i, 1).toString();
            satuan[index] = tbDokter.getValueAt(i, 2).toString();
            stok[index] = tbDokter.getValueAt(i, 3).toString();
            try { harga[index] = Double.parseDouble(tbDokter.getValueAt(i, 7).toString()); } catch (Exception ex) { harga[index] = 0; }
            try { total[index] = Double.parseDouble(tbDokter.getValueAt(i, 8).toString()); } catch (Exception ex) { total[index] = 0; }
            index++;
        }
    }

    Valid.tabelKosong(tabMode);

    // restore baris-baris yang sudah ada
    for (i = 0; i < jml; i++) {
        tabMode.addRow(new Object[]{ kodebarang[i], namabarang[i], satuan[i], stok[i], 0.0, 0.0, jumlah[i], harga[i], total[i] });
    }

    // tambahkan hasil pencarian barang baru (default nol)
    try {
        ps = koneksi.prepareStatement(
            "select ipsrsbarang.kode_brng, concat(ipsrsbarang.nama_brng,' (',ipsrsbarang.jenis,')'), ipsrsbarang.kode_sat, stok, " +
            "ipsrsbarang.harga from ipsrsbarang where ipsrsbarang.status='1' and " +
            "(ipsrsbarang.kode_brng like ? or ipsrsbarang.nama_brng like ? or ipsrsbarang.jenis like ?) " +
            "order by ipsrsbarang.nama_brng"
        );

        try {
            ps.setString(1, "%" + TCari.getText().trim() + "%");
            ps.setString(2, "%" + TCari.getText().trim() + "%");
            ps.setString(3, "%" + TCari.getText().trim() + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                    0.0, 0.0, 0.0, rs.getDouble(5), 0.0
                });
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (rs != null) { rs.close(); }
            if (ps != null) { ps.close(); }
        }
    } catch (SQLException e) {
        System.out.println("Notifikasi : " + e);
    }
    }
    
    public void isCek(){
//        autoNomor();
        TCari.requestFocus();
        if(akses.getjml2()>=1){
            kdptg.setEditable(false);
            btnPetugas.setEnabled(false);
            kdptg.setText(akses.getkode());
            BtnSimpan.setEnabled(akses.getipsrs_stok_keluar());
            BtnTambah.setEnabled(akses.getipsrs_barang());
            nmptg.setText(form.petugas.tampil3(kdptg.getText()));
        }        
    }

    
    private void getData(){
        row=tbDokter.getSelectedRow();
        if(row!=-1){
            double diminta = num(tbDokter.getValueAt(row,4));
            double lama    = num(tbDokter.getValueAt(row,5));
            double baru    = num(tbDokter.getValueAt(row,6));
            double harga   = num(tbDokter.getValueAt(row,7));
            double sisa = Math.max(0, diminta - lama);
            if(baru > sisa){
                JOptionPane.showMessageDialog(rootPane,"Jumlah baru melebihi sisa permintaan ("+Valid.SetAngka(sisa)+")");
                baru = sisa;
                tbDokter.setValueAt(baru, row, 6);
            }
            tbDokter.setValueAt(baru*harga, row, 8);
        }
        isHitung();
    }
    

    
    public void tampil(String nopermintaan,String keterangan) {
        Valid.tabelKosong(tabMode);        
        try{
            Keterangan.setText(keterangan);
            String noKeluarDB = Sequel.cariIsi(
                "SELECT no_keluar FROM ipsrspengeluaran WHERE keterangan LIKE ? LIMIT 1",
                "%"+nopermintaan+"%"
            );
            if(noKeluarDB!=null && !noKeluarDB.trim().equals("")){
                NoKeluar.setText(noKeluarDB);
            }
            ps = koneksi.prepareStatement(
                "SELECT b.kode_brng, CONCAT(b.nama_brng,' (',b.jenis,')') AS nama, " +
                "b.kode_sat, b.stok, dpm.jumlah AS diminta, COALESCE(idp.jumlah,0) AS diberikan_lama, b.harga " +
                "FROM ipsrsbarang b " +
                "JOIN detail_permintaan_non_medis dpm ON b.kode_brng=dpm.kode_brng " +
                "LEFT JOIN ipsrsdetailpengeluaran idp ON idp.kode_brng=b.kode_brng AND idp.no_keluar=? " +
                "WHERE dpm.no_permintaan=? " +
                "ORDER BY b.nama_brng"
            );
            ps.setString(1, NoKeluar.getText());
            ps.setString(2, nopermintaan);
            rs=ps.executeQuery();
            while(rs.next()){
                tabMode.addRow(new Object[]{
                    rs.getString("kode_brng"), rs.getString("nama"), rs.getString("kode_sat"),
                    rs.getString("stok"),
                    rs.getDouble("diminta"), rs.getDouble("diberikan_lama"),
                    0.0, rs.getDouble("harga"), 0.0
                });
            } 
        }catch(Exception e){
            System.out.println(e);
        }finally{
            if(rs!=null){ try{ rs.close(); }catch(Exception ig){} }
            if(ps!=null){ try{ ps.close(); }catch(Exception ig){} }
        }
        isHitung();
    }
    
    
    private void isHitung(){
        ttl=0;
        jml=tbDokter.getRowCount();
        for(i=0;i<jml;i++){    
            keluar=0;
            try {
                keluar=Double.parseDouble(tbDokter.getValueAt(i,8).toString());
            }catch (Exception e) {
                keluar=0;                 
            }
            ttl=ttl+keluar;
        }
        LTotal.setText(Valid.SetAngka(ttl));ttl=0;
        jml=tbDokter.getRowCount();
        for(i=0;i<jml;i++){    
            keluar=0;
            try {
                keluar=Double.parseDouble(tbDokter.getValueAt(i,8).toString());
            }catch (Exception e) {
                keluar=0;                 
            }
            ttl=ttl+keluar;
        }
        LTotal.setText(Valid.SetAngka(ttl));
    }
    
public void setNoRm(String nopermintaan, String ruangan, String petugas) {
        NoPermintaan.setText(nopermintaan);
        Ruangan.setText(ruangan);
        Pegawai.setText(petugas);        
    }    

    
    // --- Helper: safe number parse for table values (accepts "", "10", "10,5", "1,000.25")
    private static double num(Object v){
        if(v==null) return 0.0;
        String s = v.toString().trim();
        if(s.isEmpty()) return 0.0;
        try{
            s = s.replaceAll("\\s+", "");
            if(s.indexOf(',') >= 0 && s.indexOf('.') < 0){
                s = s.replace(',', '.');
            } else {
                s = s.replace(",", "");
            }
            return Double.parseDouble(s);
        }catch(Exception e){
            return 0.0;
        }
    }
    }
