/*
 * SisruteService.java
 *
 * Helper service untuk endpoint Sisrute (Rujukan SATUSEHAT) BPJS Kesehatan.
 * Mengikuti pattern ApiBPJS / ApiSatuSehat existing di SIMRS Khanza.
 *
 * Endpoint yang di-handle:
 *   1. POST /Rujukan/GetKriteriaRujukan
 *   2. POST /Rujukan/GetFaskesRujukan
 *   3. POST /Rujukan/Insert        (Insert Rujukan)
 *   4. DELETE /Rujukan/deleteKunjungan
 *
 * Header autentikasi (sama dengan VClaim):
 *   X-Cons-ID, X-Timestamp, X-Signature, user_key
 *
 * Catatan revisi v6:
 *   - Cons-ID, Secret Key, user_key dibaca dari konfigurasi khusus BPJS SISRUTE,
 *     bukan lagi dari VClaim umum.
 *   - Base URL dibaca dari URLAPIBPJSSISRUTE.
 *   - Signature dibuat memakai secretKey SISRUTE, bukan ApiBPJS.getHmac() lama.
 *   - Endpoint FKRTL/RS memakai /Rujukan/GetKriteriaRujukan, /Rujukan/GetFaskesRujukan,
 *     /Rujukan/Insert, dan /Rujukan/deleteKunjungan.
 *
 * @author dibuat untuk SIMRS Khanza - Bridging Sisrute SATUSEHAT
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

import AESsecurity.EnkripsiAES;
import java.io.FileInputStream;
import java.util.Properties;

public class SisruteService {

    // ==== Konfigurasi (dari koneksiDB) ====
    private String baseUrl;       // contoh DEV: https://apijkn-dev.bpjs-kesehatan.go.id/vclaim-sisrute-rest
    private String userKey;       // user_key khusus BPJS SISRUTE
    private String consId;        // cons id khusus BPJS SISRUTE
    private String secretKey;     // secret key khusus BPJS SISRUTE
    private String envSisrute;    // dev/prod
    private String kodeFaskesSatuSehatRujukan;
    private String kodePpkBpjsSisrute;
    private String idSatuSehatOrg;       // fallback dari IDSATUSEHAT() bila KODEFASKESSATUSEHATRUJUKAN salah
   

    private static boolean sisruteLoaded = false;

    // ==== Helper (reuse signature dari ApiBPJS) ====
    private final ApiBPJS api = new ApiBPJS();
    private final ObjectMapper mapper = new ObjectMapper();

    // ==== Workspace request ====
    private HttpHeaders headers;
    private HttpEntity<String> requestEntity;
    private JsonNode root;
    private String utc;
    private String responseBody;

    public SisruteService() {
        try {
            // Konfigurasi khusus BPJS SISRUTE.
            // Dibaca via reflection agar file ini tetap bisa compile saat koneksiDB.java
            // belum ditambah getter; tetapi untuk credential terenkripsi, getter koneksiDB tetap wajib.
            
            
//            baseUrl   = readKoneksiDB("URLAPIBPJSSISRUTE");
//            userKey   = readKoneksiDB("USERKEYAPIBPJSSISRUTE");
//            consId    = readKoneksiDB("CONSIDAPIBPJSSISRUTE");
//            secretKey = readKoneksiDB("SECRETKEYAPIBPJSSISRUTE");
//            envSisrute = readKoneksiDB("ENVBPJSSISRUTE");
//            kodeFaskesSatuSehatRujukan = readKoneksiDB("KODEFASKESSATUSEHATRUJUKAN");
//            kodePpkBpjsSisrute = readKoneksiDB("KODEPPKBPJSSISRUTE");
//            idSatuSehatOrg = readKoneksiDB("IDSATUSEHAT");
            
            loadConfigSisrute();
            if (envSisrute == null || envSisrute.trim().isEmpty()) {
                envSisrute = "dev";
            }

            // Fallback aman bila getter baru belum ada.
            // Catatan: fallback credential VClaim umum hanya untuk mencegah NullPointer;
            // untuk testing DEV SISRUTE sebaiknya tetap tambahkan getter baru di koneksiDB.java.
            if (isBlank(baseUrl)) {
                baseUrl = deriveSisruteUrlFromBpjsUrl(koneksiDB.URLAPIBPJS(), envSisrute);
            }
            if (isBlank(userKey)) {
                userKey = koneksiDB.USERKEYAPIBPJS();
                System.out.println("[SisruteService] WARNING: USERKEYAPIBPJSSISRUTE belum terbaca, fallback ke USERKEYAPIBPJS().");
            }
            if (isBlank(consId)) {
                consId = koneksiDB.CONSIDAPIBPJS();
                System.out.println("[SisruteService] WARNING: CONSIDAPIBPJSSISRUTE belum terbaca, fallback ke CONSIDAPIBPJS().");
            }
            if (isBlank(secretKey)) {
                secretKey = koneksiDB.SECRETKEYAPIBPJS();
                System.out.println("[SisruteService] WARNING: SECRETKEYAPIBPJSSISRUTE belum terbaca, fallback ke SECRETKEYAPIBPJS().");
            }

            baseUrl = normalizeBaseUrl(baseUrl, envSisrute);

            System.out.println("[SisruteService] ENV       : " + envSisrute);
            System.out.println("[SisruteService] Base URL  : " + baseUrl);
            System.out.println("[SisruteService] Faskes SS : " + safe(kodeFaskesSatuSehatRujukan));
            System.out.println("[SisruteService] PPK BPJS  : " + safe(kodePpkBpjsSisrute));
            System.out.println("[SisruteService] IDSATUSEHAT fallback : " + safe(idSatuSehatOrg));
        } catch (Exception ex) {
            System.out.println("SisruteService init error : " + ex);
        }
    }

    /**
     * Membaca getter static di koneksiDB.java, misalnya URLAPIBPJSSISRUTE().
     * Nama key dikirim tanpa tanda kurung.
     */
    private String readKoneksiDB(String methodName) {
        try {
            Method m = koneksiDB.class.getMethod(methodName);
            Object val = m.invoke(null);
            return val == null ? "" : val.toString().trim();
        } catch (Exception e) {
            System.out.println("[SisruteService] Getter koneksiDB." + methodName + "() belum tersedia/bermasalah: " + e.getClass().getSimpleName());
            return "";
        }
    }

    /**
     * Derive URL Integrasi SatuSehat Rujukan dari URL VClaim BPJS.
     *
     * Untuk RS/FKRTL gunakan base:
     *   DEV  : https://apijkn-dev.bpjs-kesehatan.go.id/vclaim-sisrute-rest
     *   PROD : https://apijkn.bpjs-kesehatan.go.id/vclaim-sisrute-rest
     *
     * Catatan penting:
     *   Endpoint lama /sisrute-rest akan menghasilkan 404 untuk jalur FKRTL.
     *   /pcare-sisrute-rest dipakai untuk FKTP/PCARE, bukan untuk RS/FKRTL.
     */
    private String deriveSisruteUrlFromBpjsUrl(String urlBpjs, String env) {
        if (urlBpjs == null || urlBpjs.trim().isEmpty()) {
            return isDev(env)
                    ? "https://dvlp.bpjs-kesehatan.go.id/vclaim-sisrute-rest/api/v1.0"
                    : "https://bpjs-kesehatan.go.id/vclaim-sisrute-rest/api/v1.0";
        }

        String url = urlBpjs.trim();
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

        // Kalau sudah benar, pakai apa adanya.
        if (url.endsWith("/vclaim-sisrute-rest")) {
            return url;
        }

        // Kalau masih memakai endpoint lama/keliru, paksa ke endpoint FKRTL yang benar.
        if (url.endsWith("/sisrute-rest")) {
            return url.substring(0, url.length() - "/sisrute-rest".length()) + "/vclaim-sisrute-rest";
        }
        if (url.endsWith("/pcare-sisrute-rest")) {
            return url.substring(0, url.length() - "/pcare-sisrute-rest".length()) + "/vclaim-sisrute-rest";
        }
        if (url.endsWith("/vclaim-rest")) {
            return url.substring(0, url.length() - "/vclaim-rest".length()) + "/vclaim-sisrute-rest";
        }
        if (url.endsWith("/vclaim")) {
            return url.substring(0, url.length() - "/vclaim".length()) + "/vclaim-sisrute-rest";
        }

        // Host standar BPJS.
//        if (url.contains("apijkn-dev.bpjs-kesehatan.go.id")) {
//            return "https://apijkn-dev.bpjs-kesehatan.go.id/vclaim-sisrute-rest";
//        }
//        if (url.contains("apijkn.bpjs-kesehatan.go.id")) {
//            return "https://apijkn.bpjs-kesehatan.go.id/vclaim-sisrute-rest";
//        }
        
        
          // Host standar BPJS.
        if (url.contains("apijkn-dev.bpjs-kesehatan.go.id")) {
            return "https://apijkn-dev.bpjs-kesehatan.go.id/vclaim-sisrute-rest";
        }
        if (url.contains("apijkn.bpjs-kesehatan.go.id")) {
            return "https://apijkn.bpjs-kesehatan.go.id/vclaim-sisrute-rest";
        }

        // Fallback: ganti path terakhir menjadi /vclaim-sisrute-rest.
        int idx = url.indexOf("://");
        int firstSlashAfterHost = idx > -1 ? url.indexOf("/", idx + 3) : -1;
        if (firstSlashAfterHost > -1) {
            return url.substring(0, firstSlashAfterHost) + "/vclaim-sisrute-rest";
        }
        return url + "/vclaim-sisrute-rest";
    }

    private String normalizeBaseUrl(String url, String env) {
        if (isBlank(url)) {
            return isDev(env)
                    ? "https://dvlp.bpjs-kesehatan.go.id/vclaim-sisrute-rest/api/v1.0"
                    : "https://bpjs-kesehatan.go.id/vclaim-sisrute-rest/api/v1.0";
        }
        String u = url.trim();
        if (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        // Kalau user mengisi host dev/prod tetapi path masih salah, luruskan ke FKRTL.
        if (u.endsWith("/sisrute-rest") || u.endsWith("/pcare-sisrute-rest") || u.endsWith("/vclaim-rest")) {
            return deriveSisruteUrlFromBpjsUrl(u, env);
        }
        if (!u.endsWith("/vclaim-sisrute-rest/api/v1.0")) {
            return deriveSisruteUrlFromBpjsUrl(u, env);
        }
        return u;
    }

    private boolean isDev(String env) {
        return env == null || env.trim().isEmpty()
                || env.equalsIgnoreCase("dev")
                || env.equalsIgnoreCase("development")
                || env.equalsIgnoreCase("uat");
    }

    // =================================================================
    //  HEADER & HTTP CLIENT
    // =================================================================

    /** Bangun header standar Sisrute: Cons-ID, Timestamp, Signature, user_key. */
    private HttpHeaders buildHeaders() {
        utc = String.valueOf(api.GetUTCdatetimeAsString());
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add("X-Cons-ID", consId);
        h.add("X-Timestamp", utc);
        h.add("X-Signature", getHmacSisrute(utc));
        h.add("user_key", userKey);
        return h;
    }

    /** Membuat signature BPJS memakai consId & secretKey khusus SISRUTE. */
    private String getHmacSisrute(String timestamp) {
        try {
            String data = consId + "&" + timestamp;
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("[SisruteService] Gagal membuat signature: " + e);
            return "";
        }
    }

    /**
     * RestTemplate yang support DELETE dengan body
     * (DELETE standar Spring tidak boleh punya body, padahal Sisrute butuh).
     */
    private RestTemplate getRestWithDeleteBody() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] trustManagers = {
            new X509TrustManager() {
                @Override public X509Certificate[] getAcceptedIssuers() { return null; }
                @Override public void checkServerTrusted(X509Certificate[] a, String b) throws CertificateException {}
                @Override public void checkClientTrusted(X509Certificate[] a, String b) throws CertificateException {}
            }
        };
        sslContext.init(null, trustManagers, new SecureRandom());
        SSLSocketFactory sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme scheme = new Scheme("https", 443, sslFactory);

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory() {
            @Override
            protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
                if (HttpMethod.DELETE == httpMethod) {
                    return new HttpEntityEnclosingDeleteRequest(uri);
                }
                return super.createHttpUriRequest(httpMethod, uri);
            }
        };
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        RestTemplate rt = new RestTemplate();
        rt.setRequestFactory(factory);
        return rt;
    }

    /** Custom request class untuk DELETE-with-body. */
    public static class HttpEntityEnclosingDeleteRequest extends HttpEntityEnclosingRequestBase {
        public HttpEntityEnclosingDeleteRequest(final URI uri) {
            super();
            setURI(uri);
        }
        @Override
        public String getMethod() {
            return "DELETE";
        }
    }

    // =================================================================
    //  ENDPOINT 1 : GET KRITERIA RUJUKAN
    //  POST /Rujukan/GetKriteriaRujukan
    // =================================================================

    /**
     * Ambil daftar kriteria rujukan yang harus dijawab user.
     *
     * @param kodeFaskesSatuSehat kode faskes asal (Satu Sehat)
     * @param kodeDiagnosa        ICD-10 (contoh: "I10")
     * @param encounterReference  reference encounter aktif, contoh: "Encounter/9f476605-..."
     *                            Boleh kirim UUID saja, akan auto-prefix.
     * @return JsonNode response body. Cek metaData.code == 200 untuk sukses.
     *         Field response.kriteriaRujukan[] berisi pertanyaan untuk dialog.
     *         Field response.jejaringWilayahRujukan[] berisi daftar provinsi.
     * @throws Exception kalau gagal koneksi / parsing
     */
    public JsonNode getKriteriaRujukan(String kodeFaskesSatuSehat,
                                       String kodeDiagnosa,
                                       String encounterReference) throws Exception {
        String url = baseUrl + "/Rujukan/GetKriteriaRujukan";
        String encounterRef = normalizeEncounterRef(encounterReference);
        String kodeFaskes = pilihKodeFaskes(kodeFaskesSatuSehat);

        String body = "{"
                + "\"kodeFaskesSatuSehat\":\"" + kodeFaskes + "\","
                + "\"kodeDiagnosa\":\"" + safe(kodeDiagnosa) + "\","
                + "\"encounter\":{"
                +   "\"reference\":\"" + encounterRef + "\""
                + "}"
                + "}";

        return doPost(url, body);
    }

    // =================================================================
    //  ENDPOINT 2 : GET FASKES RUJUKAN
    //  POST /Rujukan/GetFaskesRujukan
    // =================================================================

    /**
     * Cari daftar faskes tujuan rujukan.
     *
     * @param kodeFaskesSatuSehat   kode faskes asal Satu Sehat
     * @param kodeDiagnosa          ICD-10
     * @param kodeSpesialis         kode sub-spesialis (3 digit, contoh: "095")
     * @param tglRencanaKunjungan   format: "yyyy-MM-dd" (atau "dd-MM-yyyy" sesuai docs)
     * @param kriteriaJsonItem      JSON string PERSIS sesuai format docs untuk
     *                              field "kriteriaRujukan.item"
     *                              Contoh:
     *                              [{"linkId":"3216","text":"Terapi/Pengobatan",
     *                                "answer":[{"valueBoolean":false}]}, ...]
     * @param kodePropinsi          kode provinsi jejaring tujuan (contoh: "31")
     * @param namaPropinsi          nama provinsi (contoh: "DKI Jakarta")
     * @param kodeKabupaten         kode kabupaten (boleh "")
     * @param namaKabupaten         nama kabupaten (boleh "")
     * @param encounterReference    reference encounter aktif
     */
    public JsonNode getFaskesRujukan(String kodeFaskesSatuSehat,
                                     String kodeDiagnosa,
                                     String kodeSpesialis,
                                     String tglRencanaKunjungan,
                                     String kriteriaJsonItem,
                                     String kodePropinsi,
                                     String namaPropinsi,
                                     String kodeKabupaten,
                                     String namaKabupaten,
                                     String encounterReference) throws Exception {
        String url = baseUrl + "/Rujukan/GetFaskesRujukan";
         //String url = baseUrl + "/Sisrute/GetFaskesRujukan";
        String encounterRef = normalizeEncounterRef(encounterReference);
        String kodeFaskes = pilihKodeFaskes(kodeFaskesSatuSehat);

        String body = "{"
                + "\"kodeFaskesSatuSehat\":\"" + kodeFaskes + "\","
                + "\"kodeDiagnosa\":\"" + safe(kodeDiagnosa) + "\","
                + "\"kodeSpesialis\":\"" + safe(kodeSpesialis) + "\","
                + "\"tglRencanaKunjungan\":\"" + safe(tglRencanaKunjungan) + "\","
                + "\"kriteriaRujukan\":{"
                +   "\"item\":" + (kriteriaJsonItem == null || kriteriaJsonItem.trim().isEmpty()
                                    ? "[]" : kriteriaJsonItem)
                + "},"
                + "\"codeJejaringWilayah\":{"
                +   "\"kodePropinsi\":\""  + safe(kodePropinsi)  + "\","
                +   "\"namaPropinsi\":\""  + safe(namaPropinsi)  + "\","
                +   "\"kodeKabupaten\":\"" + safe(kodeKabupaten) + "\","
                +   "\"namaKabupaten\":\"" + safe(namaKabupaten) + "\""
                + "},"
                + "\"encounter\":{"
                +   "\"reference\":\"" + encounterRef + "\""
                + "}"
                + "}";

        return doPost(url, body);
    }

    // =================================================================
    //  ENDPOINT 3 : INSERT RUJUKAN (POST KUNJUNGAN)
    //  POST /Rujukan/Insert
    // =================================================================

    /**
     * Kirim data rujukan lengkap ke Sisrute.
     * Karena field-nya banyak, dibungkus dalam class InsertRujukanRequest.
     */
    public JsonNode insertRujukan(InsertRujukanRequest r) throws Exception {
        String url = baseUrl + "/Rujukan/Insert";

        String body = "{"
            + "\"request\":{"
            +   "\"t_rujukan\":{"
            +     "\"noSep\":\""              + safe(r.noSep) + "\","
            +     "\"tglRujukan\":\""         + safe(r.tglRujukan) + "\","
            +     "\"tglRencanaKunjungan\":\""+ safe(r.tglRencanaKunjungan) + "\","
            +     "\"ppkDirujuk\":\""         + safe(r.ppkDirujuk) + "\","
            +     "\"jnsPelayanan\":\""       + safe(r.jnsPelayanan) + "\","
            +     "\"catatan\":\""            + escape(r.catatan) + "\","
            +     "\"diagRujukan\":\""        + safe(r.diagRujukan) + "\","
            +     "\"tipeRujukan\":\""        + safe(r.tipeRujukan) + "\","
            +     "\"poliRujukan\":\""        + safe(r.poliRujukan) + "\","
            +     "\"user\":\""               + safe(r.user) + "\","
            +     "\"satuSehatRujukan\":{"
            +       "\"kodeFaskesSatuSehat\":\""           + pilihKodeFaskes(r.kodeFaskesSatuSehat) + "\","
            +       "\"idPasienSatuSehat\":\""             + safe(r.idPasienSatuSehat) + "\","
            +       "\"kdppkSatuSehatTujuanRujukan\":\""   + safe(r.kdppkSatuSehatTujuanRujukan) + "\","
            +       "\"kdDokterSatuSehat\":\""             + safe(r.kdDokterSatuSehat) + "\","
            +       "\"encounter\":{"
            +         "\"reference\":\""                   + safe(r.encounterReference) + "\""
            +       "},"
            +       "\"patientInstruction\":\""            + escape(r.patientInstruction) + "\","
            +       "\"kriteriaRujukan\":{"
            +         "\"item\":" + (r.kriteriaJsonItem == null || r.kriteriaJsonItem.trim().isEmpty()
                                       ? "[]" : r.kriteriaJsonItem)
            +       "},"
            +       "\"keteranganRujukan\":\""             + escape(r.keteranganRujukan) + "\","
            +       "\"codeJejaringWilayah\":{"
            +         "\"kodePropinsi\":\""  + safe(r.kodePropinsi)  + "\","
            +         "\"namaPropinsi\":\""  + safe(r.namaPropinsi)  + "\","
            +         "\"kodeKabupaten\":\"" + safe(r.kodeKabupaten) + "\","
            +         "\"namaKabupaten\":\"" + safe(r.namaKabupaten) + "\""
            +       "}"
            +     "}"
            +   "}"
            + "}"
            + "}";

        return doPost(url, body);
    }

    /** DTO untuk Insert Rujukan supaya parameter tidak terlalu panjang. */
    public static class InsertRujukanRequest {
        // Data rujukan utama (sudah ada di Khanza)
        public String noSep;
        public String tglRujukan;            // yyyy-MM-dd
        public String tglRencanaKunjungan;   // yyyy-MM-dd
        public String ppkDirujuk;            // kode faskes BPJS
        public String jnsPelayanan;          // 1=Ranap, 2=Ralan
        public String catatan;
        public String diagRujukan;           // ICD-10
        public String tipeRujukan;           // 0=Penuh, 1=Partial, 2=Rujuk Balik
        public String poliRujukan;           // kode poli BPJS
        public String user;

        // Data Satu Sehat
        public String kodeFaskesSatuSehat;
        public String idPasienSatuSehat;
        public String kdppkSatuSehatTujuanRujukan;
        public String kdDokterSatuSehat;
        public String encounterReference;
        public String patientInstruction;
        public String kriteriaJsonItem;      // JSON array string
        public String keteranganRujukan;

        // Wilayah jejaring
        public String kodePropinsi;
        public String namaPropinsi;
        public String kodeKabupaten;
        public String namaKabupaten;
    }

    // =================================================================
    //  ENDPOINT 4 : DELETE RUJUKAN
    //  DELETE /Rujukan/deleteKunjungan
    // =================================================================

    /**
     * Hapus rujukan yang sudah dikirim ke Sisrute.
     */
    public JsonNode deleteRujukan(String noRujukan,
                                  String user,
                                  String kodeFaskesSatuSehat,
                                  String idPasienSatuSehat,
                                  String kdppkSatuSehatTujuanRujukan,
                                  String kdDokterSatuSehat,
                                  String encounterReference,
                                  String patientInstruction,
                                  String keteranganRujukan) throws Exception {
        String url = baseUrl + "/Rujukan/Delete";
        //String encounterRef = normalizeEncounterRef(encounterReference);

        String body = "{"
                + "\"request\":{"
                +   "\"t_rujukan\":{"
                +     "\"noRujukan\":\"" + safe(noRujukan) + "\","
                +     "\"user\":\""      + safe(user) + "\","
                +     "\"satuSehatRujukan\":{"
                +       "\"kodeFaskesSatuSehat\":\""         + pilihKodeFaskes(kodeFaskesSatuSehat) + "\","
                +       "\"idPasienSatuSehat\":\""           + safe(idPasienSatuSehat) + "\","
                +       "\"kdppkSatuSehatTujuanRujukan\":\"" + safe(kdppkSatuSehatTujuanRujukan) + "\","
                +       "\"kdDokterSatuSehat\":\""           + safe(kdDokterSatuSehat) + "\","
                +       "\"encounter\":{"
                +         "\"reference\":\"" + encounterReference + "\""
                +       "},"
                +       "\"patientInstruction\":\"" + escape(patientInstruction) + "\","
                +       "\"keteranganRujukan\":\""  + escape(keteranganRujukan)  + "\""
                +     "}"
                +   "}"
                + "}"
                + "}";

        return doDelete(url, body);
    }

    // =================================================================
    //  HTTP CALLERS
    // =================================================================

    private JsonNode doPost(String url, String body) throws Exception {
        headers = buildHeaders();
        requestEntity = new HttpEntity<>(body, headers);

        System.out.println("==== Sisrute POST ====");
        System.out.println("URL : " + url);
        System.out.println("Body: " + body);

        try {
            responseBody = api.getRest()
                    .exchange(url, HttpMethod.POST, requestEntity, String.class)
                    .getBody();

            responseBody = normalizeResponseBody(responseBody);
            System.out.println("Response: " + responseBody);
            root = processBpjsResponse(responseBody);
            return root;
        } catch (HttpStatusCodeException e) {
            System.out.println("==== Sisrute HTTP ERROR ==== ");
            System.out.println("Status : " + e.getStatusCode());
            System.out.println("Body   : " + e.getResponseBodyAsString());
            throw e;
        }
    }

    private JsonNode doDelete(String url, String body) throws Exception {
        headers = buildHeaders();
        HttpEntity<String> req = new HttpEntity<>(body, headers);

        System.out.println("==== Sisrute DELETE ====");
        System.out.println("URL : " + url);
        System.out.println("Body: " + body);

        try {
            responseBody = getRestWithDeleteBody()
                    .exchange(url, HttpMethod.DELETE, req, String.class)
                    .getBody();

            responseBody = normalizeResponseBody(responseBody);
            System.out.println("Response: " + responseBody);
            root = processBpjsResponse(responseBody);
            return root;
        } catch (HttpStatusCodeException e) {
            System.out.println("==== Sisrute HTTP ERROR ==== ");
            System.out.println("Status : " + e.getStatusCode());
            System.out.println("Body   : " + e.getResponseBodyAsString());
            throw e;
        }
    }


    // =================================================================
    //  RESPONSE NORMALIZER + DECRYPT BPJS
    // =================================================================

    /**
     * Beberapa response BPJS-Sisrute DEV kadang terbaca sebagai UTF-16 endian terbalik,
     * sehingga JSON muncul seperti: 笀∀洀攀琀愀䐀愀琀愀...
     * Method ini mengembalikan string JSON normal sebelum diparse Jackson.
     */
    private String normalizeResponseBody(String body) {
        if (body == null || body.length() == 0) {
            return body;
        }

        char first = body.charAt(0);
        if (first == 0x7B00 || first == 0x5B00 || body.startsWith("笀")) {
            try {
                byte[] raw = new byte[body.length() * 2];
                for (int i = 0; i < body.length(); i++) {
                    char c = body.charAt(i);
                    raw[i * 2] = (byte) ((c >> 8) & 0xFF);
                    raw[(i * 2) + 1] = (byte) (c & 0xFF);
                }
                return new String(raw, "UTF-16LE").trim();
            } catch (Exception e) {
                System.out.println("[SisruteService] Gagal normalisasi response UTF-16LE: " + e);
                return body;
            }
        }
        return body.trim();
    }

    /**
     * Parse response BPJS-Sisrute. Bila field response masih terenkripsi seperti VClaim,
     * otomatis decrypt + decompress, lalu mengganti field response menjadi JSON asli.
     */
    private JsonNode processBpjsResponse(String body) throws Exception {
        JsonNode parsed = mapper.readTree(body);

        if (parsed.has("response") && parsed.path("response").isTextual()) {
            String encrypted = parsed.path("response").asText();
            if (!isBlank(encrypted) && looksEncrypted(encrypted)) {
                try {
                    String decrypted = decryptBpjsResponse(encrypted);
                    if (!isBlank(decrypted)) {
                        System.out.println("Response Decrypted: " + shortLog(decrypted));
                        if (!looksJsonText(decrypted)) {
                            throw new IllegalStateException("Response BPJS berhasil di-decrypt, tetapi hasil decompress belum berbentuk JSON. Prefix: " + shortLog(decrypted));
                        }
                        JsonNode decryptedNode = mapper.readTree(decrypted);
                        ObjectNode obj = (ObjectNode) parsed.deepCopy();
                        obj.set("response", decryptedNode);
                        return obj;
                    }
                } catch (Exception e) {
                    System.out.println("[SisruteService] Gagal decrypt response BPJS-Sisrute: " + e);
                    throw e;
                }
            }
        }

        return parsed;
    }

    private boolean looksEncrypted(String s) {
        if (s == null) return false;
        String v = s.trim();
        // Response terenkripsi BPJS biasanya base64 panjang dan bukan JSON langsung.
        return v.length() > 40 && !v.startsWith("{") && !v.startsWith("[");
    }

    private String shortLog(String s) {
        if (s == null) return "";
        String v = s.replace('\r', ' ').replace('\n', ' ').trim();
        return v.length() > 1200 ? v.substring(0, 1200) + " ...[dipotong]" : v;
    }

    /** Decrypt response BPJS VClaim/Sisrute: AES-CBC + LZString.
     *
     * Patch v9:
     * - Tidak lagi mengandalkan LZString lokal dulu.
     * - Memakai ApiBPJSEnc + ApiBPJSLZString bawaan Khanza yang sudah terbukti
     *   dipakai ApiBPJS.Decrypt() untuk response VClaim.
     * - Tetap memakai key SISRUTE: consId + secretKey + timestamp.
     */
    private String decryptBpjsResponse(String encryptedResponse) throws Exception {
        String key = safe(consId) + safe(secretKey) + safe(utc);

        // AES decrypt memakai helper bawaan Khanza agar sama persis dengan ApiBPJS.Decrypt().
        ApiBPJSAesKeySpec aesKey = ApiBPJSEnc.generateKey(key);
        String compressed = ApiBPJSEnc.decrypt(encryptedResponse, aesKey.getKey(), aesKey.getIv());
        if (compressed == null) {
            return "";
        }
        compressed = compressed.trim();

        // Mayoritas response BPJS memakai LZString.decompressFromEncodedURIComponent().
        String decompressed = tryDecompressKhanza(compressed);
        if (looksJsonText(decompressed)) {
            return decompressed.trim();
        }

        // Fallback: beberapa gateway kadang mengembalikan bentuk base64-compressed.
        String decompressedBase64 = tryDecompressKhanzaBase64(compressed);
        if (looksJsonText(decompressedBase64)) {
            return decompressedBase64.trim();
        }

        // Fallback terakhir ke LZString lokal dari patch sebelumnya.
        String decompressedLocal = null;
        try {
            decompressedLocal = LZString.decompressFromEncodedURIComponent(compressed);
        } catch (Exception e) {
            System.out.println("[SisruteService] LZString lokal gagal: " + e);
        }
        if (looksJsonText(decompressedLocal)) {
            return decompressedLocal.trim();
        }

        // Kembalikan compressed agar caller bisa menampilkan prefix error yang jelas.
        return compressed;
    }

    private String tryDecompressKhanza(String compressed) {
        try {
            String out = ApiBPJSLZString.decompressFromEncodedURIComponent(compressed);
            return out == null ? "" : out;
        } catch (Exception e) {
            System.out.println("[SisruteService] ApiBPJSLZString URI gagal: " + e);
            return "";
        }
    }

    private String tryDecompressKhanzaBase64(String compressed) {
        try {
            String out = ApiBPJSLZString.decompressFromBase64(compressed);
            return out == null ? "" : out;
        } catch (Exception e) {
            System.out.println("[SisruteService] ApiBPJSLZString Base64 gagal: " + e);
            return "";
        }
    }

    private boolean looksJsonText(String s) {
        if (s == null) return false;
        String v = s.trim();
        return v.startsWith("{") || v.startsWith("[");
    }

    /** Minimal LZ-String decompressFromEncodedURIComponent, dibuat lokal agar tidak tergantung library tambahan. */
    private static class LZString {
        private static final String KEY_STR_URI_SAFE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$";
        private static final Map<Character, Integer> BASE_REVERSE_DIC = new HashMap<Character, Integer>();

        static {
            for (int i = 0; i < KEY_STR_URI_SAFE.length(); i++) {
                BASE_REVERSE_DIC.put(KEY_STR_URI_SAFE.charAt(i), i);
            }
        }

        private interface GetNextValue {
            int get(int index);
        }

        private static class Data {
            int val;
            int position;
            int index;
        }

        public static String decompressFromEncodedURIComponent(String input) {
            if (input == null) return null;
            if (input.length() == 0) return "";
            final String data = input.replace(' ', '+');
            return decompress(data.length(), 32, new GetNextValue() {
                @Override
                public int get(int index) {
                    Character c = data.charAt(index);
                    Integer val = BASE_REVERSE_DIC.get(c);
                    return val == null ? 0 : val;
                }
            });
        }

        private static String decompress(int length, int resetValue, GetNextValue getNextValue) {
            List<String> dictionary = new ArrayList<String>();
            int next;
            int enlargeIn = 4;
            int dictSize = 4;
            int numBits = 3;
            String entry;
            StringBuilder result = new StringBuilder();
            int i;
            String w;
            int bits, resb, maxpower, power;
            String c;
            Data data = new Data();
            data.val = getNextValue.get(0);
            data.position = resetValue;
            data.index = 1;

            for (i = 0; i < 3; i++) {
                dictionary.add(String.valueOf((char) i));
            }

            bits = 0;
            maxpower = 2;
            power = 1;
            while (power != maxpower) {
                resb = data.val & data.position;
                data.position >>= 1;
                if (data.position == 0) {
                    data.position = resetValue;
                    data.val = getNextValue.get(data.index++);
                }
                bits |= (resb > 0 ? 1 : 0) * power;
                power <<= 1;
            }

            next = bits;
            switch (next) {
                case 0:
                    bits = 0;
                    maxpower = 256;
                    power = 1;
                    while (power != maxpower) {
                        resb = data.val & data.position;
                        data.position >>= 1;
                        if (data.position == 0) {
                            data.position = resetValue;
                            data.val = getNextValue.get(data.index++);
                        }
                        bits |= (resb > 0 ? 1 : 0) * power;
                        power <<= 1;
                    }
                    c = String.valueOf((char) bits);
                    break;
                case 1:
                    bits = 0;
                    maxpower = 65536;
                    power = 1;
                    while (power != maxpower) {
                        resb = data.val & data.position;
                        data.position >>= 1;
                        if (data.position == 0) {
                            data.position = resetValue;
                            data.val = getNextValue.get(data.index++);
                        }
                        bits |= (resb > 0 ? 1 : 0) * power;
                        power <<= 1;
                    }
                    c = String.valueOf((char) bits);
                    break;
                case 2:
                    return "";
                default:
                    return null;
            }

            dictionary.add(c);
            w = c;
            result.append(c);

            while (true) {
                if (data.index > length) {
                    return "";
                }

                bits = 0;
                maxpower = 1 << numBits;
                power = 1;
                while (power != maxpower) {
                    resb = data.val & data.position;
                    data.position >>= 1;
                    if (data.position == 0) {
                        data.position = resetValue;
                        data.val = getNextValue.get(data.index++);
                    }
                    bits |= (resb > 0 ? 1 : 0) * power;
                    power <<= 1;
                }

                int cc = bits;
                switch (cc) {
                    case 0:
                        bits = 0;
                        maxpower = 256;
                        power = 1;
                        while (power != maxpower) {
                            resb = data.val & data.position;
                            data.position >>= 1;
                            if (data.position == 0) {
                                data.position = resetValue;
                                data.val = getNextValue.get(data.index++);
                            }
                            bits |= (resb > 0 ? 1 : 0) * power;
                            power <<= 1;
                        }
                        dictionary.add(String.valueOf((char) bits));
                        cc = dictSize++;
                        enlargeIn--;
                        break;
                    case 1:
                        bits = 0;
                        maxpower = 65536;
                        power = 1;
                        while (power != maxpower) {
                            resb = data.val & data.position;
                            data.position >>= 1;
                            if (data.position == 0) {
                                data.position = resetValue;
                                data.val = getNextValue.get(data.index++);
                            }
                            bits |= (resb > 0 ? 1 : 0) * power;
                            power <<= 1;
                        }
                        dictionary.add(String.valueOf((char) bits));
                        cc = dictSize++;
                        enlargeIn--;
                        break;
                    case 2:
                        return result.toString();
                }

                if (enlargeIn == 0) {
                    enlargeIn = 1 << numBits;
                    numBits++;
                }

                if (cc < dictionary.size() && dictionary.get(cc) != null) {
                    entry = dictionary.get(cc);
                } else {
                    if (cc == dictSize) {
                        entry = w + w.charAt(0);
                    } else {
                        return null;
                    }
                }
                result.append(entry);

                dictionary.add(w + entry.charAt(0));
                dictSize++;
                enlargeIn--;
                w = entry;

                if (enlargeIn == 0) {
                    enlargeIn = 1 << numBits;
                    numBits++;
                }
            }
        }
    }

    // =================================================================
    //  UTILITY
    // =================================================================

    /**
     * Pilih kode faskes Satu Sehat yang benar untuk dikirim ke BPJS-Sisrute.
     *
     * Catatan penting:
     * - Nilai ini dipakai BPJS untuk membuat reference FHIR: Organization/{kode}.
     * - Jadi isinya harus ID Organization Satu Sehat/IHS faskes, misalnya 100028359.
     * - Jangan isi dengan kode wilayah/kemkes pendek seperti 7371052, karena SATUSEHAT
     *   akan menolak dengan pesan: Wrong reference ID format: Organization/7371052.
     *
     * Urutan prioritas:
     * 1. KODEFASKESSATUSEHATRUJUKAN, jika formatnya terlihat seperti Org ID Satu Sehat.
     * 2. kode dari form, biasanya dari IDSATUSEHAT().
     * 3. IDSATUSEHAT(), sebagai fallback tambahan.
     * 4. Kalau semuanya tidak ideal, tetap kirim nilai yang ada supaya pesan error API terlihat.
     */
    private String pilihKodeFaskes(String kodeDariForm) {
        String dariConfig = safe(kodeFaskesSatuSehatRujukan);
        String dariForm = safe(kodeDariForm);
        String dariIdSatusehat = safe(idSatuSehatOrg);

        if (!isBlank(dariConfig) && isLikelySatuSehatOrgId(dariConfig)) {
            return dariConfig;
        }
        if (!isBlank(dariConfig) && !isLikelySatuSehatOrgId(dariConfig)) {
            System.out.println("[SisruteService] WARNING: KODEFASKESSATUSEHATRUJUKAN='" + dariConfig + "' terlihat bukan Organization ID Satu Sehat. Nilai ini dilewati.");
        }

        if (!isBlank(dariForm) && isLikelySatuSehatOrgId(dariForm)) {
            return dariForm;
        }

        if (!isBlank(dariIdSatusehat) && isLikelySatuSehatOrgId(dariIdSatusehat)) {
            return dariIdSatusehat;
        }

        // Fallback terakhir: jangan kosong, agar response API tetap memberi pesan diagnostik.
        if (!isBlank(dariConfig)) return dariConfig;
        if (!isBlank(dariForm)) return dariForm;
        return dariIdSatusehat;
    }

    /**
     * Validasi ringan untuk menghindari kode wilayah/faskes pendek yang sering keliru.
     * Contoh valid dari Postman/playbook umumnya: 10000005, 100010939, 100028359.
     * Jika nanti Org ID SATUSEHAT di tempat bro berbeda pola, cukup isi KODEFASKESSATUSEHATRUJUKAN
     * dengan Org ID yang benar dan sesuaikan validasi ini.
     */
    private boolean isLikelySatuSehatOrgId(String s) {
        if (isBlank(s)) return false;
        String v = s.trim();
        if (v.startsWith("Organization/")) {
            v = v.substring("Organization/".length());
        }
        // UUID juga valid sebagai FHIR id, walau contoh rujukan BPJS umumnya numeric 100xxxxxx.
        if (v.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            return true;
        }
        // Hindari kode wilayah/kemkes pendek seperti 7371052.
        return v.matches("100[0-9]{5,12}");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Tambahkan prefix "Encounter/" jika user kirim UUID polos. */
    private String normalizeEncounterRef(String ref) {
        if (ref == null) return "";
        String t = ref.trim();
        if (t.isEmpty()) return "";
        if (t.toLowerCase().startsWith("encounter/")) return t;
        return "Encounter/" + t;
    }
//    private String normalizeEncounterRef(String ref) {
//    if (ref == null) return "";
//
//    String t = ref.trim();
//
//    if (t.isEmpty()) return "";
//
//    if (t.toLowerCase().startsWith("encounter/")) {
//        t = t.substring(t.indexOf('/') + 1);
//    }
//
//    return t;
//}
    
    

    /** Null-safe trim. */
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /** Escape karakter khusus JSON untuk konten teks bebas. */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }

    /** Helper cepat untuk cek code metadata. */
    public static boolean isOk(JsonNode response) {
        if (response == null) return false;
        JsonNode meta = response.path("metaData");
        return "200".equals(meta.path("code").asText());
    }

    /** Helper cepat untuk ambil pesan metadata. */
    public static String getMessage(JsonNode response) {
        if (response == null) return "Response kosong";
        return response.path("metaData").path("message").asText();
    }

    // =================================================================
    //  MAIN - untuk tes manual
    // =================================================================

    /**
     * Tes manual. Pastikan koneksiDB.java sudah ditambah getter khusus BPJS SISRUTE:
     *   - URLAPIBPJSSISRUTE()
     *   - CONSIDAPIBPJSSISRUTE()
     *   - SECRETKEYAPIBPJSSISRUTE()
     *   - USERKEYAPIBPJSSISRUTE()
     *   - KODEFASKESSATUSEHATRUJUKAN()
     *   - KODEPPKBPJSSISRUTE()
     *   - ENVBPJSSISRUTE()
     *
     * Cara pakai:
     *   1. Aktifkan blok tes yang diinginkan (uncomment).
     *   2. Run as Java Application.
     */
    public static void main(String[] args) {
        SisruteService svc = new SisruteService();

        try {
            // ===== TES 1: GET KRITERIA RUJUKAN =====
            System.out.println("\n>>> TES GetKriteriaRujukan");
            JsonNode r1 = svc.getKriteriaRujukan(
                    "100005239",                                       // kodeFaskesSatuSehat
                    "I10",                                             // kodeDiagnosa
                    "Encounter/9f476605-d133-4cc6-bcac-626e2ecacded"   // encounter ref
            );
            System.out.println("OK? " + isOk(r1) + " | Msg: " + getMessage(r1));

            // ===== TES 2: GET FASKES RUJUKAN =====
            // Aktifkan setelah TES 1 sukses dan punya kriteria yang bisa dijawab.
            /*
            System.out.println("\n>>> TES GetFaskesRujukan");
            String kriteriaItem =
                "[" +
                "  {\"linkId\":\"3216\",\"text\":\"Terapy/Pengobatan\",\"answer\":[{\"valueBoolean\":false}]}," +
                "  {\"linkId\":\"3215\",\"text\":\"Tindakan Medis\",   \"answer\":[{\"valueString\":\"01.24\"}]}," +
                "  {\"linkId\":\"3214\",\"text\":\"Upaya Diagnosis\",  \"answer\":[{\"valueBoolean\":false}]}" +
                "]";
            JsonNode r2 = svc.getFaskesRujukan(
                    "100010951", "I10", "095",
                    "2026-03-01",
                    kriteriaItem,
                    "31", "DKI Jakarta", "", "",
                    "Encounter/9f476605-d133-4cc6-bcac-626e2ecacded"
            );
            System.out.println("OK? " + isOk(r2) + " | Msg: " + getMessage(r2));
            System.out.println("Jumlah faskes: " + r2.path("response").path("count").asInt());
            */

            // ===== TES 3: INSERT RUJUKAN =====
            /*
            System.out.println("\n>>> TES insertRujukan");
            InsertRujukanRequest req = new InsertRujukanRequest();
            req.noSep = "1001R0120126V000010";
            req.tglRujukan = "2026-03-03";
            req.tglRencanaKunjungan = "2026-03-03";
            req.ppkDirujuk = "0903R004";
            req.jnsPelayanan = "2";
            req.catatan = "tolong untuk dirujuk";
            req.diagRujukan = "I10";
            req.tipeRujukan = "0";
            req.poliRujukan = "005";
            req.user = "tester";
            req.kodeFaskesSatuSehat = "100010939";
            req.idPasienSatuSehat = "P20395452616";
            req.kdppkSatuSehatTujuanRujukan = "100025612";
            req.kdDokterSatuSehat = "10009880728";
            req.encounterReference = "897d7713-77cd-492e-bd1e-1fd8d7c2b33d";
            req.patientInstruction = "Rujukan ke RSUP HASAN SADIKIN";
            req.kriteriaJsonItem = kriteriaItem;
            req.keteranganRujukan = "Rujukan ke RSUP HASAN SADIKIN";
            req.kodePropinsi = "31";
            req.namaPropinsi = "DKI Jakarta";
            req.kodeKabupaten = "";
            req.namaKabupaten = "";
            JsonNode r3 = svc.insertRujukan(req);
            System.out.println("OK? " + isOk(r3) + " | Msg: " + getMessage(r3));
            System.out.println("noRujukan: " + r3.path("response").path("rujukan").path("noRujukan").asText());
            */

            // ===== TES 4: DELETE RUJUKAN =====
            /*
            System.out.println("\n>>> TES deleteRujukan");
            JsonNode r4 = svc.deleteRujukan(
                    "1001R0120326B000017", "tester",
                    "100010939", "P20395452616", "100025548", "10009880728",
                    "897d7713-77cd-492e-bd1e-1fd8d7c2b33d",
                    "Rujukan ke RSUP HASAN SADIKIN",
                    "Rujukan ke RSUP HASAN SADIKIN"
            );
            System.out.println("OK? " + isOk(r4) + " | Msg: " + getMessage(r4));
            */

        } catch (Exception ex) {
            System.out.println("Tes error: " + ex);
            ex.printStackTrace();
        }
    }
    
    private void loadConfigSisrute() {

    try {

        Properties prop = new Properties();
        prop.loadFromXML(new FileInputStream("setting/database.xml"));

        // NON ENCRYPT
        baseUrl =
                prop.getProperty("URLAPIBPJSSISRUTE", "").trim();

        envSisrute =
                prop.getProperty("ENVBPJSSISRUTE", "").trim();

        // ENCRYPT
        userKey =
                EnkripsiAES.decrypt(
                        prop.getProperty("USERKEYAPIBPJSSISRUTE", ""));

        consId =
                EnkripsiAES.decrypt(
                        prop.getProperty("CONSIDAPIBPJSSISRUTE", ""));

        secretKey =
                EnkripsiAES.decrypt(
                        prop.getProperty("SECRETKEYAPIBPJSSISRUTE", ""));

        kodeFaskesSatuSehatRujukan =
                EnkripsiAES.decrypt(
                        prop.getProperty("KODEFASKESSATUSEHATRUJUKAN", ""));

        kodePpkBpjsSisrute =
                EnkripsiAES.decrypt(
                        prop.getProperty("KODEPPKBPJSSISRUTE", ""));

        idSatuSehatOrg =
                EnkripsiAES.decrypt(
                        prop.getProperty("IDSATUSEHATRUJUKAN", ""));
        
        System.out.println("config dasar baseUrl: " + baseUrl);

    } catch (Exception e) {

        System.out.println(
                "[SisruteService] Gagal load config : " + e);

    }
}
}