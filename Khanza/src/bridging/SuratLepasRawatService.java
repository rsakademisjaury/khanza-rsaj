package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Layanan terisolasi: transaksi tidak memakai koneksi bersama form SIMRS. */
final class SuratLepasRawatService {
    interface Connections { Connection open() throws SQLException; }
    interface Gateway {
        void ubah(Data data, LocalDate tanggal, String petugas) throws Exception;
        Remote baca(String nomor) throws Exception;
    }
    static final class Ditolak extends Exception {
        Ditolak(String pesan) { super(pesan); }
    }
    static final class Remote {
        String noSurat, noSEP, dokter, poli;
        LocalDate tanggal;
    }
    static final class Data {
        String noRawat, noSEP, noRM, nama, noSurat, dokter, namaDokter, poli, namaPoli;
        String noKartu, lahir, jk, diagnosaAwal;
        String asal, petugasAwal, dicatat, pending, diagnosa, obat, kontrolResume, berkasStatus;
        LocalDate tanggal, awal, tanggalSurat;
        @Override public String toString() { return noRawat + " / " + noSurat; }
    }
    private final Connections connections;
    private final Gateway gateway;
    private final SuratLepasRawatBerkas berkas;
    SuratLepasRawatService() { this(() -> koneksiDB.condbTransaksi(), new BPJSGateway()); }
    SuratLepasRawatService(Connections connections, Gateway gateway) {
        this(connections,gateway,new SuratLepasRawatBerkas(connections,new SuratLepasRawatFileGateway()));
    }
    SuratLepasRawatService(Connections connections, Gateway gateway,SuratLepasRawatBerkas berkas) {
        this.connections = connections;
        this.gateway = gateway;
        this.berkas = berkas;
    }

    static void cekAkses(boolean boleh) throws Exception {
        if (!boleh) throw new Exception("Anda tidak memiliki hak akses Surat Kontrol BPJS.");
    }
    void siap(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM information_schema.TRIGGERS "
                + "WHERE TRIGGER_SCHEMA=DATABASE() AND TRIGGER_NAME IN ('rsaj_slr_insert','rsaj_slr_guard_update',"
                + "'rsaj_slr_update','rsaj_slr_guard_delete','rsaj_slr_delete','rsaj_slr_awal_lock_update',"
                + "'rsaj_slr_awal_lock_delete','rsaj_slr_riwayat_lock_update','rsaj_slr_riwayat_lock_delete')");
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next() || rs.getInt(1) != 9) throw new SQLException(
                    "Penyimpanan audit belum lengkap. Jalankan rsaj_surat_lepas_rawat.sql terlebih dahulu.");
        }
        try (PreparedStatement ps = c.prepareStatement("SELECT a.no_surat,r.id,k.id FROM rsaj_lepas_rawat_awal a "
                + "LEFT JOIN rsaj_lepas_rawat_riwayat r ON 1=0 LEFT JOIN rsaj_lepas_rawat_kirim k ON 1=0 LIMIT 0")) {
            ps.executeQuery().close();
        }
        SuratLepasRawatBerkas.siap(c);
    }

    private static final String SELECT = "SELECT s.no_rawat,s.no_sep,s.nomr,s.nama_pasien,s.no_kartu,s.tanggal_lahir,s.jkel,s.nmdiagnosaawal,"
            + "k.no_surat,k.tgl_surat,k.tgl_rencana,k.kd_dokter_bpjs,k.nm_dokter_bpjs,"
            + "k.kd_poli_bpjs,k.nm_poli_bpjs,a.tgl_kontrol_awal,a.asal_tanggal,a.petugas,a.dicatat_pada,"
            + "(SELECT status FROM rsaj_lepas_rawat_kirim q WHERE q.no_surat=k.no_surat "
            + "AND q.status IN ('DIPROSES','PERLU_CEK') ORDER BY q.dibuat DESC LIMIT 1) AS pending,"
            + "(SELECT status FROM rsaj_lepas_rawat_berkas_job z WHERE z.no_surat=k.no_surat "
            + "AND z.status IN ('MENUNGGU','PROSES','GAGAL') ORDER BY z.id DESC LIMIT 1) AS berkas_status,"
            + "r.diagnosa_utama,r.obat_pulang,r.kontrol FROM bridging_sep s "
            + "LEFT JOIN bridging_surat_kontrol_bpjs k ON k.no_sep=s.no_sep "
            + "LEFT JOIN rsaj_lepas_rawat_awal a ON a.no_surat=k.no_surat "
            + "LEFT JOIN resume_pasien_ranap r ON r.no_rawat=s.no_rawat WHERE s.jnspelayanan='1' ";

    List<Data> daftar(String noRM) throws Exception {
        List<Data> result = new ArrayList<>();
        try (Connection c = connections.open()) {
            siap(c);
            try (PreparedStatement ps = c.prepareStatement(SELECT + "AND s.nomr=? ORDER BY s.tglsep DESC,k.tgl_surat DESC")) {
                ps.setString(1, noRM);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(map(rs)); }
            }
        }
        return result;
    }
    private Data ambil(Connection c, String nomor, boolean lock) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "AND k.no_surat=?" + (lock ? " FOR UPDATE" : ""))) {
            ps.setString(1, nomor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Surat/SEP rawat inap asal tidak ditemukan. Muat ulang daftar.");
                return map(rs);
            }
        }
    }
    private static LocalDate tanggal(ResultSet rs, String name) throws SQLException {
        Date value = rs.getDate(name);
        return value == null ? null : value.toLocalDate();
    }
    private static Data map(ResultSet rs) throws SQLException {
        Data d = new Data();
        d.noRawat=rs.getString("no_rawat"); d.noSEP=rs.getString("no_sep");
        d.noKartu=rs.getString("no_kartu"); d.lahir=rs.getString("tanggal_lahir");
        d.jk=rs.getString("jkel"); d.diagnosaAwal=rs.getString("nmdiagnosaawal");
        d.noRM=rs.getString("nomr"); d.nama=rs.getString("nama_pasien"); d.noSurat=rs.getString("no_surat");
        d.tanggal=tanggal(rs,"tgl_rencana"); d.awal=tanggal(rs,"tgl_kontrol_awal");
        d.tanggalSurat=tanggal(rs,"tgl_surat"); d.dokter=rs.getString("kd_dokter_bpjs");
        d.namaDokter=rs.getString("nm_dokter_bpjs"); d.poli=rs.getString("kd_poli_bpjs");
        d.namaPoli=rs.getString("nm_poli_bpjs"); d.asal=rs.getString("asal_tanggal");
        d.petugasAwal=rs.getString("petugas"); d.dicatat=rs.getString("dicatat_pada");
        d.pending=rs.getString("pending"); d.diagnosa=rs.getString("diagnosa_utama");
        d.berkasStatus=rs.getString("berkas_status");
        d.obat=rs.getString("obat_pulang"); d.kontrolResume=rs.getString("kontrol");
        return d;
    }

    List<Object[]> riwayat(String nomor) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(
                "SELECT waktu,aksi,tgl_sebelum,tgl_sesudah,petugas,alasan,sumber FROM rsaj_lepas_rawat_riwayat "
                + "WHERE no_surat=? ORDER BY id DESC")) {
            ps.setString(1, nomor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),
                    rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7)});
            }
        }
        return rows;
    }

    String ubah(Data dipilih, LocalDate baru, String alasan, String petugas, boolean boleh) throws Exception {
        cekAkses(boleh);
        if (dipilih == null || dipilih.noSurat == null || dipilih.tanggal == null)
            throw new Exception("Pilih Surat Lepas Rawat yang sudah memiliki SKDP dan tanggal kontrol.");
        if (baru == null || baru.isBefore(LocalDate.now())) throw new Exception("Tanggal kontrol tidak boleh sebelum hari ini.");
        if (baru.equals(dipilih.tanggal)) throw new Exception("Tanggal kontrol belum berubah.");
        if (alasan == null || alasan.trim().isEmpty()) throw new Exception("Isi alasan perubahan tanggal kontrol.");
        if (alasan.trim().length() > 1000) throw new Exception("Alasan maksimal 1.000 karakter.");
        String id = UUID.randomUUID().toString();
        Data aktual;
        try (Connection c = connections.open()) {
            siap(c);
            c.setAutoCommit(false);
            try {
                aktual=ambil(c,dipilih.noSurat,true);
                if (!sama(dipilih,aktual)) throw new Exception("Data berubah oleh petugas lain. Muat ulang sebelum menyimpan.");
                if (aktual.pending != null) throw new Exception("Ada pengiriman belum pasti. Gunakan Cek Status BPJS dahulu.");
                try (PreparedStatement ps=c.prepareStatement("INSERT INTO rsaj_lepas_rawat_kirim "
                        + "(id,no_surat,no_sep,tgl_sebelum,tgl_sesudah,kode_dokter,kode_poli,petugas,alasan,status,pesan,dibuat,diperbarui) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,'DIPROSES','',NOW(),NOW())")) {
                    ps.setString(1,id); ps.setString(2,aktual.noSurat); ps.setString(3,aktual.noSEP);
                    ps.setDate(4,Date.valueOf(aktual.tanggal)); ps.setDate(5,Date.valueOf(baru));
                    ps.setString(6,aktual.dokter); ps.setString(7,aktual.poli); ps.setString(8,petugas);
                    ps.setString(9,alasan.trim()); ps.executeUpdate();
                }
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
        }
        try {
            gateway.ubah(aktual,baru,petugas);
        } catch (Ditolak e) {
            status(id,"DITOLAK",e.getMessage());
            throw new Exception("BPJS menolak perubahan: " + e.getMessage() + "\nTanggal lokal tetap.");
        } catch (Exception e) {
            status(id,"PERLU_CEK","Respons pengiriman belum dapat dipastikan.");
            throw new Exception("Respons BPJS belum dapat dipastikan. Tanggal lokal tetap.\nGunakan Cek Status BPJS sebelum mengirim ulang.");
        }
        try { terapkan(id); }
        catch (Exception e) {
            status(id,"PERLU_CEK","BPJS berhasil, penyimpanan lokal perlu diselaraskan.");
            throw new Exception("BPJS sudah menerima perubahan, tetapi penyimpanan lokal belum selesai.\nGunakan Cek Status BPJS untuk menyelaraskan; jangan buat surat baru.", e);
        }
        return "Tanggal kontrol BPJS dan riwayat perubahan berhasil disimpan.\n"+perbaruiBerkas(dipilih.noSurat);
    }

    private String perbaruiBerkas(String surat) {
        try { return berkas.proses(surat); }
        catch(Exception e) { return "Tanggal kontrol sudah tersimpan, tetapi berkas belum terganti.\n"
                + e.getMessage()+"\nGunakan Perbarui Berkas; perubahan tanggal tidak perlu dikirim ulang."; }
    }
    List<SuratLepasRawatBerkas.Ref> kandidatBerkas(String surat,boolean boleh) throws Exception {
        cekAkses(boleh);return berkas.kandidat(surat);
    }
    void tautkanBerkas(String surat,List<SuratLepasRawatBerkas.Ref> refs,String petugas,boolean boleh) throws Exception {
        cekAkses(boleh);berkas.tautkan(surat,refs,petugas);
    }
    private static boolean sama(Data a, Data b) {
        return Objects.equals(a.noSEP,b.noSEP) && Objects.equals(a.noRawat,b.noRawat)
                && Objects.equals(a.noRM,b.noRM) && Objects.equals(a.tanggal,b.tanggal)
                && Objects.equals(a.dokter,b.dokter) && Objects.equals(a.poli,b.poli);
    }
    private void status(String id, String status, String pesan) throws SQLException {
        try (Connection c=connections.open(); PreparedStatement ps=c.prepareStatement(
                "UPDATE rsaj_lepas_rawat_kirim SET status=?,pesan=?,diperbarui=NOW() WHERE id=?")) {
            ps.setString(1,status); ps.setString(2,pesan); ps.setString(3,id); ps.executeUpdate();
        }
    }
    private void terapkan(String id) throws Exception {
        try (Connection c=connections.open()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps=c.prepareStatement("SELECT * FROM rsaj_lepas_rawat_kirim WHERE id=? FOR UPDATE")) {
                ps.setString(1,id);
                try (ResultSet rs=ps.executeQuery()) {
                    if (!rs.next()) throw new Exception("Catatan pengiriman tidak ditemukan.");
                    if ("BERHASIL".equals(rs.getString("status"))) { c.rollback(); return; }
                    if (!"DIPROSES".equals(rs.getString("status")) && !"PERLU_CEK".equals(rs.getString("status")))
                        throw new Exception("Pengiriman sudah ditutup.");
                    String nomor=rs.getString("no_surat");
                    Data d=ambil(c,nomor,true);
                    LocalDate lama=tanggal(rs,"tgl_sebelum"), baru=tanggal(rs,"tgl_sesudah");
                    if (!Objects.equals(d.noSEP,rs.getString("no_sep"))
                            || !Objects.equals(d.dokter,rs.getString("kode_dokter"))
                            || !Objects.equals(d.poli,rs.getString("kode_poli"))
                            || !Objects.equals(d.tanggal,lama)) throw new Exception("Identitas/tanggal lokal sudah berubah.");
                    try (SuratLepasRawatAudit audit=new SuratLepasRawatAudit(c,rs.getString("petugas"),
                            "FORM_LEPAS_RAWAT",rs.getString("alasan"),id);
                            PreparedStatement update=c.prepareStatement("UPDATE bridging_surat_kontrol_bpjs SET tgl_rencana=? "
                                    + "WHERE no_surat=? AND tgl_rencana=?")) {
                        update.setDate(1,Date.valueOf(baru)); update.setString(2,nomor);
                        update.setDate(3,Date.valueOf(lama));
                        if (update.executeUpdate()!=1) throw new Exception("Tanggal berubah oleh petugas lain.");
                    }
                    try (PreparedStatement selesai=c.prepareStatement("UPDATE rsaj_lepas_rawat_kirim "
                            + "SET status='BERHASIL',pesan='BPJS dan SIMRS tersimpan',diperbarui=NOW() WHERE id=?")) {
                        selesai.setString(1,id); selesai.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
        }
    }

    String periksa(Data dipilih, boolean boleh) throws Exception {
        cekAkses(boleh);
        if (dipilih==null || dipilih.noSurat==null) throw new Exception("Pilih surat yang akan diperiksa.");
        String id=null, status=null, sep=null, dokter=null, poli=null;
        LocalDate lama=null, baru=null;
        long umur=0;
        try (Connection c=connections.open(); PreparedStatement ps=c.prepareStatement(
                "SELECT *,TIMESTAMPDIFF(SECOND,dibuat,NOW()) AS umur FROM rsaj_lepas_rawat_kirim "
                + "WHERE no_surat=? AND status IN ('DIPROSES','PERLU_CEK') ORDER BY dibuat DESC LIMIT 1")) {
            ps.setString(1,dipilih.noSurat);
            try (ResultSet rs=ps.executeQuery()) {
                if (rs.next()) {
                    id=rs.getString("id"); status=rs.getString("status"); sep=rs.getString("no_sep");
                    dokter=rs.getString("kode_dokter"); poli=rs.getString("kode_poli");
                    lama=tanggal(rs,"tgl_sebelum"); baru=tanggal(rs,"tgl_sesudah"); umur=rs.getLong("umur");
                }
            }
        }
        if ("DIPROSES".equals(status) && umur<120) throw new Exception("Pengiriman masih diproses. Coba periksa kembali setelah dua menit.");
        Remote remote=gateway.baca(dipilih.noSurat);
        if (id==null) {
            if (!cocok(remote,dipilih.noSurat,dipilih.noSEP,dipilih.dokter,dipilih.poli)
                    || !Objects.equals(remote.tanggal,dipilih.tanggal))
                throw new Exception("Data BPJS berbeda dari SIMRS. Gunakan Tarik VClaim pada form SKDP untuk menyesuaikan; perubahan akan diaudit.");
            return "Tanggal kontrol SIMRS dan BPJS sesuai: " + remote.tanggal+"\n"+perbaruiBerkas(dipilih.noSurat);
        }
        if (!cocok(remote,dipilih.noSurat,sep,dokter,poli))
            throw new Exception("Identitas surat/dokter/poli di BPJS berbeda. Pengiriman tetap ditandai perlu diperiksa.");
        if (Objects.equals(remote.tanggal,baru)) {
            terapkan(id);
            return "BPJS sudah menerima tanggal baru. SIMRS dan riwayat perubahan berhasil diselaraskan.\n"+perbaruiBerkas(dipilih.noSurat);
        }
        if (Objects.equals(remote.tanggal,lama)) {
            if (umur<120) throw new Exception("BPJS masih memakai tanggal sebelumnya. Tunggu dua menit sejak pengiriman, lalu periksa lagi.");
            status(id,"DITOLAK","Diperiksa ulang: BPJS masih menggunakan tanggal sebelumnya.");
            return "BPJS masih menggunakan tanggal sebelumnya. Tanggal SIMRS tetap; Anda dapat mengajukan perubahan kembali.";
        }
        throw new Exception("Tanggal BPJS berbeda dari tanggal sebelum maupun sesudah permintaan. Perlu pemeriksaan petugas BPJS.");
    }
    private static boolean cocok(Remote r, String surat, String sep, String dokter, String poli) {
        return Objects.equals(r.noSurat,surat) && Objects.equals(r.noSEP,sep)
                && Objects.equals(r.dokter,dokter) && Objects.equals(r.poli,poli);
    }

    private static final class BPJSGateway implements Gateway {
        @Override public void ubah(Data d, LocalDate tanggal, String petugas) throws Exception {
            Map<String,Object> isi=new LinkedHashMap<>();
            isi.put("noSuratKontrol",d.noSurat); isi.put("noSEP",d.noSEP);
            isi.put("kodeDokter",d.dokter); isi.put("poliKontrol",d.poli);
            isi.put("tglRencanaKontrol",tanggal.toString());
            String user=petugas.replace(" ","");
            isi.put("user",user.substring(0,Math.min(9,user.length())));
            Map<String,Object> request=new LinkedHashMap<>(); request.put("request",isi);
            kirim("/RencanaKontrol/Update",HttpMethod.PUT,new ObjectMapper().writeValueAsString(request),false);
        }
        @Override public Remote baca(String nomor) throws Exception {
            if (!nomor.matches("[A-Za-z0-9._-]{1,40}")) throw new Exception("Nomor surat tidak valid.");
            JsonNode d=kirim("/RencanaKontrol/noSuratKontrol/"+nomor,HttpMethod.GET,null,true);
            Remote r=new Remote(); r.noSurat=d.path("noSuratKontrol").asText();
            r.noSEP=d.path("sep").path("noSep").asText();
            if (r.noSEP.isEmpty()) r.noSEP=d.path("noSepAsalKontrol").asText();
            r.dokter=d.path("kodeDokter").asText(); r.poli=d.path("poliTujuan").asText();
            String tanggal=d.path("tglRencanaKontrol").asText();
            if (tanggal.length()<10) throw new Exception("Tanggal kontrol BPJS tidak lengkap.");
            r.tanggal=LocalDate.parse(tanggal.substring(0,10)); return r;
        }
        private JsonNode kirim(String path, HttpMethod method, String json, boolean decrypt) throws Exception {
            ApiBPJS api=new ApiBPJS(); ObjectMapper mapper=new ObjectMapper();
            String utc=String.valueOf(api.GetUTCdatetimeAsString());
            HttpHeaders headers=new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("X-Cons-ID",koneksiDB.CONSIDAPIBPJS()); headers.add("X-Timestamp",utc);
            headers.add("X-Signature",api.getHmac(utc)); headers.add("user_key",koneksiDB.USERKEYAPIBPJS());
            RestTemplate rest=api.getRest();
            HttpComponentsClientHttpRequestFactory factory=(HttpComponentsClientHttpRequestFactory)rest.getRequestFactory();
            try {
                org.apache.http.params.HttpConnectionParams.setConnectionTimeout(factory.getHttpClient().getParams(),10000);
                org.apache.http.params.HttpConnectionParams.setSoTimeout(factory.getHttpClient().getParams(),15000);
                JsonNode root=mapper.readTree(rest.exchange(koneksiDB.URLAPIBPJS()+path,method,
                        new HttpEntity<String>(json,headers),String.class).getBody());
                JsonNode meta=root.path("metaData");
                if (!"200".equals(meta.path("code").asText())) {
                    if (meta.path("code").asText().isEmpty()) throw new Exception("Respons BPJS tidak lengkap.");
                    throw new Ditolak(meta.path("message").asText());
                }
                return decrypt ? mapper.readTree(api.Decrypt(root.path("response").asText(),utc)) : root;
            } finally { factory.getHttpClient().getConnectionManager().shutdown(); }
        }
    }
}
