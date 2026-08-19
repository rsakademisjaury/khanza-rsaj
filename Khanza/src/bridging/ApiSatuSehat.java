package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ApiSatuSehat {        
    private String key,clientid,urlauth,token;
    private long millis;
    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;
    private ApiBPJSAesKeySpec mykey;
    private HttpHeaders header ;
    private JsonNode root;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private static String cachedAccessToken = null;
    private static long tokenExpiresAtMillis = 0L;
    private static final long REFRESH_BUFFER_SECONDS = 60L;
    private static final long DEFAULT_TOKEN_TTL_SECONDS = 3600L;
    
    public ApiSatuSehat(){
        try {
            key = koneksiDB.SECRETKEYSATUSEHAT();
            clientid = koneksiDB.CLIENTIDSATUSEHAT();
            urlauth = koneksiDB.URLAUTHSATUSEHAT();
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }
    }

    public String TokenSatuSehat(){
        try {    
            header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            requestEntity = new HttpEntity("client_id="+clientid+"&client_secret="+key,header);
            root = mapper.readTree(getRest().exchange(urlauth+"/accesstoken?grant_type=client_credentials", HttpMethod.POST, requestEntity, String.class).getBody());
            token=root.path("access_token").asText();
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }
        return token;
    }
        
    public long GetUTCdatetimeAsString(){    
        millis = System.currentTimeMillis();   
        return millis/1000;
    }
    
    public String Decrypt(String data,String utc)throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        System.out.println(data);
        mykey = ApiBPJSEnc.generateKey(clientid+key+utc);
        data=ApiBPJSEnc.decrypt(data, mykey.getKey(), mykey.getIv());
        data=ApiBPJSLZString.decompressFromEncodedURIComponent(data);
        System.out.println(data);
        return data;
    }
    
    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = SSLContext.getInstance("SSL");
        TrustManager[] trustManagers= {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
            }
        };
        sslContext.init(null,trustManagers , new SecureRandom());
        sslFactory=new SSLSocketFactory(sslContext,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        scheme=new Scheme("https",443,sslFactory);
        factory=new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        return new RestTemplate(factory);
    }
    
    // ====================================================================
    //  METHOD BARU untuk RUJUKAN SATUSEHAT (FHIR R4)
    //  Existing methods di atas tidak diubah - backward compatible.
    // ====================================================================

    /**
     * Token getter dengan caching. Wrapper di sekitar TokenSatuSehat() existing.
     * Kalau cached token masih valid, return langsung tanpa hit OAuth.
     */
    public String getValidToken() {
        long nowMillis = System.currentTimeMillis();
        long bufferMillis = REFRESH_BUFFER_SECONDS * 1000L;
        if (cachedAccessToken != null
                && !cachedAccessToken.isEmpty()
                && nowMillis + bufferMillis < tokenExpiresAtMillis) {
            return cachedAccessToken;
        }
        // cache expired/null -> panggil method existing untuk request baru
        String fresh = TokenSatuSehat();
        if (fresh != null && !fresh.isEmpty()) {
            cachedAccessToken = fresh;
            // default 1 jam (SATUSEHAT umumnya kasih expires_in 3600)
            tokenExpiresAtMillis = System.currentTimeMillis() + (DEFAULT_TOKEN_TTL_SECONDS * 1000L);
        }
        return fresh;
    }

    /**
     * Force refresh token. Bersihkan cache dan request baru.
     * Berguna kalau dapat error 401 dari FHIR endpoint.
     */
    public String forceRefreshToken() {
        cachedAccessToken = null;
        tokenExpiresAtMillis = 0L;
        return getValidToken();
    }

    /**
     * UTC datetime saat ini dalam format ISO-8601 dengan offset +00:00.
     * Contoh: "2026-05-04T03:35:00+00:00"
     * Sesuai Playbook v5.1 hal. 41 (semua datetime SATUSEHAT pakai UTC+00).
     */
    public String getUtcDatetimeNow() {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
        return nowUtc.format(fmt);
    }

    /**
     * Membangun HttpHeaders standar untuk endpoint FHIR SATUSEHAT.
     * Bearer token + Content-Type application/json.
     */
    public HttpHeaders buildAuthHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add("Authorization", "Bearer " + getValidToken());
        return h;
    }

    /** Base URL FHIR SATUSEHAT, e.g. "https://api-satusehat.kemkes.go.id/fhir-r4/v1". */
    public String getBaseUrl() {
        try {
            return koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
            return "";
        }
    }

    /** Organization ID Fasyankes Perujuk (UUID dari registrasi SATUSEHAT). */
    public String getOrgIdPerujuk() {
        try {
            return koneksiDB.IDSATUSEHAT();
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
            return "";
        }
    }

    /** ObjectMapper Jackson (shared instance). */
    public ObjectMapper getMapper() {
        return mapper;
    }

    // ===== FHIR Reference builders =====

    public String orgRef(String orgId) {
        return "Organization/" + safe(orgId);
    }

    public String patientRef(String patientIhsId) {
        return "Patient/" + safe(patientIhsId);
    }

    public String practitionerRef(String practitionerIhsId) {
        return "Practitioner/" + safe(practitionerIhsId);
    }

    public String encounterRef(String encounterId) {
        return "Encounter/" + safe(encounterId);
    }

    public String taskRef(String taskId) {
        return "Task/" + safe(taskId);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

}
