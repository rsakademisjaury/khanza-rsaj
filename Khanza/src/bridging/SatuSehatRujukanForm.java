package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import laporan.DlgCariPenyakit;
import org.springframework.web.client.HttpStatusCodeException;

public class SatuSehatRujukanForm extends JDialog {
    private static final Color BORDER_COLOR = new Color(240, 245, 235);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color PANEL_COLOR = new Color(255, 255, 254);
    private static final Color ACTION_COLOR = new Color(250, 255, 245);
    private static final Font DEFAULT_FONT = new Font("Tahoma", Font.PLAIN, 11);
    private static final Font SECTION_FONT = new Font("Tahoma", Font.BOLD, 11);
    private static final int KANDIDAT_PILIH = 0;
    private static final int KANDIDAT_NO = 1;
    private static final int KANDIDAT_ORG_ID = 2;
    private static final int KANDIDAT_NAMA = 3;
    private static final int KANDIDAT_JARAK = 4;
    private static final int KANDIDAT_ESTIMASI = 5;
    private static final int KANDIDAT_STRATA = 6;
    private static final int KANDIDAT_BPJS = 7;
    private static final int KANDIDAT_KEMKES = 8;
    private static final int KANDIDAT_TASK = 9;
    private static final int KANDIDAT_STATUS = 10;
    private static final int KANDIDAT_RESPON = 11;

    private final JComboBox<String> jenisRujukan = new JComboBox<String>(new String[]{"Rawat Jalan", "Rawat Inap", "IGD"});
    private final JTextField baseUrl = new JTextField();
    private final JTextField nomorRujukan = new JTextField();
    private final JTextField kodeFaskesSatuSehat = new JTextField();
    private final JTextField kodeProvinsiWilayah = new JTextField();
    private final JTextField namaProvinsiWilayah = new JTextField();
    private final JTextField kodeKabupatenWilayah = new JTextField();
    private final JTextField namaKabupatenWilayah = new JTextField();
    private final JTextField patientId = new JTextField();
    private final JTextField patientName = new JTextField();
    private final JTextField practitionerId = new JTextField();
    private final JTextField practitionerName = new JTextField();
    private final JTextField practitionerRujukanId = new JTextField();
    private final JTextField practitionerRujukanName = new JTextField();
    private final JTextField orgPerujukId = new JTextField();
    private final JTextField orgPerujukName = new JTextField();
    private final JTextField orgRujukanId = new JTextField();
    private final JTextField orgRujukanName = new JTextField();
    private final JTextField encounterId = new JTextField();
    private final JTextField diagnosisCode = new JTextField();
    private final JTextField diagnosisDisplay = new JTextField();
    private final JButton btnCariDiagnosa = new JButton();
    private final JTextField performerTypeCode = new JTextField();
    private final JTextField performerTypeDisplay = new JTextField();
    private final widget.Tanggal tanggalRujuk = new widget.Tanggal();
    private final widget.ComboBox jamRujuk = new widget.ComboBox();
    private final widget.ComboBox menitRujuk = new widget.ComboBox();
    private final widget.ComboBox detikRujuk = new widget.ComboBox();
    private final JPanel panelJamRujuk = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
    private final JTextField authoredOn = new JTextField();
    private final JTextField occurrenceDateTime = new JTextField();
    private final JTextField appointmentStart = new JTextField();
    private final JTextField appointmentEnd = new JTextField();
    private final JTextArea keterangan = new JTextArea(3, 20);
    private final JTextArea supportingInfo = new JTextArea(5, 20);
    private final JTextArea hasilRadiologi = new JTextArea(4, 20);
    private final JTextArea carePlanInfo = new JTextArea(8, 20);
    private final DefaultTableModel tabModeKriteriaRujukan = createTabModeKriteriaRujukan();
    private final JTable tbKriteriaRujukan = new JTable(tabModeKriteriaRujukan);
    private final DefaultTableModel tabModeKandidatFaskes = createTabModeKandidatFaskes();
    private final JTable tbKandidatFaskes = new JTable(tabModeKandidatFaskes);
    private final DefaultTableModel tabModeDataRujukan = createTabModeDataRujukan();
    private final JTable tbDataRujukan = new JTable(tabModeDataRujukan);
    private final ObjectMapper mapper = new ObjectMapper();
    private final SatuSehatKirimRujukan rujukan = new SatuSehatKirimRujukan();
    private final sekuel Sequel = new sekuel();
    private final Connection koneksi = koneksiDB.condb();
    private String taskPraPermintaanId = "";
    private String taskPencarianKandidatId = "";
    private String taskRujukanId = "";
    private String carePlanId = "";
    private String serviceRequestId = "";
    private String appointmentId = "";
    private String statusTaskRujukan = "";
    private String responTaskRujukan = "";

    public SatuSehatRujukanForm(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        pastikanKolomKandidatRujukan();
        isiDefault();
        muatDataRujukanTersimpan();
    }

    private void initComponents() {
        setTitle("Rujukan SATUSEHAT");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setMinimumSize(new Dimension(915, 720));
        setPreferredSize(new Dimension(915, 760));
        setLayout(new BorderLayout());

        widget.InternalFrame internalFrame = new widget.InternalFrame();
        internalFrame.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_COLOR),
                "::[ Rujukan SATUSEHAT ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, DEFAULT_FONT, TEXT_COLOR));
        internalFrame.setFont(new Font("Tahoma", Font.ITALIC, 12));
        internalFrame.setName("internalFrame1");
        internalFrame.setLayout(new BorderLayout(1, 1));

        JTabbedPane tabRawat = new JTabbedPane();
        tabRawat.setBackground(PANEL_COLOR);
        tabRawat.setForeground(TEXT_COLOR);
        tabRawat.setFont(DEFAULT_FONT);
        tabRawat.setName("TabRawat");

        widget.ScrollPane scrollInput = new widget.ScrollPane();
        scrollInput.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        scrollInput.setName("ScrollInput");
        scrollInput.setOpaque(true);
        scrollInput.setViewportView(formPanel());
        tabRawat.addTab("Input Rujukan SATUSEHAT", scrollInput);

        widget.ScrollPane scrollTeknis = new widget.ScrollPane();
        scrollTeknis.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        scrollTeknis.setName("ScrollDetailTeknis");
        scrollTeknis.setOpaque(true);
        scrollTeknis.setViewportView(detailTeknisPanel());
        tabRawat.addTab("Detail Teknis", scrollTeknis);

        widget.ScrollPane scrollData = new widget.ScrollPane();
        scrollData.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        scrollData.setName("ScrollDataRujukan");
        scrollData.setOpaque(true);
        scrollData.setViewportView(dataRujukanPanel());
        tabRawat.addTab("Data Rujukan", scrollData);

        internalFrame.add(tabRawat, BorderLayout.CENTER);

        widget.panelisi actions = new widget.panelisi();
        actions.setName("panelGlass8");
        actions.setPreferredSize(new Dimension(44, 54));
        actions.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        JButton btnKirimTugasRujukan = new JButton("Tugas Rujukan");
        JButton btnKirimPermintaanRujukan = new JButton("Permintaan Rujukan");
        JButton btnCekStatusRujukan = new JButton("Cek Status");
        JButton btnKirimAppointment = new JButton("Kirim Appointment");
        JButton btnTutup = new JButton("Tutup");
        styleActionButton(btnKirimTugasRujukan, "save-16x16.png", 118);
        styleActionButton(btnKirimPermintaanRujukan, "save-16x16.png", 138);
        styleActionButton(btnCekStatusRujukan, "Search-16x16.png", 100);
        styleActionButton(btnKirimAppointment, "save-16x16.png", 132);
        styleActionButton(btnTutup, "exit.png", 82);
        btnKirimTugasRujukan.addActionListener(e -> kirimTugasRujukan());
        btnKirimPermintaanRujukan.addActionListener(e -> kirimPermintaanRujukan());
        btnCekStatusRujukan.addActionListener(e -> cekStatusRujukan());
        btnKirimAppointment.addActionListener(e -> tampilkanDialogAppointment());
        btnTutup.addActionListener(e -> dispose());
        jenisRujukan.addActionListener(e -> isiDefaultKriteriaRujukan());
        actions.add(btnKirimTugasRujukan);
        actions.add(btnKirimPermintaanRujukan);
        actions.add(btnCekStatusRujukan);
        actions.add(btnKirimAppointment);
        actions.add(btnTutup);
        internalFrame.add(actions, BorderLayout.PAGE_END);
        add(internalFrame, BorderLayout.CENTER);
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        rapikanInput();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int row = 0;
        row = addSection(panel, c, row, "Data Pasien");
        row = addRow(panel, c, row, "No. Rujukan", nomorRujukan, "Jenis Rujukan", jenisRujukan);
        row = addRow(panel, c, row, "Nama Pasien", patientName, "Dokter Perujuk", practitionerName);
        row = addRow(panel, c, row, "Tgl Rujukan", tanggalRujuk, "Jam Rujukan", panelJamRujuk);

        row = addSection(panel, c, row, "Data Rujukan");
        row = addRow(panel, c, row, "Faskes Perujuk", orgPerujukName, "Faskes Tujuan", orgRujukanName);
        row = addRow(panel, c, row, "Praktisi Tujuan", practitionerRujukanName, "Spesialis", performerTypeDisplay);
        row = addRowComponent(panel, c, row, "Kode Diagnosa", panelKodeDiagnosa(), "Diagnosa Rujuk", diagnosisDisplay);
        row = addRow(panel, c, row, "Provinsi", namaProvinsiWilayah, "Kab/Kota", namaKabupatenWilayah);
        row = addTextAreaRow(panel, c, row, "Keterangan", keterangan, 0.18);

        row = addSection(panel, c, row, "Kriteria Tindakan");

        JPanel kriteriaActions = new JPanel(new BorderLayout(5, 5));
        JPanel kriteriaKirimActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton btnKirimPraPermintaan = new JButton("Pra Permintaan");
        JButton btnKirimKandidat = new JButton("Kirim Kandidat");
        JButton btnHapusKriteria = new JButton("Hapus");
        kriteriaActions.setBackground(PANEL_COLOR);
        kriteriaKirimActions.setBackground(PANEL_COLOR);
        styleButton(btnKirimPraPermintaan, "save-16x16.png", 118);
        styleButton(btnKirimKandidat, "accept.png", 120);
        styleButton(btnHapusKriteria, "stop_f2.png", 82);
        btnKirimPraPermintaan.addActionListener(e -> kirimPraPermintaan());
        btnKirimKandidat.addActionListener(e -> kirimPencarianKandidat());
        btnHapusKriteria.addActionListener(e -> removeSelectedKriteriaRujukanRows());
        kriteriaKirimActions.add(btnKirimPraPermintaan);
        kriteriaKirimActions.add(btnKirimKandidat);
        kriteriaActions.add(kriteriaKirimActions, BorderLayout.WEST);
        kriteriaActions.add(btnHapusKriteria, BorderLayout.EAST);

        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(label("Kriteria Rujukan"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        panel.add(kriteriaActions, c);
        c.gridwidth = 1;
        row++;

        setupKriteriaTable();
        c.gridx = 1;
        c.gridy = row;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 0.42;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(tbKriteriaRujukan), c);
        c.gridwidth = 1;
        row++;

        row = addSection(panel, c, row, "Kandidat Faskes");

        widget.panelisi kandidatActions = new widget.panelisi();
        kandidatActions.setName("panelKandidatInput");
        kandidatActions.setPreferredSize(new Dimension(44, 43));
        kandidatActions.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        JButton btnAmbilKandidat = new JButton("Ambil");
        JButton btnHapusKandidat = new JButton("Kosongkan");
        styleActionButton(btnAmbilKandidat, "Search-16x16.png", 90);
        styleActionButton(btnHapusKandidat, "Cancel-2-16x16.png", 105);
        btnAmbilKandidat.addActionListener(e -> ambilKandidatFaskes());
        btnHapusKandidat.addActionListener(e -> resetKandidatFaskes());
        kandidatActions.add(btnAmbilKandidat);
        kandidatActions.add(btnHapusKandidat);

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label("Kandidat Faskes"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        panel.add(kandidatActions, c);
        c.gridwidth = 1;
        row++;

        setupKandidatTable();
        c.gridx = 1;
        c.gridy = row;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 0.55;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(tbKandidatFaskes), c);

        return panel;
    }

    private JPanel detailTeknisPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        rapikanInput();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int row = 0;
        row = addSection(panel, c, row, "FHIR dan Endpoint");
        row = addRow(panel, c, row, "Base URL", baseUrl, "Kode Faskes", kodeFaskesSatuSehat);

        row = addSection(panel, c, row, "ID Resource");
        row = addRow(panel, c, row, "Patient ID", patientId, "Practitioner ID", practitionerId);
        row = addRow(panel, c, row, "Encounter ID", encounterId, "Org Perujuk ID", orgPerujukId);
        row = addRow(panel, c, row, "Org Tujuan ID", orgRujukanId, "Praktisi Tujuan ID", practitionerRujukanId);

        row = addSection(panel, c, row, "Kode dan Waktu");
        row = addSingleRow(panel, c, row, "Kode Spesialis", performerTypeCode);
        row = addRow(panel, c, row, "Authored On", authoredOn, "Occurrence", occurrenceDateTime);
        row = addRow(panel, c, row, "Appointment Mulai", appointmentStart, "Appointment Selesai", appointmentEnd);
        row = addRow(panel, c, row, "Kode Provinsi", kodeProvinsiWilayah, "Kode Kab/Kota", kodeKabupatenWilayah);

        row = addSection(panel, c, row, "Data Pendukung");
        row = addTextAreaRow(panel, c, row, "CarePlan Terkirim", carePlanInfo, 0.24);

        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label("Supporting Info"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.24;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(supportingInfo), c);
        JButton btnAmbilPendukung = new JButton("Ambil Pendukung");
        styleButton(btnAmbilPendukung, "Search-16x16.png", 132);
        btnAmbilPendukung.addActionListener(e -> ambilDataPendukungKlinis(true));
        c.gridx = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(btnAmbilPendukung, c);
        row++;

        row = addTextAreaRow(panel, c, row, "Hasil Radiologi", hasilRadiologi, 0.24);
        return panel;
    }

    private JPanel dataRujukanPanel() {
        JPanel panel = new JPanel(new BorderLayout(1, 1));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        widget.panelisi dataActions = new widget.panelisi();
        dataActions.setName("panelDataRujukan");
        dataActions.setPreferredSize(new Dimension(44, 43));
        dataActions.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnPakaiData = new JButton("Pakai Data");
        styleActionButton(btnRefresh, "Search-16x16.png", 100);
        styleActionButton(btnPakaiData, "edit_f2.png", 105);
        btnRefresh.addActionListener(e -> muatDataRujukanTersimpan());
        btnPakaiData.addActionListener(e -> pakaiDataRujukanTerpilih());
        dataActions.add(btnRefresh);
        dataActions.add(btnPakaiData);
        panel.add(dataActions, BorderLayout.PAGE_END);

        setupDataRujukanTable();
        widget.ScrollPane scroll = new widget.ScrollPane();
        scroll.setOpaque(true);
        scroll.setViewportView(tbDataRujukan);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private int addSection(JPanel panel, GridBagConstraints c, int row, String title) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 4;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel label = new JLabel(title);
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT_COLOR);
        label.setBorder(BorderFactory.createEmptyBorder(row == 0 ? 0 : 8, 0, 2, 0));
        panel.add(label, c);
        c.gridwidth = 1;
        return row + 1;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DEFAULT_FONT);
        label.setForeground(TEXT_COLOR);
        label.setPreferredSize(new Dimension(115, 23));
        return label;
    }

    private JPanel panelKodeDiagnosa() {
        JPanel panel = new JPanel(new BorderLayout(3, 0));
        panel.setBackground(PANEL_COLOR);
        styleButton(btnCariDiagnosa, "Search-16x16.png", 28);
        btnCariDiagnosa.setToolTipText("Cari diagnosa dari tabel penyakit");
        if (!Boolean.TRUE.equals(btnCariDiagnosa.getClientProperty("action-cari-diagnosa"))) {
            btnCariDiagnosa.putClientProperty("action-cari-diagnosa", Boolean.TRUE);
            btnCariDiagnosa.addActionListener(e -> bukaDialogDiagnosa());
        }
        panel.add(diagnosisCode, BorderLayout.CENTER);
        panel.add(btnCariDiagnosa, BorderLayout.EAST);
        return panel;
    }

    private void bukaDialogDiagnosa() {
        DlgCariPenyakit penyakit = new DlgCariPenyakit(null, false);
        penyakit.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                pilihDiagnosa(penyakit);
                diagnosisDisplay.requestFocus();
            }
        });
        penyakit.isCek();
        penyakit.emptTeks();
        penyakit.setSize(Math.max(650, getWidth() - 20), Math.max(250, getHeight() - 80));
        penyakit.setLocationRelativeTo(this);
        penyakit.setVisible(true);
    }

    private void pilihDiagnosa(DlgCariPenyakit penyakit) {
        JTable table = penyakit.getTable();
        int row = table.getSelectedRow();
        if(row == -1){
            return;
        }
        diagnosisCode.setText(nilai(String.valueOf(table.getValueAt(row, 0))));
        diagnosisDisplay.setText(nilai(String.valueOf(table.getValueAt(row, 1))));
    }

    private int addRow(JPanel panel, GridBagConstraints c, int row, String label1, JTextField field1, String label2, JLabel labelOnly) {
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = 0;
        panel.add(label(label1), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field1, c);
        c.gridx = 2;
        c.weightx = 0;
        labelOnly.setFont(DEFAULT_FONT);
        labelOnly.setForeground(TEXT_COLOR);
        panel.add(labelOnly, c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(new JLabel(""), c);
        return row + 1;
    }

    private int addRow(JPanel panel, GridBagConstraints c, int row, String label1, JTextField field1, String label2, JTextField field2) {
        return addRowComponent(panel, c, row, label1, field1, label2, field2);
    }

    private int addRow(JPanel panel, GridBagConstraints c, int row, String label1, widget.Tanggal field1, String label2, JTextField field2) {
        return addRowComponent(panel, c, row, label1, field1, label2, field2);
    }

    private int addRow(JPanel panel, GridBagConstraints c, int row, String label1, widget.Tanggal field1, String label2, JPanel field2) {
        return addRowComponent(panel, c, row, label1, field1, label2, field2);
    }

    private int addRowComponent(JPanel panel, GridBagConstraints c, int row, String label1, JComponent field1, String label2, JComponent field2) {
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = 0;
        panel.add(label(label1), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field1, c);
        c.gridx = 2;
        c.weightx = 0;
        panel.add(label(label2), c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(field2, c);
        return row + 1;
    }

    private int addSingleRow(JPanel panel, GridBagConstraints c, int row, String labelText, JComponent field) {
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        panel.add(label(labelText), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        panel.add(field, c);
        c.gridwidth = 1;
        return row + 1;
    }

    private int addTextAreaRow(JPanel panel, GridBagConstraints c, int row, String labelText, JTextArea textArea, double weightY) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label(labelText), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = weightY;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(textArea), c);
        c.gridwidth = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        return row + 1;
    }

    private int addRow(JPanel panel, GridBagConstraints c, int row, String label1, JTextField field1, String label2, JComboBox<String> combo) {
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = 0;
        panel.add(label(label1), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field1, c);
        c.gridx = 2;
        c.weightx = 0;
        panel.add(label(label2), c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(combo, c);
        return row + 1;
    }

    private void rapikanInput() {
        JTextField[] fields = new JTextField[]{
            baseUrl, nomorRujukan, kodeFaskesSatuSehat, kodeProvinsiWilayah, namaProvinsiWilayah,
            kodeKabupatenWilayah, namaKabupatenWilayah, patientId, patientName, practitionerId,
            practitionerName, practitionerRujukanId, practitionerRujukanName, orgPerujukId, orgPerujukName,
            orgRujukanId, orgRujukanName, encounterId, diagnosisCode, diagnosisDisplay, authoredOn,
            performerTypeCode, performerTypeDisplay, occurrenceDateTime, appointmentStart, appointmentEnd
        };
        for(JTextField field : fields){
            field.setFont(DEFAULT_FONT);
            field.setForeground(TEXT_COLOR);
            field.setPreferredSize(new Dimension(120, 23));
        }
        jenisRujukan.setFont(DEFAULT_FONT);
        jenisRujukan.setForeground(TEXT_COLOR);
        jenisRujukan.setPreferredSize(new Dimension(120, 23));
        tanggalRujuk.setFont(DEFAULT_FONT);
        tanggalRujuk.setForeground(TEXT_COLOR);
        tanggalRujuk.setPreferredSize(new Dimension(120, 23));
        tanggalRujuk.setDisplayFormat("dd-MM-yyyy");
        siapkanComboWaktuRujukan();
        keterangan.setFont(DEFAULT_FONT);
        keterangan.setForeground(TEXT_COLOR);
        keterangan.setLineWrap(true);
        keterangan.setWrapStyleWord(true);
        supportingInfo.setFont(DEFAULT_FONT);
        supportingInfo.setForeground(TEXT_COLOR);
        supportingInfo.setLineWrap(true);
        supportingInfo.setWrapStyleWord(true);
        hasilRadiologi.setFont(DEFAULT_FONT);
        hasilRadiologi.setForeground(TEXT_COLOR);
        hasilRadiologi.setLineWrap(true);
        hasilRadiologi.setWrapStyleWord(true);
        carePlanInfo.setFont(DEFAULT_FONT);
        carePlanInfo.setForeground(TEXT_COLOR);
        carePlanInfo.setLineWrap(true);
        carePlanInfo.setWrapStyleWord(true);
        carePlanInfo.setEditable(false);
        carePlanInfo.setBackground(new Color(245, 245, 245));
        pasangSinkronWaktuRujukan();
    }

    private void styleButton(JButton button, String iconName, int width) {
        button.setFont(DEFAULT_FONT);
        button.setForeground(TEXT_COLOR);
        button.setPreferredSize(new Dimension(width, 23));
        button.setMargin(new Insets(1, 4, 1, 4));
        setIconIfExists(button, iconName);
    }

    private void styleActionButton(JButton button, String iconName, int width) {
        button.setFont(DEFAULT_FONT);
        button.setForeground(TEXT_COLOR);
        button.setPreferredSize(new Dimension(width, 30));
        button.setMargin(new Insets(1, 4, 1, 4));
        setIconIfExists(button, iconName);
    }

    private void setIconIfExists(JButton button, String iconName) {
        java.net.URL iconUrl = getClass().getResource("/picture/" + iconName);
        if(iconUrl != null){
            button.setIcon(new ImageIcon(iconUrl));
        }
    }

    private void pasangSinkronWaktuRujukan() {
        if (Boolean.TRUE.equals(tanggalRujuk.getClientProperty("sync-waktu-rujukan"))) {
            return;
        }
        tanggalRujuk.putClientProperty("sync-waktu-rujukan", Boolean.TRUE);
        tanggalRujuk.addActionListener(e -> terapkanWaktuRujukanDariInput());
        FocusAdapter listener = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                terapkanWaktuRujukanDariInput();
            }
        };
        tanggalRujuk.addFocusListener(listener);
    }

    private void siapkanComboWaktuRujukan() {
        if (Boolean.TRUE.equals(panelJamRujuk.getClientProperty("combo-waktu-rujukan"))) {
            return;
        }
        panelJamRujuk.putClientProperty("combo-waktu-rujukan", Boolean.TRUE);
        panelJamRujuk.setBackground(PANEL_COLOR);
        jamRujuk.setModel(new javax.swing.DefaultComboBoxModel<String>(angkaDuaDigit(24)));
        menitRujuk.setModel(new javax.swing.DefaultComboBoxModel<String>(angkaDuaDigit(60)));
        detikRujuk.setModel(new javax.swing.DefaultComboBoxModel<String>(angkaDuaDigit(60)));
        widget.ComboBox[] combos = new widget.ComboBox[]{jamRujuk, menitRujuk, detikRujuk};
        for (widget.ComboBox combo : combos) {
            combo.setFont(DEFAULT_FONT);
            combo.setForeground(TEXT_COLOR);
            combo.setPreferredSize(new Dimension(42, 23));
            combo.addActionListener(e -> terapkanWaktuRujukanDariInput());
        }
        panelJamRujuk.add(jamRujuk);
        panelJamRujuk.add(new JLabel(":"));
        panelJamRujuk.add(menitRujuk);
        panelJamRujuk.add(new JLabel(":"));
        panelJamRujuk.add(detikRujuk);
    }

    private String[] angkaDuaDigit(int jumlah) {
        String[] values = new String[jumlah];
        for (int i = 0; i < jumlah; i++) {
            values[i] = String.format("%02d", i);
        }
        return values;
    }

    private void setWaktuRujukan(String waktu) {
        String value = nilai(waktu);
        if(value.equals("")){
            value = OffsetDateTime.now(ZoneId.systemDefault()).withNano(0).toString();
        }
        authoredOn.setText(value);
        occurrenceDateTime.setText(value);
        appointmentStart.setText(value);
        appointmentEnd.setText(tambahJam(value, 5));
        sinkronInputTanggalJam(value);
    }

    private void sinkronInputTanggalJam(String waktu) {
        String value = nilai(waktu);
        if(value.length() >= 19){
            try {
                tanggalRujuk.setDate(java.sql.Date.valueOf(value.substring(0, 10)));
            } catch (Exception e) {
                // datepicker tetap menggunakan tanggal terakhir bila format tidak valid
            }
            String jam = value.substring(11, 19);
            jamRujuk.setSelectedItem(jam.substring(0, 2));
            menitRujuk.setSelectedItem(jam.substring(3, 5));
            detikRujuk.setSelectedItem(jam.substring(6, 8));
        }
    }

    private void terapkanWaktuRujukanDariInput() {
        String tanggal = tanggalPickerIso();
        String jam = jamComboRujukan();
        if(tanggal.equals("")){
            tanggal = LocalDate.now().toString();
        }
        if(jam.equals("")){
            jam = LocalTime.now().withNano(0).toString();
            if(jam.length() == 5){
                jam = jam + ":00";
            }
        }
        setWaktuRujukan(tanggal + "T" + jam + "+08:00");
    }

    private String jamComboRujukan() {
        return nilai(String.valueOf(jamRujuk.getSelectedItem())) + ":"
                + nilai(String.valueOf(menitRujuk.getSelectedItem())) + ":"
                + nilai(String.valueOf(detikRujuk.getSelectedItem()));
    }

    private String tanggalPickerIso() {
        String value = tanggalRujuk.getSelectedItem() == null ? "" : tanggalRujuk.getSelectedItem().toString().trim();
        if(value.length() >= 10){
            String text = value.substring(0, 10);
            if(text.matches("\\d{4}-\\d{2}-\\d{2}")){
                return text;
            }
            if(text.matches("\\d{2}-\\d{2}-\\d{4}")){
                return text.substring(6, 10) + "-" + text.substring(3, 5) + "-" + text.substring(0, 2);
            }
        }
        return "";
    }

    private void isiDefault() {
        try {
            baseUrl.setText(koneksiDB.URLFHIRSATUSEHAT());
        } catch (Exception e) {
            baseUrl.setText("");
        }
        setWaktuRujukan(OffsetDateTime.now(ZoneId.systemDefault()).withNano(0).toString());
        diagnosisCode.setText("I61.9");
        diagnosisDisplay.setText("Intracerebral haemorrhage, unspecified");
        keterangan.setText("Rujukan untuk pemeriksaan dan penanganan lebih lanjut");
        supportingInfo.setText("Condition/{id_condition}\nObservation/{id_observation}\nDiagnosticReport/{id_diagnosticreport}");
        kodeProvinsiWilayah.setText("73");
        namaProvinsiWilayah.setText("Sulawesi Selatan");
        kodeKabupatenWilayah.setText("7371");
        namaKabupatenWilayah.setText("Kota Makassar");
        isiDefaultKriteriaRujukan();
        refreshCarePlanInfo();
    }

    public void setDataRujukan(
            String jenisRujukanPasien, String nomorRujukanPasien, String idPasien, String namaPasien,
            String idPraktisi, String namaPraktisi, String idOrganisasiPerujuk, String namaOrganisasiPerujuk,
            String idOrganisasiRujukan, String namaOrganisasiRujukan, String idEncounter,
            String kodeDiagnosa, String namaDiagnosa, String waktuRujukan, String keteranganRujukan,
            String dataPendukung) {
        setDataRujukan(jenisRujukanPasien, nomorRujukanPasien, "", idPasien, namaPasien, idPraktisi,
                namaPraktisi, idOrganisasiPerujuk, namaOrganisasiPerujuk, idOrganisasiRujukan,
                namaOrganisasiRujukan, idEncounter, kodeDiagnosa, namaDiagnosa, waktuRujukan,
                keteranganRujukan, dataPendukung);
    }

    public void setDataRujukan(
            String jenisRujukanPasien, String nomorRujukanPasien, String kodeFaskes,
            String idPasien, String namaPasien, String idPraktisi, String namaPraktisi,
            String idOrganisasiPerujuk, String namaOrganisasiPerujuk, String idOrganisasiRujukan,
            String namaOrganisasiRujukan, String idEncounter, String kodeDiagnosa, String namaDiagnosa,
            String waktuRujukan, String keteranganRujukan, String dataPendukung) {
        setDataRujukan(jenisRujukanPasien, nomorRujukanPasien, kodeFaskes, idPasien, namaPasien,
                idPraktisi, namaPraktisi, idOrganisasiPerujuk, namaOrganisasiPerujuk,
                idOrganisasiRujukan, namaOrganisasiRujukan, idEncounter, kodeDiagnosa,
                namaDiagnosa, waktuRujukan, keteranganRujukan, dataPendukung, "");
    }

    public void setDataRujukan(
            String jenisRujukanPasien, String nomorRujukanPasien, String kodeFaskes,
            String idPasien, String namaPasien, String idPraktisi, String namaPraktisi,
            String idOrganisasiPerujuk, String namaOrganisasiPerujuk, String idOrganisasiRujukan,
            String namaOrganisasiRujukan, String idEncounter, String kodeDiagnosa, String namaDiagnosa,
            String waktuRujukan, String keteranganRujukan, String dataPendukung, String idCarePlan) {
        if ("IGD".equalsIgnoreCase(jenisRujukanPasien)) {
            jenisRujukan.setSelectedItem("IGD");
        } else if ("Rawat Inap".equalsIgnoreCase(jenisRujukanPasien)) {
            jenisRujukan.setSelectedItem("Rawat Inap");
        } else {
            jenisRujukan.setSelectedItem("Rawat Jalan");
        }
        nomorRujukan.setText(nilai(nomorRujukanPasien));
        kodeFaskesSatuSehat.setText(nilai(kodeFaskes));
        patientId.setText(nilai(idPasien));
        patientName.setText(nilai(namaPasien));
        practitionerId.setText(nilai(idPraktisi));
        practitionerName.setText(nilai(namaPraktisi));
        practitionerRujukanId.setText(nilai(idPraktisi));
        practitionerRujukanName.setText(nilai(namaPraktisi));
        orgPerujukId.setText(nilai(idOrganisasiPerujuk));
        orgPerujukName.setText(nilai(namaOrganisasiPerujuk));
        orgRujukanId.setText(nilai(idOrganisasiRujukan));
        orgRujukanName.setText(nilai(namaOrganisasiRujukan));
        encounterId.setText(nilai(idEncounter));
        diagnosisCode.setText(nilai(kodeDiagnosa));
        diagnosisDisplay.setText(nilai(namaDiagnosa));
        setWaktuRujukan(nilai(waktuRujukan));
        keterangan.setText(nilai(keteranganRujukan));
        supportingInfo.setText(nilai(dataPendukung));
        hasilRadiologi.setText("");
        carePlanId = nilai(idCarePlan);
        isiDefaultKriteriaRujukan();
        muatRujukanTersimpan();
        ambilDataPendukungKlinis(false);
        refreshCarePlanInfo();
    }

    private DefaultTableModel createTabModeKriteriaRujukan(){
        return new DefaultTableModel(null,new Object[]{
                "No","Kode","Kriteria","Jawaban"
            }){
              @Override public boolean isCellEditable(int rowIndex,int colIndex){
                  return colIndex == 3;
              }
        };
    }

    private DefaultTableModel createTabModeKandidatFaskes(){
        return new DefaultTableModel(null,new Object[]{
                "Pilih","No","Org ID","Nama Faskes","Jarak","Estimasi","Strata","Kode BPJS","Kode Kemkes",
                "Task Rujukan","Status","Respon"
            }){
              @Override public boolean isCellEditable(int rowIndex,int colIndex){
                  return colIndex == 0;
              }
              @Override public Class getColumnClass(int columnIndex){
                  return columnIndex == 0 ? Boolean.class : Object.class;
              }
        };
    }

    private DefaultTableModel createTabModeDataRujukan(){
        return new DefaultTableModel(null,new Object[]{
                "No.Rawat","No.Rujukan","Tanggal","Pasien","Jenis","Faskes Tujuan","Task Rujukan",
                "ServiceRequest","Appointment","Status","Respon"
            }){
              @Override public boolean isCellEditable(int rowIndex,int colIndex){
                  return false;
              }
        };
    }

    private void setupKriteriaTable() {
        tbKriteriaRujukan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbKriteriaRujukan.setFont(DEFAULT_FONT);
        tbKriteriaRujukan.setRowHeight(23);
        tbKriteriaRujukan.setDefaultRenderer(Object.class, new WarnaTable());
        int[] widths = {45, 120, 520, 160};
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = tbKriteriaRujukan.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
        tbKriteriaRujukan.setPreferredScrollableViewportSize(new Dimension(720, 110));
        tbKriteriaRujukan.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_DELETE){
                    removeSelectedKriteriaRujukanRows();
                }
            }
        });
    }

    private void setupKandidatTable() {
        tbKandidatFaskes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbKandidatFaskes.setFont(DEFAULT_FONT);
        tbKandidatFaskes.setRowHeight(23);
        tbKandidatFaskes.setDefaultRenderer(Object.class, new WarnaTable());
        int[] widths = {45, 45, 230, 300, 90, 90, 120, 120, 120, 230, 90, 120};
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = tbKandidatFaskes.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
        tbKandidatFaskes.setPreferredScrollableViewportSize(new Dimension(720, 120));
        tbKandidatFaskes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    pakaiKandidatTerpilih();
                }
            }
        });
    }

    private void setupDataRujukanTable() {
        tbDataRujukan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbDataRujukan.setFont(DEFAULT_FONT);
        tbDataRujukan.setRowHeight(23);
        tbDataRujukan.setDefaultRenderer(Object.class, new WarnaTable());
        int[] widths = {105, 105, 85, 170, 90, 190, 220, 220, 220, 80, 80};
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = tbDataRujukan.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
        tbDataRujukan.setPreferredScrollableViewportSize(new Dimension(720, 420));
        tbDataRujukan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    pakaiDataRujukanTerpilih();
                }
            }
        });
    }

    private String nilai(String nilai) {
        return nilai == null ? "" : nilai.trim();
    }

    private String getTableCellValueAsString(JTable table, int row, int column){
        if(table == null || row < 0 || row >= table.getRowCount() || column < 0 || column >= table.getColumnCount()){
            return "";
        }
        Object value = table.getValueAt(row, column);
        return value == null ? "" : value.toString().trim();
    }

    private void removeSelectedKriteriaRujukanRows() {
        int selectedRow = tbKriteriaRujukan.getSelectedRow();
        if(selectedRow == -1){
            return;
        }
        if(selectedRow >= 0 && selectedRow < tabModeKriteriaRujukan.getRowCount()){
            tabModeKriteriaRujukan.removeRow(selectedRow);
            for(int i=0; i<tabModeKriteriaRujukan.getRowCount(); i++){
                tabModeKriteriaRujukan.setValueAt(i+1, i, 0);
            }
        }
    }

    private ArrayNode collectKriteriaRujukanItemsFromTable() {
        ArrayNode items = mapper.createArrayNode();
        for(int i=0;i<tbKriteriaRujukan.getRowCount();i++){
            String linkId = getTableCellValueAsString(tbKriteriaRujukan, i, 1);
            if(linkId.equals("")){
                continue;
            }
            String text = getTableCellValueAsString(tbKriteriaRujukan, i, 2);
            String answerText = getTableCellValueAsString(tbKriteriaRujukan, i, 3);

            ObjectNode item = mapper.createObjectNode();
            item.put("linkId", linkId);
            item.put("text", text);
            ArrayNode answerNode = mapper.createArrayNode();
            ObjectNode answer = mapper.createObjectNode();
            if(!answerText.equals("")){
                if("true".equalsIgnoreCase(answerText) || "false".equalsIgnoreCase(answerText)){
                    answer.put("valueBoolean", Boolean.parseBoolean(answerText));
                }else{
                    answer.put("valueString", answerText);
                }
                answerNode.add(answer);
                item.set("answer", answerNode);
            }
            items.add(item);
        }
        if("IGD".equals(String.valueOf(jenisRujukan.getSelectedItem()))){
            ArrayNode grouped = mapper.createArrayNode();
            ObjectNode group = mapper.createObjectNode();
            group.put("linkId", "0");
            group.put("text", "GAWAT DARURAT");
            group.set("item", items);
            grouped.add(group);
            return grouped;
        }
        return items;
    }

    private ArrayNode collectWilayahRujukanItems() {
        ArrayNode wilayah = mapper.createArrayNode();
        ObjectNode root = mapper.createObjectNode();
        root.put("linkId", "1");
        root.put("text", "Jejaring wilayah rujukan");
        ArrayNode child = mapper.createArrayNode();
        child.add(wilayahCodingItem("1.1", "Provinsi", kodeProvinsiWilayah.getText().trim(), namaProvinsiWilayah.getText().trim()));
        child.add(wilayahCodingItem("1.2", "Kabupaten/Kota", kodeKabupatenWilayah.getText().trim(), namaKabupatenWilayah.getText().trim()));
        root.set("item", child);
        wilayah.add(root);
        return wilayah;
    }

    private ObjectNode wilayahCodingItem(String linkId, String text, String code, String display) {
        ObjectNode item = mapper.createObjectNode();
        item.put("linkId", linkId);
        item.put("text", text);
        ObjectNode coding = mapper.createObjectNode();
        coding.put("system", "http://sys-ids.kemkes.go.id/administrative-area");
        coding.put("code", nilai(code));
        coding.put("display", nilai(display));
        ObjectNode answer = mapper.createObjectNode();
        answer.set("valueCoding", coding);
        item.set("answer", mapper.createArrayNode().add(answer));
        return item;
    }

    private void kirimPraPermintaan() {
        try {
            if(!taskPraPermintaanId.equals("")){
                logPraPermintaan("Task Pra Permintaan sudah ada. ID: "+taskPraPermintaanId);
                JOptionPane.showMessageDialog(this, "Task Pra Permintaan sudah terkirim.\nID: "+taskPraPermintaanId+"\nLanjutkan dengan Kirim Kandidat.");
                return;
            }
            if(tabModeKriteriaRujukan.getRowCount() == 0){
                isiDefaultKriteriaRujukan();
            }
            SatuSehatKirimRujukan.RujukanData data = collectData();
            logPraPermintaan("Mulai kirim Task Pra Permintaan");
            logPraPermintaan("No Rujukan: "+data.nomorRujukan);
            logPraPermintaan("Jenis Rujukan: "+data.jenisRujukan);
            logPraPermintaan("Patient: "+data.patientId+" / "+data.patientName);
            logPraPermintaan("Encounter: "+data.encounterId);
            logPraPermintaan("Organization Perujuk: "+data.orgPerujukId+" / "+data.orgPerujukName);
            logPraPermintaan("Diagnosis Utama: "+data.diagnosisUtamaCode+" / "+data.diagnosisUtamaDisplay);
            SatuSehatKirimRujukan.KirimResult result = rujukan.kirimPraPermintaan(data);
            logPraPermintaan("Task Pra Permintaan terkirim. ID: "+result.id);
            logPraPermintaan("Response status: "+result.root.path("status").asText());
            if(!nilai(result.id).equals("")){
                taskPraPermintaanId = result.id;
                tambahSupportingInfo("Task/"+result.id);
            }
            populateKriteriaRujukanDariTask(result.root);
            simpanRujukanDasar(collectData());
            simpanLogRujukan("PRA_PERMINTAAN", "POST", endpoint("/Task"), "", result.rawJson);
            logPraPermintaan("Jumlah kriteria di tabel: "+tabModeKriteriaRujukan.getRowCount());
        } catch (Exception e) {
            logPraPermintaan("Gagal kirim Task Pra Permintaan: "+e);
            JOptionPane.showMessageDialog(this, "Gagal mengirim Task Pra Permintaan: " + e.getMessage());
        }
    }

    private void kirimPencarianKandidat() {
        try {
            if(!taskPencarianKandidatId.equals("")){
                logPencarianKandidat("Task Pencarian Kandidat sudah ada. ID: "+taskPencarianKandidatId);
                if(tabModeKandidatFaskes.getRowCount() > 0){
                    JOptionPane.showMessageDialog(this, "Task Pencarian Kandidat sudah terkirim dan kandidat sudah ada di tabel.\nID: "+taskPencarianKandidatId);
                    return;
                }
                logPencarianKandidat("Tabel kandidat kosong, reset Task Pencarian Kandidat lama dan kirim ulang.");
                taskPencarianKandidatId = "";
            }
            if(tabModeKriteriaRujukan.getRowCount() == 0){
                isiDefaultKriteriaRujukan();
            }
            SatuSehatKirimRujukan.RujukanData data = collectData();
            data.taskPraPermintaanId = taskPraPermintaanId;
            ArrayNode kriteriaItems = collectKriteriaRujukanItemsFromTable();
            ArrayNode wilayahItems = collectWilayahRujukanItems();
            logPencarianKandidat("Mulai kirim Task Pencarian Kandidat");
            logPencarianKandidat("No Rujukan: "+data.nomorRujukan);
            logPencarianKandidat("Jenis Rujukan: "+data.jenisRujukan);
            logPencarianKandidat("Patient: "+data.patientId+" / "+data.patientName);
            logPencarianKandidat("Encounter: "+data.encounterId);
            logPencarianKandidat("Diagnosis Utama: "+data.diagnosisUtamaCode+" / "+data.diagnosisUtamaDisplay);
            logPencarianKandidat("Jumlah kriteria: "+kriteriaItems.size());
            logPencarianKandidat("Wilayah: "+kodeProvinsiWilayah.getText().trim()+" - "+kodeKabupatenWilayah.getText().trim());
            SatuSehatKirimRujukan.KirimResult result = rujukan.kirimPencarianKandidat(data, kriteriaItems, wilayahItems);
            if(!nilai(result.id).equals("")){
                taskPencarianKandidatId = result.id;
            }
            populateKandidatFaskesDariTask(result.root);
            simpanRujukanDasar(collectData());
            simpanKandidatFaskes();
            simpanLogRujukan("PENCARIAN_KANDIDAT", "POST", endpoint("/Task"), "", result.rawJson);
            logPencarianKandidat("Task Pencarian Kandidat terkirim. ID: "+result.id);
            logPencarianKandidat("Response status: "+result.root.path("status").asText());
            logPencarianKandidat("Jumlah kandidat di tabel: "+tabModeKandidatFaskes.getRowCount());
        } catch (Exception e) {
            logPencarianKandidat("Gagal kirim Task Pencarian Kandidat: "+e);
            if(e instanceof HttpStatusCodeException){
                logPencarianKandidat("Response body: "+((HttpStatusCodeException)e).getResponseBodyAsString());
            }
            JOptionPane.showMessageDialog(this, "Gagal mengirim Task Pencarian Kandidat: " + e.getMessage());
        }
    }

    private void ambilKandidatFaskes() {
        try {
            if(taskPencarianKandidatId.equals("")){
                JOptionPane.showMessageDialog(this, "Task Pencarian Kandidat belum ada. Kirim Kandidat terlebih dahulu.");
                return;
            }
            logPencarianKandidat("Ambil Task Pencarian Kandidat. ID: "+taskPencarianKandidatId);
            SatuSehatKirimRujukan.KirimResult result = rujukan.ambilTask(taskPencarianKandidatId);
            populateKandidatFaskesDariTask(result.root);
            simpanKandidatFaskes();
            simpanLogRujukan("AMBIL_KANDIDAT", "GET", endpoint("/Task/"+taskPencarianKandidatId), "", result.rawJson);
            logPencarianKandidat("Status Task Pencarian Kandidat: "+result.root.path("status").asText());
            logPencarianKandidat("Jumlah kandidat di tabel: "+tabModeKandidatFaskes.getRowCount());
        } catch (Exception e) {
            logPencarianKandidat("Gagal ambil Task Pencarian Kandidat: "+e);
            if(e instanceof HttpStatusCodeException){
                logPencarianKandidat("Response body: "+((HttpStatusCodeException)e).getResponseBodyAsString());
                if(((HttpStatusCodeException)e).getStatusCode().value() == 404){
                    taskPencarianKandidatId = "";
                    JOptionPane.showMessageDialog(this, "Task kandidat lama tidak ditemukan di SATUSEHAT staging.\nID kandidat sudah direset. Klik Kirim Kandidat untuk membuat ulang.");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Gagal mengambil kandidat faskes: " + e.getMessage());
        }
    }

    private void kirimTugasRujukan() {
        try {
            List<Integer> kandidatRows = kandidatDipilihUntukTaskRujukan();
            if(!kandidatRows.isEmpty() && orgRujukanId.getText().trim().equals("")){
                pakaiKandidatDariModelRow(kandidatRows.get(0));
            }
            if(orgRujukanId.getText().trim().equals("")){
                pakaiKandidatTerpilih();
            }
            if(orgRujukanId.getText().trim().equals("")){
                JOptionPane.showMessageDialog(this, "Centang atau pilih kandidat faskes terlebih dahulu.");
                return;
            }
            SatuSehatKirimRujukan.RujukanData data = collectData();
            data.taskPencarianKandidatId = taskPencarianKandidatId;
            if(data.carePlanId.equals("")){
                data.carePlanUuid = UUID.randomUUID().toString();
            }
            ObjectNode carePlanPayload = rujukan.buildCarePlan(data);
            tampilkanInformasiCarePlan(data, carePlanPayload, data.carePlanId);
            List<SatuSehatKirimRujukan.FaskesKandidat> kandidatList = kandidatTaskRujukanDariRows(kandidatRows);
            if(kandidatList.isEmpty()){
                JOptionPane.showMessageDialog(this, "Centang kandidat faskes yang akan dikirim tugas rujukan.");
                return;
            }

            logTugasRujukan("Mulai kirim Bundle Task Rujukan");
            logTugasRujukan("Jumlah faskes tujuan: "+kandidatList.size());
            logTugasRujukan("CarePlan dari tabel: "+(data.carePlanId.equals("") ? "-" : data.carePlanId));
            logTugasRujukan("CarePlan title: "+carePlanPayload.path("title").asText());
            logTugasRujukan("CarePlan category: "+codingSummary(carePlanPayload.path("category")));
            SatuSehatKirimRujukan.KirimResult result = rujukan.kirimBundleTugasRujukan(
                    data, kandidatList, collectKriteriaRujukanItemsFromTable(), collectWilayahRujukanItems(), false);
            List<String> taskIds = cariResourceIdsDariBundleResponse(result.root, "Task");
            tandaiTaskKandidatTerkirim(kandidatRows, kandidatList, taskIds);
            taskRujukanId = taskIds.isEmpty() ? "" : taskIds.get(0);
            String responseCarePlanId = cariResourceIdDariBundleResponse(result.root, "CarePlan");
            carePlanId = responseCarePlanId.equals("") ? data.carePlanId : responseCarePlanId;
            tampilkanInformasiCarePlan(data, carePlanPayload, carePlanId);
            if(!taskRujukanId.equals("")){
                tambahSupportingInfo("Task/"+taskRujukanId);
            }
            logTugasRujukan("Bundle Task Rujukan terkirim");
            logTugasRujukan("Task Rujukan ID: "+String.join(", ", taskIds));
            logTugasRujukan("CarePlan ID: "+carePlanId);
            statusTaskRujukan = "requested";
            responTaskRujukan = "";
            simpanKandidatFaskes();
            simpanRujukanDasar(collectData());
            simpanLogRujukan("TUGAS_RUJUKAN", "POST", endpoint(""), "", result.rawJson);
            JOptionPane.showMessageDialog(this, "Tugas rujukan terkirim ke "+kandidatList.size()+" faskes.\nTask utama: "+taskRujukanId+"\nCarePlan: "+carePlanId);
        } catch (Exception e) {
            logTugasRujukan("Gagal kirim tugas rujukan: "+e);
            if(e instanceof HttpStatusCodeException){
                logTugasRujukan("Response body: "+((HttpStatusCodeException)e).getResponseBodyAsString());
            }
            JOptionPane.showMessageDialog(this, "Gagal mengirim tugas rujukan: " + e.getMessage());
        }
    }

    private void kirimPermintaanRujukan() {
        try {
            if((taskRujukanId.equals("") || !"accepted".equalsIgnoreCase(responTaskRujukan)) && pakaiKandidatAcceptedPertama()){
                logPermintaanRujukan("Memakai kandidat faskes yang sudah accepted: "+orgRujukanId.getText().trim()+" / "+orgRujukanName.getText().trim());
            }
            if(orgRujukanId.getText().trim().equals("")){
                JOptionPane.showMessageDialog(this, "Faskes tujuan belum dipilih.");
                return;
            }
            if(!kandidatDenganTaskRujukan().isEmpty() && !"accepted".equalsIgnoreCase(responTaskRujukan)){
                JOptionPane.showMessageDialog(this, "Belum ada kandidat faskes yang menerima rujukan.\nKlik Cek Status sampai salah satu faskes accepted, lalu kirim Permintaan Rujukan.");
                return;
            }
            if(carePlanId.equals("")){
                JOptionPane.showMessageDialog(this, "CarePlan belum ada. Kirim Tugas Rujukan terlebih dahulu.");
                return;
            }
            SatuSehatKirimRujukan.RujukanData data = collectData();
            data.taskPencarianKandidatId = taskPencarianKandidatId;
            logPermintaanRujukan("Mulai kirim ServiceRequest Permintaan Rujukan");
            logPermintaanRujukan("Faskes tujuan: "+data.orgRujukanId+" / "+data.orgRujukanName);
            logPermintaanRujukan("CarePlan: "+carePlanId);
            logPermintaanRujukan("Task Rujukan: "+taskRujukanId);
            if(!resourceSatuSehatAda("CarePlan", carePlanId)){
                JOptionPane.showMessageDialog(this, "CarePlan tidak ditemukan di SATUSEHAT.\nKirim ulang Tugas Rujukan agar ID CarePlan valid.");
                return;
            }
            if(!resourceSatuSehatAda("Task", taskRujukanId)){
                JOptionPane.showMessageDialog(this, "Task Rujukan tidak ditemukan di SATUSEHAT.\nKirim ulang Tugas Rujukan agar ID Task valid.");
                return;
            }
            SatuSehatKirimRujukan.KirimResult result = rujukan.kirimServiceRequest(data, carePlanId, taskRujukanId);
            serviceRequestId = result.id;
            if(!serviceRequestId.equals("")){
                tambahSupportingInfo("ServiceRequest/"+serviceRequestId);
            }
            logPermintaanRujukan("ServiceRequest terkirim. ID: "+serviceRequestId);
            logPermintaanRujukan("Response status: "+result.root.path("status").asText());
            simpanRujukanDasar(collectData());
            simpanLogRujukan("PERMINTAAN_RUJUKAN", "POST", endpoint("/ServiceRequest"), "", result.rawJson);
            JOptionPane.showMessageDialog(this, "Permintaan rujukan terkirim.\nServiceRequest: "+serviceRequestId);
        } catch (Exception e) {
            logPermintaanRujukan("Gagal kirim ServiceRequest: "+e);
            if(e instanceof HttpStatusCodeException){
                logPermintaanRujukan("Response body: "+((HttpStatusCodeException)e).getResponseBodyAsString());
            }
            JOptionPane.showMessageDialog(this, "Gagal mengirim permintaan rujukan: " + e.getMessage());
        }
    }

    private boolean resourceSatuSehatAda(String resourceType, String id) {
        String value = nilai(id);
        if(value.equals("")){
            return false;
        }
        try {
            rujukan.ambilResource(resourceType + "/" + stripPrefix(value, resourceType + "/"));
            return true;
        } catch (Exception e) {
            logPermintaanRujukan(resourceType + " tidak ditemukan/validasi gagal: " + value + " - " + e.getMessage());
            return false;
        }
    }

    private void cekStatusRujukan() {
        try {
            List<Integer> rowsTaskKandidat = kandidatDenganTaskRujukan();
            if(!rowsTaskKandidat.isEmpty()){
                cekStatusKandidatRujukan(rowsTaskKandidat);
                return;
            }
            if(taskRujukanId.equals("")){
                JOptionPane.showMessageDialog(this, "Task rujukan belum ada. Kirim Tugas Rujukan terlebih dahulu.");
                return;
            }
            logTugasRujukan("Ambil Task Rujukan. ID: "+taskRujukanId);
            SatuSehatKirimRujukan.KirimResult result = rujukan.ambilTask(taskRujukanId);
            String status = result.root.path("status").asText();
            String response = responseTaskRujukan(result.root);
            logTugasRujukan("Status Task Rujukan: "+status);
            logTugasRujukan("Respon Faskes Rujukan: "+(response.equals("") ? "belum ada" : response));
            statusTaskRujukan = status;
            responTaskRujukan = response;
            simpanRujukanDasar(collectData());
            simpanLogRujukan("CEK_STATUS_RUJUKAN", "GET", endpoint("/Task/"+taskRujukanId), "", result.rawJson);
            JOptionPane.showMessageDialog(this,
                    "Status Task Rujukan: "+status+"\nRespon Faskes: "+displayResponseTask(response));
        } catch (Exception e) {
            logTugasRujukan("Gagal cek status Task Rujukan: "+e);
            if(e instanceof HttpStatusCodeException){
                logTugasRujukan("Response body: "+((HttpStatusCodeException)e).getResponseBodyAsString());
            }
            JOptionPane.showMessageDialog(this, "Gagal cek status Task Rujukan: " + e.getMessage());
        }
    }

    private void cekStatusKandidatRujukan(List<Integer> rowsTaskKandidat) throws Exception {
        int diterima = 0;
        int ditolak = 0;
        int belum = 0;
        StringBuilder ringkasan = new StringBuilder();
        for(Integer row : rowsTaskKandidat){
            if(row == null || row < 0 || row >= tabModeKandidatFaskes.getRowCount()){
                continue;
            }
            String taskId = getTableValue(tabModeKandidatFaskes, row, KANDIDAT_TASK);
            if(taskId.equals("")){
                continue;
            }
            logTugasRujukan("Ambil Task kandidat. ID: "+taskId);
            SatuSehatKirimRujukan.KirimResult result = rujukan.ambilTask(taskId);
            String status = result.root.path("status").asText();
            String response = responseTaskRujukan(result.root);
            tabModeKandidatFaskes.setValueAt(status, row, KANDIDAT_STATUS);
            tabModeKandidatFaskes.setValueAt(response, row, KANDIDAT_RESPON);
            simpanLogRujukan("CEK_STATUS_RUJUKAN", "GET", endpoint("/Task/"+taskId), "", result.rawJson);
            if("accepted".equalsIgnoreCase(response)){
                diterima++;
                taskRujukanId = taskId;
                statusTaskRujukan = status;
                responTaskRujukan = response;
                pakaiKandidatDariModelRow(row);
            }else if("rejected".equalsIgnoreCase(response)){
                ditolak++;
            }else{
                belum++;
            }
            ringkasan.append(getTableValue(tabModeKandidatFaskes, row, KANDIDAT_NAMA))
                    .append(" : ")
                    .append(status.equals("") ? "-" : status)
                    .append(" / ")
                    .append(displayResponseTask(response))
                    .append("\n");
        }
        simpanKandidatFaskes();
        simpanRujukanDasar(collectData());
        JOptionPane.showMessageDialog(this,
                "Status kandidat faskes diperbarui.\nDiterima: "+diterima+"\nDitolak: "+ditolak+"\nBelum respon: "+belum+"\n\n"+ringkasan.toString());
    }

    private void kirimResponTaskRujukan(boolean diterima) {
        try {
            if(taskRujukanId.equals("")){
                JOptionPane.showMessageDialog(this, "Task rujukan belum ada. Kirim Tugas Rujukan terlebih dahulu.");
                return;
            }
            String response = diterima ? "accepted" : "rejected";
            int konfirmasi = JOptionPane.showConfirmDialog(this,
                    "Kirim respon "+response+" untuk Task Rujukan?\nAksi ini mengikuti langkah Faskes Rujukan pada collection staging.",
                    "Konfirmasi Respon Task Rujukan", JOptionPane.YES_NO_OPTION);
            if(konfirmasi != JOptionPane.YES_OPTION){
                return;
            }
            logTugasRujukan("Kirim respon Task Rujukan: "+response+". ID: "+taskRujukanId);
            SatuSehatKirimRujukan.KirimResult result = rujukan.kirimResponTaskPatch(taskRujukanId, diterima);
            if("OperationOutcome".equals(result.root.path("resourceType").asText())){
                logTugasRujukan("Gagal respon Task Rujukan: "+result.rawJson);
                JOptionPane.showMessageDialog(this, "SATUSEHAT mengembalikan OperationOutcome.\nCek log untuk detail response.");
                return;
            }
            String status = result.root.path("status").asText();
            String responseResult = responseTaskRujukan(result.root);
            logTugasRujukan("Respon Task Rujukan terkirim");
            logTugasRujukan("Status Task Rujukan: "+status);
            logTugasRujukan("Respon Faskes Rujukan: "+(responseResult.equals("") ? response : responseResult));
            statusTaskRujukan = status;
            responTaskRujukan = responseResult.equals("") ? response : responseResult;
            simpanRujukanDasar(collectData());
            simpanResponTaskRujukan(statusTaskRujukan, responTaskRujukan);
            simpanLogRujukan("RESPON_TASK_RUJUKAN", "PATCH", endpoint("/Task/"+taskRujukanId), "", result.rawJson);
            JOptionPane.showMessageDialog(this,
                    "Respon Task Rujukan terkirim.\nStatus: "+status+"\nRespon: "+displayResponseTask(responseResult.equals("") ? response : responseResult));
        } catch (Exception e) {
            logTugasRujukan("Gagal kirim respon Task Rujukan: "+e);
            if(e instanceof HttpStatusCodeException){
                logTugasRujukan("Response body: "+((HttpStatusCodeException)e).getResponseBodyAsString());
            }
            JOptionPane.showMessageDialog(this, "Gagal mengirim respon Task Rujukan: " + e.getMessage());
        }
    }

    private String responseTaskRujukan(JsonNode task) {
        for(JsonNode output : task.path("output")){
            if(hasCoding(output.path("type"), "response-referral-task")){
                String code = output.path("valueCoding").path("code").asText();
                if(!code.equals("")){
                    return code;
                }
                for(JsonNode coding : output.path("valueCodeableConcept").path("coding")){
                    code = coding.path("code").asText();
                    if(!code.equals("")){
                        return code;
                    }
                }
            }
        }
        return "";
    }

    private String displayResponseTask(String response) {
        if("accepted".equals(response)){
            return "Diterima";
        }
        if("rejected".equals(response)){
            return "Ditolak";
        }
        return "Belum ada";
    }

    private void tampilkanDialogAppointment() {
        if(taskRujukanId.equals("")){
            JOptionPane.showMessageDialog(this, "Task rujukan belum ada. Kirim Tugas Rujukan terlebih dahulu.");
            return;
        }
        if(appointmentEnd.getText().trim().equals("")){
            appointmentEnd.setText(tambahJam(appointmentStart.getText().trim(), 5));
        }

        JDialog dialog = new JDialog(this, "Kirim Appointment", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(6, 6));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField noRujukanField = readonlyField(nomorRujukan.getText().trim());
        JTextField pasienField = readonlyField(patientName.getText().trim());
        JTextField taskField = readonlyField(taskRujukanId);
        JTextField serviceRequestField = readonlyField(serviceRequestId);
        JTextField praktisiId = new JTextField(practitionerRujukanId.getText().trim());
        JTextField praktisiNama = new JTextField(practitionerRujukanName.getText().trim());
        JTextField tanggalMulai = new JTextField();
        JTextField tanggalSelesai = new JTextField();
        JComboBox<String> jamMulai = new JComboBox<String>(angkaDuaDigit(24));
        JComboBox<String> menitMulai = new JComboBox<String>(angkaDuaDigit(60));
        JComboBox<String> detikMulai = new JComboBox<String>(angkaDuaDigit(60));
        JComboBox<String> jamSelesai = new JComboBox<String>(angkaDuaDigit(24));
        JComboBox<String> menitSelesai = new JComboBox<String>(angkaDuaDigit(60));
        JComboBox<String> detikSelesai = new JComboBox<String>(angkaDuaDigit(60));

        setDateTimeControls(appointmentStart.getText().trim(), tanggalMulai, jamMulai, menitMulai, detikMulai);
        setDateTimeControls(appointmentEnd.getText().trim(), tanggalSelesai, jamSelesai, menitSelesai, detikSelesai);

        int row = 0;
        row = addAppointmentDialogRow(panel, c, row, "No. Rujukan", noRujukanField, "Pasien", pasienField);
        row = addAppointmentDialogRow(panel, c, row, "Task", taskField, "ServiceRequest", serviceRequestField);
        row = addAppointmentDialogRow(panel, c, row, "Practitioner ID", praktisiId, "Nama Praktisi", praktisiNama);
        row = addAppointmentDialogRow(panel, c, row, "Tanggal Mulai", tanggalMulai, "Jam Mulai", timePanel(jamMulai, menitMulai, detikMulai));
        row = addAppointmentDialogRow(panel, c, row, "Tanggal Selesai", tanggalSelesai, "Jam Selesai", timePanel(jamSelesai, menitSelesai, detikSelesai));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 8));
        actions.setBackground(PANEL_COLOR);
        JButton btnKirim = new JButton("Kirim");
        JButton btnBatal = new JButton("Batal");
        styleButton(btnKirim, "save-16x16.png", 90);
        styleButton(btnBatal, "Cancel-2-16x16.png", 90);
        btnKirim.addActionListener(e -> {
            if(praktisiId.getText().trim().equals("")){
                JOptionPane.showMessageDialog(dialog, "Practitioner ID belum diisi.");
                return;
            }
            String mulai = dateTimeFromControls(tanggalMulai, jamMulai, menitMulai, detikMulai);
            String selesai = dateTimeFromControls(tanggalSelesai, jamSelesai, menitSelesai, detikSelesai);
            if(mulai.equals("") || selesai.equals("")){
                JOptionPane.showMessageDialog(dialog, "Tanggal appointment belum valid. Gunakan format yyyy-MM-dd.");
                return;
            }
            practitionerRujukanId.setText(praktisiId.getText().trim());
            practitionerRujukanName.setText(praktisiNama.getText().trim());
            appointmentStart.setText(mulai);
            appointmentEnd.setText(selesai);
            dialog.dispose();
            kirimAppointment();
        });
        btnBatal.addActionListener(e -> dialog.dispose());
        actions.add(btnKirim);
        actions.add(btnBatal);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.PAGE_END);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JTextField readonlyField(String value) {
        JTextField field = new JTextField(nilai(value));
        field.setEditable(false);
        field.setFont(DEFAULT_FONT);
        field.setForeground(TEXT_COLOR);
        field.setPreferredSize(new Dimension(180, 23));
        return field;
    }

    private int addAppointmentDialogRow(JPanel panel, GridBagConstraints c, int row, String label1, JComponent field1,
            String label2, JComponent field2) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(label(label1), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field1, c);
        c.gridx = 2;
        c.weightx = 0;
        panel.add(label(label2), c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(field2, c);
        return row + 1;
    }

    private JPanel timePanel(JComboBox<String> jam, JComboBox<String> menit, JComboBox<String> detik) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panel.setBackground(PANEL_COLOR);
        JComboBox[] combos = new JComboBox[]{jam, menit, detik};
        for(JComboBox combo : combos){
            combo.setFont(DEFAULT_FONT);
            combo.setForeground(TEXT_COLOR);
            combo.setPreferredSize(new Dimension(48, 23));
        }
        panel.add(jam);
        panel.add(new JLabel(":"));
        panel.add(menit);
        panel.add(new JLabel(":"));
        panel.add(detik);
        return panel;
    }

    private void setDateTimeControls(String value, JTextField tanggal, JComboBox<String> jam,
            JComboBox<String> menit, JComboBox<String> detik) {
        String text = nilai(value);
        if(text.length() < 19){
            text = OffsetDateTime.now(ZoneId.systemDefault()).withNano(0).toString();
        }
        tanggal.setFont(DEFAULT_FONT);
        tanggal.setForeground(TEXT_COLOR);
        tanggal.setPreferredSize(new Dimension(120, 23));
        tanggal.setText(text.substring(0, 10));
        jam.setSelectedItem(text.substring(11, 13));
        menit.setSelectedItem(text.substring(14, 16));
        detik.setSelectedItem(text.substring(17, 19));
    }

    private String dateTimeFromControls(JTextField tanggal, JComboBox<String> jam,
            JComboBox<String> menit, JComboBox<String> detik) {
        String date = tanggal.getText().trim();
        if(!date.matches("\\d{4}-\\d{2}-\\d{2}")){
            return "";
        }
        return date + "T" + jam.getSelectedItem() + ":" + menit.getSelectedItem() + ":"
                + detik.getSelectedItem() + OffsetDateTime.now(ZoneId.systemDefault()).getOffset().toString();
    }

    private void kirimAppointment() {
        try {
            if(taskRujukanId.equals("")){
                JOptionPane.showMessageDialog(this, "Task rujukan belum ada. Kirim Tugas Rujukan terlebih dahulu.");
                return;
            }
            if(practitionerRujukanId.getText().trim().equals("")){
                JOptionPane.showMessageDialog(this, "Praktisi tujuan belum diisi. Isi Practitioner ID faskes rujukan terlebih dahulu.");
                return;
            }
            String mulai = appointmentStart.getText().trim();
            String selesai = appointmentEnd.getText().trim();
            if(mulai.equals("")){
                JOptionPane.showMessageDialog(this, "Appointment Mulai belum diisi.");
                return;
            }
            if(selesai.equals("")){
                selesai = tambahJam(mulai, 5);
                appointmentEnd.setText(selesai);
            }
            SatuSehatKirimRujukan.KirimResult taskResult = rujukan.ambilTask(taskRujukanId);
            String response = responseTaskRujukan(taskResult.root);
            if(!"accepted".equals(response)){
                JOptionPane.showMessageDialog(this, "Task rujukan belum accepted. Tunggu faskes tujuan menerima rujukan dulu.");
                return;
            }
            SatuSehatKirimRujukan.RujukanData data = collectData();
            logTugasRujukan("Mulai kirim Appointment");
            logTugasRujukan("Appointment: "+mulai+" - "+selesai);
            logTugasRujukan("Praktisi tujuan: "+data.practitionerRujukanId+" / "+data.practitionerRujukanName);
            SatuSehatKirimRujukan.KirimResult result = rujukan.kirimAppointment(data, taskRujukanId, mulai, selesai);
            appointmentId = result.id;
            if(appointmentId.equals("")){
                appointmentId = cariResourceIdDariBundleResponse(result.root, "Appointment");
            }
            logTugasRujukan("Appointment terkirim. ID: "+appointmentId);
            logTugasRujukan("Response status: "+result.root.path("status").asText());
            simpanRujukanDasar(collectData());
            simpanAppointmentRujukan(mulai, selesai);
            simpanLogRujukan("APPOINTMENT", "POST", endpoint(""), "", result.rawJson);
            JOptionPane.showMessageDialog(this, "Appointment terkirim.\nAppointment: "+appointmentId);
        } catch (Exception e) {
            logTugasRujukan("Gagal kirim Appointment: "+e);
            if(e instanceof HttpStatusCodeException){
                logTugasRujukan("Response body: "+((HttpStatusCodeException)e).getResponseBodyAsString());
            }
            JOptionPane.showMessageDialog(this, "Gagal mengirim Appointment: " + e.getMessage());
        }
    }

    private String cariResourceIdDariBundleResponse(JsonNode root, String resourceType) {
        List<String> ids = cariResourceIdsDariBundleResponse(root, resourceType);
        return ids.isEmpty() ? "" : ids.get(0);
    }

    private List<String> cariResourceIdsDariBundleResponse(JsonNode root, String resourceType) {
        List<String> ids = new ArrayList<String>();
        for(JsonNode entry : root.path("entry")){
            JsonNode response = entry.path("response");
            String idDariLocation = resourceIdDariLocation(response.path("location").asText(), resourceType);
            if(!idDariLocation.equals("")){
                ids.add(idDariLocation);
                continue;
            }
            String resourceId = response.path("resourceID").asText();
            if(resourceType.equals(response.path("resourceType").asText()) && !resourceId.equals("")){
                ids.add(stripPrefix(resourceId, resourceType + "/"));
            }
        }
        return ids;
    }

    private String resourceIdDariLocation(String location, String resourceType) {
        String value = nilai(location);
        String prefix = resourceType + "/";
        int start = -1;
        if(value.startsWith(prefix)){
            start = prefix.length();
        }else{
            String marker = "/" + prefix;
            int markerIndex = value.indexOf(marker);
            if(markerIndex >= 0){
                start = markerIndex + marker.length();
            }
        }
        if(start < 0 || start >= value.length()){
            return "";
        }
        String remaining = value.substring(start);
        int end = remaining.indexOf("/");
        return end >= 0 ? remaining.substring(0, end) : remaining;
    }

    private void simpanRujukanDasar(SatuSehatKirimRujukan.RujukanData data) {
        String noRawat = noRawatRujukan();
        String noRujukan = noRujukanRujukan();
        if(noRawat.equals("") || noRujukan.equals("")){
            return;
        }
        String sql = "insert into satu_sehat_rujukan (no_rawat,no_rujukan,tgl_perawatan,jam_rawat,status_rawat,jenis_rujukan,"
                + "id_patient,nama_patient,id_practitioner,nama_practitioner,id_encounter,id_org_perujuk,nama_org_perujuk,"
                + "id_org_tujuan,nama_org_tujuan,kode_diagnosa,nama_diagnosa,keterangan,id_task_pra_permintaan,"
                + "id_task_pencarian_kandidat,id_task_rujukan,id_careplan,id_servicerequest,id_appointment,status_task_rujukan,respon_rujukan) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "on duplicate key update tgl_perawatan=values(tgl_perawatan),jam_rawat=values(jam_rawat),"
                + "status_rawat=values(status_rawat),jenis_rujukan=values(jenis_rujukan),id_patient=values(id_patient),"
                + "nama_patient=values(nama_patient),id_practitioner=values(id_practitioner),nama_practitioner=values(nama_practitioner),"
                + "id_encounter=values(id_encounter),id_org_perujuk=values(id_org_perujuk),nama_org_perujuk=values(nama_org_perujuk),"
                + "id_org_tujuan=values(id_org_tujuan),nama_org_tujuan=values(nama_org_tujuan),kode_diagnosa=values(kode_diagnosa),"
                + "nama_diagnosa=values(nama_diagnosa),keterangan=values(keterangan),id_task_pra_permintaan=values(id_task_pra_permintaan),"
                + "id_task_pencarian_kandidat=values(id_task_pencarian_kandidat),id_task_rujukan=values(id_task_rujukan),"
                + "id_careplan=values(id_careplan),id_servicerequest=values(id_servicerequest),id_appointment=values(id_appointment),"
                + "status_task_rujukan=values(status_task_rujukan),respon_rujukan=values(respon_rujukan)";
        Sequel.queryu2(sql, 26, new String[]{
            noRawat,
            noRujukan,
            tanggalDariWaktu(data.authoredOn),
            jamDariWaktu(data.authoredOn),
            "Ralan",
            jenisRujukanDb(),
            data.patientId,
            data.patientName,
            data.practitionerId,
            data.practitionerName,
            data.encounterId,
            data.orgPerujukId,
            data.orgPerujukName,
            data.orgRujukanId,
            data.orgRujukanName,
            data.diagnosisUtamaCode,
            data.diagnosisUtamaDisplay,
            data.keterangan,
            taskPraPermintaanId,
            taskPencarianKandidatId,
            taskRujukanId,
            carePlanId,
            serviceRequestId,
            appointmentId,
            statusTaskRujukan,
            responTaskRujukan
        });
        muatDataRujukanTersimpan();
    }

    private void simpanKandidatFaskes() {
        String noRawat = noRawatRujukan();
        String noRujukan = noRujukanRujukan();
        if(noRawat.equals("") || noRujukan.equals("")){
            return;
        }
        pastikanKolomKandidatRujukan();
        Sequel.queryu2("delete from satu_sehat_rujukan_kandidat where no_rawat=? and no_rujukan=?", 2,
                new String[]{noRawat, noRujukan});
        String sql = "insert into satu_sehat_rujukan_kandidat "
                + "(no_rawat,no_rujukan,urut,id_organization,nama_organization,jarak_km,estimasi_menit,strata,kode_bpjs,kode_kemkes,"
                + "dipilih,id_task_rujukan,status_task_rujukan,respon_rujukan) "
                + "values (?,?,?,?,?,nullif(?,''),nullif(?,''),?,?,?,?,?,?,?) "
                + "on duplicate key update urut=values(urut),nama_organization=values(nama_organization),"
                + "jarak_km=values(jarak_km),estimasi_menit=values(estimasi_menit),strata=values(strata),"
                + "kode_bpjs=values(kode_bpjs),kode_kemkes=values(kode_kemkes),dipilih=values(dipilih),"
                + "id_task_rujukan=values(id_task_rujukan),status_task_rujukan=values(status_task_rujukan),respon_rujukan=values(respon_rujukan)";
        for(int i = 0; i < tabModeKandidatFaskes.getRowCount(); i++){
            Sequel.queryu2(sql, 14, new String[]{
                noRawat,
                noRujukan,
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_NO),
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_ORG_ID),
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_NAMA),
                angkaDariTeks(getTableValue(tabModeKandidatFaskes, i, KANDIDAT_JARAK)),
                angkaDariTeks(getTableValue(tabModeKandidatFaskes, i, KANDIDAT_ESTIMASI)),
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_STRATA),
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_BPJS),
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_KEMKES),
                Boolean.TRUE.equals(tabModeKandidatFaskes.getValueAt(i, KANDIDAT_PILIH)) ? "true" : "false",
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_TASK),
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_STATUS),
                getTableValue(tabModeKandidatFaskes, i, KANDIDAT_RESPON)
            });
        }
    }

    private void pastikanKolomKandidatRujukan() {
        tambahKolomJikaBelumAda("satu_sehat_rujukan_kandidat", "dipilih", "varchar(5) default 'false'");
        tambahKolomJikaBelumAda("satu_sehat_rujukan_kandidat", "id_task_rujukan", "varchar(64) default ''");
        tambahKolomJikaBelumAda("satu_sehat_rujukan_kandidat", "status_task_rujukan", "varchar(20) default ''");
        tambahKolomJikaBelumAda("satu_sehat_rujukan_kandidat", "respon_rujukan", "varchar(20) default ''");
    }

    private void tambahKolomJikaBelumAda(String tabel, String kolom, String definisi) {
        java.sql.Statement st = null;
        try {
            st = koneksi.createStatement();
            st.executeUpdate("alter table " + tabel + " add column " + kolom + " " + definisi);
        } catch (Exception e) {
            String pesan = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if(!pesan.contains("duplicate") && !pesan.contains("exists") && !pesan.contains("duplikat")){
                System.out.println("INFO SATUSEHAT RUJUKAN : Gagal memastikan kolom "+tabel+"."+kolom+" - "+e);
            }
        } finally {
            try {
                if(st != null){
                    st.close();
                }
            } catch (Exception e) {
                System.out.println("INFO SATUSEHAT RUJUKAN : Gagal menutup statement alter kolom - "+e);
            }
        }
    }

    private void simpanResponTaskRujukan(String status, String respon) {
        String noRawat = noRawatRujukan();
        String noRujukan = noRujukanRujukan();
        if(noRawat.equals("") || noRujukan.equals("")){
            return;
        }
        Sequel.queryu2("update satu_sehat_rujukan set status_task_rujukan=?,respon_rujukan=?,waktu_respon=now() "
                        + "where no_rawat=? and no_rujukan=?",
                4, new String[]{nilai(status), nilai(respon), noRawat, noRujukan});
        muatDataRujukanTersimpan();
    }

    private void simpanAppointmentRujukan(String mulai, String selesai) {
        String noRawat = noRawatRujukan();
        String noRujukan = noRujukanRujukan();
        if(noRawat.equals("") || noRujukan.equals("")){
            return;
        }
        Sequel.queryu2("update satu_sehat_rujukan set id_appointment=?,appointment_mulai=nullif(?,''),appointment_selesai=nullif(?,'') "
                        + "where no_rawat=? and no_rujukan=?",
                5, new String[]{appointmentId, mysqlDateTime(mulai), mysqlDateTime(selesai), noRawat, noRujukan});
        muatDataRujukanTersimpan();
    }

    private void simpanLogRujukan(String tahap, String method, String url, String requestJson, String responseJson) {
        String noRawat = noRawatRujukan();
        String noRujukan = noRujukanRujukan();
        if(noRawat.equals("") || noRujukan.equals("")){
            return;
        }
        Sequel.queryu2("insert into satu_sehat_rujukan_log "
                        + "(no_rawat,no_rujukan,tahap,method,url,request_json,response_json) values (?,?,?,?,?,?,?)",
                7, new String[]{noRawat, noRujukan, nilai(tahap), nilai(method), nilai(url), nilai(requestJson), nilai(responseJson)});
    }

    private void muatDataRujukanTersimpan() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select no_rawat,no_rujukan,tgl_perawatan,nama_patient,jenis_rujukan,"
                    + "nama_org_tujuan,id_task_rujukan,id_servicerequest,id_appointment,status_task_rujukan,respon_rujukan "
                    + "from satu_sehat_rujukan order by tgl_perawatan desc,jam_rawat desc,no_rujukan desc limit 200");
            rs = ps.executeQuery();
            tabModeDataRujukan.setRowCount(0);
            while(rs.next()){
                tabModeDataRujukan.addRow(new Object[]{
                    rs.getString("no_rawat"),
                    rs.getString("no_rujukan"),
                    rs.getString("tgl_perawatan"),
                    rs.getString("nama_patient"),
                    rs.getString("jenis_rujukan"),
                    rs.getString("nama_org_tujuan"),
                    rs.getString("id_task_rujukan"),
                    rs.getString("id_servicerequest"),
                    rs.getString("id_appointment"),
                    rs.getString("status_task_rujukan"),
                    rs.getString("respon_rujukan")
                });
            }
        } catch (Exception e) {
            System.out.println("INFO SATUSEHAT RUJUKAN : Gagal memuat tabel data rujukan: " + e);
        } finally {
            try {
                if(rs != null){
                    rs.close();
                }
                if(ps != null){
                    ps.close();
                }
            } catch (Exception e) {
                System.out.println("INFO SATUSEHAT RUJUKAN : Gagal menutup query tabel data rujukan: " + e);
            }
        }
    }

    private void pakaiDataRujukanTerpilih() {
        int row = tbDataRujukan.getSelectedRow();
        if(row == -1){
            JOptionPane.showMessageDialog(this, "Pilih data rujukan terlebih dahulu.");
            return;
        }
        String noRawat = getTableCellValueAsString(tbDataRujukan, row, 0);
        String noRujukan = getTableCellValueAsString(tbDataRujukan, row, 1);
        muatRujukanTersimpan(noRawat, noRujukan);
    }

    private void muatRujukanTersimpan() {
        muatRujukanTersimpan(noRawatRujukan(), "");
    }

    private void muatRujukanTersimpan(String noRawat, String noRujukan) {
        String rawat = nilai(noRawat);
        String rujukan = nilai(noRujukan);
        if(rawat.equals("")){
            return;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if(rujukan.equals("")){
                ps = koneksi.prepareStatement("select * from satu_sehat_rujukan where no_rawat=? order by no_rujukan desc limit 1");
                ps.setString(1, rawat);
            }else{
                ps = koneksi.prepareStatement("select * from satu_sehat_rujukan where no_rawat=? and no_rujukan=? limit 1");
                ps.setString(1, rawat);
                ps.setString(2, rujukan);
            }
            rs = ps.executeQuery();
            if(rs.next()){
                terapkanRujukanTersimpan(rs);
            }
        } catch (Exception e) {
            System.out.println("INFO SATUSEHAT RUJUKAN : Gagal memuat data tersimpan: " + e);
        } finally {
            try {
                if(rs != null){
                    rs.close();
                }
                if(ps != null){
                    ps.close();
                }
            } catch (Exception e) {
                System.out.println("INFO SATUSEHAT RUJUKAN : Gagal menutup query data tersimpan: " + e);
            }
        }
    }

    private void terapkanRujukanTersimpan(ResultSet rs) throws Exception {
        setIfNotEmpty(nomorRujukan, rs.getString("no_rujukan"));
        setIfNotEmpty(patientId, rs.getString("id_patient"));
        setIfNotEmpty(patientName, rs.getString("nama_patient"));
        setIfNotEmpty(practitionerId, rs.getString("id_practitioner"));
        setIfNotEmpty(practitionerName, rs.getString("nama_practitioner"));
        setIfNotEmpty(encounterId, rs.getString("id_encounter"));
        setIfNotEmpty(orgPerujukId, rs.getString("id_org_perujuk"));
        setIfNotEmpty(orgPerujukName, rs.getString("nama_org_perujuk"));
        setIfNotEmpty(orgRujukanId, rs.getString("id_org_tujuan"));
        setIfNotEmpty(orgRujukanName, rs.getString("nama_org_tujuan"));
        setIfNotEmpty(diagnosisCode, rs.getString("kode_diagnosa"));
        setIfNotEmpty(diagnosisDisplay, rs.getString("nama_diagnosa"));
        setJenisRujukanFromDb(rs.getString("jenis_rujukan"));
        String keteranganDb = nilai(rs.getString("keterangan"));
        if(!keteranganDb.equals("")){
            keterangan.setText(keteranganDb);
        }
        String waktuRujukanDb = fhirDateTime(nilai(rs.getString("tgl_perawatan")) + " " + nilai(rs.getString("jam_rawat")));
        if(!waktuRujukanDb.equals("")){
            setWaktuRujukan(waktuRujukanDb);
        }
        taskPraPermintaanId = nilai(rs.getString("id_task_pra_permintaan"));
        taskPencarianKandidatId = nilai(rs.getString("id_task_pencarian_kandidat"));
        taskRujukanId = nilai(rs.getString("id_task_rujukan"));
        carePlanId = nilai(rs.getString("id_careplan"));
        serviceRequestId = nilai(rs.getString("id_servicerequest"));
        appointmentId = nilai(rs.getString("id_appointment"));
        statusTaskRujukan = nilai(rs.getString("status_task_rujukan"));
        responTaskRujukan = nilai(rs.getString("respon_rujukan"));
        String appointmentMulai = fhirDateTime(rs.getString("appointment_mulai"));
        String appointmentSelesai = fhirDateTime(rs.getString("appointment_selesai"));
        if(!appointmentMulai.equals("")){
            appointmentStart.setText(appointmentMulai);
        }
        if(!appointmentSelesai.equals("")){
            appointmentEnd.setText(appointmentSelesai);
        }
        tambahSupportingInfoIfId("Encounter", encounterId.getText().trim());
        tambahSupportingInfoIfId("Task", taskPraPermintaanId);
        tambahSupportingInfoIfId("Task", taskPencarianKandidatId);
        tambahSupportingInfoIfId("Task", taskRujukanId);
        tambahSupportingInfoIfId("ServiceRequest", serviceRequestId);
        refreshCarePlanInfo();
        muatKandidatFaskesTersimpan(nilai(rs.getString("no_rawat")), nilai(rs.getString("no_rujukan")));
        logTugasRujukan("Data rujukan tersimpan dimuat dari database. Task: "+taskRujukanId+", ServiceRequest: "+serviceRequestId+", Appointment: "+appointmentId);
    }

    private void muatKandidatFaskesTersimpan(String noRawat, String noRujukan) {
        if(nilai(noRawat).equals("") || nilai(noRujukan).equals("")){
            return;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            pastikanKolomKandidatRujukan();
            ps = koneksi.prepareStatement("select urut,id_organization,nama_organization,jarak_km,estimasi_menit,strata,kode_bpjs,kode_kemkes,"
                    + "dipilih,id_task_rujukan,status_task_rujukan,respon_rujukan "
                    + "from satu_sehat_rujukan_kandidat where no_rawat=? and no_rujukan=? order by urut");
            ps.setString(1, noRawat);
            ps.setString(2, noRujukan);
            rs = ps.executeQuery();
            tabModeKandidatFaskes.setRowCount(0);
            while(rs.next()){
                tabModeKandidatFaskes.addRow(new Object[]{
                    "true".equalsIgnoreCase(nilai(rs.getString("dipilih"))),
                    rs.getString("urut"),
                    rs.getString("id_organization"),
                    rs.getString("nama_organization"),
                    formatSatuan(rs.getString("jarak_km"), "km"),
                    formatSatuan(rs.getString("estimasi_menit"), "minute"),
                    rs.getString("strata"),
                    rs.getString("kode_bpjs"),
                    rs.getString("kode_kemkes"),
                    rs.getString("id_task_rujukan"),
                    rs.getString("status_task_rujukan"),
                    rs.getString("respon_rujukan")
                });
            }
            urutkanKandidatFaskesTerdekat();
        } catch (Exception e) {
            System.out.println("INFO SATUSEHAT RUJUKAN : Gagal memuat kandidat tersimpan: " + e);
        } finally {
            try {
                if(rs != null){
                    rs.close();
                }
                if(ps != null){
                    ps.close();
                }
            } catch (Exception e) {
                System.out.println("INFO SATUSEHAT RUJUKAN : Gagal menutup query kandidat tersimpan: " + e);
            }
        }
    }

    private void setIfNotEmpty(JTextField field, String value) {
        String text = nilai(value);
        if(!text.equals("")){
            field.setText(text);
        }
    }

    private void setJenisRujukanFromDb(String value) {
        String text = nilai(value);
        if("Rawat Inap".equalsIgnoreCase(text)){
            jenisRujukan.setSelectedItem("Rawat Inap");
        }else if("IGD".equalsIgnoreCase(text)){
            jenisRujukan.setSelectedItem("IGD");
        }else if("Rawat Jalan".equalsIgnoreCase(text)){
            jenisRujukan.setSelectedItem("Rawat Jalan");
        }
    }

    private void tambahSupportingInfoIfId(String resourceType, String id) {
        String value = nilai(id);
        if(!value.equals("")){
            tambahSupportingInfo(resourceType + "/" + value);
        }
    }

    private void ambilDataPendukungKlinis(boolean tampilkanPesan) {
        String noRawat = noRawatRujukan();
        if(noRawat.equals("")){
            if(tampilkanPesan){
                JOptionPane.showMessageDialog(this, "No. rawat/rujukan belum terisi.");
            }
            return;
        }
        try {
            bersihkanPlaceholderSupportingInfo();
            int jumlah = 0;
            jumlah += tambahDataPendukungDariTabel("Condition", "satu_sehat_condition", "id_condition", noRawat);
            jumlah += tambahDataPendukungDariTabel("ClinicalImpression", "satu_sehat_clinicalimpression", "id_clinicalimpression", noRawat);
            String[] tabelObservation = new String[]{
                "satu_sehat_observationttvbb",
                "satu_sehat_observationttvgcs",
                "satu_sehat_observationttvkesadaran",
                "satu_sehat_observationttvlp",
                "satu_sehat_observationttvnadi",
                "satu_sehat_observationttvrespirasi",
                "satu_sehat_observationttvspo2",
                "satu_sehat_observationttvsuhu",
                "satu_sehat_observationttvtb",
                "satu_sehat_observationttvtensi"
            };
            for(String table : tabelObservation){
                jumlah += tambahDataPendukungDariTabel("Observation", table, "id_observation", noRawat);
            }
            jumlah += tambahPaketDataPendukungOrder(noRawat, "permintaan_lab",
                    "satu_sehat_servicerequest_lab", "satu_sehat_specimen_lab",
                    "satu_sehat_observation_lab", "satu_sehat_diagnosticreport_lab");
            jumlah += tambahPaketDataPendukungOrder(noRawat, "permintaan_labmb",
                    "satu_sehat_servicerequest_lab_mb", "satu_sehat_specimen_lab_mb",
                    "satu_sehat_observation_lab_mb", "satu_sehat_diagnosticreport_lab_mb");
            jumlah += tambahPaketDataPendukungOrder(noRawat, "permintaan_radiologi",
                    "satu_sehat_servicerequest_radiologi", "satu_sehat_specimen_radiologi",
                    "satu_sehat_observation_radiologi", "satu_sehat_diagnosticreport_radiologi");
            jumlah += tambahHasilRadiologi(noRawat);
            jumlah += tambahDataPendukungDariTabel("AllergyIntolerance", "satu_sehat_allergy_intolerance", "id_allergy_intolerance", noRawat);
            jumlah += tambahDataPendukungDariTabel("AllergyIntolerance", "satu_sehat_allergy", "id_allergy", noRawat);
            jumlah += tambahDataPendukungDariTabel("Procedure", "satu_sehat_procedure", "id_procedure", noRawat);
            jumlah += tambahMedicationRequest(noRawat);
            jumlah += tambahDataPendukungDariTabel("MedicationDispense", "satu_sehat_medicationdispense", "id_medicationdispanse", noRawat);
            jumlah += tambahMedicationAdministration(noRawat);
            refreshCarePlanInfo();
            if(tampilkanPesan){
                JOptionPane.showMessageDialog(this, jumlah == 0
                        ? "Tidak ada data pendukung SATUSEHAT baru untuk no. rawat ini."
                        : "Data pendukung ditambahkan: " + jumlah);
            }
        } catch (Exception e) {
            logPermintaanRujukan("Gagal mengambil data pendukung klinis: " + e);
            if(tampilkanPesan){
                JOptionPane.showMessageDialog(this, "Gagal mengambil data pendukung: " + e.getMessage());
            }
        }
    }

    private int tambahDataPendukungDariTabel(String resourceType, String tableName, String idColumn, String noRawat) throws Exception {
        if(!tableColumnExists(tableName, "no_rawat") || !tableColumnExists(tableName, idColumn)){
            return 0;
        }
        String sql = "select distinct `" + idColumn + "` as id from `" + tableName + "` "
                + "where no_rawat=? and ifnull(`" + idColumn + "`,'')<>''";
        return tambahDataPendukungDariQuery(resourceType, sql, noRawat);
    }

    private int tambahPaketDataPendukungOrder(String noRawat, String orderTable,
            String serviceRequestTable, String specimenTable, String observationTable, String diagnosticReportTable) throws Exception {
        int jumlah = 0;
        jumlah += tambahDataPendukungDariOrder("ServiceRequest", serviceRequestTable, "id_servicerequest", orderTable, noRawat);
        jumlah += tambahDataPendukungDariOrder("Specimen", specimenTable, "id_specimen", orderTable, noRawat);
        jumlah += tambahDataPendukungDariOrder("Observation", observationTable, "id_observation", orderTable, noRawat);
        jumlah += tambahDataPendukungDariTabel("DiagnosticReport", diagnosticReportTable, "id_diagnosticreport", noRawat);
        jumlah += tambahDataPendukungDariOrder("DiagnosticReport", diagnosticReportTable, "id_diagnosticreport", orderTable, noRawat);
        return jumlah;
    }

    private int tambahDataPendukungDariOrder(String resourceType, String tableName, String idColumn,
            String orderTableName, String noRawat) throws Exception {
        if(!tableColumnExists(tableName, "noorder") || !tableColumnExists(tableName, idColumn)
                || !tableColumnExists(orderTableName, "noorder") || !tableColumnExists(orderTableName, "no_rawat")){
            return 0;
        }
        String sql = "select distinct data.`" + idColumn + "` as id from `" + tableName + "` data "
                + "inner join `" + orderTableName + "` order_data on order_data.noorder=data.noorder "
                + "where order_data.no_rawat=? and ifnull(data.`" + idColumn + "`,'')<>''";
        return tambahDataPendukungDariQuery(resourceType, sql, noRawat);
    }

    private int tambahMedicationRequest(String noRawat) throws Exception {
        int jumlah = 0;
        jumlah += tambahMedicationRequestDariResep("satu_sehat_medicationrequest", noRawat);
        jumlah += tambahMedicationRequestDariResep("satu_sehat_medicationrequest_racikan", noRawat);
        return jumlah;
    }

    private int tambahMedicationRequestDariResep(String tableName, String noRawat) throws Exception {
        if(!tableColumnExists(tableName, "no_resep") || !tableColumnExists(tableName, "id_medicationrequest")
                || !tableColumnExists("resep_obat", "no_resep") || !tableColumnExists("resep_obat", "no_rawat")){
            return 0;
        }
        String sql = "select distinct med_request.id_medicationrequest as id from `" + tableName + "` med_request "
                + "inner join resep_obat resep on resep.no_resep=med_request.no_resep "
                + "where resep.no_rawat=? and ifnull(med_request.id_medicationrequest,'')<>''";
        return tambahDataPendukungDariQuery("MedicationRequest", sql, noRawat);
    }

    private int tambahHasilRadiologi(String noRawat) throws Exception {
        if(!tableColumnExists("permintaan_radiologi", "no_rawat")
                || !tableColumnExists("permintaan_radiologi", "noorder")
                || !tableColumnExists("permintaan_radiologi", "tgl_hasil")
                || !tableColumnExists("permintaan_radiologi", "jam_hasil")
                || !tableColumnExists("permintaan_radiologi", "dokter_perujuk")
                || !tableColumnExists("permintaan_pemeriksaan_radiologi", "noorder")
                || !tableColumnExists("permintaan_pemeriksaan_radiologi", "kd_jenis_prw")
                || !tableColumnExists("jns_perawatan_radiologi", "kd_jenis_prw")
                || !tableColumnExists("jns_perawatan_radiologi", "nm_perawatan")
                || !tableColumnExists("periksa_radiologi", "no_rawat")
                || !tableColumnExists("periksa_radiologi", "tgl_periksa")
                || !tableColumnExists("periksa_radiologi", "jam")
                || !tableColumnExists("periksa_radiologi", "dokter_perujuk")
                || !tableColumnExists("hasil_radiologi", "no_rawat")
                || !tableColumnExists("hasil_radiologi", "tgl_periksa")
                || !tableColumnExists("hasil_radiologi", "jam")
                || !tableColumnExists("hasil_radiologi", "hasil")){
            return 0;
        }
        String sql = "select distinct permintaan.noorder, pemeriksaan.kd_jenis_prw, "
                + "permintaan.tgl_hasil, permintaan.jam_hasil, jenis.nm_perawatan, hasil.hasil "
                + "from permintaan_radiologi permintaan "
                + "inner join permintaan_pemeriksaan_radiologi pemeriksaan on pemeriksaan.noorder=permintaan.noorder "
                + "inner join jns_perawatan_radiologi jenis on jenis.kd_jenis_prw=pemeriksaan.kd_jenis_prw "
                + "inner join periksa_radiologi periksa on periksa.no_rawat=permintaan.no_rawat "
                + "and periksa.tgl_periksa=permintaan.tgl_hasil and periksa.jam=permintaan.jam_hasil "
                + "and periksa.dokter_perujuk=permintaan.dokter_perujuk "
                + "inner join hasil_radiologi hasil on hasil.no_rawat=periksa.no_rawat "
                + "and hasil.tgl_periksa=periksa.tgl_periksa and hasil.jam=periksa.jam "
                + "where permintaan.no_rawat=? and ifnull(hasil.hasil,'')<>'' "
                + "order by permintaan.tgl_hasil,permintaan.jam_hasil,permintaan.noorder,pemeriksaan.kd_jenis_prw";
        PreparedStatement ps = null;
        ResultSet rs = null;
        int jumlah = 0;
        try {
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            while(rs.next()){
                if(tambahHasilRadiologiText(ringkasanHasilRadiologi(rs))){
                    jumlah++;
                }
            }
        } finally {
            if(rs != null){
                rs.close();
            }
            if(ps != null){
                ps.close();
            }
        }
        return jumlah;
    }

    private int tambahMedicationAdministration(String noRawat) throws Exception {
        int jumlah = 0;
        String[][] candidates = new String[][]{
            {"satu_sehat_medicationadministration", "id_medicationadministration"},
            {"satu_sehat_medicationadministration", "id_medication_administration"},
            {"satu_sehat_medicationadministration", "id"},
            {"satu_sehat_medication_administration", "id_medicationadministration"},
            {"satu_sehat_medication_administration", "id_medication_administration"},
            {"satu_sehat_medication_administration", "id"}
        };
        for(String[] candidate : candidates){
            jumlah += tambahDataPendukungDariTabel("MedicationAdministration", candidate[0], candidate[1], noRawat);
        }
        return jumlah;
    }

    private int tambahDataPendukungDariQuery(String resourceType, String sql, String noRawat) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int jumlah = 0;
        try {
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            while(rs.next()){
                if(tambahSupportingInfo(resourceType + "/" + rs.getString("id"))){
                    jumlah++;
                }
            }
        } finally {
            if(rs != null){
                rs.close();
            }
            if(ps != null){
                ps.close();
            }
        }
        return jumlah;
    }

    private boolean tableColumnExists(String tableName, String columnName) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select count(*) from information_schema.columns "
                    + "where table_schema=database() and table_name=? and column_name=?");
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } finally {
            if(rs != null){
                rs.close();
            }
            if(ps != null){
                ps.close();
            }
        }
    }

    private void bersihkanPlaceholderSupportingInfo() {
        Set<String> references = new LinkedHashSet<String>();
        for(String line : supportingInfo.getText().split("\\r?\\n")){
            String reference = line.trim();
            if(!reference.equals("") && !isPlaceholderReference(reference)){
                references.add(reference);
            }
        }
        setSupportingInfoReferences(references);
    }

    private void setSupportingInfoReferences(Set<String> references) {
        StringBuilder text = new StringBuilder();
        for(String reference : references){
            if(text.length() > 0){
                text.append("\n");
            }
            text.append(reference);
        }
        supportingInfo.setText(text.toString());
    }

    private String ringkasanHasilRadiologi(ResultSet rs) throws Exception {
        StringBuilder text = new StringBuilder();
        text.append("Radiologi ").append(nilai(rs.getString("noorder")));
        String tindakan = nilai(rs.getString("nm_perawatan"));
        if(!tindakan.equals("")){
            text.append(" - ").append(tindakan);
        }
        String waktu = nilai(rs.getString("tgl_hasil")) + " " + nilai(rs.getString("jam_hasil"));
        if(!waktu.trim().equals("")){
            text.append(" (").append(waktu.trim()).append(")");
        }
        text.append("\n").append(nilai(rs.getString("hasil")));
        return text.toString().trim();
    }

    private boolean tambahHasilRadiologiText(String text) {
        String value = nilai(text);
        if(value.equals("")){
            return false;
        }
        Set<String> hasil = new LinkedHashSet<String>();
        for(String item : hasilRadiologi.getText().split("\\r?\\n\\s*\\r?\\n")){
            String existing = item.trim();
            if(!existing.equals("")){
                hasil.add(existing);
            }
        }
        boolean added = hasil.add(value);
        StringBuilder merged = new StringBuilder();
        for(String item : hasil){
            if(merged.length() > 0){
                merged.append("\n\n");
            }
            merged.append(item);
        }
        hasilRadiologi.setText(merged.toString());
        return added;
    }

    private String fhirDateTime(String value) {
        String text = nilai(value);
        if(text.equals("") || "0000-00-00 00:00:00".equals(text)){
            return "";
        }
        if(text.length() >= 19){
            return text.substring(0, 10) + "T" + text.substring(11, 19) + "+08:00";
        }
        return text;
    }

    private String formatSatuan(String value, String satuan) {
        String text = nilai(value);
        return text.equals("") ? "" : text + " " + satuan;
    }

    private String noRawatRujukan() {
        return nomorRujukan.getText().trim();
    }

    private String noRujukanRujukan() {
        return nomorRujukan.getText().trim();
    }

    private String jenisRujukanDb() {
        return String.valueOf(jenisRujukan.getSelectedItem());
    }

    private String tanggalDariWaktu(String waktu) {
        String value = nilai(waktu);
        if(value.length() >= 10){
            return value.substring(0, 10);
        }
        return LocalDate.now().toString();
    }

    private String jamDariWaktu(String waktu) {
        String value = nilai(waktu);
        if(value.length() >= 19){
            return value.substring(11, 19);
        }
        return LocalTime.now().withNano(0).toString();
    }

    private String mysqlDateTime(String waktu) {
        String value = nilai(waktu);
        if(value.length() >= 19){
            return value.substring(0, 10) + " " + value.substring(11, 19);
        }
        return "";
    }

    private String angkaDariTeks(String teks) {
        String value = nilai(teks);
        StringBuilder angka = new StringBuilder();
        for(int i = 0; i < value.length(); i++){
            char c = value.charAt(i);
            if((c >= '0' && c <= '9') || c == '.' || c == '-'){
                angka.append(c);
            }else if(angka.length() > 0){
                break;
            }
        }
        return angka.toString();
    }

    private String getTableValue(DefaultTableModel model, int row, int column) {
        if(row < 0 || row >= model.getRowCount() || column < 0 || column >= model.getColumnCount()){
            return "";
        }
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString().trim();
    }

    private void urutkanKandidatFaskesTerdekat() {
        if(tabModeKandidatFaskes.getRowCount() <= 1){
            return;
        }
        List<Object[]> rows = new ArrayList<Object[]>();
        for(int row = 0; row < tabModeKandidatFaskes.getRowCount(); row++){
            Object[] values = new Object[tabModeKandidatFaskes.getColumnCount()];
            for(int column = 0; column < tabModeKandidatFaskes.getColumnCount(); column++){
                values[column] = tabModeKandidatFaskes.getValueAt(row, column);
            }
            rows.add(values);
        }
        rows.sort((left, right) -> {
            int compare = Double.compare(jarakKandidatFaskes(left[KANDIDAT_JARAK]), jarakKandidatFaskes(right[KANDIDAT_JARAK]));
            if(compare != 0){
                return compare;
            }
            return Integer.compare(urutKandidatFaskes(left[KANDIDAT_NO]), urutKandidatFaskes(right[KANDIDAT_NO]));
        });
        tabModeKandidatFaskes.setRowCount(0);
        int nomor = 1;
        for(Object[] row : rows){
            row[KANDIDAT_NO] = nomor++;
            tabModeKandidatFaskes.addRow(row);
        }
    }

    private double jarakKandidatFaskes(Object value) {
        String text = value == null ? "" : value.toString().replace(',', '.');
        String angka = angkaDariTeks(text);
        if(angka.equals("")){
            return Double.MAX_VALUE;
        }
        try {
            return Double.parseDouble(angka);
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    private int urutKandidatFaskes(Object value) {
        String angka = angkaDariTeks(value == null ? "" : value.toString());
        if(angka.equals("")){
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(angka);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private String endpoint(String path) {
        String root = baseUrl.getText().trim();
        String suffix = nilai(path);
        if(root.endsWith("/") && suffix.startsWith("/")){
            return root + suffix.substring(1);
        }
        if(!root.endsWith("/") && !suffix.equals("") && !suffix.startsWith("/")){
            return root + "/" + suffix;
        }
        return root + suffix;
    }

    private void refreshCarePlanInfo() {
        try {
            SatuSehatKirimRujukan.RujukanData data = collectData();
            ObjectNode carePlan = rujukan.buildCarePlan(data);
            tampilkanInformasiCarePlan(data, carePlan, carePlanId);
        } catch (Exception e) {
            carePlanInfo.setText("");
        }
    }

    private void tampilkanInformasiCarePlan(SatuSehatKirimRujukan.RujukanData data, ObjectNode carePlan, String idTerkirim) {
        try {
            StringBuilder info = new StringBuilder();
            String id = nilai(idTerkirim);
            if(id.equals("")){
                id = jsonText(carePlan.path("id"), "");
            }
            info.append("ID CarePlan : ").append(id.equals("") ? "belum terkirim / akan dibuat baru" : id).append("\n");
            info.append("Status      : ").append(jsonText(carePlan.path("status"), "-")).append("\n");
            info.append("Intent      : ").append(jsonText(carePlan.path("intent"), "-")).append("\n");
            info.append("Judul       : ").append(jsonText(carePlan.path("title"), "-")).append("\n");
            info.append("Kategori    : ").append(codingSummary(carePlan.path("category"))).append("\n");
            info.append("Pasien      : ").append(referenceSummary(carePlan.path("subject"))).append("\n");
            info.append("Encounter   : ").append(referenceSummary(carePlan.path("encounter"))).append("\n");
            info.append("Author      : ").append(referenceSummary(carePlan.path("author"))).append("\n");
            info.append("Contributor : ").append(referenceArraySummary(carePlan.path("contributor"))).append("\n");
            info.append("Diagnosa    : ").append(data.diagnosisUtamaCode).append(" / ").append(data.diagnosisUtamaDisplay).append("\n");
            info.append("Dibuat      : ").append(jsonText(carePlan.path("created"), "-")).append("\n");
            info.append("Keterangan  : ").append(jsonText(carePlan.path("description"), "-")).append("\n");
            info.append("Hasil Rad   : ").append(data.hasilRadiologi.equals("") ? "-" : data.hasilRadiologi.replace("\n", " ")).append("\n");
            info.append("Identifier  : ").append(identifierSummary(carePlan.path("identifier"))).append("\n");
            info.append("Addresses   : ").append(referenceArraySummary(carePlan.path("addresses")));
            carePlanInfo.setText(info.toString());
            carePlanInfo.setCaretPosition(0);
        } catch (Exception e) {
            carePlanInfo.setText("Gagal menampilkan informasi CarePlan: " + e.getMessage());
        }
    }

    private String codingSummary(JsonNode node) {
        if(node == null || node.isMissingNode() || node.isNull()){
            return "-";
        }
        List<String> values = new ArrayList<String>();
        if(node.isArray()){
            for(JsonNode item : node){
                String summary = codingSummary(item);
                if(!summary.equals("-")){
                    values.add(summary);
                }
            }
        }else{
            JsonNode codings = node.path("coding");
            if(codings.isArray()){
                for(JsonNode coding : codings){
                    String code = jsonText(coding.path("code"), "");
                    String display = jsonText(coding.path("display"), "");
                    if(!code.equals("") || !display.equals("")){
                        values.add(code + (display.equals("") ? "" : " - " + display));
                    }
                }
            }
            String text = jsonText(node.path("text"), "");
            if(!text.equals("")){
                values.add(text);
            }
        }
        if(values.isEmpty()){
            return "-";
        }
        StringBuilder result = new StringBuilder();
        for(String value : values){
            if(result.length() > 0){
                result.append("; ");
            }
            result.append(value);
        }
        return result.toString();
    }

    private String referenceSummary(JsonNode node) {
        if(node == null || node.isMissingNode() || node.isNull()){
            return "-";
        }
        String reference = jsonText(node.path("reference"), "");
        String display = jsonText(node.path("display"), "");
        if(reference.equals("") && display.equals("")){
            return "-";
        }
        return reference + (display.equals("") ? "" : " / " + display);
    }

    private String referenceArraySummary(JsonNode node) {
        if(node == null || node.isMissingNode() || node.isNull()){
            return "-";
        }
        if(!node.isArray()){
            return referenceSummary(node);
        }
        StringBuilder result = new StringBuilder();
        for(JsonNode item : node){
            String summary = referenceSummary(item);
            if(!summary.equals("-")){
                if(result.length() > 0){
                    result.append("; ");
                }
                result.append(summary);
            }
        }
        return result.length() == 0 ? "-" : result.toString();
    }

    private String identifierSummary(JsonNode node) {
        if(node == null || node.isMissingNode() || node.isNull()){
            return "-";
        }
        if(!node.isArray()){
            String system = jsonText(node.path("system"), "");
            String value = jsonText(node.path("value"), "");
            if(system.equals("") && value.equals("")){
                return "-";
            }
            return value + (system.equals("") ? "" : " (" + system + ")");
        }
        StringBuilder result = new StringBuilder();
        for(JsonNode item : node){
            String summary = identifierSummary(item);
            if(!summary.equals("-")){
                if(result.length() > 0){
                    result.append("; ");
                }
                result.append(summary);
            }
        }
        return result.length() == 0 ? "-" : result.toString();
    }

    private String jsonText(JsonNode node, String defaultValue) {
        if(node == null || node.isMissingNode() || node.isNull()){
            return defaultValue;
        }
        String value = node.asText();
        return value == null || value.equals("") ? defaultValue : value;
    }

    private void logPencarianKandidat(String message) {
        System.out.println("INFO SATUSEHAT RUJUKAN PENCARIAN KANDIDAT : " + message);
    }

    private void logPraPermintaan(String message) {
        System.out.println("INFO SATUSEHAT RUJUKAN PRA PERMINTAAN : " + message);
    }

    private void logTugasRujukan(String message) {
        System.out.println("INFO SATUSEHAT RUJUKAN TUGAS RUJUKAN : " + message);
    }

    private void logPermintaanRujukan(String message) {
        System.out.println("INFO SATUSEHAT RUJUKAN PERMINTAAN RUJUKAN : " + message);
    }

    private boolean tambahSupportingInfo(String reference) {
        String value = nilai(reference);
        if(value.equals("") || hasSupportingInfo(value)){
            return false;
        }
        if(supportingInfo.getText().trim().equals("")){
            supportingInfo.setText(value);
        }else{
            supportingInfo.append("\n"+value);
        }
        return true;
    }

    private boolean hasSupportingInfo(String reference) {
        String value = nilai(reference);
        if(value.equals("")){
            return true;
        }
        for(String line : supportingInfo.getText().split("\\r?\\n")){
            if(value.equals(line.trim())){
                return true;
            }
        }
        return false;
    }

    private void resetKandidatFaskes() {
        taskPencarianKandidatId = "";
        taskRujukanId = "";
        serviceRequestId = "";
        appointmentId = "";
        statusTaskRujukan = "";
        responTaskRujukan = "";
        orgRujukanId.setText("");
        orgRujukanName.setText("");
        tabModeKandidatFaskes.setRowCount(0);
        refreshCarePlanInfo();
        logPencarianKandidat("Kandidat faskes dikosongkan. Task kandidat dan data rujukan setelahnya direset.");
    }

    private void resetSessionRujukan() {
        taskPraPermintaanId = "";
        taskPencarianKandidatId = "";
        taskRujukanId = "";
        carePlanId = "";
        serviceRequestId = "";
        appointmentId = "";
        statusTaskRujukan = "";
        responTaskRujukan = "";
        orgRujukanId.setText("");
        orgRujukanName.setText("");
        tabModeKandidatFaskes.setRowCount(0);
        isiDefaultKriteriaRujukan();
        refreshCarePlanInfo();
    }

    private void populateKriteriaRujukanDariTask(JsonNode task) {
        JsonNode questionnaire = cariQuestionnaireKriteria(task);
        if(questionnaire == null || questionnaire.path("item").isMissingNode()){
            isiDefaultKriteriaRujukan();
            JOptionPane.showMessageDialog(this, "Task Pra Permintaan terkirim, tetapi Questionnaire kriteria tidak ditemukan di response. Form memakai kriteria default dari collection.");
            return;
        }
        tabModeKriteriaRujukan.setRowCount(0);
        tambahItemQuestionnaire(questionnaire.path("item"), "");
    }

    private JsonNode cariQuestionnaireKriteria(JsonNode task) {
        String reference = "";
        for(JsonNode output : task.path("output")){
            if(hasCoding(output.path("type"), "referral-criteria")){
                reference = output.path("valueReference").path("reference").asText();
                break;
            }
        }
        String id = reference.startsWith("#") ? reference.substring(1) : reference;
        if(id.equals("")){
            id = "123456789";
        }
        JsonNode fallbackQuestionnaire = null;
        for(JsonNode contained : task.path("contained")){
            if("Questionnaire".equals(contained.path("resourceType").asText())){
                if(id.equals(contained.path("id").asText())
                        || contained.path("title").asText().toLowerCase().contains("kriteria")){
                    return contained;
                }
                if(fallbackQuestionnaire == null && isQuestionnaireKriteria(contained)){
                    fallbackQuestionnaire = contained;
                }
            }
        }
        return fallbackQuestionnaire;
    }

    private boolean isQuestionnaireKriteria(JsonNode questionnaire) {
        String title = questionnaire.path("title").asText().toLowerCase();
        if(title.contains("jejaring") || title.contains("wilayah") || title.contains("area")){
            return false;
        }
        for(JsonNode item : questionnaire.path("item")){
            if(!item.path("answerOption").isMissingNode()){
                return false;
            }
        }
        return questionnaire.path("item").isArray() && questionnaire.path("item").size() > 0;
    }

    private boolean hasCoding(JsonNode codeable, String code) {
        for(JsonNode coding : codeable.path("coding")){
            if(code.equals(coding.path("code").asText())){
                return true;
            }
        }
        return false;
    }

    private void populateKandidatFaskesDariTask(JsonNode response) {
        populateKandidatFaskesDariTask(response, true);
    }

    private void populateKandidatFaskesDariTask(JsonNode response, boolean tampilkanPesanKosong) {
        tabModeKandidatFaskes.setRowCount(0);
        tambahKandidatDariNode(response);
        urutkanKandidatFaskesTerdekat();
        if(tampilkanPesanKosong && tabModeKandidatFaskes.getRowCount() == 0){
            JOptionPane.showMessageDialog(this, "Daftar kandidat faskes belum ditemukan di response Task.\nStatus Task: "+response.path("status").asText()+"\nCoba Ambil Kandidat lagi setelah SATUSEHAT mengisi output kandidat.");
        }
    }

    private void tambahKandidatDariNode(JsonNode node) {
        if(node == null || node.isMissingNode()){
            return;
        }
        for(JsonNode output : node.path("output")){
            if(isOutputKandidatFaskes(output)){
                tambahKandidatFaskes(output);
            }
        }
        for(JsonNode entry : node.path("entry")){
            tambahKandidatDariNode(entry.path("resource"));
        }
    }

    private boolean isOutputKandidatFaskes(JsonNode output) {
        return hasCoding(output.path("type"), "candidate-referral-facility") || hasCoding(output.path("type"), "candidate");
    }

    private void tambahKandidatFaskes(JsonNode output) {
        String reference = output.path("valueReference").path("reference").asText();
        String orgId = stripPrefix(reference, "Organization/");
        String nama = output.path("valueReference").path("display").asText();
        String jarak = "";
        String estimasi = "";
        String strata = "";
        String kodeBpjs = "";
        String kodeKemkes = "";
        for(JsonNode extension : output.path("extension")){
            for(JsonNode child : extension.path("extension")){
                String url = child.path("url").asText();
                if("distance".equals(url)){
                    JsonNode quantity = child.path("valueQuantity");
                    jarak = quantity.path("value").asText();
                    if(!quantity.path("unit").asText().equals("")){
                        jarak = jarak + " " + quantity.path("unit").asText();
                    }
                }else if("estimated-time".equals(url)){
                    JsonNode quantity = child.path("valueQuantity");
                    estimasi = quantity.path("value").asText();
                    if(!quantity.path("unit").asText().equals("")){
                        estimasi = estimasi + " " + quantity.path("unit").asText();
                    }
                }else if("strata".equals(url)){
                    strata = child.path("valueCode").asText();
                }else if("bpjs-code".equals(url)){
                    kodeBpjs = child.path("valueCode").asText();
                }else if("kemkes-code".equals(url)){
                    kodeKemkes = child.path("valueCode").asText();
                }
            }
        }
        if(!orgId.equals("")){
            tabModeKandidatFaskes.addRow(new Object[]{
                Boolean.FALSE,
                tabModeKandidatFaskes.getRowCount() + 1,
                orgId,
                nama,
                jarak,
                estimasi,
                strata,
                kodeBpjs,
                kodeKemkes,
                "",
                "",
                ""
            });
        }
    }

    private void pakaiKandidatTerpilih() {
        int row = tbKandidatFaskes.getSelectedRow();
        if(row == -1){
            JOptionPane.showMessageDialog(this, "Pilih kandidat faskes terlebih dahulu.");
            return;
        }
        int modelRow = tbKandidatFaskes.convertRowIndexToModel(row);
        pakaiKandidatDariModelRow(modelRow);
    }

    private void pakaiKandidatDariModelRow(int modelRow) {
        if(modelRow < 0 || modelRow >= tabModeKandidatFaskes.getRowCount()){
            return;
        }
        tabModeKandidatFaskes.setValueAt(Boolean.TRUE, modelRow, KANDIDAT_PILIH);
        orgRujukanId.setText(getTableValue(tabModeKandidatFaskes, modelRow, KANDIDAT_ORG_ID));
        orgRujukanName.setText(getTableValue(tabModeKandidatFaskes, modelRow, KANDIDAT_NAMA));
        String taskId = getTableValue(tabModeKandidatFaskes, modelRow, KANDIDAT_TASK);
        if(!taskId.equals("")){
            taskRujukanId = taskId;
            statusTaskRujukan = getTableValue(tabModeKandidatFaskes, modelRow, KANDIDAT_STATUS);
            responTaskRujukan = getTableValue(tabModeKandidatFaskes, modelRow, KANDIDAT_RESPON);
        }
    }

    private List<Integer> kandidatDipilihUntukTaskRujukan() {
        List<Integer> rows = new ArrayList<Integer>();
        for(int row = 0; row < tabModeKandidatFaskes.getRowCount(); row++){
            Object pilih = tabModeKandidatFaskes.getValueAt(row, KANDIDAT_PILIH);
            if(Boolean.TRUE.equals(pilih)){
                rows.add(row);
            }
        }
        if(rows.isEmpty() && tbKandidatFaskes.getSelectedRow() != -1){
            int modelRow = tbKandidatFaskes.convertRowIndexToModel(tbKandidatFaskes.getSelectedRow());
            tabModeKandidatFaskes.setValueAt(Boolean.TRUE, modelRow, KANDIDAT_PILIH);
            rows.add(modelRow);
        }
        return rows;
    }

    private List<Integer> kandidatDenganTaskRujukan() {
        List<Integer> rows = new ArrayList<Integer>();
        for(int row = 0; row < tabModeKandidatFaskes.getRowCount(); row++){
            if(!getTableValue(tabModeKandidatFaskes, row, KANDIDAT_TASK).equals("")){
                rows.add(row);
            }
        }
        return rows;
    }

    private boolean pakaiKandidatAcceptedPertama() {
        for(int row = 0; row < tabModeKandidatFaskes.getRowCount(); row++){
            if("accepted".equalsIgnoreCase(getTableValue(tabModeKandidatFaskes, row, KANDIDAT_RESPON))){
                pakaiKandidatDariModelRow(row);
                return true;
            }
        }
        return false;
    }

    private List<SatuSehatKirimRujukan.FaskesKandidat> kandidatTaskRujukanDariRows(List<Integer> rows) {
        List<SatuSehatKirimRujukan.FaskesKandidat> kandidatList = new ArrayList<SatuSehatKirimRujukan.FaskesKandidat>();
        Set<String> organisasiDipakai = new LinkedHashSet<String>();
        for(Integer row : rows){
            if(row == null || row < 0 || row >= tabModeKandidatFaskes.getRowCount()){
                continue;
            }
            String orgId = getTableValue(tabModeKandidatFaskes, row, KANDIDAT_ORG_ID);
            if(orgId.equals("") || organisasiDipakai.contains(orgId)){
                continue;
            }
            organisasiDipakai.add(orgId);
            SatuSehatKirimRujukan.FaskesKandidat kandidat = new SatuSehatKirimRujukan.FaskesKandidat(
                    orgId, getTableValue(tabModeKandidatFaskes, row, KANDIDAT_NAMA));
            kandidat.taskUuid = UUID.randomUUID().toString();
            kandidatList.add(kandidat);
        }
        return kandidatList;
    }

    private void tandaiTaskKandidatTerkirim(List<Integer> rows, List<SatuSehatKirimRujukan.FaskesKandidat> kandidatList, List<String> taskIds) {
        int index = 0;
        for(Integer row : rows){
            if(row == null || row < 0 || row >= tabModeKandidatFaskes.getRowCount()){
                continue;
            }
            if(index >= kandidatList.size()){
                break;
            }
            String taskId = index < taskIds.size() ? taskIds.get(index) : "";
            if(!taskId.equals("")){
                tabModeKandidatFaskes.setValueAt(taskId, row, KANDIDAT_TASK);
                tabModeKandidatFaskes.setValueAt("requested", row, KANDIDAT_STATUS);
                tabModeKandidatFaskes.setValueAt("", row, KANDIDAT_RESPON);
            }
            logTugasRujukan("Task kandidat "+getTableValue(tabModeKandidatFaskes, row, KANDIDAT_ORG_ID)
                    +" / "+getTableValue(tabModeKandidatFaskes, row, KANDIDAT_NAMA)+": "+(taskId.equals("") ? "-" : taskId));
            index++;
        }
    }

    private String stripPrefix(String value, String prefix) {
        String text = nilai(value);
        return text.startsWith(prefix) ? text.substring(prefix.length()) : text;
    }

    private void tambahItemQuestionnaire(JsonNode items, String parentText) {
        if(!items.isArray()){
            return;
        }
        for(JsonNode item : items){
            String linkId = item.path("linkId").asText();
            String text = item.path("text").asText();
            String label = parentText.equals("") ? text : parentText + " - " + text;
            if(item.path("item").isArray() && item.path("item").size() > 0){
                tambahItemQuestionnaire(item.path("item"), label);
            }else if(!linkId.equals("")){
                tabModeKriteriaRujukan.addRow(new Object[]{
                    tabModeKriteriaRujukan.getRowCount() + 1,
                    linkId,
                    label,
                    defaultJawabanKriteria(linkId, item.path("type").asText())
                });
            }
        }
    }

    private String defaultJawabanKriteria(String linkId, String type) {
        if(!"IGD".equals(String.valueOf(jenisRujukan.getSelectedItem())) || !"boolean".equalsIgnoreCase(nilai(type))){
            return "";
        }
        if("000003".equals(linkId)){
            return "false";
        }
        if("000001".equals(linkId) || "000002".equals(linkId) || "000004".equals(linkId) || "000005".equals(linkId)){
            return "true";
        }
        return "";
    }

    private void isiDefaultKriteriaRujukan() {
        tabModeKriteriaRujukan.setRowCount(0);
        if("IGD".equals(String.valueOf(jenisRujukan.getSelectedItem()))){
            tabModeKriteriaRujukan.addRow(new Object[]{1, "000001", "Mengancam nyawa, membahayakan diri dan orang lain/lingkungan", "true"});
            tabModeKriteriaRujukan.addRow(new Object[]{2, "000002", "Adanya gangguan pada jalan nafas, pernafasan, dan sirkulasi", "true"});
            tabModeKriteriaRujukan.addRow(new Object[]{3, "000003", "Adanya penurunan kesadaran", "false"});
            tabModeKriteriaRujukan.addRow(new Object[]{4, "000004", "Adanya gangguan hemodinamik", "true"});
            tabModeKriteriaRujukan.addRow(new Object[]{5, "000005", "Memerlukan tindakan segera", "true"});
        }else{
            tabModeKriteriaRujukan.addRow(new Object[]{1, "66813", "Terapy/Pengobatan", ""});
            tabModeKriteriaRujukan.addRow(new Object[]{2, "41904", "Tindakan Medis", ""});
            tabModeKriteriaRujukan.addRow(new Object[]{3, "16995", "Upaya Diagnosis", ""});
        }
    }

    private SatuSehatKirimRujukan.RujukanData collectData() {
        SatuSehatKirimRujukan.RujukanData data = new SatuSehatKirimRujukan.RujukanData();
        data.jenisRujukan = jenisRujukanValue();
        data.nomorRujukan = nomorRujukan.getText().trim();
        data.patientId = patientId.getText().trim();
        data.patientName = patientName.getText().trim();
        data.practitionerId = practitionerId.getText().trim();
        data.practitionerName = practitionerName.getText().trim();
        data.practitionerRujukanId = practitionerRujukanId.getText().trim();
        data.practitionerRujukanName = practitionerRujukanName.getText().trim();
        data.orgPerujukId = orgPerujukId.getText().trim();
        data.orgPerujukName = orgPerujukName.getText().trim();
        data.orgPerujukFaskesUtamaId = "";
        try {
            data.orgPerujukFaskesUtamaId = koneksiDB.IDSATUSEHAT();
        } catch (Exception e) {
            data.orgPerujukFaskesUtamaId = "";
        }
        data.orgPerujukFaskesUtamaName = data.orgPerujukName;
        data.orgRujukanId = orgRujukanId.getText().trim();
        data.orgRujukanName = orgRujukanName.getText().trim();
        data.encounterId = encounterId.getText().trim();
        data.diagnosisUtamaCode = diagnosisCode.getText().trim();
        data.diagnosisUtamaDisplay = diagnosisDisplay.getText().trim();
        data.performerTypeCode = performerTypeCode.getText().trim();
        data.performerTypeDisplay = performerTypeDisplay.getText().trim();
        data.authoredOn = authoredOn.getText().trim();
        data.occurrenceDateTime = occurrenceDateTime.getText().trim();
        data.keterangan = keterangan.getText().trim();
        data.patientInstruction = keterangan.getText().trim();
        data.hasilRadiologi = hasilRadiologi.getText().trim();
        data.carePlanId = carePlanId;
        data.serviceRequestId = serviceRequestId;
        data.taskPencarianKandidatId = taskPencarianKandidatId;
        for (String line : supportingInfo.getText().split("\\r?\\n")) {
            String reference = line.trim();
            if (reference.equals("") || isPlaceholderReference(reference)) {
                continue;
            }
            if (reference.startsWith("Condition/")) {
                data.conditionIds.add(reference);
            }
            if (reference.startsWith("Task/")) {
                continue;
            }
            if (!isTaskReference(reference, taskPencarianKandidatId)
                    && !isTaskReference(reference, taskRujukanId)
                    && !isTaskReference(reference, taskPraPermintaanId)) {
                data.supportingInfoReferences.add(reference);
            }
        }
        return data;
    }

    private String tambahJam(String tanggal, int jumlahJam) {
        try {
            return OffsetDateTime.parse(nilai(tanggal)).plusHours(jumlahJam).toString();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isTaskReference(String reference, String taskId) {
        if(taskId == null || taskId.trim().equals("")){
            return false;
        }
        String value = reference == null ? "" : reference.trim();
        String id = taskId.trim();
        return value.equals(id) || value.equals("Task/"+id);
    }

    private boolean isPlaceholderReference(String reference) {
        String value = nilai(reference);
        return value.contains("{") || value.contains("}") || value.toLowerCase().contains("id_");
    }

    private String jenisRujukanValue() {
        String selected = String.valueOf(jenisRujukan.getSelectedItem());
        if (selected.equals("Rawat Inap")) {
            return SatuSehatKirimRujukan.RAWAT_INAP;
        }
        if (selected.equals("IGD")) {
            return SatuSehatKirimRujukan.IGD;
        }
        return SatuSehatKirimRujukan.RAWAT_JALAN;
    }

}
