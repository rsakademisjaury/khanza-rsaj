package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.springframework.web.client.HttpStatusCodeException;

public class SatuSehatRujukanMasukForm extends JDialog {
    private static final Color BORDER_COLOR = new Color(240, 245, 235);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color PANEL_COLOR = new Color(255, 255, 254);
    private static final Color ACTION_COLOR = new Color(250, 255, 245);
    private static final Font DEFAULT_FONT = new Font("Tahoma", Font.PLAIN, 11);
    private static final int[] RUJUKAN_COLUMN_WIDTHS = new int[]{220, 80, 220, 140, 180, 160, 220, 200, 145, 180, 180, 80, 145, 0, 0};
    private static final int[] TASK_COLUMN_WIDTHS = new int[]{190, 80, 190, 140, 180, 150, 220, 180, 145, 0};
    private static final int[] SERVICE_REQUEST_COLUMN_WIDTHS = new int[]{220, 80, 220, 140, 180, 160, 220, 200, 145, 180, 0};

    private final JTextField orgId = new JTextField();
    private final JTextField patientId = new JTextField();
    private final JComboBox<String> statusTask = new JComboBox<String>(new String[]{"requested", "completed", "accepted", "rejected", "Semua"});
    private final JComboBox<String> statusServiceRequest = new JComboBox<String>(new String[]{"active", "completed", "on-hold", "revoked", "Semua"});
    private final DefaultTableModel tabModeRujukanMasuk = createRujukanMasukModel();
    private final DefaultTableModel tabModeTask = createTaskModel();
    private final DefaultTableModel tabModeServiceRequest = createServiceRequestModel();
    private final JTable tbRujukanMasuk = new JTable(tabModeRujukanMasuk);
    private final JTable tbTask = new JTable(tabModeTask);
    private final JTable tbServiceRequest = new JTable(tabModeServiceRequest);
    private final ObjectMapper mapper = new ObjectMapper();
    private final SatuSehatKirimRujukan rujukan = new SatuSehatKirimRujukan();
    private final Map<String, String> patientNameCache = new LinkedHashMap<String, String>();
    private JTabbedPane tabData;

    public SatuSehatRujukanMasukForm(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        isiDefault();
    }

    private void initComponents() {
        setTitle("ServiceRequest SATUSEHAT");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(true);
        setMinimumSize(new Dimension(1100, 680));
        setPreferredSize(new Dimension(1240, 760));
        setLayout(new BorderLayout());

        widget.InternalFrame internalFrame = new widget.InternalFrame();
        internalFrame.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_COLOR),
                "::[ ServiceRequest SATUSEHAT ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, DEFAULT_FONT, TEXT_COLOR));
        internalFrame.setFont(new Font("Tahoma", Font.ITALIC, 12));
        internalFrame.setLayout(new BorderLayout(1, 1));

        internalFrame.add(filterPanel(), BorderLayout.PAGE_START);
        internalFrame.add(dataPanel(), BorderLayout.CENTER);
        internalFrame.add(actionPanel(), BorderLayout.PAGE_END);
        add(internalFrame, BorderLayout.CENTER);
    }

    private JPanel filterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(label("Organization ID"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(orgId, c);

        c.gridx = 2;
        c.weightx = 0;
        panel.add(label("Patient ID Task"), c);
        c.gridx = 3;
        c.weightx = 0.7;
        panel.add(patientId, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(label("Status Task"), c);
        c.gridx = 1;
        c.weightx = 0.25;
        panel.add(statusTask, c);

        c.gridx = 2;
        c.weightx = 0;
        panel.add(label("Status ServiceRequest"), c);
        c.gridx = 3;
        c.weightx = 0.25;
        panel.add(statusServiceRequest, c);

        return panel;
    }

    private JPanel dataPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        tabData = new JTabbedPane();
        tabData.setFont(DEFAULT_FONT);
        tabData.addTab("Rujukan Masuk", tableScroll(tbRujukanMasuk));

        panel.add(tabData, BorderLayout.CENTER);
        prepareTable(tbRujukanMasuk, 13, RUJUKAN_COLUMN_WIDTHS);
        hideColumn(tbRujukanMasuk, 14);
        prepareTable(tbTask, 9, TASK_COLUMN_WIDTHS);
        prepareTable(tbServiceRequest, 10, SERVICE_REQUEST_COLUMN_WIDTHS);
        return panel;
    }

    private widget.ScrollPane tableScroll(JTable table) {
        widget.ScrollPane scroll = new widget.ScrollPane();
        scroll.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        scroll.setViewportView(table);
        return scroll;
    }

    private JPanel actionPanel() {
        widget.panelisi actions = new widget.panelisi();
        actions.setName("panelGlass8");
        actions.setPreferredSize(new Dimension(44, 54));
        actions.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        JButton btnAmbilTask = button("Ambil Rujukan", "Search-16x16.png", 140);
        JButton btnTerima = button("Terima Task", "accept.png", 122);
        JButton btnTolak = button("Tolak Task", "stop_f2.png", 116);
        JButton btnTutup = button("Tutup", "exit.png", 92);

        btnAmbilTask.addActionListener(e -> ambilTaskMasuk());
        btnTerima.addActionListener(e -> responTaskTerpilih(true));
        btnTolak.addActionListener(e -> responTaskTerpilih(false));
        btnTutup.addActionListener(e -> dispose());

        actions.add(btnAmbilTask);
        actions.add(btnTerima);
        actions.add(btnTolak);
        actions.add(btnTutup);
        return actions;
    }

    private void isiDefault() {
        orgId.setText(koneksiDB.IDSATUSEHAT());
        statusTask.setSelectedItem("requested");
        statusServiceRequest.setSelectedItem("active");
    }

    private void ambilTaskMasuk() {
        ambilTaskMasuk("");
    }

    private void ambilTaskMasuk(String logAwal) {
        if (!validOrg()) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<TaskMasukResult, Void>() {
            protected TaskMasukResult doInBackground() throws Exception {
                SatuSehatKirimRujukan.KirimResult serviceRequestResult = rujukan.cariServiceRequestRujukanMasuk(
                        orgId.getText().trim(), selectedStatus(statusServiceRequest));
                Set<String> taskReferences = taskReferencesFromServiceRequest(serviceRequestResult.root);
                ObjectNode taskBundle;
                if(!patientId.getText().trim().equals("")){
                    SatuSehatKirimRujukan.KirimResult taskResult = rujukan.cariTaskRujukanMasuk(
                            orgId.getText().trim(), patientId.getText().trim(), selectedStatus(statusTask));
                    taskBundle = salinTaskBundle(taskResult.root);
                }else{
                    taskBundle = bundleTaskKosong();
                }
                for (String taskReference : taskReferences) {
                    if(taskBundleBerisiTask(taskBundle, taskReference)){
                        continue;
                    }
                    SatuSehatKirimRujukan.KirimResult taskFromServiceRequest = rujukan.ambilTask(taskReference);
                    ObjectNode entry = mapper.createObjectNode();
                    entry.put("fullUrl", taskReference);
                    entry.set("resource", taskFromServiceRequest.root);
                    ((ArrayNode) taskBundle.withArray("entry")).add(entry);
                }
                taskBundle.put("total", taskBundle.path("entry").size());
                siapkanNamaPasienDariTask(taskBundle);
                siapkanNamaPasien(serviceRequestResult.root, taskBundle);
                return new TaskMasukResult(serviceRequestResult.root, taskBundle, taskBundle.path("entry").size());
            }

            protected void done() {
                try {
                    TaskMasukResult result = get();
                    isiTabelServiceRequest(result.serviceRequestRoot);
                    isiTabelTask(result.taskRoot, logAwal);
                    isiTabelRujukanMasuk(result.serviceRequestRoot, result.taskRoot);
                    if (result.taskReferenceCount == 0) {
                        System.out.println("JSON : " + compact(result.taskRoot));
                    }
                    tabData.setSelectedIndex(0);
                } catch (Exception e) {
                    tampilkanError("Gagal mengambil Task rujukan masuk", e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void ambilServiceRequestMasuk() {
        if (!validOrg()) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<SatuSehatKirimRujukan.KirimResult, Void>() {
            protected SatuSehatKirimRujukan.KirimResult doInBackground() throws Exception {
                SatuSehatKirimRujukan.KirimResult result = rujukan.cariServiceRequestRujukanMasuk(orgId.getText().trim(), selectedStatus(statusServiceRequest));
                siapkanNamaPasien(result.root, null);
                return result;
            }

            protected void done() {
                try {
                    SatuSehatKirimRujukan.KirimResult result = get();
                    isiTabelServiceRequest(result.root);
                    isiTabelRujukanMasuk(result.root, null);
                    tabData.setSelectedIndex(0);
                } catch (Exception e) {
                    tampilkanError("Gagal mengambil ServiceRequest rujukan masuk", e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private ObjectNode salinTaskBundle(JsonNode root) {
        ObjectNode bundle = bundleTaskKosong();
        ArrayNode entries = mapper.createArrayNode();
        for(JsonNode entry : root.path("entry")){
            entries.add(entry);
        }
        bundle.set("entry", entries);
        bundle.put("total", entries.size());
        return bundle;
    }

    private ObjectNode bundleTaskKosong() {
        ObjectNode bundle = mapper.createObjectNode();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "searchset");
        bundle.put("total", 0);
        bundle.set("entry", mapper.createArrayNode());
        return bundle;
    }

    private boolean taskBundleBerisiTask(JsonNode taskBundle, String taskReference) {
        String taskId = taskIdFromReference(taskReference);
        if(taskId.equals("")){
            return false;
        }
        for(JsonNode entry : taskBundle.path("entry")){
            if(taskId.equals(entry.path("resource").path("id").asText())){
                return true;
            }
        }
        return false;
    }

    private void responTaskTerpilih(boolean diterima) {
        String taskId = selectedTaskId();
        if (taskId.equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih rujukan yang memiliki Task terlebih dahulu.");
            return;
        }
        if (diterima) {
            tampilkanKonfirmasiTerimaDenganDataPendukung(taskId);
            return;
        }
        String pesan = diterima ? "menerima" : "menolak";
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin " + pesan + " Task " + taskId + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }
        kirimResponTask(taskId, diterima);
    }

    private void tampilkanKonfirmasiTerimaDenganDataPendukung(String taskId) {
        JsonNode serviceRequest = selectedServiceRequestNode();
        if (serviceRequest == null || serviceRequest.isMissingNode()) {
            int konfirmasi = JOptionPane.showConfirmDialog(this,
                    "ServiceRequest belum ditemukan untuk Task " + taskId + ".\nTerima Task approval rujukan ini?",
                    "Konfirmasi Terima Task", JOptionPane.YES_NO_OPTION);
            if(konfirmasi == JOptionPane.YES_OPTION){
                kirimResponTask(taskId, true);
            }
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<ObjectNode, Void>() {
            protected ObjectNode doInBackground() throws Exception {
                return ambilDataPendukungDariServiceRequest(serviceRequest);
            }

            protected void done() {
                try {
                    ObjectNode bundle = get();
                    int konfirmasi = JOptionPane.showConfirmDialog(SatuSehatRujukanMasukForm.this,
                            panelKonfirmasiTerima(taskId, serviceRequest, bundle),
                            "Konfirmasi Terima Rujukan", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (konfirmasi == JOptionPane.YES_OPTION) {
                        kirimResponTask(taskId, true);
                    }
                } catch (Exception e) {
                    tampilkanError("Gagal mengambil data pendukung untuk konfirmasi", e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private JPanel panelKonfirmasiTerima(String taskId, JsonNode serviceRequest, JsonNode bundle) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JLabel("Yakin menerima rujukan Task " + taskId + "?"), BorderLayout.PAGE_START);
        JTextArea detail = new JTextArea(ringkasanKonfirmasiPendukung(serviceRequest, bundle), 18, 82);
        detail.setEditable(false);
        detail.setFont(DEFAULT_FONT);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(detail);
        scroll.setPreferredSize(new Dimension(760, 360));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private String ringkasanKonfirmasiPendukung(JsonNode serviceRequest, JsonNode bundle) {
        StringBuilder builder = new StringBuilder();
        appendSummaryLine(builder, "ServiceRequest", serviceRequest.path("id").asText());
        appendSummaryLine(builder, "Pasien", patientDisplay(serviceRequest, null));
        appendSummaryLine(builder, "Jenis", codingDisplay(serviceRequest.path("code")));
        appendSummaryLine(builder, "Instruksi", serviceRequest.path("patientInstruction").asText());
        appendSummaryLine(builder, "Catatan", noteText(serviceRequest.path("note")));
        builder.append("\nData Pendukung:\n");
        int no = 1;
        for (JsonNode entry : bundle.path("entry")) {
            JsonNode resource = entry.path("resource");
            String reference = entry.path("fullUrl").asText();
            builder.append(no++).append(". ")
                    .append(firstNonEmpty(resource.path("resourceType").asText(), "-"))
                    .append(" | ").append(firstNonEmpty(resource.path("id").asText(), reference));
            String jenis = jenisResource(resource);
            if (!jenis.equals("")) {
                builder.append(" | ").append(jenis);
            }
            String ringkasan = ringkasanResource(resource);
            if (!ringkasan.equals("")) {
                builder.append("\n   ").append(ringkasan);
            }
            builder.append("\n");
        }
        if (no == 1) {
            builder.append("- Tidak ada data pendukung.\n");
        }
        return builder.toString();
    }

    private void appendSummaryLine(StringBuilder builder, String label, String value) {
        String text = value == null ? "" : value.trim();
        if (text.equals("")) {
            return;
        }
        builder.append(label).append(" : ").append(text).append("\n");
    }

    private void kirimResponTask(String taskId, boolean diterima) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<SatuSehatKirimRujukan.KirimResult, Void>() {
            protected SatuSehatKirimRujukan.KirimResult doInBackground() throws Exception {
                return rujukan.kirimResponTaskPatch(taskId, diterima);
            }

            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(SatuSehatRujukanMasukForm.this, "Respon Task berhasil dikirim.");
                    ambilTaskMasuk("");
                } catch (Exception e) {
                    tampilkanError("Gagal mengirim respon Task", e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void tampilkanDataPendukungTerpilih() {
        JsonNode serviceRequest = selectedServiceRequestNode();
        if (serviceRequest == null || serviceRequest.isMissingNode()) {
            JOptionPane.showMessageDialog(this, "Pilih ServiceRequest rujukan masuk terlebih dahulu.");
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<ObjectNode, Void>() {
            protected ObjectNode doInBackground() throws Exception {
                return ambilDataPendukungDariServiceRequest(serviceRequest);
            }

            protected void done() {
                try {
                    JOptionPane.showMessageDialog(SatuSehatRujukanMasukForm.this,
                            panelDataPendukung(serviceRequest, get()),
                            "Data Pendukung Rujukan", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    tampilkanError("Gagal mengambil data pendukung", e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private JPanel panelDataPendukung(JsonNode serviceRequest, JsonNode bundle) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JLabel("Data pendukung ServiceRequest " + serviceRequest.path("id").asText()), BorderLayout.PAGE_START);
        JTextArea detail = new JTextArea(ringkasanKonfirmasiPendukung(serviceRequest, bundle), 18, 82);
        detail.setEditable(false);
        detail.setFont(DEFAULT_FONT);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(detail);
        scroll.setPreferredSize(new Dimension(760, 360));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JsonNode selectedServiceRequestNode() {
        try {
            if (tabData.getSelectedIndex() == 0 && tbRujukanMasuk.getSelectedRow() >= 0) {
                int row = tbRujukanMasuk.convertRowIndexToModel(tbRujukanMasuk.getSelectedRow());
                return mapper.readTree(getTableValue(tabModeRujukanMasuk, row, 13));
            }
            if (tbServiceRequest.getSelectedRow() >= 0) {
                int row = tbServiceRequest.convertRowIndexToModel(tbServiceRequest.getSelectedRow());
                return mapper.readTree(getTableValue(tabModeServiceRequest, row, 10));
            }
            if (tabModeRujukanMasuk.getRowCount() == 1) {
                return mapper.readTree(getTableValue(tabModeRujukanMasuk, 0, 13));
            }
            if (tabModeServiceRequest.getRowCount() == 1) {
                return mapper.readTree(getTableValue(tabModeServiceRequest, 0, 10));
            }
        } catch (Exception e) {
            System.out.println("JSON : " + e);
        }
        return null;
    }

    private ObjectNode ambilDataPendukungDariServiceRequest(JsonNode serviceRequest) throws Exception {
        ObjectNode bundle = mapper.createObjectNode();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "collection");
        ArrayNode entries = mapper.createArrayNode();
        bundle.set("entry", entries);

        LinkedHashSet<String> pending = new LinkedHashSet<String>();
        LinkedHashSet<String> processed = new LinkedHashSet<String>();
        if(!serviceRequest.path("id").asText().equals("")){
            tambahEntryPendukung(entries, "ServiceRequest/" + serviceRequest.path("id").asText(), serviceRequest);
            processed.add("ServiceRequest/" + serviceRequest.path("id").asText());
        }
        kumpulkanReferensiServiceRequest(serviceRequest, pending);

        while(!pending.isEmpty()){
            String reference = pending.iterator().next();
            pending.remove(reference);
            reference = normalizeReference(reference);
            if(reference.equals("") || processed.contains(reference) || skipReference(reference)){
                continue;
            }
            processed.add(reference);
            try {
                SatuSehatKirimRujukan.KirimResult result = rujukan.ambilResource(reference);
                tambahEntryPendukung(entries, reference, result.root);
                kumpulkanReferensiLanjutan(result.root, pending);
            } catch (Exception e) {
                tambahEntryPendukung(entries, reference, errorResource(reference, e));
            }
        }
        bundle.put("total", entries.size());
        return bundle;
    }

    private void kumpulkanReferensiServiceRequest(JsonNode serviceRequest, Set<String> references) {
        collectReferences(references, serviceRequest.path("supportingInfo"));
        collectReferences(references, serviceRequest.path("reasonReference"));
        collectReferences(references, serviceRequest.path("basedOn"));
    }

    private void kumpulkanReferensiLanjutan(JsonNode resource, Set<String> references) {
        String resourceType = resource.path("resourceType").asText();
        if ("CarePlan".equals(resourceType)) {
            collectReferences(references, resource.path("supportingInfo"));
            collectReferences(references, resource.path("addresses"));
            collectReferences(references, resource.path("activity"));
        } else if ("DiagnosticReport".equals(resourceType)) {
            collectReferences(references, resource.path("result"));
            collectReferences(references, resource.path("specimen"));
            collectReferences(references, resource.path("basedOn"));
            collectReferences(references, resource.path("media"));
        } else if ("Observation".equals(resourceType)) {
            collectReferences(references, resource.path("derivedFrom"));
            collectReferences(references, resource.path("hasMember"));
            collectReferences(references, resource.path("partOf"));
        } else if ("Task".equals(resourceType)) {
            collectReferences(references, resource.path("basedOn"));
            collectReferences(references, resource.path("focus"));
            collectTaskIoReferences(references, resource.path("input"));
            collectTaskIoReferences(references, resource.path("output"));
        } else if ("ServiceRequest".equals(resourceType)) {
            kumpulkanReferensiServiceRequest(resource, references);
        }
    }

    private void collectTaskIoReferences(Set<String> references, JsonNode nodes) {
        if(!nodes.isArray()){
            collectReferences(references, nodes.path("valueReference"));
            return;
        }
        for(JsonNode node : nodes){
            collectReferences(references, node.path("valueReference"));
        }
    }

    private void collectReferences(Set<String> references, JsonNode nodes) {
        if (nodes == null || nodes.isMissingNode() || nodes.isNull()) {
            return;
        }
        if (nodes.isArray()) {
            for(JsonNode node : nodes){
                collectReferences(references, node);
            }
            return;
        }
        JsonNode referenceNode = nodes.path("reference");
        String reference = referenceNode.isObject() ? referenceNode.path("reference").asText() : referenceNode.asText();
        if(reference.equals("") && nodes.path("detail").isObject()){
            reference = nodes.path("detail").path("reference").asText();
        }
        if(reference.equals("") && nodes.path("link").isObject()){
            reference = nodes.path("link").path("reference").asText();
        }
        if(reference.equals("") && nodes.path("itemReference").isObject()){
            reference = nodes.path("itemReference").path("reference").asText();
        }
        if(!reference.equals("")){
            reference = normalizeReference(reference);
            if(!reference.equals("") && !skipReference(reference)){
                references.add(reference);
            }
        }
    }

    private boolean skipReference(String reference) {
        String value = normalizeReference(reference);
        return value.equals("") || value.startsWith("#") || value.startsWith("urn:")
                || value.startsWith("Patient/") || value.startsWith("Practitioner/")
                || value.startsWith("PractitionerRole/") || value.startsWith("Organization/")
                || value.startsWith("Encounter/") || value.startsWith("Location/");
    }

    private String normalizeReference(String reference) {
        String value = reference == null ? "" : reference.trim();
        if(value.equals("")){
            return "";
        }
        int marker = value.indexOf("/fhir-r4/v1/");
        if(marker >= 0){
            value = value.substring(marker + "/fhir-r4/v1/".length());
        }
        while(value.startsWith("/")){
            value = value.substring(1);
        }
        return value;
    }

    private void tambahEntryPendukung(ArrayNode entries, String reference, JsonNode resource) {
        ObjectNode entry = mapper.createObjectNode();
        entry.put("fullUrl", reference);
        entry.set("resource", resource);
        entries.add(entry);
    }

    private ObjectNode errorResource(String reference, Exception e) {
        ObjectNode error = mapper.createObjectNode();
        error.put("resourceType", "OperationOutcome");
        error.put("id", reference);
        ArrayNode issue = mapper.createArrayNode();
        ObjectNode item = mapper.createObjectNode();
        item.put("severity", "error");
        item.put("code", "exception");
        item.put("diagnostics", e.getMessage());
        issue.add(item);
        error.set("issue", issue);
        return error;
    }

    private String selectedTaskId() {
        if (tabData.getSelectedIndex() == 0 && tbRujukanMasuk.getSelectedRow() >= 0) {
            int row = tbRujukanMasuk.convertRowIndexToModel(tbRujukanMasuk.getSelectedRow());
            return getTableValue(tabModeRujukanMasuk, row, 10);
        }
        if (tbTask.getSelectedRow() >= 0) {
            int row = tbTask.convertRowIndexToModel(tbTask.getSelectedRow());
            return getTableValue(tabModeTask, row, 0);
        }
        return "";
    }

    private void isiTabelTask(JsonNode root) throws Exception {
        isiTabelTask(root, "");
    }

    private void isiTabelTask(JsonNode root, String logAwal) throws Exception {
        tabModeTask.setRowCount(0);
        JsonNode entries = root.path("entry");
        for (JsonNode entry : entries) {
            JsonNode task = entry.path("resource");
            tabModeTask.addRow(new Object[]{
                task.path("id").asText(),
                task.path("status").asText(),
                codingDisplay(task.path("code")),
                reference(task.path("for")),
                display(task.path("for")),
                reference(task.path("requester")),
                display(task.path("requester")),
                firstIdentifier(task),
                task.path("authoredOn").asText(),
                pretty(task)
            });
        }
    }

    private void isiTabelRujukanMasuk(JsonNode serviceRequestRoot, JsonNode taskRoot) throws Exception {
        tabModeRujukanMasuk.setRowCount(0);
        Map<String, JsonNode> tasks = taskMapById(taskRoot);
        Set<String> taskDenganServiceRequest = new LinkedHashSet<String>();
        for (JsonNode entry : serviceRequestRoot.path("entry")) {
            JsonNode serviceRequest = entry.path("resource");
            String taskId = taskIdFromReference(firstTaskReference(serviceRequest));
            JsonNode task = tasks.get(taskId);
            if(!taskId.equals("")){
                taskDenganServiceRequest.add(taskId);
            }
            tambahRowRujukanMasuk(serviceRequest, task);
        }
        for (Map.Entry<String, JsonNode> entry : tasks.entrySet()) {
            if(!taskDenganServiceRequest.contains(entry.getKey())){
                tambahRowRujukanMasukDariTask(entry.getValue());
            }
        }
    }

    private void isiRujukanMasukTunggal(JsonNode serviceRequest, JsonNode task) throws Exception {
        tabModeRujukanMasuk.setRowCount(0);
        tambahRowRujukanMasuk(serviceRequest, task);
    }

    private void tambahRowRujukanMasuk(JsonNode serviceRequest, JsonNode task) throws Exception {
        boolean adaTask = task != null && !task.isMissingNode() && !task.isNull();
        String taskId = adaTask ? task.path("id").asText() : taskIdFromReference(firstTaskReference(serviceRequest));
        tabModeRujukanMasuk.addRow(new Object[]{
            serviceRequest.path("id").asText(),
            serviceRequest.path("status").asText(),
            codingDisplay(serviceRequest.path("code")),
            reference(serviceRequest.path("subject")),
            patientDisplay(serviceRequest, task),
            reference(serviceRequest.path("requester")),
            display(serviceRequest.path("requester")),
            firstIdentifier(serviceRequest),
            firstNonEmpty(serviceRequest.path("authoredOn").asText(), serviceRequest.path("occurrenceDateTime").asText()),
            joinedReferences(serviceRequest.path("basedOn")),
            taskId,
            adaTask ? task.path("status").asText() : "",
            adaTask ? task.path("authoredOn").asText() : "",
            pretty(serviceRequest),
            adaTask ? pretty(task) : ""
        });
    }

    private void tambahRowRujukanMasukDariTask(JsonNode task) throws Exception {
        tabModeRujukanMasuk.addRow(new Object[]{
            "",
            "Belum ServiceRequest",
            codingDisplay(task.path("code")),
            reference(task.path("for")),
            patientDisplayLokal(null, task),
            reference(task.path("requester")),
            display(task.path("requester")),
            firstIdentifier(task),
            task.path("authoredOn").asText(),
            joinedReferences(task.path("basedOn")),
            task.path("id").asText(),
            task.path("status").asText(),
            task.path("authoredOn").asText(),
            "",
            pretty(task)
        });
    }

    private Map<String, JsonNode> taskMapById(JsonNode taskRoot) {
        Map<String, JsonNode> tasks = new LinkedHashMap<String, JsonNode>();
        if (taskRoot == null || taskRoot.isMissingNode() || taskRoot.isNull()) {
            return tasks;
        }
        for (JsonNode entry : taskRoot.path("entry")) {
            JsonNode task = entry.path("resource");
            String id = task.path("id").asText();
            if (!id.equals("")) {
                tasks.put(id, task);
            }
        }
        return tasks;
    }

    private String firstTaskReference(JsonNode serviceRequest) {
        Set<String> references = new LinkedHashSet<String>();
        collectTaskReferences(references, serviceRequest.path("supportingInfo"));
        collectTaskReferences(references, serviceRequest.path("basedOn"));
        return references.isEmpty() ? "" : references.iterator().next();
    }

    private String patientDisplay(JsonNode serviceRequest, JsonNode task) {
        String localDisplay = patientDisplayLokal(serviceRequest, task);
        if (!localDisplay.equals("")) {
            return localDisplay;
        }
        String patientReference = reference(serviceRequest.path("subject"));
        String cachedName = patientNameCache.get(normalizeReference(patientReference));
        if (cachedName != null && !cachedName.equals("")) {
            return cachedName;
        }
        return patientReference;
    }

    private String patientDisplayLokal(JsonNode serviceRequest, JsonNode task) {
        if (serviceRequest != null && !serviceRequest.isMissingNode() && !serviceRequest.isNull()) {
            String serviceRequestDisplay = display(serviceRequest.path("subject"));
            if (!serviceRequestDisplay.equals("")) {
                return serviceRequestDisplay;
            }
        }
        if (task != null && !task.isMissingNode() && !task.isNull()) {
            String taskDisplay = display(task.path("for"));
            if (!taskDisplay.equals("")) {
                return taskDisplay;
            }
            String cachedName = patientNameCache.get(normalizeReference(reference(task.path("for"))));
            if (cachedName != null && !cachedName.equals("")) {
                return cachedName;
            }
        }
        return "";
    }

    private void siapkanNamaPasien(JsonNode serviceRequestRoot, JsonNode taskRoot) {
        Map<String, JsonNode> tasks = taskMapById(taskRoot);
        for (JsonNode entry : serviceRequestRoot.path("entry")) {
            JsonNode serviceRequest = entry.path("resource");
            JsonNode task = tasks.get(taskIdFromReference(firstTaskReference(serviceRequest)));
            siapkanNamaPasienTunggal(serviceRequest, task);
        }
    }

    private void siapkanNamaPasienDariTask(JsonNode taskRoot) {
        for (JsonNode entry : taskRoot.path("entry")) {
            JsonNode task = entry.path("resource");
            String patientReference = normalizeReference(reference(task.path("for")));
            if (patientReference.equals("") || !patientReference.startsWith("Patient/") || patientNameCache.containsKey(patientReference)) {
                continue;
            }
            try {
                SatuSehatKirimRujukan.KirimResult result = rujukan.ambilResource(patientReference);
                patientNameCache.put(patientReference, firstNonEmpty(patientName(result.root), patientIdentifier(result.root), patientReference));
            } catch (Exception e) {
                patientNameCache.put(patientReference, patientReference);
                System.out.println("JSON : gagal mengambil " + patientReference + " - " + e.getMessage());
            }
        }
    }

    private void siapkanNamaPasienTunggal(JsonNode serviceRequest, JsonNode task) {
        if (!patientDisplayLokal(serviceRequest, task).equals("")) {
            return;
        }
        String patientReference = normalizeReference(reference(serviceRequest.path("subject")));
        if (patientReference.equals("") || !patientReference.startsWith("Patient/") || patientNameCache.containsKey(patientReference)) {
            return;
        }
        try {
            SatuSehatKirimRujukan.KirimResult result = rujukan.ambilResource(patientReference);
            patientNameCache.put(patientReference, firstNonEmpty(patientName(result.root), patientIdentifier(result.root), patientReference));
        } catch (Exception e) {
            patientNameCache.put(patientReference, patientReference);
            System.out.println("JSON : gagal mengambil " + patientReference + " - " + e.getMessage());
        }
    }

    private String patientName(JsonNode patient) {
        if (!"Patient".equals(patient.path("resourceType").asText())) {
            return "";
        }
        JsonNode names = patient.path("name");
        if (names.isArray()) {
            for (JsonNode name : names) {
                String text = humanName(name);
                if (!text.equals("")) {
                    return text;
                }
            }
        }
        return humanName(names);
    }

    private String patientIdentifier(JsonNode patient) {
        if (!"Patient".equals(patient.path("resourceType").asText())) {
            return "";
        }
        String fallback = "";
        JsonNode identifiers = patient.path("identifier");
        if (identifiers.isArray()) {
            for (JsonNode identifier : identifiers) {
                String system = identifier.path("system").asText();
                String value = identifier.path("value").asText();
                if (value.equals("") || value.contains("#")) {
                    continue;
                }
                if (system.endsWith("/ihs-number")) {
                    return value;
                }
                if (fallback.equals("")) {
                    fallback = value;
                }
            }
        }
        return fallback;
    }

    private String humanName(JsonNode name) {
        String text = name.path("text").asText();
        if (!text.equals("")) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        appendNamePart(builder, name.path("prefix"));
        appendNamePart(builder, name.path("given"));
        appendNamePart(builder, name.path("family").asText());
        appendNamePart(builder, name.path("suffix"));
        return builder.toString();
    }

    private void appendNamePart(StringBuilder builder, JsonNode node) {
        if (node.isArray()) {
            for (JsonNode item : node) {
                appendNamePart(builder, item.asText());
            }
            return;
        }
        appendNamePart(builder, node.asText());
    }

    private void appendNamePart(StringBuilder builder, String value) {
        String text = value == null ? "" : value.trim();
        if (text.equals("")) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" ");
        }
        builder.append(text);
    }

    private void isiTabelServiceRequest(JsonNode root) throws Exception {
        tabModeServiceRequest.setRowCount(0);
        JsonNode entries = root.path("entry");
        for (JsonNode entry : entries) {
            JsonNode serviceRequest = entry.path("resource");
            tabModeServiceRequest.addRow(new Object[]{
                serviceRequest.path("id").asText(),
                serviceRequest.path("status").asText(),
                codingDisplay(serviceRequest.path("code")),
                reference(serviceRequest.path("subject")),
                display(serviceRequest.path("subject")),
                reference(serviceRequest.path("requester")),
                display(serviceRequest.path("requester")),
                firstIdentifier(serviceRequest),
                serviceRequest.path("authoredOn").asText(),
                joinedReferences(serviceRequest.path("basedOn")),
                pretty(serviceRequest)
            });
        }
    }

    private void isiServiceRequestTunggal(JsonNode serviceRequest) throws Exception {
        tabModeServiceRequest.setRowCount(0);
        tabModeServiceRequest.addRow(new Object[]{
            serviceRequest.path("id").asText(),
            serviceRequest.path("status").asText(),
            codingDisplay(serviceRequest.path("code")),
            reference(serviceRequest.path("subject")),
            display(serviceRequest.path("subject")),
            reference(serviceRequest.path("requester")),
            display(serviceRequest.path("requester")),
            firstIdentifier(serviceRequest),
            serviceRequest.path("authoredOn").asText(),
            joinedReferences(serviceRequest.path("basedOn")),
            pretty(serviceRequest)
        });
        System.out.println("JSON : " + compact(serviceRequest));
    }

    private String jenisResource(JsonNode resource) {
        String resourceType = resource.path("resourceType").asText();
        if ("OperationOutcome".equals(resourceType)) {
            return resource.path("issue").path(0).path("code").asText();
        }
        if ("DiagnosticReport".equals(resourceType)) {
            return codingDisplay(resource.path("category")) + " / " + codingDisplay(resource.path("code"));
        }
        if ("Observation".equals(resourceType) || "Condition".equals(resourceType)
                || "Procedure".equals(resourceType) || "ServiceRequest".equals(resourceType)) {
            return codingDisplay(resource.path("code"));
        }
        if ("MedicationRequest".equals(resourceType) || "MedicationDispense".equals(resourceType)
                || "MedicationAdministration".equals(resourceType)) {
            return medicationDisplay(resource);
        }
        if ("AllergyIntolerance".equals(resourceType)) {
            return codingDisplay(resource.path("code"));
        }
        if ("ClinicalImpression".equals(resourceType)) {
            return resource.path("status").asText();
        }
        if ("Task".equals(resourceType)) {
            return codingDisplay(resource.path("code"));
        }
        return firstNonEmpty(codingDisplay(resource.path("type")), codingDisplay(resource.path("category")), resourceType);
    }

    private String ringkasanResource(JsonNode resource) {
        String resourceType = resource.path("resourceType").asText();
        if ("OperationOutcome".equals(resourceType)) {
            return resource.path("issue").path(0).path("diagnostics").asText();
        }
        if ("DiagnosticReport".equals(resourceType)) {
            return ringkasanDiagnosticReport(resource);
        }
        if ("Observation".equals(resourceType)) {
            return ringkasanObservation(resource);
        }
        if ("Condition".equals(resourceType)) {
            return firstNonEmpty(codingDisplay(resource.path("code")), codingDisplay(resource.path("clinicalStatus")),
                    reference(resource.path("subject")));
        }
        if ("ClinicalImpression".equals(resourceType)) {
            return firstNonEmpty(resource.path("description").asText(), resource.path("summary").asText(),
                    codingDisplay(resource.path("prognosisCodeableConcept")));
        }
        if ("AllergyIntolerance".equals(resourceType)) {
            return firstNonEmpty(codingDisplay(resource.path("code")), codingDisplay(resource.path("clinicalStatus")));
        }
        if ("Procedure".equals(resourceType)) {
            return firstNonEmpty(codingDisplay(resource.path("code")), resource.path("status").asText());
        }
        if ("MedicationRequest".equals(resourceType) || "MedicationDispense".equals(resourceType)
                || "MedicationAdministration".equals(resourceType)) {
            return medicationDisplay(resource);
        }
        if ("Specimen".equals(resourceType)) {
            return firstNonEmpty(codingDisplay(resource.path("type")), resource.path("status").asText());
        }
        if ("ServiceRequest".equals(resourceType)) {
            StringBuilder result = new StringBuilder();
            appendSummary(result, "Kode", codingDisplay(resource.path("code")));
            appendSummary(result, "Instruksi", resource.path("patientInstruction").asText());
            appendSummary(result, "Catatan", noteText(resource.path("note")));
            appendSummary(result, "Pendukung", joinedReferences(resource.path("supportingInfo")));
            return result.toString();
        }
        if ("CarePlan".equals(resourceType)) {
            StringBuilder result = new StringBuilder();
            appendSummary(result, "Judul", resource.path("title").asText());
            appendSummary(result, "Deskripsi", resource.path("description").asText());
            appendSummary(result, "Catatan", noteText(resource.path("note")));
            appendSummary(result, "Pendukung", joinedReferences(resource.path("supportingInfo")));
            return result.toString();
        }
        if ("Task".equals(resourceType)) {
            return firstNonEmpty(resource.path("status").asText(), codingDisplay(resource.path("code")));
        }
        return firstNonEmpty(resource.path("text").path("div").asText(), resource.path("status").asText());
    }

    private String ringkasanDiagnosticReport(JsonNode resource) {
        StringBuilder result = new StringBuilder();
        appendSummary(result, "Status", resource.path("status").asText());
        appendSummary(result, "Kesimpulan", resource.path("conclusion").asText());
        appendSummary(result, "Hasil", joinedReferences(resource.path("result")));
        appendSummary(result, "Specimen", joinedReferences(resource.path("specimen")));
        appendSummary(result, "Order", joinedReferences(resource.path("basedOn")));
        return result.toString();
    }

    private String ringkasanObservation(JsonNode resource) {
        StringBuilder result = new StringBuilder();
        appendSummary(result, "Kode", codingDisplay(resource.path("code")));
        appendSummary(result, "Nilai", nilaiObservation(resource));
        appendSummary(result, "Interpretasi", codingDisplay(resource.path("interpretation")));
        appendSummary(result, "Catatan", noteText(resource.path("note")));
        return result.toString();
    }

    private String nilaiObservation(JsonNode observation) {
        if(!observation.path("valueQuantity").isMissingNode()){
            JsonNode quantity = observation.path("valueQuantity");
            String value = quantity.path("value").asText();
            String unit = firstNonEmpty(quantity.path("unit").asText(), quantity.path("code").asText());
            return (value + " " + unit).trim();
        }
        if(!observation.path("valueCodeableConcept").isMissingNode()){
            return codingDisplay(observation.path("valueCodeableConcept"));
        }
        if(!observation.path("valueString").isMissingNode()){
            return observation.path("valueString").asText();
        }
        if(!observation.path("valueBoolean").isMissingNode()){
            return observation.path("valueBoolean").asText();
        }
        if(!observation.path("valueInteger").isMissingNode()){
            return observation.path("valueInteger").asText();
        }
        if(!observation.path("valueDateTime").isMissingNode()){
            return observation.path("valueDateTime").asText();
        }
        if(observation.path("component").isArray()){
            StringBuilder result = new StringBuilder();
            for(JsonNode component : observation.path("component")){
                String item = codingDisplay(component.path("code")) + ": " + nilaiObservation(component);
                appendJoined(result, item.trim());
            }
            return result.toString();
        }
        return "";
    }

    private String medicationDisplay(JsonNode resource) {
        return firstNonEmpty(
                codingDisplay(resource.path("medicationCodeableConcept")),
                reference(resource.path("medicationReference")),
                codingDisplay(resource.path("medication")));
    }

    private String tanggalResource(JsonNode resource) {
        return firstNonEmpty(
                resource.path("authoredOn").asText(),
                resource.path("effectiveDateTime").asText(),
                resource.path("issued").asText(),
                resource.path("performedDateTime").asText(),
                resource.path("occurrenceDateTime").asText(),
                resource.path("recordedDate").asText(),
                resource.path("date").asText(),
                resource.path("created").asText());
    }

    private String noteText(JsonNode notes) {
        StringBuilder result = new StringBuilder();
        if(notes.isArray()){
            for(JsonNode note : notes){
                appendJoined(result, note.path("text").asText());
            }
        } else {
            appendJoined(result, notes.path("text").asText());
        }
        return result.toString();
    }

    private void appendSummary(StringBuilder builder, String label, String value) {
        String text = value == null ? "" : value.trim();
        if(text.equals("")){
            return;
        }
        if(builder.length() > 0){
            builder.append(" | ");
        }
        builder.append(label).append(": ").append(text);
    }

    private String firstNonEmpty(String... values) {
        for(String value : values){
            if(value != null && !value.trim().equals("")){
                return value.trim();
            }
        }
        return "";
    }

    private DefaultTableModel createTaskModel() {
        return new DefaultTableModel(null, new Object[]{"ID Task", "Status", "Jenis", "Patient Ref", "Pasien", "Requester Ref", "Requester", "Identifier", "Authored On", "Raw JSON"}) {
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
    }

    private DefaultTableModel createRujukanMasukModel() {
        return new DefaultTableModel(null, new Object[]{"ID ServiceRequest", "Status SR", "Jenis", "Patient Ref", "Pasien", "Requester Ref", "Requester", "Identifier", "Tanggal", "Based On", "ID Task", "Status Task", "Task Authored", "Raw ServiceRequest", "Raw Task"}) {
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
    }

    private DefaultTableModel createServiceRequestModel() {
        return new DefaultTableModel(null, new Object[]{"ID ServiceRequest", "Status", "Jenis", "Patient Ref", "Pasien", "Requester Ref", "Requester", "Identifier", "Authored On", "Based On", "Raw JSON"}) {
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
    }

    private void prepareTable(JTable table, int rawColumn, int[] columnWidths) {
        table.setFont(DEFAULT_FONT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(24);
        table.setIntercellSpacing(new Dimension(8, 3));
        table.getTableHeader().setFont(DEFAULT_FONT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 26));
        table.setDefaultRenderer(Object.class, new WarnaTable());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    if (table == tbServiceRequest) {
                        sinkronkanTaskDariServiceRequestTerpilih(true, true);
                    } else if (table == tbRujukanMasuk) {
                        tampilkanDataPendukungTerpilih();
                    } else {
                        // no-op
                    }
                }
            }
        });
        applyColumnWidths(table, columnWidths);
        hideColumn(table, rawColumn);
    }

    private void applyColumnWidths(JTable table, int[] columnWidths) {
        int max = Math.min(table.getColumnModel().getColumnCount(), columnWidths.length);
        for (int i = 0; i < max; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidths[i]);
        }
    }

    private void hideColumn(JTable table, int index) {
        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
    }

    private JButton button(String text, String icon, int width) {
        JButton button = new JButton(text);
        button.setFont(DEFAULT_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACTION_COLOR);
        button.setPreferredSize(new Dimension(width, 30));
        button.setMinimumSize(new Dimension(width, 30));
        button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/" + icon)));
        button.setIconTextGap(6);
        return button;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DEFAULT_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private String selectedStatus(JComboBox<String> combo) {
        String value = combo.getSelectedItem() == null ? "" : combo.getSelectedItem().toString();
        return "Semua".equals(value) ? "" : value;
    }

    private boolean validOrg() {
        if (orgId.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Organization ID belum diisi.");
            return false;
        }
        return true;
    }

    private Set<String> taskReferencesFromServiceRequest(JsonNode root) {
        Set<String> references = new LinkedHashSet<String>();
        JsonNode entries = root.path("entry");
        for (JsonNode entry : entries) {
            JsonNode serviceRequest = entry.path("resource");
            collectTaskReferences(references, serviceRequest.path("supportingInfo"));
            collectTaskReferences(references, serviceRequest.path("basedOn"));
        }
        return references;
    }

    private void sinkronkanTaskDariServiceRequestTerpilih(boolean pindahTab, boolean tampilkanPesan) {
        if (tbServiceRequest.getSelectedRow() < 0) {
            if (tampilkanPesan) {
                JOptionPane.showMessageDialog(this, "Pilih ServiceRequest rujukan masuk terlebih dahulu.");
            }
            return;
        }
        JsonNode serviceRequest;
        try {
            int row = tbServiceRequest.convertRowIndexToModel(tbServiceRequest.getSelectedRow());
            serviceRequest = mapper.readTree(getTableValue(tabModeServiceRequest, row, 10));
        } catch (Exception e) {
            tampilkanError("Gagal membaca ServiceRequest terpilih", e);
            return;
        }
        Set<String> references = new LinkedHashSet<String>();
        collectTaskReferences(references, serviceRequest.path("supportingInfo"));
        collectTaskReferences(references, serviceRequest.path("basedOn"));
        if (references.isEmpty()) {
            if (tampilkanPesan) {
                JOptionPane.showMessageDialog(this, "ServiceRequest ini tidak memiliki referensi Task.");
            }
            return;
        }
        String taskReference = references.iterator().next();
        if (pilihTaskDiTabel(taskReference, pindahTab)) {
            return;
        }
        ambilDanPilihTask(taskReference, pindahTab);
    }

    private void ambilDanPilihTask(String taskReference, boolean pindahTab) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<SatuSehatKirimRujukan.KirimResult, Void>() {
            protected SatuSehatKirimRujukan.KirimResult doInBackground() throws Exception {
                return rujukan.ambilTask(taskReference);
            }

            protected void done() {
                try {
                    JsonNode task = get().root;
                    tambahAtauGantiTask(task);
                    pilihTaskDiTabel("Task/" + task.path("id").asText(), pindahTab);
                } catch (Exception e) {
                    tampilkanError("Gagal mengambil Task dari ServiceRequest", e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void tambahAtauGantiTask(JsonNode task) throws Exception {
        String taskId = task.path("id").asText();
        for (int i = 0; i < tabModeTask.getRowCount(); i++) {
            if (taskId.equals(getTableValue(tabModeTask, i, 0))) {
                tabModeTask.removeRow(i);
                break;
            }
        }
        tabModeTask.addRow(new Object[]{
            taskId,
            task.path("status").asText(),
            codingDisplay(task.path("code")),
            reference(task.path("for")),
            display(task.path("for")),
            reference(task.path("requester")),
            display(task.path("requester")),
            firstIdentifier(task),
            task.path("authoredOn").asText(),
            pretty(task)
        });
    }

    private boolean pilihTaskDiTabel(String taskReference, boolean pindahTab) {
        String taskId = taskIdFromReference(taskReference);
        if (taskId.equals("")) {
            return false;
        }
        for (int i = 0; i < tabModeTask.getRowCount(); i++) {
            if (taskId.equals(getTableValue(tabModeTask, i, 0))) {
                int viewRow = tbTask.convertRowIndexToView(i);
                tbTask.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                tbTask.scrollRectToVisible(tbTask.getCellRect(viewRow, 0, true));
                if (pindahTab) {
                    tabData.setSelectedIndex(0);
                }
                return true;
            }
        }
        return false;
    }

    private String taskIdFromReference(String taskReference) {
        String value = normalizeReference(taskReference);
        return value.startsWith("Task/") ? value.substring("Task/".length()) : value;
    }

    private void collectTaskReferences(Set<String> references, JsonNode nodes) {
        if (!nodes.isArray()) {
            String reference = nodes.path("reference").asText();
            if (reference.startsWith("Task/")) {
                references.add(reference);
            }
            return;
        }
        for (JsonNode node : nodes) {
            String reference = node.path("reference").asText();
            if (reference.startsWith("Task/")) {
                references.add(reference);
            }
        }
    }

    private String firstIdentifier(JsonNode resource) {
        JsonNode identifier = resource.path("identifier");
        if (identifier.isArray() && identifier.size() > 0) {
            JsonNode first = identifier.get(0);
            String value = first.path("value").asText();
            String system = first.path("system").asText();
            return value.equals("") ? system : value;
        }
        return "";
    }

    private String codingDisplay(JsonNode codeable) {
        if (codeable.isArray()) {
            for (JsonNode item : codeable) {
                String text = codingDisplay(item);
                if (!text.equals("")) {
                    return text;
                }
            }
            return "";
        }
        JsonNode coding = codeable.path("coding");
        if (coding.isArray() && coding.size() > 0) {
            JsonNode first = coding.get(0);
            String display = first.path("display").asText();
            String code = first.path("code").asText();
            return display.equals("") ? code : display;
        }
        return codeable.path("text").asText();
    }

    private String reference(JsonNode node) {
        return node.path("reference").asText();
    }

    private String display(JsonNode node) {
        return node.path("display").asText();
    }

    private String joinedReferences(JsonNode refs) {
        StringBuilder builder = new StringBuilder();
        if (refs.isArray()) {
            for (JsonNode ref : refs) {
                collectDisplayReference(builder, ref);
            }
        } else {
            collectDisplayReference(builder, refs);
        }
        return builder.toString();
    }

    private void collectDisplayReference(StringBuilder builder, JsonNode ref) {
        if (ref == null || ref.isMissingNode() || ref.isNull()) {
            return;
        }
        JsonNode referenceNode = ref.path("reference");
        String reference = referenceNode.isObject() ? referenceNode.path("reference").asText() : referenceNode.asText();
        appendJoined(builder, reference);
    }

    private void appendJoined(StringBuilder builder, String value) {
        if (value == null || value.equals("")) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value);
    }

    private String pretty(JsonNode node) throws Exception {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    private String compact(JsonNode node) throws Exception {
        return mapper.writeValueAsString(node);
    }

    private String getTableValue(DefaultTableModel model, int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private void tampilkanError(String pesan, Exception e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        String detail = cause.getMessage();
        if (cause instanceof HttpStatusCodeException) {
            detail = ((HttpStatusCodeException) cause).getResponseBodyAsString();
        }
        System.out.println("JSON : " + detail);
        JOptionPane.showMessageDialog(this, pesan + ": " + cause.getMessage());
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatRujukanMasukForm dialog = new SatuSehatRujukanMasukForm(new javax.swing.JFrame(), true);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });
    }

    private static class TaskMasukResult {
        private final JsonNode serviceRequestRoot;
        private final JsonNode taskRoot;
        private final int taskReferenceCount;

        private TaskMasukResult(JsonNode serviceRequestRoot, JsonNode taskRoot, int taskReferenceCount) {
            this.serviceRequestRoot = serviceRequestRoot;
            this.taskRoot = taskRoot;
            this.taskReferenceCount = taskReferenceCount;
        }
    }
}
