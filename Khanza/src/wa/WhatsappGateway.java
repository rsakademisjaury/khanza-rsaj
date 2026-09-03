package wa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDBWA;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Gateway WhatsApp terpusat untuk SIMRS Khanza.
 *
 * Mode:
 * - FONNTE : selalu menggunakan Fonnte.
 * - GOWA   : selalu menggunakan Go WhatsApp Web Multidevice.
 * - AUTO   : Fonnte sebagai primary, GoWA sebagai fallback hanya jika kegagalan
 *            Fonnte sudah pasti. Pada status UNKNOWN fallback sengaja tidak
 *            dijalankan untuk mencegah pesan ganda.
 *
 * Konfigurasi dibaca setiap kali mengirim sehingga perubahan file konfigurasi
 * dapat diterapkan tanpa mengubah source. Lokasi default:
 *   setting/whatsapp-gateway.properties
 *
 * Nilai juga dapat dioverride melalui JVM system property / environment variable.
 */
public final class WhatsappGateway {

    private static final String FONNTE_URL = "https://api.fonnte.com/send";
    private static final String CONFIG_FILE = "setting/whatsapp-gateway.properties";
    private static final ObjectMapper JSON = new ObjectMapper();

    public enum Status {
        SUCCESS,
        FAILED,
        UNKNOWN
    }

    public static final class Hasil {
        private final Status status;
        private final String provider;
        private final boolean fallback;
        private final String messageId;
        private final String target;
        private final String pesan;
        private final String rawResponse;
        private final int httpCode;

        private Hasil(Status status, String provider, boolean fallback,
                String messageId, String target, String pesan,
                String rawResponse, int httpCode) {
            this.status = status;
            this.provider = provider == null ? "" : provider;
            this.fallback = fallback;
            this.messageId = messageId == null ? "" : messageId;
            this.target = target == null ? "" : target;
            this.pesan = pesan == null ? "" : pesan;
            this.rawResponse = rawResponse == null ? "" : rawResponse;
            this.httpCode = httpCode;
        }

        public boolean berhasil() {
            return status == Status.SUCCESS;
        }

        public boolean gagalPasti() {
            return status == Status.FAILED;
        }

        public boolean statusTidakPasti() {
            return status == Status.UNKNOWN;
        }

        public Status getStatus() {
            return status;
        }

        public String getProvider() {
            return provider;
        }

        public String getProviderLabel() {
            return fallback ? provider + " (Fallback)" : provider;
        }

        public boolean isFallback() {
            return fallback;
        }

        public String getMessageId() {
            if (!messageId.trim().equals("")) return messageId;
            return provider.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ENGLISH)
                    + "-" + System.currentTimeMillis();
        }

        public String getTarget() {
            return target;
        }

        public String getPesan() {
            return pesan;
        }

        public String getRawResponse() {
            return rawResponse;
        }

        public int getHttpCode() {
            return httpCode;
        }
    }

    private static final class Config {
        String provider;
        String gowaBaseUrl;
        String gowaUsername;
        String gowaPassword;
        String gowaDeviceId;
        int fonnteConnectTimeout;
        int fonnteReadTimeout;
        int gowaConnectTimeout;
        int gowaReadTimeout;
    }

    private WhatsappGateway() {
    }

    /** Pengiriman text standar. */
    public static Hasil kirimPesan(String noHp, String pesan) {
        return kirimPesan(noHp, pesan, noHp, new LinkedHashMap<String, String>());
    }

    /**
     * Pengiriman text dengan kompatibilitas parameter Fonnte lama.
     * targetFonnte dipertahankan agar format target dengan variabel pipe (|)
     * yang sudah digunakan source lama tidak berubah.
     */
    public static Hasil kirimPesan(String noHp, String pesan, String targetFonnte,
            Map<String, String> fieldFonnte) {
        Config cfg = bacaConfig();
        String mode = normalisasiMode(cfg.provider);
        String nomorGoWA = normalisasiNomorIndonesia(noHp);
        Map<String, String> extra = fieldFonnte == null
                ? new LinkedHashMap<String, String>() : fieldFonnte;

        if (mode.equals("GOWA")) {
            Hasil siap = cekGoWA(cfg);
            if (!siap.berhasil()) return siap;
            return kirimPesanGoWA(cfg, nomorGoWA, pesan, false);
        }

        Hasil fonnte = kirimPesanFonnte(cfg, targetFonnte, pesan, extra);
        if (mode.equals("FONNTE") || fonnte.berhasil()) {
            return fonnte;
        }

        // AUTO hanya fallback bila kegagalan primary sudah pasti.
        if (fonnte.statusTidakPasti()) {
            return hasil(Status.UNKNOWN, "Fonnte", false, fonnte.getMessageId(),
                    fonnte.getTarget(),
                    gabungPesan(fonnte.getPesan(),
                            "GoWA tidak dijalankan otomatis untuk mencegah pesan terkirim dua kali."),
                    fonnte.getRawResponse(), fonnte.getHttpCode());
        }

        Hasil siap = cekGoWA(cfg);
        if (!siap.berhasil()) {
            return hasil(Status.FAILED, "AUTO", false, "", nomorGoWA,
                    "Fonnte gagal. GoWA juga tidak siap: " + siap.getPesan(),
                    siap.getRawResponse(), siap.getHttpCode());
        }

        Hasil gowa = kirimPesanGoWA(cfg, nomorGoWA, pesan, true);
        if (gowa.berhasil()) return gowa;
        return hasil(gowa.getStatus(), gowa.getProvider(), true,
                gowa.getMessageId(), gowa.getTarget(),
                "Fonnte gagal dan fallback GoWA juga gagal: " + gowa.getPesan(),
                gowa.getRawResponse(), gowa.getHttpCode());
    }

    /** Pengiriman file/lampiran. Fonnte primary, GoWA fallback pada mode AUTO. */
    public static Hasil kirimFile(String noHp, String caption, File file, String namaFile) {
        Config cfg = bacaConfig();
        String mode = normalisasiMode(cfg.provider);
        String nomorGoWA = normalisasiNomorIndonesia(noHp);

        if (file == null || !file.isFile()) {
            return hasil(Status.FAILED, "SIMRS", false, "", nomorGoWA,
                    "File yang akan dikirim tidak ditemukan.", "", 0);
        }

        if (mode.equals("GOWA")) {
            Hasil siap = cekGoWA(cfg);
            if (!siap.berhasil()) return siap;
            return kirimFileGoWA(cfg, nomorGoWA, caption, file, namaFile, false);
        }

        Hasil fonnte = kirimFileFonnte(cfg, noHp, caption, file, namaFile);
        if (mode.equals("FONNTE") || fonnte.berhasil()) {
            return fonnte;
        }

        if (fonnte.statusTidakPasti()) {
            return hasil(Status.UNKNOWN, "Fonnte", false, fonnte.getMessageId(),
                    fonnte.getTarget(),
                    gabungPesan(fonnte.getPesan(),
                            "GoWA tidak dijalankan otomatis untuk mencegah file terkirim dua kali."),
                    fonnte.getRawResponse(), fonnte.getHttpCode());
        }

        Hasil siap = cekGoWA(cfg);
        if (!siap.berhasil()) {
            return hasil(Status.FAILED, "AUTO", false, "", nomorGoWA,
                    "Fonnte gagal. GoWA juga tidak siap: " + siap.getPesan(),
                    siap.getRawResponse(), siap.getHttpCode());
        }

        Hasil gowa = kirimFileGoWA(cfg, nomorGoWA, caption, file, namaFile, true);
        if (gowa.berhasil()) return gowa;
        return hasil(gowa.getStatus(), gowa.getProvider(), true,
                gowa.getMessageId(), gowa.getTarget(),
                "Fonnte gagal dan fallback GoWA juga gagal: " + gowa.getPesan(),
                gowa.getRawResponse(), gowa.getHttpCode());
    }

    /** Cek koneksi device GoWA secara eksplisit. */
    public static Hasil cekGoWA() {
        return cekGoWA(bacaConfig());
    }

    private static Hasil kirimPesanFonnte(Config cfg, String target,
            String pesan, Map<String, String> extra) {
        String token;
        try {
            token = koneksiDBWA.TOKENWA();
        } catch (Exception ex) {
            return hasil(Status.FAILED, "Fonnte", false, "", target,
                    "Konfigurasi token Fonnte tidak dapat dibaca: " + pesanException(ex), "", 0);
        }
        if (token == null || token.trim().equals("")) {
            return hasil(Status.FAILED, "Fonnte", false, "", target,
                    "Token Fonnte belum tersedia di konfigurasi aplikasi.", "", 0);
        }

        LinkedHashMap<String, String> form = new LinkedHashMap<String, String>();
        form.put("target", target == null ? "" : target);
        form.put("message", pesan == null ? "" : pesan);
        if (extra != null) form.putAll(extra);

        HttpURLConnection con = null;
        boolean requestPossiblySent = false;
        try {
            byte[] postData = formUrlEncoded(form).getBytes(StandardCharsets.UTF_8);
            con = bukaKoneksi(FONNTE_URL, cfg.fonnteConnectTimeout, cfg.fonnteReadTimeout);
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", token.trim());
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.setRequestProperty("Content-Length", String.valueOf(postData.length));
            con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            requestPossiblySent = true;
            try {
                out.write(postData);
                out.flush();
            } finally {
                out.close();
            }
            return bacaHasilFonnte(con, target);
        } catch (IOException ex) {
            Status st = requestPossiblySent ? Status.UNKNOWN : Status.FAILED;
            return hasil(st, "Fonnte", false, "", target,
                    pesanException(ex), "", safeHttpCode(con));
        } catch (Exception ex) {
            return hasil(Status.UNKNOWN, "Fonnte", false, "", target,
                    pesanException(ex), "", safeHttpCode(con));
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static Hasil kirimFileFonnte(Config cfg, String noHp, String caption,
            File file, String namaFile) {
        String token;
        try {
            token = koneksiDBWA.TOKENWA();
        } catch (Exception ex) {
            return hasil(Status.FAILED, "Fonnte", false, "", noHp,
                    "Konfigurasi token Fonnte tidak dapat dibaca: " + pesanException(ex), "", 0);
        }
        if (token == null || token.trim().equals("")) {
            return hasil(Status.FAILED, "Fonnte", false, "", noHp,
                    "Token Fonnte belum tersedia di konfigurasi aplikasi.", "", 0);
        }

        String boundary = "----KhanzaFonnte" + System.currentTimeMillis();
        HttpURLConnection con = null;
        boolean requestPossiblySent = false;
        try {
            con = bukaKoneksi(FONNTE_URL, cfg.fonnteConnectTimeout,
                    Math.max(cfg.fonnteReadTimeout, 90000));
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", token.trim());
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setDoOutput(true);

            OutputStream out = con.getOutputStream();
            requestPossiblySent = true;
            try {
                tulisField(out, boundary, "target", noHp == null ? "" : noHp);
                tulisField(out, boundary, "message", caption == null ? "" : caption);
                tulisField(out, boundary, "countryCode", noHp != null && noHp.trim().startsWith("0") ? "62" : "0");
                tulisField(out, boundary, "filename", namaFile == null ? file.getName() : namaFile);
                tulisFile(out, boundary, "file", namaFile == null ? file.getName() : namaFile, file);
                out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
            } finally {
                out.close();
            }
            return bacaHasilFonnte(con, noHp);
        } catch (IOException ex) {
            Status st = requestPossiblySent ? Status.UNKNOWN : Status.FAILED;
            return hasil(st, "Fonnte", false, "", noHp,
                    pesanException(ex), "", safeHttpCode(con));
        } catch (Exception ex) {
            return hasil(Status.UNKNOWN, "Fonnte", false, "", noHp,
                    pesanException(ex), "", safeHttpCode(con));
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static Hasil bacaHasilFonnte(HttpURLConnection con, String targetDefault) throws IOException {
        int code = con.getResponseCode();
        String body = bacaBody(con, code);
        if (code < 200 || code >= 300) {
            return hasil(Status.FAILED, "Fonnte", false, "", targetDefault,
                    "HTTP " + code + detailRespons(body), body, code);
        }
        try {
            JsonNode root = JSON.readTree(body);
            JsonNode statusNode = root.get("status");
            boolean sukses = statusNode != null && (statusNode.asBoolean(false)
                    || "true".equalsIgnoreCase(statusNode.asText()));
            String id = ambilPertama(root.get("id"));
            String target = ambilPertama(root.get("target"));
            if (target.equals("")) target = targetDefault == null ? "" : targetDefault;
            if (sukses) {
                return hasil(Status.SUCCESS, "Fonnte", false, id, target,
                        "Pesan berhasil dikirim.", body, code);
            }
            return hasil(Status.FAILED, "Fonnte", false, id, target,
                    ambilDetailJson(root, "Fonnte menolak pengiriman."), body, code);
        } catch (Exception ex) {
            return hasil(Status.UNKNOWN, "Fonnte", false, "", targetDefault,
                    "Respons Fonnte tidak dapat dipastikan: " + pesanException(ex), body, code);
        }
    }

    private static Hasil cekGoWA(Config cfg) {
        if (cfg.gowaBaseUrl.equals("")) {
            return hasil(Status.FAILED, "GoWA", false, "", "",
                    "gowa.base_url belum diisi di " + CONFIG_FILE + ".", "", 0);
        }
        if (cfg.gowaDeviceId.equals("")) {
            return hasil(Status.FAILED, "GoWA", false, "", "",
                    "gowa.device_id belum diisi di " + CONFIG_FILE + ".", "", 0);
        }

        HttpURLConnection con = null;
        try {
            String url = cfg.gowaBaseUrl + "/devices/" + encodePath(cfg.gowaDeviceId) + "/status";
            con = bukaKoneksi(url, cfg.gowaConnectTimeout, cfg.gowaReadTimeout);
            con.setRequestMethod("GET");
            pasangAuthGoWA(con, cfg, false);
            int code = con.getResponseCode();
            String body = bacaBody(con, code);
            if (code < 200 || code >= 300) {
                return hasil(Status.FAILED, "GoWA", false, "", "",
                        "Status device GoWA gagal, HTTP " + code + detailRespons(body), body, code);
            }
            JsonNode root = JSON.readTree(body);
            JsonNode results = root.path("results");
            boolean connected = results.path("is_connected").asBoolean(false);
            boolean loggedIn = results.path("is_logged_in").asBoolean(false);
            if (!connected || !loggedIn) {
                return hasil(Status.FAILED, "GoWA", false, "", "",
                        "Device GoWA belum siap (connected=" + connected
                        + ", logged_in=" + loggedIn + ").", body, code);
            }
            return hasil(Status.SUCCESS, "GoWA", false, "", "",
                    "Device GoWA siap.", body, code);
        } catch (Exception ex) {
            return hasil(Status.FAILED, "GoWA", false, "", "",
                    "Tidak dapat memeriksa GoWA: " + pesanException(ex), "", safeHttpCode(con));
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static Hasil kirimPesanGoWA(Config cfg, String noHp, String pesan, boolean fallback) {
        if (noHp.equals("")) {
            return hasil(Status.FAILED, "GoWA", fallback, "", "",
                    "Nomor WhatsApp tujuan kosong/tidak valid.", "", 0);
        }
        HttpURLConnection con = null;
        boolean requestPossiblySent = false;
        try {
            LinkedHashMap<String, Object> bodyMap = new LinkedHashMap<String, Object>();
            bodyMap.put("phone", noHp);
            bodyMap.put("message", pesan == null ? "" : pesan);
            byte[] data = JSON.writeValueAsBytes(bodyMap);

            con = bukaKoneksi(cfg.gowaBaseUrl + "/send/message",
                    cfg.gowaConnectTimeout, cfg.gowaReadTimeout);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            pasangAuthGoWA(con, cfg, true);
            con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            requestPossiblySent = true;
            try {
                out.write(data);
                out.flush();
            } finally {
                out.close();
            }
            return bacaHasilGoWA(con, noHp, fallback);
        } catch (IOException ex) {
            Status st = requestPossiblySent ? Status.UNKNOWN : Status.FAILED;
            return hasil(st, "GoWA", fallback, "", noHp,
                    pesanException(ex), "", safeHttpCode(con));
        } catch (Exception ex) {
            return hasil(Status.UNKNOWN, "GoWA", fallback, "", noHp,
                    pesanException(ex), "", safeHttpCode(con));
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static Hasil kirimFileGoWA(Config cfg, String noHp, String caption,
            File file, String namaFile, boolean fallback) {
        if (noHp.equals("")) {
            return hasil(Status.FAILED, "GoWA", fallback, "", "",
                    "Nomor WhatsApp tujuan kosong/tidak valid.", "", 0);
        }
        String boundary = "----KhanzaGoWA" + System.currentTimeMillis();
        HttpURLConnection con = null;
        boolean requestPossiblySent = false;
        try {
            con = bukaKoneksi(cfg.gowaBaseUrl + "/send/file",
                    cfg.gowaConnectTimeout, Math.max(cfg.gowaReadTimeout, 90000));
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            pasangAuthGoWA(con, cfg, true);
            con.setDoOutput(true);

            OutputStream out = con.getOutputStream();
            requestPossiblySent = true;
            try {
                tulisField(out, boundary, "phone", noHp);
                tulisField(out, boundary, "caption", caption == null ? "" : caption);
                tulisFile(out, boundary, "file", namaFile == null ? file.getName() : namaFile, file);
                out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
            } finally {
                out.close();
            }
            return bacaHasilGoWA(con, noHp, fallback);
        } catch (IOException ex) {
            Status st = requestPossiblySent ? Status.UNKNOWN : Status.FAILED;
            return hasil(st, "GoWA", fallback, "", noHp,
                    pesanException(ex), "", safeHttpCode(con));
        } catch (Exception ex) {
            return hasil(Status.UNKNOWN, "GoWA", fallback, "", noHp,
                    pesanException(ex), "", safeHttpCode(con));
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static Hasil bacaHasilGoWA(HttpURLConnection con, String target, boolean fallback) throws IOException {
        int code = con.getResponseCode();
        String body = bacaBody(con, code);
        if (code < 200 || code >= 300) {
            return hasil(Status.FAILED, "GoWA", fallback, "", target,
                    "HTTP " + code + detailRespons(body), body, code);
        }
        try {
            JsonNode root = JSON.readTree(body);
            String kode = root.path("code").asText();
            if ("SUCCESS".equalsIgnoreCase(kode)) {
                String id = root.path("results").path("message_id").asText();
                return hasil(Status.SUCCESS, "GoWA", fallback, id, target,
                        "Pesan berhasil dikirim.", body, code);
            }
            return hasil(Status.FAILED, "GoWA", fallback, "", target,
                    ambilDetailJson(root, "GoWA menolak pengiriman."), body, code);
        } catch (Exception ex) {
            return hasil(Status.UNKNOWN, "GoWA", fallback, "", target,
                    "Respons GoWA tidak dapat dipastikan: " + pesanException(ex), body, code);
        }
    }

    private static void pasangAuthGoWA(HttpURLConnection con, Config cfg, boolean deviceScoped) {
        if (!cfg.gowaUsername.equals("")) {
            String raw = cfg.gowaUsername + ":" + cfg.gowaPassword;
            String basic = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
            con.setRequestProperty("Authorization", "Basic " + basic);
        }
        if (deviceScoped && !cfg.gowaDeviceId.equals("")) {
            con.setRequestProperty("X-Device-Id", cfg.gowaDeviceId);
        }
    }

    private static HttpURLConnection bukaKoneksi(String url, int connectTimeout, int readTimeout) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        con.setUseCaches(false);
        return con;
    }

    private static Config bacaConfig() {
        Properties p = new Properties();
        File file = new File(CONFIG_FILE);
        if (file.isFile()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                p.load(in);
            } catch (Exception ex) {
                System.err.println("WhatsappGateway: gagal membaca " + CONFIG_FILE + ": " + ex.getMessage());
            } finally {
                if (in != null) try { in.close(); } catch (Exception ex) {}
            }
        }

        Config c = new Config();
        c.provider = nilai("wa.provider", "WA_PROVIDER", p, "provider", "AUTO");
        c.gowaBaseUrl = hapusSlashAkhir(nilai("gowa.base_url", "GOWA_BASE_URL", p, "gowa.base_url", ""));
        c.gowaUsername = nilai("gowa.username", "GOWA_USERNAME", p, "gowa.username", "");
        c.gowaPassword = nilai("gowa.password", "GOWA_PASSWORD", p, "gowa.password", "");
        c.gowaDeviceId = nilai("gowa.device_id", "GOWA_DEVICE_ID", p, "gowa.device_id", "");
        c.fonnteConnectTimeout = angka(nilai("fonnte.connect_timeout_ms", "FONNTE_CONNECT_TIMEOUT_MS", p,
                "fonnte.connect_timeout_ms", "10000"), 10000);
        c.fonnteReadTimeout = angka(nilai("fonnte.read_timeout_ms", "FONNTE_READ_TIMEOUT_MS", p,
                "fonnte.read_timeout_ms", "45000"), 45000);
        c.gowaConnectTimeout = angka(nilai("gowa.connect_timeout_ms", "GOWA_CONNECT_TIMEOUT_MS", p,
                "gowa.connect_timeout_ms", "5000"), 5000);
        c.gowaReadTimeout = angka(nilai("gowa.read_timeout_ms", "GOWA_READ_TIMEOUT_MS", p,
                "gowa.read_timeout_ms", "45000"), 45000);
        return c;
    }

    private static String nilai(String systemProperty, String env, Properties p,
            String key, String defaultValue) {
        String v = System.getProperty(systemProperty);
        if (v != null && !v.trim().equals("")) return v.trim();
        v = System.getenv(env);
        if (v != null && !v.trim().equals("")) return v.trim();
        v = p.getProperty(key);
        if (v != null && !v.trim().equals("")) return v.trim();
        return defaultValue;
    }

    private static String normalisasiMode(String provider) {
        String p = provider == null ? "AUTO" : provider.trim().toUpperCase(Locale.ENGLISH);
        if (!p.equals("FONNTE") && !p.equals("GOWA") && !p.equals("AUTO")) return "AUTO";
        return p;
    }

    /** Normalisasi nomor Indonesia untuk endpoint GoWA: 08xxx / 8xxx -> 628xxx. */
    public static String normalisasiNomorIndonesia(String nomor) {
        if (nomor == null) return "";
        String n = nomor.replaceAll("[^0-9]", "");
        if (n.startsWith("0") && n.length() > 1) return "62" + n.substring(1);
        if (n.startsWith("8")) return "62" + n;
        return n;
    }

    private static String formUrlEncoded(Map<String, String> form) throws Exception {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, String> e : form.entrySet()) {
            if (b.length() > 0) b.append('&');
            b.append(URLEncoder.encode(e.getKey(), "UTF-8"));
            b.append('=');
            b.append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), "UTF-8"));
        }
        return b.toString();
    }

    private static void tulisField(OutputStream out, String boundary,
            String nama, String nilai) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + nama + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write((nilai == null ? "" : nilai).getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static void tulisFile(OutputStream out, String boundary,
            String namaField, String namaFile, File file) throws IOException {
        String safeName = namaFile == null || namaFile.trim().equals("") ? file.getName() : namaFile;
        safeName = safeName.replace("\"", "").replace("\r", "").replace("\n", "");
        String mime = URLConnection.guessContentTypeFromName(safeName);
        if (mime == null || mime.trim().equals("")) mime = "application/octet-stream";
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + namaField
                + "\"; filename=\"" + safeName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + mime + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        InputStream in = new FileInputStream(file);
        try {
            byte[] buffer = new byte[8192];
            int baca;
            while ((baca = in.read(buffer)) != -1) out.write(buffer, 0, baca);
        } finally {
            in.close();
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static String bacaBody(HttpURLConnection con, int code) throws IOException {
        InputStream in = code >= 400 ? con.getErrorStream() : con.getInputStream();
        if (in == null) return "";
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }

    private static String ambilPertama(JsonNode node) {
        if (node == null || node.isNull()) return "";
        if (node.isArray()) return node.size() > 0 ? node.get(0).asText() : "";
        return node.asText();
    }

    private static String ambilDetailJson(JsonNode root, String defaultText) {
        if (root == null) return defaultText;
        String[] keys = new String[]{"detail", "message", "reason", "error"};
        for (String key : keys) {
            JsonNode n = root.get(key);
            if (n != null && !n.isNull() && !n.asText().trim().equals("")) {
                return potong(n.asText());
            }
        }
        return defaultText;
    }

    private static String detailRespons(String body) {
        if (body == null || body.trim().equals("")) return ".";
        try {
            JsonNode root = JSON.readTree(body);
            return ": " + ambilDetailJson(root, potong(body));
        } catch (Exception ex) {
            return ": " + potong(body);
        }
    }

    private static String potong(String s) {
        if (s == null) return "";
        String v = s.replaceAll("[\\r\\n]+", " ").trim();
        return v.length() > 300 ? v.substring(0, 300) + "..." : v;
    }

    private static String pesanException(Exception ex) {
        if (ex == null) return "Kesalahan tidak diketahui.";
        String m = ex.getMessage();
        return ex.getClass().getSimpleName() + (m == null || m.trim().equals("") ? "" : ": " + potong(m));
    }

    private static int safeHttpCode(HttpURLConnection con) {
        if (con == null) return 0;
        try { return con.getResponseCode(); } catch (Exception ex) { return 0; }
    }

    private static int angka(String value, int defaultValue) {
        try {
            int n = Integer.parseInt(value);
            return n > 0 ? n : defaultValue;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private static String hapusSlashAkhir(String url) {
        if (url == null) return "";
        String v = url.trim();
        while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
        return v;
    }

    private static String encodePath(String text) throws Exception {
        return URLEncoder.encode(text == null ? "" : text, "UTF-8").replace("+", "%20");
    }

    private static String gabungPesan(String a, String b) {
        String aa = a == null ? "" : a.trim();
        String bb = b == null ? "" : b.trim();
        if (aa.equals("")) return bb;
        if (bb.equals("")) return aa;
        return aa + " " + bb;
    }

    private static Hasil hasil(Status status, String provider, boolean fallback,
            String messageId, String target, String pesan, String raw, int httpCode) {
        return new Hasil(status, provider, fallback, messageId, target,
                pesan, raw, httpCode);
    }
}
