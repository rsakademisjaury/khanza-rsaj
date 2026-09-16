package bridging;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Draf lokal + state workflow API. Bukan pengganti resource FHIR. */
public final class SatuSehatRujukanIGDRanapDraft {
    public enum Jenis {
        IGD("IGD", "Ralan"), RANAP("Rawat Inap", "Ranap");
        public final String label, statusDiagnosis;
        Jenis(String label, String status) { this.label = label; this.statusDiagnosis = status; }
        public static Jenis from(String text) {
            if ("IGD".equalsIgnoreCase(text)) return IGD;
            if ("RANAP".equalsIgnoreCase(text) || "Rawat Inap".equalsIgnoreCase(text)) return RANAP;
            throw new IllegalArgumentException("Jenis rujukan harus IGD atau RANAP.");
        }
    }
    public static final List<String> FIELDS = Collections.unmodifiableList(Arrays.asList(
        "diagnosis_code", "diagnosis_name", "doctor", "origin", "reason", "care",
        "clinical", "therapy", "procedures", "allergies", "blood_pressure", "pulse",
        "respiratory_rate", "spo2", "temperature", "consciousness", "measured_at",
        "region", "transport", "escort", "planned_departure", "notes",
        "bpjs_card_number", "ambulance_registration",
        "supporting_documents", "emergency_confirmed",
        // Tahap II - referensi dan state transaksi SATUSEHAT. Tetap disimpan
        // pada payload_xml agar tidak menambah/mengubah kolom database V4.
        "condition_id", "secondary_diagnosis_code", "secondary_diagnosis_name", "secondary_condition_id", "practitioner_ihs",
        "province_code", "province_display", "city_code", "city_display",
        "service_group_system", "service_group_code", "service_group_display",
        "clinical_speciality_code", "clinical_speciality_display",
        "practitioner_speciality_code", "practitioner_speciality_display",
        "criteria_state_json", "candidate_state_json", "approval_state_json", "pre_request_signature", "candidate_query_signature",
        "task_pre_id", "task_candidate_id", "task_candidate_response_id", "careplan_id",
        "service_request_id", "national_referral_number",
        "selected_org_id", "selected_org_name", "selected_approval_task_id",
        "no_rujukan_pcare",
        "last_api_action", "last_api_request", "last_api_response"));
    public final String noRawat, noRm;
    public final Jenis jenis;
    public long version;
    public String updatedBy = "", updatedAt = "";
    private final Map<String, String> values = new LinkedHashMap<String, String>();
    public SatuSehatRujukanIGDRanapDraft(String rawat, String rm, Jenis jenis) {
        if (jenis == null) throw new IllegalArgumentException("Jenis rujukan wajib tersedia.");
        this.noRawat = safe(rawat); this.noRm = safe(rm); this.jenis = jenis;
        for (String f : FIELDS) values.put(f, "");
    }
    public String get(String key) { checkKey(key); return values.get(key); }
    public void set(String key, String value) { checkKey(key); values.put(key, safe(value)); }
    private static void checkKey(String key) {
        if (!FIELDS.contains(key)) throw new IllegalArgumentException("Field draf tidak dikenali.");
    }
    public SatuSehatRujukanIGDRanapDraft copy() {
        SatuSehatRujukanIGDRanapDraft d = new SatuSehatRujukanIGDRanapDraft(noRawat, noRm, jenis);
        d.values.putAll(values); d.version = version; d.updatedBy = updatedBy; d.updatedAt = updatedAt;
        return d;
    }
    public String toXml() throws IOException {
        Properties p = new Properties(); p.setProperty("schema_version", "2");
        for (Map.Entry<String, String> f : values.entrySet()) p.setProperty(f.getKey(), f.getValue());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        p.storeToXML(out, "Draf + state integrasi SATUSEHAT Rujukan IGD/RANAP", "UTF-8");
        if (out.size() > 262144) throw new IOException("Isi draf melebihi batas 256 KiB.");
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
    public void loadXml(String xml) throws IOException {
        if (xml == null || xml.getBytes(StandardCharsets.UTF_8).length > 262144)
            throw new IOException("Data draf kosong atau melebihi batas ukuran.");
        Properties p = new Properties();
        p.loadFromXML(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        String schema = p.getProperty("schema_version", "1");
        if (!"1".equals(schema) && !"2".equals(schema))
            throw new IOException("Versi data draf belum didukung; draf tidak ditimpa.");
        // Kompatibel dengan draf V4 schema 1: field Tahap II yang belum ada
        // otomatis menjadi string kosong dan tidak merusak data lama.
        for (String f : FIELDS) values.put(f, p.getProperty(f, ""));
    }
    public static String safe(String s) { return s == null ? "" : s.trim(); }
}
