package bridging;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

/** JDBC lokal. Connection pinjaman: tidak close/commit/rollback/setAutoCommit koneksi induk. */
public final class SatuSehatRujukanIGDRanapRepository {
    public static final String TABLE = "satusehat_rujukan_igd_ranap_draft";
    public interface ConnectionProvider { Connection get() throws Exception; }
    private final ConnectionProvider provider;
    // Samakan sumber lookup IHS dengan form BPJSRujukanSatuSehat yang sudah berjalan.
    private final SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();
    public SatuSehatRujukanIGDRanapRepository(ConnectionProvider provider) {
        if (provider == null) throw new IllegalArgumentException("ConnectionProvider wajib tersedia.");
        this.provider = provider;
    }
    /** API koneksi yang ditemukan dalam source lampiran; tanpa dependensi compile tambahan. */
    public static SatuSehatRujukanIGDRanapRepository forKhanza() {
        return new SatuSehatRujukanIGDRanapRepository(() -> {
            try { return (Connection) Class.forName("fungsi.koneksiDB").getMethod("condb").invoke(null); }
            catch (InvocationTargetException ex) { throw new SQLException("Koneksi database Khanza belum tersedia.", ex.getCause()); }
            catch (ReflectiveOperationException ex) { throw new SQLException("Jalankan form dari project Khanza atau gunakan main untuk pratinjau.", ex); }
        });
    }
    public static final class Visit {
        public String noRawat = "", noRm = "", name = "", doctor = "", doctorCode = "", origin = "";
        public String patientNik = "", doctorNik = "", practitionerIhs = "";
        public String diagnosisCode = "", diagnosisName = "", secondaryDiagnosisCode = "", secondaryDiagnosisName = "";
        public String procedures = "", encounter = "", ihs = "", conditionId = "", secondaryConditionId = "";
        public String bloodPressure = "", pulse = "", respiratoryRate = "", spo2 = "", temperature = "", consciousness = "", measuredAt = "";
        public String clinicalSummary = "", allergies = "";
        public final List<String> sepNumbers = new ArrayList<String>(), warnings = new ArrayList<String>();
        public SatuSehatRujukanIGDRanapDraft draft;
        public boolean draftAvailable = true;
    }
    public static final class HistoryItem {
        public final String rawat, rm, jenis, updatedBy, updatedAt;
        public final long version;
        HistoryItem(ResultSet r) throws SQLException {
            rawat = r.getString("no_rawat"); rm = r.getString("no_rm"); jenis = r.getString("jenis_rujukan");
            updatedBy = r.getString("updated_by"); updatedAt = String.valueOf(r.getTimestamp("updated_at")); version = r.getLong("version");
        }
    }
    public static final class DiseaseItem {
        public final String code, name;
        DiseaseItem(String code, String name) { this.code = safe(code); this.name = safe(name); }
        @Override public String toString() { return code + " - " + name; }
    }
    private Connection connection() throws Exception {
        Connection c = provider.get();
        if (c == null || c.isClosed()) throw new SQLException("Koneksi database tidak tersedia.");
        return c;
    }
    private static PreparedStatement prepare(Connection c, String sql) throws SQLException {
        PreparedStatement p = c.prepareStatement(sql);
        try { p.setQueryTimeout(15); } catch (SQLException unsupported) { /* Driver lama. */ }
        return p;
    }

    /** Cari ICD-10 dari master penyakit lokal untuk picker diagnosis rujukan. */
    public List<DiseaseItem> searchDiseases(String keyword, int limit) throws Exception {
        String key = safe(keyword); int max = Math.max(10, Math.min(limit, 300));
        Connection c = connection(); synchronized (c) {
            List<DiseaseItem> out = new ArrayList<DiseaseItem>();
            String like = "%" + key + "%";
            try (PreparedStatement p = prepare(c, "SELECT kd_penyakit,nm_penyakit FROM penyakit "
                    + "WHERE kd_penyakit LIKE ? OR nm_penyakit LIKE ? ORDER BY kd_penyakit LIMIT " + max)) {
                p.setString(1, like); p.setString(2, like);
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) out.add(new DiseaseItem(r.getString(1), r.getString(2)));
                }
            }
            return out;
        }
    }

    /** Ambil Encounter yang sudah tersimpan untuk no.rawat tanpa memuat ulang seluruh draf. */
    public String findEncounterId(String noRawat) throws Exception {
        String rawat = safe(noRawat); if (rawat.isEmpty()) return "";
        Connection c = connection(); synchronized (c) {
            try (PreparedStatement p = prepare(c, "SELECT id_encounter FROM satu_sehat_encounter WHERE no_rawat=? "
                    + "AND TRIM(IFNULL(id_encounter,''))<>'' ORDER BY id_encounter DESC LIMIT 1")) {
                p.setString(1, rawat); try (ResultSet r = p.executeQuery()) { return r.next() ? safe(r.getString(1)) : ""; }
            }
        }
    }

    /** Ambil Condition existing untuk diagnosis/no.rawat tertentu. */
    public String findConditionId(String noRawat, String diagnosisCode) throws Exception {
        String rawat = safe(noRawat), code = safe(diagnosisCode); if (rawat.isEmpty() || code.isEmpty()) return "";
        Connection c = connection(); synchronized (c) {
            try (PreparedStatement p = prepare(c, "SELECT id_condition FROM satu_sehat_condition WHERE no_rawat=? "
                    + "AND kd_penyakit=? AND TRIM(IFNULL(id_condition,''))<>'' ORDER BY id_condition DESC LIMIT 1")) {
                p.setString(1, rawat); p.setString(2, code);
                try (ResultSet r = p.executeQuery()) { return r.next() ? safe(r.getString(1)) : ""; }
            }
        }
    }

    /** Simpan ID Condition hasil POST ke cache existing Khanza. Tidak mengubah diagnosis rekam medis. */
    public void cacheConditionId(String noRawat, String diagnosisCode, SatuSehatRujukanIGDRanapDraft.Jenis jenis, String conditionId) throws Exception {
        String rawat=safe(noRawat), code=safe(diagnosisCode), id=safe(conditionId);
        if(rawat.isEmpty()||code.isEmpty()||id.isEmpty()||jenis==null)throw new IllegalArgumentException("Identitas Condition belum lengkap.");
        Connection c=connection(); synchronized(c){
            try(PreparedStatement u=prepare(c,"UPDATE satu_sehat_condition SET status=?,id_condition=? WHERE no_rawat=? AND kd_penyakit=?")){
                u.setString(1,jenis.statusDiagnosis);u.setString(2,id);u.setString(3,rawat);u.setString(4,code);
                if(u.executeUpdate()>0)return;
            }
            try(PreparedStatement i=prepare(c,"INSERT INTO satu_sehat_condition(no_rawat,kd_penyakit,status,id_condition) VALUES(?,?,?,?)")){
                i.setString(1,rawat);i.setString(2,code);i.setString(3,jenis.statusDiagnosis);i.setString(4,id);i.executeUpdate();
            }
        }
    }
    public Visit loadVisit(String rawat, SatuSehatRujukanIGDRanapDraft.Jenis jenis) throws Exception {
        return loadVisitInternal(rawat, jenis, true);
    }
    /** Ambil ulang sumber data klinis/registrasi TANPA memuat payload draf lama. */
    public Visit loadVisitFresh(String rawat, SatuSehatRujukanIGDRanapDraft.Jenis jenis) throws Exception {
        return loadVisitInternal(rawat, jenis, false);
    }
    private Visit loadVisitInternal(String rawat, SatuSehatRujukanIGDRanapDraft.Jenis jenis, boolean includeDraft) throws Exception {
        rawat = SatuSehatRujukanIGDRanapDraft.safe(rawat);
        if (rawat.isEmpty() || rawat.length() > 32 || jenis == null) throw new IllegalArgumentException("No. rawat atau jenis tidak valid.");
        Connection c = connection();
        synchronized (c) {
            Visit v = new Visit();
            try (PreparedStatement p = prepare(c, "SELECT rp.no_rawat,rp.no_rkm_medis,ps.nm_pasien,ps.no_ktp AS no_ktp_pasien,"
                    + "dk.nm_dokter,rp.kd_dokter,pg.no_ktp AS no_ktp_dokter,rp.kd_poli,pl.nm_poli "
                    + "FROM reg_periksa rp JOIN pasien ps ON ps.no_rkm_medis=rp.no_rkm_medis "
                    + "LEFT JOIN dokter dk ON dk.kd_dokter=rp.kd_dokter "
                    + "LEFT JOIN pegawai pg ON pg.nik=dk.kd_dokter "
                    + "LEFT JOIN poliklinik pl ON pl.kd_poli=rp.kd_poli WHERE rp.no_rawat=?")) {
                p.setString(1, rawat);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Kunjungan tidak ditemukan. Periksa no. rawat.", "02000");
                    v.noRawat = r.getString("no_rawat"); v.noRm = r.getString("no_rkm_medis");
                    v.name = r.getString("nm_pasien"); v.patientNik = safe(r.getString("no_ktp_pasien"));
                    v.doctor = r.getString("nm_dokter"); v.doctorCode = safe(r.getString("kd_dokter"));
                    v.doctorNik = safe(r.getString("no_ktp_dokter"));
                    String poliName=safe(r.getString("nm_poli")), poliCode=safe(r.getString("kd_poli"));
                    v.origin = poliName.isEmpty() ? (poliCode.isEmpty()?"":"Poli asal: "+poliCode) : "Poli asal: "+poliName+(poliCode.isEmpty()?"":" ["+poliCode+"]");
                }
            }
            try (PreparedStatement p = prepare(c, "SELECT dp.kd_penyakit,py.nm_penyakit FROM diagnosa_pasien dp "
                    + "LEFT JOIN penyakit py ON py.kd_penyakit=dp.kd_penyakit "
                    + "WHERE dp.no_rawat=? AND dp.prioritas='1' AND dp.status=? ORDER BY dp.kd_penyakit")) {
                p.setString(1, rawat); p.setString(2, jenis.statusDiagnosis);
                try (ResultSet r = p.executeQuery()) {
                    if (r.next()) { v.diagnosisCode = r.getString(1); v.diagnosisName = r.getString(2); }
                    if (r.next()) { v.diagnosisCode = ""; v.diagnosisName = ""; v.warnings.add("Lebih dari satu diagnosis prioritas 1; isi diagnosis rujukan setelah ditinjau."); }
                }
            } catch (SQLException ex) { optionalFailure(v, "Diagnosis", ex); }
            // Postman Rujukan Pasien 30-06-2026 mengirim secondary-diagnosis pada
            // Task pencarian kandidat bila diagnosis sekunder tersedia. Ambil
            // hanya prioritas 2 yang tunggal; jika ambigu jangan mengarang data.
            try (PreparedStatement p = prepare(c, "SELECT dp.kd_penyakit,py.nm_penyakit FROM diagnosa_pasien dp "
                    + "LEFT JOIN penyakit py ON py.kd_penyakit=dp.kd_penyakit "
                    + "WHERE dp.no_rawat=? AND dp.prioritas='2' AND dp.status=? ORDER BY dp.kd_penyakit")) {
                p.setString(1, rawat); p.setString(2, jenis.statusDiagnosis);
                try (ResultSet r = p.executeQuery()) {
                    if (r.next()) { v.secondaryDiagnosisCode = safe(r.getString(1)); v.secondaryDiagnosisName = safe(r.getString(2)); }
                    if (r.next()) { v.secondaryDiagnosisCode = ""; v.secondaryDiagnosisName = ""; v.warnings.add("Lebih dari satu diagnosis prioritas 2; secondary-diagnosis tidak diisi otomatis."); }
                }
            } catch (SQLException ex) { optionalFailure(v, "Diagnosis sekunder", ex); }
            if (jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP) {
                // Rawat inap: prioritaskan DPJP dan bangsal terbaru, bukan dokter/poli registrasi awal.
                try (PreparedStatement p = prepare(c, "SELECT d.kd_dokter,d.nm_dokter,pg.no_ktp FROM dpjp_ranap dr "
                        + "JOIN dokter d ON d.kd_dokter=dr.kd_dokter LEFT JOIN pegawai pg ON pg.nik=d.kd_dokter "
                        + "WHERE dr.no_rawat=? ORDER BY dr.kd_dokter")) {
                    p.setString(1, rawat); try (ResultSet r=p.executeQuery()) {
                        if(r.next()){v.doctorCode=safe(r.getString(1));v.doctor=safe(r.getString(2));v.doctorNik=safe(r.getString(3));}
                        if(r.next())v.warnings.add("DPJP rawat inap lebih dari satu; form memakai DPJP pertama dan perlu verifikasi user.");
                    }
                } catch(SQLException ex){optionalFailure(v,"DPJP rawat inap",ex);}
                try (PreparedStatement p = prepare(c, "SELECT ki.kd_kamar,b.nm_bangsal FROM kamar_inap ki "
                        + "LEFT JOIN kamar k ON k.kd_kamar=ki.kd_kamar LEFT JOIN bangsal b ON b.kd_bangsal=k.kd_bangsal "
                        + "WHERE ki.no_rawat=? ORDER BY ki.tgl_masuk DESC,ki.jam_masuk DESC LIMIT 1")) {
                    p.setString(1,rawat);try(ResultSet r=p.executeQuery()){
                        if(r.next()){String kamar=safe(r.getString(1)),bangsal=safe(r.getString(2));v.origin=bangsal.isEmpty()?kamar:(bangsal+(kamar.isEmpty()?"":" / "+kamar));}
                    }
                } catch(SQLException ex){optionalFailure(v,"Bangsal rawat inap",ex);}
            }
            try (PreparedStatement p = prepare(c, "SELECT DISTINCT kode FROM prosedur_pasien WHERE no_rawat=? AND status=? ORDER BY kode")) {
                p.setString(1, rawat); p.setString(2, jenis.statusDiagnosis);
                Set<String> codes = new LinkedHashSet<String>();
                try (ResultSet r = p.executeQuery()) { while (r.next()) codes.add(r.getString(1)); }
                v.procedures = String.join(", ", codes);
            } catch (SQLException ex) { optionalFailure(v, "Prosedur", ex); }
            try (PreparedStatement p = prepare(c, "SELECT DISTINCT no_sep FROM bridging_sep WHERE no_rawat=? ORDER BY no_sep")) {
                p.setString(1, rawat);
                try (ResultSet r = p.executeQuery()) { while (r.next()) { String sep = r.getString(1); if (sep != null && !sep.trim().isEmpty()) v.sepNumbers.add(sep); } }
            } catch (SQLException ex) { optionalFailure(v, "SEP", ex); }
            try (PreparedStatement p = prepare(c, "SELECT id_encounter FROM satu_sehat_encounter WHERE no_rawat=?")) {
                p.setString(1, rawat);
                try (ResultSet r = p.executeQuery()) {
                    if (r.next()) v.encounter = r.getString(1);
                    if (r.next()) { v.encounter = ""; v.warnings.add("Encounter lebih dari satu; perlu pencocokan episode."); }
                }
            } catch (SQLException ex) { optionalFailure(v, "Encounter", ex); }
            // IHS pasien: pola sama dengan BPJSRujukanSatuSehat -> cache lokal, lalu lookup SATUSEHAT via NIK.
            if (!v.patientNik.isEmpty()) {
                try (PreparedStatement p = prepare(c, "SELECT DISTINCT ihs_pasien FROM satu_sehat_pasien "
                        + "WHERE no_ktp=? AND TRIM(IFNULL(ihs_pasien,''))<>''")) {
                    p.setString(1, v.patientNik);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) v.ihs = safe(r.getString(1));
                        if (r.next()) { v.ihs = ""; v.warnings.add("IHS pasien lokal lebih dari satu; perlu pencocokan identitas."); }
                    }
                } catch (SQLException ex) { optionalFailure(v, "IHS pasien lokal", ex); }
                if (v.ihs.isEmpty()) {
                    try {
                        v.ihs = safe(cekViaSatuSehat.tampilIDPasien(v.patientNik));
                        if (!v.ihs.isEmpty()) {
                            upsertPasienIhs(c, v.patientNik, v.noRm, v.ihs);
                        } else {
                            v.warnings.add("IHS pasien tidak ditemukan di SATUSEHAT untuk NIK yang tersimpan.");
                        }
                    } catch (Exception ex) {
                        v.warnings.add("Lookup IHS pasien ke SATUSEHAT gagal: " + safe(ex.getMessage()));
                    }
                }
            } else {
                v.warnings.add("NIK pasien kosong; IHS pasien tidak dapat dicari ke SATUSEHAT.");
            }

            // IHS dokter/praktisi: cache lokal, lalu lookup SATUSEHAT via NIK dokter.
            if (!v.doctorNik.isEmpty()) {
                try (PreparedStatement p = prepare(c, "SELECT DISTINCT ihs_praktisi FROM satu_sehat_praktisi "
                        + "WHERE no_ktp=? AND TRIM(IFNULL(ihs_praktisi,''))<>''")) {
                    p.setString(1, v.doctorNik);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) v.practitionerIhs = safe(r.getString(1));
                        if (r.next()) { v.practitionerIhs = ""; v.warnings.add("IHS dokter lokal lebih dari satu; perlu pencocokan identitas."); }
                    }
                } catch (SQLException ex) { optionalFailure(v, "IHS dokter lokal", ex); }
                if (v.practitionerIhs.isEmpty()) {
                    try {
                        v.practitionerIhs = safe(cekViaSatuSehat.tampilIDParktisi(v.doctorNik));
                        if (!v.practitionerIhs.isEmpty()) {
                            upsertPraktisiIhs(c, v.doctorNik, v.doctorCode, v.practitionerIhs);
                        } else {
                            v.warnings.add("IHS dokter tidak ditemukan di SATUSEHAT untuk NIK yang tersimpan.");
                        }
                    } catch (Exception ex) {
                        v.warnings.add("Lookup IHS dokter ke SATUSEHAT gagal: " + safe(ex.getMessage()));
                    }
                }
            } else if (jenis != SatuSehatRujukanIGDRanapDraft.Jenis.RANAP) {
                v.warnings.add("NIK dokter kosong; IHS dokter tidak dapat dicari ke SATUSEHAT.");
            }
            // Gunakan Condition yang sudah pernah dikirim untuk diagnosis utama ini.
            // Tahap II tidak membuat Condition baru secara otomatis dari tombol rujukan.
            if (v.diagnosisCode != null && !v.diagnosisCode.trim().isEmpty()) {
                try (PreparedStatement p = prepare(c, "SELECT id_condition FROM satu_sehat_condition "
                        + "WHERE no_rawat=? AND kd_penyakit=? AND TRIM(IFNULL(id_condition,''))<>'' "
                        + "ORDER BY id_condition DESC LIMIT 1")) {
                    p.setString(1, rawat); p.setString(2, v.diagnosisCode);
                    try (ResultSet r = p.executeQuery()) { if (r.next()) v.conditionId = safe(r.getString(1)); }
                } catch (SQLException ex) { optionalFailure(v, "Condition diagnosis", ex); }
            }
            if (!v.secondaryDiagnosisCode.isEmpty()) {
                try (PreparedStatement p = prepare(c, "SELECT id_condition FROM satu_sehat_condition "
                        + "WHERE no_rawat=? AND kd_penyakit=? AND TRIM(IFNULL(id_condition,''))<>'' "
                        + "ORDER BY id_condition DESC LIMIT 1")) {
                    p.setString(1, rawat); p.setString(2, v.secondaryDiagnosisCode);
                    try (ResultSet r = p.executeQuery()) { if (r.next()) v.secondaryConditionId = safe(r.getString(1)); }
                } catch (SQLException ex) { optionalFailure(v, "Condition diagnosis sekunder", ex); }
            }
            // Tanda vital/pemeriksaan terbaru. IGD memakai pemeriksaan_ralan, rawat inap pemeriksaan_ranap.
            String pemeriksaanTable = jenis == SatuSehatRujukanIGDRanapDraft.Jenis.RANAP ? "pemeriksaan_ranap" : "pemeriksaan_ralan";
            try (PreparedStatement p = prepare(c, "SELECT suhu_tubuh,tensi,nadi,respirasi,spo2,gcs,kesadaran,keluhan,pemeriksaan,alergi,tgl_perawatan,jam_rawat "
                    + "FROM "+pemeriksaanTable+" WHERE no_rawat=? ORDER BY tgl_perawatan DESC,jam_rawat DESC LIMIT 1")) {
                p.setString(1,rawat);try(ResultSet r=p.executeQuery()){
                    if(r.next()){
                        v.temperature=safe(r.getString("suhu_tubuh"));v.bloodPressure=safe(r.getString("tensi"));v.pulse=safe(r.getString("nadi"));
                        v.respiratoryRate=safe(r.getString("respirasi"));v.spo2=safe(r.getString("spo2"));
                        String kes=safe(r.getString("kesadaran")),gcs=safe(r.getString("gcs"));v.consciousness=kes.isEmpty()?gcs:(kes+(gcs.isEmpty()?"":" / GCS "+gcs));
                        String kel=safe(r.getString("keluhan")),per=safe(r.getString("pemeriksaan"));
                        v.clinicalSummary=kel.isEmpty()?per:(per.isEmpty()?kel:(kel+" | Pemeriksaan: "+per));v.allergies=safe(r.getString("alergi"));
                        String tgl=String.valueOf(r.getDate("tgl_perawatan")),jam=safe(r.getString("jam_rawat"));
                        v.measuredAt=("null".equalsIgnoreCase(tgl)?"":tgl)+(jam.isEmpty()?"":" "+jam);
                    }
                }
            } catch(SQLException ex){optionalFailure(v,"Tanda vital/pemeriksaan terbaru",ex);}

            v.draft = new SatuSehatRujukanIGDRanapDraft(v.noRawat, v.noRm, jenis);
            v.draft.set("diagnosis_code", v.diagnosisCode); v.draft.set("diagnosis_name", v.diagnosisName);
            v.draft.set("doctor", v.doctor); v.draft.set("origin", v.origin); v.draft.set("procedures", v.procedures);
            v.draft.set("blood_pressure",v.bloodPressure);v.draft.set("pulse",v.pulse);v.draft.set("respiratory_rate",v.respiratoryRate);
            v.draft.set("spo2",v.spo2);v.draft.set("temperature",v.temperature);v.draft.set("consciousness",v.consciousness);v.draft.set("measured_at",v.measuredAt);
            v.draft.set("clinical",v.clinicalSummary);v.draft.set("allergies",v.allergies);
            if(includeDraft) try { loadDraft(c, v); }
            catch (SQLException ex) {
                if (!schemaMissing(ex)) throw ex;
                v.draftAvailable = false; v.warnings.add("Tabel/kolom draf belum tersedia. Periksa SQL pemasangan sebelum menyimpan.");
            }
            // Jangan menimpa pilihan manual pada draf lama. Isi otomatis hanya bila kosong.
            if (v.draft.get("condition_id").isEmpty() && v.conditionId != null) v.draft.set("condition_id", v.conditionId);
            if (v.draft.get("secondary_diagnosis_code").isEmpty()) v.draft.set("secondary_diagnosis_code", v.secondaryDiagnosisCode);
            if (v.draft.get("secondary_diagnosis_name").isEmpty()) v.draft.set("secondary_diagnosis_name", v.secondaryDiagnosisName);
            if (v.draft.get("secondary_condition_id").isEmpty()) v.draft.set("secondary_condition_id", v.secondaryConditionId);
            if (v.draft.get("practitioner_ihs").isEmpty() && v.practitionerIhs != null) v.draft.set("practitioner_ihs", v.practitionerIhs);
            return v;
        }
    }
    private static String safe(String value) { return value == null ? "" : value.trim(); }

    private static void upsertPasienIhs(Connection c, String nik, String noRm, String ihs) {
        if (c == null || nik.isEmpty() || ihs.isEmpty()) return;
        try (PreparedStatement u = prepare(c, "UPDATE satu_sehat_pasien SET no_rkm_medis=?,ihs_pasien=? WHERE no_ktp=?")) {
            u.setString(1, noRm); u.setString(2, ihs); u.setString(3, nik);
            if (u.executeUpdate() > 0) return;
        } catch (SQLException ignored) { return; }
        try (PreparedStatement i = prepare(c, "INSERT INTO satu_sehat_pasien(no_ktp,no_rkm_medis,ihs_pasien) VALUES(?,?,?)")) {
            i.setString(1, nik); i.setString(2, noRm); i.setString(3, ihs); i.executeUpdate();
        } catch (SQLException ignored) { /* Lookup tetap boleh dipakai walau cache gagal disimpan. */ }
    }

    private static void upsertPraktisiIhs(Connection c, String nik, String kdDokter, String ihs) {
        if (c == null || nik.isEmpty() || ihs.isEmpty()) return;
        try (PreparedStatement u = prepare(c, "UPDATE satu_sehat_praktisi SET kd_dokter=?,ihs_praktisi=? WHERE no_ktp=?")) {
            u.setString(1, kdDokter); u.setString(2, ihs); u.setString(3, nik);
            if (u.executeUpdate() > 0) return;
        } catch (SQLException ignored) { return; }
        try (PreparedStatement i = prepare(c, "INSERT INTO satu_sehat_praktisi(no_ktp,kd_dokter,ihs_praktisi) VALUES(?,?,?)")) {
            i.setString(1, nik); i.setString(2, kdDokter); i.setString(3, ihs); i.executeUpdate();
        } catch (SQLException ignored) { /* Lookup tetap boleh dipakai walau cache gagal disimpan. */ }
    }

    private void loadDraft(Connection c, Visit v) throws SQLException, IOException {
        try (PreparedStatement p = prepare(c, "SELECT no_rm,payload_xml,version,updated_by,updated_at FROM " + TABLE + " WHERE no_rawat=? AND jenis_rujukan=?")) {
            p.setString(1, v.noRawat); p.setString(2, v.draft.jenis.name());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return;
                if (!v.noRm.equals(r.getString("no_rm"))) throw new SQLException("Identitas draf tidak sama dengan kunjungan; draf tidak dimuat.", "45000");
                v.draft.loadXml(r.getString("payload_xml")); v.draft.version = r.getLong("version");
                if (v.draft.version < 1) throw new SQLException("Versi draf tidak valid; periksa data tersimpan.", "45000");
                v.draft.updatedBy = r.getString("updated_by"); v.draft.updatedAt = String.valueOf(r.getTimestamp("updated_at"));
            }
        }
    }
    /** Pemeriksaan versi mencegah perubahan operator lain tertimpa tanpa diketahui. */
    public SatuSehatRujukanIGDRanapDraft save(SatuSehatRujukanIGDRanapDraft input, String operator) throws Exception {
        final SatuSehatRujukanIGDRanapDraft d = input.copy(); operator = SatuSehatRujukanIGDRanapDraft.safe(operator);
        if (d.noRawat.isEmpty() || d.noRm.isEmpty() || d.noRawat.length() > 32 || d.noRm.length() > 32 || d.version < 0)
            throw new IllegalArgumentException("Identitas atau versi draf tidak valid. Muat ulang kunjungan.");
        if (operator.isEmpty() || operator.length() > 100) throw new IllegalArgumentException("Identitas operator belum tersedia atau terlalu panjang.");
        final String xml = d.toXml(); Connection c = connection();
        synchronized (c) {
            if (!c.getAutoCommit()) throw new SQLException("Koneksi sedang dalam transaksi lain. Selesaikan transaksi itu sebelum menyimpan draf.");
            try (PreparedStatement p = prepare(c, "SELECT no_rkm_medis FROM reg_periksa WHERE no_rawat=?")) {
                p.setString(1, d.noRawat);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next() || !d.noRm.equals(r.getString(1))) throw new SQLException("Identitas kunjungan berubah; muat ulang sebelum menyimpan.", "45000");
                }
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (d.version == 0) {
                try (PreparedStatement p = prepare(c, "INSERT INTO " + TABLE + " (no_rawat,jenis_rujukan,no_rm,payload_xml,version,updated_by,updated_at) VALUES (?,?,?,?,1,?,?)")) {
                    p.setString(1, d.noRawat); p.setString(2, d.jenis.name()); p.setString(3, d.noRm); p.setString(4, xml);
                    p.setString(5, operator); p.setTimestamp(6, now); p.executeUpdate();
                } catch (SQLException ex) {
                    if (ex.getErrorCode() == 1062 || "23505".equals(ex.getSQLState())) throw conflict();
                    throw ex;
                }
            } else {
                try (PreparedStatement p = prepare(c, "UPDATE " + TABLE + " SET payload_xml=?,version=version+1,updated_by=?,updated_at=? "
                        + "WHERE no_rawat=? AND jenis_rujukan=? AND no_rm=? AND version=?")) {
                    p.setString(1, xml); p.setString(2, operator); p.setTimestamp(3, now); p.setString(4, d.noRawat);
                    p.setString(5, d.jenis.name()); p.setString(6, d.noRm); p.setLong(7, d.version);
                    if (p.executeUpdate() != 1) throw conflict();
                }
            }
            d.version++; d.updatedBy = operator; d.updatedAt = now.toString(); return d;
        }
    }
    private static SQLException conflict() {
        return new SQLException("Draf sudah diubah operator lain. Salin isian penting, muat ulang, lalu tinjau sebelum menyimpan kembali.", "40001");
    }
    public List<HistoryItem> history(String rawat) throws Exception {
        List<HistoryItem> list = new ArrayList<HistoryItem>(); Connection c = connection();
        synchronized (c) {
            try (PreparedStatement p = prepare(c, "SELECT no_rawat,no_rm,jenis_rujukan,version,updated_by,updated_at FROM " + TABLE + " WHERE no_rawat=? ORDER BY updated_at DESC")) {
                p.setString(1, rawat);
                try (ResultSet r = p.executeQuery()) { while (r.next()) list.add(new HistoryItem(r)); }
            }
        }
        return list;
    }
    private static boolean schemaMissing(SQLException ex) {
        return "42S02".equals(ex.getSQLState()) || "42S22".equals(ex.getSQLState()) || ex.getErrorCode() == 1146
            || ex.getErrorCode() == 1054 || ex.getErrorCode() == 42102 || ex.getErrorCode() == 42122 || ex.getErrorCode() == 42104;
    }
    private static void optionalFailure(Visit v, String field, SQLException ex) throws SQLException {
        if (!schemaMissing(ex)) throw ex;
        v.warnings.add(field + " belum dimuat: tabel/kolom perlu dicocokkan dengan database RS.");
    }
}
