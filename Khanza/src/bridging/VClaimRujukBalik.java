package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public final class VClaimRujukBalik {
    private static final ZoneId WITA = ZoneId.of("Asia/Makassar");
    private static final String INSERT = "/Rujukan/2.0/insert";
    private static final Set<String> PERLU_CEK_VCLAIM = ConcurrentHashMap.newKeySet();

    private VClaimRujukBalik() {
    }

    public static List<Sep> cariSep(String noRawat, String noSep) throws SQLException {
        String sql = "select no_sep,no_rawat,tglsep,jnspelayanan,diagawal,"
                + "nmdiagnosaawal,no_kartu,kdppkpelayanan,nomr,nama_pasien from bridging_sep where "
                + (noSep == null ? "no_rawat=?" : "no_sep=?")
                + " order by tglsep desc, no_sep desc";
        List<Sep> hasil = new ArrayList<>();
        try (Connection koneksi = koneksiDB.condbTransaksi();
                PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noSep == null ? noRawat : noSep);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new Sep(rs));
                }
            }
        }
        return hasil;
    }

    public static Map<Tipe, List<String>> riwayat(String noSep) throws SQLException {
        try (Connection koneksi = koneksiDB.condbTransaksi()) {
            return riwayat(koneksi, noSep);
        }
    }

    private static Map<Tipe, List<String>> riwayat(Connection koneksi, String noSep) throws SQLException {
        Map<Tipe, List<String>> hasil = new EnumMap<>(Tipe.class);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select no_rujukan,tipeRujukan from bridging_rujukan_bpjs where no_sep=? "
                + "and tipeRujukan in ('0. Penuh','1. Partial','2. Rujuk Balik') "
                + "order by tglRujukan desc,no_rujukan desc")) {
            ps.setString(1, noSep);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tipe tipe = Tipe.dariLabel(rs.getString("tipeRujukan"));
                    if (tipe != null) {
                        hasil.computeIfAbsent(tipe, k -> new ArrayList<>()).add(rs.getString("no_rujukan"));
                    }
                }
            }
        }
        return hasil;
    }

    public static String kirim(Sep sep, Data data, String link, ApiBPJS api) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = buatPermintaan(mapper, sep, data);
        String timestamp = String.valueOf(api.GetUTCdatetimeAsString());
        HttpHeaders headers = header(api, timestamp, MediaType.APPLICATION_FORM_URLENCODED);
        RestTemplate rest = restVClaim(api);
        return terbitkan(sep, data, () -> {
            String body = rest.exchange(link + INSERT, HttpMethod.POST,
                    new HttpEntity<String>(payload, headers), String.class).getBody();
            JsonNode root = mapper.readTree(body);
            JsonNode metadata = root.path("metaData");
            String kode = metadata.path("code").asText();
            if (kode.isEmpty()) {
                throw new IllegalStateException("Kode respons VClaim kosong.");
            }
            if (!"200".equals(kode)) {
                throw new Ditolak(metadata.path("message").asText());
            }
            JsonNode response = root.path("response");
            if (response.isTextual()) {
                response = mapper.readTree(api.Decrypt(response.asText(), timestamp));
            }
            return response.path("rujukan").path("noRujukan").asText().trim();
        });
    }

    public static Faskes cariFaskesPeserta(Sep sep, String link, ApiBPJS api) throws Exception {
        String kartu = sep.kartu.trim();
        if (!kartu.matches("[0-9]+")) {
            throw new Ditolak("Nomor kartu BPJS pada SEP kosong atau tidak valid.");
        }
        if (link.isEmpty()) {
            throw new Ditolak("Alamat layanan BPJS VClaim belum tersedia.");
        }

        String timestamp = String.valueOf(api.GetUTCdatetimeAsString());
        HttpHeaders headers = header(api, timestamp, MediaType.APPLICATION_JSON);
        RestTemplate rest = restVClaim(api);
        ObjectMapper mapper = new ObjectMapper();
        String url = link + "/Peserta/nokartu/" + kartu + "/tglSEP/" + LocalDate.now(WITA);
        String body = rest.exchange(url, HttpMethod.GET, new HttpEntity<Void>(headers), String.class).getBody();
        JsonNode root = mapper.readTree(body);
        JsonNode metadata = root.path("metaData");
        if (!"200".equals(metadata.path("code").asText())) {
            throw new Ditolak("VClaim belum dapat mengonfirmasi data peserta. Periksa nomor kartu dan status peserta.");
        }
        JsonNode response = root.path("response");
        if (response.isTextual()) {
            // Dekripsi tanpa menulis respons kepesertaan ke log.
            ApiBPJSAesKeySpec kunci = ApiBPJSEnc.generateKey(
                    koneksiDB.CONSIDAPIBPJS() + koneksiDB.SECRETKEYAPIBPJS() + timestamp);
            String hasil = ApiBPJSEnc.decrypt(response.asText(), kunci.getKey(), kunci.getIv());
            response = mapper.readTree(ApiBPJSLZString.decompressFromEncodedURIComponent(hasil));
        }
        JsonNode peserta = response.path("peserta");
        String kartuRespons = teks(peserta.path("noKartu"));
        if (!kartu.equals(kartuRespons)) {
            throw new Ditolak("Nomor kartu pada respons VClaim tidak cocok dengan SEP.");
        }
        JsonNode provider = peserta.path("provUmum");
        String kode = teks(provider.path("kdProvider"));
        String nama = teks(provider.path("nmProvider"));
        if (kode.isEmpty() || nama.isEmpty()) {
            throw new Ditolak("FKTP terdaftar belum tersedia pada data peserta BPJS.");
        }
        return new Faskes(kode, nama);
    }

    private static String teks(JsonNode nilai) {
        return nilai.isValueNode() && !nilai.isNull() ? nilai.asText().trim() : "";
    }

    private static HttpHeaders header(ApiBPJS api, String timestamp, MediaType jenis) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(jenis);
        headers.add("X-Cons-ID", koneksiDB.CONSIDAPIBPJS());
        headers.add("X-Timestamp", timestamp);
        headers.add("X-Signature", api.getHmac(timestamp));
        headers.add("user_key", koneksiDB.USERKEYAPIBPJS());
        return headers;
    }

    private static RestTemplate restVClaim(ApiBPJS api) throws Exception {
        RestTemplate rest = api.getRest();
        if (rest.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
            HttpComponentsClientHttpRequestFactory factory =
                    (HttpComponentsClientHttpRequestFactory) rest.getRequestFactory();
            HttpParams parameter = factory.getHttpClient().getParams();
            HttpConnectionParams.setConnectionTimeout(parameter, 10000);
            HttpConnectionParams.setSoTimeout(parameter, 20000);
        }
        return rest;
    }

    static String buatPermintaan(ObjectMapper mapper, Sep sep, Data data) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode rujukan = root.putObject("request").putObject("t_rujukan");
        rujukan.put("noSep", sep.nomor);
        rujukan.put("tglRujukan", data.tanggal.toString());
        rujukan.put("tglRencanaKunjungan", data.kunjungan.toString());
        rujukan.put("ppkDirujuk", data.kodeFaskes);
        rujukan.put("jnsPelayanan", data.pelayanan);
        rujukan.put("catatan", data.catatan);
        rujukan.put("diagRujukan", data.kodeDiagnosa);
        rujukan.put("tipeRujukan", data.tipe.kode);
        rujukan.put("poliRujukan", data.kodePoli);
        rujukan.put("user", data.user);
        return mapper.writeValueAsString(root);
    }

    static String terbitkan(Sep sep, Data data, Penerbit penerbit) throws Exception {
        try (Connection koneksi = koneksiDB.condbTransaksi()) {
            koneksi.setAutoCommit(false);
            try {
                kunciSep(koneksi, sep);
                List<String> lama = riwayat(koneksi, sep.nomor).getOrDefault(data.tipe, List.of());
                if (!lama.isEmpty()) {
                    throw new SudahTerbit(lama);
                }
                if (PERLU_CEK_VCLAIM.contains(sep.nomor)) {
                    throw new PeriksaVClaim("", null);
                }
                String nomor;
                try {
                    nomor = penerbit.kirim();
                    if (nomor.isEmpty()) {
                        throw new IllegalStateException("Nomor rujukan kosong.");
                    }
                } catch (Ditolak e) {
                    throw e;
                } catch (Exception e) {
                    PERLU_CEK_VCLAIM.add(sep.nomor);
                    throw new PeriksaVClaim("", e);
                }
                try {
                    simpan(koneksi, sep, data, nomor);
                    koneksi.commit();
                } catch (SQLException e) {
                    PERLU_CEK_VCLAIM.add(sep.nomor);
                    throw new PeriksaVClaim(nomor, e);
                }
                return nomor;
            } catch (Exception e) {
                try {
                    koneksi.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                throw e;
            }
        }
    }

    private static void kunciSep(Connection koneksi, Sep sep) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select no_rawat from bridging_sep where no_sep=? for update")) {
            ps.setString(1, sep.nomor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !sep.noRawat.equals(rs.getString(1))) {
                    throw new SQLException("SEP sudah berubah atau dihapus.");
                }
            }
        }
    }

    private static void simpan(Connection koneksi, Sep sep, Data data, String nomor) throws SQLException {
        String sql = "insert into bridging_rujukan_bpjs (no_sep,tglRujukan,tglRencanaKunjungan,ppkDirujuk,"
                + "nm_ppkDirujuk,jnsPelayanan,catatan,diagRujukan,nama_diagRujukan,tipeRujukan,poliRujukan,"
                + "nama_poliRujukan,no_rujukan,user) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, sep.nomor);
            ps.setDate(2, data.tanggal);
            ps.setDate(3, data.kunjungan);
            ps.setString(4, data.kodeFaskes);
            ps.setString(5, data.namaFaskes);
            ps.setString(6, data.pelayanan);
            ps.setString(7, data.catatan);
            ps.setString(8, data.kodeDiagnosa);
            ps.setString(9, data.namaDiagnosa);
            ps.setString(10, data.tipe.label);
            ps.setString(11, data.kodePoli);
            ps.setString(12, data.namaPoli);
            ps.setString(13, nomor);
            ps.setString(14, data.user);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into rujuk (no_rujuk,no_rawat,rujuk_ke,tgl_rujuk,keterangan_diagnosa,kd_dokter,"
                + "kat_rujuk,ambulance,keterangan,jam) select ?,r.no_rawat,?,?,?,d.kd_dokter,'-','-',?,current_time "
                + "from reg_periksa r left join dokter d on d.kd_dokter=r.kd_dokter where r.no_rawat=?")) {
            ps.setString(1, nomor);
            ps.setString(2, data.namaFaskes);
            ps.setDate(3, data.tanggal);
            ps.setString(4, data.namaDiagnosa);
            ps.setString(5, data.catatan);
            ps.setString(6, sep.noRawat);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Kunjungan tidak ditemukan saat menyimpan rujukan.");
            }
        }
    }

    @FunctionalInterface
    interface Penerbit {
        String kirim() throws Exception;
    }

    public static final class Faskes {
        public final String kode, nama;

        private Faskes(String kode, String nama) {
            this.kode = kode;
            this.nama = nama;
        }
    }

    public static final class Sep {
        public final String nomor, noRawat, kodeDiagnosa, namaDiagnosa;
        private final String pelayanan;
        public final String kartu, kodePpkPelayanan, noRm, namaPasien;
        final Date tanggal;

        Sep(ResultSet rs) throws SQLException {
            nomor = rs.getString("no_sep");
            noRawat = Objects.toString(rs.getString("no_rawat"), "");
            tanggal = rs.getDate("tglsep");
            pelayanan = rs.getString("jnspelayanan");
            kodeDiagnosa = rs.getString("diagawal");
            namaDiagnosa = rs.getString("nmdiagnosaawal");
            noRm = Objects.toString(rs.getString("nomr"), "");
            namaPasien = Objects.toString(rs.getString("nama_pasien"), "");
            kartu = Objects.toString(rs.getString("no_kartu"), "");
            kodePpkPelayanan = Objects.toString(rs.getString("kdppkpelayanan"), "");
        }

        @Override public String toString() {
            String jenis = "1".equals(pelayanan) ? "Rawat Inap" : "Rawat Jalan";
            return nomor + "  |  " + Objects.toString(tanggal, "-") + "  |  " + jenis;
        }
    }

    public enum Tipe {
        PENUH("0", "Penuh"),
        PARTIAL("1", "Partial"),
        RUJUK_BALIK("2", "Rujuk Balik");

        private final String kode;
        private final String label;

        Tipe(String kode, String nama) {
            this.kode = kode;
            this.label = kode + ". " + nama;
        }

        public boolean tampilKunjungan(String pelayanan) {
            return this != PARTIAL || "2".equals(pelayanan);
        }

        public boolean tampilPoli(String pelayanan) {
            return this != RUJUK_BALIK && tampilKunjungan(pelayanan);
        }

        private static Tipe dariLabel(String label) {
            for (Tipe tipe : values()) {
                if (tipe.label.equals(label)) {
                    return tipe;
                }
            }
            return null;
        }

        @Override public String toString() {
            return label;
        }
    }

    public static final class Data {
        final Tipe tipe;
        final Date tanggal, kunjungan;
        final String kodeFaskes, namaFaskes, pelayanan, catatan, kodeDiagnosa, namaDiagnosa, kodePoli, namaPoli, user;

        public Data(Tipe tipe, Date tanggal, Date kunjungan, String kodeFaskes, String namaFaskes, String pelayanan,
                String catatan, String kodeDiagnosa, String namaDiagnosa, String kodePoli, String namaPoli, String user) {
            this.tipe = tipe;
            this.tanggal = tanggal;
            this.pelayanan = tipe == Tipe.RUJUK_BALIK ? "2" : pelayanan;
            this.kunjungan = tipe.tampilKunjungan(this.pelayanan) ? kunjungan : tanggal;
            this.kodeFaskes = kodeFaskes;
            this.namaFaskes = namaFaskes;
            this.catatan = catatan;
            this.kodeDiagnosa = kodeDiagnosa;
            this.namaDiagnosa = namaDiagnosa;
            this.kodePoli = tipe.tampilPoli(this.pelayanan) ? kodePoli : "";
            this.namaPoli = tipe.tampilPoli(this.pelayanan) ? namaPoli : "";
            this.user = user;
        }
    }

    public static final class Ditolak extends Exception {
        Ditolak(String pesan) {
            super(pesan);
        }
    }

    public static final class SudahTerbit extends Exception {
        public final List<String> nomor;

        SudahTerbit(List<String> nomor) {
            this.nomor = nomor;
        }
    }

    public static final class PeriksaVClaim extends Exception {
        public final String nomor;

        PeriksaVClaim(String nomor, Exception penyebab) {
            super(penyebab);
            this.nomor = nomor;
        }
    }
}
