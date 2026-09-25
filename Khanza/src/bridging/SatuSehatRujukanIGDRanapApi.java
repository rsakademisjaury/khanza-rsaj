package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;

/**
 * Client khusus SATUSEHAT Rujukan IGD/Rawat Inap.
 *
 * Acuan implementasi utama untuk revisi ini:
 * - Playbook Rujukan Pasien v6.1 / 21-08-2026 + collection koordinasi 30-06-2026
 *   yang dibagikan pada koordinasi pengembangan SATUSEHAT/BPJS.
 * - Fokus folder 02. Rawat Inap dan 03. Rawat Darurat.
 *
 * Service ini sengaja terpisah dari bridging BPJS/rawat jalan.
 */
public final class SatuSehatRujukanIGDRanapApi {
    public static final String SYS_KEMKES = "http://terminology.kemkes.go.id";
    public static final String SYS_SNOMED = "http://snomed.info/sct";
    public static final String SYS_ICD10 = "http://hl7.org/fhir/sid/icd-10";
    public static final String SYS_ADMIN_AREA = "http://sys-ids.kemkes.go.id/administrative-area";
    public static final String SYS_TASK_STATUS = "http://hl7.org/fhir/task-status";
    public static final String SYS_CLINICAL_SPECIALITY = "http://terminology.kemkes.go.id/CodeSystem/clinical-speciality";

    public static final String RANAP_CARE_CODE = "737481003"; // Inpatient care management
    public static final String IGD_CARE_CODE = "385868005";   // Emergency treatment management

    private final ApiSatuSehat api = new ApiSatuSehat();
    private final ObjectMapper mapper = new ObjectMapper();
    // Untuk panel "Respon API". Token/Authorization tidak pernah disimpan.
    private volatile String lastMethod = "", lastUrl = "", lastRequest = "", lastResponse = "";
    private final List<ApiTrace> traceHistory = new ArrayList<ApiTrace>();

    public static final class ApiTrace {
        public final long timeMillis;
        public final String method, url, request, response;
        ApiTrace(long timeMillis, String method, String url, String request, String response) {
            this.timeMillis=timeMillis; this.method=method==null?"":method; this.url=url==null?"":url;
            this.request=request==null?"":request; this.response=response==null?"":response;
        }
    }

    public String getLastMethod() { return lastMethod; }
    public String getLastUrl() { return lastUrl; }
    public String getLastRequest() { return lastRequest; }
    public String getLastResponse() { return lastResponse; }
    public synchronized List<ApiTrace> getTraceHistory() { return new ArrayList<ApiTrace>(traceHistory); }
    private synchronized void appendTrace() {
        traceHistory.add(new ApiTrace(System.currentTimeMillis(), lastMethod, lastUrl, lastRequest, lastResponse));
        while (traceHistory.size() > 50) traceHistory.remove(0);
    }

    public static final class Question {
        public String linkId = "", text = "", type = "", group = "", groupLinkId = "", answer = "";
    }
    public static final class Candidate {
        public boolean selected;
        public String orgId = "", name = "", bpjsCode = "", strata = "", distance = "", eta = "", bed = "";
    }
    public static final class Approval {
        public boolean finalChoice;
        public String orgId = "", name = "", taskId = "", status = "requested", decision = "";
    }
    public static final class ApprovalBundleResult {
        public JsonNode raw;
        public String carePlanId = "", candidateRecommendationTaskId = "";
        public final List<Approval> approvals = new ArrayList<Approval>();
    }

    /** Detail rujukan masuk yang ditelusuri dari referensi FHIR resmi. */
    public static final class IncomingReferencedResource {
        public String role = "", reference = "", display = "", error = "";
        public JsonNode resource;
    }
    public static final class IncomingReferralDetail {
        public JsonNode task, carePlan, serviceRequest, patient, encounter, referrerOrganization, authorPractitioner;
        public final List<JsonNode> conditions = new ArrayList<JsonNode>();
        public final List<IncomingReferencedResource> references = new ArrayList<IncomingReferencedResource>();
        public final List<String> warnings = new ArrayList<String>();
        public String taskId = "", carePlanId = "", serviceRequestId = "", nationalReferralNumber = "";
    }

    public JsonNode kirimPraPermintaan(SatuSehatRujukanIGDRanapDraft.Jenis jenis,
                                       String registrationId, String encounterId,
                                       String diagnosisCode, String diagnosisDisplay) throws Exception {
        require(encounterId, "Encounter SATUSEHAT");
        require(diagnosisCode, "ICD-10 diagnosis utama");
        // Collection koordinasi membangkitkan Task_ID baru untuk setiap pra-permintaan.
        // Jangan gunakan no_rawat sebagai identifier bisnis Task karena nilainya akan
        // berulang ketika user mengulang Cek Kriteria untuk kunjungan yang sama.
        ObjectNode task = baseTask("referral-pre-request", "Referral pre request", newTaskBusinessIdentifier(), encounterId);
        ArrayNode input = task.putArray("input");
        // Collection 30-06-2026: Rawat Inap pra-permintaan hanya primary-diagnosis.
        // Rawat Darurat menambahkan Management procedure = Emergency treatment management.
        if (jenis == SatuSehatRujukanIGDRanapDraft.Jenis.IGD) {
            input.add(inputCoding(SYS_SNOMED, "119270007", "Management procedure",
                    SYS_SNOMED, IGD_CARE_CODE, "Emergency treatment management"));
        }
        input.add(inputCoding(SYS_KEMKES, "primary-diagnosis", "Primary Diagnosis",
                SYS_ICD10, diagnosisCode, diagnosisDisplay));
        return post("/Task", task);
    }

    /**
     * Ambil HANYA Questionnaire kriteria rujukan dari Task pra-permintaan.
     *
     * SATUSEHAT dapat mengembalikan lebih dari satu Questionnaire di Task.contained,
     * misalnya Kriteria Rujukan dan Jejaring Wilayah Rujukan.  Yang ditampilkan pada
     * tabel Kriteria harus resource yang direferensikan Task.output dengan
     * type.code = referral-criteria; Questionnaire area tidak boleh ikut menjadi
     * pertanyaan klinis.
     */
    public List<Question> parseQuestions(JsonNode taskResponse) {
        List<Question> result = new ArrayList<Question>();
        JsonNode task = referralPreRequestTaskNode(taskResponse);
        if (task == null) return result;

        JsonNode contained = task.path("contained");
        if (contained == null || !contained.isArray()) return result;

        // 1. Jalur utama: ikuti referensi resmi dari Task.output referral-criteria.
        String criteriaRef = "";
        JsonNode outputs = task.path("output");
        if (outputs != null && outputs.isArray()) {
            for (JsonNode output : outputs) {
                if (!hasCodingCode(output.path("type").path("coding"), "referral-criteria")) continue;
                criteriaRef = output.path("valueReference").path("reference").asText();
                if (criteriaRef != null && criteriaRef.startsWith("#")) criteriaRef = criteriaRef.substring(1);
                if (criteriaRef != null && !criteriaRef.trim().isEmpty()) break;
            }
        }

        if (criteriaRef != null && !criteriaRef.trim().isEmpty()) {
            JsonNode criteria = findContainedById(contained, criteriaRef.trim());
            if (criteria != null && "Questionnaire".equals(criteria.path("resourceType").asText())) {
                collectQuestions(criteria.path("item"), "", "", result);
                return result;
            }
        }

        // 2. Fallback untuk response lama: cari Questionnaire berjudul Kriteria Rujukan.
        for (JsonNode resource : contained) {
            if (!"Questionnaire".equals(resource.path("resourceType").asText())) continue;
            String title = resource.path("title").asText();
            if (title != null && title.toLowerCase().contains("kriteria rujukan")) {
                collectQuestions(resource.path("item"), "", "", result);
                return result;
            }
        }

        // 3. Fallback terakhir: Questionnaire pertama yang bukan jejaring/wilayah.
        for (JsonNode resource : contained) {
            if (!"Questionnaire".equals(resource.path("resourceType").asText())) continue;
            String title = resource.path("title").asText();
            String t = title == null ? "" : title.toLowerCase();
            if (t.contains("jejaring") || t.contains("wilayah")) continue;
            collectQuestions(resource.path("item"), "", "", result);
            if (!result.isEmpty()) return result;
        }
        return result;
    }

    private static boolean hasCodingCode(JsonNode codings, String expectedCode) {
        if (codings == null || !codings.isArray()) return false;
        for (JsonNode coding : codings) {
            if (expectedCode.equals(coding.path("code").asText())) return true;
        }
        return false;
    }

    private static JsonNode findContainedById(JsonNode contained, String id) {
        if (contained == null || !contained.isArray() || id == null || id.trim().isEmpty()) return null;
        for (JsonNode resource : contained) {
            if (id.equals(resource.path("id").asText())) return resource;
        }
        return null;
    }

    private void collectQuestions(JsonNode items, String groupLinkId, String groupText, List<Question> out) {
        if (items == null || !items.isArray()) return;
        for (JsonNode item : items) {
            String type = item.path("type").asText();
            String text = item.path("text").asText();
            String linkId = item.path("linkId").asText();
            if ("group".equals(type) || (item.path("item").isArray() && !item.path("item").isMissingNode() && !item.has("answer"))) {
                collectQuestions(item.path("item"), linkId, text, out);
            } else {
                Question q = new Question();
                q.linkId = linkId; q.text = text; q.type = type;
                q.groupLinkId = groupLinkId; q.group = groupText;
                out.add(q);
                collectQuestions(item.path("item"), groupLinkId, groupText, out);
            }
        }
    }

    public JsonNode cariKandidat(SatuSehatRujukanIGDRanapDraft.Jenis jenis,
                                 String registrationId, String patientId, String encounterId,
                                 String diagnosisCode, String diagnosisDisplay,
                                 String secondaryDiagnosisCode, String secondaryDiagnosisDisplay,
                                 List<Question> answers,
                                 String provinceCode, String provinceDisplay,
                                 String cityCode, String cityDisplay,
                                 String serviceGroupSystem, String serviceGroupCode, String serviceGroupDisplay) throws Exception {
        require(patientId, "IHS pasien"); require(encounterId, "Encounter SATUSEHAT");
        require(diagnosisCode, "ICD-10 diagnosis utama");
        require(provinceCode, "Kode provinsi"); require(provinceDisplay, "Nama provinsi");
        require(cityCode, "Kode kabupaten/kota"); require(cityDisplay, "Nama kabupaten/kota");
        // Collection 30-06-2026 menempatkan Kelompok Layanan sebagai input Task kandidat.
        // System terminologi bersifat tetap; kode/display tetap harus dipilih sesuai layanan kasus.
        String sgSystem = notEmpty(serviceGroupSystem) ? serviceGroupSystem.trim() : SYS_KEMKES;
        require(serviceGroupCode, "Kode Kelompok Layanan");
        require(serviceGroupDisplay, "Nama Kelompok Layanan");

        // Collection koordinasi juga membangkitkan Registration_ID baru untuk Task kandidat.
        // Resource FHIR tetap dilacak dari Task.id yang dikembalikan SATUSEHAT.
        ObjectNode task = baseTask("request-referral-candidate", "Request for referral candidate", newTaskBusinessIdentifier(), encounterId);
        task.putObject("for").put("reference", "Patient/" + patientId.trim());
        ArrayNode contained = task.putArray("contained");
        String criteriaId = "qr-criteria-" + shortId();
        String areaId = "qr-area-" + shortId();
        contained.add(questionnaireResponse(criteriaId, "https://fhir.kemkes.go.id/Questionnaire/Q100", patientId, encounterId, answers));
        contained.add(areaResponse(areaId, patientId, encounterId, provinceCode, provinceDisplay, cityCode, cityDisplay));

        ArrayNode input = task.putArray("input");
        input.add(inputReference("referral-criteria", "Referral Criteria", "#" + criteriaId, "Referral Criteria Response"));
        input.add(inputReference("area", "Area", "#" + areaId, "Jejaring Wilayah Rujukan"));
        input.add(inputCoding(SYS_SNOMED, "119270007", "Management procedure", SYS_SNOMED,
                jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP ? RANAP_CARE_CODE : IGD_CARE_CODE,
                jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP ? "Inpatient care management" : "Emergency treatment management"));
        // Urutan input mengikuti collection 30-06-2026: Rawat Inap menempatkan
        // Kelompok Layanan sebelum diagnosis utama, sedangkan Rawat Darurat
        // menempatkan diagnosis utama sebelum Kelompok Layanan.
        if (jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP) {
            input.add(inputCoding(SYS_KEMKES, "TK000562", "Kelompok Layanan",
                    sgSystem, serviceGroupCode, serviceGroupDisplay));
            input.add(inputCoding(SYS_KEMKES, "primary-diagnosis", "Primary Diagnosis", SYS_ICD10, diagnosisCode, diagnosisDisplay));
        } else {
            input.add(inputCoding(SYS_KEMKES, "primary-diagnosis", "Primary Diagnosis", SYS_ICD10, diagnosisCode, diagnosisDisplay));
            input.add(inputCoding(SYS_KEMKES, "TK000562", "Kelompok Layanan",
                    sgSystem, serviceGroupCode, serviceGroupDisplay));
        }
        if (notEmpty(secondaryDiagnosisCode)) {
            input.add(inputCoding(SYS_KEMKES, "secondary-diagnosis", "Secondary diagnosis",
                    SYS_ICD10, secondaryDiagnosisCode, secondaryDiagnosisDisplay));
        }
        return post("/Task", task);
    }

    public List<Candidate> parseCandidates(JsonNode taskResponse) {
        List<Candidate> out = new ArrayList<Candidate>();
        if (taskResponse == null) return out;
        if ("Bundle".equals(taskResponse.path("resourceType").asText())) {
            JsonNode entries = taskResponse.path("entry");
            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode resource = entry.path("resource");
                    if (!"Task".equals(resource.path("resourceType").asText())) continue;
                    String code = codingCode(resource.path("code"));
                    if ("request-referral-candidate".equals(code)) {
                        collectCandidatesFromOutput(resource.path("output"), out);
                    }
                }
            }
        } else {
            collectCandidatesFromOutput(taskResponse.path("output"), out);
        }
        return out;
    }

    public String candidateTaskStatus(JsonNode response) {
        JsonNode task = candidateTaskNode(response);
        return task == null ? "" : task.path("status").asText();
    }

    private JsonNode candidateTaskNode(JsonNode response) {
        if (response == null) return null;
        if ("Task".equals(response.path("resourceType").asText())) return response;
        if ("Bundle".equals(response.path("resourceType").asText())) {
            JsonNode entries = response.path("entry");
            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode resource = entry.path("resource");
                    if (!"Task".equals(resource.path("resourceType").asText())) continue;
                    if ("request-referral-candidate".equals(codingCode(resource.path("code")))) return resource;
                }
            }
        }
        return null;
    }

    private void collectCandidatesFromOutput(JsonNode output, List<Candidate> out) {
        if (output == null || !output.isArray()) return;
        for (JsonNode x : output) {
            String typeCode = codingCode(x.path("type"));
            // Response Task pada collection memakai type code "candidate", sedangkan
            // Task rekomendasi di Bundle Rawat Inap memakai "candidate-referral-facility".
            if (!"candidate".equals(typeCode) && !"candidate-referral-facility".equals(typeCode)) continue;
            Candidate c = new Candidate();
            JsonNode ref = x.path("valueReference");
            c.orgId = stripPrefix(ref.path("reference").asText(), "Organization/");
            c.name = ref.path("display").asText();
            JsonNode exts = x.path("extension");
            if (exts.isArray()) {
                for (JsonNode ext : exts) parseProviderAttribute(ext, c);
            }
            out.add(c);
        }
    }

    private void parseProviderAttribute(JsonNode node, Candidate c) {
        JsonNode nested = node.path("extension");
        if (!nested.isArray()) return;
        int totalBeds=0, availableBeds=0; boolean hasBeds=false;
        for (JsonNode e : nested) {
            String url = e.path("url").asText();
            if ("distance".equals(url)) c.distance = quantity(e.path("valueQuantity"));
            else if ("estimated-time".equals(url)) c.eta = quantity(e.path("valueQuantity"));
            else if ("strata".equals(url)) c.strata = e.path("valueCode").asText();
            else if ("bpjs-code".equals(url)) c.bpjsCode = e.path("valueCode").asText();
            else if ("bed-availability".equals(url)) {
                hasBeds=true; JsonNode bedParts=e.path("extension");
                int total=0, available=0;
                if(bedParts.isArray())for(JsonNode part:bedParts){
                    String bu=part.path("url").asText();
                    if("total".equals(bu))total=parseInt(part.path("valueInteger").asText());
                    else if("available".equals(bu))available=parseInt(part.path("valueInteger").asText());
                }
                totalBeds+=total; availableBeds+=available;
            }
        }
        if(hasBeds)c.bed="Tersedia "+availableBeds+" / "+totalBeds;
    }

    public ApprovalBundleResult kirimTugasRujukan(SatuSehatRujukanIGDRanapDraft.Jenis jenis,
                                                   String registrationId, String patientId, String patientName,
                                                   String encounterId, String practitionerId, String practitionerName,
                                                   String conditionId, String secondaryConditionId, String description,
                                                   String clinicalSpecialityCode, String clinicalSpecialityDisplay,
                                                   String candidateTaskId, List<Question> criteriaAnswers,
                                                   String provinceCode, String provinceDisplay,
                                                   String cityCode, String cityDisplay,
                                                   String serviceGroupSystem, String serviceGroupCode, String serviceGroupDisplay,
                                                   String diagnosisCode, String diagnosisDisplay,
                                                   String secondaryDiagnosisCode, String secondaryDiagnosisDisplay,
                                                   List<Candidate> allCandidates, List<Candidate> selected) throws Exception {
        require(patientId, "IHS pasien"); require(encounterId, "Encounter SATUSEHAT");
        require(practitionerId, "IHS Practitioner/DPJP");
        if (jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP) require(conditionId, "Condition diagnosis utama");
        require(clinicalSpecialityCode, "Kode Clinical Speciality");
        require(clinicalSpecialityDisplay, "Nama Clinical Speciality");
        if (selected == null || selected.isEmpty()) throw new IllegalArgumentException("Pilih minimal satu fasyankes kandidat.");
        if (jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP) {
            // Data berikut dipakai ulang pada Task rekomendasi kandidat yang memang
            // menjadi entry tambahan Bundle Rawat Inap pada collection.
            require(candidateTaskId, "Task pencarian kandidat");
            require(provinceCode, "Kode provinsi"); require(provinceDisplay, "Nama provinsi");
            require(cityCode, "Kode kabupaten/kota"); require(cityDisplay, "Nama kabupaten/kota");
            require(serviceGroupCode, "Kode Kelompok Layanan"); require(serviceGroupDisplay, "Nama Kelompok Layanan");
            require(diagnosisCode, "ICD-10 diagnosis utama");
        }

        String orgId = orgId(); String now = nowUtc();
        ObjectNode bundle = mapper.createObjectNode(); bundle.put("resourceType", "Bundle"); bundle.put("type", "transaction");
        ObjectNode tag = bundle.putObject("meta").putArray("tag").addObject();
        tag.put("system", SYS_KEMKES); tag.put("code", "referral-approval"); tag.put("display", "Referral approval");
        ArrayNode entries = bundle.putArray("entry");

        // Task approval pada collection selalu basedOn CarePlan di bundle yang sama.
        String carePlanUrn = "urn:uuid:" + UUID.randomUUID().toString();
        for (Candidate c : selected) {
            require(c.orgId, "Organization kandidat");
            ObjectNode e = entries.addObject(); e.put("fullUrl", "urn:uuid:" + UUID.randomUUID().toString());
            ObjectNode task = e.putObject("resource"); task.put("resourceType", "Task");
            task.putArray("identifier").add(identifier("http://sys-ids.kemkes.go.id/task/" + orgId, registrationId));
            task.putArray("basedOn").addObject().put("reference", carePlanUrn);
            task.put("status", "requested"); task.put("intent", "instance-order"); task.put("priority", "routine");
            task.set("code", codeable(SYS_KEMKES, "referral-approval-request", "Referral approval request"));
            task.putObject("for").put("reference", "Patient/" + patientId);
            task.putObject("executionPeriod").put("start", now); task.put("authoredOn", now); task.put("lastModified", now);
            task.putObject("requester").put("reference", "Organization/" + orgId);
            task.putObject("owner").put("reference", "Organization/" + c.orgId);
            task.putObject("encounter").put("reference", "Encounter/" + encounterId);
            // Pada collection Rawat Inap reasonReference aktif; pada Rawat Darurat dikomentari/tidak dikirim.
            if (jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP && notEmpty(conditionId))
                task.putObject("reasonReference").put("reference", "Condition/" + conditionId);
            ObjectNode inp = task.putArray("input").addObject();
            inp.set("type", codeable(SYS_KEMKES, "referral-task", "Referral Task"));
            ObjectNode vr = inp.putObject("valueReference"); vr.put("reference", "Organization/" + c.orgId); if (notEmpty(c.name)) vr.put("display", c.name);
            ObjectNode req = e.putObject("request"); req.put("method", "POST"); req.put("url", "Task");
        }

        ObjectNode cpEntry = entries.addObject(); cpEntry.put("fullUrl", carePlanUrn);
        ObjectNode cp = cpEntry.putObject("resource"); cp.put("resourceType", "CarePlan");
        ArrayNode cpIds = cp.putArray("identifier");
        cpIds.add(identifier("http://sys-ids.kemkes.go.id/careplan/" + orgId, registrationId));
        cpIds.add(identifier("http://sys-ids.kemkes.go.id/careplan/authoring-organization", orgId));
        cp.put("status", "active"); cp.put("intent", "plan");
        ArrayNode cats = cp.putArray("category");
        cats.add(codeable(jenis == SatuSehatRujukanIGDRanapDraft.Jenis.IGD ? SYS_KEMKES : SYS_SNOMED,
                jenis == SatuSehatRujukanIGDRanapDraft.Jenis.IGD ? "TK000068" : "736353004",
                jenis == SatuSehatRujukanIGDRanapDraft.Jenis.IGD ? "Emergency care plan" : "Inpatient care plan"));
        cats.add(codeable(SYS_SNOMED, "3457005", "Patient referral"));
        cp.put("title", "Rencana Rujukan Pasien"); if (notEmpty(description)) cp.put("description", description);
        ObjectNode subj = cp.putObject("subject"); subj.put("reference", "Patient/" + patientId); if (notEmpty(patientName)) subj.put("display", patientName);
        cp.putObject("encounter").put("reference", "Encounter/" + encounterId); cp.put("created", now);
        ObjectNode author=cp.putObject("author"); author.put("reference","Practitioner/"+practitionerId); if(notEmpty(practitionerName))author.put("display",practitionerName);
        if (jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP) {
            ArrayNode addresses=cp.putArray("addresses");
            addresses.addObject().put("reference", "Condition/" + conditionId);
            if(notEmpty(secondaryConditionId))addresses.addObject().put("reference", "Condition/" + secondaryConditionId);
        }
        cp.putArray("contributor").addObject().put("reference", "Organization/" + orgId);
        ObjectNode detail=cp.putArray("activity").addObject().putObject("detail"); detail.put("kind","ServiceRequest");
        ObjectNode activityCode=codeable(SYS_CLINICAL_SPECIALITY,clinicalSpecialityCode,clinicalSpecialityDisplay);
        activityCode.put("text","Permintaan Layanan "+clinicalSpecialityDisplay);detail.set("code",activityCode);detail.put("status","not-started");
        ObjectNode cpReq=cpEntry.putObject("request"); cpReq.put("method","POST"); cpReq.put("url","CarePlan");

        // Collection Rawat Inap menambahkan satu Task rekomendasi/candidate response
        // di bundle approval. Rawat Darurat tidak menambahkan Task ini.
        if (jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP) {
            entries.add(candidateRecommendationEntry(registrationId, patientId, encounterId, candidateTaskId,
                    criteriaAnswers, provinceCode, provinceDisplay, cityCode, cityDisplay,
                    serviceGroupSystem, serviceGroupCode, serviceGroupDisplay,
                    diagnosisCode, diagnosisDisplay, secondaryDiagnosisCode, secondaryDiagnosisDisplay,
                    allCandidates, now, orgId));
        }

        JsonNode resp = post("", bundle);
        ApprovalBundleResult result = new ApprovalBundleResult(); result.raw = resp;
        JsonNode responseEntries = resp.path("entry"); int selectedIndex = 0;
        if (responseEntries.isArray()) {
            for (JsonNode re : responseEntries) {
                JsonNode rr = re.path("response"); String type = rr.path("resourceType").asText(); String id = rr.path("resourceID").asText();
                if (id.isEmpty() && notEmpty(type)) id = idFromLocation(rr.path("location").asText(), type);
                if ("Task".equals(type) && selectedIndex < selected.size()) {
                    Candidate c=selected.get(selectedIndex++); Approval a=new Approval(); a.orgId=c.orgId; a.name=c.name; a.taskId=id; result.approvals.add(a);
                } else if ("CarePlan".equals(type)) {
                    result.carePlanId=id;
                } else if ("Task".equals(type) && selectedIndex >= selected.size()) {
                    result.candidateRecommendationTaskId=id;
                }
            }
        }
        return result;
    }

    /**
     * Buat Condition diagnosis kunjungan bila belum tersedia pada cache lokal.
     * Struktur mengikuti helper Condition yang sudah dipakai form IGD/Ranap lama.
     */
    public JsonNode kirimCondition(String patientId, String encounterId, String patientName,
                                   String diagnosisCode, String diagnosisDisplay) throws Exception {
        require(patientId,"IHS pasien"); require(encounterId,"Encounter SATUSEHAT"); require(diagnosisCode,"ICD-10 diagnosis");
        ObjectNode c=mapper.createObjectNode();c.put("resourceType","Condition");
        c.set("clinicalStatus",codeable("http://terminology.hl7.org/CodeSystem/condition-clinical","active","Active"));
        c.putArray("category").add(codeable("http://terminology.hl7.org/CodeSystem/condition-category","encounter-diagnosis","Encounter Diagnosis"));
        c.set("code",codeable(SYS_ICD10,diagnosisCode,diagnosisDisplay));
        ObjectNode subject=c.putObject("subject");subject.put("reference","Patient/"+patientId);if(notEmpty(patientName))subject.put("display",patientName);
        ObjectNode encounter=c.putObject("encounter");encounter.put("reference","Encounter/"+encounterId);if(notEmpty(patientName))encounter.put("display","Diagnosa "+patientName);
        return post("/Condition",c);
    }

    public JsonNode cekTask(String taskId) throws Exception { require(taskId,"Task ID"); return get("/Task/" + enc(taskId)); }

    /**
     * Pra-permintaan dapat diproses asinkron oleh SATUSEHAT. Response POST awal
     * kadang baru berisi Task status=requested tanpa contained/output kriteria.
     * Ambil ulang melalui FHIR search agar enrichment Questionnaire/output dapat
     * terbaca ketika sudah tersedia.
     */
    public JsonNode cekTaskPraPermintaan(String taskId) throws Exception {
        require(taskId,"Task ID pra-permintaan");
        return get("/Task?_id=" + enc(taskId));
    }

    private JsonNode referralPreRequestTaskNode(JsonNode response) {
        if (response == null) return null;
        if ("Task".equals(response.path("resourceType").asText())) {
            String code = codingCode(response.path("code"));
            if ("referral-pre-request".equals(code)) return response;
        }
        if ("Bundle".equals(response.path("resourceType").asText())) {
            JsonNode entries = response.path("entry");
            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode resource = entry.path("resource");
                    if (!"Task".equals(resource.path("resourceType").asText())) continue;
                    if ("referral-pre-request".equals(codingCode(resource.path("code")))) return resource;
                }
            }
        }
        return null;
    }

    /**
     * Collection koordinasi Rujukan 30-06-2026 mengecek hasil pencarian kandidat
     * melalui FHIR search GET /Task?_id={Task_pencarian_kandidat_id}, bukan hanya
     * direct read /Task/{id}. Search response berbentuk Bundle dan diproses oleh
     * parseCandidates()/candidateTaskStatus().
     */
    public JsonNode cekTaskPencarianKandidat(String taskId) throws Exception {
        require(taskId,"Task ID pencarian kandidat");
        return get("/Task?_id=" + enc(taskId));
    }

    /** return accepted/rejected/"" */
    public static String parseDecision(JsonNode task) {
        JsonNode out = task == null ? null : task.path("output"); if (out == null || !out.isArray()) return "";
        for (JsonNode o: out) {
            if (hasCodingCode(o.path("type").path("coding"), "response-referral-task")) {
                String code=o.path("valueCoding").path("code").asText();
                if ("accepted".equalsIgnoreCase(code) || "rejected".equalsIgnoreCase(code)) return code.toLowerCase();
            }
        }
        return "";
    }

    /**
     * Finalisasi sederhana untuk alur IGD/Rawat Inap setelah RS tujuan accepted.
     * Tahap finalisasi tidak meminta input tambahan dari user; seluruh referensi
     * workflow sudah berasal dari Task/CarePlan yang terbentuk pada tahap sebelumnya.
     */
    public JsonNode finalisasi(SatuSehatRujukanIGDRanapDraft.Jenis jenis,
                               String registrationId, String patientId, String encounterId,
                               String carePlanId, String conditionId, String secondaryConditionId,
                               String targetOrgId, String targetOrgName,
                               String approvalTaskId) throws Exception {
        return finalisasi(jenis,registrationId,patientId,encounterId,carePlanId,conditionId,secondaryConditionId,
                targetOrgId,targetOrgName,approvalTaskId,"","","","");
    }

    /**
     * Overload lama dipertahankan agar pemanggil lain tetap kompatibel. Parameter
     * performerType/PCare/instruction bersifat opsional dan hanya dikirim bila ada.
     */
    public JsonNode finalisasi(SatuSehatRujukanIGDRanapDraft.Jenis jenis,
                               String registrationId, String patientId, String encounterId,
                               String carePlanId, String conditionId, String secondaryConditionId,
                               String targetOrgId, String targetOrgName,
                               String approvalTaskId,
                               String practitionerSpecialityCode, String practitionerSpecialityDisplay,
                               String noRujukanPcare, String instruction) throws Exception {
        require(patientId,"IHS pasien"); require(encounterId,"Encounter SATUSEHAT"); require(carePlanId,"CarePlan ID");
        if(jenis==SatuSehatRujukanIGDRanapDraft.Jenis.RANAP)require(conditionId,"Condition diagnosis utama");
        require(targetOrgId,"Organization tujuan final"); require(approvalTaskId,"Task approval tujuan final");
        String orgId=orgId(); ObjectNode sr=mapper.createObjectNode(); sr.put("resourceType","ServiceRequest");
        ArrayNode ids=sr.putArray("identifier"); ids.add(identifier("http://sys-ids.kemkes.go.id/servicerequest/"+orgId,registrationId));
        if(notEmpty(noRujukanPcare)) ids.add(identifier("http://sys-ids.kemkes.go.id/referral-number-pcare",noRujukanPcare));
        sr.putArray("basedOn").addObject().put("reference","CarePlan/"+carePlanId);
        sr.put("status","active"); sr.put("intent","original-order"); sr.put("priority","stat");
        sr.putArray("category").add(codeable(SYS_SNOMED,"3457005","Patient referral"));
        String careCode=jenis==SatuSehatRujukanIGDRanapDraft.Jenis.RANAP?RANAP_CARE_CODE:IGD_CARE_CODE;
        String careDisplay=jenis==SatuSehatRujukanIGDRanapDraft.Jenis.RANAP?"Inpatient care management":"Emergency treatment management";
        ObjectNode code=codeable(SYS_SNOMED,careCode,careDisplay); if(notEmpty(instruction))code.put("text",instruction); sr.set("code",code);
        sr.putObject("subject").put("reference","Patient/"+patientId); sr.putObject("encounter").put("reference","Encounter/"+encounterId);
        sr.put("occurrenceDateTime",nowUtc()); sr.putObject("requester").put("reference","Organization/"+orgId);
        // performerType hanya dikirim bila sistem memang memiliki terminologi SNOMED CT yang valid.
        // Jangan memaksa user mengisi atau mengirim kode tebakan pada tahap finalisasi.
        if(notEmpty(practitionerSpecialityCode)&&notEmpty(practitionerSpecialityDisplay))
            sr.set("performerType",codeable(SYS_SNOMED,practitionerSpecialityCode,practitionerSpecialityDisplay));
        ObjectNode performer=sr.putArray("performer").addObject(); performer.put("reference","Organization/"+targetOrgId); if(notEmpty(targetOrgName))performer.put("display",targetOrgName);
        if(jenis==SatuSehatRujukanIGDRanapDraft.Jenis.RANAP){
            ArrayNode reasons=sr.putArray("reasonReference");reasons.addObject().put("reference","Condition/"+conditionId);
            if(notEmpty(secondaryConditionId))reasons.addObject().put("reference","Condition/"+secondaryConditionId);
            ObjectNode si=sr.putArray("supportingInfo").addObject();si.put("display","Task Respon Kandidat Faskes Rujukan");si.put("reference","Task/"+approvalTaskId);
        }
        sr.putArray("locationCode").add(codeable("http://terminology.hl7.org/CodeSystem/v3-RoleCode","HOSP","Hospital"));
        if(notEmpty(instruction))sr.put("patientInstruction",instruction);
        return post("/ServiceRequest",sr);
    }

    public JsonNode getServiceRequest(String id) throws Exception { require(id,"ServiceRequest ID"); return get("/ServiceRequest/"+enc(id)); }

    public String getNomorRujukanNasional(JsonNode sr) {
        JsonNode ids=sr==null?null:sr.path("identifier"); if(ids!=null&&ids.isArray())for(JsonNode id:ids){
            String sys=id.path("system").asText().toLowerCase();
            if((sys.contains("referral-number-satusehat")||sys.contains("referral-number-national")||(sys.contains("referral-number")&&!sys.contains("pcare")))
                    && notEmpty(id.path("value").asText()))return id.path("value").asText();
        } return "";
    }

    public JsonNode cariRujukanMasuk() throws Exception {
        // Playbook Rujukan Pasien v6.1: Task approval ditujukan ke Task.owner (Org ID RS rujukan)
        // dan Task.basedOn mereferensikan CarePlan. _include membuat CarePlan ikut tersedia pada Bundle.
        String path="/Task?owner="+enc(orgId())+"&code="+enc("referral-approval-request")+"&_include=Task:based-on&_count=100";
        ObjectNode result=mapper.createObjectNode();result.put("resourceType","Bundle");result.put("type","searchset");
        ArrayNode entries=result.putArray("entry");
        java.util.Set<String> visited=new java.util.HashSet<String>(),resources=new java.util.HashSet<String>();
        while(notEmpty(path)){
            if(Thread.currentThread().isInterrupted())throw new InterruptedException("Pemuatan rujukan dihentikan.");
            if(visited.size()>=200||!visited.add(path))throw new IOException("Halaman rujukan belum lengkap: pagination berulang atau melewati batas 200 halaman.");
            JsonNode page=get(path);
            if(!"Bundle".equals(page.path("resourceType").asText()))throw new IOException("Respons pencarian rujukan bukan Bundle; jumlah rujukan belum dapat dipastikan.");
            JsonNode rows=page.path("entry");
            if(rows.isArray())for(JsonNode entry:rows){
                JsonNode r=entry.path("resource");
                if("OperationOutcome".equals(r.path("resourceType").asText())){
                    for(JsonNode issue:r.path("issue"))if("error".equals(issue.path("severity").asText())||"fatal".equals(issue.path("severity").asText()))
                        throw new IOException("SATUSEHAT mengembalikan catatan kegagalan pencarian rujukan.");
                }
                String key=r.path("resourceType").asText()+"/"+r.path("id").asText();
                if(resources.add(key))entries.add(entry);
            }
            String next="";for(JsonNode link:page.path("link"))if("next".equals(link.path("relation").asText()))next=link.path("url").asText();
            path=notEmpty(next)?incomingPagePath(baseUrl(),path,next):"";
        }
        return result;
    }

    /** Pagination hanya ke basis FHIR yang sama; jangan teruskan token ke URL lain. */
    static String incomingPagePath(String base,String current,String next)throws Exception{
        java.net.URI root=new java.net.URI(base),from=new java.net.URI(base+current);
        java.net.URI target=next.startsWith("?")?new java.net.URI(base+current.split("\\?",2)[0]+next):from.resolve(next).normalize();
        String prefix=root.getPath();if(prefix==null)prefix="";
        if(!root.getScheme().equalsIgnoreCase(target.getScheme())||!root.getAuthority().equals(target.getAuthority())
                ||target.getUserInfo()!=null||target.getFragment()!=null||target.getPath()==null||!target.getPath().startsWith(prefix+"/"))
            throw new IOException("Tautan halaman rujukan berada di luar basis FHIR yang dikonfigurasi.");
        return target.getRawPath().substring(root.getRawPath().length())+(target.getRawQuery()==null?"":"?"+target.getRawQuery());
    }

    String organizationId()throws Exception{return orgId();}
    JsonNode incomingReference(String reference)throws Exception{
        String[] parts=referenceParts(reference);
        if(parts==null||!("CarePlan".equals(parts[0])||"Organization".equals(parts[0])||"Patient".equals(parts[0])||"Encounter".equals(parts[0])))return null;
        return get("/"+parts[0]+"/"+enc(parts[1]));
    }
    public void validateIncomingTask(JsonNode task)throws Exception{
        if(task==null||!"Task".equals(task.path("resourceType").asText())
                ||!hasCodingCode(task.path("code").path("coding"),"referral-approval-request")
                ||!orgId().equals(referenceId(referenceText(task.path("owner")),"Organization")))
            throw new IOException("Task bukan rujukan persetujuan untuk RS ini.");
    }
    public static boolean incomingPending(JsonNode task){
        if(task==null||!parseDecision(task).isEmpty())return false;
        String status=task.path("status").asText();
        return "requested".equals(status)||"received".equals(status)||"ready".equals(status)||"in-progress".equals(status)||"on-hold".equals(status);
    }

    /**
     * Muat detail rujukan masuk dengan mengikuti graph referensi FHIR yang dikirim fasyankes perujuk.
     * Alur: Task -> CarePlan -> Patient/Encounter/Organization/Practitioner/Condition.
     * Setelah RS perujuk melakukan finalisasi, ServiceRequest dicari menggunakan parameter resmi
     * subject + encounter, lalu disaring lokal agar basedOn/performer sesuai rujukan ini.
     */
    public IncomingReferralDetail detailRujukanMasuk(String taskId) throws Exception {
        require(taskId,"Task ID rujukan masuk");
        IncomingReferralDetail d=new IncomingReferralDetail();d.taskId=taskId.trim();
        d.task=get("/Task/"+enc(d.taskId));
        validateIncomingTask(d.task);
        addLoadedReference(d,"Task persetujuan","Task/"+d.taskId,"",d.task);

        // Jalur pertama: beberapa implementasi SATUSEHAT masih menyertakan Task.basedOn -> CarePlan.
        // Namun pada transaksi nyata, Task referral-approval-request dapat tidak lagi membawa basedOn
        // setelah diproses/di-PATCH. Karena itu CarePlan TIDAK boleh bergantung hanya pada Task.basedOn.
        String carePlanRef=firstReference(d.task.path("basedOn"));
        d.carePlan=getReferenceQuiet(carePlanRef,d,"CarePlan rujukan dari Task.basedOn");
        d.carePlanId=referenceId(carePlanRef,"CarePlan");
        if(d.carePlan!=null&&notEmpty(d.carePlan.path("id").asText()))d.carePlanId=d.carePlan.path("id").asText();

        String patientRef=referenceText(d.task.path("for"));
        if(!notEmpty(patientRef)&&d.carePlan!=null)patientRef=referenceText(d.carePlan.path("subject"));
        d.patient=getReferenceQuiet(patientRef,d,"Patient");

        String encounterRef=referenceText(d.task.path("encounter"));
        if(!notEmpty(encounterRef)&&d.carePlan!=null)encounterRef=referenceText(d.carePlan.path("encounter"));
        d.encounter=getReferenceQuiet(encounterRef,d,"Encounter asal");
        if(d.encounter!=null){
            JsonNode locations=d.encounter.path("location");if(locations.isArray())for(JsonNode loc:locations)getReferenceQuiet(referenceText(loc.path("location")),d,"Lokasi Encounter");
            getReferenceQuiet(referenceText(d.encounter.path("serviceProvider")),d,"ServiceProvider Encounter");
            JsonNode participants=d.encounter.path("participant");if(participants.isArray())for(JsonNode part:participants){String ref=referenceText(part.path("individual"));if(notEmpty(ref)&&(ref.contains("Practitioner/")||ref.contains("PractitionerRole/")))getReferenceQuiet(ref,d,"Nakes Encounter");}
        }

        String orgRef=referenceText(d.task.path("requester"));
        if((!notEmpty(orgRef)||!orgRef.contains("Organization/"))&&d.carePlan!=null)orgRef=firstReference(d.carePlan.path("contributor"));
        d.referrerOrganization=getReferenceQuiet(orgRef,d,"Fasyankes perujuk");

        String authorRef=d.carePlan==null?"":referenceText(d.carePlan.path("author"));
        d.authorPractitioner=getReferenceQuiet(authorRef,d,"Dokter/author CarePlan");

        // Ikuti semua reference penting yang secara resmi dapat berada di CarePlan.
        // Ini membuat sisi penerima tidak hanya membaca description/activity.code,
        // tetapi juga supportingInfo, goal, activity.reference, reasonReference,
        // performer, location, outcomeReference, productReference, contributor, dll.
        if(d.carePlan!=null)followCarePlanReferences(d.carePlan,d);
        followTaskValueReferences(d.task,d);

        LinkedHashMap<String,String> conditionRefs=new LinkedHashMap<String,String>();
        collectReferences(d.task.path("reasonReference"),conditionRefs);
        if(d.carePlan!=null)collectReferences(d.carePlan.path("addresses"),conditionRefs);
        for(String ref:conditionRefs.keySet()){
            JsonNode c=getReferenceQuiet(ref,d,"Diagnosis/Condition");if(c!=null)d.conditions.add(c);
        }

        String patientId=referenceId(patientRef,"Patient"),encounterId=referenceId(encounterRef,"Encounter");
        if(notEmpty(patientId)&&notEmpty(encounterId)){
            JsonNode search=get("/ServiceRequest?subject="+enc(patientId)+"&encounter="+enc(encounterId));
            d.serviceRequest=selectReferralServiceRequest(search,d.carePlanId,d.taskId,orgId());
            if(d.serviceRequest!=null){
                d.serviceRequestId=d.serviceRequest.path("id").asText();
                d.nationalReferralNumber=getNomorRujukanNasional(d.serviceRequest);
                addLoadedReference(d,"ServiceRequest final","ServiceRequest/"+d.serviceRequestId,"",d.serviceRequest);

                // FALLBACK UTAMA UNTUK TRANSAKSI NYATA:
                // Bila Task tidak membawa basedOn, ambil CarePlan dari ServiceRequest.basedOn.
                // Contoh nyata: ServiceRequest.basedOn = CarePlan/{id}, sedangkan Task hasil GET
                // sudah tidak memiliki elemen basedOn. Tanpa fallback ini tab CarePlan akan kosong
                // walaupun JSON ServiceRequest jelas menyimpan referensinya.
                if(d.carePlan==null){
                    String srCarePlanRef=firstReferenceOfType(d.serviceRequest.path("basedOn"),"CarePlan");
                    if(notEmpty(srCarePlanRef)){
                        JsonNode cp=getReferenceQuietOnce(srCarePlanRef,d,"CarePlan rujukan dari ServiceRequest.basedOn");
                        if(cp!=null){
                            d.carePlan=cp;
                            d.carePlanId=referenceId(srCarePlanRef,"CarePlan");
                            if(notEmpty(cp.path("id").asText()))d.carePlanId=cp.path("id").asText();

                            // Setelah CarePlan baru ditemukan dari ServiceRequest, jalankan seluruh
                            // penelusuran yang sebelumnya hanya berjalan saat CarePlan berasal dari Task.
                            followCarePlanReferences(d.carePlan,d);

                            String cpAuthor=referenceText(d.carePlan.path("author"));
                            if(d.authorPractitioner==null&&notEmpty(cpAuthor))
                                d.authorPractitioner=getReferenceQuietOnce(cpAuthor,d,"Dokter/author CarePlan");

                            String cpOrg=firstReference(d.carePlan.path("contributor"));
                            if(d.referrerOrganization==null&&notEmpty(cpOrg)&&cpOrg.contains("Organization/"))
                                d.referrerOrganization=getReferenceQuietOnce(cpOrg,d,"Fasyankes perujuk dari CarePlan");

                            // Tambahkan diagnosis yang direferensikan CarePlan.addresses dan activity.reasonReference.
                            LinkedHashMap<String,String> cpConditions=new LinkedHashMap<String,String>();
                            collectReferences(d.carePlan.path("addresses"),cpConditions);
                            collectCarePlanReasonReferences(d.carePlan,cpConditions);
                            for(String ref:cpConditions.keySet())if(!containsReference(conditionRefs,ref)){
                                JsonNode c=getReferenceQuietOnce(ref,d,"Diagnosis/Condition CarePlan");
                                if(c!=null)d.conditions.add(c);
                                conditionRefs.put(ref,ref);
                            }
                        }
                    }
                }

                if(!notEmpty(d.nationalReferralNumber))d.warnings.add("ServiceRequest final sudah ditemukan, tetapi Nomor Rujukan Nasional belum tersedia pada identifier response terbaru.");
                String srRequester=referenceText(d.serviceRequest.path("requester"));if(notEmpty(srRequester)&&!srRequester.equals(orgRef))getReferenceQuiet(srRequester,d,"Requester ServiceRequest");
                LinkedHashMap<String,String> moreConditions=new LinkedHashMap<String,String>();
                collectReferences(d.serviceRequest.path("reasonReference"),moreConditions);
                for(String ref:moreConditions.keySet())if(!containsReference(conditionRefs,ref)){
                    JsonNode c=getReferenceQuiet(ref,d,"Diagnosis/Condition ServiceRequest");if(c!=null)d.conditions.add(c);conditionRefs.put(ref,ref);
                }
                JsonNode supporting=d.serviceRequest.path("supportingInfo");
                if(supporting.isArray())for(JsonNode r:supporting){
                    String ref=referenceText(r);if(!notEmpty(ref)||ref.equals("Task/"+d.taskId))continue;
                    getReferenceQuiet(ref,d,"Data pendukung");
                }
            }else d.warnings.add("ServiceRequest final belum ditemukan. Bila Task baru diterima, RS perujuk mungkin belum melakukan Finalisasi Rujukan; gunakan Muat Ulang setelah beberapa saat/proses perujuk selesai.");
        }else d.warnings.add("ServiceRequest belum dapat dicari karena referensi Patient atau Encounter tidak tersedia pada Task/CarePlan.");
        return d;
    }

    private void followTaskValueReferences(JsonNode task,IncomingReferralDetail d){
        if(task==null)return;JsonNode input=task.path("input");if(input.isArray())for(JsonNode x:input){String ref=referenceText(x.path("valueReference"));if(notEmpty(ref))getReferenceQuiet(ref,d,"Task input");}
        JsonNode output=task.path("output");if(output.isArray())for(JsonNode x:output){String ref=referenceText(x.path("valueReference"));if(notEmpty(ref))getReferenceQuiet(ref,d,"Task output");}
    }
    private void followCarePlanReferences(JsonNode cp,IncomingReferralDetail d){
        if(cp==null)return;
        followReferenceNodes(cp.path("contributor"),d,"Contributor CarePlan");
        followReferenceNodes(cp.path("supportingInfo"),d,"SupportingInfo CarePlan");
        followReferenceNodes(cp.path("goal"),d,"Goal CarePlan");
        JsonNode activity=cp.path("activity");if(activity.isArray())for(JsonNode a:activity){
            followReferenceNodes(a.path("reference"),d,"Activity CarePlan");
            followReferenceNodes(a.path("outcomeReference"),d,"Outcome CarePlan");
            JsonNode detail=a.path("detail");
            followReferenceNodes(detail.path("reasonReference"),d,"Alasan Activity CarePlan");
            followReferenceNodes(detail.path("goal"),d,"Goal Activity CarePlan");
            followReferenceNodes(detail.path("location"),d,"Lokasi Activity CarePlan");
            followReferenceNodes(detail.path("performer"),d,"Pelaksana Activity CarePlan");
            followReferenceNodes(detail.path("productReference"),d,"Produk Activity CarePlan");
        }
    }
    private void followReferenceNodes(JsonNode refs,IncomingReferralDetail d,String role){
        if(refs==null||refs.isMissingNode()||refs.isNull())return;
        if(refs.isArray()){for(JsonNode r:refs){String ref=referenceText(r);if(notEmpty(ref))getReferenceQuietOnce(ref,d,role);}}
        else{String ref=referenceText(refs);if(notEmpty(ref))getReferenceQuietOnce(ref,d,role);}
    }
    private JsonNode getReferenceQuietOnce(String reference,IncomingReferralDetail d,String role){
        if(!notEmpty(reference))return null;String[] p=referenceParts(reference);String normalized=p==null?reference:p[0]+"/"+p[1];
        for(IncomingReferencedResource x:d.references)if(normalized.equals(x.reference)&&x.resource!=null)return x.resource;
        return getReferenceQuiet(reference,d,role);
    }

    /** Ambil reference pertama dengan tipe resource tertentu dari Reference atau array Reference. */
    private static String firstReferenceOfType(JsonNode refs,String resourceType){
        if(refs==null||!notEmpty(resourceType))return "";
        if(refs.isArray())for(JsonNode r:refs){String ref=referenceText(r);String[] p=referenceParts(ref);if(p!=null&&resourceType.equals(p[0]))return ref;}
        else{String ref=referenceText(refs);String[] p=referenceParts(ref);if(p!=null&&resourceType.equals(p[0]))return ref;}
        return "";
    }

    /** Kumpulkan reasonReference pada seluruh activity.detail CarePlan. */
    private static void collectCarePlanReasonReferences(JsonNode cp,Map<String,String> out){
        if(cp==null||out==null)return;JsonNode activity=cp.path("activity");
        if(activity.isArray())for(JsonNode a:activity){JsonNode detail=a.path("detail");collectReferences(detail.path("reasonReference"),out);}
    }

    private JsonNode selectReferralServiceRequest(JsonNode bundle,String carePlanId,String taskId,String targetOrgId){
        if(bundle==null)return null;JsonNode entries=bundle.path("entry");if(!entries.isArray())return null;
        JsonNode best=null;int bestScore=-1;
        for(JsonNode e:entries){JsonNode sr=e.path("resource");if(!"ServiceRequest".equals(sr.path("resourceType").asText()))continue;int score=0;
            boolean basedOnMatch=notEmpty(carePlanId)&&referenceArrayContains(sr.path("basedOn"),"CarePlan/"+carePlanId);
            boolean referralCategory=codeableArrayContains(sr.path("category"),"3457005");
            String code=codingCode(sr.path("code"));boolean referralCareCode=RANAP_CARE_CODE.equals(code)||IGD_CARE_CODE.equals(code);
            // Jangan pernah memilih ServiceRequest lab/radiologi lain dari encounter yang sama hanya karena performer sama.
            if(!basedOnMatch&&!referralCategory&&!referralCareCode)continue;
            if(basedOnMatch)score+=10;if(referenceArrayContains(sr.path("performer"),"Organization/"+targetOrgId))score+=7;if(referralCategory)score+=5;if(referralCareCode)score+=4;
            if(referenceArrayContains(sr.path("supportingInfo"),"Task/"+taskId))score+=3;
            if(score>bestScore){best=sr;bestScore=score;}
        }
        // Hindari mengambil ServiceRequest lain pada encounter yang sama bila tidak ada ciri rujukan.
        return bestScore>=4?best:null;
    }
    private JsonNode getReferenceQuiet(String reference,IncomingReferralDetail d,String role){
        if(!notEmpty(reference))return null;
        String[] parts=referenceParts(reference);if(parts==null){d.warnings.add(role+": referensi tidak dapat dibaca ("+reference+").");return null;}
        // Reference yang berasal dari resource FHIR SATUSEHAT dicoba langsung.
        // Jangan batasi ke allow-list kecil karena CarePlan.supportingInfo/activity dapat
        // mereferensikan banyak tipe resource FHIR yang sah.
        IncomingReferencedResource x=new IncomingReferencedResource();x.role=role;x.reference=parts[0]+"/"+parts[1];
        try{x.resource=get("/"+parts[0]+"/"+enc(parts[1]));x.display=referenceDisplay(x.resource);d.references.add(x);return x.resource;}
        catch(Exception ex){x.error=ex.getMessage()==null?ex.getClass().getSimpleName():ex.getMessage();d.references.add(x);d.warnings.add(role+" belum dapat dimuat: "+x.error);return null;}
    }
    private static boolean isSupportedIncomingReference(String type){
        return "CarePlan".equals(type)||"Patient".equals(type)||"Encounter".equals(type)||"Condition".equals(type)||"Practitioner".equals(type)||"PractitionerRole".equals(type)||"Organization".equals(type)||
            "Observation".equals(type)||"Procedure".equals(type)||"AllergyIntolerance".equals(type)||"DiagnosticReport".equals(type)||"Composition".equals(type)||
            "DocumentReference".equals(type)||"MedicationStatement".equals(type)||"QuestionnaireResponse".equals(type)||"ServiceRequest".equals(type)||"Task".equals(type)||"Location".equals(type);
    }
    private static String[] referenceParts(String reference){
        if(!notEmpty(reference)||reference.startsWith("#")||reference.startsWith("urn:"))return null;String s=reference.trim();int q=s.indexOf('?');if(q>=0)s=s.substring(0,q);int h=s.indexOf('#');if(h>=0)s=s.substring(0,h);
        int hist=s.indexOf("/_history/");if(hist>=0)s=s.substring(0,hist);while(s.endsWith("/"))s=s.substring(0,s.length()-1);String[] a=s.split("/");if(a.length<2)return null;return new String[]{a[a.length-2],a[a.length-1]};
    }
    private static String referenceId(String reference,String expectedType){String[] p=referenceParts(reference);return p!=null&&(expectedType==null||expectedType.equals(p[0]))?p[1]:"";}
    private static String referenceText(JsonNode ref){return ref==null?"":ref.path("reference").asText();}
    private static String firstReference(JsonNode refs){if(refs==null)return "";if(refs.isArray())for(JsonNode r:refs){String x=referenceText(r);if(notEmpty(x))return x;}return referenceText(refs);}
    private static void collectReferences(JsonNode refs,Map<String,String> out){if(refs==null||out==null)return;if(refs.isArray()){for(JsonNode r:refs){String x=referenceText(r);if(notEmpty(x))out.put(x,x);}}else{String x=referenceText(refs);if(notEmpty(x))out.put(x,x);}}
    private static boolean containsReference(Map<String,String> refs,String target){return refs!=null&&refs.containsKey(target);}
    private static boolean referenceArrayContains(JsonNode refs,String target){if(!notEmpty(target)||refs==null)return false;if(refs.isArray()){for(JsonNode r:refs)if(referenceMatches(referenceText(r),target))return true;}else return referenceMatches(referenceText(refs),target);return false;}
    private static boolean referenceMatches(String actual,String target){if(!notEmpty(actual)||!notEmpty(target))return false;return target.equals(actual)||actual.endsWith("/"+target);}
    private static boolean codeableArrayContains(JsonNode values,String code){if(values==null||!values.isArray())return false;for(JsonNode v:values){JsonNode c=v.path("coding");if(c.isArray())for(JsonNode x:c)if(code.equals(x.path("code").asText()))return true;}return false;}
    private void addLoadedReference(IncomingReferralDetail d,String role,String reference,String display,JsonNode resource){IncomingReferencedResource x=new IncomingReferencedResource();x.role=role;x.reference=reference;x.display=notEmpty(display)?display:referenceDisplay(resource);x.resource=resource;d.references.add(x);}
    private static String referenceDisplay(JsonNode resource){if(resource==null)return "";String rt=resource.path("resourceType").asText();if("Organization".equals(rt)||"Location".equals(rt))return resource.path("name").asText();JsonNode names=resource.path("name");if(names.isArray()&&names.size()>0){JsonNode n=names.get(0);if(notEmpty(n.path("text").asText()))return n.path("text").asText();StringBuilder b=new StringBuilder();JsonNode given=n.path("given");if(given.isArray())for(JsonNode g:given)if(notEmpty(g.asText())){if(b.length()>0)b.append(' ');b.append(g.asText());}if(notEmpty(n.path("family").asText())){if(b.length()>0)b.append(' ');b.append(n.path("family").asText());}if(b.length()>0)return b.toString();}String title=resource.path("title").asText();if(notEmpty(title))return title;JsonNode coding=resource.path("code").path("coding");if(coding.isArray()&&coding.size()>0){String display=coding.get(0).path("display").asText(),code=coding.get(0).path("code").asText();if(notEmpty(display))return display;if(notEmpty(code))return code;}String description=resource.path("description").asText();if(notEmpty(description))return description;return resource.path("status").asText();}

    public JsonNode responRujukanMasuk(String taskId, boolean accepted) throws Exception {
        require(taskId,"Task ID");JsonNode current=cekTask(taskId);validateIncomingTask(current);
        String decision=parseDecision(current),wanted=accepted?"accepted":"rejected";
        if(wanted.equals(decision))return current;
        if(!incomingPending(current))throw new IOException("Rujukan sudah diputuskan atau tidak lagi menunggu respons. Perbarui daftar rujukan.");
        ArrayNode patch=mapper.createArrayNode();
        // RFC 6902 test melindungi keputusan operator lain yang masuk bersamaan.
        ObjectNode test=patch.addObject();test.put("op","test");test.put("path","/status");test.put("value",current.path("status").asText());
        if(notEmpty(current.path("meta").path("versionId").asText())){
            ObjectNode version=patch.addObject();version.put("op","test");version.put("path","/meta/versionId");version.put("value",current.path("meta").path("versionId").asText());
        }
        ObjectNode p1=patch.addObject();p1.put("op","replace");p1.put("path","/status");p1.put("value","completed");
        ObjectNode p2=patch.addObject();p2.put("op","add");p2.put("path","/output");ArrayNode outputs=p2.putArray("value");
        if(current.path("output").isArray())for(JsonNode old:current.path("output"))outputs.add(old);
        ObjectNode o=outputs.addObject();o.set("type",codeable(SYS_KEMKES,"response-referral-task","Response referral task"));
        ObjectNode vc=o.putObject("valueCoding");vc.put("system",SYS_TASK_STATUS);vc.put("code",accepted?"accepted":"rejected");vc.put("display",accepted?"Accepted":"Rejected");
        patch("/Task/"+enc(taskId),patch);
        JsonNode confirmed=cekTask(taskId);validateIncomingTask(confirmed);
        if(!wanted.equals(parseDecision(confirmed)))throw new IOException("Respons terkirim tetapi keputusan terbaru belum dapat dikonfirmasi. Perbarui daftar sebelum mencoba kembali.");
        return confirmed;
    }

    public String compactCandidates(List<Candidate> rows) throws Exception {
        ArrayNode a=mapper.createArrayNode(); if(rows!=null)for(Candidate c:rows){ObjectNode n=a.addObject();n.put("selected",c.selected);n.put("orgId",c.orgId);n.put("name",c.name);n.put("bpjsCode",c.bpjsCode);n.put("strata",c.strata);n.put("distance",c.distance);n.put("eta",c.eta);n.put("bed",c.bed);}return mapper.writeValueAsString(a);
    }
    public List<Candidate> parseCompactCandidates(String json) throws Exception {
        List<Candidate> out=new ArrayList<Candidate>(); if(!notEmpty(json))return out;JsonNode a=mapper.readTree(json);if(a.isArray())for(JsonNode n:a){Candidate c=new Candidate();c.selected=n.path("selected").asBoolean();c.orgId=n.path("orgId").asText();c.name=n.path("name").asText();c.bpjsCode=n.path("bpjsCode").asText();c.strata=n.path("strata").asText();c.distance=n.path("distance").asText();c.eta=n.path("eta").asText();c.bed=n.path("bed").asText();out.add(c);}return out;
    }
    public String compactApprovals(List<Approval> rows) throws Exception {
        ArrayNode a=mapper.createArrayNode();if(rows!=null)for(Approval r:rows){ObjectNode n=a.addObject();n.put("finalChoice",r.finalChoice);n.put("orgId",r.orgId);n.put("name",r.name);n.put("taskId",r.taskId);n.put("status",r.status);n.put("decision",r.decision);}return mapper.writeValueAsString(a);
    }
    public List<Approval> parseCompactApprovals(String json) throws Exception {
        List<Approval> out=new ArrayList<Approval>();if(!notEmpty(json))return out;JsonNode a=mapper.readTree(json);if(a.isArray())for(JsonNode n:a){Approval r=new Approval();r.finalChoice=n.path("finalChoice").asBoolean();r.orgId=n.path("orgId").asText();r.name=n.path("name").asText();r.taskId=n.path("taskId").asText();r.status=n.path("status").asText();r.decision=n.path("decision").asText();out.add(r);}return out;
    }
    public String questionsJson(List<Question> rows) throws Exception {
        ArrayNode a=mapper.createArrayNode();if(rows!=null)for(Question q:rows){ObjectNode n=a.addObject();n.put("linkId",q.linkId);n.put("text",q.text);n.put("type",q.type);n.put("group",q.group);n.put("groupLinkId",q.groupLinkId);n.put("answer",q.answer);}return mapper.writeValueAsString(a);
    }
    public List<Question> parseQuestionsJson(String json) throws Exception {
        List<Question> out=new ArrayList<Question>();if(!notEmpty(json))return out;JsonNode a=mapper.readTree(json);if(a.isArray())for(JsonNode n:a){Question q=new Question();q.linkId=n.path("linkId").asText();q.text=n.path("text").asText();q.type=n.path("type").asText();q.group=n.path("group").asText();q.groupLinkId=n.path("groupLinkId").asText();q.answer=n.path("answer").asText();out.add(q);}return out;
    }

    private ObjectNode questionnaireResponse(String id,String questionnaire,String patient,String encounter,List<Question> answers){
        ObjectNode qr=mapper.createObjectNode();qr.put("resourceType","QuestionnaireResponse");qr.put("id",id);qr.put("questionnaire",questionnaire);qr.put("status","completed");qr.putObject("subject").put("reference","Patient/"+patient);qr.putObject("encounter").put("reference","Encounter/"+encounter);
        ArrayNode items=qr.putArray("item");
        Map<String,ArrayNode> groupedItems=new LinkedHashMap<String,ArrayNode>();
        if(answers!=null)for(Question q:answers){
            if(!notEmpty(q.linkId)||!notEmpty(q.answer))continue;
            ArrayNode target=items;
            if(notEmpty(q.group)||notEmpty(q.groupLinkId)){
                String groupId=notEmpty(q.groupLinkId)?q.groupLinkId:"0";
                String groupKey=groupId+"|"+q.group;
                target=groupedItems.get(groupKey);
                if(target==null){
                    ObjectNode group=items.addObject();group.put("linkId",groupId);if(notEmpty(q.group))group.put("text",q.group);
                    target=group.putArray("item");groupedItems.put(groupKey,target);
                }
            }
            appendQuestionAnswer(target,q);
        }
        return qr;
    }
    private void appendQuestionAnswer(ArrayNode target,Question q){
        ObjectNode item=target.addObject();item.put("linkId",q.linkId);if(notEmpty(q.text))item.put("text",q.text);
        ObjectNode ans=item.putArray("answer").addObject();String t=q.type==null?"":q.type.toLowerCase();
        if("boolean".equals(t))ans.put("valueBoolean",parseBooleanAnswer(q.answer,q.linkId));
        else if("integer".equals(t)){
            try{ans.put("valueInteger",Integer.parseInt(q.answer.trim()));}
            catch(NumberFormatException e){throw new IllegalArgumentException("Jawaban "+q.linkId+" harus berupa bilangan bulat.");}
        }else if("date".equals(t))ans.put("valueDate",q.answer.trim());
        else if("datetime".equals(t)||"dateTime".equals(q.type))ans.put("valueDateTime",q.answer.trim());
        else if("choice".equals(t)||"open-choice".equals(t)){
            String[] part=q.answer.split("\\|",-1);
            if(part.length<2||!notEmpty(part[0])||!notEmpty(part[1]))throw new IllegalArgumentException("Jawaban pilihan "+q.linkId+" gunakan format system|code|display.");
            ObjectNode coding=ans.putObject("valueCoding");coding.put("system",part[0].trim());coding.put("code",part[1].trim());if(part.length>2&&notEmpty(part[2]))coding.put("display",part[2].trim());
        }else ans.put("valueString",q.answer);
    }
    private static boolean parseBooleanAnswer(String value,String linkId){
        String v=value==null?"":value.trim().toLowerCase();
        if("true".equals(v)||"ya".equals(v)||"yes".equals(v)||"y".equals(v)||"1".equals(v))return true;
        if("false".equals(v)||"tidak".equals(v)||"no".equals(v)||"n".equals(v)||"0".equals(v))return false;
        throw new IllegalArgumentException("Jawaban "+linkId+" harus Ya/Tidak atau true/false.");
    }
    private ObjectNode areaResponse(String id,String patient,String encounter,String provinceCode,String provinceDisplay,String cityCode,String cityDisplay){
        ObjectNode qr=mapper.createObjectNode();qr.put("resourceType","QuestionnaireResponse");qr.put("id",id);qr.put("questionnaire","https://fhir.kemkes.go.id/Questionnaire/Q101");qr.put("status","completed");qr.putObject("subject").put("reference","Patient/"+patient);qr.putObject("encounter").put("reference","Encounter/"+encounter);
        ObjectNode root=qr.putArray("item").addObject();root.put("linkId","1");root.put("text","Jejaring wilayah rujukan");ArrayNode sub=root.putArray("item");
        ObjectNode prov=sub.addObject();prov.put("linkId","1.1");prov.put("text","Provinsi");ObjectNode pvc=prov.putArray("answer").addObject().putObject("valueCoding");pvc.put("system",SYS_ADMIN_AREA);pvc.put("code",provinceCode);pvc.put("display",provinceDisplay);
        ObjectNode city=sub.addObject();city.put("linkId","1.2");city.put("text","Kabupaten/Kota");ObjectNode cvc=city.putArray("answer").addObject().putObject("valueCoding");cvc.put("system",SYS_ADMIN_AREA);cvc.put("code",cityCode);cvc.put("display",cityDisplay);return qr;
    }
    private ObjectNode candidateRecommendationEntry(String registrationId,String patientId,String encounterId,String candidateTaskId,
                                                      List<Question> criteriaAnswers,
                                                      String provinceCode,String provinceDisplay,String cityCode,String cityDisplay,
                                                      String serviceGroupSystem,String serviceGroupCode,String serviceGroupDisplay,
                                                      String diagnosisCode,String diagnosisDisplay,
                                                      String secondaryDiagnosisCode,String secondaryDiagnosisDisplay,
                                                      List<Candidate> candidates,String now,String orgId){
        ObjectNode e=mapper.createObjectNode();e.put("fullUrl","urn:uuid:"+UUID.randomUUID().toString());
        ObjectNode task=e.putObject("resource");task.put("resourceType","Task");
        String criteriaId="qr-criteria-"+shortId(),areaId="qr-area-"+shortId();
        ArrayNode contained=task.putArray("contained");
        contained.add(questionnaireResponse(criteriaId,"https://fhir.kemkes.go.id/Questionnaire/Q100",patientId,encounterId,criteriaAnswers));
        contained.add(areaResponse(areaId,patientId,encounterId,provinceCode,provinceDisplay,cityCode,cityDisplay));
        task.putArray("identifier").add(identifier("http://sys-ids.kemkes.go.id/task/"+orgId,registrationId));
        task.put("status","completed");task.put("intent","instance-order");task.put("priority","routine");
        task.set("code",codeable(SYS_KEMKES,"request-referral-candidate","Request for referral candidate"));
        task.putArray("basedOn").addObject().put("reference","Task/"+candidateTaskId);
        task.putObject("for").put("reference","Patient/"+patientId);task.putObject("executionPeriod").put("start",now);
        task.put("authoredOn",now);task.put("lastModified",now);task.putObject("requester").put("reference","Organization/"+orgId);
        task.putObject("owner").put("reference","Organization/"+orgId);task.putObject("encounter").put("reference","Encounter/"+encounterId);
        ArrayNode input=task.putArray("input");
        input.add(inputReference("referral-criteria","Referral Criteria","#"+criteriaId,"Referral Criteria Response"));
        input.add(inputReference("area","Area","#"+areaId,"Jejaring Wilayah Rujukan"));
        input.add(inputCoding(SYS_SNOMED,"119270007","Management procedure",SYS_SNOMED,RANAP_CARE_CODE,"Inpatient care management"));
        input.add(inputCoding(SYS_KEMKES,"TK000562","Kelompok Layanan",notEmpty(serviceGroupSystem)?serviceGroupSystem:SYS_KEMKES,serviceGroupCode,serviceGroupDisplay));
        input.add(inputCoding(SYS_KEMKES,"primary-diagnosis","Primary Diagnosis",SYS_ICD10,diagnosisCode,diagnosisDisplay));
        if(notEmpty(secondaryDiagnosisCode))input.add(inputCoding(SYS_KEMKES,"secondary-diagnosis","Secondary diagnosis",SYS_ICD10,secondaryDiagnosisCode,secondaryDiagnosisDisplay));
        ArrayNode output=task.putArray("output");
        if(candidates!=null)for(Candidate c:candidates){
            if(c==null||!notEmpty(c.orgId))continue;
            ObjectNode o=output.addObject();o.set("type",codeable(SYS_KEMKES,"candidate-referral-facility","Candidate referral facility"));
            ObjectNode ref=o.putObject("valueReference");ref.put("reference","Organization/"+c.orgId);if(notEmpty(c.name))ref.put("display",c.name);
            ObjectNode attr=o.putArray("extension").addObject();attr.put("url","https://fhir.kemkes.go.id/r4/StructureDefinition/providerAtribute");
            ArrayNode ext=attr.putArray("extension");addQuantityFromText(ext,"distance",c.distance,"km");
            if(notEmpty(c.strata)){ObjectNode x=ext.addObject();x.put("url","strata");x.put("valueCode",c.strata);}
            if(notEmpty(c.bpjsCode)){ObjectNode x=ext.addObject();x.put("url","bpjs-code");x.put("valueCode",c.bpjsCode);}
        }
        ObjectNode req=e.putObject("request");req.put("method","POST");req.put("url","Task");return e;
    }
    private static void addQuantityFromText(ArrayNode ext,String url,String text,String defaultUnit){
        if(ext==null||!notEmpty(text))return;String value=text.trim(),unit=defaultUnit;int space=value.indexOf(' ');
        if(space>0){unit=value.substring(space+1).trim();value=value.substring(0,space).trim();}
        try{double number=Double.parseDouble(value.replace(',','.'));ObjectNode x=ext.addObject();x.put("url",url);ObjectNode q=x.putObject("valueQuantity");q.put("value",number);q.put("unit",notEmpty(unit)?unit:defaultUnit);q.put("system","http://unitofmeasure.org");q.put("code",notEmpty(unit)?unit:defaultUnit);}catch(NumberFormatException ignored){}
    }

    private ObjectNode baseTask(String code,String display,String registrationId,String encounterId) throws Exception {
        String orgId=orgId(),now=nowUtc();ObjectNode t=mapper.createObjectNode();t.put("resourceType","Task");
        t.putArray("identifier").add(identifier("http://sys-ids.kemkes.go.id/task/"+orgId,notEmpty(registrationId)?registrationId:UUID.randomUUID().toString()));
        t.put("status","requested");t.put("intent","instance-order");t.put("priority","routine");t.set("code",codeable(SYS_KEMKES,code,display));t.put("authoredOn",now);t.put("lastModified",now);t.putObject("requester").put("reference","Organization/"+orgId);t.putObject("owner").put("reference","Organization/"+orgId);t.putObject("encounter").put("reference","Encounter/"+encounterId);return t;
    }
    private ObjectNode inputCoding(String typeSystem,String typeCode,String typeDisplay,String valueSystem,String valueCode,String valueDisplay){ObjectNode i=mapper.createObjectNode();i.set("type",codeable(typeSystem,typeCode,typeDisplay));ObjectNode v=i.putObject("valueCoding");v.put("system",valueSystem);v.put("code",valueCode);if(notEmpty(valueDisplay))v.put("display",valueDisplay);return i;}
    private ObjectNode inputReference(String typeCode,String typeDisplay,String reference,String display){ObjectNode i=mapper.createObjectNode();i.set("type",codeable(SYS_KEMKES,typeCode,typeDisplay));ObjectNode r=i.putObject("valueReference");r.put("reference",reference);if(notEmpty(display))r.put("display",display);return i;}
    private ObjectNode codeable(String system,String code,String display){ObjectNode c=mapper.createObjectNode();ObjectNode x=c.putArray("coding").addObject();x.put("system",system);x.put("code",code);if(notEmpty(display))x.put("display",display);return c;}
    private ObjectNode identifier(String system,String value){ObjectNode i=mapper.createObjectNode();i.put("system",system);i.put("value",notEmpty(value)?value:UUID.randomUUID().toString());return i;}
    private String codingCode(JsonNode cc){JsonNode a=cc.path("coding");return a.isArray()&&a.size()>0?a.get(0).path("code").asText():"";}
    private static String quantity(JsonNode q){if(q==null||q.isMissingNode())return "";String v=q.path("value").asText();String u=q.path("unit").asText();return (v+" "+u).trim();}
    private static int parseInt(String value){try{return Integer.parseInt(value==null?"0":value.trim());}catch(Exception ex){return 0;}}
    private static String stripPrefix(String s,String p){return s!=null&&s.startsWith(p)?s.substring(p.length()):s==null?"":s;}
    private static String idFromLocation(String location,String resourceType){if(location==null)return "";String mark="/"+resourceType+"/";int i=location.indexOf(mark);if(i<0)return "";String rest=location.substring(i+mark.length());int slash=rest.indexOf('/');return slash>=0?rest.substring(0,slash):rest;}
    /**
     * Identifier bisnis Task mengikuti pola collection koordinasi:
     * YYYYMMDD + 5 digit acak. Identifier ini BUKAN no_rawat dan harus baru
     * untuk setiap Task pra-permintaan / pencarian kandidat.
     */
    private static String newTaskBusinessIdentifier(){
        SimpleDateFormat f=new SimpleDateFormat("yyyyMMdd");
        String date=f.format(new Date());
        int random=new java.util.Random().nextInt(100000);
        String digits=String.valueOf(random);
        while(digits.length()<5)digits="0"+digits;
        return date+digits;
    }

    private static String shortId(){return UUID.randomUUID().toString().replace("-","").substring(0,12);}
    private static String resourceIdFromLocation(String location){
        if(!notEmpty(location))return "";
        String s=location.trim();int q=s.indexOf('?');if(q>=0)s=s.substring(0,q);int h=s.indexOf('#');if(h>=0)s=s.substring(0,h);
        int hist=s.indexOf("/_history/");if(hist>=0)s=s.substring(0,hist);while(s.endsWith("/"))s=s.substring(0,s.length()-1);
        int slash=s.lastIndexOf('/');return slash>=0&&slash<s.length()-1?s.substring(slash+1):"";
    }
    private static boolean notEmpty(String s){return s!=null&&!s.trim().isEmpty();}
    private static void require(String s,String label){if(!notEmpty(s))throw new IllegalArgumentException(label+" wajib tersedia.");}
    private static String enc(String s)throws Exception{return URLEncoder.encode(s,"UTF-8");}
    private String orgId() throws Exception {String id=koneksiDB.IDSATUSEHAT();require(id,"Organization ID SATUSEHAT RS");return id.trim();}
    private static String nowUtc(){SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.format(new Date());}
    private String baseUrl() throws Exception {String u=koneksiDB.URLFHIRSATUSEHAT();require(u,"URL FHIR SATUSEHAT");while(u.endsWith("/"))u=u.substring(0,u.length()-1);return u;}
    private JsonNode post(String path,JsonNode body)throws Exception{return exchange(HttpMethod.POST,path,body,MediaType.APPLICATION_JSON);}
    private JsonNode get(String path)throws Exception{return exchange(HttpMethod.GET,path,null,MediaType.APPLICATION_JSON);}
    /**
     * PATCH JSON Patch tidak memakai HttpMethod.PATCH karena project Khanza
     * masih kompatibel dengan Spring Web 3.1.x yang belum menyediakan enum PATCH.
     * Apache Commons HttpClient sudah ada di lib Khanza dan dapat mengirim verb
     * PATCH tanpa mengubah konfigurasi RestTemplate/API SatuSehat yang lama.
     */
    private JsonNode patch(String path,JsonNode body)throws Exception{
        final String url=baseUrl()+path;
        final String requestText=body==null?"":mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
        final String requestJson=body==null?"":mapper.writeValueAsString(body);
        final String patchContentType="application/json-patch+json";
        lastMethod="PATCH";lastUrl=url;
        lastRequest="Client-Patch: PATCH-HEADERS-20260914-R2\nClient-Header-Stage: preparing\n\n"+requestText;
        lastResponse="";

        EntityEnclosingMethod method=new EntityEnclosingMethod(url){
            @Override public String getName(){return "PATCH";}
            @Override protected void writeRequestHeaders(org.apache.commons.httpclient.HttpState state,
                    org.apache.commons.httpclient.HttpConnection connection)
                    throws java.io.IOException,org.apache.commons.httpclient.HttpException{
                // Header otomatis (termasuk Content-Length) sudah dibuat oleh HttpClient di tahap ini.
                lastRequest=patchRequestTrace(this,requestText,"ready");
                int contentTypeCount=0;
                for(org.apache.commons.httpclient.Header header:getRequestHeaders()){
                    if("Content-Type".equalsIgnoreCase(header.getName())){
                        contentTypeCount++;
                        if(!patchContentType.equals(header.getValue()))
                            throw new java.io.IOException("Content-Type PATCH berubah sebelum dikirim. Periksa Respon API.");
                    }
                }
                if(contentTypeCount!=1)throw new java.io.IOException("PATCH memerlukan tepat satu header Content-Type. Periksa Respon API.");
                super.writeRequestHeaders(state,connection);
                // Dicatat dari header client pada tahap penulisan; bukan bukti header di sisi gateway.
                lastRequest=patchRequestTrace(this,requestText,"written");
            }
        };
        method.setRequestHeader("Accept","application/fhir+json, application/json");
        // Body dan header menggunakan media type yang sama, tanpa tambahan charset otomatis.
        method.setRequestEntity(new ByteArrayRequestEntity(requestJson.getBytes("UTF-8"),patchContentType));
        method.setRequestHeader("Content-Type",patchContentType);
        lastRequest=patchRequestTrace(method,requestText,"prepared");
        boolean traced=false;
        try{
            method.setRequestHeader("Authorization","Bearer "+api.TokenSatuSehat());
            int status=new HttpClient().executeMethod(method);
            String text=method.getResponseBodyAsString();
            lastResponse=text==null?"":text;
            appendTrace();traced=true;
            if(status<200||status>=300)throw new java.io.IOException("HTTP "+status+" dari SATUSEHAT: "+lastResponse);
            return notEmpty(text)?mapper.readTree(text):mapper.createObjectNode();
        }catch(Exception ex){
            if(lastResponse==null||lastResponse.isEmpty())lastResponse=ex.getMessage()==null?ex.getClass().getSimpleName():ex.getMessage();
            if(!traced)appendTrace();
            throw ex;
        }finally{method.releaseConnection();}
    }
    private static String patchRequestTrace(EntityEnclosingMethod method,String requestText,String stage){
        StringBuilder trace=new StringBuilder("Client-Patch: PATCH-HEADERS-20260914-R2\n");
        trace.append("Client-Header-Stage: ").append(stage).append('\n');
        // Allowlist saja: Authorization, Cookie, dan kredensial proxy tidak pernah disalin.
        for(org.apache.commons.httpclient.Header header:method.getRequestHeaders()){
            String name=header.getName();
            if("Content-Type".equalsIgnoreCase(name)||"Content-Length".equalsIgnoreCase(name)
                    ||"Accept".equalsIgnoreCase(name)||"Transfer-Encoding".equalsIgnoreCase(name))
                trace.append(name).append(": ").append(header.getValue()).append('\n');
        }
        trace.append("Client-Entity-Content-Type: ").append(method.getRequestEntity().getContentType()).append('\n');
        return trace.append('\n').append(requestText).toString();
    }
    private JsonNode exchange(HttpMethod method,String path,JsonNode body,MediaType contentType)throws Exception{
        HttpHeaders h=new HttpHeaders();h.setContentType(contentType);h.add("Accept","application/fhir+json, application/json");h.add("Authorization","Bearer "+api.TokenSatuSehat());
        String requestText=body==null?"":mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
        HttpEntity<String> entity=new HttpEntity<String>(body==null?null:mapper.writeValueAsString(body),h);String url=baseUrl()+path;
        lastMethod=method.name();lastUrl=url;lastRequest=requestText;lastResponse="";
        try{
            ResponseEntity<String> response=api.getRest().exchange(url,method,entity,String.class);
            String text=response.getBody();lastResponse=text==null?"":text;appendTrace();
            JsonNode parsed=notEmpty(text)?mapper.readTree(text):mapper.createObjectNode();
            // Beberapa gateway SATUSEHAT dapat mengembalikan 201 dengan ID resource pada
            // header Location. Simpan ID tersebut sebagai fallback agar workflow tidak gagal
            // hanya karena body respons kosong/berbeda bentuk.
            if(parsed!=null&&parsed.isObject()&&!notEmpty(parsed.path("id").asText())){
                String location=response.getHeaders().getFirst("Location");
                String locationId=resourceIdFromLocation(location);
                if(notEmpty(locationId))((ObjectNode)parsed).put("id",locationId);
            }
            return parsed;
        }catch(Exception ex){
            // Request aman disimpan tanpa token. Ambil body error Spring melalui
            // refleksi agar service tetap kompatibel dengan versi spring-web Khanza.
            String detail=ex.getMessage()==null?ex.getClass().getSimpleName():ex.getMessage();
            try{
                Object bodyText=ex.getClass().getMethod("getResponseBodyAsString").invoke(ex);
                if(bodyText!=null&&notEmpty(String.valueOf(bodyText)))detail=String.valueOf(bodyText);
            }catch(Exception ignored){}
            lastResponse=detail;appendTrace();throw ex;
        }
    }
}
