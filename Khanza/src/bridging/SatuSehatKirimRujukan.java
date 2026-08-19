package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class SatuSehatKirimRujukan {
    public static final String RAWAT_JALAN = "RAWAT_JALAN";
    public static final String RAWAT_INAP = "RAWAT_INAP";
    public static final String IGD = "IGD";

    private static final String KEMKES = "http://terminology.kemkes.go.id";
    private static final String KEMKES_TERMINOLOGY = "http://terminology.kemkes.go.id";
    private static final String SNOMED = "http://snomed.info/sct";
    private static final String ICD10 = "http://hl7.org/fhir/sid/icd-10";
    private static final String ROLE_CODE = "http://terminology.hl7.org/CodeSystem/v3-RoleCode";
    private static final String TASK_STATUS = "http://hl7.org/fhir/task-status";
    private static final String APPOINTMENT_TYPE = "http://terminology.hl7.org/CodeSystem/v2-0276";

    private String link = "";
    private String json = "";
    private final ApiSatuSehat api = new ApiSatuSehat();
    private final ObjectMapper mapper = new ObjectMapper();
    private HttpHeaders headers;
    private HttpEntity<String> requestEntity;

    public SatuSehatKirimRujukan() {
        try {
            link = koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    public KirimResult kirimPraPermintaan(RujukanData data) throws Exception {
        return exchange("/Task", HttpMethod.POST, buildPraPermintaanTask(data));
    }

    public ObjectNode buildPraPermintaanTask(RujukanData data) {
        ObjectNode task = buildTaskBase(data, "requested", "referral-pre-request", "Referral pre request");
        ArrayNode input = mapper.createArrayNode();
        input.add(inputCoding(SNOMED, "119270007", "Management procedure",
                coding(SNOMED, managementCode(data.jenisRujukan), managementDisplay(data.jenisRujukan))));
        input.add(inputCoding("primary-diagnosis", "Primary Diagnosis",
                coding(ICD10, data.diagnosisUtamaCode, data.diagnosisUtamaDisplay)));
        task.set("input", input);
        return task;
    }

    public KirimResult kirimPencarianKandidat(RujukanData data, ArrayNode kriteriaItems, ArrayNode wilayahItems) throws Exception {
        return exchange("/Task", HttpMethod.POST, buildPencarianKandidatTask(data, kriteriaItems, wilayahItems));
    }

    public KirimResult ambilTask(String taskId) throws Exception {
        return exchange("/Task/" + stripReference(taskId, "Task/"), HttpMethod.GET, null);
    }

    public KirimResult cariTaskRujukanMasuk(String organizationId, String patientId, String status) throws Exception {
        StringBuilder path = new StringBuilder("/Task?owner=");
        path.append(encodeQueryValue("Organization/" + stripReference(organizationId, "Organization/")));
        if (isEmpty(patientId)) {
            throw new IllegalArgumentException("Patient ID wajib diisi untuk pencarian Task rujukan masuk SATUSEHAT.");
        }
        path.append("&subject=").append(encodeQueryValue("Patient/" + stripReference(patientId, "Patient/")));
        if (!isEmpty(status)) {
            path.append("&status=").append(encodeQueryValue(status));
        }
        path.append("&_count=100");
        return exchange(path.toString(), HttpMethod.GET, null);
    }

    public ObjectNode buildPencarianKandidatTask(RujukanData data, ArrayNode kriteriaItems, ArrayNode wilayahItems) {
        ObjectNode task = buildTaskBase(data, "requested", "request-referral-candidate", "Request for referral candidate");
        task.set("identifier", mapper.createArrayNode().add(identifier("task", taskIdentifier(data, candidateTaskSuffix(data)))));
        task.set("for", reference("Patient/" + stripReference(data.patientId, "Patient/"), data.patientName));
        if (!isEmpty(data.taskPraPermintaanId)) {
            task.set("basedOn", mapper.createArrayNode().add(reference("Task/" + stripReference(data.taskPraPermintaanId, "Task/"), "")));
        }

        ArrayNode contained = mapper.createArrayNode();
        contained.add(questionnaireResponse("123456789", data.patientId, data.patientName, data.encounterId, kriteriaItems));
        contained.add(questionnaireResponse("123456788", data.patientId, data.patientName, data.encounterId, wilayahItems));
        task.set("contained", contained);

        ArrayNode input = mapper.createArrayNode();
        input.add(inputReference("referral-criteria", "Referral Criteria", "#123456789", "Referral Criteria Response"));
        input.add(inputReference("area", "Area", "#123456788", "Jejaring Wilayah Rujukan"));
        input.add(inputCoding(SNOMED, "119270007", "Management procedure",
                coding(SNOMED, managementCode(data.jenisRujukan), managementDisplay(data.jenisRujukan))));
        input.add(inputCoding("primary-diagnosis", "Primary Diagnosis",
                coding(ICD10, data.diagnosisUtamaCode, data.diagnosisUtamaDisplay)));
        if (!isEmpty(data.diagnosisSekunderCode)) {
            input.add(inputCoding("secondary-diagnosis", "Secondary diagnosis",
                    coding(ICD10, data.diagnosisSekunderCode, data.diagnosisSekunderDisplay)));
        }
        task.set("input", input);
        return task;
    }

    public KirimResult kirimBundleRawatJalan(RujukanData data) throws Exception {
        ObjectNode serviceRequest = buildServiceRequest(data, "urn:uuid:" + safeUuid(data.carePlanUuid, "careplan"), data.taskResponKandidatId);
        ObjectNode carePlan = buildCarePlan(data);

        ObjectNode bundle = mapper.createObjectNode();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "transaction");
        ArrayNode entry = mapper.createArrayNode();
        entry.add(bundleEntry("urn:uuid:" + safeUuid(data.serviceRequestUuid, "servicerequest"), serviceRequest, "POST", "ServiceRequest"));
        entry.add(bundleEntry("urn:uuid:" + safeUuid(data.carePlanUuid, "careplan"), carePlan, "POST", "CarePlan"));
        bundle.set("entry", entry);

        return exchange("", HttpMethod.POST, bundle);
    }

    public KirimResult kirimBundleTugasRujukan(RujukanData data, List<FaskesKandidat> kandidatList,
            ArrayNode kriteriaItems, ArrayNode wilayahItems, boolean sertakanTaskKandidatCompleted) throws Exception {
        ObjectNode bundle = mapper.createObjectNode();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "transaction");
        bundle.set("meta", metaTag("referral-approval", "Referral approval"));

        ArrayNode entry = mapper.createArrayNode();
        int index = 1;
        for (FaskesKandidat kandidat : kandidatList) {
            ObjectNode task = buildTaskBase(data, "requested", "referral-approval-request", "Referral approval request");
            task.set("identifier", mapper.createArrayNode().add(identifier("task", identifierOrganizationId(data),
                    taskIdentifier(data, "RUJUKAN-" + index + "-" + stripReference(kandidat.organizationId, "Organization/")))));
            task.set("owner", reference("Organization/" + stripReference(kandidat.organizationId, "Organization/"), kandidat.organizationName));
            task.set("for", reference("Patient/" + stripReference(data.patientId, "Patient/"), data.patientName));
            task.set("input", mapper.createArrayNode().add(inputReference("referral-task", "Referral Task",
                    "Organization/" + stripReference(kandidat.organizationId, "Organization/"), kandidat.organizationName)));
            entry.add(bundleEntry("urn:uuid:" + safeUuid(kandidat.taskUuid, "task-rujukan-" + index), task, "POST", "Task"));
            index++;
        }

        if (isEmpty(data.carePlanId)) {
            entry.add(bundleEntry("urn:uuid:" + safeUuid(data.carePlanUuid, "careplan"), buildCarePlan(data), "POST", "CarePlan"));
        }

        if (sertakanTaskKandidatCompleted) {
            ObjectNode completed = buildTaskBase(data, "completed", "request-referral-candidate", "Request for referral candidate");
            completed.set("for", reference("Patient/" + stripReference(data.patientId, "Patient/"), data.patientName));
            if (!isEmpty(data.taskPencarianKandidatId)) {
                completed.set("basedOn", mapper.createArrayNode().add(reference("Task/" + stripReference(data.taskPencarianKandidatId, "Task/"), "")));
            }
            ArrayNode input = mapper.createArrayNode();
            input.add(inputReference("referral-criteria", "Referral Criteria", "#123456789", "Referral Criteria Response"));
            input.add(inputReference("area", "Area", "#123456788", "Jejaring Wilayah Rujukan"));
            input.add(inputCodeable(SNOMED, "119270007", "Management procedure",
                    coding(SNOMED, managementCode(data.jenisRujukan), managementDisplay(data.jenisRujukan))));
            input.add(inputCodeable("primary-diagnosis", "Primary Diagnosis",
                    coding(ICD10, data.diagnosisUtamaCode, data.diagnosisUtamaDisplay)));
            completed.set("input", input);
            ArrayNode output = mapper.createArrayNode();
            for (FaskesKandidat kandidat : kandidatList) {
                output.add(outputReference("candidate-referral-facility", "Candidate referral facility",
                        "Organization/" + stripReference(kandidat.organizationId, "Organization/"), kandidat.organizationName));
            }
            completed.set("output", output);
            completed.set("contained", mapper.createArrayNode()
                    .add(questionnaireResponse("123456789", data.patientId, data.patientName, data.encounterId, kriteriaItems))
                    .add(questionnaireResponse("123456788", data.patientId, data.patientName, data.encounterId, wilayahItems)));
            entry.add(bundleEntry("urn:uuid:" + safeUuid(data.taskPencarianKandidatUuid, "task-pencarian-completed"), completed, "POST", "Task"));
        }

        bundle.set("entry", entry);
        return exchange("", HttpMethod.POST, bundle);
    }

    public KirimResult kirimResponTaskPatch(String taskId, boolean diterima) throws Exception {
        ArrayNode body = mapper.createArrayNode();

        ObjectNode statusPatch = mapper.createObjectNode();
        statusPatch.put("op", "replace");
        statusPatch.put("path", "/status");
        statusPatch.put("value", "completed");
        body.add(statusPatch);

        ObjectNode outputPatch = mapper.createObjectNode();
        outputPatch.put("op", "add");
        outputPatch.put("path", "/output");
        outputPatch.set("value", mapper.createArrayNode().add(responseReferralTaskOutput(diterima)));
        body.add(outputPatch);
        return exchangePatch("/Task/" + stripReference(taskId, "Task/"), body);
    }

    public KirimResult kirimResponTaskPut(RujukanData data, String taskId, FaskesKandidat kandidat, boolean diterima) throws Exception {
        ObjectNode task = buildTaskBase(data, "requested", "referral-approval-request", "Referral approval request");
        task.put("id", stripReference(taskId, "Task/"));
        task.set("owner", reference("Organization/" + stripReference(kandidat.organizationId, "Organization/"), kandidat.organizationName));
        task.set("for", reference("Patient/" + stripReference(data.patientId, "Patient/"), data.patientName));
        task.set("input", mapper.createArrayNode().add(inputReference("referral-task", "Referral Task",
                "Organization/" + stripReference(kandidat.organizationId, "Organization/"), kandidat.organizationName)));
        task.put("status", "completed");
        task.set("output", mapper.createArrayNode().add(responseReferralTaskOutput(diterima)));
        return exchange("/Task/" + stripReference(taskId, "Task/"), HttpMethod.PUT, task);
    }

    public KirimResult kirimAppointment(RujukanData data, String taskApprovalId, String tanggalMulai, String tanggalSelesai) throws Exception {
        String scheduleUuid = UUID.randomUUID().toString();
        String slotUuid = UUID.randomUUID().toString();
        String appointmentUuid = UUID.randomUUID().toString();

        ObjectNode schedule = mapper.createObjectNode();
        schedule.put("resourceType", "Schedule");
        schedule.put("active", true);
        schedule.set("serviceType", mapper.createArrayNode().add(
                codeable(SNOMED, managementCode(data.jenisRujukan), managementDisplay(data.jenisRujukan))));
        ArrayNode scheduleActor = mapper.createArrayNode();
        String appointmentPractitionerId = !isEmpty(data.practitionerRujukanId) ? data.practitionerRujukanId : data.practitionerId;
        String appointmentPractitionerName = !isEmpty(data.practitionerRujukanName) ? data.practitionerRujukanName : data.practitionerName;
        scheduleActor.add(reference("Practitioner/" + stripReference(appointmentPractitionerId, "Practitioner/"), appointmentPractitionerName));
        schedule.set("actor", scheduleActor);

        ObjectNode slot = mapper.createObjectNode();
        slot.put("resourceType", "Slot");
        slot.set("serviceType", mapper.createArrayNode().add(
                codeable(SNOMED, managementCode(data.jenisRujukan), managementDisplay(data.jenisRujukan))));
        slot.set("schedule", reference("urn:uuid:" + scheduleUuid, ""));
        slot.put("status", "free");
        slot.put("start", tanggalMulai);
        if (!isEmpty(tanggalSelesai)) {
            slot.put("end", tanggalSelesai);
        }

        ObjectNode appointment = mapper.createObjectNode();
        appointment.put("resourceType", "Appointment");
        appointment.put("status", "booked");
        appointment.set("serviceType", mapper.createArrayNode().add(
                codeable(SNOMED, managementCode(data.jenisRujukan), managementDisplay(data.jenisRujukan))));
        appointment.set("appointmentType", codeable(APPOINTMENT_TYPE, "ROUTINE", "Routine appointment"));
        appointment.set("slot", mapper.createArrayNode().add(reference("urn:uuid:" + slotUuid, "")));
        appointment.put("description", "Jadwal kunjungan pasien untuk pemeriksaan lanjutan");
        appointment.put("start", tanggalMulai);
        if (!isEmpty(tanggalSelesai)) {
            appointment.put("end", tanggalSelesai);
        }
        if (!isEmpty(taskApprovalId)) {
            appointment.set("supportingInformation", mapper.createArrayNode().add(
                    reference("Task/" + stripReference(taskApprovalId, "Task/"), "")));
        }
        ArrayNode participant = mapper.createArrayNode();
        participant.add(appointmentParticipant("Patient/" + stripReference(data.patientId, "Patient/"), data.patientName, "accepted"));
        participant.add(appointmentParticipant("Practitioner/" + stripReference(appointmentPractitionerId, "Practitioner/"), appointmentPractitionerName, "accepted"));
        appointment.set("participant", participant);

        ObjectNode bundle = mapper.createObjectNode();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "transaction");
        ArrayNode entry = mapper.createArrayNode();
        entry.add(bundleEntry("urn:uuid:" + scheduleUuid, schedule, "POST", "Schedule"));
        entry.add(bundleEntry("urn:uuid:" + slotUuid, slot, "POST", "Slot"));
        entry.add(bundleEntry("urn:uuid:" + appointmentUuid, appointment, "POST", "Appointment"));
        bundle.set("entry", entry);
        return exchange("", HttpMethod.POST, bundle);
    }

    public KirimResult kirimServiceRequest(RujukanData data, String carePlanId, String taskRujukanId) throws Exception {
        return exchange("/ServiceRequest", HttpMethod.POST, buildServiceRequest(data, carePlanId, taskRujukanId));
    }

    public KirimResult ambilServiceRequest(String serviceRequestId) throws Exception {
        return exchange("/ServiceRequest/" + stripReference(serviceRequestId, "ServiceRequest/"), HttpMethod.GET, null);
    }

    public KirimResult ambilResource(String reference) throws Exception {
        String value = safe(reference).trim();
        if (value.equals("")) {
            throw new IllegalArgumentException("Reference resource kosong.");
        }
        if (value.startsWith(link)) {
            value = value.substring(link.length());
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            int marker = value.indexOf("/fhir-r4/v1/");
            if (marker >= 0) {
                value = value.substring(marker + "/fhir-r4/v1".length());
            } else {
                throw new IllegalArgumentException("Reference resource di luar endpoint SATUSEHAT: " + reference);
            }
        }
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        return exchange(value, HttpMethod.GET, null);
    }

    public KirimResult cariServiceRequestRujukanMasuk(String organizationId, String status) throws Exception {
        StringBuilder path = new StringBuilder("/ServiceRequest?performer=");
        path.append("Organization/").append(encodeQueryValue(stripReference(organizationId, "Organization/")));
        if (!isEmpty(status)) {
            path.append("&status=").append(encodeQueryValue(status));
        }
        path.append("&_count=100");
        return exchange(path.toString(), HttpMethod.GET, null);
    }

    public ObjectNode buildCarePlan(RujukanData data) {
        ObjectNode carePlan = mapper.createObjectNode();
        carePlan.put("resourceType", "CarePlan");
        if (!isEmpty(data.carePlanId)) {
            carePlan.put("id", stripReference(data.carePlanId, "CarePlan/"));
        }
        String contributorId = !isEmpty(data.orgPerujukFaskesUtamaId) ? data.orgPerujukFaskesUtamaId : data.orgPerujukId;
        String contributorName = !isEmpty(data.orgPerujukFaskesUtamaName) ? data.orgPerujukFaskesUtamaName : data.orgPerujukName;
        ArrayNode identifiers = mapper.createArrayNode().add(identifier("careplan", identifierOrganizationId(data), data.nomorRujukan));
        if (!isEmpty(contributorId)) {
            identifiers.add(identifierWithSystem("http://sys-ids.kemkes.go.id/careplan/authoring-organization",
                    stripReference(contributorId, "Organization/")));
        }
        carePlan.set("identifier", identifiers);
        carePlan.put("status", "active");
        carePlan.put("intent", "plan");
        carePlan.set("category", mapper.createArrayNode()
                .add(codeable(IGD.equals(data.jenisRujukan) ? KEMKES : SNOMED, carePlanCode(data.jenisRujukan), carePlanDisplay(data.jenisRujukan)))
                .add(codeable(SNOMED, "3457005", "Patient referral")));
        carePlan.put("title", "Rencana Rujukan Pasien");
        if (!isEmpty(data.keterangan)) {
            carePlan.put("description", data.keterangan);
        }
        carePlan.set("subject", reference("Patient/" + stripReference(data.patientId, "Patient/"), data.patientName));
        carePlan.set("encounter", reference("Encounter/" + stripReference(data.encounterId, "Encounter/"), ""));
        carePlan.put("created", data.authoredOn);
        carePlan.set("author", reference("Practitioner/" + stripReference(data.practitionerId, "Practitioner/"), data.practitionerName));
        if (!isEmpty(contributorId)) {
            carePlan.set("contributor", mapper.createArrayNode().add(
                    reference("Organization/" + stripReference(contributorId, "Organization/"), contributorName)));
        }
        ArrayNode addresses = mapper.createArrayNode();
        for (String conditionId : data.conditionIds) {
            addresses.add(reference("Condition/" + stripReference(conditionId, "Condition/"), ""));
        }
        if (addresses.size() > 0) {
            carePlan.set("addresses", addresses);
        }
        ArrayNode supportingInfo = mapper.createArrayNode();
        for (String supportingReference : data.supportingInfoReferences) {
            String reference = safe(supportingReference).trim();
            if (!isEmpty(reference) && !reference.startsWith("Condition/")
                    && !reference.startsWith("Task/")
                    && !isSameReference(reference, data.serviceRequestId, "ServiceRequest/")) {
                supportingInfo.add(reference(reference, ""));
            }
        }
        if (supportingInfo.size() > 0) {
            carePlan.set("supportingInfo", supportingInfo);
        }
        if (!isEmpty(data.hasilRadiologi)) {
            carePlan.set("note", mapper.createArrayNode().add(annotation("Hasil radiologi:\n" + data.hasilRadiologi)));
        }
        return carePlan;
    }

    public ObjectNode buildServiceRequest(RujukanData data, String carePlanId, String taskRujukanId) {
        ObjectNode serviceRequest = mapper.createObjectNode();
        serviceRequest.put("resourceType", "ServiceRequest");
        if (!isEmpty(data.serviceRequestId)) {
            serviceRequest.put("id", stripReference(data.serviceRequestId, "ServiceRequest/"));
        }
        serviceRequest.set("identifier", mapper.createArrayNode().add(identifier("servicerequest", identifierOrganizationId(data), data.nomorRujukan)));
        serviceRequest.put("status", "active");
        serviceRequest.put("intent", "original-order");
        serviceRequest.put("priority", "stat");
        serviceRequest.set("category", mapper.createArrayNode().add(codeable(SNOMED, "3457005", "Patient referral")));
        serviceRequest.set("code", codeable(SNOMED, managementCode(data.jenisRujukan), managementDisplay(data.jenisRujukan), data.keterangan));
        serviceRequest.set("subject", reference("Patient/" + stripReference(data.patientId, "Patient/"), data.patientName));
        serviceRequest.set("encounter", reference("Encounter/" + stripReference(data.encounterId, "Encounter/"), ""));
        String requesterId = !isEmpty(data.orgPerujukFaskesUtamaId) ? data.orgPerujukFaskesUtamaId : data.orgPerujukId;
        String requesterName = !isEmpty(data.orgPerujukFaskesUtamaName) ? data.orgPerujukFaskesUtamaName : data.orgPerujukName;
        serviceRequest.set("requester", reference("Organization/" + stripReference(requesterId, "Organization/"), requesterName));
        if (!isEmpty(data.orgRujukanId)) {
            serviceRequest.set("performer", mapper.createArrayNode().add(
                    reference("Organization/" + stripReference(data.orgRujukanId, "Organization/"), data.orgRujukanName)));
        }
        if (!isEmpty(data.performerTypeCode)) {
            serviceRequest.set("performerType", codeable(
                    isEmpty(data.performerTypeSystem) ? "http://terminology.kemkes.go.id/CodeSystem/practitioner-speciality" : data.performerTypeSystem,
                    data.performerTypeCode, data.performerTypeDisplay));
        }
        if (!isEmpty(carePlanId)) {
            serviceRequest.set("basedOn", mapper.createArrayNode().add(reference(resourceReference(carePlanId, "CarePlan/"), "")));
        }
        ArrayNode reasonReferences = mapper.createArrayNode();
        for (String conditionId : data.conditionIds) {
            reasonReferences.add(reference("Condition/" + stripReference(conditionId, "Condition/"), ""));
        }
        if (reasonReferences.size() > 0) {
            serviceRequest.set("reasonReference", reasonReferences);
        }
        serviceRequest.set("locationCode", mapper.createArrayNode().add(codeable(ROLE_CODE, "HOSP", "Hospital")));
        if (!isEmpty(data.authoredOn)) {
            serviceRequest.put("authoredOn", data.authoredOn);
        }
        if (!isEmpty(data.occurrenceDateTime)) {
            serviceRequest.put("occurrenceDateTime", data.occurrenceDateTime);
        }
        ArrayNode supportingInfo = mapper.createArrayNode();
        Set<String> supportingReferences = new LinkedHashSet<String>();
        if (!isEmpty(taskRujukanId)) {
            supportingReferences.add("Task/" + stripReference(taskRujukanId, "Task/"));
        }
        for (String supportingReference : data.supportingInfoReferences) {
            String reference = safe(supportingReference).trim();
            if (isEmpty(reference) || isSameTask(reference, data.taskPencarianKandidatId)
                    || reference.startsWith("Task/")
                    || isSameReference(reference, data.serviceRequestId, "ServiceRequest/")) {
                continue;
            }
            supportingReferences.add(reference);
        }
        for (String supportingReference : supportingReferences) {
            supportingInfo.add(reference(supportingReference, ""));
        }
        if (supportingInfo.size() > 0) {
            serviceRequest.set("supportingInfo", supportingInfo);
        }
        if (!isEmpty(data.patientInstruction)) {
            serviceRequest.put("patientInstruction", data.patientInstruction);
        }
        if (!isEmpty(data.hasilRadiologi)) {
            serviceRequest.set("note", mapper.createArrayNode().add(annotation("Hasil radiologi:\n" + data.hasilRadiologi)));
        }
        return serviceRequest;
    }

    private ObjectNode buildTaskBase(RujukanData data, String status, String code, String display) {
        ObjectNode task = mapper.createObjectNode();
        task.put("resourceType", "Task");
        task.set("identifier", mapper.createArrayNode().add(identifier("task", identifierOrganizationId(data), data.nomorRujukan)));
        task.put("status", status);
        task.put("intent", "instance-order");
        task.put("priority", "routine");
        if (!isEmpty(data.authoredOn)) {
            task.put("authoredOn", data.authoredOn);
            task.put("lastModified", data.authoredOn);
        }
        task.set("code", codeable(KEMKES_TERMINOLOGY, code, display));
        if ("referral-approval-request".equals(code) && !isEmpty(data.authoredOn)) {
            ObjectNode executionPeriod = mapper.createObjectNode();
            executionPeriod.put("start", data.authoredOn);
            task.set("executionPeriod", executionPeriod);
        }
        if (!isEmpty(data.encounterId)) {
            task.set("encounter", reference("Encounter/" + stripReference(data.encounterId, "Encounter/"), ""));
        }
        if ("referral-approval-request".equals(code) && !data.conditionIds.isEmpty()) {
            task.set("reasonReference", reference("Condition/" + stripReference(data.conditionIds.get(0), "Condition/"), ""));
        }
        String orgPerujukId = data.orgPerujukId;
        String orgPerujukName = data.orgPerujukName;
        if (("request-referral-candidate".equals(code) || "referral-approval".equals(code)
                || "referral-approval-request".equals(code)) && !isEmpty(data.orgPerujukFaskesUtamaId)) {
            orgPerujukId = data.orgPerujukFaskesUtamaId;
            orgPerujukName = data.orgPerujukFaskesUtamaName;
        }
        task.set("requester", reference("Organization/" + stripReference(orgPerujukId, "Organization/"), orgPerujukName));
        task.set("owner", reference("Organization/" + stripReference(orgPerujukId, "Organization/"), orgPerujukName));
        return task;
    }

    private ObjectNode questionnaireResponse(String id, String patientId, String patientName, String encounterId, ArrayNode items) {
        ObjectNode qr = mapper.createObjectNode();
        qr.put("resourceType", "QuestionnaireResponse");
        qr.put("id", id);
        qr.put("questionnaire", "123456789".equals(id) ? "https://fhir.kemkes.go.id/Questionnaire/Q100" : "https://fhir.kemkes.go.id/Questionnaire/Q101");
        qr.put("status", "completed");
        qr.set("subject", reference("Patient/" + stripReference(patientId, "Patient/"), patientName));
        qr.set("encounter", reference("Encounter/" + stripReference(encounterId, "Encounter/"), ""));
        qr.set("item", items == null ? mapper.createArrayNode() : items);
        return qr;
    }

    private KirimResult exchange(String path, HttpMethod method, JsonNode body) throws Exception {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
        String url = link + path;
        if (body == null) {
            requestEntity = new HttpEntity<String>(headers);
            System.out.println("URL : " + url);
            json = api.getRest().exchange(url, method, requestEntity, String.class).getBody();
        } else {
            String requestJson = mapper.writeValueAsString(body);
            requestEntity = new HttpEntity<String>(requestJson, headers);
            System.out.println("URL : " + url);
            System.out.println("Request JSON : " + requestJson);
            json = api.getRest().exchange(url, method, requestEntity, String.class).getBody();
        }
        JsonNode root = mapper.readTree(json);
        System.out.println("JSON : " + json);
        return new KirimResult(root.path("id").asText(), json, root);
    }

    private KirimResult exchangePatch(String path, JsonNode body) throws Exception {
        String url = link + path;
        String requestJson = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json-patch+json")
                .header("Authorization", "Bearer " + api.TokenSatuSehat())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();
        System.out.println("URL : " + url);
        System.out.println("Request JSON : " + requestJson);
        HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        json = response.body();
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode() + " dari SATUSEHAT: " + json);
        }
        JsonNode root = mapper.readTree(json);
        System.out.println("JSON : " + json);
        return new KirimResult(root.path("id").asText(), json, root);
    }

    private ObjectNode bundleEntry(String fullUrl, ObjectNode resource, String method, String url) {
        ObjectNode entry = mapper.createObjectNode();
        entry.put("fullUrl", fullUrl);
        entry.set("resource", resource);
        ObjectNode request = mapper.createObjectNode();
        request.put("method", method);
        request.put("url", url);
        entry.set("request", request);
        return entry;
    }

    private ObjectNode metaTag(String code, String display) {
        ObjectNode meta = mapper.createObjectNode();
        meta.set("tag", mapper.createArrayNode().add(coding(KEMKES_TERMINOLOGY, code, display)));
        return meta;
    }

    private ObjectNode inputReference(String systemSuffix, String display, String reference, String referenceDisplay) {
        ObjectNode input = mapper.createObjectNode();
        input.set("type", codeable(KEMKES_TERMINOLOGY, systemSuffix, display));
        input.set("valueReference", reference(reference, referenceDisplay));
        return input;
    }

    private ObjectNode inputCoding(String typeCode, String typeDisplay, ObjectNode coding) {
        ObjectNode input = mapper.createObjectNode();
        input.set("type", codeable(KEMKES_TERMINOLOGY, typeCode, typeDisplay));
        input.set("valueCoding", coding);
        return input;
    }

    private ObjectNode inputCoding(String typeSystem, String typeCode, String typeDisplay, ObjectNode coding) {
        ObjectNode input = mapper.createObjectNode();
        input.set("type", codeable(typeSystem, typeCode, typeDisplay));
        input.set("valueCoding", coding);
        return input;
    }

    private ObjectNode outputReference(String systemSuffix, String display, String reference, String referenceDisplay) {
        ObjectNode output = mapper.createObjectNode();
        output.set("type", codeable(KEMKES_TERMINOLOGY, systemSuffix, display));
        output.set("valueReference", reference(reference, referenceDisplay));
        return output;
    }

    private ObjectNode inputCodeable(String systemSuffix, String display, ObjectNode coding) {
        ObjectNode input = mapper.createObjectNode();
        input.set("type", codeable(systemSuffix.startsWith("http") ? systemSuffix : KEMKES_TERMINOLOGY, systemSuffix, display));
        ObjectNode value = mapper.createObjectNode();
        value.set("coding", mapper.createArrayNode().add(coding));
        input.set("valueCodeableConcept", value);
        return input;
    }

    private ObjectNode inputCodeable(String typeSystem, String typeCode, String typeDisplay, ObjectNode coding) {
        ObjectNode input = mapper.createObjectNode();
        input.set("type", codeable(typeSystem, typeCode, typeDisplay));
        ObjectNode value = mapper.createObjectNode();
        value.set("coding", mapper.createArrayNode().add(coding));
        input.set("valueCodeableConcept", value);
        return input;
    }

    private ObjectNode outputCodeable(String systemSuffix, String display, ObjectNode coding) {
        ObjectNode output = mapper.createObjectNode();
        output.set("type", codeable(KEMKES_TERMINOLOGY, systemSuffix, display));
        ObjectNode value = mapper.createObjectNode();
        value.set("coding", mapper.createArrayNode().add(coding));
        output.set("valueCodeableConcept", value);
        return output;
    }

    private ObjectNode responseReferralTaskOutput(boolean diterima) {
        String code = diterima ? "accepted" : "rejected";
        String display = diterima ? "Accepted" : "Rejected";
        ObjectNode output = mapper.createObjectNode();
        output.set("type", codeable(KEMKES_TERMINOLOGY, "response-referral-task",
                "Response referral task", "Respon atas Task Rujukan"));
        output.set("valueCoding", coding(TASK_STATUS, code, display));
        return output;
    }

    private String taskIdentifier(RujukanData data, String suffix) {
        String base = safe(data.nomorRujukan).trim();
        if (base.equals("")) {
            base = String.valueOf(System.currentTimeMillis());
        }
        return suffix == null || suffix.trim().equals("") ? base : base + "-" + suffix;
    }

    private String candidateTaskSuffix(RujukanData data) {
        if (!isEmpty(data.taskPencarianKandidatUuid)) {
            return data.taskPencarianKandidatUuid;
        }
        return "KANDIDAT-" + System.currentTimeMillis();
    }

    private ObjectNode appointmentParticipant(String reference, String display, String status) {
        ObjectNode participant = mapper.createObjectNode();
        participant.set("actor", reference(reference, display));
        participant.put("status", status);
        return participant;
    }

    private ObjectNode identifier(String resourceName, String value) {
        return identifier(resourceName, safe(koneksiDB.IDSATUSEHAT()), value);
    }

    private ObjectNode identifier(String resourceName, String organizationId, String value) {
        ObjectNode identifier = mapper.createObjectNode();
        identifier.put("system", "http://sys-ids.kemkes.go.id/" + resourceName + "/" + stripReference(organizationId, "Organization/"));
        identifier.put("value", safe(value));
        return identifier;
    }

    private String identifierOrganizationId(RujukanData data) {
        if (!isEmpty(data.orgPerujukFaskesUtamaId)) {
            return data.orgPerujukFaskesUtamaId;
        }
        if (!isEmpty(data.orgPerujukId)) {
            return data.orgPerujukId;
        }
        return koneksiDB.IDSATUSEHAT();
    }

    private ObjectNode identifierWithSystem(String system, String value) {
        ObjectNode identifier = mapper.createObjectNode();
        identifier.put("system", safe(system));
        identifier.put("value", safe(value));
        return identifier;
    }

    private ObjectNode codeable(String system, String code, String display) {
        return codeable(system, code, display, "");
    }

    private ObjectNode codeable(String system, String code, String display, String text) {
        ObjectNode codeable = mapper.createObjectNode();
        codeable.set("coding", mapper.createArrayNode().add(coding(system, code, display)));
        if (!isEmpty(text)) {
            codeable.put("text", text);
        }
        return codeable;
    }

    private ObjectNode coding(String system, String code, String display) {
        ObjectNode coding = mapper.createObjectNode();
        coding.put("system", safe(system));
        coding.put("code", safe(code));
        if (!isEmpty(display)) {
            coding.put("display", display);
        }
        return coding;
    }

    private ObjectNode reference(String reference, String display) {
        ObjectNode ref = mapper.createObjectNode();
        ref.put("reference", safe(reference));
        if (!isEmpty(display)) {
            ref.put("display", display);
        }
        return ref;
    }

    private ObjectNode annotation(String text) {
        ObjectNode annotation = mapper.createObjectNode();
        annotation.put("text", safe(text));
        return annotation;
    }

    private String managementCode(String jenisRujukan) {
        if (RAWAT_INAP.equals(jenisRujukan)) {
            return "737481003";
        }
        if (IGD.equals(jenisRujukan)) {
            return "385868005";
        }
        return "737492002";
    }

    private String managementDisplay(String jenisRujukan) {
        if (RAWAT_INAP.equals(jenisRujukan)) {
            return "Inpatient care management";
        }
        if (IGD.equals(jenisRujukan)) {
            return "Emergency treatment management";
        }
        return "Outpatient care management";
    }

    private String carePlanCode(String jenisRujukan) {
        if (RAWAT_INAP.equals(jenisRujukan)) {
            return "736353004";
        }
        if (IGD.equals(jenisRujukan)) {
            return "TK000068";
        }
        return "736271009";
    }

    private String carePlanDisplay(String jenisRujukan) {
        if (RAWAT_INAP.equals(jenisRujukan)) {
            return "Inpatient care plan";
        }
        if (IGD.equals(jenisRujukan)) {
            return "Emergency care plan";
        }
        return "Outpatient care plan";
    }

    private String stripReference(String value, String prefix) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.startsWith(prefix) ? trimmed.substring(prefix.length()) : trimmed;
    }

    private String resourceReference(String value, String prefix) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith(prefix) || trimmed.startsWith("urn:")) {
            return trimmed;
        }
        return prefix + trimmed;
    }

    private String encodeQueryValue(String value) throws Exception {
        return URLEncoder.encode(safe(value), "UTF-8").replace("+", "%20");
    }

    private boolean isSameTask(String reference, String taskId) {
        return !isEmpty(taskId) && stripReference(reference, "Task/").equals(stripReference(taskId, "Task/"));
    }

    private boolean isSameReference(String reference, String resourceId, String prefix) {
        return !isEmpty(resourceId) && stripReference(reference, prefix).equals(stripReference(resourceId, prefix));
    }

    private String safeUuid(String value, String fallback) {
        return isEmpty(value) ? fallback : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().equals("");
    }

    public static class RujukanData {
        public String jenisRujukan = RAWAT_JALAN;
        public String nomorRujukan = "";
        public String patientId = "";
        public String patientName = "";
        public String practitionerId = "";
        public String practitionerName = "";
        public String practitionerRujukanId = "";
        public String practitionerRujukanName = "";
        public String orgPerujukId = "";
        public String orgPerujukName = "";
        public String orgPerujukFaskesUtamaId = "";
        public String orgPerujukFaskesUtamaName = "";
        public String orgRujukanId = "";
        public String orgRujukanName = "";
        public String encounterId = "";
        public String diagnosisUtamaCode = "";
        public String diagnosisUtamaDisplay = "";
        public String diagnosisSekunderCode = "";
        public String diagnosisSekunderDisplay = "";
        public String authoredOn = "";
        public String occurrenceDateTime = "";
        public String patientInstruction = "";
        public String keterangan = "";
        public String hasilRadiologi = "";
        public String performerTypeSystem = "";
        public String performerTypeCode = "";
        public String performerTypeDisplay = "";
        public String taskPraPermintaanId = "";
        public String taskPencarianKandidatId = "";
        public String taskPencarianKandidatUuid = "";
        public String taskResponKandidatId = "";
        public String carePlanId = "";
        public String carePlanUuid = "";
        public String serviceRequestId = "";
        public String serviceRequestUuid = "";
        public final List<String> supportingInfoReferences = new ArrayList<String>();
        public final List<String> conditionIds = new ArrayList<String>();
    }

    public static class FaskesKandidat {
        public String organizationId = "";
        public String organizationName = "";
        public String taskUuid = "";

        public FaskesKandidat() {
        }

        public FaskesKandidat(String organizationId, String organizationName) {
            this.organizationId = organizationId;
            this.organizationName = organizationName;
        }
    }

    public static class KirimResult {
        public final String id;
        public final String rawJson;
        public final JsonNode root;

        public KirimResult(String id, String rawJson, JsonNode root) {
            this.id = id;
            this.rawJson = rawJson;
            this.root = root;
        }
    }
}
