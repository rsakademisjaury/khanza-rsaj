package bridging;

import java.util.Locale;

/**
 * Mapping diagnosis ICD-10 -> Kelompok Layanan Rujukan Kompetensi +
 * Clinical Speciality SATUSEHAT.
 *
 * Sumber utama:
 * - 24 workbook Kode ICD-10 & ICD-9 Rujukan Kompetensi revisi 20-06-2026.
 * - Lampiran Terminologi Poli Tujuan, sheet "Poli Tujuan - Update".
 * - Collection koordinasi SATUSEHAT Rujukan untuk kode TK000563 dan LY133.
 *
 * Prinsip:
 * - IGD/RANAP dan status kegawatdaruratan tidak ditentukan oleh diagnosis.
 * - Mapping kelompok layanan memakai keanggotaan ICD-10 dari file kompetensi.
 * - Bila satu ICD-10 masuk lebih dari satu kelompok, dipakai tie-breaker domain
 *   diagnosis agar satu nilai dapat ditampilkan di form; informasi ambigu tetap
 *   dicatat di Result.rule.
 * - Kode TK kelompok layanan tidak ditebak. Hanya kode yang sudah tervalidasi
 *   pada material yang tersedia yang ditandai verified.
 * - Clinical speciality memakai kode LY dari sheet Update, bukan kode lama Sxxx.xx.
 */
public final class SatuSehatRujukanDiagnosisMapping {
    public static final String SERVICE_GROUP_SYSTEM = "http://terminology.kemkes.go.id";
    public static final String CLINICAL_SPECIALITY_SYSTEM = "http://terminology.kemkes.go.id/CodeSystem/clinical-speciality";

    private SatuSehatRujukanDiagnosisMapping() {}

    public static final class Result {
        public String serviceGroupCode = "";
        public String serviceGroupDisplay = "";
        public String clinicalSpecialityCode = "";
        public String clinicalSpecialityDisplay = "";
        public boolean serviceCodeVerified;
        public boolean clinicalCodeVerified;
        public String rule = "";
        public int matchedGroupCount;
        public String matchedGroups = "";

        public boolean hasSuggestion() {
            return !safe(serviceGroupDisplay).isEmpty() || !safe(clinicalSpecialityDisplay).isEmpty();
        }
        public boolean fullyCoded() {
            return serviceCodeVerified && clinicalCodeVerified
                    && !safe(serviceGroupCode).isEmpty() && !safe(clinicalSpecialityCode).isEmpty();
        }
    }

    public static Result map(String icd10, String diagnosisName) {
        String code = normalizeCode(icd10);
        String name = safe(diagnosisName).toLowerCase(Locale.ENGLISH);
        Result r = new Result();
        if (code.isEmpty()) return r;

        // 1) Prioritas utama: exact membership dari 24 file Rujukan Kompetensi.
        int[] exact = SatuSehatRujukanKompetensiData.groupsFor(code);
        if (exact.length > 0) {
            int preferred = preferredGroup(code, name);
            int selected = SatuSehatRujukanKompetensiData.contains(exact, preferred) ? preferred : exact[0];
            r.matchedGroupCount = exact.length;
            r.matchedGroups = groupList(exact);
            fillServiceGroup(r, selected);
            fillClinicalSpeciality(r, selected, code, name);
            r.rule = "ICD-10 exact pada Rujukan Kompetensi revisi 20-06-2026"
                    + (exact.length > 1 ? "; cocok ke " + exact.length + " kelompok (" + r.matchedGroups
                    + "), dipilih " + SatuSehatRujukanKompetensiData.display(selected) + " berdasarkan domain diagnosis" : "");
            return r;
        }

        // 2) Parent-code / fallback: tetap konservatif. Dipakai bila kode parent
        //    (mis. I60) tidak tercantum sebagai baris exact pada workbook.
        int preferred = preferredGroup(code, name);
        if (preferred > 0) {
            fillServiceGroup(r, preferred);
            fillClinicalSpeciality(r, preferred, code, name);
            // Karena bukan exact row pada file kompetensi, kode TK hanya dianggap
            // verified bila memang sudah ada bukti eksplisit yang kita pegang.
            r.rule = "Fallback domain ICD-10; kode exact tidak ditemukan pada daftar Rujukan Kompetensi";
        }
        return r;
    }

    private static void fillServiceGroup(Result r, int group) {
        r.serviceGroupDisplay = SatuSehatRujukanKompetensiData.display(group);
        String code = SatuSehatRujukanKompetensiData.serviceCode(group);
        if (!safe(code).isEmpty()) {
            r.serviceGroupCode = code;
            r.serviceCodeVerified = true;
        }
    }

    /**
     * Clinical speciality yang dipakai di sini adalah kode yang benar-benar ada
     * pada sheet "Poli Tujuan - Update". Untuk kelompok yang tidak mempunyai
     * satu speciality generik yang aman, kode sengaja dibiarkan kosong.
     */
    private static void fillClinicalSpeciality(Result r, int group, String code, String name) {
        switch (group) {
            case 1:
                clinical(r, "LY026", "Spesialis - Jantung dan Pembuluh Darah");
                break;
            case 2:
                clinical(r, "LY020", "Spesialis - Paru");
                break;
            case 3:
                // Uronefro memuat penyakit ginjal dan urologi. Bedakan secara
                // konservatif berdasarkan blok ICD-10 N00-N29 vs N30-N99.
                if (code.startsWith("N") && numberPart(code) >= 0 && numberPart(code) <= 29)
                    clinical(r, "LY246", "Penyakit Dalam - Ginjal dan Hipertensi");
                else
                    clinical(r, "LY028", "Spesialis - Urologi");
                break;
            case 4:
                clinical(r, "LY317", "Anak - Neonatologi");
                break;
            case 5:
                // Neoplasma dapat menuju onkologi bedah/medik/radiasi/organ spesifik.
                r.clinicalSpecialityDisplay = "Onkologi / layanan terkait lokasi neoplasma";
                break;
            case 6:
                clinical(r, "LY013", "Spesialis - Obstetri dan ginekologi");
                break;
            case 7:
                clinical(r, "LY023", "Spesialis - Orthopedi");
                break;
            case 8:
                clinical(r, "LY024", "Spesialis - Telinga Hidung Tenggorok Kepala Leher");
                break;
            case 9:
                clinical(r, "LY019", "Spesialis - Mata");
                break;
            case 10:
                clinical(r, "LY021", "Spesialis - Kulit dan Kelamin");
                break;
            case 11:
                if (isCerebrovascular(code, name))
                    clinical(r, "LY133", "Syaraf - Stroke dan Cerebro Vaskuler");
                else
                    clinical(r, "LY025", "Spesialis - Saraf");
                break;
            case 12:
                // Infeksi/parasit sangat lintas spesialis; jangan paksa LY tertentu.
                r.clinicalSpecialityDisplay = "Infeksi dan Penyakit Tropis / spesialis sesuai kasus";
                break;
            case 13:
                clinical(r, "LY102", "Penyakit Dalam - Gastroenterologi Hepatologi");
                break;
            case 14:
                clinical(r, "LY247", "Penyakit Dalam - Hemato-Onkologi");
                break;
            case 15:
                if (name.contains("rheumat") || name.contains("reumat"))
                    clinical(r, "LY252", "Penyakit Dalam - Reumatologi");
                else if (name.contains("alerg") || name.contains("imun"))
                    clinical(r, "LY243", "Penyakit Dalam - Alergi Imunologi");
                else
                    r.clinicalSpecialityDisplay = "Alergi Imunologi / Reumatologi";
                break;
            case 16:
                clinical(r, "LY096", "Spesialis - Bedah Plastik Rekonstruksi dan Estetika");
                break;
            case 17:
                r.clinicalSpecialityDisplay = "Toksikologi / spesialis sesuai kondisi klinis";
                break;
            case 18:
                clinical(r, "LY249", "Penyakit Dalam - Metabolik Endokrin");
                break;
            case 19:
                clinical(r, "LY341", "Bedah Plastik - Bedah plastik rekonstruksi dan estetika luka bakar dan luka");
                break;
            case 20:
                r.clinicalSpecialityDisplay = "Trauma / Bedah / Orthopedi sesuai lokasi cedera";
                break;
            case 21:
                clinical(r, "LY022", "Spesialis - Kedokteran Jiwa / Psikiatri/ Psikogeriatri/ NAPZA");
                break;
            case 22:
                clinical(r, "LY002", "Umum - Pelayanan medik gigi mulut");
                break;
            case 23:
                clinical(r, "LY027", "Spesialis - Kedokteran Forensik");
                break;
            case 24:
                clinical(r, "LY018", "Spesialis Penunjang - Rehabilitasi Medik");
                break;
            default:
                break;
        }
    }

    private static void clinical(Result r, String code, String display) {
        r.clinicalSpecialityCode = code;
        r.clinicalSpecialityDisplay = display;
        r.clinicalCodeVerified = true;
    }

    /**
     * Tie-breaker hanya digunakan saat satu ICD tercantum pada >1 workbook.
     * Keanggotaan exact dari workbook tetap menjadi syarat: preferred group tidak
     * pernah dipilih bila tidak ada pada daftar exact tersebut.
     */
    private static int preferredGroup(String code, String name) {
        if (isCerebrovascular(code, name)) return 11;
        if (isBurn(code, name)) return 19;
        if (isPoisoning(code, name)) return 17;
        if (name.contains("rehabilit") || name.contains("disabil")) return 24;
        if (name.contains("forensik") || name.contains("medicolegal") || name.contains("medikolegal")) return 23;
        if (name.contains("alerg") || name.contains("autoimun") || name.contains("rheumat") || name.contains("reumat")) return 15;
        if (name.contains("rekonstr") || name.contains("plastic") || name.contains("plastik") || name.contains("estetik")) return 16;

        char first = code.charAt(0);
        if (first == 'I') return 1;
        if (first == 'J') return 2;
        if (first == 'N') return 3;
        if (first == 'P') return 4;
        if (first == 'C' || (first == 'D' && numberPart(code) >= 0 && numberPart(code) <= 49)) return 5;
        if (first == 'O') return 6;
        if (first == 'M') return 7;
        if (first == 'H' && numberPart(code) >= 60) return 8;
        if (first == 'H') return 9;
        if (first == 'L') return 10;
        if (first == 'G') return 11;
        if (first == 'A' || first == 'B') return 12;
        if (first == 'K' && numberPart(code) >= 15) return 13;
        if (first == 'D') return 14;
        if (first == 'E') return 18;
        if (first == 'S' || first == 'T') return 20;
        if (first == 'F') return 21;
        if (first == 'K') return 22;
        return 0;
    }

    private static String groupList(int[] groups) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; groups != null && i < groups.length; i++) {
            if (i > 0) b.append(" | ");
            b.append(SatuSehatRujukanKompetensiData.display(groups[i]));
        }
        return b.toString();
    }

    private static boolean isCerebrovascular(String code, String name) {
        if (code.length() >= 3 && code.charAt(0) == 'I') {
            int n = numberPart(code);
            if (n >= 60 && n <= 69) return true;
        }
        return name.contains("stroke") || name.contains("cerebrovascular") || name.contains("cerebro vaskuler");
    }

    private static boolean isBurn(String code, String name) {
        int n = numberPart(code);
        return (code.charAt(0) == 'T' && n >= 20 && n <= 32)
                || name.contains("burn") || name.contains("luka bakar");
    }

    private static boolean isPoisoning(String code, String name) {
        int n = numberPart(code);
        return (code.charAt(0) == 'T' && n >= 36 && n <= 65)
                || name.contains("poison") || name.contains("keracun") || name.contains("intoxic");
    }

    private static int numberPart(String code) {
        if (code == null || code.length() < 2) return -1;
        StringBuilder b = new StringBuilder();
        for (int i = 1; i < code.length() && b.length() < 2; i++) {
            char c = code.charAt(i);
            if (c >= '0' && c <= '9') b.append(c); else if (b.length() > 0) break;
        }
        try { return b.length() == 0 ? -1 : Integer.parseInt(b.toString()); }
        catch (Exception e) { return -1; }
    }

    private static String normalizeCode(String s) {
        String v = safe(s).toUpperCase(Locale.ENGLISH);
        int sp = v.indexOf(' '); if (sp > 0) v = v.substring(0, sp);
        return v;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
