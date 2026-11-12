package keuangan;

import bridging.ApiINACBG;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.http.params.CoreConnectionPNames;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

final class BillingInacbg {
    static final String BELUM_DIMUAT = "Hasil grouping belum dimuat.";
    static final String GAGAL_AKSES = "E-Klaim gagal diakses. Silakan coba muat ulang.";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String KOLOM_KLAIM = "no_sep,kode_cbg,kode_drg,tarif_inacbg,tarif_idrg,tambahan_biaya,"
            + "pembayar_tambahan,tarif_poli_eksekutif,status_klaim,status_grouping,rincian_grouping,"
            + "final_grouping,status_data,waktu_ambil";

    private final Connection db;
    final String jenis;
    private final String tabelNota;

    BillingInacbg(Connection db, boolean ranap) {
        this.db = db;
        jenis = ranap ? "1" : "2";
        tabelNota = ranap ? "nota_inap" : "nota_jalan";
    }

    Context context(String rawat) throws SQLException {
        return context(rawat, false);
    }

    Context upgradeContext(String rawat) throws SQLException {
        if (!"1".equals(jenis)) return null;
        Context context = context(rawat);
        if (context == null || context.sep == null) return null;
        String sql = "select s.klsrawat, (select k.kelas from kamar_inap ki"
                + " join kamar k on k.kd_kamar=ki.kd_kamar where ki.no_rawat=s.no_rawat"
                + " order by (ki.stts_pulang='-') desc,ki.tgl_masuk desc,ki.jam_masuk desc limit 1) as kelas_kamar"
                + " from bridging_sep s where s.no_sep=? and s.no_rawat=? and s.jnspelayanan='1'";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, context.sep);
            ps.setString(2, rawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String hak = rs.getString("klsrawat");
                String kamar = rs.getString("kelas_kamar");
                if (hak == null || !hak.matches("[1-3]") || kamar == null) return null;
                int kelasKamar;
                switch (kamar) {
                    case "Kelas Utama": case "Kelas VIP": case "Kelas VVIP": kelasKamar = 0; break;
                    case "Kelas 1": kelasKamar = 1; break;
                    case "Kelas 2": kelasKamar = 2; break;
                    case "Kelas 3": kelasKamar = 3; break;
                    default: return null;
                }
                return kelasKamar < Integer.parseInt(hak)
                        ? new Context(context.rawat, context.nota, context.sep, context.status, hak, kamar) : null;
            }
        }
    }

    private Context context(String rawat, boolean lock) throws SQLException {
        String sql = "select n.no_nota, s.no_sep from reg_periksa r left join " + tabelNota
                + " n on n.no_rawat=r.no_rawat left join bridging_sep s on s.no_rawat=r.no_rawat"
                + " and s.jnspelayanan=? and s.no_sep<>'' where r.no_rawat=? and r.kd_pj='BPJ' limit 2";
        if (lock) sql += " for update";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, jenis);
            ps.setString(2, rawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String nota = rs.getString(1);
                String sep = rs.getString(2);
                return rs.next() ? new Context(rawat, nota, null, "Ada lebih dari satu SEP untuk jenis layanan ini.")
                        : new Context(rawat, nota, sep, sep == null ? "SEP belum tersedia." : BELUM_DIMUAT);
            }
        }
    }

    void lockForSave(Context context) throws SQLException {
        try (PreparedStatement ps = db.prepareStatement("select kd_pj from reg_periksa where no_rawat=? for update")) {
            ps.setString(1, context.rawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"BPJ".equals(rs.getString(1))) {
                    throw new SQLException("Penjamin pasien berubah. Muat ulang billing.");
                }
            }
        }
        try (PreparedStatement ps = db.prepareStatement("select 1 from billing where no_rawat=? limit 1")) {
            ps.setString(1, context.rawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) throw new SQLException("Billing sudah disimpan. Buka ulang nota.");
            }
        }
    }

    static Claim fetch(Context context, String jenis) throws Exception {
        ObjectNode request = JSON.createObjectNode();
        request.putObject("metadata").put("method", "get_claim_data");
        request.putObject("data").put("nomor_sep", context.sep);
        ApiINACBG api = new ApiINACBG() {
            @Override
            public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
                RestTemplate rest = super.getRest();
                HttpComponentsClientHttpRequestFactory factory =
                        (HttpComponentsClientHttpRequestFactory) rest.getRequestFactory();
                // Spring 3.1.0.M2 exposes the underlying Apache client for this timeout.
                factory.getHttpClient().getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 4000);
                factory.setReadTimeout(8000);
                return rest;
            }
        };
        return parse(context, jenis, JSON.readTree(api.request(request.toString()).toString()));
    }

    static Claim parse(Context context, String jenis, JsonNode response) {
        if (!"200".equals(text(response.path("metadata"), "code"))) {
            return Claim.pending(context, "E-Klaim belum mengembalikan data klaim.");
        }
        JsonNode data = response.path("response").path("data");
        if (!context.sep.equals(text(data, "nomor_sep")) || !jenis.equals(text(data, "jenis_rawat"))) {
            return Claim.pending(context, "SEP atau jenis layanan pada respons E-Klaim tidak sesuai.");
        }
        JsonNode cbg = data.path("grouper").path("response_inacbg");
        JsonNode drg = data.path("grouper").path("response_idrg");
        ObjectNode rincian = JSON.createObjectNode();
        copy(rincian, data, "kelas_rawat", "upgrade_class_ind", "upgrade_class_class",
                "add_payment_pct", "kemenkes_dc_status_cd", "bpjs_dc_status_cd");
        ObjectNode detailCbg = rincian.putObject("inacbg");
        copy(detailCbg, cbg, "base_tariff", "kelas", "sub_acute", "chronic", "special_cmg");
        copy(detailCbg.putObject("cbg"), cbg.path("cbg"), "description");
        copy(rincian.putObject("idrg"), drg, "drg_description", "status_cd", "mdc_description");
        Claim claim = new Claim(context.sep, rincian, new Timestamp(System.currentTimeMillis()));
        claim.kodeCbg = text(cbg.path("cbg"), "code");
        claim.kodeDrg = text(drg, "drg_code");
        claim.tarif = amount(cbg, "tariff");
        claim.tarifIdrg = amount(drg, "total_tarif");
        claim.tambahan = amount(data, "add_payment_amt");
        claim.pembayar = text(data, "upgrade_class_payor");
        claim.poliEksekutif = amount(data, "tarif_poli_eks");
        claim.statusKlaim = text(data, "klaim_status_cd");
        claim.statusGrouping = text(cbg, "status_cd");
        if (claim.kodeCbg.isEmpty() || "X-0-00-X".equals(claim.kodeCbg)
                || claim.tarif == null || claim.tarif.signum() == 0) {
            claim.status = "Hasil grouping INA-CBG belum tersedia.";
        } else if (!"final".equalsIgnoreCase(claim.statusGrouping)
                && !(claim.statusGrouping.isEmpty() && "final".equalsIgnoreCase(claim.statusKlaim))) {
            claim.status = "Grouping INA-CBG belum final.";
        } else if ("1".equals(jenis) && "1".equals(text(data, "upgrade_class_ind")) && claim.tambahan == null) {
            claim.status = "Nominal tambahan biaya naik kelas belum tersedia.";
        } else {
            claim.finalGrouping = true;
            claim.status = "Grouping INA-CBG final.";
        }
        return claim;
    }

    private static void copy(ObjectNode target, JsonNode source, String... names) {
        for (String name : names) {
            JsonNode node = source.get(name);
            if (node != null && !node.isNull()) target.set(name, node);
        }
    }

    static BigDecimal amount(JsonNode node, String field) {
        try {
            BigDecimal value = new BigDecimal(text(node, field)).setScale(2, RoundingMode.HALF_UP);
            return value.signum() < 0 || value.precision() > 18 ? null : value;
        } catch (NumberFormatException | ArithmeticException invalid) {
            return null;
        }
    }

    static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNull() || !value.isValueNode() ? "" : value.asText().trim();
    }

    void insert(Context expected, Claim claim, String nota, double total, String user) throws SQLException {
        Context current = context(expected.rawat, true);
        if (current == null || !nota.equals(current.nota)) {
            throw new SQLException("Nota atau penjamin berubah saat menyimpan grouping.");
        }
        if (!Objects.equals(expected.sep, current.sep)) claim = Claim.pending(current, "SEP berubah. Muat ulang klaim.");
        String sql = "insert into rsaj_billing_inacbg(jenis_layanan,no_nota,no_rawat,total_billing,"
                + "waktu_simpan,petugas_simpan," + KOLOM_KLAIM + ") values(?,?,?,?,now(),?,"
                + "?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, jenis);
            ps.setString(2, nota);
            ps.setString(3, expected.rawat);
            ps.setBigDecimal(4, BigDecimal.valueOf(total));
            ps.setString(5, user);
            bindClaim(ps, 6, claim);
            ps.executeUpdate();
        }
    }

    boolean complete(Context expected, Claim claim, String user) throws SQLException {
        if (!claim.finalGrouping || expected.nota == null) return false;
        Context current = context(expected.rawat);
        if (!expected.matches(current)) return false;
        String sql = "update rsaj_billing_inacbg a join " + tabelNota
                + " n on n.no_rawat=a.no_rawat and n.no_nota=a.no_nota"
                + " join reg_periksa r on r.no_rawat=n.no_rawat and r.kd_pj='BPJ'"
                + " join bridging_sep s on s.no_rawat=n.no_rawat and s.jnspelayanan=a.jenis_layanan and s.no_sep=?"
                + " set a." + KOLOM_KLAIM.replace(",", "=?,a.") + "=?,a.waktu_lengkapi=now(),a.petugas_lengkapi=?"
                + " where a.jenis_layanan=? and a.no_nota=? and a.no_rawat=? and a.final_grouping=0"
                + " and exists(select 1 from billing b where b.no_rawat=a.no_rawat)"
                + " and not exists(select 1 from bridging_sep other where other.no_rawat=a.no_rawat"
                + " and other.jnspelayanan=a.jenis_layanan and other.no_sep<>'' and other.no_sep<>s.no_sep)";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, expected.sep);
            int next = bindClaim(ps, 2, claim);
            ps.setString(next++, user);
            ps.setString(next++, jenis);
            ps.setString(next++, expected.nota);
            ps.setString(next, expected.rawat);
            return ps.executeUpdate() == 1;
        }
    }

    void attachToOldNota(Context expected, Claim claim, String user) throws SQLException {
        if (!claim.finalGrouping || expected.nota == null) return;
        String sql = "insert into rsaj_billing_inacbg(jenis_layanan,no_nota,no_rawat,total_billing,"
                + "waktu_simpan,petugas_simpan," + KOLOM_KLAIM + ")"
                + " select ?,n.no_nota,n.no_rawat,b.total,now(),?, ?,?,?,?,?,?,?,?,?,?,?,?,?,? from " + tabelNota + " n"
                + " join reg_periksa r on r.no_rawat=n.no_rawat and r.kd_pj='BPJ'"
                + " join bridging_sep s on s.no_rawat=n.no_rawat and s.no_sep=? and s.jnspelayanan=?"
                + " join (select no_rawat,tgl_byr,sum(totalbiaya) as total from billing where no_rawat=? and status<>'Tagihan'"
                + " group by no_rawat,tgl_byr) b on b.no_rawat=n.no_rawat and b.tgl_byr=n.tanggal"
                + " where n.no_rawat=? and n.no_nota=?"
                + " and not exists(select 1 from bridging_sep other where other.no_rawat=n.no_rawat"
                + " and other.jnspelayanan=s.jnspelayanan and other.no_sep<>'' and other.no_sep<>s.no_sep)"
                + " on duplicate key update final_grouping=rsaj_billing_inacbg.final_grouping";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, jenis);
            ps.setString(2, user);
            int next = bindClaim(ps, 3, claim);
            ps.setString(next++, expected.sep);
            ps.setString(next++, jenis);
            ps.setString(next++, expected.rawat);
            ps.setString(next++, expected.rawat);
            ps.setString(next, expected.nota);
            ps.executeUpdate();
        }
    }

    private static int bindClaim(PreparedStatement ps, int i, Claim claim) throws SQLException {
        ps.setString(i++, claim.sep);
        ps.setString(i++, claim.kodeCbg);
        ps.setString(i++, claim.kodeDrg);
        ps.setBigDecimal(i++, claim.tarif);
        ps.setBigDecimal(i++, claim.tarifIdrg);
        ps.setBigDecimal(i++, claim.tambahan);
        ps.setString(i++, claim.pembayar);
        ps.setBigDecimal(i++, claim.poliEksekutif);
        ps.setString(i++, claim.statusKlaim);
        ps.setString(i++, claim.statusGrouping);
        ps.setString(i++, claim.detail.toString());
        ps.setBoolean(i++, claim.finalGrouping);
        ps.setString(i++, claim.status);
        ps.setTimestamp(i++, claim.waktuAmbil);
        return i;
    }

    Claim saved(Context context) throws SQLException {
        if (context == null || context.nota == null) return null;
        try (PreparedStatement ps = db.prepareStatement("select " + KOLOM_KLAIM + ",total_billing"
                + " from rsaj_billing_inacbg where jenis_layanan=? and no_nota=? and no_rawat=?")) {
            ps.setString(1, jenis);
            ps.setString(2, context.nota);
            ps.setString(3, context.rawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                JsonNode detail;
                try {
                    detail = JSON.readTree(rs.getString("rincian_grouping"));
                } catch (Exception invalid) {
                    throw new SQLException("Rincian grouping pada nota tidak terbaca.", invalid);
                }
                Claim claim = new Claim(rs.getString("no_sep"), detail, rs.getTimestamp("waktu_ambil"));
                claim.kodeCbg = rs.getString("kode_cbg");
                claim.kodeDrg = rs.getString("kode_drg");
                claim.tarif = rs.getBigDecimal("tarif_inacbg");
                claim.tarifIdrg = rs.getBigDecimal("tarif_idrg");
                claim.tambahan = rs.getBigDecimal("tambahan_biaya");
                claim.pembayar = rs.getString("pembayar_tambahan");
                claim.poliEksekutif = rs.getBigDecimal("tarif_poli_eksekutif");
                claim.statusKlaim = rs.getString("status_klaim");
                claim.statusGrouping = rs.getString("status_grouping");
                claim.finalGrouping = rs.getBoolean("final_grouping");
                claim.status = rs.getString("status_data");
                claim.totalBilling = rs.getBigDecimal("total_billing");
                return claim;
            }
        }
    }

    void deleteForCancellation(String rawat) throws SQLException {
        try (PreparedStatement ps = db.prepareStatement("delete from rsaj_billing_inacbg where jenis_layanan=? and no_rawat=?")) {
            ps.setString(1, jenis);
            ps.setString(2, rawat);
            ps.executeUpdate();
        } catch (SQLException ex) {
            if (ex.getErrorCode() != 1146) throw ex;
        }
    }

    String printPath(Context context, String petugas, String secret, long expires) throws GeneralSecurityException {
        if (secret == null || secret.isEmpty()) throw new GeneralSecurityException("Kunci HybridWeb kosong");
        String payload = "billing-inacbg\n" + jenis + "\n" + context.rawat + "\n" + context.nota
                + "\n" + petugas + "\n" + expires;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        StringBuilder signature = new StringBuilder(64);
        for (byte value : mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))) {
            signature.append(Character.forDigit((value & 0xf0) >>> 4, 16));
            signature.append(Character.forDigit(value & 0x0f, 16));
        }
        return "billing/LaporanBillingINACBG.php?jenis=" + jenis
                + "&rawat=" + URLEncoder.encode(context.rawat, StandardCharsets.UTF_8)
                + "&nota=" + URLEncoder.encode(context.nota, StandardCharsets.UTF_8)
                + "&petugas=" + URLEncoder.encode(petugas, StandardCharsets.UTF_8)
                + "&exp=" + expires + "&sig=" + signature;
    }

    static final class Context {
        final String rawat, nota, sep, status;
        final String kelasHak, kelasKamar;

        Context(String rawat, String nota, String sep, String status) {
            this(rawat, nota, sep, status, null, null);
        }

        Context(String rawat, String nota, String sep, String status, String kelasHak, String kelasKamar) {
            this.rawat = rawat;
            this.nota = nota;
            this.sep = sep;
            this.status = status;
            this.kelasHak = kelasHak;
            this.kelasKamar = kelasKamar;
        }

        boolean matches(Context other) {
            return other != null && rawat.equals(other.rawat) && Objects.equals(nota, other.nota)
                    && Objects.equals(sep, other.sep) && status.equals(other.status);
        }
    }

    static final class Claim {
        final String sep;
        final JsonNode detail;
        final Timestamp waktuAmbil;
        String kodeCbg, kodeDrg, pembayar, statusKlaim, statusGrouping, status;
        BigDecimal tarif, tarifIdrg, tambahan, poliEksekutif, totalBilling;
        boolean finalGrouping;

        private Claim(String sep, JsonNode detail, Timestamp waktuAmbil) {
            this.sep = sep;
            this.detail = detail;
            this.waktuAmbil = waktuAmbil;
        }

        static Claim pending(Context context, String status) {
            Claim claim = new Claim(context.sep, JSON.createObjectNode(), null);
            claim.status = status;
            return claim;
        }
    }
}
