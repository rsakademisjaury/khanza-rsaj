/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgPenyakit.java
 *
 * Created on May 23, 2010, 12:57:16 AM
 */

package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.validasi;
import fungsi.akses;
import fungsi.sekuel;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import keuangan.Jurnal;

/**
 *
 * @author dosen
 */
public final class CopyDiagnosaProsedur extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModeDiagnosa,tabModeProsedur;
    private validasi Valid=new validasi();
    private sekuel Sequel=new sekuel();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private int i=0;
    private String la="",ld="",pa="",pd="",kodedokter="",tanggaldilakukan="",jamdilakukan="",noperawatan="",norm="",nomor="";
    private boolean sukses=true;
    
    /** Creates new form DlgPenyakit
     * @param parent
     * @param modal */
    public CopyDiagnosaProsedur(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(10,2);
        setSize(656,250);

        Object[] row={"No Rawat","No RM","Nama Pasien","Tgl Registrasi","Jam Registrasi","Poliklinik","Kode Dokter","Nama Dokter","Status Rawat"};
        tabMode=new DefaultTableModel(null,row){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokter.setModel(tabMode);

        tbDokter.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 9; i++) {
            TableColumn column = tbDokter.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(110);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(200);
            }else if(i==3){
                column.setPreferredWidth(90);
            }else if(i==4){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==5){
                column.setPreferredWidth(200);                    
            }else if(i==6){
                column.setMinWidth(0);
                column.setMaxWidth(0);            
            }else if(i==7){
                column.setPreferredWidth(200);
            }else if(i==8){
                column.setPreferredWidth(85);                
            }
        }
        tbDokter.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDiagnosa=new DefaultTableModel(null,new Object[]{"Kode","Nama Penyakit","Ciri-ciri Penyakit","Keterangan","Ktg.Penyakit","Ciri-ciri Umum"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
             Class[] types = new Class[] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, 
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbDiagnosa.setModel(tabModeDiagnosa);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbDiagnosa.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDiagnosa.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i= 0; i < 6; i++) {
            TableColumn column = tbDiagnosa.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(60);
            }else if(i==1){
                column.setPreferredWidth(280);
            }else if(i==2){
                column.setPreferredWidth(285);
            }else if(i==3){
                column.setPreferredWidth(168);
            }else if(i==4){
                column.setPreferredWidth(168);
            }else if(i==5){
                column.setPreferredWidth(168);
            }
        }
        tbDiagnosa.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeProsedur=new DefaultTableModel(null,new Object[]{"Kode","Deskripsi Panjang","Deskripsi Pendek"}){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
             Class[] types = new Class[] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbProsedur.setModel(tabModeProsedur);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbProsedur.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbProsedur.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbProsedur.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(60);
            }else if(i==1){
                column.setPreferredWidth(350);
            }else if(i==2){
                column.setPreferredWidth(350);
            }
        }
        tbProsedur.setDefaultRenderer(Object.class, new WarnaTable());

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
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbDokter = new widget.Table();
        panelisi3 = new widget.panelisi();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        BtnSimpan = new widget.Button();
        label10 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        scrollPane2 = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        Scroll1 = new widget.ScrollPane();
        tbDiagnosa = new widget.Table();
        jLabel13 = new widget.Label();
        jLabel14 = new widget.Label();
        Scroll2 = new widget.ScrollPane();
        tbProsedur = new widget.Table();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Riwayat Diagnosa Pasien ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(300, 200));

        tbDokter.setAutoCreateRowSorter(true);
        tbDokter.setName("tbDokter"); // NOI18N
        tbDokter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDokterMouseClicked(evt);
            }
        });
        Scroll.setViewportView(tbDokter);

        internalFrame1.add(Scroll, java.awt.BorderLayout.PAGE_START);

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label9.setText("Key Word :");
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(68, 23));
        panelisi3.add(label9);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(312, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi3.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('1');
        BtnCari.setToolTipText("Alt+1");
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
        panelisi3.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('2');
        BtnAll.setToolTipText("2Alt+2");
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
        panelisi3.add(BtnAll);

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16i.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(28, 23));
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
        panelisi3.add(BtnSimpan);

        label10.setText("Record :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label10);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('4');
        BtnKeluar.setToolTipText("Alt+4");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        panelisi3.add(BtnKeluar);

        internalFrame1.add(panelisi3, java.awt.BorderLayout.PAGE_END);

        scrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)), "Detail Diagnosa dan Prosedur :", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane2.setName("scrollPane2"); // NOI18N
        scrollPane2.setPreferredSize(new java.awt.Dimension(740, 333));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(730, 333));
        FormInput.setLayout(null);

        Scroll1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)));
        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);

        tbDiagnosa.setName("tbDiagnosa"); // NOI18N
        Scroll1.setViewportView(tbDiagnosa);

        FormInput.add(Scroll1);
        Scroll1.setBounds(10, 30, 1160, 123);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("Diagnosa :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(10, 10, 68, 23);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Prosedur :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(10, 160, 68, 23);

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)));
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbProsedur.setName("tbProsedur"); // NOI18N
        Scroll2.setViewportView(tbProsedur);

        FormInput.add(Scroll2);
        Scroll2.setBounds(10, 180, 1160, 123);

        scrollPane2.setViewportView(FormInput);

        internalFrame1.add(scrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbDokter.requestFocus();
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
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCari, TCari);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        emptTeks();
    }//GEN-LAST:event_formWindowActivated

    private void tbDokterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDokterMouseClicked
        if(tabMode.getRowCount()!=0){
            if(tbDokter.getSelectedRow()>-1){
                try {
                    Valid.tabelKosong(tabModeDiagnosa);
                    ps=koneksi.prepareStatement(
                            "SELECT reg_periksa.no_rawat,diagnosa_pasien.no_rawat,diagnosa_pasien.kd_penyakit,diagnosa_pasien.prioritas,diagnosa_pasien.status_penyakit,penyakit.nm_penyakit,penyakit.ciri_ciri,penyakit.keterangan, "+
                            "penyakit.kd_ktg,penyakit.status,diagnosa_pasien.status FROM diagnosa_pasien "+
                            "INNER JOIN reg_periksa ON diagnosa_pasien.no_rawat=reg_periksa.no_rawat "+
                            "INNER JOIN penyakit ON diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit WHERE "+
                            "diagnosa_pasien.no_rawat=? ORDER BY diagnosa_pasien.prioritas ASC");
                    try {
                        ps.setString(1,tabMode.getValueAt(tbDokter.getSelectedRow(),0).toString());
                        rs=ps.executeQuery();
                        while(rs.next()){
                            tabModeDiagnosa.addRow(new Object[]{
                                rs.getString("kd_penyakit"),rs.getString("nm_penyakit"),rs.getString("ciri_ciri"),rs.getString("keterangan"),rs.getString("kd_ktg"),rs.getString("status")
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
                    
                    Valid.tabelKosong(tabModeProsedur);
                    ps=koneksi.prepareStatement(
                            "SELECT reg_periksa.no_rawat,prosedur_pasien.no_rawat,prosedur_pasien.kode,prosedur_pasien.status,prosedur_pasien.prioritas,icd9.kode,icd9.deskripsi_panjang,icd9.deskripsi_pendek, "+
                            "icd9.deskripsi_panjang,icd9.deskripsi_pendek FROM prosedur_pasien "+
                            "INNER JOIN reg_periksa ON prosedur_pasien.no_rawat=reg_periksa.no_rawat "+
                            "INNER JOIN icd9 ON prosedur_pasien.kode=icd9.kode WHERE "+
                            "prosedur_pasien.no_rawat=? ORDER BY prosedur_pasien.prioritas ASC");
                    try {
                        ps.setString(1,tabMode.getValueAt(tbDokter.getSelectedRow(),0).toString());
                        rs=ps.executeQuery();
                        while(rs.next()){
                            tabModeProsedur.addRow(new Object[]{rs.getString("kode"),rs.getString("deskripsi_panjang"),rs.getString("deskripsi_pendek")});
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
    }//GEN-LAST:event_tbDokterMouseClicked

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
if (tbDokter.getSelectedRow() > -1) {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//    System.out.println("DEBUG: tbDokter row selected: " + tbDokter.getSelectedRow());
    
    String kodedokter = akses.getkode(); // Assume akses.getkode() returns a String.
    if (kodedokter.equals(tbDokter.getValueAt(tbDokter.getSelectedRow(), 6).toString())) {
        boolean dataCopied = false;
        boolean sukses = true;

//        System.out.println("DEBUG: Matching kode dokter: " + kodedokter);
        
        for (int i = 0; i < tbDiagnosa.getRowCount(); i++) {
//            System.out.println("DEBUG: Checking diagnosa: " + tbDiagnosa.getValueAt(i, 0).toString());
            
            if (Sequel.cariInteger(
                "select count(diagnosa_pasien.kd_penyakit) from diagnosa_pasien inner join reg_periksa on diagnosa_pasien.no_rawat=reg_periksa.no_rawat inner join pasien " +
                "on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where pasien.no_rkm_medis='" + norm + "' and diagnosa_pasien.kd_penyakit='" + tbDiagnosa.getValueAt(i, 0).toString() + "'") > 0) {
                
//                System.out.println("DEBUG: Found existing diagnosa");
                
                if (!Sequel.menyimpantf2("diagnosa_pasien", "?,?,?,?,?", "Penyakit " + tbDiagnosa.getValueAt(i, 1).toString(), 5, new String[]{
                    noperawatan, tbDiagnosa.getValueAt(i, 0).toString(), "Ralan", Sequel.cariIsi("select ifnull(MAX(diagnosa_pasien.prioritas)+1,1) from diagnosa_pasien where diagnosa_pasien.no_rawat=? and diagnosa_pasien.status='Ralan'", noperawatan), "Lama"
                })) {
                    sukses = false;
//                    System.out.println("DEBUG: Failed to copy diagnosa (Lama)");
                } else {
                    dataCopied = true;
//                    System.out.println("DEBUG: Successfully copied diagnosa (Lama)");
                }
            } else {
                if (!Sequel.menyimpantf2("diagnosa_pasien", "?,?,?,?,?", "Penyakit " + tbDiagnosa.getValueAt(i, 1).toString(), 5, new String[]{
                    noperawatan, tbDiagnosa.getValueAt(i, 0).toString(), "Ralan", Sequel.cariIsi("select ifnull(MAX(diagnosa_pasien.prioritas)+1,1) from diagnosa_pasien where diagnosa_pasien.no_rawat=? and diagnosa_pasien.status='Ralan'", noperawatan), "Baru"
                })) {
                    sukses = false;
//                    System.out.println("DEBUG: Failed to copy diagnosa (Baru)");
                } else {
                    dataCopied = true;
//                    System.out.println("DEBUG: Successfully copied diagnosa (Baru)");
                }
            }
        }

        for (int i = 0; i < tbProsedur.getRowCount(); i++) {
//            System.out.println("DEBUG: Checking prosedur: " + tbProsedur.getValueAt(i, 0).toString());

            if (!Sequel.menyimpantf2("prosedur_pasien", "?,?,?,?", "Prosedur " + tbProsedur.getValueAt(i, 1).toString(), 4, new String[]{
                noperawatan, tbProsedur.getValueAt(i, 0).toString(), "Ralan", Sequel.cariIsi("select ifnull(MAX(prosedur_pasien.prioritas)+1,1) from prosedur_pasien where prosedur_pasien.no_rawat=? and prosedur_pasien.status='Ralan'", noperawatan)
            })) {
                sukses = false;
//                System.out.println("DEBUG: Failed to copy prosedur");
            } else {
                dataCopied = true;
//                System.out.println("DEBUG: Successfully copied prosedur");
            }
        }

        if (sukses) {
            Sequel.Commit();
//            System.out.println("DEBUG: Commit successful");
        } else {
            JOptionPane.showMessageDialog(null, "Eits, periksa kembali data diagnosanya juragan.....,\napakah sudah ada diagnosa sebelum melanjutkan menyimpan...!!");
//            System.out.println("DEBUG: Commit failed, transaction rolled back");
        }

        Sequel.AutoComitTrue();
        if (sukses) {
            if (dataCopied) {
                String NamaPasien = Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=?", norm);
                JOptionPane.showMessageDialog(rootPane, String.format("%s %s %s", NamaPasien, norm, noperawatan));
//                System.out.println("DEBUG: Data copied, displaying message: " + NamaPasien + " " + norm + " " + noperawatan);
            } else {
                JOptionPane.showMessageDialog(rootPane, "Diagnosa tidak ada juaragan....., tidak ada data yang disalin");
//                System.out.println("DEBUG: No data copied");
            }
            dispose();
        }
    } else {
        JOptionPane.showMessageDialog(rootPane, "Eiiits.... data tidak sesuai juragan...!!");
//        System.out.println("DEBUG: No matching kode dokter found");
    }

    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
} else {
    JOptionPane.showMessageDialog(rootPane, "Eiiits.... data tidak sesuai juragan...!!");
//    System.out.println("DEBUG: No row selected in tbDokter");
}

    }//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
        BtnSimpanActionPerformed(null);
    }
    }//GEN-LAST:event_BtnSimpanKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            CopyDiagnosaProsedur dialog = new CopyDiagnosaProsedur(new javax.swing.JFrame(), true);
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
    private widget.Button BtnSimpan;
    private widget.PanelBiasa FormInput;
    private widget.Label LCount;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll2;
    private widget.TextBox TCari;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label label10;
    private widget.Label label9;
    private widget.panelisi panelisi3;
    private widget.ScrollPane scrollPane2;
    public widget.Table tbDiagnosa;
    private widget.Table tbDokter;
    public widget.Table tbProsedur;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
"SELECT reg_periksa.no_rawat, reg_periksa.tgl_registrasi, reg_periksa.jam_reg, reg_periksa.kd_dokter, " +
               "pasien.no_rkm_medis, reg_periksa.kd_poli, reg_periksa.status_lanjut, pasien.nm_pasien, dokter.nm_dokter, poliklinik.nm_poli " +
               "FROM reg_periksa " +
               "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
               "INNER JOIN dokter ON reg_periksa.kd_dokter = dokter.kd_dokter " +
               "INNER JOIN poliklinik ON reg_periksa.kd_poli = poliklinik.kd_poli " +        
                    "WHERE pasien.no_rkm_medis=? " +
                    "ORDER BY reg_periksa.tgl_registrasi DESC");
                //"No Rawat","No RM","Nama Pasien","Tgl Registrasi","Jam Registrasi","Kode Dokter","Nama Dokter"
            try {
                ps.setString(1,TCari.getText());
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_registrasi"),rs.getString("jam_reg"),rs.getString("nm_poli"),rs.getString("kd_dokter"),rs.getString("nm_dokter"),rs.getString("status_lanjut")
                    });
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }
    
    public void emptTeks() {
        TCari.requestFocus();
    }

    public JTable getTable(){
        return tbDokter;
    }
    
    public void setDokter(String kode,String tanggal, String jam,String norawat,String nomorrm){
        this.kodedokter=kode;
        this.tanggaldilakukan=tanggal;
        this.jamdilakukan=jam;
        this.noperawatan=norawat;
        this.norm=nomorrm;
        TCari.setText(nomorrm);
    }
    
    public void isCek(){        
//        BtnTambah.setEnabled(akses.gettemplate_pemeriksaan());
    }
}
