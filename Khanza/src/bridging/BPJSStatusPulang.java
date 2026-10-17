package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Akses SKDP lokal dan pengiriman update pulang; tidak memeriksa status ke BPJS. */
final class BPJSStatusPulang {
    enum Status { KOSONG, RALAN, TANPA_SKDP, ADA_SKDP, BERHASIL }

    static final class Hasil {
        final Status status;
        final String tanggal;
        final String catatan;
        Hasil(Status status, String tanggal) {
            this(status, tanggal, "");
        }
        Hasil(Status status, String tanggal, String catatan) {
            this.status = status;
            this.tanggal = tanggal;
            this.catatan = catatan;
        }
    }

    static final class DataSEP {
        String noSEP, noRawat, noKartu, tanggal, jenis, laka;
    }

    private static final Set<String> SEDANG_KIRIM = Collections.newSetFromMap(
            new ConcurrentHashMap<String, Boolean>());
    private final Connection koneksi;

    BPJSStatusPulang(Connection koneksi) {
        this.koneksi = koneksi;
    }

    DataSEP data(String noSEP, String noRawat) throws Exception {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select no_sep,no_rawat,no_kartu,tglsep,jnspelayanan,lakalantas from bridging_sep where no_sep=?")) {
            ps.setString(1, noSEP);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !noRawat.equals(rs.getString("no_rawat"))) {
                    throw new Exception("SEP tidak sesuai dengan kunjungan pasien yang dipilih.");
                }
                DataSEP d = new DataSEP();
                d.noSEP = rs.getString("no_sep");
                d.noRawat = rs.getString("no_rawat");
                d.noKartu = rs.getString("no_kartu");
                d.tanggal = rs.getString("tglsep");
                d.jenis = rs.getString("jnspelayanan");
                d.laka = rs.getString("lakalantas");
                return d;
            }
        }
    }

    boolean adaSKDP(String noSEP) throws Exception {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select 1 from bridging_surat_kontrol_bpjs where no_sep=? limit 1")) {
            ps.setString(1, noSEP);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    Hasil periksa(String noSEP, String noRawat) throws Exception {
        if (noSEP.isEmpty() || noRawat.isEmpty()) {
            return new Hasil(Status.KOSONG, "");
        }
        if (adaSKDP(noSEP)) {
            return new Hasil(Status.ADA_SKDP, "");
        }
        DataSEP d = data(noSEP, noRawat);
        if ("2".equals(d.jenis)) {
            return new Hasil(Status.RALAN, "");
        }
        if (!"1".equals(d.jenis)) {
            throw new Exception("Jenis pelayanan SEP belum dapat dipastikan dari data lokal.");
        }
        return new Hasil(Status.TANPA_SKDP, "");
    }

    Hasil update(String noSEP, String noRawat, String status, String tanggal,
            String noLP, String user) throws Exception {
        if (!SEDANG_KIRIM.add(noSEP)) {
            throw new Exception("Update pulang SEP ini sedang diproses. Silakan cek ulang.");
        }
        try {
            // Periksa SKDP lokal lagi agar data baru dari petugas lain ikut diperhitungkan.
            if (adaSKDP(noSEP)) {
                throw new Exception("SKDP untuk SEP ini sudah tersimpan. Update pulang tidak dikirim.");
            }
            DataSEP d = data(noSEP, noRawat);
            if (!"1".equals(d.jenis)) {
                throw new Exception("Update pulang hanya untuk SEP rawat inap.");
            }
            String tanggalBPJS = tanggal.substring(0, 10);
            LocalDate pulang = LocalDate.parse(tanggalBPJS);
            if (tanggal.length() != 10) {
                java.time.LocalDateTime.parse(tanggal,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (pulang.isBefore(LocalDate.parse(d.tanggal.substring(0, 10)))
                    || pulang.isAfter(LocalDate.now())) {
                throw new Exception("Tanggal pulang harus antara tanggal SEP dan hari ini.");
            }
            if (!status.matches("[135]")) {
                throw new Exception("Pilih status 1, 3, atau 5. Pasien meninggal diperbarui melalui VClaim.");
            }
            Map<String, Object> sep = new HashMap<String, Object>();
            sep.put("noSep", noSEP);
            sep.put("statusPulang", status);
            sep.put("tglPulang", tanggalBPJS);
            sep.put("noSuratMeninggal", "");
            sep.put("tglMeninggal", "");
            sep.put("noLPManual", noLP.trim());
            sep.put("user", user);
            Map<String, Object> request = new HashMap<String, Object>();
            request.put("t_sep", sep);
            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("request", request);
            kirimUpdate(new ObjectMapper().writeValueAsString(payload));
            // Tanggal lokal hanya disimpan setelah BPJS menyatakan berhasil.
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "update bridging_sep set tglpulang=? where no_sep=? and no_rawat=? and jnspelayanan='1'")) {
                ps.setString(1, tanggal.length() == 10 ? tanggal + " 00:00:00" : tanggal);
                ps.setString(2, noSEP);
                ps.setString(3, noRawat);
                if (ps.executeUpdate() != 1) {
                    throw new Exception("Data SEP lokal tidak ditemukan.");
                }
            } catch (Exception ex) {
                return new Hasil(Status.BERHASIL, tanggalBPJS,
                        "BPJS sudah menerima update pulang, tetapi tanggal lokal belum tersimpan. "
                        + "Periksa data SEP lokal; jangan mengirim ulang update.");
            }
            return new Hasil(Status.BERHASIL, tanggalBPJS);
        } finally {
            SEDANG_KIRIM.remove(noSEP);
        }
    }

    private void kirimUpdate(String json) throws Exception {
        ApiBPJS api = new ApiBPJS(); // Hindari field ApiBPJS bersama antarworker.
        String utc = String.valueOf(api.GetUTCdatetimeAsString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("X-Cons-ID", koneksiDB.CONSIDAPIBPJS());
        headers.add("X-Timestamp", utc);
        headers.add("X-Signature", api.getHmac(utc));
        headers.add("user_key", koneksiDB.USERKEYAPIBPJS());
        RestTemplate rest = api.getRest();
        HttpComponentsClientHttpRequestFactory factory =
                (HttpComponentsClientHttpRequestFactory) rest.getRequestFactory();
        try {
            // Gunakan API Apache HttpClient yang juga dipakai ApiBPJS.getRest().
            org.apache.http.params.HttpConnectionParams.setConnectionTimeout(
                    factory.getHttpClient().getParams(), 10000);
            org.apache.http.params.HttpConnectionParams.setSoTimeout(
                    factory.getHttpClient().getParams(), 15000);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rest.exchange(koneksiDB.URLAPIBPJS() + "/SEP/2.0/updtglplg",
                    HttpMethod.PUT, new HttpEntity<String>(json, headers), String.class).getBody());
            JsonNode meta = root.path("metaData");
            if (!"200".equals(meta.path("code").asText())) {
                throw new Exception("BPJS: " + (meta.has("message")
                        ? meta.path("message").asText() : "Respons tidak dikenali."));
            }
        } finally {
            factory.getHttpClient().getConnectionManager().shutdown();
        }
    }
}
