package bridging;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

/**
 * BPJSSisruteApi
 *
 * Class awal untuk bridging BPJS Sisrute / VCLAIM-SISRUTE.
 *
 * Endpoint awal yang ditest:
 *   POST {URLAPIBPJSSISRUTE}/Rujukan/GetKriteriaRujukan
 *
 * Konfigurasi database.xml yang dibaca:
 *   URLAPIBPJSSISRUTE
 *   CONSIDAPIBPJSSISRUTE
 *   SECRETKEYAPIBPJSSISRUTE
 *   USERKEYAPIBPJSSISRUTE
 *   KODEFASKESSATUSEHATRUJUKAN
 *
 * Catatan:
 * - Jika CONSID/SECRET/USERKEY BPJSSISRUTE kosong, class ini mencoba fallback ke method
 *   fungsi.koneksiDB.CONSIDAPIBPJS(), SECRETKEYAPIBPJS(), USERKEYAPIBPJS().
 * - Fallback ini berguna agar tidak mengganggu konfigurasi VClaim lama.
 * - Untuk production, sebaiknya isi key BPJSSISRUTE sendiri sesuai credential Trustmark BPJS.
 */
public class BPJSSisruteApi {

    private static final String DEFAULT_URL_DEV = "https://apijkn-dev.bpjs-kesehatan.go.id/vclaim-sisrute-rest";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 30000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 60000;

    private final String baseUrl;
    private final String consId;
    private final String secretKey;
    private final String userKey;
    private final String kodeFaskesSatuSehatDefault;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public BPJSSisruteApi(
            String baseUrl,
            String consId,
            String secretKey,
            String userKey,
            String kodeFaskesSatuSehatDefault
    ) {
        this(baseUrl, consId, secretKey, userKey, kodeFaskesSatuSehatDefault,
                DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    public BPJSSisruteApi(
            String baseUrl,
            String consId,
            String secretKey,
            String userKey,
            String kodeFaskesSatuSehatDefault,
            int connectTimeoutMs,
            int readTimeoutMs
    ) {
        this.baseUrl = normalizeBaseUrl(firstNonBlank(baseUrl, DEFAULT_URL_DEV));
        this.consId = nullToEmpty(consId).trim();
        this.secretKey = nullToEmpty(secretKey).trim();
        this.userKey = nullToEmpty(userKey).trim();
        this.kodeFaskesSatuSehatDefault = nullToEmpty(kodeFaskesSatuSehatDefault).trim();
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * Membaca konfigurasi dari database.xml Khanza.
     *
     * Lokasi yang dicoba:
     * - System property: -Ddatabase.xml=/path/database.xml
     * - setting/database.xml
     * - database.xml
     * - src/setting/database.xml
     * - ../setting/database.xml
     */
    public static BPJSSisruteApi fromDatabaseXml() throws Exception {
        File xmlFile = findDatabaseXml();
        return fromDatabaseXml(xmlFile);
    }

    public static BPJSSisruteApi fromDatabaseXml(File xmlFile) throws Exception {
        if (xmlFile == null || !xmlFile.exists()) {
            throw new IllegalArgumentException("File database.xml tidak ditemukan.");
        }

        Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(xmlFile);
            prop.loadFromXML(fis);
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (Exception ignored) {}
            }
        }

        String baseUrl = firstNonBlank(
                prop.getProperty("URLAPIBPJSSISRUTE"),
                DEFAULT_URL_DEV
        );

        /*
         * Untuk credential:
         * 1) Ambil dari key khusus BPJSSISRUTE jika diisi.
         * 2) Jika kosong, coba ambil dari method fungsi.koneksiDB yang biasanya sudah decrypt.
         * 3) Jika masih kosong, ambil raw dari database.xml lama sebagai fallback terakhir.
         */
        String consId = firstNonBlank(
                prop.getProperty("CONSIDAPIBPJSSISRUTE"),
                invokeKoneksiDB("CONSIDAPIBPJS"),
                prop.getProperty("CONSIDAPIBPJS")
        );

        String secretKey = firstNonBlank(
                prop.getProperty("SECRETKEYAPIBPJSSISRUTE"),
                invokeKoneksiDB("SECRETKEYAPIBPJS"),
                prop.getProperty("SECRETKEYAPIBPJS")
        );

        String userKey = firstNonBlank(
                prop.getProperty("USERKEYAPIBPJSSISRUTE"),
                invokeKoneksiDB("USERKEYAPIBPJS"),
                prop.getProperty("USERKEYAPIBPJS")
        );

        String kodeFaskesSatuSehat = firstNonBlank(
                prop.getProperty("KODEFASKESSATUSEHATRUJUKAN"),
                ""
        );

        return new BPJSSisruteApi(baseUrl, consId, secretKey, userKey, kodeFaskesSatuSehat);
    }

    /**
     * Test endpoint awal:
     * POST /Rujukan/GetKriteriaRujukan
     *
     * Payload dibuat dalam bentuk:
     * {
     *   "request": {
     *     "kodeDiagnosa": "I10",
     *     "kodeFaskesSatuSehat": "..."
     *   }
     * }
     */
    public ApiResponse getKriteriaRujukan(String kodeDiagnosa) throws Exception {
        return getKriteriaRujukan(kodeDiagnosa, this.kodeFaskesSatuSehatDefault);
    }

    public ApiResponse getKriteriaRujukan(String kodeDiagnosa, String kodeFaskesSatuSehat) throws Exception {
        String diagnosa = nullToEmpty(kodeDiagnosa).trim();
        String faskes = firstNonBlank(kodeFaskesSatuSehat, this.kodeFaskesSatuSehatDefault);

        if (diagnosa.length() == 0) {
            throw new IllegalArgumentException("Kode diagnosa wajib diisi, contoh: I10");
        }
        if (faskes.length() == 0) {
            throw new IllegalArgumentException("Kode Faskes Satu Sehat wajib diisi. Isi KODEFASKESSATUSEHATRUJUKAN di database.xml atau kirim sebagai parameter.");
        }

        validateCredential();

        String body = "{"
                + "\"request\":{"
                + "\"kodeDiagnosa\":\"" + escapeJson(diagnosa) + "\","
                + "\"kodeFaskesSatuSehat\":\"" + escapeJson(faskes) + "\""
                + "}"
                + "}";

        ApiResponse response = postJson("/Rujukan/GetKriteriaRujukan", body);

        /*
         * Jika ternyata server meminta GET, method alternatif bisa dicoba manual dari form/test:
         * getKriteriaRujukanAsGet(kodeDiagnosa, kodeFaskesSatuSehat)
         */
        return response;
    }

    /**
     * Alternatif jika Postman resmi BPJS menunjukkan metode GET dengan query parameter.
     * Tidak dipakai default agar tidak mengubah alur utama.
     */
    public ApiResponse getKriteriaRujukanAsGet(String kodeDiagnosa, String kodeFaskesSatuSehat) throws Exception {
        String diagnosa = nullToEmpty(kodeDiagnosa).trim();
        String faskes = firstNonBlank(kodeFaskesSatuSehat, this.kodeFaskesSatuSehatDefault);

        if (diagnosa.length() == 0) {
            throw new IllegalArgumentException("Kode diagnosa wajib diisi, contoh: I10");
        }
        if (faskes.length() == 0) {
            throw new IllegalArgumentException("Kode Faskes Satu Sehat wajib diisi.");
        }

        validateCredential();

        String path = "/Rujukan/GetKriteriaRujukan"
                + "?kodeDiagnosa=" + urlEncode(diagnosa)
                + "&kodeFaskesSatuSehat=" + urlEncode(faskes);

        return request("GET", path, null);
    }

    public ApiResponse postJson(String path, String jsonBody) throws Exception {
        return request("POST", path, jsonBody);
    }

    private ApiResponse request(String method, String path, String body) throws Exception {
        String urlTarget = this.baseUrl + normalizePath(path);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String signature = generateSignature(this.consId, this.secretKey, timestamp);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlTarget);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(this.connectTimeoutMs);
            conn.setReadTimeout(this.readTimeoutMs);
            conn.setRequestMethod(method);
            conn.setDoInput(true);

            conn.setRequestProperty("X-cons-id", this.consId);
            conn.setRequestProperty("X-timestamp", timestamp);
            conn.setRequestProperty("X-signature", signature);
            conn.setRequestProperty("user_key", this.userKey);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");

            if (body != null && body.trim().length() > 0) {
                conn.setDoOutput(true);
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
                OutputStream os = null;
                try {
                    os = conn.getOutputStream();
                    os.write(bytes);
                    os.flush();
                } finally {
                    if (os != null) {
                        try { os.close(); } catch (Exception ignored) {}
                    }
                }
            }

            int statusCode = conn.getResponseCode();
            String responseBody = readResponseBody(conn, statusCode);

            return new ApiResponse(statusCode, responseBody, urlTarget, method, body);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void validateCredential() {
        StringBuilder sb = new StringBuilder();

        if (this.baseUrl.length() == 0) {
            sb.append("- URLAPIBPJSSISRUTE kosong\n");
        }
        if (this.consId.length() == 0) {
            sb.append("- CONSIDAPIBPJSSISRUTE/CONSIDAPIBPJS kosong\n");
        }
        if (this.secretKey.length() == 0) {
            sb.append("- SECRETKEYAPIBPJSSISRUTE/SECRETKEYAPIBPJS kosong\n");
        }
        if (this.userKey.length() == 0) {
            sb.append("- USERKEYAPIBPJSSISRUTE/USERKEYAPIBPJS kosong\n");
        }

        if (sb.length() > 0) {
            throw new IllegalStateException("Konfigurasi BPJS Sisrute belum lengkap:\n" + sb.toString());
        }
    }

    public static String generateSignature(String consId, String secretKey, String timestamp) throws Exception {
        String data = consId + "&" + timestamp;
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);
        byte[] hmacData = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacData);
    }

    private static String readResponseBody(HttpURLConnection conn, int statusCode) throws Exception {
        InputStream is = null;
        try {
            if (statusCode >= 200 && statusCode < 400) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
                if (is == null) {
                    is = conn.getInputStream();
                }
            }
            return readAll(is);
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception ignored) {}
            }
        }
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) {
            return "";
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (br != null) {
                try { br.close(); } catch (Exception ignored) {}
            }
        }
    }

    private static File findDatabaseXml() {
        String override = System.getProperty("database.xml");
        if (override != null && override.trim().length() > 0) {
            File f = new File(override.trim());
            if (f.exists()) {
                return f;
            }
        }

        String[] candidates = new String[] {
                "setting/database.xml",
                "database.xml",
                "src/setting/database.xml",
                "../setting/database.xml",
                "../../setting/database.xml"
        };

        for (String candidate : candidates) {
            File f = new File(candidate);
            if (f.exists()) {
                return f;
            }
        }

        return null;
    }

    /**
     * Coba panggil method static fungsi.koneksiDB agar credential lama yang terenkripsi
     * tetap bisa dibaca melalui mekanisme Khanza.
     */
    private static String invokeKoneksiDB(String methodName) {
        try {
            Class<?> cls = Class.forName("fungsi.koneksiDB");
            Method method = cls.getMethod(methodName);
            Object value = method.invoke(null);
            return value == null ? "" : String.valueOf(value).trim();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String normalizeBaseUrl(String value) {
        String result = nullToEmpty(value).trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String normalizePath(String value) {
        String result = nullToEmpty(value).trim();
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        return result;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && value.trim().length() > 0) {
                return value.trim();
            }
        }
        return "";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < 32) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    public String debugConfigMasked() {
        return "baseUrl=" + this.baseUrl
                + "\nconsId=" + mask(this.consId)
                + "\nsecretKey=" + mask(this.secretKey)
                + "\nuserKey=" + mask(this.userKey)
                + "\nkodeFaskesSatuSehatDefault=" + mask(this.kodeFaskesSatuSehatDefault);
    }

    private static String mask(String value) {
        String v = nullToEmpty(value);
        if (v.length() <= 4) {
            return v.length() == 0 ? "(kosong)" : "****";
        }
        return v.substring(0, 2) + "****" + v.substring(v.length() - 2);
    }

    /**
     * Test cepat dari command line:
     *
     * java -cp build/classes bridging.BPJSSisruteApi I10 100019399
     *
     * Argumen:
     * args[0] = kode diagnosa, default I10
     * args[1] = kode faskes Satu Sehat, optional jika sudah ada di database.xml
     */
    
//    public static void main(String[] args) {
//        try {
//            String kodeDiagnosa = args.length > 0 ? args[0] : "I10";
//            String kodeFaskes = args.length > 1 ? args[1] : "";
//
//            BPJSSisruteApi api = BPJSSisruteApi.fromDatabaseXml();
//
//            System.out.println("=== CONFIG BPJS SISRUTE ===");
//            System.out.println(api.debugConfigMasked());
//            System.out.println();
//
//            System.out.println("=== TEST GetKriteriaRujukan ===");
//            System.out.println("Kode Diagnosa: " + kodeDiagnosa);
//            System.out.println("Kode Faskes Satu Sehat: " + (kodeFaskes.trim().length() > 0 ? kodeFaskes : "(ambil dari database.xml)"));
//            System.out.println();
//
//            ApiResponse response = api.getKriteriaRujukan(kodeDiagnosa, kodeFaskes);
//
//            System.out.println("Method : " + response.method);
//            System.out.println("URL    : " + response.url);
//            System.out.println("HTTP   : " + response.statusCode);
//            System.out.println("Body   : ");
//            System.out.println(response.body);
//        } catch (Exception e) {
//            System.err.println("Gagal test BPJS Sisrute:");
//            e.printStackTrace();
//        }
//    }
    
public static void main(String[] args) {
    try {
        BPJSSisruteApi api = BPJSSisruteApi.fromDatabaseXml();

        String kodeDiagnosa = "I10";
        String kodeFaskesSatuSehat = "7371052";

        System.out.println("===== CONFIG YANG TERBACA =====");
        System.out.println(api.debugConfigMasked());
        System.out.println("===============================");

        BPJSSisruteApi.ApiResponse response =
                api.getKriteriaRujukan(kodeDiagnosa, kodeFaskesSatuSehat);

        System.out.println("===== RESPONSE GET KRITERIA RUJUKAN =====");
        System.out.println("HTTP Code : " + response.statusCode);
        System.out.println("URL       : " + response.url);
        System.out.println("Method    : " + response.method);
        System.out.println("Body      : ");
        System.out.println(response.body);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public static class ApiResponse {
        public final int statusCode;
        public final String body;
        public final String url;
        public final String method;
        public final String requestBody;

        public ApiResponse(int statusCode, String body, String url, String method, String requestBody) {
            this.statusCode = statusCode;
            this.body = body == null ? "" : body;
            this.url = url == null ? "" : url;
            this.method = method == null ? "" : method;
            this.requestBody = requestBody == null ? "" : requestBody;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        @Override
        public String toString() {
            return "ApiResponse{"
                    + "statusCode=" + statusCode
                    + ", method='" + method + '\''
                    + ", url='" + url + '\''
                    + ", body='" + body + '\''
                    + '}';
        }
    }
}
