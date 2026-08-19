/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgPasienMati.java
 *
 * Created on Aug 30, 2010, 7:46:01 AM
 */

package bridging;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import kepegawaian.DlgCariDokter;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import simrskhanza.DlgPasien;
import digital_klaim.ViewerKoding;
import digital_klaim.DlgViewPdfEklaim;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author dosen3
 */
public class INACBGData extends javax.swing.JDialog {
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private DlgPasien pasien=new DlgPasien(null,false);
    private PreparedStatement ps,pskoders;
    private ResultSet rs,rskode,rs2,rs3,rsdiagnosa, rsprosedur;
    private DlgCariDokter dokter=new DlgCariDokter(null,true);
    private String tgl, finger="", lama="",payor_id="",payor_cd="",coder="",dokterkonsul="",tgl_masuk_ranap="",tgl_keluar_ranap="",dpjp_ranap="",inacbg_diagnosa="",prosedur_inacbg="";
    private String[] part;
    private String noRawat="";
    public  ApiINACBG inacbg = new ApiINACBG();
    private int total_biaya = 0,i=0,j=0;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter formatter_tanggal = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    

    
    /** Creates new form DlgPasienMati
     * @param parent
     * @param modal */
    public INACBGData(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
//        tampilkanDiagnosa();
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){  
                        dpjp.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                        dpjp.requestFocus();  
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

        dokter.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // Klik kiri (Button 1)
                    dokter.dispose();
                }
            }
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

        no_rawat = new widget.TextBox();
        icd10 = new widget.TextBox();
        icd9 = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.panelisi();
        tgl_masuk = new widget.Tanggal();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        jLabel12 = new widget.Label();
        pnb = new widget.TextBox();
        ahli = new widget.TextBox();
        no_kartu = new widget.TextBox();
        cara_masuk = new widget.ComboBox();
        sistole = new widget.TextBox();
        jenis_rawat = new widget.ComboBox();
        cob = new widget.ComboBox();
        jaminan = new widget.ComboBox();
        jLabel6 = new widget.Label();
        rehab = new widget.TextBox();
        rad = new widget.TextBox();
        alkes = new widget.TextBox();
        obat = new widget.TextBox();
        pb = new widget.TextBox();
        kpr = new widget.TextBox();
        akomodasi = new widget.TextBox();
        lab = new widget.TextBox();
        bmhp = new widget.TextBox();
        kronis = new widget.TextBox();
        ksl = new widget.TextBox();
        pnj = new widget.TextBox();
        ri = new widget.TextBox();
        pl_darah = new widget.TextBox();
        sewa_alat = new widget.TextBox();
        obat_kemo = new widget.TextBox();
        jLabel27 = new widget.Label();
        jLabel28 = new widget.Label();
        jLabel29 = new widget.Label();
        jLabel30 = new widget.Label();
        jLabel31 = new widget.Label();
        jLabel32 = new widget.Label();
        jLabel33 = new widget.Label();
        jLabel34 = new widget.Label();
        jLabel35 = new widget.Label();
        jLabel36 = new widget.Label();
        jLabel37 = new widget.Label();
        jLabel38 = new widget.Label();
        jLabel39 = new widget.Label();
        jLabel40 = new widget.Label();
        jLabel41 = new widget.Label();
        jLabel42 = new widget.Label();
        jam_masuk = new widget.ComboBox();
        menit_masuk = new widget.ComboBox();
        tgl_keluar = new widget.Tanggal();
        jam_keluar = new widget.ComboBox();
        menit_keluar = new widget.ComboBox();
        rawat_intensif = new javax.swing.JCheckBox();
        ventilator = new javax.swing.JCheckBox();
        naik_turun_kelas = new javax.swing.JCheckBox();
        lama_naik_kelas = new widget.TextBox();
        chronic = new widget.TextBox();
        sub_acute = new widget.TextBox();
        berat_bayi = new widget.TextBox();
        dpjp = new widget.TextBox();
        validasi_cvd19 = new widget.Button();
        co_covid_19 = new widget.TextBox();
        status_pulang = new widget.ComboBox();
        kelas_eksekutif = new javax.swing.JCheckBox();
        tgl_intubasi = new widget.Tanggal();
        jam_intubasi = new widget.ComboBox();
        menit_intubasi = new widget.ComboBox();
        total_rupiah = new widget.Label();
        menit_ekstubasi = new widget.ComboBox();
        jam_ekstubasi = new widget.ComboBox();
        tgl_ekstubasi = new widget.Tanggal();
        hari_intensif = new widget.TextBox();
        jLabel66 = new widget.Label();
        jLabel67 = new widget.Label();
        distole = new widget.TextBox();
        tarif_eksekutif = new widget.TextBox();
        jLabel13 = new widget.Label();
        pnb1 = new widget.TextBox();
        tgl_lahir = new widget.TextBox();
        jenis_kelamin = new widget.TextBox();
        jam_ventilator = new widget.TextBox();
        jLabel65 = new widget.Label();
        jLabel68 = new widget.Label();
        nm_diag_1 = new widget.TextBox();
        jLabel70 = new widget.Label();
        jLabel71 = new widget.Label();
        kd_diag_1 = new widget.TextBox();
        nm_diag_2 = new widget.TextBox();
        kd_diag_2 = new widget.TextBox();
        nm_diag_3 = new widget.TextBox();
        kd_diag_3 = new widget.TextBox();
        nm_diag_4 = new widget.TextBox();
        kd_diag_4 = new widget.TextBox();
        nm_diag_5 = new widget.TextBox();
        kd_diag_5 = new widget.TextBox();
        nm_psdr_1 = new widget.TextBox();
        kd_psdr_1 = new widget.TextBox();
        nm_psdr_2 = new widget.TextBox();
        kd_psdr_2 = new widget.TextBox();
        nm_psdr_3 = new widget.TextBox();
        kd_psdr_3 = new widget.TextBox();
        nm_psdr_4 = new widget.TextBox();
        kd_psdr_4 = new widget.TextBox();
        nm_psdr_5 = new widget.TextBox();
        kd_psdr_5 = new widget.TextBox();
        chk_diag_5 = new javax.swing.JCheckBox();
        chk_diag_1 = new javax.swing.JCheckBox();
        chk_diag_2 = new javax.swing.JCheckBox();
        chk_diag_3 = new javax.swing.JCheckBox();
        chk_diag_4 = new javax.swing.JCheckBox();
        jLabel72 = new widget.Label();
        jLabel73 = new widget.Label();
        total_rupiah1 = new widget.Label();
        jLabel77 = new widget.Label();
        ap_5 = new widget.TextBox();
        jLabel78 = new widget.Label();
        ap_1 = new widget.TextBox();
        ap_2 = new widget.TextBox();
        ap_3 = new widget.TextBox();
        ap_4 = new widget.TextBox();
        ap_10 = new widget.TextBox();
        ap_6 = new widget.TextBox();
        ap_7 = new widget.TextBox();
        ap_8 = new widget.TextBox();
        ap_9 = new widget.TextBox();
        jLabel49 = new widget.Label();
        Tsep = new widget.TextBox();
        jLabel50 = new widget.Label();
        total_grouping_tersimpan = new widget.Label();
        deskripsi_grouping_tersimpan = new widget.Label();
        code_cbg_grouping_tersimpan = new widget.Label();
        pasien_tb = new widget.TextBox();
        validasi_tb = new widget.Button();
        batal_validasi_tb = new widget.Button();
        check_hd = new javax.swing.JCheckBox();
        jLabel56 = new widget.Label();
        pasien_hd = new widget.ComboBox();
        jLabel79 = new widget.Label();
        kt_darah = new widget.TextBox();
        hak_kelas = new widget.ComboBox();
        naik_kelas_pelayanan = new widget.ComboBox();
        stage_2 = new widget.ComboBox();
        btnDPJP = new widget.Button();
        jLabel61 = new widget.Label();
        jLabel76 = new widget.Label();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new widget.Label();
        jLabel8 = new widget.Label();
        jLabel9 = new widget.Label();
        jLabel10 = new widget.Label();
        jLabel11 = new widget.Label();
        jLabel14 = new widget.Label();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel16 = new widget.Label();
        jLabel17 = new widget.Label();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel18 = new widget.Label();
        jLabel19 = new widget.Label();
        jLabel20 = new widget.Label();
        jLabel21 = new widget.Label();
        jLabel23 = new widget.Label();
        jLabel24 = new widget.Label();
        jLabel25 = new widget.Label();
        jLabel26 = new widget.Label();
        jLabel43 = new widget.Label();
        jLabel58 = new widget.Label();
        jLabel22 = new widget.Label();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel46 = new widget.Label();
        jLabel62 = new widget.Label();
        jLabel63 = new widget.Label();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel15 = new widget.Label();
        jLabel80 = new widget.Label();
        jSeparator10 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator12 = new javax.swing.JSeparator();
        jSeparator13 = new javax.swing.JSeparator();
        jSeparator14 = new javax.swing.JSeparator();
        jSeparator15 = new javax.swing.JSeparator();
        jSeparator16 = new javax.swing.JSeparator();
        jSeparator17 = new javax.swing.JSeparator();
        jSeparator18 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        ResponEklaim = new javax.swing.JTextPane();
        jLabel69 = new widget.Label();
        jLabel81 = new widget.Label();
        jSeparator20 = new javax.swing.JSeparator();
        jLabel74 = new widget.Label();
        jLabel82 = new widget.Label();
        jLabel53 = new widget.Label();
        jLabel75 = new widget.Label();
        jLabel83 = new widget.Label();
        labelDiagnosa = new widget.Label();
        jSeparator21 = new javax.swing.JSeparator();
        jSeparator19 = new javax.swing.JSeparator();
        jLabel84 = new widget.Label();
        labelProsedur = new widget.Label();
        panelGlass8 = new widget.panelisi();
        hapusKlaim = new widget.Button();
        editKlaim = new widget.Button();
        Riwayat = new widget.Button();
        jSeparator4 = new javax.swing.JSeparator();
        BtnGrouper = new widget.Button();
        finalklaim = new widget.Button();
        kirimonline = new widget.Button();
        jSeparator5 = new javax.swing.JSeparator();
        ViewPDFBerkas = new widget.Button();
        ViewPDFEklaim = new widget.Button();
        jSeparator6 = new javax.swing.JSeparator();
        BtnKeluar = new widget.Button();

        no_rawat.setHighlighter(null);
        no_rawat.setName("no_rawat"); // NOI18N

        icd10.setHighlighter(null);
        icd10.setName("icd10"); // NOI18N

        icd9.setHighlighter(null);
        icd9.setName("icd9"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "Bridgin E-Klaim", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(1310, 728));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(1300, 1260));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(1100, 1260));
        FormInput.setLayout(null);

        tgl_masuk.setEditable(false);
        tgl_masuk.setForeground(new java.awt.Color(0, 0, 0));
        tgl_masuk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2025-10-14" }));
        tgl_masuk.setDisplayFormat("yyyy-MM-dd");
        tgl_masuk.setName("tgl_masuk"); // NOI18N
        tgl_masuk.setOpaque(false);
        FormInput.add(tgl_masuk);
        tgl_masuk.setBounds(210, 175, 85, 26);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(85, 20, 270, 26);

        TNoRM.setEditable(false);
        TNoRM.setText("123456");
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.setPreferredSize(new java.awt.Dimension(235, 30));
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(20, 20, 60, 26);

        jLabel12.setText("Distole");
        jLabel12.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(1070, 85, 40, 26);

        pnb.setBackground(new java.awt.Color(249, 249, 249));
        pnb.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pnb.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnb.setHighlighter(null);
        pnb.setName("pnb"); // NOI18N
        pnb.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pnbKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                pnbKeyTyped(evt);
            }
        });
        FormInput.add(pnb);
        pnb.setBounds(250, 450, 100, 26);

        ahli.setBackground(new java.awt.Color(249, 249, 249));
        ahli.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        ahli.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ahli.setHighlighter(null);
        ahli.setName("ahli"); // NOI18N
        ahli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ahliKeyReleased(evt);
            }
        });
        FormInput.add(ahli);
        ahli.setBounds(250, 490, 100, 26);

        no_kartu.setEditable(false);
        no_kartu.setBackground(new java.awt.Color(249, 249, 249));
        no_kartu.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        no_kartu.setText("0001845850544");
        no_kartu.setHighlighter(null);
        no_kartu.setName("no_kartu"); // NOI18N
        FormInput.add(no_kartu);
        no_kartu.setBounds(360, 85, 115, 26);

        cara_masuk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "gp : Rujukan FKTP", "hosp-trans : Rujukan FKRTL", "mp : Rujukan Spesialis", "outp : Dari Rawat Jalan", "inp : Dari Rawat Inap", "emd : Dari Rawat Darurat", "born : Lahir di RS", "nursing : Rujukan Panti Jompo", "psych : Rujukan dari RS Jiwa", "rehab : Rujukan Fasilitas Rehab", "other : Lain-lain" }));
        cara_masuk.setName("cara_masuk"); // NOI18N
        FormInput.add(cara_masuk);
        cara_masuk.setBounds(160, 210, 225, 26);

        sistole.setHighlighter(null);
        sistole.setName("sistole"); // NOI18N
        FormInput.add(sistole);
        sistole.setBounds(1020, 85, 40, 26);

        jenis_rawat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 : RAWAT INAP", "2 : RAWAT JALAN", "3 : IGD" }));
        jenis_rawat.setSelectedIndex(1);
        jenis_rawat.setName("jenis_rawat"); // NOI18N
        jenis_rawat.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jenis_rawatItemStateChanged(evt);
            }
        });
        FormInput.add(jenis_rawat);
        jenis_rawat.setBounds(160, 140, 225, 26);

        cob.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "# : TIDAK COB", "0001 : MANDIRI INHEALTH", "0005 : ASURANSI SINAR MAS", "0006 : ASURANSI TUGU MANDIRI", "0007 : ASURANSI MITRA MAPARYA", "0008 : ASURANSI AXA MANDIRI FINANSIAL SERVICE", "0009 : ASURANSI AXA FINANSIAL INDONESIA", "0010 : LIPPO GENERAL INSURANCE", "0011 : ARTHAGRAHA GENERAL INSURANSE", "0012 : TUGU PRATAMA INDONESIA", "0013 : ASURANSI BINA DANA ARTA", "0014 : ASURANSI JIWA SINAR MAS MSIG", "0015 : AVRIST ASSURANCE", "0016 : ASURANSI JIWA SRAYA", "0017 : ASURANSI JIWA CENTRAL ASIA RAYA", "0018 : ASURANSI TAKAFUL KELUARGA", "0019 : ASURANSI JIWA GENERALI INDONESIA", "0020 : ASURANSI ASTRA BUANA", "0021 : ASURANSI UMUM MEGA", "0022 : ASURANSI MULTI ARTHA GUNA", "0023 : ASURANSI AIA INDONESIA", "0024 : ASURANSI JIWA EQUITY LIFE INDONESIA", "0025 : ASURANSI JIWA RECAPITAL", "0026 : GREAT EASTERN LIFE INDONESIA", "0027 : ASURANSI ADISARANA WANAARTHA", "0028 : ASURANSI JIWA BRINGIN JIWA SEJAHTERA", "0029 : BOSOWA ASURANSI", "0030 : MNC LIFE ASSURANCE", "0031 : ASURANSI AVIVA INDONESIA", "0032 : ASURANSI CENTRAL ASIA RAYA", "0033 : ASURANSI ALLIANZ LIFE INDONESIA", "0034 : ASURANSI BINTANG", "0035 : TOKIO MARINE LIFE INSURANCE INDONESIA", "0036 : MALACCA TRUST WUWUNGAN", "0037 : ASURANSI JASA INDONESIA", "0038 : ASURANSI JIWA MANULIFE INDONESIA", "0039 : ASURANSI BANGUN ASKRIDA", "0040 : ASURANSI JIWA SEQUIS FINANCIAL", "0041 : ASURANSI AXA INDONESIA", "0042 : BNI LIFE", "0043 : ACE LIFE INSURANCE", "0044 : CITRA INTERNATIONAL UNDERWRITERS", "0045 : ASURANSI RELIANCE INDONESIA", "0046 : HANWHA LIFE INSURANCE INDONESIA", "0047 : ASURANSI DAYIN MITRA", "0048 : ASURANSI ADIRA DINAMIKA", "0049 : PAN PASIFIC INSURANCE", "0050 : ASURANSI SAMSUNG TUGU", "0051 : ASURANSI UMUM BUMI PUTERA MUDA 1967", "0052 : ASURANSI JIWA KRESNA", "0053 : ASURANSI RAMAYANA", "0054 : VICTORIA INSURANCE", "0055 : ASURANSI JIWA BERSAMA BUMIPUTERA 1912", "0056 : FWD LIFE INDONESIA", "0057 : ASURANSI TAKAFUL KELUARGA", "0058 : ASURANSI TUGU KRESNA PRATAMA", "0059 : SOMPO INSURANCE" }));
        cob.setName("cob"); // NOI18N
        FormInput.add(cob);
        cob.setBounds(625, 85, 310, 26);

        jaminan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "3 : JKN ", "71 : JAMINAN COVID-19", "72 : JAMINAN KIPI", "73 : JAMINAN BAYI BARU LAHIR", "74 : JAMINAN PERPANJANGAN MASA RAWAT", "75 : JAMINAN CO-INSIDENSE", "76 : JAMPERSAL", "77 : JAMINAN PEMULIHAN KESEHATAN PRIORITAS" }));
        jaminan.setName("jaminan"); // NOI18N
        FormInput.add(jaminan);
        jaminan.setBounds(20, 85, 335, 26);

        jLabel6.setForeground(new java.awt.Color(102, 102, 102));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel6.setText("Jaminan / Cara Bayar");
        jLabel6.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        FormInput.add(jLabel6);
        jLabel6.setBounds(20, 60, 130, 23);

        rehab.setBackground(new java.awt.Color(249, 249, 249));
        rehab.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rehab.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rehab.setHighlighter(null);
        rehab.setName("rehab"); // NOI18N
        rehab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                rehabKeyReleased(evt);
            }
        });
        FormInput.add(rehab);
        rehab.setBounds(250, 572, 100, 26);

        rad.setBackground(new java.awt.Color(249, 249, 249));
        rad.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rad.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rad.setHighlighter(null);
        rad.setName("rad"); // NOI18N
        rad.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                radKeyReleased(evt);
            }
        });
        FormInput.add(rad);
        rad.setBounds(250, 530, 100, 26);

        alkes.setBackground(new java.awt.Color(249, 249, 249));
        alkes.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        alkes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        alkes.setHighlighter(null);
        alkes.setName("alkes"); // NOI18N
        alkes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                alkesKeyReleased(evt);
            }
        });
        FormInput.add(alkes);
        alkes.setBounds(250, 657, 100, 26);

        obat.setBackground(new java.awt.Color(249, 249, 249));
        obat.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        obat.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        obat.setHighlighter(null);
        obat.setName("obat"); // NOI18N
        obat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                obatKeyReleased(evt);
            }
        });
        FormInput.add(obat);
        obat.setBounds(250, 615, 100, 26);

        pb.setBackground(new java.awt.Color(249, 249, 249));
        pb.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pb.setText("111,111");
        pb.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pb.setHighlighter(null);
        pb.setName("pb"); // NOI18N
        pb.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pbKeyPressed(evt);
            }
        });
        FormInput.add(pb);
        pb.setBounds(620, 450, 100, 26);

        kpr.setBackground(new java.awt.Color(249, 249, 249));
        kpr.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        kpr.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        kpr.setHighlighter(null);
        kpr.setName("kpr"); // NOI18N
        kpr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kprKeyPressed(evt);
            }
        });
        FormInput.add(kpr);
        kpr.setBounds(620, 490, 100, 26);

        akomodasi.setBackground(new java.awt.Color(249, 249, 249));
        akomodasi.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        akomodasi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        akomodasi.setHighlighter(null);
        akomodasi.setName("akomodasi"); // NOI18N
        akomodasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                akomodasiKeyPressed(evt);
            }
        });
        FormInput.add(akomodasi);
        akomodasi.setBounds(620, 572, 100, 26);

        lab.setBackground(new java.awt.Color(249, 249, 249));
        lab.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lab.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lab.setHighlighter(null);
        lab.setName("lab"); // NOI18N
        lab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                labKeyPressed(evt);
            }
        });
        FormInput.add(lab);
        lab.setBounds(620, 530, 100, 26);

        bmhp.setBackground(new java.awt.Color(249, 249, 249));
        bmhp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        bmhp.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        bmhp.setHighlighter(null);
        bmhp.setName("bmhp"); // NOI18N
        bmhp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                bmhpKeyPressed(evt);
            }
        });
        FormInput.add(bmhp);
        bmhp.setBounds(620, 657, 100, 26);

        kronis.setBackground(new java.awt.Color(249, 249, 249));
        kronis.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        kronis.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        kronis.setHighlighter(null);
        kronis.setName("kronis"); // NOI18N
        kronis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kronisKeyPressed(evt);
            }
        });
        FormInput.add(kronis);
        kronis.setBounds(620, 615, 100, 26);

        ksl.setBackground(new java.awt.Color(249, 249, 249));
        ksl.setColumns(0);
        ksl.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        ksl.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ksl.setHighlighter(null);
        ksl.setName("ksl"); // NOI18N
        ksl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kslKeyPressed(evt);
            }
        });
        FormInput.add(ksl);
        ksl.setBounds(970, 450, 100, 26);

        pnj.setBackground(new java.awt.Color(249, 249, 249));
        pnj.setColumns(0);
        pnj.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pnj.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnj.setHighlighter(null);
        pnj.setName("pnj"); // NOI18N
        pnj.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pnjKeyPressed(evt);
            }
        });
        FormInput.add(pnj);
        pnj.setBounds(970, 490, 100, 26);

        ri.setBackground(new java.awt.Color(249, 249, 249));
        ri.setColumns(0);
        ri.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        ri.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ri.setHighlighter(null);
        ri.setName("ri"); // NOI18N
        ri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                riKeyPressed(evt);
            }
        });
        FormInput.add(ri);
        ri.setBounds(970, 572, 100, 26);

        pl_darah.setBackground(new java.awt.Color(249, 249, 249));
        pl_darah.setColumns(0);
        pl_darah.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pl_darah.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pl_darah.setHighlighter(null);
        pl_darah.setName("pl_darah"); // NOI18N
        pl_darah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pl_darahKeyPressed(evt);
            }
        });
        FormInput.add(pl_darah);
        pl_darah.setBounds(970, 530, 100, 26);

        sewa_alat.setBackground(new java.awt.Color(249, 249, 249));
        sewa_alat.setColumns(0);
        sewa_alat.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        sewa_alat.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        sewa_alat.setHighlighter(null);
        sewa_alat.setName("sewa_alat"); // NOI18N
        sewa_alat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sewa_alatKeyPressed(evt);
            }
        });
        FormInput.add(sewa_alat);
        sewa_alat.setBounds(970, 657, 100, 26);

        obat_kemo.setBackground(new java.awt.Color(249, 249, 249));
        obat_kemo.setColumns(0);
        obat_kemo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        obat_kemo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        obat_kemo.setHighlighter(null);
        obat_kemo.setName("obat_kemo"); // NOI18N
        obat_kemo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                obat_kemoKeyPressed(evt);
            }
        });
        FormInput.add(obat_kemo);
        obat_kemo.setBounds(970, 615, 100, 26);

        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel27.setText("BMHP");
        jLabel27.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel27.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(420, 657, 190, 26);

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel28.setText("Prosedur Non Bedah");
        jLabel28.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel28.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(20, 450, 220, 26);

        jLabel29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel29.setText("Tenaga Ahli");
        jLabel29.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel29.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(20, 490, 220, 26);

        jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel30.setText("Radiologi");
        jLabel30.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel30.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(20, 530, 220, 26);

        jLabel31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel31.setText("Rehabilitasi");
        jLabel31.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel31.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(20, 572, 220, 26);

        jLabel32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel32.setText("Obat");
        jLabel32.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel32.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(20, 615, 220, 26);

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel33.setText("Prosedur Bedah");
        jLabel33.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel33.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(420, 450, 190, 26);

        jLabel34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel34.setText("Keperawatan");
        jLabel34.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel34.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel34.setName("jLabel34"); // NOI18N
        FormInput.add(jLabel34);
        jLabel34.setBounds(420, 490, 190, 26);

        jLabel35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel35.setText("Labotatorium");
        jLabel35.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel35.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(420, 530, 190, 26);

        jLabel36.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel36.setText("Kamar / Akomodasi");
        jLabel36.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel36.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel36.setName("jLabel36"); // NOI18N
        FormInput.add(jLabel36);
        jLabel36.setBounds(420, 572, 190, 26);

        jLabel37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel37.setText("Obat Kronis");
        jLabel37.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel37.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(420, 615, 190, 26);

        jLabel38.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel38.setText("Konsultasi");
        jLabel38.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel38.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(810, 450, 150, 26);

        jLabel39.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel39.setText("Penunjang");
        jLabel39.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel39.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(810, 490, 150, 26);

        jLabel40.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel40.setText("Pelayanan Darah");
        jLabel40.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel40.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(810, 530, 150, 26);

        jLabel41.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel41.setText("Rawat Intensif");
        jLabel41.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel41.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel41.setName("jLabel41"); // NOI18N
        FormInput.add(jLabel41);
        jLabel41.setBounds(810, 572, 150, 26);

        jLabel42.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel42.setText("Obat Kemoterapi");
        jLabel42.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel42.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(810, 615, 150, 26);

        jam_masuk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        jam_masuk.setName("jam_masuk"); // NOI18N
        FormInput.add(jam_masuk);
        jam_masuk.setBounds(300, 175, 40, 26);

        menit_masuk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        menit_masuk.setName("menit_masuk"); // NOI18N
        FormInput.add(menit_masuk);
        menit_masuk.setBounds(345, 175, 40, 26);

        tgl_keluar.setEditable(false);
        tgl_keluar.setForeground(new java.awt.Color(0, 0, 0));
        tgl_keluar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2025-10-14" }));
        tgl_keluar.setDisplayFormat("yyyy-MM-dd");
        tgl_keluar.setName("tgl_keluar"); // NOI18N
        tgl_keluar.setOpaque(false);
        FormInput.add(tgl_keluar);
        tgl_keluar.setBounds(450, 175, 85, 26);

        jam_keluar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        jam_keluar.setName("jam_keluar"); // NOI18N
        FormInput.add(jam_keluar);
        jam_keluar.setBounds(540, 175, 40, 26);

        menit_keluar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        menit_keluar.setName("menit_keluar"); // NOI18N
        FormInput.add(menit_keluar);
        menit_keluar.setBounds(585, 175, 40, 26);

        rawat_intensif.setBackground(new java.awt.Color(255, 255, 255));
        rawat_intensif.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        rawat_intensif.setText("Ada Rawat Intensif");
        rawat_intensif.setName("rawat_intensif"); // NOI18N
        rawat_intensif.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rawat_intensifItemStateChanged(evt);
            }
        });
        FormInput.add(rawat_intensif);
        rawat_intensif.setBounds(540, 140, 140, 26);

        ventilator.setBackground(new java.awt.Color(255, 255, 255));
        ventilator.setFont(new java.awt.Font("Inter", 1, 12)); // NOI18N
        ventilator.setText("Ventilator");
        ventilator.setName("ventilator"); // NOI18N
        ventilator.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ventilatorItemStateChanged(evt);
            }
        });
        FormInput.add(ventilator);
        ventilator.setBounds(690, 140, 90, 26);

        naik_turun_kelas.setBackground(new java.awt.Color(255, 255, 255));
        naik_turun_kelas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        naik_turun_kelas.setText("Naik / Turun Kelas");
        naik_turun_kelas.setName("naik_turun_kelas"); // NOI18N
        naik_turun_kelas.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                naik_turun_kelasItemStateChanged(evt);
            }
        });
        FormInput.add(naik_turun_kelas);
        naik_turun_kelas.setBounds(395, 140, 140, 26);

        lama_naik_kelas.setEditable(false);
        lama_naik_kelas.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        lama_naik_kelas.setEnabled(false);
        lama_naik_kelas.setHighlighter(null);
        lama_naik_kelas.setName("lama_naik_kelas"); // NOI18N
        FormInput.add(lama_naik_kelas);
        lama_naik_kelas.setBounds(450, 210, 85, 26);

        chronic.setText("-");
        chronic.setHighlighter(null);
        chronic.setName("chronic"); // NOI18N
        FormInput.add(chronic);
        chronic.setBounds(450, 245, 85, 26);

        sub_acute.setText("-");
        sub_acute.setHighlighter(null);
        sub_acute.setName("sub_acute"); // NOI18N
        FormInput.add(sub_acute);
        sub_acute.setBounds(220, 245, 80, 26);

        berat_bayi.setHighlighter(null);
        berat_bayi.setName("berat_bayi"); // NOI18N
        FormInput.add(berat_bayi);
        berat_bayi.setBounds(970, 175, 85, 26);

        dpjp.setHighlighter(null);
        dpjp.setName("dpjp"); // NOI18N
        FormInput.add(dpjp);
        dpjp.setBounds(160, 280, 580, 26);

        validasi_cvd19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Select.png"))); // NOI18N
        validasi_cvd19.setText("Validasi");
        validasi_cvd19.setToolTipText("");
        validasi_cvd19.setEnabled(false);
        validasi_cvd19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        validasi_cvd19.setIconTextGap(8);
        validasi_cvd19.setName("validasi_cvd19"); // NOI18N
        validasi_cvd19.setPreferredSize(new java.awt.Dimension(100, 30));
        validasi_cvd19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validasi_cvd19ActionPerformed(evt);
            }
        });
        FormInput.add(validasi_cvd19);
        validasi_cvd19.setBounds(310, 350, 90, 26);

        co_covid_19.setEditable(false);
        co_covid_19.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        co_covid_19.setEnabled(false);
        co_covid_19.setHighlighter(null);
        co_covid_19.setName("co_covid_19"); // NOI18N
        FormInput.add(co_covid_19);
        co_covid_19.setBounds(160, 350, 140, 26);

        status_pulang.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 : Atas persetujuan dokter", "2 : Dirujuk", "3 : Atas permintaan sendiri", "4 : Meninggal", "5 : Lain-lain" }));
        status_pulang.setName("status_pulang"); // NOI18N
        FormInput.add(status_pulang);
        status_pulang.setBounds(970, 210, 170, 26);

        kelas_eksekutif.setBackground(new java.awt.Color(255, 255, 255));
        kelas_eksekutif.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        kelas_eksekutif.setText("Kelas Eksekutif");
        kelas_eksekutif.setName("kelas_eksekutif"); // NOI18N
        kelas_eksekutif.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                kelas_eksekutifItemStateChanged(evt);
            }
        });
        FormInput.add(kelas_eksekutif);
        kelas_eksekutif.setBounds(395, 140, 120, 26);

        tgl_intubasi.setEditable(false);
        tgl_intubasi.setForeground(new java.awt.Color(0, 0, 0));
        tgl_intubasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2025-10-14" }));
        tgl_intubasi.setDisplayFormat("yyyy-MM-dd");
        tgl_intubasi.setName("tgl_intubasi"); // NOI18N
        tgl_intubasi.setOpaque(false);
        FormInput.add(tgl_intubasi);
        tgl_intubasi.setBounds(970, 315, 85, 26);

        jam_intubasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        jam_intubasi.setName("jam_intubasi"); // NOI18N
        FormInput.add(jam_intubasi);
        jam_intubasi.setBounds(1060, 315, 40, 26);

        menit_intubasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        menit_intubasi.setName("menit_intubasi"); // NOI18N
        FormInput.add(menit_intubasi);
        menit_intubasi.setBounds(1105, 315, 40, 26);

        total_rupiah.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        total_rupiah.setText("123456789");
        total_rupiah.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        total_rupiah.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        total_rupiah.setName("total_rupiah"); // NOI18N
        FormInput.add(total_rupiah);
        total_rupiah.setBounds(620, 400, 150, 30);

        menit_ekstubasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        menit_ekstubasi.setName("menit_ekstubasi"); // NOI18N
        FormInput.add(menit_ekstubasi);
        menit_ekstubasi.setBounds(1105, 350, 40, 23);

        jam_ekstubasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        jam_ekstubasi.setName("jam_ekstubasi"); // NOI18N
        FormInput.add(jam_ekstubasi);
        jam_ekstubasi.setBounds(1060, 350, 40, 23);

        tgl_ekstubasi.setEditable(false);
        tgl_ekstubasi.setForeground(new java.awt.Color(0, 0, 0));
        tgl_ekstubasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2025-10-14" }));
        tgl_ekstubasi.setDisplayFormat("yyyy-MM-dd");
        tgl_ekstubasi.setName("tgl_ekstubasi"); // NOI18N
        tgl_ekstubasi.setOpaque(false);
        FormInput.add(tgl_ekstubasi);
        tgl_ekstubasi.setBounds(970, 350, 85, 23);

        hari_intensif.setEditable(false);
        hari_intensif.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        hari_intensif.setEnabled(false);
        hari_intensif.setHighlighter(null);
        hari_intensif.setName("hari_intensif"); // NOI18N
        FormInput.add(hari_intensif);
        hari_intensif.setBounds(690, 210, 85, 26);

        jLabel66.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel66.setText("Check Diagnosa Utama");
        jLabel66.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel66.setName("jLabel66"); // NOI18N
        FormInput.add(jLabel66);
        jLabel66.setBounds(1410, 1290, 150, 23);

        jLabel67.setText("Sistole");
        jLabel67.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel67.setName("jLabel67"); // NOI18N
        FormInput.add(jLabel67);
        jLabel67.setBounds(970, 85, 40, 26);

        distole.setHighlighter(null);
        distole.setName("distole"); // NOI18N
        FormInput.add(distole);
        distole.setBounds(1120, 85, 40, 26);

        tarif_eksekutif.setEditable(false);
        tarif_eksekutif.setHighlighter(null);
        tarif_eksekutif.setName("tarif_eksekutif"); // NOI18N
        FormInput.add(tarif_eksekutif);
        tarif_eksekutif.setBounds(690, 245, 85, 26);

        jLabel13.setText("Tarif Eksekutif");
        jLabel13.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(550, 245, 130, 26);

        pnb1.setBackground(new java.awt.Color(249, 249, 249));
        pnb1.setHighlighter(null);
        pnb1.setName("pnb1"); // NOI18N
        pnb1.setPreferredSize(new java.awt.Dimension(100, 26));
        FormInput.add(pnb1);
        pnb1.setBounds(250, 450, 100, 26);

        tgl_lahir.setEditable(false);
        tgl_lahir.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tgl_lahir.setText("1987-02-07");
        tgl_lahir.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        tgl_lahir.setEnabled(false);
        tgl_lahir.setHighlighter(null);
        tgl_lahir.setName("tgl_lahir"); // NOI18N
        FormInput.add(tgl_lahir);
        tgl_lahir.setBounds(395, 20, 80, 26);

        jenis_kelamin.setEditable(false);
        jenis_kelamin.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jenis_kelamin.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jenis_kelamin.setEnabled(false);
        jenis_kelamin.setHighlighter(null);
        jenis_kelamin.setName("jenis_kelamin"); // NOI18N
        FormInput.add(jenis_kelamin);
        jenis_kelamin.setBounds(360, 20, 30, 26);

        jam_ventilator.setEditable(false);
        jam_ventilator.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jam_ventilator.setEnabled(false);
        jam_ventilator.setHighlighter(null);
        jam_ventilator.setName("jam_ventilator"); // NOI18N
        FormInput.add(jam_ventilator);
        jam_ventilator.setBounds(970, 280, 85, 26);

        jLabel65.setText("Ventilator Berapa Jam ");
        jLabel65.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel65.setName("jLabel65"); // NOI18N
        FormInput.add(jLabel65);
        jLabel65.setBounds(790, 280, 160, 26);

        jLabel68.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel68.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel68.setText("Tarif Rumah Sakit :");
        jLabel68.setToolTipText("Total nilai tertagih pada perawatan dalam satu periode, tidak termasuk item tagihan pada tarif Non INA-CBG yang tersebut di bawah");
        jLabel68.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel68.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel68.setIconTextGap(8);
        jLabel68.setName("jLabel68"); // NOI18N
        FormInput.add(jLabel68);
        jLabel68.setBounds(430, 400, 160, 30);

        nm_diag_1.setEditable(false);
        nm_diag_1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_diag_1.setEnabled(false);
        nm_diag_1.setHighlighter(null);
        nm_diag_1.setName("nm_diag_1"); // NOI18N
        FormInput.add(nm_diag_1);
        nm_diag_1.setBounds(1260, 1310, 190, 23);

        jLabel70.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel70.setText("Alkes");
        jLabel70.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel70.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel70.setName("jLabel70"); // NOI18N
        FormInput.add(jLabel70);
        jLabel70.setBounds(20, 657, 220, 26);

        jLabel71.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel71.setText("Diagnosa ( ICD-10 ) :");
        jLabel71.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel71.setName("jLabel71"); // NOI18N
        FormInput.add(jLabel71);
        jLabel71.setBounds(30, 800, 200, 26);

        kd_diag_1.setEditable(false);
        kd_diag_1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_diag_1.setEnabled(false);
        kd_diag_1.setHighlighter(null);
        kd_diag_1.setName("kd_diag_1"); // NOI18N
        FormInput.add(kd_diag_1);
        kd_diag_1.setBounds(1190, 1310, 65, 23);

        nm_diag_2.setEditable(false);
        nm_diag_2.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_diag_2.setEnabled(false);
        nm_diag_2.setHighlighter(null);
        nm_diag_2.setName("nm_diag_2"); // NOI18N
        FormInput.add(nm_diag_2);
        nm_diag_2.setBounds(1260, 1340, 190, 23);

        kd_diag_2.setEditable(false);
        kd_diag_2.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_diag_2.setEnabled(false);
        kd_diag_2.setHighlighter(null);
        kd_diag_2.setName("kd_diag_2"); // NOI18N
        FormInput.add(kd_diag_2);
        kd_diag_2.setBounds(1190, 1340, 65, 23);

        nm_diag_3.setEditable(false);
        nm_diag_3.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_diag_3.setEnabled(false);
        nm_diag_3.setHighlighter(null);
        nm_diag_3.setName("nm_diag_3"); // NOI18N
        FormInput.add(nm_diag_3);
        nm_diag_3.setBounds(1260, 1370, 190, 23);

        kd_diag_3.setEditable(false);
        kd_diag_3.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_diag_3.setEnabled(false);
        kd_diag_3.setHighlighter(null);
        kd_diag_3.setName("kd_diag_3"); // NOI18N
        FormInput.add(kd_diag_3);
        kd_diag_3.setBounds(1190, 1370, 65, 23);

        nm_diag_4.setEditable(false);
        nm_diag_4.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_diag_4.setEnabled(false);
        nm_diag_4.setHighlighter(null);
        nm_diag_4.setName("nm_diag_4"); // NOI18N
        FormInput.add(nm_diag_4);
        nm_diag_4.setBounds(1260, 1400, 190, 23);

        kd_diag_4.setEditable(false);
        kd_diag_4.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_diag_4.setEnabled(false);
        kd_diag_4.setHighlighter(null);
        kd_diag_4.setName("kd_diag_4"); // NOI18N
        FormInput.add(kd_diag_4);
        kd_diag_4.setBounds(1190, 1400, 65, 23);

        nm_diag_5.setEditable(false);
        nm_diag_5.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_diag_5.setEnabled(false);
        nm_diag_5.setHighlighter(null);
        nm_diag_5.setName("nm_diag_5"); // NOI18N
        FormInput.add(nm_diag_5);
        nm_diag_5.setBounds(1260, 1430, 190, 23);

        kd_diag_5.setEditable(false);
        kd_diag_5.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_diag_5.setEnabled(false);
        kd_diag_5.setHighlighter(null);
        kd_diag_5.setName("kd_diag_5"); // NOI18N
        FormInput.add(kd_diag_5);
        kd_diag_5.setBounds(1190, 1430, 65, 23);

        nm_psdr_1.setEditable(false);
        nm_psdr_1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_psdr_1.setEnabled(false);
        nm_psdr_1.setHighlighter(null);
        nm_psdr_1.setName("nm_psdr_1"); // NOI18N
        FormInput.add(nm_psdr_1);
        nm_psdr_1.setBounds(1630, 1310, 190, 23);

        kd_psdr_1.setEditable(false);
        kd_psdr_1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_psdr_1.setEnabled(false);
        kd_psdr_1.setHighlighter(null);
        kd_psdr_1.setName("kd_psdr_1"); // NOI18N
        FormInput.add(kd_psdr_1);
        kd_psdr_1.setBounds(1550, 1310, 70, 23);

        nm_psdr_2.setEditable(false);
        nm_psdr_2.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_psdr_2.setEnabled(false);
        nm_psdr_2.setHighlighter(null);
        nm_psdr_2.setName("nm_psdr_2"); // NOI18N
        FormInput.add(nm_psdr_2);
        nm_psdr_2.setBounds(1630, 1340, 190, 23);

        kd_psdr_2.setEditable(false);
        kd_psdr_2.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_psdr_2.setEnabled(false);
        kd_psdr_2.setHighlighter(null);
        kd_psdr_2.setName("kd_psdr_2"); // NOI18N
        FormInput.add(kd_psdr_2);
        kd_psdr_2.setBounds(1550, 1340, 70, 23);

        nm_psdr_3.setEditable(false);
        nm_psdr_3.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_psdr_3.setEnabled(false);
        nm_psdr_3.setHighlighter(null);
        nm_psdr_3.setName("nm_psdr_3"); // NOI18N
        FormInput.add(nm_psdr_3);
        nm_psdr_3.setBounds(1630, 1370, 190, 23);

        kd_psdr_3.setEditable(false);
        kd_psdr_3.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_psdr_3.setEnabled(false);
        kd_psdr_3.setHighlighter(null);
        kd_psdr_3.setName("kd_psdr_3"); // NOI18N
        FormInput.add(kd_psdr_3);
        kd_psdr_3.setBounds(1550, 1370, 70, 23);

        nm_psdr_4.setEditable(false);
        nm_psdr_4.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_psdr_4.setEnabled(false);
        nm_psdr_4.setHighlighter(null);
        nm_psdr_4.setName("nm_psdr_4"); // NOI18N
        FormInput.add(nm_psdr_4);
        nm_psdr_4.setBounds(1630, 1400, 190, 23);

        kd_psdr_4.setEditable(false);
        kd_psdr_4.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_psdr_4.setEnabled(false);
        kd_psdr_4.setHighlighter(null);
        kd_psdr_4.setName("kd_psdr_4"); // NOI18N
        FormInput.add(kd_psdr_4);
        kd_psdr_4.setBounds(1550, 1400, 70, 23);

        nm_psdr_5.setEditable(false);
        nm_psdr_5.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        nm_psdr_5.setEnabled(false);
        nm_psdr_5.setHighlighter(null);
        nm_psdr_5.setName("nm_psdr_5"); // NOI18N
        FormInput.add(nm_psdr_5);
        nm_psdr_5.setBounds(1630, 1430, 190, 23);

        kd_psdr_5.setEditable(false);
        kd_psdr_5.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kd_psdr_5.setEnabled(false);
        kd_psdr_5.setHighlighter(null);
        kd_psdr_5.setName("kd_psdr_5"); // NOI18N
        FormInput.add(kd_psdr_5);
        kd_psdr_5.setBounds(1550, 1430, 70, 23);

        chk_diag_5.setBackground(new java.awt.Color(255, 255, 255));
        chk_diag_5.setFont(new java.awt.Font("Inter", 1, 12)); // NOI18N
        chk_diag_5.setName("chk_diag_5"); // NOI18N
        chk_diag_5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chk_diag_5ItemStateChanged(evt);
            }
        });
        FormInput.add(chk_diag_5);
        chk_diag_5.setBounds(10, 990, 30, 22);

        chk_diag_1.setBackground(new java.awt.Color(255, 255, 255));
        chk_diag_1.setFont(new java.awt.Font("Inter", 1, 12)); // NOI18N
        chk_diag_1.setName("chk_diag_1"); // NOI18N
        chk_diag_1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chk_diag_1ItemStateChanged(evt);
            }
        });
        chk_diag_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chk_diag_1ActionPerformed(evt);
            }
        });
        FormInput.add(chk_diag_1);
        chk_diag_1.setBounds(10, 838, 30, 22);

        chk_diag_2.setBackground(new java.awt.Color(255, 255, 255));
        chk_diag_2.setFont(new java.awt.Font("Inter", 1, 12)); // NOI18N
        chk_diag_2.setName("chk_diag_2"); // NOI18N
        chk_diag_2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chk_diag_2ItemStateChanged(evt);
            }
        });
        FormInput.add(chk_diag_2);
        chk_diag_2.setBounds(10, 875, 30, 22);

        chk_diag_3.setBackground(new java.awt.Color(255, 255, 255));
        chk_diag_3.setFont(new java.awt.Font("Inter", 1, 12)); // NOI18N
        chk_diag_3.setName("chk_diag_3"); // NOI18N
        chk_diag_3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chk_diag_3ItemStateChanged(evt);
            }
        });
        FormInput.add(chk_diag_3);
        chk_diag_3.setBounds(10, 915, 30, 22);

        chk_diag_4.setBackground(new java.awt.Color(255, 255, 255));
        chk_diag_4.setFont(new java.awt.Font("Inter", 1, 12)); // NOI18N
        chk_diag_4.setName("chk_diag_4"); // NOI18N
        chk_diag_4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chk_diag_4ItemStateChanged(evt);
            }
        });
        FormInput.add(chk_diag_4);
        chk_diag_4.setBounds(10, 952, 30, 22);

        jLabel72.setText("5 ");
        jLabel72.setName("jLabel72"); // NOI18N
        FormInput.add(jLabel72);
        jLabel72.setBounds(1300, 280, 40, 26);

        jLabel73.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel73.setText("Prosedur Pasien");
        jLabel73.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel73.setName("jLabel73"); // NOI18N
        FormInput.add(jLabel73);
        jLabel73.setBounds(1550, 1290, 115, 23);

        total_rupiah1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        total_rupiah1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        total_rupiah1.setName("total_rupiah1"); // NOI18N
        FormInput.add(total_rupiah1);
        total_rupiah1.setBounds(200, 700, 140, 30);

        jLabel77.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel77.setText("APGAR SCORE BAYI");
        jLabel77.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel77.setName("jLabel77"); // NOI18N
        FormInput.add(jLabel77);
        jLabel77.setBounds(1270, 250, 130, 26);

        ap_5.setHighlighter(null);
        ap_5.setName("ap_5"); // NOI18N
        ap_5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_5KeyPressed(evt);
            }
        });
        FormInput.add(ap_5);
        ap_5.setBounds(1280, 400, 40, 26);

        jLabel78.setText("1 ");
        jLabel78.setName("jLabel78"); // NOI18N
        FormInput.add(jLabel78);
        jLabel78.setBounds(1260, 280, 20, 26);

        ap_1.setHighlighter(null);
        ap_1.setName("ap_1"); // NOI18N
        FormInput.add(ap_1);
        ap_1.setBounds(1280, 280, 40, 26);

        ap_2.setHighlighter(null);
        ap_2.setName("ap_2"); // NOI18N
        ap_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_2KeyPressed(evt);
            }
        });
        FormInput.add(ap_2);
        ap_2.setBounds(1280, 310, 40, 26);

        ap_3.setHighlighter(null);
        ap_3.setName("ap_3"); // NOI18N
        ap_3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_3KeyPressed(evt);
            }
        });
        FormInput.add(ap_3);
        ap_3.setBounds(1280, 340, 40, 26);

        ap_4.setHighlighter(null);
        ap_4.setName("ap_4"); // NOI18N
        ap_4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_4KeyPressed(evt);
            }
        });
        FormInput.add(ap_4);
        ap_4.setBounds(1280, 370, 40, 26);

        ap_10.setHighlighter(null);
        ap_10.setName("ap_10"); // NOI18N
        ap_10.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_10KeyPressed(evt);
            }
        });
        FormInput.add(ap_10);
        ap_10.setBounds(1340, 400, 40, 26);

        ap_6.setHighlighter(null);
        ap_6.setName("ap_6"); // NOI18N
        ap_6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_6KeyPressed(evt);
            }
        });
        FormInput.add(ap_6);
        ap_6.setBounds(1340, 280, 40, 26);

        ap_7.setHighlighter(null);
        ap_7.setName("ap_7"); // NOI18N
        ap_7.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_7KeyPressed(evt);
            }
        });
        FormInput.add(ap_7);
        ap_7.setBounds(1340, 310, 40, 26);

        ap_8.setHighlighter(null);
        ap_8.setName("ap_8"); // NOI18N
        ap_8.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_8KeyPressed(evt);
            }
        });
        FormInput.add(ap_8);
        ap_8.setBounds(1340, 340, 40, 26);

        ap_9.setHighlighter(null);
        ap_9.setName("ap_9"); // NOI18N
        ap_9.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ap_9KeyPressed(evt);
            }
        });
        FormInput.add(ap_9);
        ap_9.setBounds(1340, 370, 40, 26);

        jLabel49.setForeground(new java.awt.Color(255, 0, 0));
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel49.setText("#  JANGAN LUPA KLIK GROUPER LAGI");
        jLabel49.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        jLabel49.setName("jLabel49"); // NOI18N
        FormInput.add(jLabel49);
        jLabel49.setBounds(680, 1280, 230, 20);

        Tsep.setEditable(false);
        Tsep.setBackground(new java.awt.Color(249, 249, 249));
        Tsep.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Tsep.setText("0342R0490525V000011");
        Tsep.setHighlighter(null);
        Tsep.setName("Tsep"); // NOI18N
        Tsep.setOpaque(true);
        FormInput.add(Tsep);
        Tsep.setBounds(480, 85, 140, 26);

        jLabel50.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel50.setText("Sewa Alat");
        jLabel50.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel50.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel50.setName("jLabel50"); // NOI18N
        FormInput.add(jLabel50);
        jLabel50.setBounds(810, 657, 150, 26);

        total_grouping_tersimpan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        total_grouping_tersimpan.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        total_grouping_tersimpan.setName("total_grouping_tersimpan"); // NOI18N
        FormInput.add(total_grouping_tersimpan);
        total_grouping_tersimpan.setBounds(600, 700, 170, 30);

        deskripsi_grouping_tersimpan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        deskripsi_grouping_tersimpan.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        deskripsi_grouping_tersimpan.setName("deskripsi_grouping_tersimpan"); // NOI18N
        FormInput.add(deskripsi_grouping_tersimpan);
        deskripsi_grouping_tersimpan.setBounds(640, 750, 580, 30);

        code_cbg_grouping_tersimpan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        code_cbg_grouping_tersimpan.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        code_cbg_grouping_tersimpan.setName("code_cbg_grouping_tersimpan"); // NOI18N
        FormInput.add(code_cbg_grouping_tersimpan);
        code_cbg_grouping_tersimpan.setBounds(240, 750, 150, 30);

        pasien_tb.setHighlighter(null);
        pasien_tb.setName("pasien_tb"); // NOI18N
        FormInput.add(pasien_tb);
        pasien_tb.setBounds(160, 315, 140, 26);

        validasi_tb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Select.png"))); // NOI18N
        validasi_tb.setText("Validasi");
        validasi_tb.setToolTipText("");
        validasi_tb.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        validasi_tb.setIconTextGap(8);
        validasi_tb.setName("validasi_tb"); // NOI18N
        validasi_tb.setPreferredSize(new java.awt.Dimension(100, 30));
        validasi_tb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validasi_tbActionPerformed(evt);
            }
        });
        FormInput.add(validasi_tb);
        validasi_tb.setBounds(310, 315, 90, 26);

        batal_validasi_tb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/101.png"))); // NOI18N
        batal_validasi_tb.setText("Batal Validasi");
        batal_validasi_tb.setToolTipText("");
        batal_validasi_tb.setIconTextGap(8);
        batal_validasi_tb.setName("batal_validasi_tb"); // NOI18N
        batal_validasi_tb.setPreferredSize(new java.awt.Dimension(100, 30));
        batal_validasi_tb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batal_validasi_tbActionPerformed(evt);
            }
        });
        FormInput.add(batal_validasi_tb);
        batal_validasi_tb.setBounds(400, 315, 115, 26);

        check_hd.setBackground(new java.awt.Color(255, 255, 255));
        check_hd.setFont(new java.awt.Font("Inter", 1, 12)); // NOI18N
        check_hd.setText("Pasien HD");
        check_hd.setName("check_hd"); // NOI18N
        check_hd.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                check_hdItemStateChanged(evt);
            }
        });
        check_hd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                check_hdActionPerformed(evt);
            }
        });
        FormInput.add(check_hd);
        check_hd.setBounds(1180, 140, 90, 22);

        jLabel56.setText("Dializer");
        jLabel56.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel56.setName("jLabel56"); // NOI18N
        FormInput.add(jLabel56);
        jLabel56.setBounds(1180, 175, 90, 23);

        pasien_hd.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "MULTIPLE USE", "SINGLE USE" }));
        pasien_hd.setSelectedIndex(1);
        pasien_hd.setName("pasien_hd"); // NOI18N
        pasien_hd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasien_hdActionPerformed(evt);
            }
        });
        FormInput.add(pasien_hd);
        pasien_hd.setBounds(1280, 175, 110, 26);

        jLabel79.setText("Kantong Darah");
        jLabel79.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel79.setName("jLabel79"); // NOI18N
        FormInput.add(jLabel79);
        jLabel79.setBounds(1170, 210, 100, 23);

        kt_darah.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        kt_darah.setHighlighter(null);
        kt_darah.setName("kt_darah"); // NOI18N
        FormInput.add(kt_darah);
        kt_darah.setBounds(1280, 210, 50, 26);

        hak_kelas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kelas 1", "Kelas 2", "Kelas 3" }));
        hak_kelas.setSelectedIndex(2);
        hak_kelas.setName("hak_kelas"); // NOI18N
        FormInput.add(hak_kelas);
        hak_kelas.setBounds(970, 140, 170, 26);

        naik_kelas_pelayanan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "kelas_3 : Kelas 3", "kelas_2 : Kelas 2", "kelas_1 : Kelas 1", "vip : Diatas Kelas 1" }));
        naik_kelas_pelayanan.setName("naik_kelas_pelayanan"); // NOI18N
        FormInput.add(naik_kelas_pelayanan);
        naik_kelas_pelayanan.setBounds(970, 245, 170, 26);

        stage_2.setBackground(new java.awt.Color(0, 255, 204));
        stage_2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        stage_2.setName("stage_2"); // NOI18N
        FormInput.add(stage_2);
        stage_2.setBounds(970, 701, 250, 30);

        btnDPJP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnDPJP.setMnemonic('X');
        btnDPJP.setToolTipText("Alt+X");
        btnDPJP.setName("btnDPJP"); // NOI18N
        btnDPJP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDPJPActionPerformed(evt);
            }
        });
        btnDPJP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDPJPKeyPressed(evt);
            }
        });
        FormInput.add(btnDPJP);
        btnDPJP.setBounds(744, 280, 30, 26);

        jLabel61.setForeground(new java.awt.Color(255, 0, 0));
        jLabel61.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel61.setText("#  BEDA DPJP DIGANTI SEBELUM GROUPER");
        jLabel61.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        jLabel61.setName("jLabel61"); // NOI18N
        FormInput.add(jLabel61);
        jLabel61.setBounds(680, 1300, 230, 20);

        jLabel76.setForeground(new java.awt.Color(255, 0, 0));
        jLabel76.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel76.setText("#  JIKA TERLANJUR GROUPER EXIT DULU");
        jLabel76.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        jLabel76.setName("jLabel76"); // NOI18N
        FormInput.add(jLabel76);
        jLabel76.setBounds(680, 1320, 230, 30);

        jSeparator1.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(20, 55, 940, 1);
        jSeparator1.getAccessibleContext().setAccessibleName("");

        jLabel7.setForeground(new java.awt.Color(102, 102, 102));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setText("No. Peserta");
        jLabel7.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        FormInput.add(jLabel7);
        jLabel7.setBounds(360, 60, 110, 23);

        jLabel8.setForeground(new java.awt.Color(102, 102, 102));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("No. SEP");
        jLabel8.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(480, 60, 130, 23);

        jLabel9.setText("Jenis Rawat");
        jLabel9.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(50, 140, 90, 26);

        jLabel10.setText("Tanggal Rawat");
        jLabel10.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(50, 175, 90, 26);

        jLabel11.setText("Cara Masuk");
        jLabel11.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(50, 210, 90, 26);

        jLabel14.setForeground(new java.awt.Color(102, 102, 102));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("COB");
        jLabel14.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(630, 60, 300, 23);

        jSeparator2.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(150, 125, 1, 265);

        jSeparator3.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(20, 125, 1200, 1);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("Masuk");
        jLabel16.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(160, 175, 50, 23);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText("Pulang ");
        jLabel17.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(400, 175, 60, 26);

        jSeparator7.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator7.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator7.setName("jSeparator7"); // NOI18N
        FormInput.add(jSeparator7);
        jSeparator7.setBounds(960, 15, 1, 375);

        jLabel18.setText("Hak Kelas");
        jLabel18.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(840, 140, 110, 26);

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel19.setText("LOS");
        jLabel19.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N
        FormInput.add(jLabel19);
        jLabel19.setBounds(400, 210, 60, 26);

        jLabel20.setText("ADL Score");
        jLabel20.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(50, 245, 90, 26);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel21.setText("Sub Acute");
        jLabel21.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N
        FormInput.add(jLabel21);
        jLabel21.setBounds(160, 245, 90, 26);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("Chronic");
        jLabel23.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(400, 245, 60, 26);

        jLabel24.setText("Berat Lahir (gram)");
        jLabel24.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(830, 175, 120, 26);

        jLabel25.setText("Cara Pulang");
        jLabel25.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(810, 210, 140, 26);

        jLabel26.setText("Kelas Pelayanan");
        jLabel26.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(830, 245, 120, 26);

        jLabel43.setText("DPJP");
        jLabel43.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel43.setName("jLabel43"); // NOI18N
        FormInput.add(jLabel43);
        jLabel43.setBounds(50, 280, 90, 26);

        jLabel58.setText("Co-Insidense COVID-19");
        jLabel58.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel58.setName("jLabel58"); // NOI18N
        FormInput.add(jLabel58);
        jLabel58.setBounds(0, 350, 140, 26);

        jLabel22.setText("Pasien TB");
        jLabel22.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(30, 315, 110, 26);

        jSeparator8.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator8.setName("jSeparator8"); // NOI18N
        FormInput.add(jSeparator8);
        jSeparator8.setBounds(20, 390, 1200, 1);

        jLabel46.setText("Intubasi ");
        jLabel46.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel46.setName("jLabel46"); // NOI18N
        FormInput.add(jLabel46);
        jLabel46.setBounds(840, 315, 110, 26);

        jLabel62.setText("Ekstubasi");
        jLabel62.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel62.setName("jLabel62"); // NOI18N
        FormInput.add(jLabel62);
        jLabel62.setBounds(840, 350, 110, 26);

        jLabel63.setText("Rawat Insentif ( Hari )");
        jLabel63.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel63.setName("jLabel63"); // NOI18N
        FormInput.add(jLabel63);
        jLabel63.setBounds(550, 210, 130, 26);

        jSeparator9.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator9.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator9.setName("jSeparator9"); // NOI18N
        FormInput.add(jSeparator9);
        jSeparator9.setBounds(800, 125, 1, 265);

        jLabel15.setForeground(new java.awt.Color(102, 102, 102));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Tekanan Darah (mmHg)");
        jLabel15.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(970, 60, 190, 23);

        jLabel80.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel80.setText("Rp.");
        jLabel80.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel80.setName("jLabel80"); // NOI18N
        FormInput.add(jLabel80);
        jLabel80.setBounds(590, 400, 30, 30);

        jSeparator10.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator10.setName("jSeparator10"); // NOI18N
        FormInput.add(jSeparator10);
        jSeparator10.setBounds(20, 440, 1200, 1);

        jSeparator11.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator11.setName("jSeparator11"); // NOI18N
        FormInput.add(jSeparator11);
        jSeparator11.setBounds(20, 482, 1200, 1);

        jSeparator12.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator12.setName("jSeparator12"); // NOI18N
        FormInput.add(jSeparator12);
        jSeparator12.setBounds(20, 522, 1200, 1);

        jSeparator13.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator13.setName("jSeparator13"); // NOI18N
        FormInput.add(jSeparator13);
        jSeparator13.setBounds(20, 563, 1200, 1);

        jSeparator14.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator14.setName("jSeparator14"); // NOI18N
        FormInput.add(jSeparator14);
        jSeparator14.setBounds(20, 606, 1200, 1);

        jSeparator15.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator15.setName("jSeparator15"); // NOI18N
        FormInput.add(jSeparator15);
        jSeparator15.setBounds(20, 648, 1200, 1);

        jSeparator16.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator16.setName("jSeparator16"); // NOI18N
        FormInput.add(jSeparator16);
        jSeparator16.setBounds(20, 692, 1200, 1);

        jSeparator17.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator17.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator17.setName("jSeparator17"); // NOI18N
        FormInput.add(jSeparator17);
        jSeparator17.setBounds(800, 440, 1, 300);

        jSeparator18.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator18.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator18.setName("jSeparator18"); // NOI18N
        FormInput.add(jSeparator18);
        jSeparator18.setBounds(400, 440, 1, 300);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        ResponEklaim.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ResponEklaim.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        ResponEklaim.setName("ResponEklaim"); // NOI18N
        jScrollPane1.setViewportView(ResponEklaim);
        jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        FormInput.add(jScrollPane1);
        jScrollPane1.setBounds(20, 1060, 1200, 180);

        jLabel69.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel69.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel69.setText("Tarif INA-CBG :");
        jLabel69.setToolTipText("Total nilai tertagih pada perawatan dalam satu periode, tidak termasuk item tagihan pada tarif Non INA-CBG yang tersebut di bawah");
        jLabel69.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel69.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel69.setIconTextGap(8);
        jLabel69.setName("jLabel69"); // NOI18N
        FormInput.add(jLabel69);
        jLabel69.setBounds(30, 700, 130, 30);

        jLabel81.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel81.setText("Rp.");
        jLabel81.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel81.setName("jLabel81"); // NOI18N
        FormInput.add(jLabel81);
        jLabel81.setBounds(160, 700, 30, 30);

        jSeparator20.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator20.setName("jSeparator20"); // NOI18N
        FormInput.add(jSeparator20);
        jSeparator20.setBounds(20, 740, 1200, 1);

        jLabel74.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel74.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel74.setText("Data Grouper");
        jLabel74.setToolTipText("Total nilai tertagih pada perawatan dalam satu periode, tidak termasuk item tagihan pada tarif Non INA-CBG yang tersebut di bawah");
        jLabel74.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel74.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel74.setIconTextGap(8);
        jLabel74.setName("jLabel74"); // NOI18N
        FormInput.add(jLabel74);
        jLabel74.setBounds(430, 700, 130, 30);

        jLabel82.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel82.setText("Rp.");
        jLabel82.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel82.setName("jLabel82"); // NOI18N
        FormInput.add(jLabel82);
        jLabel82.setBounds(560, 700, 30, 30);

        jLabel53.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/question_mark24 (1).png"))); // NOI18N
        jLabel53.setText("Grouper Stage 2");
        jLabel53.setToolTipText("Total nilai tertagih pada perawatan dalam satu periode, tidak termasuk item tagihan pada tarif Non INA-CBG yang tersebut di bawah");
        jLabel53.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel53.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel53.setIconTextGap(8);
        jLabel53.setName("jLabel53"); // NOI18N
        FormInput.add(jLabel53);
        jLabel53.setBounds(820, 700, 140, 30);

        jLabel75.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel75.setText("Code CBG Grouping Tersimpan :");
        jLabel75.setToolTipText("Total nilai tertagih pada perawatan dalam satu periode, tidak termasuk item tagihan pada tarif Non INA-CBG yang tersebut di bawah");
        jLabel75.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel75.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel75.setIconTextGap(8);
        jLabel75.setName("jLabel75"); // NOI18N
        FormInput.add(jLabel75);
        jLabel75.setBounds(30, 750, 210, 30);

        jLabel83.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel83.setText("Deskripsi Grouping Tersimpan :");
        jLabel83.setToolTipText("Total nilai tertagih pada perawatan dalam satu periode, tidak termasuk item tagihan pada tarif Non INA-CBG yang tersebut di bawah");
        jLabel83.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel83.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel83.setIconTextGap(8);
        jLabel83.setName("jLabel83"); // NOI18N
        FormInput.add(jLabel83);
        jLabel83.setBounds(430, 750, 210, 30);

        labelDiagnosa.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDiagnosa.setToolTipText("");
        labelDiagnosa.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelDiagnosa.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        labelDiagnosa.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        labelDiagnosa.setIconTextGap(8);
        labelDiagnosa.setName("labelDiagnosa"); // NOI18N
        labelDiagnosa.setPreferredSize(new java.awt.Dimension(1000, 300));
        labelDiagnosa.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        FormInput.add(labelDiagnosa);
        labelDiagnosa.setBounds(40, 840, 560, 220);

        jSeparator21.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator21.setName("jSeparator21"); // NOI18N
        FormInput.add(jSeparator21);
        jSeparator21.setBounds(20, 790, 1200, 1);

        jSeparator19.setForeground(new java.awt.Color(204, 204, 204));
        jSeparator19.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator19.setName("jSeparator19"); // NOI18N
        FormInput.add(jSeparator19);
        jSeparator19.setBounds(620, 790, 1, 270);

        jLabel84.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel84.setText("Prosedur ( ICD-9-CM ) :");
        jLabel84.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel84.setName("jLabel84"); // NOI18N
        FormInput.add(jLabel84);
        jLabel84.setBounds(640, 800, 240, 26);

        labelProsedur.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelProsedur.setToolTipText("");
        labelProsedur.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelProsedur.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        labelProsedur.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        labelProsedur.setIconTextGap(8);
        labelProsedur.setName("labelProsedur"); // NOI18N
        labelProsedur.setPreferredSize(new java.awt.Dimension(1000, 300));
        labelProsedur.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        FormInput.add(labelProsedur);
        labelProsedur.setBounds(640, 840, 570, 220);

        scrollInput.setViewportView(FormInput);

        internalFrame1.add(scrollInput, java.awt.BorderLayout.CENTER);

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 9));

        hapusKlaim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        hapusKlaim.setMnemonic('K');
        hapusKlaim.setText("Hapus");
        hapusKlaim.setToolTipText("Alt+K");
        hapusKlaim.setIconTextGap(8);
        hapusKlaim.setName("hapusKlaim"); // NOI18N
        hapusKlaim.setPreferredSize(new java.awt.Dimension(80, 30));
        hapusKlaim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapusKlaimActionPerformed(evt);
            }
        });
        hapusKlaim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                hapusKlaimKeyPressed(evt);
            }
        });
        panelGlass8.add(hapusKlaim);

        editKlaim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        editKlaim.setText("Edit");
        editKlaim.setToolTipText("Alt+S");
        editKlaim.setIconTextGap(8);
        editKlaim.setName("editKlaim"); // NOI18N
        editKlaim.setPreferredSize(new java.awt.Dimension(65, 30));
        editKlaim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editKlaimActionPerformed(evt);
            }
        });
        panelGlass8.add(editKlaim);

        Riwayat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/copy.png"))); // NOI18N
        Riwayat.setText("ICD");
        Riwayat.setToolTipText("Alt+S");
        Riwayat.setIconTextGap(8);
        Riwayat.setName("Riwayat"); // NOI18N
        Riwayat.setPreferredSize(new java.awt.Dimension(65, 30));
        Riwayat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RiwayatActionPerformed(evt);
            }
        });
        panelGlass8.add(Riwayat);

        jSeparator4.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jSeparator4.setName("jSeparator4"); // NOI18N
        jSeparator4.setOpaque(true);
        jSeparator4.setPreferredSize(new java.awt.Dimension(1, 15));
        panelGlass8.add(jSeparator4);

        BtnGrouper.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/shuffle.png"))); // NOI18N
        BtnGrouper.setMnemonic('B');
        BtnGrouper.setText("Grouper");
        BtnGrouper.setToolTipText("Alt+B");
        BtnGrouper.setIconTextGap(8);
        BtnGrouper.setName("BtnGrouper"); // NOI18N
        BtnGrouper.setPreferredSize(new java.awt.Dimension(90, 30));
        BtnGrouper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGrouperActionPerformed(evt);
            }
        });
        BtnGrouper.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnGrouperKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnGrouper);

        finalklaim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/done.png"))); // NOI18N
        finalklaim.setText("Final");
        finalklaim.setToolTipText("Alt+S");
        finalklaim.setIconTextGap(8);
        finalklaim.setName("finalklaim"); // NOI18N
        finalklaim.setPreferredSize(new java.awt.Dimension(75, 30));
        finalklaim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finalklaimActionPerformed(evt);
            }
        });
        panelGlass8.add(finalklaim);

        kirimonline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        kirimonline.setText("Kirim Klaim Online ");
        kirimonline.setToolTipText("Alt+S");
        kirimonline.setIconTextGap(8);
        kirimonline.setName("kirimonline"); // NOI18N
        kirimonline.setPreferredSize(new java.awt.Dimension(150, 30));
        kirimonline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kirimonlineActionPerformed(evt);
            }
        });
        panelGlass8.add(kirimonline);

        jSeparator5.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jSeparator5.setName("jSeparator5"); // NOI18N
        jSeparator5.setOpaque(true);
        jSeparator5.setPreferredSize(new java.awt.Dimension(1, 15));
        panelGlass8.add(jSeparator5);

        ViewPDFBerkas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search.png"))); // NOI18N
        ViewPDFBerkas.setText("Berkas");
        ViewPDFBerkas.setToolTipText("Alt+S");
        ViewPDFBerkas.setIconTextGap(8);
        ViewPDFBerkas.setName("ViewPDFBerkas"); // NOI18N
        ViewPDFBerkas.setPreferredSize(new java.awt.Dimension(80, 30));
        ViewPDFBerkas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewPDFBerkasActionPerformed(evt);
            }
        });
        panelGlass8.add(ViewPDFBerkas);

        ViewPDFEklaim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Archive.png"))); // NOI18N
        ViewPDFEklaim.setText("Eklaim");
        ViewPDFEklaim.setToolTipText("Alt+S");
        ViewPDFEklaim.setIconTextGap(8);
        ViewPDFEklaim.setName("ViewPDFEklaim"); // NOI18N
        ViewPDFEklaim.setPreferredSize(new java.awt.Dimension(80, 30));
        ViewPDFEklaim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewPDFEklaimActionPerformed(evt);
            }
        });
        panelGlass8.add(ViewPDFEklaim);

        jSeparator6.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jSeparator6.setName("jSeparator6"); // NOI18N
        jSeparator6.setOpaque(true);
        jSeparator6.setPreferredSize(new java.awt.Dimension(1, 15));
        panelGlass8.add(jSeparator6);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setIconTextGap(8);
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
        panelGlass8.add(BtnKeluar);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnGrouperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGrouperActionPerformed
        if (stage_2.getSelectedItem() == null) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            klaimbaru();
            setdataklaim();
            grouper1();
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); 
        } else {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            grouper2();
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
}//GEN-LAST:event_BtnGrouperActionPerformed

    private void BtnGrouperKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnGrouperKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            emptTeks();
        }
}//GEN-LAST:event_BtnGrouperKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
       tampilkanDiagnosa();
       tampilkanProsedur();
    }//GEN-LAST:event_formWindowOpened

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=? ",TPasien,TNoRM.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
//            BtnSeekActionPerformed(null);
        }else{
            Valid.pindah(evt,cara_masuk,pnb);
        }
    }//GEN-LAST:event_TNoRMKeyPressed

    private void klaimbaru(){
        if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
        } else {
            try {
                // Create a human-readable JSON string
                    String jenisKelamin = jenis_kelamin.getText().trim();
                    int gender = 0;

                    switch (jenisKelamin) {
                        case "L":
                            gender = 1;
                            break;
                        case "P":
                            gender = 2;
                            break;
                        default:
                            JOptionPane.showMessageDialog(null, "Jenis kelamin tidak valid");
                            return;
                    }

                    String requestJson = "{\n" + 
                        " \"metadata\": {\n" + 
                        " \"method\": \"new_claim\"\n" + 
                        " },\n" + 
                        " \"data\": {\n" + 
                        " \"nomor_kartu\": \"" + no_kartu.getText().trim() + "\",\n" + 
                        " \"nomor_sep\": \"" + Tsep.getText().trim() + "\",\n" + 
                        " \"nomor_rm\": \"" + TNoRM.getText().trim() + "\",\n" + 
                        " \"nama_pasien\": \"" + TPasien.getText().trim() + "\",\n" + 
                        " \"tgl_lahir\": \"" + tgl_lahir.getText().trim() + " 00:00:00\",\n" + 
                        " \"gender\": \"" + gender + "\"\n" + 
                        " }\n" + 
                        "}";

                // Use the JSON string in your request
                String request = requestJson;
//                System.out.println("request JSON : "+requestJson);

                JSONObject response = inacbg.request(request);

                    JSONObject requestdata = new JSONObject(requestJson);
                    
                    String nomorRmResponse = requestdata.getJSONObject("data").getString("nomor_rm").trim().toUpperCase();
                    String nomorRmInput = TNoRM.getText().trim().toUpperCase();

                    String namaPasienResponse = requestdata.getJSONObject("data").getString("nama_pasien").trim().toUpperCase();
                    String namaPasienInput = TPasien.getText().trim().toUpperCase();
                    
                    String GenderResponse = requestdata.getJSONObject("data").getString("gender").trim().toUpperCase();
                    String GenderInput = jenis_kelamin.getText().trim().toUpperCase();

                    if (!nomorRmResponse.equals(nomorRmInput) || !namaPasienResponse.equals(namaPasienInput) || 
                        !(GenderResponse.equals("1") && GenderInput.equalsIgnoreCase("L")) && 
                        !(GenderResponse.equals("2") && GenderInput.equalsIgnoreCase("P"))) {
//                        System.out.println("Data pasien tidak sesuai");
//                        JOptionPane.showMessageDialog(null, "Data pasien tidak sesuai", "Pesan Kesalahan", JOptionPane.ERROR_MESSAGE);
                    } else {
//                        System.out.println("Data pasien sesuai");
//                        JOptionPane.showMessageDialog(null, "Data pasien sesuai", "Pesan Sukses", JOptionPane.INFORMATION_MESSAGE);
                    }

                    StyledDocument doc = ResponEklaim.getStyledDocument();
                    Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
                    StyleConstants.setForeground(styleRed, Color.RED);
                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                    StyleConstants.setBold(styleRed, true);
                    StyleConstants.setFontSize(styleRed, 12);

                    Style styleBlue = ResponEklaim.addStyle("BlueBoldStyle", null);
                    StyleConstants.setForeground(styleBlue, Color.BLUE);
                    StyleConstants.setFontFamily(styleBlue, "Tahoma");
                    StyleConstants.setBold(styleBlue, true);
                    StyleConstants.setFontSize(styleBlue, 12);

                    if (!nomorRmResponse.equals(nomorRmInput) || !namaPasienResponse.equals(namaPasienInput) || !String.valueOf(gender).equals(GenderResponse)) {
//                        System.out.println("Data pasien tidak sesuai");
                        // Tampilkan pesan kesalahan
                        StyleConstants.setForeground(styleRed, Color.RED);
                        doc.insertString(doc.getLength(), "Data Pasien = No. RM : " + nomorRmInput + ", ", styleRed);
                        doc.insertString(doc.getLength(), "Nama : " + namaPasienInput + ", ", styleRed);
                        String genderText = "";
                        if (GenderInput.equalsIgnoreCase("L")) {
                            genderText = "Laki-laki";
                        } else if (GenderInput.equalsIgnoreCase("P")) {
                            genderText = "Perempuan";
                        }
                        doc.insertString(doc.getLength(), "JK : " + genderText + "\n", styleRed);
                        doc.insertString(doc.getLength(), "Data Eklaim = No. RM : " + nomorRmResponse + ", ", styleRed);
                        doc.insertString(doc.getLength(), "Nama : " + namaPasienResponse  + ", ", styleRed);
                        if (GenderResponse.equals("1")) {
                            genderText = "Laki-laki";
                        } else if (GenderResponse.equals("2")) {
                            genderText = "Perempuan";
                        }
                        doc.insertString(doc.getLength(), "JK : " + genderText + "\n", styleRed);
                        doc.insertString(doc.getLength(), "Data pasien dikirim dengan data eklaim tidak sesuai\n", styleRed);
                    } else {
//                        System.out.println("Data pasien sesuai");
                        // Tampilkan pesan sukses
                        StyleConstants.setForeground(styleBlue, Color.BLUE);
                        doc.insertString(doc.getLength(), "Data Pasien = No. RM : " + nomorRmInput + ", ", styleBlue);
                        doc.insertString(doc.getLength(), "Nama : " + namaPasienInput + ", ", styleBlue);
                        String genderText = "";
                        if (GenderInput.equalsIgnoreCase("L")) {
                            genderText = "Laki-laki";
                        } else if (GenderInput.equalsIgnoreCase("P")) {
                            genderText = "Perempuan";
                        }
                        doc.insertString(doc.getLength(), "JK : " + genderText + "\n", styleBlue);
                        doc.insertString(doc.getLength(), "Data Eklaim = No. RM : " + nomorRmResponse + ", ", styleBlue);
                        doc.insertString(doc.getLength(), "Nama : " + namaPasienResponse + ", ", styleBlue);                        
                        if (GenderResponse.equals("1")) {
                            genderText = "Laki-laki";
                        } else if (GenderResponse.equals("2")) {
                            genderText = "Perempuan";
                        }
                        doc.insertString(doc.getLength(), "JK : " + genderText + "\n", styleBlue);
                        doc.insertString(doc.getLength(), "Data pasien sesuai\n", styleBlue);
                    }
//                    ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
                    
//                System.out.println("respon klaimbaru :\n "+response);

                    // Clear the document before inserting new text
//                    doc.remove(0, doc.getLength());

                    // Prepare the response text
                    String responseText = "Klaim Baru\n ";
                    String responseContent = response.toString() + "\n";
                    String[] words = responseContent.split(" ");

                    // Insert the "Status :" text with blue style
                    doc.insertString(doc.getLength(), responseText, styleBlue);

                    // Insert the remaining response content with no style
//                    doc.insertString(doc.getLength(), responseContent, null);
                  
                        // Check for tariff and base_tariff mismatch and highlight it
                        if (response.has("response")) {
                            JSONObject data = response.getJSONObject("response");
                            if (data.has("cbg")) {
                                JSONObject cbg = data.getJSONObject("cbg");
                                double tariff = cbg.getDouble("tariff");
                                double baseTariff = cbg.getDouble("base_tariff");
                                if (tariff != baseTariff) {
                                    StyleConstants.setForeground(styleRed, Color.RED);
                                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                                    StyleConstants.setBold(styleRed, true);
                                    StyleConstants.setFontSize(styleRed, 12);
//                                    doc.remove(0, doc.getLength());
                                    doc.insertString(doc.getLength(), "Tarif Grouper INACBG " + cbg.getString("tariff") + " tidak sama dengan base tarif: " + cbg.getString("base_tariff") + " Cek dan/atau kirim data via E-Klaim\n", styleRed);
                                }
                            }
                        }

                    // Insert words with specific style conditions
                    for (String word : words) {
                        if (word.contains("Ungroupable") || word.contains("Unrelated") || word.contains("not accepted as Primary Diagnosis")) {
                            doc.insertString(doc.getLength(), word + " ", styleRed);
                        } else {
                            doc.insertString(doc.getLength(), word + " ", null);
                        }
                    }
                if(response.has("metadata")){
                    JSONObject code = response.getJSONObject("metadata");
                    if(code.getInt("code")==200){
                        JSONObject data = response.getJSONObject("response");
                        if (data.has("patient_id")) {
                            if(Sequel.menyimpantf2("inacbg_klaim_baru","?,?,?,?","EKLAIM",4,new String[]{
                               Tsep.getText(),data.getInt("patient_id")+"",data.getInt("admission_id")+"",data.getInt("hospital_admission_id")+""
                            })==false){
                                Sequel.mengedit("inacbg_klaim_baru", "no_sep='"+Tsep.getText()+"'", "patient_id='"+data.getInt("patient_id")+"',admission_id='"+data.getInt("admission_id")+"',hospital_admission_id='"+data.getInt("hospital_admission_id")+"'");
                            }
                        }
//                        System.out.println("RESPON :\n "+code.getString("message"));
                    }else{
                        if (!code.getString("message").contains("Duplikasi")) {
                            JOptionPane.showMessageDialog(null,"RESPON :\n "+code.getString("message"));
                        }
                    }
                } else {
                    System.out.println("response :\n "+response); 
                    System.err.println("\n The 'data' field is missing in the response.");
                }
                ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
            } catch (Exception e) {
                System.err.println("Error in sitb: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void setdataklaim(){
        if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
        } else {            
            switch (jaminan.getSelectedIndex()) {
                case 0:
                    payor_id = 3+"";
                    payor_cd = "JKN";
                    break;
                case 1:
                    payor_id = 71+"";
                    payor_cd = "COVID-19";
                    break;
                case 2:
                    payor_id = 72+"";
                    payor_cd = "KIPI";
                    break;
                case 3:
                    payor_id = 73+"";
                    payor_cd = "BBL";
                    break;
                case 4:
                    payor_id = 74+"";
                    payor_cd = "PMR";
                    break;
                case 5:
                    payor_id = 75+"";
                    payor_cd = "CO-INS";
                    break;
                case 6:
                    payor_id = 76+"";
                    payor_cd = "JPS";
                    break;
                case 7:
                    payor_id = 77+"";
                    payor_cd = "JPKP";
                    break;
                default:
                    payor_id = 3+"";
                    payor_cd = "JKN";
                    break;
            }
            try {
//                
                String requestJson =   "{\n"+
                                        "  \"metadata\": {\n" +
                                        "        \"method\": \"set_claim_data\",\n" +
                                        "        \"nomor_sep\": \""+Tsep.getText()+"\"\n" +
                                        "    },\n" +
                                        "    \"data\": {\n" +
                                        "        \"nomor_sep\": \""+Tsep.getText()+"\",\n" +
                                        "        \"nomor_kartu\": \""+no_kartu.getText()+"\",\n" +
                                        "        \"tgl_masuk\": \""+tgl_masuk.getSelectedItem()+" "+jam_masuk.getSelectedItem()+":"+menit_masuk.getSelectedItem()+":00\",\n" +
                                        "        \"tgl_pulang\": \""+tgl_keluar.getSelectedItem()+" "+jam_keluar.getSelectedItem()+":"+menit_keluar.getSelectedItem()+":00\",\n" +
                                        "        \"cara_masuk\": \""+cara_masuk.getSelectedItem().toString().split(":")[0].trim()+"\",\n" +
                                        "        \"jenis_rawat\": \""+jenis_rawat.getSelectedItem().toString().split(":")[0].trim()+"\",\n" +
                                        "        \"kelas_rawat\": \""+hak_kelas.getSelectedItem().toString().split(" ")[1].trim()+"\",\n" +
                                        "        \"adl_sub_acute\": \""+sub_acute.getText()+"\",\n" +
                                        "        \"adl_chronic\": \""+chronic.getText()+"\",\n" +
                                        "        \"icu_indikator\": \""+(rawat_intensif.isSelected() == true ? 1 : 0)+"\",\n" +
                                                 
                                        (rawat_intensif.isSelected() == true ? 
                                        "        \"icu_los\": \""+hari_intensif.getText()+"\",\n" +
                                        "        \"ventilator_hour\": \""+jam_ventilator.getText()+"\",\n" +
                                        "        \"ventilator\": {\n" +
                                        "            \"use_ind\": \""+(ventilator.isSelected()==true ? 1 : 0)+"\",\n" +
                                        "            \"start_dttm\": \""+tgl_intubasi.getSelectedItem()+" "+jam_intubasi.getSelectedItem()+":"+menit_intubasi.getSelectedItem()+":00\",\n" +
                                        "            \"stop_dttm\": \""+tgl_ekstubasi.getSelectedItem()+" "+jam_ekstubasi.getSelectedItem()+":"+menit_ekstubasi.getSelectedItem()+":00\"\n" +
                                        "        },\n"  
                                        : "") + 
                        
                        
                                        "        \"upgrade_class_ind\": \""+(naik_turun_kelas.isSelected()==true ? 1 : 0)+"\",\n" +
                        
                                        (naik_turun_kelas.isSelected()==true ? 
                                        "        \"upgrade_class_class\": \""+naik_kelas_pelayanan.getSelectedItem().toString().split(":")[0].trim()+"\",\n" + 
                                        "        \"upgrade_class_los\": \""+lama_naik_kelas.getText().trim()+"\",\n" +
                                        "        \"upgrade_class_payor\": \"peserta\",\n" +
                                        "        \"add_payment_pct\": \"35\",\n" 
                                        : "") +
                                        
                                        "        \"sistole\": \""+sistole.getText()+"\",\n" +
                                        "        \"diastole\": \""+distole.getText()+"\",\n" +
                                        "        \"discharge_status\": \""+status_pulang.getSelectedItem().toString().split(":")[0].trim()+"\",\n" +
                                        
                                        (check_hd.isSelected()==true ? 
                                        "        \"dializer_single_use\": \""+pasien_hd.getSelectedIndex()+"\",\n"                       
//                                        "        \"dializer_single_use\": \""+pasien_hd.getSelectedIndex()+"\",\n" +
//                                        "        \"kantong_darah\": \""+kt_darah.getText().trim()+"\",\n" 
                                        : "") +
                        
                                        "        \"birth_weight\": \""+berat_bayi.getText()+"\",\n" +
                                        "        \"diagnosa\": \""+icd10.getText()+"\",\n" +
                                        "        \"procedure\": \"" + (icd9.getText().trim().isEmpty() ? "#" : icd9.getText()) + "\",\n" +
                                        "        \"diagnosa_inagrouper\": \""+icd10.getText()+"\",\n" +
                                        "        \"procedure_inagrouper\": \""+ (icd9.getText().trim().isEmpty() ? "#" : icd9.getText()) + "\",\n" +
                                        "        \"tarif_rs\": {\n" +
                                        "            \"prosedur_non_bedah\": \""+pnb.getText()+"\",\n" +
                                        "            \"prosedur_bedah\": \""+pb.getText()+"\",\n" +
                                        "            \"konsultasi\": \""+ksl.getText()+"\",\n" +
                                        "            \"tenaga_ahli\": \""+ahli.getText()+"\",\n" +
                                        "            \"keperawatan\": \""+kpr.getText()+"\",\n" +
                                        "            \"penunjang\": \""+pnj.getText()+"\",\n" +
                                        "            \"radiologi\": \""+rad.getText()+"\",\n" +
                                        "            \"laboratorium\": \""+lab.getText()+"\",\n" +
                                        "            \"pelayanan_darah\": \""+pl_darah.getText()+"\",\n" +
                                        "            \"rehabilitasi\": \""+rehab.getText()+"\",\n" +
                                        "            \"kamar\": \""+akomodasi.getText()+"\",\n" +
                                        "            \"rawat_intensif\": \""+ri.getText()+"\",\n" +
                                        "            \"obat\": \""+obat.getText()+"\",\n" +
                                        "            \"obat_kronis\": \""+kronis.getText()+"\",\n" +
                                        "            \"obat_kemoterapi\": \""+obat_kemo.getText()+"\",\n" +
                                        "            \"alkes\": \""+alkes.getText()+"\",\n" +
                                        "            \"bmhp\": \""+bmhp.getText()+"\",\n" +
                                        "            \"sewa_alat\": \""+sewa_alat.getText()+"\"\n" +
                                        "        },\n" +
//                                        "        \"pemulasaraan_jenazah\": \"1\",\n" +
//                                        "        \"kantong_jenazah\": \"1\",\n" +
//                                        "        \"peti_jenazah\": \"1\",\n" +
//                                        "        \"plastik_erat\": \"1\",\n" +
//                                        "        \"desinfektan_jenazah\": \"1\",\n" +
//                                        "        \"mobil_jenazah\": \"0\",\n" +
//                                        "        \"desinfektan_mobil_jenazah\": \"0\",\n" +
//                                        "        \"covid19_status_cd\": \"1\",\n" +
//                                        "        \"nomor_kartu_t\": \"nik\",\n" +
//                                        "        \"episodes\": \"1;12#2;3#6;5\",\n" +
//                                        "        \"covid19_cc_ind\": \"1\",\n" +
//                                        "        \"covid19_rs_darurat_ind\": \"1\",\n" +
//                                        "        \"covid19_co_insidense_ind\": \"1\",\n" +
//                                        "        \"covid19_penunjang_pengurang\": {\n" +
//                                        "            \"lab_asam_laktat\": \"1\",\n" +
//                                        "            \"lab_procalcitonin\": \"1\",\n" +
//                                        "            \"lab_crp\": \"1\",\n" +
//                                        "            \"lab_kultur\": \"1\",\n" +
//                                        "            \"lab_d_dimer\": \"1\",\n" +
//                                        "            \"lab_pt\": \"1\",\n" +
//                                        "            \"lab_aptt\": \"1\",\n" +
//                                        "            \"lab_waktu_pendarahan\": \"1\",\n" +
//                                        "            \"lab_anti_hiv\": \"1\",\n" +
//                                        "            \"lab_analisa_gas\": \"1\",\n" +
//                                        "            \"lab_albumin\": \"1\",\n" +
//                                        "            \"rad_thorax_ap_pa\": \"0\"\n" +
//                                        "        },\n" +
//                                        "        \"terapi_konvalesen\": \"1000000\",\n" +
//                                        "        \"akses_naat\": \"C\",\n" +
//                                        "        \"isoman_ind\": \"0\",\n" +
//                                        "        \"bayi_lahir_status_cd\": 1,\n" +
//                                        "        \"alteplase_ind\": 0,\n" +
//                                        "        \"apgar\": {\n" +
//                                        "            \"menit_1\": {\n" +
//                                        "                \"appearance\": "+ap_1.getText()+",\n" +
//                                        "                \"pulse\": "+ap_2.getText()+",\n" +
//                                        "                \"grimace\": "+ap_3.getText()+",\n" +
//                                        "                \"activity\": "+ap_4.getText()+",\n" +
//                                        "                \"respiration\": "+ap_5.getText()+"\n" +
//                                        "            },\n" +
//                                        "            \"menit_5\": {\n" +
//                                        "                \"appearance\": "+ap_6.getText()+",\n" +
//                                        "                \"pulse\": "+ap_7.getText()+",\n" +
//                                        "                \"grimace\": "+ap_8.getText()+",\n" +
//                                        "                \"activity\": "+ap_9.getText()+",\n" +
//                                        "                \"respiration\": "+ap_10.getText()+"\n" +
//                                        "            }\n" +
//                                        "        },\n" +
//                                        "        \"persalinan\": {\n" +
//                                        "            \"usia_kehamilan\": \"22\",\n" +
//                                        "            \"gravida\": \"2\",\n" +
//                                        "            \"partus\": \"4\",\n" +
//                                        "            \"abortus\": \"2\",\n" +
//                                        "            \"onset_kontraksi\": \"induksi\",\n" +
//                                        "            \"delivery\": [\n" +
//                                        "                {\n" +
//                                        "                    \"delivery_sequence\": \"1\",\n" +
//                                        "                    \"delivery_method\": \"vaginal\",\n" +
//                                        "                    \"delivery_dttm\": \"2023-01-21 17:01:33\",\n" +
//                                        "                    \"letak_janin\": \"kepala\",\n" +
//                                        "                    \"kondisi\": \"livebirth\",\n" +
//                                        "                    \"use_manual\": \"1\",\n" +
//                                        "                    \"use_forcep\": \"0\",\n" +
//                                        "                    \"use_vacuum\": \"1\",\n" +
//                                        "                    \"shk_spesimen_ambil\": \"ya\",\n" +
//                                        "                    \"shk_lokasi\": \"tumit\",\n" +
//                                        "                    \"shk_spesimen_dttm\": \"2023-01-21 18:11:33\"\n" +
//                                        "                },\n" +
//                                        "                {\n" +
//                                        "                    \"delivery_sequence\": \"2\",\n" +
//                                        "                    \"delivery_method\": \"vaginal\",\n" +
//                                        "                    \"delivery_dttm\": \"2023-01-21 17:03:49\",\n" +
//                                        "                    \"letak_janin\": \"lintang\",\n" +
//                                        "                    \"kondisi\": \"livebirth\",\n" +
//                                        "                    \"use_manual\": \"1\",\n" +
//                                        "                    \"use_forcep\": \"0\",\n" +
//                                        "                    \"use_vacuum\": \"0\",\n" +
//                                        "                    \"shk_spesimen_ambil\": \"tidak\",\n" +
//                                        "                    \"shk_alasan\": \"akses-sulit\"\n" +
//                                        "                }\n" +
//                                        "            ]\n" +
//                                        "        },\n" +
                                        "        \"tarif_poli_eks\": \""+(tarif_eksekutif.getText().equals(0) ? 0 : tarif_eksekutif.getText())+"\",\n" +
//                                        "        \"nama_dokter\": \""+(dpjp.getText().trim().isEmpty() ? "" : dpjp.getText())+"\",\n" +
                                        "        \"nama_dokter\": \""+dpjp.getText()+"\",\n" +                        
                                        "        \"kode_tarif\": \""+koneksiDB.KELASRSEKLAIM()+"\",\n" +
                                        "        \"payor_id\": \""+payor_id+"\",\n" +
                                        "        \"payor_cd\": \""+payor_cd+"\",\n" +
                                        "        \"cob_cd\": \""+cob.getSelectedItem().toString().split(":")[0].trim()+"\",\n" +
                                        "        \"coder_nik\": \""+coder+"\"\n" +
                                        "    \n}"
                                    + " }\n";

                // Use the JSON string in your request
                String request = requestJson;
//                System.out.println("request JSON : "+requestJson);

                JSONObject response = inacbg.request(request);
//                System.out.println("response data klaim :\n "+response); 

                    JSONObject data = new JSONObject(requestJson);
                    String nomorKartuResponse = data.getJSONObject("data").getString("nomor_kartu").trim().toUpperCase();

                    String nomorKartuInput = no_kartu.getText().trim().toUpperCase();

                    if (!nomorKartuResponse.equals(nomorKartuInput)) {
                        // Kedua string tidak sama
//                        System.out.println("Data pasien tidak sesuai");
                        // Tambahkan kode untuk menampilkan pesan kesalahan
                    } else {
                        // Kedua string sama
//                        System.out.println("Data pasien sesuai");
                        // Tambahkan kode untuk menampilkan pesan sukses
                    }

                    StyledDocument doc = ResponEklaim.getStyledDocument();
                    Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
                    StyleConstants.setForeground(styleRed, Color.RED);
                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                    StyleConstants.setBold(styleRed, true);
                    StyleConstants.setFontSize(styleRed, 12);

                    Style styleBlue = ResponEklaim.addStyle("BlueBoldStyle", null);
                    StyleConstants.setForeground(styleBlue, Color.BLUE);
                    StyleConstants.setFontFamily(styleBlue, "Tahoma");
                    StyleConstants.setBold(styleBlue, true);
                    StyleConstants.setFontSize(styleBlue, 12);

                    if (!nomorKartuResponse.equals(nomorKartuInput)) {
                        // Kedua string tidak sama
//                        System.out.println("Data pasien tidak sesuai");
                        StyleConstants.setForeground(styleRed, Color.RED);
                        doc.insertString(doc.getLength(), "Nomor Kartu Pasien: " + nomorKartuInput + "\n", styleRed);
                        doc.insertString(doc.getLength(), "Nomor Kartu Eklaim: " + nomorKartuResponse + "\n", styleRed);
                        doc.insertString(doc.getLength(), "Data pasien dikirim dengan data eklaim tidak sesuai\n", styleRed);
                    } else {
                        // Kedua string sama
//                        System.out.println("Data pasien sesuai");
                        StyleConstants.setForeground(styleBlue, Color.BLUE);
                        doc.insertString(doc.getLength(), "Nomor Kartu Pasien: " + nomorKartuInput + "\n", styleBlue);
                        doc.insertString(doc.getLength(), "Nomor Kartu Eklaim: " + nomorKartuResponse + "\n", styleBlue);
                        doc.insertString(doc.getLength(), "Data pasien sesuai\n", styleBlue);
                    }
                    ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());

                if(response.has("metadata")){
                    JSONObject code = response.getJSONObject("metadata");
                    if(code.getInt("code")==200){
//                        JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
//                        if(Sequel.menyimpantf2("inacbg_data_terkirim","?,?","DATA EKLAIM",2,new String[]{
//                               Tsep.getText(),coder
//                        })==false){
//                            Sequel.mengedit("inacbg_data_terkirim", "no_sep='"+Tsep.getText()+"'", "nik='"+coder+"'");
//                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"RESPON :\n "+code.getString("message"));
                    }
                } else {
                    System.out.println("response :\n "+response); 
                    System.err.println("\n The 'data' field is missing in the response.");
                }
                
            } catch (Exception e) {
                System.err.println("Error in sitb: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
        
    private void grouper1(){
        if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
        } else {
            try {
                // Create a human-readable JSON string
                String requestJson ="{\n" +
                                        "\"metadata\": {\n" +
                                            "\"method\":\"grouper\",\n" +
                                            "\"stage\":\"1\"\n" +
                                        "},\n" +
                                        "\"data\": {\n" +
                                            "\"nomor_sep\":\""+Tsep.getText().trim()+"\"\n" +
                                        "}\n" +
                                    "}";

                // Use the JSON string in your request
                String request = requestJson;
//                System.out.println("request JSON : "+requestJson);
                JSONObject response = inacbg.request(request);
                JSONObject code = response.getJSONObject("metadata");
//                System.out.println("response grouper1 :\n "+response);
                    StyledDocument doc = ResponEklaim.getStyledDocument();
                    Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
                    StyleConstants.setForeground(styleRed, Color.RED);
                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                    StyleConstants.setBold(styleRed, true);
                    StyleConstants.setFontSize(styleRed, 12);

                    // Clear the document before inserting new text
//                    doc.remove(0, doc.getLength());
                    
                    Style styleBlue = ResponEklaim.addStyle("BlueBoldStyle", null);
                    StyleConstants.setForeground(styleBlue, Color.BLUE);
                    StyleConstants.setFontFamily(styleBlue, "Tahoma");
                    StyleConstants.setBold(styleBlue, true);
                    StyleConstants.setFontSize(styleBlue, 12);

                    // Clear the document before inserting new text
//                    doc.remove(0, doc.getLength());

                    // Prepare the response text
                    String responseText = "Grouper 1\n ";
                    String responseContent = response.toString() + "\n";
                    String[] words = responseContent.split(" ");

                    // Insert the "Status :" text with blue style
                    doc.insertString(doc.getLength(), responseText, styleBlue);

                    // Insert the remaining response content with no style
//                    doc.insertString(doc.getLength(), responseContent, null);
                  
                        // Check for tariff and base_tariff mismatch and highlight it
                        if (response.has("response")) {
                            JSONObject data = response.getJSONObject("response");
                            if (data.has("cbg")) {
                                JSONObject cbg = data.getJSONObject("cbg");
                                double tariff = cbg.getDouble("tariff");
                                double baseTariff = cbg.getDouble("base_tariff");
                                if (tariff != baseTariff) {
                                    StyleConstants.setForeground(styleRed, Color.RED);
                                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                                    StyleConstants.setBold(styleRed, true);
                                    StyleConstants.setFontSize(styleRed, 12);
//                                    doc.remove(0, doc.getLength());
                                    doc.insertString(doc.getLength(), "Tarif Grouper INACBG " + cbg.getString("tariff") + " tidak sama dengan base tarif: " + cbg.getString("base_tariff") + " Cek dan/atau kirim data via E-Klaim\n", styleRed);
                                }
                            }
                        }

                    // Insert words with specific style conditions
                    for (String word : words) {
                        if (word.contains("Ungroupable") || word.contains("Unrelated") || word.contains("not accepted as Primary Diagnosis")) {
                            doc.insertString(doc.getLength(), word + " ", styleRed);
                        } else {
                            doc.insertString(doc.getLength(), word + " ", null);
                        }
                    }       
                if(response.has("metadata")){
                    if(code.getInt("code")==200){
                        JSONObject data = response.getJSONObject("response");
                        JSONObject trf = data.getJSONObject("cbg");
                        if (trf.has("tariff")) {
                            if(Sequel.menyimpantf2("inacbg_grouping_stage1","?,?,?,?","GROUPER EKLAIM",4,new String[]{
                               Tsep.getText(),trf.getString("code"),trf.getString("description"),trf.getDouble("tariff")+""
                            })==false){
                                Sequel.mengedit("inacbg_grouping_stage1", "no_sep='"+Tsep.getText()+"'", "code_cbg='"+trf.getString("code")+"',deskripsi='"+trf.getString("description")+"',tarif='"+trf.getDouble("tariff")+"'");
                            }
                            total_rupiah1.setText(Valid.SetAngka(trf.getDouble("tariff"))); 
                                if (trf.getDouble("tariff") < total_biaya) { 
                                    total_rupiah1.setForeground(Color.RED); 
                                }                            
//                            total_rupiah1.setText(Valid.SetAngka(trf.getDouble("tariff")));
//                            CodeInaCBG.setText(code.getString("code"));
//                            DeskripsiInaCBG.setText(data.getString("description"));
//                            RupiahInaCBG.setText(trf.getString("tariff"));
                        }else{
                            JOptionPane.showMessageDialog(null,"PASIEN UNGROUPABLE (GAGAL GROUPER)");
                        }
                        totalGroupingTersimpan(Tsep.getText());
                        if(response.has("special_cmg_option")){
                            System.out.println("spesial :\n "+response.getJSONArray("special_cmg_option"));
                            JSONArray spesial = response.getJSONArray("special_cmg_option");
                            jLabel49.setVisible(true);
                            stage_2.setVisible(true);
                            jLabel53.setVisible(true);
                            jLabel61.setVisible(true);                
                            jLabel76.setVisible(true);
                            
                            for (int i = 0; i < spesial.length(); i++) {
                                JSONObject cmg = spesial.getJSONObject(i);
                                stage_2.addItem(cmg.getString("code")+" : "+cmg.getString("description"));
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"RESPON :\n "+code.getString("message"));
                    }
                    totalGroupingTersimpan(Tsep.getText());
                } else {
                    System.out.println("response :\n "+response); 
                    System.err.println("\n The 'data' field is missing in the response.");
                }
                ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
            } catch (Exception e) {
                System.err.println("Error in sitb: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void grouper2(){
        if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
        } else {
            try {
                // Create a human-readable JSON string
                String requestJson ="{\n" +
                                        "\"metadata\": {\n" +
                                            "\"method\":\"grouper\",\n" +
                                            "\"stage\":\"2\"\n" +
                                        "},\n" +
                                        "\"data\": {\n" +
                                            "\"nomor_sep\":\""+Tsep.getText().trim()+"\",\n" +
                                            "\"special_cmg\": \""+stage_2.getSelectedItem().toString().split(":")[0].trim()+"\"\n" +
                                        "}\n" +
                                    "}";


                // Use the JSON string in your request
                String request = requestJson;
//                System.out.println("request JSON : "+requestJson);
                JSONObject response = inacbg.request(request);
                JSONObject code = response.getJSONObject("metadata");
//                System.out.println("response grouper2 :\n "+response);
                    StyledDocument doc = ResponEklaim.getStyledDocument();
                    Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
                    StyleConstants.setForeground(styleRed, Color.RED);
                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                    StyleConstants.setBold(styleRed, true);
                    StyleConstants.setFontSize(styleRed, 12);
                    
                    Style styleBlue = ResponEklaim.addStyle("BlueBoldStyle", null);
                    StyleConstants.setForeground(styleBlue, Color.BLUE);
                    StyleConstants.setFontFamily(styleBlue, "Tahoma");
                    StyleConstants.setBold(styleBlue, true);
                    StyleConstants.setFontSize(styleBlue, 12);

                    // Clear the document before inserting new text
//                    doc.remove(0, doc.getLength());

                    // Prepare the response text
                    String responseText = "Grouper 2\n ";
                    String responseContent = response.toString() + "\n";
                    String[] words = responseContent.split(" ");

                    // Insert the "Status :" text with blue style
                    doc.insertString(doc.getLength(), responseText, styleBlue);

                    // Insert the remaining response content with no style
 //                   doc.insertString(doc.getLength(), responseContent, null);
                  
                        // Check for tariff and base_tariff mismatch and highlight it
                        if (response.has("response")) {
                            JSONObject data = response.getJSONObject("response");
                            if (data.has("cbg")) {
                                JSONObject cbg = data.getJSONObject("cbg");
                                double tariff = cbg.getDouble("tariff");
                                double baseTariff = cbg.getDouble("base_tariff");
                                if (tariff != baseTariff) {
                                    StyleConstants.setForeground(styleBlue, Color.BLUE);
                                    StyleConstants.setFontFamily(styleBlue, "Tahoma");
                                    StyleConstants.setBold(styleBlue, true);
                                    StyleConstants.setFontSize(styleBlue, 12);
//                                    doc.remove(0, doc.getLength());
                                    doc.insertString(doc.getLength(), "Tarif Total Prosedur Operasi Katarak " + cbg.getString("tariff") + " dibanding base tarif: " + cbg.getString("base_tariff") + "\n", styleBlue);
                                }
                            }
                        }

                    // Insert words with specific style conditions
                    for (String word : words) {
                        if (word.contains("Ungroupable") || word.contains("Unrelated") || word.contains("not accepted as Primary Diagnosis")) {
                            doc.insertString(doc.getLength(), word + " ", styleRed);
                        } else {
                            doc.insertString(doc.getLength(), word + " ", null);
                        }
                    }                
                if(response.has("metadata")){
                    if(code.getInt("code")==200){
                        JSONObject data = response.getJSONObject("response");
                        JSONObject trf = data.getJSONObject("cbg");
//                        System.out.println("resdata : "+trf);
                        if (trf.has("tariff")) {
                            if(Sequel.menyimpantf2("inacbg_grouping_stage2","?,?,?,?","GROUPER EKLAIM",4,new String[]{
                               Tsep.getText(),trf.getString("code"),trf.getString("description"),trf.getDouble("tariff")+""
                            })==false){
                                Sequel.mengedit("inacbg_grouping_stage2", "no_sep='"+Tsep.getText()+"'", "code_cbg='"+trf.getString("code")+"',deskripsi='"+trf.getString("description")+"',tarif='"+trf.getDouble("tariff")+"'");
                            }
                            total_rupiah1.setText(Valid.SetAngka(trf.getDouble("tariff"))); 
                                if (trf.getDouble("tariff") < total_biaya) { 
                                    total_rupiah1.setForeground(Color.RED); 
                                }
//                            total_rupiah1.setText(Valid.SetAngka(trf.getDouble("tariff")));
//                            CodeInaCBG.setText(code.getString("code"));
//                            DeskripsiInaCBG.setText(data.getString("description"));
//                            RupiahInaCBG.setText(trf.getString("tariff"));                            
                        }
                        totalGroupingTersimpan(Tsep.getText());
                    }else{
                        System.out.println("response :\n "+response);
                    }                  
                } else {
                    System.out.println("response :\n "+response); 
                    System.err.println("\n The 'data' field is missing in the response.");
                }
                ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
            } catch (Exception e) {
                System.err.println("Error in sitb: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void validasi_cvd19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validasi_cvd19ActionPerformed
        
    }//GEN-LAST:event_validasi_cvd19ActionPerformed

    private void jenis_rawatItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jenis_rawatItemStateChanged
        // TODO add your handling code here:
        if (jenis_rawat.getSelectedIndex() == 0) {
            naik_turun_kelas.setVisible(true);
            rawat_intensif.setVisible(true);
            kelas_eksekutif.setVisible(false);
            jLabel58.setVisible(true);
            jLabel22.setVisible(true);
            validasi_cvd19.setVisible(true);
            validasi_tb.setVisible(true);
            pasien_tb.setVisible(true);
            co_covid_19.setVisible(true);
            kelas_eksekutif.setSelected(false);
            jLabel62.setVisible(false);
            naik_kelas_pelayanan.setVisible(false);
            jLabel26.setVisible(false);
        } else {
            naik_turun_kelas.setVisible(false);
            rawat_intensif.setVisible(false);
            kelas_eksekutif.setVisible(true);
            jLabel58.setVisible(false);
            jLabel22.setVisible(true);
            validasi_cvd19.setVisible(false);
            validasi_tb.setVisible(true);
            pasien_tb.setVisible(true);
            co_covid_19.setVisible(false);
            kelas_eksekutif.setSelected(false);
            jLabel62.setVisible(true);
            naik_kelas_pelayanan.setVisible(true);
            jLabel26.setVisible(true);
        }
    }//GEN-LAST:event_jenis_rawatItemStateChanged

    private void rawat_intensifItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rawat_intensifItemStateChanged
        // TODO add your handling code here:
        if (rawat_intensif.isSelected() == true) {
            ventilator.setVisible(true);
            jLabel63.setVisible(true);
            hari_intensif.setVisible(true);
            ventilatorItemStateChanged(null);
        } else {
            ventilator.setVisible(false);
            jLabel63.setVisible(false);
            hari_intensif.setVisible(false);
            ventilatorItemStateChanged(null);
        }
    }//GEN-LAST:event_rawat_intensifItemStateChanged

    private void naik_turun_kelasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_naik_turun_kelasItemStateChanged
        // TODO add your handling code here:
        if (naik_turun_kelas.isSelected() == true) {
           jLabel62.setVisible(true);
           naik_kelas_pelayanan.setVisible(true);
           jLabel19.setVisible(true);
           jLabel63.setVisible(true);
           lama_naik_kelas.setVisible(true);
        } else {
           jLabel62.setVisible(false);
           naik_kelas_pelayanan.setVisible(false);
           jLabel63.setVisible(false);
           jLabel19.setVisible(false);
           lama_naik_kelas.setVisible(false); 
        }
    }//GEN-LAST:event_naik_turun_kelasItemStateChanged

    private void kelas_eksekutifItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_kelas_eksekutifItemStateChanged
        // TODO add your handling code here:
        if (kelas_eksekutif.isSelected() == true) {
           jLabel13.setVisible(true);
           tarif_eksekutif.setVisible(true);
        } else {
           jLabel13.setVisible(false);
           tarif_eksekutif.setVisible(false);
        }
    }//GEN-LAST:event_kelas_eksekutifItemStateChanged

    private void ventilatorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ventilatorItemStateChanged
        // TODO add your handling code here:
        if (ventilator.isSelected() == true) {
            tgl_intubasi.setVisible(true);
            jam_intubasi.setVisible(true);
            menit_intubasi.setVisible(true);
            tgl_ekstubasi.setVisible(true);
            jam_ekstubasi.setVisible(true);
            menit_ekstubasi.setVisible(true);
            jLabel46.setVisible(true);
            jLabel62.setVisible(true);
            jLabel65.setVisible(true);
            jam_ventilator.setVisible(true);
        } else {
            tgl_intubasi.setVisible(false);
            jam_intubasi.setVisible(false);
            menit_intubasi.setVisible(false);
            tgl_ekstubasi.setVisible(false);
            jam_ekstubasi.setVisible(false);
            menit_ekstubasi.setVisible(false);
            jLabel46.setVisible(false);
            jLabel62.setVisible(false);
            jLabel65.setVisible(false);
            jam_ventilator.setVisible(false);
        }
    }//GEN-LAST:event_ventilatorItemStateChanged

    private void pbKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pbKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_pbKeyPressed

    private void kprKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kprKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_kprKeyPressed

    private void labKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_labKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_labKeyPressed

    private void akomodasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_akomodasiKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_akomodasiKeyPressed

    private void kronisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kronisKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_kronisKeyPressed

    private void bmhpKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_bmhpKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_bmhpKeyPressed

    private void kslKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kslKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_kslKeyPressed

    private void pnjKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pnjKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_pnjKeyPressed

    private void pl_darahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pl_darahKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_pl_darahKeyPressed

    private void riKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_riKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_riKeyPressed

    private void obat_kemoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_obat_kemoKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_obat_kemoKeyPressed

    private void sewa_alatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sewa_alatKeyPressed
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_sewa_alatKeyPressed

    private void pnbKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pnbKeyReleased
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_pnbKeyReleased

    private void ahliKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ahliKeyReleased
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_ahliKeyReleased

    private void radKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_radKeyReleased
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_radKeyReleased

    private void rehabKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rehabKeyReleased
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_rehabKeyReleased

    private void obatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_obatKeyReleased
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_obatKeyReleased

    private void alkesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_alkesKeyReleased
        // TODO add your handling code here:
        hitungtotalbiaya();
    }//GEN-LAST:event_alkesKeyReleased

    private void pnbKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pnbKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_pnbKeyTyped

    private void kirimonlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kirimonlineActionPerformed
/*        // TODO add your handling code here:
//        int reply = JOptionPane.showConfirmDialog(rootPane,"SUDAH YAKIN MAU KIRIM ONLINE PASIEN "+TPasien.getText()+" ?","Konfirmasi",JOptionPane.YES_NO_OPTION);
//        if (reply == JOptionPane.YES_OPTION) {
            try {
                if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
                    JOptionPane.showMessageDialog(null,"DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
                } else {
                    try {
                        // Create a human-readable JSON string
                        String requestJson ="{\n" +
                                                "\"metadata\": {\n" +
                                                    "\"method\":\"send_claim_individual\"\n" +
                                                "},\n" +
                                                "\"data\": {\n" +
                                                    "\"nomor_sep\":\""+Tsep.getText().trim()+"\"\n" +
                                                "}\n" +
                                            "}";


                        // Use the JSON string in your request
                        String request = requestJson;
                        System.out.println("request JSON : "+requestJson);
                        JSONObject response = inacbg.request(request);
                        JSONObject code = response.getJSONObject("metadata");
                        System.out.println("Status DC Kemenkes :\n "+response);
                        StyledDocument doc = ResponEklaim.getStyledDocument();

                        Style styleBlue = ResponEklaim.addStyle("BlueBoldStyle", null);
                        StyleConstants.setForeground(styleBlue, Color.BLUE);
                        StyleConstants.setFontFamily(styleBlue, "Tahoma");
                        StyleConstants.setBold(styleBlue, true);
                        StyleConstants.setFontSize(styleBlue, 12);

                        Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
                        StyleConstants.setForeground(styleRed, Color.RED);
                        StyleConstants.setFontFamily(styleRed, "Tahoma");
                        StyleConstants.setBold(styleRed, true);
                        StyleConstants.setFontSize(styleRed, 12);

                        // Clear the document before inserting new text
 //                       doc.remove(0, doc.getLength());

                        // Prepare the response text
                        String responseText = "Kirim DC Kemenkes\n ";
                        String responseContent = response.toString() + "\n";
                        String[] words = responseContent.split(" ");

                        // Insert the "Status :" text with blue style
                        doc.insertString(doc.getLength(), responseText, styleBlue);

                        // Insert the remaining response content with no style
 //                       doc.insertString(doc.getLength(), responseContent, null);

                                // Check for tariff and base_tariff mismatch and highlight it
                            if (response.has("response")) {
                                JSONObject data = response.getJSONObject("response");
                                if (data.has("cbg")) {
                                    JSONObject cbg = data.getJSONObject("cbg");
                                    double tariff = cbg.getDouble("tariff");
                                    double baseTariff = cbg.getDouble("base_tariff");
                                    if (tariff != baseTariff) {
                                        doc.insertString(doc.getLength(), "Tarif Grouper INACBG " + cbg.getString("tariff") + " tidak sama dengan base tarif: " + cbg.getString("base_tariff") + " Cek dan/atau kirim data via E-Klaim\n", styleRed);
                                    }
                                }
                            }

                            // Insert words with specific style conditions
                            for (String word : words) {
                                if (word.contains("Ungroupable") || word.contains("Unrelated") || word.contains("not accepted as Primary Diagnosis")) {
                                    doc.insertString(doc.getLength(), word + " ", styleRed);
                                } else if (word.contains("\"kemkes_dc_status\":\"sent\"")) {
                                    doc.insertString(doc.getLength(), word + " ", styleBlue);
                                } else {
                                    doc.insertString(doc.getLength(), word + " ", null);
                                }
                            }     
                            
                        if(response.has("metadata")){
                            if(code.getInt("code")==200){
//                                JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                            }else{
                                JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                            }
                        } else {
                            System.out.println("response :\n "+response); 
                            System.err.println("\n The 'data' field is missing in the response.");
                        }
                        ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
                    } catch (Exception e) {
                        System.err.println("Error in sitb: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }catch (Exception ex) {
                System.out.println("Notifikasi Bridging Hapus : "+ex);
            }
    //    }*/
        if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
        } else {
            try {
                kirimDataKlaim();
                DownloadPDFKlaim();
                totalGroupingTersimpan(Tsep.getText());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
    }//GEN-LAST:event_kirimonlineActionPerformed

    private void chk_diag_5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chk_diag_5ItemStateChanged
        // TODO add your handling code here:
        if (chk_diag_5.isSelected()==true && !kd_diag_5.getText().trim().isEmpty()) {
            chk_diag_4.setSelected(false);
            chk_diag_3.setSelected(false);
            chk_diag_2.setSelected(false);
            chk_diag_1.setSelected(false);
            
            icd10.setText("");
            icd10.setText(kd_diag_5.getText().trim()+(kd_diag_1.getText().trim().equals("") ? "" : "#"+kd_diag_1.getText().trim())+
                    (kd_diag_2.getText().trim().equals("") ? "" : "#"+kd_diag_2.getText().trim())+(kd_diag_3.getText().trim().equals("") ? "" : "#"+kd_diag_3.getText().trim())+
                    (kd_diag_4.getText().trim().equals("") ? "" : "#"+kd_diag_4.getText().trim()));
        }else{
            chk_diag_5.setSelected(false);
        }
    }//GEN-LAST:event_chk_diag_5ItemStateChanged

    private void chk_diag_1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chk_diag_1ItemStateChanged
        // TODO add your handling code here:
        if (chk_diag_1.isSelected()==true && !kd_diag_1.getText().trim().isEmpty()) {
            chk_diag_2.setSelected(false);
            chk_diag_3.setSelected(false);
            chk_diag_4.setSelected(false);
            chk_diag_5.setSelected(false);
            
            icd10.setText("");
            icd10.setText(kd_diag_1.getText().trim()+(kd_diag_2.getText().trim().equals("") ? "" : "#"+kd_diag_2.getText().trim())+
                    (kd_diag_3.getText().trim().equals("") ? "" : "#"+kd_diag_3.getText().trim())+(kd_diag_4.getText().trim().equals("") ? "" : "#"+kd_diag_4.getText().trim())+
                    (kd_diag_5.getText().trim().equals("") ? "" : "#"+kd_diag_5.getText().trim()));
        }else{
            chk_diag_1.setSelected(false);
        }
    }//GEN-LAST:event_chk_diag_1ItemStateChanged

    private void chk_diag_2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chk_diag_2ItemStateChanged
        // TODO add your handling code here:
        if (chk_diag_2.isSelected()==true && !kd_diag_2.getText().trim().isEmpty()) {
            chk_diag_1.setSelected(false);
            chk_diag_3.setSelected(false);
            chk_diag_4.setSelected(false);
            chk_diag_5.setSelected(false);
            
            icd10.setText("");
            icd10.setText(kd_diag_2.getText().trim()+(kd_diag_1.getText().trim().equals("") ? "" : "#"+kd_diag_1.getText().trim())+
                    (kd_diag_3.getText().trim().equals("") ? "" : "#"+kd_diag_3.getText().trim())+(kd_diag_4.getText().trim().equals("") ? "" : "#"+kd_diag_4.getText().trim())+
                    (kd_diag_5.getText().trim().equals("") ? "" : "#"+kd_diag_5.getText().trim()));
        }else{
            chk_diag_2.setSelected(false);
        }
    }//GEN-LAST:event_chk_diag_2ItemStateChanged

    private void chk_diag_3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chk_diag_3ItemStateChanged
        // TODO add your handling code here:
        if (chk_diag_3.isSelected()==true && !kd_diag_3.getText().trim().isEmpty()) {
            chk_diag_1.setSelected(false);
            chk_diag_2.setSelected(false);
            chk_diag_4.setSelected(false);
            chk_diag_5.setSelected(false);
            
            icd10.setText("");
            icd10.setText(kd_diag_3.getText().trim()+(kd_diag_1.getText().trim().equals("") ? "" : "#"+kd_diag_1.getText().trim())+
                    (kd_diag_2.getText().trim().equals("") ? "" : "#"+kd_diag_2.getText().trim())+(kd_diag_4.getText().trim().equals("") ? "" : "#"+kd_diag_4.getText().trim())+
                    (kd_diag_5.getText().trim().equals("") ? "" : "#"+kd_diag_5.getText().trim()));
        }else{
            chk_diag_3.setSelected(false);
        }
    }//GEN-LAST:event_chk_diag_3ItemStateChanged

    private void chk_diag_4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chk_diag_4ItemStateChanged
        // TODO add your handling code here:
        if (chk_diag_4.isSelected()==true && !kd_diag_4.getText().trim().isEmpty()) {
            chk_diag_2.setSelected(false);
            chk_diag_3.setSelected(false);
            chk_diag_1.setSelected(false);
            chk_diag_5.setSelected(false);
            
            icd10.setText("");
            icd10.setText(kd_diag_4.getText().trim()+(kd_diag_1.getText().trim().equals("") ? "" : "#"+kd_diag_1.getText().trim())+
                    (kd_diag_2.getText().trim().equals("") ? "" : "#"+kd_diag_2.getText().trim())+(kd_diag_3.getText().trim().equals("") ? "" : "#"+kd_diag_3.getText().trim())+
                    (kd_diag_5.getText().trim().equals("") ? "" : "#"+kd_diag_5.getText().trim()));
        }else{
            chk_diag_4.setSelected(false);
        }
    }//GEN-LAST:event_chk_diag_4ItemStateChanged

    private void ap_5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_5KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_5KeyPressed

    private void ap_2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_2KeyPressed

    private void ap_3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_3KeyPressed

    private void ap_4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_4KeyPressed

    private void ap_10KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_10KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_10KeyPressed

    private void ap_6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_6KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_6KeyPressed

    private void ap_7KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_7KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_7KeyPressed

    private void ap_8KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_8KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_8KeyPressed

    private void ap_9KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ap_9KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ap_9KeyPressed

    private void finalklaimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finalklaimActionPerformed
        // TODO add your handling code here:
//        int reply = JOptionPane.showConfirmDialog(rootPane,"SUDAH YAKIN MAU FINAL KLAIM PASIEN "+TPasien.getText()+" ?","Konfirmasi",JOptionPane.YES_NO_OPTION);
//        if (reply == JOptionPane.YES_OPTION) {
            if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(null,"DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
            } else {
                try {
                    // Create a human-readable JSON string
                    String requestJson ="{\n" +
                                            "\"metadata\": {\n" +
                                                "\"method\":\"claim_final\"\n" +
                                            "},\n" +
                                            "\"data\": {\n" +
                                                "\"nomor_sep\":\""+Tsep.getText().trim()+"\",\n" +
                                                "\"coder_nik\": \""+coder+"\"\n" +
                                            "}\n" +
                                        "}";


                    // Use the JSON string in your request
                    String request = requestJson;
//                    System.out.println("request JSON : "+requestJson);
                    JSONObject response = inacbg.request(request);
                    JSONObject code = response.getJSONObject("metadata");
//                    System.out.println("response final :\n "+response);
                    StyledDocument doc = ResponEklaim.getStyledDocument();

                    // Define styles
                    Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
                    StyleConstants.setForeground(styleRed, Color.RED);
                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                    StyleConstants.setBold(styleRed, true);
                    StyleConstants.setFontSize(styleRed, 12);

                    Style styleBlue = ResponEklaim.addStyle("BlueBoldStyle", null);
                    StyleConstants.setForeground(styleBlue, Color.BLUE);
                    StyleConstants.setFontFamily(styleBlue, "Tahoma");
                    StyleConstants.setBold(styleBlue, true);
                    StyleConstants.setFontSize(styleBlue, 12);

                    // Clear the document before inserting new text
                    // doc.remove(0, doc.getLength());

                    // Prepare the response text
                    String responseText = "Status Final\n "; // Perubahan 1: Tambahkan teks Status Final
                    String responseContent = response.toString() + "\n";
                    String[] words = responseContent.split(" ");

                    // Insert the "Status Final" text with blue style
                    doc.insertString(doc.getLength(), responseText, styleBlue); // Perubahan 2: Beri gaya biru pada Status Final

                    // Insert the remaining response content with no style
//                    doc.insertString(doc.getLength(), responseContent, null);

                    // Check for tariff and base_tariff mismatch and highlight it
                    if (response.has("response")) {
                        JSONObject data = response.getJSONObject("response");
                        if (data.has("cbg")) {
                            JSONObject cbg = data.getJSONObject("cbg");
                            double tariff = cbg.getDouble("tariff");
                            double baseTariff = cbg.getDouble("base_tariff");
                            if (tariff != baseTariff) {
                                doc.insertString(doc.getLength(), "Tarif Grouper INACBG " + cbg.getString("tariff") + " tidak sama dengan base tarif: " + cbg.getString("base_tariff") + " Cek dan/atau kirim data via E-Klaim\n", styleRed);
                            }
                        }
                    }

                    // Insert words with specific style conditions
                    for (String word : words) {
                        if (word.contains("Ungroupable") || word.contains("Unrelated") || word.contains("not accepted as Primary Diagnosis")) {
                            doc.insertString(doc.getLength(), word + " ", styleRed);
                        } else {
                            doc.insertString(doc.getLength(), word + " ", null);
                        }
                    }

                    if(response.has("metadata")){
                        if(code.getInt("code")==200){
//                            JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                        }else{
                            JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                        }
                        totalGroupingTersimpan(Tsep.getText());
                    } else {
                        System.out.println("response :\n "+response); 
                        System.err.println("\n The 'data' field is missing in the response.");
                    }
                    ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
                } catch (Exception e) {
                    System.err.println("Error in sitb: " + e.getMessage());
                    e.printStackTrace();
                }
            }
//        }
    }//GEN-LAST:event_finalklaimActionPerformed

    private void hapusKlaimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusKlaimActionPerformed
        // TODO add your handling code here:
        int reply = JOptionPane.showConfirmDialog(rootPane,"SUDAH YAKIN MAU HAPUS KLAIM PASIEN "+TPasien.getText()+" (KLAIM INI AKAN TERHAPUS !!!!!!) ?","Konfirmasi",JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(null,"DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
            } else {
                try {
                    // Create a human-readable JSON string
                    String requestJson ="{\n" +
                                            "\"metadata\": {\n" +
                                                "\"method\":\"delete_claim\"\n" +
                                            "},\n" +
                                            "\"data\": {\n" +
                                                "\"nomor_sep\":\""+Tsep.getText().trim()+"\",\n" +
                                                "\"coder_nik\": \""+coder+"\"\n" +
                                            "}\n" +
                                        "}";


                    // Use the JSON string in your request
                    String request = requestJson;
                    System.out.println("request JSON : "+requestJson);
                    JSONObject response = inacbg.request(request);
                    JSONObject code = response.getJSONObject("metadata");
                    System.out.println("response hapus :\n "+response);
                    StyledDocument doc = ResponEklaim.getStyledDocument();
                    
                    Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
                    StyleConstants.setForeground(styleRed, Color.RED);
                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                    StyleConstants.setBold(styleRed, true);
                    StyleConstants.setFontSize(styleRed, 12);

                    // Clear the document before inserting new text
 //                   doc.remove(0, doc.getLength());

                    // Prepare the response text
                    String responseText = "Hapus Data\n ";
                    String responseContent = response.toString() + "\n";
                    String[] words = responseContent.split(" ");

                    // Insert the "Status :" text with blue style
                    doc.insertString(doc.getLength(), responseText, styleRed);

                    // Insert the remaining response content with no style
 //                   doc.insertString(doc.getLength(), responseContent, null);
                  
                        // Check for tariff and base_tariff mismatch and highlight it
                        if (response.has("response")) {
                            JSONObject data = response.getJSONObject("response");
                            if (data.has("cbg")) {
                                JSONObject cbg = data.getJSONObject("cbg");
                                double tariff = cbg.getDouble("tariff");
                                double baseTariff = cbg.getDouble("base_tariff");
                                if (tariff != baseTariff) {
                                    StyleConstants.setForeground(styleRed, Color.RED);
                                    StyleConstants.setFontFamily(styleRed, "Tahoma");
                                    StyleConstants.setBold(styleRed, true);
                                    StyleConstants.setFontSize(styleRed, 12);
   //                                 doc.remove(0, doc.getLength());
                                    doc.insertString(doc.getLength(), "Tarif Grouper INACBG " + cbg.getString("tariff") + " tidak sama dengan base tarif: " + cbg.getString("base_tariff") + " Cek dan/atau kirim data via E-Klaim\n", styleRed);
                                }
                            }
                        }

                    // Insert words with specific style conditions
                    for (String word : words) {
                        if (word.contains("Ungroupable") || word.contains("Unrelated") || word.contains("not accepted as Primary Diagnosis")) {
                            doc.insertString(doc.getLength(), word + " ", styleRed);
                        } else {
                            doc.insertString(doc.getLength(), word + " ", null);
                        }
                    }
                    if(response.has("metadata")){
                        if(code.getInt("code")==200){
//                            JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                            Sequel.meghapus("inacbg_data_terkirim", "no_sep", Tsep.getText());
                            Sequel.meghapus("inacbg_grouping_stage1", "no_sep", Tsep.getText());
                            Sequel.meghapus("inacbg_grouping_stage2", "no_sep", Tsep.getText());
                        }else{
                            JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                        }
                        totalGroupingTersimpan(Tsep.getText());
                    } else {
                        System.out.println("response :\n "+response); 
                        System.err.println("\n The 'data' field is missing in the response.");
                    }
                    ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
                } catch (Exception e) {
                    System.err.println("Error in sitb: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_hapusKlaimActionPerformed

    private void hapusKlaimKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hapusKlaimKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_hapusKlaimKeyPressed

    private void editKlaimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editKlaimActionPerformed
        // TODO add your handling code here:
//        int reply = JOptionPane.showConfirmDialog(rootPane,"SUDAH YAKIN MAU EDIT KLAIM PASIEN "+TPasien.getText()+" ?","Konfirmasi",JOptionPane.YES_NO_OPTION);
//        if (reply == JOptionPane.YES_OPTION) {
            if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(null,"DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
            } else {
                try {
                    // Create a human-readable JSON string
                    String requestJson ="{\n" +
                                            "\"metadata\": {\n" +
                                                "\"method\":\"reedit_claim\"\n" +
                                            "},\n" +
                                            "\"data\": {\n" +
                                                "\"nomor_sep\":\""+Tsep.getText().trim()+"\"\n" +
                                            "}\n" +
                                        "}";


                    // Use the JSON string in your request
                    String request = requestJson;
//                    System.out.println("request JSON : "+requestJson);
                    JSONObject response = inacbg.request(request);
                    JSONObject code = response.getJSONObject("metadata");
//                    System.out.println("response edit :\n "+response);
                    StyledDocument doc = ResponEklaim.getStyledDocument();
                    Style style = ResponEklaim.addStyle("RedBoldStyle", null);
                    StyleConstants.setForeground(style, Color.RED);
                    StyleConstants.setFontFamily(style, "Tahoma");
                    StyleConstants.setBold(style, true);
                    StyleConstants.setFontSize(style, 12);
                    
                    Style stylePurple = ResponEklaim.addStyle("PurpleBoldStyle", null);
                    StyleConstants.setForeground(stylePurple, new Color(128, 0, 128));
                    StyleConstants.setFontFamily(stylePurple, "Tahoma");
                    StyleConstants.setBold(stylePurple, true);
                    StyleConstants.setFontSize(stylePurple, 12);

                    // Clear the document before inserting new text
 //                   doc.remove(0, doc.getLength());

                    // Prepare the response text
                    String responseText = "Edit Data\n ";
                    String responseContent = response.toString() + "\n";
                    String[] words = responseContent.split(" ");

                    // Insert the "Status :" text with blue style
                    doc.insertString(doc.getLength(), responseText, stylePurple);

                    // Insert the remaining response content with no style
//                    doc.insertString(doc.getLength(), responseContent, null);
                  
                        // Check for tariff and base_tariff mismatch and highlight it
                        if (response.has("response")) {
                            JSONObject data = response.getJSONObject("response");
                            if (data.has("cbg")) {
                                JSONObject cbg = data.getJSONObject("cbg");
                                double tariff = cbg.getDouble("tariff");
                                double baseTariff = cbg.getDouble("base_tariff");
                                if (tariff != baseTariff) {
                                    StyleConstants.setForeground(style, Color.RED);
                                    StyleConstants.setFontFamily(style, "Tahoma");
                                    StyleConstants.setBold(style, true);
                                    StyleConstants.setFontSize(style, 12);
   //                                 doc.remove(0, doc.getLength());
                                    doc.insertString(doc.getLength(), "Tarif Grouper INACBG " + cbg.getString("tariff") + " tidak sama dengan base tarif: " + cbg.getString("base_tariff") + " Cek dan/atau kirim data via E-Klaim\n", style);
                                }
                            }
                        }

                    // Insert words with specific style conditions
                    for (String word : words) {
                        if (word.contains("Ungroupable") || word.contains("Unrelated") || word.contains("not accepted as Primary Diagnosis")) {
                            doc.insertString(doc.getLength(), word + " ", style);
                        } else {
                            doc.insertString(doc.getLength(), word + " ", null);
                        }
                    }
                    if(response.has("metadata")){
                        if(code.getInt("code")==200){
//                            JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                            Sequel.meghapus("inacbg_data_terkirim", "no_sep", Tsep.getText());
                            Sequel.meghapus("inacbg_data_terkirim2", "no_sep", Tsep.getText());                            
                            Sequel.meghapus("inacbg_grouping_stage1", "no_sep", Tsep.getText());
                            Sequel.meghapus("inacbg_grouping_stage2", "no_sep", Tsep.getText());  
                            Sequel.meghapus("inacbg_grouping_stage12", "no_sep", Tsep.getText());                               
                        }else{
                            JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                        }
                        totalGroupingTersimpan(Tsep.getText());
                    } else {
                        System.out.println("response :\n "+response); 
                        System.err.println("\n The 'data' field is missing in the response.");
                    }
                    ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
                } catch (Exception e) {
                    System.err.println("Error in sitb: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        //}
    }//GEN-LAST:event_editKlaimActionPerformed

    private void validasi_tbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validasi_tbActionPerformed
        // TODO add your handling code here:
        int reply = JOptionPane.showConfirmDialog(rootPane,"YAKIN VALIDASI SITB PASIEN "+TPasien.getText()+" ?","Konfirmasi",JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            if (pasien_tb.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(null,"Silahkan isi nomor sitb pasien terlebih dahulu..!!");
            } else {
                try {
                    // Create a human-readable JSON string
                    String requestJson = "{\n"
                    + " \"metadata\": {\n"
                    + " \"method\": \"sitb_validate\"\n"
                    + "},\n"
                    + " \"data\": {\n"
                    + " \"nomor_sep\": \""+Tsep.getText()+"\",\n"
                    + " \"nomor_register_sitb\": \"" + pasien_tb.getText().trim() + "\"\n"
                    + " }\n"
                    + "}";

                    // Use the JSON string in your request
                    String request = requestJson;
                    System.out.println("request JSON : "+requestJson);

                    JSONObject response = inacbg.request(request);
                    System.out.println("response data klaim : "+response);

                    if(response.has("metadata")){
                        JSONObject code = response.getJSONObject("metadata");
                        JSONObject res = response.getJSONObject("response");
                        if (code.getInt("code") == 200) {
                            if(res.getString("status").equals("VALID")){
                                JOptionPane.showMessageDialog(null,"RESPON : STATUS PASIEN "+TPasien.getText().trim()+" ( "+res.getString("status")+" )");
                                Sequel.menyimpan("inacbg_validasi_tb","'"+TNoRM.getText()+"','"+pasien_tb.getText().trim()+"'");
                            }else{
                                JOptionPane.showMessageDialog(null,"RESPON : "+res.getString("detail"));
                            }
                        }
                    } else {
                        System.out.println("response : "+response);
                        System.err.println("\n The 'data' field is missing in the response.");
                    }

                } catch (Exception e) {
                    System.err.println("Error in sitb: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_validasi_tbActionPerformed

    private void batal_validasi_tbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batal_validasi_tbActionPerformed
        // TODO add your handling code here:
        int reply = JOptionPane.showConfirmDialog(rootPane,"YAKIN MAU BATAL REGISTER SITB PASIEN "+TPasien.getText()+" ?","Konfirmasi",JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            if (pasien_tb.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(null,"Silahkan isi nomor sitb pasien terlebih dahulu..!!");
            } else {
                try {
                    // Create a human-readable JSON string
                    String requestJson = "{\n"
                    + " \"metadata\": {\n"
                    + " \"method\": \"sitb_invalidate\"\n"
                    + "},\n"
                    + " \"data\": {\n"
                    + " \"nomor_sep\": \""+Tsep.getText()+"\"\n"
                    + " }\n"
                    + "}";

                    // Use the JSON string in your request
                    String request = requestJson;
                    System.out.println("request JSON : "+requestJson);

                    JSONObject response = inacbg.request(request);
                    System.out.println("response data klaim : "+response);

                    if(response.has("metadata")){
                        JSONObject code = response.getJSONObject("metadata");
                        JSONObject res = response.getJSONObject("metadata");
                        if(code.getInt("code")==200){
                            JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
                            Sequel.meghapus("inacbg_validasi_tb","no_rkm_medis", TNoRM.getText());
                        } else {
                            JOptionPane.showMessageDialog(null,"RESPON : "+res.getString("message"));
                        }
                    } else {
                        System.out.println("response : "+response);
                        System.err.println("\n The 'data' field is missing in the response.");
                    }

                } catch (Exception e) {
                    System.err.println("Error in sitb: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }//GEN-LAST:event_batal_validasi_tbActionPerformed

    private void check_hdItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_check_hdItemStateChanged
        // TODO add your handling code here:
//        if (check_hd.isSelected() == true) {
            if (kd_psdr_1.getText().equals("39.95")) {
            check_hd.setSelected(true);    
            jLabel56.setVisible(true);
            jLabel79.setVisible(true);
            pasien_hd.setVisible(true);
            kt_darah.setVisible(true);
            
        }else{
            check_hd.setSelected(false);  
            check_hd.setVisible(false);             
            jLabel56.setVisible(false);
            jLabel79.setVisible(false);
            pasien_hd.setVisible(false);
            kt_darah.setVisible(false);
        }
    }//GEN-LAST:event_check_hdItemStateChanged

    private void pasien_hdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasien_hdActionPerformed
    // Mendapatkan pilihan dari pasien_hd
    int selectedIndex = pasien_hd.getSelectedIndex();

    // Mengecek pilihan dan mengatur nilai dializer_single_use sesuai
    if (selectedIndex == 0) { // Misalnya, indeks 0 adalah "MULTIPLE USE"
        pasien_hd.putClientProperty("dializer_single_use", "0");
    } else if (selectedIndex == 1) { // Misalnya, indeks 1 adalah "SINGLE USE"
        pasien_hd.putClientProperty("dializer_single_use", "1");
    }
    // Mengecek hasilnya
//    System.out.println("Dializer Single Use: " + pasien_hd.getClientProperty("dializer_single_use"));
    }//GEN-LAST:event_pasien_hdActionPerformed

    private void check_hdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_hdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_check_hdActionPerformed

    private void RiwayatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RiwayatActionPerformed
    if(Tsep.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        Tsep.requestFocus();
    }else{
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ViewerKoding resume=new ViewerKoding(null,true);
        String noSEP = Tsep.getText();
        String StatusRawat = jenis_rawat.getSelectedItem().toString();// Get value from textbox
        String norw = Sequel.cariIsi("select no_rawat from bridging_sep where no_sep=?", noSEP);
        resume.setDataPasien2(norw,TNoRM.getText(),TPasien.getText(),tgl_masuk.getDate(),tgl_keluar.getDate(),StatusRawat);
        resume.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
        resume.setLocationRelativeTo(internalFrame1);
        resume.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                updateDataDanRefreshGUI();
            }
        });
        resume.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_RiwayatActionPerformed

    private void ViewPDFEklaimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewPDFEklaimActionPerformed
/*    if(Tsep.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        Tsep.requestFocus();
    }else{
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgViewPdfEklaim resume=new DlgViewPdfEklaim(null,true);
//        String noSEP = Tsep.getText();
//        String StatusRawat = jenis_rawat.getSelectedItem().toString();// Get value from textbox
//        String norw = Sequel.cariIsi("select no_rawat from bridging_sep where no_sep=?", noSEP);
//        String kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%E-KLAIM%'");
        String FileName = "EKLAIM_" + Tsep.getText().trim() + ".pdf";
        resume.tampilPdfBerkasDigital(FileName);
        resume.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
        resume.setLocationRelativeTo(internalFrame1);
        resume.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
        }        // TODO add your handling code here:*/
if (TNoRM.getText().trim().equals("") || Tsep.getText().trim().equals("") || no_kartu.getText().trim().equals("")) {
    JOptionPane.showMessageDialog(null, "DATA NOMOR REKAM MEDIS / NOMOR SEP / NOMOR KARTU TIDAK TERISI !!!!!");
        } else {
            try {
                String requestJson = "{\n" +
                        " \"metadata\": {\n" +
                        " \"method\": \"claim_print\"\n" +
                        " },\n" +
                        " \"data\": {\n" +
                        " \"nomor_sep\": \"" + Tsep.getText().trim() + "\"\n" +
                        " }\n" +
                        "}";

                JSONObject response = inacbg.request(requestJson);

                if (response.has("data")) {
                    String data = response.getString("data");
                    byte[] byteArray = Base64.getDecoder().decode(data);

                    InputStream inputStream = new ByteArrayInputStream(byteArray);
                    try {
                        String directory = "./tempPDF";
                        File dir = new File(directory);
                        if (!dir.exists()) {
                            dir.mkdirs(); // Membuat folder jika belum ada
                        }

                        String fileName = "EKLAIM_" + Tsep.getText().trim() + ".pdf";
                        String filePath = directory + "/" + fileName;
                        Path path = Paths.get(filePath);
                        Files.deleteIfExists(path); // Menghapus file lama jika ada
                        Files.copy(inputStream, path); // Menyimpan file baru
//                        JOptionPane.showMessageDialog(null, "File berhasil disimpan di: " + filePath);
                        DlgViewPdfEklaim resume=new DlgViewPdfEklaim(null,true);
                        String FileName = "EKLAIM_" + Tsep.getText().trim() + ".pdf";
                        resume.tampilPdfEklaim(FileName);
                        resume.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
                        resume.setLocationRelativeTo(internalFrame1);
                        resume.setVisible(true);
                        this.setCursor(Cursor.getDefaultCursor());
                        HapusPDF(); // Pastikan metode ini relevan dan tetap diperlukan
                    } finally {
                        inputStream.close(); // Menutup aliran input untuk mencegah memory leak
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Eklaim belum final/dikirim");
                }
            } catch (Exception e) {
                e.printStackTrace(); // Debugging untuk melihat error
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_ViewPDFEklaimActionPerformed

    private void btnDPJPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDPJPActionPerformed
        // Mengatur ukuran menjadi setengah dari lebar dan tinggi internalFrame1
        dokter.setSize((int) (internalFrame1.getWidth() * 0.5), (int) (internalFrame1.getHeight() * 0.5));
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
//        updateDPJPRefreshGUI();
    }//GEN-LAST:event_btnDPJPActionPerformed

    private void btnDPJPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDPJPKeyPressed

    }//GEN-LAST:event_btnDPJPKeyPressed

    private void ViewPDFBerkasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewPDFBerkasActionPerformed
    if(Tsep.getText().trim().equals("")){
        JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih dulu pasien...!!!");
        Tsep.requestFocus();
    }else{
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgViewPdfEklaim resume=new DlgViewPdfEklaim(null,true);
//        String noSEP = Tsep.getText();
//        String StatusRawat = jenis_rawat.getSelectedItem().toString();// Get value from textbox
//        String norw = Sequel.cariIsi("select no_rawat from bridging_sep where no_sep=?", noSEP);
//        String kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%E-KLAIM%'");
        String FileName = "EKLAIM_" + Tsep.getText().trim() + ".pdf";
        resume.tampilPdfBerkasDigital(FileName);
        resume.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
        resume.setLocationRelativeTo(internalFrame1);
        resume.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
        }        // TODO add your handling code here:        // TODO add your handling code here:
    }//GEN-LAST:event_ViewPDFBerkasActionPerformed

    private void chk_diag_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chk_diag_1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chk_diag_1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            INACBGData dialog = new INACBGData(new javax.swing.JFrame(), true);
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
    private widget.Button BtnGrouper;
    private widget.Button BtnKeluar;
    private widget.panelisi FormInput;
    private javax.swing.JTextPane ResponEklaim;
    private widget.Button Riwayat;
    private widget.TextBox TNoRM;
    private widget.TextBox TPasien;
    private widget.TextBox Tsep;
    private widget.Button ViewPDFBerkas;
    private widget.Button ViewPDFEklaim;
    private widget.TextBox ahli;
    private widget.TextBox akomodasi;
    private widget.TextBox alkes;
    private widget.TextBox ap_1;
    private widget.TextBox ap_10;
    private widget.TextBox ap_2;
    private widget.TextBox ap_3;
    private widget.TextBox ap_4;
    private widget.TextBox ap_5;
    private widget.TextBox ap_6;
    private widget.TextBox ap_7;
    private widget.TextBox ap_8;
    private widget.TextBox ap_9;
    private widget.Button batal_validasi_tb;
    private widget.TextBox berat_bayi;
    private widget.TextBox bmhp;
    private widget.Button btnDPJP;
    private widget.ComboBox cara_masuk;
    private javax.swing.JCheckBox check_hd;
    private javax.swing.JCheckBox chk_diag_1;
    private javax.swing.JCheckBox chk_diag_2;
    private javax.swing.JCheckBox chk_diag_3;
    private javax.swing.JCheckBox chk_diag_4;
    private javax.swing.JCheckBox chk_diag_5;
    private widget.TextBox chronic;
    private widget.TextBox co_covid_19;
    private widget.ComboBox cob;
    private widget.Label code_cbg_grouping_tersimpan;
    private widget.Label deskripsi_grouping_tersimpan;
    private widget.TextBox distole;
    private widget.TextBox dpjp;
    private widget.Button editKlaim;
    private widget.Button finalklaim;
    private widget.ComboBox hak_kelas;
    private widget.Button hapusKlaim;
    private widget.TextBox hari_intensif;
    private widget.TextBox icd10;
    private widget.TextBox icd9;
    private widget.InternalFrame internalFrame1;
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
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel36;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel46;
    private widget.Label jLabel49;
    private widget.Label jLabel50;
    private widget.Label jLabel53;
    private widget.Label jLabel56;
    private widget.Label jLabel58;
    private widget.Label jLabel6;
    private widget.Label jLabel61;
    private widget.Label jLabel62;
    private widget.Label jLabel63;
    private widget.Label jLabel65;
    private widget.Label jLabel66;
    private widget.Label jLabel67;
    private widget.Label jLabel68;
    private widget.Label jLabel69;
    private widget.Label jLabel7;
    private widget.Label jLabel70;
    private widget.Label jLabel71;
    private widget.Label jLabel72;
    private widget.Label jLabel73;
    private widget.Label jLabel74;
    private widget.Label jLabel75;
    private widget.Label jLabel76;
    private widget.Label jLabel77;
    private widget.Label jLabel78;
    private widget.Label jLabel79;
    private widget.Label jLabel8;
    private widget.Label jLabel80;
    private widget.Label jLabel81;
    private widget.Label jLabel82;
    private widget.Label jLabel83;
    private widget.Label jLabel84;
    private widget.Label jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JSeparator jSeparator18;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator20;
    private javax.swing.JSeparator jSeparator21;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private widget.ComboBox jam_ekstubasi;
    private widget.ComboBox jam_intubasi;
    private widget.ComboBox jam_keluar;
    private widget.ComboBox jam_masuk;
    private widget.TextBox jam_ventilator;
    private widget.ComboBox jaminan;
    private widget.TextBox jenis_kelamin;
    private widget.ComboBox jenis_rawat;
    private widget.TextBox kd_diag_1;
    private widget.TextBox kd_diag_2;
    private widget.TextBox kd_diag_3;
    private widget.TextBox kd_diag_4;
    private widget.TextBox kd_diag_5;
    private widget.TextBox kd_psdr_1;
    private widget.TextBox kd_psdr_2;
    private widget.TextBox kd_psdr_3;
    private widget.TextBox kd_psdr_4;
    private widget.TextBox kd_psdr_5;
    private javax.swing.JCheckBox kelas_eksekutif;
    private widget.Button kirimonline;
    private widget.TextBox kpr;
    private widget.TextBox kronis;
    private widget.TextBox ksl;
    private widget.TextBox kt_darah;
    private widget.TextBox lab;
    private widget.Label labelDiagnosa;
    private widget.Label labelProsedur;
    private widget.TextBox lama_naik_kelas;
    private widget.ComboBox menit_ekstubasi;
    private widget.ComboBox menit_intubasi;
    private widget.ComboBox menit_keluar;
    private widget.ComboBox menit_masuk;
    private widget.ComboBox naik_kelas_pelayanan;
    private javax.swing.JCheckBox naik_turun_kelas;
    private widget.TextBox nm_diag_1;
    private widget.TextBox nm_diag_2;
    private widget.TextBox nm_diag_3;
    private widget.TextBox nm_diag_4;
    private widget.TextBox nm_diag_5;
    private widget.TextBox nm_psdr_1;
    private widget.TextBox nm_psdr_2;
    private widget.TextBox nm_psdr_3;
    private widget.TextBox nm_psdr_4;
    private widget.TextBox nm_psdr_5;
    private widget.TextBox no_kartu;
    private widget.TextBox no_rawat;
    private widget.TextBox obat;
    private widget.TextBox obat_kemo;
    private widget.panelisi panelGlass8;
    private widget.ComboBox pasien_hd;
    private widget.TextBox pasien_tb;
    private widget.TextBox pb;
    private widget.TextBox pl_darah;
    private widget.TextBox pnb;
    private widget.TextBox pnb1;
    private widget.TextBox pnj;
    private widget.TextBox rad;
    private javax.swing.JCheckBox rawat_intensif;
    private widget.TextBox rehab;
    private widget.TextBox ri;
    private widget.ScrollPane scrollInput;
    private widget.TextBox sewa_alat;
    private widget.TextBox sistole;
    private widget.ComboBox stage_2;
    private widget.ComboBox status_pulang;
    private widget.TextBox sub_acute;
    private widget.TextBox tarif_eksekutif;
    private widget.Tanggal tgl_ekstubasi;
    private widget.Tanggal tgl_intubasi;
    private widget.Tanggal tgl_keluar;
    private widget.TextBox tgl_lahir;
    private widget.Tanggal tgl_masuk;
    private widget.Label total_grouping_tersimpan;
    private widget.Label total_rupiah;
    private widget.Label total_rupiah1;
    private widget.Button validasi_cvd19;
    private widget.Button validasi_tb;
    private javax.swing.JCheckBox ventilator;
    // End of variables declaration//GEN-END:variables

    public void emptTeks() {
        TNoRM.setText("");
        TPasien.setText("");
        Tsep.setText("");
        tgl_masuk.setDate(new Date());
        tgl_masuk.requestFocus();
        ResponEklaim.setText("");
//        CodeInaCBG.setText("");
    }
    
    public void setDiagnosa(String norw, String norm, String pasien) {
        //cek koder
        try {

            rs = koneksi.prepareStatement("select * from bridging_sep where no_rawat='"+norw+"'").executeQuery();
            while (rs.next()) {
                coder = Sequel.cariIsi("select no_ik from inacbg_coder_nik where nik='"+akses.getkode()+"'");
                if (akses.getkode().equals("Admin Utama")){
                   coder=Sequel.cariIsi("select no_ik from inacbg_coder_nik LIMIT 1");
                } else if (!coder.equals("")){
                   coder=coder;
                } else {
                    JOptionPane.showMessageDialog(null,"ANDA BUKAN CODER KLAIM, JADINYA TIDAK BISA !!!!!");
                    BtnKeluarActionPerformed(null);
                }

                no_rawat.setText(norw);
                no_kartu.setText(rs.getString("no_kartu"));  
                Tsep.setText(rs.getString("no_sep"));  
                
                TPasien.setText(pasien);   
                jenis_kelamin.setText(rs.getString("jkel"));
                tgl_lahir.setText(rs.getString("tanggal_lahir"));
                TNoRM.setText(norm);
                
                if (rs.getString("jnspelayanan").equals("2")) {
                    hak_kelas.setSelectedIndex(2);
                } else {
                    switch (rs.getString("klsrawat")) {
                        case "1":
                            hak_kelas.setSelectedIndex(0);
                            break;
                        case "2":
                            hak_kelas.setSelectedIndex(1);
                            break;
                        case "3":
                            hak_kelas.setSelectedIndex(2);
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
                
                if (rs.getString("kdpolitujuan").equals("HDL")) {
                    check_hd.setSelected(true);
                } else {
                    check_hd.setSelected(false);
                }
                
                              
/*                String noRawat = no_rawat.getText();
                // Pastikan noRawat tidak kosong atau null
                if (noRawat == null || noRawat.isEmpty()) {
                    System.out.println("Error: noRawat is empty or null");
                    return;
                }*/

                // Menggabungkan dua query SQL ke dalam satu string
                String queryRalan = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Ralan Dokter Paramedis' and billing.nm_perawatan not like '%terapi%'";
                String queryRanap = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Ranap Dokter Paramedis' and billing.nm_perawatan not like '%terapi%'";

                // Menjalankan query dan menggabungkan hasilnya
                String hasilRalan = Sequel.cariIsi(queryRalan);
                String hasilRanap = Sequel.cariIsi(queryRanap);

                // Log hasil query
//                System.out.println("Query Ralan: " + queryRalan);
//                System.out.println("Hasil Ralan: " + hasilRalan);
//                System.out.println("Query Ranap: " + queryRanap);
//                System.out.println("Hasil Ranap: " + hasilRanap);

                // Menghitung total prosedur non-bedah
                int prosedurNonBedah = 0;
                if (hasilRalan != null && !hasilRalan.isEmpty()) {
                    prosedurNonBedah += Integer.parseInt(hasilRalan);
                }
                if (hasilRanap != null && !hasilRanap.isEmpty()) {
                    prosedurNonBedah += Integer.parseInt(hasilRanap);
                }

                // Log hasil akhir
//                System.out.println("Total Prosedur Non Bedah: " + prosedurNonBedah);

                // Menampilkan hasil akhir dalam text field atau komponen lain
                if (prosedurNonBedah == 0) {
                    pnb.setText("0");
                } else {
                    pnb.setText(String.valueOf(prosedurNonBedah));
                }

                // Query SQL untuk mendapatkan total biaya operasi
                String queryOperasi = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Operasi'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilOperasi = Sequel.cariIsi(queryOperasi);

                // Log hasil query
//                System.out.println("Query Operasi: " + queryOperasi);
//                System.out.println("Hasil Operasi: " + hasilOperasi);

                // Memeriksa nilai kosong dan konversi ke integer
                int prosedurBedah = hasilOperasi != null && !hasilOperasi.isEmpty() ? Integer.parseInt(hasilOperasi) : 0;

                // Log hasil akhir
//                System.out.println("Total Prosedur Bedah: " + prosedurBedah);

                // Menampilkan total biaya operasi dalam text field atau komponen lain
                pb.setText(String.valueOf(prosedurBedah));

                // Jika hasilnya kosong, set menjadi "0"
                if (prosedurBedah == 0) {
                    pb.setText("0");
                }

                // Query SQL untuk mendapatkan total biaya konsultasi dari Ranap Dokter dan Ralan Dokter
                String queryRanapDokter = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Ranap Dokter'";
                String queryRalanDokter = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Ralan Dokter'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilRanapDokter = Sequel.cariIsi(queryRanapDokter);
                String hasilRalanDokter = Sequel.cariIsi(queryRalanDokter);

                // Log hasil query
//                System.out.println("Query Ranap Dokter: " + queryRanapDokter);
//                System.out.println("Hasil Ranap Dokter: " + hasilRanapDokter);
//                System.out.println("Query Ralan Dokter: " + queryRalanDokter);
//                System.out.println("Hasil Ralan Dokter: " + hasilRalanDokter);

                // Menghitung total biaya konsultasi
                int konsultasi = 0;
                if (hasilRanapDokter != null && !hasilRanapDokter.isEmpty()) {
                    konsultasi += Integer.parseInt(hasilRanapDokter);
                }
                if (hasilRalanDokter != null && !hasilRalanDokter.isEmpty()) {
                    konsultasi += Integer.parseInt(hasilRalanDokter);
                }

                // Log hasil akhir
//                System.out.println("Total Konsultasi: " + konsultasi);

                // Menampilkan total biaya konsultasi dalam text field atau komponen lain
                if (konsultasi == 0) {
                    ksl.setText("0");
                } else {
                    ksl.setText(String.valueOf(konsultasi));
                }

                // Deklarasi variabel tenagaAhli dan inisialisasi dengan nilai 0
/*                int tenagaAhli = 0;
                // Log nilai awal tenagaAhli
//                System.out.println("Nilai awal tenagaAhli: " + tenagaAhli);
                // Memeriksa apakah tenagaAhli adalah string kosong dan mengatur nilai menjadi "0" jika demikian
                if (String.valueOf(tenagaAhli).equals("")) {
                    tenagaAhli = 0;
                }
                // Log nilai tenagaAhli setelah pemeriksaan
//                System.out.println("Nilai tenagaAhli setelah pemeriksaan: " + tenagaAhli);
                // Menampilkan nilai tenagaAhli dalam text field atau komponen lain
                ahli.setText(String.valueOf(tenagaAhli));*/
                ahli.setText("0");
                
                // Query SQL untuk mendapatkan total biaya keperawatan dari Ranap Paramedis dan Ralan Paramedis
                String queryRanapParamedis = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Ranap Paramedis'";
                String queryRalanParamedis = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Ralan Paramedis'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilRanapParamedis = Sequel.cariIsi(queryRanapParamedis);
                String hasilRalanParamedis = Sequel.cariIsi(queryRalanParamedis);

                // Log hasil query
//                System.out.println("Hasil Ranap Paramedis: " + hasilRanapParamedis);
//                System.out.println("Hasil Ralan Paramedis: " + hasilRalanParamedis);

                // Menghitung total biaya keperawatan
                int keperawatan = 0;
                if (hasilRanapParamedis != null && !hasilRanapParamedis.isEmpty()) {
                    keperawatan += Integer.parseInt(hasilRanapParamedis);
                }
                if (hasilRalanParamedis != null && !hasilRalanParamedis.isEmpty()) {
                    keperawatan += Integer.parseInt(hasilRalanParamedis);
                }

                // Jika hasilnya kosong, set menjadi "0"
                if (keperawatan == 0) {
                    keperawatan = 0;
                }

                // Menampilkan total biaya keperawatan dalam text field atau komponen lain
                kpr.setText(String.valueOf(keperawatan));
                
                // Query SQL untuk mendapatkan total biaya radiologi
                String queryRadiologi = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Radiologi'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilRadiologi = Sequel.cariIsi(queryRadiologi);

                // Log hasil query
//                System.out.println("Query Radiologi: " + queryRadiologi);
//                System.out.println("Hasil Radiologi: " + hasilRadiologi);

                // Memeriksa nilai kosong dan konversi ke integer
                int radiologi = 0;
                if (hasilRadiologi != null && !hasilRadiologi.isEmpty()) {
                    radiologi = Integer.parseInt(hasilRadiologi);
                }

                // Log hasil akhir
//                System.out.println("Total Radiologi: " + radiologi);

                // Menampilkan total biaya radiologi dalam text field atau komponen lain
                rad.setText(String.valueOf(radiologi));

                // Jika hasilnya kosong, set menjadi "0"
                if (radiologi == 0) {
                    rad.setText("0");
                }

                // Query SQL untuk mendapatkan total biaya laboratorium
                String queryLaboratorium = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Laborat'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilLaboratorium = Sequel.cariIsi(queryLaboratorium);

                // Log hasil query
//                System.out.println("Query Laboratorium: " + queryLaboratorium);
//                System.out.println("Hasil Laboratorium: " + hasilLaboratorium);

                // Memeriksa nilai kosong dan konversi ke integer
                int laboratorium = 0;
                if (hasilLaboratorium != null && !hasilLaboratorium.isEmpty()) {
                    laboratorium = Integer.parseInt(hasilLaboratorium);
                }

                // Log hasil akhir
//                System.out.println("Total Laboratorium: " + laboratorium);

                // Menampilkan total biaya laboratorium dalam text field atau komponen lain
                lab.setText(String.valueOf(laboratorium));

                // Jika hasilnya kosong, set menjadi "0"
                if (laboratorium == 0) {
                    lab.setText("0");
                }

                // Query SQL untuk mendapatkan total biaya kamar dan biaya registrasi
                String queryKamar = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Kamar'";
                String queryBiayaReg = "select biaya_reg from reg_periksa where no_rawat='" + norw + "'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilKamar = Sequel.cariIsi(queryKamar);
                String hasilBiayaReg = Sequel.cariIsi(queryBiayaReg);

                // Log hasil query
//                System.out.println("Query Kamar: " + queryKamar);
//                System.out.println("Hasil Kamar: " + hasilKamar);
//                System.out.println("Query Biaya Reg: " + queryBiayaReg);
//                System.out.println("Hasil Biaya Reg: " + hasilBiayaReg);

                // Menghitung total biaya kamar dan biaya registrasi
                int kamarTotal = 0;
                if (hasilKamar != null && !hasilKamar.isEmpty()) {
                    kamarTotal += Integer.parseInt(hasilKamar);
                }
                if (hasilBiayaReg != null && !hasilBiayaReg.isEmpty()) {
                    kamarTotal += Integer.parseInt(hasilBiayaReg);
                }

                // Log hasil akhir
//                System.out.println("Total Kamar: " + kamarTotal);

                // Menampilkan total biaya akomodasi dalam text field atau komponen lain
                akomodasi.setText(String.valueOf(kamarTotal));

                // Jika hasilnya kosong, set menjadi "0"
                if (kamarTotal == 0) {
                    akomodasi.setText("0");
                }

                // Query untuk mendapatkan total biaya obat kronis
                String queryObatKronis = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.nm_perawatan like '%kronis%' and billing.no_rawat='" + norw + "' and billing.status='Obat'";
                String hasilObatKronis = Sequel.cariIsi(queryObatKronis);
                if (hasilObatKronis == null || hasilObatKronis.isEmpty()) {
                    hasilObatKronis = "0";
                }
                int obatKronis = Integer.parseInt(hasilObatKronis);
                kronis.setText(String.valueOf(obatKronis));

                // Query untuk mendapatkan total biaya obat kemoterapi
                String queryObatKemoterapi = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.nm_perawatan like '%kemo%' and billing.no_rawat='" + norw + "' and billing.status='Obat'";
                String hasilObatKemoterapi = Sequel.cariIsi(queryObatKemoterapi);
                if (hasilObatKemoterapi == null || hasilObatKemoterapi.isEmpty()) {
                    hasilObatKemoterapi = "0";
                }
                int obatKemoterapi = Integer.parseInt(hasilObatKemoterapi);
                obat_kemo.setText(String.valueOf(obatKemoterapi));

                // Query untuk mendapatkan total biaya obat, retur obat, dan resep pulang
                String queryObat = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Obat'";
                String queryReturObat = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Retur Obat'";
                String queryResepPulang = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Resep Pulang'";

                String hasilObat = Sequel.cariIsi(queryObat);
                if (hasilObat == null || hasilObat.isEmpty()) {
                    hasilObat = "0";
                }
                String hasilReturObat = Sequel.cariIsi(queryReturObat);
                if (hasilReturObat == null || hasilReturObat.isEmpty()) {
                    hasilReturObat = "0";
                }
                String hasilResepPulang = Sequel.cariIsi(queryResepPulang);
                if (hasilResepPulang == null || hasilResepPulang.isEmpty()) {
                    hasilResepPulang = "0";
                }

                int totalObat = Integer.parseInt(hasilObat) + Integer.parseInt(hasilReturObat) + Integer.parseInt(hasilResepPulang);
                totalObat -= (obatKronis + obatKemoterapi);
                obat.setText(String.valueOf(totalObat));

                // Log hasil akhir
//                System.out.println("Total Obat: " + totalObat);
//                System.out.println("Total Obat Kronis: " + obatKronis);
//                System.out.println("Total Obat Kemoterapi: " + obatKemoterapi);
                
                // Query SQL untuk mendapatkan total biaya BMHP
                String queryBmhp = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Tambahan'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilBmhp = Sequel.cariIsi(queryBmhp);

                // Log hasil query
//                System.out.println("Query BMHP: " + queryBmhp);
//                System.out.println("Hasil BMHP: " + hasilBmhp);

                // Memeriksa nilai kosong dan konversi ke integer
                int bmhpTotal = 0;
                if (hasilBmhp != null && !hasilBmhp.isEmpty()) {
                    bmhpTotal = Integer.parseInt(hasilBmhp);
                }

                // Log hasil akhir
//                System.out.println("Total BMHP: " + bmhpTotal);

                // Menampilkan total biaya BMHP dalam text field atau komponen lain
                bmhp.setText(String.valueOf(bmhpTotal));

                // Jika hasilnya kosong, set menjadi "0"
                if (bmhpTotal == 0) {
                    bmhpTotal = 0;
                }
                
                // Query SQL untuk mendapatkan total biaya harian dan service
                String queryHarian = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Harian'";
                String queryService = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Service'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilHarian = Sequel.cariIsi(queryHarian);
                String hasilService = Sequel.cariIsi(queryService);

                // Log hasil query
//                System.out.println("Query Harian: " + queryHarian);
//                System.out.println("Hasil Harian: " + hasilHarian);
//                System.out.println("Query Service: " + queryService);
//                System.out.println("Hasil Service: " + hasilService);

                // Menghitung total biaya sewa alat
                int sewaAlat = 0;
                if (hasilHarian != null && !hasilHarian.isEmpty()) {
                    sewaAlat += Integer.parseInt(hasilHarian);
                }
                if (hasilService != null && !hasilService.isEmpty()) {
                    sewaAlat += Integer.parseInt(hasilService);
                }

                // Log hasil akhir
 //               System.out.println("Total Sewa Alat: " + sewaAlat);

                // Menampilkan total biaya sewa alat dalam text field atau komponen lain
                if (sewaAlat == 0) {
                    sewa_alat.setText("0");
                } else {
                    sewa_alat.setText(String.valueOf(sewaAlat));
                }

                // Query SQL untuk mendapatkan total biaya rehabilitasi
                String queryRalanTerapi = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Ralan Dokter Paramedis' and billing.nm_perawatan like '%terapi%'";
                String queryRanapTerapi = "select if(sum(billing.totalbiaya)='', '0', sum(billing.totalbiaya)) from billing where billing.no_rawat='" + norw + "' and billing.status='Ranap Dokter Paramedis' and billing.nm_perawatan like '%terapi%'";

                // Menjalankan query dan mendapatkan hasilnya
                String hasilRalanTerapi = Sequel.cariIsi(queryRalanTerapi);
                String hasilRanapTerapi = Sequel.cariIsi(queryRanapTerapi);

                // Log hasil query
//                System.out.println("Query Ralan Terapi: " + queryRalanTerapi);
//                System.out.println("Hasil Ralan Terapi: " + hasilRalanTerapi);
//                System.out.println("Query Ranap Terapi: " + queryRanapTerapi);
//                System.out.println("Hasil Ranap Terapi: " + hasilRanapTerapi);

                // Menghitung total biaya rehabilitasi
                int rehabilitasi = 0;
                if (hasilRalanTerapi != null && !hasilRalanTerapi.isEmpty()) {
                    rehabilitasi += Integer.parseInt(hasilRalanTerapi);
                }
                if (hasilRanapTerapi != null && !hasilRanapTerapi.isEmpty()) {
                    rehabilitasi += Integer.parseInt(hasilRanapTerapi);
                }

                // Log hasil akhir
//                System.out.println("Total Rehabilitasi: " + rehabilitasi);

                // Menampilkan total biaya rehabilitasi dalam text field atau komponen lain
                rehab.setText(String.valueOf(rehabilitasi));

                // Jika hasilnya kosong, set menjadi "0"
                if (rehabilitasi == 0) {
                    rehab.setText("0");
                }

//                alkes.setText((Sequel.cariIsi("select (select ifnull(sum(totalbiaya),0) from billing where nm_perawatan like '%(ALKES)%' and no_rawat='"+no_rawat.getText()+"') + (select ifnull(sum(totalbiaya),0) from billing where (nm_perawatan like '%Lensa%' OR nm_perawatan like '%LENSA%' OR nm_perawatan like '%lensa%') and no_rawat='"+no_rawat.getText()+"')")));
                alkes.setText("0");                

//                pnj.setText(Sequel.cariIsi("select (select ifnull(sum(totalbiaya),0) from billing where nm_perawatan not like '%selisih%' and no_rawat='"+no_rawat.getText()+"' and status='Tambahan') + (select ifnull(sum(totalbiaya),0) from billing where nm_perawatan like '%(PNG)%' and no_rawat='"+no_rawat.getText()+"')"));
                pnj.setText("0");
//                pl_darah.setText(Sequel.cariIsi("select ifnull(sum(totalbiaya),0) from billing where nm_perawatan like '%(DRH)%' and no_rawat='"+no_rawat.getText()+"'"));
                pl_darah.setText("0");

                ri.setText("0");

//                set jumlah darah hd
//                kt_darah.setText(Sequel.cariIsi("select jumlah from billing where nm_perawatan like '%(DRH)%' and no_rawat='"+no_rawat.getText()+"'"));
                pasien_tb.setText(Sequel.cariIsi("select no_sitb from inacbg_validasi_tb where no_rkm_medis='"+TNoRM.getText().trim()+"' "));
                hitungtotalbiaya();

                try {
                    rs = koneksi.prepareStatement("select reg_periksa.tgl_registrasi, reg_periksa.jam_reg, reg_periksa.status_lanjut, reg_periksa.status_bayar, dokter.nm_dokter from reg_periksa inner join "
                            + "dokter on reg_periksa.kd_dokter=dokter.kd_dokter where reg_periksa.no_rawat='"+no_rawat.getText()+"'").executeQuery();
                    if (rs.next()) {
                        if (rs.getString("status_lanjut").equals("Ralan")) {
                            dokterkonsul = Sequel.cariIsi("select concat(', ',dokter.nm_dokter) from reg_periksa inner "
                                    + "join konsultasi_medik on konsultasi_medik.no_rawat=reg_periksa.no_rawat inner join dokter on konsultasi_medik.kd_dokter_dikonsuli=dokter.kd_dokter where "
                                    + "reg_periksa.no_rawat='"+no_rawat.getText()+"'");
                             
                            tgl_masuk.setDate(rs.getDate("tgl_registrasi"));
                            tgl_keluar.setDate(rs.getDate("tgl_registrasi"));
                            jam_masuk.setSelectedItem(rs.getString("jam_reg").substring(0,2));
                            jam_keluar.setSelectedItem(rs.getString("jam_reg").substring(0,2));
                            menit_masuk.setSelectedItem(rs.getString("jam_reg").substring(3,5));
                            menit_keluar.setSelectedItem(rs.getString("jam_reg").substring(3,5));
                            dpjp.setText(rs.getString("nm_dokter")+dokterkonsul);
                        } else {
                            jenis_rawat.setSelectedIndex(0);

                            tgl_masuk_ranap = Sequel.cariIsi("SELECT DISTINCT(concat(tgl_masuk,' ',jam_masuk)) as jam_masuk FROM kamar_inap WHERE no_rawat='"+no_rawat.getText()+"' ORDER BY tgl_masuk ASC LIMIT 1");
                            tgl_keluar_ranap = Sequel.cariIsi("SELECT DISTINCT(concat(tgl_keluar,' ',jam_keluar)) as jam_keluar FROM kamar_inap WHERE no_rawat='"+no_rawat.getText()+"' AND tgl_keluar<>'0000-00-00' ORDER BY tgl_masuk DESC LIMIT 1");

                            if (tgl_keluar_ranap.equals("")) {
                                tgl_keluar_ranap = tgl_masuk_ranap;
                                JOptionPane.showMessageDialog(null,"PASIEN INI BELUM DIPULANGKAN !!!!!!");
                            }
                            
                            LocalDateTime tanggalWaktu1 = LocalDateTime.parse(tgl_masuk_ranap, formatter);
                            LocalDateTime tanggalWaktu2 = LocalDateTime.parse(tgl_keluar_ranap, formatter);

                            // Menghitung selisih hari
                            long selisihHari = ChronoUnit.DAYS.between(tanggalWaktu1, tanggalWaktu2);
                            long selisihJam = ChronoUnit.HOURS.between(tanggalWaktu1, tanggalWaktu2);

                            try{
                                rs3=koneksi.prepareStatement(
                                    "select dokter.nm_dokter from dpjp_ranap inner join dokter on dpjp_ranap.kd_dokter=dokter.kd_dokter where dpjp_ranap.no_rawat='"+no_rawat.getText()+"'").executeQuery();

                                while (rs3.next()) {
                                    dpjp_ranap = dpjp_ranap+rs3.getString("nm_dokter")+", ";
                                }
                            } catch (Exception e) {
                                System.out.println("Status Lanjut : "+e);
                            }

                            Valid.SetTgl(tgl_masuk,tgl_masuk_ranap.substring(0,10));
                            Valid.SetTgl(tgl_keluar,tgl_keluar_ranap.substring(0,10));
                            jam_masuk.setSelectedItem(tgl_masuk_ranap.substring(11,13));
                            jam_keluar.setSelectedItem(tgl_keluar_ranap.substring(11,13));
                            menit_masuk.setSelectedItem(tgl_masuk_ranap.substring(14,16));
                            menit_keluar.setSelectedItem(tgl_keluar_ranap.substring(14,16));
                            dpjp.setText(dpjp_ranap);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("error eklaim : "+e);
                }
                
//                diagnosa
                try {
                    rsdiagnosa = koneksi.prepareStatement("select penyakit.kd_penyakit, penyakit.nm_penyakit from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit where diagnosa_pasien.no_rawat='"+norw+"' "
                            + "order by diagnosa_pasien.prioritas asc").executeQuery();
                    i=1;
                    while (rsdiagnosa.next()) {
//                        System.out.println("data "+rsdiagnosa.getString("kd_penyakit"));
                        
                        if (i == 1) {
                            kd_diag_1.setText(rsdiagnosa.getString("kd_penyakit"));
                            nm_diag_1.setText(rsdiagnosa.getString("nm_penyakit"));
                        }
                        if (i == 2) {
                            kd_diag_2.setText(rsdiagnosa.getString("kd_penyakit"));
                            nm_diag_2.setText(rsdiagnosa.getString("nm_penyakit"));
                        }
                        if (i == 3) {
                            kd_diag_3.setText(rsdiagnosa.getString("kd_penyakit"));
                            nm_diag_3.setText(rsdiagnosa.getString("nm_penyakit"));
                        }
                        if (i == 4) {
                            kd_diag_4.setText(rsdiagnosa.getString("kd_penyakit"));
                            nm_diag_4.setText(rsdiagnosa.getString("nm_penyakit"));
                        }
                        if (i == 5) {
                            kd_diag_5.setText(rsdiagnosa.getString("kd_penyakit"));
                            nm_diag_5.setText(rsdiagnosa.getString("nm_penyakit"));
                        }
                        i++;
                    }
                } catch (Exception e) {
//                    System.out.println("ICD 10 : "+e);
                }
                
//                prosedur
                try {
                    rsprosedur = koneksi.prepareStatement("select icd9.kode, icd9.deskripsi_panjang from prosedur_pasien inner join icd9 on prosedur_pasien.kode=icd9.kode where prosedur_pasien.no_rawat='"+norw+"' "
                            + "order by prosedur_pasien.prioritas asc").executeQuery();
                    j=1;
                    while (rsprosedur.next()) {
//                        System.out.println("data "+rsprosedur.getString("kode"));
                        
                        if (j == 1) {
                            kd_psdr_1.setText(rsprosedur.getString("kode"));
                            nm_psdr_1.setText(rsprosedur.getString("deskripsi_panjang"));
                        }
                        if (j == 2) {
                            kd_psdr_2.setText(rsprosedur.getString("kode"));
                            nm_psdr_2.setText(rsprosedur.getString("deskripsi_panjang"));
                        }
                        if (j == 3) {
                            kd_psdr_3.setText(rsprosedur.getString("kode"));
                            nm_psdr_3.setText(rsprosedur.getString("deskripsi_panjang"));
                        }
                        if (j == 4) {
                            kd_psdr_4.setText(rsprosedur.getString("kode"));
                            nm_psdr_4.setText(rsprosedur.getString("deskripsi_panjang"));
                        }
                        if (j == 5) {
                            kd_psdr_5.setText(rsprosedur.getString("kode"));
                            nm_psdr_5.setText(rsprosedur.getString("deskripsi_panjang"));
                        }
                        j++;
                    }
                    
                    icd9.setText("");
                    icd9.setText(kd_psdr_1.getText().trim()+(kd_psdr_2.getText().trim().equals("") ? "" : "#"+kd_psdr_2.getText())
                                    +(kd_psdr_3.getText().trim().equals("") ? "" : "#"+kd_psdr_3.getText())+(kd_psdr_4.getText().trim().equals("") ? "" : "#"+kd_psdr_4.getText())
                                    +(kd_psdr_5.getText().trim().equals("") ? "" : "#"+kd_psdr_5.getText()));
                } catch (Exception e) {
//                    System.out.println("ICD 9 : "+e);
                }
                
               
                if (!kd_diag_1.getText().trim().equals("")) {
                   chk_diag_1.setSelected(true);
                }
                
                jLabel49.setVisible(false);                
                jLabel53.setVisible(false);
                jLabel61.setVisible(false);                
                jLabel76.setVisible(false);
                stage_2.setVisible(false);

                check_hdItemStateChanged(null);
                jenis_rawatItemStateChanged(null);
                rawat_intensifItemStateChanged(null);
                kelas_eksekutifItemStateChanged(null);
                naik_turun_kelasItemStateChanged(null);
                
                String distoles = Sequel.cariIsi("select pemeriksaan_ralan.tensi from pemeriksaan_ralan where no_rawat='" + norw + "' order by pemeriksaan_ralan.tensi desc limit 1");
                if (distoles != null && distoles.contains("/")) {
                    String sistoles = distoles.split("/")[0];
                    distoles = distoles.split("/")[1];
                    sistole.setText(sistoles);
                    distole.setText(distoles);
                } else {
                    sistole.setText(distoles);
                    distole.setText("");
                }
                
            }
            totalGroupingTersimpan(Tsep.getText());  
        } catch (Exception e) {
            System.out.println("error : "+e);
        }
    }
    
    public void hitungtotalbiaya(){
        if (pnb.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            pnb.requestFocus();
        }
        else if (pb.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            pb.requestFocus();
        }
        else if (ahli.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            ahli.requestFocus();
        }
        else if (rad.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            rad.requestFocus();
        }
        else if (rehab.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            rehab.requestFocus();
        }
        else if (obat.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            obat.requestFocus();
        }
        else if (alkes.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            alkes.requestFocus();
        }
        else if (kpr.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            kpr.requestFocus();
        }
        else if (lab.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            lab.requestFocus();
        }
        else if (akomodasi.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            akomodasi.requestFocus();
        }
        else if (kronis.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            kronis.requestFocus();
        }
        else if (bmhp.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            bmhp.requestFocus();
        }
        else if (ksl.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            ksl.requestFocus();
        }
        else if (pnb.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            pnb.requestFocus();
        }
        else if (pnj.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            pnj.requestFocus();
        }
        else if (pl_darah.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            pl_darah.requestFocus();
        }
        else if (ri.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            ri.requestFocus();
        }
        else if (obat_kemo.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            obat_kemo.requestFocus();
        }
        else if (sewa_alat.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,"DATA INI TIDAK BOLEH KOSONG DAN HANYA BOLEH ANGKA !!!!");
            sewa_alat.requestFocus();
        } else {
            total_biaya = (Integer.parseInt(pnb.getText()) + Integer.parseInt(pb.getText()) + Integer.parseInt(ksl.getText()) + Integer.parseInt(ahli.getText()) + Integer.parseInt(kpr.getText()) +  Integer.parseInt(rad.getText()) + Integer.parseInt(lab.getText()) + Integer.parseInt(alkes.getText()) + Integer.parseInt(kronis.getText()) + Integer.parseInt(obat_kemo.getText()) + Integer.parseInt(obat.getText()) + Integer.parseInt(pnj.getText()) + Integer.parseInt(pl_darah.getText()) + Integer.parseInt(bmhp.getText()) + Integer.parseInt(sewa_alat.getText()) + Integer.parseInt(rehab.getText()) + Integer.parseInt(ri.getText()) + Integer.parseInt(akomodasi.getText()) );
            total_rupiah.setText(String.valueOf(Valid.SetAngka(total_biaya)));
        }
    }
    

    public void totalGroupingTersimpan(String no_sep) {
        try {
            // Mencari data di grouping_stage2 terlebih dahulu
            String query1 = "SELECT tarif, deskripsi, code_cbg FROM inacbg_grouping_stage2 WHERE no_sep=?";
            PreparedStatement ps1 = koneksi.prepareStatement(query1);
            ps1.setString(1, no_sep);
            ResultSet rs1 = ps1.executeQuery();

            Font smallFont = new Font("Tahoma", Font.PLAIN, 12); // Change to desired font name and size
            deskripsi_grouping_tersimpan.setFont(smallFont);
            code_cbg_grouping_tersimpan.setFont(smallFont);

            if (rs1.next()) {
                double tarif = rs1.getDouble("tarif");
                String deskripsi = rs1.getString("deskripsi");
                String code_cbg = rs1.getString("code_cbg");
                total_rupiah1.setText(Valid.SetAngka(tarif));
                if (tarif < total_biaya) {
                    total_rupiah1.setForeground(Color.RED);
                } else {
                    total_rupiah1.setForeground(Color.BLACK);
                }
                // Memperbarui label
                total_grouping_tersimpan.setText(Valid.SetAngka(tarif));
                deskripsi_grouping_tersimpan.setText(deskripsi);
                code_cbg_grouping_tersimpan.setText(code_cbg);
            } else {
                // Jika tidak ditemukan di grouping_stage2, mencari di grouping_stage1
                String query2 = "SELECT tarif, deskripsi, code_cbg FROM inacbg_grouping_stage1 WHERE no_sep=?";
                PreparedStatement ps2 = koneksi.prepareStatement(query2);
                ps2.setString(1, no_sep);
                ResultSet rs2 = ps2.executeQuery();

                if (rs2.next()) {
                    double tarif = rs2.getDouble("tarif");
                    String deskripsi = rs2.getString("deskripsi");
                    String code_cbg = rs2.getString("code_cbg");
                    total_rupiah1.setText(Valid.SetAngka(tarif));
                    if (tarif < total_biaya) {
                        total_rupiah1.setForeground(Color.RED);
                    } else {
                        total_rupiah1.setForeground(Color.BLACK);
                    }
                    // Memperbarui label
                    total_grouping_tersimpan.setText(Valid.SetAngka(tarif));
                    deskripsi_grouping_tersimpan.setText(deskripsi);
                    code_cbg_grouping_tersimpan.setText(code_cbg);
                } else {
                    // Jika tidak ditemukan di grouping_stage2 dan grouping_stage1, mencari di grouping_stage12
                    String query3 = "SELECT tarif, deskripsi, code_cbg FROM inacbg_grouping_stage12 WHERE no_sep=?";
                    PreparedStatement ps3 = koneksi.prepareStatement(query3);
                    ps3.setString(1, no_sep);
                    ResultSet rs3 = ps3.executeQuery();

                    if (rs3.next()) {
                        double tarif = rs3.getDouble("tarif");
                        String deskripsi = rs3.getString("deskripsi");
                        String code_cbg = rs3.getString("code_cbg");
                        total_rupiah1.setText(Valid.SetAngka(tarif));
                        if (tarif < total_biaya) {
                            total_rupiah1.setForeground(Color.RED);
                        } else {
                            total_rupiah1.setForeground(Color.BLACK);
                        }
                        // Memperbarui label
                        total_grouping_tersimpan.setText(Valid.SetAngka(tarif));
                        deskripsi_grouping_tersimpan.setText(deskripsi);
                        code_cbg_grouping_tersimpan.setText(code_cbg);
                    } else {
                        // Jika data tidak ditemukan di ketiga tabel, set total_rupiah1, deskripsi, cbg dan label ke "Belum Ada"
                        total_rupiah1.setText("Belum Ada");
                        total_rupiah1.setForeground(Color.BLACK);
                        total_grouping_tersimpan.setText("Belum Ada");
                        deskripsi_grouping_tersimpan.setText("Belum Ada");
                        code_cbg_grouping_tersimpan.setText("Belum Ada");
                    }
                    rs3.close();
                    ps3.close();
                }
                rs2.close();
                ps2.close();
            }
            rs1.close();
            ps1.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat memuat data: " + e.getMessage());
        }
    }

    private void kirimDataKlaim() throws Exception {
        String requestJson = "{\n" +
                " \"metadata\": {\n" +
                " \"method\": \"send_claim_individual\"\n" +
                " },\n" +
                " \"data\": {\n" +
                " \"nomor_sep\": \"" + Tsep.getText().trim() + "\"\n" +
                " }\n" +
                "}";
        
        JSONObject response = inacbg.request(requestJson);
//        System.out.println("Response: " + response.toString());

        StyledDocument doc = ResponEklaim.getStyledDocument();
        Style styleBlue = ResponEklaim.addStyle("BlueBoldStyle", null);
        StyleConstants.setForeground(styleBlue, Color.BLUE);
        StyleConstants.setFontFamily(styleBlue, "Tahoma");
        StyleConstants.setBold(styleBlue, true);
        StyleConstants.setFontSize(styleBlue, 12);

        Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
        StyleConstants.setForeground(styleRed, Color.RED);
        StyleConstants.setFontFamily(styleRed, "Tahoma");
        StyleConstants.setBold(styleRed, true);
        StyleConstants.setFontSize(styleRed, 12);

        Style styleGreen = ResponEklaim.addStyle("GreenBoldStyle", null);
        StyleConstants.setForeground(styleGreen, Color.GREEN);
        StyleConstants.setFontFamily(styleGreen, "Tahoma");
        StyleConstants.setBold(styleGreen, true);
        StyleConstants.setFontSize(styleGreen, 12);

        String responseText = "Kirim DC Kemenkes\n ";
        String responseContent = response.toString() + "\n";
        String[] words = responseContent.split(" ");

        doc.insertString(doc.getLength(), responseText, styleBlue);

        for (String word : words) {
            if (word.contains("Ungroupable") || word.contains("Unrelated") || word.contains("not accepted as Primary Diagnosis")) {
                doc.insertString(doc.getLength(), word + " ", styleRed);
            } else if (word.contains("\"kemkes_dc_status\":\"sent\"")) {
                doc.insertString(doc.getLength(), word + " ", styleBlue);
            } else if (word.contains("Success") || word.contains("Berhasil")) {
                doc.insertString(doc.getLength(), word + " ", styleGreen);
            } else {
                doc.insertString(doc.getLength(), word + " ", null);
            }
        }

        if(response.has("metadata")){
            JSONObject code = response.getJSONObject("metadata");
            if(code.getInt("code")==200){
                if(Sequel.menyimpantf2("inacbg_data_terkirim","?,?","DATA EKLAIM",2,new String[]{
                    Tsep.getText(),coder
                        })==false){
                            Sequel.mengedit("inacbg_data_terkirim", "no_sep='"+Tsep.getText()+"'", "nik='"+coder+"'");
                        }                
                // JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
            }else{
                JOptionPane.showMessageDialog(null,"RESPON : "+code.getString("message"));
            }
        } else {
            System.out.println("response :\n "+response);
            System.err.println("\n The 'data' field is missing in the response.");
        }
        ResponEklaim.setCaretPosition(ResponEklaim.getDocument().getLength());
    }
 
    private void DownloadPDFKlaim() throws Exception {
        String requestJson = "{\n" +
                " \"metadata\": {\n" +
                " \"method\": \"claim_print\"\n" +
                " },\n" +
                " \"data\": {\n" +
                " \"nomor_sep\": \"" + Tsep.getText().trim() + "\"\n" +
                " }\n" +
                "}";
        JSONObject response = inacbg.request(requestJson);
        String data = response.getString("data");
        byte[] byteArray = Base64.getDecoder().decode(data);
        InputStream inputStream = new ByteArrayInputStream(byteArray);
        String Dir = "./tempPDF/" + "EKLAIM_" + Tsep.getText().trim() + ".pdf";
        File file = new File(Dir);
        file.delete();
        Path path = Paths.get(Dir);
        Files.copy(inputStream, path);
        String FileName = "EKLAIM_" + Tsep.getText().trim() + ".pdf";
        String filePath = "tempPDF/" + FileName;
        UploadPDF(FileName, "berkasrawat/pages/upload/");
        HapusPDF();
    }

    public void UploadPDF(String fileName, String uploadPath) {
        try {
            System.out.println("Upload " + fileName);
            File tmpFile = new File("tempPDF/" + fileName);
            if (!tmpFile.exists()) {
                System.out.println("Temporary file does not exist.");
                JOptionPane.showMessageDialog(null, "Temporary file does not exist.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if the file already exists on the server
            String fullUploadPath = uploadPath.endsWith("/") ? uploadPath + fileName : uploadPath + "/" + fileName;
            URL verifyUrl = new URL("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/" + fullUploadPath);
            HttpURLConnection connection = (HttpURLConnection) verifyUrl.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                int option = JOptionPane.showConfirmDialog(null, "File " + fileName + " sudah ada, yakin ingin menggantinya..???", "", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (option != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            byte[] data = FileUtils.readFileToByteArray(tmpFile);
            org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/uploadfilepdf.php?doc=" + URLEncoder.encode(uploadPath, "UTF-8"));

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", data, org.apache.http.entity.ContentType.DEFAULT_BINARY, fileName);

            postRequest.setEntity(builder.build());

            org.apache.http.HttpResponse postResponse = httpClient.execute(postRequest);

            String responseContent = EntityUtils.toString(postResponse.getEntity());

            if (postResponse.getStatusLine().getStatusCode() == 200) {
                connection = (HttpURLConnection) verifyUrl.openConnection();
                connection.setRequestMethod("GET");
                int fileSize = connection.getContentLength();

                if (fileSize > 0) {
                    boolean uploadSuccess;
                    String noSEP = Tsep.getText(); // Get value from textbox
                    String norw = Sequel.cariIsi("select no_rawat from bridging_sep where no_sep=?", noSEP);
                    String kodeberkas = Sequel.cariIsi("SELECT kode FROM master_berkas_digital WHERE nama LIKE '%E-KLAIM%'");
                    if (Sequel.cariInteger("SELECT COUNT(no_rawat) AS jumlah FROM berkas_digital_perawatan WHERE lokasi_file='pages/upload/" + fileName + "'") > 0) {
                        uploadSuccess = Sequel.mengedittf("berkas_digital_perawatan", "lokasi_file=?", "no_rawat=?,kode=?, lokasi_file=?", 4, new String[]{
                            norw, kodeberkas, "pages/upload/" + fileName, "pages/upload/" + fileName
                        });
                    } else {
                        uploadSuccess = Sequel.menyimpantf("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
                            norw, kodeberkas, "pages/upload/" + fileName
                        });
                    }
                        if (uploadSuccess) {
        /*                    ImageIcon greenCheckmarkIcon = new ImageIcon(getClass().getResource("/picture/Select.png"));
                            JLabel messageLabel = new JLabel("File RESUME " + fileName + " berhasil disimpan di berkas digital", greenCheckmarkIcon, JLabel.LEFT);
                            messageLabel.setIconTextGap(10); // Add space between the text and the icon
                            messageLabel.setHorizontalTextPosition(JLabel.RIGHT); // Ensure text is on the left and icon is on the right
                            messageLabel.setHorizontalAlignment(SwingConstants.CENTER); // Align everything to the center
                            JOptionPane.showMessageDialog(null, messageLabel, "", JOptionPane.PLAIN_MESSAGE);*/
                            StyledDocument doc = ResponEklaim.getStyledDocument();
                            Style styleBlue = ResponEklaim.addStyle("BlueBoldStyle", null);
                            StyleConstants.setForeground(styleBlue, Color.BLUE);
                            StyleConstants.setFontFamily(styleBlue, "Tahoma");
                            StyleConstants.setBold(styleBlue, true);
                            StyleConstants.setFontSize(styleBlue, 12);
                            doc.insertString(doc.getLength(), "File " + fileName + " berhasil disimpan di berkas digital\n", styleBlue);

                        // Menampilkan pilihan untuk membuka file di browser
                        int option = JOptionPane.showConfirmDialog(null, "Apakah Anda ingin membuka file " + fileName + "?", "Buka File", JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.YES_OPTION) {
                            // Open the uploaded file in the browser
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().browse(verifyUrl.toURI());
                            } else {
                                // Menambahkan pesan jika Desktop tidak mendukung
                                Style styleRed = ResponEklaim.addStyle("RedBoldStyle", null);
                                StyleConstants.setForeground(styleRed, Color.RED);
                                StyleConstants.setFontFamily(styleRed, "Tahoma");
                                StyleConstants.setBold(styleRed, true);
                                StyleConstants.setFontSize(styleRed, 12);
                                doc.insertString(doc.getLength(), "Desktop is not supported. Cannot open the file in the browser", styleRed);
                            }
                        } else {
                            // Jika pengguna memilih NO, beri tahu bahwa file tidak akan dibuka
    //                        Style styleInfo = ResponEklaim.addStyle("InfoStyle", null);
    //                        StyleConstants.setForeground(styleInfo, Color.BLACK);
    //                        StyleConstants.setFontFamily(styleInfo, "Tahoma");
    //                        StyleConstants.setFontSize(styleInfo, 12);
    //                       doc.insertString(doc.getLength(), "File tidak dibuka di browser.\n", styleInfo);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Upload " + fileName + " gagal disimpan", "", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.out.println("File not found or invalid size on server.");
                    JOptionPane.showMessageDialog(null, "Upload successful but file not found or invalid size on server.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                System.out.println("Failed to upload file, response status: " + postResponse.getStatusLine());
                JOptionPane.showMessageDialog(null, "Upload failed, server response: " + postResponse.getStatusLine(), "Warning", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e);
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void HapusPDF() {
        File file = new File("tempPDF");
        String[] myFiles;
        if (file.isDirectory()) {
            myFiles = file.list();
            for (int i = 0; i < myFiles.length; i++) {
                File myFile = new File(file, myFiles[i]);
                myFile.delete();
            }
        }
    }
    
/*    private void updateDPJPRefreshGUI() {
        dpjp.setText("");
        this.revalidate();
        this.repaint();
    } */
    
    private void updateDataDanRefreshGUI() {
        updateData();
        this.revalidate();
        this.repaint();
    }

    private void updateData() {
        // Kosongkan field diagnosa
        kd_diag_1.setText("");
        kd_diag_2.setText("");
        kd_diag_3.setText("");
        kd_diag_4.setText("");
        kd_diag_5.setText("");
        nm_diag_1.setText("");
        nm_diag_2.setText("");
        nm_diag_3.setText("");
        nm_diag_4.setText("");
        nm_diag_5.setText("");
        chk_diag_1.setSelected(false);
        // Diagnosa
        try {
            String noSEP = Tsep.getText();
            String norw = Sequel.cariIsi("select no_rawat from bridging_sep where no_sep=?", noSEP);
            rsdiagnosa = koneksi.prepareStatement("select penyakit.kd_penyakit, penyakit.nm_penyakit from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit where diagnosa_pasien.no_rawat='"+norw+"' " + "order by diagnosa_pasien.prioritas asc").executeQuery();
            i=1;
            while (rsdiagnosa.next()) {
                // System.out.println("data "+rsdiagnosa.getString("kd_penyakit"));
                if (i == 1) {
                    kd_diag_1.setText(rsdiagnosa.getString("kd_penyakit"));
                    nm_diag_1.setText(rsdiagnosa.getString("nm_penyakit"));
                }
                if (i == 2) {
                    kd_diag_2.setText(rsdiagnosa.getString("kd_penyakit"));
                    nm_diag_2.setText(rsdiagnosa.getString("nm_penyakit"));
                }
                if (i == 3) {
                    kd_diag_3.setText(rsdiagnosa.getString("kd_penyakit"));
                    nm_diag_3.setText(rsdiagnosa.getString("nm_penyakit"));
                }
                if (i == 4) {
                    kd_diag_4.setText(rsdiagnosa.getString("kd_penyakit"));
                    nm_diag_4.setText(rsdiagnosa.getString("nm_penyakit"));
                }
                if (i == 5) {
                    kd_diag_5.setText(rsdiagnosa.getString("kd_penyakit"));
                    nm_diag_5.setText(rsdiagnosa.getString("nm_penyakit"));
                }
                i++;
            }
        } catch (Exception e) {
//            System.out.println("ICD 10 : "+e);
        }

        // Prosedur
        try {
            String noSEP = Tsep.getText();
            String norw = Sequel.cariIsi("select no_rawat from bridging_sep where no_sep=?", noSEP);
            rsprosedur = koneksi.prepareStatement("select icd9.kode, icd9.deskripsi_panjang from prosedur_pasien inner join icd9 on prosedur_pasien.kode=icd9.kode where prosedur_pasien.no_rawat='"+norw+"' " + "order by prosedur_pasien.prioritas asc").executeQuery();
            j=1;
            // Kosongkan field prosedur
            kd_psdr_1.setText("");
            kd_psdr_2.setText("");
            kd_psdr_3.setText("");
            kd_psdr_4.setText("");
            kd_psdr_5.setText("");
            nm_psdr_1.setText("");
            nm_psdr_2.setText("");
            nm_psdr_3.setText("");
            nm_psdr_4.setText("");
            nm_psdr_5.setText("");
            check_hd.setSelected(false);
            while (rsprosedur.next()) {
                // System.out.println("data "+rsprosedur.getString("kode"));
                if (j == 1) {
                    kd_psdr_1.setText(rsprosedur.getString("kode"));
                    nm_psdr_1.setText(rsprosedur.getString("deskripsi_panjang"));
                }
                if (j == 2) {
                    kd_psdr_2.setText(rsprosedur.getString("kode"));
                    nm_psdr_2.setText(rsprosedur.getString("deskripsi_panjang"));
                }
                if (j == 3) {
                    kd_psdr_3.setText(rsprosedur.getString("kode"));
                    nm_psdr_3.setText(rsprosedur.getString("deskripsi_panjang"));
                }
                if (j == 4) {
                    kd_psdr_4.setText(rsprosedur.getString("kode"));
                    nm_psdr_4.setText(rsprosedur.getString("deskripsi_panjang"));
                }
                if (j == 5) {
                    kd_psdr_5.setText(rsprosedur.getString("kode"));
                    nm_psdr_5.setText(rsprosedur.getString("deskripsi_panjang"));
                }
                j++;
            }
            icd9.setText("");
            icd9.setText(kd_psdr_1.getText().trim()+(kd_psdr_2.getText().trim().equals("") ? "" : "#"+kd_psdr_2.getText())
                    +(kd_psdr_3.getText().trim().equals("") ? "" : "#"+kd_psdr_3.getText())+(kd_psdr_4.getText().trim().equals("") ? "" : "#"+kd_psdr_4.getText())
                    +(kd_psdr_5.getText().trim().equals("") ? "" : "#"+kd_psdr_5.getText()));
        } catch (Exception e) {
//            System.out.println("ICD 9 : "+e);
        }
        if (!kd_diag_1.getText().trim().equals("")) {
            chk_diag_1.setSelected(true);
        }
            if (kd_psdr_1.getText().equals("39.95")) {
            check_hd.setSelected(true);
            check_hd.setVisible(true);            
            jLabel56.setVisible(true);
            jLabel79.setVisible(true);
            pasien_hd.setVisible(true);
            kt_darah.setVisible(true);            
            }else{
            check_hd.setSelected(false);  
            check_hd.setVisible(false);             
            jLabel56.setVisible(false);
            jLabel79.setVisible(false);
            pasien_hd.setVisible(false);
            kt_darah.setVisible(false);
        }
    }

// public void tampilkanDiagnosa() {
//        ArrayList<String> namaList = new ArrayList<>();
//        ArrayList<String> kodeList = new ArrayList<>();
//
//        if (!nm_diag_1.getText().trim().isEmpty() && !kd_diag_1.getText().trim().isEmpty()) {
//            namaList.add(nm_diag_1.getText().trim());
//            kodeList.add(kd_diag_1.getText().trim());
//        }
//        if (!nm_diag_2.getText().trim().isEmpty() && !kd_diag_2.getText().trim().isEmpty()) {
//            namaList.add(nm_diag_2.getText().trim());
//            kodeList.add(kd_diag_2.getText().trim());
//        }
//        if (!nm_diag_3.getText().trim().isEmpty() && !kd_diag_3.getText().trim().isEmpty()) {
//            namaList.add(nm_diag_3.getText().trim());
//            kodeList.add(kd_diag_3.getText().trim());
//        }
//        if (!nm_diag_4.getText().trim().isEmpty() && !kd_diag_4.getText().trim().isEmpty()) {
//            namaList.add(nm_diag_4.getText().trim());
//            kodeList.add(kd_diag_4.getText().trim());
//        }
//        if (!nm_diag_5.getText().trim().isEmpty() && !kd_diag_5.getText().trim().isEmpty()) {
//            namaList.add(nm_diag_5.getText().trim());
//            kodeList.add(kd_diag_5.getText().trim());
//        }
//
//        StringBuilder html = new StringBuilder();
//        html.append("<html><div style='font-family:sans-serif; font-size:12px;'>");
//
//        for (int i = 0; i < namaList.size(); i++) {
//            html.append(namaList.get(i)).append(" ")
//                .append("<span style='background-color:#d0f0ff; color:black; padding:2px 4px; border-radius:4px;'>")
//                .append(kodeList.get(i)).append("</span>");
//
//            if (i < namaList.size() - 1) {
//                html.append("<br><hr style='border:0; border-top:1px solid #ccc; margin:4px 0;'>");
//            }
//        }
//
//        html.append("</div></html>");
//        labelDiagnosa.setText(html.toString());
//    }    

// Method untuk menampilkan daftar diagnosis ke labelDiagnosa
//private void tampilkanDiagnosa() {
//    // List untuk menampung nama dan kode diagnosis yang valid
//    ArrayList<String> namaList = new ArrayList<>();
//    ArrayList<String> kodeList = new ArrayList<>();
//
//    // Cek dan ambil data diagnosis dari input form, hanya jika tidak kosong
//    if (!nm_diag_1.getText().trim().isEmpty() && !kd_diag_1.getText().trim().isEmpty()) {
//        namaList.add(nm_diag_1.getText().trim());
//        kodeList.add(kd_diag_1.getText().trim());
//    }
//    if (!nm_diag_2.getText().trim().isEmpty() && !kd_diag_2.getText().trim().isEmpty()) {
//        namaList.add(nm_diag_2.getText().trim());
//        kodeList.add(kd_diag_2.getText().trim());
//    }
//    if (!nm_diag_3.getText().trim().isEmpty() && !kd_diag_3.getText().trim().isEmpty()) {
//        namaList.add(nm_diag_3.getText().trim());
//        kodeList.add(kd_diag_3.getText().trim());
//    }
//    if (!nm_diag_4.getText().trim().isEmpty() && !kd_diag_4.getText().trim().isEmpty()) {
//        namaList.add(nm_diag_4.getText().trim());
//        kodeList.add(kd_diag_4.getText().trim());
//    }
//    if (!nm_diag_5.getText().trim().isEmpty() && !kd_diag_5.getText().trim().isEmpty()) {
//        namaList.add(nm_diag_5.getText().trim());
//        kodeList.add(kd_diag_5.getText().trim());
//    }
//
//    // Membuat string HTML untuk ditampilkan di JLabel
//    StringBuilder html = new StringBuilder();
//    
//    // Awal tag HTML, set font Arial dan ukuran 14px
//    html.append("<html><div style='font-family:'Segoe UI'; font-size:14px;'>");
//
//    // Loop setiap diagnosis untuk ditampilkan
//    for (int i = 0; i < namaList.size(); i++) {
//        // Tampilkan nama diagnosis (tanpa bold)
//        html.append(namaList.get(i)).append(" ");
//        
//        // Tampilkan kode diagnosis dalam badge biru dan bold
//        html.append("<span style='")
//            .append("background-color:#d0f0ff; ")
//            .append("color:#003366; ")
//            .append("font-weight:bold; ")
//            .append("font-style:normal; ") // pastikan tidak miring
//            .append("padding:5px 10px; ")
//            .append("border-radius:10px; ")
//            .append("display:inline-block;'> [ ")
//            .append(kodeList.get(i))
//            .append(" ] </span>");
//
//        // Jika bukan baris terakhir, tambahkan garis pemisah abu muda
//        if (i < namaList.size() - 1) {
//            html.append("<br><hr style='border:0; border-top:0px dotted #e0e0e0; margin:4px 0;'>");
//        }
//    }
//
//    // Tutup tag HTML
//    html.append("</div></html>");
//
//    // Set isi labelDiagnosa dengan hasil HTML
//    labelDiagnosa.setText(html.toString());
//}
    
private void tampilkanDiagnosa() {
    ArrayList<String> namaList = new ArrayList<>();
    ArrayList<String> kodeList = new ArrayList<>();

    if (!nm_diag_1.getText().trim().isEmpty() && !kd_diag_1.getText().trim().isEmpty()) {
        namaList.add(nm_diag_1.getText().trim());
        kodeList.add(kd_diag_1.getText().trim());
    }
    if (!nm_diag_2.getText().trim().isEmpty() && !kd_diag_2.getText().trim().isEmpty()) {
        namaList.add(nm_diag_2.getText().trim());
        kodeList.add(kd_diag_2.getText().trim());
    }
    if (!nm_diag_3.getText().trim().isEmpty() && !kd_diag_3.getText().trim().isEmpty()) {
        namaList.add(nm_diag_3.getText().trim());
        kodeList.add(kd_diag_3.getText().trim());
    }
    if (!nm_diag_4.getText().trim().isEmpty() && !kd_diag_4.getText().trim().isEmpty()) {
        namaList.add(nm_diag_4.getText().trim());
        kodeList.add(kd_diag_4.getText().trim());
    }
    if (!nm_diag_5.getText().trim().isEmpty() && !kd_diag_5.getText().trim().isEmpty()) {
        namaList.add(nm_diag_5.getText().trim());
        kodeList.add(kd_diag_5.getText().trim());
    }

    StringBuilder html = new StringBuilder();
    html.append("<html><div style='font-family:'Segoe UI'; font-size:14px;'>");

    for (int i = 0; i < namaList.size(); i++) {
        String label = (i == 0) ? "  Primer" : "  Sekunder";

        // Nama diagnosis
        html.append(namaList.get(i)).append(" ");

        // Kode diagnosis dalam badge biru muda
        html.append("<span style='")
            .append("background-color:#d0f0ff; ")
            .append("color:#003366; ")
            .append("font-weight:bold; ")
            .append("font-style:normal; ")
            .append("padding:5px 10px; ")
            .append("border-radius:50px; ")
            .append("display:inline-block;'> [ ")
            .append(kodeList.get(i))
            .append(" ]</span> ");

        // Tambahkan label Primer/Sekunder setelah kode
        html.append("<span style='color:#999999; font-style:normal; style='font-family:'Segoe UI'; font-size:13px;'>")
            .append(label)
            .append("</span>");

        // Garis pemisah jika bukan baris terakhir
        if (i < namaList.size() - 1) {
            html.append("<br><hr style='border:0; border-top:0px dotted #e0e0e0; margin:4px 0;'>");
        }
    }

    html.append("</div></html>");
    labelDiagnosa.setText(html.toString());
}

    
    
// Method untuk menampilkan daftar diagnosis ke labelDiagnosa
private void tampilkanProsedur() {
    // List untuk menampung nama dan kode diagnosis yang valid
    ArrayList<String> namaList = new ArrayList<>();
    ArrayList<String> kodeList = new ArrayList<>();

    // Cek dan ambil data diagnosis dari input form, hanya jika tidak kosong
    if (!nm_psdr_1.getText().trim().isEmpty() && !kd_psdr_1.getText().trim().isEmpty()) {
        namaList.add(nm_psdr_1.getText().trim());
        kodeList.add(kd_psdr_1.getText().trim());
    }
    if (!nm_psdr_2.getText().trim().isEmpty() && !kd_psdr_2.getText().trim().isEmpty()) {
        namaList.add(nm_psdr_2.getText().trim());
        kodeList.add(kd_psdr_2.getText().trim());
    }
    if (!nm_psdr_3.getText().trim().isEmpty() && !kd_psdr_3.getText().trim().isEmpty()) {
        namaList.add(nm_psdr_3.getText().trim());
        kodeList.add(kd_psdr_3.getText().trim());
    }
    if (!nm_psdr_4.getText().trim().isEmpty() && !kd_psdr_4.getText().trim().isEmpty()) {
        namaList.add(nm_psdr_4.getText().trim());
        kodeList.add(kd_psdr_4.getText().trim());
    }
    if (!nm_psdr_5.getText().trim().isEmpty() && !kd_psdr_5.getText().trim().isEmpty()) {
        namaList.add(nm_psdr_5.getText().trim());
        kodeList.add(kd_psdr_5.getText().trim());
    }

    // Membuat string HTML untuk ditampilkan di JLabel
    StringBuilder html = new StringBuilder();
    
    // Awal tag HTML, set font Arial dan ukuran 14px
    html.append("<html><div style='font-family:'Segoe UI'; font-size:14px;'>");

    // Loop setiap diagnosis untuk ditampilkan
    for (int i = 0; i < namaList.size(); i++) {
        // Tampilkan nama diagnosis (tanpa bold)
        html.append(namaList.get(i)).append(" ");
        
        // Tampilkan kode diagnosis dalam badge biru dan bold
        html.append("<span style='")
            .append("background-color:#d0f0ff; ")
            .append("color:#003366; ")
            .append("font-weight:bold; ")
            .append("font-style:normal; ") // pastikan tidak miring
            .append("padding:5px 10px; ")
            .append("border-radius:50px; ")
            .append("display:inline-block;'> [ ")
            .append(kodeList.get(i))
            .append(" ]</span>");

        // Jika bukan baris terakhir, tambahkan garis pemisah abu muda
        if (i < namaList.size() - 1) {
            html.append("<br><hr style='border:0; border-top:0px dotted #e0e0e0; margin:4px 0;'>");
        }
    }

    // Tutup tag HTML
    html.append("</div></html>");

    // Set isi labelDiagnosa dengan hasil HTML
    labelProsedur.setText(html.toString());
}
    
}
