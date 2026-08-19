/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgPemberianObat.java
 *
 * Created on 27 Mei 10, 14:52:31
 */

package laporan;

import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Date;
import javax.swing.JOptionPane;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import javax.swing.event.DocumentEvent;
import rekammedis.RMRiwayatPerawatan;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;


/**
 *
 * @author perpustakaan
 */
public class DlgDiagnosaPenyakit extends javax.swing.JDialog {
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();  
    // cache nama penyakit biar hemat query
    private final Map<String,String> cacheNmPenyakit = new HashMap<>();

    // util kecil
    private static String safeStr(String s){ return (s == null ? "" : s); }
    private static String safeCode(String s){ return (s == null || s.trim().isEmpty()) ? "" : s; }



    /** Creates new form DlgPemberianObat
     * @param parent
     * @param modal */
    public DlgDiagnosaPenyakit(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));       
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        panelDiagnosa1.setRM(TNoRw.getText(),TNoRM.getText(),Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
                        panelDiagnosa1.pilihTab();
                        LCount.setText(panelDiagnosa1.getRecord()+"");
                    }                        
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        panelDiagnosa1.setRM(TNoRw.getText(),TNoRM.getText(),Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
                        panelDiagnosa1.pilihTab();
                        LCount.setText(panelDiagnosa1.getRecord()+"");
                    } 
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        panelDiagnosa1.setRM(TNoRw.getText(),TNoRM.getText(),Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
                        panelDiagnosa1.pilihTab();
                        LCount.setText(panelDiagnosa1.getRecord()+"");
                    } 
                }
            });
        } 
        
        panelDiagnosa1.tbDiagnosaPasien.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow()!= -1){
                    TNoRw.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),2).toString());
                    TNoRM.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),3).toString());
                    TPasien.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),4).toString());
                    KodeP.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),5).toString());
                    NamaP.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),6).toString());
                    TPrioritas.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),9).toString());
                }                
            }

            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        
        panelDiagnosa1.tbDiagnosaPasien.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                if(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow()!= -1){
                    TNoRw.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),2).toString());
                    TNoRM.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),3).toString());
                    TPasien.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),4).toString());
                    KodeP.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),5).toString());
                    NamaP.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),6).toString());
                    TPrioritas.setText(panelDiagnosa1.tbDiagnosaPasien.getValueAt(panelDiagnosa1.tbDiagnosaPasien.getSelectedRow(),9).toString());
                } 
            }
        });
        
        panelDiagnosa1.tbTindakanPasien.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(panelDiagnosa1.tbTindakanPasien.getSelectedRow()!= -1){
                    TNoRw.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),2).toString());
                    TNoRM.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),3).toString());
                    TPasien.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),4).toString());
                    KodeP.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),5).toString());
                    TPrioritas.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),8).toString());
                    
                }                
            }

            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        
        panelDiagnosa1.tbTindakanPasien.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                if(panelDiagnosa1.tbTindakanPasien.getSelectedRow()!= -1){
                    TNoRw.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),2).toString());
                    TNoRM.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),3).toString());
                    TPasien.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),4).toString());
                    KodeP.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),5).toString());
                    TPrioritas.setText(panelDiagnosa1.tbTindakanPasien.getValueAt(panelDiagnosa1.tbTindakanPasien.getSelectedRow(),8).toString());
                } 
            }
        });               

    }

    //private DlgCariObatPenyakit dlgobtpny=new DlgCariObatPenyakit(null,false);
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnRiwayatPerawatan = new widget.Button();
        jLabel10 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel14 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel19 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        FormInput = new widget.PanelBiasa();
        jLabel3 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        jLabel17 = new widget.Label();
        Status = new widget.ComboBox();
        TPrioritas = new widget.TextBox();
        jLabel18 = new widget.Label();
        KodeP = new widget.TextBox();
        jLabel20 = new widget.Label();
        BtnUpdate = new widget.Button();
        NamaP = new widget.TextBox();
        panelDiagnosa1 = new laporan.PanelDiagnosa();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Riwayat Diagnosa & Prosedur Tindakan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

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

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
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

        BtnRiwayatPerawatan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/open.png"))); // NOI18N
        BtnRiwayatPerawatan.setMnemonic('6');
        BtnRiwayatPerawatan.setText("Riwayat Perawatan");
        BtnRiwayatPerawatan.setToolTipText("Alt+6");
        BtnRiwayatPerawatan.setName("BtnRiwayatPerawatan"); // NOI18N
        BtnRiwayatPerawatan.setPreferredSize(new java.awt.Dimension(150, 30));
        BtnRiwayatPerawatan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRiwayatPerawatanActionPerformed(evt);
            }
        });
        BtnRiwayatPerawatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnRiwayatPerawatanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnRiwayatPerawatan);

        jLabel10.setText("Record :");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass8.add(jLabel10);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(110, 23));
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
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));

        jLabel14.setText("Tgl.Rawat :");
        jLabel14.setName("jLabel14"); // NOI18N
        jLabel14.setPreferredSize(new java.awt.Dimension(63, 23));
        panelGlass9.add(jLabel14);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "24-07-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass9.add(DTPCari1);

        jLabel19.setText("s.d");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(18, 23));
        panelGlass9.add(jLabel19);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "24-07-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass9.add(DTPCari2);

        jSeparator5.setBackground(new java.awt.Color(220, 225, 215));
        jSeparator5.setForeground(new java.awt.Color(220, 225, 215));
        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setName("jSeparator5"); // NOI18N
        jSeparator5.setOpaque(true);
        jSeparator5.setPreferredSize(new java.awt.Dimension(1, 23));
        panelGlass9.add(jSeparator5);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(87, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(367, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('6');
        BtnCari.setToolTipText("Alt+6");
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

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(865, 80));
        FormInput.setLayout(null);

        jLabel3.setText("No.Rawat :");
        jLabel3.setName("jLabel3"); // NOI18N
        FormInput.add(jLabel3);
        jLabel3.setBounds(-2, 10, 70, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(71, 10, 140, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(215, 10, 60, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.setPreferredSize(new java.awt.Dimension(25, 28));
        FormInput.add(TPasien);
        TPasien.setBounds(280, 10, 340, 23);

        jLabel17.setText("Status : ");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(10, 40, 60, 23);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ralan", "Ranap" }));
        Status.setName("Status"); // NOI18N
        Status.setPreferredSize(new java.awt.Dimension(308, 23));
        FormInput.add(Status);
        Status.setBounds(70, 40, 70, 23);

        TPrioritas.setEditable(false);
        TPrioritas.setHighlighter(null);
        TPrioritas.setName("TPrioritas"); // NOI18N
        FormInput.add(TPrioritas);
        TPrioritas.setBounds(215, 40, 60, 23);

        jLabel18.setText("Prioritas : ");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(150, 40, 60, 23);

        KodeP.setHighlighter(null);
        KodeP.setName("KodeP"); // NOI18N
        FormInput.add(KodeP);
        KodeP.setBounds(320, 40, 55, 23);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setText("Kode  : ");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(280, 40, 50, 23);

        BtnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh1.png"))); // NOI18N
        BtnUpdate.setMnemonic('6');
        BtnUpdate.setText("Ganti Diagnosa");
        BtnUpdate.setToolTipText("Alt+6");
        BtnUpdate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnUpdate.setName("BtnUpdate"); // NOI18N
        BtnUpdate.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateActionPerformed(evt);
            }
        });
        BtnUpdate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateKeyPressed(evt);
            }
        });
        FormInput.add(BtnUpdate);
        BtnUpdate.setBounds(630, 40, 120, 23);

        NamaP.setHighlighter(null);
        NamaP.setName("NamaP"); // NOI18N
        FormInput.add(NamaP);
        NamaP.setBounds(380, 40, 240, 23);

        internalFrame1.add(FormInput, java.awt.BorderLayout.PAGE_START);

        panelDiagnosa1.setName("panelDiagnosa1"); // NOI18N
        internalFrame1.add(panelDiagnosa1, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            panelDiagnosa1.setRM(TNoRw.getText(),TNoRM.getText(),Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
            panelDiagnosa1.pilihTab();
            LCount.setText(panelDiagnosa1.getRecord()+"");
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        panelDiagnosa1.setRM(TNoRw.getText(),"",Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
        panelDiagnosa1.pilihTab();
        LCount.setText(panelDiagnosa1.getRecord()+"");
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        Valid.pindah(evt,TCari,panelDiagnosa1.Diagnosa);
}//GEN-LAST:event_TNoRwKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"No.Rawat");
        }else{ 
            panelDiagnosa1.setRM(TNoRw.getText(),TNoRM.getText(),Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
            panelDiagnosa1.simpan();
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,panelDiagnosa1.Diagnosa,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        panelDiagnosa1.batal();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnBatalActionPerformed(null);
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(TPasien.getText().trim().equals("")){
             JOptionPane.showMessageDialog(null,"Maaf, Gagal menghapus. Pilih dulu   yang mau dihapus.\nKlik data pada table untuk memilih...!!!!");
        }else if(!(TPasien.getText().trim().equals(""))){
            panelDiagnosa1.setRM(TNoRw.getText(),TNoRM.getText(),Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
            panelDiagnosa1.hapus();            
            panelDiagnosa1.tampil();
            panelDiagnosa1.tampil2();
        }
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnPrint);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        panelDiagnosa1.cetak();
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnAll);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        panelDiagnosa1.setRM(TNoRw.getText(),"",Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
        panelDiagnosa1.pilihTab();
        LCount.setText(panelDiagnosa1.getRecord()+"");
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        panelDiagnosa1.setRM(TNoRw.getText(),TNoRM.getText(),Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
        panelDiagnosa1.pilihTab();
        LCount.setText(panelDiagnosa1.getRecord()+"");
    }//GEN-LAST:event_formWindowOpened

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        if(this.getHeight()<605){   
            panelDiagnosa1.ScrollInput.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            panelDiagnosa1.FormData.setPreferredSize(new Dimension(panelDiagnosa1.FormData.WIDTH,420));
            if(this.getWidth()<900){
                panelDiagnosa1.ScrollInput.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);                                
                panelDiagnosa1.FormData.setPreferredSize(new Dimension(890,420));
            }else{
                panelDiagnosa1.ScrollInput.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);                
            }
        }else{
            panelDiagnosa1.ScrollInput.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);            
            if(this.getWidth()<900){
                panelDiagnosa1.ScrollInput.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);                                
                panelDiagnosa1.FormData.setPreferredSize(new Dimension(890,panelDiagnosa1.FormData.HEIGHT));
            }else{
                panelDiagnosa1.ScrollInput.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);                
            }
        }
    }//GEN-LAST:event_formWindowActivated

    private void BtnRiwayatPerawatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRiwayatPerawatanActionPerformed
        if(TNoRw.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
            TCari.requestFocus();
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            RMRiwayatPerawatan resume=new RMRiwayatPerawatan(null,false);
            resume.setNoRm2(TNoRw.getText(),TNoRM.getText(),TPasien.getText());
            resume.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
            resume.setLocationRelativeTo(internalFrame1);
            resume.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnRiwayatPerawatanActionPerformed

    private void BtnRiwayatPerawatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnRiwayatPerawatanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnRiwayatPerawatanKeyPressed

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
   if (TNoRw.getText().trim().isEmpty() || TPasien.getText().trim().isEmpty()) {
        Valid.textKosong(TNoRw, "No.Rawat");
        return;
    }
    int row = panelDiagnosa1.getTable().getSelectedRow();
    if (row < 0) {
        javax.swing.JOptionPane.showMessageDialog(null, "Silakan pilih baris diagnosa yang ingin diubah.");
        return;
    }

    // ====== SESUAIKAN INDEKS KOLUMNYA DENGAN TABEL KAMU ======
    final int COL_KD_PENYAKIT = 5; // Kode
    final int COL_STATUS      = 7; // Ralan/Ranap
    final int COL_PRIORITAS   = 9; // 1,2,3...
    // =========================================================

    String noRawat         = TNoRw.getText().trim();
    String kdPenyakitLama  = String.valueOf(panelDiagnosa1.getTable().getValueAt(row, COL_KD_PENYAKIT)).trim();
    String status          = String.valueOf(panelDiagnosa1.getTable().getValueAt(row, COL_STATUS)).trim();

    int prioritas;
    try {
        prioritas = Integer.parseInt(String.valueOf(panelDiagnosa1.getTable().getValueAt(row, COL_PRIORITAS)));
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(null, "Prioritas tidak valid.");
        return;
    }

    String kdPenyakitBaru = KodeP.getText().trim();
    if (kdPenyakitBaru.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(null, "Kode penyakit baru belum diisi.");
        return;
    }
    if (kdPenyakitBaru.equalsIgnoreCase(kdPenyakitLama)) {
        javax.swing.JOptionPane.showMessageDialog(null, "Kode penyakit baru sama dengan yang lama.");
        return;
    }

    try {
        boolean ok = gantiDiagnosa(noRawat, status, prioritas, kdPenyakitLama, kdPenyakitBaru);
        if (ok) {
            javax.swing.JOptionPane.showMessageDialog(null, "Diagnosa berhasil diganti & resume tersinkron.");
            // refresh tampilan tabel kamu
            // tampil(); // atau method refresh-mu
        }
    } catch (Exception ex) {
        javax.swing.JOptionPane.showMessageDialog(null, "Gagal mengganti diagnosa : " + ex.getMessage());
    }
    
    }//GEN-LAST:event_BtnUpdateActionPerformed

private boolean gantiDiagnosa(String noRawat, String status, int prioritas,
                              String kdLama, String kdBaru) throws Exception {
    if (noRawat == null || noRawat.isEmpty()) throw new IllegalArgumentException("no_rawat kosong");
    if (status == null || status.isEmpty())   throw new IllegalArgumentException("status kosong");

    // validasi kd_penyakit baru ada di master
    if (!existsPenyakit(kdBaru)) {
        throw new IllegalStateException("Kode penyakit baru tidak ditemukan di master penyakit.");
    }

    koneksi.setAutoCommit(false);

    String sqlCekTarget = "SELECT COUNT(*) FROM diagnosa_pasien " +
            "WHERE no_rawat=? AND LOWER(status)=LOWER(?) AND prioritas=? AND kd_penyakit=?";
    String sqlCekDuplikat = "SELECT COUNT(*) FROM diagnosa_pasien " +
            "WHERE no_rawat=? AND LOWER(status)=LOWER(?) AND kd_penyakit=?";
    String sqlUpdate = "UPDATE diagnosa_pasien SET kd_penyakit=? " +
            "WHERE no_rawat=? AND LOWER(status)=LOWER(?) AND prioritas=? AND kd_penyakit=?";

    try (PreparedStatement psT = koneksi.prepareStatement(sqlCekTarget);
         PreparedStatement psD = koneksi.prepareStatement(sqlCekDuplikat);
         PreparedStatement psU = koneksi.prepareStatement(sqlUpdate)) {

        // pastikan baris target ada
        psT.setString(1, noRawat);
        psT.setString(2, status);
        psT.setInt(3, prioritas);
        psT.setString(4, kdLama);
        try (ResultSet rs = psT.executeQuery()) {
            rs.next();
            if (rs.getInt(1) == 0) {
                koneksi.rollback();
                throw new IllegalStateException("Baris diagnosa target tidak ditemukan (mungkin sudah berubah).");
            }
        }

        // cegah dupl. kd baru pada status yg sama
        psD.setString(1, noRawat);
        psD.setString(2, status);
        psD.setString(3, kdBaru);
        try (ResultSet rs = psD.executeQuery()) {
            rs.next();
            if (rs.getInt(1) > 0) {
                koneksi.rollback();
                throw new IllegalStateException("Kode penyakit baru sudah ada pada daftar diagnosa status ini.");
            }
        }

        // update
        psU.setString(1, kdBaru);
        psU.setString(2, noRawat);
        psU.setString(3, status);
        psU.setInt(4, prioritas);
        psU.setString(5, kdLama);
        int affected = psU.executeUpdate();
        if (affected == 0) {
            koneksi.rollback();
            throw new IllegalStateException("Tidak ada baris yang ter-update.");
        }

        // sinkron resume
        if ("Ralan".equalsIgnoreCase(status)) {
            syncResumePasienRalan(noRawat);
        } else if ("Ranap".equalsIgnoreCase(status)) {
            syncResumePasienRanap(noRawat);
        } else {
            // status tak dikenal: amankan keduanya
            syncResumePasienRalan(noRawat);
            syncResumePasienRanap(noRawat);
        }

        koneksi.commit();
        return true;
    } catch (Exception e) {
        try { koneksi.rollback(); } catch (Exception ig) {}
        throw e;
    } finally {
        try { koneksi.setAutoCommit(true); } catch (Exception ig) {}
    }
}

private void syncResumePasienRalan(String noRawat) throws Exception {
    syncResumeCommon(noRawat, "Ralan", 
        "resume_pasien",
        new String[]{
            "diagnosa_utama","kd_diagnosa_utama",
            "diagnosa_sekunder","kd_diagnosa_sekunder",
            "diagnosa_sekunder2","kd_diagnosa_sekunder2",
            "diagnosa_sekunder3","kd_diagnosa_sekunder3",
            "diagnosa_sekunder4","kd_diagnosa_sekunder4"
        }
    );
}

private void syncResumePasienRanap(String noRawat) throws Exception {
    syncResumeCommon(noRawat, "Ranap", 
        "resume_pasien_ranap",
        new String[]{
            "diagnosa_utama","kd_diagnosa_utama",
            "diagnosa_sekunder","kd_diagnosa_sekunder",
            "diagnosa_sekunder2","kd_diagnosa_sekunder2",
            "diagnosa_sekunder3","kd_diagnosa_sekunder3",
            "diagnosa_sekunder4","kd_diagnosa_sekunder4"
        }
    );
}

private void syncResumeCommon(String noRawat, String statusFilter, String resumeTable, String[] kolomPairs) throws Exception {
    final String sqlDiag = "SELECT prioritas, kd_penyakit " +
                           "FROM diagnosa_pasien " +
                           "WHERE no_rawat=? AND LOWER(status)=LOWER(?) " +
                           "ORDER BY prioritas ASC";

    String[] kd = new String[5];  // prioritas 1..5
    String[] nm = new String[5];

    try (PreparedStatement ps = koneksi.prepareStatement(sqlDiag)) {
        ps.setString(1, noRawat);
        ps.setString(2, statusFilter);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int pr = rs.getInt("prioritas");
                if (pr >= 1 && pr <= 5) {
                    String k = rs.getString("kd_penyakit");
                    kd[pr - 1] = k;
                    nm[pr - 1] = getNamaPenyakit(k); // penyakit.nm_penyakit
                }
            }
        }
    }

    String sqlUpdate = "UPDATE " + resumeTable + " SET " +
            kolomPairs[0] + "=?, "  + kolomPairs[1] + "=?, " +
            kolomPairs[2] + "=?, "  + kolomPairs[3] + "=?, " +
            kolomPairs[4] + "=?, "  + kolomPairs[5] + "=?, " +
            kolomPairs[6] + "=?, "  + kolomPairs[7] + "=?, " +
            kolomPairs[8] + "=?, "  + kolomPairs[9] + "=? "  +
            "WHERE no_rawat=?";

    int updated;
    try (PreparedStatement psU = koneksi.prepareStatement(sqlUpdate)) {
        psU.setString(1,  safeStr(nm[0])); psU.setString(2,  safeCode(kd[0]));
        psU.setString(3,  safeStr(nm[1])); psU.setString(4,  safeCode(kd[1]));
        psU.setString(5,  safeStr(nm[2])); psU.setString(6,  safeCode(kd[2]));
        psU.setString(7,  safeStr(nm[3])); psU.setString(8,  safeCode(kd[3]));  // <- tadinya bisa null
        psU.setString(9,  safeStr(nm[4])); psU.setString(10, safeCode(kd[4]));  // <- tadinya bisa null
        psU.setString(11, noRawat);
        updated = psU.executeUpdate();
    }

    if (updated == 0) {
        String sqlInsert = "INSERT INTO " + resumeTable + " (" +
                "no_rawat," +
                kolomPairs[0] + "," + kolomPairs[1] + "," +
                kolomPairs[2] + "," + kolomPairs[3] + "," +
                kolomPairs[4] + "," + kolomPairs[5] + "," +
                kolomPairs[6] + "," + kolomPairs[7] + "," +
                kolomPairs[8] + "," + kolomPairs[9] +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement psI = koneksi.prepareStatement(sqlInsert)) {
            psI.setString(1,  noRawat);
            psI.setString(2,  safeStr(nm[0])); psI.setString(3,  safeCode(kd[0]));
            psI.setString(4,  safeStr(nm[1])); psI.setString(5,  safeCode(kd[1]));
            psI.setString(6,  safeStr(nm[2])); psI.setString(7,  safeCode(kd[2]));
            psI.setString(8,  safeStr(nm[3])); psI.setString(9,  safeCode(kd[3]));
            psI.setString(10, safeStr(nm[4])); psI.setString(11, safeCode(kd[4]));
        }
    }
}

private boolean existsPenyakit(String kd) throws Exception {
    final String sql = "SELECT 1 FROM penyakit WHERE kd_penyakit=? LIMIT 1";
    try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
        ps.setString(1, kd);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}

private String getNamaPenyakit(String kd) throws Exception {
    if (kd == null || kd.isEmpty()) return "";
    String cached = cacheNmPenyakit.get(kd);
    if (cached != null) return cached;

    final String sql = "SELECT nm_penyakit FROM penyakit WHERE kd_penyakit=? LIMIT 1";
    try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
        ps.setString(1, kd);
        try (ResultSet rs = ps.executeQuery()) {
            String nm = rs.next() ? rs.getString(1) : "";
            cacheNmPenyakit.put(kd, nm);
            return nm;
        }
    }
}

private void hardResyncResumeRalan(String noRawat) throws Exception {
    final String sql =
        "UPDATE resume_pasien rp " +
        "LEFT JOIN penyakit p0 ON p0.kd_penyakit = rp.kd_diagnosa_utama " +
        "LEFT JOIN penyakit p1 ON p1.kd_penyakit = rp.kd_diagnosa_sekunder " +
        "LEFT JOIN penyakit p2 ON p2.kd_penyakit = rp.kd_diagnosa_sekunder2 " +
        "LEFT JOIN penyakit p3 ON p3.kd_penyakit = rp.kd_diagnosa_sekunder3 " +
        "LEFT JOIN penyakit p4 ON p4.kd_penyakit = rp.kd_diagnosa_sekunder4 " +
        "SET rp.diagnosa_utama     = IFNULL(p0.nm_penyakit,''), " +
        "    rp.diagnosa_sekunder  = IFNULL(p1.nm_penyakit,''), " +
        "    rp.diagnosa_sekunder2 = IFNULL(p2.nm_penyakit,''), " +
        "    rp.diagnosa_sekunder3 = IFNULL(p3.nm_penyakit,''), " +
        "    rp.diagnosa_sekunder4 = IFNULL(p4.nm_penyakit,'') " +
        "WHERE rp.no_rawat = ?";
    try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
        ps.setString(1, noRawat);
        ps.executeUpdate();
    }
}

private void hardResyncResumeRanap(String noRawat) throws Exception {
    final String sql =
        "UPDATE resume_pasien_ranap rp " +
        "LEFT JOIN penyakit p0 ON p0.kd_penyakit = rp.kd_diagnosa_utama " +
        "LEFT JOIN penyakit p1 ON p1.kd_penyakit = rp.kd_diagnosa_sekunder " +
        "LEFT JOIN penyakit p2 ON p2.kd_penyakit = rp.kd_diagnosa_sekunder2 " +
        "LEFT JOIN penyakit p3 ON p3.kd_penyakit = rp.kd_diagnosa_sekunder3 " +
        "LEFT JOIN penyakit p4 ON p4.kd_penyakit = rp.kd_diagnosa_sekunder4 " +
        "SET rp.diagnosa_utama     = IFNULL(p0.nm_penyakit,''), " +
        "    rp.diagnosa_sekunder  = IFNULL(p1.nm_penyakit,''), " +
        "    rp.diagnosa_sekunder2 = IFNULL(p2.nm_penyakit,''), " +
        "    rp.diagnosa_sekunder3 = IFNULL(p3.nm_penyakit,''), " +
        "    rp.diagnosa_sekunder4 = IFNULL(p4.nm_penyakit,'') " +
        "WHERE rp.no_rawat = ?";
    try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
        ps.setString(1, noRawat);
        ps.executeUpdate();
    }
}

    
    private void BtnUpdateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgDiagnosaPenyakit dialog = new DlgDiagnosaPenyakit(new javax.swing.JFrame(), true);
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
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnRiwayatPerawatan;
    private widget.Button BtnSimpan;
    private widget.Button BtnUpdate;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.TextBox KodeP;
    private widget.Label LCount;
    private widget.TextBox NamaP;
    private widget.ComboBox Status;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TextBox TPrioritas;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel14;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel3;
    private widget.Label jLabel6;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator5;
    public laporan.PanelDiagnosa panelDiagnosa1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    // End of variables declaration//GEN-END:variables

    

    private void isRawat() {
         Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=? ",TNoRM,TNoRw.getText());
    }

    private void isPsien() {
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=? ",TPasien,TNoRM.getText());
    }


    
    public void setNoRm(String norwt, Date tgl1, Date tgl2,String status) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        Status.setSelectedItem(status);
        isRawat();
        isPsien();   
        DTPCari1.setDate(tgl1);
        DTPCari2.setDate(tgl2);
        panelDiagnosa1.setRM(TNoRw.getText(),TNoRM.getText(),Valid.SetTgl(DTPCari1.getSelectedItem()+""),Valid.SetTgl(DTPCari2.getSelectedItem()+""),Status.getSelectedItem().toString(),TCari.getText().trim());
    }
    
    
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getdiagnosa_pasien());
        BtnHapus.setEnabled(akses.getdiagnosa_pasien());        
        BtnPrint.setEnabled(akses.getdiagnosa_pasien());
        panelDiagnosa1.btnTambahPenyakit.setEnabled(akses.getpenyakit());
        panelDiagnosa1.btnTambahProsedur.setEnabled(akses.geticd9());
        
        if(Sequel.cariInteger("SELECT COUNT(*) FROM dokter_mpp WHERE kd_dokter = ? AND status = '2'", akses.getkode()) > 0){
            BtnUpdate.setVisible(true);
        } else {
            BtnUpdate.setVisible(false);
        }

    }

    


}
