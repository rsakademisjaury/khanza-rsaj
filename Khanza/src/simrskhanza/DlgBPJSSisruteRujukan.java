/*
 * DlgBPJSSisruteRujukan.java
 *
 * Revisi form Integrasi Sistem Rujukan BPJS Kesehatan dengan Sisrute dan
 * SATUSEHAT Rujukan untuk SIMRS Khanza.
 *
 * Acuan revisi:
 * - Postman Docs API Integrasi Satu Sehat Rujukan:
 *   FKTP/PCARE  : https://apijkn-dev.bpjs-kesehatan.go.id/pcare-sisrute-rest
 *   FKRTL/VCLAIM: https://apijkn-dev.bpjs-kesehatan.go.id/vclaim-sisrute-rest
 * - Postman Collection SATUSEHAT Rujukan Pasien:
 *   Rawat Jalan versi 27012026
 *   Rawat Inap versi 11022026
 *   IGD/Rawat Darurat versi 11022026
 *
 * Cara pasang:
 * 1. Simpan file ini di src/simrskhanza/DlgBPJSSisruteRujukan.java
 * 2. Clean and Build.
 * 3. Panggil dari frmUtama:
 *      if(dlgBPJSSisruteRujukan==null){
 *          dlgBPJSSisruteRujukan=new DlgBPJSSisruteRujukan(this,false);
 *      }
 *      // Ukuran otomatis mengikuti area layar monitor user.
 *      dlgBPJSSisruteRujukan.setVisible(true);
 *
 * Catatan teknis:
 * - Form ini dibuat mandiri dengan widget.* Khanza (TextBox, Button, ComboBox, Table, ScrollPane, Label, PanelBiasa) agar tampilan konsisten tanpa mengganggu source Khanza lain.
 * - Token SATUSEHAT memakai OAuth2 bearer token sesuai pola Postman Collection.
 * - Auth BPJS Sisrute memakai X-cons-id, X-timestamp, X-signature, user_key.
 * - Tidak ada credential contoh yang ditanam di source; isi credential dari environment RS.
 */
package simrskhanza;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import fungsi.koneksiDB;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import widget.Button;
import widget.CekBox;
import widget.ComboBox;
import widget.Label;
import widget.PanelBiasa;
import widget.ScrollPane;
import widget.Table;
import widget.TextBox;
import widget.panelisi;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.RenderingHints;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class DlgBPJSSisruteRujukan extends JDialog {

    private final Color BLUE = new Color(20, 86, 148);
    private final Color GREEN = new Color(0, 145, 128);
    private final Color RED = new Color(201, 71, 71);
    private final Color BG = new Color(247, 250, 252);
    private final Color BORDER = new Color(214, 225, 236);
    private final Font FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font FONT_BOLD = new Font("Segoe UI Semibold", Font.BOLD, 12);

    private final Connection koneksi = koneksiDB.condb();

    private ComboBox cmbBpjsMode;
    private TextBox txtBpjsBaseUrl;
    private TextBox txtBpjsConsId;
    private JPasswordField txtBpjsSecret;
    private TextBox txtBpjsUserKey;

    private TextBox txtAuthUrl;
    private TextBox txtFhirBaseUrl;
    private TextBox txtClientId;
    private JPasswordField txtClientSecret;
    private TextBox txtToken;

    private ComboBox cmbJenisRujukan;
    private TextBox txtNoRawat;
    private TextBox txtNamaPasien;
    private TextBox txtNoSep;
    private TextBox txtNoKunjungan;
    private TextBox txtTanggal;
    private TextBox txtTanggalRencana;
    private TextBox txtOrgPerujuk;
    private TextBox txtNamaOrgPerujuk;
    private TextBox txtOrgTujuan;
    private TextBox txtNamaOrgTujuan;
    private TextBox txtOrgBpjs;
    private TextBox txtPatientId;
    private TextBox txtCoverageNo;
    private TextBox txtEncounterRef;
    private TextBox txtPractitionerId;
    private TextBox txtDiagnosisCode;
    private TextBox txtDiagnosisDisplay;
    private TextBox txtDiagnosisSekunderCode;
    private TextBox txtDiagnosisSekunderDisplay;
    private TextBox txtSpecialityCode;
    private TextBox txtSpecialityDisplay;
    private TextBox txtPpkDirujuk;
    private TextBox txtKdppkSSTujuan;
    private TextBox txtKodeFaskesSS;
    private TextBox txtPoliRujukan;
    private TextBox txtTipeRujukan;
    private TextBox txtJnsPelayanan;
    private TextBox txtCatatan;
    private TextBox txtUser;
    private TextBox txtProvCode;
    private TextBox txtProvDisplay;
    private TextBox txtKabCode;
    private TextBox txtKabDisplay;
    private TextBox txtSarana;
    private TextBox txtManagementCode;
    private TextBox txtManagementDisplay;

    private DefaultTableModel modelKriteria;
    private Table tbKriteria;
    private DefaultTableModel modelFaskes;
    private Table tbFaskes;
    private DefaultTableModel modelLog;
    private Table tbLog;

    private JTextArea txtJsonRequest;
    private JTextArea txtJsonResponse;
    private Label lblStatus;
    private boolean autoScreenSizeApplying = false;

    public DlgBPJSSisruteRujukan(Frame parent, boolean modal) {
        super(parent, modal);
        initLookAndFeel();
        initComponents();
        initCustomComponents();
        installAutoScreenSizeBehavior();
        setDefaultValues();
        initShortcut();
    }

    /**
     * Memaksa ukuran dialog mengikuti area kerja monitor setiap kali form ditampilkan.
     * Ini penting karena frmUtama Khanza kadang masih memanggil setSize(1200,720)
     * sebelum setVisible(true), sehingga ukuran otomatis di constructor bisa tertimpa.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            applyAutoScreenSize();
        }
        super.setVisible(visible);
        if (visible && !isModal()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    applyAutoScreenSize();
                }
            });
        }
    }

    private void installAutoScreenSizeBehavior() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                applyAutoScreenSize();
            }
        });
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // gunakan look and feel default
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the NetBeans Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("BPJS Sisrute & SATUSEHAT Rujukan - SIMRS Khanza");
        getContentPane().setLayout(new java.awt.BorderLayout());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initCustomComponents() {
        setTitle("BPJS Sisrute & SATUSEHAT Rujukan - SIMRS Khanza");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel center = newPanel(new BorderLayout(10, 10));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(10, 14, 8, 14));
        center.add(createAuthPanel(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_BOLD);
        tabs.addTab("1. Data Rujukan", createTabData());
        tabs.addTab("2. BPJS Sisrute", createTabBPJS());
        tabs.addTab("3. SATUSEHAT Postman", createTabSatuSehat());
        tabs.addTab("4. JSON / Response", createTabJson());
        tabs.addTab("Log", createTabLog());
        center.add(tabs, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        pack();
        applyAutoScreenSize();
    }

    /**
     * Menyesuaikan ukuran dialog dengan area layar monitor user saat form dibuka.
     * Dibuat di luar guarded block NetBeans agar Design view tetap aman.
     */
    private void applyAutoScreenSize() {
        if (autoScreenSizeApplying) {
            return;
        }
        autoScreenSizeApplying = true;
        try {
            GraphicsConfiguration gc = getGraphicsConfiguration();
            if (gc == null && getOwner() != null) {
                gc = getOwner().getGraphicsConfiguration();
            }
            if (gc == null) {
                gc = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration();
            }

            Rectangle bounds = gc.getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

            // Pakai seluruh area kerja monitor, dikurangi taskbar Windows.
            // Tidak memakai margin besar supaya dialog benar-benar melebar mengikuti monitor.
            int x = bounds.x + screenInsets.left;
            int y = bounds.y + screenInsets.top;
            int w = bounds.width - screenInsets.left - screenInsets.right;
            int h = bounds.height - screenInsets.top - screenInsets.bottom;

            if (w < 760) {
                w = Math.max(640, bounds.width);
                x = bounds.x;
            }
            if (h < 560) {
                h = Math.max(520, bounds.height);
                y = bounds.y;
            }

            setMinimumSize(new Dimension(Math.min(900, w), Math.min(580, h)));
            setPreferredSize(new Dimension(w, h));
            setBounds(x, y, w, h);
            validate();
            repaint();
        } catch (Exception e) {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int w = Math.max(760, screen.width);
            int h = Math.max(560, screen.height - 40);
            setMinimumSize(new Dimension(Math.min(900, w), Math.min(580, h)));
            setSize(w, h);
            setLocation(0, 0);
            validate();
            repaint();
        } finally {
            autoScreenSizeApplying = false;
        }
    }

    private JPanel createHeaderPanel() {
        JPanel header = newPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 18, 12, 18));

        Label title = newLabel("Sistem Rujukan BPJS Sisrute & SATUSEHAT Rujukan");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 21));
        title.setForeground(BLUE);
        Label sub = newLabel("Mode PCARE/FKTP dan VCLAIM/FKRTL + template FHIR Task/Bundle sesuai Postman Collection Rujukan Pasien");
        sub.setFont(FONT);
        sub.setForeground(new Color(82, 96, 110));

        JPanel titlePanel = newPanel(new BorderLayout(0, 3));
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub, BorderLayout.CENTER);

        JPanel badge = newPanelIsi(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        badge.setOpaque(false);
        badge.add(pill("PCARE", GREEN));
        badge.add(pill("VCLAIM", BLUE));
        badge.add(pill("FHIR TASK", new Color(92, 122, 184)));
        badge.add(pill("BUNDLE", new Color(120, 84, 172)));

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private Label pill(String text, Color color) {
        Label l = newLabel(text);
        l.setOpaque(true);
        l.setBackground(color);
        l.setForeground(Color.WHITE);
        l.setFont(FONT_BOLD);
        l.setBorder(new EmptyBorder(6, 12, 6, 12));
        return l;
    }

    private JPanel createAuthPanel() {
        JPanel p = card(new GridBagLayout());
        p.setBorder(new EmptyBorder(10, 12, 10, 12));
        GridBagConstraints c = gbc();

        cmbBpjsMode = newComboBox(new String[]{
            "VCLAIM / FKRTL / RS - vclaim-sisrute-rest",
            "PCARE / FKTP - pcare-sisrute-rest"
        });
        txtBpjsBaseUrl = newTextBox();
        txtBpjsConsId = newTextBox();
        txtBpjsSecret = newPasswordField();
        txtBpjsUserKey = newTextBox();
        txtAuthUrl = newTextBox();
        txtFhirBaseUrl = newTextBox();
        txtClientId = newTextBox();
        txtClientSecret = newPasswordField();
        txtToken = newTextBox();

        Button btnDefaultBpjs = blueButton("Default BPJS");
        btnDefaultBpjs.addActionListener((ActionEvent e) -> applyDefaultBpjsUrl());
        Button btnToken = greenButton("Generate Token SATUSEHAT");
        btnToken.addActionListener((ActionEvent e) -> generateToken());

        int row = 0;
        addLabel(p, "Mode BPJS", 0, row, c); addField(p, cmbBpjsMode, 1, row, 2, c);
        addLabel(p, "Base URL BPJS", 3, row, c); addField(p, txtBpjsBaseUrl, 4, row, 4, c); addField(p, btnDefaultBpjs, 8, row, 1, c);
        row++;
        addLabel(p, "X-cons-id", 0, row, c); addField(p, txtBpjsConsId, 1, row, 2, c);
        addLabel(p, "Secret BPJS", 3, row, c); addField(p, txtBpjsSecret, 4, row, 2, c);
        addLabel(p, "user_key", 6, row, c); addField(p, txtBpjsUserKey, 7, row, 2, c);
        row++;
        addLabel(p, "Auth URL SATUSEHAT", 0, row, c); addField(p, txtAuthUrl, 1, row, 3, c);
        addLabel(p, "FHIR Base URL", 4, row, c); addField(p, txtFhirBaseUrl, 5, row, 4, c);
        row++;
        addLabel(p, "Client ID", 0, row, c); addField(p, txtClientId, 1, row, 2, c);
        addLabel(p, "Client Secret", 3, row, c); addField(p, txtClientSecret, 4, row, 2, c);
        addLabel(p, "Bearer Token", 6, row, c); addField(p, txtToken, 7, row, 1, c); addField(p, btnToken, 8, row, 1, c);

        cmbBpjsMode.addActionListener((ActionEvent e) -> applyDefaultBpjsUrl());
        return p;
    }

    private JPanel createTabData() {
        JPanel wrapper = newPanel(new BorderLayout(10, 10));
        wrapper.setBackground(BG);
        JPanel form = card(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = gbc();

        cmbJenisRujukan = newComboBox(new String[]{"Rawat Jalan", "Rawat Inap", "IGD / Rawat Darurat"});
        txtNoRawat = newTextBox();
        txtNamaPasien = newTextBox();
        txtNoSep = newTextBox();
        txtNoKunjungan = newTextBox();
        txtTanggal = newTextBox();
        txtTanggalRencana = newTextBox();
        txtOrgPerujuk = newTextBox();
        txtNamaOrgPerujuk = newTextBox();
        txtOrgTujuan = newTextBox();
        txtNamaOrgTujuan = newTextBox();
        txtOrgBpjs = newTextBox();
        txtPatientId = newTextBox();
        txtCoverageNo = newTextBox();
        txtEncounterRef = newTextBox();
        txtPractitionerId = newTextBox();
        txtDiagnosisCode = newTextBox();
        txtDiagnosisDisplay = newTextBox();
        txtDiagnosisSekunderCode = newTextBox();
        txtDiagnosisSekunderDisplay = newTextBox();
        txtSpecialityCode = newTextBox();
        txtSpecialityDisplay = newTextBox();
        txtPpkDirujuk = newTextBox();
        txtKdppkSSTujuan = newTextBox();
        txtKodeFaskesSS = newTextBox();
        txtPoliRujukan = newTextBox();
        txtTipeRujukan = newTextBox("0");
        txtJnsPelayanan = newTextBox("2");
        txtCatatan = newTextBox();
        txtUser = newTextBox();
        txtProvCode = newTextBox("73");
        txtProvDisplay = newTextBox("SULAWESI SELATAN");
        txtKabCode = newTextBox();
        txtKabDisplay = newTextBox();
        txtSarana = newTextBox();
        txtManagementCode = newTextBox();
        txtManagementDisplay = newTextBox();

        int row = 0;
        addSection(form, "Data Kunjungan", row++, c);
        JPanel panelNoRawat = newPanel(new BorderLayout(5, 0));
        panelNoRawat.setOpaque(false);
        panelNoRawat.add(txtNoRawat, BorderLayout.CENTER);
        Button btnAmbilSimrsInline = greenButton("Ambil");
        btnAmbilSimrsInline.setToolTipText("Ambil otomatis data rujukan dari SIMRS Khanza berdasarkan No. Rawat");
        btnAmbilSimrsInline.addActionListener((ActionEvent e) -> ambilDataDariSimrsKhanza());
        panelNoRawat.add(btnAmbilSimrsInline, BorderLayout.EAST);
        addRow2(form, row++, c, "Jenis Rujukan", cmbJenisRujukan, "No. Rawat", panelNoRawat);
        addRow2(form, row++, c, "Nama Pasien", txtNamaPasien, "No. SEP / PCare", txtNoSep);
        addRow2(form, row++, c, "No. Kunjungan", txtNoKunjungan, "Tanggal Rujukan", txtTanggal);
        addRow2(form, row++, c, "Tanggal Rencana", txtTanggalRencana, "User", txtUser);
        addRow2(form, row++, c, "Catatan", txtCatatan, "", newLabel(""));

        addSection(form, "Identitas SATUSEHAT", row++, c);
        addRow2(form, row++, c, "Organization Perujuk", txtOrgPerujuk, "Nama Perujuk", txtNamaOrgPerujuk);
        addRow2(form, row++, c, "Organization Tujuan", txtOrgTujuan, "Nama Tujuan", txtNamaOrgTujuan);
        addRow2(form, row++, c, "Organization BPJS", txtOrgBpjs, "Patient ID", txtPatientId);
        addRow2(form, row++, c, "Coverage / No Kartu", txtCoverageNo, "Encounter Ref", txtEncounterRef);
        addRow2(form, row++, c, "Practitioner ID", txtPractitionerId, "Kode Faskes SS", txtKodeFaskesSS);

        addSection(form, "Diagnosa, Spesialis, dan Jejaring", row++, c);
        addRow2(form, row++, c, "Diagnosa Utama", txtDiagnosisCode, "Display Diagnosa", txtDiagnosisDisplay);
        addRow2(form, row++, c, "Diagnosa Sekunder", txtDiagnosisSekunderCode, "Display Sekunder", txtDiagnosisSekunderDisplay);
        addRow2(form, row++, c, "Kode Spesialis", txtSpecialityCode, "Nama Spesialis", txtSpecialityDisplay);
        addRow2(form, row++, c, "Provinsi", txtProvCode, "Nama Provinsi", txtProvDisplay);
        addRow2(form, row++, c, "Kab/Kota", txtKabCode, "Nama Kab/Kota", txtKabDisplay);
        addRow2(form, row++, c, "Sarana", txtSarana, "Poli Rujukan", txtPoliRujukan);
        addRow2(form, row++, c, "PPK Dirujuk", txtPpkDirujuk, "KDPPK SS Tujuan", txtKdppkSSTujuan);
        addRow2(form, row++, c, "Kode Layanan", txtManagementCode, "Display Layanan", txtManagementDisplay);

        JPanel right = newPanel(new BorderLayout(8, 8));
        right.setBackground(BG);
        right.add(infoPanel(
                "Gunakan tombol Ambil dari SIMRS Khanza untuk data lokal. Jika Patient ID, Practitioner ID, atau Encounter masih kosong, gunakan tombol Ambil dari API SATUSEHAT berdasarkan NIK/no_rawat.\n\n"
              + "Alur yang disiapkan:\n"
              + "1) BPJS Sisrute: Get Kriteria -> Get Faskes -> Insert Rujukan -> Delete Rujukan.\n"
              + "2) SATUSEHAT Postman: Generate Token -> Task Pra Permintaan -> Task Kandidat -> Bundle/ServiceRequest.\n\n"
              + "Rawat Jalan pada dokumen BPJS berjalan lewat BPJS lalu diteruskan ke Satu Sehat Rujukan. Rawat Inap dan IGD mengirim langsung ke Satu Sehat Rujukan."
        ), BorderLayout.NORTH);

        JPanel actions = card(new GridBagLayout());
        actions.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints ac = gbc();
        Button btnAmbilSimrs = greenButton("Ambil dari SIMRS Khanza");
        btnAmbilSimrs.setToolTipText("Mengisi No SEP, Nama Pasien, No Kartu, Diagnosa, Patient ID, Practitioner ID, dan Encounter dari database Khanza");
        btnAmbilSimrs.addActionListener((ActionEvent e) -> ambilDataDariSimrsKhanza());
        Button btnAmbilSatuSehatApi = greenButton("Ambil Patient/Practitioner API");
        btnAmbilSatuSehatApi.setToolTipText("Ambil Patient ID dan Practitioner ID langsung dari API SATUSEHAT berdasarkan NIK pasien dan NIK dokter");
        btnAmbilSatuSehatApi.addActionListener((ActionEvent e) -> ambilPatientPractitionerDariApiSatuSehat());
        Button btnAmbilEncounterApi = blueButton("Ambil Encounter API");
        btnAmbilEncounterApi.setToolTipText("Ambil Encounter langsung dari API SATUSEHAT berdasarkan identifier no_rawat dan Organization Perujuk");
        btnAmbilEncounterApi.addActionListener((ActionEvent e) -> ambilEncounterDariApiSatuSehat());
        Button btnAmbilSemuaApi = greenButton("Ambil Semua SS API");
        btnAmbilSemuaApi.setToolTipText("Ambil Patient ID, Practitioner ID, dan Encounter dari API SATUSEHAT");
        btnAmbilSemuaApi.addActionListener((ActionEvent e) -> ambilSemuaDariApiSatuSehat());
        Button btnApplyJenis = blueButton("Set Default Jenis Rujukan");
        btnApplyJenis.addActionListener((ActionEvent e) -> applyJenisDefaults());
        Button btnSeed = greenButton("Isi Kriteria Default");
        btnSeed.addActionListener((ActionEvent e) -> seedCriteriaByJenis());
        Button btnBpjsInsert = blueButton("Preview JSON BPJS Insert");
        btnBpjsInsert.addActionListener((ActionEvent e) -> setRequest(buildBpjsInsertJson()));
        Button btnTaskPre = greenButton("Preview Task Pra Permintaan");
        btnTaskPre.addActionListener((ActionEvent e) -> setRequest(buildTaskPraPermintaanJson()));
        Button btnTaskCandidate = greenButton("Preview Task Kandidat");
        btnTaskCandidate.addActionListener((ActionEvent e) -> setRequest(buildTaskKandidatJson()));
        Button btnBundle = greenButton("Preview Bundle SATUSEHAT");
        btnBundle.addActionListener((ActionEvent e) -> setRequest(buildBundleRujukanJson()));

        addField(actions, btnAmbilSimrs, 0, 0, 2, ac);
        addField(actions, btnAmbilSatuSehatApi, 0, 1, 2, ac);
        addField(actions, btnAmbilEncounterApi, 0, 2, 2, ac);
        addField(actions, btnAmbilSemuaApi, 0, 3, 2, ac);
        addField(actions, btnApplyJenis, 0, 4, 2, ac);
        addField(actions, btnSeed, 0, 5, 2, ac);
        addField(actions, btnBpjsInsert, 0, 6, 2, ac);
        addField(actions, btnTaskPre, 0, 7, 2, ac);
        addField(actions, btnTaskCandidate, 0, 8, 2, ac);
        addField(actions, btnBundle, 0, 9, 2, ac);
        right.add(actions, BorderLayout.CENTER);

        JPanel toolbar = card(new FlowLayout(FlowLayout.LEFT, 8, 8));
        Button btnAmbilSimrsTop = greenButton("Ambil dari SIMRS Khanza");
        btnAmbilSimrsTop.setToolTipText("Isi otomatis data dari Khanza berdasarkan No. Rawat");
        btnAmbilSimrsTop.addActionListener((ActionEvent e) -> ambilDataDariSimrsKhanza());
        toolbar.add(btnAmbilSimrsTop);
        Button btnAmbilSatuSehatApiTop = greenButton("Ambil Patient/Practitioner dari API SATUSEHAT");
        btnAmbilSatuSehatApiTop.setToolTipText("Ambil Patient ID dan Practitioner ID langsung ke SATUSEHAT berdasarkan NIK");
        btnAmbilSatuSehatApiTop.addActionListener((ActionEvent e) -> ambilPatientPractitionerDariApiSatuSehat());
        toolbar.add(btnAmbilSatuSehatApiTop);
        Button btnAmbilEncounterApiTop = blueButton("Ambil Encounter dari API");
        btnAmbilEncounterApiTop.setToolTipText("Ambil Encounter dari SATUSEHAT berdasarkan no_rawat dan Organization Perujuk");
        btnAmbilEncounterApiTop.addActionListener((ActionEvent e) -> ambilEncounterDariApiSatuSehat());
        toolbar.add(btnAmbilEncounterApiTop);
        toolbar.add(newLabel("Isi No. Rawat, klik Ambil SIMRS, lalu gunakan tombol API jika Patient/Practitioner/Encounter masih kosong."));

        cmbJenisRujukan.addActionListener((ActionEvent e) -> applyJenisDefaults());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, right);
        split.setResizeWeight(0.72);
        split.setBorder(null);

        ScrollPane scrollTabData = newScrollPane(split);
        scrollTabData.setBorder(null);
        scrollTabData.setVerticalScrollBarPolicy(ScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollTabData.setHorizontalScrollBarPolicy(ScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTabData.getVerticalScrollBar().setUnitIncrement(18);
        scrollTabData.getHorizontalScrollBar().setUnitIncrement(18);

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(scrollTabData, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTabBPJS() {
        JPanel wrapper = newPanel(new BorderLayout(10, 10));
        wrapper.setBackground(BG);
        JPanel top = card(new FlowLayout(FlowLayout.LEFT, 8, 8));

        Button btnGetKriteria = blueButton("Get Kriteria Rujukan");
        btnGetKriteria.addActionListener((ActionEvent e) -> bpjsGetKriteria());
        Button btnGetFaskes = blueButton("Get Faskes Rekomendasi");
        btnGetFaskes.addActionListener((ActionEvent e) -> bpjsGetFaskes());
        Button btnInsert = greenButton("Insert / Kirim Rujukan");
        btnInsert.addActionListener((ActionEvent e) -> bpjsInsertRujukan());
        Button btnDelete = redButton("Delete Rujukan");
        btnDelete.addActionListener((ActionEvent e) -> bpjsDeleteRujukan());
        Button btnPreviewDelete = grayButton("Preview JSON Delete");
        btnPreviewDelete.addActionListener((ActionEvent e) -> setRequest(buildBpjsDeleteJson()));

        top.add(btnGetKriteria);
        top.add(btnGetFaskes);
        top.add(btnInsert);
        top.add(btnPreviewDelete);
        top.add(btnDelete);

        modelKriteria = new DefaultTableModel(new Object[]{"Pilih", "Link/Kode", "Pertanyaan/Kriteria", "Tipe", "Jawaban"}, 0) {
            Class[] types = new Class[]{Boolean.class, String.class, String.class, String.class, String.class};
            @Override public Class getColumnClass(int columnIndex) { return types[columnIndex]; }
            @Override public boolean isCellEditable(int row, int col) { return true; }
        };
        tbKriteria = newTable(modelKriteria);
        setupTable(tbKriteria);
        tbKriteria.getColumnModel().getColumn(0).setMaxWidth(55);
        tbKriteria.getColumnModel().getColumn(1).setPreferredWidth(100);
        tbKriteria.getColumnModel().getColumn(2).setPreferredWidth(360);
        tbKriteria.getColumnModel().getColumn(3).setPreferredWidth(90);
        tbKriteria.getColumnModel().getColumn(4).setPreferredWidth(180);

        modelFaskes = new DefaultTableModel(new Object[]{"Kode SS", "KDPPK", "Nama Faskes", "Alamat", "Kelas", "Jarak", "Jadwal", "Kapasitas", "Telp"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tbFaskes = newTable(modelFaskes);
        setupTable(tbFaskes);
        tbFaskes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbFaskes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbFaskes.getSelectedRow() >= 0) {
                int row = tbFaskes.convertRowIndexToModel(tbFaskes.getSelectedRow());
                txtKodeFaskesSS.setText(safe(modelFaskes.getValueAt(row, 0)));
                txtKdppkSSTujuan.setText(safe(modelFaskes.getValueAt(row, 1)));
                txtPpkDirujuk.setText(safe(modelFaskes.getValueAt(row, 1)));
                txtOrgTujuan.setText(firstNonEmpty(txtOrgTujuan.getText(), safe(modelFaskes.getValueAt(row, 0))));
                txtNamaOrgTujuan.setText(safe(modelFaskes.getValueAt(row, 2)));
                appendStatus("Faskes dipilih: " + safe(modelFaskes.getValueAt(row, 2)));
            }
        });

        JSplitPane splitTables = new JSplitPane(JSplitPane.VERTICAL_SPLIT, newScrollPane(tbKriteria), newScrollPane(tbFaskes));
        splitTables.setResizeWeight(0.50);
        splitTables.setBorder(null);

        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(splitTables, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTabSatuSehat() {
        JPanel wrapper = newPanel(new BorderLayout(10, 10));
        wrapper.setBackground(BG);

        JPanel top = card(new FlowLayout(FlowLayout.LEFT, 8, 8));
        Button btnToken = greenButton("1. Generate Token");
        btnToken.addActionListener((ActionEvent e) -> generateToken());
        Button btnTaskPra = blueButton("2. POST Task Pra Permintaan");
        btnTaskPra.addActionListener((ActionEvent e) -> satusehatPost("/Task", buildTaskPraPermintaanJson(), "POST TASK PRA"));
        Button btnTaskKandidat = blueButton("3. POST Task Kandidat");
        btnTaskKandidat.addActionListener((ActionEvent e) -> satusehatPost("/Task", buildTaskKandidatJson(), "POST TASK KANDIDAT"));
        Button btnBundle = greenButton("4. POST Bundle Rujukan");
        btnBundle.addActionListener((ActionEvent e) -> satusehatPost("", buildBundleRujukanJson(), "POST BUNDLE RUJUKAN"));
        Button btnServiceRequest = greenButton("POST ServiceRequest");
        btnServiceRequest.addActionListener((ActionEvent e) -> {
            if (validServiceRequestInput()) {
                satusehatPost("/ServiceRequest", buildServiceRequestJson(false), "POST SERVICEREQUEST");
            }
        });
        Button btnGetServiceRequest = grayButton("GET ServiceRequest ID");
        btnGetServiceRequest.addActionListener((ActionEvent e) -> getServiceRequestById());

        top.add(btnToken);
        top.add(btnTaskPra);
        top.add(btnTaskKandidat);
        top.add(btnBundle);
        top.add(btnServiceRequest);
        top.add(btnGetServiceRequest);

        JPanel info = infoPanel(
                "Template SATUSEHAT mengikuti alur Postman Collection: Generate Token memakai {{auth_url}}/accesstoken?grant_type=client_credentials, "
              + "pra permintaan dan kandidat memakai POST {{base_url}}/Task, Bundle memakai POST {{base_url}}, dan ServiceRequest memakai POST/GET {{base_url}}/ServiceRequest. "
              + "Nilai UUID dibuat otomatis saat tombol preview/kirim dijalankan."
        );

        Table tbKriteriaSS = newTable(modelKriteria);
        setupTable(tbKriteriaSS);
        tbKriteriaSS.getColumnModel().getColumn(0).setMaxWidth(55);
        tbKriteriaSS.getColumnModel().getColumn(1).setPreferredWidth(100);
        tbKriteriaSS.getColumnModel().getColumn(2).setPreferredWidth(360);
        tbKriteriaSS.getColumnModel().getColumn(3).setPreferredWidth(90);
        tbKriteriaSS.getColumnModel().getColumn(4).setPreferredWidth(180);

        JPanel mid = newPanel(new BorderLayout(8, 8));
        mid.setOpaque(false);
        mid.add(info, BorderLayout.NORTH);
        mid.add(newScrollPane(tbKriteriaSS), BorderLayout.CENTER);

        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(mid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTabJson() {
        JPanel wrapper = newPanel(new BorderLayout(10, 10));
        wrapper.setBackground(BG);

        txtJsonRequest = new JTextArea();
        txtJsonResponse = new JTextArea();
        txtJsonRequest.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtJsonResponse.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtJsonRequest.setLineWrap(false);
        txtJsonResponse.setLineWrap(false);
        txtJsonRequest.setTabSize(2);
        txtJsonResponse.setTabSize(2);

        JPanel buttons = card(new FlowLayout(FlowLayout.LEFT, 8, 8));
        Button btnClearReq = grayButton("Kosongkan Request");
        btnClearReq.addActionListener((ActionEvent e) -> txtJsonRequest.setText(""));
        Button btnClearRes = grayButton("Kosongkan Response");
        btnClearRes.addActionListener((ActionEvent e) -> txtJsonResponse.setText(""));
        Button btnBpjsInsert = blueButton("Template BPJS Insert");
        btnBpjsInsert.addActionListener((ActionEvent e) -> setRequest(buildBpjsInsertJson()));
        Button btnBpjsDelete = redButton("Template BPJS Delete");
        btnBpjsDelete.addActionListener((ActionEvent e) -> setRequest(buildBpjsDeleteJson()));
        Button btnTaskPra = greenButton("Template Task Pra");
        btnTaskPra.addActionListener((ActionEvent e) -> setRequest(buildTaskPraPermintaanJson()));
        Button btnTaskKandidat = greenButton("Template Task Kandidat");
        btnTaskKandidat.addActionListener((ActionEvent e) -> setRequest(buildTaskKandidatJson()));
        Button btnBundle = greenButton("Template Bundle");
        btnBundle.addActionListener((ActionEvent e) -> setRequest(buildBundleRujukanJson()));
        buttons.add(btnClearReq); buttons.add(btnClearRes); buttons.add(btnBpjsInsert); buttons.add(btnBpjsDelete); buttons.add(btnTaskPra); buttons.add(btnTaskKandidat); buttons.add(btnBundle);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, titledScroll("Request JSON", txtJsonRequest), titledScroll("Response", txtJsonResponse));
        split.setResizeWeight(0.52);
        split.setBorder(null);

        wrapper.add(buttons, BorderLayout.NORTH);
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTabLog() {
        JPanel wrapper = newPanel(new BorderLayout(10, 10));
        wrapper.setBackground(BG);
        modelLog = new DefaultTableModel(new Object[]{"Waktu", "Aksi", "Endpoint", "HTTP", "Ringkasan"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tbLog = newTable(modelLog);
        setupTable(tbLog);
        wrapper.add(infoPanel("Log ini hanya log sesi berjalan. Untuk log permanen gunakan optional_sql_log_sisrute.sql lalu panggil insert log dari source Khanza bila diperlukan."), BorderLayout.NORTH);
        wrapper.add(newScrollPane(tbLog), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createBottomPanel() {
        JPanel p = newPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(8, 14, 8, 14));
        lblStatus = newLabel("Siap.");
        lblStatus.setFont(FONT);
        lblStatus.setForeground(new Color(82, 96, 110));
        JPanel right = newPanelIsi(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        Button btnReset = grayButton("Kosongkan Form");
        btnReset.addActionListener((ActionEvent e) -> clearForm());
        Button btnClose = redButton("Keluar");
        btnClose.addActionListener((ActionEvent e) -> dispose());
        right.add(btnReset); right.add(btnClose);
        p.add(lblStatus, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private PanelBiasa newPanel(java.awt.LayoutManager layout) {
        PanelBiasa p = new PanelBiasa();
        p.setLayout(layout);
        return p;
    }

    private panelisi newPanelIsi(java.awt.LayoutManager layout) {
        panelisi p = new panelisi();
        p.setLayout(layout);
        return p;
    }

    private Label newLabel(String text) {
        Label l = new Label();
        l.setText(text);
        l.setFont(FONT);
        return l;
    }

    private TextBox newTextBox() {
        TextBox t = new TextBox();
        t.setFont(FONT);
        t.setPreferredSize(new Dimension(160, 23));
        return t;
    }

    private TextBox newTextBox(String text) {
        TextBox t = newTextBox();
        t.setText(text);
        return t;
    }

    private JPasswordField newPasswordField() {
        JPasswordField p = new JPasswordField();
        p.setFont(FONT);
        p.setPreferredSize(new Dimension(160, 23));
        return p;
    }

    private ComboBox newComboBox(String[] items) {
        ComboBox c = new ComboBox();
        c.setModel(new DefaultComboBoxModel(items));
        c.setFont(FONT);
        c.setPreferredSize(new Dimension(160, 23));
        return c;
    }

    private Table newTable(DefaultTableModel model) {
        Table t = new Table();
        t.setModel(model);
        return t;
    }

    private ScrollPane newScrollPane(Component view) {
        ScrollPane sp = new ScrollPane();
        sp.setViewportView(view);
        return sp;
    }

    private JPanel card(java.awt.LayoutManager layout) {
        PanelBiasa p = newPanel(layout);
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(BORDER));
        return p;
    }

    private JPanel infoPanel(String text) {
        JPanel p = newPanel(new BorderLayout());
        p.setBackground(new Color(237, 247, 246));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(190, 224, 220)), new EmptyBorder(10, 12, 10, 12)));
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(FONT);
        area.setForeground(new Color(55, 88, 91));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        p.add(area, BorderLayout.CENTER);
        return p;
    }

    private ScrollPane titledScroll(String title, JTextArea area) {
        ScrollPane sp = newScrollPane(area);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), title, javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, FONT_BOLD, BLUE));
        return sp;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        return c;
    }

    private void addLabel(JPanel p, String text, int x, int y, GridBagConstraints c) {
        Label l = newLabel(text);
        l.setFont(FONT_BOLD);
        l.setForeground(new Color(62, 76, 89));
        c.gridx = x; c.gridy = y; c.gridwidth = 1; c.weightx = 0;
        p.add(l, c);
    }

    private void addField(JPanel p, Component comp, int x, int y, int w, GridBagConstraints c) {
        comp.setFont(FONT);
        c.gridx = x; c.gridy = y; c.gridwidth = w; c.weightx = 1.0;
        p.add(comp, c);
    }

    private void addSection(JPanel p, String text, int row, GridBagConstraints c) {
        Label l = newLabel(text);
        l.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        l.setForeground(GREEN);
        l.setBorder(new EmptyBorder(8, 0, 4, 0));
        c.gridx = 0; c.gridy = row; c.gridwidth = 4; c.weightx = 1.0;
        p.add(l, c);
    }

    private void addRow2(JPanel p, int row, GridBagConstraints c, String label1, JComponent field1, String label2, JComponent field2) {
        addLabel(p, label1, 0, row, c); addField(p, field1, 1, row, 1, c);
        addLabel(p, label2, 2, row, c); addField(p, field2, 3, row, 1, c);
    }

    private Button blueButton(String text) { return button(text, "/picture/accept.png", BLUE, Color.BLACK); }
    private Button greenButton(String text) { return button(text, "/picture/accept.png", GREEN, Color.BLACK); }
    private Button redButton(String text) { return button(text, "/picture/exit.png", RED, Color.BLACK); }
    private Button grayButton(String text) { return button(text, "/picture/Cancel-2-16x16.png", new Color(236, 240, 244), new Color(40, 54, 68)); }

    private Button button(String text, String iconPath, final Color bg, final Color fg) {
        final Icon icon = safeIcon(iconPath);
        final Color textColor = Color.BLACK;
        Button b = new Button() {
            @Override
            public void update(Graphics g) {
                paint(g);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color fill = bg;
                if (!isEnabled()) {
                    fill = mixColor(bg, Color.WHITE, 0.35f);
                } else if (getModel().isPressed()) {
                    fill = bg.darker();
                } else if (getModel().isRollover()) {
                    // Jangan dibuat putih saat hover. Cukup sedikit lebih terang agar warna tetap aman.
                    fill = mixColor(bg, Color.WHITE, isLightColor(bg) ? 0.04f : 0.10f);
                }

                int arc = 12;
                int w = Math.max(1, getWidth() - 1);
                int h = Math.max(1, getHeight() - 1);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // Border digambar sendiri agar tidak berubah oleh rollover bawaan widget.Button/LookAndFeel.
                g2.setColor(bg.darker());
                g2.drawRoundRect(0, 0, w, h, arc, arc);

                String caption = getText() == null ? "" : getText();
                Font font = getFont() == null ? FONT_BOLD : getFont();
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();

                int gap = (icon != null && caption.length() > 0) ? 8 : 0;
                Insets ins = getInsets();
                int available = Math.max(1, getWidth() - ins.left - ins.right - 8);
                String drawText = clipText(caption, fm, available - (icon == null ? 0 : icon.getIconWidth() + gap));
                int textW = fm.stringWidth(drawText);
                int iconW = icon == null ? 0 : icon.getIconWidth();
                int iconH = icon == null ? 0 : icon.getIconHeight();
                int totalW = iconW + gap + textW;
                int x = Math.max(ins.left + 4, (getWidth() - totalW) / 2);
                int centerY = getHeight() / 2;

                if (icon != null) {
                    int iy = centerY - (iconH / 2);
                    icon.paintIcon(this, g2, x, iy);
                    x += iconW + gap;
                }

                g2.setColor(isEnabled() ? textColor : mixColor(textColor, Color.WHITE, 0.45f));
                int ty = centerY + ((fm.getAscent() - fm.getDescent()) / 2);
                g2.drawString(drawText, x, ty);
                g2.dispose();
            }
        };
        b.setText(text);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setIcon(icon);

        // Pakai widget.Button Khanza, tetapi warna tombol dikunci sendiri.
        // Penyebab error sebelumnya: paint bawaan widget/LookAndFeel masih ikut mengganti
        // background ketika mouse masuk, sehingga tombol berubah putih. Di sini background,
        // border, icon, dan teks digambar manual supaya hover tetap stabil.
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setRolloverEnabled(true);
        b.setBackground(bg);
        b.setForeground(textColor);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setMargin(new Insets(7, 14, 7, 14));

        b.setMinimumSize(new Dimension(150, 32));
        b.setPreferredSize(new Dimension(Math.max(150, text.length() * 8 + 58), 32));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static boolean isLightColor(Color c) {
        return ((c.getRed() * 299) + (c.getGreen() * 587) + (c.getBlue() * 114)) / 1000 > 200;
    }

    private static Color mixColor(Color a, Color b, float ratioB) {
        float r = Math.max(0f, Math.min(1f, ratioB));
        float ia = 1f - r;
        return new Color(
                Math.min(255, Math.round((a.getRed() * ia) + (b.getRed() * r))),
                Math.min(255, Math.round((a.getGreen() * ia) + (b.getGreen() * r))),
                Math.min(255, Math.round((a.getBlue() * ia) + (b.getBlue() * r)))
        );
    }

    private static String clipText(String text, FontMetrics fm, int maxWidth) {
        if (text == null || text.length() == 0 || maxWidth <= 0 || fm.stringWidth(text) <= maxWidth) {
            return text == null ? "" : text;
        }
        String ellipsis = "...";
        int ellW = fm.stringWidth(ellipsis);
        if (ellW >= maxWidth) {
            return "";
        }
        int n = text.length();
        while (n > 0 && fm.stringWidth(text.substring(0, n)) + ellW > maxWidth) {
            n--;
        }
        return text.substring(0, Math.max(0, n)) + ellipsis;
    }

    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final Insets insets;

        RoundedBorder(Color color, int radius, Insets insets) {
            this.color = color;
            this.radius = radius;
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(insets.top, insets.left, insets.bottom, insets.right);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insetsOut) {
            insetsOut.top = insets.top;
            insetsOut.left = insets.left;
            insetsOut.bottom = insets.bottom;
            insetsOut.right = insets.right;
            return insetsOut;
        }
    }

    private ImageIcon safeIcon(String path) {
        try {
            URL urlIcon = getClass().getResource(path);
            return urlIcon == null ? null : new ImageIcon(urlIcon);
        } catch (Exception e) {
            return null;
        }
    }

    private void setupTable(JTable table) {
        table.setFont(FONT);
        table.setRowHeight(26);
        table.setGridColor(new Color(231, 237, 243));
        table.setSelectionBackground(new Color(219, 235, 252));
        table.setSelectionForeground(Color.BLACK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(239, 244, 249));
        header.setForeground(new Color(45, 62, 80));
        table.setDefaultEditor(Boolean.class, new DefaultCellEditor(new CekBox()));
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(140);
        }
    }

    private void initShortcut() {
        getRootPane().registerKeyboardAction((ActionEvent e) -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "sendActive");
        getRootPane().getActionMap().put("sendActive", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { setRequest(buildBpjsInsertJson()); }
        });
    }

    private void setDefaultValues() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        txtTanggal.setText(today);
        txtTanggalRencana.setText(today);
        txtAuthUrl.setText("https://api-satusehat.kemkes.go.id/oauth2/v1");
        txtFhirBaseUrl.setText("https://api-satusehat.kemkes.go.id/fhir-r4/v1");
        txtUser.setText(System.getProperty("user.name", "SIMRS"));
        txtDiagnosisCode.setText("I61.9");
        txtDiagnosisDisplay.setText("Intracerebral haemorrhage, unspecified");
        txtSpecialityCode.setText("1071");
        txtSpecialityDisplay.setText("Penyakit dalam");
        applyDefaultBpjsUrl();
        applyJenisDefaults();
        seedCriteriaByJenis();
    }

    private void applyDefaultBpjsUrl() {
        if (cmbBpjsMode.getSelectedIndex() == 0) {
            txtBpjsBaseUrl.setText("https://apijkn-dev.bpjs-kesehatan.go.id/vclaim-sisrute-rest");
        } else {
            txtBpjsBaseUrl.setText("https://apijkn-dev.bpjs-kesehatan.go.id/pcare-sisrute-rest");
        }
    }

    private boolean isBpjsFKRTL() {
        return cmbBpjsMode.getSelectedIndex() == 0;
    }

    private String bpjsPath(String jenis) {
        if ("kriteria".equals(jenis)) return isBpjsFKRTL() ? "/Rujukan/GetKriteriaRujukan" : "/Sisrute/GetKriteriaRujukan";
        if ("faskes".equals(jenis)) return isBpjsFKRTL() ? "/Rujukan/GetFaskesRujukan" : "/Sisrute/GetFaskesRujukan";
        if ("insert".equals(jenis)) return isBpjsFKRTL() ? "/Rujukan/Insert" : "/Sisrute/Insert";
        if ("delete".equals(jenis)) return isBpjsFKRTL() ? "/Rujukan/deleteKunjungan" : "/Sisrute/deleteKunjungan";
        return "/";
    }

    private int jenisIndex() {
        return cmbJenisRujukan.getSelectedIndex();
    }

    private void applyJenisDefaults() {
        if (cmbJenisRujukan == null) return;
        if (jenisIndex() == 0) {
            txtManagementCode.setText("737492002");
            txtManagementDisplay.setText("Outpatient care management");
            txtJnsPelayanan.setText("2");
            txtTipeRujukan.setText("0");
        } else if (jenisIndex() == 1) {
            txtManagementCode.setText("305351004");
            txtManagementDisplay.setText("Inpatient care");
            txtJnsPelayanan.setText("1");
            txtTipeRujukan.setText("0");
        } else {
            txtManagementCode.setText("385868005");
            txtManagementDisplay.setText("Emergency treatment management");
            txtJnsPelayanan.setText("1");
            txtTipeRujukan.setText("0");
        }
    }

    private void seedCriteriaByJenis() {
        if (modelKriteria == null) return;
        modelKriteria.setRowCount(0);
        if (jenisIndex() == 2) {
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "000001", "Mengancam nyawa, membahayakan diri dan orang lain/lingkungan", "boolean", "true"});
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "000002", "Adanya gangguan pada jalan nafas, pernafasan, dan sirkulasi", "boolean", "true"});
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "000003", "Adanya penurunan kesadaran", "boolean", "false"});
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "000004", "Adanya gangguan hemodinamik", "boolean", "true"});
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "000005", "Memerlukan tindakan segera", "boolean", "true"});
        } else if (jenisIndex() == 1) {
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "2532,9774", "Terapy/Pengobatan", "boolean", "true"});
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "9773,2531", "Tindakan Medis", "text", "01.24"});
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "9772,2530", "Upaya Diagnosis", "boolean", "false"});
        } else {
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "3216", "Terapi/Pengobatan", "boolean", "false"});
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "3215", "Tindakan Medis", "text", "01.24"});
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "3214", "Upaya Diagnosis", "boolean", "false"});
        }
        appendStatus("Kriteria default disiapkan untuk " + cmbJenisRujukan.getSelectedItem());
    }

    private void bpjsGetKriteria() {
        if (!validBpjsAuth()) return;
        String body = "{\n"
                + "  \"kodeDiagnosa\": \"" + esc(txtDiagnosisCode.getText()) + "\",\n"
                + "  \"kodeFaskesSatuSehat\": \"" + esc(txtKodeFaskesSS.getText()) + "\"\n"
                + "}";
        setRequest(body);
        executeBpjs("POST", bpjsPath("kriteria"), body, "BPJS GET KRITERIA", (String response) -> fillKriteriaFromResponse(response));
    }

    private void bpjsGetFaskes() {
        if (!validBpjsAuth()) return;
        String body = "{\n"
                + "  \"kodeFaskesSatuSehat\": \"" + esc(txtKodeFaskesSS.getText()) + "\",\n"
                + "  \"spesialisSubSpesialis\": \"" + esc(txtSpecialityCode.getText()) + "\",\n"
                + "  \"sarana\": \"" + esc(txtSarana.getText()) + "\",\n"
                + "  \"kodeDiagnosa\": \"" + esc(txtDiagnosisCode.getText()) + "\",\n"
                + "  \"tglRencanaDirujuk\": \"" + esc(txtTanggalRencana.getText()) + "\",\n"
                + "  \"kriteriaRujukan\": " + selectedCriteriaBPJSJson() + ",\n"
                + "  \"codeJejaringWilayah\": \"" + esc(firstNonEmpty(txtKabCode.getText(), txtProvCode.getText())) + "\"\n"
                + "}";
        setRequest(body);
        executeBpjs("POST", bpjsPath("faskes"), body, "BPJS GET FASKES", (String response) -> fillFaskesFromResponse(response));
    }

    private void bpjsInsertRujukan() {
        if (!validBpjsAuth()) return;
        String body = txtJsonRequest.getText().trim();
        if (body.length() == 0 || !body.contains("t_rujukan")) body = buildBpjsInsertJson();
        setRequest(body);
        executeBpjs("POST", bpjsPath("insert"), body, "BPJS INSERT RUJUKAN", (String response) -> {
            String no = firstNonEmpty(pick(response, "noRujukan"), pick(response, "noKunjungan"), pick(response, "noRujukanSatuSehat"));
            if (no.length() > 0) txtNoKunjungan.setText(no);
        });
    }

    private void bpjsDeleteRujukan() {
        if (!validBpjsAuth()) return;
        if (txtNoKunjungan.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Isi No. Kunjungan/No. Rujukan yang akan dihapus.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int opt = JOptionPane.showConfirmDialog(this, "Hapus rujukan " + txtNoKunjungan.getText() + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;
        String body = buildBpjsDeleteJson();
        setRequest(body);
        executeBpjs("POST", bpjsPath("delete"), body, "BPJS DELETE RUJUKAN", null);
    }

    private void generateToken() {
        if (txtAuthUrl.getText().trim().length() == 0 || txtClientId.getText().trim().length() == 0 || new String(txtClientSecret.getPassword()).trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Auth URL, Client ID, dan Client Secret SATUSEHAT harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        appendStatus("Generate token SATUSEHAT...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<ApiResult, Void> worker = new SwingWorker<ApiResult, Void>() {
            String endpoint;
            @Override protected ApiResult doInBackground() throws Exception {
                endpoint = normalizeBaseUrl(txtAuthUrl.getText()) + "/accesstoken?grant_type=client_credentials";
                String body = "client_id=" + url(txtClientId.getText()) + "&client_secret=" + url(new String(txtClientSecret.getPassword()));
                return callFormUrlEncoded(endpoint, body);
            }
            @Override protected void done() {
                try {
                    ApiResult res = get();
                    txtJsonResponse.setText(res.body);
                    String token = pick(res.body, "access_token");
                    if (token.length() > 0) txtToken.setText(token);
                    addLog("GENERATE TOKEN", endpoint, res.httpCode, summarize(res.body));
                    appendStatus("Generate token selesai. HTTP " + res.httpCode);
                } catch (Exception e) {
                    txtJsonResponse.setText("ERROR: " + e.getMessage());
                    appendStatus("Generate token gagal: " + e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void satusehatPost(String path, String body, String action) {
        if (!validSatuSehatAuth()) return;
        setRequest(body);
        executeSatuSehat("POST", path, body, action, null);
    }

    private void getServiceRequestById() {
        if (!validSatuSehatAuth()) return;
        String id = JOptionPane.showInputDialog(this, "Masukkan ID ServiceRequest:");
        if (id == null || id.trim().length() == 0) return;
        executeSatuSehat("GET", "/ServiceRequest/" + id.trim(), null, "GET SERVICEREQUEST", null);
    }

    private boolean validBpjsAuth() {
        if (txtBpjsBaseUrl.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Base URL BPJS belum diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtBpjsConsId.getText().trim().length() == 0 || new String(txtBpjsSecret.getPassword()).trim().length() == 0 || txtBpjsUserKey.getText().trim().length() == 0) {
            int opt = JOptionPane.showConfirmDialog(this, "Credential BPJS belum lengkap. Tetap lanjut?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            return opt == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private boolean validSatuSehatAuth() {
        if (txtFhirBaseUrl.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "FHIR Base URL belum diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtToken.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Bearer token SATUSEHAT masih kosong. Klik Generate Token dulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private interface ApiCallback { void done(String response); }

    private void executeBpjs(String method, String path, String body, String action, ApiCallback callback) {
        appendStatus("Memproses " + action + "...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        txtJsonResponse.setText("Memproses request ke " + path + " ...");
        SwingWorker<ApiResult, Void> worker = new SwingWorker<ApiResult, Void>() {
            String endpoint;
            @Override protected ApiResult doInBackground() throws Exception {
                endpoint = normalizeBaseUrl(txtBpjsBaseUrl.getText()) + path;
                return callBpjs(method, endpoint, body);
            }
            @Override protected void done() {
                try {
                    ApiResult res = get();
                    txtJsonResponse.setText(res.body);
                    txtJsonResponse.setCaretPosition(0);
                    addLog(action, endpoint, res.httpCode, summarize(res.body));
                    appendStatus(action + " selesai. HTTP " + res.httpCode);
                    if (callback != null) callback.done(res.body);
                } catch (Exception e) {
                    txtJsonResponse.setText("ERROR: " + e.getMessage());
                    appendStatus("Gagal: " + e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void executeSatuSehat(String method, String path, String body, String action, ApiCallback callback) {
        appendStatus("Memproses " + action + "...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        txtJsonResponse.setText("Memproses request ke " + path + " ...");
        SwingWorker<ApiResult, Void> worker = new SwingWorker<ApiResult, Void>() {
            String endpoint;
            @Override protected ApiResult doInBackground() throws Exception {
                endpoint = normalizeBaseUrl(txtFhirBaseUrl.getText()) + path;
                return callBearerJson(method, endpoint, body, txtToken.getText().trim());
            }
            @Override protected void done() {
                try {
                    ApiResult res = get();
                    txtJsonResponse.setText(res.body);
                    txtJsonResponse.setCaretPosition(0);
                    addLog(action, endpoint, res.httpCode, summarize(res.body));
                    appendStatus(action + " selesai. HTTP " + res.httpCode);
                    if (callback != null) callback.done(res.body);
                } catch (Exception e) {
                    txtJsonResponse.setText("ERROR: " + e.getMessage());
                    appendStatus("Gagal: " + e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private ApiResult callBpjs(String method, String fullUrl, String body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(90000);
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String signature = createBpjsSignature(txtBpjsConsId.getText().trim(), new String(txtBpjsSecret.getPassword()).trim(), timestamp);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("X-cons-id", txtBpjsConsId.getText().trim());
        conn.setRequestProperty("X-timestamp", timestamp);
        conn.setRequestProperty("X-signature", signature);
        conn.setRequestProperty("user_key", txtBpjsUserKey.getText().trim());
        if (body != null && body.trim().length() > 0 && !"GET".equalsIgnoreCase(method)) {
            writeBody(conn, body, "application/json; charset=utf-8");
        }
        return readResponse(conn);
    }

    private ApiResult callBearerJson(String method, String fullUrl, String body, String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(90000);
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/fhir+json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/fhir+json, application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        if (body != null && body.trim().length() > 0 && !"GET".equalsIgnoreCase(method)) {
            writeBody(conn, body, "application/fhir+json; charset=utf-8");
        }
        return readResponse(conn);
    }

    private ApiResult callFormUrlEncoded(String fullUrl, String body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(90000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        writeBody(conn, body, "application/x-www-form-urlencoded; charset=utf-8");
        return readResponse(conn);
    }

    private void writeBody(HttpURLConnection conn, String body, String contentType) throws Exception {
        conn.setDoOutput(true);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }
    }

    private ApiResult readResponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String response = readStream(is);
        conn.disconnect();
        return new ApiResult(code, response);
    }

    private static class ApiResult {
        final int httpCode;
        final String body;
        ApiResult(int httpCode, String body) { this.httpCode = httpCode; this.body = body; }
    }

    private String createBpjsSignature(String consId, String secret, String timestamp) throws Exception {
        String data = consId + "&" + timestamp;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return java.util.Base64.getEncoder().encodeToString(raw);
    }

    private String readStream(InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString().trim();
    }

    private String buildBpjsInsertJson() {
        return "{\n"
                + "  \"request\": {\n"
                + "    \"t_rujukan\": {\n"
                + "      \"noSep\": \"" + esc(txtNoSep.getText()) + "\",\n"
                + "      \"tglRujukan\": \"" + esc(txtTanggal.getText()) + "\",\n"
                + "      \"tglRencanaKunjungan\": \"" + esc(txtTanggalRencana.getText()) + "\",\n"
                + "      \"ppkDirujuk\": \"" + esc(txtPpkDirujuk.getText()) + "\",\n"
                + "      \"jnsPelayanan\": \"" + esc(txtJnsPelayanan.getText()) + "\",\n"
                + "      \"catatan\": \"" + esc(txtCatatan.getText()) + "\",\n"
                + "      \"diagRujukan\": \"" + esc(txtDiagnosisCode.getText()) + "\",\n"
                + "      \"tipeRujukan\": \"" + esc(txtTipeRujukan.getText()) + "\",\n"
                + "      \"poliRujukan\": \"" + esc(txtPoliRujukan.getText()) + "\",\n"
                + "      \"user\": \"" + esc(txtUser.getText()) + "\",\n"
                + "      \"satuSehatRujukan\": {\n"
                + "        \"kodeFaskesSatuSehat\": \"" + esc(txtKodeFaskesSS.getText()) + "\",\n"
                + "        \"idPasienSatuSehat\": \"" + esc(txtPatientId.getText()) + "\",\n"
                + "        \"kdppkSatuSehatTujuanRujukan\": \"" + esc(txtKdppkSSTujuan.getText()) + "\",\n"
                + "        \"kdDokterSatuSehat\": \"" + esc(txtPractitionerId.getText()) + "\",\n"
                + "        \"encounter\": {\n"
                + "          \"reference\": \"" + esc(encounterReference()) + "\"\n"
                + "        },\n"
                + "        \"patientInstruction\": \"Rujukan ke " + esc(firstNonEmpty(txtNamaOrgTujuan.getText(), txtNamaOrgPerujuk.getText())) + "\",\n"
                + "        \"kriteriaRujukan\": " + selectedCriteriaBPJSJson() + ",\n"
                + "        \"keteranganRujukan\": \"" + esc(firstNonEmpty(txtCatatan.getText(), "Rujukan pasien")) + "\",\n"
                + "        \"codeJejaringWilayah\": \"" + esc(firstNonEmpty(txtKabCode.getText(), txtProvCode.getText())) + "\"\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }

    private String buildBpjsDeleteJson() {
        return "{\n"
                + "  \"noKunjungan\": \"" + esc(txtNoKunjungan.getText()) + "\",\n"
                + "  \"satuSehatRujukan\": {\n"
                + "    \"kodeFaskesSatuSehat\": \"" + esc(txtKodeFaskesSS.getText()) + "\",\n"
                + "    \"idPasienSatuSehat\": \"" + esc(txtPatientId.getText()) + "\",\n"
                + "    \"kdppkSatuSehatTujuanRujukan\": \"" + esc(txtKdppkSSTujuan.getText()) + "\",\n"
                + "    \"kdDokterSatuSehat\": \"" + esc(txtPractitionerId.getText()) + "\",\n"
                + "    \"encounter\": { \"reference\": \"" + esc(encounterReference()) + "\" },\n"
                + "    \"patientInstruction\": \"Rujukan ke " + esc(txtNamaOrgTujuan.getText()) + "\",\n"
                + "    \"keteranganRujukan\": \"" + esc(firstNonEmpty(txtCatatan.getText(), "Hapus rujukan")) + "\"\n"
                + "  }\n"
                + "}";
    }

    private String buildTaskPraPermintaanJson() {
        String id = newRegistrationId();
        StringBuilder input = new StringBuilder();
        if (jenisIndex() != 0) {
            input.append(managementInput()).append(",\n");
        }
        input.append(primaryDiagnosisInput());
        return "{\n"
                + "  \"resourceType\": \"Task\",\n"
                + "  \"identifier\": [{ \"system\": \"http://sys-ids.kemkes.go.id/task/" + esc(txtOrgPerujuk.getText()) + "\", \"value\": \"" + id + "\" }],\n"
                + "  \"status\": \"requested\",\n"
                + "  \"intent\": \"instance-order\",\n"
                + "  \"priority\": \"routine\",\n"
                + "  \"code\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id\", \"code\": \"referral-pre-request\", \"display\": \"Referral pre request\" }] },\n"
                + "  \"authoredOn\": \"" + fhirDateTime() + "\",\n"
                + "  \"lastModified\": \"" + fhirDateTime() + "\",\n"
                + "  \"requester\": { \"reference\": \"Organization/" + esc(txtOrgPerujuk.getText()) + "\" },\n"
                + "  \"owner\": { \"reference\": \"Organization/" + esc(txtOrgPerujuk.getText()) + "\" },\n"
                + "  \"encounter\": { \"reference\": \"" + esc(encounterReference()) + "\" },\n"
                + "  \"input\": [\n" + input.toString() + "\n  ]\n"
                + "}";
    }

    private String buildTaskKandidatJson() {
        String id = newRegistrationId();
        return "{\n"
                + "  \"resourceType\": \"Task\",\n"
                + "  \"contained\": [\n"
                + buildQuestionnaireResponseKriteria("123456789") + ",\n"
                + buildQuestionnaireResponseArea("123456788") + "\n"
                + "  ],\n"
                + "  \"identifier\": [{ \"system\": \"http://sys-ids.kemkes.go.id/task/" + esc(txtOrgPerujuk.getText()) + "\", \"value\": \"" + id + "\" }],\n"
                + "  \"status\": \"requested\",\n"
                + "  \"intent\": \"instance-order\",\n"
                + "  \"priority\": \"routine\",\n"
                + "  \"code\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id\", \"code\": \"request-referral-candidate\", \"display\": \"Request for referral candidate\" }] },\n"
                + "  \"for\": { \"reference\": \"" + esc(patientReference()) + "\" },\n"
                + "  \"authoredOn\": \"" + fhirDateTime() + "\",\n"
                + "  \"lastModified\": \"" + fhirDateTime() + "\",\n"
                + "  \"requester\": { \"reference\": \"Organization/" + esc(txtOrgPerujuk.getText()) + "\" },\n"
                + "  \"owner\": { \"reference\": \"Organization/" + esc(txtOrgPerujuk.getText()) + "\" },\n"
                + "  \"encounter\": { \"reference\": \"" + esc(encounterReference()) + "\" },\n"
                + "  \"input\": [\n"
                + "    { \"type\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id\", \"code\": \"referral-criteria\", \"display\": \"Referral Criteria\" }] }, \"valueReference\": { \"reference\": \"#123456789\", \"display\": \"Referral Criteria Response\" } },\n"
                + "    { \"type\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id\", \"code\": \"area\", \"display\": \"Area\" }] }, \"valueReference\": { \"reference\": \"#123456788\", \"display\": \"Jejaring Wilayah Rujukan\" } },\n"
                + managementInput() + ",\n"
                + primaryDiagnosisInput()
                + secondaryDiagnosisInputIfAny()
                + "\n  ]\n"
                + "}";
    }

    private String buildBundleRujukanJson() {
        String carePlanId = uuid();
        String serviceRequestId = uuid();
        if (jenisIndex() == 0) {
            return "{\n"
                    + "  \"resourceType\": \"Bundle\",\n"
                    + "  \"type\": \"transaction\",\n"
                    + "  \"entry\": [\n"
                    + "    {\n"
                    + "      \"fullUrl\": \"urn:uuid:" + serviceRequestId + "\",\n"
                    + "      \"resource\": " + indent(buildServiceRequestJson(true), 6) + ",\n"
                    + "      \"request\": { \"method\": \"POST\", \"url\": \"ServiceRequest\" }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"fullUrl\": \"urn:uuid:" + carePlanId + "\",\n"
                    + "      \"resource\": " + indent(buildCarePlanJson(carePlanId), 6) + ",\n"
                    + "      \"request\": { \"method\": \"POST\", \"url\": \"CarePlan\" }\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";
        }
        String taskApprovalId = uuid();
        return "{\n"
                + "  \"resourceType\": \"Bundle\",\n"
                + "  \"type\": \"transaction\",\n"
                + "  \"entry\": [\n"
                + "    {\n"
                + "      \"fullUrl\": \"urn:uuid:" + taskApprovalId + "\",\n"
                + "      \"resource\": " + indent(buildTaskApprovalRequestJson(taskApprovalId), 6) + ",\n"
                + "      \"request\": { \"method\": \"POST\", \"url\": \"Task\" }\n"
                + "    },\n"
                + "    {\n"
                + "      \"fullUrl\": \"urn:uuid:" + carePlanId + "\",\n"
                + "      \"resource\": " + indent(buildCarePlanJson(carePlanId), 6) + ",\n"
                + "      \"request\": { \"method\": \"POST\", \"url\": \"CarePlan\" }\n"
                + "    }\n"
                + "  ]\n"
                + "}";
    }

    private String buildTaskApprovalRequestJson(String taskId) {
        return "{\n"
                + "  \"resourceType\": \"Task\",\n"
                + "  \"identifier\": [{ \"system\": \"http://sys-ids.kemkes.go.id/task/" + esc(txtOrgPerujuk.getText()) + "\", \"value\": \"" + newRegistrationId() + "\" }],\n"
                + "  \"status\": \"requested\",\n"
                + "  \"intent\": \"instance-order\",\n"
                + "  \"priority\": \"routine\",\n"
                + "  \"code\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id\", \"code\": \"referral-approval-request\", \"display\": \"Referral approval request\" }] },\n"
                + "  \"for\": { \"reference\": \"" + esc(patientReference()) + "\" },\n"
                + "  \"executionPeriod\": { \"start\": \"" + fhirDateTime() + "\" },\n"
                + "  \"authoredOn\": \"" + fhirDateTime() + "\",\n"
                + "  \"lastModified\": \"" + fhirDateTime() + "\",\n"
                + "  \"requester\": { \"reference\": \"Organization/" + esc(txtOrgPerujuk.getText()) + "\", \"display\": \"" + esc(txtNamaOrgPerujuk.getText()) + "\" },\n"
                + "  \"owner\": { \"reference\": \"Organization/" + esc(txtOrgPerujuk.getText()) + "\" },\n"
                + "  \"encounter\": { \"reference\": \"" + esc(encounterReference()) + "\" },\n"
                + "  \"input\": [{\n"
                + "    \"type\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id\", \"code\": \"referral-task\", \"display\": \"Referral Task\" }] },\n"
                + "    \"valueReference\": { \"reference\": \"Organization/" + esc(txtOrgTujuan.getText()) + "\", \"display\": \"" + esc(txtNamaOrgTujuan.getText()) + "\" }\n"
                + "  }]\n"
                + "}";
    }

    private String buildServiceRequestJson(boolean forBundle) {
        String orgBpjs = orgBpjsId();
        String orgTujuan = orgTujuanId();
        String namaTujuan = firstNonEmpty(txtNamaOrgTujuan.getText(), txtPpkDirujuk.getText(), txtKdppkSSTujuan.getText(), orgTujuan);
        return "{\n"
                + "  \"resourceType\": \"ServiceRequest\",\n"
                + "  \"identifier\": [\n"
                + "    { \"system\": \"http://sys-ids.kemkes.go.id/servicerequest/" + esc(txtOrgPerujuk.getText()) + "\", \"value\": \"" + newRegistrationId() + "\" },\n"
                + "    { \"system\": \"http://sys-ids.kemkes.go.id/referral-number-pcare\", \"value\": \"" + esc(txtNoSep.getText()) + "\" },\n"
                + "    { \"system\": \"https://sys-ids.kemkes.go.id/insurance-subscriber/" + esc(orgBpjs) + "\", \"value\": \"" + esc(txtCoverageNo.getText()) + "\" }\n"
                + "  ],\n"
                + "  \"status\": \"active\",\n"
                + "  \"intent\": \"original-order\",\n"
                + "  \"priority\": \"stat\",\n"
                + "  \"category\": [{ \"coding\": [{ \"system\": \"http://snomed.info/sct\", \"code\": \"3457005\", \"display\": \"Patient referral\" }] }],\n"
                + "  \"code\": { \"coding\": [{ \"system\": \"http://snomed.info/sct\", \"code\": \"" + esc(txtManagementCode.getText()) + "\", \"display\": \"" + esc(txtManagementDisplay.getText()) + "\" }], \"text\": \"Rujukan pasien untuk pemeriksaan dan penanganan lebih lanjut\" },\n"
                + "  \"subject\": { \"reference\": \"" + esc(patientReference()) + "\" },\n"
                + "  \"encounter\": { \"reference\": \"" + esc(encounterReference()) + "\" },\n"
                + "  \"occurrenceDateTime\": \"" + fhirDateTime() + "\",\n"
                + "  \"requester\": { \"reference\": \"Organization/" + esc(txtOrgPerujuk.getText()) + "\", \"display\": \"" + esc(txtNamaOrgPerujuk.getText()) + "\" },\n"
                + "  \"performerType\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id/CodeSystem/practitioner-speciality\", \"code\": \"" + esc(txtSpecialityCode.getText()) + "\", \"display\": \"" + esc(txtSpecialityDisplay.getText()) + "\" }] },\n"
                + "  \"performer\": [{ \"reference\": \"Organization/" + esc(orgTujuan) + "\", \"display\": \"" + esc(namaTujuan) + "\" }],\n"
                + "  \"reasonCode\": [{ \"coding\": [{ \"system\": \"http://hl7.org/fhir/sid/icd-10\", \"code\": \"" + esc(txtDiagnosisCode.getText()) + "\", \"display\": \"" + esc(txtDiagnosisDisplay.getText()) + "\" }] }],\n"
                + "  \"patientInstruction\": \"Rujukan ke " + esc(namaTujuan) + "\"\n"
                + "}";
    }

    private String buildCarePlanJson(String carePlanId) {
        return "{\n"
                + "  \"resourceType\": \"CarePlan\",\n"
                + "  \"status\": \"active\",\n"
                + "  \"intent\": \"plan\",\n"
                + "  \"category\": [{ \"coding\": [{ \"system\": \"http://snomed.info/sct\", \"code\": \"736271009\", \"display\": \"Outpatient care plan\" }] }],\n"
                + "  \"title\": \"Rencana Rujukan Pasien\",\n"
                + "  \"subject\": { \"reference\": \"" + esc(patientReference()) + "\" },\n"
                + "  \"encounter\": { \"reference\": \"" + esc(encounterReference()) + "\" },\n"
                + "  \"created\": \"" + fhirDateTime() + "\",\n"
                + "  \"author\": { \"reference\": \"Organization/" + esc(txtOrgPerujuk.getText()) + "\", \"display\": \"" + esc(txtNamaOrgPerujuk.getText()) + "\" },\n"
                + "  \"description\": \"" + esc(firstNonEmpty(txtCatatan.getText(), "Rencana rujukan pasien ke faskes tujuan")) + "\"\n"
                + "}";
    }

    private String buildQuestionnaireResponseKriteria(String containedId) {
        StringBuilder sb = new StringBuilder();
        sb.append("    {\n");
        sb.append("      \"resourceType\": \"QuestionnaireResponse\",\n");
        sb.append("      \"id\": \"").append(containedId).append("\",\n");
        sb.append("      \"questionnaire\": \"https://fhir.kemkes.go.id/Questionnaire/Q100\",\n");
        sb.append("      \"status\": \"completed\",\n");
        sb.append("      \"subject\": { \"reference\": \"").append(esc(patientReference())).append("\" },\n");
        sb.append("      \"encounter\": { \"reference\": \"").append(esc(encounterReference())).append("\" },\n");
        sb.append("      \"item\": ").append(selectedCriteriaFHIRJson()).append("\n");
        sb.append("    }");
        return sb.toString();
    }

    private String buildQuestionnaireResponseArea(String containedId) {
        return "    {\n"
                + "      \"resourceType\": \"QuestionnaireResponse\",\n"
                + "      \"id\": \"" + containedId + "\",\n"
                + "      \"questionnaire\": \"https://fhir.kemkes.go.id/Questionnaire/Q101\",\n"
                + "      \"status\": \"completed\",\n"
                + "      \"subject\": { \"reference\": \"" + esc(patientReference()) + "\" },\n"
                + "      \"encounter\": { \"reference\": \"" + esc(encounterReference()) + "\" },\n"
                + "      \"item\": [{\n"
                + "        \"linkId\": \"1\",\n"
                + "        \"text\": \"Jejaring wilayah rujukan\",\n"
                + "        \"item\": [\n"
                + "          { \"linkId\": \"1.1\", \"text\": \"Provinsi\", \"answer\": [{ \"valueCoding\": { \"system\": \"http://sys-ids.kemkes.go.id/administrative-area\", \"code\": \"" + esc(txtProvCode.getText()) + "\", \"display\": \"" + esc(txtProvDisplay.getText()) + "\" } }] },\n"
                + "          { \"linkId\": \"1.2\", \"text\": \"Kabupaten/Kota\", \"answer\": [{ \"valueCoding\": { \"system\": \"http://sys-ids.kemkes.go.id/administrative-area\", \"code\": \"" + esc(txtKabCode.getText()) + "\", \"display\": \"" + esc(txtKabDisplay.getText()) + "\" } }] }\n"
                + "        ]\n"
                + "      }]\n"
                + "    }";
    }

    private String selectedCriteriaFHIRJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (int i = 0; i < modelKriteria.getRowCount(); i++) {
            Object checked = modelKriteria.getValueAt(i, 0);
            if (checked instanceof Boolean && ((Boolean) checked)) {
                if (!first) sb.append(",");
                String linkId = safe(modelKriteria.getValueAt(i, 1));
                String text = safe(modelKriteria.getValueAt(i, 2));
                String type = safe(modelKriteria.getValueAt(i, 3));
                String answer = safe(modelKriteria.getValueAt(i, 4));
                sb.append("\n        {");
                sb.append("\"linkId\": \"").append(esc(linkId)).append("\", ");
                sb.append("\"text\": \"").append(esc(text)).append("\", ");
                sb.append("\"answer\": [");
                if ("boolean".equalsIgnoreCase(type)) {
                    sb.append("{ \"valueBoolean\": ").append("true".equalsIgnoreCase(answer) || "1".equals(answer) ? "true" : "false").append(" }");
                } else {
                    sb.append("{ \"valueString\": \"").append(esc(answer)).append("\" }");
                }
                sb.append("]}");
                first = false;
            }
        }
        if (!first) sb.append("\n      ");
        sb.append("]");
        return sb.toString();
    }

    private String selectedCriteriaBPJSJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (int i = 0; i < modelKriteria.getRowCount(); i++) {
            Object checked = modelKriteria.getValueAt(i, 0);
            if (checked instanceof Boolean && ((Boolean) checked)) {
                if (!first) sb.append(",");
                sb.append("\n          {")
                  .append("\"kode\":\"").append(esc(safe(modelKriteria.getValueAt(i, 1)))).append("\",")
                  .append("\"kriteria\":\"").append(esc(safe(modelKriteria.getValueAt(i, 2)))).append("\",")
                  .append("\"jawaban\":\"").append(esc(safe(modelKriteria.getValueAt(i, 4)))).append("\"")
                  .append("}");
                first = false;
            }
        }
        if (!first) sb.append("\n        ");
        sb.append("]");
        return sb.toString();
    }

    private String managementInput() {
        return "    {\n"
                + "      \"type\": { \"coding\": [{ \"system\": \"http://snomed.info/sct\", \"code\": \"119270007\", \"display\": \"Management procedure\" }] },\n"
                + "      \"valueCoding\": { \"system\": \"http://snomed.info/sct\", \"code\": \"" + esc(txtManagementCode.getText()) + "\", \"display\": \"" + esc(txtManagementDisplay.getText()) + "\" }\n"
                + "    }";
    }

    private String primaryDiagnosisInput() {
        return "    {\n"
                + "      \"type\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id\", \"code\": \"primary-diagnosis\", \"display\": \"Primary Diagnosis\" }] },\n"
                + "      \"valueCoding\": { \"system\": \"http://hl7.org/fhir/sid/icd-10\", \"code\": \"" + esc(txtDiagnosisCode.getText()) + "\", \"display\": \"" + esc(txtDiagnosisDisplay.getText()) + "\" }\n"
                + "    }";
    }

    private String secondaryDiagnosisInputIfAny() {
        if (txtDiagnosisSekunderCode.getText().trim().length() == 0) return "";
        return ",\n    {\n"
                + "      \"type\": { \"coding\": [{ \"system\": \"http://terminology.kemkes.go.id\", \"code\": \"secondary-diagnosis\", \"display\": \"Secondary diagnosis\" }] },\n"
                + "      \"valueCoding\": { \"system\": \"http://hl7.org/fhir/sid/icd-10\", \"code\": \"" + esc(txtDiagnosisSekunderCode.getText()) + "\", \"display\": \"" + esc(txtDiagnosisSekunderDisplay.getText()) + "\" }\n"
                + "    }";
    }

    private void fillKriteriaFromResponse(String response) {
        if (response == null || response.trim().length() == 0) return;
        modelKriteria.setRowCount(0);
        Pattern pItem = Pattern.compile("\\{[^{}]*\\\"linkId\\\"\\s*:\\s*\\\"?([^\\\",}]+)\\\"?[^{}]*\\\"text\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"[^{}]*(?:\\\"type\\\"\\s*:\\s*\\\"([^\\\"]*)\\\")?[^{}]*\\}", Pattern.CASE_INSENSITIVE);
        Matcher m = pItem.matcher(response);
        int count = 0;
        while (m.find()) {
            String code = m.group(1);
            String text = m.group(2);
            String type = firstNonEmpty(m.group(3), "boolean");
            if (text.length() > 0) {
                modelKriteria.addRow(new Object[]{Boolean.TRUE, code, text, type, "boolean".equalsIgnoreCase(type) ? "false" : ""});
                count++;
            }
        }
        if (count == 0) {
            Pattern objPattern = Pattern.compile("\\{([^{}]*)\\}");
            Matcher om = objPattern.matcher(response);
            while (om.find()) {
                String obj = om.group(1);
                String kode = firstNonEmpty(pick(obj, "kode"), pick(obj, "code"), pick(obj, "linkId"), pick(obj, "id"));
                String text = firstNonEmpty(pick(obj, "pertanyaan"), pick(obj, "kriteria"), pick(obj, "nama"), pick(obj, "text"));
                if (kode.length() > 0 || text.length() > 0) {
                    modelKriteria.addRow(new Object[]{Boolean.TRUE, kode, text, "boolean", "false"});
                    count++;
                }
            }
        }
        if (count == 0) {
            modelKriteria.addRow(new Object[]{Boolean.TRUE, "", "Response belum bisa diparse otomatis; salin kriteria dari response raw", "boolean", "false"});
        }
    }

    private void fillFaskesFromResponse(String response) {
        if (response == null || response.trim().length() == 0) return;
        modelFaskes.setRowCount(0);
        Pattern objPattern = Pattern.compile("\\{([^{}]*)\\}");
        Matcher m = objPattern.matcher(response);
        int count = 0;
        while (m.find()) {
            String obj = m.group(1);
            String kodeSS = pick(obj, "kodeFaskesSatuSehat");
            String kdppk = firstNonEmpty(pick(obj, "kdppk"), pick(obj, "kodePpk"), pick(obj, "kdPpk"));
            String nama = firstNonEmpty(pick(obj, "nmppk"), pick(obj, "nama"), pick(obj, "namaFaskes"), pick(obj, "nmPpk"));
            if (kodeSS.length() > 0 || kdppk.length() > 0 || nama.length() > 0) {
                modelFaskes.addRow(new Object[]{
                    kodeSS, kdppk, nama,
                    firstNonEmpty(pick(obj, "alamatPpk"), pick(obj, "alamat")),
                    firstNonEmpty(pick(obj, "kelas"), pick(obj, "kelasRs")),
                    firstNonEmpty(pick(obj, "distance"), pick(obj, "jarak")),
                    firstNonEmpty(pick(obj, "jadwal"), pick(obj, "jadwalPraktek")),
                    firstNonEmpty(pick(obj, "kapasitas"), pick(obj, "kapasitasBed")),
                    firstNonEmpty(pick(obj, "telpPpk"), pick(obj, "telp"))
                });
                count++;
            }
        }
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "Response diterima, tetapi daftar faskes belum bisa diparse otomatis. Cek tab JSON/Response.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setRequest(String json) {
        txtJsonRequest.setText(json);
        txtJsonRequest.setCaretPosition(0);
        appendStatus("Template request siap.");
    }

    private void appendStatus(String text) {
        lblStatus.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()) + " - " + text);
    }

    private void addLog(String action, String endpoint, int code, String summary) {
        if (modelLog == null) return;
        modelLog.addRow(new Object[]{new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), action, endpoint, String.valueOf(code), summary});
    }

    private void clearForm() {
        JTextComponent[] fields = new JTextComponent[]{txtNoRawat, txtNamaPasien, txtNoSep, txtNoKunjungan, txtOrgPerujuk, txtNamaOrgPerujuk, txtOrgTujuan, txtNamaOrgTujuan, txtOrgBpjs, txtPatientId, txtCoverageNo, txtEncounterRef, txtPractitionerId, txtDiagnosisSekunderCode, txtDiagnosisSekunderDisplay, txtPpkDirujuk, txtKdppkSSTujuan, txtKodeFaskesSS, txtPoliRujukan, txtCatatan, txtKabCode, txtKabDisplay, txtSarana};
        for (JTextComponent f : fields) if (f != null) f.setText("");
        txtJsonRequest.setText("");
        txtJsonResponse.setText("");
        if (modelFaskes != null) modelFaskes.setRowCount(0);
        seedCriteriaByJenis();
        appendStatus("Form dikosongkan.");
    }


    private String ambilNoRawatTerakhirDariRegPeriksa() {
        return cariIsiOptional(
            "select no_rawat from reg_periksa where stts<>'Batal' order by tgl_registrasi desc, jam_reg desc, no_rawat desc limit 1"
        );
    }

    public void setNoRawat(String noRawat) {
        txtNoRawat.setText(noRawat == null ? "" : noRawat.trim());
        if (txtNoRawat.getText().trim().length() > 0) {
            ambilDataDariSimrsKhanza();
        }
    }

    public void tampil(String noRawat) {
        setNoRawat(noRawat);
    }

    private void ambilDataDariSimrsKhanza() {
        String noRawat = txtNoRawat.getText().trim();
        if (noRawat.length() == 0) {
            noRawat = ambilNoRawatTerakhirDariRegPeriksa();
            if (noRawat.length() == 0) {
                JOptionPane.showMessageDialog(this, "No. Rawat kosong dan data terakhir di reg_periksa tidak ditemukan.", "Ambil Data SIMRS Khanza", JOptionPane.WARNING_MESSAGE);
                return;
            }
            txtNoRawat.setText(noRawat);
            appendStatus("No. Rawat kosong, otomatis memakai kunjungan terakhir: " + noRawat);
        }

        appendStatus("Mengambil data dari SIMRS Khanza...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        int terisi = 0;
        StringBuilder info = new StringBuilder();
        PreparedStatement psLocal = null;
        ResultSet rsLocal = null;
        try {
            psLocal = koneksi.prepareStatement(
                "select rp.no_rawat,rp.no_rkm_medis,rp.kd_dokter,rp.tgl_registrasi,rp.jam_reg,rp.status_lanjut," +
                " p.nm_pasien,p.no_peserta,p.no_ktp,pg.nama as nm_dokter,pg.no_ktp as nik_dokter " +
                "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis " +
                "left join pegawai pg on rp.kd_dokter=pg.nik " +
                "where rp.no_rawat=? limit 1"
            );
            psLocal.setString(1, noRawat);
            rsLocal = psLocal.executeQuery();
            if (!rsLocal.next()) {
                info.append("No. Rawat ").append(noRawat).append(" tidak ditemukan di tabel reg_periksa. Cek kembali format no_rawat yang dipilih.");
                appendStatus("No. Rawat tidak ditemukan.");
                JOptionPane.showMessageDialog(this, info.toString(), "Ambil Data SIMRS Khanza", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String noRm = safe(rsLocal.getString("no_rkm_medis"));
            String kdDokter = safe(rsLocal.getString("kd_dokter"));
            String statusLanjut = safe(rsLocal.getString("status_lanjut"));
            String namaPasien = safe(rsLocal.getString("nm_pasien"));
            String noKartu = safe(rsLocal.getString("no_peserta"));
            String nikPasien = safe(rsLocal.getString("no_ktp"));
            String nikDokter = safe(rsLocal.getString("nik_dokter"));

            terisi += setTextIfValue(txtNamaPasien, namaPasien);
            terisi += setTextIfValue(txtCoverageNo, noKartu);
            if (txtTanggal.getText().trim().length() == 0) {
                terisi += setTextIfValue(txtTanggal, safe(rsLocal.getString("tgl_registrasi")));
            }

            if ("Ranap".equalsIgnoreCase(statusLanjut)) {
                cmbJenisRujukan.setSelectedItem("Rawat Inap");
            } else if ("Ralan".equalsIgnoreCase(statusLanjut)) {
                cmbJenisRujukan.setSelectedItem("Rawat Jalan");
            }
            applyJenisDefaults();

            String jnsPelayanan = "Ranap".equalsIgnoreCase(statusLanjut) ? "1" : "2";
            String noSep = cariIsiOptional(
                "select no_sep from bridging_sep where no_rawat=? and jnspelayanan=? order by tglsep desc limit 1",
                noRawat, jnsPelayanan
            );
            if (noSep.length() == 0) {
                noSep = cariIsiOptional("select no_sep from bridging_sep where no_rawat=? order by tglsep desc limit 1", noRawat);
            }
            terisi += setTextIfValue(txtNoSep, noSep);

            String kartuSep = cariIsiOptional("select no_kartu from bridging_sep where no_rawat=? order by tglsep desc limit 1", noRawat);
            if (kartuSep.length() > 0) terisi += setTextIfValue(txtCoverageNo, kartuSep);

            String[] diagUtama = cariDiagnosa(noRawat, statusLanjut, "1");
            terisi += setTextIfValue(txtDiagnosisCode, diagUtama[0]);
            terisi += setTextIfValue(txtDiagnosisDisplay, diagUtama[1]);

            String[] diagSekunder = cariDiagnosa(noRawat, statusLanjut, "2");
            terisi += setTextIfValue(txtDiagnosisSekunderCode, diagSekunder[0]);
            terisi += setTextIfValue(txtDiagnosisSekunderDisplay, diagSekunder[1]);

            String patientId = cariPatientIdSatuSehat(noRm, nikPasien);
            terisi += setTextIfValue(txtPatientId, bersihkanReference(patientId, "Patient/"));

            String practitionerId = cariPractitionerIdSatuSehat(kdDokter, nikDokter);
            terisi += setTextIfValue(txtPractitionerId, bersihkanReference(practitionerId, "Practitioner/"));

            String encounter = cariEncounterSatuSehat(noRawat);
            if (encounter.length() > 0 && !encounter.startsWith("Encounter/")) {
                encounter = "Encounter/" + encounter;
            }
            terisi += setTextIfValue(txtEncounterRef, encounter);

            if (txtCatatan.getText().trim().length() == 0) {
                terisi += setTextIfValue(txtCatatan, "Rujukan pasien " + namaPasien + " dengan diagnosa " + firstNonEmpty(diagUtama[0], txtDiagnosisCode.getText()));
            }

            info.append("Data utama SIMRS ditemukan untuk ").append(namaPasien).append(".");
            info.append("\n\nBerhasil diambil/diupdate:");
            info.append("\n- Nama Pasien: ").append(namaPasien.length() > 0 ? namaPasien : "-");
            info.append("\n- No. SEP: ").append(txtNoSep.getText().trim().length() > 0 ? txtNoSep.getText().trim() : "-");
            info.append("\n- No. Kartu: ").append(txtCoverageNo.getText().trim().length() > 0 ? txtCoverageNo.getText().trim() : "-");
            info.append("\n- Diagnosa: ").append(txtDiagnosisCode.getText().trim().length() > 0 ? txtDiagnosisCode.getText().trim() : "-");
            info.append("\n\nCatatan SATUSEHAT:");
            if (patientId.length() == 0) {
                info.append("\n- Patient ID belum ada di database lokal. Jalankan/cek menu Referensi Pasien SATUSEHAT atau pastikan kolom/tabel penyimpanan IHS pasien ada.");
            } else {
                info.append("\n- Patient ID ditemukan.");
            }
            if (practitionerId.length() == 0) {
                info.append("\n- Practitioner ID belum ada di database lokal. Jalankan/cek menu Referensi Praktisi SATUSEHAT untuk dokter terkait.");
            } else {
                info.append("\n- Practitioner ID ditemukan.");
            }
            if (encounter.length() == 0) {
                info.append("\n- Encounter belum ada di database lokal. Pastikan Encounter kunjungan ini sudah dikirim ke SATUSEHAT dan ID responsenya tersimpan.");
            } else {
                info.append("\n- Encounter ditemukan.");
            }
        } catch (Exception ex) {
            info.append("Gagal mengambil data SIMRS: ").append(ex.getMessage());
        } finally {
            try { if (rsLocal != null) rsLocal.close(); } catch (Exception e) {}
            try { if (psLocal != null) psLocal.close(); } catch (Exception e) {}
            setCursor(Cursor.getDefaultCursor());
        }

        appendStatus("Ambil data SIMRS selesai. Field terisi/diupdate: " + terisi);
        JOptionPane.showMessageDialog(this, info.toString(), "Ambil Data SIMRS Khanza", JOptionPane.INFORMATION_MESSAGE);
    }


    private void ambilSemuaDariApiSatuSehat() {
        ambilPatientPractitionerDariApiSatuSehat();
        ambilEncounterDariApiSatuSehat();
    }

    private void ambilPatientPractitionerDariApiSatuSehat() {
        if (!validSatuSehatAuth()) return;
        String noRawat = txtNoRawat.getText().trim();
        if (noRawat.length() == 0) {
            JOptionPane.showMessageDialog(this, "Isi No. Rawat dulu, lalu klik Ambil dari SIMRS Khanza agar NIK pasien/dokter bisa ditemukan.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nikPasien = ambilNikPasienDariNoRawat(noRawat);
        String nikDokter = ambilNikDokterDariNoRawat(noRawat);
        StringBuilder validasi = new StringBuilder();
        if (nikPasien.length() == 0) validasi.append("- NIK pasien kosong di data pasien/no_ktp.\n");
        if (nikDokter.length() == 0) validasi.append("- NIK dokter kosong di data pegawai/no_ktp.\n");
        if (validasi.length() > 0) {
            JOptionPane.showMessageDialog(this, "Tidak bisa ambil ke API SATUSEHAT karena:\n" + validasi.toString(), "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        appendStatus("Mengambil Patient ID dan Practitioner ID dari API SATUSEHAT...");
        ambilPatientDariApiSatuSehat(nikPasien);
        ambilPractitionerDariApiSatuSehat(nikDokter);
    }

    private void ambilPatientDariApiSatuSehat(String nikPasien) {
        String identifier = "https://fhir.kemkes.go.id/id/nik|" + nikPasien;
        String path = "/Patient?identifier=" + url(identifier);
        executeSatuSehat("GET", path, null, "GET PATIENT BY NIK", (String response) -> {
            String id = extractResourceId(response, "Patient");
            if (id.length() > 0) {
                txtPatientId.setText(id);
                appendStatus("Patient ID ditemukan dari API SATUSEHAT: " + id);
                JOptionPane.showMessageDialog(this, "Patient ID ditemukan dari API SATUSEHAT:\n" + id, "Ambil Patient SATUSEHAT", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Patient ID tidak ditemukan dari API SATUSEHAT untuk NIK pasien: " + nikPasien + "\n\nCek tab JSON/Response untuk detail respons.", "Ambil Patient SATUSEHAT", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void ambilPractitionerDariApiSatuSehat(String nikDokter) {
        String identifier = "https://fhir.kemkes.go.id/id/nik|" + nikDokter;
        String path = "/Practitioner?identifier=" + url(identifier);
        executeSatuSehat("GET", path, null, "GET PRACTITIONER BY NIK", (String response) -> {
            String id = extractResourceId(response, "Practitioner");
            if (id.length() > 0) {
                txtPractitionerId.setText(id);
                appendStatus("Practitioner ID ditemukan dari API SATUSEHAT: " + id);
                JOptionPane.showMessageDialog(this, "Practitioner ID ditemukan dari API SATUSEHAT:\n" + id, "Ambil Practitioner SATUSEHAT", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Practitioner ID tidak ditemukan dari API SATUSEHAT untuk NIK dokter: " + nikDokter + "\n\nCek tab JSON/Response untuk detail respons.", "Ambil Practitioner SATUSEHAT", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void ambilEncounterDariApiSatuSehat() {
        if (!validSatuSehatAuth()) return;
        String noRawat = txtNoRawat.getText().trim();
        String orgId = txtOrgPerujuk.getText().trim();
        if (noRawat.length() == 0) {
            JOptionPane.showMessageDialog(this, "Isi No. Rawat dulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (orgId.length() == 0) {
            JOptionPane.showMessageDialog(this, "Isi Organization Perujuk dulu, contoh Organization ID RS dari SATUSEHAT.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String identifier = "http://sys-ids.kemkes.go.id/encounter/" + orgId + "|" + noRawat;
        String path = "/Encounter?identifier=" + url(identifier);
        executeSatuSehat("GET", path, null, "GET ENCOUNTER BY NO RAWAT", (String response) -> {
            String id = extractResourceId(response, "Encounter");
            if (id.length() > 0) {
                txtEncounterRef.setText("Encounter/" + id);
                appendStatus("Encounter ditemukan dari API SATUSEHAT: Encounter/" + id);
                JOptionPane.showMessageDialog(this, "Encounter ditemukan dari API SATUSEHAT:\nEncounter/" + id, "Ambil Encounter SATUSEHAT", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Encounter tidak ditemukan dari API SATUSEHAT untuk no_rawat: " + noRawat + "\n\nPastikan Encounter sudah pernah dikirim dan identifier memakai system:\n" + identifier + "\n\nCek tab JSON/Response untuk detail respons.", "Ambil Encounter SATUSEHAT", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private String ambilNikPasienDariNoRawat(String noRawat) {
        return cariIsiOptional(
            "select p.no_ktp from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis where rp.no_rawat=? limit 1",
            noRawat
        );
    }

    private String ambilNikDokterDariNoRawat(String noRawat) {
        String nik = cariIsiOptional(
            "select pg.no_ktp from reg_periksa rp inner join pegawai pg on rp.kd_dokter=pg.nik where rp.no_rawat=? limit 1",
            noRawat
        );
        if (nik.length() > 0) return nik;
        return cariIsiOptional(
            "select no_ktp from pegawai where nik=(select kd_dokter from reg_periksa where no_rawat=? limit 1) limit 1",
            noRawat
        );
    }

    private String extractResourceId(String json, String resourceType) {
        if (json == null || resourceType == null) return "";
        Pattern p1 = Pattern.compile("\\\"resourceType\\\"\\s*:\\s*\\\"" + Pattern.quote(resourceType) + "\\\"[\\s\\S]*?\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(json);
        if (m1.find()) return bersihkanReference(m1.group(1), resourceType + "/");

        Pattern p2 = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"[\\s\\S]*?\\\"resourceType\\\"\\s*:\\s*\\\"" + Pattern.quote(resourceType) + "\\\"", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(json);
        if (m2.find()) return bersihkanReference(m2.group(1), resourceType + "/");
        return "";
    }

    private String[] cariDiagnosa(String noRawat, String statusLanjut, String prioritas) {
        String kd = "", nm = "";
        String status1 = "Ranap".equalsIgnoreCase(statusLanjut) ? "ranap" : "ralan";
        String status2 = "Ranap".equalsIgnoreCase(statusLanjut) ? "Ranap" : "Ralan";
        kd = cariIsiOptional(
            "select kd_penyakit from diagnosa_pasien where no_rawat=? and prioritas=? and (status=? or status=?) limit 1",
            noRawat, prioritas, status1, status2
        );
        if (kd.length() == 0) {
            kd = cariIsiOptional("select kd_penyakit from diagnosa_pasien where no_rawat=? and prioritas=? limit 1", noRawat, prioritas);
        }
        if (kd.length() == 0 && "1".equals(prioritas)) {
            kd = cariIsiOptional("select kd_diagnosa_utama from resume_pasien where no_rawat=? limit 1", noRawat);
        }
        if (kd.length() == 0 && "2".equals(prioritas)) {
            kd = cariIsiOptional("select kd_diagnosa_sekunder from resume_pasien where no_rawat=? limit 1", noRawat);
        }
        if (kd.length() > 0) {
            nm = cariIsiOptional("select nm_penyakit from penyakit where kd_penyakit=? limit 1", kd);
        }
        return new String[]{kd, nm};
    }

    private String cariPatientIdSatuSehat(String noRm, String nik) {
        if ((noRm == null || noRm.trim().length() == 0) && (nik == null || nik.trim().length() == 0)) return "";
        String hasil = cariIsiOptional(new String[]{
            "select no_ihs from pasien where no_rkm_medis=? limit 1",
            "select ihs_number from pasien where no_rkm_medis=? limit 1",
            "select id_pasien from pasien where no_rkm_medis=? limit 1",
            "select patient_id from pasien where no_rkm_medis=? limit 1",
            "select id_satusehat from pasien where no_rkm_medis=? limit 1",
            "select no_ihs from satusehat_pasien where no_rkm_medis=? limit 1",
            "select ihs_number from satusehat_pasien where no_rkm_medis=? limit 1",
            "select patient from satusehat_pasien where no_rkm_medis=? limit 1",
            "select patient_id from satusehat_pasien where no_rkm_medis=? limit 1",
            "select id_pasien from satusehat_pasien where no_rkm_medis=? limit 1",
            "select no_ihs from satusehat_patient where no_rkm_medis=? limit 1",
            "select ihs_number from satusehat_patient where no_rkm_medis=? limit 1",
            "select patient_id from satusehat_patient where no_rkm_medis=? limit 1",
            "select id_pasien from satusehat_patient where no_rkm_medis=? limit 1",
            "select no_ihs from satu_sehat_referensi_pasien where no_rkm_medis=? limit 1",
            "select ihs_number from satu_sehat_referensi_pasien where no_rkm_medis=? limit 1",
            "select id_pasien from satu_sehat_referensi_pasien where no_rkm_medis=? limit 1",
            "select patient_id from satu_sehat_referensi_pasien where no_rkm_medis=? limit 1",
            "select id_pasien from satu_sehat_patient where no_rkm_medis=? limit 1",
            "select patient_id from satu_sehat_patient where no_rkm_medis=? limit 1"
        }, noRm);
        if (hasil.length() > 0 || nik == null || nik.trim().length() == 0) return hasil;
        return cariIsiOptional(new String[]{
            "select no_ihs from pasien where no_ktp=? limit 1",
            "select ihs_number from pasien where no_ktp=? limit 1",
            "select id_pasien from pasien where no_ktp=? limit 1",
            "select patient_id from pasien where no_ktp=? limit 1",
            "select no_ihs from satusehat_pasien where nik=? limit 1",
            "select ihs_number from satusehat_pasien where nik=? limit 1",
            "select patient_id from satusehat_pasien where nik=? limit 1",
            "select id_pasien from satusehat_pasien where nik=? limit 1",
            "select id_pasien from satu_sehat_referensi_pasien where nik=? limit 1",
            "select patient_id from satu_sehat_referensi_pasien where nik=? limit 1",
            "select ihs_number from satu_sehat_referensi_pasien where nik=? limit 1"
        }, nik);
    }

    private String cariPractitionerIdSatuSehat(String kdDokter, String nikDokter) {
        if ((kdDokter == null || kdDokter.trim().length() == 0) && (nikDokter == null || nikDokter.trim().length() == 0)) return "";
        String hasil = cariIsiOptional(new String[]{
            "select no_ihs from pegawai where nik=? limit 1",
            "select ihs_number from pegawai where nik=? limit 1",
            "select id_practitioner from pegawai where nik=? limit 1",
            "select practitioner_id from pegawai where nik=? limit 1",
            "select id_practitioner from satusehat_practitioner where kd_dokter=? limit 1",
            "select practitioner_id from satusehat_practitioner where kd_dokter=? limit 1",
            "select practitioner from satusehat_practitioner where kd_dokter=? limit 1",
            "select ihs_number from satusehat_practitioner where kd_dokter=? limit 1",
            "select no_ihs from satusehat_practitioner where kd_dokter=? limit 1",
            "select id_practitioner from satusehat_praktisi where kd_dokter=? limit 1",
            "select practitioner_id from satusehat_praktisi where kd_dokter=? limit 1",
            "select practitioner from satusehat_praktisi where kd_dokter=? limit 1",
            "select ihs_number from satusehat_praktisi where kd_dokter=? limit 1",
            "select id_practitioner from satu_sehat_referensi_praktisi where kd_dokter=? limit 1",
            "select practitioner_id from satu_sehat_referensi_praktisi where kd_dokter=? limit 1",
            "select ihs_number from satu_sehat_referensi_praktisi where kd_dokter=? limit 1"
        }, kdDokter);
        if (hasil.length() > 0 || nikDokter == null || nikDokter.trim().length() == 0) return hasil;
        return cariIsiOptional(new String[]{
            "select no_ihs from pegawai where no_ktp=? limit 1",
            "select ihs_number from pegawai where no_ktp=? limit 1",
            "select id_practitioner from pegawai where no_ktp=? limit 1",
            "select practitioner_id from pegawai where no_ktp=? limit 1",
            "select id_practitioner from satusehat_practitioner where nik=? limit 1",
            "select practitioner_id from satusehat_practitioner where nik=? limit 1",
            "select practitioner from satusehat_practitioner where nik=? limit 1",
            "select ihs_number from satusehat_practitioner where nik=? limit 1",
            "select id_practitioner from satusehat_praktisi where nik=? limit 1",
            "select practitioner_id from satusehat_praktisi where nik=? limit 1",
            "select ihs_number from satusehat_praktisi where nik=? limit 1",
            "select id_practitioner from satu_sehat_referensi_praktisi where nik=? limit 1",
            "select practitioner_id from satu_sehat_referensi_praktisi where nik=? limit 1",
            "select ihs_number from satu_sehat_referensi_praktisi where nik=? limit 1"
        }, nikDokter);
    }

    private String cariEncounterSatuSehat(String noRawat) {
        if (noRawat == null || noRawat.trim().length() == 0) return "";
        return cariIsiOptional(new String[]{
            "select id_encounter from satusehat_encounter where no_rawat=? order by tgl_kirim desc limit 1",
            "select encounter from satusehat_encounter where no_rawat=? order by tgl_kirim desc limit 1",
            "select id_encounter from satu_sehat_encounter where no_rawat=? order by tgl_kirim desc limit 1",
            "select encounter from satu_sehat_encounter where no_rawat=? order by tgl_kirim desc limit 1",
            "select id_encounter from satusehat_kirim_encounter where no_rawat=? order by tgl_kirim desc limit 1",
            "select encounter from satusehat_kirim_encounter where no_rawat=? order by tgl_kirim desc limit 1",
            "select id_encounter from satu_sehat_kirim_encounter where no_rawat=? order by tgl_kirim desc limit 1",
            "select encounter from satu_sehat_kirim_encounter where no_rawat=? order by tgl_kirim desc limit 1",
            "select id_encounter from satusehat_response where no_rawat=? and resource_type='Encounter' order by tgl_kirim desc limit 1",
            "select resource_id from satusehat_response where no_rawat=? and resource_type='Encounter' order by tgl_kirim desc limit 1",
            "select id_encounter from satu_sehat_response where no_rawat=? and resource_type='Encounter' order by tgl_kirim desc limit 1",
            "select resource_id from satu_sehat_response where no_rawat=? and resource_type='Encounter' order by tgl_kirim desc limit 1"
        }, noRawat);
    }

    private String cariIsiOptional(String[] sqls, String... params) {
        for (String sql : sqls) {
            String hasil = cariIsiOptional(sql, params);
            if (hasil.length() > 0) return hasil;
        }
        return "";
    }

    private String cariIsiOptional(String sql, String... params) {
        PreparedStatement psCari = null;
        ResultSet rsCari = null;
        try {
            psCari = koneksi.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                psCari.setString(i + 1, params[i] == null ? "" : params[i]);
            }
            rsCari = psCari.executeQuery();
            if (rsCari.next()) {
                String hasil = rsCari.getString(1);
                return hasil == null ? "" : hasil.trim();
            }
        } catch (Exception e) {
            // Abaikan agar tetap kompatibel dengan variasi struktur tabel Khanza/SATUSEHAT di tiap RS.
        } finally {
            try { if (rsCari != null) rsCari.close(); } catch (Exception e) {}
            try { if (psCari != null) psCari.close(); } catch (Exception e) {}
        }
        return "";
    }

    private int setTextIfValue(JTextComponent field, String value) {
        if (field == null || value == null) return 0;
        String v = value.trim();
        if (v.length() == 0) return 0;
        if (!v.equals(field.getText().trim())) {
            field.setText(v);
            return 1;
        }
        return 0;
    }

    private String bersihkanReference(String value, String prefix) {
        String v = value == null ? "" : value.trim();
        if (v.startsWith(prefix)) return v.substring(prefix.length());
        return v;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    private String patientReference() {
        String v = txtPatientId.getText().trim();
        if (v.length() == 0) return "";
        return v.startsWith("Patient/") ? v : "Patient/" + v;
    }

    private String encounterReference() {
        String v = txtEncounterRef.getText().trim();
        if (v.length() == 0) return "";
        return v.startsWith("Encounter/") ? v : "Encounter/" + v;
    }

    private String orgBpjsId() {
        return firstNonEmpty(txtOrgBpjs.getText(), txtOrgPerujuk.getText());
    }

    private String orgTujuanId() {
        return firstNonEmpty(txtOrgTujuan.getText(), txtKodeFaskesSS.getText());
    }

    private boolean validServiceRequestInput() {
        StringBuilder msg = new StringBuilder();
        if (txtPatientId.getText().trim().length() == 0) msg.append("- Patient ID masih kosong. Klik Ambil Patient/Practitioner dari API SATUSEHAT.\n");
        if (txtEncounterRef.getText().trim().length() == 0) msg.append("- Encounter masih kosong. Klik Ambil Encounter dari API atau kirim Encounter dulu dari menu SATUSEHAT Kirim Encounter.\n");
        if (txtOrgPerujuk.getText().trim().length() == 0) msg.append("- Organization Perujuk masih kosong. Isi Organization ID SATUSEHAT RS.\n");
        if (orgTujuanId().length() == 0) msg.append("- Organization Tujuan masih kosong. Pilih faskes tujuan dari Get Faskes atau isi Organization Tujuan SATUSEHAT.\n");
        if (orgBpjsId().length() == 0) msg.append("- Organization BPJS/Perujuk masih kosong, sehingga insurance-subscriber system tidak valid.\n");
        if (txtCoverageNo.getText().trim().length() == 0) msg.append("- No kartu BPJS/Coverage masih kosong. Klik Ambil dari SIMRS Khanza.\n");
        if (txtDiagnosisCode.getText().trim().length() == 0) msg.append("- Diagnosa utama masih kosong.\n");
        if (msg.length() > 0) {
            JOptionPane.showMessageDialog(this, "Data belum lengkap untuk kirim ServiceRequest:\n\n" + msg.toString(), "Validasi ServiceRequest", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private String fhirDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
    }

    private String newRegistrationId() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date()) + String.valueOf((int)(Math.random() * 90000 + 10000));
    }

    private String uuid() {
        return UUID.randomUUID().toString();
    }

    private String normalizeBaseUrl(String url) {
        String u = url.trim();
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        return u;
    }

    private String indent(String json, int spaces) {
        String pad = "";
        for (int i = 0; i < spaces; i++) pad += " ";
        return json.replace("\n", "\n" + pad);
    }

    private String pick(String json, String key) {
        if (json == null || key == null) return "";
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"?([^\\\",}\\n\\r]*)\\\"?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1).trim();
        return "";
    }

    private String summarize(String text) {
        if (text == null) return "";
        String s = text.replace('\n', ' ').replace('\r', ' ').trim();
        return s.length() > 150 ? s.substring(0, 150) + "..." : s;
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) if (v != null && v.trim().length() > 0) return v.trim();
        return "";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    private String url(String s) {
        try { return URLEncoder.encode(s == null ? "" : s, "UTF-8"); }
        catch (Exception e) { return ""; }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
