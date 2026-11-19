package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

final class JadwalKontrolHfis {
    private JadwalKontrolHfis() {
    }

    static List<JadwalKontrolRanap.Sesi> ambil(String dokter, String poli, LocalDate tanggal) {
        try {
            String link = koneksiDB.URLAPIMOBILEJKN().trim();
            if (link.isEmpty()) {
                return null;
            }
            ApiMobileJKN api = new ApiMobileJKN();
            String utc = Long.toString(api.GetUTCdatetimeAsString());
            HttpHeaders headers = new HttpHeaders();
            headers.add("x-cons-id", koneksiDB.CONSIDAPIMOBILEJKN());
            headers.add("x-timestamp", utc);
            headers.add("x-signature", api.getHmac(utc));
            headers.add("user_key", koneksiDB.USERKEYAPIMOBILEJKN());

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String method) throws IOException {
                    super.prepareConnection(connection, method);
                    connection.setConnectTimeout(3500);
                    connection.setReadTimeout(5000);
                }
            };
            String body = new RestTemplate(factory).exchange(link.replaceFirst("/+$", "")
                    + "/jadwaldokter/kodepoli/{poli}/tanggal/{tanggal}", HttpMethod.GET,
                    new HttpEntity<Void>(headers), String.class, poli, tanggal.toString()).getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);
            if (!"200".equals(root.path("metadata").path("code").asText())) {
                return null;
            }
            JsonNode response = root.path("response");
            if (response.isTextual()) {
                response = mapper.readTree(api.Decrypt(response.asText(), utc));
            }
            return baca(response, dokter, tanggal);
        } catch (Exception ex) {
            System.out.println("Jadwal HFIS SKDP: " + ex.getClass().getSimpleName());
            return null;
        }
    }

    static List<JadwalKontrolRanap.Sesi> baca(JsonNode data, String dokter, LocalDate tanggal) {
        if (!data.isArray()) {
            return null;
        }
        Map<String, JadwalKontrolRanap.Sesi> menurutJam = new LinkedHashMap<>();
        for (JsonNode baris : data) {
            if (!dokter.equals(baris.path("kodedokter").asText().trim())) {
                continue;
            }
            String jam = baris.path("jadwal").asText().trim();
            String liburHfis = baris.path("libur").asText();
            boolean libur = "1".equals(liburHfis) || "true".equalsIgnoreCase(liburHfis);
            String kapasitas = baris.path("kapasitaspasien").asText().trim();
            if (!libur && (jam.isEmpty() || !kapasitas.matches("[0-9]{1,9}"))) {
                return null;
            }
            JadwalKontrolRanap.Sesi sesi = new JadwalKontrolRanap.Sesi(JadwalKontrolRanap.hari(tanggal),
                    jam.isEmpty() ? "—" : jam, libur ? 0 : Integer.parseInt(kapasitas), libur);
            menurutJam.merge(sesi.jam, sesi, (lama, baru) -> {
                if (lama.libur != baru.libur) {
                    return lama.libur ? lama : baru;
                }
                return lama.kuota >= baru.kuota ? lama : baru;
            });
        }
        return menurutJam.isEmpty() ? null : new ArrayList<>(menurutJam.values());
    }
}
