package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import fungsi.akses;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import net.sf.jasperreports.engine.JasperCompileManager;

/** UI Tahap II: draf lokal + workflow SATUSEHAT Rujukan IGD/Rawat Inap.
 *  HTTP hanya dijalankan melalui SatuSehatRujukanIGDRanapApi. */
public final class SatuSehatRujukanIGDRanapPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color INK = new Color(25,49,69), MUTED = new Color(91,113,131);
    private static final Color BLUE = new Color(10,112,182), BG = new Color(244,247,251);
    private static final Color LINE = new Color(219,229,238), LIGHT = new Color(233,244,252);
    private static final Color GREEN = new Color(26,111,79), AMBER = new Color(142,86,16);
    private static final Font BODY = new Font("Segoe UI",Font.PLAIN,13), SMALL = new Font("Segoe UI",Font.PLAIN,12);
    private static final Font STRONG = new Font("Segoe UI Semibold",Font.BOLD,14);
    private static final String DEFAULT_PROVINCE_CODE = "73";
    private static final String DEFAULT_PROVINCE_DISPLAY = "Sulawesi Selatan";
    private static final String DEFAULT_CITY_CODE = "7371";
    private static final String DEFAULT_CITY_DISPLAY = "Kota Makassar";
    private static final String ADMIN_AREA_SYSTEM = "http://sys-ids.kemkes.go.id/administrative-area";
    private static final String DEFAULT_SERVICE_GROUP_SYSTEM = "http://terminology.kemkes.go.id";
    private static final String[][] TRANSPORT_OPTIONS = {
        {"", "(tidak ada)"}, {"49122002", "Ambulance (umum)"}, {"1285128001", "Ambulans Gawat Darurat"},
        {"465341007", "Ambulance Transport (PSC)"}, {"71783008", "Mobil"}, {"90748009", "Motor"}, {"74964007", "Lain-lain"}
    };
    private final SatuSehatRujukanIGDRanapRepository repository;
    private final boolean preview;
    private final Map<String,JTextComponent> fields = new LinkedHashMap<String,JTextComponent>();
    private final Map<String,JLabel> clinicalValues = new LinkedHashMap<String,JLabel>();
    private final Map<String,String> clinicalCaptions = new LinkedHashMap<String,String>();
    private final Map<String,String> criteriaGroupId = new LinkedHashMap<String,String>();
    private final Map<String,String> criteriaGroupText = new LinkedHashMap<String,String>();
    private final JButton[] stages = new JButton[4];
    private final List<JButton> copies = new ArrayList<JButton>();
    private final JLabel name = label("Pilih kunjungan pasien",STRONG,INK);
    private final JLabel rm = label("No. RM  -",BODY,MUTED), rawat = label("No. Rawat  -",BODY,MUTED);
    private final JLabel sep = label("No. SEP  -",SMALL,MUTED);
    private final JLabel title = label("Rujukan Keluar",new Font("Segoe UI Semibold",Font.BOLD,23),INK);
    private final JLabel badge = pill("IGD",LIGHT,BLUE), saveStatus = pill("Belum ada draf",LIGHT,BLUE);
    private final JLabel footerStatus = label("Muat kunjungan untuk mulai mengisi draf.",SMALL,MUTED);
    private final JLabel doctorLabel = label("Dokter pengirim",SMALL,MUTED);
    private final JLabel clinicalTitle = label("Konfirmasi kegawatdaruratan",STRONG,INK);
    private final JTextArea clinicalHelp = readOnly(2), sources = readOnly(5);
    private final JLabel ihsValue = label("IHS pasien: —",SMALL,MUTED);
    private final JLabel encounterValue = label("Encounter: —",SMALL,MUTED);
    private final JLabel clinicalSource = label("Pilih kunjungan untuk melihat data yang tersedia.",SMALL,MUTED);
    private final JLabel diagnosisMappingInfo = label("Mapping diagnosis: —",SMALL,MUTED);
    private final JLabel loadingNote = label("",SMALL,AMBER);
    private final JPanel loadingNotes = new JPanel(new BorderLayout(8,0));
    private final JButton clinicalDetails = button("Lihat data klinis",Color.WHITE,BLUE);
    private final JButton refreshData = button("Refresh Data",Color.WHITE,BLUE);
    private final JButton chooseDiagnosis = button("",Color.WHITE,BLUE);
    private final JButton openEncounter = button("Buat Encounter",Color.WHITE,BLUE);
    private final JButton createCondition = button("Cari Condition",Color.WHITE,BLUE);
    private final JButton findCandidate = button("Cari Rumah Sakit",Color.WHITE,BLUE);
    private final PlaceholderTextField candidateSearchField = new PlaceholderTextField("Cari rs kandidat disini");
    private javax.swing.table.TableRowSorter<DefaultTableModel> candidateSorter;
    private final JComboBox<String> transportCombo = new JComboBox<String>();
    private final JButton save = button("Simpan draf",BLUE,Color.WHITE);
    private final JButton exit = button("Keluar",Color.WHITE,MUTED);
    private final JCheckBox emergency = new JCheckBox("Kegawatdaruratan dikonfirmasi dokter");
    private final JPanel toastPanel = new JPanel(new BorderLayout(12,0));
    private final JTextArea toastText = readOnly(1);
    private final Timer toastTimer = new Timer(5000,e -> hideToast());
    private final JPanel pages = new JPanel(new CardLayout());
    private final JPanel stageStrip = new JPanel(new GridLayout(1,4,8,0));
    private final JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
    private final DefaultTableModel criteriaModel = new DefaultTableModel(new String[]{"linkId","Pertanyaan kriteria","Tipe","Jawaban"},0){
        @Override public boolean isCellEditable(int r,int c){return c==3;}
    };
    private final DefaultTableModel candidateModel = new DefaultTableModel(new String[]{"Pilih","Org ID","Nama RS","Kode BPJS","Strata","Jarak","Estimasi","Bed"},0){
        @Override public boolean isCellEditable(int r,int c){return c==0;}
        @Override public Class<?> getColumnClass(int c){return c==0?Boolean.class:String.class;}
    };
    private final DefaultTableModel approvalModel = new DefaultTableModel(new String[]{"Final","Org ID","Nama RS","Task ID","Status","Keputusan"},0){
        @Override public boolean isCellEditable(int r,int c){return c==0 && "accepted".equalsIgnoreCase(String.valueOf(getValueAt(r,5)));}
        @Override public Class<?> getColumnClass(int c){return c==0?Boolean.class:String.class;}
    };
    private JTable criteriaTable, candidateTable, approvalTable;
    private JPanel criteriaCard, candidateCard;
    private JScrollPane criteriaScrollPane, candidateScrollPane;
    private final JLabel finalStatusLabel = label("Belum difinalisasi.", SMALL, MUTED);
    private final JLabel finalResultTitle = label("Rujukan belum difinalisasi", STRONG, MUTED);
    private final JLabel finalNationalNumber = label("—", new Font("Segoe UI Semibold",Font.BOLD,22), INK);
    private final JLabel finalTargetLabel = label("RS tujuan: —", SMALL, MUTED);
    private final JLabel finalServiceRequestLabel = label("ServiceRequest: —", SMALL, MUTED);
    private final JButton copyNationalReferral = button("Salin Nomor",Color.WHITE,BLUE);
    private final JButton printReferral = button("Cetak Rujukan",BLUE,Color.WHITE);
    private JPanel finalResultCard;
    private final JLabel workflowIdsLabel = label("Task/CarePlan: —", SMALL, MUTED);
    private final JLabel criteriaTaskIdLabel = label("Task Pra-Permintaan: —", SMALL, MUTED);
    private final JLabel candidateTaskIdLabel = label("Task Pencarian Kandidat: —", SMALL, MUTED);
    private SatuSehatRujukanIGDRanapApi apiClient;
    private String lastApiAction="", lastApiRequest="", lastApiResponse="";
    private final JTextField ihsPatientField = new JTextField();
    private final JTextField encounterField = new JTextField();
    private final List<ApiTraceGroup> apiTraceGroups = new ArrayList<ApiTraceGroup>();
    private int apiGroupedTraceCount;
    private static final class ApiTraceGroup {
        String action=""; int start,end; boolean error; long timeMillis; String errorMessage="";
    }
    private SatuSehatRujukanIGDRanapDraft.Jenis jenis = SatuSehatRujukanIGDRanapDraft.Jenis.IGD;
    private SatuSehatRujukanIGDRanapRepository.Visit visit;
    private SatuSehatRujukanIGDRanapDraft draft;
    private String operator = "";
    private SatuSehatRujukanIGDRanapDraft.Jenis requestedJenis;
    private boolean busy, dirty, restoring, draftAvailable, candidateSelectionAdjusting;
    private int selectedStage, selectedPage;
    private Runnable onClose;

    public SatuSehatRujukanIGDRanapPanel(SatuSehatRujukanIGDRanapRepository repository, boolean preview) {
        if (!preview && repository == null) throw new IllegalArgumentException("Repository wajib tersedia.");
        this.repository = repository; this.preview = preview;
        this.apiClient = preview ? null : new SatuSehatRujukanIGDRanapApi();
        setLayout(new BorderLayout()); setBackground(BG); setFont(BODY);
        JPanel main = new JPanel(new BorderLayout(0,12)); main.setBackground(BG);
        main.setBorder(BorderFactory.createEmptyBorder(16,22,14,22));
        JPanel overview = new JPanel(new BorderLayout(0,10)); overview.setOpaque(false);
        overview.add(identity(),BorderLayout.NORTH); buildNotice(); overview.add(toastPanel,BorderLayout.SOUTH);
        main.add(overview,BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0,12)); center.setOpaque(false);
        buildStages(); center.add(stageStrip,BorderLayout.NORTH); pages.setOpaque(false);
        pages.add(scroll(clinical()),"stage0"); pages.add(scroll(candidates()),"stage1");
        pages.add(scroll(approvals()),"stage2"); pages.add(scroll(finalPage()),"stage3");
        center.add(pages,BorderLayout.CENTER); main.add(center,BorderLayout.CENTER); main.add(footer(),BorderLayout.SOUTH);
        add(main,BorderLayout.CENTER);
        DocumentListener changes = listener(() -> { if (!restoring && !busy && draft != null && !preview) { dirty = true; refreshStatus(); } });
        for (JTextComponent f : fields.values()) f.getDocument().addDocumentListener(changes);
        emergency.addActionListener(e -> { if (!restoring && !busy && !preview && draft != null) { dirty = true; refreshStatus(); } });
        criteriaModel.addTableModelListener(e -> handleCriteriaAnswerChange(e));
        candidateModel.addTableModelListener(e -> handleCandidateSelectionChange(e));
        approvalModel.addTableModelListener(e -> {
            if(restoring||busy||draft==null||preview)return;
            if(e.getColumn()==0 && e.getFirstRow()>=0 && e.getFirstRow()<approvalModel.getRowCount()
                    && Boolean.TRUE.equals(approvalModel.getValueAt(e.getFirstRow(),0))){
                restoring=true;
                try{for(int i=0;i<approvalModel.getRowCount();i++)if(i!=e.getFirstRow())approvalModel.setValueAt(Boolean.FALSE,i,0);}
                finally{restoring=false;}
            }
            dirty=true;refreshStatus();
        });
        initTransportCombo();
        setEditing(false); refresh();
    }
    private void buildNotice() {
        toastPanel.setName("pesan_status");
        toastPanel.setBackground(new Color(236,248,242)); toastPanel.setBorder(BorderFactory.createEmptyBorder(7,12,7,8));
        toastPanel.add(toastText,BorderLayout.CENTER); toastTimer.setRepeats(false);
        toastPanel.setVisible(false);
    }
    private JPanel identity() {
        JPanel area=new JPanel(new BorderLayout(14,0)); area.setOpaque(false); area.setName("baris_judul_pasien");
        JPanel heading=vertical(); heading.setName("judul_rujukan"); heading.setPreferredSize(new Dimension(232,64));
        heading.setBorder(BorderFactory.createEmptyBorder(3,0,3,0)); title.setName("judul_halaman");
        heading.add(title); heading.add(Box.createVerticalStrut(3));
        JTextArea help=readOnly(2); help.setText(""); heading.add(help); area.add(heading,BorderLayout.WEST);

        RoundPanel patient=new RoundPanel(Color.WHITE,LINE,18); patient.setName("kartu_pasien");
        patient.setLayout(new BorderLayout(10,0)); patient.setBorder(BorderFactory.createEmptyBorder(6,12,6,10));
        patient.setPreferredSize(new Dimension(800,64)); patient.setMinimumSize(new Dimension(0,64));
        patient.add(new JLabel(new LineIcon("user",BLUE,26)),BorderLayout.WEST);
        JPanel details=new JPanel(); details.setLayout(new BoxLayout(details,BoxLayout.X_AXIS)); details.setOpaque(false); details.setName("identitas_sebaris");
        name.setName("identitas_nama"); rm.setName("identitas_rm"); rawat.setName("identitas_rawat"); sep.setName("identitas_sep");
        identityCell(details,name,null); identitySeparator(details);
        identityCell(details,rm,copy("No. RM","rm")); identitySeparator(details);
        identityCell(details,rawat,copy("No. Rawat","rawat")); identitySeparator(details);
        identityCell(details,sep,copy("Nomor SEP yang tercatat","sep")); details.add(Box.createHorizontalGlue());
        patient.add(details,BorderLayout.CENTER);

        JPanel tag=new JPanel(new GridBagLayout()); tag.setOpaque(false);
        badge.setName("jenis_kunjungan"); badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createEmptyBorder(3,8,3,8));
        badge.setToolTipText("Jenis layanan mengikuti kunjungan yang dibuka dari daftar pasien."); tag.add(badge);
        patient.add(tag,BorderLayout.EAST); area.add(patient,BorderLayout.CENTER); return area;
    }
    private static void identityCell(JPanel row,JLabel value,JButton copy) {
        JPanel cell=new JPanel(new BorderLayout(4,0)){
            @Override public Dimension getMaximumSize(){return new Dimension(getPreferredSize().width,30);}
        };cell.setOpaque(false);cell.setAlignmentY(.5f);cell.setMinimumSize(new Dimension(copy==null?0:24,30));
        value.setMinimumSize(new Dimension(0,20));cell.add(value,BorderLayout.CENTER);
        if(copy!=null){copy.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));cell.add(copy,BorderLayout.EAST);}
        row.add(cell);
    }
    private static void identitySeparator(JPanel row){
        int height=row.getFontMetrics(BODY).getAscent();JPanel line=new JPanel();line.setName("separator_identitas");line.setBackground(new Color(179,196,210));line.setAlignmentY(.5f);
        Dimension size=new Dimension(1,height);line.setPreferredSize(size);line.setMinimumSize(size);line.setMaximumSize(size);
        row.add(Box.createHorizontalStrut(10));row.add(line);row.add(Box.createHorizontalStrut(10));
    }
    private static Icon copyIcon(){
        java.net.URL resource=SatuSehatRujukanIGDRanapPanel.class.getResource("/picture/copy_biru.png");
        if(resource==null)return null;
        ImageIcon original=new ImageIcon(resource);
        if(original.getIconWidth()<=0||original.getIconHeight()<=0)return null;
        return new ImageIcon(original.getImage().getScaledInstance(16,16,Image.SCALE_SMOOTH));
    }
    private JButton copy(String title,String key) {
        Icon icon=copyIcon();JButton b=icon==null?new JButton("Salin"):new JButton(icon);b.setName("salin_"+key);b.setFont(SMALL);
        b.setToolTipText("Salin "+title); b.getAccessibleContext().setAccessibleName("Salin "+title);
        b.setContentAreaFilled(false); b.setBorder(BorderFactory.createEmptyBorder(2,7,2,5));
        b.addActionListener(e -> {
            if (visit==null) return;
            String text="rm".equals(key)?visit.noRm:"rawat".equals(key)?visit.noRawat:
                "ihs".equals(key)?SatuSehatRujukanIGDRanapDraft.safe(visit.ihs):
                "encounter".equals(key)?SatuSehatRujukanIGDRanapDraft.safe(visit.encounter):String.join(", ",visit.sepNumbers);
            if (text.isEmpty()) { toast("Belum ada nomor yang dapat disalin.",true); return; }
            try { Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text),null); toast(title+" berhasil disalin.",false); }
            catch (RuntimeException ex) { toast("Clipboard sedang tidak tersedia.",true); }
        }); copies.add(b); return b;
    }
    private void buildStages() {
        stageStrip.setOpaque(false); String[] names={"Klinis & kriteria","Kandidat RS","Persetujuan RS","Finalisasi & transfer"};
        for (int i=0;i<4;i++) { final int n=i; JButton b=new JButton(names[i]); b.setName("tahap_"+(i+1)); b.setIcon(new StageNumberIcon(i+1)); b.setIconTextGap(9); b.setUI(new BasicButtonUI()); b.setFont(BODY); b.setHorizontalAlignment(JLabel.LEFT);
            b.setContentAreaFilled(false); b.setToolTipText("Lihat tahap ini. Aksi utama mengikuti alur SATUSEHAT Rujukan."); b.addActionListener(e -> {selectedStage=n;showPage(0);}); stages[i]=b; stageStrip.add(b); }
    }
    private void initTransportCombo(){
        String[] items=new String[TRANSPORT_OPTIONS.length];
        for(int i=0;i<TRANSPORT_OPTIONS.length;i++)items[i]=TRANSPORT_OPTIONS[i][0].isEmpty()?TRANSPORT_OPTIONS[i][1]:TRANSPORT_OPTIONS[i][0]+" - "+TRANSPORT_OPTIONS[i][1];
        transportCombo.setModel(new DefaultComboBoxModel<String>(items));transportCombo.setFont(BODY);transportCombo.setForeground(INK);transportCombo.setBackground(Color.WHITE);
        transportCombo.setMaximumRowCount(10);transportCombo.setName("transport");
        transportCombo.addActionListener(e->{if(!restoring&&!busy&&draft!=null&&!preview){dirty=true;refreshStatus();}});
    }
    private String selectedTransportValue(){
        int i=transportCombo.getSelectedIndex();if(i<=0||i>=TRANSPORT_OPTIONS.length)return "";
        return TRANSPORT_OPTIONS[i][0]+"|"+TRANSPORT_OPTIONS[i][1];
    }
    private void setTransportFromDraft(String value){
        String v=value==null?"":value.trim();int selected=0;
        for(int i=1;i<TRANSPORT_OPTIONS.length;i++)if(v.equals(TRANSPORT_OPTIONS[i][0])||v.startsWith(TRANSPORT_OPTIONS[i][0]+"|")){selected=i;break;}
        transportCombo.setSelectedIndex(selected);
    }
    private void handleCriteriaAnswerChange(javax.swing.event.TableModelEvent e){
        if(restoring||busy||draft==null||preview)return;
        dirty=true;refreshStatus();
        if(e.getColumn()!=javax.swing.event.TableModelEvent.ALL_COLUMNS&&e.getColumn()!=3)return;
        if(selectedPage==0&&selectedStage==0&&allCriteriaAnswered()){
            SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
                if(criteriaTable!=null&&criteriaTable.isEditing()){try{criteriaTable.getCellEditor().stopCellEditing();}catch(Exception ignore){}}
                selectedStage=1;showPage(0);
                toast("Semua jawaban Kriteria SATUSEHAT sudah terisi. Tombol aksi berubah menjadi Cari Faskes dan Tahap 2 - Kandidat Rumah Sakit ditampilkan.",false);
            }});
        }
    }
    private boolean allCriteriaAnswered(){
        if(criteriaModel.getRowCount()==0)return false;
        for(int i=0;i<criteriaModel.getRowCount();i++){
            String type=str(criteriaModel.getValueAt(i,2));
            String answer=str(criteriaModel.getValueAt(i,3)).trim();
            if("boolean".equalsIgnoreCase(type)){
                String normalized=normalizeBooleanAnswer(answer);
                if(!"Ya".equalsIgnoreCase(normalized)&&!"Tidak".equalsIgnoreCase(normalized))return false;
            }else if(answer.isEmpty())return false;
        }
        return true;
    }
    private void applyCandidateFilter(){
        if(candidateSorter==null)return;
        String q=candidateSearchField.getText()==null?"":candidateSearchField.getText().trim();
        if(q.isEmpty()){candidateSorter.setRowFilter(null);return;}
        final String needle=q.toLowerCase(java.util.Locale.ENGLISH);
        candidateSorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel,Integer>(){
            @Override public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel,? extends Integer> entry){
                for(int c=1;c<=7;c++){
                    Object v=entry.getValue(c);
                    if(v!=null&&String.valueOf(v).toLowerCase(java.util.Locale.ENGLISH).contains(needle))return true;
                }
                return false;
            }
        });
    }
    private void focusCandidateSearch(){
        SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
            if(candidateSearchField.isShowing()&&candidateSearchField.isEnabled()){
                candidateSearchField.requestFocusInWindow();candidateSearchField.selectAll();
            }
        }});
    }
    private void handleCandidateSelectionChange(javax.swing.event.TableModelEvent e){
        if(restoring||candidateSelectionAdjusting||busy||draft==null||preview)return;
        if(e.getColumn()==0&&e.getFirstRow()>=0&&e.getFirstRow()<candidateModel.getRowCount()
                && Boolean.TRUE.equals(candidateModel.getValueAt(e.getFirstRow(),0))
                && jenis==SatuSehatRujukanIGDRanapDraft.Jenis.IGD){
            candidateSelectionAdjusting=true;restoring=true;
            try{for(int i=0;i<candidateModel.getRowCount();i++)if(i!=e.getFirstRow())candidateModel.setValueAt(Boolean.FALSE,i,0);}
            finally{restoring=false;candidateSelectionAdjusting=false;}
            dirty=true;refreshStatus();selectedStage=2;showPage(0);toast("Kandidat IGD dipilih. Lanjut ke Kirim Tugas.",false);return;
        }
        dirty=true;refreshStatus();
    }
    private JPanel clinical() {
        // Aksi klinis ditempatkan menempel pada field yang relevan agar alurnya jelas.
        chooseDiagnosis.setText("");
        chooseDiagnosis.setIcon(new LineIcon("search",BLUE,16));
        chooseDiagnosis.setToolTipText("Cari diagnosis ICD-10 dari master penyakit Khanza.");
        chooseDiagnosis.getAccessibleContext().setAccessibleName("Cari Diagnosa");
        openEncounter.setIcon(new LineIcon("plus",BLUE,15));
        createCondition.setIcon(new LineIcon("search",BLUE,15));
        openEncounter.setToolTipText("Buka form SATUSEHAT Encounter lalu muat ulang ID Encounter kunjungan ini.");
        createCondition.setToolTipText("Cari Condition diagnosis yang sudah ada; bila belum ada, buat Condition SATUSEHAT.");
        chooseDiagnosis.addActionListener(e -> showDiseasePicker());
        openEncounter.addActionListener(e -> doOpenEncounter());
        createCondition.addActionListener(e -> doCreateOrFindCondition());
        refreshData.setIcon(new LineIcon("reload",BLUE,16));
        refreshData.setToolTipText("Ambil ulang diagnosis, dokter/DPJP, poli/bangsal, Encounter, IHS, Condition, prosedur dan tanda vital terbaru dari database.");
        refreshData.addActionListener(e -> doRefreshData());

        Vertical all=new Vertical();

        JPanel summary=card("Ringkasan klinis","Tinjau data kunjungan sebelum digunakan."); summary.setName("kartu_ringkasan"); JPanel grid=grid();
        fieldWithIconButtonFixed(grid,"Kode ICD-10","diagnosis_code",chooseDiagnosis,0,0,178,10);
        field(grid,"Nama diagnosis","diagnosis_name",1,0,1,false);
        field(grid,doctorLabel,"doctor",2,0,1,false);
        field(grid,"Lokasi asal / bangsal","origin",3,0,1,false);
        field(grid,"Alasan dan kebutuhan rujukan","reason",0,1,2,true);
        field(grid,"Ringkasan kondisi / keluhan","clinical",2,1,2,true);
        summary.add(grid);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
        all.add(summary);
        all.add(Box.createVerticalStrut(12));

        JPanel confirmation=card(null,null); confirmation.setName("kartu_konfirmasi"); confirmation.add(clinicalTitle); confirmation.add(Box.createVerticalStrut(8)); confirmation.add(clinicalHelp);
        emergency.setFont(SMALL); emergency.setForeground(INK); emergency.setOpaque(false); emergency.setAlignmentX(0); confirmation.add(emergency);
        JPanel need=grid();
        referenceInput(need,"IHS pasien",ihsPatientField,null,0,0,2);
        referenceInput(need,"Encounter",encounterField,openEncounter,2,0,2);
        field(need,"Pelayanan / perawatan yang dibutuhkan","care",0,1,4,false);

        // Wilayah dibuat satu baris: kode dibuat ringkas sesuai panjang kode administratif.
        fieldFixed(need,"Kode Provinsi","province_code",0,2,160,2);
        field(need,"Nama Provinsi","province_display",1,2,1,false);
        fieldFixed(need,"Kode Kab./Kota","city_code",2,2,160,4);
        field(need,"Nama Kab./Kota","city_display",3,2,1,false);

        // System Kelompok Layanan sengaja tidak ditampilkan karena sudah paten.
        // Nilainya tetap disimpan/dikirim melalui DEFAULT_SERVICE_GROUP_SYSTEM.
        fieldFixed(need,"Kode Kelompok Layanan","service_group_code",0,3,160,9);
        field(need,"Nama Kelompok Layanan","service_group_display",1,3,1,false);
        fieldFixed(need,"Kode Clinical Speciality","clinical_speciality_code",2,3,160,8);
        field(need,"Nama Clinical Speciality","clinical_speciality_display",3,3,1,false);

        fieldWithButton(need,"ID Condition diagnosis","condition_id",createCondition,0,4,2);
        field(need,"IHS Practitioner/DPJP","practitioner_ihs",2,4,2,false);
        confirmation.add(need);
        confirmation.add(Box.createVerticalStrut(8));
        diagnosisMappingInfo.setAlignmentX(0); confirmation.add(diagnosisMappingInfo);
        confirmation.add(Box.createVerticalStrut(10));
        confirmation.add(note("Kelompok Layanan dan Clinical Speciality mengikuti diagnosis. Status IGD/kegawatdaruratan tetap mengikuti konteks pelayanan dan Questionnaire Q100, bukan kode diagnosis. Kode terminologi yang belum tervalidasi tidak diisi otomatis.",LIGHT,BLUE));
        confirmation.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
        all.add(confirmation);
        all.add(Box.createVerticalStrut(10));
        all.add(clinicalReferences());
        all.add(Box.createVerticalStrut(12));
        criteriaCard=createCriteriaCard();
        criteriaCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
        all.add(criteriaCard);
        return all;
    }
    private JPanel clinicalReferences(){
        // Detail klinis sengaja disembunyikan dari halaman utama agar form tetap ringkas.
        // Metadata tetap disiapkan supaya dialog "Lihat data klinis" dapat menampilkan detail lengkap.
        if(clinicalCaptions.isEmpty()){
            clinicalValue("blood_pressure","TD");clinicalValue("pulse","Nadi");clinicalValue("respiratory_rate","Napas");
            clinicalValue("spo2","SpO2");clinicalValue("temperature","Suhu");clinicalValue("consciousness","Kesadaran");
            clinicalValue("measured_at","Waktu pengukuran");clinicalValue("therapy","Terapi");clinicalValue("procedures","Prosedur / ICD-9-CM");
            clinicalValue("allergies","Alergi");clinicalValue("supporting_documents","Dokumen pendukung");
        }
        JPanel p=flow();p.setName("aksi_lihat_data_klinis");
        clinicalDetails.setName("detail_klinis");clinicalDetails.setIcon(new LineIcon("eye",BLUE,16));
        clinicalDetails.setBorder(BorderFactory.createEmptyBorder(7,10,7,10));
        clinicalDetails.addActionListener(e -> showClinicalDetails());p.add(clinicalDetails);
        refreshData.setName("refresh_data");refreshData.setBorder(BorderFactory.createEmptyBorder(7,10,7,10));p.add(refreshData);
        return p;
    }
    private void referenceInput(JPanel grid,String text,JTextField value,JButton action,int x,int y,int span){
        JPanel box=vertical();JLabel l=label(text,SMALL,MUTED);box.add(l);box.add(Box.createVerticalStrut(6));
        styleInput(value);value.setEditable(false);value.setFocusable(true);value.setName("ref_"+text.toLowerCase().replace(' ','_'));
        InputFrame frame=new InputFrame(value);frame.setAlignmentX(0);frame.add(value,BorderLayout.CENTER);
        if(action!=null){JPanel east=new JPanel(new BorderLayout());east.setOpaque(false);action.setPreferredSize(new Dimension(action.getText()==null||action.getText().trim().isEmpty()?36:Math.max(118,action.getPreferredSize().width),34));east.add(action,BorderLayout.CENTER);frame.add(east,BorderLayout.EAST);}
        frame.setPreferredSize(new Dimension(100,36));frame.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));box.add(frame);
        int h=box.getPreferredSize().height;box.setPreferredSize(new Dimension(100*span,h));box.setMinimumSize(new Dimension(70*span,h));
        GridBagConstraints c=new GridBagConstraints();c.gridx=x;c.gridy=y;c.gridwidth=span;c.weightx=span;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;c.insets=new Insets(9,0,4,10);grid.add(box,c);
    }
    private JLabel clinicalValue(String key,String caption){
        JLabel l=label(caption+": —",SMALL,MUTED);l.setName("label_"+key);l.setMinimumSize(new Dimension(0,21));
        l.setPreferredSize(new Dimension(120,21));l.setMaximumSize(new Dimension(Integer.MAX_VALUE,21));
        clinicalValues.put(key,l);clinicalCaptions.put(key,caption);return l;
    }
    private void updateClinicalLabels(){
        ihsValue.setText("IHS pasien: "+or(visit.ihs,"belum tersedia"));ihsValue.setToolTipText(ihsValue.getText());
        encounterValue.setText("Encounter: "+or(visit.encounter,"belum tersedia"));encounterValue.setToolTipText(encounterValue.getText());
        ihsPatientField.setText(or(visit.ihs,"belum tersedia"));ihsPatientField.setToolTipText(ihsPatientField.getText());
        encounterField.setText(or(visit.encounter,"belum tersedia"));encounterField.setToolTipText(encounterField.getText());
        for(Map.Entry<String,JLabel> entry:clinicalValues.entrySet()){
            String key=entry.getKey(),value=draft.get(key),unit="";
            if("blood_pressure".equals(key)&&value.matches("[0-9]+\\s*/\\s*[0-9]+"))unit=" mmHg";
            else if(value.matches("[0-9]+([.,][0-9]+)?")){
                if("pulse".equals(key)||"respiratory_rate".equals(key))unit=" /menit";
                else if("spo2".equals(key))unit=" %";else if("temperature".equals(key))unit=" °C";
            }
            String text=clinicalCaptions.get(key)+": "+or(value,"—")+unit;
            JLabel l=entry.getValue();l.putClientProperty("html.disable",Boolean.TRUE);l.setText(text.replaceAll("\\s+"," "));
            l.setToolTipText(text);l.getAccessibleContext().setAccessibleDescription(text);
        }
        clinicalSource.setText(preview?"Data fiktif untuk pratinjau.":"Sumber klinis: draf/database lokal. Workflow API aktif pada Tahap II; data klinis pendukung tidak otomatis dibuat ulang.");
        clinicalSource.setToolTipText("Nilai kosong berarti belum tersedia pada sumber lokal yang dimuat. Buka Lihat detail untuk sumber dan catatan lengkap.");
        loadingNotes.setVisible(!visit.warnings.isEmpty());
        loadingNote.setText(visit.warnings.size()+" catatan pemuatan data — buka Lihat detail.");loadingNote.setToolTipText(String.join("; ",visit.warnings));
    }
    private void showClinicalDetails(){
        if(visit==null||draft==null)return;
        StringBuilder text=new StringBuilder("IHS pasien: ").append(or(visit.ihs,"belum tersedia")).append("\nEncounter: ").append(or(visit.encounter,"belum tersedia")).append("\n\n");
        for(Map.Entry<String,String> entry:clinicalCaptions.entrySet())text.append(entry.getValue()).append(":\n").append(or(draft.get(entry.getKey()),"Belum tersedia pada data yang dimuat.")).append("\n\n");
        text.append("CATATAN PEMUATAN DATA\n").append(sources.getText());
        JTextArea detail=readOnly(16);detail.setText(text.toString());detail.setCaretPosition(0);
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(this),"Detail data klinis",Dialog.ModalityType.APPLICATION_MODAL);
        JPanel content=new JPanel(new BorderLayout(0,12));content.setBackground(Color.WHITE);content.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        content.add(scroll(detail),BorderLayout.CENTER);JButton close=button("Tutup",BLUE,Color.WHITE);close.addActionListener(e -> dialog.dispose());content.add(close,BorderLayout.SOUTH);
        dialog.setContentPane(content);dialog.setSize(Math.min(760,Math.max(420,getWidth()-40)),Math.min(580,Math.max(320,getHeight()-40)));dialog.setLocationRelativeTo(this);dialog.setVisible(true);
    }
    /**
     * Refresh sumber data tanpa memakai isi payload draf lama. Data yang berasal dari
     * rekam medis/registrasi ditarik ulang, sedangkan catatan manual rujukan tetap
     * dipertahankan. Workflow lokal direset supaya Task lama tidak dipakai dengan
     * diagnosis/Encounter yang sudah berubah.
     */
    private void doRefreshData(){
        if(preview||busy||visit==null||draft==null)return;
        if(workflowStarted()){
            int ok=JOptionPane.showConfirmDialog(this,
                    "Refresh Data akan mengambil ulang data klinis/registrasi terbaru dan mereset state workflow lokal (Task/Kriteria/Kandidat/CarePlan) agar tidak memakai data lama.\n\nResource yang sudah pernah terkirim ke SATUSEHAT tidak dihapus. Lanjutkan?",
                    "Refresh Data",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(ok!=JOptionPane.YES_OPTION)return;
        }
        final String rawat=visit.noRawat;final SatuSehatRujukanIGDRanapDraft.Jenis kind=jenis;
        setBusy(true,"Mengambil ulang diagnosis, dokter, lokasi, Encounter dan tanda vital...");
        new SwingWorker<SatuSehatRujukanIGDRanapRepository.Visit,Void>(){
            @Override protected SatuSehatRujukanIGDRanapRepository.Visit doInBackground()throws Exception{return repository.loadVisitFresh(rawat,kind);}
            @Override protected void done(){try{applyRefreshedVisit(get());setBusy(false,"Data sumber terbaru berhasil dimuat.");
                toast("Refresh Data berhasil. Diagnosis, dokter/DPJP, poli/bangsal, Encounter, IHS, Condition, prosedur dan tanda vital dimuat ulang. Workflow lokal direset agar Cek Kriteria membuat Task yang sesuai data terbaru.",false);
                if(!preview&&draftAvailable&&!operator.isEmpty())saveDraft(null,false);
            }catch(Exception ex){setBusy(false,"Refresh Data gagal. Data sebelumnya tetap tersedia.");failure("Refresh Data",ex);}}
        }.execute();
    }
    private void applyRefreshedVisit(SatuSehatRujukanIGDRanapRepository.Visit fresh){
        if(fresh==null||fresh.draft==null||draft==null)return;
        restoring=true;
        try{
            SatuSehatRujukanIGDRanapDraft keep=draft.copy();
            String[] sourceKeys={"diagnosis_code","diagnosis_name","secondary_diagnosis_code","secondary_diagnosis_name","doctor","origin","procedures","condition_id","secondary_condition_id","practitioner_ihs","blood_pressure","pulse","respiratory_rate","spo2","temperature","consciousness","measured_at","clinical","allergies"};
            for(String k:sourceKeys)keep.set(k,fresh.draft.get(k));
            // Mapping diagnosis harus dihitung ulang dari diagnosis terbaru, bukan diwarisi dari draf.
            keep.set("service_group_system",DEFAULT_SERVICE_GROUP_SYSTEM);keep.set("service_group_code","");keep.set("service_group_display","");
            keep.set("clinical_speciality_code","");keep.set("clinical_speciality_display","");
            // State workflow lokal selalu dikosongkan pada refresh eksplisit.
            String[] workflowKeys={"criteria_state_json","candidate_state_json","approval_state_json","pre_request_signature","candidate_query_signature","task_pre_id","task_candidate_id","task_candidate_response_id","careplan_id","service_request_id","national_referral_number","selected_org_id","selected_org_name","selected_approval_task_id"};
            for(String k:workflowKeys)keep.set(k,"");
            visit=fresh;draft=keep;visit.draft=keep.copy();draftAvailable=fresh.draftAvailable;
            name.setText(fresh.name);rm.setText("No. RM  "+fresh.noRm);rawat.setText("No. Rawat  "+fresh.noRawat);
            String sepText=String.join(", ",fresh.sepNumbers);sep.setText(fresh.sepNumbers.isEmpty()?"SEP belum tercatat":fresh.sepNumbers.size()>1?"SEP: "+fresh.sepNumbers.size()+" nomor tercatat":"SEP  "+sepText);
            for(Map.Entry<String,JTextComponent> f:fields.entrySet()){f.getValue().setText(draft.get(f.getKey()));f.getValue().setCaretPosition(0);}
            setTransportFromDraft(draft.get("transport"));applyDefaultReferralArea();applyDiagnosisMapping(true,false);
            emergency.setSelected("true".equals(draft.get("emergency_confirmed")));
            restoreWorkflowState();selectedStage=0;showPage(0);updateClinicalLabels();updateWorkflowLabels();
            String note="Data sumber direfresh langsung dari database, bukan dari payload draf lama. Diagnosis/dokter/lokasi/Encounter/IHS/Condition/prosedur/tanda vital memakai nilai terbaru yang tersedia. Workflow lokal direset pada refresh agar Task lama tidak dipakai untuk data baru.";
            if(!fresh.warnings.isEmpty())note+="\n\n"+String.join("\n",fresh.warnings);sources.setText(note);sources.setCaretPosition(0);
            dirty=true;refreshStatus();refresh();
        }finally{restoring=false;}
    }

    private boolean workflowStarted(){
        if(draft==null)return false;
        return !draft.get("task_pre_id").isEmpty()||!draft.get("task_candidate_id").isEmpty()||!draft.get("careplan_id").isEmpty()||!draft.get("service_request_id").isEmpty();
    }
    private void resetWorkflowForClinicalChange(String reason){
        if(draft==null)return;
        String[] keys={"criteria_state_json","candidate_state_json","approval_state_json","pre_request_signature","candidate_query_signature","task_pre_id","task_candidate_id","task_candidate_response_id","careplan_id","service_request_id","national_referral_number","selected_org_id","selected_org_name","selected_approval_task_id"};
        for(String k:keys)draft.set(k,"");
        restoring=true;try{criteriaModel.setRowCount(0);candidateModel.setRowCount(0);approvalModel.setRowCount(0);criteriaGroupId.clear();criteriaGroupText.clear();}finally{restoring=false;}
        selectedStage=0;dirty=true;updateWorkflowLabels();refresh();toast(reason+" Workflow rujukan lokal direset agar Task lama tidak dipakai untuk data klinis baru.",true);
    }
    private void showDiseasePicker(){
        if(preview||busy||visit==null||draft==null)return;
        final JDialog d=new JDialog(SwingUtilities.getWindowAncestor(this),"Pilih Diagnosis ICD-10",Dialog.ModalityType.APPLICATION_MODAL);
        final DefaultTableModel m=model(new String[]{"Kode ICD-10","Nama Penyakit"});
        final JTable table=workflowTable(m);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        final JTextField q=new JTextField();styleInput(q);InputFrame qFrame=new InputFrame(q);qFrame.add(q,BorderLayout.CENTER);qFrame.setPreferredSize(new Dimension(360,36));
        final JButton search=button("Cari",BLUE,Color.WHITE);final JButton choose=button("Pilih",BLUE,Color.WHITE);final JLabel info=label("Ketik kode atau nama penyakit.",SMALL,MUTED);
        JPanel top=new JPanel(new BorderLayout(8,0));top.setOpaque(false);top.add(qFrame,BorderLayout.CENTER);top.add(search,BorderLayout.EAST);
        JPanel bottom=new JPanel(new BorderLayout(8,0));bottom.setOpaque(false);bottom.add(info,BorderLayout.CENTER);JPanel ba=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));ba.setOpaque(false);JButton close=button("Batal",Color.WHITE,MUTED);ba.add(close);ba.add(choose);bottom.add(ba,BorderLayout.EAST);
        JPanel content=new JPanel(new BorderLayout(0,10));content.setBackground(Color.WHITE);content.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));content.add(top,BorderLayout.NORTH);content.add(scroll(table),BorderLayout.CENTER);content.add(bottom,BorderLayout.SOUTH);d.setContentPane(content);d.setSize(760,500);d.setLocationRelativeTo(this);
        final Runnable load=new Runnable(){@Override public void run(){
            search.setEnabled(false);info.setText("Mencari...");final String key=q.getText().trim();
            new SwingWorker<List<SatuSehatRujukanIGDRanapRepository.DiseaseItem>,Void>(){
                @Override protected List<SatuSehatRujukanIGDRanapRepository.DiseaseItem> doInBackground()throws Exception{return repository.searchDiseases(key,200);}
                @Override protected void done(){try{List<SatuSehatRujukanIGDRanapRepository.DiseaseItem> rows=get();m.setRowCount(0);for(SatuSehatRujukanIGDRanapRepository.DiseaseItem x:rows)m.addRow(new Object[]{x.code,x.name});fitTableColumns(table,18,0);info.setText(rows.size()+" diagnosis ditemukan.");}
                    catch(Exception ex){info.setText("Pencarian gagal: "+or(ex.getMessage(),"database tidak tersedia"));}finally{search.setEnabled(true);}}
            }.execute();
        }};
        search.addActionListener(e->load.run());q.addActionListener(e->load.run());close.addActionListener(e->d.dispose());
        choose.addActionListener(e->{int vr=table.getSelectedRow();if(vr<0){info.setText("Pilih satu diagnosis terlebih dahulu.");return;}int r=table.convertRowIndexToModel(vr);String code=str(m.getValueAt(r,0)),nm=str(m.getValueAt(r,1));
            String old=value("diagnosis_code");boolean resetNeeded=!old.equals(code)&&workflowStarted();if(resetNeeded){
                int ok=JOptionPane.showConfirmDialog(d,"Diagnosis rujukan berubah. Task/CarePlan lokal yang sudah terbentuk harus direset agar tidak memakai diagnosis lama.\nLanjutkan?","Ganti diagnosis",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);if(ok!=JOptionPane.YES_OPTION)return;
            }
            fields.get("diagnosis_code").setText(code);fields.get("diagnosis_name").setText(nm);fields.get("condition_id").setText("");visit.diagnosisCode=code;visit.diagnosisName=nm;visit.conditionId="";
            if(resetNeeded)resetWorkflowForClinicalChange("Diagnosis diubah.");
            applyDiagnosisMapping(true,true);dirty=true;d.dispose();lookupConditionForCurrentDiagnosis();
        });
        table.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2)choose.doClick();}});
        load.run();d.setVisible(true);
    }
    private void lookupConditionForCurrentDiagnosis(){
        if(visit==null||repository==null)return;final String rawat=visit.noRawat,dx=value("diagnosis_code");if(dx.isEmpty())return;
        new SwingWorker<String,Void>(){
            @Override protected String doInBackground()throws Exception{return repository.findConditionId(rawat,dx);}
            @Override protected void done(){try{String id=get();if(!id.isEmpty()){fields.get("condition_id").setText(id);visit.conditionId=id;dirty=true;toast("Condition existing ditemukan dan dipakai ulang: "+id,false);}else toast("Diagnosis dipilih. Condition belum ada; gunakan Cari Condition.",false);}catch(Exception ex){toast("Diagnosis dipilih, tetapi pengecekan Condition lokal gagal.",true);}}
        }.execute();
    }
    private void doOpenEncounter(){
        if(preview||busy||visit==null)return;
        try{
            java.awt.Window owner=SwingUtilities.getWindowAncestor(this);java.awt.Frame frame=owner instanceof java.awt.Frame?(java.awt.Frame)owner:null;
            Class<?> cls=Class.forName("bridging.SatuSehatKirimEncounter");java.lang.reflect.Constructor<?> ctor=cls.getConstructor(java.awt.Frame.class,boolean.class);
            Object obj=ctor.newInstance(frame,Boolean.TRUE);
            if(obj instanceof java.awt.Dialog){java.awt.Dialog dlg=(java.awt.Dialog)obj;int w=owner==null?1280:Math.max(1180,(int)(owner.getWidth()*.95));int h=owner==null?720:Math.max(700,(int)(owner.getHeight()*.95));dlg.setSize(w,h);dlg.setLocationRelativeTo(owner);dlg.setVisible(true);}
            refreshEncounterReference();
        }catch(Exception ex){toast("Form Satu Sehat Kirim Encounter tidak dapat dibuka: "+or(ex.getMessage(),ex.getClass().getSimpleName()),true);}
    }
    private void refreshEncounterReference(){
        if(visit==null)return;final String rawat=visit.noRawat,old=or(visit.encounter,"");setBusy(true,"Memeriksa Encounter terbaru...");
        new SwingWorker<String,Void>(){
            @Override protected String doInBackground()throws Exception{return repository.findEncounterId(rawat);}
            @Override protected void done(){try{String id=get();if(id.isEmpty()){setBusy(false,"Encounter belum ditemukan.");toast("Encounter untuk no.rawat ini belum tersimpan di satu_sehat_encounter.",true);return;}
                if(!old.isEmpty()&&!old.equals(id)&&workflowStarted())resetWorkflowForClinicalChange("Encounter berubah.");visit.encounter=id;encounterValue.setText("Encounter: "+id);encounterValue.setToolTipText(encounterValue.getText());encounterField.setText(id);encounterField.setToolTipText(id);setBusy(false,"Encounter tersedia: "+id);toast("Encounter berhasil diperbarui dari database.",false);
            }catch(Exception ex){setBusy(false,"Pengecekan Encounter gagal.");failure("Memuat Encounter",ex);}}
        }.execute();
    }
    private static final class ConditionResult{String id="";boolean created;JsonNode response;}
    private void doCreateOrFindCondition(){
        if(preview||busy||visit==null||draft==null)return;
        final String dx=value("diagnosis_code"),dxName=value("diagnosis_name");if(dx.isEmpty()){toast("Pilih/isi diagnosis ICD-10 terlebih dahulu.",true);return;}
        if(visit.ihs==null||visit.ihs.trim().isEmpty()){toast("IHS pasien belum tersedia.",true);return;}if(visit.encounter==null||visit.encounter.trim().isEmpty()){toast("Encounter belum tersedia. Gunakan Buka Encounter terlebih dahulu.",true);return;}
        final String rawat=visit.noRawat,patient=visit.ihs,encounter=visit.encounter,patientName=visit.name;setBusy(true,"Mencari Condition diagnosis...");
        new SwingWorker<ConditionResult,Void>(){
            @Override protected ConditionResult doInBackground()throws Exception{ConditionResult out=new ConditionResult();out.id=repository.findConditionId(rawat,dx);if(!out.id.isEmpty())return out;
                out.response=apiClient.kirimCondition(patient,encounter,patientName,dx,dxName);out.id=responseId(out.response);if(out.id.isEmpty())throw new IllegalStateException("SATUSEHAT tidak mengembalikan ID Condition.");repository.cacheConditionId(rawat,dx,jenis,out.id);out.created=true;return out;}
            @Override protected void done(){try{ConditionResult r=get();if(r.created)rememberApi("Buat Condition diagnosis");fields.get("condition_id").setText(r.id);visit.conditionId=r.id;dirty=true;setBusy(false,"Condition: "+r.id);if(r.created)persistApiSuccess("Condition diagnosis berhasil dibuat dan disimpan ke cache lokal.");else{toast("Condition existing ditemukan dan dipakai ulang: "+r.id,false);refreshStatus();}}
                catch(Exception ex){if(apiClient!=null&&!apiClient.getLastUrl().isEmpty())rememberApi("Buat Condition diagnosis",true);setBusy(false,"Condition gagal diproses.");failure("Cari Condition",ex);}}
        }.execute();
    }
    private JPanel candidates() {
        Vertical p=new Vertical();
        RoundPanel candidatePanel=new RoundPanel(Color.WHITE,LINE,18);candidatePanel.setLayout(new BoxLayout(candidatePanel,BoxLayout.Y_AXIS));candidatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);candidatePanel.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        candidateCard=candidatePanel;
        candidateCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));

        JPanel candidateHeader=new JPanel(new BorderLayout(12,0));candidateHeader.setOpaque(false);candidateHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        candidateHeader.add(label("Kandidat Rumah Sakit",STRONG,INK),BorderLayout.WEST);
        styleInput(candidateSearchField);candidateSearchField.setName("cari_kandidat_rs");candidateSearchField.setToolTipText("Ketik nama RS, Org ID, kode BPJS, strata, jarak, estimasi, atau informasi bed untuk memfilter kandidat secara langsung.");
        candidateSearchField.getAccessibleContext().setAccessibleName("Cari RS kandidat");
        InputFrame searchFrame=new InputFrame(candidateSearchField);searchFrame.add(candidateSearchField,BorderLayout.CENTER);searchFrame.setPreferredSize(new Dimension(360,34));searchFrame.setMinimumSize(new Dimension(260,34));searchFrame.setMaximumSize(new Dimension(420,34));candidateHeader.add(searchFrame,BorderLayout.EAST);
        candidateCard.add(candidateHeader);candidateCard.add(Box.createVerticalStrut(7));
        JTextArea candidateSub=readOnly(1);candidateSub.setText("Kandidat berasal dari output Task request-referral-candidate. IGD hanya memilih satu RS; Rawat Inap dapat memilih beberapa kandidat sebelum Kirim Tugas.");candidateCard.add(candidateSub);candidateCard.add(Box.createVerticalStrut(8));

        JPanel candidateTop=new JPanel(new BorderLayout(10,0));candidateTop.setOpaque(false);candidateTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        candidateTaskIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);candidateTop.add(candidateTaskIdLabel,BorderLayout.CENTER);
        candidateCard.add(candidateTop);candidateCard.add(Box.createVerticalStrut(8));

        candidateTable=workflowTable(candidateModel); candidateTable.setName("tabel_kandidat");
        candidateSorter=new javax.swing.table.TableRowSorter<DefaultTableModel>(candidateModel);candidateTable.setRowSorter(candidateSorter);
        candidateSearchField.getDocument().addDocumentListener(listener(() -> applyCandidateFilter()));
        candidateTable.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){
            int viewRow=candidateTable.rowAtPoint(e.getPoint()),viewCol=candidateTable.columnAtPoint(e.getPoint());
            if(viewRow<0||viewCol==0)return;
            int modelRow=candidateTable.convertRowIndexToModel(viewRow);
            if(modelRow>=0&&modelRow<candidateModel.getRowCount()&&!Boolean.TRUE.equals(candidateModel.getValueAt(modelRow,0))){
                candidateModel.setValueAt(Boolean.TRUE,modelRow,0);
            }
        }});
        candidateScrollPane=scroll(candidateTable);candidateScrollPane.setPreferredSize(new Dimension(980,250));candidateScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);candidateCard.add(candidateScrollPane);
        candidateCard.add(Box.createVerticalStrut(10));candidateCard.add(note("Ketik beberapa karakter pada kotak pencarian untuk memfilter kandidat secara langsung. Klik baris rumah sakit untuk otomatis mencentang kolom Pilih. Bed, jarak, strata, dan estimasi hanya ditampilkan bila dikembalikan SATUSEHAT.",LIGHT,BLUE));
        p.add(candidateCard);
        SwingUtilities.invokeLater(new Runnable(){@Override public void run(){fitTableColumns(candidateTable,18,0);}});
        return p;
    }
    private JPanel createCriteriaCard(){
        JPanel criteria=card("Kriteria SATUSEHAT","Pertanyaan di bawah diambil dari Questionnaire pada respons Task pra-permintaan. Isi kolom Jawaban sebelum Cari Faskes.");
        criteria.setAlignmentX(Component.LEFT_ALIGNMENT);
        criteriaTaskIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        criteria.add(criteriaTaskIdLabel);criteria.add(Box.createVerticalStrut(8));
        criteriaTable=workflowTable(criteriaModel);criteriaTable.setName("tabel_kriteria");criteriaTable.setFillsViewportHeight(false);
        installCriteriaAnswerEditor(criteriaTable);
        // Kolom Tipe tetap dipertahankan pada model sebagai metadata internal untuk editor/payload,
        // tetapi tidak ditampilkan ke user agar tabel kriteria lebih ringkas.
        if(criteriaTable.getColumnModel().getColumnCount()>2)criteriaTable.removeColumn(criteriaTable.getColumnModel().getColumn(2));
        criteriaScrollPane=scroll(criteriaTable);criteriaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);criteriaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        criteriaScrollPane.setPreferredSize(new Dimension(980,80));criteriaScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);criteria.add(criteriaScrollPane);
        SwingUtilities.invokeLater(new Runnable(){@Override public void run(){fitTableColumns(criteriaTable,18,560);resizeCriteriaCard();}});
        return criteria;
    }
    private JPanel approvals() {
        Vertical p=new Vertical(); JPanel c=card("Persetujuan rumah sakit tujuan","Task referral-approval-request dibuat per kandidat. Status keputusan dibaca dari Task.output response-referral-task.");
        approvalTable=workflowTable(approvalModel);approvalTable.setName("tabel_persetujuan");
        JScrollPane sp=scroll(approvalTable);sp.setPreferredSize(new Dimension(900,250));c.add(sp);
        c.add(Box.createVerticalStrut(10));c.add(note("Setelah RS tujuan mengembalikan accepted, bila hanya satu RS yang menerima maka sistem otomatis menjadikannya tujuan final. Jika lebih dari satu RS accepted, pilih tepat satu pada kolom Final.",LIGHT,BLUE));
        p.add(c); return p;
    }
    private JPanel finalPage() {
        Vertical p=new Vertical();
        JPanel c=card("Finalisasi rujukan","Setelah satu RS tujuan menerima rujukan, tahap ini langsung mengirim ServiceRequest ke SATUSEHAT.");
        workflowIdsLabel.setAlignmentX(0);finalStatusLabel.setAlignmentX(0);
        c.add(workflowIdsLabel);c.add(Box.createVerticalStrut(5));c.add(finalStatusLabel);c.add(Box.createVerticalStrut(12));
        c.add(note("Tidak ada isian tambahan pada tahap ini. Jika hanya satu RS berstatus accepted, sistem otomatis menjadikannya tujuan final. Klik Finalisasi Rujukan untuk mengirim ServiceRequest ke SATUSEHAT.",LIGHT,BLUE));
        p.add(c);p.add(Box.createVerticalStrut(12));

        finalResultCard=new RoundPanel(Color.WHITE,LINE,18);
        finalResultCard.setLayout(new BoxLayout(finalResultCard,BoxLayout.Y_AXIS));
        finalResultCard.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        finalResultCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        finalResultCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
        finalResultCard.setVisible(false);
        finalResultTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        finalNationalNumber.setAlignmentX(Component.LEFT_ALIGNMENT);
        finalTargetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        finalServiceRequestLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        finalResultCard.add(finalResultTitle);finalResultCard.add(Box.createVerticalStrut(8));
        finalResultCard.add(label("Nomor Rujukan Nasional",SMALL,MUTED));finalResultCard.add(Box.createVerticalStrut(3));
        finalResultCard.add(finalNationalNumber);finalResultCard.add(Box.createVerticalStrut(9));
        finalResultCard.add(finalTargetLabel);finalResultCard.add(Box.createVerticalStrut(4));
        finalResultCard.add(finalServiceRequestLabel);finalResultCard.add(Box.createVerticalStrut(12));
        JPanel actions=flow();
        copyNationalReferral.setIcon(new LineIcon("copy",BLUE,15));
        copyNationalReferral.setToolTipText("Salin Nomor Rujukan Nasional ke clipboard.");
        copyNationalReferral.addActionListener(e -> copyNationalReferralNumber());
        printReferral.setIcon(new LineIcon("print",Color.WHITE,15));
        printReferral.setToolTipText("Buka report Surat Rujukan SATUSEHAT.");
        printReferral.addActionListener(e -> doCetakRujukan());
        actions.add(copyNationalReferral);actions.add(Box.createHorizontalStrut(8));actions.add(printReferral);
        finalResultCard.add(actions);
        p.add(finalResultCard);
        return p;
    }

    private JPanel footer() {
        JPanel p=new JPanel(new BorderLayout(12,0));p.setName("tombol_bawah");p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,LINE),BorderFactory.createEmptyBorder(10,0,0,0)));
        leftActions.setName("aksi_kiri");leftActions.setOpaque(false);p.add(leftActions,BorderLayout.WEST);
        exit.setToolTipText("Keluar ke daftar pasien (Esc)");exit.addActionListener(e -> {if(onClose!=null)requestClose(onClose);});
        save.setIcon(new LineIcon("save",Color.WHITE,16));save.addActionListener(e -> saveDraft(null));footerAction(save);footerAction(exit);
        JPanel state=new JPanel(new BorderLayout(8,0));state.setOpaque(false);state.add(saveStatus,BorderLayout.WEST);state.add(footerStatus,BorderLayout.CENTER);p.add(state,BorderLayout.CENTER);
        JButton api=button("Respon API",Color.WHITE,BLUE);api.setName("respon_api");api.setIcon(new LineIcon("code",BLUE,16));api.addActionListener(e -> showApi());
        p.add(footerAction(api),BorderLayout.EAST);return p;
    }
    private static JButton footerAction(JButton b){
        Dimension size=new Dimension(b.getPreferredSize().width,36);b.setPreferredSize(size);b.setMinimumSize(size);b.setMaximumSize(size);return b;
    }
    private boolean matchesLoaded() { return draft!=null && visit!=null && draft.noRawat.equals(visit.noRawat) && draft.noRm.equals(visit.noRm) && draft.jenis==jenis; }
    private void refreshStatus() {
        saveStatus.setText(busy?"Memproses...":preview?"Pratinjau tampilan":draft==null?"Belum ada draf":!matchesLoaded()?"Periksa kunjungan":dirty?"Belum disimpan":draft.version>0?"Draf tersimpan / v"+draft.version:"Draf baru");
        save.setEnabled(!preview&&!busy&&draftAvailable&&matchesLoaded()&&(dirty||draft.version==0));
    }
    private void refresh() {
        SatuSehatRujukanIGDRanapDraft.Jenis shownKind=visit==null&&requestedJenis!=null?requestedJenis:jenis;
        badge.setText(shownKind.label);badge.setVisible(visit!=null||requestedJenis!=null);
        for(int i=0;i<4;i++){stages[i].setForeground(i==selectedStage?BLUE:MUTED);stages[i].setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,3,0,i==selectedStage?BLUE:LINE),BorderFactory.createEmptyBorder(8,4,12,4)));}
        boolean igd=jenis==SatuSehatRujukanIGDRanapDraft.Jenis.IGD;doctorLabel.setText(igd?"Dokter pengirim":"DPJP pengirim (verifikasi)");
        clinicalTitle.setText(igd?"Konfirmasi kegawatdaruratan":"Kebutuhan rawat inap");clinicalHelp.setText(igd?"Penilaian kegawatdaruratan ditetapkan dokter.":"Tinjau jenis perawatan dan kemampuan RS tujuan.");emergency.setVisible(igd);
        stageStrip.setVisible(selectedPage==0);leftActions.removeAll();
        if(selectedPage==0){
            leftActions.add(save);
            String actionText=selectedStage==0?"Cek Kriteria":selectedStage==1?"Cari Faskes":selectedStage==2?(approvalModel.getRowCount()==0?"Kirim Tugas":"Cek Status"):"Finalisasi Rujukan";
            JButton action=button(actionText,BLUE,Color.WHITE);
            action.setEnabled(visit!=null&&!busy&&!preview&&draftAvailable);
            action.addActionListener(e -> {
                if(selectedStage==0)doCekKriteria();
                else if(selectedStage==1)doCariKandidat();
                else if(selectedStage==2)doApproval();
                else doFinalisasi();
            });
            leftActions.add(footerAction(action));
        }
        leftActions.add(exit);
        refreshStatus();revalidate();repaint();
    }
    private void setEditing(boolean enabled) {
        for(JTextComponent f:fields.values())f.setEnabled(enabled);emergency.setEnabled(enabled);transportCombo.setEnabled(enabled);
        if(criteriaTable!=null)criteriaTable.setEnabled(enabled);if(candidateTable!=null)candidateTable.setEnabled(enabled);if(approvalTable!=null)approvalTable.setEnabled(enabled);
        clinicalDetails.setEnabled(visit!=null&&!busy);chooseDiagnosis.setEnabled(visit!=null&&!busy&&!preview);openEncounter.setEnabled(visit!=null&&!busy&&!preview);createCondition.setEnabled(visit!=null&&!busy&&!preview);findCandidate.setEnabled(visit!=null&&!busy&&!preview&&candidateModel.getRowCount()>0);candidateSearchField.setEnabled(visit!=null&&!busy&&candidateModel.getRowCount()>0);
        for(JButton b:copies)b.setEnabled(visit!=null&&!busy);refreshStatus();
    }
    private void setBusy(boolean value,String message){busy=value;footerStatus.setText(message);footerStatus.setToolTipText(message);setEditing(visit!=null&&!busy);refresh();}
    public boolean isBusy(){return busy;}
    public boolean isDirty(){return dirty;}
    public SatuSehatRujukanIGDRanapDraft.Jenis getJenis(){return jenis;}
    public void setOperator(String code){operator=SatuSehatRujukanIGDRanapDraft.safe(code);}
    public void setOnClose(Runnable close){onClose=close;}
    public void setJenis(final SatuSehatRujukanIGDRanapDraft.Jenis kind){
        if(!SwingUtilities.isEventDispatchThread()){SwingUtilities.invokeLater(() -> setJenis(kind));return;}
        if(kind==null)throw new IllegalArgumentException("Jenis wajib tersedia.");
        if(busy||kind==jenis){refresh();return;}if(dirty&&!confirmDiscard()){refresh();return;}
        if(preview){showPreview(kind);return;}if(visit!=null)loadInternal(visit.noRawat,kind);else{jenis=kind;refresh();}
    }
    public void loadVisit(final String noRawat,final SatuSehatRujukanIGDRanapDraft.Jenis kind){
        if(!SwingUtilities.isEventDispatchThread()){SwingUtilities.invokeLater(() -> loadVisit(noRawat,kind));return;}
        if(preview||busy)return;if(dirty&&!confirmDiscard())return;loadInternal(noRawat,kind);
    }
    private void loadInternal(String input,final SatuSehatRujukanIGDRanapDraft.Jenis kind){
        final String id=SatuSehatRujukanIGDRanapDraft.safe(input);if(id.isEmpty()){toast("Pilih kunjungan dari daftar pasien terlebih dahulu.",true);return;}
        requestedJenis=kind;
        setBusy(true,"Memuat kunjungan dan draf...");
        new SwingWorker<SatuSehatRujukanIGDRanapRepository.Visit,Void>(){
            @Override protected SatuSehatRujukanIGDRanapRepository.Visit doInBackground()throws Exception{return repository.loadVisit(id,kind);}
            @Override protected void done(){try{applyVisit(get());setBusy(false,draft.version>0?"Draf dipulihkan dari database.":"Data kunjungan dimuat. Lengkapi kebutuhan rujukan.");
                toast(visit.warnings.isEmpty()?"Data kunjungan dan draf berhasil dimuat.":"Data kunjungan dimuat dengan catatan. Periksa Catatan pemuatan data di bawah form.",!visit.warnings.isEmpty());
            }catch(Exception ex){setBusy(false,"Pemuatan belum berhasil. Data sebelumnya tetap tersedia.");failure("Memuat kunjungan",ex);}}
        }.execute();
    }
    private void applyVisit(SatuSehatRujukanIGDRanapRepository.Visit loaded){
        restoring=true;
        try{visit=loaded;draft=loaded.draft.copy();jenis=draft.jenis;draftAvailable=loaded.draftAvailable;
            name.setText(loaded.name);rm.setText("No. RM  "+loaded.noRm);rawat.setText("No. Rawat  "+loaded.noRawat);
            name.setToolTipText(loaded.name);rm.setToolTipText("No. RM: "+loaded.noRm);rawat.setToolTipText("No. Rawat: "+loaded.noRawat);
            String sepText=String.join(", ",loaded.sepNumbers);sep.setText(loaded.sepNumbers.isEmpty()?"SEP belum tercatat":loaded.sepNumbers.size()>1?"SEP: "+loaded.sepNumbers.size()+" nomor tercatat":"SEP  "+sepText);
            sep.setToolTipText("Referensi lokal: "+sepText+". Bukan validasi eligibilitas atau SEP aktif.");
            for(Map.Entry<String,JTextComponent> f:fields.entrySet()){f.getValue().setText(draft.get(f.getKey()));f.getValue().setCaretPosition(0);}
            setTransportFromDraft(draft.get("transport"));
            applyDefaultReferralArea();
            applyDiagnosisMapping(false,false);
            emergency.setSelected("true".equals(draft.get("emergency_confirmed")));
            restoreWorkflowState();
            updateClinicalLabels();
            updateWorkflowLabels();
            String note="Sumber IHS: cache lokal, dengan fallback lookup SATUSEHAT via NIK bila cache kosong. Sumber Encounter: cache bridging lokal. Sumber nilai klinis: draf yang dimuat; prosedur awal berasal dari prosedur_pasien sesuai jenis kunjungan.\nNilai kosong berarti belum tersedia pada sumber yang dimuat, bukan hasil pemeriksaan normal atau tidak ada alergi.\nKelompok Layanan dan Clinical Speciality dicoba dipetakan dari diagnosis. Status IGD/kegawatdaruratan tetap mengikuti konteks pelayanan dan Questionnaire Q100, bukan Kelompok Layanan. Kode terminologi yang belum tervalidasi tidak dibuat-buat dan harus dipilih manual.\nTahap II mengirim workflow rujukan (Task/Bundle/ServiceRequest). Condition diagnosis dapat dicari/dibuat secara eksplisit melalui tombol; Observation/Procedure pendukung tidak dibuat otomatis.";
            if(!loaded.warnings.isEmpty())note+="\n\n"+String.join("\n",loaded.warnings);
            if(draft.version>0)note+="\n\nDraf v"+draft.version+" / "+draft.updatedAt+" / "+draft.updatedBy;
            sources.setText(note);sources.setCaretPosition(0);dirty=false;
            // Saat form IGD dibuka, selalu mulai dari Tahap 1 (Klinis & kriteria).
            // State Task/draf lama tetap dimuat dan tidak dihapus; user masih dapat membuka tahap berikutnya lewat strip tahap.
            selectedStage=jenis==SatuSehatRujukanIGDRanapDraft.Jenis.IGD?0:
                (!draft.get("approval_state_json").isEmpty()?3:!draft.get("task_candidate_id").isEmpty()?2:!draft.get("task_pre_id").isEmpty()?1:0);
            showPage(0);
        }finally{restoring=false;}
    }
    private SatuSehatRujukanIGDRanapDraft capture(){
        SatuSehatRujukanIGDRanapDraft d=draft.copy();
        for(Map.Entry<String,JTextComponent> f:fields.entrySet())d.set(f.getKey(),f.getValue().getText());
        d.set("transport",selectedTransportValue());
        d.set("emergency_confirmed",Boolean.toString(emergency.isSelected()));
        if(apiClient!=null){
            try{
                d.set("criteria_state_json",apiClient.questionsJson(collectCriteria()));
                d.set("candidate_state_json",apiClient.compactCandidates(collectCandidates()));
                d.set("approval_state_json",apiClient.compactApprovals(collectApprovals()));
            }catch(Exception ex){throw new IllegalStateException("State workflow tidak dapat diserialisasi: "+ex.getMessage(),ex);}
        }
        d.set("last_api_action",lastApiAction);d.set("last_api_request",lastApiRequest);d.set("last_api_response",lastApiResponse);
        return d;
    }
    private void saveDraft(final Runnable after){saveDraft(after,true);}
    private void saveDraft(final Runnable after,final boolean showSuccessToast){
        if(preview||busy||draft==null||!draftAvailable)return;
        if(!matchesLoaded()){toast("Identitas draf tidak sesuai kunjungan. Buka kembali pasien dari daftar.",true);return;}
        if(operator.isEmpty()){toast("Sesi operator belum dikenali. Buka form melalui akun Khanza yang aktif.",true);return;}
        final SatuSehatRujukanIGDRanapDraft snapshot=capture();final String who=operator;setBusy(true,"Menyimpan draf ke database...");
        new SwingWorker<SatuSehatRujukanIGDRanapDraft,Void>(){
            @Override protected SatuSehatRujukanIGDRanapDraft doInBackground()throws Exception{return repository.save(snapshot,who);}
            @Override protected void done(){try{draft=get();visit.draft=draft.copy();dirty=false;setBusy(false,"Tersimpan v"+draft.version+" / "+draft.updatedAt+" / "+draft.updatedBy);if(showSuccessToast)toast("Draf "+jenis.label+" berhasil disimpan.",false);if(after!=null)after.run();}
                catch(Exception ex){setBusy(false,"Draf belum tersimpan. Isian tetap tersedia.");failure("Menyimpan draf",ex);}}
        }.execute();
    }

    private void restoreWorkflowState(){
        criteriaModel.setRowCount(0);candidateModel.setRowCount(0);approvalModel.setRowCount(0);criteriaGroupId.clear();criteriaGroupText.clear();
        lastApiAction=draft.get("last_api_action");lastApiRequest=draft.get("last_api_request");lastApiResponse=draft.get("last_api_response");
        if(apiClient==null)return;
        try{populateCriteria(apiClient.parseQuestionsJson(draft.get("criteria_state_json")));}catch(Exception ex){/* state lama/kosong diabaikan */}
        try{populateCandidates(apiClient.parseCompactCandidates(draft.get("candidate_state_json")));}catch(Exception ex){/* state lama/kosong diabaikan */}
        try{populateApprovals(apiClient.parseCompactApprovals(draft.get("approval_state_json")));}catch(Exception ex){/* state lama/kosong diabaikan */}
    }
    /**
     * Memetakan diagnosis ke Kelompok Layanan dan Clinical Speciality.
     * Jenis alur IGD/RANAP tidak ditentukan di sini; IGD tetap mengikuti konteks
     * pelayanan + Questionnaire Q100. Hanya kode terminologi yang sudah
     * tervalidasi dari materi koordinasi yang diisi otomatis.
     */
    private void applyDiagnosisMapping(boolean force, boolean notify){
        if(draft==null)return;
        String dx=value("diagnosis_code"), dxName=value("diagnosis_name");
        SatuSehatRujukanDiagnosisMapping.Result m=SatuSehatRujukanDiagnosisMapping.map(dx,dxName);
        JTextComponent sgSystem=fields.get("service_group_system"),sgCode=fields.get("service_group_code"),sgName=fields.get("service_group_display"),
            csCode=fields.get("clinical_speciality_code"),csName=fields.get("clinical_speciality_display");
        if(sgSystem!=null)sgSystem.setText(SatuSehatRujukanDiagnosisMapping.SERVICE_GROUP_SYSTEM);
        draft.set("service_group_system",SatuSehatRujukanDiagnosisMapping.SERVICE_GROUP_SYSTEM);

        String currentSg=value("service_group_code");
        boolean invalidOld="TK000562".equalsIgnoreCase(currentSg);
        if(!m.hasSuggestion()){
            // Diagnosis baru yang belum termapping tidak boleh mewarisi mapping diagnosis lama.
            if(force){
                if(sgCode!=null)sgCode.setText("");if(sgName!=null)sgName.setText("");
                if(csCode!=null)csCode.setText("");if(csName!=null)csName.setText("");
                draft.set("service_group_code","");draft.set("service_group_display","");
                draft.set("clinical_speciality_code","");draft.set("clinical_speciality_display","");
                dirty=true;refreshStatus();
            }else if(invalidOld&&sgCode!=null){sgCode.setText("");draft.set("service_group_code","");}
            diagnosisMappingInfo.setText("Mapping diagnosis: belum ada rule otomatis untuk "+or(dx,"diagnosis ini")+" — pilih Kelompok Layanan dan Clinical Speciality secara manual.");
            diagnosisMappingInfo.setForeground(AMBER);
            if(notify)toast("Belum ada mapping otomatis untuk diagnosis "+or(dx,"ini")+". Pilih Kelompok Layanan dan Clinical Speciality secara manual.",true);
            return;
        }

        if(m.serviceCodeVerified){
            if(force||currentSg.isEmpty()||invalidOld){if(sgCode!=null)sgCode.setText(m.serviceGroupCode);if(sgName!=null)sgName.setText(m.serviceGroupDisplay);}
        }else{
            if(force||invalidOld){if(sgCode!=null)sgCode.setText("");}
            if((force||value("service_group_display").isEmpty()||invalidOld)&&sgName!=null)sgName.setText(m.serviceGroupDisplay);
        }

        String currentCs=value("clinical_speciality_code");
        boolean legacyCs=isLegacyClinicalSpecialityCode(currentCs);
        if(m.clinicalCodeVerified){
            // Jangan mempertahankan kode clinical-speciality lama dari draf/manual input.
            // Mapping terverifikasi (contoh LY133 dari collection koordinasi) selalu menang.
            if(force||currentCs.isEmpty()||legacyCs){if(csCode!=null)csCode.setText(m.clinicalSpecialityCode);if(csName!=null)csName.setText(m.clinicalSpecialityDisplay);}
        }else{
            // Format lama Sxxx.xx (contoh S001.09) ditolak SATUSEHAT STG saat ini.
            // Jika mapping diagnosis belum memiliki kode LY yang tervalidasi, kosongkan kode
            // agar aplikasi berhenti sebelum mengirim CarePlan dengan terminologi usang.
            if((force||legacyCs)&&csCode!=null)csCode.setText("");
            if((force||value("clinical_speciality_display").isEmpty()||legacyCs)&&csName!=null)csName.setText(m.clinicalSpecialityDisplay);
        }

        // Sinkronkan ke draf tanpa membuat field baru / ALTER TABLE.
        draft.set("service_group_code",value("service_group_code"));draft.set("service_group_display",value("service_group_display"));
        draft.set("clinical_speciality_code",value("clinical_speciality_code"));draft.set("clinical_speciality_display",value("clinical_speciality_display"));
        String serviceInfo=m.serviceCodeVerified?m.serviceGroupCode+" / "+m.serviceGroupDisplay:m.serviceGroupDisplay+" [kode resmi belum tervalidasi]";
        String clinicalInfo=m.clinicalCodeVerified?m.clinicalSpecialityCode+" / "+m.clinicalSpecialityDisplay:m.clinicalSpecialityDisplay+" [kode resmi belum tervalidasi]";
        diagnosisMappingInfo.setText("Mapping diagnosis "+dx+": "+serviceInfo+" • "+clinicalInfo);
        diagnosisMappingInfo.setToolTipText("Rule: "+m.rule+". Alur IGD/RANAP tetap ditentukan oleh konteks layanan, bukan mapping diagnosis.");
        diagnosisMappingInfo.setForeground((m.serviceCodeVerified&&m.clinicalCodeVerified)?GREEN:AMBER);
        if(force){dirty=true;refreshStatus();}
        if(notify){
            if(m.serviceCodeVerified&&m.clinicalCodeVerified)toast("Mapping diagnosis diterapkan otomatis: "+m.serviceGroupDisplay+" / "+m.clinicalSpecialityDisplay+".",false);
            else toast("Diagnosis dipetakan ke "+m.serviceGroupDisplay+" / "+m.clinicalSpecialityDisplay+". Kode yang belum tervalidasi tetap harus dipilih manual.",false);
        }
    }

    private void applyDefaultReferralArea(){
        applyDefaultField("province_code",DEFAULT_PROVINCE_CODE);
        applyDefaultField("province_display",DEFAULT_PROVINCE_DISPLAY);
        applyDefaultField("city_code",DEFAULT_CITY_CODE);
        applyDefaultField("city_display",DEFAULT_CITY_DISPLAY);
        // Collection resmi menggunakan system terminologi ini untuk Kelompok Layanan.
        applyDefaultField("service_group_system",DEFAULT_SERVICE_GROUP_SYSTEM);
    }
    private void applyDefaultField(String key,String value){
        JTextComponent field=fields.get(key);
        if(field!=null&&SatuSehatRujukanIGDRanapDraft.safe(field.getText()).isEmpty())field.setText(value);
        if(draft!=null&&draft.get(key).isEmpty())draft.set(key,value);
    }
    private void applyDefaultAreaToCriteria(){
        String provinceCode=or(value("province_code"),DEFAULT_PROVINCE_CODE);
        String provinceDisplay=or(value("province_display"),DEFAULT_PROVINCE_DISPLAY);
        String cityCode=or(value("city_code"),DEFAULT_CITY_CODE);
        String cityDisplay=or(value("city_display"),DEFAULT_CITY_DISPLAY);
        for(int i=0;i<criteriaModel.getRowCount();i++){
            String linkId=str(criteriaModel.getValueAt(i,0));
            String question=str(criteriaModel.getValueAt(i,1));
            if(isProvinceCriterion(linkId,question))criteriaModel.setValueAt(ADMIN_AREA_SYSTEM+"|"+provinceCode+"|"+provinceDisplay,i,3);
            else if(isCityCriterion(linkId,question))criteriaModel.setValueAt(ADMIN_AREA_SYSTEM+"|"+cityCode+"|"+cityDisplay,i,3);
        }
    }
    private static boolean isAreaCriterion(String linkId,String text){return isProvinceCriterion(linkId,text)||isCityCriterion(linkId,text);}
    private static boolean isProvinceCriterion(String linkId,String text){
        String q=text==null?"":text.toLowerCase();
        return "1.1".equals(linkId)||q.contains("provinsi")||q.contains("propinsi");
    }
    private static boolean isCityCriterion(String linkId,String text){
        String q=text==null?"":text.toLowerCase().trim();
        return "1.2".equals(linkId)||q.contains("kabupaten/kota")||q.contains("kab/kota")||q.startsWith("kode kabupaten")||"kabupaten".equals(q)||"kota".equals(q);
    }
    private List<SatuSehatRujukanIGDRanapApi.Question> collectCriteria(){
        List<SatuSehatRujukanIGDRanapApi.Question> out=new ArrayList<SatuSehatRujukanIGDRanapApi.Question>();
        for(int i=0;i<criteriaModel.getRowCount();i++){
            SatuSehatRujukanIGDRanapApi.Question q=new SatuSehatRujukanIGDRanapApi.Question();
            q.linkId=str(criteriaModel.getValueAt(i,0));q.text=str(criteriaModel.getValueAt(i,1));q.type=str(criteriaModel.getValueAt(i,2));q.answer=str(criteriaModel.getValueAt(i,3));
            q.groupLinkId=criteriaGroupId.containsKey(q.linkId)?criteriaGroupId.get(q.linkId):"";
            q.group=criteriaGroupText.containsKey(q.linkId)?criteriaGroupText.get(q.linkId):"";out.add(q);
        }return out;
    }
    private List<SatuSehatRujukanIGDRanapApi.Question> collectReferralCriteria(){
        List<SatuSehatRujukanIGDRanapApi.Question> out=new ArrayList<SatuSehatRujukanIGDRanapApi.Question>();
        for(SatuSehatRujukanIGDRanapApi.Question q:collectCriteria())if(!isAreaCriterion(q.linkId,q.text))out.add(q);
        return out;
    }
    private List<SatuSehatRujukanIGDRanapApi.Candidate> collectCandidates(){
        List<SatuSehatRujukanIGDRanapApi.Candidate> out=new ArrayList<SatuSehatRujukanIGDRanapApi.Candidate>();
        for(int i=0;i<candidateModel.getRowCount();i++){
            SatuSehatRujukanIGDRanapApi.Candidate c=new SatuSehatRujukanIGDRanapApi.Candidate();
            c.selected=Boolean.TRUE.equals(candidateModel.getValueAt(i,0));c.orgId=str(candidateModel.getValueAt(i,1));c.name=str(candidateModel.getValueAt(i,2));
            c.bpjsCode=str(candidateModel.getValueAt(i,3));c.strata=str(candidateModel.getValueAt(i,4));c.distance=str(candidateModel.getValueAt(i,5));
            c.eta=str(candidateModel.getValueAt(i,6));c.bed=str(candidateModel.getValueAt(i,7));out.add(c);
        }return out;
    }
    private List<SatuSehatRujukanIGDRanapApi.Approval> collectApprovals(){
        List<SatuSehatRujukanIGDRanapApi.Approval> out=new ArrayList<SatuSehatRujukanIGDRanapApi.Approval>();
        for(int i=0;i<approvalModel.getRowCount();i++){
            SatuSehatRujukanIGDRanapApi.Approval a=new SatuSehatRujukanIGDRanapApi.Approval();
            a.finalChoice=Boolean.TRUE.equals(approvalModel.getValueAt(i,0));a.orgId=str(approvalModel.getValueAt(i,1));a.name=str(approvalModel.getValueAt(i,2));
            a.taskId=str(approvalModel.getValueAt(i,3));a.status=str(approvalModel.getValueAt(i,4));a.decision=str(approvalModel.getValueAt(i,5));out.add(a);
        }return out;
    }
    private void populateCriteria(List<SatuSehatRujukanIGDRanapApi.Question> rows){
        boolean before=restoring;restoring=true;
        try{
            criteriaModel.setRowCount(0);criteriaGroupId.clear();criteriaGroupText.clear();
            if(rows!=null)for(SatuSehatRujukanIGDRanapApi.Question q:rows){
                criteriaModel.addRow(new Object[]{q.linkId,q.text,q.type,q.answer});
                if(q.linkId!=null){criteriaGroupId.put(q.linkId,q.groupLinkId==null?"":q.groupLinkId);criteriaGroupText.put(q.linkId,q.group==null?"":q.group);}
            }
            applyDefaultAreaToCriteria();
        }finally{restoring=before;}
        if(criteriaTable!=null)SwingUtilities.invokeLater(new Runnable(){@Override public void run(){fitTableColumns(criteriaTable,18,560);resizeCriteriaCard();}});
    }
    private void populateCandidates(List<SatuSehatRujukanIGDRanapApi.Candidate> rows){
        boolean before=restoring;restoring=true;try{candidateSearchField.setText("");candidateModel.setRowCount(0);if(rows!=null)for(SatuSehatRujukanIGDRanapApi.Candidate c:rows)candidateModel.addRow(new Object[]{c.selected,c.orgId,c.name,c.bpjsCode,c.strata,c.distance,c.eta,c.bed});}finally{restoring=before;}
        if(candidateTable!=null)SwingUtilities.invokeLater(new Runnable(){@Override public void run(){fitTableColumns(candidateTable,18,0);applyCandidateFilter();if(candidateModel.getRowCount()>0)focusCandidateSearch();}});
    }
    private void populateApprovals(List<SatuSehatRujukanIGDRanapApi.Approval> rows){
        boolean before=restoring;restoring=true;try{approvalModel.setRowCount(0);if(rows!=null)for(SatuSehatRujukanIGDRanapApi.Approval a:rows)approvalModel.addRow(new Object[]{a.finalChoice,a.orgId,a.name,a.taskId,a.status,a.decision});}finally{restoring=before;}
        if(approvalTable!=null)SwingUtilities.invokeLater(new Runnable(){@Override public void run(){fitTableColumns(approvalTable, 18, 360);}});
    }
    private void resizeCriteriaCard(){
        if(criteriaTable==null||criteriaScrollPane==null)return;
        int headerHeight=criteriaTable.getTableHeader()==null?24:criteriaTable.getTableHeader().getPreferredSize().height;
        int rows=Math.max(1,criteriaTable.getRowCount());
        int height=headerHeight+(rows*criteriaTable.getRowHeight())+4;
        Dimension size=new Dimension(980,height);
        criteriaScrollPane.setPreferredSize(size);
        criteriaScrollPane.setMinimumSize(new Dimension(100,height));
        criteriaScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,height));
        criteriaScrollPane.revalidate();
        if(criteriaCard!=null){criteriaCard.revalidate();criteriaCard.repaint();}
    }
    private void focusCriteriaCard(){
        SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
            SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
                if(criteriaCard==null)return;
                criteriaCard.scrollRectToVisible(new Rectangle(0,0,Math.max(1,criteriaCard.getWidth()),Math.min(110,Math.max(1,criteriaCard.getHeight()))));
                if(criteriaTable!=null&&criteriaTable.getRowCount()>0)criteriaTable.requestFocusInWindow();
            }});
        }});
    }
    private void focusCandidateCard(){
        SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
            if(candidateCard==null)return;
            candidateCard.scrollRectToVisible(new Rectangle(0,0,Math.max(1,candidateCard.getWidth()),Math.min(110,Math.max(1,candidateCard.getHeight()))));
            focusCandidateSearch();
        }});
    }
    private void updateWorkflowLabels(){
        if(draft==null)return;
        String preId=or(draft.get("task_pre_id"),"—"),candidateId=or(draft.get("task_candidate_id"),"—");
        String ids="Pra "+preId+"  •  Kandidat "+candidateId+"  •  CarePlan "+or(draft.get("careplan_id"),"—");
        workflowIdsLabel.setText(ids);workflowIdsLabel.setToolTipText(ids);
        String preText="Task Pra-Permintaan: "+preId;criteriaTaskIdLabel.setText(preText);criteriaTaskIdLabel.setToolTipText(preText);
        String candidateText="Task Pencarian Kandidat: "+candidateId;candidateTaskIdLabel.setText(candidateText);candidateTaskIdLabel.setToolTipText(candidateText);
        String sr=draft.get("service_request_id"),nr=draft.get("national_referral_number");
        String status=!nr.isEmpty()?"No. Rujukan Nasional: "+nr:!sr.isEmpty()?"ServiceRequest: "+sr+" / nomor nasional belum dikembalikan":"Belum difinalisasi.";
        finalStatusLabel.setText(status);finalStatusLabel.setToolTipText(status);
        boolean completed=!nr.isEmpty();
        finalResultTitle.setText(completed?"✓ RUJUKAN BERHASIL":"Menunggu Nomor Rujukan Nasional");
        finalResultTitle.setForeground(completed?GREEN:AMBER);
        finalNationalNumber.setText(completed?nr:"—");finalNationalNumber.setToolTipText(completed?nr:null);
        finalTargetLabel.setText("RS tujuan: "+or(draft.get("selected_org_name"),"—")+(draft.get("selected_org_id").isEmpty()?"":"  •  Org ID "+draft.get("selected_org_id")));
        finalServiceRequestLabel.setText("ServiceRequest: "+or(sr,"—"));
        copyNationalReferral.setEnabled(completed&&!busy);printReferral.setEnabled(completed&&!busy);
        if(finalResultCard!=null)finalResultCard.setVisible(!sr.isEmpty()||completed);
    }
    private void copyNationalReferralNumber(){
        if(draft==null)return;
        String nr=draft.get("national_referral_number");
        if(nr.isEmpty()){toast("Nomor Rujukan Nasional belum tersedia.",true);return;}
        try{Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(nr),null);toast("Nomor Rujukan Nasional berhasil disalin.",false);}
        catch(RuntimeException ex){toast("Clipboard sedang tidak tersedia.",true);}
    }

    private java.awt.image.BufferedImage referralQrImage(String text){
        try{
            BitMatrix matrix=new MultiFormatWriter().encode(text,BarcodeFormat.QR_CODE,180,180);
            return MatrixToImageWriter.toBufferedImage(matrix);
        }catch(Exception ex){return null;}
    }

    private static String sqlQuote(String value){return value==null?"":value.replace("'","''");}

    private void ensureReferralReportCompiled() throws Exception{
        File jrxml=new File("./report/rptSatuSehatRujukanIGDRanap.jrxml");
        File jasper=new File("./report/rptSatuSehatRujukanIGDRanap.jasper");
        if(!jrxml.isFile())throw new IllegalStateException("Template report tidak ditemukan: "+jrxml.getPath());
        if(!jasper.isFile()||jasper.lastModified()<jrxml.lastModified())JasperCompileManager.compileReportToFile(jrxml.getPath(),jasper.getPath());
    }

    private void doCetakRujukan(){
        if(preview){toast("Mode pratinjau tidak mencetak report.",true);return;}
        if(visit==null||draft==null){toast("Muat kunjungan pasien terlebih dahulu.",true);return;}
        final String nr=draft.get("national_referral_number");
        if(nr.isEmpty()){toast("Report hanya dapat dicetak setelah Nomor Rujukan Nasional terbit.",true);return;}
        try{
            ensureReferralReportCompiled();
            Map<String,Object> param=new HashMap<String,Object>();
            param.put("namars",akses.getnamars());param.put("alamatrs",akses.getalamatrs());param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());param.put("kontakrs",akses.getkontakrs());param.put("emailrs",akses.getemailrs());
            sekuel reportSequel=new sekuel();
            validasi reportValid=new validasi();
            param.put("logo",reportSequel.cariGambar("select setting.logo from setting"));
            param.put("nomor_rujukan_nasional",nr);param.put("jenis_rujukan",jenis.label);
            param.put("ihs_pasien",or(visit.ihs,"-"));param.put("no_sep",visit.sepNumbers.isEmpty()?"-":String.join(", ",visit.sepNumbers));
            param.put("diagnosis",value("diagnosis_code")+(value("diagnosis_name").isEmpty()?"":" - "+value("diagnosis_name")));
            param.put("alasan_rujukan",or(value("reason"),"-"));param.put("dokter_pengirim",or(value("doctor"),"-"));
            param.put("rs_tujuan",or(draft.get("selected_org_name"),"-"));param.put("org_tujuan",or(draft.get("selected_org_id"),"-"));
            param.put("service_request_id",or(draft.get("service_request_id"),"-"));param.put("encounter_id",or(visit.encounter,"-"));
            java.text.SimpleDateFormat df=new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'WITA'");df.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Makassar"));
            param.put("tanggal_cetak",df.format(new java.util.Date()));param.put("status_rujukan","DITERIMA / FINAL");
            param.put("qr_image",referralQrImage(nr));
            String rawat=sqlQuote(visit.noRawat);
            String qry="select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_ktp,pasien.tgl_lahir,pasien.jk "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat='"+rawat+"' limit 1";
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            reportValid.MyReportqry("rptSatuSehatRujukanIGDRanap.jasper","report","[ Surat Rujukan SATUSEHAT ]",qry,param);
        }catch(Exception ex){toast("Cetak Rujukan: "+or(ex.getMessage(),ex.getClass().getSimpleName()),true);}
        finally{setCursor(Cursor.getDefaultCursor());}
    }

    private void rememberApi(String action){rememberApi(action,false);}
    private void rememberApi(String action,boolean error){
        lastApiAction=action;
        if(apiClient!=null){
            lastApiRequest=limitLog((apiClient.getLastMethod()+" "+apiClient.getLastUrl()+"\n\n"+apiClient.getLastRequest()).trim(),32768);
            lastApiResponse=limitLog(apiClient.getLastResponse(),65536);
            List<SatuSehatRujukanIGDRanapApi.ApiTrace> traces=apiClient.getTraceHistory();
            int end=traces.size();
            if(end>apiGroupedTraceCount){
                ApiTraceGroup g=new ApiTraceGroup();g.action=apiCategory(action);g.start=apiGroupedTraceCount;g.end=end;g.error=error;
                if(error)g.errorMessage=defaultApiErrorMessage(g.action,lastApiResponse);
                g.timeMillis=end>0?traces.get(end-1).timeMillis:System.currentTimeMillis();apiTraceGroups.add(g);apiGroupedTraceCount=end;
                while(apiTraceGroups.size()>40)apiTraceGroups.remove(0);
            }else if(error&&!apiTraceGroups.isEmpty()){
                ApiTraceGroup g=apiTraceGroups.get(apiTraceGroups.size()-1);if(g.action.equals(apiCategory(action))){g.error=true;if(g.errorMessage.isEmpty())g.errorMessage=defaultApiErrorMessage(g.action,lastApiResponse);}
            }
        }
        if(draft!=null){draft.set("last_api_action",lastApiAction);draft.set("last_api_request",lastApiRequest);draft.set("last_api_response",lastApiResponse);}
    }
    private void markLatestApiIssue(String action,String message){
        String category=apiCategory(action);String detail=SatuSehatRujukanIGDRanapDraft.safe(message);
        for(int i=apiTraceGroups.size()-1;i>=0;i--){ApiTraceGroup g=apiTraceGroups.get(i);if(g.action.equals(category)){g.error=true;g.errorMessage=detail.isEmpty()?defaultApiErrorMessage(category,lastApiResponse):detail;return;}}
    }
    private static String defaultApiErrorMessage(String action,String response){
        String r=SatuSehatRujukanIGDRanapDraft.safe(response);
        String lower=r.toLowerCase(java.util.Locale.ENGLISH);
        if(lower.contains("code not found")&&lower.contains("clinical-speciality")){
            return "Clinical Speciality ditolak SATUSEHAT. Kode pada CarePlan.activity.detail.code tidak ditemukan pada CodeSystem clinical-speciality yang aktif. "
                +"Kode lama seperti S001.09 tidak boleh dikirim ulang; gunakan kode clinical-speciality aktif yang tervalidasi (contoh format pada collection koordinasi: LYxxx). "
                +"Periksa field Kode Clinical Speciality, lalu kirim ulang Task. Respon asli tetap tersedia pada tab Response.";
        }
        if(r.length()>1200)r=r.substring(0,1200)+" ...";
        return "Proses "+or(action,"API")+" ditandai bermasalah."+(r.isEmpty()?" Periksa Request dan Response untuk detail.":" Respon terakhir: "+r);
    }
    private static String apiCategory(String action){
        String a=action==null?"API":action.trim();int slash=a.indexOf(" /");if(slash>0)a=a.substring(0,slash).trim();
        if(a.startsWith("Cek Status"))return "Cek Status";if(a.startsWith("Finalisasi"))return "Finalisasi";
        if(a.startsWith("Rujukan Masuk"))return "Rujukan Masuk";if(a.startsWith("Buat Condition"))return "Cari Condition";
        return a.isEmpty()?"API":a;
    }
    private void persistApiSuccess(String message){
        dirty=true;updateWorkflowLabels();toast(message,false);refresh();
        if(!preview&&draftAvailable&&!operator.isEmpty())saveDraft(null,false);
        else if(!preview)toast(message+" State API belum tersimpan karena operator/tabel draf belum tersedia.",true);
    }
    private boolean validateApiBase(){
        if(preview||apiClient==null){toast("Mode pratinjau tidak mengirim API.",true);return false;}
        if(visit==null||draft==null){toast("Muat kunjungan pasien terlebih dahulu.",true);return false;}
        if(visit.ihs==null||visit.ihs.trim().isEmpty()){toast("IHS pasien belum ditemukan dari cache lokal maupun lookup SATUSEHAT.",true);return false;}
        if(visit.encounter==null||visit.encounter.trim().isEmpty()){toast("Encounter SATUSEHAT belum tersedia untuk kunjungan ini.",true);return false;}
        if(value("diagnosis_code").isEmpty()){toast("Diagnosis utama / ICD-10 wajib diisi.",true);return false;}
        return true;
    }
    private String value(String key){JTextComponent t=fields.get(key);return t==null?"":SatuSehatRujukanIGDRanapDraft.safe(t.getText());}
    private static String str(Object o){return o==null?"":String.valueOf(o).trim();}
    private static String limitLog(String text,int max){if(text==null)return "";return text.length()<=max?text:text.substring(0,max)+"\n...[dipotong untuk menjaga ukuran draf]";}
    private static String jsonText(JsonNode node,String fallback){
        if(node==null||node.isNull()||node.isMissingNode())return fallback==null?"":fallback;
        String value=node.asText();
        return value==null||value.trim().isEmpty()?(fallback==null?"":fallback):value;
    }
    private static String responseId(JsonNode node){return jsonText(node==null?null:node.path("id"),"");}
    private static String responseTaskId(JsonNode node,String wantedCode){
        if(node==null)return "";
        String rootId=responseId(node);
        if("Task".equals(jsonText(node.path("resourceType"),""))&&!rootId.isEmpty())return rootId;
        JsonNode entries=node.path("entry");
        if(entries.isArray())for(JsonNode entry:entries){
            JsonNode resource=entry.path("resource");
            if("Task".equals(jsonText(resource.path("resourceType"),""))){
                String code="";JsonNode coding=resource.path("code").path("coding");
                if(coding.isArray())for(JsonNode c:coding){String x=jsonText(c.path("code"),"");if(!x.isEmpty()){code=x;break;}}
                String id=responseId(resource);
                if(!id.isEmpty()&&(wantedCode==null||wantedCode.isEmpty()||wantedCode.equals(code)))return id;
            }
            String location=jsonText(entry.path("response").path("location"),"");
            String id=taskIdFromLocation(location);if(!id.isEmpty())return id;
        }
        // Beberapa gateway menambahkan id hasil Location ke root object tanpa resourceType.
        return rootId;
    }
    private static String responseTaskStatus(JsonNode node,String wantedCode){
        if(node==null)return "";
        if("Task".equals(jsonText(node.path("resourceType"),""))){
            String code="";JsonNode coding=node.path("code").path("coding");
            if(coding.isArray())for(JsonNode c:coding){String x=jsonText(c.path("code"),"");if(!x.isEmpty()){code=x;break;}}
            if(wantedCode==null||wantedCode.isEmpty()||wantedCode.equals(code))return jsonText(node.path("status"),"");
        }
        JsonNode entries=node.path("entry");
        if(entries.isArray())for(JsonNode entry:entries){
            JsonNode resource=entry.path("resource");
            if(!"Task".equals(jsonText(resource.path("resourceType"),"")))continue;
            String code="";JsonNode coding=resource.path("code").path("coding");
            if(coding.isArray())for(JsonNode c:coding){String x=jsonText(c.path("code"),"");if(!x.isEmpty()){code=x;break;}}
            if(wantedCode==null||wantedCode.isEmpty()||wantedCode.equals(code))return jsonText(resource.path("status"),"");
        }
        return "";
    }
    private static String responseTaskBusinessIdentifier(JsonNode node,String wantedCode){
        JsonNode task=responseTaskNode(node,wantedCode);if(task==null)return "";JsonNode ids=task.path("identifier");
        if(ids.isArray())for(JsonNode id:ids){String value=jsonText(id.path("value"),"");if(!value.isEmpty())return value;}
        return "";
    }
    private static JsonNode responseTaskNode(JsonNode node,String wantedCode){
        if(node==null)return null;
        if("Task".equals(jsonText(node.path("resourceType"),""))){
            String code="";JsonNode coding=node.path("code").path("coding");if(coding.isArray())for(JsonNode c:coding){String x=jsonText(c.path("code"),"");if(!x.isEmpty()){code=x;break;}}
            if(wantedCode==null||wantedCode.isEmpty()||wantedCode.equals(code))return node;
        }
        JsonNode entries=node.path("entry");if(entries.isArray())for(JsonNode entry:entries){JsonNode resource=entry.path("resource");if(!"Task".equals(jsonText(resource.path("resourceType"),"")))continue;
            String code="";JsonNode coding=resource.path("code").path("coding");if(coding.isArray())for(JsonNode c:coding){String x=jsonText(c.path("code"),"");if(!x.isEmpty()){code=x;break;}}
            if(wantedCode==null||wantedCode.isEmpty()||wantedCode.equals(code))return resource;
        }return null;
    }
    private static boolean legacyTaskBusinessIdentifier(String value){return value==null||!value.matches("\\d{13}");}
    private static String taskIdFromLocation(String location){
        String s=SatuSehatRujukanIGDRanapDraft.safe(location);if(s.isEmpty())return "";
        int q=s.indexOf('?');if(q>=0)s=s.substring(0,q);int h=s.indexOf('#');if(h>=0)s=s.substring(0,h);
        int hist=s.indexOf("/_history/");if(hist>=0)s=s.substring(0,hist);
        while(s.endsWith("/"))s=s.substring(0,s.length()-1);
        int marker=s.lastIndexOf("/Task/");
        if(marker>=0){String id=s.substring(marker+6);int slash=id.indexOf('/');return slash>=0?id.substring(0,slash):id;}
        if(s.startsWith("Task/")){String id=s.substring(5);int slash=id.indexOf('/');return slash>=0?id.substring(0,slash):id;}
        return "";
    }
    private static final class CandidateSearchResult{String taskId="";JsonNode response;}

    private String currentPreRequestSignature(){
        if(visit==null)return "";
        return jenis.name()+"|"+or(visit.noRawat,"")+"|"+or(visit.encounter,"")+"|"+value("diagnosis_code").toUpperCase();
    }
    private void doCekKriteria(){
        if(!validateApiBase()||busy)return;
        applyDiagnosisMapping(false,false);
        final String signature=currentPreRequestSignature();
        final String storedSignature=draft.get("pre_request_signature");
        final String oldTask=draft.get("task_pre_id");
        final boolean reuse=!oldTask.isEmpty()&&!signature.isEmpty()&&signature.equals(storedSignature);
        if(!oldTask.isEmpty()&&!reuse){
            resetWorkflowForClinicalChange(storedSignature.isEmpty()?"Task pra-permintaan berasal dari draf versi lama/tanpa signature.":"Diagnosis atau Encounter berubah.");
        }
        final String existing=reuse?oldTask:"";
        final String reg=visit.noRawat;final String encounter=visit.encounter;
        final String dx=value("diagnosis_code"),dxName=value("diagnosis_name");
        setBusy(true,existing.isEmpty()?"Mengirim Task pra-permintaan baru...":"Memuat ulang Task pra-permintaan...");
        focusCriteriaCard();
        new SwingWorker<JsonNode,Void>(){
            @Override protected JsonNode doInBackground()throws Exception{
                JsonNode r=existing.isEmpty()?apiClient.kirimPraPermintaan(jenis,reg,encounter,dx,dxName):apiClient.cekTaskPraPermintaan(existing);
                String id=responseTaskId(r,"referral-pre-request");if(id.isEmpty())id=existing;
                // Draf lama dapat menunjuk Task yang dibuat saat identifier masih memakai no_rawat.
                // Task legacy seperti itu tidak dipakai ulang karena collection koordinasi memakai identifier unik YYYYMMDD+5 digit.
                if(!existing.isEmpty()&&apiClient.parseQuestions(r).isEmpty()&&legacyTaskBusinessIdentifier(responseTaskBusinessIdentifier(r,"referral-pre-request"))){
                    r=apiClient.kirimPraPermintaan(jenis,reg,encounter,dx,dxName);
                    id=responseTaskId(r,"referral-pre-request");
                }
                // Backend dapat mengembalikan Task requested lebih dulu; tunggu enrichment Questionnaire.
                for(int attempt=1;attempt<=20&&apiClient.parseQuestions(r).isEmpty()&&!id.isEmpty();attempt++){
                    try{Thread.sleep(1500L);}catch(InterruptedException ie){Thread.currentThread().interrupt();break;}
                    r=apiClient.cekTaskPraPermintaan(id);
                }
                return r;
            }
            @Override protected void done(){try{
                JsonNode r=get();rememberApi("Cek Kriteria / referral-pre-request");String id=responseTaskId(r,"referral-pre-request");if(id.isEmpty())id=existing;
                if(id.isEmpty())throw new IllegalStateException("SATUSEHAT tidak mengembalikan ID Task pra-permintaan.");
                draft.set("task_pre_id",id);draft.set("pre_request_signature",signature);
                List<SatuSehatRujukanIGDRanapApi.Question> qs=apiClient.parseQuestions(r);populateCriteria(qs);
                selectedStage=0;setBusy(false,"Task pra-permintaan: "+id);showPage(0);focusCriteriaCard();
                String diagnosisLabel=dx+(dxName.isEmpty()?"":" - "+dxName);
                if(qs.isEmpty()){
                    String taskStatus=responseTaskStatus(r,"referral-pre-request");
                    String msg="Task pra-permintaan berhasil dibuat/dibaca. Task ID: "+id+". ";
                    if(taskStatus.isEmpty()||"requested".equalsIgnoreCase(taskStatus)){
                        msg+="SATUSEHAT belum mengembalikan Questionnaire kriteria setelah proses polling. Diagnosis: "+diagnosisLabel+". "
                           +"Klik Cek Kriteria lagi untuk mengecek Task yang sama. Jika diagnosis/Encounter di database baru saja berubah, gunakan Refresh Data agar Task lama tidak dipakai.";
                    }else{
                        msg+="Status Task: "+taskStatus.toUpperCase()+", tetapi Questionnaire kriteria kosong/tidak dikembalikan. Periksa Respon API.";
                    }
                    markLatestApiIssue("Cek Kriteria",msg);
                    dirty=true;updateWorkflowLabels();refresh();if(!preview&&draftAvailable&&!operator.isEmpty())saveDraft(null,false);toast(msg,true);
                }else{
                    persistApiSuccess("Cek Kriteria berhasil. Task ID: "+id+". Kriteria SATUSEHAT dimuat: "+qs.size()+" pertanyaan.");
                }
            }catch(Exception ex){rememberApi("Cek Kriteria / referral-pre-request",true);setBusy(false,"Cek kriteria gagal.");failure("Cek Kriteria",ex);}}
        }.execute();
    }
    private void doCariKandidat(){
        if(!validateApiBase()||busy)return;
        applyDiagnosisMapping(false,false);
        if(criteriaModel.getRowCount()==0){toast("Jalankan Cek Kriteria terlebih dahulu.",true);return;}
        if(value("province_code").isEmpty()||value("city_code").isEmpty()){toast("Kode Provinsi dan Kabupaten/Kota wajib diisi.",true);return;}
        if(value("service_group_code").isEmpty()||value("service_group_display").isEmpty()){
            toast("Kelompok Layanan sudah dicoba dipetakan dari diagnosis, tetapi kode resminya belum tersedia/terverifikasi. Lengkapi Kode dan Nama Kelompok Layanan sebelum Cari Faskes.",true);return;
        }
        // TK000562 adalah kode TYPE input "Kelompok Layanan" pada collection,
        // bukan kode VALUE kelompok layanan. Mengirim TK000562 sebagai valueCoding
        // dapat menghasilkan Task requested tanpa kandidat/output.
        if("TK000562".equalsIgnoreCase(value("service_group_code"))){
            draft.set("task_candidate_id","");draft.set("candidate_state_json","");draft.set("candidate_query_signature","");
            candidateModel.setRowCount(0);
            toast("Kode TK000562 adalah kode TYPE 'Kelompok Layanan', bukan kode NILAI kelompok layanan. Pilih kode terminologi kelompok layanan yang benar lalu Cari Faskes kembali.",true);
            return;
        }

        final List<SatuSehatRujukanIGDRanapApi.Question> qs=collectReferralCriteria();
        final String dx=value("diagnosis_code"),dxName=value("diagnosis_name"),secondaryDx=draft.get("secondary_diagnosis_code"),secondaryDxName=draft.get("secondary_diagnosis_name"),
            prov=value("province_code"),provName=value("province_display"),city=value("city_code"),cityName=value("city_display"),
            sgSystem=or(value("service_group_system"),DEFAULT_SERVICE_GROUP_SYSTEM),sgCode=value("service_group_code"),sgName=value("service_group_display");
        final String reg=visit.noRawat,patient=visit.ihs,encounter=visit.encounter;
        final String signature=candidateQuerySignature(qs,dx,secondaryDx,prov,city,sgSystem,sgCode,sgName);
        final String savedSignature=draft.get("candidate_query_signature");
        final String existing=signature.equals(savedSignature)?draft.get("task_candidate_id"):"";

        if(existing.isEmpty()&&!draft.get("task_candidate_id").isEmpty()){
            draft.set("task_candidate_id","");draft.set("candidate_state_json","");candidateModel.setRowCount(0);
        }

        setBusy(true,existing.isEmpty()?"Mencari kandidat fasyankes...":"Memuat ulang hasil kandidat fasyankes...");
        new SwingWorker<CandidateSearchResult,Void>(){
            @Override protected CandidateSearchResult doInBackground()throws Exception{
                CandidateSearchResult out=new CandidateSearchResult();
                JsonNode r;
                String id=existing;
                if(existing.isEmpty()){
                    r=apiClient.cariKandidat(jenis,reg,patient,encounter,
                        dx,dxName,secondaryDx,secondaryDxName,qs,prov,provName,city,cityName,sgSystem,sgCode,sgName);
                    id=responseTaskId(r,"request-referral-candidate");
                }else{
                    r=apiClient.cekTaskPencarianKandidat(existing);
                }
                out.taskId=id;out.response=r;

                // Collection koordinasi memakai GET /Task?_id={id}. SATUSEHAT dapat
                // mengembalikan POST awal berstatus requested tanpa output kandidat,
                // jadi polling search dilakukan terbatas di background (UI tetap responsif).
                for(int attempt=1;attempt<=10&&apiClient.parseCandidates(r).isEmpty()&&!id.isEmpty();attempt++){
                    try{Thread.sleep(1500L);}catch(InterruptedException ie){Thread.currentThread().interrupt();break;}
                    r=apiClient.cekTaskPencarianKandidat(id);out.response=r;
                    String status=apiClient.candidateTaskStatus(r);
                    if("failed".equalsIgnoreCase(status)||"cancelled".equalsIgnoreCase(status))break;
                }
                return out;
            }
            @Override protected void done(){try{
                CandidateSearchResult result=get();rememberApi("Cari Faskes / request-referral-candidate");
                JsonNode r=result.response;String id=or(result.taskId,responseTaskId(r,"request-referral-candidate"));
                if(id.isEmpty())throw new IllegalStateException("ID Task pencarian kandidat tidak ditemukan pada respons SATUSEHAT.");
                draft.set("task_candidate_id",id);draft.set("candidate_query_signature",signature);
                List<SatuSehatRujukanIGDRanapApi.Candidate> cs=apiClient.parseCandidates(r);populateCandidates(cs);
                selectedStage=1;setBusy(false,"Task kandidat: "+id);showPage(0);focusCandidateCard();
                String taskStatus=apiClient.candidateTaskStatus(r);
                if(cs.isEmpty()){
                    dirty=true;updateWorkflowLabels();refresh();
                    if(!preview&&draftAvailable&&!operator.isEmpty())saveDraft(null,false);
                    if("requested".equalsIgnoreCase(taskStatus)||taskStatus.isEmpty())
                        toast("Task kandidat tersimpan ("+id+"), tetapi masih REQUESTED dan belum memiliki output kandidat. Klik Cari Faskes lagi untuk cek ulang setelah beberapa saat.",true);
                    else
                        toast("Task kandidat status "+or(taskStatus,"tidak diketahui")+" tetapi tidak berisi kandidat. Periksa Kelompok Layanan, kriteria, wilayah, dan Respon API.",true);
                }else{
                    persistApiSuccess("Cari Faskes berhasil. Task ID: "+id+". Kandidat fasyankes dimuat: "+cs.size()+" RS.");
                }
            }catch(Exception ex){rememberApi("Cari Faskes / request-referral-candidate",true);setBusy(false,"Pencarian kandidat gagal.");failure("Cari Faskes",ex);}}
        }.execute();
    }

    private String candidateQuerySignature(List<SatuSehatRujukanIGDRanapApi.Question> qs,String dx,String secondaryDx,
                                           String prov,String city,String sgSystem,String sgCode,String sgName){
        StringBuilder b=new StringBuilder();
        b.append(jenis==null?"":jenis.name()).append('|').append(dx).append('|').append(secondaryDx)
         .append('|').append(prov).append('|').append(city).append('|').append(sgSystem)
         .append('|').append(sgCode).append('|').append(sgName);
        if(qs!=null)for(SatuSehatRujukanIGDRanapApi.Question q:qs){
            b.append('|').append(q==null?"":q.groupLinkId).append(':').append(q==null?"":q.linkId)
             .append(':').append(q==null?"":q.type).append('=').append(q==null?"":q.answer);
        }
        String raw=b.toString();
        return Integer.toHexString(raw.hashCode())+"-"+raw.length();
    }

    private void doApproval(){
        if(!validateApiBase()||busy)return;
        applyDiagnosisMapping(false,false);
        if(approvalModel.getRowCount()==0){
            String clinicalCode=value("clinical_speciality_code");
            if(isLegacyClinicalSpecialityCode(clinicalCode)){
                // Pertahanan terakhir bila draf lama belum sempat dibersihkan oleh mapping.
                JTextComponent f=fields.get("clinical_speciality_code");if(f!=null)f.setText("");
                draft.set("clinical_speciality_code","");dirty=true;refreshStatus();
                toast("Kode Clinical Speciality "+clinicalCode+" adalah kode lama dan ditolak SATUSEHAT STG. Kode sudah dikosongkan. Isi kode clinical-speciality aktif yang tervalidasi (format pada collection terbaru menggunakan LYxxx), lalu Kirim Tugas kembali.",true);return;
            }
            if(value("clinical_speciality_code").isEmpty()||value("clinical_speciality_display").isEmpty()){
                toast("Clinical Speciality belum lengkap. Sistem sudah mencoba mapping dari diagnosis. Jangan gunakan kode lama S001.09; isi kode clinical-speciality aktif yang tervalidasi sebelum Kirim Tugas.",true);return;
            }
            final List<SatuSehatRujukanIGDRanapApi.Candidate> allCandidates=collectCandidates();
            final List<SatuSehatRujukanIGDRanapApi.Candidate> selected=new ArrayList<SatuSehatRujukanIGDRanapApi.Candidate>();
            for(SatuSehatRujukanIGDRanapApi.Candidate c:allCandidates)if(c.selected)selected.add(c);
            if(selected.isEmpty()){toast("Centang minimal satu kandidat RS pada tahap Kandidat RS.",true);return;}
            if(jenis==SatuSehatRujukanIGDRanapDraft.Jenis.IGD&&selected.size()!=1){toast("Rujukan IGD hanya boleh memilih satu kandidat RS. Lepas pilihan lain lalu kirim kembali.",true);return;}
            final List<SatuSehatRujukanIGDRanapApi.Question> criteria=collectReferralCriteria();
            final String reg=visit.noRawat,patient=visit.ihs,patientName=visit.name,encounter=visit.encounter,practitioner=value("practitioner_ihs"),doctor=value("doctor"),
                condition=value("condition_id"),secondaryCondition=draft.get("secondary_condition_id"),reason=value("reason"),clinicalSpecCode=value("clinical_speciality_code"),clinicalSpecName=value("clinical_speciality_display"),
                candidateTask=draft.get("task_candidate_id"),prov=value("province_code"),provName=value("province_display"),city=value("city_code"),cityName=value("city_display"),
                sgSystem=or(value("service_group_system"),DEFAULT_SERVICE_GROUP_SYSTEM),sgCode=value("service_group_code"),sgName=value("service_group_display"),
                dx=value("diagnosis_code"),dxName=value("diagnosis_name"),secondaryDx=draft.get("secondary_diagnosis_code"),secondaryDxName=draft.get("secondary_diagnosis_name");
            setBusy(true,"Mengirim tugas rujukan ke "+selected.size()+" kandidat...");
            new SwingWorker<SatuSehatRujukanIGDRanapApi.ApprovalBundleResult,Void>(){
                @Override protected SatuSehatRujukanIGDRanapApi.ApprovalBundleResult doInBackground()throws Exception{
                    return apiClient.kirimTugasRujukan(jenis,reg,patient,patientName,encounter,practitioner,doctor,
                        condition,secondaryCondition,reason,clinicalSpecCode,clinicalSpecName,candidateTask,criteria,
                        prov,provName,city,cityName,sgSystem,sgCode,sgName,dx,dxName,secondaryDx,secondaryDxName,allCandidates,selected);
                }
                @Override protected void done(){try{
                    SatuSehatRujukanIGDRanapApi.ApprovalBundleResult r=get();rememberApi("Kirim Tugas / Bundle referral-approval");
                    if(r.carePlanId==null||r.carePlanId.trim().isEmpty())throw new IllegalStateException("Bundle sukses tetapi ID CarePlan tidak terbaca.");
                    if(r.approvals.size()!=selected.size())throw new IllegalStateException("Jumlah Task approval pada respons tidak sesuai jumlah kandidat.");
                    draft.set("careplan_id",r.carePlanId);draft.set("task_candidate_response_id",r.candidateRecommendationTaskId==null?"":r.candidateRecommendationTaskId);
                    populateApprovals(r.approvals);setBusy(false,"CarePlan: "+r.carePlanId);
                    persistApiSuccess("Tugas rujukan berhasil dikirim ke "+r.approvals.size()+" kandidat. Gunakan Cek Status untuk membaca jawaban RS.");
                }catch(Exception ex){rememberApi("Kirim Tugas / Bundle referral-approval",true);setBusy(false,"Pengiriman tugas gagal.");failure("Kirim Tugas",ex);}}
            }.execute();
        }else{
            final List<SatuSehatRujukanIGDRanapApi.Approval> rows=collectApprovals();setBusy(true,"Memeriksa status persetujuan RS...");
            new SwingWorker<List<SatuSehatRujukanIGDRanapApi.Approval>,Void>(){
                @Override protected List<SatuSehatRujukanIGDRanapApi.Approval> doInBackground()throws Exception{
                    for(SatuSehatRujukanIGDRanapApi.Approval a:rows){
                        if(a.taskId==null||a.taskId.trim().isEmpty())continue;JsonNode task=apiClient.cekTask(a.taskId);
                        a.status=jsonText(task.path("status"),a.status);a.decision=apiClient.parseDecision(task);
                    }return rows;
                }
                @Override protected void done(){try{
                    List<SatuSehatRujukanIGDRanapApi.Approval> r=get();rememberApi("Cek Status Task approval");populateApprovals(r);
                    int accepted=autoSelectSingleAcceptedApproval();
                    if(accepted>0)selectedStage=3;setBusy(false,"Status "+r.size()+" Task diperbarui.");
                    persistApiSuccess(accepted==1?"RS tujuan menerima rujukan. Lanjutkan dengan Finalisasi Rujukan.":accepted>1?accepted+" RS menerima. Pilih satu tujuan final pada kolom Final.":"Belum ada RS yang berstatus accepted.");
                }catch(Exception ex){rememberApi("Cek Status Task approval",true);setBusy(false,"Cek status gagal.");failure("Cek Status",ex);}}
            }.execute();
        }
    }
    private int autoSelectSingleAcceptedApproval(){
        int accepted=0,acceptedRow=-1;
        for(int i=0;i<approvalModel.getRowCount();i++){
            if("accepted".equalsIgnoreCase(str(approvalModel.getValueAt(i,5)))){accepted++;acceptedRow=i;}
        }
        if(accepted==1&&acceptedRow>=0){
            boolean before=restoring;restoring=true;
            try{
                for(int i=0;i<approvalModel.getRowCount();i++)approvalModel.setValueAt(Boolean.valueOf(i==acceptedRow),i,0);
            }finally{restoring=before;}
        }
        return accepted;
    }
    private void doFinalisasi(){
        if(!validateApiBase()||busy)return;
        List<SatuSehatRujukanIGDRanapApi.Approval> approvals=collectApprovals();
        SatuSehatRujukanIGDRanapApi.Approval chosen=null;int selected=0,accepted=0;
        for(SatuSehatRujukanIGDRanapApi.Approval a:approvals){
            if("accepted".equalsIgnoreCase(a.decision)){
                accepted++;
                if(chosen==null)chosen=a;
            }
            if(a.finalChoice){chosen=a;selected++;}
        }
        if(selected==0&&accepted==1){
            autoSelectSingleAcceptedApproval();
            approvals=collectApprovals();chosen=null;
            for(SatuSehatRujukanIGDRanapApi.Approval a:approvals)if(a.finalChoice){chosen=a;break;}
        }else if(selected!=1){
            if(accepted>1)toast("Lebih dari satu RS berstatus accepted. Pilih satu tujuan final pada Tahap 3 - Persetujuan RS.",true);
            else toast("Belum ada RS tujuan yang berstatus accepted.",true);
            return;
        }
        if(chosen==null||!"accepted".equalsIgnoreCase(chosen.decision)){toast("Tujuan final harus sudah berstatus accepted.",true);return;}
        final SatuSehatRujukanIGDRanapApi.Approval target=chosen;final String existing=draft.get("service_request_id"),carePlan=draft.get("careplan_id"),oldNational=draft.get("national_referral_number");
        final String reg=visit.noRawat,patient=visit.ihs,encounter=visit.encounter,condition=value("condition_id"),secondaryCondition=draft.get("secondary_condition_id");
        setBusy(true,existing.isEmpty()?"Mengirim ServiceRequest rujukan...":"Memuat ServiceRequest final...");
        new SwingWorker<JsonNode,Void>(){
            @Override protected JsonNode doInBackground()throws Exception{
                if(!existing.isEmpty())return apiClient.getServiceRequest(existing);
                return apiClient.finalisasi(jenis,reg,patient,encounter,carePlan,condition,secondaryCondition,
                    target.orgId,target.name,target.taskId);
            }
            @Override protected void done(){try{
                JsonNode r=get();rememberApi("Finalisasi / ServiceRequest");String id=existing.isEmpty()?responseId(r):existing;
                if(id.isEmpty())throw new IllegalStateException("SATUSEHAT tidak mengembalikan ID ServiceRequest.");
                String national=apiClient.getNomorRujukanNasional(r);
                draft.set("service_request_id",id);draft.set("national_referral_number",national);draft.set("selected_org_id",target.orgId);
                draft.set("selected_org_name",target.name);draft.set("selected_approval_task_id",target.taskId);setBusy(false,"ServiceRequest: "+id);
                persistApiSuccess(national.isEmpty()?"ServiceRequest berhasil dibuat. Nomor rujukan nasional belum ada pada respons; klik Finalisasi Rujukan lagi untuk memuat ServiceRequest terbaru.":"Rujukan final berhasil. No. Rujukan Nasional: "+national);
                if(!national.isEmpty()&&oldNational.isEmpty())SwingUtilities.invokeLater(() -> doCetakRujukan());
            }catch(Exception ex){rememberApi("Finalisasi / ServiceRequest",true);setBusy(false,"Finalisasi gagal.");failure("Finalisasi Rujukan",ex);}}
        }.execute();
    }
    // Form ini khusus rujukan KELUAR; halaman masuk dipindahkan ke SatuSehatRujukanMasukPanel.
    private void showPage(int page){selectedPage=0;((CardLayout)pages.getLayout()).show(pages,"stage"+selectedStage);title.setText("Rujukan Keluar");refresh();if(selectedStage==0)focusClinicalSummary();}
    private void focusClinicalSummary(){
        SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
            JTextComponent dx=fields.get("diagnosis_code");if(dx!=null&&dx.isEnabled())dx.requestFocusInWindow();
            SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
                if(pages.getComponentCount()>0&&pages.getComponent(0) instanceof JScrollPane){
                    JScrollPane sp=(JScrollPane)pages.getComponent(0);
                    sp.getViewport().setViewPosition(new Point(0,0));
                    if(sp.getVerticalScrollBar()!=null)sp.getVerticalScrollBar().setValue(0);
                }
            }});
        }});
    }
    private boolean confirmDiscard(){return JOptionPane.showConfirmDialog(this,"Ada perubahan draf yang belum disimpan.\nLanjutkan dan abaikan perubahan ini?","Perubahan belum disimpan",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION;}
    public void requestClose(Runnable close){
        if(busy){toast("Tunggu proses selesai sebelum menutup form.",true);return;}if(!dirty){close.run();return;}
        Object[] options={"Simpan & tutup","Abaikan perubahan","Kembali"};int result=JOptionPane.showOptionDialog(this,"Draf memiliki perubahan yang belum disimpan.","Tutup rujukan",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);
        if(result==0)saveDraft(close);else if(result==1)close.run();
    }
    private void failure(String action,Exception error){
        Throwable cause=error instanceof ExecutionException&&error.getCause()!=null?error.getCause():error;String message;
        if(cause instanceof SQLException){String state=((SQLException)cause).getSQLState();
            if(state==null||"40001".equals(state)||"45000".equals(state)||"02000".equals(state))message=cause.getMessage();
            else if(state.startsWith("42"))message="Tabel/kolom belum sesuai atau akses database belum tersedia. Periksa SQL pemasangan.";
            else message="Database belum dapat memproses permintaan (SQLState "+state+").";
        }else if(cause instanceof IOException&&("Rujukan Masuk".equals(action)||"Respons Rujukan Masuk".equals(action)||"Detail Rujukan Masuk".equals(action))){
            // Alur ini memanggil HTTP/JSON SATUSEHAT, tidak membaca atau menyimpan XML draf.
            // Pertahankan HTTP status/body atau error koneksi asli, bukan pesan batas ukuran draf.
            message="Proses SATUSEHAT belum berhasil dikonfirmasi. Detail: "+or(cause.getMessage(),cause.getClass().getSimpleName())
                +" Lihat Request dan Response pada Respon API.";
            markLatestApiIssue(lastApiAction,message);
        }else if(cause instanceof IOException)message="Isi draf tidak dapat dibaca atau melebihi batas 256 KiB. Data tersimpan tidak ditimpa.";
        else if(cause instanceof IllegalArgumentException||cause instanceof IllegalStateException)message=cause.getMessage();
        else if(cause.getClass().getName().contains("Http")||cause.getClass().getName().contains("RestClient"))message=or(cause.getMessage(),"HTTP SATUSEHAT gagal.");
        else message=or(cause.getMessage(),"Proses belum selesai. Periksa koneksi dan catatan pemasangan.");
        toast(action+": "+message,true);
    }
    private void toast(String message,boolean warning){toastText.setText(message);toastText.setToolTipText(message);toastText.setCaretPosition(0);toastText.setForeground(warning?AMBER:GREEN);toastPanel.setBackground(warning?new Color(255,244,224):new Color(236,248,242));toastPanel.setVisible(true);toastTimer.restart();revalidate();repaint();}
    private void hideToast(){toastTimer.stop();toastPanel.setVisible(false);revalidate();repaint();}
    @Override public void removeNotify(){toastTimer.stop();toastPanel.setVisible(false);super.removeNotify();}
    private void showApi(){
        final JDialog d=new JDialog(SwingUtilities.getWindowAncestor(this),"Respon API",Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p=new JPanel(new BorderLayout(0,12));p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        JPanel header=new JPanel(new BorderLayout(10,0));header.setOpaque(false);
        JPanel titleBox=vertical();titleBox.add(label("Respon API",new Font("Segoe UI Semibold",Font.BOLD,20),INK));
        final List<SatuSehatRujukanIGDRanapApi.ApiTrace> traces=apiClient==null?new ArrayList<SatuSehatRujukanIGDRanapApi.ApiTrace>():apiClient.getTraceHistory();
        JLabel actionInfo=label(apiTraceGroups.isEmpty()?"Aksi terakhir: "+or(lastApiAction,"belum ada"):"Dikelompokkan per proses agar error mudah dilacak.",SMALL,MUTED);titleBox.add(Box.createVerticalStrut(3));titleBox.add(actionInfo);header.add(titleBox,BorderLayout.WEST);
        JLabel safeInfo=pill("Authorization tidak disimpan",new Color(236,248,242),GREEN);header.add(safeInfo,BorderLayout.EAST);p.add(header,BorderLayout.NORTH);

        final JPanel center=new JPanel(new BorderLayout(0,8));center.setOpaque(false);
        final JComboBox<String> categoryPicker=new JComboBox<String>();categoryPicker.setFont(BODY);
        final JComboBox<String> responsePicker=new JComboBox<String>();responsePicker.setFont(BODY);
        final JPanel pickers=new JPanel(new GridLayout(1,2,8,0));pickers.setOpaque(false);
        final List<ApiTraceGroup> groups=new ArrayList<ApiTraceGroup>(apiTraceGroups);
        if(!groups.isEmpty()){
            String[] labels=new String[groups.size()];java.text.SimpleDateFormat tf=new java.text.SimpleDateFormat("HH:mm:ss");
            for(int i=0;i<groups.size();i++){ApiTraceGroup g=groups.get(groups.size()-1-i);labels[i]=(g.error?"ERROR • ":"OK • ")+g.action+" • "+Math.max(1,g.end-g.start)+" HTTP • "+tf.format(new java.util.Date(g.timeMillis));}
            categoryPicker.setModel(new DefaultComboBoxModel<String>(labels));pickers.add(categoryPicker);pickers.add(responsePicker);center.add(pickers,BorderLayout.NORTH);
        }
        final JPanel viewer=new JPanel(new BorderLayout());viewer.setOpaque(false);
        final Runnable renderResponse=new Runnable(){@Override public void run(){
            viewer.removeAll();
            if(groups.isEmpty()||traces.isEmpty()){
                JTabbedPane tabs=new JTabbedPane();tabs.setFont(BODY);tabs.addTab("Request",apiJsonPanel(or(lastApiRequest,"—"),"Request terakhir",false));tabs.addTab("Response / Error",apiJsonPanel(or(lastApiResponse,"—"),"Response terakhir",false));tabs.addTab("Keterangan Error",apiMessagePanel("Belum ada keterangan error terkelompok untuk respon ini.",false));viewer.add(tabs,BorderLayout.CENTER);
            }else{
                int gi=Math.max(0,categoryPicker.getSelectedIndex());ApiTraceGroup g=groups.get(groups.size()-1-gi);
                int count=Math.max(0,Math.min(g.end,traces.size())-Math.min(g.start,traces.size()));
                int ri=Math.max(0,responsePicker.getSelectedIndex());if(count==0)ri=0;else if(ri>=count)ri=count-1;
                int traceIndex=Math.min(g.start+ri,traces.size()-1);SatuSehatRujukanIGDRanapApi.ApiTrace tr=traces.get(traceIndex);
                String request=(tr.method+" "+tr.url+"\n\n"+or(tr.request,"—")).trim();String response=or(tr.response,"—");
                String marker=(g.error?"ERROR • ":"OK • ")+g.action+" • Respons "+(ri+1)+" dari "+Math.max(1,count);
                JTabbedPane tabs=new JTabbedPane();tabs.setFont(BODY);tabs.setBackground(Color.WHITE);
                tabs.addTab("Request",apiJsonPanel(request,marker+" • Request",false));
                tabs.addTab(g.error?"Response / ERROR":"Response",apiJsonPanel(response,marker+" • Response",g.error));
                tabs.addTab("Keterangan Error",apiMessagePanel(g.error?or(g.errorMessage,"Proses ini ditandai bermasalah. Periksa Request dan Response untuk detail."):"Tidak ada keterangan error pada proses ini.",g.error));
                viewer.add(tabs,BorderLayout.CENTER);
            }
            viewer.revalidate();viewer.repaint();
        }};
        final Runnable loadResponses=new Runnable(){@Override public void run(){
            if(groups.isEmpty()){renderResponse.run();return;}int gi=Math.max(0,categoryPicker.getSelectedIndex());ApiTraceGroup g=groups.get(groups.size()-1-gi);int start=Math.min(g.start,traces.size()),end=Math.min(g.end,traces.size());
            String[] labels=new String[Math.max(0,end-start)];for(int i=start;i<end;i++){SatuSehatRujukanIGDRanapApi.ApiTrace tr=traces.get(i);String u=tr.url==null?"":tr.url;int slash=u.indexOf("/fhir");if(slash>=0)u=u.substring(slash);labels[i-start]="Respons "+(i-start+1)+" • "+tr.method+" • "+u;}
            responsePicker.setModel(new DefaultComboBoxModel<String>(labels));if(labels.length>0)responsePicker.setSelectedIndex(labels.length-1);renderResponse.run();
        }};
        categoryPicker.addActionListener(e->loadResponses.run());responsePicker.addActionListener(e->renderResponse.run());loadResponses.run();
        center.add(viewer,BorderLayout.CENTER);p.add(center,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));bottom.setOpaque(false);JButton close=button("Tutup",BLUE,Color.WHITE);close.addActionListener(e -> d.dispose());bottom.add(close);p.add(bottom,BorderLayout.SOUTH);
        d.setContentPane(p);d.setSize(1020,700);d.setLocationRelativeTo(this);d.setVisible(true);
    }
    private JPanel apiMessagePanel(String message,boolean error){
        JPanel panel=new JPanel(new BorderLayout());panel.setBackground(Color.WHITE);
        JTextArea text=readOnly(8);text.setFont(BODY);text.setForeground(error?new Color(160,45,45):INK);text.setText(SatuSehatRujukanIGDRanapDraft.safe(message));text.setCaretPosition(0);
        RoundPanel box=new RoundPanel(error?new Color(255,246,246):LIGHT,error?new Color(244,188,188):LINE,14);box.setLayout(new BorderLayout());box.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));box.add(text,BorderLayout.CENTER);panel.add(box,BorderLayout.NORTH);return panel;
    }
    private JPanel apiJsonPanel(String raw,String marker,boolean error){
        JPanel panel=new JPanel(new BorderLayout(0,7));panel.setBackground(Color.WHITE);
        String formatted=formatApiText(raw);String[] lines=formatted.split("\n",-1);
        JPanel bar=new JPanel(new BorderLayout(8,0));bar.setOpaque(false);
        JLabel info=pill(marker+" • baris 1–"+Math.max(1,lines.length),error?new Color(255,240,240):LIGHT,error?new Color(180,45,45):BLUE);bar.add(info,BorderLayout.WEST);
        final String copyText=formatted;String copyCaption=marker.endsWith("Request")?"Salin Request":"Salin Respons";JButton copyAll=button(copyCaption,Color.WHITE,BLUE);copyAll.setIcon(new LineIcon("copy",BLUE,14));copyAll.addActionListener(e->{try{Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(copyText),null);toast("Satu respons API berhasil disalin.",false);}catch(RuntimeException ex){toast("Clipboard sedang tidak tersedia.",true);}});bar.add(copyAll,BorderLayout.EAST);panel.add(bar,BorderLayout.NORTH);
        final DefaultTableModel model=new DefaultTableModel(new String[]{"#","JSON / API"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        for(int i=0;i<lines.length;i++)model.addRow(new Object[]{Integer.valueOf(i+1),lines[i]});
        final JTable table=new JTable(model);table.setFont(new Font("Consolas",Font.PLAIN,12));table.setRowHeight(25);table.setGridColor(new Color(238,242,246));table.setShowVerticalLines(false);table.setSelectionBackground(error?new Color(254,226,226):new Color(219,234,254));table.setSelectionForeground(INK);table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setFont(new Font("Segoe UI Semibold",Font.PLAIN,11));table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setMinWidth(44);table.getColumnModel().getColumn(0).setMaxWidth(54);table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(1).setCellRenderer(new ApiJsonLineRenderer());
        JScrollPane sp=scroll(table);sp.getViewport().setBackground(Color.WHITE);panel.add(sp,BorderLayout.CENTER);return panel;
    }
    private static String formatApiText(String raw){
        String value=raw==null?"":raw.trim();if(value.isEmpty())return "—";
        int split=value.indexOf("\n\n");
        if(split>0){String head=value.substring(0,split).trim(),body=value.substring(split+2).trim();if(isJsonText(body))return head+"\n\n"+prettyJson(body);}
        return isJsonText(value)?prettyJson(value):value;
    }
    private static boolean isJsonText(String text){
        if(text==null)return false;String t=text.trim();return (t.startsWith("{")&&t.endsWith("}"))||(t.startsWith("[")&&t.endsWith("]"));
    }
    private static String prettyJson(String raw){
        StringBuilder out=new StringBuilder();int indent=0;boolean inString=false,escaped=false;
        for(int i=0;i<raw.length();i++){char ch=raw.charAt(i);
            if(inString){out.append(ch);if(escaped)escaped=false;else if(ch=='\\')escaped=true;else if(ch=='\"')inString=false;continue;}
            if(ch=='\"'){inString=true;out.append(ch);continue;}
            if(Character.isWhitespace(ch))continue;
            if(ch=='{'||ch=='['){out.append(ch).append('\n');indent++;appendIndent(out,indent);}
            else if(ch=='}'||ch==']'){out.append('\n');indent=Math.max(0,indent-1);appendIndent(out,indent);out.append(ch);}
            else if(ch==','){out.append(ch).append('\n');appendIndent(out,indent);}
            else if(ch==':'){out.append(": ");}
            else out.append(ch);
        }
        return out.toString();
    }
    private static void appendIndent(StringBuilder out,int level){for(int i=0;i<level;i++)out.append("  ");}
    private static String htmlEscape(String s){return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    private static String colorJsonLine(String line){
        StringBuilder out=new StringBuilder("<html><span style='font-family:Consolas;'>");int i=0;
        while(i<line.length()){char ch=line.charAt(i);
            if(ch==' '){out.append("&nbsp;");i++;continue;}
            if(ch=='\"'){int start=i++;boolean escaped=false;while(i<line.length()){char c=line.charAt(i++);if(escaped)escaped=false;else if(c=='\\')escaped=true;else if(c=='\"')break;}String token=line.substring(start,i);int j=i;while(j<line.length()&&Character.isWhitespace(line.charAt(j)))j++;boolean key=j<line.length()&&line.charAt(j)==':';out.append("<span style='color:").append(key?"#2563EB":"#15803D").append(";'>").append(htmlEscape(token)).append("</span>");continue;}
            if(Character.isDigit(ch)||ch=='-'){int start=i++;while(i<line.length()&&("0123456789+-.eE".indexOf(line.charAt(i))>=0))i++;out.append("<span style='color:#B45309;'>").append(htmlEscape(line.substring(start,i))).append("</span>");continue;}
            if(line.startsWith("true",i)||line.startsWith("false",i)||line.startsWith("null",i)){String token=line.startsWith("true",i)?"true":line.startsWith("false",i)?"false":"null";out.append("<span style='color:#7C3AED;'>").append(token).append("</span>");i+=token.length();continue;}
            if("{}[],:".indexOf(ch)>=0)out.append("<span style='color:#64748B;'>").append(ch).append("</span>");else out.append(htmlEscape(String.valueOf(ch)));i++;
        }
        return out.append("</span></html>").toString();
    }
    private static final class ApiJsonLineRenderer extends javax.swing.table.DefaultTableCellRenderer{
        @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focused,int row,int column){
            JLabel l=(JLabel)super.getTableCellRendererComponent(table,"",selected,focused,row,column);l.setText(colorJsonLine(value==null?"":String.valueOf(value)));l.setOpaque(true);l.setBackground(selected?new Color(219,234,254):Color.WHITE);l.setBorder(BorderFactory.createEmptyBorder(0,6,0,6));return l;
        }
    }
    private static final class ApiCopyRenderer extends JButton implements javax.swing.table.TableCellRenderer{
        ApiCopyRenderer(){super("Salin");setFont(new Font("Segoe UI",Font.PLAIN,11));setForeground(BLUE);setFocusPainted(false);setBorderPainted(false);setContentAreaFilled(false);}
        @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focused,int row,int column){setText("Salin");setBackground(selected?new Color(219,234,254):Color.WHITE);return this;}
    }
    /** Pratinjau eksplisit, tanpa akses database/API; seluruh identitas fiktif. */
    public void showPreview(SatuSehatRujukanIGDRanapDraft.Jenis kind){
        if(!preview)throw new IllegalStateException("Data pratinjau tidak boleh digunakan pada mode Khanza.");
        SatuSehatRujukanIGDRanapRepository.Visit v=new SatuSehatRujukanIGDRanapRepository.Visit();v.noRawat="DEMO/2026/001";v.noRm="DEMO-001";v.name="Pasien Demo A / Data fiktif";v.draftAvailable=false;
        v.draft=new SatuSehatRujukanIGDRanapDraft(v.noRawat,v.noRm,kind);v.draft.set("diagnosis_code","R06.0");v.draft.set("diagnosis_name","Dyspnoea");v.draft.set("doctor","dr. Dokter Demo");v.draft.set("origin",kind==SatuSehatRujukanIGDRanapDraft.Jenis.IGD?"IGD / Observasi":"Bangsal Demo");
        v.draft.set("reason","Memerlukan pelayanan lanjutan sesuai penilaian dokter.");v.draft.set("clinical","Contoh ringkasan pasien. Bukan data atau rekomendasi klinis.");v.draft.set("care",kind==SatuSehatRujukanIGDRanapDraft.Jenis.IGD?"Pelayanan gawat darurat":"Perawatan intensif");v.draft.set("region","Kota Makassar");
        v.draft.set("blood_pressure","110/70");v.draft.set("pulse","88");v.draft.set("respiratory_rate","24");v.draft.set("spo2","94");v.draft.set("temperature","36.8");v.draft.set("consciousness","Contoh / sadar");v.draft.set("measured_at","10/09/2026 14.10 WITA (contoh)");
        applyVisit(v);sources.setText("Semua data merupakan contoh fiktif. Pratinjau tidak mengakses database maupun API.");footerStatus.setText("Pratinjau Java / isian contoh tidak disimpan.");setEditing(true);refresh();
    }
    private void installCriteriaAnswerEditor(JTable table){
        if(table==null||table.getColumnModel().getColumnCount()<4)return;
        table.getColumnModel().getColumn(3).setCellEditor(new CriteriaAnswerEditor());
        table.getColumnModel().getColumn(3).setCellRenderer(new CriteriaAnswerRenderer());
    }
    private static String normalizeBooleanAnswer(String value){
        String v=value==null?"":value.trim().toLowerCase();
        if(v.isEmpty())return "";
        if("true".equals(v)||"ya".equals(v)||"yes".equals(v)||"y".equals(v)||"1".equals(v))return "Ya";
        if("false".equals(v)||"tidak".equals(v)||"no".equals(v)||"n".equals(v)||"0".equals(v))return "Tidak";
        return value==null?"":value.trim();
    }
    private static void fitTableColumns(JTable table,int padding,int maxWidth){
        if(table==null)return;
        javax.swing.table.JTableHeader header=table.getTableHeader();
        for(int col=0;col<table.getColumnCount();col++){
            int width=0;
            javax.swing.table.TableColumn column=table.getColumnModel().getColumn(col);
            javax.swing.table.TableCellRenderer headerRenderer=column.getHeaderRenderer();
            if(headerRenderer==null&&header!=null)headerRenderer=header.getDefaultRenderer();
            if(headerRenderer!=null){
                Component hc=headerRenderer.getTableCellRendererComponent(table,column.getHeaderValue(),false,false,-1,col);
                width=Math.max(width,hc.getPreferredSize().width+padding);
            }
            for(int row=0;row<table.getRowCount();row++){
                javax.swing.table.TableCellRenderer renderer=table.getCellRenderer(row,col);
                Component cc=table.prepareRenderer(renderer,row,col);
                width=Math.max(width,cc.getPreferredSize().width+padding);
            }
            if(maxWidth>0)width=Math.min(width,maxWidth);
            column.setMinWidth(width);
            column.setPreferredWidth(width);
        }
    }
    private static final class CriteriaAnswerRenderer extends javax.swing.table.DefaultTableCellRenderer{
        @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focused,int row,int column){
            int modelRow=table.convertRowIndexToModel(row);String type=String.valueOf(table.getModel().getValueAt(modelRow,2));
            Object display=value;
            String linkId=String.valueOf(table.getModel().getValueAt(modelRow,0));
            String question=String.valueOf(table.getModel().getValueAt(modelRow,1));
            if("boolean".equalsIgnoreCase(type))display=normalizeBooleanAnswer(value==null?"":String.valueOf(value));
            else if(isAreaCriterion(linkId,question)){
                String raw=value==null?"":String.valueOf(value);String[] part=raw.split("\\|",-1);
                if(part.length>=3)display=part[1]+" - "+part[2];
            }
            super.getTableCellRendererComponent(table,display,selected,focused,row,column);
            setForeground(selected?Color.WHITE:INK);setBackground(selected?BLUE:Color.WHITE);setOpaque(true);
            return this;
        }
    }
    private static final class CriteriaAnswerEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor{
        private final JComboBox combo=new JComboBox(new String[]{"","Ya","Tidak"});
        private final JTextField text=new JTextField();
        private JComponent active;
        CriteriaAnswerEditor(){combo.setFont(BODY);text.setFont(BODY);combo.putClientProperty("JComboBox.isTableCellEditor",Boolean.TRUE);}
        @Override public Object getCellEditorValue(){return active==combo?String.valueOf(combo.getSelectedItem()):text.getText();}
        @Override public Component getTableCellEditorComponent(JTable table,Object value,boolean selected,int row,int column){
            int modelRow=table.convertRowIndexToModel(row);String type=String.valueOf(table.getModel().getValueAt(modelRow,2));
            if("boolean".equalsIgnoreCase(type)){active=combo;combo.setSelectedItem(normalizeBooleanAnswer(value==null?"":String.valueOf(value)));return combo;}
            active=text;text.setText(value==null?"":String.valueOf(value));return text;
        }
    }
    private static DocumentListener listener(final Runnable r){return new DocumentListener(){public void insertUpdate(DocumentEvent e){r.run();}public void removeUpdate(DocumentEvent e){r.run();}public void changedUpdate(DocumentEvent e){r.run();}};}
    private static boolean isLegacyClinicalSpecialityCode(String code){
        // S001.09 berasal dari contoh lama dan terbukti ditolak oleh SATUSEHAT STG saat ini.
        // Jangan memblokir seluruh pola Sxxx.xx tanpa bukti terminologi aktifnya.
        String c=SatuSehatRujukanIGDRanapDraft.safe(code).trim().toUpperCase(java.util.Locale.ENGLISH);
        return "S001.09".equals(c);
    }
    private static String or(String s,String fallback){return s==null||s.trim().isEmpty()?fallback:s;}
    private static JLabel label(String s,Font f,Color c){JLabel l=new JLabel(s);l.setFont(f);l.setForeground(c);l.setAlignmentX(0);return l;}
    private static JLabel pill(String s,final Color bg,Color fg){JLabel l=new JLabel(s){@Override protected void paintComponent(Graphics g){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(bg);p.fillRoundRect(0,0,getWidth(),getHeight(),10,10);p.dispose();super.paintComponent(g);}};l.setFont(SMALL);l.setForeground(fg);l.setAlignmentX(0);l.setBorder(BorderFactory.createEmptyBorder(7,11,7,11));return l;}
    private static JPanel vertical(){JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setOpaque(false);p.setAlignmentX(0);return p;}
    private static JPanel flow(){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));p.setOpaque(false);p.setAlignmentX(0);return p;}
    private static JPanel grid(){JPanel p=new JPanel(new GridBagLayout());p.setOpaque(false);p.setAlignmentX(0);return p;}
    private static JPanel card(String title,String sub){RoundPanel p=new RoundPanel(Color.WHITE,LINE,18);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setAlignmentX(0);p.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        if(title!=null){p.add(label(title,STRONG,INK));p.add(Box.createVerticalStrut(7));}if(sub!=null){JTextArea t=readOnly(1);t.setText(sub);p.add(t);p.add(Box.createVerticalStrut(7));}return p;}
    private static JTextArea readOnly(int rows){JTextArea t=new JTextArea(rows,1){@Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}};t.setEditable(false);t.setFont(SMALL);t.setForeground(MUTED);t.setOpaque(false);t.setLineWrap(true);t.setWrapStyleWord(true);t.setAlignmentX(0);return t;}
    private static JPanel note(String s,Color bg,Color fg){RoundPanel p=new RoundPanel(bg,bg,12);p.setLayout(new BorderLayout());p.setBorder(BorderFactory.createEmptyBorder(11,12,11,12));JTextArea t=readOnly(3);t.setText(s);t.setForeground(fg);p.add(t);p.setAlignmentX(0);return p;}
    private static JPanel empty(String icon,String title,String message){JPanel p=vertical();p.setBorder(BorderFactory.createEmptyBorder(28,20,28,20));JLabel i=new JLabel(new LineIcon(icon,BLUE,36));i.setAlignmentX(.5f);p.add(i);p.add(Box.createVerticalStrut(14));JLabel l=label(title,STRONG,INK);l.setAlignmentX(.5f);p.add(l);p.add(Box.createVerticalStrut(12));JTextArea t=readOnly(3);t.setText(message);p.add(t);return p;}
    private static JButton button(String text,final Color bg,Color fg){JButton b=new JButton(text);b.setFont(BODY);b.setForeground(fg);b.setBackground(bg);b.setOpaque(false);b.setContentAreaFilled(false);b.setAlignmentX(0);
        b.setBorder(BorderFactory.createEmptyBorder(9,13,9,13));b.setUI(new BasicButtonUI(){@Override public void paint(Graphics g,JComponent c){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(c.isEnabled()?bg:new Color(232,238,243));p.fillRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,12,12);p.setColor(LINE);p.drawRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,12,12);p.dispose();super.paint(g,c);}
            @Override protected void paintText(Graphics g,AbstractButton b,Rectangle r,String text){if(b.isEnabled())super.paintText(g,b,r,text);else{g.setColor(MUTED);g.drawString(text,r.x,r.y+g.getFontMetrics().getAscent());}}
        });b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private static void styleToggle(JToggleButton b,final boolean tab){
        b.setFont(BODY);b.setForeground(BLUE);b.setOpaque(false);b.setContentAreaFilled(false);b.setRolloverEnabled(true);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setUI(new BasicButtonUI(){@Override public void paint(Graphics g,JComponent c){AbstractButton b=(AbstractButton)c;Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(b.isSelected()?LIGHT:b.getModel().isRollover()?BG:Color.WHITE);
            if(tab){p.fillRect(0,0,c.getWidth(),c.getHeight());if(b.isSelected()){p.setColor(BLUE);p.fillRoundRect(12,c.getHeight()-3,c.getWidth()-24,3,3,3);}}
            else{p.fillRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,12,12);p.setColor(b.isSelected()?new Color(171,213,242):LINE);p.drawRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,12,12);}
            if(b.hasFocus()){p.setColor(BLUE);p.drawRoundRect(3,3,c.getWidth()-7,c.getHeight()-7,8,8);}p.dispose();super.paint(g,c);}});
    }
    private static void styleInput(JTextComponent t){t.setAlignmentX(0);t.setFont(BODY);t.setForeground(INK);t.setBackground(Color.WHITE);t.setOpaque(false);t.setCaretColor(BLUE);t.setDisabledTextColor(MUTED);t.setBorder(BorderFactory.createEmptyBorder(7,10,7,10));}
    private void comboField(JPanel grid,String text,JComboBox<String> combo,int x,int y,int span){
        JPanel box=vertical();JLabel l=label(text,SMALL,MUTED);box.add(l);box.add(Box.createVerticalStrut(6));
        combo.setPreferredSize(new Dimension(100,36));combo.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));l.setLabelFor(combo);box.add(combo);
        int h=box.getPreferredSize().height;box.setPreferredSize(new Dimension(100*span,h));box.setMinimumSize(new Dimension(70*span,h));
        GridBagConstraints c=new GridBagConstraints();c.gridx=x;c.gridy=y;c.gridwidth=span;c.weightx=span;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;c.insets=new Insets(9,0,4,10);grid.add(box,c);
    }
    private void fieldFixed(JPanel grid,String text,String key,int x,int y,int width,int chars){
        JPanel box=vertical();JLabel l=label(text,SMALL,MUTED);box.add(l);box.add(Box.createVerticalStrut(6));
        int fixedWidth=Math.max(width,l.getPreferredSize().width+18);
        JTextField t=new JTextField(chars);styleInput(t);t.setColumns(chars);t.setName(key);t.getAccessibleContext().setAccessibleName(text);l.setLabelFor(t);fields.put(key,t);
        InputFrame frame=new InputFrame(t);frame.setAlignmentX(0);frame.add(t,BorderLayout.CENTER);frame.setPreferredSize(new Dimension(fixedWidth,36));frame.setMinimumSize(new Dimension(fixedWidth,36));frame.setMaximumSize(new Dimension(fixedWidth,36));box.add(frame);
        int h=box.getPreferredSize().height;box.setPreferredSize(new Dimension(fixedWidth,h));box.setMinimumSize(new Dimension(fixedWidth,h));box.setMaximumSize(new Dimension(fixedWidth,h));
        GridBagConstraints c=new GridBagConstraints();c.gridx=x;c.gridy=y;c.weightx=0;c.fill=GridBagConstraints.NONE;c.anchor=GridBagConstraints.NORTHWEST;c.insets=new Insets(9,0,4,10);grid.add(box,c);
    }
    private void fieldWithIconButtonFixed(JPanel grid,String text,String key,JButton action,int x,int y,int width,int chars){
        JPanel box=vertical();JLabel l=label(text,SMALL,MUTED);box.add(l);box.add(Box.createVerticalStrut(6));
        JTextField t=new JTextField(chars);styleInput(t);t.setColumns(chars);t.setName(key);t.getAccessibleContext().setAccessibleName(text);l.setLabelFor(t);fields.put(key,t);
        InputFrame frame=new InputFrame(t);frame.setAlignmentX(0);frame.add(t,BorderLayout.CENTER);
        JPanel east=new JPanel(new BorderLayout());east.setOpaque(false);action.setPreferredSize(new Dimension(36,34));action.setMinimumSize(new Dimension(36,34));action.setMaximumSize(new Dimension(36,34));east.add(action,BorderLayout.CENTER);frame.add(east,BorderLayout.EAST);
        frame.setPreferredSize(new Dimension(width,36));frame.setMinimumSize(new Dimension(width,36));frame.setMaximumSize(new Dimension(width,36));box.add(frame);
        int h=box.getPreferredSize().height;box.setPreferredSize(new Dimension(width,h));box.setMinimumSize(new Dimension(width,h));box.setMaximumSize(new Dimension(width,h));
        GridBagConstraints c=new GridBagConstraints();c.gridx=x;c.gridy=y;c.weightx=0;c.fill=GridBagConstraints.NONE;c.anchor=GridBagConstraints.NORTHWEST;c.insets=new Insets(9,0,4,10);grid.add(box,c);
    }
    private void fieldWithButton(JPanel grid,String text,String key,JButton action,int x,int y,int span){
        JPanel box=vertical();JLabel l=label(text,SMALL,MUTED);box.add(l);box.add(Box.createVerticalStrut(6));
        JTextField t=new JTextField(6);styleInput(t);t.setName(key);t.getAccessibleContext().setAccessibleName(text);l.setLabelFor(t);fields.put(key,t);
        InputFrame frame=new InputFrame(t);frame.setAlignmentX(0);frame.add(t,BorderLayout.CENTER);
        JPanel east=new JPanel(new BorderLayout());east.setOpaque(false);action.setPreferredSize(new Dimension(action.getText()==null||action.getText().trim().isEmpty()?36:Math.max(118,action.getPreferredSize().width),34));east.add(action,BorderLayout.CENTER);frame.add(east,BorderLayout.EAST);
        frame.setPreferredSize(new Dimension(100,36));frame.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));box.add(frame);
        int fieldHeight=box.getPreferredSize().height;box.setPreferredSize(new Dimension(100*span,fieldHeight));box.setMinimumSize(new Dimension(70*span,fieldHeight));
        GridBagConstraints c=new GridBagConstraints();c.gridx=x;c.gridy=y;c.gridwidth=span;c.weightx=span;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;c.insets=new Insets(9,0,4,10);grid.add(box,c);
    }
    private void field(JPanel grid,String text,String key,int x,int y,int span,boolean multi){field(grid,label(text,SMALL,MUTED),key,x,y,span,multi);}
    private void field(JPanel grid,JLabel label,String key,int x,int y,int span,boolean multi){JPanel box=vertical();box.add(label);box.add(Box.createVerticalStrut(6));JTextComponent t;
        if(multi){JTextArea a=new JTextArea(3,5);a.setLineWrap(true);a.setWrapStyleWord(true);t=a;}else t=new JTextField(6);
        styleInput(t);t.setName(key);t.getAccessibleContext().setAccessibleName(label.getText());label.setLabelFor(t);fields.put(key,t);
        InputFrame frame=new InputFrame(t);frame.setAlignmentX(0);
        if(multi){JScrollPane s=scroll(t);s.setOpaque(false);s.getViewport().setOpaque(false);frame.add(s,BorderLayout.CENTER);frame.setPreferredSize(new Dimension(100,76));}
        else{frame.add(t,BorderLayout.CENTER);frame.setPreferredSize(new Dimension(100,36));frame.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));}
        box.add(frame);
        int fieldHeight=box.getPreferredSize().height;box.setPreferredSize(new Dimension(100*span,fieldHeight));box.setMinimumSize(new Dimension(70*span,fieldHeight));
        GridBagConstraints c=new GridBagConstraints();c.gridx=x;c.gridy=y;c.gridwidth=span;c.weightx=span;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;c.insets=new Insets(9,0,4,10);grid.add(box,c);}
    private static JTable workflowTable(DefaultTableModel model){
        JTable t=new JTable(model);t.setFont(BODY);t.setRowHeight(32);t.setGridColor(LINE);t.setShowVerticalLines(false);t.setFillsViewportHeight(true);
        t.setSelectionBackground(BLUE);t.setSelectionForeground(Color.WHITE);t.setForeground(INK);t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.getTableHeader().setFont(SMALL);t.getTableHeader().setReorderingAllowed(false);t.setAutoCreateRowSorter(true);return t;
    }
    private static DefaultTableModel model(String[] headers){return new DefaultTableModel(headers,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
    private static JScrollPane scroll(Component c){JScrollPane s=new JScrollPane(c);s.setBorder(BorderFactory.createEmptyBorder());s.getViewport().setBackground(c instanceof JTable?Color.WHITE:BG);s.getVerticalScrollBar().setUnitIncrement(18);s.getVerticalScrollBar().setPreferredSize(new Dimension(9,0));
        s.getVerticalScrollBar().setUI(new BasicScrollBarUI(){@Override protected void configureScrollBarColors(){thumbColor=new Color(181,199,214);trackColor=BG;}@Override protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}@Override protected JButton createIncreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}});s.setAlignmentX(0);return s;}
    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;
        PlaceholderTextField(String placeholder){this.placeholder=placeholder==null?"":placeholder;}
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(getText()!=null&&!getText().isEmpty())return;
            Graphics2D p=(Graphics2D)g.create();
            p.setFont(getFont());p.setColor(MUTED);
            Insets in=getInsets();FontMetrics fm=p.getFontMetrics();
            int y=(getHeight()-fm.getHeight())/2+fm.getAscent();
            p.drawString(placeholder,Math.max(in.left,10),y);p.dispose();
        }
    }
    private static final class Vertical extends JPanel implements Scrollable {
        Vertical(){setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setOpaque(false);}
        public Dimension getPreferredScrollableViewportSize(){return new Dimension(1000,650);}
        public int getScrollableUnitIncrement(Rectangle r,int o,int d){return 18;}
        public int getScrollableBlockIncrement(Rectangle r,int o,int d){return Math.max(18,r.height-30);}
        public boolean getScrollableTracksViewportWidth(){return true;}public boolean getScrollableTracksViewportHeight(){return false;}
    }
    private static final class RoundPanel extends JPanel {
        final Color fill,stroke;final int radius;RoundPanel(Color fill,Color stroke,int radius){this.fill=fill;this.stroke=stroke;this.radius=radius;setOpaque(false);}
        @Override protected void paintComponent(Graphics g){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(fill);p.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);p.setColor(stroke);p.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);p.dispose();super.paintComponent(g);}
    }
    /** Background and rounded edge belong to the container, including multiline fields. */
    private static final class InputFrame extends JPanel {
        private final JTextComponent editor;
        InputFrame(JTextComponent editor){super(new BorderLayout());this.editor=editor;setOpaque(false);setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
            editor.addFocusListener(new java.awt.event.FocusAdapter(){@Override public void focusGained(java.awt.event.FocusEvent e){repaint();}@Override public void focusLost(java.awt.event.FocusEvent e){repaint();}});
            editor.addPropertyChangeListener("enabled",e -> repaint());
        }
        @Override protected void paintComponent(Graphics g){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(editor.isEnabled()?Color.WHITE:new Color(247,249,251));p.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
            p.setColor(editor.hasFocus()?BLUE:LINE);p.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);p.dispose();super.paintComponent(g);
        }
        @Override protected void paintChildren(Graphics g){Graphics2D p=(Graphics2D)g.create();p.clip(new java.awt.geom.RoundRectangle2D.Double(1,1,getWidth()-2,getHeight()-2,10,10));super.paintChildren(p);p.dispose();}
    }
    private static final class StageNumberIcon implements Icon {
        private final int number;
        StageNumberIcon(int number){this.number=number;}
        public int getIconWidth(){return 26;}public int getIconHeight(){return 26;}
        public void paintIcon(Component c,Graphics graphics,int x,int y){Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(BLUE);g.fillOval(x,y,26,26);
            g.setFont(STRONG);g.setColor(Color.WHITE);String text=Integer.toString(number);FontMetrics fm=g.getFontMetrics();
            g.drawString(text,x+(26-fm.stringWidth(text))/2,y+(26-fm.getHeight())/2+fm.getAscent());g.dispose();
        }
    }
    private static final class LineIcon implements Icon {
        final String kind;final Color color;final int size;LineIcon(String kind,Color color,int size){this.kind=kind;this.color=color;this.size=size;}
        public int getIconWidth(){return size;}public int getIconHeight(){return size;}
        public void paintIcon(Component c,Graphics graphics,int x,int y){Graphics2D g=(Graphics2D)graphics.create();g.translate(x,y);g.scale(size/24.0,size/24.0);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(color);g.setStroke(new BasicStroke(1.6f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            if("hospital".equals(kind)){g.drawRoundRect(4,3,16,19,3,3);g.drawLine(9,8,15,8);g.drawLine(12,5,12,11);g.drawRect(10,16,4,6);}
            else if("user".equals(kind)){g.drawOval(8,3,8,8);g.drawArc(4,13,16,15,0,180);}
            else if("send".equals(kind)){g.drawLine(3,4,22,12);g.drawLine(22,12,3,21);g.drawLine(3,21,6,12);g.drawLine(6,12,3,4);g.drawLine(6,12,22,12);}
            else if("inbox".equals(kind)){g.drawRoundRect(3,5,18,16,3,3);g.drawLine(3,14,8,14);g.drawLine(8,14,10,17);g.drawLine(10,17,14,17);g.drawLine(14,17,16,14);g.drawLine(16,14,21,14);}
            else if("history".equals(kind)){g.drawArc(3,3,18,18,-40,310);g.drawLine(12,7,12,12);g.drawLine(12,12,16,14);g.drawLine(3,2,3,8);g.drawLine(3,8,8,8);}
            else if("search".equals(kind)){g.drawOval(3,3,13,13);g.drawLine(15,15,22,22);}
            else if("reload".equals(kind)){g.drawArc(4,4,16,16,40,290);g.drawLine(20,3,20,9);g.drawLine(20,9,14,9);}
            else if("copy".equals(kind)){g.drawRoundRect(7,7,13,14,2,2);g.drawLine(4,16,4,3);g.drawLine(4,3,16,3);}
            else if("print".equals(kind)){g.drawRect(6,3,12,6);g.drawRoundRect(3,9,18,9,2,2);g.drawRect(6,14,12,7);g.drawLine(17,12,18,12);}
            else if("save".equals(kind)){g.drawRoundRect(3,3,18,18,2,2);g.drawRect(7,3,10,6);g.drawRect(7,14,10,7);}
            else if("plus".equals(kind)){g.drawOval(4,4,16,16);g.drawLine(12,8,12,16);g.drawLine(8,12,16,12);}
            else if("eye".equals(kind)){g.drawOval(7,8,10,8);g.drawArc(2,5,20,14,15,150);g.drawArc(2,5,20,14,195,150);g.fillOval(10,10,4,4);}
            else{g.drawLine(8,6,2,12);g.drawLine(2,12,8,18);g.drawLine(16,6,22,12);g.drawLine(22,12,16,18);}g.dispose();}
    }
}
