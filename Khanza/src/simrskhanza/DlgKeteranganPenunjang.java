/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile 
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami 
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */

package simrskhanza;

// ============================================================================
// VERSI REVISI UI CLINICAL v7 RALAN - 25 SEPTEMBER 2026
// Ciri versi ini:
// 1. Header bagian atas sudah dihilangkan.
// 2. Kotak/kartu rekap sudah dihilangkan.
// 3. Tab Riwayat Pelayanan langsung berada setelah data pasien.
// 4. Tombol Tutup berada di bagian paling bawah.
// 5. Identitas pasien compact model label (No. Rawat, Nama Pasien, No. RM,
//    dan Tanggal Masuk) tanpa Tarif/hari.
// 6. Navigasi kategori menonjolkan tab aktif; tab nonaktif netral hitam/abu.
// 7. Rincian Lab/Resep memakai bullet bulat renderer tanpa karakter Unicode.
// 8. Status pelayanan menggunakan badge kapsul dan icon status.
// 9. Tampilan disamakan dengan DlgKeteranganPenunjangRanap V7; sumber data khusus rawat jalan.
// ============================================================================

import fungsi.WarnaTable;
import fungsi.WarnaTable2;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.event.KeyEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import inventory.DlgCariKonversi;
import inventory.DlgCariObat;
import inventory.DlgCariObat2;
import inventory.DlgCariObat3;
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author perpustakaan
 */
public class DlgKeteranganPenunjang extends javax.swing.JDialog {
    // Warna utama tampilan clinical. Seluruh pengaturan visual dipusatkan di sini
    // agar mudah disesuaikan tanpa menyentuh query ataupun proses pengambilan data.
    private static final Color CLINICAL_BLUE = new Color(30, 103, 210);
    private static final Color CLINICAL_BLUE_SOFT = new Color(243, 248, 255);
    private static final Color CLINICAL_GREEN = new Color(46, 157, 85);
    private static final Color CLINICAL_TEXT = new Color(36, 48, 66);
    private static final Color CLINICAL_MUTED = new Color(103, 116, 137);
    private static final Color CLINICAL_BORDER = new Color(218, 226, 236);
    private static final Color CLINICAL_BACKGROUND = new Color(246, 249, 252);
    private static final Color CLINICAL_ROW_ALT = new Color(248, 250, 253);
    private static final String DETAIL_PREFIX_CLINICAL="__DETAIL_CLINICAL__";
    private static final Color CLINICAL_TAB_INACTIVE_TEXT=new Color(45,55,70);

    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final DefaultTableModel tabModeLab,tabModeRad,tabModeApotek,tabModeDokter,tabModePetugas,tabModeDokterPetugas;
    private int i;
    private String
            sqlpscarilab="select jns_perawatan_lab.nm_perawatan, count(periksa_lab.kd_jenis_prw) as jml,periksa_lab.biaya as biaya, "+
                    "sum(periksa_lab.biaya) as total,jns_perawatan_lab.kd_jenis_prw,sum(periksa_lab.tarif_perujuk+periksa_lab.tarif_tindakan_dokter) as totaldokter, "+
                    "sum(periksa_lab.tarif_tindakan_petugas) as totalpetugas,sum(periksa_lab.kso) as totalkso,sum(periksa_lab.bhp) as totalbhp "+
                    " from periksa_lab inner join jns_perawatan_lab on jns_perawatan_lab.kd_jenis_prw=periksa_lab.kd_jenis_prw where "+
                    " periksa_lab.no_rawat=? group by periksa_lab.kd_jenis_prw  ",
            sqlpscariobat="select databarang.nama_brng,jenis.nama,detail_pemberian_obat.biaya_obat,"+
                          "sum(detail_pemberian_obat.jml) as jml,sum(detail_pemberian_obat.embalase+detail_pemberian_obat.tuslah) as tambahan,"+
                          "(sum(detail_pemberian_obat.total)-sum(detail_pemberian_obat.embalase+detail_pemberian_obat.tuslah)) as total, "+
                          "sum((detail_pemberian_obat.h_beli*detail_pemberian_obat.jml)) as totalbeli "+
                          "from detail_pemberian_obat inner join databarang inner join jenis "+
                          "on detail_pemberian_obat.kode_brng=databarang.kode_brng and databarang.kdjns=jenis.kdjns where "+
                          "detail_pemberian_obat.no_rawat=? group by detail_pemberian_obat.kode_brng order by jenis.nama",
            sqlpsdetaillab="select sum(detail_periksa_lab.biaya_item) as total,sum(detail_periksa_lab.bagian_perujuk+detail_periksa_lab.bagian_dokter) as totaldokter, "+
                           "sum(detail_periksa_lab.bagian_laborat) as totalpetugas,sum(detail_periksa_lab.kso) as totalkso,sum(detail_periksa_lab.bhp) as totalbhp "+
                           "from detail_periksa_lab where detail_periksa_lab.no_rawat=? "+
                           "and detail_periksa_lab.kd_jenis_prw=?",
            sqlpscariradiologi="select jns_perawatan_radiologi.nm_perawatan, count(periksa_radiologi.kd_jenis_prw) as jml,periksa_radiologi.biaya as biaya, "+
                    "sum(periksa_radiologi.biaya) as total,jns_perawatan_radiologi.kd_jenis_prw,sum(periksa_radiologi.tarif_perujuk+periksa_radiologi.tarif_tindakan_dokter) as totaldokter, "+
                    "sum(periksa_radiologi.tarif_tindakan_petugas) as totalpetugas,sum(periksa_radiologi.kso) as totalkso,sum(periksa_radiologi.bhp) as totalbhp "+
                    " from periksa_radiologi inner join jns_perawatan_radiologi on jns_perawatan_radiologi.kd_jenis_prw=periksa_radiologi.kd_jenis_prw where "+
                    " periksa_radiologi.no_rawat=? group by periksa_radiologi.kd_jenis_prw  ";
    private PreparedStatement pscarilab,pscariobat,psobatlangsung,psdetaillab,pscariradiologi,pscaridokter,pscaripetugas,pscaridokterpetugas;
    private ResultSet rscarilab,rscariobat,rsdetaillab,rsobatlangsung,rscariradiologi,rscaridokter,rscaripetugas,rscaridokterpetugas;
    private WarnaTable2 warna=new WarnaTable2();
    private WarnaTable2 warna2=new WarnaTable2();

    // Komponen tambahan khusus tampilan. Komponen tabel dan field pasien lama
    // tetap digunakan sehingga model data serta fungsi yang sudah ada tidak berubah.
    private CardLayout layoutRiwayatClinical;
    private JPanel panelTabClinical;
    private JPanel panelRiwayatClinical;
    private JTextField TCariClinical;
    private JLabel LNoRwClinical;
    private JLabel LNoRMClinical;
    private JLabel LPasienClinical;
    private JLabel LTanggalMasukClinical;
    private JLabel lblJumlahDataClinical;
    private JTable[] tabelClinical;
    private JButton[] tombolTabClinical;
    private int kategoriAktifClinical=3;
    private final String[] namaKategoriClinical={
        "Tindakan Dokter","Tindakan Petugas","Dokter & Petugas",
        "Laboratorium","Radiologi","Resep"
    };
    private final String[] kunciKategoriClinical={
        "DOKTER","PETUGAS","DOKTER_PETUGAS","LAB","RADIOLOGI","RESEP"
    };
    private final Color[] warnaTabClinical={
        new Color(31,103,210),new Color(22,150,145),new Color(82,92,180),
        new Color(31,132,190),new Color(111,79,178),new Color(43,151,82)
    };

    private static final String IKON_NO_RAWAT_CLINICAL="/picture/no_rawat.png";
    private static final String IKON_NAMA_PASIEN_CLINICAL="/picture/nama_pasien.png";
    private static final String IKON_NO_RM_CLINICAL="/picture/no_rm.png";
    private static final String IKON_TANGGAL_MASUK_CLINICAL="/picture/tanggal_masuk.png";
    private static final String[] IKON_TAB_CLINICAL={
        "/picture/tab_tindakan_dokter.png",
        "/picture/tab_tindakan_petugas.png",
        "/picture/tab_dokter_petugas.png",
        "/picture/tab_laboratorium.png",
        "/picture/tab_radiologi.png",
        "/picture/tab_resep.png"
    };
    private final List<ClinicalMasterDetailRow> dataLabClinical=new ArrayList<>();
    private final List<ClinicalMasterDetailRow> dataApotekClinical=new ArrayList<>();
    private final List<ClinicalDisplayRowMeta> tampilanLabClinical=new ArrayList<>();
    private final List<ClinicalDisplayRowMeta> tampilanApotekClinical=new ArrayList<>();

    /** Creates new form DlgPemberianObat
     * @param parent
     * @param modal */
    public DlgKeteranganPenunjang(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        tabModeDokter=new DefaultTableModel(null,new Object[]{
            "Tanggal",
            "Jam",
            "Nama Pemeriksaan",
            "Total",
            "Biaya dr.",
            "Dokter"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokter.setModel(tabModeDokter);

        tbDokter.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 6; i++) {
            TableColumn column = tbDokter.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(85);
            }else if(i==3){
                column.setPreferredWidth(85);    
            }else if(i==4){
                column.setPreferredWidth(200);
            }else if(i==5){
                column.setPreferredWidth(200);
            }
        }
        tbDokter.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModePetugas=new DefaultTableModel(null,new Object[]{
            "Tanggal","Jam","Nama Pemeriksaan","Petugas"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbPetugas.setModel(tabModePetugas);

        tbPetugas.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbPetugas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 4; i++) {
            TableColumn column = tbPetugas.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(200);
            }else if(i==3){
                column.setPreferredWidth(200);
            }
        }
        tbPetugas.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDokterPetugas=new DefaultTableModel(null,new Object[]{
            "Tanggal","Jam","Nama Pemeriksaan","Dokter","Petugas"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokterPetugas.setModel(tabModeDokterPetugas);

        tbDokterPetugas.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokterPetugas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 5; i++) {
            TableColumn column = tbDokterPetugas.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(200);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(200);
            }
        }
        tbDokterPetugas.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeLab=new DefaultTableModel(null,new Object[]{
            "No.Permintaan","Tanggal","Jam","Nama Pemeriksaan","Dokter Perujuk","Status"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbLab.setModel(tabModeLab);

        tbLab.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbLab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 6; i++) {
            TableColumn column = tbLab.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(55);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(200);
            }else if(i==5){
                column.setPreferredWidth(100);
            }
        }
        tbLab.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeRad=new DefaultTableModel(null,new Object[]{
            "No.Permintaan","Tanggal","Jam","Nama Pemeriksaan","Dokter Perujuk","Status"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbRadiologi.setModel(tabModeRad);

        tbRadiologi.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbRadiologi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 6; i++) {
            TableColumn column = tbRadiologi.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(55);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(200);
            }else if(i==5){
                column.setPreferredWidth(100);
            }
        }
        tbRadiologi.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeApotek=new DefaultTableModel(null,new Object[]{
            "No.Resep","Tanggal","Jam","Dokter Peresep","Status"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbApotek.setModel(tabModeApotek);

        tbApotek.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbApotek.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 5; i++) {
            TableColumn column = tbApotek.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(55);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(100);
            }
        }
        tbApotek.setDefaultRenderer(Object.class, new WarnaTable());

        inisialisasiTampilanClinical();
        Dimension layar=Toolkit.getDefaultToolkit().getScreenSize();
        setSize(
            Math.max(1000,Math.min(1380,layar.width-20)),
            Math.max(650,Math.min(740,layar.height-40))
        );
        setLocationRelativeTo(parent);
        
        //TCatatan.setText(TCatatan); 
//        TCatatan.setLineWrap(true);
//        TCatatan.setWrapStyleWord(true);
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
        FormInput = new widget.PanelBiasa();
        panelPermintaan = new widget.panelisi();
        scrollPane8 = new widget.ScrollPane();
        tbDokter = new widget.Table();
        scrollPane5 = new widget.ScrollPane();
        tbLab = new widget.Table();
        scrollPane9 = new widget.ScrollPane();
        tbPetugas = new widget.Table();
        scrollPane6 = new widget.ScrollPane();
        tbRadiologi = new widget.Table();
        scrollPane10 = new widget.ScrollPane();
        tbDokterPetugas = new widget.Table();
        scrollPane7 = new widget.ScrollPane();
        tbApotek = new widget.Table();
        jLabel3 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        jLabel4 = new widget.Label();
        SEP = new widget.TextBox();
        panelGlass8 = new widget.panelisi();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(218, 226, 236))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1380, 740));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(1360, 640));
        FormInput.setLayout(null);

        panelPermintaan.setBorder(null);
        panelPermintaan.setName("panelPermintaan"); // NOI18N
        panelPermintaan.setPreferredSize(new java.awt.Dimension(100, 137));
        panelPermintaan.setLayout(new java.awt.GridLayout(3, 0));

        scrollPane8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Tindakan Dokter : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane8.setName("scrollPane8"); // NOI18N

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
        tbDokter.setToolTipText("");
        tbDokter.setName("tbDokter"); // NOI18N
        scrollPane8.setViewportView(tbDokter);

        panelPermintaan.add(scrollPane8);

        scrollPane5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Permintaan Laboratorium : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane5.setName("scrollPane5"); // NOI18N

        tbLab.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbLab.setToolTipText("");
        tbLab.setName("tbLab"); // NOI18N
        scrollPane5.setViewportView(tbLab);

        panelPermintaan.add(scrollPane5);

        scrollPane9.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Tindakan Petugas : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane9.setName("scrollPane9"); // NOI18N

        tbPetugas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbPetugas.setToolTipText("");
        tbPetugas.setName("tbPetugas"); // NOI18N
        scrollPane9.setViewportView(tbPetugas);

        panelPermintaan.add(scrollPane9);

        scrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Permintaan Radiologi : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane6.setName("scrollPane6"); // NOI18N

        tbRadiologi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbRadiologi.setToolTipText("");
        tbRadiologi.setName("tbRadiologi"); // NOI18N
        scrollPane6.setViewportView(tbRadiologi);

        panelPermintaan.add(scrollPane6);

        scrollPane10.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Tindakan Dokter & Petugas : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane10.setName("scrollPane10"); // NOI18N

        tbDokterPetugas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbDokterPetugas.setToolTipText("");
        tbDokterPetugas.setName("tbDokterPetugas"); // NOI18N
        scrollPane10.setViewportView(tbDokterPetugas);

        panelPermintaan.add(scrollPane10);

        scrollPane7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Permintaan Resep : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane7.setName("scrollPane7"); // NOI18N

        tbApotek.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbApotek.setToolTipText("");
        tbApotek.setName("tbApotek"); // NOI18N
        scrollPane7.setViewportView(tbApotek);

        panelPermintaan.add(scrollPane7);

        FormInput.add(panelPermintaan);
        panelPermintaan.setBounds(14, 240, 1332, 390);

        jLabel3.setText("No.Rawat :");
        jLabel3.setName("jLabel3"); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(jLabel3);
        jLabel3.setBounds(30, 10, 70, 23);

        TNoRw.setEditable(false);
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.setPreferredSize(new java.awt.Dimension(150, 23));
        TNoRw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TNoRwActionPerformed(evt);
            }
        });
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(110, 10, 150, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.setPreferredSize(new java.awt.Dimension(100, 23));
        TNoRM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TNoRMActionPerformed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(260, 10, 100, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.setPreferredSize(new java.awt.Dimension(320, 23));
        TPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TPasienActionPerformed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(360, 10, 235, 23);

        jLabel4.setText("No.SEP :");
        jLabel4.setName("jLabel4"); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(jLabel4);
        jLabel4.setBounds(30, 40, 70, 23);

        SEP.setEditable(false);
        SEP.setHighlighter(null);
        SEP.setName("SEP"); // NOI18N
        SEP.setPreferredSize(new java.awt.Dimension(150, 23));
        SEP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SEPActionPerformed(evt);
            }
        });
        SEP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SEPKeyPressed(evt);
            }
        });
        FormInput.add(SEP);
        SEP.setBounds(110, 40, 150, 23);

        internalFrame1.add(FormInput, java.awt.BorderLayout.CENTER);
        FormInput.getAccessibleContext().setAccessibleName("");
        FormInput.getAccessibleContext().setAccessibleDescription("");

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(100, 56));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnKeluar.setMnemonic('T');
        BtnKeluar.setText("Tutup");
        BtnKeluar.setToolTipText("Alt+T");
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

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        
    }//GEN-LAST:event_formWindowActivated

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            isRawat();
        }
    }//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TPasienActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TPasienActionPerformed

    private void TNoRwActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TNoRwActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TNoRwActionPerformed

    private void TNoRMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TNoRMActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TNoRMActionPerformed

    private void SEPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SEPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SEPActionPerformed

    private void SEPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SEPKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_SEPKeyPressed

    /**
     * Menyusun ulang komponen lama menjadi tampilan clinical review.
     * Seluruh tabel, model, field pasien, serta event tombol lama tetap dipakai.
     */
    private void inisialisasiTampilanClinical(){
        getContentPane().setBackground(CLINICAL_BACKGROUND);

        internalFrame1.removeAll();
        internalFrame1.setLayout(new BorderLayout());
        internalFrame1.setBorder(BorderFactory.createLineBorder(CLINICAL_BORDER));
        internalFrame1.setBackground(CLINICAL_BACKGROUND);

        FormInput.removeAll();
        FormInput.setLayout(new BorderLayout());
        FormInput.setBorder(BorderFactory.createEmptyBorder(12,14,10,14));
        FormInput.setBackground(CLINICAL_BACKGROUND);
        FormInput.setOpaque(true);

        JPanel panelIsi=new JPanel();
        panelIsi.setOpaque(false);
        panelIsi.setLayout(new BoxLayout(panelIsi,BoxLayout.Y_AXIS));

        JPanel kartuPasien=buatKartuPasienClinical();
        JPanel riwayat=buatPanelRiwayatClinical();

        kartuPasien.setAlignmentX(Component.LEFT_ALIGNMENT);
        riwayat.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartuPasien.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));
        riwayat.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));

        panelIsi.add(kartuPasien);
        panelIsi.add(Box.createVerticalStrut(5));
        panelIsi.add(riwayat);

        FormInput.add(panelIsi,BorderLayout.CENTER);
        internalFrame1.add(FormInput,BorderLayout.CENTER);
        internalFrame1.add(buatFooterClinical(),BorderLayout.PAGE_END);

        aturTabelClinical();
        pasangPencarianClinical();
        tampilkanKategoriClinical(kategoriAktifClinical);

        internalFrame1.revalidate();
        internalFrame1.repaint();
    }

    private JPanel buatFooterClinical(){
        JPanel panelFooter=new JPanel(new FlowLayout(FlowLayout.LEFT,14,10));
        panelFooter.setBackground(Color.WHITE);
        panelFooter.setBorder(BorderFactory.createMatteBorder(1,0,0,0,CLINICAL_BORDER));
        panelFooter.setPreferredSize(new Dimension(100,56));

        BtnKeluar.setText(" Tutup");
        BtnKeluar.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
        BtnKeluar.setForeground(CLINICAL_TEXT);
        BtnKeluar.setBackground(Color.WHITE);
        BtnKeluar.setFocusPainted(false);
        BtnKeluar.setContentAreaFilled(true);
        BtnKeluar.setOpaque(true);
        BtnKeluar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLINICAL_BORDER),
            BorderFactory.createEmptyBorder(5,10,5,10)
        ));
        BtnKeluar.setPreferredSize(new Dimension(100,36));

        panelFooter.add(BtnKeluar);
        return panelFooter;
    }

    private JPanel buatKartuPasienClinical(){
        JPanel kartu=new JPanel(new BorderLayout());
        kartu.setOpaque(false);
        kartu.setBorder(BorderFactory.createEmptyBorder(0,2,0,2));
        kartu.setPreferredSize(new Dimension(100,48));

        LNoRwClinical=buatLabelNilaiIdentitasClinical(TNoRw.getText());
        LPasienClinical=buatLabelNilaiIdentitasClinical(TPasien.getText());
        LNoRMClinical=buatLabelNilaiIdentitasClinical(TNoRM.getText());
        LTanggalMasukClinical=buatLabelNilaiIdentitasClinical("-");

        JPanel detail=new JPanel();
        detail.setOpaque(false);
        detail.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        detail.add(buatGrupIdentitasClinical("No. Rawat",LNoRwClinical,
                IKON_NO_RAWAT_CLINICAL,new ClinicalIdentityItemIcon(0,new Color(33,112,214))));
        detail.add(buatSeparatorIdentitasClinical());
        detail.add(buatGrupIdentitasClinical("Nama Pasien",LPasienClinical,
                IKON_NAMA_PASIEN_CLINICAL,new ClinicalIdentityItemIcon(1,new Color(33,112,214))));
        detail.add(buatSeparatorIdentitasClinical());
        detail.add(buatGrupIdentitasClinical("No. RM",LNoRMClinical,
                IKON_NO_RM_CLINICAL,new ClinicalIdentityItemIcon(2,new Color(33,112,214))));
        detail.add(buatSeparatorIdentitasClinical());
        detail.add(buatGrupIdentitasClinical("Tanggal Masuk",LTanggalMasukClinical,
                IKON_TANGGAL_MASUK_CLINICAL,new ClinicalIdentityItemIcon(3,new Color(33,112,214))));

        kartu.add(detail,BorderLayout.CENTER);
        sinkronkanIdentitasClinical();
        return kartu;
    }

    private JLabel buatLabelNilaiIdentitasClinical(String nilai){
        JLabel label=new JLabel(nilai==null || nilai.trim().isEmpty()?"-":nilai);
        label.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
        label.setForeground(CLINICAL_TEXT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel buatGrupIdentitasClinical(String judul,JLabel nilai,String pathIkon,Icon fallback){
        JPanel panel=new JPanel(new BorderLayout(7,0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));

        JLabel ikon=new JLabel(muatIkonResourceClinical(pathIkon,20,20,fallback));
        ikon.setHorizontalAlignment(SwingConstants.CENTER);
        ikon.setVerticalAlignment(SwingConstants.CENTER);
        ikon.setPreferredSize(new Dimension(20,20));

        JPanel teks=new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks,BoxLayout.Y_AXIS));

        JLabel labelJudul=new JLabel(judul);
        labelJudul.setFont(new Font("Segoe UI",Font.PLAIN,10));
        labelJudul.setForeground(CLINICAL_MUTED);
        labelJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        nilai.setAlignmentX(Component.LEFT_ALIGNMENT);

        teks.add(labelJudul);
        teks.add(Box.createVerticalStrut(1));
        teks.add(nilai);

        panel.add(ikon,BorderLayout.LINE_START);
        panel.add(teks,BorderLayout.CENTER);
        return panel;
    }

    private Component buatSeparatorIdentitasClinical(){
        JPanel wadah=new JPanel(new GridBagLayout());
        wadah.setOpaque(false);
        wadah.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        JPanel garis=new JPanel();
        garis.setBackground(CLINICAL_BORDER);
        garis.setPreferredSize(new Dimension(1,15));
        garis.setMinimumSize(new Dimension(1,15));
        garis.setMaximumSize(new Dimension(1,15));
        wadah.add(garis);
        return wadah;
    }

    private Icon muatIkonResourceClinical(String pathIkon,int lebar,int tinggi,Icon fallback){
        try{
            java.net.URL lokasi=DlgKeteranganPenunjang.class.getResource(pathIkon);
            if(lokasi!=null){
                ImageIcon ikonAsli=new ImageIcon(lokasi);
                Image gambar=ikonAsli.getImage().getScaledInstance(lebar,tinggi,Image.SCALE_SMOOTH);
                return new ImageIcon(gambar);
            }
        }catch(Exception e){
            System.out.println("Notif Muat Icon Clinical : "+e);
        }
        return fallback;
    }

    private void aturUkuranKomponenPreferred(JComponent komponen){
        Dimension ukuran=komponen.getPreferredSize();
        komponen.setMinimumSize(ukuran);
        komponen.setMaximumSize(ukuran);
    }

    private void sinkronkanIdentitasClinical(){
        if(LNoRwClinical!=null){
            LNoRwClinical.setText(nilaiIdentitasClinical(TNoRw.getText()));
        }
        if(LNoRMClinical!=null){
            LNoRMClinical.setText(nilaiIdentitasClinical(TNoRM.getText()));
        }
        if(LPasienClinical!=null){
            LPasienClinical.setText(nilaiIdentitasClinical(TPasien.getText()));
        }
        if(panelGlass8!=null){
            panelGlass8.revalidate();
            panelGlass8.repaint();
        }
    }

    private String nilaiIdentitasClinical(String nilai){
        return nilai==null || nilai.trim().isEmpty()?"-":nilai.trim();
    }

    private JPanel buatPanelRiwayatClinical(){
        panelRiwayatClinical=new ClinicalRoundedPanel(new BorderLayout(),16);
        panelRiwayatClinical.setOpaque(false);
        panelRiwayatClinical.setPreferredSize(new Dimension(100,390));

        JPanel panelAtas=new JPanel();
        panelAtas.setOpaque(false);
        panelAtas.setLayout(new BoxLayout(panelAtas,BoxLayout.Y_AXIS));
        panelAtas.setBorder(BorderFactory.createEmptyBorder(12,16,10,16));

        JPanel barisJudul=new JPanel(new BorderLayout(16,0));
        barisJudul.setOpaque(false);
        JLabel judul=new JLabel("Riwayat Pelayanan");
        judul.setFont(new Font("Segoe UI Semibold",Font.BOLD,16));
        judul.setForeground(CLINICAL_TEXT);

        JPanel panelCari=new JPanel(new BorderLayout(8,0));
        panelCari.setOpaque(false);
        JLabel labelCari=new JLabel("Cari");
        labelCari.setFont(new Font("Segoe UI",Font.PLAIN,12));
        labelCari.setForeground(CLINICAL_MUTED);
        TCariClinical=new ClinicalRoundedTextField(18);
        TCariClinical.setFont(new Font("Segoe UI",Font.PLAIN,12));
        TCariClinical.setForeground(CLINICAL_TEXT);
        TCariClinical.setToolTipText("Cari pemeriksaan, dokter, petugas, nomor permintaan atau status");
        TCariClinical.setPreferredSize(new Dimension(285,34));
        panelCari.add(labelCari,BorderLayout.LINE_START);
        panelCari.add(TCariClinical,BorderLayout.CENTER);

        barisJudul.add(judul,BorderLayout.LINE_START);
        barisJudul.add(panelCari,BorderLayout.LINE_END);

        panelTabClinical=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        panelTabClinical.setBackground(Color.WHITE);
        tombolTabClinical=new JButton[namaKategoriClinical.length];
        for(int indeks=0;indeks<namaKategoriClinical.length;indeks++){
            tombolTabClinical[indeks]=buatTombolTabClinical(indeks);
            panelTabClinical.add(tombolTabClinical[indeks]);
        }

        panelAtas.add(barisJudul);
        panelAtas.add(Box.createVerticalStrut(8));
        panelAtas.add(panelTabClinical);

        layoutRiwayatClinical=new CardLayout();
        panelPermintaan.removeAll();
        panelPermintaan.setLayout(layoutRiwayatClinical);
        panelPermintaan.setBackground(Color.WHITE);
        panelPermintaan.setBorder(BorderFactory.createMatteBorder(1,0,0,0,CLINICAL_BORDER));
        panelPermintaan.add(scrollPane8,kunciKategoriClinical[0]);
        panelPermintaan.add(scrollPane9,kunciKategoriClinical[1]);
        panelPermintaan.add(scrollPane10,kunciKategoriClinical[2]);
        panelPermintaan.add(scrollPane5,kunciKategoriClinical[3]);
        panelPermintaan.add(scrollPane6,kunciKategoriClinical[4]);
        panelPermintaan.add(scrollPane7,kunciKategoriClinical[5]);

        JPanel panelBawah=new JPanel(new BorderLayout());
        panelBawah.setOpaque(false);
        panelBawah.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,CLINICAL_BORDER),
            BorderFactory.createEmptyBorder(8,16,8,16)
        ));
        lblJumlahDataClinical=new JLabel("Menampilkan 0 data");
        lblJumlahDataClinical.setFont(new Font("Segoe UI",Font.PLAIN,12));
        lblJumlahDataClinical.setForeground(CLINICAL_MUTED);
        panelBawah.add(lblJumlahDataClinical,BorderLayout.LINE_START);

        panelRiwayatClinical.add(panelAtas,BorderLayout.PAGE_START);
        panelRiwayatClinical.add(panelPermintaan,BorderLayout.CENTER);
        panelRiwayatClinical.add(panelBawah,BorderLayout.PAGE_END);
        return panelRiwayatClinical;
    }

    private JButton buatTombolTabClinical(final int indeks){
        Icon ikonTab=muatIkonTabClinical(indeks);
        ClinicalTabButton tombol=new ClinicalTabButton(
            namaKategoriClinical[indeks],indeks,warnaTabClinical[indeks],ikonTab
        );
        tombol.addActionListener((java.awt.event.ActionEvent evt) -> {
            tampilkanKategoriClinical(indeks);
        });
        return tombol;
    }

    /**
     * Memuat icon tab langsung dari folder /picture/. Cara pertama sengaja sama
     * dengan icon bawaan Khanza (contoh /picture/cross.png). Dua fallback lain
     * disiapkan agar icon tetap terbaca saat dijalankan dari NetBeans/project.
     */
    private Icon muatIkonTabClinical(int indeks){
        if(indeks<0 || indeks>=IKON_TAB_CLINICAL.length){
            return null;
        }
        String path=IKON_TAB_CLINICAL[indeks];
        try{
            java.net.URL lokasi=getClass().getResource(path);
            if(lokasi==null){
                ClassLoader loader=Thread.currentThread().getContextClassLoader();
                if(loader!=null){
                    lokasi=loader.getResource(path.startsWith("/")?path.substring(1):path);
                }
            }
            if(lokasi!=null){
                return ukuranIkonTabClinical(new ImageIcon(lokasi));
            }

            // Fallback saat resource belum tersalin ke build/classes oleh NetBeans.
            String relatif=path.startsWith("/")?path.substring(1):path;
            File[] kandidat={
                new File("src",relatif),
                new File(relatif),
                new File(".",relatif)
            };
            for(File file:kandidat){
                if(file.isFile()){
                    return ukuranIkonTabClinical(new ImageIcon(file.getAbsolutePath()));
                }
            }
            System.out.println("Icon tab tidak ditemukan : "+path+
                    " | pastikan file berada di src/picture dan ikut masuk build/classes/picture");
        }catch(Exception e){
            System.out.println("Notif Muat Icon Tab Clinical : "+e);
        }
        return null;
    }

    private Icon ukuranIkonTabClinical(ImageIcon ikonAsli){
        if(ikonAsli==null || ikonAsli.getIconWidth()<=0 || ikonAsli.getIconHeight()<=0){
            return null;
        }
        if(ikonAsli.getIconWidth()==16 && ikonAsli.getIconHeight()==16){
            return ikonAsli;
        }
        Image gambar=ikonAsli.getImage().getScaledInstance(16,16,Image.SCALE_SMOOTH);
        return new ImageIcon(gambar);
    }

    private void aturTabelClinical(){
        tabelClinical=new JTable[]{tbDokter,tbPetugas,tbDokterPetugas,tbLab,tbRadiologi,tbApotek};
        javax.swing.JScrollPane[] scrollClinical={scrollPane8,scrollPane9,scrollPane10,scrollPane5,scrollPane6,scrollPane7};

        for(int indeks=0;indeks<tabelClinical.length;indeks++){
            JTable tabel=tabelClinical[indeks];
            tabel.setFont(new Font("Segoe UI",Font.PLAIN,12));
            tabel.setForeground(CLINICAL_TEXT);
            tabel.setBackground(Color.WHITE);
            tabel.setSelectionBackground(new Color(220,235,255));
            tabel.setSelectionForeground(CLINICAL_TEXT);
            tabel.setRowHeight(31);
            tabel.setShowHorizontalLines(true);
            tabel.setShowVerticalLines(false);
            tabel.setGridColor(new Color(231,236,243));
            tabel.setIntercellSpacing(new Dimension(0,1));
            tabel.setFillsViewportHeight(true);
            tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            tabel.setDefaultRenderer(Object.class,new ClinicalCellRenderer());
            tabel.getTableHeader().setReorderingAllowed(false);
            tabel.getTableHeader().setResizingAllowed(true);
            tabel.getTableHeader().setOpaque(true);
            tabel.getTableHeader().setBackground(new Color(245,248,252));
            tabel.getTableHeader().setForeground(CLINICAL_TEXT);
            tabel.getTableHeader().setFont(new Font("Segoe UI Semibold",Font.BOLD,12));
            tabel.getTableHeader().setPreferredSize(new Dimension(100,35));
            tabel.setRowSorter(new TableRowSorter<TableModel>(tabel.getModel()));

            scrollClinical[indeks].setBorder(BorderFactory.createEmptyBorder());
            scrollClinical[indeks].getViewport().setBackground(Color.WHITE);
        }

        tbLab.getColumnModel().getColumn(5).setCellRenderer(new ClinicalStatusRenderer());
        tbRadiologi.getColumnModel().getColumn(5).setCellRenderer(new ClinicalStatusRenderer());
        tbApotek.getColumnModel().getColumn(4).setCellRenderer(new ClinicalStatusRenderer());

        DefaultTableCellRenderer angka=new ClinicalCellRenderer();
        angka.setHorizontalAlignment(SwingConstants.RIGHT);
        tbDokter.getColumnModel().getColumn(3).setCellRenderer(angka);
        tbDokter.getColumnModel().getColumn(4).setCellRenderer(angka);

        siapkanInteraksiDetailClinical();
        tbLab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tbApotek.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        aturLebarSemuaKolomClinical();
    }

    private void siapkanInteraksiDetailClinical(){
        tbLab.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                toggleRincianLabClinical(tbLab.rowAtPoint(e.getPoint()));
            }
        });
        tbApotek.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                toggleRincianApotekClinical(tbApotek.rowAtPoint(e.getPoint()));
            }
        });
    }

    private void toggleRincianLabClinical(int rowView){
        if(rowView<0 || tampilanLabClinical.isEmpty()){
            return;
        }
        int rowModel=tbLab.convertRowIndexToModel(rowView);
        if(rowModel<0 || rowModel>=tampilanLabClinical.size()){
            return;
        }
        ClinicalDisplayRowMeta meta=tampilanLabClinical.get(rowModel);
        if(meta.detailRow || meta.parentIndex<0 || meta.parentIndex>=dataLabClinical.size()){
            return;
        }
        ClinicalMasterDetailRow baris=dataLabClinical.get(meta.parentIndex);
        if(baris.details.isEmpty()){
            return;
        }
        baris.expanded=!baris.expanded;
        renderLabRowsClinical();
        if(rowView<tbLab.getRowCount()){
            tbLab.setRowSelectionInterval(rowView,rowView);
        }
    }

    private void toggleRincianApotekClinical(int rowView){
        if(rowView<0 || tampilanApotekClinical.isEmpty()){
            return;
        }
        int rowModel=tbApotek.convertRowIndexToModel(rowView);
        if(rowModel<0 || rowModel>=tampilanApotekClinical.size()){
            return;
        }
        ClinicalDisplayRowMeta meta=tampilanApotekClinical.get(rowModel);
        if(meta.detailRow || meta.parentIndex<0 || meta.parentIndex>=dataApotekClinical.size()){
            return;
        }
        ClinicalMasterDetailRow baris=dataApotekClinical.get(meta.parentIndex);
        if(baris.details.isEmpty()){
            return;
        }
        baris.expanded=!baris.expanded;
        renderApotekRowsClinical();
    }

    private void bangunDataLabClinicalDariModel(){
        dataLabClinical.clear();
        for(int row=0;row<tabModeLab.getRowCount();row++){
            String[] parent=ambilDataBarisModel(tabModeLab,row,6);
            normalisasiBarisLabClinical(parent);
            List<String> details=ambilRincianLabClinical(parent[0],parent[3]);
            dataLabClinical.add(new ClinicalMasterDetailRow(parent,details));
        }
    }

    private void bangunDataApotekClinicalDariModel(){
        dataApotekClinical.clear();
        for(int row=0;row<tabModeApotek.getRowCount();row++){
            String[] parent=ambilDataBarisModel(tabModeApotek,row,5);
            List<String> details=ambilRincianApotekClinical(parent[0]);
            dataApotekClinical.add(new ClinicalMasterDetailRow(parent,details));
        }
    }

    private String[] ambilDataBarisModel(DefaultTableModel model,int row,int jumlahKolom){
        String[] data=new String[jumlahKolom];
        for(int kolom=0;kolom<jumlahKolom;kolom++){
            Object nilai=model.getValueAt(row,kolom);
            data[kolom]=nilai==null?"":nilai.toString();
        }
        return data;
    }

    private void normalisasiBarisLabClinical(String[] parent){
        if(parent.length<6){
            return;
        }
        if((parent[5]==null || parent[5].trim().isEmpty()) &&
                ("Sudah Terlayani".equalsIgnoreCase(parent[4]) || "Belum Terlayani".equalsIgnoreCase(parent[4]))){
            parent[5]=parent[4];
            parent[4]=parent[3];
            parent[3]="Pemeriksaan Laboratorium";
        }
        for(int i=0;i<parent.length;i++){
            if(parent[i]==null){
                parent[i]="";
            }
        }
    }

    private List<String> ambilRincianLabClinical(String noOrder,String namaDefault){
        LinkedHashSet<String> hasil=new LinkedHashSet<>();

        // Sub rincian yang benar dibaca dari HASIL pemeriksaan (detail_periksa_lab),
        // bukan dari tabel permintaan. noorder dipetakan ke tgl_hasil/jam_hasil
        // pada permintaan_lab lalu dicocokkan dengan tgl_periksa/jam pada detail_periksa_lab.
        String sqlDetail="select trim(template_laboratorium.Pemeriksaan) as pemeriksaan "
                +"from permintaan_lab "
                +"inner join detail_periksa_lab on detail_periksa_lab.no_rawat=permintaan_lab.no_rawat "
                +"and detail_periksa_lab.tgl_periksa=permintaan_lab.tgl_hasil "
                +"and detail_periksa_lab.jam=permintaan_lab.jam_hasil "
                +"inner join template_laboratorium on template_laboratorium.id_template=detail_periksa_lab.id_template "
                +"where permintaan_lab.noorder=? "
                +"order by detail_periksa_lab.kd_jenis_prw,template_laboratorium.urut";
        try(PreparedStatement psDetail=koneksi.prepareStatement(sqlDetail)){
            psDetail.setString(1,noOrder);
            try(ResultSet rsDetail=psDetail.executeQuery()){
                while(rsDetail.next()){
                    String rincian=rsDetail.getString("pemeriksaan");
                    if(rincian!=null && !rincian.trim().isEmpty()){
                        hasil.add(rincian.trim());
                    }
                }
            }
        }catch(Exception e){
            System.out.println("Notif Rincian Lab : "+e);
        }
        return new ArrayList<>(hasil);
    }

    private List<String> ambilRincianApotekClinical(String noResep){
        LinkedHashSet<String> hasil=new LinkedHashSet<>();
        String[] calonSql={
            "select concat(databarang.nama_brng,'  x',resep_dokter.jml,'  ' ,ifnull(resep_dokter.aturan_pakai,'')) as rincian "+
            "from resep_dokter inner join databarang on resep_dokter.kode_brng=databarang.kode_brng where resep_dokter.no_resep=? order by databarang.nama_brng",
            "select concat(databarang.nama_brng,'  x',detail_pemberian_obat.jml,'  ' ,ifnull(detail_pemberian_obat.aturan_pakai,'')) as rincian "+
            "from detail_pemberian_obat inner join databarang on detail_pemberian_obat.kode_brng=databarang.kode_brng where detail_pemberian_obat.no_resep=? order by databarang.nama_brng"
        };
        for(String sqlDetail:calonSql){
            try(PreparedStatement psDetail=koneksi.prepareStatement(sqlDetail)){
                psDetail.setString(1,noResep);
                try(ResultSet rsDetail=psDetail.executeQuery()){
                    while(rsDetail.next()){
                        String rincian=rsDetail.getString(1);
                        if(rincian!=null && !rincian.trim().isEmpty()){
                            hasil.add(rincian.trim());
                        }
                    }
                }
                if(!hasil.isEmpty()){
                    break;
                }
            }catch(Exception e){
                System.out.println("Notif Rincian Resep : "+e);
            }
        }
        return new ArrayList<>(hasil);
    }

    private void renderLabRowsClinical(){
        tampilanLabClinical.clear();
        Valid.tabelKosong(tabModeLab);
        for(int indeks=0;indeks<dataLabClinical.size();indeks++){
            ClinicalMasterDetailRow master=dataLabClinical.get(indeks);
            String[] parent=master.parentData.clone();
            // Nomor permintaan ditampilkan apa adanya tanpa simbol expand Unicode.
            // Expand/collapse tetap dilakukan dengan klik pada baris induk.
            parent[0]=bersihkanNomorClinical(parent[0]);
            tabModeLab.addRow(parent);
            tampilanLabClinical.add(new ClinicalDisplayRowMeta(indeks,false));
            if(master.expanded){
                for(String detail:master.details){
                    // Prefix internal ASCII tidak ditampilkan. Renderer menggantinya
                    // dengan bullet bulat yang digambar langsung sehingga aman dari
                    // masalah encoding/font pada komputer user.
                    tabModeLab.addRow(new String[]{"","","",DETAIL_PREFIX_CLINICAL+detail,"",""});
                    tampilanLabClinical.add(new ClinicalDisplayRowMeta(indeks,true));
                }
            }
        }
        aturLebarKolomOtomatisClinical(tbLab);
        terapkanPencarianClinical();
    }

    private void renderApotekRowsClinical(){
        tampilanApotekClinical.clear();
        Valid.tabelKosong(tabModeApotek);
        for(int indeks=0;indeks<dataApotekClinical.size();indeks++){
            ClinicalMasterDetailRow master=dataApotekClinical.get(indeks);
            String[] parent=master.parentData.clone();
            parent[0]=bersihkanNomorClinical(parent[0]);
            tabModeApotek.addRow(parent);
            tampilanApotekClinical.add(new ClinicalDisplayRowMeta(indeks,false));
            if(master.expanded){
                for(String detail:master.details){
                    tabModeApotek.addRow(new String[]{"","","",DETAIL_PREFIX_CLINICAL+detail,""});
                    tampilanApotekClinical.add(new ClinicalDisplayRowMeta(indeks,true));
                }
            }
        }
        aturLebarKolomOtomatisClinical(tbApotek);
        terapkanPencarianClinical();
    }

    private String bersihkanNomorClinical(String nomor){
        String hasil=nomor==null?"":nomor.trim();
        // Membersihkan sisa marker dari versi UI sebelumnya jika model lama
        // sempat dirender dengan karakter expand yang tidak didukung encoding.
        while(!hasil.isEmpty() && !Character.isLetterOrDigit(hasil.charAt(0))){
            hasil=hasil.substring(1).trim();
        }
        return hasil;
    }

    private void aturLebarSemuaKolomClinical(){
        if(tabelClinical==null){
            return;
        }
        for(JTable tabel:tabelClinical){
            aturLebarKolomOtomatisClinical(tabel);
        }
    }

    private void aturLebarKolomOtomatisClinical(JTable tabel){
        // AUTO_RESIZE_OFF + min/preferred/actual width yang sama memastikan
        // Swing tidak mengecilkan lagi kolom hingga teks terpotong. Bila total
        // lebar melebihi viewport, scroll horizontal yang akan bekerja.
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel kolomModel=tabel.getColumnModel();

        for(int kolom=0;kolom<tabel.getColumnCount();kolom++){
            int lebar=55;

            // Ukur header berdasarkan renderer yang benar-benar dipakai Swing.
            TableCellRenderer rendererHeader=tabel.getTableHeader().getDefaultRenderer();
            Component komponenHeader=rendererHeader.getTableCellRendererComponent(
                    tabel,tabel.getColumnName(kolom),false,false,-1,kolom);
            lebar=Math.max(lebar,komponenHeader.getPreferredSize().width+20);

            // Ukur seluruh data berdasarkan renderer, bukan perkiraan FontMetrics.
            // Dengan cara ini nama pemeriksaan/dokter panjang tidak lagi terpotong.
            for(int row=0;row<tabel.getRowCount();row++){
                TableCellRenderer renderer=tabel.getCellRenderer(row,kolom);
                Component komponen=tabel.prepareRenderer(renderer,row,kolom);
                lebar=Math.max(lebar,komponen.getPreferredSize().width+18);
            }

            if((tabel==tbLab || tabel==tbRadiologi) && kolom==5){
                lebar=Math.max(lebar,150);
            }else if(tabel==tbApotek && kolom==4){
                lebar=Math.max(lebar,150);
            }

            TableColumn kolomTabel=kolomModel.getColumn(kolom);
            kolomTabel.setMinWidth(lebar);
            kolomTabel.setPreferredWidth(lebar);
            kolomTabel.setWidth(lebar);
        }

        tabel.setPreferredScrollableViewportSize(new Dimension(
                Math.max(tabel.getPreferredSize().width,tabel.getColumnModel().getTotalColumnWidth()),
                tabel.getPreferredScrollableViewportSize().height
        ));
        tabel.revalidate();
        tabel.repaint();
    }

    private void pasangPencarianClinical(){
        TCariClinical.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e){
                terapkanPencarianClinical();
            }

            @Override
            public void removeUpdate(DocumentEvent e){
                terapkanPencarianClinical();
            }

            @Override
            public void changedUpdate(DocumentEvent e){
                terapkanPencarianClinical();
            }
        });
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private void terapkanPencarianClinical(){
        String kata=TCariClinical.getText().trim();
        RowFilter filter=null;
        if(!kata.isEmpty()){
            filter=RowFilter.regexFilter("(?i)"+Pattern.quote(kata));
        }
        for(JTable tabel:tabelClinical){
            if(tabel.getRowSorter() instanceof TableRowSorter){
                ((TableRowSorter)tabel.getRowSorter()).setRowFilter(filter);
            }
        }
        perbaruiJumlahDataClinical();
    }

    private void tampilkanKategoriClinical(int indeks){
        if(indeks<0 || indeks>=kunciKategoriClinical.length || layoutRiwayatClinical==null){
            return;
        }
        kategoriAktifClinical=indeks;
        layoutRiwayatClinical.show(panelPermintaan,kunciKategoriClinical[indeks]);
        perbaruiGayaNavigasiClinical();
        perbaruiJumlahDataClinical();
    }

    private void perbaruiGayaNavigasiClinical(){
        if(tombolTabClinical!=null){
            for(int indeks=0;indeks<tombolTabClinical.length;indeks++){
                boolean aktif=indeks==kategoriAktifClinical;
                if(tombolTabClinical[indeks] instanceof ClinicalTabButton){
                    ((ClinicalTabButton)tombolTabClinical[indeks]).setAktif(aktif);
                }else{
                    tombolTabClinical[indeks].setForeground(aktif?CLINICAL_BLUE:CLINICAL_MUTED);
                }
            }
        }
    }

    private void perbaruiJumlahDataClinical(){
        if(lblJumlahDataClinical==null || tabelClinical==null || kategoriAktifClinical>=tabelClinical.length){
            return;
        }
        JTable tabel=tabelClinical[kategoriAktifClinical];
        int seluruh=tabel.getModel().getRowCount();
        int tampil=tabel.getRowCount();
        if(TCariClinical!=null && !TCariClinical.getText().trim().isEmpty()){
            lblJumlahDataClinical.setText("Menampilkan "+tampil+" dari "+seluruh+" data");
        }else{
            lblJumlahDataClinical.setText("Menampilkan "+seluruh+" data");
        }
    }

    private static class ClinicalRoundedTextField extends JTextField{
        private final int arc;

        ClinicalRoundedTextField(int arc){
            this.arc=arc;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        }

        @Override
        protected void paintComponent(Graphics grafik){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc);
            g2.dispose();
            super.paintComponent(grafik);
        }

        @Override
        protected void paintBorder(Graphics grafik){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hasFocus()?new Color(135,181,241):CLINICAL_BORDER);
            g2.setStroke(new BasicStroke(hasFocus()?1.2f:1f));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc);
            g2.dispose();
        }
    }

    private static class ClinicalRoundedPanel extends JPanel{
        private final int arc;

        ClinicalRoundedPanel(java.awt.LayoutManager layout,int arc){
            super(layout);
            this.arc=arc;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        }

        @Override
        protected void paintComponent(Graphics grafik){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc);
            g2.setColor(CLINICAL_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc);
            g2.dispose();
            super.paintComponent(grafik);
        }
    }

    /**
     * Tombol kategori berbentuk pill/card agar navigasi riwayat lebih hidup.
     * Warna tiap kategori tetap konsisten dengan icon dan tidak memengaruhi data.
     */
    /**
     * Tombol kategori berbentuk pill/card agar navigasi riwayat lebih hidup.
     * Icon diprioritaskan dari file /picture/*.png, fallback ke icon vektor.
     */
    private static class ClinicalTabButton extends JButton{
        private final int kategori;
        private final Color warna;
        private final Icon ikonResource;
        private boolean aktif=false;
        private boolean hover=false;

        ClinicalTabButton(String teks,int kategori,Color warna,Icon ikonResource){
            super(teks);
            this.kategori=kategori;
            this.warna=warna;
            this.ikonResource=ikonResource;
            setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));
            setIconTextGap(7);
            setHorizontalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setVerticalTextPosition(SwingConstants.CENTER);
            setFocusPainted(false);
            setFocusable(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAktif(false);
            addMouseListener(new MouseAdapter(){
                @Override
                public void mouseEntered(MouseEvent e){
                    hover=true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e){
                    hover=false;
                    repaint();
                }
            });
        }

        void setAktif(boolean aktif){
            this.aktif=aktif;
            // Tab aktif dibuat solid sesuai warna kategorinya agar langsung terbaca.
            // Icon dan teks putih; tab nonaktif tetap netral gelap.
            Color warnaTampil=aktif?Color.WHITE:CLINICAL_TAB_INACTIVE_TEXT;
            setForeground(warnaTampil);
            setIcon(buatIkonState(warnaTampil));
            repaint();
        }

        /**
         * Icon sumber selalu berasal dari file PNG /picture/. Warna hanya ditint
         * untuk membedakan tab aktif dan nonaktif. Jika file tidak ditemukan,
         * barulah fallback vektor lama dipakai agar tombol tidak pernah kosong.
         */
        private Icon buatIkonState(Color warnaTampil){
            if(ikonResource instanceof ImageIcon){
                ImageIcon sumber=(ImageIcon)ikonResource;
                int w=Math.max(1,sumber.getIconWidth());
                int h=Math.max(1,sumber.getIconHeight());
                BufferedImage hasil=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2=hasil.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(sumber.getImage(),0,0,16,16,null);
                g2.dispose();

                for(int y=0;y<16;y++){
                    for(int x=0;x<16;x++){
                        int argb=hasil.getRGB(x,y);
                        int alpha=(argb>>>24)&0xff;
                        if(alpha>0){
                            hasil.setRGB(x,y,(alpha<<24)|(warnaTampil.getRGB()&0x00ffffff));
                        }
                    }
                }
                return new ImageIcon(hasil);
            }
            if(ikonResource!=null){
                return ikonResource;
            }
            return new ClinicalTabIcon(kategori,warnaTampil);
        }

        @Override
        protected void paintComponent(Graphics grafik){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int arc=12;

            Color latar;
            Color garis;
            if(aktif){
                // Active state: background penuh warna kategori, border sedikit lebih gelap.
                latar=warna;
                garis=warna.darker();
            }else if(hover){
                // Nonaktif tetap netral; hover hanya memberi sedikit feedback visual.
                latar=new Color(248,250,252);
                garis=new Color(168,178,191);
            }else{
                latar=Color.WHITE;
                garis=CLINICAL_BORDER;
            }

            g2.setColor(latar);
            g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc);
            g2.setColor(garis);
            g2.setStroke(new BasicStroke(aktif?1.4f:1f));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,arc,arc);
            g2.dispose();
            super.paintComponent(grafik);
        }

        private static Color campurWarna(Color warna1,Color warna2,float rasioWarna2){
            float r=Math.max(0f,Math.min(1f,rasioWarna2));
            int merah=Math.round(warna1.getRed()*(1f-r)+warna2.getRed()*r);
            int hijau=Math.round(warna1.getGreen()*(1f-r)+warna2.getGreen()*r);
            int biru=Math.round(warna1.getBlue()*(1f-r)+warna2.getBlue()*r);
            return new Color(merah,hijau,biru);
        }
    }

    /** Icon ringkas untuk tiap informasi identitas pasien. */
    private static class ClinicalIdentityItemIcon implements Icon{
        private final int jenis;
        private final Color warna;

        ClinicalIdentityItemIcon(int jenis,Color warna){
            this.jenis=jenis;
            this.warna=warna;
        }

        @Override
        public int getIconWidth(){
            return 22;
        }

        @Override
        public int getIconHeight(){
            return 22;
        }

        @Override
        public void paintIcon(Component komponen,Graphics grafik,int x,int y){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(warna);
            g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            switch(jenis){
                case 0: // No. Rawat - bed
                    g2.drawLine(x+2,y+13,x+20,y+13);
                    g2.drawLine(x+3,y+8,x+3,y+17);
                    g2.drawLine(x+19,y+10,x+19,y+17);
                    g2.drawRoundRect(x+5,y+9,13,4,2,2);
                    g2.drawOval(x+5,y+6,4,3);
                    g2.drawLine(x+3,y+16,x+3,y+20);
                    g2.drawLine(x+19,y+16,x+19,y+20);
                    break;
                case 1: // Nama Pasien
                    g2.drawOval(x+6,y+2,8,8);
                    g2.drawArc(x+3,y+11,14,9,0,180);
                    g2.drawOval(x+15,y+13,6,6);
                    g2.drawLine(x+18,y+14,x+18,y+18);
                    g2.drawLine(x+16,y+16,x+20,y+16);
                    break;
                case 2: // No. RM - kartu rekam medis
                    g2.drawRoundRect(x+2,y+3,18,16,3,3);
                    g2.drawRect(x+5,y+6,5,5);
                    g2.drawLine(x+7,y+7,x+7,y+10);
                    g2.drawLine(x+6,y+8,x+9,y+8);
                    g2.drawLine(x+12,y+7,x+18,y+7);
                    g2.drawLine(x+12,y+10,x+18,y+10);
                    g2.drawLine(x+5,y+14,x+18,y+14);
                    break;
                default: // Tanggal Masuk - kalender
                    g2.drawRoundRect(x+3,y+5,16,14,3,3);
                    g2.drawLine(x+3,y+9,x+19,y+9);
                    g2.drawLine(x+7,y+2,x+7,y+7);
                    g2.drawLine(x+15,y+2,x+15,y+7);
                    g2.fillOval(x+7,y+12,2,2);
                    g2.fillOval(x+12,y+12,2,2);
                    break;
            }
            g2.dispose();
        }
    }

    /** Icon salin kecil seperti referensi kartu identitas. */
    private static class ClinicalCopyIcon implements Icon{
        @Override
        public int getIconWidth(){
            return 15;
        }

        @Override
        public int getIconHeight(){
            return 15;
        }

        @Override
        public void paintIcon(Component komponen,Graphics grafik,int x,int y){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(70,80,94));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x+1,y+1,9,10,2,2);
            g2.drawRoundRect(x+5,y+4,9,10,2,2);
            g2.dispose();
        }
    }

    /** Icon outline pasien agar kartu identitas tidak bergantung pada file gambar eksternal. */
    private static class PatientIdentityIcon implements Icon{
        @Override
        public int getIconWidth(){
            return 20;
        }

        @Override
        public int getIconHeight(){
            return 20;
        }

        @Override
        public void paintIcon(Component komponen,Graphics grafik,int x,int y){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(25,35,49));
            g2.setStroke(new BasicStroke(1.4f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2.drawOval(x+7,y+1,6,6);
            g2.drawArc(x+3,y+9,14,10,0,180);
            g2.drawLine(x+3,y+14,x+3,y+18);
            g2.drawLine(x+17,y+14,x+17,y+18);
            g2.dispose();
        }
    }

    /** Enam icon kecil dibentuk langsung dengan Graphics2D sesuai kategori tab. */
    private static class ClinicalTabIcon implements Icon{
        private final int kategori;
        private final Color warna;

        ClinicalTabIcon(int kategori,Color warna){
            this.kategori=kategori;
            this.warna=warna;
        }

        @Override
        public int getIconWidth(){
            return 18;
        }

        @Override
        public int getIconHeight(){
            return 18;
        }

        @Override
        public void paintIcon(Component komponen,Graphics grafik,int x,int y){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(warna);
            g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            switch(kategori){
                case 0: // tindakan dokter - stetoskop
                    g2.drawLine(x+3,y+2,x+3,y+7);
                    g2.drawLine(x+8,y+2,x+8,y+7);
                    g2.drawArc(x+3,y+4,5,7,180,180);
                    g2.drawArc(x+5,y+9,8,6,180,-220);
                    g2.drawOval(x+12,y+8,3,3);
                    break;
                case 1: // tindakan petugas - satu petugas
                    g2.drawOval(x+6,y+2,6,6);
                    g2.drawArc(x+3,y+9,12,8,0,180);
                    g2.drawLine(x+3,y+13,x+3,y+16);
                    g2.drawLine(x+15,y+13,x+15,y+16);
                    break;
                case 2: // dokter dan petugas - dua orang
                    g2.drawOval(x+2,y+3,5,5);
                    g2.drawOval(x+11,y+3,5,5);
                    g2.drawArc(x,y+9,9,7,0,180);
                    g2.drawArc(x+9,y+9,9,7,0,180);
                    break;
                case 3: // laboratorium - labu pemeriksaan
                    g2.drawLine(x+6,y+2,x+12,y+2);
                    g2.drawLine(x+8,y+2,x+8,y+7);
                    g2.drawLine(x+10,y+2,x+10,y+7);
                    g2.drawPolyline(new int[]{x+8,x+3,x+3,x+15,x+15,x+10},
                                    new int[]{y+7,y+14,y+16,y+16,y+14,y+7},6);
                    g2.drawLine(x+5,y+12,x+13,y+12);
                    break;
                case 4: // radiologi - lembar hasil rontgen
                    g2.drawRoundRect(x+2,y+1,14,16,3,3);
                    g2.drawLine(x+9,y+4,x+9,y+14);
                    g2.drawArc(x+4,y+5,5,8,80,200);
                    g2.drawArc(x+9,y+5,5,8,-100,200);
                    break;
                default: // resep - kapsul obat
                    g2.rotate(-Math.PI/4,x+9,y+9);
                    g2.drawRoundRect(x+4,y+2,10,14,9,9);
                    g2.drawLine(x+4,y+9,x+14,y+9);
                    break;
            }
            g2.dispose();
        }
    }

    private static class ClinicalCellRenderer extends DefaultTableCellRenderer{
        private final Icon bulletIcon=new ClinicalBulletIcon();

        ClinicalCellRenderer(){
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
            String teks=value==null?"":value.toString();
            boolean rincian=teks.startsWith(DETAIL_PREFIX_CLINICAL);
            String teksTampil=rincian?teks.substring(DETAIL_PREFIX_CLINICAL.length()):teks;
            super.getTableCellRendererComponent(table,teksTampil,isSelected,hasFocus,row,column);
            setFont(new Font("Segoe UI",Font.PLAIN,rincian?11:12));
            setIcon(rincian?bulletIcon:null);
            setIconTextGap(rincian?8:4);

            if(isSelected){
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }else{
                setBackground(rincian?new Color(250,252,255):(row%2==0?Color.WHITE:CLINICAL_ROW_ALT));
                setForeground(rincian?CLINICAL_MUTED:CLINICAL_TEXT);
            }
            setBorder(BorderFactory.createEmptyBorder(0,rincian?18:10,0,10));
            return this;
        }
    }

    /** Bullet bulat digambar langsung, tidak memakai karakter Unicode. */
    private static class ClinicalBulletIcon implements Icon{
        @Override
        public int getIconWidth(){
            return 7;
        }

        @Override
        public int getIconHeight(){
            return 7;
        }

        @Override
        public void paintIcon(Component c,Graphics g,int x,int y){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(92,112,140));
            g2.fillOval(x+1,y+1,5,5);
            g2.dispose();
        }
    }

    private static class ClinicalStatusRenderer extends JPanel implements TableCellRenderer{
        private final ClinicalStatusBadge badge=new ClinicalStatusBadge();

        ClinicalStatusRenderer(){
            setLayout(new FlowLayout(FlowLayout.LEFT,8,4));
            setOpaque(true);
            add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
            String status=value==null?"":value.toString();
            boolean selesai=status.toLowerCase().contains("sudah");
            setBackground(isSelected?table.getSelectionBackground():(row%2==0?Color.WHITE:CLINICAL_ROW_ALT));
            badge.setVisible(status!=null && !status.trim().isEmpty());
            if(badge.isVisible()){
                badge.setStatus(status,selesai);
            }
            return this;
        }
    }

    private static class ClinicalMasterDetailRow{
        private final String[] parentData;
        private final List<String> details;
        private boolean expanded=false;

        ClinicalMasterDetailRow(String[] parentData,List<String> details){
            this.parentData=parentData;
            this.details=details==null?new ArrayList<String>():details;
        }
    }

    private static class ClinicalDisplayRowMeta{
        private final int parentIndex;
        private final boolean detailRow;

        ClinicalDisplayRowMeta(int parentIndex,boolean detailRow){
            this.parentIndex=parentIndex;
            this.detailRow=detailRow;
        }
    }

    /** Badge status berbentuk kapsul dengan icon centang atau jam. */
    private static class ClinicalStatusBadge extends JLabel{
        private Color warnaLatar;
        private Color warnaGaris;

        ClinicalStatusBadge(){
            setOpaque(false);
            setFont(new Font("Segoe UI Semibold",Font.PLAIN,11));
            setIconTextGap(5);
            setBorder(BorderFactory.createEmptyBorder(3,9,3,9));
        }

        void setStatus(String status,boolean selesai){
            Color warnaStatus=selesai?new Color(31,126,67):new Color(176,102,19);
            setText(status);
            setForeground(warnaStatus);
            warnaLatar=selesai?new Color(232,247,238):new Color(255,245,226);
            warnaGaris=selesai?new Color(183,225,199):new Color(239,210,158);
            setIcon(new ClinicalStatusIcon(selesai,warnaStatus));
        }

        @Override
        protected void paintComponent(Graphics grafik){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int lengkung=Math.max(1,getHeight()-1);
            g2.setColor(warnaLatar==null?Color.WHITE:warnaLatar);
            g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,lengkung,lengkung);
            g2.setColor(warnaGaris==null?CLINICAL_BORDER:warnaGaris);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,lengkung,lengkung);
            g2.dispose();
            super.paintComponent(grafik);
        }
    }

    private static class ClinicalStatusIcon implements Icon{
        private final boolean selesai;
        private final Color warna;

        ClinicalStatusIcon(boolean selesai,Color warna){
            this.selesai=selesai;
            this.warna=warna;
        }

        @Override
        public int getIconWidth(){
            return 13;
        }

        @Override
        public int getIconHeight(){
            return 13;
        }

        @Override
        public void paintIcon(Component komponen,Graphics grafik,int x,int y){
            Graphics2D g2=(Graphics2D)grafik.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.4f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            if(selesai){
                g2.setColor(warna);
                g2.fillOval(x,y,12,12);
                g2.setColor(Color.WHITE);
                g2.drawLine(x+3,y+6,x+5,y+8);
                g2.drawLine(x+5,y+8,x+9,y+4);
            }else{
                g2.setColor(warna);
                g2.drawOval(x,y,12,12);
                g2.drawLine(x+6,y+3,x+6,y+6);
                g2.drawLine(x+6,y+6,x+9,y+7);
            }
            g2.dispose();
        }
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgKeteranganPenunjang dialog = new DlgKeteranganPenunjang(new javax.swing.JFrame(), true);
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
    private widget.Button BtnKeluar;
    private widget.PanelBiasa FormInput;
    public widget.TextBox SEP;
    private widget.TextBox TNoRM;
    public widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel3;
    private widget.Label jLabel4;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelPermintaan;
    private widget.ScrollPane scrollPane10;
    private widget.ScrollPane scrollPane5;
    private widget.ScrollPane scrollPane6;
    private widget.ScrollPane scrollPane7;
    private widget.ScrollPane scrollPane8;
    private widget.ScrollPane scrollPane9;
    private widget.Table tbApotek;
    private widget.Table tbDokter;
    private widget.Table tbDokterPetugas;
    private widget.Table tbLab;
    private widget.Table tbPetugas;
    private widget.Table tbRadiologi;
    // End of variables declaration//GEN-END:variables
    

    private void isRawat() {
         Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=? ",TNoRM,TNoRw.getText());
         sinkronkanIdentitasClinical();
    }

    private void isPsien() {
        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis=? ",TPasien,TNoRM.getText());
        sinkronkanIdentitasClinical();
    }

    public void setNoRm(String norwt) {
        TNoRw.setText(norwt);
        Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=? ",TNoRM,TNoRw.getText());
        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis=? ",TPasien,TNoRM.getText());
        Sequel.cariIsi("select no_sep from bridging_sep where no_rawat=? ",SEP,TNoRw.getText());
        sinkronkanIdentitasClinical();
        isiIdentitasRawatClinical();
        keterangan();
//        isPsien();   
//        Sequel.cariIsi("select catatan from catatan_pasien where no_rkm_medis=?",TCatatan,TNoRM.getText());       
    }

    /**
     * Melengkapi informasi tanggal masuk pada kartu pasien. Query hanya membaca
     * data registrasi rawat jalan dan tidak mengubah proses pengambilan riwayat pelayanan yang lama.
     */
    private void isiIdentitasRawatClinical(){
        if(LTanggalMasukClinical==null){
            return;
        }
        LTanggalMasukClinical.setText("-");
        String sqlIdentitas="select date_format(reg_periksa.tgl_registrasi,'%d-%m-%Y') as tanggal_masuk,"+
                "reg_periksa.jam_reg as jam_masuk from reg_periksa where reg_periksa.no_rawat=? limit 1";
        try(PreparedStatement psIdentitas=koneksi.prepareStatement(sqlIdentitas)){
            psIdentitas.setString(1,TNoRw.getText());
            try(ResultSet rsIdentitas=psIdentitas.executeQuery()){
                if(rsIdentitas.next()){
                    String tanggal=rsIdentitas.getString("tanggal_masuk");
                    String jam=rsIdentitas.getString("jam_masuk");
                    if(jam!=null && jam.length()>5){
                        jam=jam.substring(0,5);
                    }
                    LTanggalMasukClinical.setText((tanggal==null?"-":tanggal)+
                            (jam==null || jam.trim().isEmpty()?"":" "+jam));
                }
            }
        }catch(Exception e){
            System.out.println("Notif Identitas Ralan : "+e);
        }
    }

    private void keterangan(){
            try {
                    Valid.tabelKosong(tabModeDokter);
                    pscaridokter=koneksi.prepareStatement(""
                            + "select rawat_jl_dr.tgl_perawatan,"
                            + "rawat_jl_dr.jam_rawat,"
                            + "jns_perawatan.nm_perawatan,"
                            + "rawat_jl_dr.tarif_tindakandr,"
                            + "rawat_jl_dr.biaya_rawat,"
                            + "dokter.nm_dokter from rawat_jl_dr "+
                        "inner join jns_perawatan on rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "+
                        "inner join dokter on rawat_jl_dr.kd_dokter=dokter.kd_dokter "+
                        "where rawat_jl_dr.no_rawat=? order by rawat_jl_dr.tgl_perawatan desc,rawat_jl_dr.jam_rawat desc ");
                    try {
                        pscaridokter.setString(1,TNoRw.getText());
                        rscaridokter=pscaridokter.executeQuery();
                        while(rscaridokter.next()){
                            tabModeDokter.addRow(new String[]{
                                rscaridokter.getString("tgl_perawatan"),
                                rscaridokter.getString("jam_rawat"),
                                rscaridokter.getString("nm_perawatan"),
                                rscaridokter.getString("tarif_tindakandr"),
                                rscaridokter.getString("biaya_rawat"),
                                rscaridokter.getString("nm_dokter")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(pscaridokter!=null){
                            rscaridokter.close();
                        }
                        if(pscaridokter!=null){
                            pscaridokter.close();
                        }
                    }
                    
                    Valid.tabelKosong(tabModePetugas);
                    pscaripetugas=koneksi.prepareStatement("select rawat_jl_pr.tgl_perawatan,rawat_jl_pr.jam_rawat,jns_perawatan.nm_perawatan,petugas.nama from rawat_jl_pr "+
                        "inner join jns_perawatan on rawat_jl_pr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "+
                        "inner join petugas on rawat_jl_pr.nip=petugas.nip "+
                        "where rawat_jl_pr.no_rawat=? order by rawat_jl_pr.tgl_perawatan desc,rawat_jl_pr.jam_rawat desc ");
                    try {
                        pscaripetugas.setString(1,TNoRw.getText());
                        rscaripetugas=pscaripetugas.executeQuery();
                        while(rscaripetugas.next()){
                            tabModePetugas.addRow(new String[]{
                                rscaripetugas.getString("tgl_perawatan"),rscaripetugas.getString("jam_rawat"),rscaripetugas.getString("nm_perawatan"),rscaripetugas.getString("nama")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(pscaripetugas!=null){
                            rscaripetugas.close();
                        }
                        if(pscaripetugas!=null){
                            pscaripetugas.close();
                        }
                    }
                    
                    Valid.tabelKosong(tabModeDokterPetugas);
                    pscaridokterpetugas=koneksi.prepareStatement("select rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,jns_perawatan.nm_perawatan,dokter.nm_dokter,petugas.nama from rawat_jl_drpr "+
                        "inner join jns_perawatan on rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "+
                        "inner join dokter on rawat_jl_drpr.kd_dokter=dokter.kd_dokter "+
                        "inner join petugas on rawat_jl_drpr.nip=petugas.nip "+
                        "where rawat_jl_drpr.no_rawat=? order by rawat_jl_drpr.tgl_perawatan desc,rawat_jl_drpr.jam_rawat desc ");
                    try {
                        pscaridokterpetugas.setString(1,TNoRw.getText());
                        rscaridokterpetugas=pscaridokterpetugas.executeQuery();
                        while(rscaridokterpetugas.next()){
                            tabModeDokterPetugas.addRow(new String[]{
                                rscaridokterpetugas.getString("tgl_perawatan"),rscaridokterpetugas.getString("jam_rawat"),rscaridokterpetugas.getString("nm_perawatan"),rscaridokterpetugas.getString("nm_dokter"),rscaridokterpetugas.getString("nama")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(pscaridokterpetugas!=null){
                            rscaridokterpetugas.close();
                        }
                        if(pscaridokterpetugas!=null){
                            pscaridokterpetugas.close();
                        }
                    }
                
                    Valid.tabelKosong(tabModeLab);
                    pscarilab=koneksi.prepareStatement("select permintaan_lab.noorder,permintaan_lab.tgl_permintaan,"+
                        "if(permintaan_lab.jam_permintaan='00:00:00','',permintaan_lab.jam_permintaan) as jam_permintaan,jns_perawatan_lab.nm_perawatan,"+
                        "if(permintaan_lab.tgl_hasil='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,"+
                        "dokter.nm_dokter from permintaan_lab inner join dokter on permintaan_lab.dokter_perujuk=dokter.kd_dokter "+
                        "inner join permintaan_detail_permintaan_lab on permintaan_lab.noorder=permintaan_detail_permintaan_lab.noorder "+
                        "inner join jns_perawatan_lab on permintaan_detail_permintaan_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "+
                        "where permintaan_lab.status='ralan' and permintaan_lab.no_rawat=? group by permintaan_lab.noorder order by permintaan_lab.tgl_permintaan,permintaan_lab.jam_permintaan desc");
                    try {
                        pscarilab.setString(1,TNoRw.getText());
                        rscarilab=pscarilab.executeQuery();
                        while(rscarilab.next()){
                            tabModeLab.addRow(new String[]{
                                bersihkanNomorClinical(rscarilab.getString("noorder")),rscarilab.getString("tgl_permintaan"),rscarilab.getString("jam_permintaan"),rscarilab.getString("nm_perawatan"),rscarilab.getString("nm_dokter"),rscarilab.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscarilab!=null){
                            rscarilab.close();
                        }
                        if(pscarilab!=null){
                            pscarilab.close();
                        }
                    }

                    pscarilab=koneksi.prepareStatement("select permintaan_labpa.noorder,permintaan_labpa.tgl_permintaan,"+
                        "if(permintaan_labpa.jam_permintaan='00:00:00','',permintaan_labpa.jam_permintaan) as jam_permintaan,"+
                        "if(permintaan_labpa.tgl_hasil='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,"+
                        "dokter.nm_dokter from permintaan_labpa inner join dokter on permintaan_labpa.dokter_perujuk=dokter.kd_dokter "+
                        "where permintaan_labpa.status='ralan' and permintaan_labpa.no_rawat=? order by permintaan_labpa.tgl_permintaan,permintaan_labpa.jam_permintaan desc");
                    try {
                        pscarilab.setString(1,TNoRw.getText());
                        rscarilab=pscarilab.executeQuery();
                        while(rscarilab.next()){
                            tabModeLab.addRow(new String[]{
                                bersihkanNomorClinical(rscarilab.getString("noorder")),rscarilab.getString("tgl_permintaan"),rscarilab.getString("jam_permintaan"),rscarilab.getString("nm_dokter"),rscarilab.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscarilab!=null){
                            rscarilab.close();
                        }
                        if(pscarilab!=null){
                            pscarilab.close();
                        }
                    }

                    pscarilab=koneksi.prepareStatement("select permintaan_labmb.noorder,permintaan_labmb.tgl_permintaan,"+
                        "if(permintaan_labmb.jam_permintaan='00:00:00','',permintaan_labmb.jam_permintaan) as jam_permintaan,"+
                        "if(permintaan_labmb.tgl_hasil='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,"+
                        "dokter.nm_dokter from permintaan_labmb inner join dokter on permintaan_labmb.dokter_perujuk=dokter.kd_dokter "+
                        "where permintaan_labmb.status='ralan' and permintaan_labmb.no_rawat=? order by permintaan_labmb.tgl_permintaan,permintaan_labmb.jam_permintaan desc");
                    try {
                        pscarilab.setString(1,TNoRw.getText());
                        rscarilab=pscarilab.executeQuery();
                        while(rscarilab.next()){
                            tabModeLab.addRow(new String[]{
                                bersihkanNomorClinical(rscarilab.getString("noorder")),rscarilab.getString("tgl_permintaan"),rscarilab.getString("jam_permintaan"),rscarilab.getString("nm_dokter"),rscarilab.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscarilab!=null){
                            rscarilab.close();
                        }
                        if(pscarilab!=null){
                            pscarilab.close();
                        }
                    }

                    // Setelah seluruh data laboratorium selesai dimuat, bangun master-detail.
                    // Ini wajib dilakukan sebelum interaksi klik agar metadata baris parent
                    // dan rincian dari detail_periksa_lab sudah tersedia.
                    bangunDataLabClinicalDariModel();
                    renderLabRowsClinical();

                    Valid.tabelKosong(tabModeRad);
                    pscariradiologi=koneksi.prepareStatement("select permintaan_radiologi.noorder,permintaan_radiologi.tgl_permintaan,"+
                        "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,jns_perawatan_radiologi.nm_perawatan,"+
                        "if(permintaan_radiologi.tgl_hasil='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,"+
                        "dokter.nm_dokter from permintaan_radiologi inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                        "inner join permintaan_pemeriksaan_radiologi on permintaan_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder "+
                        "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                        "where permintaan_radiologi.status='ralan' and permintaan_radiologi.no_rawat=? group by permintaan_radiologi.noorder order by permintaan_radiologi.tgl_permintaan,permintaan_radiologi.jam_permintaan desc");
                    try {
                        pscariradiologi.setString(1,TNoRw.getText());
                        rscariradiologi=pscariradiologi.executeQuery();
                        while(rscariradiologi.next()){
                            tabModeRad.addRow(new String[]{
                                bersihkanNomorClinical(rscariradiologi.getString("noorder")),rscariradiologi.getString("tgl_permintaan"),rscariradiologi.getString("jam_permintaan"),rscariradiologi.getString("nm_perawatan"),rscariradiologi.getString("nm_dokter"),rscariradiologi.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscariradiologi!=null){
                            rscariradiologi.close();
                        }
                        if(pscariradiologi!=null){
                            pscariradiologi.close();
                        }
                    }

                    Valid.tabelKosong(tabModeApotek);
                    psobatlangsung=koneksi.prepareStatement("select resep_obat.no_resep,resep_obat.tgl_peresepan,resep_obat.jam_peresepan,"+
                        " dokter.nm_dokter,if(resep_obat.tgl_perawatan='0000-00-00','Belum Terlayani','Sudah Terlayani') as status "+
                        " from resep_obat inner join dokter on resep_obat.kd_dokter=dokter.kd_dokter "+
                        " where resep_obat.tgl_peresepan<>'0000-00-00' and resep_obat.status='ralan' and resep_obat.no_rawat=? order by resep_obat.tgl_perawatan desc,resep_obat.jam desc");
                    try {
                        psobatlangsung.setString(1,TNoRw.getText());
                        rscariobat=psobatlangsung.executeQuery();
                        while(rscariobat.next()){
                            tabModeApotek.addRow(new String[]{
                                bersihkanNomorClinical(rscariobat.getString("no_resep")),rscariobat.getString("tgl_peresepan"),rscariobat.getString("jam_peresepan"),rscariobat.getString("nm_dokter"),rscariobat.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscariobat!=null){
                            rscariobat.close();
                        }
                        if(psobatlangsung!=null){
                            psobatlangsung.close();
                        }
                    }
                    bangunDataApotekClinicalDariModel();
                    renderApotekRowsClinical();
                    aturLebarKolomOtomatisClinical(tbDokter);
                    aturLebarKolomOtomatisClinical(tbPetugas);
                    aturLebarKolomOtomatisClinical(tbDokterPetugas);
                    aturLebarKolomOtomatisClinical(tbRadiologi);
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        aturLebarKolomOtomatisClinical(tbLab);
                        aturLebarKolomOtomatisClinical(tbApotek);
                    });
                    perbaruiJumlahDataClinical();
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
                }
            }
    }
    
    
//    public void isCe?k(){
//        BtnSimpan.setEnabled(true);
//        BtnHapus.setEnabled(true);
//        BtnEdit.setEnabled(true);
//    }



//}
