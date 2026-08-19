package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.nio.file.*;
import org.springframework.http.HttpStatus;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;



/**
 *
 * @author windiartonugroho
 */
public class ApiOrthanc {
    private HttpHeaders headers ;
    private JsonNode root;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;
    private String auth,authEncrypt,requestJson;
    private byte[] encodedBytes;
    private int i=1;
    private static boolean is2xx(int sc) { return sc >= 200 && sc < 300; }

    
    public ApiOrthanc(){
        try {
            auth=koneksiDB.USERORTHANC()+":"+koneksiDB.PASSORTHANC();
            encodedBytes = Base64.encodeBase64(auth.getBytes());
            authEncrypt= new String(encodedBytes);
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }
    }
    
    public String Auth(){
        return authEncrypt;
    }
    
    public JsonNode AmbilSeries(String Norm,String Tanggal1,String Tanggal2){
        System.out.println("Percobaan Mengambil Photo Pasien : "+Norm);
        try{
            headers = new HttpHeaders();
            System.out.println("Auth : "+authEncrypt);
            headers.add("Authorization", "Basic "+authEncrypt);
            requestJson = "{"+
                              "\"Level\": \"Study\","+
                              "\"Expand\": true,"+
                              "\"Query\": {"+
                                   "\"StudyDate\": \""+Tanggal1+"-"+Tanggal2+"\","+
                                   "\"PatientID\": \""+Norm+"\""+
                              "}"+
                          "}";
            System.out.println("Request JSON : "+requestJson);
            requestEntity = new HttpEntity(requestJson,headers);
            System.out.println("URL : "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/tools/find");
            requestJson=getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/tools/find", HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("Result JSON : "+requestJson);
            root = mapper.readTree(requestJson);
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
            JOptionPane.showMessageDialog(null,"Gagal mengambil data dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }
    
//    public JsonNode AmbilPng(String NoRawat,String Series){
//        System.out.println("Percobaan Mengambil Gambar PNG : "+NoRawat+", Series : "+Series);
//        try{
//            headers = new HttpHeaders();
//            System.out.println("Auth : "+authEncrypt);
//            headers.add("Authorization", "Basic "+authEncrypt);
//            requestEntity = new HttpEntity(headers);
//            System.out.println("URL : "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/series/"+Series);
//            requestJson=getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/series/"+Series, HttpMethod.GET, requestEntity, String.class).getBody();
//            System.out.println("Result JSON : "+requestJson);
//            root = mapper.readTree(requestJson);
//            i=1;
//            for(JsonNode list:root.path("Instances")){
//                 System.out.println("Mengambil Gambar PNG "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/instances/"+list.asText()+"/preview");
//                 headers = new HttpHeaders();
//                 headers.add("Authorization", "Basic "+authEncrypt);
//                 headers.add("Accept","image/png");
//                 headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
//                 headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
//                 HttpEntity<String> entity = new HttpEntity<>(headers);
//                 ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/instances/"+list.asText()+"/preview", HttpMethod.GET, entity, byte[].class);
//                 Files.write(Paths.get("./gambarradiologi/"+NoRawat+i+".png"),response.getBody());
//                 i++;
//            }
//            JOptionPane.showMessageDialog(null,"Pengambilan Gambar PNG dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//            JOptionPane.showMessageDialog(null,"Gagal mengambil Gambar PNG dari Orthanc, silahkan hubungi administrator ..!!");
//        }
//        return root;
//    }
    
public JsonNode AmbilPng(String NoRawat, String Series) {
    System.out.println("Percobaan Mengambil Gambar PNG : " + NoRawat + ", Series : " + Series);
    int saved = 0, total = 0;
    try {
        Path saveDir = ensureSaveDir();
        System.out.println("Folder simpan: " + saveDir.toAbsolutePath());

        // Pastikan kita punya Orthanc Series ID
        String sId = resolveSeriesId(Series);

        // Ambil info series (optional: untuk return)
        HttpHeaders h0 = new HttpHeaders();
        h0.add("Authorization", "Basic " + authEncrypt);
        ResponseEntity<String> rSeries = getRest().exchange(
                orthancBase() + "/series/" + sId, HttpMethod.GET, new HttpEntity<>(h0), String.class);
        JsonNode root = is2xx(rSeries) ? mapper.readTree(rSeries.getBody()) : mapper.createObjectNode();

        // Ambil daftar instance
        List<String> instances = fetchInstanceIds(sId);
        total = instances.size();
        System.out.println("Jumlah instance: " + total);

        int i = 1;
        for (String id : instances) {
            byte[] bytes = fetchPreviewBytes(id);
            if (bytes == null) { System.out.println("Skip " + id + " (no bytes)"); continue; }

            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                BufferedImage img = ImageIO.read(bais);
                if (img == null) { System.out.println("Decode null: " + id); continue; }
                Path out = saveDir.resolve(NoRawat + "-" + String.format("%03d", i) + ".png");
                ImageIO.write(img, "png", out.toFile());
                System.out.println("Saved: " + out.toAbsolutePath());
                saved++; i++;
            }
        }

        String msg = (saved > 0)
                ? "Pengambilan Gambar PNG dari Orthanc berhasil (" + saved + "/" + total + ").\nFolder: " + saveDir.toAbsolutePath()
                : "Tidak ada file PNG yang tersimpan (" + saved + "/" + total + "). Cek Series/akses Orthanc atau izin preview/rendered.";
        JOptionPane.showMessageDialog(null, msg);
        return root;
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar PNG: " + e.getMessage());
        return null;
    }
}
    
//    public JsonNode AmbilJpg(String NoRawat,String Series){
//        System.out.println("Percobaan Mengambil Gambar JPG : "+NoRawat+", Series : "+Series);
//        try{
//            headers = new HttpHeaders();
//            System.out.println("Auth : "+authEncrypt);
//            headers.add("Authorization", "Basic "+authEncrypt);
//            requestEntity = new HttpEntity(headers);
//            System.out.println("URL : "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/series/"+Series);
//            requestJson=getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/series/"+Series, HttpMethod.GET, requestEntity, String.class).getBody();
//            System.out.println("Result JSON : "+requestJson);
//            root = mapper.readTree(requestJson);
//            i=1;
//            for(JsonNode list:root.path("Instances")){
//                 System.out.println("Mengambil Gambar JPG "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/instances/"+list.asText()+"/preview");
//                 headers = new HttpHeaders();
//                 headers.add("Authorization", "Basic "+authEncrypt);
//                 headers.add("Accept","image/jpeg");
//                 headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
//                 headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
//                 HttpEntity<String> entity = new HttpEntity<>(headers);
//                 ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/instances/"+list.asText()+"/preview", HttpMethod.GET, entity, byte[].class);
//                 Files.write(Paths.get("./gambarradiologi/"+NoRawat+i+".jpg"),response.getBody());
//                 i++;
//            }
//            JOptionPane.showMessageDialog(null,"Pengambilan Gambar JPG dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//            JOptionPane.showMessageDialog(null,"Gagal mengambil Gambar JPG dari Orthanc, silahkan hubungi administrator ..!!");
//        }
//        return root;
//    }

public JsonNode AmbilJpg(String NoRawat, String Series) {
    System.out.println("Percobaan Mengambil Gambar JPG : " + NoRawat + ", Series : " + Series);
    int saved = 0, total = 0;
    try {
        Path saveDir = ensureSaveDir();
        System.out.println("Folder simpan: " + saveDir.toAbsolutePath());

        String sId = resolveSeriesId(Series);

        HttpHeaders h0 = new HttpHeaders();
        h0.add("Authorization", "Basic " + authEncrypt);
        ResponseEntity<String> rSeries = getRest().exchange(
                orthancBase() + "/series/" + sId, HttpMethod.GET, new HttpEntity<>(h0), String.class);
        JsonNode root = is2xx(rSeries) ? mapper.readTree(rSeries.getBody()) : mapper.createObjectNode();

        List<String> instances = fetchInstanceIds(sId);
        total = instances.size();
        System.out.println("Jumlah instance: " + total);

        int i = 1;
        for (String id : instances) {
            byte[] bytes = fetchPreviewBytes(id);
            if (bytes == null) { System.out.println("Skip " + id + " (no bytes)"); continue; }

            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                BufferedImage img = ImageIO.read(bais);
                if (img == null) { System.out.println("Decode null: " + id); continue; }
                Path out = saveDir.resolve(NoRawat + "-" + String.format("%03d", i) + ".jpg");
                ImageIO.write(img, "jpg", out.toFile());
                System.out.println("Saved: " + out.toAbsolutePath());
                saved++; i++;
            }
        }

        String msg = (saved > 0)
                ? "Pengambilan Gambar JPG dari Orthanc berhasil (" + saved + "/" + total + ").\nFolder: " + saveDir.toAbsolutePath()
                : "Tidak ada file JPG yang tersimpan (" + saved + "/" + total + ").";
        JOptionPane.showMessageDialog(null, msg);
        return root;
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar JPG: " + e.getMessage());
        return null;
    }
}
    
//    public JsonNode AmbilBmp(String NoRawat,String Series){
//        System.out.println("Percobaan Mengambil Gambar BMP : "+NoRawat+", Series : "+Series);
//        try{
//            headers = new HttpHeaders();
//            System.out.println("Auth : "+authEncrypt);
//            headers.add("Authorization", "Basic "+authEncrypt);
//            requestEntity = new HttpEntity(headers);
//            System.out.println("URL : "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/series/"+Series);
//            requestJson=getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/series/"+Series, HttpMethod.GET, requestEntity, String.class).getBody();
//            System.out.println("Result JSON : "+requestJson);
//            root = mapper.readTree(requestJson);
//            i=1;
//            for(JsonNode list:root.path("Instances")){
//                 System.out.println("Mengambil Gambar BMP "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/instances/"+list.asText()+"/preview");
//                 headers = new HttpHeaders();
//                 headers.add("Authorization", "Basic "+authEncrypt);
//                 headers.add("Accept","image/bmp");
//                 headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
//                 headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
//                 HttpEntity<String> entity = new HttpEntity<>(headers);
//                 ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/instances/"+list.asText()+"/preview", HttpMethod.GET, entity, byte[].class);
//                 Files.write(Paths.get("./gambarradiologi/"+NoRawat+i+".bmp"),response.getBody());
//                 i++;
//            }
//            JOptionPane.showMessageDialog(null,"Pengambilan Gambar BMP dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//            JOptionPane.showMessageDialog(null,"Gagal mengambil Gambar BMP dari Orthanc, silahkan hubungi administrator ..!!");
//        }
//        return root;
//    }

public JsonNode AmbilBmp(String NoRawat, String Series) {
    System.out.println("Percobaan Mengambil Gambar BMP : " + NoRawat + ", Series : " + Series);
    int saved = 0, total = 0;
    try {
        Path saveDir = ensureSaveDir();
        System.out.println("Folder simpan: " + saveDir.toAbsolutePath());

        String sId = resolveSeriesId(Series);

        HttpHeaders h0 = new HttpHeaders();
        h0.add("Authorization", "Basic " + authEncrypt);
        ResponseEntity<String> rSeries = getRest().exchange(
                orthancBase() + "/series/" + sId, HttpMethod.GET, new HttpEntity<>(h0), String.class);
        JsonNode root = is2xx(rSeries) ? mapper.readTree(rSeries.getBody()) : mapper.createObjectNode();

        List<String> instances = fetchInstanceIds(sId);
        total = instances.size();

        int i = 1;
        for (String id : instances) {
            byte[] bytes = fetchPreviewBytes(id);
            if (bytes == null) { System.out.println("Skip " + id + " (no bytes)"); continue; }

            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                BufferedImage img = ImageIO.read(bais);
                if (img == null) { System.out.println("Decode null: " + id); continue; }
                Path out = saveDir.resolve(NoRawat + "-" + String.format("%03d", i) + ".bmp");
                ImageIO.write(img, "bmp", out.toFile());
                System.out.println("Saved: " + out.toAbsolutePath());
                saved++; i++;
            }
        }

        String msg = (saved > 0)
                ? "Pengambilan Gambar BMP berhasil (" + saved + "/" + total + ").\nFolder: " + saveDir.toAbsolutePath()
                : "Tidak ada file BMP yang tersimpan (" + saved + "/" + total + ").";
        JOptionPane.showMessageDialog(null, msg);
        return root;
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar BMP: " + e.getMessage());
        return null;
    }
}
    
//    public JsonNode AmbilDcm(String NoRawat,String Series){
//        System.out.println("Percobaan Mengambil Gambar DCM : "+NoRawat+", Series : "+Series);
//        try{
//            headers = new HttpHeaders();
//            System.out.println("Auth : "+authEncrypt);
//            headers.add("Authorization", "Basic "+authEncrypt);
//            requestEntity = new HttpEntity(headers);
//            System.out.println("URL : "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/series/"+Series);
//            requestJson=getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/series/"+Series, HttpMethod.GET, requestEntity, String.class).getBody();
//            System.out.println("Result JSON : "+requestJson);
//            root = mapper.readTree(requestJson);
//            i=1;
//            for(JsonNode list:root.path("Instances")){
//                 System.out.println("Mengambil Gambar DCM "+koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/instances/"+list.asText()+"/file");
//                 headers = new HttpHeaders();
//                 headers.add("Authorization", "Basic "+authEncrypt);
//                 headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
//                 headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
//                 HttpEntity<String> entity = new HttpEntity<>(headers);
//                 ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/instances/"+list.asText()+"/file", HttpMethod.GET, entity, byte[].class);
//                 Files.write(Paths.get("./gambarradiologi/"+NoRawat+i+".dcm"),response.getBody());
//                 i++;
//            }
//            JOptionPane.showMessageDialog(null,"Pengambilan Gambar DCM dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//            JOptionPane.showMessageDialog(null,"Gagal mengambil Gambar DCM dari Orthanc, silahkan hubungi administrator ..!!");
//        }
//        return root;
//    }

public JsonNode AmbilDcm(String NoRawat, String Series) {
    System.out.println("Percobaan Mengambil Gambar DCM : " + NoRawat + ", Series : " + Series);
    int saved = 0, total = 0;
    try {
        Path saveDir = ensureSaveDir();
        System.out.println("Folder simpan: " + saveDir.toAbsolutePath());

        String sId = resolveSeriesId(Series);

        HttpHeaders h0 = new HttpHeaders();
        h0.add("Authorization", "Basic " + authEncrypt);
        ResponseEntity<String> rSeries = getRest().exchange(
                orthancBase() + "/series/" + sId, HttpMethod.GET, new HttpEntity<>(h0), String.class);
        JsonNode root = is2xx(rSeries) ? mapper.readTree(rSeries.getBody()) : mapper.createObjectNode();

        List<String> instances = fetchInstanceIds(sId);
        total = instances.size();

        int i = 1;
        for (String id : instances) {
            HttpHeaders h = new HttpHeaders();
            h.add("Authorization", "Basic " + authEncrypt);

            try {
                ResponseEntity<byte[]> resp = getRest().exchange(
                        orthancBase() + "/instances/" + id + "/file",
                        HttpMethod.GET, new HttpEntity<>(h), byte[].class);

                if (!is2xx(resp) || resp.getBody() == null || resp.getBody().length == 0) {
                    System.out.println("Skip " + id + " status=" + resp.getStatusCode());
                    continue;
                }

                Path out = saveDir.resolve(NoRawat + "-" + String.format("%03d", i) + ".dcm");
                Files.write(out, resp.getBody());
                System.out.println("Saved: " + out.toAbsolutePath());
                saved++; i++;
            } catch (Exception ex) {
                System.out.println("Error ambil DCM " + id + " : " + ex.getMessage());
            }
        }

        String msg = (saved > 0)
                ? "Pengambilan DCM berhasil (" + saved + "/" + total + ").\nFolder: " + saveDir.toAbsolutePath()
                : "Tidak ada file DCM yang tersimpan (" + saved + "/" + total + ").";
        JOptionPane.showMessageDialog(null, msg);
        return root;
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal mengambil DCM: " + e.getMessage());
        return null;
    }
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
    
private Path ensureSaveDir() throws Exception {
    Path appDir  = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
    Path saveDir = appDir.resolve("gambarradiologi");
    Files.createDirectories(saveDir);
    return saveDir;
}

private byte[] fetchPreviewBytes(String instanceId, String auth) {
    String base = koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + instanceId;
    HttpHeaders h = new HttpHeaders();
    h.add("Authorization", "Basic " + auth);
    // Jangan set Accept (biarkan server mengirim default JPEG/PNG)
    try {
        ResponseEntity<byte[]> r = getRest().exchange(base + "/preview", HttpMethod.GET, new HttpEntity<>(h), byte[].class);
        int sc = r.getStatusCode().value();
        if (sc >= 200 && sc < 300 && r.getBody() != null && r.getBody().length > 0) return r.getBody();
    } catch (Exception ex) {
        System.out.println("preview gagal: " + ex.getMessage());
    }
    // fallback ke rendered
    try {
        ResponseEntity<byte[]> r = getRest().exchange(base + "/rendered", HttpMethod.GET, new HttpEntity<>(h), byte[].class);
        int sc = r.getStatusCode().value();
        if (sc >= 200 && sc < 300 && r.getBody() != null && r.getBody().length > 0) return r.getBody();
    } catch (Exception ex) {
        System.out.println("rendered gagal: " + ex.getMessage());
    }
    return null;
}

private boolean is2xx(ResponseEntity<?> r) {
    int sc = r.getStatusCode().value();
    return sc >= 200 && sc < 300;
}

private String orthancBase() {
    return koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC();
}

/** Jika Series berformat UID (ada titik), cari Orthanc Series ID via /tools/lookup. */
private String resolveSeriesId(String seriesParam) throws Exception {
    if (!seriesParam.contains(".")) {
        return seriesParam; // kemungkinan besar sudah Orthanc ID
    }
    HttpHeaders h = new HttpHeaders();
    h.add("Authorization", "Basic " + authEncrypt);
    h.setContentType(MediaType.APPLICATION_JSON);

    String payload = "{\"Identifiers\":{\"SeriesInstanceUID\":\"" + seriesParam + "\"}}";
    ResponseEntity<String> r = getRest().exchange(
            orthancBase() + "/tools/lookup",
            HttpMethod.POST, new HttpEntity<>(payload, h), String.class);

    if (is2xx(r)) {
        JsonNode j = mapper.readTree(r.getBody());
        if (j.has("ID")) {
            return j.get("ID").asText();
        }
    }
    throw new IllegalStateException("Tidak bisa memetakan SeriesInstanceUID ke Orthanc ID: " + seriesParam);
}

/** Ambil daftar instance ID dari sebuah series Orthanc. */
private List<String> fetchInstanceIds(String orthancSeriesId) throws Exception {
    HttpHeaders h = new HttpHeaders();
    h.add("Authorization", "Basic " + authEncrypt);

    ResponseEntity<String> r = getRest().exchange(
            orthancBase() + "/series/" + orthancSeriesId + "/instances",
            HttpMethod.GET, new HttpEntity<>(h), String.class);

    List<String> list = new ArrayList<>();
    if (is2xx(r)) {
        ArrayNode arr = (ArrayNode) mapper.readTree(r.getBody());
        for (JsonNode n : arr) list.add(n.asText());
    }
    return list;
}

/** Ambil bytes preview (coba /preview, jika gagal /rendered). Jangan set Accept; biarkan default. */
private byte[] fetchPreviewBytes(String instanceId) {
    HttpHeaders h = new HttpHeaders();
    h.add("Authorization", "Basic " + authEncrypt);
    try {
        ResponseEntity<byte[]> r = getRest().exchange(
                orthancBase() + "/instances/" + instanceId + "/preview",
                HttpMethod.GET, new HttpEntity<>(h), byte[].class);
        if (is2xx(r) && r.getBody() != null && r.getBody().length > 0) return r.getBody();
    } catch (Exception e) {
        System.out.println("preview gagal: " + e.getMessage());
    }
    try {
        ResponseEntity<byte[]> r = getRest().exchange(
                orthancBase() + "/instances/" + instanceId + "/rendered",
                HttpMethod.GET, new HttpEntity<>(h), byte[].class);
        if (is2xx(r) && r.getBody() != null && r.getBody().length > 0) return r.getBody();
    } catch (Exception e) {
        System.out.println("rendered gagal: " + e.getMessage());
    }
    return null;
}


}
