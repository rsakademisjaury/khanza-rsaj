package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Desktop hanya mengirim tiket sekali pakai. OAuth credential tetap di backend. */
public final class SsrmeClient {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ObjectMapper JSON = new ObjectMapper();
    private final java.util.function.Consumer<SsrmeTrace.Entry> trace;
    public SsrmeClient(){this(e -> { });}
    public SsrmeClient(java.util.function.Consumer<SsrmeTrace.Entry> trace){this.trace=trace;}
    public static final class Context {
        public final String noRawat, noRm;
        public Context(String noRawat, String noRm) { this.noRawat = text(noRawat); this.noRm = text(noRm); }
    }
    public static final class Doctor {
        public final String code, name;
        public Doctor(String code, String name) { this.code = text(code); this.name = text(name); }
        @Override public String toString() { return name + " (" + code + ")"; }
    }
    public static final class Setup {
        public URI backend;
        public String actor, actorDoctor, patientName, patientRm, visit, suggestedDoctor;
        public boolean view, consent, emergency;
        public final List<Doctor> doctors = new ArrayList<Doctor>();
    }
    public static final class Failure extends Exception {
        public final String code;
        public Failure(String code, String message) { super(message); this.code = code; }
    }
    public static final class Link {
        public final URI url;
        public final String expiresAt;
        public final boolean partial, warnings;
        Link(URI url, String expiresAt, boolean partial, boolean warnings) {
            this.url = url; this.expiresAt = expiresAt; this.partial = partial; this.warnings = warnings;
        }
    }

    public Setup load(Connection c, String actor, Context context) throws Exception {
        if (text(actor).isEmpty()) throw new Failure("SESSION", "Silakan login kembali ke SIMRS.");
        if (context.noRm.isEmpty() && context.noRawat.isEmpty()) throw new Failure("PATIENT", "Pilih pasien terlebih dahulu.");
        Setup s = new Setup(); s.actor = actor;
        try (PreparedStatement q = c.prepareStatement("select aktif,backend_url from rsaj_ssrme_setting where id=1"); ResultSet r = q.executeQuery()) {
            if (!r.next() || r.getInt(1) != 1) throw new Failure("DISABLED", "SSRME belum diaktifkan. Hubungi IT untuk konfigurasi awal.");
            s.backend = backendUri(r.getString(2));
        }
        // Izin mengikuti akun SIMRS yang sudah login, tanpa tabel mapping SSRME.
        boolean registrasi = false, igd = false;
        boolean admin = "Admin Utama".equals(actor);
        String sqlAkun = admin ? "select 1,1 from admin limit 1"
                : "select registrasi,igd from `user` where id_user=AES_ENCRYPT(?,'nur')";
        try (PreparedStatement q = c.prepareStatement(sqlAkun)) {
            if (!admin) q.setString(1, actor);
            try (ResultSet r = q.executeQuery()) {
                if (!r.next()) throw new Failure("FORBIDDEN", "Akun login tidak ditemukan dalam data user SIMRS.");
                registrasi = r.getBoolean(1); igd = r.getBoolean(2);
            }
        }
        String rm = context.noRm; s.visit = context.noRawat; s.suggestedDoctor = "";
        if (!s.visit.isEmpty()) {
            try (PreparedStatement q = c.prepareStatement("select no_rkm_medis,kd_dokter from reg_periksa where no_rawat=?")) {
                q.setString(1, s.visit);
                try (ResultSet r = q.executeQuery()) {
                    if (!r.next()) throw new Failure("VISIT", "Kunjungan tidak ditemukan. Pilih ulang pasien.");
                    if (!rm.isEmpty() && !rm.equals(r.getString(1))) throw new Failure("PATIENT_MISMATCH", "Pasien dan no. rawat tidak sesuai. Pilih ulang pasien.");
                    rm = r.getString(1); s.suggestedDoctor = text(r.getString(2));
                }
            }
        }
        s.patientRm = rm;
        try (PreparedStatement q = c.prepareStatement("select nm_pasien from pasien where no_rkm_medis=?")) {
            q.setString(1, rm);
            try (ResultSet r = q.executeQuery()) {
                if (!r.next()) throw new Failure("PATIENT", "Identitas pasien tidak ditemukan.");
                s.patientName = text(r.getString(1));
            }
        }
        try (PreparedStatement q = c.prepareStatement("select d.kd_dokter,d.nm_dokter from dokter d join pegawai p on p.nik=d.kd_dokter "
                + "where d.status='1' and p.stts_aktif<>'KELUAR' order by d.nm_dokter"); ResultSet r = q.executeQuery()) {
            while (r.next()) s.doctors.add(new Doctor(r.getString(1), r.getString(2)));
        }
        boolean ownDoctor = false;
        for (Doctor doctor : s.doctors) if (doctor.code.equals(actor)) ownDoctor = true;
        s.actorDoctor = ownDoctor ? actor : ""; s.view = ownDoctor;
        s.consent = ownDoctor || registrasi || igd; s.emergency = igd;
        if (!s.view && !s.consent) throw new Failure("FORBIDDEN", "Akun ini bukan dokter aktif dan tidak memiliki izin Registrasi/IGD di SIMRS.");
        if (s.doctors.isEmpty()) throw new Failure("PRACTITIONER", "Belum ada dokter aktif yang terhubung dengan data pegawai SIMRS. Periksa kesamaan kode dokter dan NIK pegawai.");
        return s;
    }

    public String ticket(Connection c, Setup setup, String doctor, String action) throws Exception {
        if (!"VIEW".equals(action) && !"CONSENT".equals(action) && !"EMERGENCY".equals(action)) throw new Failure("ACTION", "Aksi SSRME tidak valid.");
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes); String token = hex(bytes);
        String hash = hex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII)));
        try (PreparedStatement q = c.prepareStatement("insert into rsaj_ssrme_ticket (ticket_hash,user_code,no_rkm_medis,no_rawat,kd_dokter,action,expires_at) "
                + "values (?,?,?,?,?,?,timestampadd(second,120,current_timestamp))")) {
            q.setString(1, hash); q.setString(2, setup.actor); q.setString(3, setup.patientRm); q.setString(4, setup.visit);
            q.setString(5, doctor); q.setString(6, action); q.executeUpdate();
        }
        return token;
    }

    public Link request(URI backend, String ticket, boolean consent) throws Exception {
        long started=System.nanoTime();
        trace.accept(new SsrmeTrace.Entry(SsrmeTrace.now(),"POST",backend.toString(),0,0,"{}","{\"state\":\"RUNNING\"}"));
        HttpURLConnection c = (HttpURLConnection) backendUri(backend.toString()).toURL().openConnection();
        c.setInstanceFollowRedirects(false); c.setConnectTimeout(8000); c.setReadTimeout(90000);
        c.setRequestMethod("POST"); c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json"); c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Cache-Control", "no-store"); c.setRequestProperty("X-SSRME-Ticket", ticket);
        c.setFixedLengthStreamingMode(2);
        int httpStatus=0;boolean reported=false;
        try {
            try (OutputStream out = c.getOutputStream()) { out.write("{}".getBytes(StandardCharsets.US_ASCII)); }
            int status = c.getResponseCode();httpStatus=status;
            InputStream stream = status >= 400 ? c.getErrorStream() : c.getInputStream();
            if (stream == null) throw new Failure("NETWORK", "Server SSRME tidak memberikan respons. Hubungi IT.");
            byte[] bytes;
            try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] chunk = new byte[4096]; int n;
                while ((n = in.read(chunk)) != -1) {
                    if (out.size()+n > 65536) throw new Failure("RESPONSE", "Respons server SSRME terlalu besar.");
                    out.write(chunk, 0, n);
                }
                bytes = out.toByteArray();
            }
            JsonNode r;
            try { r = JSON.readTree(bytes); }
            catch (Exception e) { throw new Failure("RESPONSE", "Respons server SSRME belum dapat dibaca. Periksa pemasangan backend."); }
            for(JsonNode row:r.path("diagnostics")) {
                trace.accept(new SsrmeTrace.Entry(SsrmeTrace.now(), row.path("method").asText(), row.path("endpoint").asText(),
                    row.path("http").asInt(), row.path("duration_ms").asLong(), row.path("request").toString(), row.path("response").toString()));
            }
            com.fasterxml.jackson.databind.node.ObjectNode summary=JSON.createObjectNode();
            summary.put("success",r.path("success").asBoolean());summary.put("code",r.path("code").asText());summary.put("message",r.path("message").asText());
            trace.accept(new SsrmeTrace.Entry(SsrmeTrace.now(),"POST",backend.toString(),status,(System.nanoTime()-started)/1000000,"{}",summary.toString()));
            reported=true;return parse(status, r, consent);
        } catch (Exception e) {
            if(!reported){
                String code=e instanceof Failure?((Failure)e).code:"NETWORK_ERROR";
                com.fasterxml.jackson.databind.node.ObjectNode failure=JSON.createObjectNode();failure.put("code",code);
                trace.accept(new SsrmeTrace.Entry(SsrmeTrace.now(),"POST",backend.toString(),httpStatus,(System.nanoTime()-started)/1000000,"{}",failure.toString()));
            }
            throw e;
        } finally { c.disconnect(); }
    }

    public static Link parse(int status, JsonNode r, boolean consent) throws Exception {
        if (r == null || !r.isObject()) throw new Failure("RESPONSE", "Respons SSRME tidak valid.");
        if (status != 200 || !r.path("success").asBoolean(false)) {
            String code = jsonText(r.path("code"), "SERVER_ERROR");
            String msg = jsonText(r.path("message"), "");
            // Backend kita mengembalikan pesan terkontrol; tidak menampilkan HTML atau raw API body.
            if (msg.isEmpty() || msg.length()>600 || msg.contains("<") || msg.contains(">")) msg = "SSRME belum dapat diproses. Hubungi IT.";
            throw new Failure(code, msg);
        }
        String expected = consent ? "CONSENT_LINK_CREATED" : "VIEW_LINK_CREATED";
        if (!expected.equals(r.path("code").asText())) throw new Failure("RESPONSE", "Jenis tautan SSRME tidak sesuai permintaan.");
        URI url = viewerUri(jsonText(r.path("url"), ""), consent);
        String expires = jsonText(r.path("expired_at"), "");
        try {
            if (!java.time.OffsetDateTime.parse(expires).toInstant().isAfter(java.time.Instant.now())) throw new IllegalArgumentException();
        } catch (Exception e) { throw new Failure("EXPIRED", "Tautan SSRME sudah kedaluwarsa atau masa berlakunya tidak valid."); }
        return new Link(url, expires, r.path("partial").asBoolean(false), r.path("has_warnings").asBoolean(false));
    }

    public static URI backendUri(String value) throws Failure {
        try {
            URI u = new URI(text(value));
            // HTTP hanya loopback untuk pengujian lokal, bukan alamat LAN produksi.
            boolean loopback = "http".equals(u.getScheme()) && ("127.0.0.1".equals(u.getHost()) || "localhost".equals(u.getHost()));
            if ((!"https".equals(u.getScheme()) && !loopback) || u.getHost()==null || u.getUserInfo()!=null || u.getFragment()!=null || u.getQuery()!=null) throw new IllegalArgumentException();
            return u;
        } catch (Exception e) { throw new Failure("CONFIG", "URL backend SSRME belum valid. Gunakan HTTPS dengan sertifikat tepercaya."); }
    }
    public static URI viewerUri(String value, boolean consent) throws Failure {
        try {
            URI u = new URI(value);
            boolean host = "satusehat-stg.dto.kemkes.go.id".equals(u.getHost()) || "satusehat.kemkes.go.id".equals(u.getHost());
            String p = u.getPath();
            boolean path = consent ? "/rekammedis/consent".equals(p) || "/rekammedis/consent/".equals(p) : "/rekammedis".equals(p) || "/rekammedis/".equals(p);
            if (!"https".equals(u.getScheme()) || !host || !path || u.getUserInfo()!=null || u.getFragment()!=null || (u.getPort()!=-1 && u.getPort()!=443)) throw new IllegalArgumentException();
            String query = u.getRawQuery();
            if (query == null || !query.matches("(?:.*&)?"+(consent ? "launch" : "shlink")+"=[^&]+(?:&.*)?")) throw new IllegalArgumentException();
            return u;
        } catch (Exception e) { throw new Failure("LINK", "Alamat viewer SSRME tidak valid."); }
    }
    // Jackson 2.2.3 bawaan RSAJ belum memiliki JsonNode.asText(String).
    // Pertahankan fallback untuk missing/null tanpa mengubah teks kosong yang sah.
    private static String jsonText(JsonNode node, String fallback) {
        return node == null || node.isNull() || node.isMissingNode() ? fallback : node.asText();
    }
    private static String hex(byte[] bytes) {
        StringBuilder b = new StringBuilder(); for (byte x : bytes) b.append(String.format(java.util.Locale.ROOT, "%02x", x & 255)); return b.toString();
    }
    public static String text(String value) { return value == null ? "" : value.trim(); }
}
