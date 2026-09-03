package fungsi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Client VClaim khusus endpoint FingerPrint.
 * Memakai konfigurasi BPJS yang sudah ada pada setting/database.xml.
 */
public class BPJSApiFinger {
    private final ObjectMapper mapper = new ObjectMapper();
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public BPJSApiFinger() {
        this(10000, 20000);
    }

    public BPJSApiFinger(int connectTimeoutMs, int readTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    public String validasiKonfigurasi() {
        StringBuilder kosong = new StringBuilder();
        tambahJikaKosong(kosong, "URLAPIBPJS", koneksiDB.URLAPIBPJS());
        tambahJikaKosong(kosong, "CONSIDAPIBPJS", koneksiDB.CONSIDAPIBPJS());
        tambahJikaKosong(kosong, "SECRETKEYAPIBPJS", koneksiDB.SECRETKEYAPIBPJS());
        tambahJikaKosong(kosong, "USERKEYAPIBPJS", koneksiDB.USERKEYAPIBPJS());
        if (kosong.length() > 0) {
            return "Konfigurasi VClaim belum terbaca: " + kosong.toString()
                    + ". Periksa file setting/database.xml dari SIMKES/Service Aplicare aktif.";
        }
        return "";
    }

    private void tambahJikaKosong(StringBuilder kosong, String nama, String nilai) {
        if (isBlank(nilai)) {
            if (kosong.length() > 0) kosong.append(", ");
            kosong.append(nama);
        }
    }

    public HasilFinger cekFinger(String noKartu, String tanggalPelayanan) {
        String masalahKonfigurasi = validasiKonfigurasi();
        if (!isBlank(masalahKonfigurasi)) {
            return HasilFinger.error(masalahKonfigurasi);
        }
        String consId = koneksiDB.CONSIDAPIBPJS();
        String secretKey = koneksiDB.SECRETKEYAPIBPJS();
        String userKey = koneksiDB.USERKEYAPIBPJS();
        String baseUrl = normalizeUrl(koneksiDB.URLAPIBPJS());

        HttpURLConnection conn = null;
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        try {
            String endpoint = "/SEP/FingerPrint/Peserta/" + encode(noKartu)
                    + "/TglPelayanan/" + encode(tanggalPelayanan);
            conn = (HttpURLConnection) new URL(baseUrl + endpoint).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(connectTimeoutMs);
            conn.setReadTimeout(readTimeoutMs);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("X-cons-id", consId);
            conn.setRequestProperty("X-timestamp", timestamp);
            conn.setRequestProperty("X-signature", signature(consId, secretKey, timestamp));
            conn.setRequestProperty("user_key", userKey);

            int httpStatus = conn.getResponseCode();
            String rawBody = readBody(httpStatus >= 200 && httpStatus < 400
                    ? conn.getInputStream() : conn.getErrorStream());
            if (isBlank(rawBody)) {
                return HasilFinger.error("Respons BPJS kosong (HTTP " + httpStatus + ")");
            }

            JsonNode root = mapper.readTree(rawBody);
            JsonNode meta = root.has("metaData") ? root.path("metaData") : root.path("metadata");
            String metaCode = meta.path("code").asText();
            String metaMessage = meta.path("message").asText();
            if (!"200".equals(metaCode)) {
                return HasilFinger.gagalMeta(metaCode, metaMessage, httpStatus);
            }

            String encryptedResponse = root.path("response").asText();
            if (isBlank(encryptedResponse)) {
                return HasilFinger.error("Response BPJS berhasil tetapi payload fingerprint kosong");
            }
            String plain = decryptAndDecompress(encryptedResponse, consId + secretKey + timestamp);
            if (isBlank(plain)) {
                return HasilFinger.error("Payload fingerprint gagal didekripsi/dekompresi");
            }
            JsonNode response = mapper.readTree(plain);
            String kode = response.path("kode").asText();
            String pesan = response.has("pesan") ? response.path("pesan").asText()
                    : response.path("message").asText();
            return HasilFinger.respons(kode, pesan, metaCode, metaMessage, httpStatus);
        } catch (Exception e) {
            return HasilFinger.error("Request fingerprint gagal: " + e.getClass().getSimpleName() + " - " + safe(e.getMessage()));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String signature(String consId, String secretKey, String timestamp) throws Exception {
        String data = consId + "&" + timestamp;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String decryptAndDecompress(String encrypted, String keyString) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] key = sha256.digest(keyString.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                new IvParameterSpec(Arrays.copyOfRange(key, 0, 16)));
        String lzEncoded = new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
        return LZString.decompressFromEncodedURIComponent(lzEncoded);
    }

    private String readBody(InputStream in) throws Exception {
        if (in == null) return "";
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    private String encode(String text) throws Exception {
        return URLEncoder.encode(text == null ? "" : text, "UTF-8").replace("+", "%20");
    }

    private String normalizeUrl(String alamat) {
        if (alamat == null) return "";
        alamat = alamat.trim();
        while (alamat.endsWith("/")) alamat = alamat.substring(0, alamat.length() - 1);
        return alamat;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private String safe(String s) { return s == null ? "" : s.replace('\n', ' ').replace('\r', ' '); }

    public static final class HasilFinger {
        public final boolean requestValid;
        public final String kodeFinger;
        public final String pesan;
        public final String metadataCode;
        public final String metadataMessage;
        public final int httpStatus;

        private HasilFinger(boolean requestValid, String kodeFinger, String pesan,
                String metadataCode, String metadataMessage, int httpStatus) {
            this.requestValid = requestValid;
            this.kodeFinger = kodeFinger == null ? "" : kodeFinger;
            this.pesan = pesan == null ? "" : pesan;
            this.metadataCode = metadataCode == null ? "" : metadataCode;
            this.metadataMessage = metadataMessage == null ? "" : metadataMessage;
            this.httpStatus = httpStatus;
        }
        public static HasilFinger respons(String kode, String pesan, String metaCode, String metaMessage, int http) {
            return new HasilFinger(true, kode, pesan, metaCode, metaMessage, http);
        }
        public static HasilFinger gagalMeta(String code, String msg, int http) {
            return new HasilFinger(false, "", msg, code, msg, http);
        }
        public static HasilFinger error(String msg) {
            return new HasilFinger(false, "", msg, "", msg, 0);
        }
        public boolean sudahFinger() { return requestValid && "1".equals(kodeFinger); }
    }

    /** Implementasi LZ-String untuk response terkompresi VClaim. */
    private static final class LZString {
        private static final String URI_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$";
        private interface GetNextValue { int value(int index); }

        static String decompressFromEncodedURIComponent(String input) {
            if (input == null) return "";
            if (input.length() == 0) return null;
            final String value = input.replace(' ', '+');
            return decompress(value.length(), 32, new GetNextValue() {
                public int value(int index) { return URI_ALPHABET.indexOf(value.charAt(index)); }
            });
        }

        private static String decompress(int length, int resetValue, GetNextValue getNextValue) {
            java.util.List<String> dictionary = new java.util.ArrayList<String>();
            int enlargeIn = 4, dictSize = 4, numBits = 3;
            String entry = "", w, result;
            Data data = new Data(getNextValue.value(0), resetValue, 1);
            for (int i = 0; i < 3; i++) dictionary.add(String.valueOf((char) i));
            int bits = 0, maxpower = 4, power = 1;
            while (power != maxpower) {
                int resb = data.val & data.position;
                data.position >>= 1;
                if (data.position == 0) { data.position = resetValue; data.val = getNextValue.value(data.index++); }
                bits |= (resb > 0 ? 1 : 0) * power;
                power <<= 1;
            }
            int next = bits;
            char c;
            switch (next) {
                case 0:
                    bits = readBits(8, data, resetValue, getNextValue); c = (char) bits; break;
                case 1:
                    bits = readBits(16, data, resetValue, getNextValue); c = (char) bits; break;
                case 2: return "";
                default: c = 0;
            }
            dictionary.add(String.valueOf(c));
            w = String.valueOf(c);
            StringBuilder output = new StringBuilder(w);
            while (true) {
                if (data.index > length) return "";
                int cc = readBits(numBits, data, resetValue, getNextValue);
                switch (cc) {
                    case 0:
                        dictionary.add(String.valueOf((char) readBits(8, data, resetValue, getNextValue)));
                        cc = dictSize++;
                        enlargeIn--;
                        break;
                    case 1:
                        dictionary.add(String.valueOf((char) readBits(16, data, resetValue, getNextValue)));
                        cc = dictSize++;
                        enlargeIn--;
                        break;
                    case 2:
                        return output.toString();
                    default:
                        break;
                }
                if (enlargeIn == 0) { enlargeIn = 1 << numBits; numBits++; }
                if (cc < dictionary.size() && dictionary.get(cc) != null) {
                    entry = dictionary.get(cc);
                } else if (cc == dictSize) {
                    entry = w + w.charAt(0);
                } else {
                    return null;
                }
                output.append(entry);
                dictionary.add(w + entry.charAt(0));
                dictSize++;
                enlargeIn--;
                w = entry;
                if (enlargeIn == 0) { enlargeIn = 1 << numBits; numBits++; }
            }
        }
        private static int readBits(int number, Data data, int resetValue, GetNextValue getNextValue) {
            int bits = 0, maxpower = 1 << number, power = 1;
            while (power != maxpower) {
                int resb = data.val & data.position;
                data.position >>= 1;
                if (data.position == 0) { data.position = resetValue; data.val = getNextValue.value(data.index++); }
                bits |= (resb > 0 ? 1 : 0) * power;
                power <<= 1;
            }
            return bits;
        }
        private static final class Data {
            int val, position, index;
            Data(int val, int position, int index) { this.val = val; this.position = position; this.index = index; }
        }
    }
}
