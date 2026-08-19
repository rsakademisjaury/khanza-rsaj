/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * kontribusi dari dokter Salim Mulyana
 */

package surat;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import fungsi.validasi2;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import kepegawaian.DlgCariDokter;


/**
 * 
 * @author salimmulyana
 */
public final class SuratVisum extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private validasi2 Valid1=new validasi2();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private String tgl,finger,bln_angka = "", bln_romawi = "",kodedokter="",namadokter="";
    private DlgCariDokter dokter = new DlgCariDokter(null, false);
    private String nomorSuratGenerated = "";
    private String idDipilih = "", tglLahirPasien = "", umurPasien = "", alamatPasien = "", noBuktiIdentitasPasien = "";
    private String tempatPemeriksaan = "", tglPemeriksaan = "", tempatKeluarVer = "", tglKeluarVer = "";
    private String nipDokter = "", jabatanKompetensi = "";
    private static final String TABEL_VISUM = "surat_visum_et_repertum";
    private static final String[] KOLOM_VISUM = new String[]{
            "id",
            "no_rawat",
            "no_rkm_medis",
            "no_surat_ver",
            "no_surat_permintaan",
            "tgl_terima_spv",
            "pihak_pembuat_spv",
            "jenis_pemeriksaan_diminta",
            "tempat_pemeriksaan",
            "tgl_pemeriksaan",
            "nama_pasien",
            "tgl_lahir",
            "umur",
            "alamat",
            "no_bukti_identitas",
            "anamnesis",
            "kesadaran",
            "denyut_nadi",
            "pernapasan",
            "tekanan_darah",
            "suhu_tubuh",
            "pakaian",
            "tinggi_badan",
            "berat_badan",
            "ciri_khusus",
            "kepala",
            "leher",
            "bahu",
            "dada",
            "punggung",
            "perut",
            "pinggang",
            "bokong",
            "dubur",
            "alat_kelamin",
            "anggota_gerak_atas",
            "anggota_gerak_bawah",
            "pemeriksaan_laboratorium",
            "pemeriksaan_radiologi",
            "pemeriksaan_odontogram",
            "pemeriksaan_lain_lain",
            "ringkasan_pemeriksaan",
            "kesimpulan",
            "lampiran_hasil_pemeriksaan_klinis",
            "lampiran_pemeriksaan_toksikologi",
            "lampiran_pemeriksaan_histopatologi",
            "lampiran_foto",
            "lampiran_video",
            "lampiran_lain_lain",
            "tempat_keluar_ver",
            "tgl_keluar_ver",
            "kd_dokter",
            "nama_dokter",
            "nip_dokter",
            "jabatan_kompetensi",
            "created_at",
            "updated_at",
            "created_by",
            "updated_by",
            "status_data"
    };
    private static final String[] KOLOM_CARI_VISUM = new String[]{
            "no_surat_ver",
            "no_surat_permintaan",
            "no_rawat",
            "no_rkm_medis",
            "nama_pasien",
            "kd_dokter",
            "nama_dokter",
            "status_data",
            "lampiran_hasil_pemeriksaan_klinis",
            "lampiran_pemeriksaan_toksikologi",
            "lampiran_pemeriksaan_histopatologi",
            "lampiran_foto",
            "lampiran_video",
            "lampiran_lain_lain"
    };
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public SuratVisum(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(628,674);
        pastikanKolomLampiranVisum();
        
        tabMode=new DefaultTableModel(null,KOLOM_VISUM){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < KOLOM_VISUM.length; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            String namaKolom = KOLOM_VISUM[i];
            if(namaKolom.equals("id")){
                column.setPreferredWidth(60);
            }else if(namaKolom.equals("no_rawat")){
                column.setPreferredWidth(130);
            }else if(namaKolom.equals("no_rkm_medis")){
                column.setPreferredWidth(95);
            }else if(namaKolom.equals("no_surat_ver") || namaKolom.equals("no_surat_permintaan")){
                column.setPreferredWidth(180);
            }else if(namaKolom.startsWith("tgl_") || namaKolom.equals("created_at") || namaKolom.equals("updated_at")){
                column.setPreferredWidth(145);
            }else if(namaKolom.contains("lampiran") || namaKolom.equals("pihak_pembuat_spv") || namaKolom.equals("alamat") || namaKolom.equals("anamnesis") || namaKolom.equals("jabatan_kompetensi") || namaKolom.startsWith("pemeriksaan_") || namaKolom.equals("ringkasan_pemeriksaan") || namaKolom.equals("kesimpulan") || namaKolom.equals("pakaian") || namaKolom.equals("ciri_khusus") || namaKolom.equals("kepala") || namaKolom.equals("leher") || namaKolom.equals("bahu") || namaKolom.equals("dada") || namaKolom.equals("punggung") || namaKolom.equals("perut") || namaKolom.equals("pinggang") || namaKolom.equals("bokong") || namaKolom.equals("dubur") || namaKolom.equals("alat_kelamin") || namaKolom.equals("anggota_gerak_atas") || namaKolom.equals("anggota_gerak_bawah")){
                column.setPreferredWidth(220);
            }else{
                column.setPreferredWidth(130);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        NoSurat.setDocument(new batasInput((byte)50).getKata(NoSurat));
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));  
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
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                    KdDokter.requestFocus();
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        
        TNoRw.setVisible(false);
        TNoRM.setVisible(false);
        TPasien.setVisible(false);
        KdDokter.setVisible(false);
        NmDokter.setVisible(false);
        
        BtnDokter.setVisible(false);
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
        MnCetakSuratRawat = new javax.swing.JMenuItem();
        MnCetakSuratRawat1 = new javax.swing.JMenuItem();
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
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        PanelInput = new javax.swing.JPanel();
        ScrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new widget.Label();
        jLabel9 = new widget.Label();
        labelNoRawat = new widget.Label();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel11 = new widget.Label();
        labelNoRM = new widget.Label();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel14 = new widget.Label();
        labelNamaPasien = new widget.Label();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        labelDokter = new widget.Label();
        jLabel22 = new widget.Label();
        scrollAnamnesis = new widget.ScrollPane();
        txtAnamnesis = new widget.TextArea();
        lblAnamnesis = new widget.Label();
        jLabel23 = new widget.Label();
        lblKesadaran = new widget.Label();
        txtKesadaran = new widget.TextBox();
        lblDenyutNadi = new widget.Label();
        txtDenyutNadi = new widget.TextBox();
        lblPernapasan = new widget.Label();
        txtPernapasan = new widget.TextBox();
        lblTekananDarah = new widget.Label();
        txtTekananDarah = new widget.TextBox();
        lblSuhuTubuh = new widget.Label();
        txtSuhuTubuh = new widget.TextBox();
        lblPakaian = new widget.Label();
        scrollPakaian = new widget.ScrollPane();
        txtPakaian = new widget.TextArea();
        lblTinggiBadan = new widget.Label();
        txtTinggiBadan = new widget.TextBox();
        lblBeratBadan = new widget.Label();
        txtBeratBadan = new widget.TextBox();
        lblCiriKhusus = new widget.Label();
        scrollCiriKhusus = new widget.ScrollPane();
        txtCiriKhusus = new widget.TextArea();
        lblKepala = new widget.Label();
        scrollKepala = new widget.ScrollPane();
        txtKepala = new widget.TextArea();
        lblLeher = new widget.Label();
        scrollLeher = new widget.ScrollPane();
        txtLeher = new widget.TextArea();
        lblBahu = new widget.Label();
        scrollBahu = new widget.ScrollPane();
        txtBahu = new widget.TextArea();
        lblDada = new widget.Label();
        scrollDada = new widget.ScrollPane();
        txtDada = new widget.TextArea();
        lblPunggung = new widget.Label();
        scrollPunggung = new widget.ScrollPane();
        txtPunggung = new widget.TextArea();
        lblPerut = new widget.Label();
        scrollPerut = new widget.ScrollPane();
        txtPerut = new widget.TextArea();
        lblPinggang = new widget.Label();
        scrollPinggang = new widget.ScrollPane();
        txtPinggang = new widget.TextArea();
        lblBokong = new widget.Label();
        scrollBokong = new widget.ScrollPane();
        txtBokong = new widget.TextArea();
        lblDubur = new widget.Label();
        scrollDubur = new widget.ScrollPane();
        txtDubur = new widget.TextArea();
        lblAlatKelamin = new widget.Label();
        scrollAlatKelamin = new widget.ScrollPane();
        txtAlatKelamin = new widget.TextArea();
        lblAnggotaGerakAtas = new widget.Label();
        scrollAnggotaGerakAtas = new widget.ScrollPane();
        txtAnggotaGerakAtas = new widget.TextArea();
        lblAnggotaGerakBawah = new widget.Label();
        scrollAnggotaGerakBawah = new widget.ScrollPane();
        txtAnggotaGerakBawah = new widget.TextArea();
        jLabel24 = new widget.Label();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel25 = new widget.Label();
        lblLaboratorium = new widget.Label();
        scrollLaboratorium = new widget.ScrollPane();
        txtLaboratorium = new widget.TextArea();
        lblRadiologi = new widget.Label();
        lblOdontogram = new widget.Label();
        lblLainLain = new widget.Label();
        lblRingkasan = new widget.Label();
        lblKesimpulan = new widget.Label();
        scrollRadiologi = new widget.ScrollPane();
        txtRadiologi = new widget.TextArea();
        scrollOdontogram = new widget.ScrollPane();
        txtOdontogram = new widget.TextArea();
        scrollLainLain = new widget.ScrollPane();
        txtLainLain = new widget.TextArea();
        scrollRingkasan = new widget.ScrollPane();
        txtRingkasan = new widget.TextArea();
        scrollKesimpulan = new widget.ScrollPane();
        txtKesimpulan = new widget.TextArea();
        jLabel26 = new widget.Label();
        jSeparator8 = new javax.swing.JSeparator();
        lblLampiranHasilPemeriksaanKlinis = new widget.Label();
        scrollLampiranHasilPemeriksaanKlinis = new widget.ScrollPane();
        txtLaboratorium1 = new widget.TextArea();
        lblLampiranPemeriksaanToksikologi = new widget.Label();
        scrollLampiranPemeriksaanToksikologi = new widget.ScrollPane();
        txtRadiologi1 = new widget.TextArea();
        lblLampiranPemeriksaanHistopatologi = new widget.Label();
        scrollLampiranPemeriksaanHistopatologi = new widget.ScrollPane();
        txtOdontogram1 = new widget.TextArea();
        lblLampiranFoto = new widget.Label();
        scrollLampiranFoto = new widget.ScrollPane();
        txtLainLain1 = new widget.TextArea();
        lblLampiranVideo = new widget.Label();
        scrollLampiranVideo = new widget.ScrollPane();
        txtRingkasan1 = new widget.TextArea();
        lblLampiran_lain = new widget.Label();
        scrollLampiranLain = new widget.ScrollPane();
        txtKesimpulan1 = new widget.TextArea();
        PanelKananInput = new widget.PanelBiasa();
        jLabel17 = new widget.Label();
        lblNoSuratVer = new widget.Label();
        NoSurat = new widget.TextBox();
        lblNoSuratPermintaan = new widget.Label();
        txtNoSuratPermintaan = new widget.TextBox();
        lblJenisPemeriksaan = new widget.Label();
        txtJenisPemeriksaan = new widget.TextBox();
        lblTglTerimaSPV = new widget.Label();
        TanggalAkhir = new widget.Tanggal();
        lblStatusData = new widget.Label();
        cmbStatusData = new widget.ComboBox();
        lblPihakPembuatSPV = new widget.Label();
        scrollPihakPembuatSPV = new widget.ScrollPane();
        txtPihakPembuatSPV1 = new widget.TextArea();
        ChkInput = new widget.CekBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCetakSuratRawat.setBackground(new java.awt.Color(250, 250, 250));
        MnCetakSuratRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSuratRawat.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakSuratRawat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSuratRawat.setText("Cetak Surat Sudah Pulang");
        MnCetakSuratRawat.setName("MnCetakSuratRawat"); // NOI18N
        MnCetakSuratRawat.setPreferredSize(new java.awt.Dimension(250, 26));
        MnCetakSuratRawat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSuratRawatActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSuratRawat);

        MnCetakSuratRawat1.setBackground(new java.awt.Color(250, 250, 250));
        MnCetakSuratRawat1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCetakSuratRawat1.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakSuratRawat1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakSuratRawat1.setText("Cetak Surat Masih Dirawat");
        MnCetakSuratRawat1.setName("MnCetakSuratRawat1"); // NOI18N
        MnCetakSuratRawat1.setPreferredSize(new java.awt.Dimension(250, 26));
        MnCetakSuratRawat1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakSuratRawat1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakSuratRawat1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "   Form Input Surat Visum Et Revertum", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N
        internalFrame1.setAutoscrolls(true);
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
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbObatKeyReleased(evt);
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
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 50));
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10);
        flowLayout1.setAlignOnBaseline(true);
        panelGlass9.setLayout(flowLayout1);

        jLabel19.setText("Tgl. Surat :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(67, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-05-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-05-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(205, 23));
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass9.add(LCount);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setAutoscrolls(true);
        PanelInput.setMinimumSize(new java.awt.Dimension(1200, 470));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(1200, 470));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ScrollInput.setAutoscrolls(true);
        ScrollInput.setName("ScrollInput"); // NOI18N
        ScrollInput.setPreferredSize(new java.awt.Dimension(880, 430));

        FormInput.setAutoscrolls(true);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(1800, 1300));
        FormInput.setLayout(null);

        TNoRw.setEditable(false);
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(960, 10, 23, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(1020, 10, 23, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(990, 10, 23, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokterKeyPressed(evt);
            }
        });
        FormInput.add(KdDokter);
        KdDokter.setBounds(1050, 10, 23, 23);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(1080, 10, 23, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('2');
        BtnDokter.setToolTipText("ALt+2");
        BtnDokter.setName("BtnDokter"); // NOI18N
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
        BtnDokter.setBounds(1110, 10, 28, 23);

        jSeparator1.setForeground(new java.awt.Color(229, 229, 229));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(20, 0, 1020, 10);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/userline.png"))); // NOI18N
        jLabel8.setText("Ringkasan Pasien");
        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel8.setIconTextGap(10);
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(20, 10, 470, 30);

        jLabel9.setForeground(new java.awt.Color(51, 51, 51));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("No. Rawat :");
        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(50, 50, 100, 23);

        labelNoRawat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNoRawat.setText("2026/04/14/000055");
        labelNoRawat.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        labelNoRawat.setName("labelNoRawat"); // NOI18N
        FormInput.add(labelNoRawat);
        labelNoRawat.setBounds(50, 70, 150, 22);

        jSeparator2.setForeground(new java.awt.Color(229, 229, 229));
        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(220, 50, 10, 50);

        jLabel11.setForeground(new java.awt.Color(51, 51, 51));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("No. RM :");
        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(240, 50, 80, 23);

        labelNoRM.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNoRM.setText("123456");
        labelNoRM.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        labelNoRM.setName("labelNoRM"); // NOI18N
        FormInput.add(labelNoRM);
        labelNoRM.setBounds(240, 70, 80, 22);

        jSeparator3.setForeground(new java.awt.Color(229, 229, 229));
        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(320, 50, 10, 50);

        jLabel14.setForeground(new java.awt.Color(51, 51, 51));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Nama Pasien :");
        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(340, 50, 270, 23);

        labelNamaPasien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNamaPasien.setText("123456");
        labelNamaPasien.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        labelNamaPasien.setName("labelNamaPasien"); // NOI18N
        FormInput.add(labelNamaPasien);
        labelNamaPasien.setBounds(340, 70, 380, 22);

        jSeparator4.setForeground(new java.awt.Color(229, 229, 229));
        jSeparator4.setName("jSeparator4"); // NOI18N
        FormInput.add(jSeparator4);
        jSeparator4.setBounds(20, 110, 1120, 10);

        jSeparator5.setForeground(new java.awt.Color(229, 229, 229));
        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setName("jSeparator5"); // NOI18N
        FormInput.add(jSeparator5);
        jSeparator5.setBounds(730, 50, 10, 50);

        labelDokter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDokter.setText("123456");
        labelDokter.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        labelDokter.setName("labelDokter"); // NOI18N
        FormInput.add(labelDokter);
        labelDokter.setBounds(750, 70, 390, 22);

        jLabel22.setForeground(new java.awt.Color(51, 51, 51));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel22.setText("Dokter IGD :");
        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(750, 50, 260, 23);

        scrollAnamnesis.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollAnamnesis.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollAnamnesis.setName("scrollAnamnesis"); // NOI18N

        txtAnamnesis.setColumns(20);
        txtAnamnesis.setForeground(new java.awt.Color(0, 0, 0));
        txtAnamnesis.setRows(5);
        txtAnamnesis.setName("txtAnamnesis"); // NOI18N
        scrollAnamnesis.setViewportView(txtAnamnesis);

        FormInput.add(scrollAnamnesis);
        scrollAnamnesis.setBounds(50, 190, 1090, 40);

        lblAnamnesis.setForeground(new java.awt.Color(51, 51, 51));
        lblAnamnesis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAnamnesis.setText("Anamnesis");
        lblAnamnesis.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAnamnesis.setName("lblAnamnesis"); // NOI18N
        FormInput.add(lblAnamnesis);
        lblAnamnesis.setBounds(50, 160, 190, 30);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/resumeri.png"))); // NOI18N
        jLabel23.setText("Hasil Pemeriksaan");
        jLabel23.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel23.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel23.setIconTextGap(10);
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(20, 120, 380, 30);

        lblKesadaran.setForeground(new java.awt.Color(51, 51, 51));
        lblKesadaran.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKesadaran.setText("Kesadaran");
        lblKesadaran.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblKesadaran.setName("lblKesadaran"); // NOI18N
        FormInput.add(lblKesadaran);
        lblKesadaran.setBounds(50, 300, 190, 30);

        txtKesadaran.setForeground(new java.awt.Color(0, 0, 0));
        txtKesadaran.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtKesadaran.setName("txtKesadaran"); // NOI18N
        FormInput.add(txtKesadaran);
        txtKesadaran.setBounds(50, 330, 350, 26);

        lblDenyutNadi.setForeground(new java.awt.Color(51, 51, 51));
        lblDenyutNadi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDenyutNadi.setText("Denyut nadi");
        lblDenyutNadi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDenyutNadi.setName("lblDenyutNadi"); // NOI18N
        FormInput.add(lblDenyutNadi);
        lblDenyutNadi.setBounds(420, 300, 190, 30);

        txtDenyutNadi.setForeground(new java.awt.Color(0, 0, 0));
        txtDenyutNadi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtDenyutNadi.setName("txtDenyutNadi"); // NOI18N
        FormInput.add(txtDenyutNadi);
        txtDenyutNadi.setBounds(420, 330, 350, 26);

        lblPernapasan.setForeground(new java.awt.Color(51, 51, 51));
        lblPernapasan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPernapasan.setText("Pernapasan");
        lblPernapasan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPernapasan.setName("lblPernapasan"); // NOI18N
        FormInput.add(lblPernapasan);
        lblPernapasan.setBounds(790, 300, 190, 30);

        txtPernapasan.setForeground(new java.awt.Color(0, 0, 0));
        txtPernapasan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtPernapasan.setName("txtPernapasan"); // NOI18N
        FormInput.add(txtPernapasan);
        txtPernapasan.setBounds(790, 330, 350, 26);

        lblTekananDarah.setForeground(new java.awt.Color(51, 51, 51));
        lblTekananDarah.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTekananDarah.setText("Tekanan darah");
        lblTekananDarah.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTekananDarah.setName("lblTekananDarah"); // NOI18N
        FormInput.add(lblTekananDarah);
        lblTekananDarah.setBounds(50, 360, 190, 30);

        txtTekananDarah.setForeground(new java.awt.Color(0, 0, 0));
        txtTekananDarah.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtTekananDarah.setName("txtTekananDarah"); // NOI18N
        FormInput.add(txtTekananDarah);
        txtTekananDarah.setBounds(50, 390, 160, 26);

        lblSuhuTubuh.setForeground(new java.awt.Color(51, 51, 51));
        lblSuhuTubuh.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblSuhuTubuh.setText("Suhu tubuh");
        lblSuhuTubuh.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSuhuTubuh.setName("lblSuhuTubuh"); // NOI18N
        FormInput.add(lblSuhuTubuh);
        lblSuhuTubuh.setBounds(230, 360, 170, 30);

        txtSuhuTubuh.setForeground(new java.awt.Color(0, 0, 0));
        txtSuhuTubuh.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtSuhuTubuh.setName("txtSuhuTubuh"); // NOI18N
        FormInput.add(txtSuhuTubuh);
        txtSuhuTubuh.setBounds(230, 390, 170, 26);

        lblPakaian.setForeground(new java.awt.Color(51, 51, 51));
        lblPakaian.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPakaian.setText("Pakaian");
        lblPakaian.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPakaian.setName("lblPakaian"); // NOI18N
        FormInput.add(lblPakaian);
        lblPakaian.setBounds(50, 420, 190, 30);

        scrollPakaian.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPakaian.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPakaian.setName("scrollPakaian"); // NOI18N

        txtPakaian.setColumns(20);
        txtPakaian.setRows(5);
        txtPakaian.setName("txtPakaian"); // NOI18N
        scrollPakaian.setViewportView(txtPakaian);

        FormInput.add(scrollPakaian);
        scrollPakaian.setBounds(50, 450, 350, 40);

        lblTinggiBadan.setForeground(new java.awt.Color(51, 51, 51));
        lblTinggiBadan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTinggiBadan.setText("Tinggi badan");
        lblTinggiBadan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTinggiBadan.setName("lblTinggiBadan"); // NOI18N
        FormInput.add(lblTinggiBadan);
        lblTinggiBadan.setBounds(420, 360, 150, 30);

        txtTinggiBadan.setForeground(new java.awt.Color(0, 0, 0));
        txtTinggiBadan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtTinggiBadan.setName("txtTinggiBadan"); // NOI18N
        FormInput.add(txtTinggiBadan);
        txtTinggiBadan.setBounds(420, 390, 150, 26);

        lblBeratBadan.setForeground(new java.awt.Color(51, 51, 51));
        lblBeratBadan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblBeratBadan.setText("Berat badan");
        lblBeratBadan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblBeratBadan.setName("lblBeratBadan"); // NOI18N
        FormInput.add(lblBeratBadan);
        lblBeratBadan.setBounds(590, 360, 180, 30);

        txtBeratBadan.setForeground(new java.awt.Color(0, 0, 0));
        txtBeratBadan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBeratBadan.setName("txtBeratBadan"); // NOI18N
        FormInput.add(txtBeratBadan);
        txtBeratBadan.setBounds(590, 390, 180, 26);

        lblCiriKhusus.setForeground(new java.awt.Color(51, 51, 51));
        lblCiriKhusus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCiriKhusus.setText("Ciri khusus");
        lblCiriKhusus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCiriKhusus.setName("lblCiriKhusus"); // NOI18N
        FormInput.add(lblCiriKhusus);
        lblCiriKhusus.setBounds(420, 420, 190, 30);

        scrollCiriKhusus.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollCiriKhusus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollCiriKhusus.setName("scrollCiriKhusus"); // NOI18N

        txtCiriKhusus.setColumns(20);
        txtCiriKhusus.setRows(5);
        txtCiriKhusus.setName("txtCiriKhusus"); // NOI18N
        scrollCiriKhusus.setViewportView(txtCiriKhusus);

        FormInput.add(scrollCiriKhusus);
        scrollCiriKhusus.setBounds(420, 450, 350, 40);

        lblKepala.setForeground(new java.awt.Color(51, 51, 51));
        lblKepala.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKepala.setText("Kepala");
        lblKepala.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblKepala.setName("lblKepala"); // NOI18N
        FormInput.add(lblKepala);
        lblKepala.setBounds(790, 420, 190, 30);

        scrollKepala.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollKepala.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollKepala.setName("scrollKepala"); // NOI18N

        txtKepala.setColumns(20);
        txtKepala.setRows(5);
        txtKepala.setName("txtKepala"); // NOI18N
        scrollKepala.setViewportView(txtKepala);

        FormInput.add(scrollKepala);
        scrollKepala.setBounds(790, 450, 350, 40);

        lblLeher.setForeground(new java.awt.Color(51, 51, 51));
        lblLeher.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLeher.setText("Leher");
        lblLeher.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLeher.setName("lblLeher"); // NOI18N
        FormInput.add(lblLeher);
        lblLeher.setBounds(50, 500, 190, 30);

        scrollLeher.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLeher.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollLeher.setName("scrollLeher"); // NOI18N

        txtLeher.setColumns(20);
        txtLeher.setRows(5);
        txtLeher.setName("txtLeher"); // NOI18N
        scrollLeher.setViewportView(txtLeher);

        FormInput.add(scrollLeher);
        scrollLeher.setBounds(50, 530, 350, 40);

        lblBahu.setForeground(new java.awt.Color(51, 51, 51));
        lblBahu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblBahu.setText("Bahu");
        lblBahu.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblBahu.setName("lblBahu"); // NOI18N
        FormInput.add(lblBahu);
        lblBahu.setBounds(420, 500, 190, 30);

        scrollBahu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollBahu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollBahu.setName("scrollBahu"); // NOI18N

        txtBahu.setColumns(20);
        txtBahu.setRows(5);
        txtBahu.setName("txtBahu"); // NOI18N
        scrollBahu.setViewportView(txtBahu);

        FormInput.add(scrollBahu);
        scrollBahu.setBounds(420, 530, 350, 40);

        lblDada.setForeground(new java.awt.Color(51, 51, 51));
        lblDada.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDada.setText("Dada");
        lblDada.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDada.setName("lblDada"); // NOI18N
        FormInput.add(lblDada);
        lblDada.setBounds(790, 500, 190, 30);

        scrollDada.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollDada.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollDada.setName("scrollDada"); // NOI18N

        txtDada.setColumns(20);
        txtDada.setRows(5);
        txtDada.setName("txtDada"); // NOI18N
        scrollDada.setViewportView(txtDada);

        FormInput.add(scrollDada);
        scrollDada.setBounds(790, 530, 350, 40);

        lblPunggung.setForeground(new java.awt.Color(51, 51, 51));
        lblPunggung.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPunggung.setText("Punggung");
        lblPunggung.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPunggung.setName("lblPunggung"); // NOI18N
        FormInput.add(lblPunggung);
        lblPunggung.setBounds(50, 580, 190, 30);

        scrollPunggung.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPunggung.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPunggung.setName("scrollPunggung"); // NOI18N

        txtPunggung.setColumns(20);
        txtPunggung.setRows(5);
        txtPunggung.setName("txtPunggung"); // NOI18N
        scrollPunggung.setViewportView(txtPunggung);

        FormInput.add(scrollPunggung);
        scrollPunggung.setBounds(50, 610, 350, 40);

        lblPerut.setForeground(new java.awt.Color(51, 51, 51));
        lblPerut.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPerut.setText("Perut");
        lblPerut.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPerut.setName("lblPerut"); // NOI18N
        FormInput.add(lblPerut);
        lblPerut.setBounds(420, 580, 190, 30);

        scrollPerut.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPerut.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPerut.setName("scrollPerut"); // NOI18N

        txtPerut.setColumns(20);
        txtPerut.setRows(5);
        txtPerut.setName("txtPerut"); // NOI18N
        scrollPerut.setViewportView(txtPerut);

        FormInput.add(scrollPerut);
        scrollPerut.setBounds(420, 610, 350, 40);

        lblPinggang.setForeground(new java.awt.Color(51, 51, 51));
        lblPinggang.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPinggang.setText("Pinggang");
        lblPinggang.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPinggang.setName("lblPinggang"); // NOI18N
        FormInput.add(lblPinggang);
        lblPinggang.setBounds(790, 580, 190, 30);

        scrollPinggang.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPinggang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollPinggang.setName("scrollPinggang"); // NOI18N

        txtPinggang.setColumns(20);
        txtPinggang.setRows(5);
        txtPinggang.setName("txtPinggang"); // NOI18N
        scrollPinggang.setViewportView(txtPinggang);

        FormInput.add(scrollPinggang);
        scrollPinggang.setBounds(790, 610, 350, 40);

        lblBokong.setForeground(new java.awt.Color(51, 51, 51));
        lblBokong.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblBokong.setText("Bokong");
        lblBokong.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblBokong.setName("lblBokong"); // NOI18N
        FormInput.add(lblBokong);
        lblBokong.setBounds(50, 660, 190, 30);

        scrollBokong.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollBokong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollBokong.setName("scrollBokong"); // NOI18N

        txtBokong.setColumns(20);
        txtBokong.setRows(5);
        txtBokong.setName("txtBokong"); // NOI18N
        scrollBokong.setViewportView(txtBokong);

        FormInput.add(scrollBokong);
        scrollBokong.setBounds(50, 690, 350, 40);

        lblDubur.setForeground(new java.awt.Color(51, 51, 51));
        lblDubur.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDubur.setText("Dubur");
        lblDubur.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDubur.setName("lblDubur"); // NOI18N
        FormInput.add(lblDubur);
        lblDubur.setBounds(420, 660, 190, 30);

        scrollDubur.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollDubur.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollDubur.setName("scrollDubur"); // NOI18N

        txtDubur.setColumns(20);
        txtDubur.setRows(5);
        txtDubur.setName("txtDubur"); // NOI18N
        scrollDubur.setViewportView(txtDubur);

        FormInput.add(scrollDubur);
        scrollDubur.setBounds(420, 690, 350, 40);

        lblAlatKelamin.setForeground(new java.awt.Color(51, 51, 51));
        lblAlatKelamin.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAlatKelamin.setText("Alat kelamin");
        lblAlatKelamin.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAlatKelamin.setName("lblAlatKelamin"); // NOI18N
        FormInput.add(lblAlatKelamin);
        lblAlatKelamin.setBounds(790, 660, 190, 30);

        scrollAlatKelamin.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollAlatKelamin.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollAlatKelamin.setName("scrollAlatKelamin"); // NOI18N

        txtAlatKelamin.setColumns(20);
        txtAlatKelamin.setRows(5);
        txtAlatKelamin.setName("txtAlatKelamin"); // NOI18N
        scrollAlatKelamin.setViewportView(txtAlatKelamin);

        FormInput.add(scrollAlatKelamin);
        scrollAlatKelamin.setBounds(790, 690, 350, 40);

        lblAnggotaGerakAtas.setForeground(new java.awt.Color(51, 51, 51));
        lblAnggotaGerakAtas.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAnggotaGerakAtas.setText("Anggota gerak atas");
        lblAnggotaGerakAtas.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAnggotaGerakAtas.setName("lblAnggotaGerakAtas"); // NOI18N
        FormInput.add(lblAnggotaGerakAtas);
        lblAnggotaGerakAtas.setBounds(50, 740, 190, 30);

        scrollAnggotaGerakAtas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollAnggotaGerakAtas.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollAnggotaGerakAtas.setName("scrollAnggotaGerakAtas"); // NOI18N

        txtAnggotaGerakAtas.setColumns(20);
        txtAnggotaGerakAtas.setRows(5);
        txtAnggotaGerakAtas.setName("txtAnggotaGerakAtas"); // NOI18N
        scrollAnggotaGerakAtas.setViewportView(txtAnggotaGerakAtas);

        FormInput.add(scrollAnggotaGerakAtas);
        scrollAnggotaGerakAtas.setBounds(50, 770, 350, 40);

        lblAnggotaGerakBawah.setForeground(new java.awt.Color(51, 51, 51));
        lblAnggotaGerakBawah.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAnggotaGerakBawah.setText("Anggota gerak bawah");
        lblAnggotaGerakBawah.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAnggotaGerakBawah.setName("lblAnggotaGerakBawah"); // NOI18N
        FormInput.add(lblAnggotaGerakBawah);
        lblAnggotaGerakBawah.setBounds(420, 740, 190, 30);

        scrollAnggotaGerakBawah.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollAnggotaGerakBawah.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        scrollAnggotaGerakBawah.setName("scrollAnggotaGerakBawah"); // NOI18N

        txtAnggotaGerakBawah.setColumns(20);
        txtAnggotaGerakBawah.setRows(5);
        txtAnggotaGerakBawah.setName("txtAnggotaGerakBawah"); // NOI18N
        scrollAnggotaGerakBawah.setViewportView(txtAnggotaGerakBawah);

        FormInput.add(scrollAnggotaGerakBawah);
        scrollAnggotaGerakBawah.setBounds(420, 770, 350, 40);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/rkontrol.png"))); // NOI18N
        jLabel24.setText("Pemeriksaan Fisik");
        jLabel24.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel24.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel24.setIconTextGap(10);
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(20, 260, 440, 30);

        jSeparator6.setForeground(new java.awt.Color(229, 229, 229));
        jSeparator6.setName("jSeparator6"); // NOI18N
        FormInput.add(jSeparator6);
        jSeparator6.setBounds(20, 250, 1120, 10);

        jSeparator7.setForeground(new java.awt.Color(229, 229, 229));
        jSeparator7.setName("jSeparator7"); // NOI18N
        FormInput.add(jSeparator7);
        jSeparator7.setBounds(20, 830, 1120, 10);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/igd.png"))); // NOI18N
        jLabel25.setText("Pemeriksaan Penunjang");
        jLabel25.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel25.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel25.setIconTextGap(10);
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(20, 840, 520, 30);

        lblLaboratorium.setForeground(new java.awt.Color(51, 51, 51));
        lblLaboratorium.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLaboratorium.setText("Laboratorium");
        lblLaboratorium.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLaboratorium.setName("lblLaboratorium"); // NOI18N
        FormInput.add(lblLaboratorium);
        lblLaboratorium.setBounds(50, 880, 190, 30);

        scrollLaboratorium.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLaboratorium.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollLaboratorium.setName("scrollLaboratorium"); // NOI18N

        txtLaboratorium.setColumns(20);
        txtLaboratorium.setRows(5);
        txtLaboratorium.setName("txtLaboratorium"); // NOI18N
        scrollLaboratorium.setViewportView(txtLaboratorium);

        FormInput.add(scrollLaboratorium);
        scrollLaboratorium.setBounds(50, 910, 350, 40);

        lblRadiologi.setForeground(new java.awt.Color(51, 51, 51));
        lblRadiologi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRadiologi.setText("Radiologi");
        lblRadiologi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblRadiologi.setName("lblRadiologi"); // NOI18N
        FormInput.add(lblRadiologi);
        lblRadiologi.setBounds(420, 880, 190, 30);

        lblOdontogram.setForeground(new java.awt.Color(51, 51, 51));
        lblOdontogram.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblOdontogram.setText("Odontogram");
        lblOdontogram.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblOdontogram.setName("lblOdontogram"); // NOI18N
        FormInput.add(lblOdontogram);
        lblOdontogram.setBounds(790, 880, 190, 30);

        lblLainLain.setForeground(new java.awt.Color(51, 51, 51));
        lblLainLain.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLainLain.setText("Lain-lain");
        lblLainLain.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLainLain.setName("lblLainLain"); // NOI18N
        FormInput.add(lblLainLain);
        lblLainLain.setBounds(50, 960, 190, 30);

        lblRingkasan.setForeground(new java.awt.Color(51, 51, 51));
        lblRingkasan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRingkasan.setText("Ringkasan pemeriksaan");
        lblRingkasan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblRingkasan.setName("lblRingkasan"); // NOI18N
        FormInput.add(lblRingkasan);
        lblRingkasan.setBounds(420, 960, 190, 30);

        lblKesimpulan.setForeground(new java.awt.Color(51, 51, 51));
        lblKesimpulan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKesimpulan.setText("Kesimpulan");
        lblKesimpulan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblKesimpulan.setName("lblKesimpulan"); // NOI18N
        FormInput.add(lblKesimpulan);
        lblKesimpulan.setBounds(790, 960, 190, 30);

        scrollRadiologi.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollRadiologi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollRadiologi.setName("scrollRadiologi"); // NOI18N

        txtRadiologi.setColumns(20);
        txtRadiologi.setRows(5);
        txtRadiologi.setName("txtRadiologi"); // NOI18N
        scrollRadiologi.setViewportView(txtRadiologi);

        FormInput.add(scrollRadiologi);
        scrollRadiologi.setBounds(420, 910, 350, 40);

        scrollOdontogram.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollOdontogram.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollOdontogram.setName("scrollOdontogram"); // NOI18N

        txtOdontogram.setColumns(20);
        txtOdontogram.setRows(5);
        txtOdontogram.setName("txtOdontogram"); // NOI18N
        scrollOdontogram.setViewportView(txtOdontogram);

        FormInput.add(scrollOdontogram);
        scrollOdontogram.setBounds(790, 910, 350, 40);

        scrollLainLain.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLainLain.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollLainLain.setName("scrollLainLain"); // NOI18N

        txtLainLain.setColumns(20);
        txtLainLain.setRows(5);
        txtLainLain.setName("txtLainLain"); // NOI18N
        scrollLainLain.setViewportView(txtLainLain);

        FormInput.add(scrollLainLain);
        scrollLainLain.setBounds(50, 990, 350, 40);

        scrollRingkasan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollRingkasan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollRingkasan.setName("scrollRingkasan"); // NOI18N

        txtRingkasan.setColumns(20);
        txtRingkasan.setRows(5);
        txtRingkasan.setName("txtRingkasan"); // NOI18N
        scrollRingkasan.setViewportView(txtRingkasan);

        FormInput.add(scrollRingkasan);
        scrollRingkasan.setBounds(420, 990, 350, 40);

        scrollKesimpulan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollKesimpulan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollKesimpulan.setName("scrollKesimpulan"); // NOI18N

        txtKesimpulan.setColumns(20);
        txtKesimpulan.setRows(5);
        txtKesimpulan.setName("txtKesimpulan"); // NOI18N
        scrollKesimpulan.setViewportView(txtKesimpulan);

        FormInput.add(scrollKesimpulan);
        scrollKesimpulan.setBounds(790, 990, 350, 40);

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/liattindakan.png"))); // NOI18N
        jLabel26.setText("Lampiran Pemeriksaan");
        jLabel26.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel26.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel26.setIconTextGap(10);
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(20, 1060, 300, 30);

        jSeparator8.setForeground(new java.awt.Color(229, 229, 229));
        jSeparator8.setName("jSeparator8"); // NOI18N
        FormInput.add(jSeparator8);
        jSeparator8.setBounds(20, 1050, 1120, 10);

        lblLampiranHasilPemeriksaanKlinis.setForeground(new java.awt.Color(51, 51, 51));
        lblLampiranHasilPemeriksaanKlinis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLampiranHasilPemeriksaanKlinis.setText("Hasil Pemeriksaan Klinis");
        lblLampiranHasilPemeriksaanKlinis.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLampiranHasilPemeriksaanKlinis.setName("lblLampiranHasilPemeriksaanKlinis"); // NOI18N
        FormInput.add(lblLampiranHasilPemeriksaanKlinis);
        lblLampiranHasilPemeriksaanKlinis.setBounds(50, 1110, 190, 30);

        scrollLampiranHasilPemeriksaanKlinis.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLampiranHasilPemeriksaanKlinis.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollLampiranHasilPemeriksaanKlinis.setName("scrollLampiranHasilPemeriksaanKlinis"); // NOI18N

        txtLaboratorium1.setColumns(20);
        txtLaboratorium1.setRows(5);
        txtLaboratorium1.setName("txtLaboratorium1"); // NOI18N
        scrollLampiranHasilPemeriksaanKlinis.setViewportView(txtLaboratorium1);

        FormInput.add(scrollLampiranHasilPemeriksaanKlinis);
        scrollLampiranHasilPemeriksaanKlinis.setBounds(50, 1140, 350, 40);

        lblLampiranPemeriksaanToksikologi.setForeground(new java.awt.Color(51, 51, 51));
        lblLampiranPemeriksaanToksikologi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLampiranPemeriksaanToksikologi.setText("Pemeriksaan Toksikologi");
        lblLampiranPemeriksaanToksikologi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLampiranPemeriksaanToksikologi.setName("lblLampiranPemeriksaanToksikologi"); // NOI18N
        FormInput.add(lblLampiranPemeriksaanToksikologi);
        lblLampiranPemeriksaanToksikologi.setBounds(420, 1110, 190, 30);

        scrollLampiranPemeriksaanToksikologi.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLampiranPemeriksaanToksikologi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollLampiranPemeriksaanToksikologi.setName("scrollLampiranPemeriksaanToksikologi"); // NOI18N

        txtRadiologi1.setColumns(20);
        txtRadiologi1.setRows(5);
        txtRadiologi1.setName("txtRadiologi1"); // NOI18N
        scrollLampiranPemeriksaanToksikologi.setViewportView(txtRadiologi1);

        FormInput.add(scrollLampiranPemeriksaanToksikologi);
        scrollLampiranPemeriksaanToksikologi.setBounds(420, 1140, 350, 40);

        lblLampiranPemeriksaanHistopatologi.setForeground(new java.awt.Color(51, 51, 51));
        lblLampiranPemeriksaanHistopatologi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLampiranPemeriksaanHistopatologi.setText("Pemeriksaan Histopatologi");
        lblLampiranPemeriksaanHistopatologi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLampiranPemeriksaanHistopatologi.setName("lblLampiranPemeriksaanHistopatologi"); // NOI18N
        FormInput.add(lblLampiranPemeriksaanHistopatologi);
        lblLampiranPemeriksaanHistopatologi.setBounds(790, 1110, 190, 30);

        scrollLampiranPemeriksaanHistopatologi.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLampiranPemeriksaanHistopatologi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollLampiranPemeriksaanHistopatologi.setName("scrollLampiranPemeriksaanHistopatologi"); // NOI18N

        txtOdontogram1.setColumns(20);
        txtOdontogram1.setRows(5);
        txtOdontogram1.setName("txtOdontogram1"); // NOI18N
        scrollLampiranPemeriksaanHistopatologi.setViewportView(txtOdontogram1);

        FormInput.add(scrollLampiranPemeriksaanHistopatologi);
        scrollLampiranPemeriksaanHistopatologi.setBounds(790, 1140, 350, 40);

        lblLampiranFoto.setForeground(new java.awt.Color(51, 51, 51));
        lblLampiranFoto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLampiranFoto.setText("Foto");
        lblLampiranFoto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLampiranFoto.setName("lblLampiranFoto"); // NOI18N
        FormInput.add(lblLampiranFoto);
        lblLampiranFoto.setBounds(50, 1190, 190, 30);

        scrollLampiranFoto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLampiranFoto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollLampiranFoto.setName("scrollLampiranFoto"); // NOI18N

        txtLainLain1.setColumns(20);
        txtLainLain1.setRows(5);
        txtLainLain1.setName("txtLainLain1"); // NOI18N
        scrollLampiranFoto.setViewportView(txtLainLain1);

        FormInput.add(scrollLampiranFoto);
        scrollLampiranFoto.setBounds(50, 1220, 350, 40);

        lblLampiranVideo.setForeground(new java.awt.Color(51, 51, 51));
        lblLampiranVideo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLampiranVideo.setText("Video");
        lblLampiranVideo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLampiranVideo.setName("lblLampiranVideo"); // NOI18N
        FormInput.add(lblLampiranVideo);
        lblLampiranVideo.setBounds(420, 1190, 190, 30);

        scrollLampiranVideo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLampiranVideo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollLampiranVideo.setName("scrollLampiranVideo"); // NOI18N

        txtRingkasan1.setColumns(20);
        txtRingkasan1.setRows(5);
        txtRingkasan1.setName("txtRingkasan1"); // NOI18N
        scrollLampiranVideo.setViewportView(txtRingkasan1);

        FormInput.add(scrollLampiranVideo);
        scrollLampiranVideo.setBounds(420, 1220, 350, 40);

        lblLampiran_lain.setForeground(new java.awt.Color(51, 51, 51));
        lblLampiran_lain.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLampiran_lain.setText("Lain-lain");
        lblLampiran_lain.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLampiran_lain.setName("lblLampiran_lain"); // NOI18N
        FormInput.add(lblLampiran_lain);
        lblLampiran_lain.setBounds(790, 1190, 190, 30);

        scrollLampiranLain.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollLampiranLain.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollLampiranLain.setName("scrollLampiranLain"); // NOI18N

        txtKesimpulan1.setColumns(20);
        txtKesimpulan1.setRows(5);
        txtKesimpulan1.setName("txtKesimpulan1"); // NOI18N
        scrollLampiranLain.setViewportView(txtKesimpulan1);

        FormInput.add(scrollLampiranLain);
        scrollLampiranLain.setBounds(790, 1220, 350, 40);

        ScrollInput.setViewportView(FormInput);

        PanelInput.add(ScrollInput, java.awt.BorderLayout.CENTER);

        PanelKananInput.setMinimumSize(new java.awt.Dimension(240, 120));
        PanelKananInput.setName("PanelKananInput"); // NOI18N
        PanelKananInput.setPreferredSize(new java.awt.Dimension(380, 430));
        PanelKananInput.setLayout(null);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/resumeri.png"))); // NOI18N
        jLabel17.setText("Surat Permintaan VeR");
        jLabel17.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel17.setIconTextGap(10);
        jLabel17.setName("jLabel17"); // NOI18N
        PanelKananInput.add(jLabel17);
        jLabel17.setBounds(10, 10, 260, 30);

        lblNoSuratVer.setForeground(new java.awt.Color(51, 51, 51));
        lblNoSuratVer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoSuratVer.setText("No. Surat Keterangan VeR");
        lblNoSuratVer.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblNoSuratVer.setName("lblNoSuratVer"); // NOI18N
        PanelKananInput.add(lblNoSuratVer);
        lblNoSuratVer.setBounds(40, 50, 190, 30);

        NoSurat.setForeground(new java.awt.Color(0, 0, 0));
        NoSurat.setText("tes");
        NoSurat.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        NoSurat.setName("NoSurat"); // NOI18N
        PanelKananInput.add(NoSurat);
        NoSurat.setBounds(40, 80, 300, 26);

        lblNoSuratPermintaan.setForeground(new java.awt.Color(51, 51, 51));
        lblNoSuratPermintaan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoSuratPermintaan.setText("No. Surat Permintaan VeR");
        lblNoSuratPermintaan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblNoSuratPermintaan.setName("lblNoSuratPermintaan"); // NOI18N
        PanelKananInput.add(lblNoSuratPermintaan);
        lblNoSuratPermintaan.setBounds(40, 120, 260, 30);

        txtNoSuratPermintaan.setForeground(new java.awt.Color(0, 0, 0));
        txtNoSuratPermintaan.setText("tes");
        txtNoSuratPermintaan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtNoSuratPermintaan.setName("txtNoSuratPermintaan"); // NOI18N
        PanelKananInput.add(txtNoSuratPermintaan);
        txtNoSuratPermintaan.setBounds(40, 150, 300, 26);

        lblJenisPemeriksaan.setForeground(new java.awt.Color(51, 51, 51));
        lblJenisPemeriksaan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblJenisPemeriksaan.setText("Jenis pemeriksaan diminta");
        lblJenisPemeriksaan.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblJenisPemeriksaan.setName("lblJenisPemeriksaan"); // NOI18N
        PanelKananInput.add(lblJenisPemeriksaan);
        lblJenisPemeriksaan.setBounds(40, 190, 260, 30);

        txtJenisPemeriksaan.setForeground(new java.awt.Color(0, 0, 0));
        txtJenisPemeriksaan.setName("txtJenisPemeriksaan"); // NOI18N
        PanelKananInput.add(txtJenisPemeriksaan);
        txtJenisPemeriksaan.setBounds(40, 220, 300, 26);

        lblTglTerimaSPV.setForeground(new java.awt.Color(51, 51, 51));
        lblTglTerimaSPV.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTglTerimaSPV.setText("Tgl / Jam SPV diterima");
        lblTglTerimaSPV.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTglTerimaSPV.setName("lblTglTerimaSPV"); // NOI18N
        PanelKananInput.add(lblTglTerimaSPV);
        lblTglTerimaSPV.setBounds(40, 260, 190, 30);

        TanggalAkhir.setForeground(new java.awt.Color(0, 0, 0));
        TanggalAkhir.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-05-2026  06:34:20" }));
        TanggalAkhir.setDisplayFormat("dd-MM-yyyy  hh:mm:ss");
        TanggalAkhir.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        TanggalAkhir.setName("TanggalAkhir"); // NOI18N
        TanggalAkhir.setOpaque(false);
        TanggalAkhir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalAkhirActionPerformed(evt);
            }
        });
        TanggalAkhir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalAkhirKeyPressed(evt);
            }
        });
        PanelKananInput.add(TanggalAkhir);
        TanggalAkhir.setBounds(40, 290, 170, 26);

        lblStatusData.setForeground(new java.awt.Color(51, 51, 51));
        lblStatusData.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblStatusData.setText("Status Data");
        lblStatusData.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblStatusData.setName("lblStatusData"); // NOI18N
        PanelKananInput.add(lblStatusData);
        lblStatusData.setBounds(230, 260, 130, 30);

        cmbStatusData.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DRAFT", "FINAL" }));
        cmbStatusData.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbStatusData.setName("cmbStatusData"); // NOI18N
        PanelKananInput.add(cmbStatusData);
        cmbStatusData.setBounds(230, 290, 110, 26);

        lblPihakPembuatSPV.setForeground(new java.awt.Color(51, 51, 51));
        lblPihakPembuatSPV.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPihakPembuatSPV.setText("Pihak pembuat SPV");
        lblPihakPembuatSPV.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPihakPembuatSPV.setName("lblPihakPembuatSPV"); // NOI18N
        PanelKananInput.add(lblPihakPembuatSPV);
        lblPihakPembuatSPV.setBounds(40, 330, 190, 30);

        scrollPihakPembuatSPV.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrollPihakPembuatSPV.setName("scrollPihakPembuatSPV"); // NOI18N

        txtPihakPembuatSPV1.setColumns(20);
        txtPihakPembuatSPV1.setForeground(new java.awt.Color(0, 0, 0));
        txtPihakPembuatSPV1.setRows(5);
        txtPihakPembuatSPV1.setName("txtPihakPembuatSPV1"); // NOI18N
        scrollPihakPembuatSPV.setViewportView(txtPihakPembuatSPV1);

        PanelKananInput.add(scrollPihakPembuatSPV);
        scrollPihakPembuatSPV.setBounds(40, 360, 300, 70);

        PanelInput.add(PanelKananInput, java.awt.BorderLayout.LINE_END);

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText("Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setIconTextGap(10);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 25));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        internalFrame1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
            isPsien();
        }else{            
            Valid.pindah(evt,TCari,NoSurat);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "pasien");
        } else if (NoSurat.getText().trim().equals("")) {
            Valid.textKosong(NoSurat, "No. Surat VeR");
        } else if (KdDokter.getText().trim().equals("")) {
            Valid.textKosong(KdDokter, "Dokter");
        } else {
            simpanVisum();
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,TanggalAkhir,BtnBatal);
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
        hapusVisum();
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if (idDipilih.trim().equals("")) {
            JOptionPane.showMessageDialog(null,"Maaf, pilih dulu data VeR yang mau diganti pada tabel...!!!!");
            tbObat.requestFocus();
        } else if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
            Valid.textKosong(TNoRw,"pasien");
        } else if (NoSurat.getText().trim().equals("")) {
            Valid.textKosong(NoSurat,"No. Surat VeR");
        } else {
            gantiVisum();
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
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnEdit,TCari);}
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
            tgl=" surat_keterangan_ranap.tanggalawal between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' ";
            if(TCari.getText().trim().equals("")){
                Valid.MyReportqry("rptDataSuratKeteranganRawatInap.jasper","report","::[ Data Surat Keterangan Rawat Inap ]::",
                     "select surat_keterangan_ranap.no_surat,surat_keterangan_ranap.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"+
                     "surat_keterangan_ranap.tanggalawal,surat_keterangan_ranap.tanggalakhir,surat_keterangan_ranap.kd_dokter "+                  
                     "from surat_keterangan_ranap inner join reg_periksa on surat_keterangan_ranap.no_rawat=reg_periksa.no_rawat "+
                     "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                     "where "+tgl+"order by surat_keterangan_ranap.no_surat",param);
            }else{
                Valid.MyReportqry("rptDataSuratKeteranganRawatInap.jasper","report","::[ Data Surat Keterangan Rawat Inap ]::",
                    "select surat_keterangan_ranap.no_surat,surat_keterangan_ranap.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,"+
                     "surat_keterangan_ranap.tanggalawal,surat_keterangan_ranap.tanggalakhir,surat_keterangan_ranap.kd_dokter "+                  
                     "from surat_keterangan_ranap inner join reg_periksa on surat_keterangan_ranap.no_rawat=reg_periksa.no_rawat "+
                     "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                     "where "+tgl+"and no_surat like '%"+TCari.getText().trim()+"%' or "+
                     tgl+"and surat_keterangan_ranap.no_rawat like '%"+TCari.getText().trim()+"%' or "+
                     tgl+"and reg_periksa.no_rkm_medis like '%"+TCari.getText().trim()+"%' or "+
                     tgl+"and pasien.nm_pasien like '%"+TCari.getText().trim()+"%' or "+
                     tgl+"and surat_keterangan_ranap.tanggalawal like '%"+TCari.getText().trim()+"%' or "+
                     tgl+"and surat_keterangan_ranap.tanggalakhir like '%"+TCari.getText().trim()+"%' "+
                     "order by surat_keterangan_ranap.no_surat",param);
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
   
                                  
    private void TanggalAkhirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalAkhirKeyPressed
        Valid.pindah(evt,TanggalAkhir,BtnSimpan);
}//GEN-LAST:event_TanggalAkhirKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
       isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void tbObatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbObatKeyReleased

    private void TanggalAkhirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalAkhirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalAkhirActionPerformed

    private void MnCetakSuratRawatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSuratRawatActionPerformed
       if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try{
                String idCetak = idDipilih.trim();
                if(idCetak.equals("")){
                    idCetak = Sequel.cariIsi("select cast(id as char) from "+TABEL_VISUM+" where no_rawat='"+amanSql(TNoRw.getText())+"' and no_surat_ver='"+amanSql(NoSurat.getText())+"' order by id desc limit 1");
                }
                if(idCetak.equals("")){
                    idCetak = Sequel.cariIsi("select cast(id as char) from "+TABEL_VISUM+" where no_rawat='"+amanSql(TNoRw.getText())+"' order by id desc limit 1");
                }
                if(idCetak.equals("")){
                    JOptionPane.showMessageDialog(null,"Maaf, data Visum Et Repertum belum tersimpan. Silahkan simpan data terlebih dahulu...!!!");
                    return;
                }

                Map<String, Object> param = new HashMap<>();
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());
                String projectDir = System.getProperty("user.dir");
                String logoPath = projectDir + "/setting/logo2.png";
                param.put("logo2", logoPath);

                String kdDokterTTD = Sequel.cariIsi("select kd_dokter from "+TABEL_VISUM+" where id='"+amanSql(idCetak)+"'");
                String namaDokterTTD = Sequel.cariIsi("select nama_dokter from "+TABEL_VISUM+" where id='"+amanSql(idCetak)+"'");
                String nipDokterTTD = Sequel.cariIsi("select nip_dokter from "+TABEL_VISUM+" where id='"+amanSql(idCetak)+"'");
                String tglKeluarTTD = Sequel.cariIsi("select ifnull(date_format(tgl_keluar_ver,'%d-%m-%Y'),date_format(curdate(),'%d-%m-%Y')) from "+TABEL_VISUM+" where id='"+amanSql(idCetak)+"'");
                if(kdDokterTTD.equals("")) kdDokterTTD = KdDokter.getText();
                if(namaDokterTTD.equals("")) namaDokterTTD = NmDokter.getText();
                if(nipDokterTTD.equals("")) nipDokterTTD = kdDokterTTD;
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",kdDokterTTD);
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+namaDokterTTD+"\nID "+(finger.equals("")?nipDokterTTD:finger)+"\n"+tglKeluarTTD);

                Valid.MyReportqry("rptSuratVisumEtRepertum.jasper","report","::[ Surat Keterangan Visum Et Repertum ]::",
                    sqlCetakVisum(idCetak),param);
            }catch(Exception e){
                System.out.println("Notifikasi cetak surat visum et repertum : "+e);
                JOptionPane.showMessageDialog(null,"Gagal mencetak Surat Visum Et Repertum : "+e.getMessage());
            }finally{
                this.setCursor(Cursor.getDefaultCursor());
            }
       }        
    }//GEN-LAST:event_MnCetakSuratRawatActionPerformed

    private String sqlCetakVisum(String idCetak) {
        return "select " +
            "cast(v.id as char) as id," +
            "ifnull(v.no_rawat,'') as no_rawat," +
            "ifnull(v.no_rkm_medis,'') as no_rkm_medis," +
            "ifnull(v.no_surat_ver,'') as no_surat_ver," +
            "ifnull(v.no_surat_permintaan,'') as no_surat_permintaan," +
            "ifnull(date_format(v.tgl_terima_spv,'%Y-%m-%d %H:%i:%s'),'') as tgl_terima_spv," +
            "ifnull(date_format(v.tgl_terima_spv,'%d-%m-%Y %H:%i:%s'),'') as tgl_terima_spv_fmt," +
            "ifnull(v.pihak_pembuat_spv,'') as pihak_pembuat_spv," +
            "ifnull(v.jenis_pemeriksaan_diminta,'') as jenis_pemeriksaan_diminta," +
            "ifnull(v.tempat_pemeriksaan,'') as tempat_pemeriksaan," +
            "ifnull(date_format(v.tgl_pemeriksaan,'%Y-%m-%d %H:%i:%s'),'') as tgl_pemeriksaan," +
            "ifnull(date_format(v.tgl_pemeriksaan,'%d-%m-%Y %H:%i:%s'),'') as tgl_pemeriksaan_fmt," +
            "concat(ifnull(v.tempat_pemeriksaan,''),if(v.tgl_pemeriksaan is null,'',concat(', ',date_format(v.tgl_pemeriksaan,'%d-%m-%Y %H:%i:%s')))) as tempat_tanggal_pemeriksaan," +
            "ifnull(v.nama_pasien,'') as nama_pasien," +
            "ifnull(date_format(v.tgl_lahir,'%Y-%m-%d'),'') as tgl_lahir," +
            "ifnull(date_format(v.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir_fmt," +
            "ifnull(v.umur,'') as umur," +
            "concat(ifnull(date_format(v.tgl_lahir,'%d-%m-%Y'),''),if(ifnull(v.umur,'')='', '', concat(' / ',v.umur))) as tgl_lahir_umur," +
            "ifnull(v.alamat,'') as alamat," +
            "ifnull(v.no_bukti_identitas,'') as no_bukti_identitas," +
            "ifnull(v.anamnesis,'') as anamnesis," +
            "ifnull(v.kesadaran,'') as kesadaran," +
            "ifnull(v.denyut_nadi,'') as denyut_nadi," +
            "ifnull(v.pernapasan,'') as pernapasan," +
            "ifnull(v.tekanan_darah,'') as tekanan_darah," +
            "ifnull(v.suhu_tubuh,'') as suhu_tubuh," +
            "ifnull(v.pakaian,'') as pakaian," +
            "ifnull(v.tinggi_badan,'') as tinggi_badan," +
            "ifnull(v.berat_badan,'') as berat_badan," +
            "ifnull(v.ciri_khusus,'') as ciri_khusus," +
            "ifnull(v.kepala,'') as kepala," +
            "ifnull(v.leher,'') as leher," +
            "ifnull(v.bahu,'') as bahu," +
            "ifnull(v.dada,'') as dada," +
            "ifnull(v.punggung,'') as punggung," +
            "ifnull(v.perut,'') as perut," +
            "ifnull(v.pinggang,'') as pinggang," +
            "ifnull(v.bokong,'') as bokong," +
            "ifnull(v.dubur,'') as dubur," +
            "ifnull(v.alat_kelamin,'') as alat_kelamin," +
            "ifnull(v.anggota_gerak_atas,'') as anggota_gerak_atas," +
            "ifnull(v.anggota_gerak_bawah,'') as anggota_gerak_bawah," +
            "ifnull(v.pemeriksaan_laboratorium,'') as pemeriksaan_laboratorium," +
            "ifnull(v.pemeriksaan_radiologi,'') as pemeriksaan_radiologi," +
            "ifnull(v.pemeriksaan_odontogram,'') as pemeriksaan_odontogram," +
            "ifnull(v.pemeriksaan_lain_lain,'') as pemeriksaan_lain_lain," +
            "ifnull(v.ringkasan_pemeriksaan,'') as ringkasan_pemeriksaan," +
            "ifnull(v.kesimpulan,'') as kesimpulan," +
            "ifnull(v.lampiran_hasil_pemeriksaan_klinis,'') as lampiran_hasil_pemeriksaan_klinis," +
            "ifnull(v.lampiran_pemeriksaan_toksikologi,'') as lampiran_pemeriksaan_toksikologi," +
            "ifnull(v.lampiran_pemeriksaan_histopatologi,'') as lampiran_pemeriksaan_histopatologi," +
            "ifnull(v.lampiran_foto,'') as lampiran_foto," +
            "ifnull(v.lampiran_video,'') as lampiran_video," +
            "ifnull(v.lampiran_lain_lain,'') as lampiran_lain_lain," +
            "ifnull(v.tempat_keluar_ver,'') as tempat_keluar_ver," +
            "ifnull(date_format(v.tgl_keluar_ver,'%Y-%m-%d'),'') as tgl_keluar_ver," +
            "ifnull(date_format(v.tgl_keluar_ver,'%d-%m-%Y'),'') as tgl_keluar_ver_fmt," +
            "ifnull(v.kd_dokter,'') as kd_dokter," +
            "ifnull(v.nama_dokter,'') as nama_dokter," +
            "ifnull(v.nip_dokter,'') as nip_dokter," +
            "concat(ifnull(v.nama_dokter,''),if(ifnull(v.nip_dokter,'')='', '', concat(' / ',v.nip_dokter))) as dokter_nip," +
            "ifnull(v.jabatan_kompetensi,'') as jabatan_kompetensi," +
            "ifnull(date_format(v.created_at,'%Y-%m-%d %H:%i:%s'),'') as created_at," +
            "ifnull(date_format(v.updated_at,'%Y-%m-%d %H:%i:%s'),'') as updated_at," +
            "ifnull(v.created_by,'') as created_by," +
            "ifnull(v.updated_by,'') as updated_by," +
            "ifnull(v.status_data,'') as status_data " +
            "from "+TABEL_VISUM+" v where v.id='"+amanSql(idCetak)+"' limit 1";
    }

    private String amanSql(String nilai) {
        return nilai == null ? "" : nilai.replace("'", "''");
    }

    private void MnCetakSuratRawat1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakSuratRawat1ActionPerformed
       if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();
                param.put("TanggalAwal",TanggalAkhir.getSelectedItem().toString());                
                param.put("TanggalAkhir",TanggalAkhir.getSelectedItem().toString());
                param.put("nosakit",NoSurat.getText());
                param.put("nik",Sequel.cariIsi("select no_ktp from pasien where pasien.no_rkm_medis=?",TNoRM.getText()));
                param.put("dokter",Sequel.cariIsi("select nm_dokter from dokter inner join surat_keterangan_ranap on surat_keterangan_ranap.kd_dokter=dokter.kd_dokter where surat_keterangan_ranap.no_rawat=?",TNoRw.getText()));
                param.put("norm",Sequel.cariIsi("select no_rkm_medis from pasien where pasien.no_rkm_medis=?",TNoRM.getText()));
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());   
                String projectDir = System.getProperty("user.dir"); // Mendapatkan path direktori proyek
                String logoPath = projectDir + "/setting/logo2.png"; // Jalur relatif dari folder proyek
                param.put("logo2", logoPath);
                param.put("penyakit",Sequel.cariIsi("select concat(diagnosa_pasien.kd_penyakit,' ',penyakit.nm_penyakit) from diagnosa_pasien inner join reg_periksa inner join penyakit "+
                    "on diagnosa_pasien.no_rawat=reg_periksa.no_rawat and diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "+
                    "where diagnosa_pasien.no_rawat=? and diagnosa_pasien.prioritas='1'",TNoRw.getText()));
                kodedokter=Sequel.cariIsi("select dpjp_ranap.kd_dokter from dpjp_ranap where dpjp_ranap.no_rawat=?",TNoRw.getText());
                namadokter=Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",kodedokter);
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",kodedokter);
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+namadokter+"\nID "+(finger.equals("")?kodedokter:finger)+"\n"+Sequel.cariIsi("select DATE_FORMAT(reg_periksa.tgl_registrasi,'%d-%m-%Y') from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));  
                Valid.MyReportqry("rptSuratKeteranganRawatInap1.jasper","report","::[ Surat Keterangan Rawat Inap ]::",
                              "select DATE_FORMAT(reg_periksa.tgl_registrasi,'%d-%m-%Y')as tgl_registrasi,perusahaan_pasien.nama_perusahaan,reg_periksa.no_rawat,dokter.nm_dokter,pasien.keluarga,pasien.namakeluarga,pasien.tgl_lahir,if(pasien.jk='L','Laki-laki','Perempuan') as jk," +
                              " pasien.nm_pasien,if(pasien.jk='L','Laki-laki','Perempuan') as jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur)as umur,pasien.pekerjaan,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat" +
                              " from reg_periksa inner join pasien inner join dokter inner join kelurahan inner join perusahaan_pasien inner join kecamatan inner join kabupaten" +
                              " on reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_dokter=dokter.kd_dokter and pasien.kd_kel=kelurahan.kd_kel "+
                              "and pasien.perusahaan_pasien=perusahaan_pasien.kode_perusahaan and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kab=kabupaten.kd_kab "+
                              "where reg_periksa.no_rawat='"+TNoRw.getText()+"' ",param);
                this.setCursor(Cursor.getDefaultCursor());  
       }        // TODO add your handling code here:
    }//GEN-LAST:event_MnCetakSuratRawat1ActionPerformed

    private void KdDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokterKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?", NmDokter, KdDokter.getText());
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            TanggalAkhir.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            BtnDokterActionPerformed(null);
        }
    }//GEN-LAST:event_KdDokterKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        //        Valid.pindah(evt,Detik,cmbSkor1);
    }//GEN-LAST:event_BtnDokterKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SuratVisum dialog = new SuratVisum(new javax.swing.JFrame(), true);
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
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.TextBox KdDokter;
    private widget.Label LCount;
    private javax.swing.JMenuItem MnCetakSuratRawat;
    private javax.swing.JMenuItem MnCetakSuratRawat1;
    private widget.TextBox NmDokter;
    private widget.TextBox NoSurat;
    private javax.swing.JPanel PanelInput;
    private widget.PanelBiasa PanelKananInput;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane ScrollInput;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal TanggalAkhir;
    private widget.ComboBox cmbStatusData;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel11;
    private widget.Label jLabel14;
    private widget.Label jLabel17;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private widget.Label labelDokter;
    private widget.Label labelNamaPasien;
    private widget.Label labelNoRM;
    private widget.Label labelNoRawat;
    private widget.Label lblAlatKelamin;
    private widget.Label lblAnamnesis;
    private widget.Label lblAnggotaGerakAtas;
    private widget.Label lblAnggotaGerakBawah;
    private widget.Label lblBahu;
    private widget.Label lblBeratBadan;
    private widget.Label lblBokong;
    private widget.Label lblCiriKhusus;
    private widget.Label lblDada;
    private widget.Label lblDenyutNadi;
    private widget.Label lblDubur;
    private widget.Label lblJenisPemeriksaan;
    private widget.Label lblKepala;
    private widget.Label lblKesadaran;
    private widget.Label lblKesimpulan;
    private widget.Label lblLaboratorium;
    private widget.Label lblLainLain;
    private widget.Label lblLampiranFoto;
    private widget.Label lblLampiranHasilPemeriksaanKlinis;
    private widget.Label lblLampiranPemeriksaanHistopatologi;
    private widget.Label lblLampiranPemeriksaanToksikologi;
    private widget.Label lblLampiranVideo;
    private widget.Label lblLampiran_lain;
    private widget.Label lblLeher;
    private widget.Label lblNoSuratPermintaan;
    private widget.Label lblNoSuratVer;
    private widget.Label lblOdontogram;
    private widget.Label lblPakaian;
    private widget.Label lblPernapasan;
    private widget.Label lblPerut;
    private widget.Label lblPihakPembuatSPV;
    private widget.Label lblPinggang;
    private widget.Label lblPunggung;
    private widget.Label lblRadiologi;
    private widget.Label lblRingkasan;
    private widget.Label lblStatusData;
    private widget.Label lblSuhuTubuh;
    private widget.Label lblTekananDarah;
    private widget.Label lblTglTerimaSPV;
    private widget.Label lblTinggiBadan;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollAlatKelamin;
    private widget.ScrollPane scrollAnamnesis;
    private widget.ScrollPane scrollAnggotaGerakAtas;
    private widget.ScrollPane scrollAnggotaGerakBawah;
    private widget.ScrollPane scrollBahu;
    private widget.ScrollPane scrollBokong;
    private widget.ScrollPane scrollCiriKhusus;
    private widget.ScrollPane scrollDada;
    private widget.ScrollPane scrollDubur;
    private widget.ScrollPane scrollKepala;
    private widget.ScrollPane scrollKesimpulan;
    private widget.ScrollPane scrollLaboratorium;
    private widget.ScrollPane scrollLainLain;
    private widget.ScrollPane scrollLampiranFoto;
    private widget.ScrollPane scrollLampiranHasilPemeriksaanKlinis;
    private widget.ScrollPane scrollLampiranLain;
    private widget.ScrollPane scrollLampiranPemeriksaanHistopatologi;
    private widget.ScrollPane scrollLampiranPemeriksaanToksikologi;
    private widget.ScrollPane scrollLampiranVideo;
    private widget.ScrollPane scrollLeher;
    private widget.ScrollPane scrollOdontogram;
    private widget.ScrollPane scrollPakaian;
    private widget.ScrollPane scrollPerut;
    private widget.ScrollPane scrollPihakPembuatSPV;
    private widget.ScrollPane scrollPinggang;
    private widget.ScrollPane scrollPunggung;
    private widget.ScrollPane scrollRadiologi;
    private widget.ScrollPane scrollRingkasan;
    private widget.Table tbObat;
    private widget.TextArea txtAlatKelamin;
    private widget.TextArea txtAnamnesis;
    private widget.TextArea txtAnggotaGerakAtas;
    private widget.TextArea txtAnggotaGerakBawah;
    private widget.TextArea txtBahu;
    private widget.TextBox txtBeratBadan;
    private widget.TextArea txtBokong;
    private widget.TextArea txtCiriKhusus;
    private widget.TextArea txtDada;
    private widget.TextBox txtDenyutNadi;
    private widget.TextArea txtDubur;
    private widget.TextBox txtJenisPemeriksaan;
    private widget.TextArea txtKepala;
    private widget.TextBox txtKesadaran;
    private widget.TextArea txtKesimpulan;
    private widget.TextArea txtKesimpulan1;
    private widget.TextArea txtLaboratorium;
    private widget.TextArea txtLaboratorium1;
    private widget.TextArea txtLainLain;
    private widget.TextArea txtLainLain1;
    private widget.TextArea txtLeher;
    private widget.TextBox txtNoSuratPermintaan;
    private widget.TextArea txtOdontogram;
    private widget.TextArea txtOdontogram1;
    private widget.TextArea txtPakaian;
    private widget.TextBox txtPernapasan;
    private widget.TextArea txtPerut;
    private widget.TextArea txtPihakPembuatSPV1;
    private widget.TextArea txtPinggang;
    private widget.TextArea txtPunggung;
    private widget.TextArea txtRadiologi;
    private widget.TextArea txtRadiologi1;
    private widget.TextArea txtRingkasan;
    private widget.TextArea txtRingkasan1;
    private widget.TextBox txtSuhuTubuh;
    private widget.TextBox txtTekananDarah;
    private widget.TextBox txtTinggiBadan;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            String awal = Valid.SetTgl(DTPCari1.getSelectedItem()+"") + " 00:00:00";
            String akhir = Valid.SetTgl(DTPCari2.getSelectedItem()+"") + " 23:59:59";
            String cari = "%" + TCari.getText().trim() + "%";
            StringBuilder sql = new StringBuilder("select " + gabungKolomVisum() + " from " + TABEL_VISUM +
                    " where tgl_terima_spv between ? and ? ");
            if(!TCari.getText().trim().equals("")){
                sql.append(" and (");
                for(int idx=0; idx<KOLOM_CARI_VISUM.length; idx++){
                    if(idx>0) sql.append(" or ");
                    sql.append(KOLOM_CARI_VISUM[idx]).append(" like ?");
                }
                sql.append(") ");
            }
            sql.append(" order by tgl_terima_spv desc, no_surat_ver desc");
            ps = koneksi.prepareStatement(sql.toString());
            ps.setString(1, awal);
            ps.setString(2, akhir);
            if(!TCari.getText().trim().equals("")){
                int no = 3;
                for(int idx=0; idx<KOLOM_CARI_VISUM.length; idx++){
                    ps.setString(no++, cari);
                }
            }
            rs = ps.executeQuery();
            while(rs.next()){
                Object[] row = new Object[KOLOM_VISUM.length];
                for(int c=0; c<KOLOM_VISUM.length; c++){
                    row[c] = rs.getString(KOLOM_VISUM[c]);
                }
                tabMode.addRow(row);
            }
        }catch(Exception e){
            System.out.println("Notifikasi tampil surat visum : "+e);
        }finally{
            try{ if(rs!=null) rs.close(); }catch(Exception e){}
            try{ if(ps!=null) ps.close(); }catch(Exception e){}
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    public void emptTeks() {
        idDipilih = "";
        TNoRw.setText("");
        TNoRM.setText("");
        TPasien.setText("");
        KdDokter.setText("");
        NmDokter.setText("");
        labelNoRawat.setText("-");
        labelNoRM.setText("-");
        labelNamaPasien.setText("-");
        labelDokter.setText("-");
        txtNoSuratPermintaan.setText("");
        txtJenisPemeriksaan.setText("");
        txtPihakPembuatSPV1.setText("");
        txtAnamnesis.setText("");
        txtKesadaran.setText("");
        txtDenyutNadi.setText("");
        txtPernapasan.setText("");
        txtTekananDarah.setText("");
        txtSuhuTubuh.setText("");
        txtPakaian.setText("");
        txtTinggiBadan.setText("");
        txtBeratBadan.setText("");
        txtCiriKhusus.setText("");
        txtKepala.setText("");
        txtLeher.setText("");
        txtBahu.setText("");
        txtDada.setText("");
        txtPunggung.setText("");
        txtPerut.setText("");
        txtPinggang.setText("");
        txtBokong.setText("");
        txtDubur.setText("");
        txtAlatKelamin.setText("");
        txtAnggotaGerakAtas.setText("");
        txtAnggotaGerakBawah.setText("");
        txtLaboratorium.setText("");
        txtRadiologi.setText("");
        txtOdontogram.setText("");
        txtLainLain.setText("");
        txtRingkasan.setText("");
        txtKesimpulan.setText("");
        txtLaboratorium1.setText("");
        txtRadiologi1.setText("");
        txtOdontogram1.setText("");
        txtLainLain1.setText("");
        txtRingkasan1.setText("");
        txtKesimpulan1.setText("");
        tglLahirPasien = "";
        umurPasien = "";
        alamatPasien = "";
        noBuktiIdentitasPasien = "";
        tempatPemeriksaan = "";
        tglPemeriksaan = "";
        tempatKeluarVer = "";
        tglKeluarVer = "";
        nipDokter = "";
        jabatanKompetensi = "";
        cmbStatusData.setSelectedItem("DRAFT");
        TanggalAkhir.setDate(new Date());
        nomorSurat();
        NoSurat.requestFocus();
    }

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            idDipilih = nilaiTabel("id");
            TNoRw.setText(nilaiTabel("no_rawat"));
            TNoRM.setText(nilaiTabel("no_rkm_medis"));
            NoSurat.setText(nilaiTabel("no_surat_ver"));
            txtNoSuratPermintaan.setText(nilaiTabel("no_surat_permintaan"));
            setTanggalWidget(TanggalAkhir, nilaiTabel("tgl_terima_spv"));
            txtPihakPembuatSPV1.setText(nilaiTabel("pihak_pembuat_spv"));
            txtJenisPemeriksaan.setText(nilaiTabel("jenis_pemeriksaan_diminta"));
            tempatPemeriksaan = nilaiTabel("tempat_pemeriksaan");
            tglPemeriksaan = nilaiTabel("tgl_pemeriksaan");
            TPasien.setText(nilaiTabel("nama_pasien"));
            tglLahirPasien = nilaiTabel("tgl_lahir");
            umurPasien = nilaiTabel("umur");
            alamatPasien = nilaiTabel("alamat");
            noBuktiIdentitasPasien = nilaiTabel("no_bukti_identitas");
            txtAnamnesis.setText(nilaiTabel("anamnesis"));
            txtKesadaran.setText(nilaiTabel("kesadaran"));
            txtDenyutNadi.setText(nilaiTabel("denyut_nadi"));
            txtPernapasan.setText(nilaiTabel("pernapasan"));
            txtTekananDarah.setText(nilaiTabel("tekanan_darah"));
            txtSuhuTubuh.setText(nilaiTabel("suhu_tubuh"));
            txtPakaian.setText(nilaiTabel("pakaian"));
            txtTinggiBadan.setText(nilaiTabel("tinggi_badan"));
            txtBeratBadan.setText(nilaiTabel("berat_badan"));
            txtCiriKhusus.setText(nilaiTabel("ciri_khusus"));
            txtKepala.setText(nilaiTabel("kepala"));
            txtLeher.setText(nilaiTabel("leher"));
            txtBahu.setText(nilaiTabel("bahu"));
            txtDada.setText(nilaiTabel("dada"));
            txtPunggung.setText(nilaiTabel("punggung"));
            txtPerut.setText(nilaiTabel("perut"));
            txtPinggang.setText(nilaiTabel("pinggang"));
            txtBokong.setText(nilaiTabel("bokong"));
            txtDubur.setText(nilaiTabel("dubur"));
            txtAlatKelamin.setText(nilaiTabel("alat_kelamin"));
            txtAnggotaGerakAtas.setText(nilaiTabel("anggota_gerak_atas"));
            txtAnggotaGerakBawah.setText(nilaiTabel("anggota_gerak_bawah"));
            txtLaboratorium.setText(nilaiTabel("pemeriksaan_laboratorium"));
            txtRadiologi.setText(nilaiTabel("pemeriksaan_radiologi"));
            txtOdontogram.setText(nilaiTabel("pemeriksaan_odontogram"));
            txtLainLain.setText(nilaiTabel("pemeriksaan_lain_lain"));
            txtRingkasan.setText(nilaiTabel("ringkasan_pemeriksaan"));
            txtKesimpulan.setText(nilaiTabel("kesimpulan"));
            txtLaboratorium1.setText(nilaiTabel("lampiran_hasil_pemeriksaan_klinis"));
            txtRadiologi1.setText(nilaiTabel("lampiran_pemeriksaan_toksikologi"));
            txtOdontogram1.setText(nilaiTabel("lampiran_pemeriksaan_histopatologi"));
            txtLainLain1.setText(nilaiTabel("lampiran_foto"));
            txtRingkasan1.setText(nilaiTabel("lampiran_video"));
            txtKesimpulan1.setText(nilaiTabel("lampiran_lain_lain"));
            tempatKeluarVer = nilaiTabel("tempat_keluar_ver");
            tglKeluarVer = nilaiTabel("tgl_keluar_ver");
            KdDokter.setText(nilaiTabel("kd_dokter"));
            NmDokter.setText(nilaiTabel("nama_dokter"));
            nipDokter = nilaiTabel("nip_dokter");
            jabatanKompetensi = nilaiTabel("jabatan_kompetensi");
            cmbStatusData.setSelectedItem(nilaiTabel("status_data").equals("") ? "DRAFT" : nilaiTabel("status_data"));
            updateLabelRingkasan();
            ChkInput.setSelected(true);
            isForm();
        }
    }

    private void simpanVisum() {
        PreparedStatement psSimpan = null;
        try{
            loadDataPasienDariRegistrasi();
            if(hitungData("select count(*) from "+TABEL_VISUM+" where no_surat_ver=?", NoSurat.getText().trim()) > 0){
                JOptionPane.showMessageDialog(null,"Maaf, No. Surat VeR sudah pernah tersimpan...!!!!");
                NoSurat.requestFocus();
                return;
            }
            psSimpan = koneksi.prepareStatement(sqlInsertVisum());
            isiParameterInsert(psSimpan);
            psSimpan.executeUpdate();
            tampil();
            emptTeks();
            JOptionPane.showMessageDialog(null,"Data surat Visum Et Repertum berhasil disimpan.");
        }catch(Exception e){
            System.out.println("Notifikasi simpan surat visum : "+e);
            JOptionPane.showMessageDialog(null,"Gagal menyimpan data surat Visum Et Repertum : "+e.getMessage());
        }finally{
            try{ if(psSimpan!=null) psSimpan.close(); }catch(Exception e){}
        }
    }

    private void gantiVisum() {
        PreparedStatement psGanti = null;
        try{
            loadDataPasienDariRegistrasi();
            psGanti = koneksi.prepareStatement(sqlUpdateVisum());
            isiParameterUpdate(psGanti);
            psGanti.executeUpdate();
            tampil();
            emptTeks();
            JOptionPane.showMessageDialog(null,"Data surat Visum Et Repertum berhasil diganti.");
        }catch(Exception e){
            System.out.println("Notifikasi ganti surat visum : "+e);
            JOptionPane.showMessageDialog(null,"Gagal mengganti data surat Visum Et Repertum : "+e.getMessage());
        }finally{
            try{ if(psGanti!=null) psGanti.close(); }catch(Exception e){}
        }
    }

    private void hapusVisum() {
        if(idDipilih.trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, pilih dulu data VeR yang mau dihapus pada tabel...!!!!");
            tbObat.requestFocus();
            return;
        }
        if(JOptionPane.showConfirmDialog(null,"Yakin data surat Visum Et Repertum ini mau dihapus?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            PreparedStatement psHapus = null;
            try{
                psHapus = koneksi.prepareStatement("delete from "+TABEL_VISUM+" where id=?");
                psHapus.setString(1, idDipilih);
                psHapus.executeUpdate();
                tampil();
                emptTeks();
                JOptionPane.showMessageDialog(null,"Data surat Visum Et Repertum berhasil dihapus.");
            }catch(Exception e){
                System.out.println("Notifikasi hapus surat visum : "+e);
                JOptionPane.showMessageDialog(null,"Gagal menghapus data surat Visum Et Repertum : "+e.getMessage());
            }finally{
                try{ if(psHapus!=null) psHapus.close(); }catch(Exception e){}
            }
        }
    }

    private void isiParameterInsert(PreparedStatement psIsi) throws SQLException {
        int no = 1;
        for(int c=1; c<KOLOM_VISUM.length; c++){
            setNullable(psIsi, no++, ambilNilaiKolom(KOLOM_VISUM[c]));
        }
    }

    private void isiParameterUpdate(PreparedStatement psIsi) throws SQLException {
        int no = 1;
        for(int c=1; c<KOLOM_VISUM.length; c++){
            if(KOLOM_VISUM[c].equals("created_at") || KOLOM_VISUM[c].equals("created_by")){
                continue;
            }
            setNullable(psIsi, no++, ambilNilaiKolom(KOLOM_VISUM[c]));
        }
        psIsi.setString(no, idDipilih);
    }

    private String ambilNilaiKolom(String kolom) {
        if(kolom.equals("no_rawat")) return TNoRw.getText();
        if(kolom.equals("no_rkm_medis")) return TNoRM.getText();
        if(kolom.equals("no_surat_ver")) return NoSurat.getText();
        if(kolom.equals("no_surat_permintaan")) return txtNoSuratPermintaan.getText();
        if(kolom.equals("tgl_terima_spv")) return tanggalTerimaSPV();
        if(kolom.equals("pihak_pembuat_spv")) return txtPihakPembuatSPV1.getText();
        if(kolom.equals("jenis_pemeriksaan_diminta")) return txtJenisPemeriksaan.getText();
        if(kolom.equals("tempat_pemeriksaan")) return tempatPemeriksaan;
        if(kolom.equals("tgl_pemeriksaan")) return tglPemeriksaan.equals("") ? tanggalTerimaSPV() : tglPemeriksaan;
        if(kolom.equals("nama_pasien")) return TPasien.getText();
        if(kolom.equals("tgl_lahir")) return tglLahirPasien;
        if(kolom.equals("umur")) return umurPasien;
        if(kolom.equals("alamat")) return alamatPasien;
        if(kolom.equals("no_bukti_identitas")) return noBuktiIdentitasPasien;
        if(kolom.equals("anamnesis")) return txtAnamnesis.getText();
        if(kolom.equals("kesadaran")) return txtKesadaran.getText();
        if(kolom.equals("denyut_nadi")) return txtDenyutNadi.getText();
        if(kolom.equals("pernapasan")) return txtPernapasan.getText();
        if(kolom.equals("tekanan_darah")) return txtTekananDarah.getText();
        if(kolom.equals("suhu_tubuh")) return txtSuhuTubuh.getText();
        if(kolom.equals("pakaian")) return txtPakaian.getText();
        if(kolom.equals("tinggi_badan")) return txtTinggiBadan.getText();
        if(kolom.equals("berat_badan")) return txtBeratBadan.getText();
        if(kolom.equals("ciri_khusus")) return txtCiriKhusus.getText();
        if(kolom.equals("kepala")) return txtKepala.getText();
        if(kolom.equals("leher")) return txtLeher.getText();
        if(kolom.equals("bahu")) return txtBahu.getText();
        if(kolom.equals("dada")) return txtDada.getText();
        if(kolom.equals("punggung")) return txtPunggung.getText();
        if(kolom.equals("perut")) return txtPerut.getText();
        if(kolom.equals("pinggang")) return txtPinggang.getText();
        if(kolom.equals("bokong")) return txtBokong.getText();
        if(kolom.equals("dubur")) return txtDubur.getText();
        if(kolom.equals("alat_kelamin")) return txtAlatKelamin.getText();
        if(kolom.equals("anggota_gerak_atas")) return txtAnggotaGerakAtas.getText();
        if(kolom.equals("anggota_gerak_bawah")) return txtAnggotaGerakBawah.getText();
        if(kolom.equals("pemeriksaan_laboratorium")) return txtLaboratorium.getText();
        if(kolom.equals("pemeriksaan_radiologi")) return txtRadiologi.getText();
        if(kolom.equals("pemeriksaan_odontogram")) return txtOdontogram.getText();
        if(kolom.equals("pemeriksaan_lain_lain")) return txtLainLain.getText();
        if(kolom.equals("ringkasan_pemeriksaan")) return txtRingkasan.getText();
        if(kolom.equals("kesimpulan")) return txtKesimpulan.getText();
        if(kolom.equals("lampiran_hasil_pemeriksaan_klinis")) return txtLaboratorium1.getText();
        if(kolom.equals("lampiran_pemeriksaan_toksikologi")) return txtRadiologi1.getText();
        if(kolom.equals("lampiran_pemeriksaan_histopatologi")) return txtOdontogram1.getText();
        if(kolom.equals("lampiran_foto")) return txtLainLain1.getText();
        if(kolom.equals("lampiran_video")) return txtRingkasan1.getText();
        if(kolom.equals("lampiran_lain_lain")) return txtKesimpulan1.getText();
        if(kolom.equals("tempat_keluar_ver")) return tempatKeluarVer;
        if(kolom.equals("tgl_keluar_ver")) return tglKeluarVer;
        if(kolom.equals("kd_dokter")) return KdDokter.getText();
        if(kolom.equals("nama_dokter")) return NmDokter.getText();
        if(kolom.equals("nip_dokter")) return nipDokter.equals("") ? KdDokter.getText() : nipDokter;
        if(kolom.equals("jabatan_kompetensi")) return jabatanKompetensi;
        if(kolom.equals("created_at") || kolom.equals("updated_at")) return waktuSekarang();
        if(kolom.equals("created_by") || kolom.equals("updated_by")) return akses.getkode();
        if(kolom.equals("status_data")) return cmbStatusData.getSelectedItem().toString();
        return "";
    }

    private String sqlInsertVisum() {
        StringBuilder kolom = new StringBuilder();
        StringBuilder nilai = new StringBuilder();
        for(int c=1; c<KOLOM_VISUM.length; c++){
            if(c>1){
                kolom.append(",");
                nilai.append(",");
            }
            kolom.append(KOLOM_VISUM[c]);
            nilai.append("?");
        }
        return "insert into "+TABEL_VISUM+" ("+kolom.toString()+") values ("+nilai.toString()+")";
    }

    private String sqlUpdateVisum() {
        StringBuilder sql = new StringBuilder("update "+TABEL_VISUM+" set ");
        boolean pertama = true;
        for(int c=1; c<KOLOM_VISUM.length; c++){
            if(KOLOM_VISUM[c].equals("created_at") || KOLOM_VISUM[c].equals("created_by")){
                continue;
            }
            if(!pertama) sql.append(",");
            sql.append(KOLOM_VISUM[c]).append("=?");
            pertama = false;
        }
        sql.append(" where id=?");
        return sql.toString();
    }

    private void pastikanKolomLampiranVisum() {
        tambahKolomJikaBelumAda("lampiran_hasil_pemeriksaan_klinis", "TEXT NULL");
        tambahKolomJikaBelumAda("lampiran_pemeriksaan_toksikologi", "TEXT NULL");
        tambahKolomJikaBelumAda("lampiran_pemeriksaan_histopatologi", "TEXT NULL");
        tambahKolomJikaBelumAda("lampiran_foto", "TEXT NULL");
        tambahKolomJikaBelumAda("lampiran_video", "TEXT NULL");
        tambahKolomJikaBelumAda("lampiran_lain_lain", "TEXT NULL");
    }

    private void tambahKolomJikaBelumAda(String namaKolom, String definisiKolom) {
        PreparedStatement psCek = null;
        ResultSet rsCek = null;
        java.sql.Statement stAlter = null;
        try{
            psCek = koneksi.prepareStatement(
                "select count(*) from information_schema.columns where table_schema=database() and table_name=? and column_name=?"
            );
            psCek.setString(1, TABEL_VISUM);
            psCek.setString(2, namaKolom);
            rsCek = psCek.executeQuery();
            if(rsCek.next() && rsCek.getInt(1)==0){
                stAlter = koneksi.createStatement();
                stAlter.executeUpdate("alter table "+TABEL_VISUM+" add column "+namaKolom+" "+definisiKolom);
            }
        }catch(Exception e){
            System.out.println("Notifikasi cek/tambah kolom lampiran visum "+namaKolom+" : "+e);
        }finally{
            try{ if(rsCek!=null) rsCek.close(); }catch(Exception e){}
            try{ if(psCek!=null) psCek.close(); }catch(Exception e){}
            try{ if(stAlter!=null) stAlter.close(); }catch(Exception e){}
        }
    }

    private String gabungKolomVisum() {
        StringBuilder kolom = new StringBuilder();
        for(int c=0; c<KOLOM_VISUM.length; c++){
            if(c>0) kolom.append(",");
            kolom.append(KOLOM_VISUM[c]);
        }
        return kolom.toString();
    }

    private int indeksKolom(String namaKolom) {
        for(int c=0; c<KOLOM_VISUM.length; c++){
            if(KOLOM_VISUM[c].equals(namaKolom)) return c;
        }
        return -1;
    }

    private String nilaiTabel(String namaKolom) {
        int kolom = indeksKolom(namaKolom);
        if(kolom < 0 || tbObat.getSelectedRow() < 0) return "";
        Object nilai = tbObat.getValueAt(tbObat.getSelectedRow(), kolom);
        return nilai == null ? "" : nilai.toString();
    }

    private void setNullable(PreparedStatement psIsi, int no, String nilai) throws SQLException {
        if(nilai == null || nilai.trim().equals("")){
            psIsi.setNull(no, Types.VARCHAR);
        }else{
            psIsi.setString(no, nilai.trim());
        }
    }

    private String tanggalTerimaSPV() {
        try{
            String tanggal = TanggalAkhir.getSelectedItem().toString();
            if(tanggal.length() >= 10){
                return Valid.SetTgl(tanggal.substring(0,10)) + " 00:00:00";
            }
        }catch(Exception e){}
        return waktuSekarang();
    }

    private String waktuSekarang() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private String formatTanggal(Date tanggal) {
        if(tanggal == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(tanggal);
    }

    private String formatTanggalJam(Date tanggal) {
        if(tanggal == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tanggal);
    }

    private void setTanggalWidget(widget.Tanggal target, String nilai) {
        try{
            if(nilai != null && nilai.length() >= 10){
                Valid.SetTgl(target, nilai.substring(0,10));
            }
        }catch(Exception e){}
    }

    private int hitungData(String sql, String parameter) {
        PreparedStatement psHitung = null;
        ResultSet rsHitung = null;
        try{
            psHitung = koneksi.prepareStatement(sql);
            psHitung.setString(1, parameter);
            rsHitung = psHitung.executeQuery();
            if(rsHitung.next()){
                return rsHitung.getInt(1);
            }
        }catch(Exception e){
            System.out.println("Notifikasi hitung surat visum : "+e);
        }finally{
            try{ if(rsHitung!=null) rsHitung.close(); }catch(Exception e){}
            try{ if(psHitung!=null) psHitung.close(); }catch(Exception e){}
        }
        return 0;
    }

    private void loadDataPasienDariRegistrasi() {
        PreparedStatement psPasien = null;
        ResultSet rsPasien = null;
        try{
            psPasien = koneksi.prepareStatement(
                "select pasien.tgl_lahir, concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur, " +
                "concat(pasien.alamat, if(kelurahan.nm_kel is null or kelurahan.nm_kel='','',concat(', ',kelurahan.nm_kel)), " +
                "if(kecamatan.nm_kec is null or kecamatan.nm_kec='','',concat(', ',kecamatan.nm_kec)), " +
                "if(kabupaten.nm_kab is null or kabupaten.nm_kab='','',concat(', ',kabupaten.nm_kab))) as alamat, " +
                "pasien.no_ktp, concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) as tgl_periksa, " +
                "dokter.nm_dokter, dokter.kd_dokter as nip_dokter " +
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter " +
                "left join kelurahan on pasien.kd_kel=kelurahan.kd_kel " +
                "left join kecamatan on pasien.kd_kec=kecamatan.kd_kec " +
                "left join kabupaten on pasien.kd_kab=kabupaten.kd_kab " +
                "where reg_periksa.no_rawat=?"
            );
            psPasien.setString(1, TNoRw.getText());
            rsPasien = psPasien.executeQuery();
            if(rsPasien.next()){
                if(tglLahirPasien.equals("")) tglLahirPasien = rsPasien.getString("tgl_lahir") == null ? "" : rsPasien.getString("tgl_lahir");
                if(umurPasien.equals("")) umurPasien = rsPasien.getString("umur") == null ? "" : rsPasien.getString("umur");
                if(alamatPasien.equals("")) alamatPasien = rsPasien.getString("alamat") == null ? "" : rsPasien.getString("alamat");
                if(noBuktiIdentitasPasien.equals("")) noBuktiIdentitasPasien = rsPasien.getString("no_ktp") == null ? "" : rsPasien.getString("no_ktp");
                if(tglPemeriksaan.equals("")) tglPemeriksaan = rsPasien.getString("tgl_periksa") == null ? "" : rsPasien.getString("tgl_periksa");
                if(NmDokter.getText().trim().equals("")) NmDokter.setText(rsPasien.getString("nm_dokter") == null ? "" : rsPasien.getString("nm_dokter"));
                if(nipDokter.equals("")) nipDokter = rsPasien.getString("nip_dokter") == null ? KdDokter.getText() : rsPasien.getString("nip_dokter");
            }
        }catch(Exception e){
            System.out.println("Notifikasi ambil data pasien surat visum : "+e);
        }finally{
            try{ if(rsPasien!=null) rsPasien.close(); }catch(Exception e){}
            try{ if(psPasien!=null) psPasien.close(); }catch(Exception e){}
        }
        if(tempatPemeriksaan.equals("")) tempatPemeriksaan = "IGD";
        updateLabelRingkasan();
    }

    private void updateLabelRingkasan() {
        labelNoRawat.setText(TNoRw.getText().trim().equals("") ? "-" : TNoRw.getText());
        labelNoRM.setText(TNoRM.getText().trim().equals("") ? "-" : TNoRM.getText());
        labelNamaPasien.setText(TPasien.getText().trim().equals("") ? "-" : TPasien.getText());
        labelDokter.setText(NmDokter.getText().trim().equals("") ? "-" : NmDokter.getText());
    }

    private void isRawat() {
         Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat='"+TNoRw.getText()+"' ",TNoRM);
    }

    private void isPsien() {
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='"+TNoRM.getText()+"' ",TPasien);
    }
    
    public void setNoRm(String norwt) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        isRawat();
        isPsien();
        loadDataPasienDariRegistrasi();
        ChkInput.setSelected(true);
        isForm();
    }

    public void setNoRm(String norwt, String norm, String namaPasien, String kdDokter, String namaDokter, Date tglAwal, Date tglAkhir) {
        idDipilih = "";
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TPasien.setText(namaPasien);
        KdDokter.setText(kdDokter);
        NmDokter.setText(namaDokter);
        TCari.setText(norwt);
        if(tglAwal != null){
            DTPCari1.setDate(tglAwal);
            TanggalAkhir.setDate(tglAwal);
            tglPemeriksaan = formatTanggalJam(tglAwal);
        }
        if(tglAkhir != null){
            DTPCari2.setDate(tglAkhir);
            if(tglPemeriksaan.equals("")){
                tglPemeriksaan = formatTanggalJam(tglAkhir);
            }
        }
        loadDataPasienDariRegistrasi();
        if(NoSurat.getText().trim().equals("")){
            nomorSurat();
        }
        ChkInput.setSelected(true);
        isForm();
        updateLabelRingkasan();
        
        // Cek apakah ada resume
        try (PreparedStatement ps = koneksi.prepareStatement("SELECT COUNT(*) FROM surat_visum_et_repertum WHERE no_rawat = ?")) {
        ps.setString(1, norwt);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                ChkInput.setSelected(rs.getInt(1) == 0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,this.getHeight()-135));
            ScrollInput.setVisible(true);
            PanelKananInput.setVisible(true);
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            ScrollInput.setVisible(false);
            PanelKananInput.setVisible(false);
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
        PanelInput.revalidate();
        PanelInput.repaint();
    }
       
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getsurat_keterangan_rawat_inap());
        BtnHapus.setEnabled(akses.getsurat_keterangan_rawat_inap());
        BtnEdit.setEnabled(akses.getsurat_keterangan_rawat_inap());
    }
    
    private void nomorSurat() {
        try {
            bln_angka = TanggalAkhir.getSelectedItem().toString().substring(3, 5);
            switch (bln_angka) {
                case "01": bln_romawi = "I"; break;
                case "02": bln_romawi = "II"; break;
                case "03": bln_romawi = "III"; break;
                case "04": bln_romawi = "IV"; break;
                case "05": bln_romawi = "V"; break;
                case "06": bln_romawi = "VI"; break;
                case "07": bln_romawi = "VII"; break;
                case "08": bln_romawi = "VIII"; break;
                case "09": bln_romawi = "IX"; break;
                case "10": bln_romawi = "X"; break;
                case "11": bln_romawi = "XI"; break;
                case "12": bln_romawi = "XII"; break;
                default: bln_romawi = ""; break;
            }
            String tahun = TanggalAkhir.getSelectedItem().toString().substring(6, 10);
            String lastNumber = Sequel.cariIsi(
                "SELECT IFNULL(MAX(CONVERT(LEFT(no_surat_ver, 3), SIGNED)),0) FROM "+TABEL_VISUM+" WHERE " +
                "no_surat_ver LIKE '%/VER/RSAJ/" + bln_romawi + "/" + tahun + "'"
            );
            int nextNumber = 1;
            if (lastNumber != null && !lastNumber.trim().equals("")) {
                nextNumber = Integer.parseInt(lastNumber) + 1;
            }
            nomorSuratGenerated = String.format("%03d", nextNumber) + "/VER/RSAJ/" + bln_romawi + "/" + tahun;
            NoSurat.setText(nomorSuratGenerated);
        } catch (Exception e) {
            nomorSuratGenerated = "";
        }
    }

}



